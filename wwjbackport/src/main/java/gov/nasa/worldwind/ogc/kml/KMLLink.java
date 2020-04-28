/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.util.*;

import java.net.*;
import java.util.Locale;

/**
 * Represents the KML <i>Link</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLLink.java 13835 2010-09-20 18:48:46Z pabercrombie $
 */
// TODO: append viewFormat parameters to query string 
public class KMLLink extends KMLAbstractObject
{
    /** Href with query parameters appended. Generated once and cached. */
    protected String hrefWithQuery;

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLLink(String namespaceURI)
    {
        super(namespaceURI);
    }

    public String getHref()
    {
        return (String) this.getField("href");
    }

    public String getRefreshMode()
    {
        return (String) this.getField("refreshMode");
    }

    protected Double getRefreshInterval()
    {
        return (Double) this.getField("refreshInterval");
    }

    public String getViewRefreshMode()
    {
        return (String) this.getField("viewRefreshMode");
    }

    public Double getViewRefreshTime()
    {
        return (Double) this.getField("viewRefreshTime");
    }

    public Double getViewBoundScale()
    {
        return (Double) this.getField("viewBoundScale");
    }

    public String getViewFormat()
    {
        return (String) this.getField("viewFormat");
    }

    public String getHttpQuery()
    {
        return (String) this.getField("httpQuery");
    }

    /**
     * Get the href string with query parameters appended. If the {@code href} is not a URL, query parameters are
     * ignored and {@code href} is returned unmodified. Query parameters are read from the {@code httpQuery} field.
     * Query parameters are URL encoded by this method.
     * <p/>
     * The [clientName] and [clientVersion] query parameters can be set in the configuration file using the keys {@link
     * AVKey#NAME} and {@link AVKey#VERSION}.
     *
     * @return Href string with query parameters.
     *
     * @see #getHref()
     * @see #getHttpQuery()
     * @see gov.nasa.worldwind.Configuration
     */
    public String getHrefWithQuery()
    {
        if (this.hrefWithQuery == null)
            this.buildHrefWithQuery();

        return this.hrefWithQuery;
    }

    /** Build the full query string and store it to {@link #hrefWithQuery}. */
    protected void buildHrefWithQuery()
    {
        String href = this.getHref();

        if (href == null)
        {
            this.hrefWithQuery = null;
        }

        String queryString = this.getHttpQuery();
        if (WWUtil.isEmpty(queryString))
        {
            this.hrefWithQuery = href;
        }

        try
        {
            String clientName = Configuration.getStringValue(AVKey.NAME, Version.getVersionName());
            String clientVersion = Configuration.getStringValue(AVKey.VERSION, Version.getVersionNumber());

            // TODO: append viewFormat parameters

            URL url = new URL(href);
            queryString = queryString.replaceAll("\\[clientVersion\\]", clientVersion)
                .replaceAll("\\[kmlVersion\\]", KMLConstants.KML_VERSION)
                .replaceAll("\\[clientName\\]", clientName)
                .replaceAll("\\[language\\]", Locale.getDefault().getLanguage());

            URI newUri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath(),
                queryString, url.getRef());

            hrefWithQuery = newUri.toString();
        }
        catch (URISyntaxException e)
        {
            this.hrefWithQuery = href; // If the href failed to parse as a URI assume that it is a path to a local file
        }
        catch (MalformedURLException e)
        {
            this.hrefWithQuery = href; // If the href failed to parse as a URI assume that it is a path to a local file
        }
    }
}
