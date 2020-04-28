/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwindow.features;

import gov.nasa.worldwindow.core.*;

/**
 * @author tag
 * @version $Id: FileMenu.java 13566 2010-07-21 04:13:57Z tgaskins $
 */
public class FileMenu extends AbstractMenu
{
    public FileMenu(Registry registry)
    {
        super("File", Constants.FILE_MENU, registry);
    }

    @Override
    public void initialize(Controller controller)
    {
        super.initialize(controller);

        this.addToMenuBar();
    }
}
