/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.data;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.Cacheable;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.*;
import org.gdal.gdal.*;
import org.gdal.gdalconst.gdalconst;
import org.gdal.osr.SpatialReference;

import java.awt.geom.*;
import java.io.*;
import java.nio.*;
import java.util.*;

/**
 * @author Lado Garakanidze
 * @version $Id: GDALDataRaster.java 14217 2010-12-10 01:16:38Z garakl $
 */

public class GDALDataRaster extends AbstractDataRaster implements Cacheable, Disposable
{
    protected Dataset dsVRT = null;
    protected SpatialReference srs;
    protected File srcFile = null;
    protected GDAL.Area area = null;

    protected static final int DEFAULT_MAX_RASTER_SIZE_LIMIT = 3072;

    protected static int getMaxRasterSizeLimit()
    {
        return DEFAULT_MAX_RASTER_SIZE_LIMIT;
    }

    public GDALDataRaster(File file) throws IllegalArgumentException, FileNotFoundException
    {
        this(file, false);
    }

    /**
     * @param file
     * @param quickReadingMode if quick reading mode is enabled GDAL will not spend much time on heavy calculations,
     *                         like for example calculating Min/Max for entire elevation raster
     *
     * @throws IllegalArgumentException
     * @throws FileNotFoundException
     */
    public GDALDataRaster(File file, boolean quickReadingMode) throws IllegalArgumentException, FileNotFoundException
    {
        super();

        if (null == file)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.srcFile = file;
        String name = this.srcFile.getName();
        if (null != name && name.length() > 0)
        {
            this.setValue(AVKey.DATASET_NAME, name);
            this.setValue(AVKey.DISPLAY_NAME, name);
        }

        this.setValue(GDAL.READING_MODE, (quickReadingMode) ? GDAL.READING_MODE_QUICK : GDAL.READING_MODE_FULL);

        this.init(GDALUtils.open(file));
    }

    public void setSector(Sector sector) throws IllegalArgumentException
    {
        if (null == sector)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (!this.hasKey(AVKey.COORDINATE_SYSTEM)
            || AVKey.COORDINATE_SYSTEM_UNKNOWN.equals(this.getValue(AVKey.COORDINATE_SYSTEM))
            )
            this.setValue(AVKey.COORDINATE_SYSTEM, AVKey.COORDINATE_SYSTEM_GEOGRAPHIC);

        this.srs = GDALUtils.createGeographicSRS();

        this.setValue(AVKey.SECTOR, sector);

        this.area = new GDAL.Area(this.srs, sector);
        this.setValue(AVKey.GDAL_AREA, this.area);

        if (this.width > 0)
        {
            double dx = sector.getDeltaLonDegrees() / this.width;
            this.setValue(AVKey.PIXEL_WIDTH, dx);
        }

        if (this.height > 0)
        {
            double dy = sector.getDeltaLatDegrees() / this.height;
            this.setValue(AVKey.PIXEL_WIDTH, dy);
        }

        if (this.dsVRT != null)
        {
            if (!"VRT".equalsIgnoreCase(this.dsVRT.GetDriver().getShortName()))
            {
                Driver vrt = gdal.GetDriverByName("VRT");
                if (null != vrt)
                    this.dsVRT = vrt.CreateCopy("", this.dsVRT);
            }

            double[] gt = GDALUtils.calcGetGeoTransform(sector, this.width, this.height);
            this.dsVRT.SetGeoTransform(gt);

            String error = GDALUtils.getErrorMessage();
            if (error != null)
            {
                String message = Logging.getMessage("gdal.InternalError", error);
                Logging.logger().severe(message);
//                throw new WWRuntimeException( message );
            }

            if (null != this.srs)
                this.dsVRT.SetProjection(srs.ExportToWkt());

            error = GDALUtils.getErrorMessage();
            if (error != null)
            {
                String message = Logging.getMessage("gdal.InternalError", error);
                Logging.logger().severe(message);
//                throw new WWRuntimeException( message );
            }

            this.srs = this.readSpatialReference(this.dsVRT);
        }
    }

