/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.exception;

/**
 * Thrown to indicate a service has failed.
 *
 * @author tag
 * @version $Id: ServiceException.java 8393 2009-01-10 05:36:05Z tgaskins $
 */
public class ServiceException extends WWRuntimeException
{
    public ServiceException(String message)
    {
        super(message);
    }
}
