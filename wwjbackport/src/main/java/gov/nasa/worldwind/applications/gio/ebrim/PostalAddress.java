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
public interface PostalAddress
{
    String getCity();

    void setCity(String city);

    String getCountry();

    void setCountry(String country);

    String getPostalCode();

    void setPostalCode(String postalCode);

    String getStateOrProvince();

    void setStateOrProvince(String stateOrProvince);

    String getStreet();

    void setStreet(String street);

    String getStreetNumber();

    void setStreetNumber(String streetNumber);
}
