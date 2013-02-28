/*
 * JCoverageDialog.java
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
 * Created on April 29, 2008, 1:12 PM
 */

package jsattrak.gui;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import jsattrak.coverage.ColorMap;
import jsattrak.coverage.CoolColorMap;
import jsattrak.coverage.CoverageAnalyzer;
import jsattrak.coverage.GrayColorMap;
import jsattrak.coverage.HotColorMap;
import jsattrak.objects.AbstractSatellite;
import jsattrak.utilities.CustomFileFilter;
import jsattrak.utilities.ProgressStatus;
import jsattrak.utilities.UnoptimizedDeepCopy;
import name.gano.astro.time.Time;

/**
 *
 * @author  sgano
 */
public class JCoverageDialog extends javax.swing.JPanel
{
    Hashtable<String, AbstractSatellite> satHash;
    CoverageAnalyzer ca;
    Vector<J2DEarthPanel> twoDWindowVec;
    Time currentJulianDate;
    JSatTrak app;
    private JInternalFrame iframe;

    /** Creates new form JCoverageDialog */
    public JCoverageDialog(CoverageAnalyzer ca, final Time currentJulianDate, JSatTrak app, Hashtable<String, AbstractSatellite> satHash, Vector<J2DEarthPanel> twoDWindowVec)
    {
        initComponents();

        this.ca = ca;
        this.satHash = satHash;
        this.twoDWindowVec = twoDWindowVec;
        this.currentJulianDate = currentJulianDate;
        this.app = app;

        // display current settings in the dialog
        latLowerField.setText(ca.getLatBounds()[0] + "");
        latUpperField.setText(ca.getLatBounds()[1] + "");
        lonLowerField.setText(ca.getLongBounds()[0] + "");
        lonUpperField.setText(ca.getLongBounds()[1] + "");
        latSegTextField.setText(ca.getLatPanels() + "");
        lonSegTextField.setText(ca.getLongPanels() + "");
        minElevTextField.setText(ca.getElevationLimit() + "");
        // current sats in use:
        for(String name : ca.getSatVector())
        {
            ((DefaultListModel)satIncludedList.getModel()).addElement(name);
        }
        // add current sat list 
        for(AbstractSatellite sat : satHash.values())
        {
            ((DefaultListModel)availSatList.getModel()).addElement(sat.getName());
        }
        dyanmicUpdateCheckBox.setSelected(ca.isDynamicUpdating());

        // viz options
        alphaLabel.setText((int)Math.round(ca.getAlpha() * 100.0 / 255) + "");
        alphaSlider.setValue((int)Math.round(ca.getAlpha() * 100.0 / 255));
        showGridCheckBox.setSelected(ca.isPlotCoverageGrid());
        showColorBarCheckBox.setSelected(ca.isShowColorBar());
        ColorMap map = ca.getColorMap(); // jet cool hot gray

        if(map instanceof CoolColorMap)
        {
            colorMapComboBox.setSelectedIndex(1);
        }
        else if(map instanceof HotColorMap)
        {
            colorMapComboBox.setSelectedIndex(2);
        }
        else if(map instanceof GrayColorMap)
        {
            colorMapComboBox.setSelectedIndex(3);
        }
        else //default

        {
            colorMapComboBox.setSelectedIndex(0);
        }

        // 2D windows
        Vector<Integer> selectedWindows = new Vector<Integer>();
        int i = 0;
        for(J2DEarthPanel ep : twoDWindowVec)
        {
            ((DefaultListModel)twoDWindowList.getModel()).addElement(ep.getName());
            // set if it should be selected?
            if(ep.checkIfIncludesRenderableObject(ca))
            {
                selectedWindows.add(i);
            }
            i++;
        } // for 2D windows

        int[] sel = new int[selectedWindows.size()];
        for(int j = 0; j < selectedWindows.size(); j++)
        {
            sel[j] = selectedWindows.get(j);
        }

        twoDWindowList.setSelectedIndices(sel);
        
        // put date in auto run text box
        startTextField.setText(app.getCurrentJulianDay().getDateTimeStr());


    } // JCoverageDialog


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        dyanmicUpdateCheckBox = new javax.swing.JCheckBox();
        clearDataButton = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        latLowerField = new javax.swing.JTextField();
        latUpperField = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        lonLowerField = new javax.swing.JTextField();
        lonUpperField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        latSegTextField = new javax.swing.JTextField();
        lonSegTextField = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        satIncludedList = new javax.swing.JList();
        addSatButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        availSatList = new javax.swing.JList();
        removeSatButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        minElevTextField = new javax.swing.JTextField();
        jPanel4 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        alphaSlider = new javax.swing.JSlider();
        alphaLabel = new javax.swing.JLabel();
        showGridCheckBox = new javax.swing.JCheckBox();
        showColorBarCheckBox = new javax.swing.JCheckBox();
        jLabel9 = new javax.swing.JLabel();
        colorMapComboBox = new javax.swing.JComboBox();
        colorMapLabel = new jsattrak.coverage.ColorMapLabel();
        jPanel11 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        twoDWindowList = new javax.swing.JList();
        none2DButton = new javax.swing.JButton();
        all2DButton = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        startTextField = new javax.swing.JTextField();
        stopTextField = new javax.swing.JTextField();
        runButton = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTextArea2 = new javax.swing.JTextArea();
        runProgressBar = new javax.swing.JProgressBar();
        jPanel13 = new javax.swing.JPanel();
        refreshButton = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        exportDataButton = new javax.swing.JButton();
        jScrollPane6 = new javax.swing.JScrollPane();
        dataSummaryTextArea = new javax.swing.JTextArea();

