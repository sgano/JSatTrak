/*
 * TestRPY.java  -- test roll pitch yaw
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
 * Created on July 14, 2008, 11:52 AM
 */

package name.gano.worldwind.modelloader;

import javax.swing.JOptionPane;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import name.gano.astro.MathUtils;
import net.java.joglutils.model.ModelFactory;

/**
 *
 * @author  sgano
 */
public class TestRPY extends javax.swing.JFrame 
{

    private WorldWindowGLCanvas wwd;
    WWModel3D_new satModel;
    
    //BasicModelView3 bmv;
    
    /** Creates new form TestRPY */
    public TestRPY() 
    {
        initComponents();
        
        // add wwj to panel
        // add WWJ to panel
        wwd = new WorldWindowGLCanvas();
        //wwd.setPreferredSize(new java.awt.Dimension(600, 400));
        wwjPanel.add(wwd, java.awt.BorderLayout.CENTER);
        
        Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        // m.setLayers(layerList);
        m.setShowWireframeExterior(false);
        m.setShowWireframeInterior(false);
        m.setShowTessellationBoundingVolumes(false);
        
//        // add political boundary layer
//        m.getLayers().add(new CountryBoundariesLayer());
//
//        // set default layer visabiliy
        for (Layer layer : m.getLayers())
        {

            if (layer instanceof PlaceNameLayer)
            {
                ((PlaceNameLayer) layer).setEnabled(false); // off
            }

        } // for layers
        
        wwd.setModel(m);

        // add model
        try
        {

            Model3DLayer_new layer = new Model3DLayer_new();
//            layer.setMaitainConstantSize(true);
//            layer.setSize(300000);

            //net.java.joglutils.model.geometry.Model model3DS = ModelFactory.createModel("test/data/globalstar/Globalstar.3ds");
            net.java.joglutils.model.geometry.Model model3DS = ModelFactory.createModel("data/models/isscomplete/iss_v2.3ds");
            //net.java.joglutils.model.geometry.Model model3DS = ModelFactory.createModel("data/models/shenzhou_V-full/models/russia.3ds");
            //model3DS.setUseLighting(false); // turn off lighting!
            
//                for (int i=0; i<100; i++) {
//                    layer.addModel(new WWModel3D(model3DS,
//                            new Position(Angle.fromDegrees(generator.nextInt()%80),
//                                         Angle.fromDegrees(generator.nextInt()%180),
//                                         750000)));
//                }
            satModel = new WWModel3D_new(model3DS,
                    new Position(Angle.fromDegrees(0),
                    Angle.fromDegrees(0),
                    750000));
            satModel.setMaitainConstantSize(true);
            satModel.setSize(300000);
            
            layer.addModel(satModel);

            m.getLayers().add(layer);
            
            
            // SET VIEW
             // create and set view
//                //bmv = new BasicModelView2( Position.fromDegrees(0, 0, 750000), wwd.getModel().getGlobe());
//                bmv = new BasicModelView3( ((BasicOrbitView)wwd.getView()).getOrbitViewModel() );
//                wwd.setView( bmv );
//                
//                BasicModelViewInputHandler3 mih = new BasicModelViewInputHandler3();
//                mih.setEventSource(wwd);
//                wwd.setInputHandler( mih );
//                mih.setSmoothViewChanges(true); // FALSE MAKES THE VIEW FAST!!
//                
//                // settings for great closeups!
//                wwd.getView().setNearClipDistance(10000);
//                wwd.getView().setFarClipDistance(5.0E7);
//                bmv.setZoom(900000);
//                bmv.setPitch(Angle.fromDegrees(120));
                
                // create model view input handler, assign current wwd, and set it
//                BasicModelViewInputHandler mih = new BasicModelViewInputHandler();
//                mih.getViewInputBroker().setWorldWindow(wwd);
//                wwd.setInputHandler( mih );
            
//            // manual test
//            OrbitView view = (OrbitView)wwd.getView();
//            view.setCenterPosition(Position.fromDegrees(0, 0, 750000));
//            view.setHeading(Angle.fromDegrees(270));
//            view.setPitch(Angle.fromDegrees(60)); // limit 90?
//            view.setZoom(900000);
//            
//            view.setCenterPosition(Position.fromDegrees(0, 0, 750000));
            
            
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
         
        
        yawSlider.setValue(0);
        pitchSlider.setValue(0);
        rollSlider.setValue(0);
        
        latSlider.setValue(0);
        lonSlider.setValue(0);

    } // TestRPY

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        yawSlider = new javax.swing.JSlider();
        yawText = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        pitchText = new javax.swing.JTextField();
        pitchSlider = new javax.swing.JSlider();
        jLabel3 = new javax.swing.JLabel();
        rollText = new javax.swing.JTextField();
        rollSlider = new javax.swing.JSlider();
        jButton1 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        latText = new javax.swing.JTextField();
        latSlider = new javax.swing.JSlider();
        jLabel5 = new javax.swing.JLabel();
        lonText = new javax.swing.JTextField();
        lonSlider = new javax.swing.JSlider();
        jButton2 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        xBox = new javax.swing.JTextField();
        yBox = new javax.swing.JTextField();
        zBox = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        wwjPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Model Props"));

        jLabel1.setText("Yaw:");

        yawSlider.setMaximum(180);
        yawSlider.setMinimum(-180);
        yawSlider.setValue(1);
        yawSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                yawSliderStateChanged(evt);
            }
        });
        yawSlider.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                yawSliderPropertyChange(evt);
            }
        });

        yawText.setEditable(false);

        jLabel2.setText("Pitch:");

        pitchText.setEditable(false);

        pitchSlider.setMajorTickSpacing(30);
        pitchSlider.setMaximum(180);
        pitchSlider.setMinimum(-180);
        pitchSlider.setValue(1);
        pitchSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                pitchSliderStateChanged(evt);
                pitchSliderStateChanged1(evt);
            }
        });
        pitchSlider.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                pitchSliderPropertyChange(evt);
            }
        });

        jLabel3.setText("Roll:");

        rollText.setEditable(false);

        rollSlider.setMaximum(180);
        rollSlider.setMinimum(-180);
        rollSlider.setValue(1);
        rollSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rollSliderStateChanged(evt);
                rollSliderStateChanged1(evt);
            }
        });
        rollSlider.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                rollSliderPropertyChange(evt);
            }
        });

        jButton1.setText("reset");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel4.setText("Lat:");

        latText.setEditable(false);

        latSlider.setMaximum(90);
        latSlider.setMinimum(-90);
        latSlider.setValue(1);
        latSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                latSliderStateChanged(evt);
                latSliderStateChanged1(evt);
                latSliderStateChanged2(evt);
            }
        });
        latSlider.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                latSliderPropertyChange(evt);
            }
        });

        jLabel5.setText("Lon:");

        lonText.setEditable(false);

        lonSlider.setMaximum(180);
        lonSlider.setMinimum(-180);
        lonSlider.setValue(1);
        lonSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                lonSliderStateChanged(evt);
                lonSliderStateChanged1(evt);
                lonSliderStateChanged2(evt);
                lonSliderStateChanged3(evt);
            }
        });
        lonSlider.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                lonSliderPropertyChange(evt);
            }
        });

        jButton2.setText("reset");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(latText, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(latSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lonText, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lonSlider, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButton2)))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(latText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(11, 11, 11)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel5)
                                    .addComponent(lonText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lonSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(latSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jLabel6.setText("Vel:");

        xBox.setText("1.000");
        xBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xBoxActionPerformed(evt);
            }
        });

        yBox.setText("1.000");

        zBox.setText("1.000");

        jButton3.setText("go");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2)
                            .addComponent(jLabel3))
                        .addGap(8, 8, 8)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(rollText)
                            .addComponent(yawText)
                            .addComponent(pitchText, javax.swing.GroupLayout.DEFAULT_SIZE, 65, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(yawSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                            .addComponent(pitchSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
                            .addComponent(rollSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButton1))
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel6))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(xBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(yBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(zBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButton3)))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabel1)
                                    .addComponent(yawText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(pitchText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(pitchSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(yawSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(rollText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(57, 57, 57)
                        .addComponent(rollSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(xBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(yBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("WWJ"));

        wwjPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(wwjPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(wwjPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

private void yawSliderPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_yawSliderPropertyChange
    //yawText.setText( yawSlider.getValue()+"" );
}//GEN-LAST:event_yawSliderPropertyChange

private void yawSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_yawSliderStateChanged
    yawText.setText(yawSlider.getValue() +"");
    satModel.setYawDeg(yawSlider.getValue()*1.0);
    wwd.redraw();
    
}//GEN-LAST:event_yawSliderStateChanged

private void pitchSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_pitchSliderStateChanged
// TODO add your handling code here:
}//GEN-LAST:event_pitchSliderStateChanged

private void pitchSliderPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_pitchSliderPropertyChange
// TODO add your handling code here:
}//GEN-LAST:event_pitchSliderPropertyChange

private void rollSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rollSliderStateChanged
// TODO add your handling code here:
}//GEN-LAST:event_rollSliderStateChanged

private void rollSliderPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_rollSliderPropertyChange
// TODO add your handling code here:
}//GEN-LAST:event_rollSliderPropertyChange

private void pitchSliderStateChanged1(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_pitchSliderStateChanged1
    pitchText.setText(pitchSlider.getValue() +"");
    satModel.setPitchDeg(pitchSlider.getValue()*1.0);
    double t = satModel.getPitchDeg();
    wwd.redraw();
}//GEN-LAST:event_pitchSliderStateChanged1

private void rollSliderStateChanged1(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rollSliderStateChanged1
    rollText.setText(rollSlider.getValue() +"");
    satModel.setRollDeg(rollSlider.getValue()*1.0);
    double t = satModel.getRollDeg();
    wwd.redraw();
}//GEN-LAST:event_rollSliderStateChanged1

private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
    yawSlider.setValue(0);
    pitchSlider.setValue(0);
    rollSlider.setValue(0);
}//GEN-LAST:event_jButton1ActionPerformed

private void latSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_latSliderStateChanged
// TODO add your handling code here:
}//GEN-LAST:event_latSliderStateChanged

private void latSliderStateChanged1(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_latSliderStateChanged1
// TODO add your handling code here:
}//GEN-LAST:event_latSliderStateChanged1

private void latSliderPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_latSliderPropertyChange
// TODO add your handling code here:
}//GEN-LAST:event_latSliderPropertyChange

