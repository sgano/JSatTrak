/*
 * ProgressBarWorker.java
 *=====================================================================
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
 * Created on August 22, 2007, 1:23 PM
 *
 * updated 23 MArch 2009 - made thread safe
 *
 */

package jsattrak.utilities;

import jsattrak.gui.JProgressDialog;
import jsattrak.gui.LoadTleDirectDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import jsattrak.gui.JSatTrak;
import name.gano.file.IOFileFilter;

/**
 *
 * @author sgano
 */
public class SatBrowserTleDataLoader extends SwingWorker<Boolean,ProgressStatus>
{
    //JProgressDialog jProgDialog = null; // SEG - 22 March 2009 is this needed?
    //boolean dialogCreated = false; // SEG - 22 March 2009 is this needed?
    
    //JProgressDialog dialog;
    
    boolean notDone = true; // not done yet
    
    //
    DefaultMutableTreeNode topTreeNode;
    private Hashtable<String,TLE> tleHash;
    JSatTrak parentComponent;
    JTextArea tleOutputTextArea;
    JTree satTree;

    // node hash
    Hashtable<String,DefaultMutableTreeNode> mainNodesHash;
    Hashtable<String,DefaultMutableTreeNode> secondaryNodesHash;

    // path to local user supplied TLE data files
    static String usrTLEpath = "data/tle_user";

    private JProgressDialog dialog = null;

    boolean loadTLEfromWeb = false;
    TLEDownloader tleDownloader;
    boolean userSelectedNoToDownload = false;
    int satCount = 0;
    
    
    /** Creates a new instance of ProgressBarWorker
     * @param parentComponent
     * @param topTreeNode
     * @param tleHash
     * @param tleOutputTextArea
     * @param satTree 
     */
    public SatBrowserTleDataLoader(JSatTrak parentComponent, DefaultMutableTreeNode topTreeNode, Hashtable<String,TLE> tleHash, JTextArea tleOutputTextArea, JTree satTree)
    {
        this.topTreeNode = topTreeNode;
        this.tleHash = tleHash;
        this.parentComponent = parentComponent;
        this.tleOutputTextArea = tleOutputTextArea;
        this.satTree = satTree;

        // do checks to see if data storage dir exsists and determine if this is webstart
      // Check to see if TLE directory exisits if not ask user if they want to download them from the web
        // no Local TLE were found would you like to download them directly from the web?

        tleDownloader = new TLEDownloader();

        loadTLEfromWeb = false;
        //ProgressBarWorker worker = null;
        //dialog  = new JProgressDialog(parentComponent, true);
        if(parentComponent == null)
        {
            System.out.println("2 JProgress Dialog Parent == NULL" );
        }

        // SEG - 22 MArch 2009 - don't create unless needed
        //final JProgressDialog dialog = new JProgressDialog(parentComponent, false);

        if( !(new File(tleDownloader.getLocalPath()).exists()) ||  !(new File(tleDownloader.getTleFilePath(0)).exists()) )
        {
            // ask user if they want to load them from the web: (and tab for proxy options)
            // tell them it may take longer to open
            LoadTleDirectDialog dlg = new LoadTleDirectDialog(parentComponent, true); // modal so it waits for the user to respond
            dlg.setVisible(true);


            // if they want to load them from the web:
            if(dlg.isWasYesSelected())
            {
                loadTLEfromWeb = true; // must load them

                // maybe pop open a progress bar here??
                // create a swing worker thread to download data from
                //worker = new ProgressBarWorker(parent);
                //worker.execute();
                dialog = new JProgressDialog(parentComponent, false);
                dialog.setVisible(true);



            }
            else
            {
                loadTLEfromWeb = true; // must load them (but user said no)
                userSelectedNoToDownload = true;
                //return new Boolean(result); // return as if nothing was wrong but no sat browswer will be shown
            }

        } // TLE files exisit?
        
    } // SatBrowserTleDataLoader
    