        setPreferredSize(new java.awt.Dimension(500, 300));
        setLayout(new java.awt.BorderLayout());

        jPanel2.setBackground(new java.awt.Color(204, 204, 204));

        dyanmicUpdateCheckBox.setBackground(new java.awt.Color(204, 204, 204));
        dyanmicUpdateCheckBox.setText("Dynamic Coverage Updates");
        dyanmicUpdateCheckBox.setToolTipText("Update Coverage Data as Time is Animated Forward");

        clearDataButton.setText("Reset/Clear Data");
        clearDataButton.setToolTipText("Clear and reset coverage data");
        clearDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearDataButtonActionPerformed(evt);
            }
        });

        jButton2.setText("Cancel");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        okButton.setText("Ok");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        jButton4.setText("Apply");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(dyanmicUpdateCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(clearDataButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 83, Short.MAX_VALUE)
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(okButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(dyanmicUpdateCheckBox)
                .addComponent(clearDataButton)
                .addComponent(jButton2)
                .addComponent(okButton)
                .addComponent(jButton4))
        );

        add(jPanel2, java.awt.BorderLayout.SOUTH);

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Coverage Grid"));

        jLabel3.setText("Latutide Bounds [min][max]");

        jLabel4.setText("Longitude Bounds [min][max]");

        jLabel5.setText("Number of Lat/Long Segments:");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(latLowerField, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(latUpperField, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel4)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lonLowerField, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lonUpperField, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel5)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(latSegTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lonSegTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(latLowerField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(latUpperField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lonLowerField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lonUpperField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(latSegTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lonSegTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Satellite Selection:"));

        satIncludedList.setModel(new DefaultListModel());
        jScrollPane1.setViewportView(satIncludedList);

        addSatButton.setText("<");
        addSatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSatButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Satellites Included:");

        availSatList.setModel(new DefaultListModel());
        jScrollPane2.setViewportView(availSatList);

        removeSatButton.setText(">");
        removeSatButton.setToolTipText("Remove Satellites from Coverage Analysis");
        removeSatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSatButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("Available:");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 136, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addSatButton)
                            .addComponent(removeSatButton)))
                    .addComponent(jLabel1))
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel2))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(83, 83, 83)
                        .addComponent(addSatButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeSatButton))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 198, Short.MAX_VALUE))
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGap(3, 3, 3)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)))))
                .addContainerGap())
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Elevation Constraints"));

        jLabel6.setText("Minimum Elevation [deg]:");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel6)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(minElevTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(39, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(minElevTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Coverage Analysis Setup", jPanel3);

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("Viz Options"));

        jLabel7.setText("Data plot transparency:");
        jLabel7.setToolTipText("0=invisible, 100=solid");

        alphaSlider.setMajorTickSpacing(25);
        alphaSlider.setMinorTickSpacing(5);
        alphaSlider.setPaintLabels(true);
        alphaSlider.setPaintTicks(true);
        alphaSlider.setToolTipText("");
        alphaSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                alphaSliderStateChanged(evt);
            }
        });

        alphaLabel.setText("000");

        showGridCheckBox.setText("Show Grid");

        showColorBarCheckBox.setText("Show Colorbar");

        jLabel9.setText("Colormap:");

        colorMapComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Jet", "Cool", "Hot", "Gray" }));
        colorMapComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorMapComboBoxActionPerformed(evt);
            }
        });

        colorMapLabel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(alphaSlider, 0, 0, Short.MAX_VALUE))
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jLabel7)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(alphaLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(showGridCheckBox)
                .addContainerGap())
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(showColorBarCheckBox)
                .addContainerGap())
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorMapComboBox, 0, 88, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(colorMapLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(alphaLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(alphaSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(showGridCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(showColorBarCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(colorMapComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorMapLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE))
        );

        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("2D Windows"));

        jLabel8.setText("Display Data In:");

        twoDWindowList.setModel(new DefaultListModel());
        jScrollPane3.setViewportView(twoDWindowList);

        none2DButton.setText("None");
        none2DButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                none2DButtonActionPerformed(evt);
            }
        });

        all2DButton.setText("All");
        all2DButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                all2DButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(jLabel8)
                .addContainerGap(88, Short.MAX_VALUE))
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(none2DButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(all2DButton)
                .addGap(13, 13, 13))
            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 165, Short.MAX_VALUE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(none2DButton)
                    .addComponent(all2DButton)))
        );

        jTextArea1.setBackground(new java.awt.Color(255, 255, 204));
        jTextArea1.setColumns(20);
        jTextArea1.setEditable(false);
        jTextArea1.setFont(new java.awt.Font("Arial", 0, 10));
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText("Note: Coverage Data in 3D windows is controlled by that window's Globe Layer options.");
        jTextArea1.setWrapStyleWord(true);
        jScrollPane4.setViewportView(jTextArea1);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(74, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(11, 11, 11))
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Data Visualization Options", jPanel4);

        jLabel10.setText("Start Time:");

        jLabel11.setText("Stop Time:");

        runButton.setText("Run");
        runButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                runButtonActionPerformed(evt);
            }
        });

        jTextArea2.setBackground(new java.awt.Color(255, 255, 204));
        jTextArea2.setColumns(20);
        jTextArea2.setEditable(false);
        jTextArea2.setFont(new java.awt.Font("Arial", 0, 10));
        jTextArea2.setLineWrap(true);
        jTextArea2.setRows(5);
        jTextArea2.setText("Run: atomatically clears the current data, turns off dynamic updates, saves any changes made in this dialog, and runs a coverage anylsis between the above times using the current timestep.");
        jTextArea2.setWrapStyleWord(true);
        jScrollPane5.setViewportView(jTextArea2);

        runProgressBar.setStringPainted(true);

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 277, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(startTextField)
                            .addComponent(stopTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)))
                    .addGroup(jPanel12Layout.createSequentialGroup()
                        .addComponent(runButton)
                        .addGap(30, 30, 30)
                        .addComponent(runProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(232, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel10)
                    .addComponent(startTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(stopTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(runButton)
                    .addComponent(runProgressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(77, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jTabbedPane1.addTab("Analysis Autorun", jPanel5);

        refreshButton.setText("Refresh Summary");
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        jLabel12.setText("Current Data Summary:");

        exportDataButton.setText("Export Data");
        exportDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportDataButtonActionPerformed(evt);
            }
        });

        dataSummaryTextArea.setColumns(1);
        dataSummaryTextArea.setRows(1);
        dataSummaryTextArea.setTabSize(4);
        jScrollPane6.setViewportView(dataSummaryTextArea);

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(exportDataButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(refreshButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(116, Short.MAX_VALUE))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addComponent(refreshButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(exportDataButton))
                    .addComponent(jScrollPane6, javax.swing.GroupLayout.DEFAULT_SIZE, 194, Short.MAX_VALUE))
                .addGap(42, 42, 42))
        );

        jTabbedPane1.addTab("Current Data", jPanel13);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 544, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 295, Short.MAX_VALUE)
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

