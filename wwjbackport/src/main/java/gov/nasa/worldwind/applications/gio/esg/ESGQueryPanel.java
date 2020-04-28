/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.applications.gio.catalogui.*;
import gov.nasa.worldwind.geom.Angle;

import javax.swing.*;
import java.awt.*;
import java.util.Date;

/**
 * @author dcollins
 * @version $Id: ESGQueryPanel.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class ESGQueryPanel extends JPanel
{
    private ESGQueryModel model;
    private ESGQueryController controller;
    private JButton queryButton;
    private JComboBox keywordsBox;
    private JCheckBox minDateCheckBox;
    private JCheckBox maxDateCheckBox;
    private DateSpinner minDateSpinner;
    private DateSpinner maxDateSpinner;
    private JCheckBox boundsCheckBox;
    private JCheckBox linkWithWWJViewCheckBox;
    private AngleSpinner minLatitudeSpinner;
    private AngleSpinner maxLatitudeSpinner;
    private AngleSpinner minLongitudeSpinner;
    private AngleSpinner maxLongitudeSpinner;
    private JCheckBox wmsCheckBox;
    private JCheckBox wfsCheckBox;
    private JCheckBox wcsCheckBox;
    private JButton toggleButton;
    private JPanel advancedPanel;
    private JLabel minLatitudeLabel;
    private JLabel maxLatitudeLabel;
    private JLabel minLongitudeLabel;
    private JLabel maxLongitudeLabel;

    public ESGQueryPanel(ESGQueryModel model)
    {
        this.model = model;
        this.controller = new ESGQueryController(this);
        this.model.addPropertyChangeListener(this.controller);
        
        makeComponents();
        layoutComponents();
        this.controller.synchronizeView();
    }

    public QueryModel getModel()
    {
        return this.model;
    }

    public String getKeywordText()
    {
        return this.model.getKeywordText();
    }

    public void setKeywordText(String newValue)
    {
        this.model.setKeywordText(newValue);
    }

    public boolean isMinDateEnabled()
    {
        Boolean b = this.model.isMinDateEnabled();
        return b != null && b;
    }

    public void setMinDateEnabled(boolean newValue)
    {
        this.model.setMinDateEnabled(newValue);
    }

    public boolean isMaxDateEnabled()
    {
        Boolean b = this.model.isMaxDateEnabled();
        return b != null && b;
    }

    public void setMaxDateEnabled(boolean newValue)
    {
        this.model.setMaxDateEnabled(newValue);
    }

    public Date getMinDate()
    {
        return this.model.getMinDate();
    }

    public void setMinDate(Date newValue)
    {
        this.model.setMinDate(newValue);
    }

    public Date getMaxDate()
    {
        return this.model.getMaxDate();
    }

    public void setMaxDate(Date newValue)
    {
        this.model.setMaxDate(newValue);
    }

    public boolean isBboxEnabled()
    {
        Boolean b = this.model.isBboxEnabled();
        return b != null && b;
    }

    public void setBboxEnabled(boolean newValue)
    {
        this.model.setBboxEnabled(newValue);
    }

    public boolean isLinkWithWWJView()
    {
        Boolean b = this.model.isLinkWithWWJView();
        return b != null && b;
    }

    public void setLinkWithWWJView(boolean newValue)
    {
        this.model.setLinkWithWWJView(newValue);
    }

    public Angle getMinLatitude()
    {
        return this.model.getMinLatitude();
    }

    public void setMinLatitude(Angle newValue)
    {
        this.model.setMinLatitude(newValue);
    }

    public Angle getMaxLatitude()
    {
        return this.model.getMaxLatitude();
    }

    public void setMaxLatitude(Angle newValue)
    {
        this.model.setMaxLatitude(newValue);
    }

    public Angle getMinLongitude()
    {
        return this.model.getMinLongitude();
    }

    public void setMinLongitude(Angle newValue)
    {
        this.model.setMinLongitude(newValue);
    }

    public Angle getMaxLongitude()
    {
        return this.model.getMaxLongitude();
    }

    public void setMaxLongitude(Angle newValue)
    {
        this.model.setMaxLongitude(newValue);
    }

    public boolean isWMSEnabled()
    {
        Boolean b = this.model.isWMSEnabled();
        return b != null && b;
    }

    public void setWMSEnabled(boolean newValue)
    {
        this.model.setWMSEnabled(newValue);
    }

    public boolean isWFSEnabled()
    {
        Boolean b = this.model.isWFSEnabled();
        return b != null && b;
    }

    public void setWFSEnabled(boolean newValue)
    {
        this.model.setWFSEnabled(newValue);
    }

    public boolean isWCSEnabled()
    {
        Boolean b = this.model.isWCSEnabled();
        return b != null && b;
    }

    public void setWCSEnabled(boolean newValue)
    {
        this.model.setWCSEnabled(newValue);
    }

    public boolean isSimpleQuery()
    {
        Boolean b = this.model.isSimpleQuery();
        return b != null && b;
    }

    public void setSimpleQuery(boolean newValue)
    {
        this.model.setSimpleQuery(newValue);
    }

    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        this.queryButton.setEnabled(enabled);
        this.keywordsBox.setEnabled(enabled);
        this.minDateCheckBox.setEnabled(enabled);
        this.maxDateCheckBox.setEnabled(enabled);
        this.boundsCheckBox.setEnabled(enabled);
        this.linkWithWWJViewCheckBox.setEnabled(enabled);
        this.wmsCheckBox.setEnabled(enabled);
        this.wfsCheckBox.setEnabled(enabled);
        this.wcsCheckBox.setEnabled(enabled);
        this.toggleButton.setEnabled(enabled);
        this.minLatitudeLabel.setEnabled(enabled);
        this.maxLatitudeLabel.setEnabled(enabled);
        this.minLongitudeLabel.setEnabled(enabled);
        this.maxLongitudeLabel.setEnabled(enabled);

        if (!enabled)
        {
            this.minDateSpinner.setEnabled(enabled);
            this.maxDateSpinner.setEnabled(enabled);
            this.minLatitudeSpinner.setEnabled(enabled);
            this.maxLatitudeSpinner.setEnabled(enabled);
            this.minLongitudeSpinner.setEnabled(enabled);
            this.maxLongitudeSpinner.setEnabled(enabled);
        }

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

    JCheckBox getMinDateCheckBox()
    {
        return this.minDateCheckBox;
    }

    JCheckBox getMaxDateCheckBox()
    {
        return this.maxDateCheckBox;
    }

    DateSpinner getMinDateSpinner()
    {
        return this.minDateSpinner;
    }

    DateSpinner getMaxDateSpinner()
    {
        return this.maxDateSpinner;
    }

    JCheckBox getBoundsCheckBox()
    {
        return this.boundsCheckBox;
    }

    JCheckBox getLinkWithWWJViewCheckBox()
    {
        return this.linkWithWWJViewCheckBox;
    }

    AngleSpinner getMinLatitudeSpinner()
    {
        return this.minLatitudeSpinner;
    }

    AngleSpinner getMaxLatitudeSpinner()
    {
        return this.maxLatitudeSpinner;
    }

    AngleSpinner getMinLongitudeSpinner()
    {
        return this.minLongitudeSpinner;
    }

    AngleSpinner getMaxLongitudeSpinner()
    {
        return this.maxLongitudeSpinner;
    }

    JCheckBox getWMSCheckBox()
    {
        return this.wmsCheckBox;
    }

    JCheckBox getWFSCheckBox()
    {
        return this.wfsCheckBox;
    }

    JCheckBox getWCSCheckBox()
    {
        return this.wcsCheckBox;
    }

    JButton getToggleButton()
    {
        return this.toggleButton;
    }

    JPanel getAdvancedPanel()
    {
        return this.advancedPanel;
    }

    JLabel getMinLatitudeLabel()
    {
        return this.minLatitudeLabel;
    }

    JLabel getMaxLatitudeLabel()
    {
        return this.maxLatitudeLabel;
    }

    JLabel getMinLongitudeLabel()
    {
        return this.minLongitudeLabel;
    }

    JLabel getMaxLongitudeLabel()
    {
        return this.maxLongitudeLabel;
    }

    private void makeComponents()
    {
        this.queryButton = new JButton("Search");
        this.keywordsBox = new JComboBox(new Object[] {""});
        this.keywordsBox.setEditable(true);
        this.minDateCheckBox = new JCheckBox("Occurring after");
        this.maxDateCheckBox = new JCheckBox("Occurring before");
        this.minDateSpinner = new DateSpinner();
        this.maxDateSpinner = new DateSpinner();
        this.boundsCheckBox = new JCheckBox("Within the region");
        this.linkWithWWJViewCheckBox = new JCheckBox("Link to World Wind view");
        this.minLatitudeSpinner = new AngleSpinner(AngleSpinner.LATITUDE);
        this.maxLatitudeSpinner = new AngleSpinner(AngleSpinner.LATITUDE);
        this.minLongitudeSpinner = new AngleSpinner(AngleSpinner.LONGITUDE);
        this.maxLongitudeSpinner = new AngleSpinner(AngleSpinner.LONGITUDE);
        this.wmsCheckBox = new JCheckBox("WMS");
        this.wfsCheckBox = new JCheckBox("WFS");
        this.wcsCheckBox = new JCheckBox("WCS");
        this.toggleButton = new JButton();
        this.advancedPanel = new JPanel();
        this.minLatitudeLabel = new JLabel("Min Lat");
        this.maxLatitudeLabel = new JLabel("Max Lat");
        this.minLongitudeLabel = new JLabel("Min Lon");
        this.maxLongitudeLabel = new JLabel("Max Lon");

        this.queryButton.setActionCommand(CatalogKey.ACTION_COMMAND_QUERY);
        this.keywordsBox.setActionCommand(CatalogKey.KEYWORD_TEXT);
        this.minDateCheckBox.setActionCommand(CatalogKey.MIN_DATE_ENABLED);
        this.maxDateCheckBox.setActionCommand(CatalogKey.MAX_DATE_ENABLED);
        this.minDateSpinner.setActionCommand(CatalogKey.MIN_DATE);
        this.maxDateSpinner.setActionCommand(CatalogKey.MAX_DATE);
        this.boundsCheckBox.setActionCommand(CatalogKey.BBOX_ENABLED);
        this.linkWithWWJViewCheckBox.setActionCommand(CatalogKey.LINK_WITH_WWJ_VIEW);
        this.minLatitudeSpinner.setActionCommand(CatalogKey.MIN_LATITUDE);
        this.maxLatitudeSpinner.setActionCommand(CatalogKey.MAX_LATITUDE);
        this.minLongitudeSpinner.setActionCommand(CatalogKey.MIN_LONGITUDE);
        this.maxLongitudeSpinner.setActionCommand(CatalogKey.MAX_LONGITUDE);
        this.wmsCheckBox.setActionCommand(CatalogKey.WMS_ENABLED);
        this.wfsCheckBox.setActionCommand(CatalogKey.WFS_ENABLED);
        this.wcsCheckBox.setActionCommand(CatalogKey.WCS_ENABLED);
        this.toggleButton.setActionCommand(CatalogKey.SIMPLE_QUERY);

        this.queryButton.addActionListener(this.controller);
        this.keywordsBox.addActionListener(this.controller);
        this.minDateCheckBox.addActionListener(this.controller);
        this.maxDateCheckBox.addActionListener(this.controller);
        this.minDateSpinner.addActionListener(this.controller);
        this.maxDateSpinner.addActionListener(this.controller);
        this.boundsCheckBox.addActionListener(this.controller);
        this.linkWithWWJViewCheckBox.addActionListener(this.controller);
        this.minLatitudeSpinner.addActionListener(this.controller);
        this.maxLatitudeSpinner.addActionListener(this.controller);
        this.minLongitudeSpinner.addActionListener(this.controller);
        this.maxLongitudeSpinner.addActionListener(this.controller);
        this.wmsCheckBox.addActionListener(this.controller);
        this.wfsCheckBox.addActionListener(this.controller);
        this.wcsCheckBox.addActionListener(this.controller);
        this.toggleButton.addActionListener(this.controller);
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
        add(Box.createVerticalStrut(10));

        panel = new JPanel();
        {
            panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
            this.wmsCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.wfsCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.wcsCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(this.wmsCheckBox);
            panel.add(Box.createHorizontalStrut(5));
            panel.add(this.wfsCheckBox);
            panel.add(Box.createHorizontalStrut(5));
            panel.add(this.wcsCheckBox);
        }
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(panel);
        add(Box.createVerticalStrut(10));        

        {
            this.advancedPanel.setLayout(new BoxLayout(this.advancedPanel, BoxLayout.PAGE_AXIS));
            this.advancedPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

            panel = new JPanel();
            {
                panel.setLayout(new GridBagLayout());

                Box box = Box.createHorizontalBox();
                {
                    this.boundsCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
                    this.linkWithWWJViewCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
                    box.add(this.boundsCheckBox);
                    box.add(Box.createHorizontalStrut(5));
                    box.add(this.linkWithWWJViewCheckBox);
                }
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.WEST;
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                panel.add(box, gbc);

                gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.WEST;
                panel.add(this.minLatitudeLabel, gbc);
                gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.WEST;
                gbc.insets = new Insets(0, 5, 5, 20);
                panel.add(this.minLatitudeSpinner, gbc);

                gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.WEST;
                panel.add(this.maxLatitudeLabel, gbc);
                gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.WEST;
                gbc.insets = new Insets(0, 5, 5, 0);
                panel.add(this.maxLatitudeSpinner, gbc);

                gbc = new GridBagConstraints();
                gbc.weightx = 1.0;
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                panel.add(new JPanel(), gbc);

                gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.WEST;
                panel.add(this.minLongitudeLabel, gbc);
                gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.WEST;
                gbc.insets = new Insets(0, 5, 0, 20);
                panel.add(this.minLongitudeSpinner, gbc);

                gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.WEST;
                panel.add(this.maxLongitudeLabel, gbc);
                gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.WEST;
                gbc.insets = new Insets(0, 5, 0, 0);
                panel.add(this.maxLongitudeSpinner, gbc);

                gbc = new GridBagConstraints();
                gbc.weightx = 1.0;
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                panel.add(new JPanel(), gbc);
            }
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.advancedPanel.add(panel);
            this.advancedPanel.add(Box.createVerticalStrut(10));

            panel = new JPanel();
            {
                panel.setLayout(new GridBagLayout());

                GridBagConstraints gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.WEST;
                panel.add(this.minDateCheckBox, gbc);
                gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.WEST;
                gbc.insets = new Insets(0, 5, 0, 20);
                panel.add(this.minDateSpinner, gbc);

                gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.WEST;
                panel.add(this.maxDateCheckBox, gbc);
                gbc = new GridBagConstraints();
                gbc.anchor = GridBagConstraints.WEST;
                gbc.insets = new Insets(0, 5, 0, 0);
                panel.add(this.maxDateSpinner, gbc);

                gbc = new GridBagConstraints();
                gbc.weightx = 1.0;
                gbc.gridwidth = GridBagConstraints.REMAINDER;
                panel.add(new JPanel(), gbc);
            }
            panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.advancedPanel.add(panel);
        }
        add(this.advancedPanel);

        this.toggleButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(this.toggleButton);
    }
}
