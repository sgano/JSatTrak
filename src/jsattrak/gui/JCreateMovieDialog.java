/*
 * JCreateMovieDialog.java
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
 * Created on October 25, 2007, 1:56 PM
 * 
 * update 29 April 2008: Shawn Gano
 * - fixed to allow other types of windows besides just 3d WWJ, using a "mode" variable to allow 3dWWJ, 2D, or other
 * 
 */

package jsattrak.gui;

import java.awt.Container;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;
import javax.media.MediaLocator;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import jsattrak.utilities.J3DEarthComponent;
import jsattrak.utilities.JpegImagesToMovie;
import name.gano.astro.time.Time;
import name.gano.file.SaveImageFile;

/**
 *
 * @author  sgano
 */
public class JCreateMovieDialog extends javax.swing.JDialog
{
    Time startTime, endTime;
    
    double timeStep = 1.0;
    int playbackFPS = 16; 
    
     // date formats for displaying and reading in
    private SimpleDateFormat dateformat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS z");
    private SimpleDateFormat dateformatShort1 = new SimpleDateFormat("dd MMM y H:m:s.S z");
    private SimpleDateFormat dateformatShort2 = new SimpleDateFormat("dd MMM y H:m:s z"); // no Milliseconds
     
    // reltive name of temporary dir to store files
    String tempDirStr = "temp_images";
    String rootNameTempImages = "screen_";
    
    JSatTrak app;
    
    // mode for type of window being used to make a movie from
    int movieMode = 0; // 0= 3dWWJ, 1=2D graphics, 2=other any JComponent
    // types of windows to make movies from:
    J3DEarthComponent threeDpanel;
    J2DEarthPanel     twoDpanel;
    Container         otherPanel;
    
    /** Creates new form JCreateMovieDialog for a 3D window
     * @param parent
     * @param modal
     * @param threeDpanel
     * @param app 
     */
    public JCreateMovieDialog(java.awt.Frame parent, boolean modal, J3DEarthComponent threeDpanel, JSatTrak app)
    {
        super(parent, modal);
        
        this.threeDpanel = threeDpanel;
        this.app = app;
        
        movieMode = 0; // set 3D wwj window type
        
        iniGUI( threeDpanel.getDialogTitle() );
        
    } // 3d Window movie maker
    
    /** Creates new form JCreateMovieDialog for a 2D window
     * @param parent
     * @param modal
     * @param twoDpanel 
     * @param app 
     */
    public JCreateMovieDialog(java.awt.Frame parent, boolean modal, J2DEarthPanel twoDpanel, JSatTrak app)
    {
        super(parent, modal);
        
        this.twoDpanel = twoDpanel;
        this.app = app;
        
        movieMode = 1; // set 3D wwj window type
        
        iniGUI(twoDpanel.getName());
    } // 2d window movie Maker
    
    /** Creates new form JCreateMovieDialog for an generic Container
     * @param parent
     * @param modal
     * @param otherPanel 
     * @param app
     * @param windowTitle Title of the window - to be shown in dialog so user knows which window movie will be made from
     */
    public JCreateMovieDialog(java.awt.Frame parent, boolean modal, Container otherPanel, JSatTrak app, String windowTitle)
    {
        super(parent, modal);
        
        this.otherPanel = otherPanel;
        this.app = app;
        
        movieMode = 2; // set 3D wwj window type
        
        iniGUI(windowTitle);
    } // 2d window movie Maker
    
    private void iniGUI(String windowTitle)
    {
         initComponents();
        
        // title name
        windowNameLabel.setText( windowTitle );
        
        // get current date
        // Start time
        startTime = new Time();
        
        startTime.setDateFormat( (SimpleDateFormat)app.getCurrentJulianDay().getDateFormat());//threeDpanel.getApp().getCurrentJulianDay().getDateFormat() ); 
        startTime.set( app.getCurrentJulianDay().getCurrentGregorianCalendar().getTimeInMillis()  );
        // End time
        endTime = new Time();
        endTime.setDateFormat( (SimpleDateFormat)app.getCurrentJulianDay().getDateFormat() ); 
        endTime.set( app.getCurrentJulianDay().getCurrentGregorianCalendar().getTimeInMillis()  );
        endTime.addSeconds( 60.0*60.0*3.0 ); // default 3 hours + 
        
        startTimeField.setText( startTime.getDateTimeStr());
        endTimeField.setText(endTime.getDateTimeStr());
        
        
        // time step
        timeStepField.setText( ""+app.getCurrentTimeStep() );
        timeStep = app.getCurrentTimeStep();
        
        // frames per sec default
        playBackRateSpinner.setValue(playbackFPS);
        
        // update other date
        updateDisplayData();
    } // ini GUI
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        windowNameLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        startTimeField = new javax.swing.JTextField();
        endTimeField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        timeStepField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        playBackRateSpinner = new javax.swing.JSpinner();
        jLabel7 = new javax.swing.JLabel();
        movieLengthLabel = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        numFramesLabel = new javax.swing.JLabel();
        genMovieButton = new javax.swing.JButton();
        movieStatusBar = new javax.swing.JProgressBar();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/other/applications-multimedia.png"))); // NOI18N
        jLabel1.setText("Movie Capture Tool");

