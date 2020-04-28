/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import javax.swing.*;
import java.awt.*;

/**
 * @author jparsons
 * @version $Id$
 */
public class PlaceNames extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);
            this.getLayerPanel().add(makeControlPanel(),  BorderLayout.SOUTH);
        }


        private JPanel makeControlPanel()
        { 
            return new PlaceNamesPanel(this.getWwd());
        }
    }



    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Place Names", AppFrame.class);
    }
}
