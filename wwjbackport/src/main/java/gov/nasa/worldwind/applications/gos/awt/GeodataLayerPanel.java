/* Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.awt;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.applications.gos.GeodataKey;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.Layer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.beans.*;

/**
 * @author dcollins
 * @version $Id: GeodataLayerPanel.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public class GeodataLayerPanel extends JPanel implements PropertyChangeListener
{
    protected WorldWindow wwd;
    // Swing components.
    protected JPanel contentPanel;
    protected JScrollPane scrollPane;

    public GeodataLayerPanel(WorldWindow wwd)
    {
        this.contentPanel = new JPanel();
        this.contentPanel.setLayout(new BoxLayout(this.contentPanel, BoxLayout.Y_AXIS));
        this.contentPanel.setBackground(Color.WHITE);
        this.contentPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40)); // top, left, bottom, right
        this.doUpdate(wwd);

        JPanel dummyPanel = new JPanel(new BorderLayout(0, 0));
        dummyPanel.setBackground(Color.WHITE);
        dummyPanel.add(this.contentPanel, BorderLayout.NORTH);

        this.scrollPane = new JScrollPane(dummyPanel,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        this.scrollPane.setAutoscrolls(false);
        this.scrollPane.setBorder(BorderFactory.createEmptyBorder());

        this.setBackground(Color.WHITE);
        this.setLayout(new BorderLayout(0, 0)); // hgap, vgap
        this.add(this.scrollPane, BorderLayout.CENTER);
    }

    public GeodataLayerPanel()
    {
        this(null);
    }

    public WorldWindow getWorldWindow()
    {
        return this.wwd;
    }

    public void setWorldWindow(WorldWindow wwd)
    {
        if (this.wwd != null)
            this.wwd.getSceneController().removePropertyChangeListener(this);

        this.wwd = wwd;
        this.update(wwd);

        if (this.wwd != null)
            this.wwd.getSceneController().addPropertyChangeListener(this);
    }

    @Override
    public void setToolTipText(String string)
    {
        this.scrollPane.setToolTipText(string);
    }

    public void propertyChange(final PropertyChangeEvent evt)
    {
        //noinspection StringEquality
        if (evt.getPropertyName() == AVKey.LAYER || evt.getPropertyName() == AVKey.LAYERS)
        {
            this.update(this.wwd);
        }
    }

    protected void update(final WorldWindow wwd)
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    update(wwd);
                }
            });
        }
        else
        {
            this.contentPanel.removeAll();
            this.doUpdate(wwd);
            this.validateTree();
        }
    }

    protected void doUpdate(WorldWindow wwd)
    {
        if (wwd == null || wwd.getModel() == null || wwd.getModel().getLayers() == null)
            return;

        for (Layer layer : wwd.getModel().getLayers())
        {
            if (layer == null)
                continue;

            if (!this.acceptLayer(layer))
                continue;

            JCheckBox jcb = new JCheckBox(new LayerAction(layer));
            jcb.setSelected(layer.isEnabled());
            this.contentPanel.add(jcb);
        }
    }

    protected boolean acceptLayer(Layer layer)
    {
        return layer != null && layer.hasKey(GeodataKey.UUID);
    }

    protected static class LayerAction extends AbstractAction
    {
        protected Layer layer;

        public LayerAction(Layer layer)
        {
            super(layer.getName());
            this.layer = layer;
        }

        public void actionPerformed(ActionEvent actionEvent)
        {
            this.layer.setEnabled(((AbstractButton) actionEvent.getSource()).isSelected());
            this.layer.firePropertyChange(AVKey.LAYER, null, this.layer);
        }
    }
}
