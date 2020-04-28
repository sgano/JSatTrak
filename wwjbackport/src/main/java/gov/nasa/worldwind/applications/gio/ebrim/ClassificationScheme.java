/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.ebrim;

import java.util.Collection;

/**
 * @author dcollins
 * @version $Id$
 */
public interface ClassificationScheme extends RegistryObject, Iterable<ClassificationNode>
{
    int getClassificationNodeCount();

    int getIndex(ClassificationNode node);

    ClassificationNode getClassificationNode(int index);

    void setClassificationNode(int index, ClassificationNode node);

    void addClassificationNode(int index, ClassificationNode node);

    void addClassificationNode(ClassificationNode node);

    void addClassificationNodes(Collection<? extends ClassificationNode> c);

    void removeClassificationNode(int index);

    void clearClassificationNodes();

    boolean isInternal();

    void setInternal(boolean internal);

    String getNodeType();

    void setNodeType(String nodeType);
}
