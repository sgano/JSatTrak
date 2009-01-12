/*
 * JSatBrowser.java
 * =====================================================================
 * Copyright (C) 2009 Shawn E. Gano
 *
 * This file is part of JSatTrak.
 *
 * JSatTrak is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JSatTrak is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JSatTrak.  If not, see <http://www.gnu.org/licenses/>.
 * =====================================================================
 *
 * Created on Jan 11, 2009, 1:25:51 PM
 */

package jsattrak.gui;

import java.io.File;
import java.util.Hashtable;
import javax.swing.JFileChooser;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import jsattrak.utilities.CustomFileFilter;
import jsattrak.utilities.SatBrowserTleDataLoader;
import jsattrak.utilities.TLE;
import jsattrak.utilities.TLEDownloader;
import jsattrak.utilities.TreeTransferHandler;

/**
 *
 * @author sgano
 */
public class JSatBrowserInternalFrame extends javax.swing.JInternalFrame
{

    DefaultTreeModel treeModel;

    DefaultMutableTreeNode topTreeNode; // top node

    // hashtable with tle's
    private Hashtable<String,TLE> tleHash;

    java.awt.Frame parent;

    // main app
    JSatTrak app;

    // data loader
    SatBrowserTleDataLoader sbtdl;  // For now custom TLE data loaded just loads for the current broswer, after it is closed, a new browser won't have the data


    /** Creates new form JSatBrowserInternalFrame
     * @param parent
     * @param app
     */
    public JSatBrowserInternalFrame(java.awt.Frame parent, JSatTrak app)
    {
        //super("Satellite Browser",true,true,true,true);
        initComponents();

        this.app = app;
        this.parent = parent;

         // setup the tree --
        // JTree tree = new JTree(System.getProperties());
        //satTree.setModel( System.getProperties() );

        // -- Create Tree List Based on TLEDownloader values

        topTreeNode = new DefaultMutableTreeNode("Satellites");
        //createNodes(top);
        //tree = new JTree(top);

        // create a hashmap of TLEs with key as text from tree
        tleHash = new Hashtable<String,TLE>();

        treeModel = new DefaultTreeModel(topTreeNode); // create tree model using root node
        satTree.setModel(treeModel); // set the tree's model

        sbtdl = new SatBrowserTleDataLoader(parent, topTreeNode, tleHash, tleOutputTextArea, satTree);

        sbtdl.execute();

        
        // Drag and Drop Handler
        // setup transfer handler
        satTree.setTransferHandler(new TreeTransferHandler(tleHash));

        // allow mutiple selections
        satTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        // if local files exist wait for thread to finish before exiting
        TLEDownloader tleDownloader = new TLEDownloader();
        if( (new File(tleDownloader.getLocalPath()).exists()) && (new File(tleDownloader.getTleFilePath(0)).exists()) )
        {
            int i=0;
            while(!sbtdl.isDone())
            {

                try
                {
                    Thread.sleep(50); // sleep for a little bit to wait for the thread
                    //i++;
                }
                catch(Exception e)
                {
                    //e.printStackTrace();
                }
            }
            //System.out.println("trys:"+ i);
        } // wait for thread if needed
    } // contructor

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        outsidePanel = new javax.swing.JPanel();
        mainPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        satTree = new javax.swing.JTree();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        tleOutputTextArea = new javax.swing.JTextArea();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        loadCustomTLEMenuItem = new javax.swing.JMenuItem();
        createCustomSatMenuItem = new javax.swing.JMenuItem();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setTitle("Satellite Browser");

        outsidePanel.setLayout(new java.awt.BorderLayout());

        satTree.setToolTipText("Drag Satellite(s) over to Satellite List");
        satTree.setDragEnabled(true);
        satTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                satTreeValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(satTree);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 330, Short.MAX_VALUE)
        );

        tleOutputTextArea.setColumns(20);
        tleOutputTextArea.setRows(5);
        tleOutputTextArea.setToolTipText("Satellite TLE");
        jScrollPane3.setViewportView(tleOutputTextArea);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 323, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 60, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        outsidePanel.add(mainPanel, java.awt.BorderLayout.CENTER);

        getContentPane().add(outsidePanel, java.awt.BorderLayout.CENTER);

        jMenu1.setText("Options");
        jMenu1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu1ActionPerformed(evt);
            }
        });

        loadCustomTLEMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/folder-open.png"))); // NOI18N
        loadCustomTLEMenuItem.setText("Load Custom TLE Data File");
        loadCustomTLEMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadCustomTLEMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(loadCustomTLEMenuItem);

        createCustomSatMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/custom/sat_icon_cst.png"))); // NOI18N
        createCustomSatMenuItem.setText("Create a Custom Satellite");
        createCustomSatMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createCustomSatMenuItemActionPerformed(evt);
            }
        });
        jMenu1.add(createCustomSatMenuItem);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void satTreeValueChanged(javax.swing.event.TreeSelectionEvent evt)//GEN-FIRST:event_satTreeValueChanged
    {//GEN-HEADEREND:event_satTreeValueChanged
        // some other sat was selected

        if(satTree.getSelectionCount() > 0)
        {
            if( tleHash.containsKey(satTree.getLastSelectedPathComponent().toString()) )
            {
                TLE selectedTLE = tleHash.get(satTree.getLastSelectedPathComponent().toString());

                tleOutputTextArea.setText( selectedTLE.getLine1() + "\n" + selectedTLE.getLine2() );
            }
            else // clear text area
            {
                tleOutputTextArea.setText("");
            }
        } // something is selected
    }//GEN-LAST:event_satTreeValueChanged

    private void jMenu1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenu1ActionPerformed
    {//GEN-HEADEREND:event_jMenu1ActionPerformed
        //nothing?
}//GEN-LAST:event_jMenu1ActionPerformed

    private void createCustomSatMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_createCustomSatMenuItemActionPerformed
    {//GEN-HEADEREND:event_createCustomSatMenuItemActionPerformed
        app.addCustomSat(); // start custom sat dialog
}//GEN-LAST:event_createCustomSatMenuItemActionPerformed

    private void loadCustomTLEMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_loadCustomTLEMenuItemActionPerformed
    {//GEN-HEADEREND:event_loadCustomTLEMenuItemActionPerformed
       // ask user to browse for file  (.tle, .dat, .txt, other?)
        final JFileChooser fc = new JFileChooser(new TLEDownloader().getTleFilePath(0));
    	CustomFileFilter xmlFilter = new CustomFileFilter("tle","*.tle");
    	fc.addChoosableFileFilter(xmlFilter);
        xmlFilter = new CustomFileFilter("dat","*.dat");
    	fc.addChoosableFileFilter(xmlFilter);
        xmlFilter = new CustomFileFilter("txt","*.txt");
    	fc.addChoosableFileFilter(xmlFilter);


    	int returnVal = fc.showOpenDialog(this);

    	if (returnVal == JFileChooser.APPROVE_OPTION)
    	{
            File file = fc.getSelectedFile();

            Boolean result = sbtdl.loadTLEDataFile(file, "Custom", null);
            //System.out.println("Result Loading: " + result.toString());
        }
        
}//GEN-LAST:event_loadCustomTLEMenuItemActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem createCustomSatMenuItem;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JMenuItem loadCustomTLEMenuItem;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JPanel outsidePanel;
    private javax.swing.JTree satTree;
    private javax.swing.JTextArea tleOutputTextArea;
    // End of variables declaration//GEN-END:variables



    /**
     *
     * @return
     */
    public Hashtable<String, TLE> getTleHash()
    {
        return tleHash;
    }

}
