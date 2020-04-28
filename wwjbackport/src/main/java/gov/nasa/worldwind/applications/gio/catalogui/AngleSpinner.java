/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author dcollins
 * @version $Id: AngleSpinner.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class AngleSpinner extends JPanel
{
    private final String mode;
    private JSpinner degreeSpinner;
    private JSpinner minuteSpinner;
    private JSpinner secondSpinner;
    @SuppressWarnings({"FieldCanBeLocal"})
    private JSpinner.NumberEditor degreeEditor;
    @SuppressWarnings({"FieldCanBeLocal"})
    private JSpinner.NumberEditor minuteEditor;
    @SuppressWarnings({"FieldCanBeLocal"})
    private JSpinner.NumberEditor secondEditor;
    private String actionCommand;    
    private EventListenerList listenerList = new EventListenerList();
    private boolean ignoreEvents;

    public static final String LATITUDE = "Latitude";
    public static final String LONGITUDE = "Longitude";

    public AngleSpinner(Angle value, String mode)
    {
        if (value == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.mode = mode;
        makeComponents();
        layoutComponents();
        setValue(value);
    }

    public AngleSpinner(String mode)
    {
        this(Angle.ZERO, mode);
    }

    public final String getMode()
    {
        return this.mode;
    }

    public double getDegrees()
    {
        return getDoubleValue(this.degreeSpinner);
    }

    public void setDegrees(double degrees)
    {
        try
        {
            this.ignoreEvents = true;
            setDoubleValue(this.degreeSpinner, degrees);
        }
        finally
        {
            this.ignoreEvents = false;
        }
    }

    public double getMinutes()
    {
        return getDoubleValue(this.minuteSpinner);
    }

    public void setMinutes(double minutes)
    {
        try
        {
            this.ignoreEvents = true;
            setDoubleValue(this.minuteSpinner, minutes);
        }
        finally
        {
            this.ignoreEvents = false;
        }
    }

    public double getSeconds()
    {
        return getDoubleValue(this.secondSpinner);
    }

    public void setSeconds(double seconds)
    {
        try
        {
            this.ignoreEvents = true;
            setDoubleValue(this.secondSpinner, seconds);
        }
        finally
        {
            this.ignoreEvents = false;
        }
    }

    public Angle getValue()
    {
        double d = getDegrees();
        double m = getMinutes();
        double s = getSeconds();
        return Angle.fromDegrees(decimalFromDMS(d, m, s));
    }

    public void setValue(Angle angle)
    {
        if (angle == null)
        {
            String message = Logging.getMessage("nullValue.AngleIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double oldDegres = getDegrees();
        double oldMinutes = getMinutes();
        double oldSeconds = getSeconds();

        try
        {
            this.ignoreEvents = true;
            double newDegrees = degreesFromDecimal(angle.degrees);
            double newMinutes = minutesFromDecimal(angle.degrees);
            double newSeconds = secondsFromDecimal(angle.degrees);
            setDegrees(newDegrees);
            setMinutes(newMinutes);
            setSeconds(newSeconds);
        }
        catch (IllegalArgumentException e)
        {
            // Setting one or more properties failed (the JSpinner editor rejected the value).
            // Restore the previous values.
            // Note: no property event will fire, even during restoration.
            setDegrees(oldDegres);
            setMinutes(oldMinutes);
            setSeconds(oldSeconds);
            throw e;
        }
        finally
        {
            this.ignoreEvents = false;
        }
    }

    public void setEnabled(boolean b)
    {
        super.setEnabled(b);
        this.degreeSpinner.setEnabled(b);
        this.minuteSpinner.setEnabled(b);
        this.secondSpinner.setEnabled(b);
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
    
    private Double getDoubleValue(JSpinner spinner)
    {
        Double d = 0.0;
        if (spinner != null)
        {
            Object value = spinner.getValue();
            if (value != null && value instanceof Number)
                d = ((Number) value).doubleValue();
        }
        return d;
    }

    private void setDoubleValue(JSpinner spinner, Double value)
    {
        if (spinner != null && value != null)
        {
            spinner.setValue(value);
        }
    }

    private double degreesFromDecimal(double angle)
    {
        return (int) angle;
    }

    private double minutesFromDecimal(double angle)
    {
        double d = degreesFromDecimal(angle);
        return 60 * Math.abs(angle - d);
    }

    private double secondsFromDecimal(double angle)
    {
        double m = minutesFromDecimal(angle);
        return 60 * Math.abs(m - (int) m);
    }

    private double decimalFromDMS(double d, double m, double s)
    {
        return d + (m / 60.0) + (s / 3600.0);
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

    private void makeComponents()
    {
        // Widget represents latitude values.
        if (this.mode != null &&
           (this.mode.contains("latitude") || this.mode.contains("Latitude") || this.mode.contains("LATITUDE")))
        {
            this.degreeSpinner = new JSpinner(new SpinnerNumberModel(0.0, -90.0, 90.0, 1.0));
            this.degreeEditor = new JSpinner.NumberEditor(this.degreeSpinner, "N 000\u00B0;S 000\u00B0");
        }
        // Widget represents longitude values.
        else
        {
            this.degreeSpinner = new JSpinner(new SpinnerNumberModel(0.0, -180.0, 180.0, 1.0));
            this.degreeEditor = new JSpinner.NumberEditor(this.degreeSpinner, "E 000\u00B0;W 000\u00B0");
        }
        this.minuteSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 60.0, 1.0));
        this.secondSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 60.0, 1.0));

        this.minuteEditor = new JSpinner.NumberEditor(this.minuteSpinner, "00\u0027\u0027");
        this.secondEditor = new JSpinner.NumberEditor(this.secondSpinner, "00\u0027\u005C\u0022\u0027");
        this.degreeSpinner.setEditor(this.degreeEditor);
        this.minuteSpinner.setEditor(this.minuteEditor);
        this.secondSpinner.setEditor(this.secondEditor);

        this.degreeSpinner.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent event)
            {
                spinnerChanged(event);
            }
        });
        this.minuteSpinner.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent event)
            {
                spinnerChanged(event);
            }
        });
        this.secondSpinner.addChangeListener(new ChangeListener()
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

        SwingUtils.contrainMaximumSize(this.degreeSpinner);
        SwingUtils.contrainMaximumSize(this.minuteSpinner);
        SwingUtils.contrainMaximumSize(this.secondSpinner);

        add(this.degreeSpinner);
        add(Box.createHorizontalStrut(5));
        add(this.minuteSpinner);
        add(Box.createHorizontalStrut(5));
        add(this.secondSpinner);
    }
}