    protected SpatialReference readSpatialReference(Dataset ds)
    {
        if (null == ds)
        {
            String message = Logging.getMessage("nullValue.DataSetIsNull");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        String proj = ds.GetProjectionRef();
        if (null == proj || 0 == proj.length())
            proj = ds.GetProjection();

        if ((null == proj || 0 == proj.length()) && null != this.srcFile)
        {
            // check if there is a corresponding .PRJ (or .prj file)
            String pathToPrjFile = WWIO.replaceSuffix(this.srcFile.getAbsolutePath(), ".prj");
            File prjFile = new File(pathToPrjFile);

            if (!prjFile.exists() && Configuration.isUnixOS())
            {
                pathToPrjFile = WWIO.replaceSuffix(this.srcFile.getAbsolutePath(), ".PRJ");
                prjFile = new File(pathToPrjFile);
            }

            try
            {
                if (prjFile.exists())
                    proj = WWIO.readTextFile(prjFile);
            }
            catch (Exception e)
            {
                String message = Logging.getMessage("generic.UnknownProjection", proj);
                Logging.logger().severe(message);
            }
        }

        SpatialReference srs = null;

        if (!WWUtil.isEmpty(proj))
            srs = new SpatialReference(proj);

        if ((null == srs || srs.IsLocal() == 1) && this.hasKey(AVKey.SPATIAL_REFERENCE_WKT))
        {
            proj = this.getStringValue(AVKey.SPATIAL_REFERENCE_WKT);
            srs = new SpatialReference(proj);
        }

        return srs;
    }

    public GDALDataRaster(Dataset ds) throws IllegalArgumentException
    {
        super();

        if (null == ds)
        {
            String message = Logging.getMessage("nullValue.DataSetIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.init(ds);
    }

    /**
     * sets next key/value pairs:
     * <p/>
     * AVKey.WIDTH - the maximum width of the image
     * <p/>
     * AVKey.HEIGHT - the maximum height of the image
     * <p/>
     * AVKey.COORDINATE_SYSTEM - one of the next values: AVKey.COORDINATE_SYSTEM_SCREEN
     * AVKey.COORDINATE_SYSTEM_GEOGRAPHIC AVKey.COORDINATE_SYSTEM_PROJECTED
     * <p/>
     * AVKey.SECTOR - in case of Geographic CS, contains a regular Geographic Sector defined by lat/lon coordinates of
     * corners in case of Projected CS, contains a bounding box of the area
     */
    protected void init(Dataset ds)
    {
        String srcWKT = null;

        AVList extParams = new AVListImpl();
        AVList params = new AVListImpl();
        GDALMetadata.extractExtendedAndFormatSpecificMetadata(ds, extParams, params);
        this.setValues(params);

        this.srs = this.readSpatialReference(ds);
        if (null != this.srs)
        {
            srcWKT = this.srs.ExportToWkt();
            this.setValue(AVKey.SPATIAL_REFERENCE_WKT, this.srs.ExportToWkt());
        }

        GDALUtils.extractRasterParameters(ds, this);

        this.dsVRT = ds;

        this.width = (Integer) this.getValue(AVKey.WIDTH);
        this.height = (Integer) this.getValue(AVKey.HEIGHT);

        Object o = this.getValue(AVKey.GDAL_AREA);
        this.area = (o != null && o instanceof GDAL.Area) ? (GDAL.Area) o : null;

        String proj = ds.GetProjectionRef();
        proj = (null == proj || 0 == proj.length()) ? ds.GetProjection() : proj;

        if ((null == proj || 0 == proj.length())
            && (srcWKT == null || 0 == srcWKT.length())
            && AVKey.COORDINATE_SYSTEM_GEOGRAPHIC.equals(this.getValue(AVKey.COORDINATE_SYSTEM))
            )
        {   // this is a case where file has GEODETIC GeoTranform matrix but does not have CS or PROJECTION data
            this.srs = GDALUtils.createGeographicSRS();
            srcWKT = this.srs.ExportToWkt();
            this.setValue(AVKey.SPATIAL_REFERENCE_WKT, this.srs.ExportToWkt());
        }

        // if the original dataset does NOT have projection information
        // AND the "srcWKT" is not empty (was taken from the accompanied .PRJ file)
        // we need to create a VRT dataset to be able to change/assign projection/geotransforms etc
        // most real drivers do not support overriding properties
        // However, JP2 files are 3 times slow when wrapped in the VRT dataset
        // therefore, we only wrap in to VRT when needed
        if ((null == proj || 0 == proj.length()) && (null != srcWKT && 0 < srcWKT.length()))
        {
            try
            {
                Driver vrt = gdal.GetDriverByName("VRT");
                if (null != vrt)
                {
                    Dataset dsWarp = vrt.CreateCopy("", ds);
                    dsWarp.SetProjection(srcWKT);
                    this.dsVRT = dsWarp;
                }
                else
                {
                    String message = Logging.getMessage("gdal.InternalError", GDALUtils.getErrorMessage());
                    Logging.logger().severe(message);
                    throw new WWRuntimeException(message);
                }
            }
            catch (Exception e)
            {
                Logging.logger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    public AVList getMetadata()
    {
        return this.copy();
    }

    public void drawOnCanvas(DataRaster canvas, Sector clipSector)
    {
        if (canvas == null)
        {
            String message = Logging.getMessage("nullValue.DestinationIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.doDrawOnCanvas(canvas, clipSector);
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

    protected void doDrawOnCanvas(DataRaster canvas, Sector clipSector)
    {
        Sector imageSector = this.getSector();
        Sector canvasSector = canvas.getSector();
        Sector overlap = null;

        if (null == imageSector
            || null == canvasSector
            || !this.intersects(canvasSector)
            || null == (overlap = imageSector.intersection(canvasSector)))
        {
            String msg = Logging.getMessage("generic.SectorRequestedOutsideCoverageArea", canvasSector, imageSector);
            Logging.logger().finest(msg);
            return;
        }

        // TODO garakl (this was added for WMS server support - do we still need it?) 
//        if( null != clipSector )
//        {
//            if( !overlap.intersects(clipSector) )
//                return;
//            else
//                clipSector = overlap.intersection( clipSector );
//        }
//        else
        clipSector = overlap;

        // Compute the region of the destination raster to be be clipped by the specified clipping sector. If no
        // clipping sector is specified, then perform no clipping. We compute the clip region for the destination
        // raster because this region is used by AWT to limit which pixels are rasterized to the destination.
        java.awt.Rectangle clipRect = this.computeClipRect(clipSector, canvas);

        if (clipRect.width == 0 || clipRect.height == 0)
            return;

        try
        {
            DataRaster raster = this.doGetSubRaster(clipRect.width, clipRect.height, clipSector, canvas.copy());
            raster.drawOnCanvas(canvas, clipSector);
        }
        catch (WWRuntimeException wwe)
        {
            Logging.logger().severe(wwe.getMessage());
        }
        catch (Exception e)
        {
            Logging.logger().log(java.util.logging.Level.SEVERE, e.getMessage(), e);
        }
    }

    public long getSizeInBytes()
    {
        return 0L;
    }

    public void dispose()
    {
        if (this.dsVRT != null)
        {
            this.dsVRT.delete();
            this.dsVRT = null;
        }

        this.clearList();

        if (this.srcFile != null)
            this.srcFile = null;

        this.srs = null;
    }

    protected String convertGeoTransformToString(double[] gt)
    {
        StringBuffer sb = new StringBuffer("GeoTransform {");
        if (null != gt && gt.length > 0)
        {
            for (double value : gt)
            {
                sb.append(" ").append(value).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
        }

        sb.append(" }");
        return sb.toString();
    }

    protected Dataset createMaskDataset(int width, int height, Sector sector)
    {
        if (width <= 0)
        {
            String message = Logging.getMessage("generic.InvalidWidth", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height <= 0)
        {
            String message = Logging.getMessage("generic.InvalidHeight", height);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Driver drvMem = gdal.GetDriverByName("MEM");

        Dataset ds = drvMem.Create("roi-mask", width, height, 1, gdalconst.GDT_UInt32);
        Band band = ds.GetRasterBand(1);
        band.SetColorInterpretation(gdalconst.GCI_AlphaBand);
        double missingSignal = (double) GDALUtils.ALPHA_MASK;
        band.SetNoDataValue(missingSignal);
        band.Fill(missingSignal);

        if (null != sector)
        {
            SpatialReference t_srs = GDALUtils.createGeographicSRS();
            String t_srs_wkt = t_srs.ExportToWkt();
            ds.SetProjection(t_srs_wkt);

            ds.SetGeoTransform(GDALUtils.calcGetGeoTransform(sector, width, height));
        }

        return ds;
    }

    protected Dataset getBestSuitedDataset(int reqWidth, int reqHeight, Sector reqSector)
    {
        if (reqWidth <= 0)
        {
            String message = Logging.getMessage("generic.InvalidWidth", reqWidth);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (reqHeight <= 0)
        {
            String message = Logging.getMessage("generic.InvalidHeight", reqHeight);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (reqSector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (null == this.dsVRT)
        {
            String message = Logging.getMessage("nullValue.DataSetIsNull");
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        if (null == this.area)
            return this.dsVRT;

        Sector extent = this.getSector();
        if (!this.intersects(reqSector))
        {
            String msg = Logging.getMessage("generic.SectorRequestedOutsideCoverageArea", reqSector, extent);
            Logging.logger().finest(msg);
            throw new WWRuntimeException(msg);
        }

        Sector clipSector = extent.intersection(reqSector);
        double srcRasterSquare = extent.getDeltaLatDegrees() * extent.getDeltaLonDegrees();
        double clipRasterSquare = clipSector.getDeltaLatDegrees() * clipSector.getDeltaLonDegrees();

        double clipAreaRatio = 100d * clipRasterSquare / srcRasterSquare;
        if (clipAreaRatio <= 1d) // if requested area is less than 1% of the entire image, let GDAL do the job
            return this.dsVRT;

        Object cs = this.getValue(AVKey.COORDINATE_SYSTEM);
        if (null == cs
            || (!AVKey.COORDINATE_SYSTEM_GEOGRAPHIC.equals(cs) && !AVKey.COORDINATE_SYSTEM_PROJECTED.equals(cs)))
        {
            String msg = (null == cs) ? "generic.UnspecifiedCoordinateSystem" : "generic.UnsupportedCoordinateSystem";
            String reason = Logging.getMessage(msg, cs);
            Logging.logger().finest(Logging.getMessage("generic.CannotCreateRaster", reason));
            return this.dsVRT;
        }

        double reqWidthRes = Math.abs(reqSector.getDeltaLonDegrees() / (double) reqWidth);
        double reqHeightRes = Math.abs(reqSector.getDeltaLatDegrees() / (double) reqHeight);

        int bandCount = this.dsVRT.getRasterCount();
        if (bandCount == 0)
            return this.dsVRT;

        Band firstBand = this.dsVRT.GetRasterBand(1);
        if (null == firstBand)
            return this.dsVRT;

        double[] gt = new double[6];
        this.dsVRT.GetGeoTransform(gt);

        boolean isNorthUpRaster = (gt[GDAL.GT_2_ROTATION_X] == 0d && gt[GDAL.GT_4_ROTATION_Y] == 0d);

        // ==============================================

//        Angle rotAngle = Angle.fromRadians( Math.tanh( gt[GDAL.GT_2_ROTATION_X] / gt[GDAL.GT_5_PIXEL_HEIGHT] ) );
//
////        cellx=round(math.hypot(gt[1],gt[4]),7)
////      celly=round(math.hypot(gt[2],gt[5]),7)
//
//        double cellx = Math.hypot( gt[GDAL.GT_1_PIXEL_WIDTH], gt[GDAL.GT_4_ROTATION_Y] );
//        double celly = Math.hypot( gt[GDAL.GT_2_ROTATION_X], gt[GDAL.GT_5_PIXEL_HEIGHT] );
//
////        math.degrees(math.tanh(gt[2]/gt[5]))

//        Angle angX = Angle.asin( gt[GDAL.GT_2_ROTATION_X] );
//        Angle angY = Angle.asin( gt[GDAL.GT_4_ROTATION_Y] );
//
//        double[] gt2 = new double[6];
//        this.dsVRT.GetGeoTransform(gt2);
//        gt2[GDAL.GT_2_ROTATION_X] = gt2[GDAL.GT_4_ROTATION_Y] = 0d;
//
//
//
//        Point2D ur = GDALUtils.getGeoPointForRasterPoint( gt, this.getWidth(), this.getHeight() );
//        Point2D ur2 = GDALUtils.getGeoPointForRasterPoint( gt2, this.getWidth(), this.getHeight() );
//
//        Vec4 origin = new Vec4( gt[GDAL.GT_0_ORIGIN_LON], gt[GDAL.GT_3_ORIGIN_LAT] );
//        Line lineUR = Line.fromSegment( origin, new Vec4( ur.getX(), ur.getY() ));
//        Line lineUR2 = Line.fromSegment( origin, new Vec4( ur2.getX(), ur2.getY() ));
//
//        Vec4 dirUR = lineUR.getDirection();
//        Vec4 dirUR2 = lineUR2.getDirection();
//
//        Vec4[] result = new Vec4[2];
//        Angle ax = Vec4.axisAngle(  dirUR, dirUR2, result );

        // ==============================================

        int bestOverviewIdx = -1;
        double ovWidthRes = 0d;
        double ovHeightRes = 0d;

        int srcHeight = this.getHeight();
        int srcWidth = this.getWidth();

        for (int i = 0; i < firstBand.GetOverviewCount(); i++)
        {
            Band overview = firstBand.GetOverview(i);
            if (null == overview)
                continue;

            int w = overview.getXSize();
            int h = overview.getYSize();

            if (0 == h || 0 == w)
                continue;

//            ovWidthRes = Math.abs(extent.getDeltaLonDegrees() / (double) w);
            ovHeightRes = Math.abs(extent.getDeltaLatDegrees() / (double) h);

            if (ovHeightRes <= reqHeightRes /*&& ovWidthRes <= reqWidthRes*/)
            {
                bestOverviewIdx = i;
                srcWidth = w;
                srcHeight = h;
                continue;
            }
            else
                break;
        }

        if (!isNorthUpRaster)
        {
            // It is a non-Northup oriented raster  (raster with rotation coefficients in the GT matrix)

            if (bestOverviewIdx == -1)
            {
                // no overviews, working with a full resolution raster
                srcHeight = this.getHeight();
                srcWidth = this.getWidth();

                for (int i = 0; true; i++)
                {
                    double scale = Math.pow(2, i);
                    double h = Math.floor(this.getHeight() / scale);
                    double w = Math.floor(this.getWidth() / scale);
                    ovWidthRes = Math.abs(extent.getDeltaLonDegrees() / w);
                    ovHeightRes = Math.abs(extent.getDeltaLatDegrees() / h);
                    if (ovHeightRes <= reqHeightRes && ovWidthRes <= reqWidthRes)
                    {
                        srcWidth = (int) w;
                        srcHeight = (int) h;
                        continue;
                    }
                    else
                        break;
                }
            }

            if (srcHeight > getMaxRasterSizeLimit() || srcWidth > getMaxRasterSizeLimit())
                return this.dsVRT;

            String msg = Logging.getMessage("gdal.UseOverviewRaster", srcWidth, srcHeight, reqWidth, reqHeight);
            Logging.logger().finest(msg);

            Dataset ds = this.buildNonNorthUpDatasetFromOverview(bestOverviewIdx, srcWidth, srcHeight);

            return (null != ds) ? ds : this.dsVRT;
        }

        if (bestOverviewIdx == -1)
        {
            // no overview was found, will use image's source bands
            srcWidth = this.getWidth();
            srcHeight = this.getHeight();
            ovWidthRes = Math.abs(reqSector.getDeltaLonDegrees() / (double) srcWidth);
            ovHeightRes = Math.abs(reqSector.getDeltaLatDegrees() / (double) srcHeight);

            String msg = Logging.getMessage("gdal.GenerateSmallerRaster", this.getWidth(), this.getHeight(), srcWidth,
                srcHeight);
            Logging.logger().finest(msg);

            // TODO garakl debug uncomment
//            return this.dsVRT;
        }
        else
        {
            String msg = Logging.getMessage("gdal.UseOverviewRaster", srcWidth, srcHeight, reqWidth, reqHeight);
            Logging.logger().finest(msg);
        }

        return this.buildNorthUpDatasetFromOverview(reqSector, reqWidth, reqHeight, bestOverviewIdx, srcWidth,
            srcHeight);
    }

    protected Dataset buildNorthUpDatasetFromOverview(Sector reqSector, int reqWidth, int reqHeight,
        int bestOverviewIdx, int srcWidth, int srcHeight)
    {
        GDAL.Area cropArea = this.area.intersection(new GDAL.Area(this.srs, reqSector).getBoundingArea());
        if (null == cropArea)
        {
            String msg = Logging.getMessage("generic.SectorRequestedOutsideCoverageArea", reqSector, this.area);
            Logging.logger().finest(msg);
            throw new WWRuntimeException(msg);
        }

        java.awt.geom.AffineTransform geoToRaster = this.area.computeGeoToRasterTransform(srcWidth, srcHeight);

        java.awt.geom.Point2D geoPoint = new java.awt.geom.Point2D.Double();
        java.awt.geom.Point2D ul = new java.awt.geom.Point2D.Double();
        java.awt.geom.Point2D lr = new java.awt.geom.Point2D.Double();

        geoPoint.setLocation(cropArea.getMinX(), cropArea.getMaxY());
        geoToRaster.transform(geoPoint, ul);

        geoPoint.setLocation(cropArea.getMaxX(), cropArea.getMinY());
        geoToRaster.transform(geoPoint, lr);

        int clipXoff = (int) Math.floor(ul.getX());
        int clipYoff = (int) Math.floor(ul.getY());
        int clipWidth = (int) Math.floor(lr.getX() - ul.getX());
        int clipHeight = (int) Math.floor(lr.getY() - ul.getY());

        clipWidth = (clipWidth > srcWidth) ? srcWidth : clipWidth;
        clipHeight = (clipHeight > srcHeight) ? srcHeight : clipHeight;

        Driver drv = gdal.GetDriverByName("MEM");
        if (null == drv)
            return this.dsVRT;

        int bandCount = this.dsVRT.getRasterCount();
        if (bandCount == 0)
            return this.dsVRT;

        Band firstBand = this.dsVRT.GetRasterBand(1);
        if (null == firstBand)
            return this.dsVRT;

        int dataType = firstBand.GetRasterDataType();

        Dataset ds = drv.Create("overview", reqWidth, reqHeight, bandCount, dataType);
        if (this.srs != null)
            ds.SetProjection(this.srs.ExportToWkt());

        double[] gt = new double[6];

        gt[GDAL.GT_0_ORIGIN_LON] = cropArea.getMinX();
        gt[GDAL.GT_3_ORIGIN_LAT] = cropArea.getMaxY();
        gt[GDAL.GT_1_PIXEL_WIDTH] = Math.abs((cropArea.getMaxX() - cropArea.getMinX()) / (double) reqWidth);
        gt[GDAL.GT_5_PIXEL_HEIGHT] = -Math.abs((cropArea.getMaxY() - cropArea.getMinY()) / (double) reqHeight);
        gt[GDAL.GT_2_ROTATION_X] = gt[GDAL.GT_4_ROTATION_Y] = 0d;

        ds.SetGeoTransform(gt);

        int size = reqWidth * reqHeight * (gdal.GetDataTypeSize(dataType) / 8);
        ByteBuffer data = ByteBuffer.allocateDirect(size);
        data.order(ByteOrder.nativeOrder());

        Double nodata = this.hasKey(AVKey.MISSING_DATA_SIGNAL) ? (Double) this.getValue(AVKey.MISSING_DATA_SIGNAL)
            : null;

        for (int i = 0; i < bandCount; i++)
        {
            Band srcBand = this.dsVRT.GetRasterBand(i + 1);
            if (null == srcBand)
                continue;

            Band ovBand = (bestOverviewIdx == -1) ? srcBand : srcBand.GetOverview(bestOverviewIdx);
            if (null == ovBand)
                continue;

            Band destBand = ds.GetRasterBand(i + 1);
            if (null != nodata)
                destBand.SetNoDataValue(nodata);

            int colorInt = srcBand.GetColorInterpretation();
            destBand.SetColorInterpretation(colorInt);
            if (colorInt == gdalconst.GCI_PaletteIndex)
                destBand.SetColorTable(srcBand.GetColorTable());

            data.rewind();
            ovBand.ReadRaster_Direct(clipXoff, clipYoff, clipWidth, clipHeight, reqWidth, reqHeight, dataType, data);

            data.rewind();
            destBand.WriteRaster_Direct(0, 0, reqWidth, reqHeight, dataType, data);
        }

        return ds;
    }

    protected Dataset buildNonNorthUpDatasetFromOverview(int bestOverviewIdx, int destWidth, int destHeight)
    {
        if (null == this.dsVRT)
            return null;

        Driver drv = gdal.GetDriverByName("MEM");
        if (null == drv)
            return null;

        Band firstBand = this.dsVRT.GetRasterBand(1);
        if (null == firstBand)
            return null;

        int bandCount = this.dsVRT.GetRasterCount();
        int destDataType = firstBand.GetRasterDataType();

        int size = destWidth * destHeight * (gdal.GetDataTypeSize(destDataType) / 8);
        ByteBuffer data = ByteBuffer.allocateDirect(size);
        data.order(ByteOrder.nativeOrder());

        Double nodata = this.hasKey(AVKey.MISSING_DATA_SIGNAL) ? (Double) this.getValue(AVKey.MISSING_DATA_SIGNAL)
            : null;

        Dataset ds = drv.Create("overview", destWidth, destHeight, bandCount, destDataType);
        if (this.srs != null)
            ds.SetProjection(this.srs.ExportToWkt());

        AffineTransform atxOverview = GDAL.getAffineTransform(this.dsVRT, destWidth, destHeight);

        double[] gt = new double[6];
        gt[GDAL.GT_0_ORIGIN_LON] = atxOverview.getTranslateX();
        gt[GDAL.GT_1_PIXEL_WIDTH] = atxOverview.getScaleX();
        gt[GDAL.GT_2_ROTATION_X] = atxOverview.getShearX();
        gt[GDAL.GT_3_ORIGIN_LAT] = atxOverview.getTranslateY();
        gt[GDAL.GT_4_ROTATION_Y] = atxOverview.getShearY();
        gt[GDAL.GT_5_PIXEL_HEIGHT] = atxOverview.getScaleY();

        ds.SetGeoTransform(gt);

        // TODO garakl - calculate crop corners (GCPs) and generate new GT
//        GCP[] gcps = new GCP[4];
//        // Order must be [ulx,uly],[urx,ury],[lrx,lry],[llx,lly]
//        gcps[0] = new GCP( this.area.corners[3].getX(), this.area.corners[3].getY(),         0,          0 );
//        gcps[1] = new GCP( this.area.corners[2].getX(), this.area.corners[2].getY(), destWidth,          0 );
//        gcps[2] = new GCP( this.area.corners[1].getX(), this.area.corners[1].getY(), destWidth, destHeight );
//        gcps[3] = new GCP( this.area.corners[0].getX(), this.area.corners[0].getY(),         0, destHeight );
//
//
//        double[] gt2 = new double[6];
//        gdal.GCPsToGeoTransform( gcps, gt2 );

        for (int i = 0; i < bandCount; i++)
        {
            Band srcBand = this.dsVRT.GetRasterBand(i + 1);
            if (null == srcBand)
                continue;

            Band ovBand = (bestOverviewIdx == -1) ? srcBand : srcBand.GetOverview(bestOverviewIdx);
            if (null == ovBand)
                continue;

            Band destBand = ds.GetRasterBand(i + 1);
            if (null != nodata)
                destBand.SetNoDataValue(nodata);

            int colorInt = srcBand.GetColorInterpretation();
            destBand.SetColorInterpretation(colorInt);
            if (colorInt == gdalconst.GCI_PaletteIndex)
                destBand.SetColorTable(srcBand.GetColorTable());

            data.rewind();
            ovBand.ReadRaster_Direct(0, 0, ovBand.getXSize(), ovBand.getYSize(),
                destWidth, destHeight, destDataType, data);

            data.rewind();
            destBand.WriteRaster_Direct(0, 0, destWidth, destHeight, destDataType, data);
        }

        return ds;
    }

    protected Dataset createCompatibleDataset(int width, int height, Sector sector, AVList destParams)
    {
        if (width <= 0)
        {
            String message = Logging.getMessage("generic.InvalidWidth", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height <= 0)
        {
            String message = Logging.getMessage("generic.InvalidHeight", height);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Driver drvMem = gdal.GetDriverByName("MEM");
        int srcNumOfBands = this.dsVRT.getRasterCount();
        Band srcBand1 = this.dsVRT.GetRasterBand(1);
        int bandDataType = srcBand1.getDataType();

        int[] bandColorInt = new int[] {gdalconst.GCI_RedBand, gdalconst.GCI_GreenBand,
            gdalconst.GCI_BlueBand, gdalconst.GCI_AlphaBand};

        int destNumOfBands = 4; // RGBA by default
        String rasterType = this.getStringValue(AVKey.RASTER_TYPE);
        if (AVKey.RASTER_TYPE_ELEVATION.equals(rasterType))
        {
            destNumOfBands = 1;
            bandColorInt = new int[] {gdalconst.GCI_GrayIndex};
        }
        else if (AVKey.RASTER_TYPE_MONOCHROME_IMAGE.equals(rasterType))
        {
            bandColorInt = new int[] {gdalconst.GCI_GrayIndex, gdalconst.GCI_AlphaBand};
            destNumOfBands = 2; // Y + alpha
        }
        else if (AVKey.RASTER_TYPE_COLOR_IMAGE.equals(rasterType))
        {
            bandColorInt = new int[] {
                gdalconst.GCI_RedBand, gdalconst.GCI_GreenBand, gdalconst.GCI_BlueBand, gdalconst.GCI_AlphaBand};

            if (AVKey.INT16.equals(this.getValue(AVKey.DATA_TYPE)) && srcNumOfBands > 3)
            {
                destNumOfBands = 3; // ignore 4th band which is some kind of infra-red
            }
            else if (srcNumOfBands >= 3)
            {
                destNumOfBands = 4; // RGBA
            }
            else
            {
                destNumOfBands = 1; // indexed 256 color image (like CADRG)
                bandColorInt = new int[] {gdalconst.GCI_PaletteIndex};
            }
        }

        Dataset ds = drvMem.Create("roi", width, height, destNumOfBands, bandDataType);

        Double nodata = this.calcNoDataForDestinationRaster(destParams);

        for (int i = 0; i < destNumOfBands; i++)
        {
            Band band = ds.GetRasterBand(i + 1);
            if (nodata != null)
                band.SetNoDataValue(nodata);

            Band srcBand = (i < srcNumOfBands) ? this.dsVRT.GetRasterBand(i + 1) : null;

            int colorInt = gdalconst.GCI_Undefined;

            if (null != srcBand)
            {
                colorInt = srcBand.GetColorInterpretation();

                if (colorInt == gdalconst.GCI_Undefined)
                    colorInt = bandColorInt[i];

                band.SetColorInterpretation(colorInt);

                if (colorInt == gdalconst.GCI_PaletteIndex)
                    band.SetColorTable(srcBand.GetColorTable());
            }
            else
            {
                colorInt = bandColorInt[i];
                band.SetColorInterpretation(colorInt);
            }

            if (colorInt == gdalconst.GCI_AlphaBand)
                band.Fill((double) GDALUtils.ALPHA_MASK);

            if (null != nodata && colorInt == gdalconst.GCI_GrayIndex)
            {
                band.Fill(nodata);
                if (null != srcBand)
                {
                    Double[] min = new Double[1];
                    srcBand.GetMinimum(min);

                    Double[] max = new Double[1];
                    srcBand.GetMaximum(max);

                    if (null != min[0] && null != max[0])
                        band.SetStatistics(min[0], max[0], 0d, 0d);
                }
            }
        }

        if (null != sector)
        {
            SpatialReference t_srs = GDALUtils.createGeographicSRS();
            String t_srs_wkt = t_srs.ExportToWkt();
            ds.SetProjection(t_srs_wkt);

            ds.SetGeoTransform(GDALUtils.calcGetGeoTransform(sector, width, height));
        }

        String[] keysToCopy = new String[] {
            AVKey.RASTER_BAND_ACTUAL_BITS_PER_PIXEL,
            AVKey.RASTER_BAND_BITS_PER_PIXEL,
            AVKey.RASTER_BAND_MAX_PIXEL_VALUE
        };
        copyValues(this, destParams, keysToCopy);

        return ds;
    }

    protected Double calcNoDataForDestinationRaster(AVList destParams)
    {
        Double nodata = null;

        if (destParams != null && destParams.hasKey(AVKey.MISSING_DATA_REPLACEMENT))
        {
            Object o = destParams.getValue(AVKey.MISSING_DATA_REPLACEMENT);
            if (null != o && o instanceof Double)
                nodata = (Double) o;
        }

        if (nodata == null && destParams != null && destParams.hasKey(AVKey.MISSING_DATA_SIGNAL))
        {
            Object o = destParams.getValue(AVKey.MISSING_DATA_SIGNAL);
            if (null != o && o instanceof Double)
                nodata = (Double) o;
        }

        if (nodata == null && this.hasKey(AVKey.MISSING_DATA_REPLACEMENT))
        {
            Object o = this.getValue(AVKey.MISSING_DATA_REPLACEMENT);
            if (null != o && o instanceof Double)
                nodata = (Double) o;
        }

        if (null == nodata && this.hasKey(AVKey.MISSING_DATA_SIGNAL))
        {
            Object o = this.getValue(AVKey.MISSING_DATA_SIGNAL);
            if (null != o && o instanceof Double)
                nodata = (Double) o;
        }

        if (null == nodata && AVKey.RASTER_TYPE_ELEVATION.equals(this.getValue(AVKey.RASTER_TYPE)))
            nodata = (double) (Short.MIN_VALUE);

        return nodata;
    }

    @Override
    public DataRaster getSubRaster(AVList params)
    {
        if (params.hasKey(AVKey.BANDS_ORDER))
            GDALUtils.extractBandOrder(this.dsVRT, params);

        return super.getSubRaster(params);
    }

    protected DataRaster doGetSubRaster(int roiWidth, int roiHeight, Sector roiSector, AVList roiParams)
    {
        Dataset destDS = null;
        Dataset maskDS = null;
        Dataset srcDS = null;
        DataRaster raster = null;

        try
        {
            roiParams = (null == roiParams) ? new AVListImpl() : roiParams;

            if (null != roiSector)
                roiParams.setValue(AVKey.SECTOR, roiSector);

            roiParams.setValue(AVKey.WIDTH, roiWidth);
            roiParams.setValue(AVKey.HEIGHT, roiHeight);

            if (null == roiSector
                || Sector.EMPTY_SECTOR.equals(roiSector)
                || !this.hasKey(AVKey.COORDINATE_SYSTEM)
                || AVKey.COORDINATE_SYSTEM_UNKNOWN.equals(this.getValue(AVKey.COORDINATE_SYSTEM))
                )
            {
                // return the entire data raster
                return GDALUtils.composeDataRaster(this.dsVRT, roiParams);
            }

            destDS = this.createCompatibleDataset(roiWidth, roiHeight, roiSector, roiParams);

            String t_srs_wkt = destDS.GetProjection();
//            SpatialReference t_srs = new SpatialReference(t_srs_wkt);

            // check if image fully contains the ROI, in this case we do not need mask
            if (null == this.area || null == this.srs || !this.area.contains(new GDAL.Area(this.srs, roiSector)))
                maskDS = this.createMaskDataset(roiWidth, roiHeight, roiSector);

            long projTime = 0L, maskTime = 0L, cropTime = 0L, totalTime = System.currentTimeMillis();

            long start = System.currentTimeMillis();

            srcDS = this.getBestSuitedDataset(roiWidth, roiHeight, roiSector);
            if (srcDS == this.dsVRT)
            {
                String message = Logging.getMessage("gdal.UseFullResolutionRaster", this.getWidth(), this.getHeight(),
                    roiWidth, roiHeight);
                Logging.logger().finest(message);
            }

            cropTime = System.currentTimeMillis() - start;

            start = System.currentTimeMillis();
            if (this.srs != null)
            {
                String s_srs_wkt = this.srs.ExportToWkt();

                gdal.ReprojectImage(srcDS, destDS, s_srs_wkt, t_srs_wkt, gdalconst.GRA_Bilinear);
                projTime = System.currentTimeMillis() - start;

                start = System.currentTimeMillis();
                if (null != maskDS)
                    gdal.ReprojectImage(srcDS, maskDS, s_srs_wkt, t_srs_wkt, gdalconst.GRA_NearestNeighbour);
                maskTime = System.currentTimeMillis() - start;
            }
            else
            {
                gdal.ReprojectImage(srcDS, destDS);
                projTime = System.currentTimeMillis() - start;

                start = System.currentTimeMillis();
                if (null != maskDS)
                    gdal.ReprojectImage(srcDS, maskDS);
                maskTime = System.currentTimeMillis() - start;
            }

            String error = GDALUtils.getErrorMessage();
            if (error != null)
            {
                String message = Logging.getMessage("gdal.InternalError", error);
                Logging.logger().severe(message);
//            throw new WWRuntimeException( message );
            }

            if (null != maskDS)
                roiParams.setValue(AVKey.GDAL_MASK_DATASET, maskDS);

            start = System.currentTimeMillis();
            raster = GDALUtils.composeDataRaster(destDS, roiParams);
            long composeTime = System.currentTimeMillis() - start;

            Logging.logger().finest("doGetSubRaster(): [" + roiWidth + "x" + roiHeight + "] - "
                + " totalTime = " + (System.currentTimeMillis() - totalTime)
                + " msec { Cropping = " + cropTime + " msec, Reprojection = " + projTime
                + " msec, Masking = " + maskTime + " msec, Composing = " + composeTime + " msec }");
        }
        finally
        {
            if (null != maskDS)
                maskDS.delete();

            if (null != destDS && destDS != this.dsVRT)
                destDS.delete();

            if (null != srcDS && srcDS != this.dsVRT)
                srcDS.delete();
        }
        return raster;
    }

    protected static Band findAlphaBand(Dataset ds)
    {
        if (null != ds)
        {
            // search backward
            int bandCount = ds.getRasterCount();
            for (int i = bandCount; i > 0; i--)
            {
                Band band = ds.GetRasterBand(i);
                if (band.GetColorInterpretation() == gdalconst.GCI_AlphaBand)
                    return band;
            }
        }
        return null;
    }

    protected static void copyValues(AVList src, AVList dest, String[] keys)
    {
        if (WWUtil.isEmpty(src) || WWUtil.isEmpty(dest) || WWUtil.isEmpty(keys))
            return;

        for (String key : keys)
        {
            Object o = src.getValue(key);
            if (!WWUtil.isEmpty(o))
                dest.setValue(key, o);
        }
    }

    protected static String convertAVListToString(AVList list)
    {
        if (null == list)
            return "";

        StringBuffer sb = new StringBuffer("{ ");
        Vector<String> keys = new Vector<String>();

        Set<Map.Entry<String, Object>> entries = list.getEntries();
        for (Map.Entry<String, Object> entry : entries)
        {
            keys.add(entry.getKey());
        }

        // sort keys
        Collections.sort(keys);

        for (String key : keys)
        {
            sb.append("\n").append(key).append("=").append(list.getValue(key));
        }
        sb.append("\n};");

        return sb.toString();
    }

    @Override
    public String toString()
    {
        return "GDALDataRaster " + convertAVListToString(this);
    }

    protected boolean intersects(Sector reqSector)
    {
        if (null != reqSector)
        {
            if (null != this.area)
                return (null != this.area.intersection(reqSector));
            else
                return reqSector.intersects(this.getSector());
        }
        return false;
    }
}