/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwindow.features;

import gov.nasa.worldwindow.core.WWOPanel;

import javax.swing.*;

/**
 * @author tag
 * @version $Id: FeaturePanel.java 13312 2010-04-13 18:28:20Z tgaskins $
 */
public interface FeaturePanel extends WWOPanel, Feature
{
    JComponent[] getDialogControls();
}
