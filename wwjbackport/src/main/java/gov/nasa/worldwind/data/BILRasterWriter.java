/* Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.formats.worldfile.WorldFile;
import gov.nasa.worldwind.geom.Sector;

import java.io.File;
import java.io.IOException;

/**
 * @author dcollins
 * @version $Id: BILRasterWriter.java 13251 2010-04-02 19:17:46Z dcollins $
 */
public class BILRasterWriter extends AbstractDataRasterWriter
{
    private boolean writeGeoreferenceFiles;
    private static final String[] bilMimeTypes = new String[] {"image/bil"};
    private static final String[] bilSuffixes = new String[] {"bil"};

    public BILRasterWriter(boolean writeGeoreferenceFiles)
    {
        super(bilMimeTypes, bilSuffixes);

        this.writeGeoreferenceFiles = writeGeoreferenceFiles;
    }

    public BILRasterWriter()
    {
        this(true); // Enable writing georeference files by default.
    }

    public boolean isWriteGeoreferenceFiles()
    {
        return this.writeGeoreferenceFiles;
    }

    public void setWriteGeoreferenceFiles(boolean writeGeoreferenceFiles)
    {
        this.writeGeoreferenceFiles = writeGeoreferenceFiles;
    }

    protected boolean doCanWrite(DataRaster raster, String formatSuffix, File file)
    {
        return (raster != null) && (raster instanceof ByteBufferRaster);
    }

    protected void doWrite(DataRaster raster, String formatSuffix, File file) throws IOException
    {
        this.writeRaster(raster, file);

        if (this.isWriteGeoreferenceFiles())
        {
            AVList worldFileParams = new AVListImpl();
            this.initWorldFileParams(raster, worldFileParams);

            String message = this.validate(worldFileParams, raster);
            if (message != null)
            {
                Logging.logger().severe(message);
                throw new java.io.IOException(message);
            }

            java.io.File dir = file.getParentFile();
            String base = WWIO.replaceSuffix(file.getName(), "");

            this.writeWorldFile(worldFileParams, new java.io.File(dir, base + ".blw"));
            this.writeHdrFile(worldFileParams, new java.io.File(dir, base + ".hdr"));
        }
    }

    protected void writeRaster(DataRaster raster, java.io.File file) throws java.io.IOException
    {
        ByteBufferRaster byteBufferRaster = (ByteBufferRaster) raster;
        java.nio.ByteBuffer byteBuffer = byteBufferRaster.getByteBuffer();

        // Do not force changes to the underlying storage device.
        boolean forceFilesystemWrite = false;
        WWIO.saveBuffer(byteBuffer, file, forceFilesystemWrite);
    }

    protected void writeWorldFile(AVList values, java.io.File file) throws java.io.IOException
    {
        Sector sector = (Sector) values.getValue(AVKey.SECTOR);
        int[] size = (int[]) values.getValue(WorldFile.WORLD_FILE_IMAGE_SIZE);

        double xPixelSize = sector.getDeltaLonDegrees() / size[0];
        double yPixelSize = -sector.getDeltaLatDegrees() / size[1];
        double xCoeff = 0.0;
        double yCoeff = 0.0;
        double xLocation = sector.getMinLongitude().degrees;
        double yLocation = sector.getMaxLatitude().degrees;

        java.io.PrintWriter out = new java.io.PrintWriter(file);
        try
        {
            out.println(xPixelSize);
            out.println(xCoeff);
            //noinspection SuspiciousNameCombination
            out.println(yCoeff);
            //noinspection SuspiciousNameCombination
            out.println(yPixelSize);
            out.println(xLocation);
            //noinspection SuspiciousNameCombination
            out.println(yLocation);
        }
        finally
        {
            out.close();
        }
    }

