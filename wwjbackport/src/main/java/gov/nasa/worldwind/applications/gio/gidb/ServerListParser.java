/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.gidb;

import gov.nasa.worldwind.applications.gio.xml.ElementParser;
import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;


/**
 * @author dcollins
 * @version $Id: ServerListParser.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class ServerListParser extends ElementParser implements ServerList
{
    private List<Server> serverListImpl;
    public static final String ELEMENT_NAME = "serverList";

    public ServerListParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);
        this.serverListImpl = new ArrayList<Server>();        
    }

    protected void doStartElement(String name, org.xml.sax.Attributes attributes) throws Exception
    {
        if (ServerParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            ServerParser parser = new ServerParser(name, attributes);
            this.serverListImpl.add(parser);
            setCurrentElement(parser);
        }
    }

    public int getServerCount()
    {
        return this.serverListImpl.size();
    }

    public int getIndex(Server server)
    {
        return this.serverListImpl.indexOf(server);
    }

    public Server getServer(int index)
    {
        if (index < 0 || index >= this.serverListImpl.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.serverListImpl.get(index);
    }

    public void setServer(int index, Server server)
    {
        if (index < 0 || index >= this.serverListImpl.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.serverListImpl.set(index, server);
    }

    public void addServer(int index, Server server)
    {
        if (index < 0 || index > this.serverListImpl.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.serverListImpl.add(index, server);
    }

    public void addServer(Server server)
    {
        this.serverListImpl.add(server);
    }

    public void addServers(Collection<? extends Server> c)
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullServer.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.serverListImpl.addAll(c);
    }

    public void removeServer(int index)
    {
        if (index < 0 || index >= this.serverListImpl.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.serverListImpl.remove(index);
    }

    public void clearServers()
    {
        this.serverListImpl.clear();
    }

    public Iterator<Server> iterator()
    {
        return this.serverListImpl.iterator();
    }
}
