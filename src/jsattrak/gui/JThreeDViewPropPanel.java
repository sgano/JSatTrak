/*
 * JThreeDViewPropPanel.java
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
 * Created on October 23, 2007, 9:18 AM
 */

package jsattrak.gui;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import java.awt.Color;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import jsattrak.objects.AbstractSatellite;
import jsattrak.utilities.J3DEarthComponent;

/**
 *
 * @author  sgano
 */
public class JThreeDViewPropPanel extends javax.swing.JPanel
{
    
    private boolean okHit = false; // if okay was hit
    
    JDialog iframe; // used to know what its parent frame is - to close window
    
    JSatTrak app; // used to force repaints
    
    J3DEarthComponent threeDPanel; // parent Panel
    public WorldWindow wwd; // wwj object
    
    /** Creates new form JThreeDViewPropPanel */
    public JThreeDViewPropPanel()
    {
        initComponents();
    }
    
    public JThreeDViewPropPanel(JSatTrak app, J3DEarthComponent threeDPanel, WorldWindow wwd)
    {
        this.app = app;
        this.threeDPanel = threeDPanel;
        this.wwd = wwd;
        
//        //((OrbitView)wwd.getView()).setLatitude(Angle.fromDegrees(-45));
//        //System.out.println("Eye pos: lat: deg:" + ((OrbitView)wwd.getView()).getLatitude().getDegrees());
//        double viewPitch = ((OrbitView) wwd.getView()).getPitch().getDegrees();
//        double viewHeading = ((OrbitView) wwd.getView()).getHeading().getDegrees();
//        System.out.println("pitch:" + viewPitch + ", heading:" + viewHeading);
//        
//        //double viewLat = ((OrbitView) wwd.getView()).getLatitude().getDegrees();
//        double viewLat = ((OrbitView) wwd.getView()).getLookAtLatitude().getDegrees();
//        double viewLon = ((OrbitView) wwd.getView()).getLookAtLongitude().getDegrees();
//        double viewAlt = ((OrbitView) wwd.getView()).getAltitude();
//        System.out.println("lat:" + viewLat + "(" + ((OrbitView) wwd.getView()).getLatitude().getDegrees() + ")"+", lon:" + viewLon + ", alt:"+viewAlt);
        
        initComponents();
        
        //set inital settings
        // === Camera Properties =============
        Angle fov = wwd.getView().getFieldOfView();
        fovTextField.setText("" + fov.getDegrees() );
        
        if(threeDPanel.isViewModeECI())
        {
            eciRadioButton.setSelected(true);
            ecefRadioButton.setSelected(false);
        }
        else
        {
            ecefRadioButton.setSelected(true);
            eciRadioButton.setSelected(false);
        }
        
        if(threeDPanel.isModelViewMode())
        {
            modelViewRadioButton.doClick();
            modeViewComboBox.setSelectedItem(threeDPanel.getModelViewString()); // hope this works?
        }
        else
        {
            earthViewRadioButton.doClick(); // set selected
        }

        smoothViewCheckBox.setSelected(threeDPanel.isSmoothViewChanges());

        // lighting
        if(threeDPanel.isSunShadingOn())
        {
            sunShadingCheckBox.doClick();
        }

        ambientSlider.setValue( threeDPanel.getAmbientLightLevel() );
        flareCheckBox.setSelected( threeDPanel.isLensFlareEnabled() );

        if(threeDPanel.getEcefTimeDepRenderableLayer().isShowTerminatorLine())
        {
            terminatorCheckBox.doClick();
        }

        terminatorColorLabel.setBackground(threeDPanel.getEcefTimeDepRenderableLayer().getTerminator().getColor());

        // Grid
        if(threeDPanel.getEciRadialGrid().isShowGrid())
        {
            eciGridCheckBox.doClick();
        }

        gridColorLabel.setBackground( threeDPanel.getEciRadialGrid().getColor() );

        // Clipping planes
        orbitNearTextField.setText(threeDPanel.getOrbitNearClipDistance()+"");
        orbitFarTextField.setText(threeDPanel.getOrbitFarClipDistance()+"");
        modelNearTextField.setText(threeDPanel.getModelViewNearClip()+"");
        modelFarTextField.setText(threeDPanel.getModelViewFarClip()+"");


    } // JThreeDViewPropPanel
    
