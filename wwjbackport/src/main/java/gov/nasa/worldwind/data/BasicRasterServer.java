/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.data;

import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.formats.dds.DDSCompressor;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.*;
import org.w3c.dom.*;

import javax.xml.xpath.*;
import java.awt.*;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * @author Lado Garakanidze
 * @version $Id: BasicRasterServer.java 14056 2010-10-27 09:11:24Z garakl $
 */

public class BasicRasterServer extends WWObjectImpl implements RasterServer
{
    protected final String XPATH_RASTER_SERVER = "/RasterServer";
    protected final String XPATH_RASTER_SERVER_PROPERTY = XPATH_RASTER_SERVER + "/Property";
    protected final String XPATH_RASTER_SERVER_SOURCE = XPATH_RASTER_SERVER + "/Sources/Source";
    protected final String XPATH_RASTER_SERVER_SOURCE_SECTOR = XPATH_RASTER_SERVER_SOURCE + "/Sector";

    protected final String RASTER_READER = "DataRasterReader";

    protected static DataRasterReader[] readers = new DataRasterReader[]
        {
            new GDALDataRasterReader(),
            new ImageIORasterReader(),
            new GeotiffRasterReader(),
            new RPFRasterReader(),
            new BILRasterReader()
        };

    protected ArrayList<AVList> rasters = new ArrayList<AVList>();
    protected DataRasterReader rasterReader = null;

    public BasicRasterServer(Object o, AVList params)
    {
        super();

        if (null != params)
            this.setValues(params);

        //TODO garakl get a defult DataRasterReader from a Configuration
//        this.rasterReader = WorldWind.getDataRasterReader();
//        this.rasterReader = new GDALDataRasterReader();

        this.init(o);
    }

