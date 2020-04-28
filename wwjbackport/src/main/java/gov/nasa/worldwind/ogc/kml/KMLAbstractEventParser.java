/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.ogc.kml.io.KMLDoc;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.xml.*;

import java.io.*;

/**
 * Implements {@link AbstractXMLEventParser} for KML elements. Provides the interface and implementation for retrieving
 * support files referred to by elements in the KML document.
 *
 * @author tag
 * @version $Id: KMLAbstractEventParser.java 13396 2010-05-25 21:02:14Z tgaskins $
 */
public abstract class KMLAbstractEventParser extends AbstractXMLEventParser
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    protected KMLAbstractEventParser(String namespaceURI)
    {
        super(namespaceURI);
    }

    /**
     * Returns an input stream for a support file referred to by this object's KML document and located relative to the
     * KML file.
     *
     * @param referenceName the support file name, exactly as specified in the reference.
     *
     * @return an input stream to the requested support file, or null if the support file cannot be found or if the
     *         reference name is an absolute path.
     *
     * @throws IllegalArgumentException if the reference name is null.
     * @throws IOException              if an error occurs while trying to find the file or open a stream to it.
     */
    public InputStream getStreamForReference(String referenceName) throws IOException
    {
        if (referenceName == null)
        {
            String message = Logging.getMessage("nullValue.FileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        XMLEventParser root = this.getRoot();

        if (!(root instanceof KMLRoot))
            return null;

        KMLDoc kmlDoc = ((KMLRoot) root).getKMLDoc();
        if (kmlDoc == null)
            return null;

        return kmlDoc.getSupportFileStream(referenceName);
    }
}
