/*
 * JTrackingPanel.java
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
 * Created on December 16, 2007, 6:38 PM
 */

package jsattrak.gui;

import jsattrak.objects.GroundStation;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.Hashtable;
import javax.swing.JFileChooser;
import javax.swing.JTable.PrintMode;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import jsattrak.objects.AbstractSatellite;
import jsattrak.utilities.CustomFileFilter;
import name.gano.astro.AER;
import name.gano.astro.AstroConst;
import name.gano.astro.MathUtils;
import name.gano.astro.bodies.Sun;
import name.gano.astro.coordinates.CoordinateConversion;
import name.gano.astro.time.Time;
import name.gano.file.FileUtilities;

/**
 *
 * @author  Shawn
 */
public class JTrackingPanel extends javax.swing.JPanel
{
    
    Hashtable<String,AbstractSatellite> satHash;
    Hashtable<String,GroundStation> gsHash;
    
    // data to tell if Lead/Lag data should be updated
    double oldLeadX = -1;
    double oldLagX = -1;
    
    String timeAsString;
    
    // current time object - passed for use in pass predictions
    Time currentJulianDate;
        
    // table 
    DefaultTableModel passTableModel;
    
    // app
    JSatTrak app;
    
    // hash table to store pass num and midpoint times -- used to setting time in app
    private Hashtable<Integer,Double> passHash = new Hashtable<Integer,Double>();
    
    Double nanDbl = new Double(Double.NaN);
          
    
    /** Creates new form JTrackingPanel
     * @param satHash
     * @param gsHash
     * @param timeAsStringIn
     * @param currentJulianDate
     * @param app 
     */
    public JTrackingPanel(Hashtable<String,AbstractSatellite> satHash, Hashtable<String,GroundStation> gsHash, String timeAsStringIn, Time currentJulianDate, JSatTrak app)
    {
        this.satHash = satHash;
        this.gsHash = gsHash;
        
        this.currentJulianDate = currentJulianDate;
        
        this.app = app;
                
        initComponents();
        
        // fill out choice boxes
        refreshComboBoxes();
        
        // do this after components INI
        this.timeAsString = timeAsStringIn;
        updateTime(timeAsString);
        
        
        // update pass table 
        passTableModel = new DefaultTableModel();
        passTable.setModel(passTableModel);
        
        passTableModel.addColumn("#"); // pass number
        passTableModel.addColumn("Rise Time"); //
        passTableModel.addColumn("Rise Az."); // new
        passTableModel.addColumn("Set Time"); //
        passTableModel.addColumn("Set Az."); // new
        passTableModel.addColumn("Duration [Sec]"); // 
        passTableModel.addColumn("Visibility"); //
        
        //passTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        passTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        //passTable.setAutoCreateRowSorter(true);
        
        // add row highliters to passTable --oops not a swingx table
        //passTable.addHighlighter(new ColorHighlighter(HighlightPredicate.EVEN, Color.WHITE, Color.black)); // even, background, foregrond
        //passTable.addHighlighter(new ColorHighlighter(HighlightPredicate.ODD, new Color(229, 229, 229), Color.black)); // odd, background, foregrond
        
        
    }
    
    public void refreshComboBoxes()
    {
        gsComboBox.removeAllItems();
        for(GroundStation gs : gsHash.values())
        {
            gsComboBox.addItem(gs.getStationName());
        }
        
        satComboBox.removeAllItems();
        for(AbstractSatellite sat : satHash.values())
        {
            satComboBox.addItem(sat.getName());
        }
        
                
    } // refreshComboBoxes
    
