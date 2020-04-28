/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.applications.gio.catalogui.AngleSpinner;
import gov.nasa.worldwind.applications.gio.catalogui.CatalogKey;
import gov.nasa.worldwind.applications.gio.catalogui.DateSpinner;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

/**
 * @author dcollins
 * @version $Id: ESGQueryController.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class ESGQueryController implements PropertyChangeListener, ActionListener
{
    private ESGQueryPanel queryPanel;
    private boolean ignoreActionEvents = false;
    private static final String ADVANCED_TO_SIMPLE_TEXT = "<< Simple";
    private static final String SIMPLE_TO_ADVANCED_TEXT = "Advanced >>";

    public ESGQueryController(ESGQueryPanel queryPanel)
    {
        if (queryPanel == null)
        {
            String message = "catalog.QueryPanelIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.queryPanel = queryPanel;
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        this.ignoreActionEvents = true;
        try
        {
            if (evt != null && evt.getPropertyName() != null)
            {
                String propertyName = evt.getPropertyName();
                Object newValue = evt.getNewValue();

                if (propertyName.equals(CatalogKey.KEYWORD_TEXT))
                {
                    setValue(this.queryPanel.getKeywordsBox(), newValue);
                }
                else if (propertyName.equals(CatalogKey.MIN_DATE))
                {
                    setValue(this.queryPanel.getMinDateSpinner(), newValue);
                }
                else if (propertyName.equals(CatalogKey.MAX_DATE))
                {
                    setValue(this.queryPanel.getMaxDateSpinner(), newValue);
                }
                else if (propertyName.equals(CatalogKey.MIN_DATE_ENABLED))
                {
                    setSelected(this.queryPanel.getMinDateCheckBox(), newValue);
                    setEnabled(this.queryPanel.getMinDateSpinner(), newValue);
                }
                else if (propertyName.equals(CatalogKey.MAX_DATE_ENABLED))
                {
                    setSelected(this.queryPanel.getMaxDateCheckBox(), newValue);
                    setEnabled(this.queryPanel.getMaxDateSpinner(), newValue);
                }
                else if (propertyName.equals(CatalogKey.MIN_LATITUDE))
                {
                    setValue(this.queryPanel.getMinLatitudeSpinner(), newValue);
                }
                else if (propertyName.equals(CatalogKey.MAX_LATITUDE))
                {
                    setValue(this.queryPanel.getMaxLatitudeSpinner(), newValue);
                }
                else if (propertyName.equals(CatalogKey.MIN_LONGITUDE))
                {
                    setValue(this.queryPanel.getMinLongitudeSpinner(), newValue);
                }
                else if (propertyName.equals(CatalogKey.MAX_LONGITUDE))
                {
                    setValue(this.queryPanel.getMaxLongitudeSpinner(), newValue);
                }
                else if (propertyName.equals(CatalogKey.BBOX_ENABLED))
                {
                    setSelected(this.queryPanel.getBoundsCheckBox(), newValue);
                    setEnabled(this.queryPanel.getMinLatitudeSpinner(), newValue);
                    setEnabled(this.queryPanel.getMaxLatitudeSpinner(), newValue);
                    setEnabled(this.queryPanel.getMinLongitudeSpinner(), newValue);
                    setEnabled(this.queryPanel.getMaxLongitudeSpinner(), newValue);
                }
                else if (propertyName.equals(CatalogKey.LINK_WITH_WWJ_VIEW))
                {
                    setSelected(this.queryPanel.getLinkWithWWJViewCheckBox(), newValue);
                }
                else if (propertyName.equals(CatalogKey.WMS_ENABLED))
                {
                    setSelected(this.queryPanel.getWMSCheckBox(), newValue);
                }
                else if (propertyName.equals(CatalogKey.WFS_ENABLED))
                {
                    setSelected(this.queryPanel.getWFSCheckBox(), newValue);
                }
                else if (propertyName.equals(CatalogKey.WCS_ENABLED))
                {
                    setSelected(this.queryPanel.getWCSCheckBox(), newValue);
                }
                else if (propertyName.equals(CatalogKey.SIMPLE_QUERY))
                {
                    setIsSimple(newValue);
                }
            }
        }
        finally
        {
            this.ignoreActionEvents = false;
        }
    }

    public void actionPerformed(ActionEvent event)
    {
        if (!this.ignoreActionEvents)
        {
            ESGQueryModel model = (ESGQueryModel) this.queryPanel.getModel();
            if (event != null && event.getActionCommand() != null && model != null)
            {
                String actionCommand = event.getActionCommand();
                if (actionCommand.equals(CatalogKey.ACTION_COMMAND_QUERY))
                {
                    model.firePropertyChange(CatalogKey.ACTION_COMMAND_QUERY, null, model);
                }
                else if (actionCommand.equals(CatalogKey.KEYWORD_TEXT))
                {
                    Object obj = this.queryPanel.getKeywordsBox().getSelectedItem();
                    model.setKeywordText(obj != null ? obj.toString() : null);
                }
                else if (actionCommand.equals(CatalogKey.MIN_DATE))
                {
                    model.setMinDate(this.queryPanel.getMinDateSpinner().getValue());
                }
                else if (actionCommand.equals(CatalogKey.MAX_DATE))
                {
                    model.setMaxDate(this.queryPanel.getMaxDateSpinner().getValue());
                }
                else if (actionCommand.equals(CatalogKey.MIN_DATE_ENABLED))
                {
                    model.setMinDateEnabled(this.queryPanel.getMinDateCheckBox().isSelected());
                    // When min-date-enabled changes, also update the min date value.
                    // This handles the case when the checkbox is clicked, but the date spinner
                    // hasn't been modified.
                    model.setMinDate(this.queryPanel.getMinDateSpinner().getValue());
                }
                else if (actionCommand.equals(CatalogKey.MAX_DATE_ENABLED))
                {
                    model.setMaxDateEnabled(this.queryPanel.getMaxDateCheckBox().isSelected());
                    // When max-date-enabled changes, also update the max date value.
                    // This handles the case when the checkbox is clicked, but the date spinner
                    // hasn't been modified.
                    model.setMaxDate(this.queryPanel.getMaxDateSpinner().getValue());
                }
                else if (actionCommand.equals(CatalogKey.MIN_LATITUDE))
                {
                    model.setMinLatitude(this.queryPanel.getMinLatitudeSpinner().getValue());
                }
                else if (actionCommand.equals(CatalogKey.MAX_LATITUDE))
                {
                    model.setMaxLatitude(this.queryPanel.getMaxLatitudeSpinner().getValue());
                }
                else if (actionCommand.equals(CatalogKey.MIN_LONGITUDE))
                {
                    model.setMinLongitude(this.queryPanel.getMinLongitudeSpinner().getValue());
                }
                else if (actionCommand.equals(CatalogKey.MAX_LONGITUDE))
                {
                    model.setMaxLongitude(this.queryPanel.getMaxLongitudeSpinner().getValue());
                }
                else if (actionCommand.equals(CatalogKey.BBOX_ENABLED))
                {
                    model.setBboxEnabled(this.queryPanel.getBoundsCheckBox().isSelected());
                    // When bbox-enabled changes, also update the bounding values.
                    // This handles the case when the checkbox is clicked, but an
                    // angle spinner hasn't been modified.
                    model.setMinLatitude(this.queryPanel.getMinLatitudeSpinner().getValue());
                    model.setMaxLatitude(this.queryPanel.getMaxLatitudeSpinner().getValue());
                    model.setMinLongitude(this.queryPanel.getMinLongitudeSpinner().getValue());
                    model.setMaxLongitude(this.queryPanel.getMaxLongitudeSpinner().getValue());
                }
                else if (actionCommand.equals(CatalogKey.LINK_WITH_WWJ_VIEW))
                {
                    model.setLinkWithWWJView(this.queryPanel.getLinkWithWWJViewCheckBox().isSelected());
                }
                else if (actionCommand.equals(CatalogKey.WMS_ENABLED))
                {
                    model.setWMSEnabled(this.queryPanel.getWMSCheckBox().isSelected());
                }
                else if (actionCommand.equals(CatalogKey.WFS_ENABLED))
                {
                    model.setWFSEnabled(this.queryPanel.getWFSCheckBox().isSelected());
                }
                else if (actionCommand.equals(CatalogKey.WCS_ENABLED))
                {
                    model.setWCSEnabled(this.queryPanel.getWCSCheckBox().isSelected());
                }
                else if (actionCommand.equals(CatalogKey.SIMPLE_QUERY))
                {
                    boolean isSimpleQuery = !this.queryPanel.getAdvancedPanel().isVisible();
                    // Toggle the model value SIMPLE_QUERY.
                    model.setSimpleQuery(!isSimpleQuery);
                }
            }
        }
    }

    public void synchronizeView()
    {
        this.ignoreActionEvents = true;
        try
        {
            ESGQueryModel model = (ESGQueryModel) this.queryPanel.getModel();
            if (model != null)
            {
                // Assign view components values from model.
                String s = model.getKeywordText();
                setValue(this.queryPanel.getKeywordsBox(), s);

                Date d = model.getMinDate();
                setValue(this.queryPanel.getMinDateSpinner(), d);

                d = model.getMaxDate();
                setValue(this.queryPanel.getMaxDateSpinner(), d);

                Angle a = model.getMinLatitude();
                setValue(this.queryPanel.getMinLatitudeSpinner(), a);

                a = model.getMaxLatitude();
                setValue(this.queryPanel.getMaxLatitudeSpinner(), a);

                a = model.getMinLongitude();
                setValue(this.queryPanel.getMinLongitudeSpinner(), a);

                a = model.getMaxLongitude();
                setValue(this.queryPanel.getMaxLongitudeSpinner(), a);

                // Selectively enable view components based on model state.
                Boolean b = model.isMinDateEnabled();
                setSelected(this.queryPanel.getMinDateCheckBox(), b);
                setEnabled(this.queryPanel.getMinDateSpinner(), b);

                b = model.isMaxDateEnabled();
                setSelected(this.queryPanel.getMaxDateCheckBox(), b);
                setEnabled(this.queryPanel.getMaxDateSpinner(), b);

                b = model.isBboxEnabled();
                setSelected(this.queryPanel.getBoundsCheckBox(), b);
                setEnabled(this.queryPanel.getMinLatitudeSpinner(), b);
                setEnabled(this.queryPanel.getMaxLatitudeSpinner(), b);
                setEnabled(this.queryPanel.getMinLongitudeSpinner(), b);
                setEnabled(this.queryPanel.getMaxLongitudeSpinner(), b);

                b = model.isLinkWithWWJView();
                setSelected(this.queryPanel.getLinkWithWWJViewCheckBox(), b);

                b = model.isWMSEnabled();
                setSelected(this.queryPanel.getWMSCheckBox(), b);

                b = model.isWFSEnabled();
                setSelected(this.queryPanel.getWFSCheckBox(), b);

                b = model.isWCSEnabled();
                setSelected(this.queryPanel.getWCSCheckBox(), b);

                b = model.isSimpleQuery();
                setIsSimple(b);
            }
        }
        finally
        {
            this.ignoreActionEvents = false;
        }
    }

    private void setEnabled(Component c, Object value)
    {
        if (c != null)
        {
            c.setEnabled(value != null && Boolean.parseBoolean(value.toString()));
        }
    }

    private void setVisible(Component c, Object value)
    {
        if (c != null)
        {
            c.setVisible(value != null && Boolean.parseBoolean(value.toString()));
        }
    }

    private void setSelected(JCheckBox cb, Object value)
    {
        if (cb != null)
        {
            cb.setSelected(value != null && Boolean.parseBoolean(value.toString()));
        }
    }

    private void setText(AbstractButton b, String text)
    {
        if (b != null)
        {
            b.setText(text);
        }
    }

    private void setValue(JComboBox b, Object value)
    {
        if (b != null)
        {
            b.setSelectedItem(value);   
        }
    }

    private void setValue(DateSpinner s, Object value)
    {
        if (s != null)
        {
            if (value != null && value instanceof Date)
                s.setValue((Date) value);
        }
    }

    private void setValue(AngleSpinner s, Object value)
    {
        if (s != null)
        {
            if (value != null && value instanceof Angle)
                s.setValue((Angle) value);
        }
    }

    private void setIsSimple(Object value)
    {
        boolean isSimple = value != null && Boolean.parseBoolean(value.toString());
        setVisible(this.queryPanel.getAdvancedPanel(), !isSimple);
        setText(this.queryPanel.getToggleButton(), isSimple ? SIMPLE_TO_ADVANCED_TEXT : ADVANCED_TO_SIMPLE_TEXT);
    }
}
