/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

/**
 * Represents the KML <i>BalloonStyle</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLBalloonStyle.java 14010 2010-10-22 19:32:12Z pabercrombie $
 */
public class KMLBalloonStyle extends KMLAbstractSubStyle
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLBalloonStyle(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getcolor()
    {
        return (String) this.getField("color");
    }

    public String getBgColor()
    {
        return (String) this.getField("bgColor");
    }

    public String getTextColor()
    {
        return (String) this.getField("textColor");
    }

    /**
     * Get the <i>text</i> field.
     *
     * @return Balloon text field.
     */
    public String getText()
    {
        return (String) this.getField("text");
    }

    public String getDisplayMode()
    {
        return (String) this.getField("displayMode");
    }

    /**
     * Does the style have at least one BalloonStyle field set? This method tests for the existence of the BalloonStyle
     * content fields (text, displayMode, bgColor, etc).
     *
     * @return True if at least one of the BalloonStyle fields is set (text, displayMode, bgColor, etc).
     */
    public boolean hasStyleFields()
    {
        return this.hasField("text")
            || this.hasField("bgColor")
            || this.hasField("textColor")
            || this.hasField("color")
            || this.hasField("displayMode");
    }
}
