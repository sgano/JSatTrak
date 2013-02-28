/*
 * SatSettingsPanel.java
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
 * Created on August 5, 2007, 9:42 PM
 */

package jsattrak.gui;

import java.awt.Color;
import java.io.File;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import jsattrak.objects.AbstractSatellite;
import jsattrak.objects.CustomSatellite;
import jsattrak.utilities.CustomFileFilter;
import jsattrak.utilities.RelativePath;

/**
 *
 * @author  sgano
 */
public class SatSettingsPanel extends javax.swing.JPanel implements java.io.Serializable
{
    
    AbstractSatellite satProps;
    
    private boolean okHit = false; // if okay was hit
    
    JInternalFrame iframe; // used to know what its parent frame is - to close window
    
    JSatTrak app; // used to force repaints
    
    /** Creates new form SatSettingsPanel */
    public SatSettingsPanel()
    {
        initComponents();
        
        // won't work - dummy
        //satProps = new SatelliteTleSGP4("test","1","2");
    }
    
    // constructor to pass SatelliteProps object
    public SatSettingsPanel(AbstractSatellite satProps, JSatTrak app)
    {
        initComponents();
        
        
        this.satProps = satProps;
        this.app = app;
        
        // if this a custom sat add extra tab at the begining
        if (satProps instanceof CustomSatellite)
        {
            CustomSatellite satProp = (CustomSatellite) satProps;
            
            JCustomSatConfigPanel configPanel = new JCustomSatConfigPanel(satProp,satProp.getEphemeris(), app);
            int tabIndex = 0;
            jTabbedPane1.add(configPanel,tabIndex);
            jTabbedPane1.setTitleAt(tabIndex, "Custom Satellite Settings");
            
            jTabbedPane1.setSelectedIndex(0); // select first tab
        }
        
        // setup the fields
        iniSatProps();
    } 
    
    // ini option fields from satProps
    private void iniSatProps()
    {
        // general
        include2DCheckBox.setSelected( satProps.getPlot2D() );
        showNameCheckBox.setSelected( satProps.isShowName2D() );
        colorPanel.setBackground( satProps.getSatColor() );
        
        // footprint
        showFootPrintCheckBox.setSelected(satProps.getPlot2DFootPrint() );
        footPrintResTextField.setText(satProps.getNumPtsFootPrint()+"" );
        fillFootprintCheckBox.setSelected(satProps.isFillFootPrint() );
        
        // ground track
        showGroundTrackCheckBox.setSelected(satProps.getShowGroundTrack() );
        groundTrackResTextField.setText(satProps.getGrnTrkPointsPerPeriod()+"");
        leadTrackTextField.setText(satProps.getGroundTrackLeadPeriodMultiplier()+"");
        lagTrackTextField.setText(satProps.getGroundTrackLagPeriodMultiplier()+"");
        
        // 3d view
        showOrbit3DCheckBox.setSelected( satProps.isShow3DOrbitTrace()  );
        showfootprint3DCheckBox.setSelected(satProps.isShow3DFootprint());
        showName3DCheckBox.setSelected( satProps.isShow3DName());
        groundTrack3dCheckBox.setSelected(satProps.isShowGroundTrack3d());
        eciRadioButton.setSelected( satProps.isShow3DOrbitTraceECI()  );
        ecefRadioButton.setSelected( !satProps.isShow3DOrbitTraceECI() );
        
        // 3d models
        use3DModelCheckBox.setSelected(satProps.isUse3dModel());
        threeDModelTextField.setText(satProps.getThreeDModelPath());
        threeDModelTextField.setEditable(satProps.isUse3dModel()); // set editable
        browseModelButton.setEnabled(use3DModelCheckBox.isSelected());
        modelScaleTextField.setEditable(use3DModelCheckBox.isSelected());
        modelScaleTextField.setText( satProps.getThreeDModelSizeFactor()+"" );
        
    } //iniSatProps

