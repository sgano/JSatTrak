/*
 *  Handler to drag and drop into Object List - adds satellites and ground stations
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
 *  Shawn E. Gano 12 Dec 2007
 */

package jsattrak.utilities;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import jsattrak.objects.GroundStation;
import jsattrak.gui.JSatTrak;
import jsattrak.objects.AbstractSatellite;
import jsattrak.objects.SatelliteTleSGP4;

/**
 *
 * @author sgano
 */
public class ObjectTreeTransferHandler extends StringTransferHandler implements java.io.Serializable 
{
    private int[] indices = null;
    private int addIndex = -1; //Location where items were added
    private int addCount = 0;  //Number of items added.
        
    // hashtable reference -- used to keep track of all sats
    Hashtable<String,AbstractSatellite> satHash;
    
    Hashtable<String,GroundStation> gsHash;
    
    // parent App
    JSatTrak parentApp;
    
    // object tree nodes:
    IconTreeNode topSatTreeNode;
    IconTreeNode topGSTreeNode;
    
    // constructor
    public ObjectTreeTransferHandler(Hashtable<String,AbstractSatellite> satHash, Hashtable<String,GroundStation> gsHash, JSatTrak app, IconTreeNode topSatTreeNode, IconTreeNode topGSTreeNode)
    {
        this.satHash = satHash;
        this.gsHash = gsHash;
        this.parentApp = app;
        
        this.topSatTreeNode = topSatTreeNode;
        this.topGSTreeNode = topGSTreeNode;
        
    }
    
    //Bundle up the selected items in the list
    //as a single string, for export.
    protected String exportString(JComponent c)
    {
        String returnString = "";
        
        // nothing can be dragged out of this component
        return returnString;
    }
    
    //Take the incoming string and wherever there is a
    //newline, break it into a separate item in the list.
    protected void importString(JComponent c, String str)
    {
        //System.out.println("HERE4 " + str);
        
        JTree target = (JTree)c;
        
        DefaultTreeModel treeModel = (DefaultTreeModel)target.getModel();
        
        
        //int index = target.get.getSelectedIndex();
        
        //System.out.println("Index: " + index);
        //System.out.println("HERE");

        //Prevent the user from dropping data back on itself.
        //For example, if the user is moving items #4,#5,#6 and #7 and
        //attempts to insert the items after item #5, this would
        //be problematic when removing the original items.
        //So this is not allowed.
//        if (indices != null && index >= indices[0] - 1 &&
//              index <= indices[indices.length - 1]) 
//        {
//            indices = null;
//            return;
//        }

//        int max = listModel.getSize();
//        if (index < 0) 
//        {
//            index = max;
//        } else 
//        {
//            index++;
//            if (index > max) 
//            {
//                index = max;
//            }
//        }
//        addIndex = index;
        
        
        String[] values = str.split("\n"); // can be many objects moved at once
        addCount = values.length;
        int objImportedCount = 0;
        for (int i = 0; i < values.length; i++) 
        {
            // now we need to split again using ### to seperate names from TLE lines
            
            String[] inLines = values[i].split("###");
            
            String type = inLines[0]; // type such as SAT or GS
            String name = inLines[1];
            String inLine1 = inLines[2];
            String inLine2 = inLines[3];
            String inLine3 = "";
            if(inLines.length > 4)
            {
                inLine3  = inLines[4];
            }
            
            // if Satellite
            if(type.equalsIgnoreCase("SAT"))
            {
                                
                // see if sat already exisits, if not add to hashtable and add to list
                if (!satHash.containsKey(name))
                {
                    // is not already in list
                   
                        try
                        {
                            // add to hashTable  -- this line is the one can can throw an exception if the data is bad
                            SatelliteTleSGP4 prop = new SatelliteTleSGP4(name, inLine1, inLine2);
                            satHash.put(name, prop);

                            // propogate satellite to current date
                            prop.propogate2JulDate(parentApp.getCurrentJulTime());

                            // add item to the tree
                            //topSatTreeNode.add( new IconTreeNode(name) );
                            IconTreeNode newNode = new IconTreeNode(name);
                            treeModel.insertNodeInto(newNode, topSatTreeNode, topSatTreeNode.getChildCount());

                            newNode.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/custom/sat_icon_tle.png"))));

                            //System.out.println("node added: " + name);
                            target.scrollPathToVisible(getPath(newNode));

                            parentApp.setStatusMessage("Satellite Added to Scenario: " + name);

                            // count number of imports
                            objImportedCount++;
                        }
                        catch (Exception e)
                        {
                            System.out.println("ERROR adding satellite to scenarion, bad TLE data: " + name);
                            // TLE not valid
                            JOptionPane.showMessageDialog(parentApp, "TLE data is invalid check TLE data file or Source:" + name, "TLE ERROR", JOptionPane.ERROR_MESSAGE);
                        }
                   
                } // check if sat already in list
            } // if satellite
            else if(type.equalsIgnoreCase("GS"))
            {
                // handle GS insert into tree
                // see if sat already exisits, if not add to hashtable and add to list
                if (!gsHash.containsKey(name))
                {
                    // is not already in list
                    // add to hashTable
                    GroundStation gs = new GroundStation(name, new double[] {Double.parseDouble(inLine1), Double.parseDouble(inLine2), Double.parseDouble(inLine3)},parentApp.getCurrentJulTime());
                    gsHash.put(name, gs);
                    
                    // add item to the tree
                    //topSatTreeNode.add( new IconTreeNode(name) );
                    IconTreeNode newNode = new IconTreeNode(name);
                    treeModel.insertNodeInto(newNode, topGSTreeNode, topGSTreeNode.getChildCount());
                    
                    newNode.setIcon( new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/custom/groundStation_obj.png")) ) );
                    
                    //System.out.println("node added: " + name);
                    
                    // expand new item
                    target.scrollPathToVisible(getPath(newNode));
                    
                    parentApp.setStatusMessage("Ground Station Added to Scenario: " + name);

                    // count number of imports
                    objImportedCount++;

                } // if not in GS hash
                
            } // if Ground Station object
            
        } // for each obj dragged
        
        // update GUI if needed
        if(objImportedCount > 0)
        {
            parentApp.forceRepainting();
            
            if(objImportedCount > 1)
            {
                
                parentApp.setStatusMessage("Multiple Objects Added to Scenario: " + objImportedCount);
            }
            
        }
        
    } // import String

    //If the remove argument is true, the drop has been
    //successful and it's time to remove the selected items 
    //from the list. If the remove argument is false, it
    //was a Copy operation and the original list is left
    //intact.
    protected void cleanup(JComponent c, boolean remove) 
    {
        /*
        if (remove && indices != null)
        {
            JList source = (JList)c;
            DefaultListModel model  = (DefaultListModel)source.getModel();
            //If we are moving items around in the same list, we
            //need to adjust the indices accordingly, since those
            //after the insertion point have moved.
            if (addCount > 0)
            {
                for (int i = 0; i < indices.length; i++)
                {
                    if (indices[i] > addIndex)
                    {
                        indices[i] += addCount;
                    }
                }
            }
            for (int i = indices.length - 1; i >= 0; i--)
            {
                model.remove(indices[i]);
            }
        }
        indices = null;
        addCount = 0;
        addIndex = -1;
         */
    }
    
    
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
    
}