    public void setParentDialog(JDialog iframe)
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        fovTextField = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        ecefRadioButton = new javax.swing.JRadioButton();
        eciRadioButton = new javax.swing.JRadioButton();
        smoothViewCheckBox = new javax.swing.JCheckBox();
        applyButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        earthViewRadioButton = new javax.swing.JRadioButton();
        modelViewRadioButton = new javax.swing.JRadioButton();
        modeViewComboBox = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        sunShadingCheckBox = new javax.swing.JCheckBox();
        flareCheckBox = new javax.swing.JCheckBox();
        terminatorCheckBox = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        ambientSlider = new javax.swing.JSlider();
        terminatorColorLabel = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        eciGridCheckBox = new javax.swing.JCheckBox();
        gridColorLabel = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        orbitNearTextField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        orbitFarTextField = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        modelFarTextField = new javax.swing.JTextField();
        modelNearTextField = new javax.swing.JTextField();

        buttonGroup1.add(earthViewRadioButton);
        buttonGroup1.add(modelViewRadioButton);

        setPreferredSize(new java.awt.Dimension(200, 200));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Camera Properties"));

        jLabel1.setText("Field of View [deg]:"); // NOI18N

        fovTextField.setText("0.0"); // NOI18N
        fovTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fovTextFieldActionPerformed(evt);
            }
        });

        jLabel2.setText("Mode:"); // NOI18N

        ecefRadioButton.setSelected(true);
        ecefRadioButton.setText("Earth Centered Earth Fixed"); // NOI18N
        ecefRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ecefRadioButtonActionPerformed(evt);
            }
        });

        eciRadioButton.setText("Earth Centered Inertial"); // NOI18N
        eciRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eciRadioButtonActionPerformed(evt);
            }
        });

        smoothViewCheckBox.setText("Smooth view changes");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fovTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(eciRadioButton)
                            .addComponent(ecefRadioButton)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(smoothViewCheckBox)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(fovTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(ecefRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(eciRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(smoothViewCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Camera Following"));

        earthViewRadioButton.setSelected(true);
        earthViewRadioButton.setText("Earth"); // NOI18N
        earthViewRadioButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                earthViewRadioButtonStateChanged(evt);
            }
        });

        modelViewRadioButton.setText("Object"); // NOI18N
        modelViewRadioButton.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                modelViewRadioButtonStateChanged(evt);
            }
        });

        modeViewComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        modeViewComboBox.setEnabled(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(earthViewRadioButton)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(modelViewRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(modeViewComboBox, 0, 217, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(earthViewRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(modelViewRadioButton)
                    .addComponent(modeViewComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Lighting Effects / Sun"));

        sunShadingCheckBox.setText("Enable Sun Light Shading");

        flareCheckBox.setText("Solar Flare / Sun");

        terminatorCheckBox.setText("Terminator Line");

        jLabel3.setText("Ambient Light Level:");

        ambientSlider.setMajorTickSpacing(25);
        ambientSlider.setMinorTickSpacing(5);
        ambientSlider.setPaintLabels(true);
        ambientSlider.setPaintTicks(true);

        terminatorColorLabel.setBackground(new java.awt.Color(0, 0, 0));
        terminatorColorLabel.setText("  ");
        terminatorColorLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        terminatorColorLabel.setOpaque(true);
        terminatorColorLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                terminatorColorLabelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sunShadingCheckBox)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(flareCheckBox)))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(terminatorCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(terminatorColorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(89, 89, 89))
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(45, 45, 45)
                .addComponent(ambientSlider, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(sunShadingCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(flareCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ambientSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(terminatorCheckBox)
                    .addComponent(terminatorColorLabel)))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Grids"));

        eciGridCheckBox.setText("ECI Grid");

        gridColorLabel.setBackground(new java.awt.Color(0, 128, 0));
        gridColorLabel.setText("  ");
        gridColorLabel.setOpaque(true);
        gridColorLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                gridColorLabelMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(eciGridCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gridColorLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(141, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(eciGridCheckBox)
                .addComponent(gridColorLabel))
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Clipping Planes"));

        jLabel5.setText("Auto Clipping Plane = -1");

        jLabel6.setText("Orbit View [meters]:");

        jLabel7.setText("Near:");

        jLabel8.setText("Far:");

        jLabel9.setText("Model View [meters]:");

        jLabel10.setText("Near:");

        jLabel11.setText("Far:");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel6)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addComponent(jLabel8)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(orbitFarTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE))
                                    .addGroup(jPanel5Layout.createSequentialGroup()
                                        .addComponent(jLabel7)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(orbitNearTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE))))
                            .addComponent(jLabel9)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel11)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(modelFarTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(modelNearTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 187, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(jLabel5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(orbitNearTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(orbitFarTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(modelNearTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(modelFarTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(91, Short.MAX_VALUE)
                .addComponent(applyButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(okButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton)
                .addGap(304, 304, 304))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 12, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(applyButton)
                    .addComponent(okButton)
                    .addComponent(cancelButton)))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void fovTextFieldActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_fovTextFieldActionPerformed
    {//GEN-HEADEREND:event_fovTextFieldActionPerformed
        // TODO add your handling code here:
}//GEN-LAST:event_fovTextFieldActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cancelButtonActionPerformed
    {//GEN-HEADEREND:event_cancelButtonActionPerformed
        // close internal frame
        try
        {
            iframe.dispose(); // could setClosed(true)
        }
        catch(Exception e){}
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_applyButtonActionPerformed
    {//GEN-HEADEREND:event_applyButtonActionPerformed
        // save settings
        boolean updateMaps = saveSettings();
        
        // force repaint
        app.forceRepainting(updateMaps);
    }//GEN-LAST:event_applyButtonActionPerformed

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

    private void ecefRadioButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_ecefRadioButtonActionPerformed
    {//GEN-HEADEREND:event_ecefRadioButtonActionPerformed
        eciRadioButton.setSelected(false);
        ecefRadioButton.setSelected(true);
    }//GEN-LAST:event_ecefRadioButtonActionPerformed

    private void eciRadioButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_eciRadioButtonActionPerformed
    {//GEN-HEADEREND:event_eciRadioButtonActionPerformed
        ecefRadioButton.setSelected(false);
        eciRadioButton.setSelected(true);    
    }//GEN-LAST:event_eciRadioButtonActionPerformed

private void earthViewRadioButtonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_earthViewRadioButtonStateChanged
    
    // if selected - auto set 
    if(earthViewRadioButton.isSelected())
    {
        eciRadioButton.setEnabled(true);
        eciRadioButton.doClick(); // auto pick this
    }
    
    modeViewComboBox.setEnabled(!earthViewRadioButton.isSelected());
}//GEN-LAST:event_earthViewRadioButtonStateChanged

private void modelViewRadioButtonStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_modelViewRadioButtonStateChanged
    
    
    if(modelViewRadioButton.isSelected())
    {
        modeViewComboBox.setEnabled(modelViewRadioButton.isSelected());
        ecefRadioButton.doClick();
        
        eciRadioButton.setEnabled(false); // hide so user can't seletct this (not possible in this view anyways)
    }
    
    // fill in object list with all satellites
    modeViewComboBox.removeAllItems();
    for(AbstractSatellite sat : app.getSatHash().values())
    {
        modeViewComboBox.addItem(sat.getName());
    }
    
}//GEN-LAST:event_modelViewRadioButtonStateChanged

private void terminatorColorLabelMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_terminatorColorLabelMouseClicked
{//GEN-HEADEREND:event_terminatorColorLabelMouseClicked
    // color selector
    Color newColor = JColorChooser.showDialog(
                     this,
                     "Choose Terminator Line Color",
                     terminatorColorLabel.getBackground());

    if(!newColor.equals(terminatorColorLabel.getBackground()))
    {
        terminatorColorLabel.setBackground(newColor);
    }

}//GEN-LAST:event_terminatorColorLabelMouseClicked

private void gridColorLabelMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_gridColorLabelMouseClicked
{//GEN-HEADEREND:event_gridColorLabelMouseClicked
    // color selector
    Color newColor = JColorChooser.showDialog(
                     this,
                     "Choose ECI Grid Line Color",
                     gridColorLabel.getBackground());

    if(!newColor.equals(gridColorLabel.getBackground()))
    {
        gridColorLabel.setBackground(newColor);
    }
}//GEN-LAST:event_gridColorLabelMouseClicked
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider ambientSlider;
    private javax.swing.JButton applyButton;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton cancelButton;
    private javax.swing.JRadioButton earthViewRadioButton;
    private javax.swing.JRadioButton ecefRadioButton;
    private javax.swing.JCheckBox eciGridCheckBox;
    private javax.swing.JRadioButton eciRadioButton;
    private javax.swing.JCheckBox flareCheckBox;
    private javax.swing.JTextField fovTextField;
    private javax.swing.JLabel gridColorLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JComboBox modeViewComboBox;
    private javax.swing.JTextField modelFarTextField;
    private javax.swing.JTextField modelNearTextField;
    private javax.swing.JRadioButton modelViewRadioButton;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField orbitFarTextField;
    private javax.swing.JTextField orbitNearTextField;
    private javax.swing.JCheckBox smoothViewCheckBox;
    private javax.swing.JCheckBox sunShadingCheckBox;
    private javax.swing.JCheckBox terminatorCheckBox;
    private javax.swing.JLabel terminatorColorLabel;
    // End of variables declaration//GEN-END:variables
    
    
    private boolean saveSettings()
    {
        boolean updateMapData = false;
        
        // === Camera Properties =============
        try
        {
            double fov = Double.parseDouble(fovTextField.getText());
            wwd.getView().setFieldOfView(Angle.fromDegrees(fov));
        }
        catch(Exception e){}
        
        
        if(eciRadioButton.isSelected())
        {
            threeDPanel.setViewModeECI(true);
        }
        else
        {
             threeDPanel.setViewModeECI(false);
        }
        
        if(modelViewRadioButton.isSelected())
        {
            threeDPanel.setModelViewString(modeViewComboBox.getSelectedItem().toString()); // save name
            
            threeDPanel.setModelViewMode(true); // do this last!
        }
        else
        {
            threeDPanel.setModelViewMode(false);
        }

        // smooth view
        threeDPanel.setSmoothViewChanges(smoothViewCheckBox.isSelected());

        // sun shading
        threeDPanel.setSunShadingOn( sunShadingCheckBox.isSelected() );
        threeDPanel.setLensFlare(flareCheckBox.isSelected());
        threeDPanel.setAmbientLightLevel( ambientSlider.getValue() ); // causes re-render always

        threeDPanel.getEcefTimeDepRenderableLayer().setShowTerminatorLine(terminatorCheckBox.isSelected());

        threeDPanel.getEcefTimeDepRenderableLayer().getTerminator().setColor(terminatorColorLabel.getBackground());

        // grid
        threeDPanel.getEciRadialGrid().setShowGrid(eciGridCheckBox.isSelected());
        threeDPanel.getEciRadialGrid().setColor( gridColorLabel.getBackground() );


        // Clipping planes
        try
        {
            threeDPanel.setOrbitNearClipDistance(Double.parseDouble(orbitNearTextField.getText().trim()));
            threeDPanel.setOrbitFarClipDistance(Double.parseDouble(orbitFarTextField.getText().trim()));
            threeDPanel.setModelViewNearClip(Double.parseDouble(modelNearTextField.getText().trim()));
            threeDPanel.setModelViewFarClip(Double.parseDouble(modelFarTextField.getText().trim()));
        }
        catch(Exception e)
        {
            System.out.println("Error in reading user input clipping values: " + e.toString());
        }


        return updateMapData;
    } // saveSettings
}