    public boolean isOkHit()
    {
        return okHit;
    }

    public void setOkHit(boolean okHit)
    {
        this.okHit = okHit;
    }
    
    public void setInternalFrame(JInternalFrame iframe)
    {
        this.iframe = iframe;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        showGroundTrackCheckBox = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        groundTrackResTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        leadTrackTextField = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        lagTrackTextField = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        showFootPrintCheckBox = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        footPrintResTextField = new javax.swing.JTextField();
        fillFootprintCheckBox = new javax.swing.JCheckBox();
        jPanel6 = new javax.swing.JPanel();
        include2DCheckBox = new javax.swing.JCheckBox();
        colorPanel = new javax.swing.JPanel();
        colorButton = new javax.swing.JButton();
        showNameCheckBox = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        showOrbit3DCheckBox = new javax.swing.JCheckBox();
        showfootprint3DCheckBox = new javax.swing.JCheckBox();
        showName3DCheckBox = new javax.swing.JCheckBox();
        groundTrack3dCheckBox = new javax.swing.JCheckBox();
        eciRadioButton = new javax.swing.JRadioButton();
        ecefRadioButton = new javax.swing.JRadioButton();
        jPanel8 = new javax.swing.JPanel();
        use3DModelCheckBox = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        threeDModelTextField = new javax.swing.JTextField();
        browseModelButton = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        modelScaleTextField = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        applyButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        jTabbedPane1.setMinimumSize(new java.awt.Dimension(0, 0));
        jTabbedPane1.setPreferredSize(new java.awt.Dimension(0, 0));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Ground Track"));

        showGroundTrackCheckBox.setText("Show Groundtrack"); // NOI18N
        showGroundTrackCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        showGroundTrackCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel1.setText("Resolution:"); // NOI18N

        groundTrackResTextField.setText("121"); // NOI18N

        jLabel2.setText("Leading Ground Tracks:"); // NOI18N

        leadTrackTextField.setText("2.0"); // NOI18N

        jLabel3.setText("Lagging Ground Tracks:"); // NOI18N

        lagTrackTextField.setText("1.0"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lagTrackTextField))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(jLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(leadTrackTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(showGroundTrackCheckBox)
                        .addGap(15, 15, 15)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(groundTrackResTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(26, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(showGroundTrackCheckBox)
                    .addComponent(jLabel1)
                    .addComponent(groundTrackResTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(leadTrackTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lagTrackTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Footprint"));

        showFootPrintCheckBox.setText("Show Viewable Footprint"); // NOI18N
        showFootPrintCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        showFootPrintCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        jLabel4.setText("Resolution:"); // NOI18N

        footPrintResTextField.setText("101"); // NOI18N

        fillFootprintCheckBox.setText("Fill Footprint"); // NOI18N
        fillFootprintCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        fillFootprintCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(showFootPrintCheckBox)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(footPrintResTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addComponent(fillFootprintCheckBox)))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(showFootPrintCheckBox)
                    .addComponent(jLabel4)
                    .addComponent(footPrintResTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fillFootprintCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("General 2D Options"));

        include2DCheckBox.setText("Include on 2D Map"); // NOI18N
        include2DCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        include2DCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        colorPanel.setBackground(new java.awt.Color(0, 0, 153));
        colorPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout colorPanelLayout = new javax.swing.GroupLayout(colorPanel);
        colorPanel.setLayout(colorPanelLayout);
        colorPanelLayout.setHorizontalGroup(
            colorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 30, Short.MAX_VALUE)
        );
        colorPanelLayout.setVerticalGroup(
            colorPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 21, Short.MAX_VALUE)
        );

        colorButton.setText("Color"); // NOI18N
        colorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorButtonActionPerformed(evt);
            }
        });

