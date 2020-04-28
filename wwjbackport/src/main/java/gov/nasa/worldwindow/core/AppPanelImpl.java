/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwindow.core;

import gov.nasa.worldwindow.features.AbstractFeature;

import javax.swing.*;
import java.awt.*;

/**
 * @author tag
 * @version $Id: AppPanelImpl.java 13266 2010-04-10 02:47:09Z tgaskins $
 */
public class AppPanelImpl extends AbstractFeature implements AppPanel
{
    private JPanel panel;

    public AppPanelImpl(Registry registry)
    {
        super("App Panel", Constants.APP_PANEL, registry);

        this.panel = new JPanel(new BorderLayout());
        this.panel.setPreferredSize(new Dimension(1280, 800));
    }

    public void initialize(final Controller controller)
    {
        super.initialize(controller);

        Dimension appSize = controller.getAppSize();
        if (appSize != null)
            this.panel.setPreferredSize(appSize);

        WWPanel wwPanel = controller.getWWPanel();
        if (wwPanel != null)
            this.panel.add(wwPanel.getJPanel(), BorderLayout.CENTER);
    }

    public JPanel getJPanel()
    {
        return this.panel;
    }
}