private void colorMapComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorMapComboBoxActionPerformed
    // set the choosen color map to the color map label
    switch(colorMapComboBox.getSelectedIndex())
    {
        case 0:
            colorMapLabel.setColorMap(new ColorMap()); //jet

            break;
        case 1:
            colorMapLabel.setColorMap(new CoolColorMap()); // cool

            break;
        case 2:
            colorMapLabel.setColorMap(new HotColorMap()); // hot

            break;
        case 3:
            colorMapLabel.setColorMap(new GrayColorMap()); // gray

            break;
    }// switch
}//GEN-LAST:event_colorMapComboBoxActionPerformed

private void alphaSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_alphaSliderStateChanged
    alphaLabel.setText(alphaSlider.getValue() + "");
}//GEN-LAST:event_alphaSliderStateChanged

private void removeSatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSatButtonActionPerformed
    // remove elments from the list
    int[] selected = satIncludedList.getSelectedIndices();
    for(int i = selected.length - 1; i >= 0; i--)
    {
        ((DefaultListModel)satIncludedList.getModel()).remove(selected[i]);
    }
}//GEN-LAST:event_removeSatButtonActionPerformed

private void addSatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSatButtonActionPerformed
    int[] selected = availSatList.getSelectedIndices();
    for(int i = 0; i < selected.length; i++)
    {
        String satName = ((DefaultListModel)availSatList.getModel()).get(selected[i]).toString();
        if(!checkIfStringInList(satName, ((DefaultListModel)satIncludedList.getModel()).toArray()))
        {
            ((DefaultListModel)satIncludedList.getModel()).addElement(satName);
        }
    }
}//GEN-LAST:event_addSatButtonActionPerformed

