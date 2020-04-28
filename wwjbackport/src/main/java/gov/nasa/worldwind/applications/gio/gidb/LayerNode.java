/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.gidb;

import gov.nasa.worldwind.applications.gio.catalogui.AVListNode;

/**
 * @author dcollins
 * @version $Id: LayerNode.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class LayerNode extends AVListNode<Layer>
{
    public LayerNode(Layer layer)
    {
        super(layer);

        update();
    }
}