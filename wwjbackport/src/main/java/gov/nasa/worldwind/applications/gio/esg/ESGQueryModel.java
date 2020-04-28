/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.applications.gio.catalogui.CatalogKey;
import gov.nasa.worldwind.applications.gio.catalogui.DefaultQueryModel;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.util.Logging;

import java.text.DateFormat;
import java.util.Date;
import java.util.logging.Level;

/**
 * @author dcollins
 * @version $Id: ESGQueryModel.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class ESGQueryModel extends DefaultQueryModel
{
    public ESGQueryModel(AVList params)
    {
        if (params == null)
        {
            String message = Logging.getMessage("nullValue.AVListIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Set configuration-based default values.
        AVList defaultParams = new AVListImpl();
        makeDefaultParams(defaultParams);
        setValues(defaultParams);
        // User specified values will override default values.
        setValues(params);
    }
    
    public ESGQueryModel()
    {
        this(new AVListImpl());
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

        o = getDateValue(CatalogKey.MIN_DATE, new Date(0));
        if (o != null)
            params.setValue(CatalogKey.MIN_DATE, o);

        o = getDateValue(CatalogKey.MAX_DATE, new Date()); // Today
        if (o != null)
            params.setValue(CatalogKey.MAX_DATE, o);

        o = getBooleanValue(CatalogKey.MIN_DATE_ENABLED, Boolean.FALSE);
        if (o != null)
            params.setValue(CatalogKey.MIN_DATE_ENABLED, o);

        o = getBooleanValue(CatalogKey.MAX_DATE_ENABLED, Boolean.FALSE);
        if (o != null)
            params.setValue(CatalogKey.MAX_DATE_ENABLED, o);

        o = getAngleValue(CatalogKey.MIN_LATITUDE, Angle.NEG90);
        if (o != null)
            params.setValue(CatalogKey.MIN_LATITUDE, o);

        o = getAngleValue(CatalogKey.MAX_LATITUDE, Angle.POS90);
        if (o != null)
            params.setValue(CatalogKey.MAX_LATITUDE, o);

        o = getAngleValue(CatalogKey.MIN_LONGITUDE, Angle.NEG180);
        if (o != null)
            params.setValue(CatalogKey.MIN_LONGITUDE, o);

        o = getAngleValue(CatalogKey.MAX_LONGITUDE, Angle.POS180);
        if (o != null)
            params.setValue(CatalogKey.MAX_LONGITUDE, o);

        o = getBooleanValue(CatalogKey.BBOX_ENABLED, Boolean.FALSE);
        if (o != null)
            params.setValue(CatalogKey.BBOX_ENABLED, o);

        o = getBooleanValue(CatalogKey.SIMPLE_QUERY, Boolean.TRUE);
        if (o != null)
            params.setValue(CatalogKey.SIMPLE_QUERY, o);

        o = getBooleanValue(CatalogKey.WMS_ENABLED, Boolean.FALSE);
        if (o != null)
            params.setValue(CatalogKey.WMS_ENABLED, o);

        o = getBooleanValue(CatalogKey.WFS_ENABLED, Boolean.FALSE);
        if (o != null)
            params.setValue(CatalogKey.WFS_ENABLED, o);

        o = getBooleanValue(CatalogKey.WCS_ENABLED, Boolean.FALSE);
        if (o != null)
            params.setValue(CatalogKey.WCS_ENABLED, o);

        o  = getBooleanValue(CatalogKey.LINK_WITH_WWJ_VIEW, Boolean.FALSE);
        if (o != null)
            params.setValue(CatalogKey.LINK_WITH_WWJ_VIEW, o);

        return params;
    }

    public String getKeywordText()
    {
        return getStringValue(this, CatalogKey.KEYWORD_TEXT);
    }

    public void setKeywordText(String newValue)
    {
        setValueFireEvent(CatalogKey.KEYWORD_TEXT, newValue);
    }

    public Date getMinDate()
    {
        return getDateValue(this, CatalogKey.MIN_DATE);
    }

    public void setMinDate(Date newValue)
    {
        setValueFireEvent(CatalogKey.MIN_DATE, newValue);
    }

    public Date getMaxDate()
    {
        return getDateValue(this, CatalogKey.MAX_DATE);
    }

    public void setMaxDate(Date newValue)
    {
        setValueFireEvent(CatalogKey.MAX_DATE, newValue);
    }

    public Boolean isMinDateEnabled()
    {
        return getBooleanValue(this, CatalogKey.MIN_DATE_ENABLED);
    }

    public void setMinDateEnabled(Boolean newValue)
    {
        setValueFireEvent(CatalogKey.MIN_DATE_ENABLED, newValue);
    }

    public Boolean isMaxDateEnabled()
    {
        return getBooleanValue(this, CatalogKey.MAX_DATE_ENABLED);
    }

    public void setMaxDateEnabled(Boolean newValue)
    {
        setValueFireEvent(CatalogKey.MAX_DATE_ENABLED, newValue);
    }

    public Angle getMinLatitude()
    {
        return getAngleValue(this, CatalogKey.MIN_LATITUDE);
    }

    public void setMinLatitude(Angle newValue)
    {
        setValueFireEvent(CatalogKey.MIN_LATITUDE, newValue);
    }

    public Angle getMaxLatitude()
    {
        return getAngleValue(this, CatalogKey.MAX_LATITUDE);
    }

    public void setMaxLatitude(Angle newValue)
    {
        setValueFireEvent(CatalogKey.MAX_LATITUDE, newValue);
    }

    public Angle getMinLongitude()
    {
        return getAngleValue(this, CatalogKey.MIN_LONGITUDE);
    }

    public void setMinLongitude(Angle newValue)
    {
        setValueFireEvent(CatalogKey.MIN_LONGITUDE, newValue);
    }

    public Angle getMaxLongitude()
    {
        return getAngleValue(this, CatalogKey.MAX_LONGITUDE);
    }

    public void setMaxLongitude(Angle newValue)
    {
        setValueFireEvent(CatalogKey.MAX_LONGITUDE, newValue);
    }

    public Boolean isBboxEnabled()
    {
        return getBooleanValue(this, CatalogKey.BBOX_ENABLED);
    }

    public void setBboxEnabled(Boolean newValue)
    {
        setValueFireEvent(CatalogKey.BBOX_ENABLED, newValue);
    }

    public Boolean isWMSEnabled()
    {
        return getBooleanValue(this, CatalogKey.WMS_ENABLED);
    }

    public void setWMSEnabled(Boolean newValue)
    {
        setValueFireEvent(CatalogKey.WMS_ENABLED, newValue);
    }

    public Boolean isWFSEnabled()
    {
        return getBooleanValue(this, CatalogKey.WFS_ENABLED);
    }

    public void setWFSEnabled(Boolean newValue)
    {
        setValueFireEvent(CatalogKey.WFS_ENABLED, newValue);
    }

    public Boolean isWCSEnabled()
    {
        return getBooleanValue(this, CatalogKey.WCS_ENABLED);
    }

    public void setWCSEnabled(Boolean newValue)
    {
        setValueFireEvent(CatalogKey.WCS_ENABLED, newValue);
    }

    public Boolean isSimpleQuery()
    {
        return getBooleanValue(this, CatalogKey.SIMPLE_QUERY);
    }

    public void setSimpleQuery(Boolean newValue)
    {
        setValueFireEvent(CatalogKey.SIMPLE_QUERY, newValue);
    }

    public Boolean isLinkWithWWJView()
    {
        return getBooleanValue(this, CatalogKey.LINK_WITH_WWJ_VIEW);
    }

    public void setLinkWithWWJView(Boolean newValue)
    {
        setValueFireEvent(CatalogKey.LINK_WITH_WWJ_VIEW, newValue);
    }

    private void setValueFireEvent(String key, Object newValue)
    {
        setValue(key, newValue);
        firePropertyChange(key, null, newValue);
    }

    public static Boolean getBooleanValue(AVList avList, String key)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.AttributeKeyIsNull");
            Logging.logger().severe(msg);
            throw new IllegalStateException(msg);
        }
        
        Object o = avList.getValue(key);
        if (o == null)
            return null;

        if (o instanceof Boolean)
            return (Boolean) o;

        String v = getStringValue(avList, key);
        if (v == null)
            return null;

        return Boolean.parseBoolean(v);
    }

    public static Date getDateValue(AVList avList, String key)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.AttributeKeyIsNull");
            Logging.logger().severe(msg);
            throw new IllegalStateException(msg);
        }

        Object o = avList.getValue(key);
        if (o == null)
            return null;

        if (o instanceof Date)
            return (Date) o;

        Long l = getLongValue(avList, key);
        if (l != null)
            return new Date(l);

        String v = getStringValue(avList, key);
        if (v == null)
            return null;

        try
        {
            return DateFormat.getDateInstance().parse(v);
        }
        catch (java.text.ParseException e)
        {
            Logging.logger().log(Level.SEVERE, "Configuration.ConversionError", v);
            return null;
        }
    }

    public static Angle getAngleValue(AVList avList, String key)
    {
        if (key == null)
        {
            String msg = Logging.getMessage("nullValue.AttributeKeyIsNull");
            Logging.logger().severe(msg);
            throw new IllegalStateException(msg);
        }

        Object o = avList.getValue(key);
        if (o == null)
            return null;

        if (o instanceof Angle)
            return (Angle) o;

        Double d = getDoubleValue(avList, key);
        if (d != null)
            return Angle.fromDegrees(d);

        return null;
    }

    private static Boolean getBooleanValue(String configurationKey, Boolean defaultValue)
    {
        if (configurationKey == null)
        {
            String msg = Logging.getMessage("nullValue.AttributeKeyIsNull");
            Logging.logger().severe(msg);
            throw new IllegalStateException(msg);
        }

        String v = Configuration.getStringValue(configurationKey);
        if (v == null)
            return defaultValue;

        return Boolean.valueOf(v);
    }

    private static Date getDateValue(String configurationKey, Date defaultValue)
    {
        if (configurationKey == null)
        {
            String msg = Logging.getMessage("nullValue.AttributeKeyIsNull");
            Logging.logger().severe(msg);
            throw new IllegalStateException(msg);
        }

        Long l = Configuration.getLongValue(configurationKey);
        if (l != null)
            return new Date(l);

        String v = Configuration.getStringValue(configurationKey);
        if (v == null)
            return defaultValue;

        try
        {
            return DateFormat.getDateInstance().parse(v);
        }
        catch (java.text.ParseException e)
        {
            Logging.logger().log(Level.SEVERE, "Configuration.ConversionError", v);
            return defaultValue;
        }
    }

    private static Angle getAngleValue(String configurationKey, Angle defaultValue)
    {
        if (configurationKey == null)
        {
            String msg = Logging.getMessage("nullValue.AttributeKeyIsNull");
            Logging.logger().severe(msg);
            throw new IllegalStateException(msg);
        }

        Double d = Configuration.getDoubleValue(configurationKey);
        if (d != null)
            return Angle.fromDegrees(d);

        return defaultValue;
    }
}