private void none2DButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_none2DButtonActionPerformed
    twoDWindowList.setSelectedIndices(new int[0]);
}//GEN-LAST:event_none2DButtonActionPerformed

private void all2DButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_all2DButtonActionPerformed
    // select all
    // Select all the items
    int start = 0;
    int end = twoDWindowList.getModel().getSize() - 1;
    if(end >= 0)
    {
        twoDWindowList.setSelectionInterval(start, end);
    }
}//GEN-LAST:event_all2DButtonActionPerformed

private void clearDataButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearDataButtonActionPerformed
    // ask to make sure user wants to do this
    int n = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to clear the coverage data?",
            "Clear Data?",
            JOptionPane.YES_NO_OPTION);
    if(n > 0)
    {
        return; // don't do anything

    }
    // if so clear data
    // if dynamic updating selected, save MJD to coverage analysis
    clearCoverageData();
    System.out.println("Coverage Data Cleared: New Panel Grid = " + ca.getLatPanels() + " x " + ca.getLongPanels());

    app.forceRepainting(false);
}//GEN-LAST:event_clearDataButtonActionPerformed

private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
    saveSettings();
    app.forceRepainting(false);

    // close internal frame
    try
    {
        iframe.dispose(); // could setClosed(true)

    }
    catch(Exception e)
    {
    }
}//GEN-LAST:event_okButtonActionPerformed

private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
    saveSettings();
    app.forceRepainting(false);
}//GEN-LAST:event_jButton4ActionPerformed

private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    // close internal frame
    try
    {
        iframe.dispose(); // could setClosed(true)

    }
    catch(Exception e)
    {
    }
}//GEN-LAST:event_jButton2ActionPerformed

