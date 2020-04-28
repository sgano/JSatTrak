/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwindow.features;

import gov.nasa.worldwindow.core.*;
import gov.nasa.worldwindow.util.Util;

import java.awt.event.*;

/**
 * @author tag
 * @version $Id: Measurement.java 13312 2010-04-13 18:28:20Z tgaskins $
 */
public class Measurement extends AbstractFeature
{
    private WWODialog dialog;

    public Measurement()
    {
        this(null);
    }

    public Measurement(Registry registry)
    {
        super("Measurement", Constants.FEATURE_MEASUREMENT, "gov/nasa/worldwindow/images/globe-sextant-64x64.png",
            registry);
        setEnabled(true);
    }

    @Override
    public void initialize(Controller controller)
    {
        super.initialize(controller);

        this.addToToolBar();
    }

    @Override
    public boolean isTwoState()
    {
        return true;
    }

    public boolean isOn()
    {
        return this.dialog != null && this.dialog.getJDialog().isVisible();
    }

    @Override
    public void turnOn(boolean tf)
    {
        if (this.dialog != null)
            this.dialog.setVisible(tf);
    }

    @Override
    protected void doActionPerformed(ActionEvent actionEvent)
    {
        if (this.dialog == null)
            this.dialog = (WWODialog) this.controller.getRegisteredObject(Constants.FEATURE_MEASUREMENT_DIALOG);
        if (this.dialog == null)
        {
            Util.getLogger().severe("Measurement dialog not registered");
            return;
        }

        this.turnOn(!this.isOn());
    }
}
