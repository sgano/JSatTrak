/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.Exportable;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.ogc.kml.impl.KMLExportUtil;
import gov.nasa.worldwind.util.*;
import static gov.nasa.worldwind.ogc.kml.impl.KMLExportUtil.kmlBoolean;

import javax.xml.stream.*;
import java.awt.*;
import java.io.*;

/**
 * @author dcollins
 * @version $Id: BasicShapeAttributes.java 13811 2010-09-17 01:25:08Z pabercrombie $
 */
public class BasicShapeAttributes implements ShapeAttributes
{
    protected long lastModifiedTime;
    protected boolean drawInterior;
    protected boolean drawOutline;
    protected boolean enableAntialiasing;
    protected boolean enableLighting;
    protected Material interiorMaterial;
    protected Material outlineMaterial;
    protected double interiorOpacity;
    protected double outlineOpacity;
    protected double outlineWidth;
    private int outlineStippleFactor;
    private short outlineStipplePattern;
    private Object interiorImageSource;
    private double interiorImageScale;
    protected boolean unresolved;

    public BasicShapeAttributes()
    {
        this.drawInterior = true;
        this.drawOutline = true;
        this.enableAntialiasing = true;
        this.enableLighting = false;
        this.interiorMaterial = Material.WHITE;
        this.outlineMaterial = Material.BLACK;
        this.interiorOpacity = 1;
        this.outlineOpacity = 1;
        this.outlineWidth = 1;
        this.outlineStippleFactor = 0;
        this.outlineStipplePattern = (short) 0xF0F0;
        this.interiorImageSource = null;
        this.interiorImageScale = 1;
        this.updateModifiedTime();
    }

