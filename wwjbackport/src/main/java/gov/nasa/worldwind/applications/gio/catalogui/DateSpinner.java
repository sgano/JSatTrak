/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;

/**
 * @author dcollins
 * @version $Id: DateSpinner.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class DateSpinner extends JPanel
{
    private JSpinner spinner;
    @SuppressWarnings({"FieldCanBeLocal"})
    private JSpinner.DateEditor editor;
    private String actionCommand;
    private EventListenerList listenerList = new EventListenerList();
    private boolean ignoreEvents = false;

    public DateSpinner(Date date)
    {
        if (date == null)
        {
            String message = Logging.getMessage("nullValue.DateIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        makeComponents(date);
        layoutComponents();
    }

    public DateSpinner()
    {
        this(new Date()); // Today
    }

    public Date getValue()
    {
        return getDateValue(this.spinner);
    }

    public void setValue(Date date)
    {
        if (date == null)
        {
            String message = Logging.getMessage("nullValue.DateIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            this.ignoreEvents = true;
            setDateValue(this.spinner, date);
        }
        finally
        {
            this.ignoreEvents = false;
        }
    }

    public void setEnabled(boolean b)
    {
        super.setEnabled(b);
        this.spinner.setEnabled(b);
    }

    public String getActionCommand()
    {
        return this.actionCommand;
    }

    public void setActionCommand(String actionCommand)
    {
        this.actionCommand = actionCommand;
    }

    public ActionListener[] getActionListeners()
    {
        return this.listenerList.getListeners(ActionListener.class);
    }

    public void addActionListener(ActionListener l)
    {
        this.listenerList.add(ActionListener.class, l);
    }

    public void removeActionListener(ActionListener l)
    {
        this.listenerList.remove(ActionListener.class, l);
    }

    private Date getDateValue(JSpinner spinner)
    {
        Date date = null;
        if (spinner != null)
        {
            Object value = spinner.getValue();
            if (value != null && value instanceof Date)
                date = (Date) value;
        }
        return date;
    }

    private void setDateValue(JSpinner spinner, Date date)
    {
        if (spinner != null && date != null)
        {
            spinner.setValue(date);
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    private void spinnerChanged(ChangeEvent event)
    {
        if (!this.ignoreEvents)
        {
            long when = System.currentTimeMillis();
            Object[] listeners = this.listenerList.getListenerList();
            ActionEvent e = null;
            for (int i = listeners.length - 2; i >= 0; i -= 2)
            {
                if (listeners[i] == ActionListener.class)
                {
                    if (e == null)
                        e = new ActionEvent(this,
                                            ActionEvent.ACTION_PERFORMED,
                                            this.actionCommand,
                                            when,
                                            0);
                    ((ActionListener) listeners[i+1]).actionPerformed(e);
                }
            }
        }
    }

    private void makeComponents(Date date)
    {
        this.spinner = new JSpinner(new SpinnerDateModel(date, null, null, Calendar.MONTH));
        this.editor = new JSpinner.DateEditor(this.spinner, "MM/yyyy");
        this.spinner.setEditor(this.editor);
        this.spinner.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent event)
            {
                spinnerChanged(event);
            }
        });
    }

    private void layoutComponents()
    {
        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        SwingUtils.contrainMaximumSize(this.spinner);
        add(this.spinner);
    }
}
