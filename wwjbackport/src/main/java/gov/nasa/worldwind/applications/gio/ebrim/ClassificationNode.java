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
public interface ClassificationNode extends RegistryObject, Iterable<ClassificationNode>
{
    int getChildCount();

    int getIndex(ClassificationNode child);

    ClassificationNode getChild(int index);

    void setChild(int index, ClassificationNode child);

    void addChild(int index, ClassificationNode child);

    void addChild(ClassificationNode child);

    void addChildren(Collection<? extends ClassificationNode> c);

    void removeChild(int index);

    void clearChildren();

    String getParent();

    void setParent(String parent);

    String getCode();

    void setCode(String code);

    String getPath();

    void setPath(String path);
}
