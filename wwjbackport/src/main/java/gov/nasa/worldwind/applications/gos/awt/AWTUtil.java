/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.awt;

import javax.swing.*;
import java.awt.*;

/**
 * @author dcollins
 * @version $Id: AWTUtil.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public class AWTUtil
{
    public static void setComponentEnabled(Component comp, boolean enabled)
    {
        if (comp instanceof JScrollPane)
        {
            Component c = ((JScrollPane) comp).getViewport().getView();
            if (c instanceof Container)
                setTreeEnabled((Container) c, enabled);
        }
        else if (comp instanceof Container)
        {
            setTreeEnabled((Container) comp, enabled);
        }
        else
        {
            comp.setEnabled(enabled);
        }

        comp.setEnabled(enabled);
    }

    public static void setTreeEnabled(Container container, boolean enabled)
    {
        synchronized (container.getTreeLock())
        {
            for (Component comp : container.getComponents())
            {
                setComponentEnabled(comp, enabled);
            }
        }
    }
}