    protected void writeHdrFile(AVList values, java.io.File file) throws java.io.IOException
    {
        int[] size = (int[]) values.getValue(WorldFile.WORLD_FILE_IMAGE_SIZE);
        Object byteOrder = values.getValue(AVKey.BYTE_ORDER);
        Object pixelType = values.getValue(AVKey.PIXEL_TYPE);

        int nBits = 0;
        if (AVKey.INT8.equals(pixelType))
            nBits = 8;
        else if (AVKey.INT16.equals(pixelType))
            nBits = 16;
        else if (AVKey.INT32.equals(pixelType) || AVKey.FLOAT32.equals(pixelType))
            nBits = 32;
        
        int rowBytes = size[0] * (nBits / 8);

        java.io.PrintWriter out = new java.io.PrintWriter(file);
        try
        {
            out.append("BYTEORDER      ").println(AVKey.BIG_ENDIAN.equals(byteOrder) ? "M" : "I");
            out.append("LAYOUT         ").println("BIL");
            out.append("NROWS          ").println(size[1]);
            out.append("NCOLS          ").println(size[0]);
            out.append("NBANDS         ").println(1);
            out.append("NBITS          ").println(nBits);
            out.append("BANDROWBYTES   ").println(rowBytes);
            out.append("TOTALROWBYTES  ").println(rowBytes);
            out.append("BANDGAPBYTES   ").println(0);

            // This code expects the string "gov.nasa.worldwind.avkey.MissingDataValue", which now corresponds to the
            // key MISSING_DATA_REPLACEMENT.
            Object o = values.getValue(AVKey.MISSING_DATA_REPLACEMENT);
            if (o != null)
                out.append("NODATA         ").println(o);
        }
        finally
        {
            out.close();
        }
    }

    protected void initWorldFileParams(DataRaster raster, AVList worldFileParams)
    {
        ByteBufferRaster byteBufferRaster = (ByteBufferRaster) raster;

        int[] size = new int[2];
        size[0] = raster.getWidth();
        size[1] = raster.getHeight();
        worldFileParams.setValue(WorldFile.WORLD_FILE_IMAGE_SIZE, size);

        Sector sector = raster.getSector();
        worldFileParams.setValue(AVKey.SECTOR, sector);

        worldFileParams.setValue(AVKey.BYTE_ORDER, getByteOrder(byteBufferRaster.getByteBuffer()));
        worldFileParams.setValue(AVKey.PIXEL_FORMAT, AVKey.ELEVATION);
        worldFileParams.setValue(AVKey.PIXEL_TYPE, getPixelType(byteBufferRaster.getBuffer()));

        double d = byteBufferRaster.getTransparentValue();
        if (d != Double.MAX_VALUE)
            worldFileParams.setValue(AVKey.MISSING_DATA_REPLACEMENT, d);
    }
    
    private static Object getPixelType(BufferWrapper buffer)
    {
        Object pixelType = null;
        if (buffer instanceof BufferWrapper.ByteBufferWrapper)
            pixelType = AVKey.INT8;
        else if (buffer instanceof BufferWrapper.ShortBufferWrapper)
            pixelType = AVKey.INT16;
        else if (buffer instanceof BufferWrapper.IntBufferWrapper)
            pixelType = AVKey.INT32;
        else if (buffer instanceof BufferWrapper.FloatBufferWrapper)
            pixelType = AVKey.FLOAT32;

        return pixelType;
    }

    private static Object getByteOrder(java.nio.ByteBuffer byteBuffer)
    {
        return java.nio.ByteOrder.LITTLE_ENDIAN.equals(byteBuffer.order()) ? AVKey.LITTLE_ENDIAN : AVKey.BIG_ENDIAN;
    }

    protected String validate(AVList worldFileParams, Object dataSource)
    {
        StringBuilder sb = new StringBuilder();

        Object o = worldFileParams.getValue(WorldFile.WORLD_FILE_IMAGE_SIZE);
        if (o == null || !(o instanceof int[]))
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("WorldFile.NoSizeSpecified", dataSource));

        o = worldFileParams.getValue(AVKey.SECTOR);
        if (o == null || !(o instanceof Sector))
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("WorldFile.NoSectorSpecified", dataSource));

        o = worldFileParams.getValue(AVKey.BYTE_ORDER);
        if (o == null || !(o instanceof String))
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("WorldFile.NoByteOrderSpecified", dataSource));

        o = worldFileParams.getValue(AVKey.PIXEL_FORMAT);
        if (o == null)
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("WorldFile.NoPixelFormatSpecified", dataSource));
        else if (!AVKey.ELEVATION.equals(o))
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("WorldFile.InvalidPixelFormat", dataSource));

        o = worldFileParams.getValue(AVKey.PIXEL_TYPE);
        if (o == null)
            sb.append(sb.length() > 0 ? ", " : "").append(Logging.getMessage("WorldFile.NoPixelTypeSpecified", dataSource));

        if (sb.length() == 0)
            return null;

        return sb.toString();
    }
}
