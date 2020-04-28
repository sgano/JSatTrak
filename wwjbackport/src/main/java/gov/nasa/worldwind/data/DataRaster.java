/* Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;

/**
 * @author dcollins
 * @version $Id: DataRaster.java 14056 2010-10-27 09:11:24Z garakl $
 */
public interface DataRaster extends AVList
{
    int getWidth();

    int getHeight();

    Sector getSector();

    void drawOnCanvas(DataRaster canvas, Sector clipSector);

    void drawOnCanvas(DataRaster canvas);

    DataRaster getSubRaster(AVList params);

    DataRaster getSubRaster(int width, int height, Sector sector, AVList params);
}
