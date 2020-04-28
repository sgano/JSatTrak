/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.exception;

/**
 * Indicates that a value, request or other item or action is not recognized.
 *
 * @author tag
 * @version $Id: WWUnrecognizedException.java 9411 2009-03-16 21:03:25Z tgaskins $
 */
public class WWUnrecognizedException extends WWRuntimeException
{
    /**
     * Construct an exception with a message string.
     *
     * @param msg the message.
     */
    public WWUnrecognizedException(String msg)
    {
        super(msg);
    }

    /**
     * Construct an exception with a message string and a intial-cause exception.
     *
     * @param msg the message.
     * @param t   the exception causing this exception.
     */
    public WWUnrecognizedException(String msg, Throwable t)
    {
        super(msg, t);
    }
}
