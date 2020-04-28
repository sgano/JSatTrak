/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

import gov.nasa.worldwind.applications.gio.catalogui.treetable.AbstractTreeTableModel;
import gov.nasa.worldwind.applications.gio.catalogui.treetable.TreeTable;
import gov.nasa.worldwind.applications.gio.catalogui.treetable.TreeTableModel;
import gov.nasa.worldwind.applications.gio.catalogui.treetable.TreeTableNode;
import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author dcollins
 * @version $Id: ResultTreeCellRenderer.java 5552 2008-07-19 01:38:46Z dcollins $
 */
public class ResultTreeCellRenderer extends DefaultTreeCellRenderer
{
    private String serviceGenericIconPath;
    private String serviceWCSIconPath;
    private String serviceWFSIconPath;
    private String serviceWMSIconPath;
    private String serviceErrorIconPath;
    private String waitingIconPath;
    private TreeTable treeTable;
    //private TreeCellRenderer defaultRenderer;
    private final Map<String, NodeIcon> iconMap = new HashMap<String, NodeIcon>();
    private final ToolTipSupport toolTipSupport = new ToolTipSupport();
    private static final String DEFAULT_SERVICE_GENERIC_ICON_PATH = "images/service-unknown.gif";
    private static final String DEFAULT_SERVICE_WCS_ICON_PATH = "images/service-wcs.gif";
    private static final String DEFAULT_SERVICE_WFS_ICON_PATH = "images/service-wfs.gif";
    private static final String DEFAULT_SERVICE_WMS_ICON_PATH = "images/service-wms.gif";
    private static final String DEFAULT_SERVICE_ERROR_ICON_PATH = "images/service-error.gif";
    private static final String DEFAULT_WAITING_ICON_PATH = "images/indicator-16.gif";

    public ResultTreeCellRenderer(TreeTable treeTable)
    {
        if (treeTable == null)
        {
            String message = "nullValue.TreeTableIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.serviceGenericIconPath = DEFAULT_SERVICE_GENERIC_ICON_PATH;
        this.serviceWCSIconPath = DEFAULT_SERVICE_WCS_ICON_PATH;
        this.serviceWFSIconPath = DEFAULT_SERVICE_WFS_ICON_PATH;
        this.serviceWMSIconPath = DEFAULT_SERVICE_WMS_ICON_PATH;
        this.serviceErrorIconPath = DEFAULT_SERVICE_ERROR_ICON_PATH;
        this.waitingIconPath = DEFAULT_WAITING_ICON_PATH;
        
        this.treeTable = treeTable;
        //this.defaultRenderer = treeTable.getTreeCellRenderer();
        //if (this.defaultRenderer != null && this.defaultRenderer instanceof DefaultTreeCellRenderer)
        //    init((DefaultTreeCellRenderer) this.defaultRenderer);
        init(this);
    }

    public String getServiceGenericIconPath()
    {
        return this.serviceGenericIconPath;
    }

    public void setServiceGenericIconPath(String serviceGenericIconPath)
    {
        this.serviceGenericIconPath = serviceGenericIconPath;
    }

    public String getServiceWCSIconPath()
    {
        return this.serviceWCSIconPath;
    }

    public void setServiceWCSIconPath(String serviceWCSIconPath)
    {
        this.serviceWCSIconPath = serviceWCSIconPath;
    }

    public String getServiceWFSIconPath()
    {
        return this.serviceWFSIconPath;
    }

    public void setServiceWFSIconPath(String serviceWFSIconPath)
    {
        this.serviceWFSIconPath = serviceWFSIconPath;
    }

    public String getServiceWMSIconPath()
    {
        return this.serviceWMSIconPath;
    }

    public void setServiceWMSIconPath(String serviceWMSIconPath)
    {
        this.serviceWMSIconPath = serviceWMSIconPath;
    }

    public String getServiceErrorIconPath()
    {
        return this.serviceErrorIconPath;
    }

    public void setServiceErrorIconPath(String serviceErrorIconPath)
    {
        this.serviceErrorIconPath = serviceErrorIconPath;
    }

    public String getWaitingIconPath()
    {
        return this.waitingIconPath;
    }

    public void setWaitingIconPath(String waitingIconPath)
    {
        this.waitingIconPath = waitingIconPath;
    }

    private void init(DefaultTreeCellRenderer renderer)
    {
        if (renderer != null)
        {
            renderer.setClosedIcon(null);
            renderer.setOpenIcon(null);
            renderer.setLeafIcon(null);
        }
    }

    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus)
    {
        Component c;
        // Attempt to use the Tree's default cell renderer, which may be a look-and-feel specific implementation.
        //if (this.defaultRenderer != null)
        //    c = this.defaultRenderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        // Fallback to DefaultCellRenderer implementation.
        //if (c == null)
            c = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

        if (value != null)
        {
            updateIcon(c, value);

            synchronized (this.toolTipSupport)
            {
                updateToolTip(c, value);
            }
        }


        return c;
    }

    protected void updateIcon(Component c, Object value)
    {
        Icon icon = null;

        if (value != null && value instanceof AVListNode)
        {
            AVListNode node = (AVListNode) value;
            String iconPath = null;

            if (node.getObject().hasKey(CatalogKey.SERVICE_TYPE))
            {
                iconPath = this.serviceGenericIconPath;

                Object o = node.getValue(CatalogKey.SERVICE_TYPE);
                if (o != null)
                {
                    if (o.equals(CatalogKey.WCS))
                        iconPath = this.serviceWCSIconPath;
                    else if (o.equals(CatalogKey.WFS))
                        iconPath = this.serviceWFSIconPath;
                    else if (o.equals(CatalogKey.WMS))
                        iconPath = this.serviceWMSIconPath;
                    else if (o.equals(CatalogKey.ERROR))
                        iconPath = this.serviceErrorIconPath;
                        
                }

                o = node.getValue(CatalogKey.EXCEPTIONS);
                if (o != null)
                    iconPath = this.serviceErrorIconPath;
            }

            // Result is waiting on some action.
            // Waiting state overrides any node specific state.
            Object o = node.getValue(CatalogKey.WAITING);
            if (o != null && o.equals(Boolean.TRUE))
                iconPath = this.waitingIconPath;

            if (iconPath != null)
                icon = getIconFromPath(iconPath, node);
        }

        if (c != null && c instanceof JLabel)
            ((JLabel) c).setIcon(icon);
    }

