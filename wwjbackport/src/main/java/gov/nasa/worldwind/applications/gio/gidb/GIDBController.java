/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.gidb;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.applications.gio.GIDBCatalogPanel;
import gov.nasa.worldwind.applications.gio.catalogui.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.util.*;

import javax.swing.*;
import java.beans.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dcollins
 * @version $Id: GIDBController.java 13375 2010-05-05 23:43:39Z dcollins $
 */
public class GIDBController implements PropertyChangeListener
{
    private GIDBCatalogPanel gidbPanel;
    private int threadPoolSize;
    private ExecutorService executor;
    private final AtomicInteger numResultActors = new AtomicInteger(0);
    private final Map<Object, gov.nasa.worldwind.layers.Layer> wwLayerMap =
        new HashMap<Object, gov.nasa.worldwind.layers.Layer>();
    private static final int DEFAULT_THREAD_POOL_SIZE = 3;

    public GIDBController(GIDBCatalogPanel gidbPanel)
    {
        if (gidbPanel == null)
        {
            String message = "nullValue.GIDBPanelIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.gidbPanel = gidbPanel;
        this.threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
        this.executor = Executors.newFixedThreadPool(this.threadPoolSize);
    }

    public int getThreadPoolSize()
    {
        return this.threadPoolSize;
    }

    public void setThreadPoolSize(int size)
    {
        if (this.executor != null)
        {
            this.executor.shutdown();
        }

        if (size < 1)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "size=" + size);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.threadPoolSize = size;
        this.executor = Executors.newFixedThreadPool(this.threadPoolSize);
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt != null)
            Logging.logger().fine(String.format("%s=%s", evt.getPropertyName(), evt.getNewValue()));