    protected void init(Object o)
    {
        if (null == o)
        {
            String message = Logging.getMessage("nullValue.ObjectIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Element rootElement = null;

        if (o instanceof Element)
            rootElement = (Element) o;
        else if (o instanceof Document)
            rootElement = ((Document) o).getDocumentElement();
        else
        {
            Document doc = WWXML.openDocument(o);
            if (null != doc)
                rootElement = doc.getDocumentElement();
        }

        if (null == rootElement)
        {
            String message = Logging.getMessage("generic.UnexpectedObjectType", o.getClass().getName());
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String rootElementName = rootElement.getNodeName();
        if (!"RasterServer".equals(rootElementName))
        {
            String message = Logging.getMessage("generic.InvalidDataSource", rootElementName);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        XPath xpath = WWXML.makeXPath();

        this.extractProperties(rootElement, xpath);

        this.buildRasterFilesList(rootElement, xpath);
    }

    public void setDataRasterReaders(DataRasterReader[] rasterReaders)
    {
        readers = rasterReaders;
    }

    public DataRasterReader[] getDataRasterReaders()
    {
        return readers;
    }

    protected void extractProperties(Element domElement, XPath xpath)
    {
        Element[] props = WWXML.getElements(domElement, XPATH_RASTER_SERVER_PROPERTY, xpath);

        if (props != null && props.length > 0)
        {
            for (Element prop : props)
            {
                String key = prop.getAttribute("name");
                String value = prop.getAttribute("value");
                if (!WWUtil.isEmpty(key) && !WWUtil.isEmpty(value))
                {
                    if (!this.hasKey(key))
                        this.setValue(key, value);
                    else
                    {
                        Object oldValue = this.getValue(key);
                        if (value.equals(oldValue))
                            continue;

                        String msg = Logging.getMessage("generic.AttemptToChangeExistingProperty", key, oldValue,
                            value);
                        Logging.logger().fine(msg);
                    }
                }
            }
        }
    }

    protected void buildRasterFilesList(Element domElement, XPath xpath)
    {
        long startTime = System.currentTimeMillis();

        int numSources = 0;
        Sector extent = null;

        try
        {
            XPathExpression xpExpression = xpath.compile(XPATH_RASTER_SERVER_SOURCE);
            NodeList nodes = (NodeList) xpExpression.evaluate(domElement, XPathConstants.NODESET);

            if (nodes == null || nodes.getLength() == 0)
                return;

            numSources = nodes.getLength();
            for (int i = 0; i < numSources; i++)
            {
                try
                {
                    Node node = nodes.item(i);
                    node.getParentNode().removeChild(node);
                    if (node.getNodeType() != Node.ELEMENT_NODE)
                        continue;

                    Element source = (Element) node;

                    String path = source.getAttribute("path");
                    if (WWUtil.isEmpty(path))
                        continue;

                    File rasterFile = new File(path);

                    AVList metadata = new AVListImpl();
                    metadata.setValue(AVKey.FILE, rasterFile);

                    DataRasterReader reader = this.findReaderFor(rasterFile, metadata);
                    if (null == reader)
                    {
//                        String message = Logging.getMessage("nullValue.ReaderIsNull") + " : " + rasterFile.getAbsolutePath();
//                        Logging.logger().severe(message);
                        continue;
                    }

                    metadata.setValue(AVKey.RASTER_READER, reader);

                    Sector sector = WWXML.getSector(source, "Sector", xpath);
                    if (null == sector)
                    {
                        reader.readMetadata(rasterFile, metadata);
                        Object o = metadata.getValue(AVKey.SECTOR);
                        if (o != null && o instanceof Sector)
                            sector = (Sector) o;
                    }
                    else
                        metadata.setValue(AVKey.SECTOR, sector);

                    if (null != sector)
                    {
                        extent = Sector.union(extent, sector);
                        this.rasters.add(metadata);
                    }
                }
                catch (Throwable t)
                {
                    Logging.logger().log(java.util.logging.Level.SEVERE, t.getMessage(), t);
                }
            }

            if (null != extent && extent.getDeltaLatDegrees() > 0d && extent.getDeltaLonDegrees() > 0d)
                this.setValue(AVKey.SECTOR, extent);
        }
        catch (Throwable t)
        {
            Logging.logger().log(java.util.logging.Level.SEVERE, t.getMessage(), t);
        }
        finally
        {
            Logging.logger().info(this.getStringValue(AVKey.DISPLAY_NAME) + ": " + numSources
                + " files in " + (System.currentTimeMillis() - startTime) + " milli-seconds");
        }
    }

    public DataRasterReader findReaderFor(Object source, AVList params)
    {
        if (source == null)
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (readers == null)
        {
            String message = Logging.getMessage("nullValue.ReaderIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (null != readers && readers.length > 0)
        {
            for (DataRasterReader reader : readers)
            {
                if (reader != null)
                {
                    if (reader.canRead(source, params))
                        return reader;
                }
            }
        }
        return null;
    }

    public Sector getSector()
    {
        return (this.hasKey(AVKey.SECTOR)) ? (Sector) this.getValue(AVKey.SECTOR) : null;
    }

    protected DataRaster composeRaster(AVList reqParams)
    {
        DataRaster reqRaster = null;

        long startTime = System.currentTimeMillis();

        if (null == reqParams)
        {
            String message = Logging.getMessage("nullValue.ParamsIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!reqParams.hasKey(AVKey.WIDTH))
        {
            String message = Logging.getMessage("generic.MissingRequiredParameter", AVKey.WIDTH);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (!reqParams.hasKey(AVKey.HEIGHT))
        {
            String message = Logging.getMessage("generic.MissingRequiredParameter", AVKey.HEIGHT);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Object o = reqParams.getValue(AVKey.SECTOR);
        if (null == o || !(o instanceof Sector))
        {
            String message = Logging.getMessage("generic.MissingRequiredParameter", AVKey.SECTOR);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Sector reqSector = (Sector) o;
        Sector rasterExtent = this.getSector();
        if (!reqSector.intersects(rasterExtent))
        {
            String message = Logging.getMessage("generic.SectorRequestedOutsideCoverageArea", reqSector, rasterExtent);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        try
        {
            int reqWidth = (Integer) reqParams.getValue(AVKey.WIDTH);
            int reqHeight = (Integer) reqParams.getValue(AVKey.HEIGHT);

            if (!reqParams.hasKey(AVKey.BYTE_ORDER))
                reqParams.setValue(AVKey.BYTE_ORDER, AVKey.BIG_ENDIAN);

            if (AVKey.ELEVATION.equalsIgnoreCase(this.getStringValue(AVKey.PIXEL_FORMAT)))
            {
                reqParams.setValue(AVKey.PIXEL_FORMAT, AVKey.ELEVATION);

                if (!reqParams.hasKey(AVKey.DATA_TYPE))
                    reqParams.setValue(AVKey.DATA_TYPE, AVKey.INT16);

                reqRaster = new ByteBufferRaster(reqWidth, reqHeight, reqSector, reqParams);
            }
            else
            {
                reqParams.setValue(AVKey.PIXEL_FORMAT, AVKey.IMAGE);
                reqRaster = new BufferedImageRaster(reqWidth, reqHeight, Transparency.TRANSLUCENT, reqSector);
            }

            if (null == this.rasters)
            {
                // TODO garakl what if no raster created because not suitable reader found??

                String message = Logging.getMessage("nullValue.RasterIsNull");
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }

            for (AVList raster : this.rasters)
            {
                try
                {
                    Sector rasterSector = (Sector) raster.getValue(AVKey.SECTOR);
                    Sector overlap = reqSector.intersection(rasterSector);
                    // SKIP, if not intersection, or intersects only on edges
                    if (null == overlap || overlap.getDeltaLatDegrees() == 0d || overlap.getDeltaLonDegrees() == 0d)
                        continue;

                    DataRasterReader reader = null;
                    // let's retrieve a DataRasterReader for this particular raster
                    if (raster.hasKey(AVKey.RASTER_READER))
                        reader = (DataRasterReader) raster.getValue(AVKey.RASTER_READER);

                    AVList rasterParameters = raster.copy(); // prevent raster parameters' change
                    if (null == reader)
                        reader = this.findReaderFor(raster.getValue(AVKey.FILE), rasterParameters);

                    if (null == reader)
                    {
                        String msg = Logging.getMessage("nullValue.ReaderIsNull");
                        Logging.logger().severe(msg);
                        continue;
                    }

                    DataRaster[] srcRasters = reader.read(raster.getValue(AVKey.FILE), raster.copy());

                    if (null != srcRasters && srcRasters.length > 0)
                    {
                        DataRaster srcRaster = srcRasters[0];
                        srcRaster.drawOnCanvas(reqRaster, overlap);
                    }
                }
                catch (Throwable t)
                {
                    Logging.logger().log(java.util.logging.Level.SEVERE, t.getMessage(), t);
                }
            }
        }
        catch (WWRuntimeException wwe)
        {
            throw wwe;
        }
        catch (Throwable t)
        {
            Logging.logger().log(java.util.logging.Level.SEVERE, t.getMessage(), t);
        }
        finally
        {
            String msg = Logging.getMessage("generic.ExecutionTime", System.currentTimeMillis() - startTime);
            Logging.logger().finest(msg);
        }

        return reqRaster;
    }

    public ByteBuffer getRasterAsByteBuffer(AVList params)
    {
        // request may contain a specific file format, different from a default file format
        String format = (null != params && params.hasKey(AVKey.IMAGE_FORMAT))
            ? params.getStringValue(AVKey.IMAGE_FORMAT) : this.getStringValue(AVKey.IMAGE_FORMAT);
        if (WWUtil.isEmpty(format))
        {
            String message = Logging.getMessage("generic.MissingRequiredParameter", AVKey.IMAGE_FORMAT);
            Logging.logger().severe(message);
            throw new WWRuntimeException(message);
        }

        DataRaster raster = this.composeRaster(params);
        if (null != raster)
        {
            try
            {
                if (raster instanceof BufferedImageRaster)
                {
                    if ("image/png".equalsIgnoreCase(format))
                    {
                        return ImageUtil.asPNG(raster);
                    }
                    else if ("image/jpeg".equalsIgnoreCase(format) || "image/jpg".equalsIgnoreCase(format))
                    {
                        return ImageUtil.asJPEG(raster);
                    }
                    if ("image/dds".equalsIgnoreCase(format))
                    {
                        return DDSCompressor.compressImage(((BufferedImageRaster) raster).getBufferedImage());
                    }
                    else
                    {
                        String msg = Logging.getMessage("generic.UnknownFileFormat", format);
                        Logging.logger().severe(msg);
                        throw new WWRuntimeException(msg);
                    }
                }
                else if (raster instanceof ByteBufferRaster)
                {
                    // Elevations as BIL16 or as BIL32 are stored in the simple ByteBuffer object
                    return ((ByteBufferRaster) raster).getByteBuffer();
                }
                else
                {
                    String msg = Logging.getMessage("generic.UnexpectedRasterType", raster.getClass().getName());
                    Logging.logger().severe(msg);
                    throw new WWRuntimeException(msg);
                }
            }
            catch (WWRuntimeException wwe)
            {
                throw wwe;
            }
            catch (Throwable t)
            {
                Logging.logger().log(java.util.logging.Level.SEVERE, t.getMessage(), t);
            }
        }

        return null;
    }
}

