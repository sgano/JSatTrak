/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.terrain;

import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.formats.worldfile.WorldFile;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.*;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author tag
 * @version $Id: LocalElevationModel.java 13113 2010-02-10 06:19:45Z tgaskins $
 */
public class LocalElevationModel extends AbstractElevationModel
{
    protected double[] extremeElevations = null;
    protected CopyOnWriteArrayList<LocalTile> tiles = new CopyOnWriteArrayList<LocalTile>();

    public double getMinElevation()
    {
        return this.extremeElevations[0];
    }

    public double getMaxElevation()
    {
        return this.extremeElevations[1];
    }

    public double[] getExtremeElevations(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.tiles.size() == 0)
            return new double[] {this.getMinElevation(), this.getMaxElevation()};

        double min = Double.MAX_VALUE;
        double max = -min;

        for (LocalTile tile : this.tiles)
        {
            if (tile.sector.contains(latitude, longitude))
            {
                if (tile.minElevation < min)
                    min = tile.minElevation;
                if (tile.maxElevation > max)
                    max = tile.maxElevation;
            }
        }

        return new double[] {
            min != Double.MAX_VALUE ? min : this.getMinElevation(),
            max != -Double.MAX_VALUE ? max : this.getMaxElevation()};
    }

    public double[] getExtremeElevations(Sector sector)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.tiles.size() == 0)
            return new double[] {this.getMinElevation(), this.getMaxElevation()};

        double min = Double.MAX_VALUE;
        double max = -min;

        for (LocalTile tile : this.tiles)
        {
            if (tile.sector.intersects(sector))
            {
                if (tile.minElevation < min)
                    min = tile.minElevation;
                if (tile.maxElevation > max)
                    max = tile.maxElevation;
            }
        }

        return new double[] {
            min != Double.MAX_VALUE ? min : this.getMinElevation(),
            max != -Double.MAX_VALUE ? max : this.getMaxElevation()};
    }

    public double getBestResolution(Sector sector)
    {
        double res = Double.MAX_VALUE;

        for (LocalTile tile : tiles)
        {
            if (!sector.intersects(tile.sector))
                continue;

            double r = tile.sector.getDeltaLatRadians() / tile.tileHeight;
            if (r < res)
                res = r;
        }

        return res;
    }

    public double getUnmappedElevation(Angle latitude, Angle longitude)
    {
        if (latitude == null || longitude == null)
        {
            String msg = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (!this.contains(latitude, longitude))
            return this.missingDataFlag;

        Double e = this.lookupElevation(latitude.radians, longitude.radians);

        return e != null ? e : this.getExtremeElevations(latitude, longitude)[0];
    }

    public double getElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution, double[] buffer)
    {
        return this.doGetElevations(sector, latlons, targetResolution, buffer, true);
    }

    public double getUnmappedElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution,
        double[] buffer)
    {
        return this.doGetElevations(sector, latlons, targetResolution, buffer, false);
    }

    public double doGetElevations(Sector sector, List<? extends LatLon> latlons, double targetResolution,
        double[] buffer, boolean mapMissingData)
    {
        if (sector == null)
        {
            String msg = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (latlons == null)
        {
            String msg = Logging.getMessage("nullValue.LatLonListIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (buffer == null)
        {
            String msg = Logging.getMessage("nullValue.ElevationsBufferIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (buffer.length < latlons.size())
        {
            String msg = Logging.getMessage("ElevationModel.ElevationsBufferTooSmall", latlons.size());
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        if (this.intersects(sector) == -1)
            return Double.MAX_VALUE;

        for (int i = 0; i < latlons.size(); i++)
        {
            LatLon ll = latlons.get(i);
            if (ll == null)
                continue;

            // If an elevation at the given location is available, write that elevation to the destination buffer.
            // If an elevation is not available but the location is within the elevation model's coverage, write the
            // elevation models extreme elevation at the location. Do nothing if the location is not within the
            // elevation model's coverage.
            Double e = this.lookupElevation(ll.getLatitude().radians, ll.getLongitude().radians);
            if (e != null && e != this.missingDataFlag)
                buffer[i] = e;
            else if (this.contains(ll.getLatitude(), ll.getLongitude()))
            {
                if (e == null)
                    buffer[i] = this.getExtremeElevations(sector)[0];
                else if (mapMissingData && e == this.getMissingDataSignal())
                    buffer[i] = this.getMissingDataReplacement();
            }
        }

        return this.getBestResolution(sector);
    }

    public void addElevations(String filePath, Sector sector, int width, int height) throws IOException
    {
        if (filePath == null)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        File file = new File(filePath);

        if (!file.exists())
        {
            String message = Logging.getMessage("generic.FileNotFound", file.getPath());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (sector == null)
        {
            String message = Logging.getMessage("nullValue.SectorIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        try
        {
            File[] worldFiles = WorldFile.getWorldFiles(file);
            AVList worldFileParams = WorldFile.decodeWorldFiles(worldFiles, null);

            // Setup parameters to instruct BufferWrapper on how to interpret the ByteBuffer.
            Object pixelType = worldFileParams.getValue(AVKey.PIXEL_TYPE);
            if (pixelType == null)
                pixelType = AVKey.INT16;

            Object byteOrder = worldFileParams.getValue(AVKey.BYTE_ORDER);
            if (byteOrder == null)
                byteOrder = AVKey.LITTLE_ENDIAN;

            // Get the missing data value from the world file parameters. If that is null, then attempt to get the
            // missing data value from the ElevationModel.
            // This code expects the string "gov.nasa.worldwind.avkey.MissingDataValue", which now corresponds to the 
            // key MISSING_DATA_REPLACEMENT.
            Double tileMissingDataFlag = AVListImpl.getDoubleValue(worldFileParams, AVKey.MISSING_DATA_REPLACEMENT);
            if (tileMissingDataFlag == null)
                tileMissingDataFlag = this.getMissingDataSignal();

            // TODO: determine and react to other formats
            // Assume .bil format
            AVList bufferParams = new AVListImpl();
            bufferParams.setValue(AVKey.DATA_TYPE, pixelType);
            bufferParams.setValue(AVKey.BYTE_ORDER, byteOrder);
            ByteBuffer byteBuffer = WWIO.readFileToBuffer(file);
            BufferWrapper buffer = BufferWrapper.wrap(byteBuffer, bufferParams);

            LocalTile tile = new LocalTile(sector, tileMissingDataFlag, width, height, buffer);
            this.tiles.add(tile);
            this.adjustMinMax(tile);
        }
        catch (IOException e)
        {
            String message = Logging.getMessage("ElevationModel.ExceptionReadingElevationFile", filePath);
            Logging.logger().severe(message);
            throw e;
        }
    }

    public void addElevations(String filePath) throws IOException
    {
        if (filePath == null)
        {
            String message = Logging.getMessage("nullValue.FilePathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        File file = new File(filePath);

        if (!file.exists())
        {
            String message = Logging.getMessage("generic.FileNotFound", file.getPath());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String suffix = WWIO.getSuffix(filePath);
        if (suffix == null)
        {
            return; // TODO: throw exception and log message
        }

        this.addElevationsFromWorldFiles(filePath);

        // TODO: determine and react to other formats
    }

    protected void addElevationsFromWorldFiles(String filePath) throws IOException
    {
        int size[];
        Sector sector;

        try
        {
            File[] worldFiles = WorldFile.getWorldFiles(new File(filePath));
            if (worldFiles == null || worldFiles.length == 0)
            {
                String message = Logging.getMessage("WorldFile.WorldFileNotFound", filePath);
                Logging.logger().severe(message);
                throw new FileNotFoundException(message);
            }

            AVList values = WorldFile.decodeWorldFiles(worldFiles, null);

            size = (int[]) values.getValue(WorldFile.WORLD_FILE_IMAGE_SIZE);
            if (size == null)
            {
                String message = Logging.getMessage("WorldFile.NoSizeSpecified", filePath);
                throw new WWRuntimeException(message);
            }

            sector = (Sector) values.getValue(AVKey.SECTOR);
            if (sector == null)
            {
                String message = Logging.getMessage("WorldFile.NoSectorSpecified", filePath);
                throw new WWRuntimeException(message);
            }
        }
        catch (IOException e)
        {
            String message = Logging.getMessage("ElevationModel.ExceptionReadingElevationFile", filePath);
            Logging.logger().severe(message);
            throw e;
        }

        this.addElevations(filePath, sector, size[0], size[1]);
    }

    public int intersects(Sector sector)
    {
        boolean intersects = false;

        for (LocalTile tile : this.tiles)
        {
            if (tile.sector.contains(sector))
                return 0;

            if (tile.sector.intersects(sector))
                intersects = true;
        }

        return intersects ? 1 : -1;
    }

    public boolean contains(Angle latitude, Angle longitude)
    {
        for (LocalTile tile : this.tiles)
        {
            if (tile.sector.contains(latitude, longitude))
                return true;
        }

        return false;
    }

    protected void adjustMinMax(LocalTile tile)
    {
        if (this.extremeElevations == null && tile != null)
        {
            this.extremeElevations = new double[] {tile.minElevation, tile.maxElevation};
        }
        else if (tile != null) // adjust for just the input tile
        {
            if (tile.minElevation < this.extremeElevations[0])
                this.extremeElevations[0] = tile.minElevation;
            if (tile.maxElevation > this.extremeElevations[1])
                this.extremeElevations[1] = tile.maxElevation;
        }
        else // Find the min and max among all the tiles
        {
            double min = Double.MAX_VALUE;
            double max = -min;

            for (LocalTile t : this.tiles)
            {
                if (t.minElevation < min)
                    min = t.minElevation;
                if (t.maxElevation > max)
                    max = t.maxElevation;
            }

            this.extremeElevations =
                new double[] {min != Double.MAX_VALUE ? min : 0, max != -Double.MAX_VALUE ? max : 0};
        }
    }

    protected Double lookupElevation(final double latRadians, final double lonRadians)
    {
        LocalTile tile = this.findTile(latRadians, lonRadians);
        if (tile == null)
            return null;

        final double sectorDeltaLat = tile.sector.getDeltaLat().radians;
        final double sectorDeltaLon = tile.sector.getDeltaLon().radians;
        final double dLat = tile.sector.getMaxLatitude().radians - latRadians;
        final double dLon = lonRadians - tile.sector.getMinLongitude().radians;
        final double sLat = dLat / sectorDeltaLat;
        final double sLon = dLon / sectorDeltaLon;

        int j = (int) ((tile.tileHeight - 1) * sLat);
        int i = (int) ((tile.tileWidth - 1) * sLon);
        int k = j * tile.tileWidth + i;

        double eLeft = tile.elevations.getDouble(k);
        double eRight = i < (tile.tileWidth - 1) ? tile.elevations.getDouble(k + 1) : eLeft;

        if (tile.missingDataFlag == eLeft || tile.missingDataFlag == eRight)
            return this.missingDataFlag;

        double dw = sectorDeltaLon / (tile.tileWidth - 1);
        double dh = sectorDeltaLat / (tile.tileHeight - 1);
        double ssLon = (dLon - i * dw) / dw;
        double ssLat = (dLat - j * dh) / dh;

        double eTop = eLeft + ssLon * (eRight - eLeft);

        if (j < tile.tileHeight - 1 && i < tile.tileWidth - 1)
        {
            eLeft = tile.elevations.getDouble(k + tile.tileWidth);
            eRight = tile.elevations.getDouble(k + tile.tileWidth + 1);

            if (tile.missingDataFlag == eLeft || tile.missingDataFlag == eRight)
                return this.missingDataFlag;
        }

        double eBot = eLeft + ssLon * (eRight - eLeft);
        return eTop + ssLat * (eBot - eTop);
    }

    protected LocalTile findTile(final double latRadians, final double lonRadians)
    {
        for (LocalTile tile : this.tiles)
        {
            if (tile.sector.containsRadians(latRadians, lonRadians))
                return tile;
        }

        return null;
    }

    protected static class LocalTile
    {
        protected final Sector sector;
        protected final int tileWidth;
        protected final int tileHeight;
        protected final double minElevation;
        protected final double maxElevation;
        protected final double missingDataFlag;
        protected final BufferWrapper elevations;

        protected LocalTile(Sector sector, double missingDataFlag, int tileWidth, int tileHeight,
            BufferWrapper elevations)
        {
            this.sector = sector;
            this.tileWidth = tileWidth;
            this.tileHeight = tileHeight;
            this.missingDataFlag = missingDataFlag;
            this.elevations = elevations;

            int len = this.elevations.length();
            if (len != 0)
            {
                double min = Double.MAX_VALUE;
                double max = -min;

                for (int i = 0; i < len; i++)
                {
                    double v = this.elevations.getDouble(i);
                    if (v == this.missingDataFlag)
                        continue;

                    if (v < min)
                        min = v;
                    if (v > max)
                        max = v;
                }

                this.minElevation = min;
                this.maxElevation = max;
            }
            else
            {
                this.minElevation = 0;
                this.maxElevation = 0;
            }
        }
    }
}
