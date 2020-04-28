/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.pick.PickedObject;

/**
 * Shows how to cause all elements under the cursor to be reported in {@link SelectEvent}s.
 *
 * @author tag
 * @version $Id: DeepPicking.java 13945 2010-10-07 01:45:42Z tgaskins $
 */
public class DeepPicking extends Airspaces
{
    public static class AppFrame extends Airspaces.AppFrame
    {
        public AppFrame()
        {
            // Prohibit batch picking for the airspaces.
            this.controller.aglAirspaces.setEnableBatchPicking(false);
            this.controller.amslAirspaces.setEnableBatchPicking(false);

            // Tell the scene controller to peroform deep picking.
            this.controller.wwd.getSceneController().setDeepPickEnabled(true);

            // Register a select listener to print the class names of the items under the cursor.
            this.controller.wwd.addSelectListener(new SelectListener()
            {
                public void selected(SelectEvent event)
                {
                    if (event.getEventAction().equals(SelectEvent.HOVER) && event.getObjects() != null)
                    {
                        System.out.printf("%d objects\n", event.getObjects().size());
                        if (event.getObjects().size() > 1)
                        {
                            for (PickedObject po : event.getObjects())
                            {
                                System.out.println(po.getObject().getClass().getName());
                            }
                        }
                    }
                }
            });
        }
    }

    public static void main(String[] args)
    {
        start("World Wind Deep Picking", AppFrame.class);
    }
}
