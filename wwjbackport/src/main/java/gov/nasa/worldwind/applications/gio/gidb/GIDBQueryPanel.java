/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.gidb;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.applications.gio.catalogui.CatalogKey;
import gov.nasa.worldwind.applications.gio.catalogui.QueryModel;
import gov.nasa.worldwind.applications.gio.catalogui.SwingUtils;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import java.awt.*;

/**
 * @author dcollins
 * @version $Id: GIDBQueryPanel.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class GIDBQueryPanel extends JPanel
{
    private QueryModel model;
    private GIDBQueryController controller;
    private JButton queryButton;
    private JComboBox keywordsBox;

    public GIDBQueryPanel(QueryModel model)
    {
        this.model = model;
        this.controller = new GIDBQueryController(this);
        this.model.addPropertyChangeListener(this.controller);
        makeDefaultParams(this.model);

        makeComponents();
        layoutComponents();
        this.controller.synchronizeView();
    }

    public QueryModel getModel()
    {
        return this.model;
    }

    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        this.queryButton.setEnabled(enabled);
        this.keywordsBox.setEnabled(enabled);

        if (enabled)
        {
            this.controller.synchronizeView();
        }
    }

    public JButton getQueryButton()
    {
        return this.queryButton;
    }

    JComboBox getKeywordsBox()
    {
        return this.keywordsBox;
    }

    private void makeComponents()
    {
        this.queryButton = new JButton("Search");
        this.keywordsBox = new JComboBox(new Object[] {""});
        this.keywordsBox.setEditable(true);

        this.queryButton.setActionCommand(CatalogKey.ACTION_COMMAND_QUERY);
        this.keywordsBox.setActionCommand(CatalogKey.KEYWORD_TEXT);

        this.queryButton.addActionListener(this.controller);
        this.keywordsBox.addActionListener(this.controller);
    }

    private void layoutComponents()
    {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JPanel panel = new JPanel();
        {
            panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
            SwingUtils.constrainMaximumHeight(this.keywordsBox);
            panel.add(this.keywordsBox);
            panel.add(Box.createHorizontalStrut(5));
            panel.add(this.queryButton);
        }
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(panel);
    }

    private static AVList makeDefaultParams(AVList params)
    {
        if (params == null)
        {
            String message = Logging.getMessage("nullValue.AVListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Object o = Configuration.getStringValue(CatalogKey.KEYWORD_TEXT, "");
        if (o != null)
            params.setValue(CatalogKey.KEYWORD_TEXT, o);

        return params;
    }
}
