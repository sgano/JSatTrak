/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.gidb;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.applications.gio.catalogui.CatalogKey;
import gov.nasa.worldwind.applications.gio.catalogui.ResultList;
import gov.nasa.worldwind.applications.gio.catalogui.ResultModel;
import gov.nasa.worldwind.applications.gio.csw.SAXResponseParser;
import gov.nasa.worldwind.applications.gio.csw.StringResponseParser;
import gov.nasa.worldwind.applications.gio.xml.ElementParser;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dcollins
 * @version $Id: GetServices.java 6812 2008-09-24 20:25:26Z dcollins $
 */
public class GetServices
{
    private AVList queryParams;
    private ResultList outResultList;
    private List<ResultModel> serverList;

    public GetServices(AVList queryParams, ResultList resultList)
    {
        if (queryParams == null)
        {
            String message = "nullValue.QueryParamsIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (resultList == null)
        {
            String message = "nullValue.ResultListIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.queryParams = queryParams;
        this.outResultList = resultList;
    }

    public void executeRequest(URL serviceURL) throws Exception
    {
        if (serviceURL == null)
        {
            String message = "nullValue.ServiceURLIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        doExecuteRequest(serviceURL);
    }

    protected void doExecuteRequest(URL serviceURL) throws Exception
    {
        URL serverListURL = fetchServerList(serviceURL);
        parseServerList(serverListURL);

        this.outResultList.clear();        
        if (this.serverList != null)
        {
            this.outResultList.addAll(this.serverList);
            this.serverList = null;
        }
    }

    protected URL fetchServerList(URL serviceURL) throws Exception
    {
        URL cacheURL = null;
        String cachePath = cachePathFor(serviceURL);
        if (cachePath != null)
        {
            cacheURL = WorldWind.getDataFileStore().findFile(cachePath, false);
            if (cacheURL == null)
            {
                StringResponseParser stringParser = new StringResponseParser();
                stringParser.parseResponse(serviceURL.openStream());
                String serverListXML = stringParser.getString();

                File file = WorldWind.getDataFileStore().newFile(cachePath);
                FileWriter writer = new FileWriter(file);
                writer.write(serverListXML);
                writer.close();

                cacheURL = WorldWind.getDataFileStore().findFile(cachePath, false);
            }
        }
        return cacheURL;
    }

    protected void parseServerList(URL serviceURL) throws Exception
    {
        InputStream is = serviceURL.openStream();
        try
        {
            ResponseParser response = new ResponseParser(this);
            response.parseResponse(is);
        }
        finally
        {
            if (is != null)
                is.close();
        }
    }

    protected void addServer(Server server)
    {
        if (server == null)
        {
            String message = "nullValue.ServerIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (isServerAccepted(server))
        {
            GIDBResultModel resultModel = new GIDBResultModel();
            resultModel.setServer(server);
            makeResultParams(resultModel);

            if (this.serverList == null)
                this.serverList = new ArrayList<ResultModel>();
            this.serverList.add(resultModel);
        }
    }

    protected void makeResultParams(GIDBResultModel resultModel)
    {
        if (resultModel == null)
            return;

        Server server = resultModel.getServer();
        if (server == null)
            return;

        PortalUtils.makeServerParams(server, resultModel);

        // Note that this is a WMS service.
        resultModel.setValue(CatalogKey.SERVICE_TYPE, CatalogKey.WMS);
        // Provide a non-null value for UI elements looking for this action.
        resultModel.setValue(CatalogKey.ACTION_COMMAND_BROWSE, resultModel);
    }

    protected boolean isServerAccepted(Server server)
    {
        if (server == null)
            return false;

        String keywordText = queryParams.getStringValue(CatalogKey.KEYWORD_TEXT);
        if (keywordText != null)
        {
            String[] keywords = keywordText.split("[ ,]");
            if (keywords != null && keywords.length > 0)
            {
                if (server.getTitle() != null && server.getTitle().getValue() != null)
                {
                    String title = server.getTitle().getValue().toLowerCase();
                    for (String s : keywords)
                        if (!title.contains(s.toLowerCase()))
                            return false;
                }
            }
        }

        return true;
    }

    protected String cachePathFor(URL url)
    {
        String s = url.toExternalForm();
        return s != null ? WWIO.formPath(s) : null;
    }

    protected static class ResponseParser extends SAXResponseParser
    {
        private GetServices getServices;

        public ResponseParser(GetServices getServices)
        {
            this.getServices = getServices;
        }

        protected void doStartDocument(String name, org.xml.sax.Attributes attributes)
        {
            if (ServerListParser.ELEMENT_NAME.equalsIgnoreCase(name))
            {
                ServerListParser parser = new ServerListParser(name, attributes, this.getServices);
                setDocumentElement(parser);
            }
        }
    }

    protected static class ServerListParser extends ElementParser
    {
        private GetServices getServices;
        private Server currentServer = null;
        public static final String ELEMENT_NAME = "serverList";

        public ServerListParser(String name, org.xml.sax.Attributes attributes, GetServices getServices)
        {
            super(name, attributes);
            this.getServices = getServices;
        }

        protected void doStartElement(String name, org.xml.sax.Attributes attributes)
        {
            if (ServerParser.ELEMENT_NAME.equalsIgnoreCase(name))
            {
                ServerParser parser = new ServerParser(name, attributes);
                this.currentServer = parser;
                setCurrentElement(parser);
            }
        }

        protected void doEndElement(String name)
        {
            if (ServerParser.ELEMENT_NAME.equalsIgnoreCase(name))
            {
                if (this.currentServer != null)
                {
                    addServer(this.currentServer);
                    this.currentServer = null;
                }
            }
        }

        public void addServer(Server server)
        {
            this.getServices.addServer(server);
        }
    }
}
