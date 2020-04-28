/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.exception;

/**
 * @author Tom Gaskins
 * @version $Id: WWRuntimeException.java 2422 2007-07-25 23:07:49Z tgaskins $
 */
public class WWRuntimeException extends RuntimeException
{
    public WWRuntimeException()
    {
    }

    public WWRuntimeException(String s)
    {
        super(s);
    }

    public WWRuntimeException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public WWRuntimeException(Throwable throwable)
    {
        super(throwable);
    }
}
