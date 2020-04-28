/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwindow.core;

import gov.nasa.worldwindow.features.Feature;

import javax.swing.*;

/**
 * @author tag
 * @version $Id: ToolBar.java 13266 2010-04-10 02:47:09Z tgaskins $
 */
public interface ToolBar
{
    JToolBar getJToolBar();

    void addFeature(Feature feature);
}
