/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

import gov.nasa.worldwind.applications.gio.catalogui.treetable.*;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * @author dcollins
 * @version $Id: ResultTreeTable.java 12975 2010-01-05 18:48:32Z dcollins $
 */
public class ResultTreeTable extends TreeTable
{
    private ResultList resultList;
    private AVList params;
    private final ToolTipSupport toolTipSupport = new ToolTipSupport();

    public ResultTreeTable(ResultList resultList, AVList tableParams)
    {
        super(new TreeTableModelAdapter(tableParams));

        this.resultList = resultList;
        this.resultList.addResultListListener(new ResultListener(this));
        this.params = initParams(tableParams);

        setAskIfTreeEditable(true);
        setTreeEditable(false);
        setFocusable(false);
        setCellSelectionEnabled(false);
        getTree().setSelectionModel(null);
        getTree().setLargeModel(true);
        getTree().addTreeExpansionListener(new ExpansionListener(this));
        addMouseMotionListener(new MouseListener(this));

        setRowHeight(24);
        getTree().setRootVisible(false);
        getTree().setShowsRootHandles(true);
        getTree().putClientProperty("JTree.lineStyle", "None");

        // Shorten initial ToolTip delay by 50%.
        int initialDelay = ToolTipManager.sharedInstance().getInitialDelay();
        ToolTipManager.sharedInstance().setInitialDelay(initialDelay / 2);
        // Disable ToolTip dismiss.
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
    }

    public ResultList getResultList()
    {
        return this.resultList;
    }

    public AVList getTableParams()
    {
        return this.params;
    }

    public String getToolTipText(MouseEvent event)
    {
        String text = super.getToolTipText(event);
        if (text == null)
        {
            if (event != null)
            {
                java.awt.Point p = event.getPoint();
                int rowIndex = rowAtPoint(p);
                int colIndex = columnAtPoint(p);
                int realColumnIndex = convertColumnIndexToModel(colIndex);

                Object o = getValueAt(rowIndex, realColumnIndex);
                if (o != null)
                {
                    this.toolTipSupport.clear();
                    this.toolTipSupport.append(o.toString(), Font.BOLD);
                    text = this.toolTipSupport.getText();
                }
            }
        }
        return text;
    }

    protected void mouseMoved(MouseEvent event)
    {
        Cursor cursor = null;
        if (event != null)
        {
            java.awt.Point p = event.getPoint();
            int rowIndex = rowAtPoint(p);
            int colIndex = columnAtPoint(p);
            int realColumnIndex = convertColumnIndexToModel(colIndex);
            if (rowIndex >= 0 && realColumnIndex >= 0)
            {
                TableCellRenderer renderer = getCellRenderer(rowIndex, realColumnIndex);
                if (renderer != null)
                {
                    Object value = getValueAt(rowIndex, realColumnIndex);
                    Component c = renderer.getTableCellRendererComponent(this, value, false, false,
                        rowIndex, realColumnIndex);
                    if (c != null)
                    {
                        cursor = c.getCursor();
                    }
                }
            }
        }
        if (cursor == null)
            cursor = Cursor.getDefaultCursor();
        setCursor(cursor);
    }

    protected void nodeExpanded(TreeTableNode node)
    {
    }

    protected void reloadResultList()
    {
    }

