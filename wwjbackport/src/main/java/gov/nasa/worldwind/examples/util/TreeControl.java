/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.examples.util;

import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.util.tree.*;

/**
 * Example of using the on-screen tree control.
 *
 * @author pabercrombie
 * @version $Id: TreeControl.java 14107 2010-11-12 20:27:06Z dcollins $
 */
public class TreeControl extends ApplicationTemplate
{
    private static final String ICON_PATH = "images/16x16-icon-nasa.png";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        HotSpotController controller;

        public AppFrame()
        {
            super(true, true, false);

            RenderableLayer layer = new RenderableLayer();

            BasicTree tree = new BasicTree();

            BasicTreeLayout layout = new BasicTreeLayout(tree, this.getWwd(), 100, 200);
            layout.getFrame().setFrameTitle("TreeControl");
            tree.setLayout(layout);

            BasicTreeModel model = new BasicTreeModel(tree);

            BasicTreeNode root = new BasicTreeNode(model, "Root", ICON_PATH);
            model.setRoot(root);

            BasicTreeNode child = new BasicTreeNode(model, "Child 1", ICON_PATH);
            child.setDescription("This is a child node");
            child.addChild(new BasicTreeNode(model, "Subchild 1,1"));
            child.addChild(new BasicTreeNode(model, "Subchild 1,2"));
            child.addChild(new BasicTreeNode(model, "Subchild 1,3", ICON_PATH));
            root.addChild(child);

            child = new BasicTreeNode(model, "Child 2", ICON_PATH);
            child.addChild(new BasicTreeNode(model, "Subchild 2,1"));
            child.addChild(new BasicTreeNode(model, "Subchild 2,2"));
            child.addChild(new BasicTreeNode(model, "Subchild 2,3"));
            root.addChild(child);

            child = new BasicTreeNode(model, "Child 3");
            child.addChild(new BasicTreeNode(model, "Subchild 3,1"));
            child.addChild(new BasicTreeNode(model, "Subchild 3,2"));
            child.addChild(new BasicTreeNode(model, "Subchild 3,3"));
            root.addChild(child);

            tree.setModel(model);

            tree.expandPath(root.getPath());

            controller = new HotSpotController(this.getWwd());

            layer.addRenderable(tree);

            // Add the layer to the model.
            insertBeforeCompass(this.getWwd(), layer);

            // Update layer panel
            this.getLayerPanel().update(this.getWwd());
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("Tree Control", AppFrame.class);
    }
}
