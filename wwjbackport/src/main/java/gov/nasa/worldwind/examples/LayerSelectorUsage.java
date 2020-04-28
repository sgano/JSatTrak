/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.examples.util.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.tree.*;

import java.util.ArrayList;

/**
 * Example of using a tree control to display a list of layers.
 *
 * @author pabercrombie
 * @version $Id $
 */
public class LayerSelectorUsage extends ApplicationTemplate
{
    private static final String LAYER_MANAGER_ICON_PATH = "gov/nasa/worldwindow/images/layer-manager-64x64.png";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected HotSpotController controller;
        protected Tree selector;
        protected RenderableLayer layerSelectorLayer;

        public AppFrame()
        {
            super(true, false, false);

            // Add a RenderableLayer with a couple shapes so that the tree will have more than one layer
            RenderableLayer shapeLayer = new RenderableLayer();
            shapeLayer.setName("Polygons");
            this.drawPolygons(shapeLayer);
            insertBeforePlacenames(getWwd(), shapeLayer);

            this.layerSelectorLayer = new RenderableLayer();

            this.selector = this.createLayerSelector();
            this.layerSelectorLayer.addRenderable(selector);

            this.controller = new HotSpotController(this.getWwd());

            this.buildTreeModel();

            // Add the layer to the model.
            insertBeforePlacenames(getWwd(), this.layerSelectorLayer);
        }

        protected Tree createLayerSelector()
        {
            String title = "Layers";

            BasicTree selector = new BasicTree();
            BasicTreeModel model = new BasicTreeModel(selector);

            TreeNode root = new BasicTreeNode(model, title, LAYER_MANAGER_ICON_PATH);
            selector.setModel(model);
            model.setRoot(root);

            BasicTreeLayout layout = new BasicTreeLayout(selector, this.getWwd(), 20, 130);
            selector.setLayout(layout);
            layout.getFrame().setIconImageSource(LAYER_MANAGER_ICON_PATH);

            BasicTreeAttributes attributes = new BasicTreeAttributes();
            attributes.setRootVisible(false);
            attributes.setBackgroundOpacity(0.7);
            attributes.setForegroundOpacity(0.7);
            layout.setAttributes(attributes);
            layout.getFrame().setAttributes(attributes);

            attributes = new BasicTreeAttributes();
            attributes.setRootVisible(false);
            attributes.setBackgroundOpacity(1.0);
            layout.setHighlightAttributes(attributes);
            layout.getFrame().setHighlightAttributes(attributes);
            layout.getFrame().setFrameTitle(title);

            selector.expandPath(root.getPath());

            return selector;
        }

        protected void buildTreeModel()
        {
            this.selector.getModel().getRoot().removeAllChildren();

            for (Layer layer : this.getWwd().getModel().getLayers())
            {
                if (layer != this.layerSelectorLayer)
                {
                    TreeNode layerNode = new LayerTreeNode((BasicTreeModel) selector.getModel(), layer);
                    this.selector.getModel().getRoot().addChild(layerNode);

                    if (layer instanceof RenderableLayer)
                        this.addRenderables(layerNode, (RenderableLayer) layer);
                }
            }
        }

        protected void addRenderables(TreeNode root, RenderableLayer layer)
        {
            for (Renderable renderable : layer.getRenderables())
            {
                String name = null;
                String description = null;
                if (renderable instanceof AVList)
                {
                    AVList list = (AVList) renderable;
                    name = list.getStringValue(AVKey.DISPLAY_NAME);
                    description = list.getStringValue(AVKey.DESCRIPTION);
                }

                if (name == null)
                    name = renderable.getClass().getSimpleName();

                BasicTreeNode node = new BasicTreeNode((BasicTreeModel) this.selector.getModel(), name);
                node.setDescription(description);
                root.addChild(node);
            }
        }

        public void drawPolygons(RenderableLayer layer)
        {
            // Create a polygon, set some of its properties and set its attributes.
            ArrayList<Position> pathPositions = new ArrayList<Position>();
            pathPositions.add(Position.fromDegrees(28, -106, 3e4));
            pathPositions.add(Position.fromDegrees(35, -104, 3e4));
            pathPositions.add(Position.fromDegrees(35, -107, 9e4));
            pathPositions.add(Position.fromDegrees(28, -107, 9e4));
            pathPositions.add(Position.fromDegrees(28, -106, 3e4));
            Polygon pgon = new Polygon(pathPositions);
            pgon.setValue(AVKey.DISPLAY_NAME, "One polygon");
            pgon.setValue(AVKey.DESCRIPTION, "This is a polygon");
            pgon.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            layer.addRenderable(pgon);

            ArrayList<Position> pathLocations = new ArrayList<Position>();
            pathLocations.add(Position.fromDegrees(28, -110, 5e4));
            pathLocations.add(Position.fromDegrees(35, -108, 5e4));
            pathLocations.add(Position.fromDegrees(35, -111, 5e4));
            pathLocations.add(Position.fromDegrees(28, -111, 5e4));
            pathLocations.add(Position.fromDegrees(28, -110, 5e4));
            pgon = new Polygon(pathLocations);
            pgon.setValue(AVKey.DISPLAY_NAME, "Another polygon");
            pgon.setValue(AVKey.DESCRIPTION, "This is also a polygon");
            layer.addRenderable(pgon);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Layer Selector", AppFrame.class);
    }
}