    public BasicShapeAttributes(ShapeAttributes attributes)
    {
        if (attributes == null)
        {
            String message = Logging.getMessage("nullValue.AttributesIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.drawInterior = attributes.isDrawInterior();
        this.drawOutline = attributes.isDrawOutline();
        this.enableAntialiasing = attributes.isEnableAntialiasing();
        this.enableLighting = attributes.isEnableLighting();
        this.interiorMaterial = attributes.getInteriorMaterial();
        this.outlineMaterial = attributes.getOutlineMaterial();
        this.interiorOpacity = attributes.getInteriorOpacity();
        this.outlineOpacity = attributes.getOutlineOpacity();
        this.outlineWidth = attributes.getOutlineWidth();
        this.outlineStippleFactor = attributes.getOutlineStippleFactor();
        this.outlineStipplePattern = attributes.getOutlineStipplePattern();
        this.interiorImageSource = attributes.getInteriorImageSource();
        this.interiorImageScale = attributes.getInteriorImageScale();
        this.updateModifiedTime();
    }

    /** {@inheritDoc} */
    public ShapeAttributes copy()
    {
        return new BasicShapeAttributes(this);
    }

    /** {@inheritDoc} */
    public void copy(ShapeAttributes attributes)
    {
        if (attributes != null)
        {
            this.drawInterior = attributes.isDrawInterior();
            this.drawOutline = attributes.isDrawOutline();
            this.enableAntialiasing = attributes.isEnableAntialiasing();
            this.enableLighting = attributes.isEnableLighting();
            this.interiorMaterial = attributes.getInteriorMaterial();
            this.outlineMaterial = attributes.getOutlineMaterial();
            this.interiorOpacity = attributes.getInteriorOpacity();
            this.outlineOpacity = attributes.getOutlineOpacity();
            this.outlineWidth = attributes.getOutlineWidth();
            this.outlineStippleFactor = attributes.getOutlineStippleFactor();
            this.outlineStipplePattern = attributes.getOutlineStipplePattern();
            this.interiorImageSource = attributes.getInteriorImageSource();
            this.interiorImageScale = attributes.getInteriorImageScale();
            this.updateModifiedTime();
        }
    }

    public boolean isUnresolved()
    {
        return unresolved;
    }

    public void setUnresolved(boolean unresolved)
    {
        this.unresolved = unresolved;
    }

    public long getLastModifiedTime()
    {
        return this.lastModifiedTime;
    }

    public boolean isDrawInterior()
    {
        return this.drawInterior;
    }

    public void setDrawInterior(boolean draw)
    {
        this.drawInterior = draw;
        this.updateModifiedTime();
    }

    public boolean isDrawOutline()
    {
        return this.drawOutline;
    }

    public void setDrawOutline(boolean draw)
    {
        this.drawOutline = draw;
        this.updateModifiedTime();
    }

    public boolean isEnableLighting()
    {
        return enableLighting;
    }

    public void setEnableLighting(boolean enableLighting)
    {
        this.enableLighting = enableLighting;
    }

    public boolean isEnableAntialiasing()
    {
        return this.enableAntialiasing;
    }

    public void setEnableAntialiasing(boolean enable)
    {
        this.enableAntialiasing = enable;
        this.updateModifiedTime();
    }

    public Material getInteriorMaterial()
    {
        return this.interiorMaterial;
    }

    public void setInteriorMaterial(Material material)
    {
        if (material == null)
        {
            String message = Logging.getMessage("nullValue.MaterialIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.interiorMaterial = material;
        this.updateModifiedTime();
    }

    public Material getOutlineMaterial()
    {
        return this.outlineMaterial;
    }

    public void setOutlineMaterial(Material material)
    {
        if (material == null)
        {
            String message = Logging.getMessage("nullValue.MaterialIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.outlineMaterial = material;
        this.updateModifiedTime();
    }

    public double getInteriorOpacity()
    {
        return this.interiorOpacity;
    }

    public void setInteriorOpacity(double opacity)
    {
        if (opacity < 0 || opacity > 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "opacity < 0 or opacity > 1");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.interiorOpacity = opacity;
        this.updateModifiedTime();
    }

    public double getOutlineOpacity()
    {
        return this.outlineOpacity;
    }

    public void setOutlineOpacity(double opacity)
    {
        if (opacity < 0 || opacity > 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "opacity < 0 or opacity > 1");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.outlineOpacity = opacity;
        this.updateModifiedTime();
    }

    public double getOutlineWidth()
    {
        return this.outlineWidth;
    }

    public void setOutlineWidth(double width)
    {
        if (width < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "width < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.outlineWidth = width;
        this.updateModifiedTime();
    }

    public int getOutlineStippleFactor()
    {
        return this.outlineStippleFactor;
    }

    public void setOutlineStippleFactor(int factor)
    {
        if (factor < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "factor < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.outlineStippleFactor = factor;
        this.updateModifiedTime();
    }

    public short getOutlineStipplePattern()
    {
        return this.outlineStipplePattern;
    }

    public void setOutlineStipplePattern(short pattern)
    {
        this.outlineStipplePattern = pattern;
        this.updateModifiedTime();
    }

    public Object getInteriorImageSource()
    {
        return this.interiorImageSource;
    }

    public void setInteriorImageSource(Object imageSource)
    {
        // Can be null
        this.interiorImageSource = imageSource;
        this.updateModifiedTime();
    }

    public double getInteriorImageScale()
    {
        return this.interiorImageScale;
    }

    public void setInteriorImageScale(double scale)
    {
        if (scale <= 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "scale <= 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.interiorImageScale = scale;
        this.updateModifiedTime();
    }

    public void getRestorableState(RestorableSupport rs, RestorableSupport.StateObject so)
    {
        rs.addStateValueAsBoolean(so, "drawInterior", this.isDrawInterior());

        rs.addStateValueAsBoolean(so, "drawOutline", this.isDrawOutline());

        rs.addStateValueAsBoolean(so, "enableAntialiasing", this.isEnableAntialiasing());

        this.getInteriorMaterial().getRestorableState(rs, rs.addStateObject(so, "interiorMaterial"));

        this.getOutlineMaterial().getRestorableState(rs, rs.addStateObject(so, "outlineMaterial"));

        rs.addStateValueAsDouble(so, "interiorOpacity", this.getInteriorOpacity());

        rs.addStateValueAsDouble(so, "outlineOpacity", this.getOutlineOpacity());

        rs.addStateValueAsDouble(so, "outlineWidth", this.getOutlineWidth());

        rs.addStateValueAsInteger(so, "outlineStippleFactor", this.getOutlineStippleFactor());

        rs.addStateValueAsInteger(so, "outlineStipplePattern", this.getOutlineStipplePattern());

        if (this.getInteriorImageSource() != null && this.getInteriorImageSource() instanceof String)
            rs.addStateValueAsString(so, "interiorImagePath", (String) this.getInteriorImageSource());

        rs.addStateValueAsDouble(so, "interiorImageScale", this.getInteriorImageScale());
    }

    public void restoreState(RestorableSupport rs, RestorableSupport.StateObject so)
    {
        Boolean b = rs.getStateValueAsBoolean(so, "drawInterior");
        if (b != null)
            this.setDrawInterior(b);

        b = rs.getStateValueAsBoolean(so, "drawOutline");
        if (b != null)
            this.setDrawOutline(b);

        b = rs.getStateValueAsBoolean(so, "enableAntialiasing");
        if (b != null)
            this.setEnableAntialiasing(b);

        RestorableSupport.StateObject mo = rs.getStateObject(so, "interiorMaterial");
        if (mo != null)
            this.setInteriorMaterial(this.getInteriorMaterial().restoreState(rs, mo));

        mo = rs.getStateObject(so, "outlineMaterial");
        if (mo != null)
            this.setOutlineMaterial(this.getOutlineMaterial().restoreState(rs, mo));

        Double d = rs.getStateValueAsDouble(so, "interiorOpacity");
        if (d != null)
            this.setInteriorOpacity(d);

        d = rs.getStateValueAsDouble(so, "outlineOpacity");
        if (d != null)
            this.setOutlineOpacity(d);

        d = rs.getStateValueAsDouble(so, "outlineWidth");
        if (d != null)
            this.setOutlineWidth(d);

        Integer i = rs.getStateValueAsInteger(so, "outlineStippleFactor");
        if (i != null)
            this.setOutlineStippleFactor(i);

        i = rs.getStateValueAsInteger(so, "outlineStipplePattern");
        if (i != null)
            this.setOutlineStipplePattern(i.shortValue());

        String s = rs.getStateValueAsString(so, "interiorImagePath");
        if (s != null)
            this.setInteriorImageSource(s);

        d = rs.getStateValueAsDouble(so, "interiorImageScale");
        if (d != null)
            this.setInteriorImageScale(d);
    }

    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        BasicShapeAttributes that = (BasicShapeAttributes) o;

        if (drawOutline != that.drawOutline)
            return false;
        if (drawInterior != that.drawInterior)
            return false;
        if (enableAntialiasing != that.enableAntialiasing)
            return false;
        if (Double.compare(that.outlineOpacity, outlineOpacity) != 0)
            return false;
        if (Double.compare(that.interiorOpacity, interiorOpacity) != 0)
            return false;
        if (Double.compare(that.outlineWidth, outlineWidth) != 0)
            return false;
        if (that.outlineStippleFactor != outlineStippleFactor)
            return false;
        if (that.outlineStipplePattern != outlineStipplePattern)
            return false;
        if (interiorMaterial != null ? !interiorMaterial.equals(that.interiorMaterial) : that.interiorMaterial != null)
            return false;
        if (outlineMaterial != null ? !outlineMaterial.equals(that.outlineMaterial) : that.outlineMaterial != null)
            return false;
        if (interiorImageSource != null ?
            !interiorImageSource.equals(that.interiorImageSource) : that.interiorImageSource != null)
            return false;
        //noinspection RedundantIfStatement
        if (Double.compare(that.interiorImageScale, interiorImageScale) != 0)
            return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        long temp;
        result = (drawInterior ? 1 : 0);
        result = 31 * result + (drawOutline ? 1 : 0);
        result = 31 * result + (enableAntialiasing ? 1 : 0);
        result = 31 * result + (interiorMaterial != null ? interiorMaterial.hashCode() : 0);
        result = 31 * result + (outlineMaterial != null ? outlineMaterial.hashCode() : 0);
        temp = interiorOpacity != +0.0d ? Double.doubleToLongBits(interiorOpacity) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = outlineOpacity != +0.0d ? Double.doubleToLongBits(outlineOpacity) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = outlineWidth != +0.0d ? Double.doubleToLongBits(outlineWidth) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = outlineStippleFactor;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = outlineStipplePattern;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (interiorImageSource != null ? interiorImageSource.hashCode() : 0);
        temp = Double.doubleToLongBits(interiorImageScale);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    protected void updateModifiedTime()
    {
        this.lastModifiedTime = System.currentTimeMillis();
    }

    /** {@inheritDoc} */
    public String isExportFormatSupported(String mimeType)
    {
        if (KMLConstants.KML_MIME_TYPE.equalsIgnoreCase(mimeType))
            return Exportable.FORMAT_SUPPORTED;
        else
            return Exportable.FORMAT_NOT_SUPPORTED;
    }

    public void export(String mimeType, Object output) throws IOException, UnsupportedOperationException
    {
        if (mimeType == null)
        {
            String message = Logging.getMessage("nullValue.Format");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (output == null)
        {
            String message = Logging.getMessage("nullValue.OutputBufferIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (KMLConstants.KML_MIME_TYPE.equalsIgnoreCase(mimeType))
        {
            try
            {
                exportAsKML(output);
            }
            catch (XMLStreamException e)
            {
                Logging.logger().throwing(getClass().getName(), "export", e);
                throw new IOException(e);
            }
        }
        else
        {
            String message = Logging.getMessage("Export.UnsupportedFormat", mimeType);
            Logging.logger().warning(message);
            throw new UnsupportedOperationException(message);
        }
    }

    /**
     * Export the placemark attributes to KML as a {@code <Style>} element. The {@code output} object will receive the
     * data. This object must be one of:
     * java.io.Writer<br/>
     * java.io.OutputStream<br/>
     * javax.xml.stream.XMLStreamWriter
     *
     * @param output Object to receive the generated KML.
     * @throws XMLStreamException If an exception occurs while writing the KML
     * @see #export(String, Object)
     */
    protected void exportAsKML(Object output) throws XMLStreamException
    {
        XMLStreamWriter xmlWriter = null;
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        boolean closeWriterWhenFinished = true;

        if (output instanceof XMLStreamWriter)
        {
            xmlWriter = (XMLStreamWriter)output;
            closeWriterWhenFinished = false;
        }
        else if (output instanceof Writer)
        {
            xmlWriter = factory.createXMLStreamWriter((Writer)output);
        }
        else if (output instanceof OutputStream)
        {
            xmlWriter = factory.createXMLStreamWriter((OutputStream)output);
        }

        if (xmlWriter == null)
        {
            String message = Logging.getMessage("Export.UnsupportedOutputObject");
            Logging.logger().warning(message);
            throw new IllegalArgumentException(message);
        }

        xmlWriter.writeStartElement("Style");

        // Line style
        xmlWriter.writeStartElement("LineStyle");

        final Color lineColor = this.getOutlineMaterial().getDiffuse();
        if (lineColor != null)
        {
            xmlWriter.writeStartElement("color");
            xmlWriter.writeCharacters(KMLExportUtil.stripHexPrefix(WWUtil.encodeColorABGR(lineColor)));
            xmlWriter.writeEndElement();

            xmlWriter.writeStartElement("colorMode");
            xmlWriter.writeCharacters("normal");
            xmlWriter.writeEndElement();
        }

        final Double lineWidth = this.getOutlineWidth();
        if (lineWidth != null)
        {
            xmlWriter.writeStartElement("width");
            xmlWriter.writeCharacters(Double.toString(lineWidth));
            xmlWriter.writeEndElement();
        }
        
        xmlWriter.writeEndElement(); // LineStyle

        // Poly style
        xmlWriter.writeStartElement("PolyStyle");

        final Color fillColor = this.getInteriorMaterial().getDiffuse();
        if (fillColor != null)
        {
            xmlWriter.writeStartElement("color");
            xmlWriter.writeCharacters(KMLExportUtil.stripHexPrefix(WWUtil.encodeColorABGR(fillColor)));
            xmlWriter.writeEndElement();

            xmlWriter.writeStartElement("colorMode");
            xmlWriter.writeCharacters("normal");
            xmlWriter.writeEndElement();
        }

        xmlWriter.writeStartElement("fill");
        xmlWriter.writeCharacters(kmlBoolean(isDrawInterior()));
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement("outline");
        xmlWriter.writeCharacters(kmlBoolean(isDrawOutline()));
        xmlWriter.writeEndElement();

        xmlWriter.writeEndElement(); // PolyStyle
        xmlWriter.writeEndElement(); // Style

        xmlWriter.flush();
        if (closeWriterWhenFinished)
            xmlWriter.close();
    }
}
