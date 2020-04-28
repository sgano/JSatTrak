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
 * @version $Id: WWODialog.java 13312 2010-04-13 18:28:20Z tgaskins $
 */
public interface WWODialog extends Feature
{
    JDialog getJDialog();

    void setVisible(boolean tf);
}