private void latSliderStateChanged2(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_latSliderStateChanged2
        latText.setText(latSlider.getValue()+"");
        Position oldPos = satModel.getPosition();
        satModel.setPosition(Position.fromDegrees(latSlider.getValue()*1.0, oldPos.getLongitude().getDegrees(), oldPos.getElevation()));
        //satModel.setPosition(Position.fromDegrees(oldPos.getLatitude().getDegrees(), oldPos.getLongitude().getDegrees(), oldPos.getElevation()));
        wwd.redraw();
}//GEN-LAST:event_latSliderStateChanged2

private void lonSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lonSliderStateChanged
// TODO add your handling code here:
}//GEN-LAST:event_lonSliderStateChanged

private void lonSliderStateChanged1(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lonSliderStateChanged1
// TODO add your handling code here:
}//GEN-LAST:event_lonSliderStateChanged1

private void lonSliderStateChanged2(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lonSliderStateChanged2
// TODO add your handling code here:
}//GEN-LAST:event_lonSliderStateChanged2

private void lonSliderPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_lonSliderPropertyChange
// TODO add your handling code here:
}//GEN-LAST:event_lonSliderPropertyChange

private void lonSliderStateChanged3(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_lonSliderStateChanged3
    lonText.setText(lonSlider.getValue() + "");
    Position oldPos = satModel.getPosition();
    satModel.setPosition(Position.fromDegrees(oldPos.getLatitude().getDegrees(), lonSlider.getValue()*1.0, oldPos.getElevation()));
    wwd.redraw();
}//GEN-LAST:event_lonSliderStateChanged3

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        latSlider.setValue(0);
        lonSlider.setValue(0);
}//GEN-LAST:event_jButton2ActionPerformed

