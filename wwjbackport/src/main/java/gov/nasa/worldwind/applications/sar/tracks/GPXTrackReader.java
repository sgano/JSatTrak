/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.sar.tracks;

import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.formats.gpx.GpxReader;
import gov.nasa.worldwind.tracks.Track;
import gov.nasa.worldwind.util.Logging;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

/**
 * @author dcollins
 * @version $Id: GPXTrackReader.java 11482 2009-06-07 20:03:47Z dcollins $
 */
public class GPXTrackReader extends AbstractTrackReader
{
    public GPXTrackReader()
    {
    }

    public String getDescription()
    {
        return "GPS Exchange Format (*.xml, *.gpx)";
    }

    protected Track[] doRead(InputStream inputStream) throws IOException
    {
        try
        {
            GpxReader reader = new GpxReader();
            reader.readStream(inputStream);
            return this.asArray(reader.getTracks());
        }
        catch (ParserConfigurationException e)
        {
            String message = Logging.getMessage("XML.ParserConfigurationException");
            Logging.logger().finest(message);
            throw new WWRuntimeException(e);
        }
        catch (SAXException e)
        {
            String message = Logging.getMessage("generic.ExceptionAttemptingToParseXml", inputStream);
            Logging.logger().severe(message);
            throw new WWRuntimeException(e);
        }
    }

    protected boolean acceptFilePath(String filePath)
    {
        String lowerCasePath = filePath.toLowerCase();
        return lowerCasePath.endsWith(".xml") || lowerCasePath.endsWith(".gpx");
    }
}
