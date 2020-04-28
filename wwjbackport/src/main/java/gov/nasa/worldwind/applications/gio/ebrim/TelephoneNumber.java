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
public interface TelephoneNumber
{
    String getAreaCode();

    void setAreaCode(String areaCode);

    String getCountryCode();

    void setCountryCode(String countryCode);

    String getExtension();

    void setExtension(String extension);

    String getNumber();

    void setNumber(String number);

    String getPhoneType();

    void setPhoneType(String phoneType);
}