private void runButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_runButtonActionPerformed
    // first get dates from text fields
    GregorianCalendar startCal = getDateFromText(startTextField.getText());
    if(startCal == null)
    {
        JOptionPane.showMessageDialog(app, "Start Date is invalid", "Invalid Date", JOptionPane.WARNING_MESSAGE);
        return;
    }
    final Time startJulianDate = new Time();
    startJulianDate.set(startCal.getTimeInMillis());

    GregorianCalendar stopCal = getDateFromText(stopTextField.getText());
    if(stopCal == null)
    {
        JOptionPane.showMessageDialog(app, "Stop Date is invalid", "Invalid Date", JOptionPane.WARNING_MESSAGE);
        return;
    }
    final Time stopJulianDate = new Time();
    stopJulianDate.set(stopCal.getTimeInMillis());

    // make sure stop is after start
    if(stopJulianDate.getMJD() <= startJulianDate.getMJD())
    {
        JOptionPane.showMessageDialog(app, "Stop Date must be after Start Date", "Invalid Date", JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    final Hashtable<String, AbstractSatellite> tempSatHash = (Hashtable<String, AbstractSatellite>)UnoptimizedDeepCopy.copy(app.getSatHash());

    final double timeStep = app.getCurrentTimeStep(); // timestep to use in calculations SECONDS

    // okay we are ready to begin, turn off dynamic coverage, save changes, clear data
    dyanmicUpdateCheckBox.setSelected(false);
    this.saveSettings();
    ca.setDynamicUpdating(false); // in sure false
    ca.clearCoverageData(startJulianDate);

    // create a thread to do calulations in background
    SwingWorker<Object, Integer> worker = new SwingWorker<Object, Integer>()
    {
       @Override
        public Object doInBackground() // Vector<String>

        {
            // perform all of the process in the background!
            // what happens when sat isn't propogated as far as date?
            // make a new Time object - to track progress

            Time currentTime = new Time();
            currentTime.set(startJulianDate.getCurrentGregorianCalendar().getTimeInMillis());

            currentTime.addSeconds(timeStep);// add first time step (inital time already included in clear data)
            while(currentTime.getMJD() <= stopJulianDate.getMJD())
            {
                // update position of satellites
                for(AbstractSatellite sat : tempSatHash.values())
                {
                    sat.propogate2JulDate(currentTime.getJulianDate());
                } // propgate each sat
                
                // update coverage
                ca.performCoverageAnalysis(currentTime, tempSatHash);
                
                //Update progress bar
                publish( (int)Math.round(100*(1.0-(stopJulianDate.getMJD()-currentTime.getMJD())/(stopJulianDate.getMJD()-startJulianDate.getMJD()))) );
                //runProgressBar.setValue( (int)Math.round(100*(1.0-(stopJulianDate.getMJD()-currentTime.getMJD())/(stopJulianDate.getMJD()-startJulianDate.getMJD()))) );
                
                // increment time
                currentTime.addSeconds(timeStep);
            } // for time loop

            // now propogate all satellites to the current time  
  
            // NEED TO ADD TIME START AND STOP TO CA OBJECT - to SAVE IN DIALOG AND SO USER KNOWS!!!!


            return null;
        } //doInBackground

       // runs every once in a while to update GUI, use publish( int ) and the int will be added to the List
        @Override
        protected void process(List<Integer> chunks)
        {
            int val = chunks.get(chunks.size() - 1);

            runProgressBar.setValue( val );
            runProgressBar.repaint();

        }

         @Override
        protected void done()
        {
            runProgressBar.setValue(0); // update progress bar

            app.forceRepainting();
        } // done -- update GUI at the finish of process

    }; // swing worker

    worker.execute(); // run swing worker

}//GEN-LAST:event_runButtonActionPerformed

private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
    // list data summary to text area
    dataSummaryTextArea.setText(""); // clear text
    
    String summaryText = "";
    
    summaryText += "Lat Bounds:  " + ca.getLatBounds()[0] + ", " + ca.getLatBounds()[1] +"\n";
    summaryText += "Long Bounds: " + ca.getLongBounds()[0] + ", " + ca.getLongBounds()[1] +"\n";
    
    summaryText += "Lat/Long Segments:  " + ca.getLatPanels() +", " + ca.getLongPanels() +"\n";
    
    summaryText += "Min. Elevation Constraint [deg]: " + ca.getElevationLimit() +"\n";
    
    summaryText += "\n";
    summaryText += "Coverage Start Time: " + ca.getStartTime().getDateTimeStr() +"\n";
    summaryText += "Coverage Stop Time [MJD]: " + ca.getLastMJD() +"\n";
    summaryText += "Coverage Time Span [days]: " + (ca.getLastMJD()-ca.getStartTime().getMJD()) +"\n";
    
    summaryText += "\nSatellite List:\n";
    summaryText += "-------------------\n";
    for(String sat : ca.getSatVector())
    {
        summaryText += sat.trim() + "\n";
    }
    
    
    // set text
    dataSummaryTextArea.setText(summaryText);
    dataSummaryTextArea.setCaretPosition(0); // start at top
    
}//GEN-LAST:event_refreshButtonActionPerformed

