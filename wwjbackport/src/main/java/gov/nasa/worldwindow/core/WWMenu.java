/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwindow.core;

/**
 * @author tag
 * @version $Id: WWMenu.java 13825 2010-09-18 00:18:35Z tgaskins $
 */
public interface WWMenu
{
    void addMenu(String featureID);

    void addMenus(String[] featureIDs);
}
