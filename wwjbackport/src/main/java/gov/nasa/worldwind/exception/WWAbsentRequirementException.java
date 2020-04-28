/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.exception;

/**
 * @author tag
 * @version $Id: WWAbsentRequirementException.java 6887 2008-10-01 21:09:05Z tgaskins $
 */
public class WWAbsentRequirementException extends WWRuntimeException
{
    public WWAbsentRequirementException()
    {
    }

    public WWAbsentRequirementException(String s)
    {
        super(s);
    }

    public WWAbsentRequirementException(String s, Throwable throwable)
    {
        super(s, throwable);
    }

    public WWAbsentRequirementException(Throwable throwable)
    {
        super(throwable);
    }
}