    protected void updateToolTip(Component c, Object value)
    {
        this.toolTipSupport.clear();

        if (value != null && value instanceof AVListNode)
        {
            AVListNode node = (AVListNode) value;

            Object o = node.getValue(CatalogKey.TITLE);
            if (o != null)
                this.toolTipSupport.append(o.toString(), Font.BOLD);

            o = node.getValue(CatalogKey.SERVICE_TYPE);
            if (o != null)
            {
                String serviceDescription = null;
                if (o.equals(CatalogKey.WCS))
                    serviceDescription = "Web Coverage Service";
                else if (o.equals(CatalogKey.WFS))
                    serviceDescription = "Web Feature Service";
                else if (o.equals(CatalogKey.WMS))
                    serviceDescription = "Web Map Service";

                if (serviceDescription != null)
                {
                    if (this.toolTipSupport.hasText())
                        this.toolTipSupport.appendLine();
                    this.toolTipSupport.append(serviceDescription, Font.ITALIC);
                }
            }

            o = node.getValue(CatalogKey.URI);
            if (o != null)
            {
                if (this.toolTipSupport.hasText())
                    this.toolTipSupport.appendLine();
                this.toolTipSupport.append(o.toString(), Font.BOLD);
            }

            o = node.getValue(CatalogKey.DESCRIPTION);
            if (o != null)
            {
                if (this.toolTipSupport.hasText())
                    this.toolTipSupport.appendParagraph();
                this.toolTipSupport.appendWrapped(o.toString());
            }

            o = node.getValue(CatalogKey.EXCEPTIONS);
            if (o != null && o instanceof CatalogExceptionList)
            {
                if (this.toolTipSupport.hasText())
                    this.toolTipSupport.appendParagraph();
                CatalogExceptionList list = (CatalogExceptionList) o;
                this.toolTipSupport.append(list);
            }
        }

        if (c != null && c instanceof JComponent)
            ((JComponent) c).setToolTipText(this.toolTipSupport.getText());
    }

    private Icon getIconFromPath(String iconPath, TreeTableNode treeNode)
    {
        // Remove the TreeNode from all TreeNodeIcons.
        for (NodeIcon o : this.iconMap.values())
            o.removeNode(treeNode);
        // If the icon is null, and the key does not intentionally map to null,
        // then load the icon.
        NodeIcon icon = this.iconMap.get(iconPath);
        if (icon == null && !this.iconMap.containsKey(iconPath))
        {
            ImageIcon imageIcon = SwingUtils.readImageIcon(iconPath, getClass());
            if (imageIcon != null)
            {
                icon = new NodeIcon(imageIcon, this.treeTable);
                this.iconMap.put(iconPath, icon);
            }
        }
        if (icon != null)
            icon.addNode(treeNode);
        return icon != null ? icon.getIcon() : null;
    }

    private static class NodeIcon implements ImageObserver
    {
        private ImageIcon icon;
        private TreeTable treeTable;
        private java.util.List<TreeTableNode> nodes = new CopyOnWriteArrayList<TreeTableNode>();

        private NodeIcon(ImageIcon icon, TreeTable treeTable)
        {
            if (icon == null)
            {
                String message = "nullValue.IconIsNull";
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (treeTable == null)
            {
                String message = "nullValue.TreeTableIsNull";
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.icon = icon;
            this.treeTable = treeTable;
            this.icon.setImageObserver(this);
        }

        public ImageIcon getIcon()
        {
            return this.icon;
        }

        public JTree getTree()
        {
            return this.treeTable.getTree();
        }

        public void addNode(TreeTableNode node)
        {
            if (node == null)
            {
                String message = "nullValue.NodeIsNull";
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.nodes.add(node);
        }

        public void removeNode(TreeTableNode node)
        {
            if (node == null)
            {
                String message = "nullValue.NodeIsNull";
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.nodes.remove(node);
        }

        public Iterable<TreeTableNode> getNodes()
        {
            return this.nodes;
        }

        public void clearNodes()
        {
            this.nodes.clear();
        }

        public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
        {
            if ((infoflags & (FRAMEBITS | ALLBITS)) != 0)
            {
                try
                {
                    repaintNodes();
                }
                catch (Throwable t)
                {
                    String message = "catalog.ExceptionDuringImageUpdate";
                    Logging.logger().log(java.util.logging.Level.SEVERE, message, t);
                }
            }
            return (infoflags & (ALLBITS | ABORT)) == 0;
        }

        private void repaintNodes()
        {
            TreeTableModel model = this.treeTable.getTreeTableModel();
            // Repaint each affected tree node individually.
            if (model != null && model instanceof AbstractTreeTableModel)
            {
                for (TreeTableNode node : this.nodes)
                {
                    TreeTableNode[] path = ((AbstractTreeTableModel) model).getPathToRoot(node);
                    TreePath treePath = new TreePath(path);
                    Rectangle rect = getTree().getPathBounds(treePath);
                    if (rect != null)
                    {
                        this.treeTable.repaint(rect);
                    }
                }
            }
            // Repaint the entire tree.
            else
            {
                getTree().repaint();
            }
        }
    }
}
