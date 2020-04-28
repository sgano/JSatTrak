/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.event;

import java.util.*;

/**
 * @author tag
 * @version $Id: RenderingExceptionListener.java 6887 2008-10-01 21:09:05Z tgaskins $
 */
public interface RenderingExceptionListener extends EventListener
{
    public void exceptionThrown(Throwable t);
}
