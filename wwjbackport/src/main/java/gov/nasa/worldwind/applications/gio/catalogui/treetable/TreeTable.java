/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui.treetable;

import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;

/**
 * @author dcollins
 * @version $Id$
 */
public class TreeTable extends JTable
{
    private TreeTableModel treeTableModel;
    protected TreeTableCellRenderer tree;
    private boolean askIfTreeEditable;
    private boolean isTreeEditable;

    public TreeTable(TreeTableModel model)
    {
        if (model == null)
        {
            String message = "nullValue.ModelIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.treeTableModel = model;
        this.tree = new TreeTableCellRenderer(new TreeModelAdapter(this.treeTableModel));
        super.setModel(new TableModelAdapter(this.treeTableModel, this.tree));
        ToolTipManager.sharedInstance().registerComponent(this.tree);

        ListToTreeSelectionModelWrapper selectionWrapper = new ListToTreeSelectionModelWrapper();
        this.tree.setSelectionModel(selectionWrapper);
        setSelectionModel(selectionWrapper.getListSelectionModel());

        setDefaultRenderer(TreeTableModel.class, this.tree);
        setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());

        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));

        if (this.tree.getRowHeight() < 1)
        {
            setRowHeight(20);
        }
    }

    public TreeTableModel getTreeTableModel()
    {
        return this.treeTableModel;
    }

    public JTree getTree()
    {
        return this.tree;
    }

    public TreeCellRenderer getTreeCellRenderer()
    {
        return this.tree.getCellRenderer();
    }

    public void setTreeCellRenderer(TreeCellRenderer x)
    {
        this.tree.setCellRenderer(x);
    }

    public boolean isAskIfTreeEditable()
    {
        return this.askIfTreeEditable;
    }

    public void setAskIfTreeEditable(boolean askIfTreeEditable)
    {
        this.askIfTreeEditable = askIfTreeEditable;
    }

    public boolean isTreeEditable()
    {
        return this.isTreeEditable;
    }

    public void setTreeEditable(boolean treeEditable)
    {
        this.isTreeEditable = treeEditable;
    }

    public int getEditingRow()
    {
        return getColumnClass(this.editingColumn) == TreeTableModel.class ? -1 : this.editingRow;
    }

    protected int realEditingRow()
    {
        return this.editingRow;
    }

    public boolean editCellAt(int row, int column, EventObject e)
    {
        boolean retValue = super.editCellAt(row, column, e);
        if (retValue && getColumnClass(column) == TreeTableModel.class)
        {
            repaint(getCellRect(row, column, false));
        }
        return retValue;
    }

    public void sizeColumnsToFit(int resizingColumn)
    {
        super.sizeColumnsToFit(resizingColumn);
        if (getEditingColumn() != -1 && getColumnClass(this.editingColumn) == TreeTableModel.class)
        {
            Rectangle cellRect = getCellRect(realEditingRow(), getEditingColumn(), false);
            Component component = getEditorComponent();
            component.setBounds(cellRect);
            component.validate();
        }
    }

    public void setRowHeight(int rowHeight)
    {
        super.setRowHeight(rowHeight);
        if (this.tree != null && this.tree.getRowHeight() != rowHeight)
        {
            this.tree.setRowHeight(getRowHeight());
        }
    }

    public void updateUI()
    {
        super.updateUI();
        if (this.tree != null)
        {
            this.tree.updateUI();
            //setDefaultEditor(TreeTableModel.class, new TreeTableCellEditor());
        }
        LookAndFeel.installColorsAndFont(this, "Tree.background", "Tree.foreground", "Tree.font");
    }

    class TreeTableCellRenderer extends JTree implements TableCellRenderer
    {
        protected int visibleRow;
        protected Object realCellValue;
        protected Border highlightBorder;

        public TreeTableCellRenderer(TreeModel model)
        {
            super(model);
        }

        public String convertValueToText(Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
        {
            String text = null;
            if (this.realCellValue != null)
                text = this.realCellValue.toString();
            if (text == null)
                text = super.convertValueToText(value, selected, expanded, leaf, row, hasFocus);
            return text;
        }

        public String getToolTipText(MouseEvent event)
        {
            event.translatePoint(0, this.visibleRow * getRowHeight());
            return super.getToolTipText(event);
        }

        public void setRowHeight(int rowHeight)
        {
            if (rowHeight > 0)
            {
                super.setRowHeight(rowHeight);
                if (TreeTable.this.getRowHeight() != rowHeight)
                {
                    TreeTable.this.setRowHeight(getRowHeight());
                }
            }
        }

        public void setBounds(int x, int y, int w, int h)
        {
            super.setBounds(x, 0, w, TreeTable.this.getHeight());
        }

        public void paint(Graphics g)
        {
            if (g == null)
            {
                String message = "nullValue.GraphicsIsNull";
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            g.translate(0, -this.visibleRow * getRowHeight());
            super.paint(g);
            if (this.highlightBorder != null)
            {
                this.highlightBorder.paintBorder(this, g, 0, visibleRow * getRowHeight(), getWidth(), getRowHeight());
            }
        }

        public void updateUI()
        {
            super.updateUI();
            TreeCellRenderer tcr = getCellRenderer();
            if (tcr instanceof DefaultTreeCellRenderer)
            {
                DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer) tcr);
                dtcr.setTextSelectionColor(UIManager.getColor("Table.selectionForeground"));
                dtcr.setBackgroundSelectionColor(UIManager.getColor("Table.selectionBackground"));
            }
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                       int row, int column)
        {
            if (table == null)
            {
                String message = "nullValue.TableIsNull";
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.realCellValue = value;

            Color background;
            Color foreground;
            if (isSelected)
            {
                background = table.getSelectionBackground();
                foreground = table.getSelectionForeground();
            }
            else
            {
                background = table.getBackground();
                foreground = table.getForeground();
            }

            this.highlightBorder = null;
            if (TreeTable.this.realEditingRow() == row && TreeTable.this.getEditingColumn() == column)
            {
                background = UIManager.getColor("Table.focusCellBackground");
                foreground = UIManager.getColor("Table.focusCellForeground");
            }
            else if (hasFocus)
            {
                this.highlightBorder = UIManager.getBorder("Table.focusCellHighlightBorder");
                if (TreeTable.this.isCellEditable(row, column))
                {
                    background = UIManager.getColor("Table.focusCellBackground");
                    foreground = UIManager.getColor("Table.focusCellForeground");
                }
            }

            this.visibleRow = row;
            setBackground(background);

            TreeCellRenderer tcr = getCellRenderer();
            if (tcr instanceof DefaultTreeCellRenderer)
            {
                DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer) tcr);
                if (isSelected)
                {
                    dtcr.setTextSelectionColor(foreground);
                    dtcr.setBackgroundSelectionColor(background);
                }
                else
                {
                    dtcr.setTextNonSelectionColor(foreground);
                    dtcr.setBackgroundNonSelectionColor(background);
                }
            }

            return this;
        }
    }

    class TreeTableCellEditor extends DefaultCellEditor
    {
        public TreeTableCellEditor()
        {
            super(new TreeTableTextField());
        }

        /**
         * Overridden to determine an offset that tree would place the
         * editor at. The offset is determined from the
         * <code>getRowBounds</code> JTree method, and additionally
         * from the icon DefaultTreeCellRenderer will use.
         * <p>The offset is then set on the TreeTableTextField component
         * created in the constructor, and returned.
         */
        public Component getTableCellEditorComponent(JTable table,
                                                     Object value,
                                                     boolean isSelected,
                                                     int r, int c)
        {
            Component component = super.getTableCellEditorComponent(table, value, isSelected, r, c);
            JTree t = TreeTable.this.getTree();
            //boolean rv = t.isRootVisible();
            //int offsetRow = rv ? r : r - 1;
            //noinspection UnnecessaryLocalVariable
            int offsetRow = r;
            Rectangle bounds = t.getRowBounds(offsetRow);
            int offset = bounds.x;
            TreeCellRenderer tcr = t.getCellRenderer();
            if (tcr instanceof DefaultTreeCellRenderer)
            {
                Object node = t.getPathForRow(offsetRow).
                        getLastPathComponent();
                Icon icon;
                if (t.getModel().isLeaf(node))
                    icon = ((DefaultTreeCellRenderer) tcr).getLeafIcon();
                else if (TreeTable.this.getTree().isExpanded(offsetRow))
                    icon = ((DefaultTreeCellRenderer) tcr).getOpenIcon();
                else
                    icon = ((DefaultTreeCellRenderer) tcr).getClosedIcon();
                if (icon != null)
                {
                    offset += ((DefaultTreeCellRenderer) tcr).getIconTextGap() +
                            icon.getIconWidth();
                }
            }
            ((TreeTableTextField) getComponent()).offset = offset;
            return component;
        }

        /**
         * This is overridden to forward the event to the tree. This will
         * return true if the click count >= 3, or the event is null.
         */
        public boolean isCellEditable(EventObject e)
        {
            if (e instanceof MouseEvent)
            {
                MouseEvent me = (MouseEvent) e;
                // If the modifiers are not 0 (or the left mouse button),
                // tree may try and toggle the selection, and table
                // will then try and toggle, resulting in the
                // selection remaining the same. To avoid this, we
                // only dispatch when the modifiers are 0 (or the left mouse
                // button).
                if (me.getModifiers() == 0 || me.getModifiers() == InputEvent.BUTTON1_MASK)
                {
                    for (int column = TreeTable.this.getColumnCount() - 1; column >= 0; column--)
                    {
                        if (TreeTable.this.getColumnClass(column) == TreeTableModel.class)
                        {
                            MouseEvent newME = new MouseEvent(
                                TreeTable.this.getTree(), me.getID(),
                                me.getWhen(), me.getModifiers(),
                                me.getX() - TreeTable.this.getCellRect(0, column, true).x,
                                me.getY(), me.getClickCount(),
                                me.isPopupTrigger());
                            TreeTable.this.getTree().dispatchEvent(newME);
                            newME = new MouseEvent(
                                TreeTable.this.getTree(), MouseEvent.MOUSE_RELEASED,
                                me.getWhen(), me.getModifiers(),
                                me.getX() - TreeTable.this.getCellRect(0, column, true).x,
                                me.getY(), me.getClickCount(),
                                me.isPopupTrigger());
                            TreeTable.this.getTree().dispatchEvent(newME);
                            break;
                        }
                    }
                }
                //noinspection RedundantIfStatement
                if (me.getClickCount() >= 3)
                {
                    //noinspection SimplifiableIfStatement
                    if (TreeTable.this.isAskIfTreeEditable())
                        return TreeTable.this.isTreeEditable();
                    return true;
                }
                return false;
            }
            //noinspection RedundantIfStatement
            if (e == null)
            {
                return true;
            }
            return false;
        }
    }

    /**
     * Component used by TreeTableCellEditor. The only thing this does
     * is to override the <code>reshape</code> method, and to ALWAYS
     * make the x location be <code>offset</code>.
     */
    private static class TreeTableTextField extends JTextField
    {
        public int offset;

        public void setBounds(int x, int y, int w, int h)
        {
            int newX = Math.max(x, offset);
            super.setBounds(newX, y, w - (newX - x), h);
        }
    }

    /**
     * ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
     * to listen for changes in the ListSelectionModel it maintains. Once
     * a change in the ListSelectionModel happens, the paths are updated
     * in the DefaultTreeSelectionModel.
     */
    class ListToTreeSelectionModelWrapper extends DefaultTreeSelectionModel
    {
        /**
         * Set to true when we are updating the ListSelectionModel.
         */
        protected boolean updatingListSelectionModel;

        public ListToTreeSelectionModelWrapper()
        {
            super();
            getListSelectionModel().addListSelectionListener
                    (createListSelectionListener());
        }

        /**
         * Returns the list selection model. ListToTreeSelectionModelWrapper
         * listens for changes to this model and updates the selected paths
         * accordingly.
         * @return the list selection model
         */
        ListSelectionModel getListSelectionModel()
        {
            return listSelectionModel;
        }

        /**
         * This is overridden to set <code>updatingListSelectionModel</code>
         * and message super. This is the only place DefaultTreeSelectionModel
         * alters the ListSelectionModel.
         */
        public void resetRowSelection()
        {
            if (!updatingListSelectionModel)
            {
                updatingListSelectionModel = true;
                try
                {
                    super.resetRowSelection();
                }
                finally
                {
                    updatingListSelectionModel = false;
                }
            }
            // Notice how we don't message super if
            // updatingListSelectionModel is true. If
            // updatingListSelectionModel is true, it implies the
            // ListSelectionModel has already been updated and the
            // paths are the only thing that needs to be updated.
        }

        /**
         * Creates and returns an instance of ListSelectionHandler.
         * @return an instance of ListSelectionHandler
         */
        protected ListSelectionListener createListSelectionListener()
        {
            return new ListSelectionHandler();
        }

        /**
         * If <code>updatingListSelectionModel</code> is false, this will
         * reset the selected paths from the selected rows in the list
         * selection model.
         */
        protected void updateSelectedPathsFromSelectedRows()
        {
            if (!updatingListSelectionModel)
            {
                updatingListSelectionModel = true;
                try
                {
                    // This is way expensive, ListSelectionModel needs an
                    // enumerator for iterating.
                    int min = listSelectionModel.getMinSelectionIndex();
                    int max = listSelectionModel.getMaxSelectionIndex();

                    clearSelection();
                    if (min != -1 && max != -1)
                    {
                        for (int counter = min; counter <= max; counter++)
                        {
                            if (listSelectionModel.isSelectedIndex(counter))
                            {
                                TreePath selPath = tree.getPathForRow
                                        (counter);

                                if (selPath != null)
                                {
                                    addSelectionPath(selPath);
                                }
                            }
                        }
                    }
                }
                finally
                {
                    updatingListSelectionModel = false;
                }
            }
        }

        /**
         * Class responsible for calling updateSelectedPathsFromSelectedRows
         * when the selection of the list changse.
         */
        class ListSelectionHandler implements ListSelectionListener
        {
            public void valueChanged(ListSelectionEvent e)
            {
                updateSelectedPathsFromSelectedRows();
            }
        }
    }
}
