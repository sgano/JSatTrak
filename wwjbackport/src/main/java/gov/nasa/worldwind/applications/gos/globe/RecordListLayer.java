/* Copyright (C) 2001, 2009 United States Government as represented by 
the Administrator of the National Aeronautics and Space Administration. 
All Rights Reserved. 
*/
package gov.nasa.worldwind.applications.gos.globe;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.applications.gos.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import javax.swing.event.*;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author dcollins
 * @version $Id: RecordListLayer.java 13585 2010-07-27 03:18:16Z dcollins $
 */
public class RecordListLayer extends AbstractLayer implements SelectListener
{
    protected boolean showRecordAnnotations = true;
    protected boolean showRecordBounds = true;
    protected RecordList recordList;
    protected AnnotationRenderer annotationRenderer = new BasicAnnotationRenderer();
    protected final ArrayList<Annotation> annotations = new ArrayList<Annotation>();
    protected final ArrayList<SurfaceObject> surfaceObjects = new ArrayList<SurfaceObject>();
    protected final EventListenerList eventListeners = new EventListenerList();

    public RecordListLayer()
    {
        String s = Configuration.getStringValue(GeodataKey.RECORD_LIST_LAYER_NAME);
        if (!WWUtil.isEmpty(s))
            this.setName(s);
    }

    public boolean isShowRecordAnnotations()
    {
        return this.showRecordAnnotations;
    }

    public void setShowRecordAnnotations(boolean show)
    {
        this.showRecordAnnotations = show;
        this.onLayerChanged();
    }

    public boolean isShowRecordBounds()
    {
        return this.showRecordBounds;
    }

    public void setShowRecordBounds(boolean show)
    {
        this.showRecordBounds = show;
        this.onLayerChanged();
    }

    public RecordList getRecordList()
    {
        return this.recordList;
    }

    public void setRecordList(RecordList recordList)
    {
        this.recordList = recordList;
        this.onLayerChanged();
    }

    public void selected(SelectEvent event)
    {
        if (event.getSource() instanceof Component)
            ((Component) event.getSource()).setCursor(Cursor.getDefaultCursor());

        if (!event.hasObjects() || event.getTopPickedObject().getParentLayer() != this)
            return;

        this.doSelected(event);
    }

    //**************************************************************//
    //********************  Rendering  *****************************//
    //**************************************************************//

    @Override
    protected void doPreRender(DrawContext dc)
    {
        for (SurfaceObject so : this.surfaceObjects)
        {
            so.preRender(dc);
        }
    }

    @Override
    protected void doPick(DrawContext dc, Point point)
    {
        this.annotationRenderer.pick(dc, this.annotations, point, this);
    }

    protected void doRender(DrawContext dc)
    {
        for (SurfaceObject so : this.surfaceObjects)
        {
            so.render(dc);
        }

        this.annotationRenderer.render(dc, this.annotations, this);
    }

    //**************************************************************//
    //********************  Renderable Assembly  *******************//
    //**************************************************************//

    protected void onLayerChanged()
    {
        this.assembleRenderables();
        this.firePropertyChange(AVKey.LAYER, null, this);
    }

    protected void assembleRenderables()
    {
        this.annotations.clear();
        this.surfaceObjects.clear();

        if (this.getRecordList() == null || this.getRecordList().getRecords() == null)
            return;

        for (Record record : this.getRecordList().getRecords())
        {
            if (record == null)
                continue;

            this.addRecordRenderables(record);
        }
    }

    protected void addRecordRenderables(Record record)
    {
        if (this.isShowRecordAnnotations())
            this.addRecordAnnotation(record);

        if (this.isShowRecordBounds())
            this.addRecordBounds(record);
    }

    protected void addRecordAnnotation(Record record)
    {
        if (record == null || record.getSector() == null)
            return;

        this.annotations.add(new RecordAnnotation(record));
    }

    protected void addRecordBounds(Record record)
    {
        if (record == null || record.getSector() == null)
            return;

        this.surfaceObjects.add(new RecordSurfaceShape(record));
    }

    //**************************************************************//
    //********************  Selection  *****************************//
    //**************************************************************//

    protected void doSelected(SelectEvent event)
    {
        String s = event.getTopPickedObject().getStringValue(AVKey.URL);
        if (WWUtil.isEmpty(s))
            return;

        URL url = WWIO.makeURL(s);
        if (url == null)
            return;

        if (event.getSource() instanceof Component)
        {
            ((Component) event.getSource()).setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
        {
            try
            {
                BrowserOpener.browse(url);
            }
            catch (Exception e)
            {
                Logging.logger().log(java.util.logging.Level.SEVERE,
                    Logging.getMessage("generic.ExceptionAttemptingToInvokeWebBrower", url.toString()), e);
            }
        }
    }
}