private void xBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xBoxActionPerformed
// TODO add your handling code here:
}//GEN-LAST:event_xBoxActionPerformed

private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        
        double[] orgOrientation = new double[] {0,0,1};
    
        double[] v = new double[3];
        v[0] = Double.parseDouble(xBox.getText());
        v[1] = Double.parseDouble(yBox.getText());
        v[2] = Double.parseDouble(zBox.getText());
        
        double normV = MathUtils.norm(v);
        double normOr = MathUtils.norm(orgOrientation);
        
        double[] unitOrient = new double[] {orgOrientation[0]/normOr,orgOrientation[1]/normOr,orgOrientation[2]/normOr};
        double[] unitV = new double[] {v[0]/normV,v[1]/normV,v[2]/normV};
        
        double angleRad = Math.acos(MathUtils.dot(unitOrient, unitV));
        
        double[] axis = MathUtils.cross(unitOrient, unitV);
        double normAxis = MathUtils.norm(axis);
        
        JOptionPane.showMessageDialog(this, "Angle: " + (angleRad*180.0/Math.PI) + ",\n normAxis: " + normAxis + ",\n Axis: " + axis[0] +"," + axis[1] +"," + axis[2]);
        
        // reorient model
        satModel.angle =     angleRad*180.0/Math.PI;
        satModel.xAxis = axis[0];
        satModel.yAxis = axis[1];
        satModel.zAxis = axis[2];
        
                
}//GEN-LAST:event_jButton3ActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new TestRPY().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
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
    private javax.swing.JSlider latSlider;
    private javax.swing.JTextField latText;
    private javax.swing.JSlider lonSlider;
    private javax.swing.JTextField lonText;
    private javax.swing.JSlider pitchSlider;
    private javax.swing.JTextField pitchText;
    private javax.swing.JSlider rollSlider;
    private javax.swing.JTextField rollText;
    private javax.swing.JPanel wwjPanel;
    private javax.swing.JTextField xBox;
    private javax.swing.JTextField yBox;
    private javax.swing.JSlider yawSlider;
    private javax.swing.JTextField yawText;
    private javax.swing.JTextField zBox;
    // End of variables declaration//GEN-END:variables

}
