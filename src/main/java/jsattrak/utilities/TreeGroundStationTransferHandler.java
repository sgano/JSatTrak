/*
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
 */

package jsattrak.utilities;

import java.util.Hashtable;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.tree.TreePath;

/**
 *
 * @author sgano
 */
public class TreeGroundStationTransferHandler  extends StringTransferHandler implements java.io.Serializable
{
     private int[] indices = null;
    private int addIndex = -1; //Location where items were added
    private int addCount = 0;  //Number of items added.
    
    private Hashtable<String,double[]> gsHash;
    
    // constructor
    public TreeGroundStationTransferHandler(Hashtable<String,double[]> gsHash)
    {
        this.gsHash = gsHash;
        
     }
    
    //Bundle up the selected items in the list
    //as a single string, for export.
    protected String exportString(JComponent c)
    {
        //System.out.println("HERE");
        
        JTree satTree = (JTree)c;
        
        String returnString = "";
        
        // allow for drag and drop of multiple sats at once
        TreePath[] treePaths = satTree.getSelectionPaths();
        //int[] selectionRows = satTree.getSelectionRows();
        
        for(int i=0; i<treePaths.length;i++)
        {
            
            String currentGSName = treePaths[i].getLastPathComponent().toString();
            //System.out.println("Name: " + currentSatName);
            
            // only transfer items that are satellites
            if( gsHash.containsKey(currentGSName) )
            {
                                
                // get the name part of the string with type GS added to the front
                returnString += "GS###" + currentGSName;
                
                double[] lla = gsHash.get(currentGSName);
                
                returnString += ( "###" + lla[0] );
                returnString += ( "###" + lla[1] );
                returnString += ( "###" + lla[2] + "\n" );
                
                //tleOutputTextArea.setText( selectedTLE.getLine1() + "\n" + selectedTLE.getLine2() );
            }
            else // do nothing -- not a satellite in hashtable
            {
                
            }
            
        }// each row selected
        
        // return string to drag/drop
        return returnString;
    }
    
    //Take the incoming string and wherever there is a
    //newline, break it into a separate item in the list.
    protected void importString(JComponent c, String str)
    {
        /*
        //System.out.println("HERE!!");
         
        JList target = (JList)c;
         
        DefaultListModel listModel = (DefaultListModel)target.getModel();
        //System.out.println("HERE4");
        int index = target.getSelectedIndex();
         
        //System.out.println("Index: " + index);
        //System.out.println("HERE");
         
        //Prevent the user from dropping data back on itself.
        //For example, if the user is moving items #4,#5,#6 and #7 and
        //attempts to insert the items after item #5, this would
        //be problematic when removing the original items.
        //So this is not allowed.
        if (indices != null && index >= indices[0] - 1 &&
              index <= indices[indices.length - 1])
        {
            indices = null;
            return;
        }
         
        int max = listModel.getSize();
        if (index < 0)
        {
            index = max;
        } else
        {
            index++;
            if (index > max)
            {
                index = max;
            }
        }
        addIndex = index;
        String[] values = str.split("\n");
        addCount = values.length;
        for (int i = 0; i < values.length; i++)
        {
            listModel.add(index++, values[i]);
        }
         */
    }

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
}
