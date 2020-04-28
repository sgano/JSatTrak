/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.awt;

import javax.swing.*;
import java.awt.event.*;

/**
 * @author dcollins
 * @version $Id: ActionPanel.java 13127 2010-02-16 04:02:26Z dcollins $
 */
public class ActionPanel extends JPanel implements ActionListener
{
    public ActionPanel()
    {
    }

    public void addActionListener(ActionListener listener)
    {
        this.listenerList.add(ActionListener.class, listener);
    }

    public void removeActionListener(ActionListener listener)
    {
        this.listenerList.remove(ActionListener.class, listener);
    }

    public ActionListener[] getActionListeners()
    {
        return this.listenerList.getListeners(ActionListener.class);
    }

    public void actionPerformed(ActionEvent event)
    {
        // Notify my ActionListeners of the ActionEvent.
        this.fireActionPerformed(event);
    }

    protected void fireActionPerformed(ActionEvent event)
    {
        for (ActionListener listener : this.getActionListeners())
        {
            listener.actionPerformed(event);
        }
    }
}
