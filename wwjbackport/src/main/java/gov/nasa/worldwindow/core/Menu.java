/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwindow.core;

import javax.swing.*;

/**
 * @author tag
 * @version $Id: Menu.java 13566 2010-07-21 04:13:57Z tgaskins $
 */
public interface Menu extends Initializable
{
    JMenu getJMenu();

    void addMenu(String featureID);
}
