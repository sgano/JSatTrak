/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.ebrim;

/**
 * @author dcollins
 * @version $Id$
 */
public interface LocalizedString
{
    String getLang();

    void setLang(String lang);

    String getCharset();

    void setCharset(String charset);

    String getValue();

    void setValue(String value);
}
