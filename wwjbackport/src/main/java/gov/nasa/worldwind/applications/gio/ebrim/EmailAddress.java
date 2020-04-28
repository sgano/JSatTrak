/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.ebrim;

/**
 * @author dcollins
 * @version $Id$
 */
public interface EmailAddress
{
    String getAddress();

    void setAddress(String address);

    String getType();

    void setType(String type);
}
