/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.layers.AnnotationLayer;

import java.awt.*;

/**
 * @author jparsons
 * @version $Id$
 */
public class GazetteerApp extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected AnnotationLayer layer;

        public AppFrame() throws IllegalAccessException, InstantiationException, ClassNotFoundException
        {
            super(true, false, false);

            this.layer = new AnnotationLayer();
            
            insertBeforeCompass(this.getWwd(), layer);

            this.getContentPane().add(new GazetteerPanel(this.getWwd(), null),   //use default yahoo service
                BorderLayout.NORTH);
        }

        public AnnotationLayer getAnnotationLayer()
        {
            return layer;
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Gazetteer Example", AppFrame.class);
    }
}