    /**
     * Add all of the TLE data files to the satellite browser
     * @return
     */
    @Override
    public Boolean doInBackground()
    {
        boolean result = true;
        
        //dialogCreated = true; // SEG - 22 March 2009 is this needed?
       
        //=================================================================================================
        
        
        //TLEDownloader tleDownloader = new TLEDownloader();
        
        // create a hashmap of top level nodes
        mainNodesHash = new Hashtable<String,DefaultMutableTreeNode>();
        secondaryNodesHash = new Hashtable<String,DefaultMutableTreeNode>();
    
        // iterate for each primary category, only create node when it is different than item before
        
        // current main node we are working with
        DefaultMutableTreeNode currentMainNode = topTreeNode;
        DefaultMutableTreeNode currentSecondaryNode;
        
        // current TLE
        TLE currentTLE = null;
        
        satCount = 0;
        
        /////////////////////////////////////////////////////////
        // if data file dir doesn't exist and the user said no to downloading them then exit
       if(loadTLEfromWeb && userSelectedNoToDownload )
       {
                return new Boolean(result); // return as if nothing was wrong but no sat browswer will be shown
       }
        ///////////////////////////////////////////////

        

                
        // Assumes each satellite TLE has a unique name
        for(int i=0; i<tleDownloader.fileNames.length; i++)
        {
            
            // see if the primary category exisits
            if(mainNodesHash.containsKey(tleDownloader.primCat[i]))
            {
                // do not add another main, get the already exisiting node
                currentMainNode = mainNodesHash.get(tleDownloader.primCat[i]);
            }
            else
            {
                // create a new main node and add it to the hashmap
                currentMainNode = new DefaultMutableTreeNode(tleDownloader.primCat[i]);
                mainNodesHash.put(tleDownloader.primCat[i],currentMainNode);
                // add node to the top node
                topTreeNode.add(currentMainNode);
            }
            
            // okay, now add a new secondary node to the main node (assume it is not already there, files are uniquly named)
            currentSecondaryNode = new DefaultMutableTreeNode(tleDownloader.secondCat[i]);
            currentMainNode.add(currentSecondaryNode);
            secondaryNodesHash.put(tleDownloader.secondCat[i],currentSecondaryNode); // save to keep track
            
                        
            // now parse through the textfile and create a TLE for each entry
            // add each TLE to the hashmap and to the JTree
            try
            {
                BufferedReader tleReader = null; // initalization of reader (either local or web)
                
                if(!loadTLEfromWeb)
                {
                    // read local file
                    File tleFile = new File(tleDownloader.getTleFilePath(i));
                    FileReader tleFileReader = new FileReader(tleFile);
                    tleReader = new BufferedReader(tleFileReader); // from local file
                }
                else
                {
                    // does this work??
                    if(!dialog.isVisible()) // if progress bar is close cancel this operation
                    {
                        result = false;
                        return new Boolean(result);
                    }
                    
                    // read from web
                    URL url = new URL(tleDownloader.getTleWebPath(i));
                    URLConnection c = url.openConnection();
                    InputStreamReader isr = new InputStreamReader(c.getInputStream());
                    tleReader = new BufferedReader(isr); // from the web
                    
                    // update progress?
                    publish( new ProgressStatus((int) Math.round( (i*100.0)/ tleDownloader.fileNames.length), tleDownloader.fileNames[i]) );
//                    dialog.setProgress( (int) Math.round( (i*100.0)/ tleDownloader.fileNames.length) );
//                    dialog.repaint();
//                    dialog.setStatusText(tleDownloader.fileNames[i]);
                }
                
                String nextLine = null;
                
                while( (nextLine = tleReader.readLine()) != null)
                {
                    // needs three lines
                    currentTLE = new TLE(nextLine,tleReader.readLine(),tleReader.readLine());
                    
                    // save TLE
                    tleHash.put(currentTLE.getSatName(),currentTLE);
                    
                    // add to tree
                    currentSecondaryNode.add( new DefaultMutableTreeNode(currentTLE.getSatName()) );
                    
                    satCount++;
                }// while there are more lines to read

                tleReader.close(); // close file
                                
            }
            catch(Exception e)
            {
                // print out error
                System.out.println("ERROR IN TLE READING POSSIBLE FILE FORMAT OR MISSING TLE FILES:" + e.toString());
                //e.printStackTrace();
                // the next line cause the app to hang
                //JOptionPane.showMessageDialog(parentComponent, "Error Loading Satellite TLE Data. Try updating data.\n"+e.toString(), "TLE LOADING ERROR", JOptionPane.ERROR_MESSAGE);
                
                result = false;
                return new Boolean(result); // quit, so user doesn't get tons of error messages
            }
     
        } // for each primary category

        // Check for user supplied TLE data files
        // use: public Boolean loadTLEDataFile(File tleFile, String primaryCategory, String secondaryCategory)
        try
        {
            // find files in tle_user directory , usrTLEpath
            File userTLdir = new File(usrTLEpath);
            if(userTLdir.isDirectory())
            {
                IOFileFilter tleFilter = new IOFileFilter("txt","tle","dat");

                File[] tleFiles = userTLdir.listFiles(tleFilter);

                for(File f : tleFiles)
                {
                    String fn = f.getName();
                    Boolean r = loadTLEDataFile(f, "Custom", fn.substring(0, fn.length()-4), false);

                    if(!r)
                    {
                        System.out.println("Error loading TLE file: " + f.getCanonicalPath());
                    }
                }

            } // is directory
            else
            {
                System.out.println("ERROR: User TLE folder path is not a directory.");
            }
        }
        catch(Exception e)
        {
            System.out.println("Error loading user supplied TLE data files:" + e.toString());
        } // end of trying to load user supplied TLE files

       
        
        //=================================================================================================
        
        //System.out.println("I am done");
        
        return new Boolean(result);
        
    } // do in background

