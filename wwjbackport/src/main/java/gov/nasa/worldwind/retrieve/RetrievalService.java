/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.retrieve;

import gov.nasa.worldwind.WWObject;

/**
 * @author Tom Gaskins
 * @version $Id: RetrievalService.java 3558 2007-11-17 08:36:45Z tgaskins $
 */
public interface RetrievalService extends WWObject
{
    RetrievalFuture runRetriever(Retriever retriever);

    RetrievalFuture runRetriever(Retriever retriever, double priority);

    void setRetrieverPoolSize(int poolSize);

    int getRetrieverPoolSize();

    boolean hasActiveTasks();

    boolean isAvailable();

    boolean contains(Retriever retriever);

    int getNumRetrieversPending();

    void shutdown(boolean immediately);
}