    protected void reloadResultListInSwingThread()
    {
        try
        {
            if (SwingUtilities.isEventDispatchThread())
            {
                reloadResultList();
            }
            else
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        reloadResultList();
                    }
                });
            }
        }
        catch (Exception e)
        {
            String message = "catalog.ExceptionWhileInvokingOnEventDispatchThread";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
    }

    protected void reloadResults(int beginIndex, int endIndex)
    {
        TreeTableModel ttm = getTreeTableModel();
        if (ttm != null)
        {
            if (ttm instanceof AbstractTreeTableModel)
            {
                for (int i = beginIndex; i <= endIndex; i++)
                {
                    TreeTableNode node = ((AbstractTreeTableModel ) ttm).getNodeForValue(this.resultList.get(i));
                    if (node instanceof AbstractTreeTableNode)
                        ((AbstractTreeTableNode) node).update();
                    ((AbstractTreeTableModel ) ttm).reload(node);
                }
            }
        }
    }

    protected void reloadResultsInSwingThread(final int beginIndex, final int endIndex)
    {
        try
        {
            if (SwingUtilities.isEventDispatchThread())
            {
                reloadResults(beginIndex, endIndex);
            }
            else
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        reloadResults(beginIndex, endIndex);
                    }
                });
            }
        }
        catch (Exception e)
        {
            String message = "catalog.ExceptionWhileInvokingOnEventDispatchThread";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
    }

    protected void repaintInSwingThread()
    {
        try
        {
            if (SwingUtilities.isEventDispatchThread())
            {
                repaint();
            }
            else
            {
                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        repaint();
                    }
                });
            }
        }
        catch (Exception e)
        {
            String message = "catalog.ExceptionWhileInvokingOnEventDispatchThread";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
    }

    protected AVList initParams(AVList params)
    {
        if (params == null)
        {
            String message = "nullValue.ParamsIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int columnCount = -1;
        Object o = params.getValue(CatalogKey.TABLE_COLUMN_COUNT);
        if (o != null)
        {
            Integer i = asInteger(o);
            if (i != null)
            {
                params.setValue(CatalogKey.TABLE_COLUMN_COUNT, i);
                columnCount = i;
            }
        }

        o = params.getValue(CatalogKey.TABLE_COLUMN_PROPERTY_KEY);
        if (o != null)
        {
            Object[] array = asPropertyKeys(o, columnCount);
            if (array != null)
                params.setValue(CatalogKey.TABLE_COLUMN_PROPERTY_KEY, array);
        }

        o = params.getValue(CatalogKey.TABLE_COLUMN_CLASS);
        if (o != null)
        {
            Class[] c = asClassArray(o);
            if (c != null)
                params.setValue(CatalogKey.TABLE_COLUMN_CLASS, c);
        }

        o = params.getValue(CatalogKey.TABLE_COLUMN_NAME);
        if (o != null)
        {
            String[] s = asStringArray(o);
            if (s != null)
                params.setValue(CatalogKey.TABLE_COLUMN_NAME, s);
        }

        o = params.getValue(CatalogKey.TABLE_COLUMN_EDITABLE);
        if (o != null)
        {
            Boolean[] b = asBooleanArray(o);
            if (b != null)
                params.setValue(CatalogKey.TABLE_COLUMN_EDITABLE, b);
        }

        o = params.getValue(CatalogKey.TABLE_COLUMN_CELL_EDITOR);
        if (o != null)
        {
            Object[] array = asInstanceArray(o);
            if (array != null)
                params.setValue(CatalogKey.TABLE_COLUMN_CELL_EDITOR, array);
        }

        o = params.getValue(CatalogKey.TABLE_COLUMN_CELL_RENDERER);
        if (o != null)
        {
            Object[] array = asInstanceArray(o);
            if (array != null)
                params.setValue(CatalogKey.TABLE_COLUMN_CELL_RENDERER, array);
        }

        o = params.getValue(CatalogKey.TABLE_COLUMN_HEADER_RENDERER);
        if (o != null)
        {
            Object[] array = asInstanceArray(o);
            if (array != null)
                params.setValue(CatalogKey.TABLE_COLUMN_HEADER_RENDERER, array);
        }

        o = params.getValue(CatalogKey.TABLE_COLUMN_HEADER_VALUE);
        if (o != null)
        {
            String[] s = asStringArray(o);
            if (s != null)
                params.setValue(CatalogKey.TABLE_COLUMN_HEADER_VALUE, s);
        }

        o = params.getValue(CatalogKey.TABLE_COLUMN_MAX_WIDTH);
        if (o != null)
        {
            Integer[] i = asIntegerArray(o);
            if (i != null)
                params.setValue(CatalogKey.TABLE_COLUMN_MAX_WIDTH, i);
        }

        o = params.getValue(CatalogKey.TABLE_COLUMN_MIN_WIDTH);
        if (o != null)
        {
            Integer[] i = asIntegerArray(o);
            if (i != null)
                params.setValue(CatalogKey.TABLE_COLUMN_MIN_WIDTH, i);
        }

        o = params.getValue(CatalogKey.TABLE_COLUMN_PREFERRED_WIDTH);
        if (o != null)
        {
            Integer[] i = asIntegerArray(o);
            if (i != null)
                params.setValue(CatalogKey.TABLE_COLUMN_PREFERRED_WIDTH, i);
        }

        o = params.getValue(CatalogKey.TABLE_COLUMN_RESIZABLE);
        if (o != null)
        {
            Boolean[] b = asBooleanArray(o);
            if (b != null)
                params.setValue(CatalogKey.TABLE_COLUMN_RESIZABLE, b);
        }

        return params;
    }

    public void tableChanged(TableModelEvent e)
    {
        super.tableChanged(e);

        // Columns changed
        if (e == null || e.getFirstRow() == TableModelEvent.HEADER_ROW)
        {
            if (getAutoCreateColumnsFromModel())
                updateColumnsFromModel();
        }
    }

    protected void updateColumnsFromModel()
    {
        TableColumnModel cm = getColumnModel();
        if (cm != null)
            for (int columnIndex = 0; columnIndex < cm.getColumnCount(); columnIndex++)
                updateColumn(cm.getColumn(columnIndex), columnIndex);
    }

    protected void updateColumn(TableColumn tc, int columnIndex)
    {
        Object o = this.params.getValue(CatalogKey.TABLE_COLUMN_CELL_EDITOR);
        if (o != null && o instanceof Object[])
        {
            o = getObjectAt((Object[]) o, columnIndex);
            if (o != null && o instanceof TableCellEditor)
                tc.setCellEditor((TableCellEditor) o);
        }

        o = this.params.getValue(CatalogKey.TABLE_COLUMN_CELL_RENDERER);
        if (o != null && o instanceof Object[])
        {
            o = getObjectAt((Object[]) o, columnIndex);
            if (o != null && o instanceof TableCellRenderer)
               tc.setCellRenderer((TableCellRenderer) o);
        }

        o = this.params.getValue(CatalogKey.TABLE_COLUMN_HEADER_RENDERER);
        if (o != null && o instanceof Object[])
        {
            o = getObjectAt((Object[]) o, columnIndex);
            if (o != null && o instanceof TableCellRenderer)
                tc.setHeaderRenderer((TableCellRenderer) o);
        }

        o = this.params.getValue(CatalogKey.TABLE_COLUMN_HEADER_VALUE);
        if (o != null && o instanceof Object[])
        {
            o = getObjectAt((Object[]) o, columnIndex);
            if (o != null)
                tc.setHeaderValue(o);
        }

        // Intentionally ignoring setIdentifier (not used).

        o = this.params.getValue(CatalogKey.TABLE_COLUMN_MAX_WIDTH);
        if (o != null && o instanceof Object[])
        {
            o = getObjectAt((Object[]) o, columnIndex);
            if (o != null && o instanceof Integer)
                tc.setMaxWidth((Integer) o);
        }

        o = this.params.getValue(CatalogKey.TABLE_COLUMN_MIN_WIDTH);
        if (o != null && o instanceof Object[])
        {
            o = getObjectAt((Object[]) o, columnIndex);
            if (o != null && o instanceof Integer)
                tc.setMinWidth((Integer) o);
        }

        // Intentionally ignoring setModelIndex (not used).

        o = this.params.getValue(CatalogKey.TABLE_COLUMN_PREFERRED_WIDTH);
        if (o != null && o instanceof Object[])
        {
            o = getObjectAt((Object[]) o, columnIndex);
            if (o != null && o instanceof Integer)
                tc.setPreferredWidth((Integer) o);
        }

        // Intentionally ignoring setWidth (in favor of preferredWidth).

        o = this.params.getValue(CatalogKey.TABLE_COLUMN_RESIZABLE);
        if (o != null && o instanceof Object[])
        {
            o = getObjectAt((Object[]) o, columnIndex);
            if (o != null && o instanceof Boolean)
                tc.setResizable((Boolean) o);
        }
    }

    protected static class ResultListener implements ResultListListener
    {
        private ResultTreeTable treeTable;

        public ResultListener(ResultTreeTable treeTable)
        {
            this.treeTable = treeTable;
        }

        public void listChanged(ResultListEvent e)
        {
            if (e != null)
            {
                int type = e.getType();
                if (type == ResultListEvent.ADD || type == ResultListEvent.REMOVE)
                {
                    this.treeTable.reloadResultListInSwingThread();
                }
                else if (e.getType() == ResultListEvent.UPDATE && e.getStartIndex() != -1 && e.getEndIndex() != -1)
                {
                    this.treeTable.reloadResultsInSwingThread(e.getStartIndex(), e.getEndIndex());
                }
                else // type == ResultListEvent.PASSIVE_UPDATE
                {
                    this.treeTable.repaintInSwingThread();
                }
            }
        }
    }

    protected static class ExpansionListener implements TreeExpansionListener
    {
        private ResultTreeTable treeTable;

        public ExpansionListener(ResultTreeTable treeTable)
        {
            this.treeTable = treeTable;
        }

        public void treeExpanded(TreeExpansionEvent event)
        {
            if (event != null && event.getPath() != null)
            {
                Object node = event.getPath().getLastPathComponent();
                if (node != null && node instanceof TreeTableNode)
                {
                    if (this.treeTable != null)
                    {
                        this.treeTable.nodeExpanded((TreeTableNode) node);
                    }
                }
            }
        }

        public void treeCollapsed(TreeExpansionEvent event)
        {
        }
    }

    protected static class MouseListener implements MouseMotionListener
    {
        private ResultTreeTable treeTable;

        public MouseListener(ResultTreeTable treeTable)
        {
            this.treeTable = treeTable;
        }

        public void mouseDragged(MouseEvent event)
        {
        }

        public void mouseMoved(MouseEvent event)
        {
            if (event != null)
                if (this.treeTable != null)
                    this.treeTable.mouseMoved(event);
        }
    }

    protected static Boolean asBoolean(Object o)
    {
        if (o == null)
            return null;

        if (o instanceof Boolean)
            return (Boolean) o;

        String s = o.toString();
        if (s == null)
            return null;

        return Boolean.parseBoolean(s.trim());
    }

    protected static Integer asInteger(Object o)
    {
        if (o == null)
            return null;

        if (o instanceof Number)
            return ((Number) o).intValue();

        String s = o.toString();
        if (s == null)
            return null;

        try
        {
            return Integer.parseInt(s.trim());
        }
        catch (Exception e)
        {
            String message = "catalog.CannotConvertToInteger " + s;
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
        return null;
    }

    protected static Boolean[] asBooleanArray(Object o)
    {
        if (o == null)
            return null;

        if (o instanceof Boolean[])
            return (Boolean[]) o;

        String[] s = asStringArray(o);
        if (s == null)
            return null;

        Boolean[] array = new Boolean[s.length];
        for (int i = 0; i < s.length; i++)
            array[i] = asBoolean(s[i]);
        return array;
    }

    protected static Integer[] asIntegerArray(Object o)
    {
        if (o == null)
            return null;

        if (o instanceof Integer[])
            return (Integer[]) o;

        String[] s = asStringArray(o);
        if (s == null)
            return null;

        Integer[] array = new Integer[s.length];
        for (int i = 0; i < s.length; i++)
            array[i] = asInteger(s[i]);
        return array;
    }

    protected static String[] asStringArray(Object o)
    {
        if (o == null)
            return null;

        if (o instanceof String[])
            return (String[]) o;

        String s = o.toString();
        if (s == null)
            return null;

        return s.split(",");
    }

    protected static Class[] asClassArray(Object o)
    {
        if (o == null)
            return null;

        if (o instanceof Class[])
            return (Class[]) o;

        String[] s = asStringArray(o);
        if (s == null)
            return null;

        Class[] array = new Class[s.length];
        for (int i = 0; i < s.length; i++)
            array[i] = getClassForName(s[i]);
        return array;
    }

    protected static Object[] asInstanceArray(Object o)
    {
        if (o == null)
            return null;

        if (o instanceof Object[])
            return (Object[]) o;

        String[] s = asStringArray(o);
        if (s == null)
            return null;

        Object[] array = new Object[s.length];
        for (int i = 0; i < s.length; i++)
        {
            Class<?> cls = getClassForName(s[i]);
            if (cls != null)
                array[i] = newInstanceOf(cls);
        }
        return array;
    }

    protected static Object[] asPropertyKeys(Object o, int columnCount)
    {
        if (o == null)
            return null;

        if (o instanceof Object[])
            return (Object[]) o;

        String[] s = asStringArray(o);
        if (s == null)
            return null;

        Object[] array = new Object[s.length];
        for (int i = 0; i < s.length; i++)
        {
            array[i] = null;
            if (columnCount > 0 && (i % (1 + columnCount)) == 0)
                array[i] = getClassForName(s[i]);
            if (array[i] == null)
                array[i] = s[i];
        }
        return array;
    }

    protected static Class<?> getClassForName(String className)
    {
        Class<?> cls = null;
        try
        {
            if (className != null && className.length() > 0)
                cls = Class.forName(className.trim());
        }
        catch (Exception e)
        {
            String message = "catalog.CannotCreateClassForName " + className;
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
        return cls;
    }

    protected static Object newInstanceOf(Class<?> cls)
    {
        Object obj = null;
        try
        {
            if (cls != null)
                obj = cls.newInstance();
        }
        catch (Exception e)
        {
            String message = "catalog.CannotCreateInstanceOfClass " + cls;
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
        return obj;
    }

    protected Object getObjectAt(Object[] array, int index)
    {
        if (array == null)
            return null;

        if (index < 0 || index >= array.length)
            return null;

        return array[index];
    }
}