    // runs every once in a while to update GUI, use publish( int ) and the int will be added to the List
    @Override
    protected void process(List<ProgressStatus> chunks)
    {
        if(dialog == null)
        {
            dialog = new JProgressDialog(parentComponent, false);
        }

        ProgressStatus ps = chunks.get(chunks.size() - 1);
        //int val = (int) (); //get the last elelemtn in the list
        //System.out.println(ps.getPercentComplete()+"");
        dialog.setProgress( ps.getPercentComplete() );
        dialog.repaint();
        dialog.setStatusText("Downloading File: " + ps.getStatusText());

    }

     @Override
    protected void done()
    {
        if(loadTLEfromWeb)
        {
            dialog.setVisible(false); // close progress dialog
        }

        // display number of satellites in list
        tleOutputTextArea.setText("Number of Satellites in list: " + satCount);

        // auto expand root node
        satTree.expandRow(0);
    } // done


// SEG - 22 March 2009 is this needed?
//    public void setProg(int percent)
//    {
//        jProgDialog.setProgress(percent);
//    }


    // =========================================================

    // SEG - 11 Jan 2009 - so custom files can be loaded

    /**
     * loads a given file and adds the TLE's to the sat Browser and auto expands the new addition
     * @param tleFile file containing TLE's (3line format, (1) name, (2) TLE line 1 (3) line 2
     * @param primaryCategory Primary list category to display the satellites under
     * @param secondaryCategory secondary category to display the satellites under (can be null)
     * @return if loading of TLE file was successful
     */
    public Boolean loadTLEDataFile(File tleFile, String primaryCategory, String secondaryCategory)
    {
        return loadTLEDataFile(tleFile,primaryCategory,secondaryCategory, true);
    }

