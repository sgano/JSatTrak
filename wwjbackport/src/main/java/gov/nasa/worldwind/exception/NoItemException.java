/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.exception;

/**
 * Thrown to indicate that an item is not available from a request or search.
 *
 * @author tag
 * @version $Id: NoItemException.java 8393 2009-01-10 05:36:05Z tgaskins $
 */
public class NoItemException extends WWRuntimeException
{
    public NoItemException(String string)
    {
        super(string);
    }
}
