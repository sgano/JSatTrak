/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind;

/**
 * @author tag
 * @version $Id: Disposable.java 13048 2010-01-27 03:08:12Z tgaskins $
 */
public interface Disposable
{
    /** Disposes of any internal resources allocated by the object. */
    public void dispose();
}
