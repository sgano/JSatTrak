/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.exception.*;
import gov.nasa.worldwind.ogc.kml.impl.*;
import gov.nasa.worldwind.ogc.kml.io.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;
import gov.nasa.worldwind.util.xml.*;
import gov.nasa.worldwind.geom.Position;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 * Parses a KML or KMZ document and provides access to its contents. Instructions for parsing KML/KMZ files and streams
 * are given in the Description section of {@link gov.nasa.worldwind.ogc.kml}.
 *
 * @author tag
 * @version $Id: KMLRoot.java 14165 2010-12-02 19:24:18Z pabercrombie $
 */
public class KMLRoot extends KMLAbstractObject implements KMLRenderable
{
    /** Reference to the KMLDoc representing the KML or KMZ file. */
    protected KMLDoc kmlDoc;
    /** The event reader used to parse the document's XML. */
    protected XMLEventReader eventReader;
    /** The parser context for the document. */
    protected KMLParserContext parserContext;

    /**
     * Creates a KML root for an untyped source. The source must be either a {@link File}, a {@link URL}, a {@link
     * InputStream}, or a {@link String} identifying either a file path or a URL. For all types other than
     * <code>InputStream</code> an attempt is made to determine whether the source is KML or KMZ; KML is assumed if the
     * test is not definitive. Null is returned if the source type is not recognized.
     *
     * @param docSource either a {@link File}, a {@link URL}, or an {@link InputStream}, or a {@link String} identifying
     *                  a file path or URL.
     *
     * @return a new {@link KMLRoot} for the specified source, or null if the source type is not supported.
     *
     * @throws IllegalArgumentException if the source is null.
     * @throws IOException              if an error occurs while reading the source.
     */
    public static KMLRoot create(Object docSource) throws IOException
    {
        if (docSource == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (docSource instanceof File)
            return new KMLRoot((File) docSource);
        else if (docSource instanceof URL)
            return new KMLRoot((URL) docSource, null);
        else if (docSource instanceof InputStream)
            return new KMLRoot((InputStream) docSource, null);
        else if (docSource instanceof String)
        {
            File file = new File((String) docSource);
            if (file.exists())
                return new KMLRoot(file);

            URL url = WWIO.makeURL(docSource);
            if (url != null)
                return new KMLRoot(url, null);
        }

        return null;
    }

    /**
     * Create a new <code>KMLRoot</code> for a {@link KMLDoc} instance. A KMLDoc represents KML and KMZ files from
     * either files or input streams.
     *
     * @param docSource the KMLDoc instance representing the KML document.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws IOException              if an error occurs while reading the KML document.
     */
    public KMLRoot(KMLDoc docSource) throws IOException
    {
        super(KMLConstants.KML_NAMESPACE);

        if (docSource == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.kmlDoc = docSource;

        this.initialize();
    }

    /**
     * Create a new <code>KMLRoot</code> for a {@link File}.
     *
     * @param docSource the File containing the document.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws IOException              if an error occurs while reading the KML document.
     */
    public KMLRoot(File docSource) throws IOException
    {
        super(KMLConstants.KML_NAMESPACE);

        if (docSource == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (WWIO.isContentType(docSource, KMLConstants.KML_MIME_TYPE))
            this.kmlDoc = new KMLFile(docSource);
        else if (WWIO.isContentType(docSource, KMLConstants.KMZ_MIME_TYPE))
            this.kmlDoc = new KMZFile(docSource);
        else
            throw new WWUnrecognizedException(Logging.getMessage("KML.UnrecognizedKMLFileType"));

        this.initialize();
    }

    /**
     * Create a new <code>KMLRoot</code> for an {@link InputStream}.
     *
     * @param docSource   the input stream containing the document.
     * @param contentType the content type of the stream data. Specify {@link KMLConstants#KML_MIME_TYPE} for plain KML
     *                    and {@link KMLConstants#KMZ_MIME_TYPE} for KMZ. The content is treated as KML for any other
     *                    value or a value of null.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws IOException              if an error occurs while reading the KML document.
     */
    public KMLRoot(InputStream docSource, String contentType) throws IOException
    {
        super(KMLConstants.KML_NAMESPACE);

        if (docSource == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (contentType != null && contentType.equals(KMLConstants.KMZ_MIME_TYPE))
            this.kmlDoc = new KMZInputStream(docSource);
        else if (contentType == null && docSource instanceof ZipInputStream)
            this.kmlDoc = new KMZInputStream(docSource);
        else
            this.kmlDoc = new KMLInputStream(docSource);

        this.initialize();
    }

    /**
     * Create a <code>KMLRoot</code> for a {@link URL}.
     *
     * @param docSource   the URL identifying the document.
     * @param contentType the content type of the data. Specify {@link KMLConstants#KML_MIME_TYPE} for plain KML and
     *                    {@link KMLConstants#KMZ_MIME_TYPE} for KMZ. Any other non-null value causes the content to be
     *                    treated as plain KML. If null is specified the content type is read from the server or other
     *                    end point of the URL. When a content type is specified, the content type returned by the URL's
     *                    end point is ignored. You can therefore force the content to be treated as KML or KMZ
     *                    regardless of what a server declares it to be.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws IOException              if an error occurs while reading the document.
     */
    public KMLRoot(URL docSource, String contentType) throws IOException
    {
        super(KMLConstants.KML_NAMESPACE);

        if (docSource == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        URLConnection conn = docSource.openConnection();
        if (contentType == null)
            contentType = conn.getContentType();

        if (contentType != null && contentType.equals(KMLConstants.KMZ_MIME_TYPE))
            this.kmlDoc = new KMZInputStream(conn.getInputStream());
        else
            this.kmlDoc = new KMLInputStream(conn.getInputStream());

        this.initialize();
    }

    /**
     * Create a new <code>KMLRoot</code> with a specific namespace. (The default namespace is defined by {@link
     * gov.nasa.worldwind.ogc.kml.KMLConstants#KML_NAMESPACE}).
     *
     * @param namespaceURI the default namespace URI.
     * @param docSource    the KML source specified via a {@link KMLDoc} instance. A KMLDoc represents KML and KMZ files
     *                     from either files or input streams.
     *
     * @throws IllegalArgumentException if the document source is null.
     * @throws java.io.IOException      if an I/O error occurs attempting to open the document source.
     */
    public KMLRoot(String namespaceURI, KMLDoc docSource) throws IOException
    {
        super(namespaceURI);

        if (docSource == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.kmlDoc = docSource;
        this.initialize();
    }

    /**
     * Called just before the constructor returns. If overriding this method be sure to invoke
     * <code>super.initialize()</code>.
     *
     * @throws java.io.IOException if an I/O error occurs attempting to open the document source.
     */
    protected void initialize() throws IOException
    {
        this.eventReader = this.createReader(this.getKMLDoc().getKMLStream());
        if (this.eventReader == null)
            throw new WWRuntimeException(Logging.getMessage("XML.UnableToOpenDocument", this.getKMLDoc()));

        this.parserContext = this.createParserContext(this.eventReader);
    }

    /**
     * Creates the event reader. Called from the constructor.
     *
     * @param docSource the document source to create a reader for. The type can be any of those supported by {@link
     *                  WWXML#openEventReader(Object)}.
     *
     * @return a new event reader, or null if the source type cannot be determined.
     */
    protected XMLEventReader createReader(Object docSource)
    {
        return WWXML.openEventReader(docSource);
    }

    /**
     * Invoked during {@link #initialize()} to create the parser context.
     *
     * @param reader the reader to associate with the parser context.
     *
     * @return a new parser context.
     */
    protected KMLParserContext createParserContext(XMLEventReader reader)
    {
        return this.parserContext = new KMLParserContext(reader, this.getNamespaceURI());
    }

    /**
     * Specifies the object to receive notifications of important occurrences during parsing, such as exceptions and the
     * occurrence of unrecognized element types.
     * <p/>
     * The default notification listener writes a message to the log, and otherwise does nothing.
     *
     * @param listener the listener to receive notifications. Specify null to indicate no listener.
     *
     * @see gov.nasa.worldwind.util.xml.XMLParserNotification
     */
    public void setNotificationListener(final XMLParserNotificationListener listener)
    {
        if (listener == null)
        {
            this.parserContext.setNotificationListener(null);
        }
        else
        {
            this.parserContext.setNotificationListener(new XMLParserNotificationListener()
            {
                public void notify(XMLParserNotification notification)
                {
                    // Set up so the user sees the notification coming from the root rather than the parser
                    notification.setSource(KMLRoot.this);
                    listener.notify(notification);
                }
            });
        }
    }

    /**
     * Returns the KML document for this <code>KMLRoot</code>.
     *
     * @return the KML document for this root.
     */
    public KMLDoc getKMLDoc()
    {
        return this.kmlDoc;
    }

    /**
     * Finds a named element in the document.
     *
     * @param id the element's identifer. If null, null is returned.
     *
     * @return the element requested, or null if there is no corresponding element in the document.
     */
    public Object getItemByID(String id)
    {
        return id != null ? this.getParserContext().getIdTable().get(id) : null;
    }

    /**
     * Resolves a reference to a remote or local element of the form address#identifier, where "address" identifies a
     * local or remote document, including the current document, and and "identifier" is the id of the desired element.
     * If the address part identifies the current document, the document is searched for the specified identifier.
     * Otherwise the document is retrieved, opened and searched for the identifier. If the address refers to a remote
     * document and the document has not previously been retrieved and cached locally, retrieval is initiated and this
     * method returns null. Once the document is successfully retrieved, subsequent calls to this method return the
     * identified element, if it exists.
     *
     * @param link the document address in the form address#identifier.
     *
     * @return the requested element, or null if the docuement or the element is not found.
     *
     * @throws IllegalArgumentException if the link is null.
     */
    public Object resolveReference(String link)
    {
        if (link == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            String[] linkParts = link.split("#");
            String linkBase = linkParts[0];
            String linkRef = linkParts.length > 1 ? linkParts[1] : null;

            // See if it's a reference to an internal element.
            if (WWUtil.isEmpty(linkBase) && !WWUtil.isEmpty(linkRef))
                return this.getItemByID(linkRef);

            // See if it's an already found and parsed KML file.
            Object o = WorldWind.getSessionCache().get(linkBase);
            if (o != null && o instanceof KMLRoot)
                return ((KMLRoot) o).getItemByID(linkRef);

            // See if it's part of a KMZ file.
            o = this.resolveLocalReference(linkBase, linkRef);
            if (o != null)
                return o;

            // Treat it as a remote reference.
            return this.resolveRemoteReference(linkBase, linkRef);
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.UnableToResolveReference", link);
            Logging.logger().warning(message);
        }

        return null;
    }

    /**
     * Resolves a reference to a local element identified by address and identifier, where "address" identifies a local
     * document, including the current document, and and "identifier" is the id of the desired element.
     *
     * @param linkBase the address of the document containing the requested element.
     * @param linkRef  the element's identifier.
     *
     * @return the requested element, or null if the element is not found.
     *
     * @throws IllegalArgumentException if the address is null.
     */
    public Object resolveLocalReference(String linkBase, String linkRef)
    {
        if (linkBase == null)
        {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            String path = this.getKMLDoc().getSupportFilePath(linkBase);
            if (path != null)
            {
                File file = new File(path);
                if (WWIO.isContentType(file, KMLConstants.KML_MIME_TYPE)
                    || WWIO.isContentType(file, KMLConstants.KMZ_MIME_TYPE))
                {
                    KMLRoot refRoot = new KMLRoot(file);
                    refRoot.parse();
                    WorldWind.getSessionCache().put(linkBase, refRoot);
                    return refRoot.getItemByID(linkRef);
                }
            }
            return path;
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.UnableToResolveReference", linkBase + "/" + linkRef);
            Logging.logger().warning(message);
            return null;
        }
    }

    public Object resolveRemoteReference(String linkBase, String linkRef)
    {
        try
        {
            URL url = WorldWind.getDataFileStore().requestFile(linkBase);
            if (url == null)
                return null;

            String contentType = WorldWind.getDataFileStore().getContentType(linkBase);
            if (contentType == null)
                contentType = WWIO.makeMimeTypeForSuffix(WWIO.getSuffix(linkBase));

            if (contentType != null)
            {
                if (contentType.equals(KMLConstants.KML_MIME_TYPE) || contentType.equals(KMLConstants.KMZ_MIME_TYPE))
                {
                    KMLRoot refRoot = KMLRoot.create(url);
                    refRoot.parse();
                    WorldWind.getSessionCache().put(linkBase, refRoot);
                    return refRoot.getItemByID(linkRef);
                }
            }

            return url;
        }
        catch (Exception e)
        {
            String message = Logging.getMessage("generic.UnableToResolveReference", linkBase + "/" + linkRef);
            Logging.logger().warning(message);
            return null;
        }
    }

    /**
     * Starts document parsing. This method initiates parsing of the KML document and returns when the full document has
     * been parsed.
     *
     * @param args optional arguments to pass to parsers of sub-elements.
     *
     * @return <code>this</code> if parsing is successful, otherwise  null.
     *
     * @throws javax.xml.stream.XMLStreamException
     *          if an exception occurs while attempting to read the event stream.
     */
    public KMLRoot parse(Object... args) throws XMLStreamException
    {
        KMLParserContext ctx = this.parserContext;

        // Create a list of the possible root elements, one for each of the KML namespaces
        List<QName> rootElements = new ArrayList<QName>(KMLConstants.KML_NAMESPACES.length);
        for (String namespace : KMLConstants.KML_NAMESPACES)
        {
            rootElements.add(new QName(namespace, "kml"));
        }

        for (XMLEvent event = ctx.nextEvent(); ctx.hasNext(); event = ctx.nextEvent())
        {
            if (event == null)
                continue;

            // Check the element against each of the possible KML root elements 
            for (QName kmlRoot : rootElements)
            {
                if (ctx.isStartElement(event, kmlRoot))
                {
                    super.parse(ctx, event, args);
                    return this;
                }
            }
        }

        return null;
    }

    protected XMLEventParserContext getParserContext()
    {
        return this.parserContext;
    }

    /**
     * Returns the <code>hint</code> attribute of the <code>KML</code> element (the document root).
     *
     * @return the hint attribute, or null if the attribute is not specified.
     */
    public String getHint()
    {
        return (String) this.getField("hint");
    }

    /**
     * Returns the {@link gov.nasa.worldwind.ogc.kml.KMLNetworkLinkControl} element if the document root contains it.
     *
     * @return the element if it is specified in the document, otherwise null.
     */
    public KMLNetworkLinkControl getNetworkLinkControl()
    {
        return (KMLNetworkLinkControl) this.getField("NetworkLinkControl");
    }

    /**
     * Returns the KML <code>Feature</code> element contained in the document root.
     *
     * @return the feature element if it is specified in the document, otherwise null.
     */
    public KMLAbstractFeature getFeature()
    {
        if (!this.hasFields())
            return null;

        for (Map.Entry<String, Object> entry : this.getFields().getEntries())
        {
            if (entry.getValue() instanceof KMLAbstractFeature)
                return (KMLAbstractFeature) entry.getValue();
        }

        return null;
    }

    public void preRender(KMLTraversalContext tc, DrawContext dc)
    {
        if (this.getFeature() != null)
            this.getFeature().preRender(tc, dc);
    }

    public void render(KMLTraversalContext tc, DrawContext dc)
    {
        if (this.getFeature() != null)
            this.getFeature().render(tc, dc);
    }

    /**
     * Create a balloon for a feature.
     *
     * @param feature               Feature to create balloon for.
     * @param style                 The feature's balloon style.
     * @param balloonAttachmentMode The balloon mode, either {@link Balloon#GLOBE_MODE} or {@link Balloon#SCREEN_MODE}.
     *
     * @return Balloon for feature.
     */
    public Balloon createBalloon(KMLAbstractFeature feature, KMLBalloonStyle style, String balloonAttachmentMode)
    {
        String text = style.getText();
        if (text == null)
            text = "";

        Balloon balloon;
        if (Balloon.GLOBE_MODE.equals(balloonAttachmentMode))
            balloon = new KMLBalloonImpl(feature, text, Position.ZERO); // 0 is dummy position
        else
            balloon = new KMLBalloonImpl(feature, text, new Point(0, 0)); // 0,0 is dummy position

        balloon.setVisible(false);
        balloon.setAlwaysOnTop(true);
        return balloon;
    }
}