    // update time called .. update info in box
    // in future might not want to search hash each iteration for gs and sat objects
    public void updateTime(String timeAsString)
    {
        this.timeAsString = timeAsString;
        
        if( gsComboBox.getSelectedIndex() < 0 ||  satComboBox.getSelectedIndex() < 0)
        {
            // something not set
            // erase all data
            aerTextArea.setText("");
            aerTextArea.setBackground(Color.white);
            
            return;
        }
        
        // get sat and gs objects they are null (otherwise same or if boxes changed?)
        // for now local vars = get them each time.
        GroundStation gs = gsHash.get(gsComboBox.getSelectedItem().toString());
        AbstractSatellite sat = satHash.get(satComboBox.getSelectedItem().toString());
        
        // calculate AER
        //double[] aer = gs.calculate_AER( sat.getJ2000Position() ); // in J2k position - incorrect
        if(sat.getTEMEPos() != null && !nanDbl.equals(sat.getTEMEPos()[0])) // check for NAN
        {
            double[] aer = gs.calculate_AER(sat.getTEMEPos());  // MOD

            // add text AER and string
            aerTextArea.setText(timeAsString + "\n\n" + "Azimuth [deg]: " + aer[0] + "\nElevation [deg]: " + aer[1] + "\nRange [m]: " + aer[2]);

            // if in view set BG color to green else white
            if (aer[1] >= gs.getElevationConst())
            {
                aerTextArea.setBackground(Color.GREEN);
            }
            else
            {
                aerTextArea.setBackground(Color.WHITE);
            }

            // update ploar plot
            jPolarPlotLabel.setCurrentAE(aer);
            jPolarPlotLabel.setElevConst(gs.getElevationConst());
            jPolarPlotLabel.setTimeString(timeAsString);
            jPolarPlotLabel.setGs2SatNameString(gs.getStationName().trim() + " to " + sat.getName().trim());
            
            // check to see if we need to update Lead/Lag data
// no good J2000 positions not saved through time right now in satprops  
            // see if we even need to bother - lead data option selected in both 2D and polar plot
            if (jPolarPlotLabel.isShowLeadLagData() && sat.getShowGroundTrack())
            {
                boolean updateLeadData = false;
                boolean updateLagData = false;

                // need to update lead data?
                if (jPolarPlotLabel.getAerLead() == null || jPolarPlotLabel.getAerLead().length < 1)
                {
                    updateLeadData = true;
                    oldLeadX = sat.getTemePosLead()[0][0];
                }
                else
                {
                    if (oldLeadX != sat.getTemePosLead()[0][0])
                    {
                        updateLeadData = true;
                        oldLeadX = sat.getTemePosLead()[0][0];
                    }
                }

                // need to update lag data?
                if (jPolarPlotLabel.getAerLag() == null || jPolarPlotLabel.getAerLag().length < 1)
                {
                    updateLagData = true;
                    oldLagX = sat.getTemePosLag()[0][0];
                }
                else
                {
                    if (oldLagX != sat.getTemePosLag()[0][0])
                    {
                        updateLagData = true;
                        oldLagX = sat.getTemePosLag()[0][0];
                    }
                }

                // update Lead data if needed
                if (updateLeadData) //|| check to see if lead/lag data has been updated..
                {
                    jPolarPlotLabel.setAerLead(AER.calculate_AER(new double[]{gs.getLatitude(), gs.getLongitude(), gs.getAltitude()}, sat.getTemePosLead(), sat.getTimeLead()));
                //System.out.println("Lead updated");
                }

                // update lag data if needed
                if (updateLagData) //|| heck to see if lead/lag data has been updated..
                {
                    jPolarPlotLabel.setAerLag(AER.calculate_AER(new double[]{gs.getLatitude(), gs.getLongitude(), gs.getAltitude()}, sat.getTemePosLag(), sat.getTimeLag()));
                //System.out.println("Lag updated");
                }

            } // lead / lag data shown
            
        } // NAN check
        else
        {
            aerTextArea.setText(timeAsString + "\n\n" + "Satellite Ephemeris Not Available");
            aerTextArea.setBackground(Color.RED);
            
            jPolarPlotLabel.setTimeString(timeAsString);
            jPolarPlotLabel.setGs2SatNameString(gs.getStationName().trim() + " to " + sat.getName().trim());
            
            // clear lead/lag data
            jPolarPlotLabel.clearLeadLagData();
            // clear current point?
            jPolarPlotLabel.resetCurrentPosition();
        }
        
        jPolarPlotLabel.repaint(); // repaint
        
    } //updateTime
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        gsComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        satComboBox = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        aerTextArea = new javax.swing.JTextArea();
        refreshComboBoxesButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPolarPlotLabel = new jsattrak.gui.JPolarPlotLabel();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        leadLagCheckBox = new javax.swing.JCheckBox();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox3 = new javax.swing.JCheckBox();
        jCheckBox4 = new javax.swing.JCheckBox();
        jCheckBox5 = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        passTable = new javax.swing.JTable();
        jLabel4 = new javax.swing.JLabel();
        timeSpanTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        timeStepTextField = new javax.swing.JTextField();
        runPassPredictionButton = new javax.swing.JButton();
        visibleOnlyCheckBox = new javax.swing.JCheckBox();
        go2passButton = new javax.swing.JButton();
        printTableButton = new javax.swing.JButton();
        saveTableButton = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        azComboBox = new javax.swing.JComboBox();

        jTabbedPane1.setMinimumSize(new java.awt.Dimension(53, 53));

        jLabel1.setText("Ground Station:"); // NOI18N

        gsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gsComboBoxActionPerformed(evt);
            }
        });

        jLabel2.setText("Satellite:"); // NOI18N

        satComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        satComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                satComboBoxActionPerformed(evt);
            }
        });

        jLabel3.setText("Current Tracking Information:"); // NOI18N

        aerTextArea.setColumns(20);
        aerTextArea.setRows(5);
        jScrollPane1.setViewportView(aerTextArea);

        refreshComboBoxesButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/view-refresh.png"))); // NOI18N
        refreshComboBoxesButton.setToolTipText("refresh sat and ground stations"); // NOI18N
        refreshComboBoxesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshComboBoxesButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(satComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(gsComboBox, 0, 169, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(refreshComboBoxesButton))
                    .addComponent(jLabel3))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(gsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(satComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addComponent(refreshComboBoxesButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 199, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Basic", jPanel1);

        jPanel2.setLayout(new java.awt.BorderLayout());

        jPolarPlotLabel.setBackground(new java.awt.Color(0, 0, 0));
        jPanel2.add(jPolarPlotLabel, java.awt.BorderLayout.CENTER);

        jToolBar1.setRollover(true);

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/document-print.png"))); // NOI18N
        jButton1.setToolTipText("Print Polar Plot - automatically prints with inverted colors, names, and time");
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jButton1);

        leadLagCheckBox.setSelected(true);
        leadLagCheckBox.setText("Lead/Lag Data"); // NOI18N
        leadLagCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                leadLagCheckBoxActionPerformed(evt);
            }
        });
        jToolBar1.add(leadLagCheckBox);

        jCheckBox1.setSelected(true);
        jCheckBox1.setText("Time"); // NOI18N
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jCheckBox1);

        jCheckBox2.setText("Names"); // NOI18N
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });
        jToolBar1.add(jCheckBox2);

        jCheckBox3.setText("Invert");
        jCheckBox3.setToolTipText("Invert line and background colors");
        jCheckBox3.setFocusable(false);
        jCheckBox3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCheckBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox3ActionPerformed(evt);
            }
        });
        jToolBar1.add(jCheckBox3);

        jCheckBox4.setText("Limit to Horizon");
        jCheckBox4.setFocusable(false);
        jCheckBox4.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCheckBox4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox4ActionPerformed(evt);
            }
        });
        jToolBar1.add(jCheckBox4);

        jCheckBox5.setSelected(true);
        jCheckBox5.setText("Compass");
        jCheckBox5.setFocusable(false);
        jCheckBox5.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jCheckBox5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox5ActionPerformed(evt);
            }
        });
        jToolBar1.add(jCheckBox5);

        jPanel2.add(jToolBar1, java.awt.BorderLayout.SOUTH);

        jTabbedPane1.addTab("Polar Plot", jPanel2);

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 12));
        jLabel6.setText("Pass Predictions:"); // NOI18N

        passTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null},
                {null, null, null},
                {null, null, null},
                {null, null, null}
            },
            new String [] {
                "#", "Rise Time", "Set Time"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(passTable);
        passTable.getColumnModel().getColumn(0).setHeaderValue("#");
        passTable.getColumnModel().getColumn(1).setHeaderValue("Rise Time");
        passTable.getColumnModel().getColumn(2).setHeaderValue("Set Time");

        jLabel4.setText("Time Span [Days]:"); // NOI18N

        timeSpanTextField.setText("10"); // NOI18N

        jLabel5.setText("Time Step [sec]:"); // NOI18N

        timeStepTextField.setText("60.0"); // NOI18N

        runPassPredictionButton.setText("Calculate"); // NOI18N
        runPassPredictionButton.setToolTipText("Calculate Pass Predictions");
        runPassPredictionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runPassPredictionButtonActionPerformed(evt);
            }
        });

        visibleOnlyCheckBox.setText("Visible Only"); // NOI18N

        go2passButton.setText("Go to Pass");
        go2passButton.setToolTipText("Set the time to the middle of the pass so it can be analyzed (e.g. polar plot)");
        go2passButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                go2passButtonActionPerformed(evt);
            }
        });

        printTableButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/document-print.png"))); // NOI18N
        printTableButton.setToolTipText("Print Table");
        printTableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printTableButtonActionPerformed(evt);
            }
        });

        saveTableButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/gnome_2_18/document-save-as.png"))); // NOI18N
        saveTableButton.setToolTipText("Save Table to File");
        saveTableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveTableButtonActionPerformed(evt);
            }
        });

        jLabel7.setText("Rise/Set Azimuth:");

        azComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Degrees", "Compass Points" }));
        azComboBox.setSelectedIndex(1);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                        .addContainerGap())
                    .addComponent(jLabel6)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(timeSpanTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(timeStepTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(54, 54, 54))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(visibleOnlyCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(azComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 99, Short.MAX_VALUE)
                        .addComponent(runPassPredictionButton)
                        .addContainerGap())
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(go2passButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 263, Short.MAX_VALUE)
                        .addComponent(saveTableButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(printTableButton)
                        .addContainerGap())))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(timeSpanTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(timeStepTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(visibleOnlyCheckBox)
                    .addComponent(runPassPredictionButton)
                    .addComponent(jLabel7)
                    .addComponent(azComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(go2passButton)
                    .addComponent(printTableButton)
                    .addComponent(saveTableButton)))
        );

        jTabbedPane1.addTab("Pass Predictions", jPanel3);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void refreshComboBoxesButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_refreshComboBoxesButtonActionPerformed
    {//GEN-HEADEREND:event_refreshComboBoxesButtonActionPerformed
        // save selections (if there are any)
        String oldSat = "null";
        if(satComboBox.getSelectedIndex() >= 0 )
        {
            oldSat = satComboBox.getSelectedItem().toString();
        }
        String oldGS = "null";
        if(gsComboBox.getSelectedIndex() >= 0 )
        {
            oldGS = gsComboBox.getSelectedItem().toString();
        }
        
        refreshComboBoxes();
        
        // select old selections if there are any
        if(!oldSat.equalsIgnoreCase("null"))
        {
            satComboBox.setSelectedItem(oldSat);
        }
        
        if(!oldGS.equalsIgnoreCase("null"))
        {
            gsComboBox.setSelectedItem(oldGS);
        }
        
    }//GEN-LAST:event_refreshComboBoxesButtonActionPerformed

    private void gsComboBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_gsComboBoxActionPerformed
    {//GEN-HEADEREND:event_gsComboBoxActionPerformed
        aerTextArea.setText(""); // clear box
        if(timeAsString != null)
            updateTime(timeAsString);
        
        objectChangeReset();
        
    }//GEN-LAST:event_gsComboBoxActionPerformed

    private void satComboBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_satComboBoxActionPerformed
    {//GEN-HEADEREND:event_satComboBoxActionPerformed
        aerTextArea.setText(""); // clear box
        if(timeAsString != null)
            updateTime(timeAsString);
        
        objectChangeReset();
    }//GEN-LAST:event_satComboBoxActionPerformed

    /**
     * functions for use for scripting purposes, sets Gound Station
     * @param gsName
     */
    public void setGroundStation(String gsName)
    {
        gsComboBox.setSelectedItem(gsName);
    }
    
    /**
     * functions for use for scripting purposes, sets Gound Station
     * @param gsName
     */
    public void setGroundStation(GroundStation gs)
    {
        setGroundStation(gs.getStationName());
    }
    
    /**
     * functions for use for scripting purposes, sets Satellite
     * @param gsName
     */
    public void setSatellite(String satName)
    {
        satComboBox.setSelectedItem(satName);
    }
    
    /**
     * functions for use for scripting purposes, sets Satellite
     * @param gsName
     */
    public void setSatellite(AbstractSatellite sat)
    {
        setSatellite(sat.getName());
    }
    
    private void objectChangeReset()
    {
        // GUI changes needed when objects are changed
        
        // clear pass table
        if (passTableModel != null) // ini of object
        {
            while (passTableModel.getRowCount() > 0)
            {
                passTableModel.removeRow(0);
            }

            // refresh lead/lag in polar plot if needed
            if (gsComboBox.getSelectedIndex() < 0 || satComboBox.getSelectedIndex() < 0)
            {
                return; // something not selected skip lead/lag update
            }

            GroundStation gs = gsHash.get(gsComboBox.getSelectedItem().toString());
            AbstractSatellite sat = satHash.get(satComboBox.getSelectedItem().toString());
            if (jPolarPlotLabel.isShowLeadLagData() && sat.getShowGroundTrack())
            {
                jPolarPlotLabel.setAerLead(AER.calculate_AER(new double[]{gs.getLatitude(), gs.getLongitude(), gs.getAltitude()}, sat.getTemePosLead(), sat.getTimeLead()));
                jPolarPlotLabel.setAerLag(AER.calculate_AER(new double[]{gs.getLatitude(), gs.getLongitude(), gs.getAltitude()}, sat.getTemePosLag(), sat.getTimeLag()));
            }
        }
        
    }
    
    private void leadLagCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_leadLagCheckBoxActionPerformed
    {//GEN-HEADEREND:event_leadLagCheckBoxActionPerformed
        jPolarPlotLabel.setShowLeadLagData(leadLagCheckBox.isSelected());
        jPolarPlotLabel.repaint();
    }//GEN-LAST:event_leadLagCheckBoxActionPerformed

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBox2ActionPerformed
    {//GEN-HEADEREND:event_jCheckBox2ActionPerformed
        jPolarPlotLabel.setDisplayNames(jCheckBox2.isSelected());
        jPolarPlotLabel.repaint();
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    private void runPassPredictionButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_runPassPredictionButtonActionPerformed
    {//GEN-HEADEREND:event_runPassPredictionButtonActionPerformed
        runPassPrediction();
    }//GEN-LAST:event_runPassPredictionButtonActionPerformed

    public void runPassPrediction()
    {
        // get info:
        double timeSpanDays = Double.parseDouble( timeSpanTextField.getText() );
        double timeStepSec = Double.parseDouble( timeStepTextField.getText() );
        boolean onlyVisible = visibleOnlyCheckBox.isSelected();
        
        // get sat and GS
        GroundStation gs = gsHash.get(gsComboBox.getSelectedItem().toString());
        AbstractSatellite sat = satHash.get(satComboBox.getSelectedItem().toString());
        
        // start time Jul Date
        double jdStart = currentJulianDate.getJulianDate();
        
        // clear hash
        passHash.clear();
        
        // clear the table
        while(passTableModel.getRowCount() > 0)
        {
            passTableModel.removeRow(0);
        }
        
        // Sun sun
        
//        // elevation data used in seach
//        double h0=0,h1=0,h2=0;
//        
//        // intially calculate elevations at 0/1 time points
//        // AER.calculate_AER(new double[]{gs.getLatitude(), gs.getLongitude(), gs.getAltitude()}, sat.getModPosLead(), sat.getTimeLead())
//        double time0 = jdStart - timeStepSec/(60.0*60.0*24.0);
//        h0 = AER.calculate_AER(gs.getLla_deg_m(), sat.calculateMODPositionFromUT(time0) , time0)[1];
//        double time1 = jdStart;
//        h1 = AER.calculate_AER(gs.getLla_deg_m(), sat.calculateMODPositionFromUT(time1) , time1)[1];
//        double time2 = jdStart + timeStepSec/(60.0*60.0*24.0);; // declare var
//        
//        System.out.println(time0 + "," + h0 );
//        System.out.println(time1 + "," + h1 );
//        
//        // use quadratic fit to search for zeros
//        for(double jd = jdStart; jd <= jdStart + timeSpanDays; jd += timeStepSec/(60.0*60.0*24.0))
//        {
//            time0 = time1;
//            time1 = time2;
//            time2 = jd + timeStepSec/(60.0*60.0*24.0);
//            
//            // calculate elevations at each time step (if needed)
//            h0 = h1;
//            h1 = h2;
//            // calculate the elevation at this newly visited point
//            h2 = AER.calculate_AER(gs.getLla_deg_m(), sat.calculateMODPositionFromUT(time2) , time2)[1];
//            System.out.println(time2 + "," + h2 );
//            
//            // create a quadratic interpolator
//            QuadraticInterpolatorSimp qis = new QuadraticInterpolatorSimp(time0, h0, time1, h1, time2, h2);
//            
////            if(qis.getRootCountInDomain() == 1)
////            {
////                System.out.println("Event(1): " + qis.getLowerRoot() );
////            }else if(qis.getRootCountInDomain() == 2)
////            {
////                System.out.println("Event(2a): " + qis.getLowerRoot() );
////                System.out.println("Event(2b): " + qis.getUpperRoot() );
////            }
//            
//        } // for loop seaching for rise/set events
        
        
        Sun internalSun = new Sun(jdStart - AstroConst.JDminusMJD); // new sun for internal calculations of Visibility
        
        // linear search
        double time0, h0;
        double time1 = jdStart;
        double h1 = AER.calculate_AER(gs.getLla_deg_m(), sat.calculateTemePositionFromUT(time1) , time1)[1] - gs.getElevationConst();
        
        int passCount = 0;
        
        if(h1 > 0)
        {
            passCount++;
            passTableModel.addRow(new Object[] {passCount,"--","","",""});
        }
        
        double lastRise = 0;
        
        for(double jd = jdStart; jd <= jdStart + timeSpanDays; jd += timeStepSec/(60.0*60.0*24.0))
        {
            time0 = time1;
            time1 = jd + timeStepSec/(60.0*60.0*24.0);
            
            // calculate elevations at each time step (if needed)
            h0 = h1;
            // calculate the elevation at this newly visited point
            h1 = AER.calculate_AER(gs.getLla_deg_m(), sat.calculateTemePositionFromUT(time1) , time1)[1] - gs.getElevationConst();
            
            // rise
            if(h0<=0 && h1 >0)
            {
                double riseTime =  findSatRiseSetRoot(sat, gs, time0, time1, h0, h1);
                //System.out.println("Rise at " + riseTime  + " (" + time0 + "," + time1 + ")"); 
                
                lastRise = riseTime; // save
                
                // add to table
                passCount++;
                // use Time object to convert Julian date to string using program settings (i.e. time zone)
                String crossTimeStr = currentJulianDate.convertJD2String(riseTime);
                
                passTableModel.addRow(new Object[] {passCount,crossTimeStr,"","","","",""});

                // calculate using the rise time - the Azimuth
                double az = AER.calculate_AER(gs.getLla_deg_m(), sat.calculateTemePositionFromUT(riseTime) , riseTime)[0];
                if(azComboBox.getSelectedIndex() == 0)
                {
                    passTableModel.setValueAt(""+String.format("%.1f", az), passTableModel.getRowCount()-1, 2);
                }
                else
                {
                    // is AZ in degrees or radians?
                    passTableModel.setValueAt(CoordinateConversion.degrees2CompassPoints(az), passTableModel.getRowCount()-1, 2);
                }
            }
            
            // set
            if(h1<=0 && h0 >0)
            {
                double setTime =  findSatRiseSetRoot(sat, gs, time0, time1, h0, h1);
                //System.out.println("Set at " + setTime  + " (" + time0 + "," + time1 + ")"); 
                
                // add to table
                String crossTimeStr = currentJulianDate.convertJD2String(setTime);
                passTableModel.setValueAt(crossTimeStr, passTableModel.getRowCount()-1, 3); // last row, 3rd column (2)
                
                // calculate using the set time - the Azimuth
                double az = AER.calculate_AER(gs.getLla_deg_m(), sat.calculateTemePositionFromUT(setTime) , setTime)[0];
                if(azComboBox.getSelectedIndex() == 0)
                {
                    passTableModel.setValueAt(""+String.format("%.1f", az), passTableModel.getRowCount()-1, 4);
                }
                else
                {
                    // is AZ in degrees or radians?
                    passTableModel.setValueAt(CoordinateConversion.degrees2CompassPoints(az), passTableModel.getRowCount()-1, 4);
                } // azimuth


                // add duration
                if(lastRise > 0)
                {
                    DecimalFormat fmt2Dig = new DecimalFormat("00.000");
                    
                    double duration = (setTime - lastRise)*24.0*60.0*60.0; // seconds
                    String durStr = fmt2Dig.format( duration );
                    passTableModel.setValueAt(durStr, passTableModel.getRowCount()-1, 5); // last row, 4rd column (3)
                }
                
                // determine visibility
                if(lastRise > 0)
                {
                    // Visiable, Radar Night, Radar Night
                    
                    // use the time 1/2 between rise and set for viz calculations
                    // DOES NOT CHECK FOR VIS NEAR END POINTS SO COULD MISS SOME PARTIAL PASS VISIBILITY
                                        
                    double julDateVizCalc = (setTime - lastRise)/2.0 + lastRise;
                    
                    // SAVE to hash - for use later
                    passHash.put(new Integer(passCount), new Double(julDateVizCalc));
                    
                    // twilight offset
                    // 7 seems good
                    // 6 is used by heavens-above.com
                    double twilightOffset = 6; // degrees extra required for darkness
                    
                    // set the suns time 
                    internalSun.setCurrentMJD( julDateVizCalc - AstroConst.JDminusMJD );
                    
                    // MOD - sun dot site positions to determine if station is in sunlight
                    double[] gsECI = AER.calculateECIposition(julDateVizCalc, gs.getLla_deg_m());
                    double sunDotSite = MathUtils.dot(internalSun.getCurrentPositionTEME(),gsECI );
                    
                    // TEST - find angle between sun -> center of Earth -> Ground Station
                    double sinFinalSigmaGS = MathUtils.norm( MathUtils.cross(internalSun.getCurrentPositionTEME(), gsECI) )
                             / ( MathUtils.norm(internalSun.getCurrentPositionTEME()) * MathUtils.norm(gsECI)  );
                    double finalSigmaGS = Math.asin(sinFinalSigmaGS)*180.0/Math.PI; // in degrees
                    
                    if(sunDotSite > 0 || (90.0-finalSigmaGS) < twilightOffset )
                    {
                        passTableModel.setValueAt("Radar Sun", passTableModel.getRowCount()-1, 6); // last row, 5rd column (4)
                    } // sun light
                    else
                    {
                        // now we know the site is in darkness - need to figure out if the satelite is in light
                        // use predict algorithm from Vallado 2nd ed.
                        double[] satMOD = sat.calculateTemePositionFromUT(julDateVizCalc);
                        double sinFinalSigma = MathUtils.norm( MathUtils.cross(internalSun.getCurrentPositionTEME(), satMOD) )
                                / ( MathUtils.norm(internalSun.getCurrentPositionTEME()) * MathUtils.norm(satMOD)  );
                        double finalSigma = Math.asin(sinFinalSigma);
                        double dist = MathUtils.norm(satMOD) * Math.cos( finalSigma - Math.PI/2.0);
                        
                        if(dist > AstroConst.R_Earth_mean) // changed to mean 30/March/2009 SEG
                        {
                            // sat is in sunlight!
                            passTableModel.setValueAt("Visible", passTableModel.getRowCount()-1, 6); // last row, 5rd column (4)
                        }
                        else  // Radar Night (both in darkness)
                        {
                            passTableModel.setValueAt("Radar Night", passTableModel.getRowCount()-1, 6); // last row, 5rd column (4)
                        }
                        
                    } // site in dark
                    
                } // visibility (with last rise)             
                
            } // set
            
            
        }// linear seach
        
        // if visible only checked remove other items from the list
        if(visibleOnlyCheckBox.isSelected())
        {
            
            int vizColumn = 6;  // vis text column
            for(int i=passTableModel.getRowCount()-1; i>=0;i-- )
            {
               
              if(!passTableModel.getValueAt(i, vizColumn).toString().equalsIgnoreCase("Visible"))
              {
                  passTableModel.removeRow(i);
              }
            }
            
            
            
        } // remove non-visible
        
        
        // set first col to be small
        passTable.getColumnModel().getColumn(0).setPreferredWidth(10);
    } // runPassPrediction
    
    private void go2passButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_go2passButtonActionPerformed
    {//GEN-HEADEREND:event_go2passButtonActionPerformed
        // get selected row in table
        int tableRow = passTable.getSelectedRow();
        go2pass(tableRow);
    }//GEN-LAST:event_go2passButtonActionPerformed

    private void printTableButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_printTableButtonActionPerformed
    {//GEN-HEADEREND:event_printTableButtonActionPerformed
        // print table

        // complete set of print parameters passed
        //http://java.sun.com/docs/books/tutorial/uiswing/misc/printtable.html
        try
        {
            // header prints too large not sure how to reduce font size
            //MessageFormat header = new MessageFormat("JSatTrak Pass Predictions: \n" + gsComboBox.getSelectedItem().toString() + " to " + satComboBox.getSelectedItem().toString());
            MessageFormat header = new MessageFormat(gsComboBox.getSelectedItem().toString() + " to " + satComboBox.getSelectedItem().toString());

            boolean complete = passTable.print(PrintMode.FIT_WIDTH, // JTable.PrintMode or PrintMode.NORMAL
                                        header, //  MessageFormat headerFormat
                                        new MessageFormat("JSatTrak Pass Predictions  - {0} -"), //  MessageFormat footerFormat
                                        true, //  boolean showPrintDialog
                                        null, //  PrintRequestAttributeSet attr
                                        false, // boolean interactive
                                        null); // PrintService
        }
        catch(Exception e)
        {
            System.out.println("ERROR Printing Pass Table: "  + e.toString());
        }

    }//GEN-LAST:event_printTableButtonActionPerformed

    private void saveTableButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_saveTableButtonActionPerformed
    {//GEN-HEADEREND:event_saveTableButtonActionPerformed
        // save data in the table to a CVS
        String fileSaveAs;

        // first as for a file name:
        final JFileChooser fc = new JFileChooser();
        CustomFileFilter xmlFilter = new CustomFileFilter("csv","*.csv");
        fc.addChoosableFileFilter(xmlFilter);

        int returnVal = fc.showSaveDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            File file = fc.getSelectedFile();

            String extension = FileUtilities.getExtension(file);
            if (extension != null)
            {
                fileSaveAs = file.getAbsolutePath();
            }
            else
            {
                // append the extension
                file = new File(file.getAbsolutePath() + ".csv");
                fileSaveAs = file.getAbsolutePath();
            }

            fileSaveAs = file.getAbsolutePath();

            // save data to file : fileSaveAs ------------------
            try
            {
                FileWriter fstream = new FileWriter(fileSaveAs);
                BufferedWriter buffOut = new BufferedWriter(fstream);

                // write the name of each column heading
                for(int i = 0; i < passTableModel.getColumnCount(); i++)
                {
                    String val = passTableModel.getColumnName(i);
                    if(i != passTableModel.getColumnCount() - 1)
                    {
                        val += ","; // add comma to all but last element
                    }

                    buffOut.write(val);
                }

                buffOut.write("\n"); // add new line before data

                // loop through all the data
                for(int i = 0; i < passTableModel.getRowCount(); i++)
                {
                    for(int j = 0; j < passTableModel.getColumnCount(); j++)
                    {
                        String val = passTableModel.getValueAt(i, j).toString();

                        if(j != passTableModel.getColumnCount() - 1)
                        {
                            val += ","; // add comma to all but last element
                        }

                        buffOut.write(val);
                    } // for eacl column

                    // add a carrage return to the file
                    buffOut.write("\n");

                } // for each row

                buffOut.close(); // close file
            }
            catch(Exception e)
            {
                // file writting / opening error
            }

        } // if approve

    }//GEN-LAST:event_saveTableButtonActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBox1ActionPerformed
    {//GEN-HEADEREND:event_jCheckBox1ActionPerformed
        jPolarPlotLabel.setDisplayTime(jCheckBox1.isSelected());
        jPolarPlotLabel.repaint();
}//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jCheckBox3ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBox3ActionPerformed
    {//GEN-HEADEREND:event_jCheckBox3ActionPerformed
        jPolarPlotLabel.setDarkColors(!jCheckBox3.isSelected());
        //jPolarPlotLabel.repaint();
    }//GEN-LAST:event_jCheckBox3ActionPerformed

    private void jCheckBox4ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBox4ActionPerformed
    {//GEN-HEADEREND:event_jCheckBox4ActionPerformed
        jPolarPlotLabel.setLimit2Horizon(jCheckBox4.isSelected());
    }//GEN-LAST:event_jCheckBox4ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jButton1ActionPerformed
    {//GEN-HEADEREND:event_jButton1ActionPerformed
        jPolarPlotLabel.print();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jCheckBox5ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBox5ActionPerformed
    {//GEN-HEADEREND:event_jCheckBox5ActionPerformed
        jPolarPlotLabel.setUseCompassPoints(jCheckBox5.isSelected());
    }//GEN-LAST:event_jCheckBox5ActionPerformed
    
    /**
     * jumps time to the mid point of a pass
     * @param passNumber pass number from last solution, range: 0 to N-1
     */
    public void go2pass(int passNumber)
    {
        
        if(passNumber >= 0)
        {
            // get # in that row
            Integer passNum = new Integer( passTableModel.getValueAt(passNumber, 0).toString() );
            
            if( passHash.containsKey(passNum) )
            {
                double jdMidPoint = passHash.get(passNum).doubleValue();

                // get milliseconds from Julian Date            
                long millis = currentJulianDate.convertJD2Calendar(jdMidPoint).getTimeInMillis();

                // get current app time
                double daysDiff = Math.abs( currentJulianDate.getJulianDate() - jdMidPoint);
                
                // check to see if lead/lag data needs updating
                app.checkTimeDiffResetGroundTracks(daysDiff);
                        
                // set app to new time
                app.setTime(millis);
            }
        }
                
    } // go2pass
    
    // bisection method, crossing time should be bracketed by time0 and time1
    private double findSatRiseSetRoot(AbstractSatellite sat, GroundStation gs, double time0, double time1, double f0, double f1)
    {
        double tol = (1.157407E-5)/4; // 1/4 a sec (in units of a day)
        
        int iterCount = 0;
        
        while(Math.abs(time1-time0) > 2*tol)
        {
            //Calculate midpoint of domain
            double timeMid = (time1 + time0) / 2.0;
            double fmid = AER.calculate_AER(gs.getLla_deg_m(), sat.calculateTemePositionFromUT(timeMid) , timeMid)[1] - gs.getElevationConst();
            
            if( f0 * fmid > 0) // same sign
            {
                // replace f0 with fmid
                f0 = fmid;
                time0 = timeMid;
                
            }else  // else replace f1 with fmid
            {
                f1 = fmid;
                time1 = timeMid;
            }
            
            iterCount++;
        } // while not in tolerance
        
        // return best gues using linear interpolation between last two points
        double a = (f1-f0)/(time1-time0);
        double b = f1-a*time1;
        double riseTime = -b/a;
        
        //System.out.println("Bisection Iters: " + iterCount);
        
        return riseTime; // return best guess  -typically:  (time0 + time1)/2.0;
        //return (time0 + time1)/2.0;
        
    } // findSatRiseSetRoot
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea aerTextArea;
    private javax.swing.JComboBox azComboBox;
    private javax.swing.JButton go2passButton;
    private javax.swing.JComboBox gsComboBox;
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private jsattrak.gui.JPolarPlotLabel jPolarPlotLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JCheckBox leadLagCheckBox;
    private javax.swing.JTable passTable;
    private javax.swing.JButton printTableButton;
    private javax.swing.JButton refreshComboBoxesButton;
    private javax.swing.JButton runPassPredictionButton;
    private javax.swing.JComboBox satComboBox;
    private javax.swing.JButton saveTableButton;
    private javax.swing.JTextField timeSpanTextField;
    private javax.swing.JTextField timeStepTextField;
    private javax.swing.JCheckBox visibleOnlyCheckBox;
    // End of variables declaration//GEN-END:variables
 
    public void setTimeSpanDays(double days)
    {
        timeSpanTextField.setText(""+days);
    }
    
    public void setTimeStepSec(double sec)
    {
        timeStepTextField.setText(""+sec);
    }
    
    public void setVisibleOnlyFilter(boolean visibleOnly)
    {
        visibleOnlyCheckBox.setSelected(visibleOnly);
    }

    public Hashtable<Integer, Double> getPassHash()
    {
        return passHash;
    }
    
    public String[][] getPredictionTableData()
    {
        String[][] data = new String[passTableModel.getRowCount()][passTableModel.getColumnCount()];
        
        for(int r = 0; r<passTableModel.getRowCount();r++)
        {
            for(int c = 0; c<passTableModel.getColumnCount();c++)
            {
                data[r][c] = passTableModel.getValueAt(r, c).toString();
            }
        }
        
        return data;
    } // getPredictionTableData
    
    /**
     * can be used in a script to have the lable rendered off screen or to set its display settings
     * @return
     */
    public JPolarPlotLabel getPolarPlotLabel()
    {
        return jPolarPlotLabel;
    }
    
}
