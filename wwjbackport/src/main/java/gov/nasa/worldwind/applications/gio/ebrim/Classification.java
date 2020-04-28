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
public interface Classification extends RegistryObject
{
    String getClassificationScheme();

    void setClassificationScheme(String classificationScheme);

    String getClassifiedObject();

    void setClassifiedObject(String classifiedObject);

    String getClassificationNode();

    void setClassificationNode(String classificationNode);

    String getNodeRepresentation();

    void setNodeRepresentation(String nodeRepresentation);
}