private void exportDataButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportDataButtonActionPerformed
    // popup file chooser .dat
        final JFileChooser fc = new JFileChooser(app.getFileSaveAs());
        CustomFileFilter xmlFilter = new CustomFileFilter("dat","*.dat");
        fc.addChoosableFileFilter(xmlFilter);
        
        int returnVal = fc.showSaveDialog(this);
        
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            File file = fc.getSelectedFile();
            
            String extension = JSatTrak.getExtension(file);
            String fileSaveAs; 
            if (extension != null)
            {
                fileSaveAs = file.getAbsolutePath();
            }
            else
            {
                // append the extension
                file = new File(file.getAbsolutePath() + ".dat");
                fileSaveAs = file.getAbsolutePath();
            }
            
            fileSaveAs = file.getAbsolutePath();
            
            // export text file (mid points) lat [deg] \t long [deg] \t coverage time [sec]
            try
            {
                PrintWriter out = new PrintWriter(new FileWriter(fileSaveAs));
                
                // first line
                out.println("Latitude [deg] \t Longitude [deg] \t Coverage Time [sec]");
                
                for(int i = 0; i<ca.getLatPanels(); i++)
                {
                    for(int j=0; j<ca.getLongPanels(); j++)
                    {
                        out.println(ca.getLatPanelMidPoints()[i] + " \t" + ca.getLonPanelMidPoints()[j] + " \t" + ca.getCoverageCumTime()[i][j]);
                    }
                }
                
                out.close();
            }
            catch(Exception e)
            {
                JOptionPane.showMessageDialog(app, "Error writing to file: " + e.toString(), "ERROR", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
        } // if approve
    
    
    
    
}//GEN-LAST:event_exportDataButtonActionPerformed

    public void clearCoverageData()
    {
        if(ca.isDynamicUpdating())
        {
            ca.clearCoverageData(currentJulianDate);
        }
        else
        {
            ca.clearCoverageData();
        }
    }

    private boolean checkIfStringInList(String str, Object[] list)
    {
        for(Object o : list)
        {
            if(str.equalsIgnoreCase(o.toString()))
            {
                return true;
            }
        }
        return false;
    } // checkIfStringInList

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSatButton;
    private javax.swing.JButton all2DButton;
    private javax.swing.JLabel alphaLabel;
    private javax.swing.JSlider alphaSlider;
    private javax.swing.JList availSatList;
    private javax.swing.JButton clearDataButton;
    private javax.swing.JComboBox colorMapComboBox;
    private jsattrak.coverage.ColorMapLabel colorMapLabel;
    private javax.swing.JTextArea dataSummaryTextArea;
    private javax.swing.JCheckBox dyanmicUpdateCheckBox;
    private javax.swing.JButton exportDataButton;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextArea jTextArea2;
    private javax.swing.JTextField latLowerField;
    private javax.swing.JTextField latSegTextField;
    private javax.swing.JTextField latUpperField;
    private javax.swing.JTextField lonLowerField;
    private javax.swing.JTextField lonSegTextField;
    private javax.swing.JTextField lonUpperField;
    private javax.swing.JTextField minElevTextField;
    private javax.swing.JButton none2DButton;
    private javax.swing.JButton okButton;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton removeSatButton;
    private javax.swing.JButton runButton;
    private javax.swing.JProgressBar runProgressBar;
    private javax.swing.JList satIncludedList;
    private javax.swing.JCheckBox showColorBarCheckBox;
    private javax.swing.JCheckBox showGridCheckBox;
    private javax.swing.JTextField startTextField;
    private javax.swing.JTextField stopTextField;
    private javax.swing.JList twoDWindowList;
    // End of variables declaration//GEN-END:variables

    private void saveSettings()
    {
        boolean requiresGridRegen = false; // if a a grid regen is needed
        // save first tab

        try
        {
            // display current settings in the dialog
            double[] latBounds = new double[]
            {
                Double.parseDouble(latLowerField.getText()), Double.parseDouble(latUpperField.getText())
            };
            double[] lonBounds = new double[]
            {
                Double.parseDouble(lonLowerField.getText()), Double.parseDouble(lonUpperField.getText())
            };

            // check if regen needed
            if(latBounds[0] != ca.getLatBounds()[0] || latBounds[1] != ca.getLatBounds()[1])
            {
                requiresGridRegen = true;
            }
            if(lonBounds[0] != ca.getLongBounds()[0] || lonBounds[1] != ca.getLongBounds()[1])
            {
                requiresGridRegen = true;
            }

            ca.setLatBounds(latBounds);
            ca.setLongBounds(lonBounds);

            int latSeg = Integer.parseInt(latSegTextField.getText());
            int lonSeg = Integer.parseInt(lonSegTextField.getText());

            if(latSeg != ca.getLatPanels() || lonSeg != ca.getLongPanels())
            {
                requiresGridRegen = true;
            }

            ca.setLatPanels(latSeg);
            ca.setLongPanels(lonSeg);

            double ele = Double.parseDouble(minElevTextField.getText());
            if(ele != ca.getElevationLimit())
            {
                requiresGridRegen = true;
            }
            ca.setElevationLimit(ele);

            //-------------------------------------

            // for each element in satIncludedList
            ca.clearSatCoverageVector(); // clear it first

            for(Object i : ((DefaultListModel)satIncludedList.getModel()).toArray())
            {
                // only adds if isn't already in list
                String satName = i.toString();//((DefaultListModel)satIncludedList.getModel()).get(i).toString();

                ca.addSatToCoverageAnaylsis(satName);
            }

            // dynamic updating
            ca.setDynamicUpdating(dyanmicUpdateCheckBox.isSelected());


            // viz options ------------
            ca.setAlpha((int)Math.round(Integer.parseInt(alphaLabel.getText()) / 100.0 * 255));

            ca.setPlotCoverageGrid(showGridCheckBox.isSelected());
            ca.setShowColorBar(showColorBarCheckBox.isSelected());

            switch(colorMapComboBox.getSelectedIndex())
            {
                case 0:
                    ca.setColorMap(new ColorMap());
                    break;
                case 1:
                    ca.setColorMap(new CoolColorMap());
                    break;
                case 2:
                    ca.setColorMap(new HotColorMap());
                    break;
                case 3:
                    ca.setColorMap(new GrayColorMap());
                    break;
            } // colormap switch

            // 2D windows save which one(s) to use as a display
            // twoDWindowList

            for(J2DEarthPanel ep : twoDWindowVec)
            {
                String twoDWindowName = ep.getName();

                boolean windowFound = false;

                // find this window -- see if it contains ca, if not add ca to it
                for(int i : twoDWindowList.getSelectedIndices())
                {
                    String windowName = ((DefaultListModel)twoDWindowList.getModel()).get(i).toString();

                    if(ep.getName().equals(windowName))
                    {
                        windowFound = true;
                        if(!ep.checkIfIncludesRenderableObject(ca))
                        {
                            ep.addRenderableObject(ca); // add it

                        }
                    }// match 

                } // for windows

                // see if window was found in list, if not remove it if it includes the ca
                if(!windowFound)
                {
                    if(ep.checkIfIncludesRenderableObject(ca))
                    {
                        ep.removeRenderableObject(ca);
                    }
                }

            } // for selected window names


        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        // clear and redo grid?
        if(requiresGridRegen)
        {
            this.clearCoverageData();
        }



    } // saveSettings


    public JInternalFrame getIframe()
    {
        return iframe;
    }

    public void setIframe(JInternalFrame iframe)
    {
        this.iframe = iframe;
    }

    // returns null if date is formatted incorrectly, otherwise gives date
    private GregorianCalendar getDateFromText(String dateText)
    {
        GregorianCalendar currentTimeDate = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        //or
        //GregorianCalendar currentTimeDate = new GregorianCalendar();

        SimpleDateFormat dateformat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS z");
        SimpleDateFormat dateformatShort1 = new SimpleDateFormat("dd MMM y H:m:s.S z");
        SimpleDateFormat dateformatShort2 = new SimpleDateFormat("dd MMM y H:m:s z"); // no Milliseconds

        //boolean dateAccepted = true; // assume date valid at first
        try
        {
            currentTimeDate.setTime(dateformatShort1.parse(dateText));
        //timeTextField.setText(dateformat.format(currentTimeDate.getTime()));
        }
        catch(Exception e2)
        {
            try
            {
                // try reading without the milliseconds
                currentTimeDate.setTime(dateformatShort2.parse(dateText));
            //timeTextField.setText(dateformat.format(currentTimeDate.getTime()));
            }
            catch(Exception e3)
            {
                // bad date input put back the old date string
                //timeTextField.setText( app.getScenarioEpochDate().getDateTimeStr() );
                //dateAccepted = false;
                return null;
            //System.out.println(" -- Rejected");
            } // catch 2

        } // catch 1

        return currentTimeDate;
    } //getDateFromText

}
