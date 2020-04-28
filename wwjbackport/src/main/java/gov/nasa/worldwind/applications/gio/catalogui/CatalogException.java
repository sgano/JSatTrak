/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

/**
 * @author dcollins
 * @version $Id: CatalogException.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class CatalogException
{
    private String description;
    private Exception exception;

    public CatalogException(String description, Exception exception)
    {
        this.description = description;
        this.exception = exception;
    }

    public String getDescription()
    {
        return this.description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Exception getException()
    {
        return this.exception;
    }

    public void setException(Exception exception)
    {
        this.exception = exception;
    }
}
