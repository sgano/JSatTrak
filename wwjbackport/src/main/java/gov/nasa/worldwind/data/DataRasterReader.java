/* Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.AVList;

/**
 * @author dcollins
 * @version $Id: DataRasterReader.java 14017 2010-10-22 21:54:25Z garakl $
 */
public interface DataRasterReader extends AVList
{
    String getDescription();

    String[] getMimeTypes();

    String[] getSuffixes();

    boolean canRead(Object source, AVList params);

    DataRaster[] read(Object source, AVList params) throws java.io.IOException;

    void readMetadata(Object source, AVList params) throws java.io.IOException;
}