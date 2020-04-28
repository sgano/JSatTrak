/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwindow.core;

import gov.nasa.worldwindow.features.FeaturePanel;

/**
 * @author tag
 * @version $Id: StatusPanel.java 13266 2010-04-10 02:47:09Z tgaskins $
 */
public interface StatusPanel extends FeaturePanel
{
    String setStatusMessage(String message);
}
