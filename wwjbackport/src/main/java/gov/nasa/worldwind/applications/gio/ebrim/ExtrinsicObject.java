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
public interface ExtrinsicObject extends RegistryObject
{
    ContentVersionInfo getContentVersionInfo();

    void setContentVersionInfo(ContentVersionInfo contentVersionInfo);

    String getMimeType();

    void setMimeType(String mimeType);

    boolean isOpaque();

    void setOpaque(boolean opaque);
}
