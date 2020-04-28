/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.*;

/**
 * @author dcollins
 * @version $Id: MipMappedBufferedImageRaster.java 11704 2009-06-17 20:28:39Z dcollins $
 */
public class MipMappedBufferedImageRaster extends BufferedImageRaster
{
    protected BufferedImageRaster[] levelRasters;

    protected MipMappedBufferedImageRaster(Sector sector, java.awt.image.BufferedImage image)
    {
        super(sector, image);

        int maxLevel = ImageUtil.getMaxMipmapLevel(image.getWidth(), image.getHeight());
        java.awt.image.BufferedImage[] levelImages = ImageUtil.buildMipmaps(image,
            java.awt.image.BufferedImage.TYPE_INT_ARGB_PRE, maxLevel);

        this.levelRasters = new BufferedImageRaster[1 + maxLevel];
        for (int i = 0; i <= maxLevel; i++)
        {
            this.levelRasters[i] = new BufferedImageRaster(sector, levelImages[i]);
        }
    }

    public long getSizeInBytes()
    {
        long sizeInBytes = 0L;
        for (BufferedImageRaster raster : this.levelRasters)
        {
            sizeInBytes += raster.getSizeInBytes();
        }

        return sizeInBytes;
    }

    public void dispose()
    {
        for (BufferedImageRaster raster : this.levelRasters)
        {
            raster.dispose();
        }
    }

    protected void doDrawOnCanvas(BufferedImageRaster canvas, Sector clipSector)
    {
        if (!this.getSector().intersects(canvas.getSector()))
            return;

        BufferedImageRaster raster = this.chooseRasterForCanvas(canvas);
        raster.doDrawOnCanvas(canvas, clipSector);
    }

    protected BufferedImageRaster chooseRasterForCanvas(BufferedImageRaster canvas)
    {
        int level = this.computeMipmapLevel(
            this.getWidth(), this.getHeight(), this.getSector(),
            canvas.getWidth(), canvas.getHeight(), canvas.getSector());

        int maxLevel = this.levelRasters.length - 1;
        level = (int) WWMath.clamp(level, 0, maxLevel);

        return this.levelRasters[level];
    }

    protected int computeMipmapLevel(int sourceWidth, int sourceHeight, Sector sourceSector,
        int destWidth, int destHeight, Sector destSector)
    {
        double sy = ((double) sourceHeight / (double) destHeight)
            * (destSector.getDeltaLatDegrees() / sourceSector.getDeltaLatDegrees());
        double sx = ((double) sourceWidth / (double) destWidth)
            * (destSector.getDeltaLonDegrees() / sourceSector.getDeltaLonDegrees());
        double scale = Math.max(sx, sy);

        if (scale < 1)
            return 0;

        return (int) WWMath.logBase2(scale);
    }
}
