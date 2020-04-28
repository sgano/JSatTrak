/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.services;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.applications.gos.*;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.retrieve.URLRetriever;
import gov.nasa.worldwind.util.*;
import org.w3c.dom.*;

import javax.xml.xpath.XPath;
import java.io.InputStream;
import java.net.*;

/**
 * @author dcollins
 * @version $Id: FGDCServiceStatus.java 13557 2010-07-17 20:56:32Z dcollins $
 */
public class FGDCServiceStatus implements ServiceStatus
{
    protected Document doc;
    protected XPath xpath;

    public static ServiceStatus retrieve(URI uri) throws Exception
    {
        // TODO: draw defaults form configuration
        return retrieve(uri, 20000, 20000);
    }

    public static ServiceStatus retrieve(URI uri, Integer connectTimeout, Integer readTimeout)
        throws Exception
    {
        if (uri == null)
        {
            String message = Logging.getMessage("nullValue.URIIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        InputStream is = null;
        try
        {
            URLRetriever retriever = URLRetriever.createRetriever(uri.toURL(),  null);

            if (retriever == null)
            {
                String message = Logging.getMessage("generic.UnrecognizedProtocol", uri.getScheme());
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }

            if (connectTimeout != null)
                retriever.setConnectTimeout(connectTimeout);

            if (readTimeout != null)
                retriever.setReadTimeout(readTimeout);

            retriever.call();

            if (!retriever.getState().equals(URLRetriever.RETRIEVER_STATE_SUCCESSFUL))
            {
                String message = Logging.getMessage("generic.RetrievalFailed", uri.toString());
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }

            if (retriever.getBuffer() == null || retriever.getBuffer().limit() == 0)
            {
                String message = Logging.getMessage("generic.RetrievalReturnedNoContent", uri.toString());
                Logging.logger().severe(message);
                throw new WWRuntimeException(message);
            }

            is = WWIO.getInputStreamFromByteBuffer(retriever.getBuffer());

            return FGDCServiceStatus.parse(WWXML.openDocumentStream(is, true));
        }
        catch (URISyntaxException e)
        {
            String message = Logging.getMessage("generic.URIInvalid", uri.toString());
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            throw e;
        }
        finally
        {
            WWIO.closeStream(is, uri.toString());
        }
    }

    public static ServiceStatus parse(Document doc)
    {
        XPath xpath = WWXML.makeXPath();
        xpath.setNamespaceContext(new FGDCServiceStatusNamespaceContext());
        return new FGDCServiceStatus(doc, xpath);
    }

    public static OnlineResource createServiceStatusMetadataResource(String uuid)
    {
        if (WWUtil.isEmpty(uuid))
        {
            String message = Logging.getMessage("nullValue.IdIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String baseURI = Configuration.getStringValue(GeodataKey.SERVICE_STATUS_METADATA_URI);
        if (WWUtil.isEmpty(baseURI))
            return null;

        URL url;
        try
        {
            Request request = new Request(new URI(baseURI));
            request.setParam("cID", "1");
            request.setParam("uId", uuid);
            url = request.getUri().toURL();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("gosApp.ServiceStatusMetadataURIInvalid", baseURI);
            Logging.logger().severe(message);
            return null;
        }

        return new OnlineResource(GeodataKey.SERVICE_STATUS_METADATA, "Service Status", url);
    }

    public static OnlineResource createServiceStatusResource(String uuid, String serviceType)
    {
        if (WWUtil.isEmpty(uuid))
        {
            String message = Logging.getMessage("nullValue.IdIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String baseURI = Configuration.getStringValue(GeodataKey.SERVICE_STATUS_URI);
        if (WWUtil.isEmpty(baseURI))
            return null;

        URL url;
        try
        {
            Request request = new Request(new URI(baseURI));
            request.setParam("type", serviceType);
            request.setParam("id", uuid);
            request.setParam("requesttype", "brief");
            request.setParam("formattype", "xml");
            url = request.getUri().toURL();
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("gosApp.ServiceStatusURIInvalid", baseURI);
            Logging.logger().severe(message);
            return null;
        }

        return new OnlineResource(GeodataKey.SERVICE_STATUS, "Service Status", url);
    }

    protected FGDCServiceStatus(Document doc, XPath xpath)
    {
        this.doc = doc;
        this.xpath = xpath;
    }

    public Document getDocument()
    {
        return this.doc;
    }

    public double getScore()
    {
        Element[] tests = this.getScoredTests();
        if (tests == null || tests.length == 0)
            return SCORE_UNKNOWN;

        Double score = WWXML.getDouble(tests[0], "./rest:performance[@type=\"currentScore\"]", this.xpath);
        return (score != null) ? score : SCORE_UNKNOWN;
    }

    public double getSpeed()
    {
        Element[] tests = this.getScoredTests();
        if (tests == null || tests.length == 0)
            return SPEED_UNKNOWN;

        Double speed = WWXML.getDouble(tests[0], "./rest:performance[@type=\"currentScore\"]", this.xpath);
        return (speed != null) ? speed : SPEED_UNKNOWN;
    }

    public OnlineResource getScoreImageResource()
    {
        double score = this.getScore();
        Element config = this.getScoreConfigElement(score);
        if (config == null)
            return null;

        String displayText = WWXML.getText(config, "@displayName", this.xpath);
        String resourceName = WWXML.getText(config, "@imageResource", this.xpath);
        Object source = ResourceUtil.getResourceReference(resourceName);

        return new OnlineResource(GeodataKey.IMAGE, displayText, (URL) source);
    }

    protected Element[] getScoredTests()
    {
        return WWXML.getElements(this.doc.getDocumentElement(),
            "/rest:response/rest:service/rest:summary/rest:scoredTest", this.xpath);
    }
    
    protected Element getScoreConfigElement(double score)
    {
        Element el = Configuration.getElement("//ServiceStatusList");
        if (el == null)
            return null;

        Element[] els = WWXML.getElements(el, "./ServiceStatus", this.xpath);
        if (els == null || els.length < 1)
            return null;

        for (Element scoredTest : els)
        {
            Double minScore = WWXML.getDouble(scoredTest, "@minScore", this.xpath);
            Double maxScore = WWXML.getDouble(scoredTest, "@maxScore", this.xpath);
            if (minScore == null || maxScore == null)
                continue;

            if (score == SCORE_UNKNOWN && (score == minScore || score == maxScore))
                return scoredTest;

            if (score >= minScore && score <= maxScore)
                return scoredTest;
        }

        return null;
    }
}
