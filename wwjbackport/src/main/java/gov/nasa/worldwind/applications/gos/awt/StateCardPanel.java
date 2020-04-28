/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.awt;

import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: StateCardPanel.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public class StateCardPanel extends JPanel
{
    protected String state;
    protected Map<String, JComponent> map = new HashMap<String, JComponent>();

    public StateCardPanel()
    {
        super(new CardLayout(0, 0)); // hgap, vgap
    }

    public String getState()
    {
        return this.state;
    }

    public void setState(String state)
    {
        if (state == null)
        {
            String message = Logging.getMessage("nullValue.StateKeyIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        CardLayout cl = (CardLayout) this.getLayout();
        cl.show(this, state);
        this.state = state;
    }

    public JComponent getComponent(String state)
    {
        return this.map.get(state);
    }

    public void setComponent(String state, JComponent component)
    {
        this.map.put(state, component);
        this.add(component, state);
    }

    //**************************************************************//
    //********************  Component Constrution  *****************//
    //**************************************************************//

    public static JComponent createWaitingComponent(String text, float alignmentX, ActionListener cancelActionListener)
    {
        JLabel label = new JLabel(text);
        label.setOpaque(false);
        label.setAlignmentX(alignmentX);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setAlignmentX(alignmentX);

        JButton cancelButton = null;
        if (cancelActionListener != null)
        {
            cancelButton = new JButton("Cancel");
            cancelButton.setAlignmentX(alignmentX);
            cancelButton.addActionListener(cancelActionListener);
        }

        Box vbox = Box.createVerticalBox();
        vbox.add(Box.createVerticalGlue());
        vbox.add(label);
        vbox.add(Box.createVerticalStrut(10));
        vbox.add(progressBar);
        if (cancelButton != null)
        {
            vbox.add(Box.createVerticalStrut(10));
            vbox.add(cancelButton);
        }
        vbox.add(Box.createVerticalGlue());

        return vbox;
    }

    public static JComponent createErrorComponent(String text, float alignmentX)
    {
        JLabel label = new JLabel(text);
        label.setOpaque(false);
        label.setAlignmentX(alignmentX);

        Box vbox = Box.createVerticalBox();
        vbox.add(Box.createVerticalGlue());
        vbox.add(label);
        vbox.add(Box.createVerticalGlue());

        return vbox;
    }
}
