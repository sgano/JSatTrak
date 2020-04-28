/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.awt;

import javax.swing.*;
import java.awt.*;

/**
 * @author dcollins
 * @version $Id: GOSTextPane.java 13127 2010-02-16 04:02:26Z dcollins $
 */
public class GOSTextPane extends JTextPane
{
    public GOSTextPane()
    {
    }

    public Dimension getPreferredSize()
    {
        Dimension d = super.getPreferredSize();

        JViewport port = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, this);
        if (port != null)
        {
            Point pointInViewportCoords = SwingUtilities.convertPoint(this, new Point(0, 0), port);

            int prefWidth = port.getWidth() - pointInViewportCoords.x;
            if (prefWidth != d.width)
                d = new Dimension(prefWidth, d.height);
        }

        return d;
    }
}
