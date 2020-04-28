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
public interface ExternalIdentifier extends RegistryObject
{
    String getRegistryObject();

    void setRegistryObject(String registryObject);

    String getIdentificationScheme();

    void setIdentificationScheme(String identificationScheme);

    String getValue();

    void setValue(String value);
}