        windowNameLabel.setFont(new java.awt.Font("Tahoma", 2, 11));
        windowNameLabel.setForeground(new java.awt.Color(102, 102, 102));
        windowNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        windowNameLabel.setText("name of window here");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(windowNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(windowNameLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jLabel2.setText("Start Time:");

        jLabel3.setText("Stop Time:");

        startTimeField.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                startTimeFieldActionPerformed(evt);
            }
        });

        endTimeField.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                endTimeFieldActionPerformed(evt);
            }
        });

        jLabel5.setText("Time Step [s]:");

        timeStepField.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                timeStepFieldActionPerformed(evt);
            }
        });

        jLabel6.setText("Playback Rate [FPS]:");

        playBackRateSpinner.addChangeListener(new javax.swing.event.ChangeListener()
        {
            public void stateChanged(javax.swing.event.ChangeEvent evt)
            {
                playBackRateSpinnerStateChanged(evt);
            }
        });

        jLabel7.setText("Length of Movie:");

        movieLengthLabel.setForeground(new java.awt.Color(102, 102, 102));
        movieLengthLabel.setText("hh:mm:ss.sss");

        jLabel9.setText("Number of Frames:");

        numFramesLabel.setForeground(new java.awt.Color(102, 102, 102));
        numFramesLabel.setText("nnnnn");

        genMovieButton.setText("Generate Movie");
        genMovieButton.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                genMovieButtonActionPerformed(evt);
            }
        });

        movieStatusBar.setStringPainted(true);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(movieStatusBar, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(startTimeField, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)
                            .addComponent(endTimeField, javax.swing.GroupLayout.DEFAULT_SIZE, 237, Short.MAX_VALUE)))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel5)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(timeStepField))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                            .addComponent(jLabel6)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(playBackRateSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(movieLengthLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(numFramesLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(genMovieButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(startTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(endTimeField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(30, 30, 30)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(timeStepField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(playBackRateSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(movieLengthLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(numFramesLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(genMovieButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
                .addComponent(movieStatusBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void startTimeFieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_startTimeFieldActionPerformed
    {//GEN-HEADEREND:event_startTimeFieldActionPerformed
        updateTime(startTime, startTimeField );
        
        // recalc
        updateDisplayData();
}//GEN-LAST:event_startTimeFieldActionPerformed

    private void genMovieButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_genMovieButtonActionPerformed
    {//GEN-HEADEREND:event_genMovieButtonActionPerformed
        // collect all data
        try
        {

            // update times
            updateTime(startTime, startTimeField);
            updateTime(endTime, endTimeField);

            timeStep = Double.parseDouble(timeStepField.getText());
            if (((Integer) playBackRateSpinner.getValue()).intValue() < 1)
            {
                playBackRateSpinner.setValue(1);
            }
            // save value
            playbackFPS = ((Integer) playBackRateSpinner.getValue()).intValue();

            // update calculations
            updateDisplayData();
        }
        catch (Exception e)
        {
            // display error
            JOptionPane.showMessageDialog(this, "PARAMETER ERROR : " + e.toString(), "ERROR", JOptionPane.ERROR_MESSAGE);
            return;
        } // catch
        
        // ASK WHERE TO SAVE MOVIE!!!!!!
        //    	Create a file chooser
        String outputMoviePath = "test.mov"; // default output
        
        final JFileChooser fc = new JFileChooser();
        jsattrak.utilities.CustomFileFilter movFilter = new jsattrak.utilities.CustomFileFilter("mov", "*.mov");
        fc.addChoosableFileFilter(movFilter);

        int returnVal = fc.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            File file = fc.getSelectedFile();

            String fileExtension = "mov"; // default
            if (fc.getFileFilter() == movFilter)
            {
                fileExtension = "mov";
            }

            String extension = getExtension(file);
            if (extension != null)
            {
                fileExtension = extension;
            }
            else
            {
                // append the extension
                file = new File(file.getAbsolutePath() + "." + fileExtension);
            //System.out.println("path="+file.getAbsolutePath());
            }
            
            outputMoviePath = "file:" + file.getAbsolutePath();

        }
        else
        {
            return; // don't do anything more - user canceled
        }
            
        
        
        // === CREATE temporary files ========================
        
        // create temp directory
        boolean success = (new File(tempDirStr)).mkdir();
        
        double deltaTsec = (endTime.getMJD() - startTime.getMJD())*24*60*60.0;
        final int numFrames = (int)Math.ceil( deltaTsec/timeStep );
        
        // do the long work in a thread so progress bar can be updated
        final String outputMoviePathFinal = outputMoviePath;
        SwingWorker<Object, Integer> worker = new SwingWorker<Object, Integer>()
                {
                    
                    public Vector<String> doInBackground()
                    {
                        Vector<String> inputFiles = new Vector<String>(); // array of files
                                                
                        for (int i = 0; i < numFrames; i++)
                        {
                            app.setTime(startTime.getCurrentGregorianCalendar().getTimeInMillis());

                            // force component to update
                            if(movieMode == 0)
                            {
                                threeDpanel.getWwd().redrawNow(); // // force wwd to update
                            }
                            else if(movieMode == 1)
                            {
                                // is this needed, seems no; well seems to help remove some black flickering artifacts
                                //twoDpanel.repaint();
                            }
                            else
                            {
                                // might not be needed either
                                //otherPanel.repaint(); // does this work?
                            }


                            // take screen shot
                            createScreenCapture(tempDirStr + "/" + rootNameTempImages + i + ".jpg");
                            // save file name
                            inputFiles.addElement(tempDirStr + "/" + rootNameTempImages + i + ".jpg");

                             publish((int) (100.0 * (i + 1.0) / numFrames));
                            //movieStatusBar.setValue((int) (100.0 * (i + 1.0) / numFrames));

                            // update time
                            startTime.addSeconds(timeStep);
                            
                            
                        } // create movie freames
                        
                        
                        movieStatusBar.setString("Building Movie File.....");
                        movieStatusBar.setIndeterminate(true);
                        
                        // stuff to do afterwards
                        
                         // create movie =========
                        // get size
                        int width;
                        int height;
                        if(movieMode == 0)
                        {
                            width = threeDpanel.getWwdWidth();
                            height = threeDpanel.getWwdHeight();
                        }
                        else if(movieMode == 1)
                        {
                            int[] twoDinfo = calculate2DMapSizeAndScreenLoc(twoDpanel);
                            width = twoDinfo[0];
                            height = twoDinfo[1];
                        }
                        else
                        {
                            width = otherPanel.getWidth();
                            height = otherPanel.getHeight();
                        }

                        // Generate the output media locators.
                        MediaLocator oml;

                        if ((oml = createMediaLocator(outputMoviePathFinal)) == null)
                        {
                            JOptionPane.showMessageDialog(null, "ERROR Creating Output File (check permissions)", "ERROR", JOptionPane.ERROR_MESSAGE);
                            System.err.println("Cannot build media locator from: " + outputMoviePathFinal);
                            
                            //movieStatusBar.setIndeterminate(false);
                            publish(0);
                            movieStatusBar.setString("ERROR!");
                            return inputFiles;
                        }

                        JpegImagesToMovie imageToMovie = new JpegImagesToMovie();
                        imageToMovie.doIt(width, height, playbackFPS, inputFiles, oml);

                        // clean up ============
                        boolean cleanSuccess = deleteDirectory(tempDirStr);
                        
                        //movieStatusBar.setIndeterminate(false);
                        publish(0);
                        movieStatusBar.setString("Finished!");
                        
                        return inputFiles;
                    } // doInBackground

                    // runs every once in a while to update GUI, use publish( int ) and the int will be added to the List
                    @Override
                    protected void process(List<Integer> chunks)
                    {
                        int val = chunks.get(chunks.size() - 1);

                        movieStatusBar.setValue(val);
                        movieStatusBar.repaint();

                    }

                    @Override
                    protected void done()
                    {
                        movieStatusBar.setIndeterminate(false);

                        movieStatusBar.setValue(0); // update progress bar

                        app.forceRepainting();
                    } // done -- update GUI at the finish of process
     
                   
                    
                }; // swing worker
        worker.execute();
        
        
}//GEN-LAST:event_genMovieButtonActionPerformed

    public static String getExtension(File f) 
    {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1)
        {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    } // getExtension
    
    // recursivley deletes a non-empty directory
    static public boolean deleteDirectory(String path2Dir)
    {
        File path = new File(path2Dir);
        
        if (path.exists())
        {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++)
            {
                if (files[i].isDirectory())
                {
                    deleteDirectory(files[i].toString()); // recursive delete
                }
                else
                {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    } // deleteDirectory
    
    private void playBackRateSpinnerStateChanged(javax.swing.event.ChangeEvent evt)//GEN-FIRST:event_playBackRateSpinnerStateChanged
    {//GEN-HEADEREND:event_playBackRateSpinnerStateChanged
        if( ((Integer)playBackRateSpinner.getValue()).intValue() < 1)
        {
            playBackRateSpinner.setValue(1);
        }
        
        // save value
        playbackFPS = ((Integer)playBackRateSpinner.getValue()).intValue();
                
        // update calculations
        updateDisplayData();
    }//GEN-LAST:event_playBackRateSpinnerStateChanged

    private void timeStepFieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_timeStepFieldActionPerformed
    {//GEN-HEADEREND:event_timeStepFieldActionPerformed
        timeStep = Double.parseDouble( timeStepField.getText() );
        
        // update calculations
        updateDisplayData();
    }//GEN-LAST:event_timeStepFieldActionPerformed

    private void endTimeFieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_endTimeFieldActionPerformed
    {//GEN-HEADEREND:event_endTimeFieldActionPerformed
         updateTime(endTime, endTimeField );
        
        // recalc
        updateDisplayData();
    }//GEN-LAST:event_endTimeFieldActionPerformed
    
//    /**
//     * @param args the command line arguments
//     */
//    public static void main(String args[])
//    {
//        java.awt.EventQueue.invokeLater(new Runnable()
//        {
//            public void run()
//            {
//                JCreateMovieDialog dialog = new JCreateMovieDialog(new javax.swing.JFrame(), true);
//                dialog.addWindowListener(new java.awt.event.WindowAdapter()
//                {
//                    public void windowClosing(java.awt.event.WindowEvent e)
//                    {
//                        System.exit(0);
//                    }
//                });
//                dialog.setVisible(true);
//            }
//        });
//    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField endTimeField;
    private javax.swing.JButton genMovieButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel movieLengthLabel;
    private javax.swing.JProgressBar movieStatusBar;
    private javax.swing.JLabel numFramesLabel;
    private javax.swing.JSpinner playBackRateSpinner;
    private javax.swing.JTextField startTimeField;
    private javax.swing.JTextField timeStepField;
    private javax.swing.JLabel windowNameLabel;
    // End of variables declaration//GEN-END:variables
    
    
    public void updateDisplayData()
    {
         double deltaTsec = (endTime.getMJD() - startTime.getMJD())*24*60*60.0;
         
         int numFrames = (int)Math.ceil( deltaTsec/timeStep );
         
         double clipLenSec = (numFrames +0.0)/playbackFPS;
         
         movieLengthLabel.setText("" + clipLenSec + " [sec]");
         numFramesLabel.setText(""+numFrames);
        
    } // updateDisplayData
    
    
    private void updateTime(Time time, JTextField timeTextField)
    {
        // save old time
        double prevJulDate = time.getJulianDate();

        // enter hit in date/time box...
        //System.out.println("Date Time Changed");

        GregorianCalendar currentTimeDate = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        //or
        //GregorianCalendar currentTimeDate = new GregorianCalendar();

        boolean dateAccepted = true; // assume date valid at first
        try
        {
            currentTimeDate.setTime(dateformatShort1.parse(timeTextField.getText()));
            timeTextField.setText(dateformat.format(currentTimeDate.getTime()));
        }
        catch (Exception e2)
        {
            try
            {
                // try reading without the milliseconds
                currentTimeDate.setTime(dateformatShort2.parse(timeTextField.getText()));
                timeTextField.setText(dateformat.format(currentTimeDate.getTime()));
            }
            catch (Exception e3)
            {
                // bad date input put back the old date string
                timeTextField.setText(dateformat.format(currentTimeDate.getTime()));
                dateAccepted = false;
            //System.out.println(" -- Rejected");
            } // catch 2

        } // catch 1

        if (dateAccepted)
        {
            // date entered was good...
            //System.out.println(" -- Accepted");

            // save
            time.set(currentTimeDate.getTimeInMillis());
        //            currentJulianDate.set(currentTimeDate.get(Calendar.YEAR),
//                                  currentTimeDate.get(Calendar.MONTH),
//                                  currentTimeDate.get(Calendar.DATE),
//                                  currentTimeDate.get(Calendar.HOUR_OF_DAY),
//                                  currentTimeDate.get(Calendar.MINUTE),
//                                  currentTimeDate.get(Calendar.SECOND));



        } // if date accepted
    } // updateTime
    
    public void createScreenCapture(String outputFileName)
    {
        try
        {
            //capture the whole screen
            //BufferedImage screencapture = new Robot().createScreenCapture(
            //      new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()) );

            // just the framePanel	 // viewsTabbedPane frame3d
            Point pt = new Point();
            int width = 0;
            int height = 0;

            // get location on screen / width / height
            if(movieMode == 0)
            {
                pt = threeDpanel.getWwdLocationOnScreen();
                width = threeDpanel.getWwdWidth();
                height = threeDpanel.getWwdHeight();
            }
            else if(movieMode == 1)
            {
                int[] twoDinfo = calculate2DMapSizeAndScreenLoc(twoDpanel);
                pt.setLocation(twoDinfo[2], twoDinfo[3]);
                width = twoDinfo[0];
                height = twoDinfo[1];
            }
            else
            {
                pt = otherPanel.getLocationOnScreen();
                width = otherPanel.getWidth();
                height = otherPanel.getHeight();
            }


            // not a possible size
            if (height <= 0 || width <= 0)
            {
                // no screen shot
                JOptionPane.showInternalMessageDialog(this, "A Screenshot was not possible - too small of size", "ERROR", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // full app screen shot
            //BufferedImage screencapture = new Robot().createScreenCapture(
            //			   new Rectangle( mainFrame.getX()+viewsTabbedPane.getX(), mainFrame.getY(),
            //					   viewsTabbedPane.getWidth(), mainFrame.getHeight() ) );
            // scree shot of just window
            BufferedImage screencapture = new Robot().createScreenCapture(
                    new Rectangle(pt.x, pt.y, width, height));



            // extension mov
            String fileExtension = "jpg"; // default
            
            // new file from name
            File file = new File(outputFileName);
            
            // save file
            //ImageIO.write(screencapture, fileExtension, file); // old way
            // new way - SEG - though fixed JPG compression (may want this to be an option)
            Exception e = SaveImageFile.saveImage(fileExtension, file, screencapture, 0.9f); // the last one is the compression quality (1=best)
            if(e != null)
            {
                System.out.println("ERROR SCREEN CAPTURE:" + e.toString());
                return;
            }


        }
        catch (Exception e4)
        {
            System.out.println("ERROR SCREEN CAPTURE:" + e4.toString());
        }
    } // createScreenCapture
        
    /**
     * Create a media locator from the given string.
     */
    static MediaLocator createMediaLocator(String url) {

	MediaLocator ml;

	if (url.indexOf(":") > 0 && (ml = new MediaLocator(url)) != null)
	    return ml;

	if (url.startsWith(File.separator)) {
	    if ((ml = new MediaLocator("file:" + url)) != null)
		return ml;
	} else {
	    String file = "file:" + System.getProperty("user.dir") + File.separator + url;
	    if ((ml = new MediaLocator(file)) != null)
		return ml;
	}

	return null;
    } // createMediaLocator
    
    
    // calculate actualy 2D map size and location on screen
    // returns int[] {width,height,pt.x,pt.y} pt = point on screen
    private int[] calculate2DMapSizeAndScreenLoc(J2DEarthPanel twoDmapPanel)
    {
        Point pt = twoDmapPanel.getLocationOnScreen();
        int width = twoDmapPanel.getWidth();
        int height = twoDmapPanel.getHeight();

        //System.out.println("2D WINDOW");
        double aspectRatio = 2.0; // width/height

        int newWidth = 1, newHeight = 1;
        if (height != 0)
        {
            if (width / height > aspectRatio)
            {
                // label has larger aspect ratio, constraint by height
                newHeight = height;
                newWidth = (int) (height * aspectRatio);
            }
            else
            {
                // label has lower aspect ratio
                newWidth = width;
                newHeight = (int) (width * 1.0 / aspectRatio);
            }

            // changes in size
            int deltaW = width - newWidth;
            int deltaH = height - newHeight;

            pt.y = pt.y + (int) (deltaH / 2.0);
            pt.x = pt.x + (int) (deltaW / 2.0);

            width = newWidth;
            height = newHeight;
        }// find scale
        
        return new int[] {width,height,pt.x,pt.y};
 
    } // calculate2DMapSizeAndScreenLoc
    
}