        if (evt != null && evt.getPropertyName() != null)
        {
            if (evt.getPropertyName().equals(CatalogKey.ACTION_COMMAND_BROWSE))
            {
                Object value = evt.getNewValue();
                if (value != null && value instanceof AVList)
                {
                    onBrowse((AVList) value);
                }
                else if (value != null && value instanceof URL)
                {
                    onBrowse(((URL) value).toExternalForm());
                }
                else if (value != null && value instanceof String)
                {
                    onBrowse((String) value);
                }
            }
            else if (evt.getPropertyName().equals(GIDBKey.ACTION_COMMAND_GET_SERVICE_CAPABILITIES))
            {
                Object value = evt.getNewValue();
                if (value != null && value instanceof GIDBResultModel)
                {
                    onGetServiceCapabilities((GIDBResultModel) evt.getNewValue());
                }
            }
            else if (evt.getPropertyName().equals(GIDBKey.ACTION_COMMAND_LAYER_PRESSED))
            {
                Object source = evt.getSource();
                Object value = evt.getNewValue();
                if (source != null && source instanceof GIDBResultModel &&
                    value != null && value instanceof Layer)
                {
                    onLayerPressed((GIDBResultModel) source, (Layer) value);
                }
            }
            else if (evt.getPropertyName().equals(CatalogKey.ACTION_COMMAND_QUERY))
            {
                onGetServices();
            }
        }
    }

    protected void onBrowse(AVList params)
    {
        if (params != null)
        {
            String path = params.getStringValue(CatalogKey.URI);
            if (path != null)
                onDoOpenInBrowser(path);
        }
    }

    protected void onBrowse(String path)
    {
        if (path != null)
        {
            onDoOpenInBrowser(path);
        }
    }

    protected void onGetServices()
    {
        AVList queryParams = this.gidbPanel.getQueryModel();
        if (queryParams != null)
        {
            queryParams = queryParams.copy();
            GetServicesTask task = new GetServicesTask(queryParams, this);
            this.executor.submit(task);
        }
    }

    protected void onGetServiceCapabilities(GIDBResultModel resultModel)
    {
        GetServiceCapabilitiesTask task = new GetServiceCapabilitiesTask(resultModel, this);
        this.executor.submit(task);
    }

    protected void onLayerPressed(GIDBResultModel resultModel, Layer layer)
    {
       UpdateLayerTask task = new UpdateLayerTask(resultModel, layer, this);
       this.executor.submit(task);
    }

    private void onDoOpenInBrowser(String path)
    {
        if (path == null)
        {
            String message = "nullValue.PathIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // TODO: fixup path (if path is missing "http://", for example)
        URL url = null;
        try
        {
            url = new URL(path);
        }
        catch (Exception e)
        {
            String message = "gidb.InvalidPath";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }

        try
        {
            if (url != null)
                BrowserOpener.browse(url);
        }
        catch (Exception e)
        {
            String message = "gidb.CannotOpenBrowser";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
    }

    protected void addResultActor()
    {
        this.numResultActors.incrementAndGet();
        setResultsWaiting(true);
    }

    protected void removeResultActor()
    {
        int i = this.numResultActors.decrementAndGet();
        if (i < 1)
            setResultsWaiting(false);
    }

    private void setResultsWaiting(boolean waiting)
    {
        if (this.gidbPanel != null)
        {
            if (this.gidbPanel.getQueryPanel() != null)
                this.gidbPanel.getQueryPanel().setEnabled(!waiting);
            if (this.gidbPanel.getResultPanel() != null)
                this.gidbPanel.getResultPanel().setWaiting(waiting);
        }
    }

    private void setResultsStatusText(String text)
    {
        if (this.gidbPanel != null)
            if (this.gidbPanel.getResultPanel() != null)
                this.gidbPanel.getResultPanel().setStatusText(text);
    }

    private void setResultsStatusSearching()
    {
        setResultsStatusText("Searching...");
    }

    private void setResultsStatusFinished()
    {
        int numResults = 0;
        if (this.gidbPanel != null)
            if (this.gidbPanel.getResultModel() != null)
                numResults = this.gidbPanel.getResultModel().size();

        StringBuilder sb = new StringBuilder();
        sb.append(numResults);
        sb.append(" result");
        if (numResults > 1)
            sb.append("s");
        sb.append(" returned");
        setResultsStatusText(sb.toString());
    }

    private static class GetServicesTask implements Callable<Object>
    {
        private AVList params;
        private GIDBController controller;

        public GetServicesTask(AVList queryParams, GIDBController controller)
        {
            this.params = queryParams;
            this.controller = controller;
        }

        public Object call() throws Exception
        {
            this.controller.doGetServices(this.params);
            return null;
        }
    }

    private void doGetServices(AVList queryParams)
    {
        if (queryParams == null)
        {
            String message = "nullValue.QueryParamsIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // TODO: capture request parameters in query history

        addResultActor();
        setResultsStatusSearching();
        try
        {
            URL serviceURL = this.gidbPanel.getServiceURL();
            GetServices getServices = new GetServices(queryParams, this.gidbPanel.getResultModel());
            getServices.executeRequest(serviceURL);
        }
        catch (Exception e)
        {
            String message = "gidb.ExceptionWhileGettingServices";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);

            JOptionPane.showMessageDialog(null,
                "Cannot communicate with GIDB Portal at\n" + this.gidbPanel.getService(),
                "Problem communicating with GIDB Portal",
                JOptionPane.ERROR_MESSAGE);
        }
        finally
        {
            removeResultActor();
            setResultsStatusFinished();
        }
    }

    private static class GetServiceCapabilitiesTask implements Callable<Object>
    {
        private GIDBResultModel resultModel;
        private GIDBController controller;

        private GetServiceCapabilitiesTask(GIDBResultModel resultModel, GIDBController controller)
        {
            this.resultModel = resultModel;
            this.controller = controller;
        }

        public Object call() throws Exception
        {
            this.controller.doGetServiceCapabilities(this.resultModel);
            return null;
        }
    }

    private void doGetServiceCapabilities(GIDBResultModel resultModel)
    {
        if (resultModel == null)
        {
            String message = "nullValue.ResultModelIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Do not process the request if the Capabilities already exists.
        if (resultModel.getCapabilities() == null)
        {
            resultModel.setValue(CatalogKey.WAITING, Boolean.TRUE);
            resultModel.firePropertyChange();
            try
            {
                GetCapabilities getCapabilities = new GetCapabilities(resultModel);
                getCapabilities.executeRequest();

                linkLayers(resultModel);
            }
            catch (Exception e)
            {
                String message = "gidb.ExceptionWhileGettingServiceCapabilities";
                Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            }
            finally
            {
                resultModel.setValue(CatalogKey.WAITING, Boolean.FALSE);
                resultModel.firePropertyChange();
            }
        }
    }

    private void linkLayers(GIDBResultModel resultModel)
    {
        if (resultModel == null)
        {
            String message = "nullValue.ResultModelIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (resultModel.getLayerList() != null)
        {
            for (Layer layer : resultModel.getLayerList())
            {
                if (layer != null)
                {
                    Object key = layerKey(layer);
                    if (this.wwLayerMap.containsKey(key))
                    {
                        Object wwLayer = this.wwLayerMap.get(key);
                        layer.setValue(CatalogKey.LAYER_STATE, CatalogKey.LAYER_STATE_INSTALLED);
                        layer.setValue(AVKey.LAYER, wwLayer);
                    }
                }
            }
        }
    }

    private static Object layerKey(AVList params)
    {
        Object key = null;
        if (params != null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(params.getValue(AVKey.LAYER_NAMES));
            sb.append("/");
            sb.append(params.getValue(AVKey.STYLE_NAMES));
            key = sb.toString();
        }
        return key;
    }

    private static class UpdateLayerTask implements Callable<Object>
    {
        private GIDBResultModel resultModel;
        private Layer layer;
        private GIDBController controller;

        private UpdateLayerTask(GIDBResultModel resultModel, Layer layer, GIDBController controller)
        {
            this.resultModel = resultModel;
            this.layer = layer;
            this.controller = controller;
        }

        public Object call() throws Exception
        {
            this.controller.doUpdateLayer(this.resultModel, this.layer);
            return null;
        }
    }

    private void doUpdateLayer(GIDBResultModel resultModel, Layer layer)
    {
        if (resultModel == null)
        {
            String message = "nullValue.ResultModelIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (layer == null)
        {
            String message = "nullValue.LayerIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            String layerState = layer.getStringValue(CatalogKey.LAYER_STATE);
            if (CatalogKey.LAYER_STATE_READY.equalsIgnoreCase(layerState) || layerState == null)
            {
                WMSCapabilities caps = resultModel.getCapabilities();
                addLayerToWorldWind(caps, layer);
                layer.setValue(CatalogKey.LAYER_STATE, CatalogKey.LAYER_STATE_INSTALLED);
            }
            else if (CatalogKey.LAYER_STATE_INSTALLED.equalsIgnoreCase(layerState))
            {
                removeLayerFromWorldWind(layer);
                layer.setValue(CatalogKey.LAYER_STATE, CatalogKey.LAYER_STATE_READY);
            }
        }
        catch (Exception e)
        {
            String message = "gidb.ExceptionWhileInstallingLayer";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            // Flag an error happened while installing the layer.
            layer.setValue(CatalogKey.LAYER_STATE, CatalogKey.LAYER_STATE_ERROR);
            layer.addException(new CatalogException(e.getMessage(), e));
        }
        finally
        {
            resultModel.firePropertyChange();
        }
    }

    private void addLayerToWorldWind(WMSCapabilities caps, AVList layerParams)
    {
        if (caps == null)
        {
            String message = "nullValue.CapabilitiesIsNull";
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }
        if (layerParams == null)
        {
            String message = "nullValue.LayerParamsIsNull";
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        WorldWindow wwd = this.gidbPanel.getWorldWindow();
        if (wwd == null)
        {
            String message = "No World Wind instance is running.";
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        gov.nasa.worldwind.layers.Layer wmsLayer =
            (gov.nasa.worldwind.layers.Layer) BasicFactory.create(AVKey.LAYER_FACTORY, caps, layerParams);

        String s = layerParams.getStringValue(AVKey.TITLE);
        if (s != null)
            wmsLayer.setName(s);

        // Some wms servers are slow, so increase the timeouts and limits used by world wind's retrievers.
        wmsLayer.setValue(AVKey.URL_CONNECT_TIMEOUT, 30000);
        wmsLayer.setValue(AVKey.URL_READ_TIMEOUT, 30000);
        wmsLayer.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, 60000);
        ApplicationTemplate.insertBeforePlacenames(wwd, wmsLayer);

        layerParams.setValue(AVKey.LAYER, wmsLayer);

        Object key = layerKey(layerParams);
        this.wwLayerMap.put(key, wmsLayer);
    }

    private void removeLayerFromWorldWind(AVList layerParams)
    {
        if (layerParams == null)
        {
            String message = "nullValue.LayerParamsIsNull";
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        WorldWindow wwd = this.gidbPanel.getWorldWindow();
        if (wwd == null)
        {
            String message = "No World Wind instance is running.";
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        gov.nasa.worldwind.layers.LayerList wwLayerList = wwd.getModel().getLayers();
        if (wwLayerList == null)
        {
            String message = "Missing Layer List in World Wind.";
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        Object o = layerParams.getValue(AVKey.LAYER);
        if (o == null || !(o instanceof gov.nasa.worldwind.layers.Layer))
        {
            String message = "Layer is not installed in World Wind.";
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        gov.nasa.worldwind.layers.Layer wwLayer = (gov.nasa.worldwind.layers.Layer) o;
        wwLayerList.remove(wwLayer);

        layerParams.removeKey(AVKey.LAYER);

        Object key = layerKey(layerParams);
        this.wwLayerMap.remove(key);
    }
}
