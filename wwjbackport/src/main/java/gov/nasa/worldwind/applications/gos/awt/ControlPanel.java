/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.awt;

import gov.nasa.worldwind.applications.gos.GeodataKey;
import gov.nasa.worldwind.avlist.AVList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * @author dcollins
 * @version $Id: ControlPanel.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public class ControlPanel extends ActionPanel
{
    protected boolean showSearchOptions = false;
    protected JButton toggleSearchOptionsButton;
    protected JCheckBox showRecordAnnotationsBox;
    protected JCheckBox showRecordBoundsBox;

    public ControlPanel()
    {
        this.toggleSearchOptionsButton = new JButton(new ShowSearchOptionsAction());

        this.showRecordAnnotationsBox = new JCheckBox("Show Annotations", true);
        this.showRecordAnnotationsBox.setOpaque(false);
        this.showRecordAnnotationsBox.addActionListener(this);

        this.showRecordBoundsBox = new JCheckBox("Show Bounds", true);
        this.showRecordBoundsBox.setOpaque(false);
        this.showRecordBoundsBox.addActionListener(this);

        this.setBackground(new Color(240, 247, 249));
        this.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 1, 0, new Color(107, 114, 218)), // top, left, bottom, right
            BorderFactory.createEmptyBorder(5, 5, 5, 5))); // top, left, bottom, right
        this.layoutComponents();
    }

    public void getParams(AVList outParams)
    {
        outParams.setValue(GeodataKey.SHOW_SEARCH_OPTIONS,
            Boolean.toString(this.showSearchOptions));
        outParams.setValue(GeodataKey.SHOW_RECORD_ANNOTATIONS,
            Boolean.toString(this.showRecordAnnotationsBox.isSelected()));
        outParams.setValue(GeodataKey.SHOW_RECORD_BOUNDS,
            Boolean.toString(this.showRecordBoundsBox.isSelected()));
    }

    protected void layoutComponents()
    {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(this.toggleSearchOptionsButton);
        this.add(Box.createHorizontalGlue());
        this.add(this.showRecordAnnotationsBox);
        this.add(Box.createHorizontalStrut(10));
        this.add(this.showRecordBoundsBox);
    }

    protected class ShowSearchOptionsAction extends AbstractAction
    {
        public ShowSearchOptionsAction()
        {
            super("Show Options...");
        }

        public void actionPerformed(ActionEvent event)
        {
            showSearchOptions = true;
            ((AbstractButton) event.getSource()).setAction(new HideSearchOptionsAction());
            fireActionPerformed(event);
        }
    }

    protected class HideSearchOptionsAction extends AbstractAction
    {
        public HideSearchOptionsAction()
        {
            super("Hide Options");
        }

        public void actionPerformed(ActionEvent event)
        {
            showSearchOptions = false;
            ((AbstractButton) event.getSource()).setAction(new ShowSearchOptionsAction());
            fireActionPerformed(event);
        }
    }
}
