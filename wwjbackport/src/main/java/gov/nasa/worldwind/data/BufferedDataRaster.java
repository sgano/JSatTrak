/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.*;

/**
 * @author dcollins
 * @version $Id: BufferedDataRaster.java 14056 2010-10-27 09:11:24Z garakl $
 */
public abstract class BufferedDataRaster extends AbstractDataRaster
{
    public BufferedDataRaster(int width, int height, Sector sector, AVList list)
    {
        super(width, height, sector, list);
    }

    public BufferedDataRaster(int width, int height, Sector sector)
    {
        this(width, height, sector, null);
    }

    public double getTransparentValue()
    {
        if (this.hasKey(AVKey.MISSING_DATA_SIGNAL))
        {
            Object o = this.getValue(AVKey.MISSING_DATA_SIGNAL);
            if (null != o && o instanceof Double)
                return (Double) o;
        }
        return Double.MAX_VALUE;
    }

    public void setTransparentValue(double transparentValue)
    {
        this.setValue(AVKey.MISSING_DATA_SIGNAL, transparentValue);
    }

    public void drawOnCanvas(DataRaster canvas, Sector clipSector)
    {
        if (canvas == null)
        {
            String message = Logging.getMessage("nullValue.DestinationIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!(canvas instanceof BufferedDataRaster))
        {
            String message = Logging.getMessage("DataRaster.IncompatibleRaster", canvas);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.doDrawOnCanvas((BufferedDataRaster) canvas, clipSector);
    }

    public void drawOnCanvas(DataRaster canvas)
    {
        if (canvas == null)
        {
            String message = Logging.getMessage("nullValue.DestinationIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.drawOnCanvas(canvas, null);
    }

    public void fill(double value)
    {
        int width = this.getWidth();
        int height = this.getHeight();
        double[] samples = new double[width];
        java.util.Arrays.fill(samples, value);

        // Fill each row of this raster with the clear color.
        for (int j = 0; j < height; j++)
        {
            this.put(0, j, samples, 0, width);
        }
    }

    /**
     * Returns a two-element array containing this BufferedDataRaster's extreme scalar values, ignoring any values
     * marked as missing-data. This returns null if this BufferedDataRaster contains no values, or if it contains only
     * values marked as missing-data.
     *
     * @return a two-element array containing this BufferedDataRaster's extreme values, or null if none exist. Entry 0
     *         contains the minimum value; entry 1 contains the maximum value.
     */
    public double[] getExtremes()
    {
        // Create local variables to store the raster's dimensions and missing data signal to eliminate any overhead in
        // the loops below.
        int width = this.getWidth();
        int height = this.getHeight();
        double missingDataSignal = this.getTransparentValue();

        // Allocate a buffer to hold one row of scalar values.
        double[] buffer = new double[width];

        // Allocate a buffer to hold the extreme values.
        double[] extremes = null;

        for (int j = 0; j < height; j++)
        {
            this.get(0, j, width, buffer, 0); // Get the row starting at (0, j).

            for (int i = 0; i < width; i++)
            {
                if (buffer[i] == missingDataSignal) // Ignore values marked as missing-data.
                    continue;

                if (extremes == null)
                    extremes = WWUtil.defaultMinMix();

                if (extremes[0] > buffer[i])
                    extremes[0] = buffer[i];
                if (extremes[1] < buffer[i])
                    extremes[1] = buffer[i];
            }
        }

        // Extremes is null if this raster is empty, or contains only values marked as missing-data.
        return extremes;
    }

    protected abstract void get(int x, int y, int length, double[] buffer, int pos);

    protected abstract void put(int x, int y, double[] buffer, int pos, int length);

    protected void doDrawOnCanvas(BufferedDataRaster canvas, Sector clipSector)
    {
        if (!this.getSector().intersects(canvas.getSector()))
            return;

        int thisWidth = this.getWidth();
        int thisHeight = this.getHeight();
        int canvasWidth = canvas.getWidth();
        int canvasHeight = canvas.getHeight();
        double thisTransparentValue = this.getTransparentValue();

        // Compute the transform from the canvas' coordinate system to this raster's coordinate system.
        java.awt.geom.AffineTransform canvasToThis = this.computeSourceToDestTransform(
            canvasWidth, canvasHeight, canvas.getSector(),
            thisWidth, thisHeight, this.getSector());

        /// Compute the region of the destination raster to be be clipped by the specified clipping sector. If no
        // clipping sector is specified, then perform no clipping. We compute the clip region for the destination
        // raster because this region is used to limit which pixels are rasterized to the destination.
        java.awt.Rectangle clipRect = new java.awt.Rectangle(0, 0, canvasWidth - 1, canvasHeight - 1);
        if (clipSector != null)
        {
            java.awt.Rectangle rect = this.computeClipRect(clipSector, canvas);
            clipRect = clipRect.intersection(rect);
        }

        // Precompute the interpolation values for each transformed x- and y-coordinate.
        InterpolantLookupTable lut = this.createLookupTable(
            canvasWidth, canvasHeight,           // lookup table dimensions
            0, thisWidth - 1, 0, thisHeight - 1, // lookup table xMin, xMax, yMin, yMax
            canvasToThis);                       // lookup transform
        // If the lookup table is null, then no values in the canvas fall within this raster's bounds. This means
        // either the two rasters do not intersect or that this raster fits entirely between two x-coordinates or two
        // y-coordinates (or both) in the canvas. In either case, we do not rasterize any contribution from this raster
        // into the canvas, and simply exit.
        if (lut == null)
            return;

        // Allocate space to hold the lookup table parameters.
        double[] xParams = new double[3];
        double[] yParams = new double[3];

        // Compute the range of x-values in this raster that are needed during rendering.
        lut.computeRangeX(xParams);
        int xParamMin = (int) Math.floor(xParams[0]);
        int xParamMax = (int) Math.ceil(xParams[1]);
        int xParamWidth = xParamMax - xParamMin + 1;

        // Allocate a buffer for two rows of samples from this raster, and allocate a buffer for one row of samples
        // from the canvas.
        double[] thisSamples = new double[2 * xParamWidth];
        double[] canvasSamples = new double[canvasWidth];
        int x1, x2, y1, y2;
        double xf, yf;

        // Iterate over each canvas row, filling canvas pixels with samples from this raster.
        for (int j = clipRect.y; j <= (clipRect.y + clipRect.height); j++)
        {
            // If the interpolant lookup table has an entry for "j", then process this row.
            if (lut.getInterpolantY(j, yParams))
            {
                y1 = (int) yParams[0];
                y2 = (int) yParams[1];
                yf = yParams[2];
                // Read the two rows of image samples that straddle yf.
                this.get(xParamMin, y1, xParamWidth, thisSamples, 0);
                this.get(xParamMin, y2, xParamWidth, thisSamples, xParamWidth);
                // Read the canvas row samples.
                canvas.get(0, j, canvasWidth, canvasSamples, 0);

                // Iterate over each canvas column, sampling canvas pixels.
                for (int i = clipRect.x; i <= (clipRect.x + clipRect.width); i++)
                {
                    // If the interpolant lookup table has an entry for "i", then process this column.
                    if (lut.getInterpolantX(i, xParams))
                    {
                        x1 = (int) xParams[0] - xParamMin;
                        x2 = (int) xParams[1] - xParamMin;
                        xf = xParams[2];
                        // Sample this raster with the interpolated coordinates. This produces a bi-linear mix
                        // of the four values surrounding the canvas pixel. Place the output in the canvas sample array.
                        sample(thisSamples, x1, x2, xf, 0, 1, yf, xParamWidth, thisTransparentValue, canvasSamples, i);
                    }
                }

                // Write the canvas row samples.
                canvas.put(0, j, canvasSamples, 0, canvasWidth);
            }
        }
    }

    @Override
    protected java.awt.geom.AffineTransform computeSourceToDestTransform(
        int sourceWidth, int sourceHeight, Sector sourceSector,
        int destWidth, int destHeight, Sector destSector)
    {
        // Compute the the transform from source to destination coordinates. In this computation a pixel is assumed
        // to have no dimension. We measure the distance between pixels rather than some pixel dimension.

        double ty = (destHeight - 1) * -(sourceSector.getMaxLatitude().degrees - destSector.getMaxLatitude().degrees)
            / destSector.getDeltaLatDegrees();
        double tx = (destWidth - 1) * (sourceSector.getMinLongitude().degrees - destSector.getMinLongitude().degrees)
            / destSector.getDeltaLonDegrees();

        double sy = ((double) (destHeight - 1) / (double) (sourceHeight - 1))
            * (sourceSector.getDeltaLatDegrees() / destSector.getDeltaLatDegrees());
        double sx = ((double) (destWidth - 1) / (double) (sourceWidth - 1))
            * (sourceSector.getDeltaLonDegrees() / destSector.getDeltaLonDegrees());

        java.awt.geom.AffineTransform transform = new java.awt.geom.AffineTransform();
        transform.translate(tx, ty);
        transform.scale(sx, sy);
        return transform;
    }

    @Override
    protected java.awt.geom.AffineTransform computeGeographicToRasterTransform(int width, int height, Sector sector)
    {
        // Compute the the transform from geographic to raster coordinates. In this computation a pixel is assumed
        // to have no dimension. We measure the distance between pixels rather than some pixel dimension.

        double ty = -sector.getMaxLatitude().degrees;
        double tx = -sector.getMinLongitude().degrees;

        double sy = -((height - 1) / sector.getDeltaLatDegrees());
        double sx = ((width - 1) / sector.getDeltaLonDegrees());

        java.awt.geom.AffineTransform transform = new java.awt.geom.AffineTransform();
        transform.scale(sx, sy);
        transform.translate(tx, ty);
        return transform;
    }

    private static void sample(double[] source, int x1, int x2, double xf, int y1, int y2, double yf, int width,
        double transparent, double[] dest, int destPos)
    {
        double ul = source[x1 + y1 * width];
        double ll = source[x1 + y2 * width];
        double lr = source[x2 + y2 * width];
        double ur = source[x2 + y1 * width];

        // If all four sample values are not transparent (or missing), then write the interpolated value to the
        // destination buffer.
        if ((ul != transparent) && (ur != transparent) && (lr != transparent) && (ll != transparent))
        {
            dest[destPos] =
                ((1.0 - xf) * (1.0 - yf) * ul)
                    + ((1.0 - xf) * (yf) * ll)
                    + ((xf) * (yf) * lr)
                    + ((xf) * (1.0 - yf) * ur);
        }
    }

    private static class InterpolantLookupTable
    {
        private int width;
        private int height;
        private double[] xParams;
        private double[] yParams;

        public InterpolantLookupTable(int width, int height)
        {
            this.width = width;
            this.height = height;
            this.xParams = new double[3 * width];
            this.yParams = new double[3 * height];
            java.util.Arrays.fill(this.xParams, -1d);
            java.util.Arrays.fill(this.yParams, -1d);
        }

        public final boolean getInterpolantX(int x, double[] params)
        {
            params[0] = this.xParams[3 * x];
            params[1] = this.xParams[3 * x + 1];
            params[2] = this.xParams[3 * x + 2];
            return params[0] != -1d;
        }

        public final boolean getInterpolantY(int y, double[] params)
        {
            params[0] = this.yParams[3 * y];
            params[1] = this.yParams[3 * y + 1];
            params[2] = this.yParams[3 * y + 2];
            return params[0] != -1d;
        }

        public final void computeRangeX(double[] params)
        {
            computeInterpolantRange(this.xParams, this.width, params);
        }

        public final void computeRangeY(double[] params)
        {
            computeInterpolantRange(this.yParams, this.height, params);
        }

        private static void computeInterpolantRange(double[] params, int size, double[] result)
        {
            double min = Double.MAX_VALUE;
            double max = -Double.MIN_VALUE;
            int index;
            for (int i = 0; i < size; i++)
            {
                index = 3 * i;
                if (params[index] != -1d)
                {
                    // Compute the minimum first parameter (x1 or y1).
                    if (params[index] < min)
                        min = params[index];
                    // Compute the maximum second parameters (x2 or y2).
                    if (params[index + 1] > max)
                        max = params[index + 1];
                }
            }
            result[0] = min;
            result[1] = max;
        }
    }

    private InterpolantLookupTable createLookupTable(int width, int height,
        double xMin, double xMax, double yMin, double yMax, java.awt.geom.AffineTransform lookupTransform)
    {
        // Compute the interpolation values for each transformed x- and y-coordinate. This assumes that the transform
        // is composed of translations and scales (no rotations or shears). Therefore the transformed coordinates of
        // each row or column would be identical.

        InterpolantLookupTable lut = new InterpolantLookupTable(width, height);

        double threshold = -1e-6; // Numerical roundoff error threshold.
        boolean haveXParam = false;
        boolean haveYParam = false;

        java.awt.geom.Point2D thisPoint = new java.awt.geom.Point2D.Double();
        java.awt.geom.Point2D canvasPoint = new java.awt.geom.Point2D.Double();
        double x, y;
        int index;

        for (int i = 0; i < width; i++)
        {
            canvasPoint.setLocation(i, 0);
            lookupTransform.transform(canvasPoint, thisPoint);
            x = thisPoint.getX();
            if (((x - xMin) > threshold) && ((xMax - x) > threshold))
            {
                x = (x < xMin) ? xMin : ((x > xMax) ? xMax : x);
                index = 3 * i;
                lut.xParams[index] = Math.floor(x);
                lut.xParams[index + 1] = Math.ceil(x);
                lut.xParams[index + 2] = x - lut.xParams[index];

                haveXParam = true;
            }
        }

        for (int j = 0; j < height; j++)
        {
            canvasPoint.setLocation(0, j);
            lookupTransform.transform(canvasPoint, thisPoint);
            y = thisPoint.getY();
            if (((y - yMin) > threshold) && ((yMax - y) > threshold))
            {
                y = (y < yMin) ? yMin : ((y > yMax) ? yMax : y);
                index = 3 * j;
                lut.yParams[index] = Math.floor(y);
                lut.yParams[index + 1] = Math.ceil(y);
                lut.yParams[index + 2] = y - lut.yParams[index];

                haveYParam = true;
            }
        }

        //
        if (haveXParam && haveYParam)
        {
            return lut;
        }

        return null;
    }

    @Override
    DataRaster doGetSubRaster(int roiWidth, int roiHeight, Sector roiSector, AVList roiParams)
    {
        DataRaster canvas = new ByteBufferRaster(roiWidth, roiHeight, roiSector, roiParams);
        this.drawOnCanvas(canvas);
        return canvas;
    }
}
