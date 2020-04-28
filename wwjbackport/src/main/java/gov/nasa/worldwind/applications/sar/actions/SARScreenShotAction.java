/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.sar.actions;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.examples.util.ScreenShotAction;

import javax.swing.*;

/**
 * @author dcollins
 * @version $Id: SARScreenShotAction.java 11816 2009-06-22 22:59:35Z dcollins $
 */
public class SARScreenShotAction extends ScreenShotAction
{
    public SARScreenShotAction(WorldWindow wwd, Icon icon)
    {
        super(wwd);
        this.putValue(Action.NAME, "Screen Shot...");
        this.putValue(Action.SHORT_DESCRIPTION, "Save a screen shot");
        this.putValue(Action.SMALL_ICON, icon);
    }
}
