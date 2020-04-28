/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.awt;

import javax.swing.*;
import javax.swing.event.*;

/**
 * @author dcollins
 * @version $Id: HyperlinkPanel.java 13127 2010-02-16 04:02:26Z dcollins $
 */
public class HyperlinkPanel extends JPanel implements HyperlinkListener
{
    public HyperlinkPanel()
    {
    }

    public void addHyperlinkListener(HyperlinkListener listener)
    {
        this.listenerList.add(HyperlinkListener.class, listener);
    }

    public void removeHyperlinkListener(HyperlinkListener listener)
    {
        this.listenerList.remove(HyperlinkListener.class, listener);
    }

    public HyperlinkListener[] getHyperlinkListeners()
    {
        return this.listenerList.getListeners(HyperlinkListener.class);
    }

    public void hyperlinkUpdate(HyperlinkEvent event)
    {
        // Notify my HyperlinkListeners of the HyperlinkEvent.
        this.fireHyperlinkEvent(event);
    }

    protected void fireHyperlinkEvent(HyperlinkEvent event)
    {
        for (HyperlinkListener listener : this.getHyperlinkListeners())
        {
            listener.hyperlinkUpdate(event);
        }
    }
}
