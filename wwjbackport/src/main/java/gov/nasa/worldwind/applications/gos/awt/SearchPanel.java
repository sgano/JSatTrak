/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.awt;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.applications.gos.*;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.WWUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

/**
 * @author dcollins
 * @version $Id: SearchPanel.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public class SearchPanel extends JPanel
{
    protected static final int DEFAULT_RECORD_PAGE_SIZE = 10;

    protected boolean initialized = false;
    protected JLabel iconLabel;
    protected JTextField keywordsField;
    protected JButton searchButton;

    public SearchPanel()
    {
        this.iconLabel = new JLabel();

        Object source = ResourceUtil.getResourceReference("generic.SearchImage");
        if (!WWUtil.isEmpty(source))
        {
            BufferedImage image = ResourceUtil.getCachedImage(source);
            if (image != null)
                this.iconLabel.setIcon(new ImageIcon(image));
        }

        Action action = new SearchAction();
        this.keywordsField = new JTextField();
        this.keywordsField.setAction(action);
        this.keywordsField.setText((String) action.getValue(Action.LONG_DESCRIPTION));
        this.keywordsField.addFocusListener(new FocusAdapter()
        {
            public void focusGained(FocusEvent e)
            {
                if (!isInitialized())
                {
                    init();
                }
            }
        });

        this.searchButton = new JButton(new SearchAction());

        this.setBackground(Color.WHITE);
        this.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40)); // top, left, bottom, right
        this.layoutComponents();
    }

    public void setEnabled(boolean enabled)
    {
        super.setEnabled(enabled);
        this.iconLabel.setEnabled(enabled);
        this.keywordsField.setEnabled(enabled);
        this.searchButton.setEnabled(enabled);
    }

    public void getParams(AVList outParams)
    {
        if (!isInitialized())
        {
            init();
        }

        String s = this.keywordsField.getText();
        if (!WWUtil.isEmpty(s))
            outParams.setValue(GeodataKey.SEARCH_TEXT, s);

        Integer i = Configuration.getIntegerValue(GeodataKey.RECORD_PAGE_SIZE, DEFAULT_RECORD_PAGE_SIZE);
        outParams.setValue(GeodataKey.RECORD_PAGE_SIZE, i);
    }

    public void setContent(AVList params)
    {
        this.keywordsField.setText(params.getStringValue(GeodataKey.SEARCH_TEXT));
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

    protected void fireActionPerformed(ActionEvent event)
    {
        for (ActionListener listener : this.getActionListeners())
        {
            listener.actionPerformed(event);
        }
    }

    protected void init()
    {
        this.keywordsField.setText(null);
        this.setInitialized(true);
    }

    protected boolean isInitialized()
    {
        return this.initialized;
    }

    protected void setInitialized(boolean initialized)
    {
        this.initialized = initialized;
    }

    protected void layoutComponents()
    {
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(this.iconLabel);
        this.add(Box.createHorizontalStrut(5));
        this.add(this.keywordsField);
        this.add(Box.createHorizontalStrut(5));
        this.add(this.searchButton);
    }

    protected class SearchAction extends AbstractAction
    {
        public SearchAction()
        {
            super("Search");
            this.putValue(Action.SHORT_DESCRIPTION, "Search");
            this.putValue(Action.LONG_DESCRIPTION, "Search gedata.gov");
        }

        public void actionPerformed(ActionEvent event)
        {
            fireActionPerformed(event);
        }
    }
}
