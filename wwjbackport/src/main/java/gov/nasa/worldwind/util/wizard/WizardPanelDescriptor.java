/* Copyright (C) 2001, 2007 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.util.wizard;

import javax.swing.*;
import java.awt.*;

/**
 * @author dcollins
 * @version $Id: WizardPanelDescriptor.java 4529 2008-02-20 03:27:48Z dcollins $
 */
public interface WizardPanelDescriptor
{
    Component getPanelComponent();

    Object getBackPanelDescriptor();

    Object getNextPanelDescriptor();

    void registerPanel(Wizard wizard);

    void aboutToDisplayPanel();

    void displayingPanel();

    void aboutToHidePanel();
}
