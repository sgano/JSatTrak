/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.wms;

import gov.nasa.worldwind.ogc.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.xml.*;
import gov.nasa.worldwind.wms.CapabilitiesRequest;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.net.*;
import java.util.*;

/**
 * @author tag
 * @version $Id$
 */
public class WMSCapabilities extends OGCCapabilities
{
    protected static final QName ROOT_ELEMENT_NAME_1_1_1 = new QName("WMT_MS_Capabilities");
    protected static final QName ROOT_ELEMENT_NAME_1_3_0 = new QName("WMS_Capabilities");

    public static WMSCapabilities retrieve(URI uri) throws Exception
    {
        try
        {
            CapabilitiesRequest request = new CapabilitiesRequest(uri);

            return new WMSCapabilities(request);
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Parses a WMS capabilities document.
     *
     * @param docSource the XML source. May be a filename, file, stream or other type allowed by {@link
     *                  gov.nasa.worldwind.util.WWXML#openEventReader(Object)}.
     *
     * @throws IllegalArgumentException if the document source is null.
     */
    public WMSCapabilities(Object docSource)
    {
        super(OGCConstants.WMS_NAMESPACE_URI, docSource);

        this.initialize();
    }

    public WMSCapabilities(CapabilitiesRequest docSource) throws URISyntaxException, MalformedURLException
    {
        super(OGCConstants.WMS_NAMESPACE_URI, docSource.getUri().toURL());

        this.initialize();
    }

    private void initialize()
    {
        this.getParserContext().registerParser(new QName(this.getDefaultNamespaceURI(), "Service"),
            new WMSServiceInformation(this.getNamespaceURI()));
        this.getParserContext().registerParser(new QName("Capability"),
            new WMSCapabilityInformation(this.getNamespaceURI()));
    }

    @Override
    public String getDefaultNamespaceURI()
    {
        return OGCConstants.WMS_NAMESPACE_URI;
    }

    public boolean isRootElementName(QName candidate)
    {
        return this.getParserContext().isSameName(candidate, ROOT_ELEMENT_NAME_1_1_1)
            || this.getParserContext().isSameName(candidate, ROOT_ELEMENT_NAME_1_3_0);
    }

    public XMLEventParser allocate(XMLEventParserContext ctx, XMLEvent event)
    {
        if (ctx.isStartElement(event, CAPABILITY))
            return ctx.allocate(event, new WMSCapabilityInformation(this.getNamespaceURI()));
        else
            return super.allocate(ctx, event);
    }

    @Override
    public WMSCapabilities parse(Object... args) throws XMLStreamException
    {
        return (WMSCapabilities) super.parse(args);
    }

    /**
     * Returns all named layers in the capabilities document.
     *
     * @return an unordered list of the document's named layers.
     */
    public List<WMSLayerCapabilities> getNamedLayers()
    {
        if (this.getCapabilityInformation() == null || this.getCapabilityInformation().getLayerCapabilities() == null)
            return null;

        List<WMSLayerCapabilities> namedLayers = new ArrayList<WMSLayerCapabilities>();

        for (WMSLayerCapabilities layer : this.getCapabilityInformation().getLayerCapabilities())
        {
            namedLayers.addAll(layer.getNamedLayers());
        }

        return namedLayers;
    }

    public WMSLayerCapabilities getLayerByName(String name)
    {
        if (WWUtil.isEmpty(name))
            return null;

        List<WMSLayerCapabilities> namedLayers = this.getNamedLayers();
        for (WMSLayerCapabilities layer : namedLayers)
        {
            if (layer.getName().equals(name))
                return layer;
        }

        return null;
    }

    public WMSCapabilityInformation getCapabilityInformation()
    {
        return (WMSCapabilityInformation) super.getCapabilityInformation();
    }

    public Set<String> getImageFormats()
    {
        Set<OGCRequestDescription> requestDescriptions = this.getCapabilityInformation().getRequestDescriptions();
        for (OGCRequestDescription rd : requestDescriptions)
        {
            if (rd.getRequestName().equals("GetMap"))
                return rd.getFormats();
        }

        return null;
    }

    public Long getLayerLatestLastUpdateTime(WMSCapabilities caps, String[] layerNames)
    {
        if (caps == null)
        {
            String message = Logging.getMessage("nullValue.WMSCapabilities");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (layerNames == null)
        {
            String message = Logging.getMessage("nullValue.WMSLayerNames");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String lastUpdate = null;

        for (String name : layerNames)
        {
            WMSLayerCapabilities layer = this.getLayerByName(name);
            if (layer == null)
                continue;

            String update = layer.getLastUpdate();
            if (update != null && update.length() > 0 && (lastUpdate == null || update.compareTo(lastUpdate) > 0))
                lastUpdate = update;
        }

        if (lastUpdate != null)
        {
            try
            {
                return Long.parseLong(lastUpdate);
            }
            catch (NumberFormatException e)
            {
                String message = Logging.getMessage("generic.ConversionError", lastUpdate);
                Logging.logger().warning(message);
            }
        }

        return null;
    }

    public Double[] getLayerExtremeElevations(WMSCapabilities caps, String[] layerNames)
    {
        if (caps == null)
        {
            String message = Logging.getMessage("nullValue.WMSCapabilities");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (layerNames == null)
        {
            String message = Logging.getMessage("nullValue.WMSLayerNames");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Double extremeMin = null;
        Double extremeMax = null;

        for (String name : layerNames)
        {
            WMSLayerCapabilities layer = caps.getLayerByName(name);
            if (layer == null)
                continue;

            Double min = layer.getExtremeElevationMin();
            if (min != null && (extremeMin == null || min.compareTo(min) > 0))
                extremeMin = min;

            Double max = layer.getExtremeElevationMax();
            if (max != null && (extremeMax == null || max.compareTo(max) > 0))
                extremeMax = max;
        }

        if (extremeMin != null || extremeMax != null)
        {
            Double[] extremes = new Double[] {null, null};

            if (extremeMin != null)
                extremes[0] = extremeMin;
            if (extremeMax != null)
                extremes[1] = extremeMax;

            return extremes;
        }

        return null;
    }

    public OGCRequestDescription getRequestDescription(String requestName)
    {
        for (OGCRequestDescription rd : this.getCapabilityInformation().getRequestDescriptions())
        {
            if (rd.getRequestName().equalsIgnoreCase(requestName))
                return rd;
        }

        return null;
    }

    public String getRequestURL(String requestName, String protocol, String requestMethod)
    {
        OGCRequestDescription rd = this.getRequestDescription(requestName);
        if (rd != null)
        {
            OGCOnlineResource ol = rd.getOnlineResouce(protocol, requestMethod);
            return ol != null ? ol.getHref() : null;
        }

        return null;
    }

    protected static final String CONNECTION_ID = "8e85d104-7b76-420a-b8cf-1e966966e1c9";
    protected static final String WMS_URL = "https://services.digitalglobe.com/mapservice/wmsaccess";

    public static void main(String[] args)
    {
        try
        {
            CapabilitiesRequest request = new CapabilitiesRequest(new URI(WMS_URL));
            request.setParam("connectid", CONNECTION_ID);
            WMSCapabilities capsC = new WMSCapabilities(request);
            capsC.parse();
            System.out.print(capsC.toString());

            System.out.println("\n================================================================");

            WMSCapabilities capsA = new WMSCapabilities("file:///Users/tag/Desktop/DigitalGlobe/wmsaccess.xml");
            capsA.parse();
            System.out.print(capsA.toString());

            System.out.println("\n================================================================");

            WMSCapabilities capsB = new WMSCapabilities("file:///Users/tag/Desktop/DigitalGlobe/wmsaccessNS.xml");
            capsB.parse();
            System.out.print(capsB.toString());

            System.out.println(capsA.toString().equals(capsB.toString()) ? "MATCH" : "DO NOT MATCH");
        }
        catch (XMLStreamException e)
        {
            e.printStackTrace();
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
    }
}
