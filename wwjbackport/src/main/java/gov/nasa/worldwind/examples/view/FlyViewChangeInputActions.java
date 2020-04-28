/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples.view;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.awt.ViewInputAttributes;
import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.view.firstperson.*;

import java.awt.event.*;

/**
 * @author jym
 * @version $Id: FlyViewChangeInputActions.java 12548 2009-09-03 15:48:47Z jterhorst $
 */
public class FlyViewChangeInputActions extends ApplicationTemplate
{

    protected static class MyFlyViewInputHandler extends FlyViewInputHandler
    {
        public MyFlyViewInputHandler()
        {

            ViewInputAttributes.ActionAttributes.MouseAction[] mouseActionTrans = {
                ViewInputAttributes.ActionAttributes.createMouseActionAttribute(MouseEvent.BUTTON3_DOWN_MASK)
            };
            this.getAttributes().setMouseActionAttributes(ViewInputAttributes.VIEW_HORIZONTAL_TRANSLATE, 0,
                ViewInputAttributes.ActionAttributes.ActionTrigger.ON_KEY_DOWN,
                mouseActionTrans,
                FlyViewInputHandler.DEFAULT_MOUSE_HORIZONTAL_TRANSLATE_MIN_VALUE,
                FlyViewInputHandler.DEFAULT_MOUSE_HORIZONTAL_TRANSLATE_MAX_VALUE,
                true, .7);

            ViewInputAttributes.ActionAttributes.MouseAction[] mouseActionRot = {
                ViewInputAttributes.ActionAttributes.createMouseActionAttribute(MouseEvent.BUTTON1_DOWN_MASK)
            };
            this.getAttributes().setMouseActionAttributes(ViewInputAttributes.VIEW_ROTATE, 0,
            ViewInputAttributes.ActionAttributes.ActionTrigger.ON_KEY_DOWN,
                mouseActionRot,
                FlyViewInputHandler.DEFAULT_MOUSE_ROTATE_MIN_VALUE,
                FlyViewInputHandler.DEFAULT_MOUSE_ROTATE_MAX_VALUE, 
                true, .7);
        }
    }
    public static void main(String[] args)
    {
        // Call the static start method like this from the main method of your derived class.
        // Substitute your application's name for the first argument.
        ApplicationTemplate.AppFrame appFrame = ApplicationTemplate.start("World Wind Fly View Controls", ApplicationTemplate.AppFrame.class);

        WorldWindow wwd = appFrame.getWwd();

        // Force the view to be a FlyView
        BasicFlyView flyView = new BasicFlyView();
        flyView.setViewInputHandler(new MyFlyViewInputHandler());
        wwd.setView(flyView);


    }
}
