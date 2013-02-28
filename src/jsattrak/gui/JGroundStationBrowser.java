/*
 * JGroundStationBrowser.java
 * =====================================================================
 *   This file is part of JSatTrak.
 *
 *   Copyright 2007-2013 Shawn E. Gano
 *   
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * =====================================================================
 *
 * Created on December 12, 2007, 2:14 PM
 */

package jsattrak.gui;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import jsattrak.utilities.TreeGroundStationTransferHandler;

/**
 *
 * @author  sgano
 */
public class JGroundStationBrowser extends javax.swing.JPanel implements java.io.Serializable
{
    public static String groundStationDB = "data/groundstations/groundstations_db.csv";
    public static String groundStationDir = "data/groundstations/";
    public static String groundStationCustomDB = "data/groundstations/groundstations_custom.csv";
    
    DefaultTreeModel treeModel;
    DefaultMutableTreeNode topTreeNode; // top node
    DefaultMutableTreeNode customSecondaryNode;
    
    Hashtable<String,double []> gsHash = new Hashtable<String, double[]>(); // stores Ground Station name and LLA info [deg,deg,meters]
    
    Frame parent;
    
    /** Creates new form JGroundStationBrowser */
    public JGroundStationBrowser(Frame parent)
    {
        this.parent = parent;
        
        initComponents();
        
        // top tree node
        topTreeNode = new DefaultMutableTreeNode("Ground Stations");
        treeModel = new DefaultTreeModel(topTreeNode); // create tree model using root node
        groundStationTree.setModel(treeModel); // set the tree's model
        
        String currentSecondaryNodeName = null;
        DefaultMutableTreeNode currentSecondaryNode = null; //new DefaultMutableTreeNode(tleDownloader.secondCat[i]);
        
        
        // read data files for Ground Stations (standard and custom)
        try
        {
            BufferedReader gsReader = null; // initalization of reader 
            
            //see if local file exists, if not stream from web
            
            // read local file
            if( new File(groundStationDB).exists())
            {
                File gsFile = new File(groundStationDB);
                FileReader gsFileReader = new FileReader(gsFile);
                gsReader = new BufferedReader(gsFileReader); // from local file
            }
            else
            {
                // read from web
                URL url = new URL("http://www.gano.name/shawn/JSatTrak/" + groundStationDB);
                URLConnection c = url.openConnection();
                InputStreamReader isr = new InputStreamReader(c.getInputStream());
                gsReader = new BufferedReader(isr); // from the web
            }
            
            String nextLine = null;
            int gsCount = 0; // count of stations loaded
            while ((nextLine = gsReader.readLine()) != null)
            {
                // split line into parts
                String[] elements = nextLine.split(",");
                
                if (elements.length == 5) // if the row is formatted correctly
                {
                    String network = elements[0];
                    String stationName = elements[1];
                    double stationLat = Double.parseDouble(elements[2]);
                    double stationLon = Double.parseDouble(elements[3]);
                    double stationAlt = Double.parseDouble(elements[4]);

                    //System.out.println("" + gsCount + " : " + stationName + ", " + stationLat+ ", " + stationLon + ", " + stationAlt);

                    // save ground station to hash
                    gsHash.put(stationName, new double[]{stationLat, stationLon, stationAlt});

                    // check to see if we are still in same Secondary node if not create new one
                    if (!network.equalsIgnoreCase(currentSecondaryNodeName))
                    {
                        // create new secondary node and add it to the tree
                        currentSecondaryNode = new DefaultMutableTreeNode(network);
                        topTreeNode.add(currentSecondaryNode);
                        currentSecondaryNodeName = network;
                    }

                    // add new Ground station to the node
                    currentSecondaryNode.add(new DefaultMutableTreeNode(stationName));

                    gsCount++;
                }
            }// while there are more lines to read
            gsReader.close();
            
            // now load custom Ground Stations
            if (new File(groundStationCustomDB).exists())
            {
                File gsFile = new File(groundStationCustomDB);
                FileReader gsFileReader = new FileReader(gsFile);
                gsReader = new BufferedReader(gsFileReader); // from local file

                customSecondaryNode = new DefaultMutableTreeNode("Custom");
                topTreeNode.add(customSecondaryNode);
                while ((nextLine = gsReader.readLine()) != null)
                {
                    // split line into parts
                    String[] elements = nextLine.split(",");

                    if (elements.length == 5) // if the row is formatted correctly
                    {
                        String network = elements[0]; // ignored for custom
                        String stationName = elements[1];
                        double stationLat = Double.parseDouble(elements[2]);
                        double stationLon = Double.parseDouble(elements[3]);
                        double stationAlt = Double.parseDouble(elements[4]);

                        //System.out.println("" + gsCount + " : " + stationName + ", " + stationLat+ ", " + stationLon + ", " + stationAlt);

                        // save ground station to hash
                        gsHash.put(stationName, new double[]{stationLat, stationLon, stationAlt});

                        // add new Ground station to the node
                        customSecondaryNode.add(new DefaultMutableTreeNode(stationName));

                        gsCount++;
                    }
                }// while there are more lines to read
                gsReader.close();
            } // if custom file exists
            else
            {
                // add blank Custom node
                customSecondaryNode = new DefaultMutableTreeNode("Custom");
                topTreeNode.add(customSecondaryNode);
            }
            
            // add text to bottom
            statusTextField.setText("Total Ground Stations loaded: " + gsCount);
            
            // auto expand root node
            groundStationTree.expandRow(0);
        
        }
        catch (Exception e)
        {
             System.out.println("ERROR IN GROUND STATION READING POSSIBLE FILE FORMAT OR MISSING FILES:");
                e.printStackTrace();
                JOptionPane.showMessageDialog(parent, "Error Loading Ground Station Data. Check data.\n"+e.toString(), "Data LOADING ERROR", JOptionPane.ERROR_MESSAGE);
        }
        
        
        // Drag and Drop Handler
        // setup transfer handler
        //must also enable drag in IDE gui for tree
        groundStationTree.setTransferHandler(new TreeGroundStationTransferHandler(gsHash));
        
        // allow mutiple selections
        groundStationTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        
        
    } // constructor
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        groundStationTree = new javax.swing.JTree();
        statusTextField = new javax.swing.JTextField();
        addGSButton = new javax.swing.JButton();

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 14));
        jLabel1.setText("Ground Station Browser");

        groundStationTree.setDragEnabled(true);
        groundStationTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener()
        {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt)
            {
                groundStationTreeValueChanged(evt);
            }
        });
        groundStationTree.addPropertyChangeListener(new java.beans.PropertyChangeListener()
        {
            public void propertyChange(java.beans.PropertyChangeEvent evt)
            {
                groundStationTreePropertyChange(evt);
            }
        });
        jScrollPane1.setViewportView(groundStationTree);

        statusTextField.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                statusTextFieldActionPerformed(evt);
            }
        });

        addGSButton.setText("+");
        addGSButton.setToolTipText("Add Custom Ground Station");
        addGSButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                addGSButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 52, Short.MAX_VALUE)
                .addComponent(addGSButton))
            .addComponent(statusTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(addGSButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 329, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void statusTextFieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_statusTextFieldActionPerformed
    {//GEN-HEADEREND:event_statusTextFieldActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_statusTextFieldActionPerformed

    private void groundStationTreePropertyChange(java.beans.PropertyChangeEvent evt)//GEN-FIRST:event_groundStationTreePropertyChange
    {//GEN-HEADEREND:event_groundStationTreePropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_groundStationTreePropertyChange

    private void groundStationTreeValueChanged(javax.swing.event.TreeSelectionEvent evt)//GEN-FIRST:event_groundStationTreeValueChanged
    {//GEN-HEADEREND:event_groundStationTreeValueChanged
        if(groundStationTree.getSelectionCount() > 0)
        {
            if( gsHash.containsKey(groundStationTree.getLastSelectedPathComponent().toString()) )
            {
                double[] lla = gsHash.get(groundStationTree.getLastSelectedPathComponent().toString());
                
                statusTextField.setText( "Lat:" + lla[0] + ", Lon:" + lla[1] +", Alt[m]:" + lla[2] );
            }
            else // clear text area
            {
                statusTextField.setText("");
            }
        } // something is selected
    }//GEN-LAST:event_groundStationTreeValueChanged

    private void addGSButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addGSButtonActionPerformed
    {//GEN-HEADEREND:event_addGSButtonActionPerformed
        JAddGroundStationDialog dlg = new JAddGroundStationDialog(parent,true);
        dlg.setVisible(true); // show dialog
        
        if(dlg.isOkHit())
        {
            // okay was hit process data
            String network = dlg.getNetwork();
            String siteName = dlg.getSiteName();
            double latitude = dlg.getLatitude();
            double longitude = dlg.getLongitude();
            double altitude = dlg.getAltitude();
            boolean saveData = dlg.isSaveData();
            
            // save ground station to hash
            gsHash.put(siteName, new double[]{latitude, longitude, altitude});

            // add new Ground station to the node
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(siteName);
            customSecondaryNode.add(newNode);
            
            groundStationTree.repaint(); // refreseh tree?
            
            // expand custom
           groundStationTree.scrollPathToVisible(getPath(newNode));
            
            if (saveData) // append data to file
            {
                try
                {
                    File gsFile = new File(groundStationCustomDB);
                    boolean append = true;
                    
                    // if file doesn't exist create a new one
                    if(!gsFile.exists()) 
                    {
                        (new File(groundStationDir)).mkdirs();
                        gsFile.createNewFile();
                        
                        append = false;
                    } // create new file
                    
                    FileWriter gsFileWriter = new FileWriter(gsFile,append);
                    BufferedWriter gsWriter = new BufferedWriter(gsFileWriter); // from local file

                    gsWriter.write("\n" + network + "," + siteName + "," + latitude + "," + longitude + "," + altitude);

                    gsWriter.close();

                }
                catch (Exception e)
                {
                    System.out.println("ERROR SAVING GROUND STATION - Check permissions or format:");
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(parent, "Error Saving Ground Station Data. Check data and permissions. \n" + e.toString(), "Data SAVING ERROR", JOptionPane.ERROR_MESSAGE);
                }
                
            } // if save data
           
        } // if okay hit
        
}//GEN-LAST:event_addGSButtonActionPerformed
    
    
    // Returns a TreePath containing the specified node.
    public TreePath getPath(TreeNode node)
    {
        List<TreeNode> list = new ArrayList<TreeNode>();

        // Add all nodes to list
        while (node != null)
        {
            list.add(node);
            node = node.getParent();
        }
        Collections.reverse(list);

        // Convert array of nodes to TreePath
        return new TreePath(list.toArray());
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addGSButton;
    private javax.swing.JTree groundStationTree;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField statusTextField;
    // End of variables declaration//GEN-END:variables
    
}
