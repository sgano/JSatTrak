/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.examples.kml;

import gov.nasa.worldwind.ogc.kml.*;

import java.io.*;
import java.net.URL;

/**
 * Shows how to open and parse KML and KMZ files, URLs and input streams.
 *
 * @author tag
 * @version $Id: ParseKML.java 13410 2010-06-04 04:06:05Z tgaskins $
 */
public class ParseKML
{
    /** Open and parse a KML file from a file. */
    public static void parseKMLFile()
    {
        try
        {
            // Construct a KMLRoot and call its parse method.
            KMLRoot root = new KMLRoot(new File("demodata/KML/GoogleTutorialExample01.kml"));
            root.parse();

            // Obtain its Feature element if it has one.
            KMLAbstractFeature feature = root.getFeature();

            // Obtain its NetworkLinkControl if it has one.
            KMLNetworkLinkControl nlc = root.getNetworkLinkControl();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /** Open and parse a KML document from a URL. */
    public static void parseKMLURL()
    {
        try
        {
            File file = new File("demodata/KML/GoogleTutorialExample01.kml");
            boolean b = file.exists();
            URL url = new URL("file:///" + file.getAbsolutePath().replace(" ", "%20"));

            // Construct a KMLRoot and call its parse method. Let the constructor determine whether KML or KMZ.
            KMLRoot root = new KMLRoot(url, null);
            root.parse();
            // See parseKMLFile above for what to do next.
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /** Open and parse a KML document from an {@link InputStream}. */
    public static void parseKMLStream()
    {
        try
        {
            File file = new File("demodata/KML/GoogleTutorialExample01.kml");
            InputStream is = new FileInputStream(file);

            // Construct a KMLRoot and call its parse method. Must specify mime type of data in stream.
            KMLRoot root = new KMLRoot(is, KMLConstants.KML_MIME_TYPE);
            root.parse();
            // See parseKMLFile above for what to do next.
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /** Open and parse a KMZ file from a file. */
    public static void parseKMZFile()
    {
        try
        {
            // Construct a KMLRoot and call its parse method.
            KMLRoot root = new KMLRoot(new File("demodata/KML/kmztest01.kmz"));
            root.parse();
            // See parseKMLFile above for what to do next.
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /** Open and parse a KMZ document from a URL. */
    public static void parseKMZURL()
    {
        try
        {
            File file = new File("demodata/KML/kmztest01.kmz");
            URL url = new URL("file:///" + file.getAbsolutePath().replace(" ", "%20"));

            // Construct a KMLRoot and call its parse method. Force interpretation as KMZ.
            KMLRoot root = new KMLRoot(url, KMLConstants.KMZ_MIME_TYPE);
            root.parse();
            // See parseKMLFile above for what to do next.
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /** Open and parse a KMZ document from an {@link InputStream}. */
    public static void parseKMZStream()
    {
        try
        {
            File file = new File("demodata/KML/kmztest01.kmz");
            InputStream is = new FileInputStream(file);

            // Construct a KMLRoot and call its parse method. Must specify mime type of data in stream.
            KMLRoot root = new KMLRoot(is, KMLConstants.KMZ_MIME_TYPE);
            root.parse();
            // See parseKMLFile above for what to do next.
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        parseKMLFile();
        parseKMLURL();
        parseKMLStream();
        parseKMZFile();
        parseKMZURL();
        parseKMZStream();
    }
}