        showNameCheckBox.setText("Show Name"); // NOI18N
        showNameCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        showNameCheckBox.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(include2DCheckBox)
                        .addGap(22, 22, 22)
                        .addComponent(showNameCheckBox))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(colorPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(colorButton)))
                .addContainerGap(69, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(include2DCheckBox)
                    .addComponent(showNameCheckBox))
                .addGap(15, 15, 15)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(colorPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(colorButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(149, 149, 149))
        );

        jTabbedPane1.addTab("2D Graphics", jPanel2);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Display Options"));

        showOrbit3DCheckBox.setText("Show Orbit Track"); // NOI18N
        showOrbit3DCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showOrbit3DCheckBoxActionPerformed(evt);
            }
        });

        showfootprint3DCheckBox.setText("Show Footprint"); // NOI18N

        showName3DCheckBox.setText("Show Name"); // NOI18N

        groundTrack3dCheckBox.setText("Show Ground Track"); // NOI18N
        groundTrack3dCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                groundTrack3dCheckBoxActionPerformed(evt);
            }
        });

        eciRadioButton.setSelected(true);
        eciRadioButton.setText("ECI"); // NOI18N
        eciRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eciRadioButtonActionPerformed(evt);
            }
        });

        ecefRadioButton.setText("ECEF"); // NOI18N
        ecefRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ecefRadioButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(showOrbit3DCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(eciRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ecefRadioButton))
                    .addComponent(showfootprint3DCheckBox)
                    .addComponent(showName3DCheckBox)
                    .addComponent(groundTrack3dCheckBox))
                .addContainerGap(80, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(showOrbit3DCheckBox)
                    .addComponent(eciRadioButton)
                    .addComponent(ecefRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showfootprint3DCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showName3DCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(groundTrack3dCheckBox))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("3D Model"));

        use3DModelCheckBox.setText("Use Custom 3D Model");
        use3DModelCheckBox.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                use3DModelCheckBoxStateChanged(evt);
            }
        });

        jLabel5.setText("Path:");

        browseModelButton.setText("Browse");
        browseModelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseModelButtonActionPerformed(evt);
            }
        });

        jLabel6.setText("Scale Factor:");

        modelScaleTextField.setText("300000");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(use3DModelCheckBox)
                        .addGap(18, 18, 18)
                        .addComponent(browseModelButton))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(modelScaleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(threeDModelTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(use3DModelCheckBox)
                    .addComponent(browseModelButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(threeDModelTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(modelScaleTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(40, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("3D Graphics", jPanel1);

        applyButton.setText("Apply"); // NOI18N
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });

        okButton.setText("Ok"); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel"); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(applyButton)
                .addGap(18, 18, 18)
                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cancelButton)
                .addContainerGap(86, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(applyButton)
                    .addComponent(okButton)
                    .addComponent(cancelButton)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void colorButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_colorButtonActionPerformed
    {//GEN-HEADEREND:event_colorButtonActionPerformed
        // popup color chooser starting with current color
        // if user hit okay, change color of  jpanel: colorPanel
        Color oldColor = colorPanel.getBackground();
        // show color Dialog
        Color newColor = JColorChooser.showDialog(app,"Choose Satellite Color", oldColor);
        
        if (newColor != null)
        {
            colorPanel.setBackground(newColor);
        }

    }//GEN-LAST:event_colorButtonActionPerformed

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_applyButtonActionPerformed
    {//GEN-HEADEREND:event_applyButtonActionPerformed
       // save settings
        boolean updateMaps = saveSettings();
        
        // force repaint
        app.forceRepainting(updateMaps);
    }//GEN-LAST:event_applyButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        // close internal frame
        try
        {
            iframe.dispose(); // could setClosed(true)
        }
        catch(Exception e){}
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_okButtonActionPerformed
    {//GEN-HEADEREND:event_okButtonActionPerformed
        // save all settings back to satProp
         boolean updateMaps = saveSettings();
        
        
        // force repaint of 2D window
          // maybe do this from JSatTrack -- when internal frame is closed of this type and ok was hit?
        okHit = true;
        // force repaint
        app.forceRepainting(updateMaps);
        
        // close internal frame
        try
        {
            iframe.dispose(); // could setClosed(true)
        }
        catch(Exception e){}
        
    }//GEN-LAST:event_okButtonActionPerformed

    private void showOrbit3DCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_showOrbit3DCheckBoxActionPerformed
    {//GEN-HEADEREND:event_showOrbit3DCheckBoxActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_showOrbit3DCheckBoxActionPerformed

    private void groundTrack3dCheckBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_groundTrack3dCheckBoxActionPerformed
    {//GEN-HEADEREND:event_groundTrack3dCheckBoxActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_groundTrack3dCheckBoxActionPerformed

    private void eciRadioButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_eciRadioButtonActionPerformed
    {//GEN-HEADEREND:event_eciRadioButtonActionPerformed
        eciRadioButton.setSelected(true);
        ecefRadioButton.setSelected(false);
}//GEN-LAST:event_eciRadioButtonActionPerformed

    private void ecefRadioButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ecefRadioButtonActionPerformed
    {//GEN-HEADEREND:event_ecefRadioButtonActionPerformed
        eciRadioButton.setSelected(false);
        ecefRadioButton.setSelected(true);
    }//GEN-LAST:event_ecefRadioButtonActionPerformed

private void use3DModelCheckBoxStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_use3DModelCheckBoxStateChanged
    threeDModelTextField.setEditable(use3DModelCheckBox.isSelected());
    browseModelButton.setEnabled(use3DModelCheckBox.isSelected());
    modelScaleTextField.setEditable(use3DModelCheckBox.isSelected());
    
}//GEN-LAST:event_use3DModelCheckBoxStateChanged

private void browseModelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseModelButtonActionPerformed
    // Browse for models in local dir
    //String localPathCononical = "data/models/";
    String localPath = "data"+File.separator+ "models" + File.separator;
    String userDir = System.getProperty("user.dir"); // path java was run from
    
    final JFileChooser fc = new JFileChooser(localPath);
    CustomFileFilter xmlFilter = new CustomFileFilter("3ds", "*.3ds");
    CustomFileFilter xmlFilter2 = new CustomFileFilter("obj", "*.obj");
    
    fc.addChoosableFileFilter(xmlFilter2);
    fc.addChoosableFileFilter(xmlFilter); // load last so 3ds is prefered

    int returnVal = fc.showOpenDialog(this);

    if(returnVal == JFileChooser.APPROVE_OPTION)
    {
        try
        {
            File file = fc.getSelectedFile();
            String test = userDir + File.separator + localPath;
            String test2 = file.getAbsolutePath();
            String relPath = RelativePath.getRelativePath(new File(test), new File(test2)); // returns conanical rel path

            threeDModelTextField.setText(relPath);
     
        }
        catch(Exception e)
        {
            System.out.println("ERROR Selecting Model File: " + e.toString());
        }
    } // if file choosen
    
    
}//GEN-LAST:event_browseModelButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyButton;
    private javax.swing.JButton browseModelButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton colorButton;
    private javax.swing.JPanel colorPanel;
    private javax.swing.JRadioButton ecefRadioButton;
    private javax.swing.JRadioButton eciRadioButton;
    private javax.swing.JCheckBox fillFootprintCheckBox;
    private javax.swing.JTextField footPrintResTextField;
    private javax.swing.JCheckBox groundTrack3dCheckBox;
    private javax.swing.JTextField groundTrackResTextField;
    private javax.swing.JCheckBox include2DCheckBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField lagTrackTextField;
    private javax.swing.JTextField leadTrackTextField;
    private javax.swing.JTextField modelScaleTextField;
    private javax.swing.JButton okButton;
    private javax.swing.JCheckBox showFootPrintCheckBox;
    private javax.swing.JCheckBox showGroundTrackCheckBox;
    private javax.swing.JCheckBox showName3DCheckBox;
    private javax.swing.JCheckBox showNameCheckBox;
    private javax.swing.JCheckBox showOrbit3DCheckBox;
    private javax.swing.JCheckBox showfootprint3DCheckBox;
    private javax.swing.JTextField threeDModelTextField;
    private javax.swing.JCheckBox use3DModelCheckBox;
    // End of variables declaration//GEN-END:variables
    
    
    // Save settings to SatProp
    // returns a boolean if Map data needs to be updated
    private boolean saveSettings()
    {
        boolean updateMapData = false;
        // === 2D GRAPHICS =================
        // general
        satProps.setPlot2d( include2DCheckBox.isSelected() );
        satProps.setShowName2D(showNameCheckBox.isSelected() );
        satProps.setSatColor( colorPanel.getBackground() );
                
        // footprint
        satProps.setPlot2DFootPrint( showFootPrintCheckBox.isSelected() );
        try
        {
            satProps.setNumPtsFootPrint( Integer.parseInt(footPrintResTextField.getText()));
        }
        catch(Exception e)
        {System.out.println( e.getMessage() );}
        satProps.setFillFootPrint( fillFootprintCheckBox.isSelected() );

        
        try
        {
            // ground track 
            satProps.setShowGroundTrack(showGroundTrackCheckBox.isSelected());
        }
        catch(Exception e)
        {
            
        }
        
        
        try
        {
            
            int oldGRes = satProps.getGrnTrkPointsPerPeriod();
            
            satProps.setGrnTrkPointsPerPeriod( Integer.parseInt(groundTrackResTextField.getText()) );
            
            if(oldGRes != satProps.getGrnTrkPointsPerPeriod())
            {
                satProps.setGroundTrackIni2False();
                updateMapData = true;
            }
        }
        catch(Exception e)
        {
            //System.out.println( e.getMessage() );
        }
        
        // save old settings to compare to see if update is needed
        double oldLead = satProps.getGroundTrackLeadPeriodMultiplier();
        double oldLag = satProps.getGroundTrackLagPeriodMultiplier();
        
        try
        {
            satProps.setGroundTrackLeadPeriodMultiplier( Double.parseDouble(leadTrackTextField.getText()) );
            
            if( oldLead != satProps.getGroundTrackLeadPeriodMultiplier())
            {
                // set ground tracks ini to false
                satProps.setGroundTrackIni2False();
                updateMapData = true;
            }
        }
        catch(Exception e)
        {System.out.println( e.getMessage() );}
        try
        {
            satProps.setGroundTrackLagPeriodMultiplier( Double.parseDouble(lagTrackTextField.getText()) );
            if( oldLag != satProps.getGroundTrackLagPeriodMultiplier())
            {
                // set ground tracks ini to false
                satProps.setGroundTrackIni2False();
                updateMapData = true;
            }
        }
        catch(Exception e){System.out.println( e.getMessage() );}
        
        
        // 3d view
        satProps.setShow3DOrbitTrace(showOrbit3DCheckBox.isSelected() );
        satProps.setShow3DFootprint(showfootprint3DCheckBox.isSelected());
        satProps.setShow3DName(showName3DCheckBox.isSelected());
        satProps.setShowGroundTrack3d( groundTrack3dCheckBox.isSelected() );
        satProps.setShow3DOrbitTraceECI( eciRadioButton.isSelected() );
        
        // 3d model
        satProps.setUse3dModel( use3DModelCheckBox.isSelected() );
        if(use3DModelCheckBox.isSelected())
        {
            satProps.setThreeDModelPath(threeDModelTextField.getText());
            satProps.setThreeDModelSizeFactor( Double.parseDouble(modelScaleTextField.getText()) );
        }  
        
        return updateMapData;
    } // saveSettings
}
