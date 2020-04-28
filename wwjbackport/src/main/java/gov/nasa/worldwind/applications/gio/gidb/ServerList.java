/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.gidb;

import java.util.Collection;

/**
 * @author dcollins
 * @version $Id: ServerList.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public interface ServerList extends Iterable<Server>
{
    int getServerCount();

    int getIndex(Server server);

    Server getServer(int index);

    void setServer(int index, Server server);

    void addServer(int index, Server server);

    void addServer(Server server);

    void addServers(Collection<? extends Server> c);

    void removeServer(int index);

    void clearServers();
}
