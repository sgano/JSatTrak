package gov.nasa.worldwindow.core;

import gov.nasa.worldwindow.features.Feature;

import javax.swing.*;/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

/**
 * @author tag
 * @version $Id: MenuBar.java 13566 2010-07-21 04:13:57Z tgaskins $
 */
public interface MenuBar extends Feature
{
    JMenuBar getJMenuBar();

    void addMenu(Menu menu);
}
