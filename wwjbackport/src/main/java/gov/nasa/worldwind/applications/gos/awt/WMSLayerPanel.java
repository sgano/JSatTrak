/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.awt;

import gov.nasa.worldwind.applications.gos.*;
import gov.nasa.worldwind.applications.gos.globe.GlobeModel;
import gov.nasa.worldwind.applications.gos.html.HTMLFormatter;
import gov.nasa.worldwind.ogc.wms.*;
import gov.nasa.worldwind.util.WWUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * @author dcollins
 * @version $Id: WMSLayerPanel.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public class WMSLayerPanel extends JPanel
{
    protected Record record;
    protected GlobeModel globeModel;
    protected WMSCapabilities caps;

    public WMSLayerPanel(Record record, GlobeModel globeModel)
    {
        this.record = record;
        this.globeModel = globeModel;

        this.setBackground(Color.WHITE);
    }

    public WMSCapabilities getCapabilities()
    {
        return this.caps;
    }

    public void setCapabilities(final WMSCapabilities caps)
    {
        if (!SwingUtilities.isEventDispatchThread())
        {
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    setCapabilities(caps);
                }
            });
        }
        else
        {
            this.caps = caps;
            this.onCapabilitiesChanged();
        }
    }

    protected void onCapabilitiesChanged()
    {
        this.removeAll();

        if (this.caps == null)
            return;

        List<WMSLayerCapabilities> layerList = this.caps.getNamedLayers();
        if (layerList == null || layerList.size() == 0)
            return;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(this.createTitle());
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.add(label);
        this.add(Box.createVerticalStrut(10));

        layerList = this.sortLayerList(layerList);

        for (WMSLayerCapabilities layer : layerList)
        {
            if (layer == null)
                continue;

            Set<WMSLayerStyle> styleSet = layer.getStyles();
            if (styleSet == null || styleSet.size() == 0)
            {
                this.addLayerControl(this.caps, layer, null);
                this.add(Box.createVerticalStrut(3));
            }
            else
            {
                for (WMSLayerStyle style : styleSet)
                {
                    if (style == null)
                        continue;

                    this.addLayerControl(this.caps, layer, style);
                    this.add(Box.createVerticalStrut(3));
                }
            }
        }
    }

    protected String createTitle()
    {
        StringBuilder sb = new StringBuilder();
        HTMLFormatter formatter = new HTMLFormatter();
        formatter.setEnableAdvancedHTML(false);

        formatter.beginHTMLBody(sb);
        formatter.beginHeading(sb, 1);
        sb.append("Map layers");

        String s = this.caps.getServiceInformation().getServiceTitle();
        if (WWUtil.isEmpty(s))
            s = "No name";

        sb.append(" for \"").append(s).append("\"");
        formatter.endHeading(sb, 1);

        s = this.caps.getRequestURL("GetCapabilities", "http", "get");
        if (!WWUtil.isEmpty(s))
        {
            formatter.beginFont(sb, "#888888");
            sb.append(" [").append(s).append("]");
            formatter.endFont(sb);
        }

        formatter.endHTMLBody(sb);

        return sb.toString();
    }

    protected List<WMSLayerCapabilities> sortLayerList(List<WMSLayerCapabilities> list)
    {
        Collections.sort(list, new Comparator<WMSLayerCapabilities>()
        {
            public int compare(WMSLayerCapabilities a, WMSLayerCapabilities b)
            {
                return String.CASE_INSENSITIVE_ORDER.compare(a.getName(), b.getName());
            }
        });

        return list;
    }

    protected void addLayerControl(WMSCapabilities caps, WMSLayerCapabilities layer, WMSLayerStyle style)
    {
        if (this.globeModel == null)
            return;

        Action action = this.globeModel.hasWMSLayer(this.record.getIdentifier(), layer, style) ?
            new RemoveWMSLayerAction(this, caps, layer, style) :
            new AddWMSLayerAction(this, caps, layer, style);
        JCheckBox jcb = new JCheckBox(action);
        jcb.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.add(jcb);
    }

    protected String makeWMSLayerDisplayName(WMSLayerCapabilities layer, WMSLayerStyle style)
    {
        return ResourceUtil.makeWMSLayerDisplayName(layer, style);
    }

    protected static class AddWMSLayerAction extends AbstractAction
    {
        protected WMSLayerPanel owner;
        protected WMSCapabilities caps;
        protected WMSLayerCapabilities layer;
        protected WMSLayerStyle style;

        public AddWMSLayerAction(WMSLayerPanel owner, WMSCapabilities caps, WMSLayerCapabilities layer,
            WMSLayerStyle style)
        {
            super(owner.makeWMSLayerDisplayName(layer, style));
            this.putValue(Action.SELECTED_KEY, false);
            this.owner = owner;
            this.caps = caps;
            this.layer = layer;
            this.style = style;
        }

        public void actionPerformed(ActionEvent event)
        {
            this.owner.globeModel.addWMSLayer(this.owner.record.getIdentifier(), this.caps, this.layer, this.style);
            ((AbstractButton) event.getSource()).setAction(
                new RemoveWMSLayerAction(this.owner, this.caps, this.layer, this.style));
        }
    }

    protected static class RemoveWMSLayerAction extends AbstractAction
    {
        protected WMSLayerPanel owner;
        protected WMSCapabilities caps;
        protected WMSLayerCapabilities layer;
        protected WMSLayerStyle style;

        public RemoveWMSLayerAction(WMSLayerPanel owner, WMSCapabilities caps, WMSLayerCapabilities layer,
            WMSLayerStyle style)
        {
            super(owner.makeWMSLayerDisplayName(layer, style));
            this.putValue(Action.SELECTED_KEY, true);
            this.caps = caps;
            this.owner = owner;
            this.layer = layer;
            this.style = style;
        }

        public void actionPerformed(ActionEvent event)
        {
            this.owner.globeModel.removeWMSLayer(this.owner.record.getIdentifier(), this.layer, this.style);
            ((AbstractButton) event.getSource()).setAction(
                new AddWMSLayerAction(this.owner, this.caps, this.layer, this.style));
        }
    }
}
