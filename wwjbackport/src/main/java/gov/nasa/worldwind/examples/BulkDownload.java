/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import java.awt.*;

/**
 * Bulk download of layer data.
 *
 * @author Patrick Murris
 * @version $Id: BulkDownload.java 10895 2009-05-06 00:24:04Z patrickmurris $
 */
public class BulkDownload extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            // Add control panel
            this.getLayerPanel().add(new BulkDownloadPanel(getWwd()), BorderLayout.SOUTH);
        }
    }


    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Layer Download", AppFrame.class);
    }
}
