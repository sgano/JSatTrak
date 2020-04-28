/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.retrieve;

import gov.nasa.worldwind.avlist.AVList;

/**
 * @author tag
 * @version $Id: RetrieverFactory.java 13979 2010-10-15 17:33:42Z tgaskins $
 */
public interface RetrieverFactory
{
    Retriever createRetriever(AVList params, RetrievalPostProcessor postProcessor);
}