    /**
     * loads a given file and adds the TLE's to the sat Browser
     * @param tleFile file containing TLE's (3line format, (1) name, (2) TLE line 1 (3) line 2
     * @param primaryCategory Primary list category to display the satellites under
     * @param secondaryCategory secondary category to display the satellites under (can be null)
     * @param autoExpandSelectNotify auto expand and select the added TLE datafile, also notify user with a dialog box if error
     * @return if loading of TLE file was successful
     */
    public Boolean loadTLEDataFile(File tleFile, String primaryCategory, String secondaryCategory, boolean autoExpandSelectNotify)
    {
        boolean result = true;
        int newSatCount = 0;
        boolean customCategoriesInFile = false; // default if they are found this is set to true

        // current TLE
        TLE currentTLE = null;

        // check if file has a custom primary and secondary category listed in the file
        // if it does it will overwrite the given categories
        try
        {
            BufferedReader tleReader = null; // initalization of reader (either local or web)

            FileReader tleFileReader = new FileReader(tleFile);
            tleReader = new BufferedReader(tleFileReader); // from local file


            String nextLine = tleReader.readLine();

            // check if first line contains category names
            if(nextLine.startsWith("##main="))
            {
                // format:
                //##main=main heading here,sub=sub heading here
                String[] data1 = nextLine.split("=");
                // change category names (will change object passed in too)
                String pri = data1[1].split(",")[0].trim();
                String sec = data1[2].trim();
                
                // make sure categories are defined, if they are flag and save them
                if(pri != null && sec != null)
                {
                    primaryCategory = pri;
                    secondaryCategory = sec;

                    // check for NULL name
                    if(secondaryCategory.equalsIgnoreCase("NULL"))
                    {
                        secondaryCategory = null;
                    }

                    customCategoriesInFile = true;
                }
            }

            tleReader.close();

        }
        catch(Exception e)
        {
            // do nothing ignore custom labels
        }

        DefaultMutableTreeNode currentMainNode = topTreeNode;
        DefaultMutableTreeNode currentSecondaryNode;

        // see if the primary category exisits
        if(mainNodesHash.containsKey(primaryCategory))
        {
            // do not add another main, get the already exisiting node
            currentMainNode = mainNodesHash.get(primaryCategory);
        }
        else
        {
            // create a new main node and add it to the hashmap
            currentMainNode = new DefaultMutableTreeNode(primaryCategory);
            mainNodesHash.put(primaryCategory, currentMainNode);
            // add node to the top node
            topTreeNode.add(currentMainNode);
        }


        // see if the secondary category exisits
        if(secondaryCategory != null)
        {
            if(secondaryNodesHash.containsKey(secondaryCategory))
            {
                // do not add another main, get the already exisiting node
                currentSecondaryNode = secondaryNodesHash.get(secondaryCategory);
            }
            else
            {
                // create a new main node and add it to the hashmap
                currentSecondaryNode = new DefaultMutableTreeNode(secondaryCategory);
                secondaryNodesHash.put(secondaryCategory, currentSecondaryNode);
                // add node to the top node
                currentMainNode.add(currentSecondaryNode);
            }
        }
        else // secondary categort is null
        {
            currentSecondaryNode = currentMainNode; // just point to main node
        }

        // now parse through the textfile and create a TLE for each entry
        // add each TLE to the hashmap and to the JTree
        try
        {
            BufferedReader tleReader = null; // initalization of reader (either local or web)

            FileReader tleFileReader = new FileReader(tleFile);
            tleReader = new BufferedReader(tleFileReader); // from local file


            String nextLine = null;

            if(customCategoriesInFile)
            {
                // read first line and dispose, since it contains category names and not TLE data
                nextLine = tleReader.readLine();
            }

            while((nextLine = tleReader.readLine()) != null)
            {
                // needs three lines
                currentTLE = new TLE(nextLine, tleReader.readLine(), tleReader.readLine());

                // save TLE
                tleHash.put(currentTLE.getSatName(), currentTLE);

                // add to tree
                currentSecondaryNode.add(new DefaultMutableTreeNode(currentTLE.getSatName()));

                newSatCount++;
            }// while there are more lines to read

            tleReader.close(); // close file

        }
        catch(Exception e)
        {

            // print out error
            System.out.println("ERROR IN TLE READING- bad format/missing file/permissions:" + e.toString());
            e.printStackTrace();
            // the next line cause the app to hang
            //JOptionPane.showMessageDialog(parentComponent, "Error Loading Satellite TLE Data. Try updating data.\n"+e.toString(), "TLE LOADING ERROR", JOptionPane.ERROR_MESSAGE);

            if(autoExpandSelectNotify) // if we should notify
            {
                try
                {
                    JOptionPane.showMessageDialog(parentComponent, "Error Loading TLE Data File, bad permissions or file format: \n" + tleFile.getCanonicalPath().toString() + "\n" + e.toString(), "TLE LOADING ERROR", JOptionPane.ERROR_MESSAGE);
                }
                catch(Exception ee)
                {
                    JOptionPane.showMessageDialog(parentComponent, "Error Loading TLE Data File, bad permissions or file format: \n" + e.toString(), "TLE LOADING ERROR", JOptionPane.ERROR_MESSAGE);
                }
            } // if we should try notification


            result = false;
            return new Boolean(result); // quit, so user doesn't get tons of error messages
        }


        // tell tree that the data has been updated!
        ((DefaultTreeModel)satTree.getModel()).reload();

        if(autoExpandSelectNotify)
        {
            // auto expand new Node and select it!
            satTree.expandPath( getTreePath(currentSecondaryNode) ); // currentSecondaryNode
            satTree.getSelectionModel().setSelectionPath( getTreePath(currentSecondaryNode) );
        }

        // display number of satellites in list
        tleOutputTextArea.setText("Number of New Satellites add to list: " + newSatCount);

        // append this info to the log
        try
        {
            System.out.println("Custom TLE data loaded from file: (" +primaryCategory+","+secondaryCategory+")" + tleFile.getCanonicalPath().toString());
        } catch(Exception e) {}

        //satTree.repaint();

        return new Boolean(result); // quit, so user doesn't get tons of error messages
         
    } // loadTLEDataFile

    // Returns a TreePath containing the specified node.
    public TreePath getTreePath(TreeNode node)
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
