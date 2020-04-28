/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.applications.gio.ESGCatalogPanel;
import gov.nasa.worldwind.applications.gio.catalogui.*;
import gov.nasa.worldwind.applications.gio.csw.*;
import gov.nasa.worldwind.applications.gio.ows.*;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.layers.Layer;
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
 * @version $Id: ESGController.java 13375 2010-05-05 23:43:39Z dcollins $
 */
public class ESGController implements PropertyChangeListener
{
    private ESGCatalogPanel esgPanel;
    private CSWConnectionPool connectionPool;
    private int threadPoolSize;
    private ExecutorService executor;
    private final AtomicInteger numResultActors = new AtomicInteger(0);
    private final Map<Object, Layer> wwLayerMap = new HashMap<Object, Layer>();
    private static final int DEFAULT_THREAD_POOL_SIZE = 3;

    public ESGController(ESGCatalogPanel esgPanel)
    {
        if (esgPanel == null)
        {
            String message = "nullValue.ESGPanelIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.esgPanel = esgPanel;

        try
        {
            URL url = this.esgPanel.getServiceURL();
            this.connectionPool = new HttpCSWConnectionPool(url);
        }
        catch (Exception e)
        {
            String message = "esg.InvalidServiceURL";
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        this.threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
        this.executor = Executors.newFixedThreadPool(this.threadPoolSize);
    }

    public CSWConnectionPool getConnectionPool()
    {
        return this.connectionPool;
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
            else if (evt.getPropertyName().equals(ESGKey.ACTION_COMMAND_GET_SERVICE_DATA))
            {
                Object value = evt.getNewValue();
                if (value != null && value instanceof ESGResultModel)
                {
                    onGetServiceData((ESGResultModel) evt.getNewValue());
                }
            }
            else if (evt.getPropertyName().equals(ESGKey.ACTION_COMMAND_GET_SERVICE_INFO))
            {
                Object value = evt.getNewValue();
                if (value != null && value instanceof ESGResultModel)
                {
                    onGetServiceInfo((ESGResultModel) evt.getNewValue());
                }
            }
            else if (evt.getPropertyName().equals(ESGKey.ACTION_COMMAND_GET_SERVICE_METADATA))
            {
                Object value = evt.getNewValue();
                if (value != null && value instanceof ESGResultModel)
                {
                    onGetServiceMetadata((ESGResultModel) evt.getNewValue());
                }
            }
            else if (evt.getPropertyName().equals(ESGKey.ACTION_COMMAND_SERVICE_DATA_PRESSED))
            {
                Object source = evt.getSource();
                Object value = evt.getNewValue();
                if (source != null && source instanceof ESGResultModel &&
                    value != null && value instanceof ServiceData)
                {
                    onUpdateServiceData((ESGResultModel) source, (ServiceData) value);
                }
            }
            else if (evt.getPropertyName().equals(CatalogKey.ACTION_COMMAND_QUERY))
            {
                onGetServices();
            }
            else if (evt.getPropertyName().equals(ESGKey.ACTION_COMMAND_SHOW_SERVICE_DETAILS))
            {
                Object source = evt.getNewValue();
                if (source != null && source instanceof ESGResultModel)
                {
                    onShowServiceDetails((ESGResultModel) evt.getNewValue());
                }
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
        AVList queryParams = this.esgPanel.getQueryModel();
        if (queryParams != null)
        {
            queryParams = queryParams.copy();
            GetServicesTask task = new GetServicesTask(queryParams, this);
            this.executor.submit(task);
        }
    }

    protected void onGetServiceData(ESGResultModel resultModel)
    {
        GetServiceDataTask task = new GetServiceDataTask(resultModel, this);
        this.executor.submit(task);
    }

    protected void onGetServiceInfo(ESGResultModel resultModel)
    {
        GetServiceInfoTask task = new GetServiceInfoTask(resultModel, this);
        this.executor.submit(task);
    }

    protected void onGetServiceMetadata(ESGResultModel resultModel)
    {
        GetServiceMetadataTask task = new GetServiceMetadataTask(resultModel, this);
        this.executor.submit(task);
    }

    protected void onShowServiceDetails(ESGResultModel resultModel)
    {
        ShowServiceDetailsTask task = new ShowServiceDetailsTask(resultModel, this);
        this.executor.submit(task);
    }

    protected void onUpdateServiceData(ESGResultModel resultModel, ServiceData serviceData)
    {
        UpdateServiceDataTask task = new UpdateServiceDataTask(resultModel, serviceData, this);
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
            String message = "esg.InvalidPath";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }

        try
        {
            if (url != null)
                BrowserOpener.browse(url);
        }
        catch (Exception e)
        {
            String message = "esg.CannotOpenBrowser";
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
        if (this.esgPanel != null)
        {
            if (this.esgPanel.getQueryPanel() != null)
                this.esgPanel.getQueryPanel().setEnabled(!waiting);
            if (this.esgPanel.getResultPanel() != null)
                this.esgPanel.getResultPanel().setWaiting(waiting);
        }
    }

    private void setResultsStatusText(String text)
    {
        if (this.esgPanel != null)
            if (this.esgPanel.getResultPanel() != null)
                this.esgPanel.getResultPanel().setStatusText(text);
    }

    private void setResultsStatusSearching()
    {
        setResultsStatusText("Searching...");
    }

    private void setResultsStatusFinished()
    {
        int numResults = 0;
        if (this.esgPanel != null)
            if (this.esgPanel.getResultModel() != null)
                numResults = this.esgPanel.getResultModel().size();

        StringBuilder sb = new StringBuilder();
        sb.append(numResults);
        sb.append(" result");
        if (numResults > 1)
            sb.append("s");
        sb.append(" returned");
        setResultsStatusText(sb.toString());
    }

    private void showExceptionDialog(ExceptionReport exceptionReport)
    {
        String title = "Problem communicating with Earth Science Gateway";
        if (exceptionReport != null)
        {
            for (ExceptionType e : exceptionReport)
            {
                if (e != null)
                {
                    String text = QueryUtils.getExceptionText(e);
                    if (text != null)
                        JOptionPane.showMessageDialog(null, text, title, JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private static class GetServicesTask implements Callable<Object>
    {
        private AVList params;
        private ESGController controller;

        public GetServicesTask(AVList queryParams, ESGController controller)
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
            GetServices getServices = new GetServices(queryParams, this.esgPanel.getResultModel());
            ExceptionReport exceptionReport = getServices.executeRequest(this.connectionPool);
            if (exceptionReport != null)
            {
                showExceptionDialog(exceptionReport);
                QueryUtils.logExceptionReport(exceptionReport);
            }
        }
        catch (CSWConnectionException e)
        {
            String message = "esg.ExceptionWhileGettingServices";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);

            JOptionPane.showMessageDialog(null,
                "Cannot communicate with Earth Science Gateway at\n" + this.esgPanel.getService(),
                "Problem communicating with Earth Science Gateway",
                JOptionPane.ERROR_MESSAGE);
        }
        catch (Exception e)
        {
            String message = "esg.ExceptionWhileGettingServices";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
        finally
        {
            removeResultActor();
            setResultsStatusFinished();
        }
    }

    private static class GetServiceDataTask implements Callable<Object>
    {
        private ESGResultModel resultModel;
        private ESGController controller;

        private GetServiceDataTask(ESGResultModel resultModel, ESGController controller)
        {
            this.resultModel = resultModel;
            this.controller = controller;
        }

        public Object call() throws Exception
        {
            this.controller.doGetServiceData(this.resultModel);
            return null;
        }
    }

    private void doGetServiceData(ESGResultModel resultModel)
    {
        if (resultModel == null)
        {
            String message = "nullValue.ResultModelIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Do not process the request if the ServicePackage is not available,
        // or if ServiceData already exists.
        if (resultModel.getServicePackage() != null &&
            resultModel.getServicePackage().getServiceDataCount() == 0)
        {
            resultModel.setValue(CatalogKey.WAITING, Boolean.TRUE);
            resultModel.firePropertyChange();
            try
            {
                GetServiceData getData = new GetServiceData(resultModel);
                getData.executeRequest(this.connectionPool);

                GetCapabilities getCapabilities = new GetCapabilities(resultModel);
                getCapabilities.executeRequest(this.connectionPool);

                linkServiceData(resultModel);
            }
            catch (Exception e)
            {
                String message = "esg.ExceptionWhileGettingServiceData";
                Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            }
            finally
            {
                resultModel.setValue(CatalogKey.WAITING, Boolean.FALSE);
                resultModel.firePropertyChange();
            }
        }
    }

    private void linkServiceData(ESGResultModel resultModel)
    {
        if (resultModel == null)
        {
            String message = "nullValue.ResultModelIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (resultModel.getServicePackage() != null)
        {
            for (int i = 0; i < resultModel.getServicePackage().getServiceDataCount(); i++)
            {
                ServiceData serviceData = resultModel.getServicePackage().getServiceData(i);
                if (serviceData != null)
                {
                    Object key = serviceDataKey(serviceData);
                    if (this.wwLayerMap.containsKey(key))
                    {
                        Object wwLayer = this.wwLayerMap.get(key);
                        serviceData.setValue(CatalogKey.LAYER_STATE, CatalogKey.LAYER_STATE_INSTALLED);
                        serviceData.setValue(AVKey.LAYER, wwLayer);
                    }
                }
            }
        }
    }

    private static Object serviceDataKey(AVList params)
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

    private static class GetServiceInfoTask implements Callable<Object>
    {
        private ESGResultModel resultModel;
        private ESGController controller;

        private GetServiceInfoTask(ESGResultModel resultModel, ESGController controller)
        {
            this.resultModel = resultModel;
            this.controller = controller;
        }

        public Object call() throws Exception
        {
            this.controller.doGetServiceInfo(this.resultModel);
            return null;
        }
    }

    private void doGetServiceInfo(ESGResultModel resultModel)
    {
        if (resultModel == null)
        {
            String message = "nullValue.ResultModelIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        addResultActor();
        resultModel.setValue(CatalogKey.WAITING, Boolean.TRUE);
        resultModel.firePropertyChange(CatalogKey.WAITING, null, resultModel);
        try
        {
            GetServiceCommon getCommon = new GetServiceCommon(resultModel);
            getCommon.executeRequest(this.connectionPool);
        }
        catch (Exception e)
        {
            String message = "esg.ExceptionWhileGettingServiceInfo";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
        finally
        {
            removeResultActor();
            resultModel.setValue(CatalogKey.WAITING, Boolean.FALSE);
            resultModel.firePropertyChange();
        }
    }

    private static class GetServiceMetadataTask implements Callable<Object>
    {
        private ESGResultModel resultModel;
        private ESGController controller;

        private GetServiceMetadataTask(ESGResultModel resultModel, ESGController controller)
        {
            this.resultModel = resultModel;
            this.controller = controller;
        }

        public Object call() throws Exception
        {
            this.controller.doGetServiceMetadata(this.resultModel);
            return null;
        }
    }

    private void doGetServiceMetadata(ESGResultModel resultModel)
    {
        if (resultModel == null)
        {
            String message = "nullValue.ResultModelIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (resultModel.getServicePackage() != null &&
            resultModel.getServicePackage().getContextDocument() == null)
        {
            resultModel.setValue(CatalogKey.WAITING, Boolean.TRUE);
            resultModel.firePropertyChange(CatalogKey.WAITING, null, resultModel);
            try
            {
                GetContextDocument getContextDocument = new GetContextDocument(resultModel);
                getContextDocument.executeRequest(this.connectionPool);
            }
            catch (Exception e)
            {
                String message = "esg.ExceptionWhileGettingServiceMetadata";
                Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            }
            finally
            {
                resultModel.setValue(CatalogKey.WAITING, Boolean.FALSE);
                resultModel.firePropertyChange();
            }
        }
    }

    private static class ShowServiceDetailsTask implements Callable<Object>
    {
        private ESGResultModel resultModel;
        private ESGController controller;

        private ShowServiceDetailsTask(ESGResultModel resultModel, ESGController controller)
        {
            this.resultModel = resultModel;
            this.controller = controller;
        }

        public Object call() throws Exception
        {
            this.controller.doGetServiceMetadata(this.resultModel);
            this.controller.doShowServiceDetails(this.resultModel);
            return null;
        }
    }

    private void doShowServiceDetails(ESGResultModel resultModel)
    {
        if (resultModel == null)
        {
            String message = "esg.ResultModelIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            ServiceDetailsDialog.showDialog(this.esgPanel.getServiceDetailsContentPath(),
                this.esgPanel.getServiceDetailsContentType(), resultModel, true);
        }
        catch (Exception e)
        {
            String message = "esg.ExceptionWhileShowingServiceDetails";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
    }

    private static class UpdateServiceDataTask implements Callable<Object>
    {
        private ESGResultModel resultModel;
        private ServiceData serviceData;
        private ESGController controller;

        private UpdateServiceDataTask(ESGResultModel resultModel, ServiceData serviceData, ESGController controller)
        {
            this.resultModel = resultModel;
            this.serviceData = serviceData;
            this.controller = controller;
        }

        public Object call() throws Exception
        {
            this.controller.doUpdateServiceData(this.resultModel, this.serviceData);
            return null;
        }
    }

    private void doUpdateServiceData(ESGResultModel resultModel, ServiceData serviceData)
    {
        if (resultModel == null)
        {
            String message = "nullValue.ResultModelIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (serviceData == null)
        {
            String message = "nullValue.ServiceDataIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        try
        {
            String layerState = serviceData.getStringValue(CatalogKey.LAYER_STATE);
            if (CatalogKey.LAYER_STATE_READY.equalsIgnoreCase(layerState) || layerState == null)
            {
                WMSCapabilities caps = resultModel.getCapabilities();
                addLayerToWorldWind(caps, serviceData);
                serviceData.setValue(CatalogKey.LAYER_STATE, CatalogKey.LAYER_STATE_INSTALLED);
            }
            else if (CatalogKey.LAYER_STATE_INSTALLED.equalsIgnoreCase(layerState))
            {
                removeLayerFromWorldWind(serviceData);
                serviceData.setValue(CatalogKey.LAYER_STATE, CatalogKey.LAYER_STATE_READY);
            }
        }
        catch (Exception e)
        {
            String message = "esg.ExceptionWhileInstallingLayer";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
            // Flag an error happened while installing the layer.
            serviceData.setValue(CatalogKey.LAYER_STATE, CatalogKey.LAYER_STATE_ERROR);
            serviceData.addException(new CatalogException(e.getMessage(), e));
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
            String message = Logging.getMessage("nullValue.WMSCapabilitiesIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (layerParams == null)
        {
            String message = Logging.getMessage("nullValue.ParametersIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        WorldWindow wwd = this.esgPanel.getWorldWindow();
        if (wwd == null)
        {
            String message = "No World Wind instance is running.";
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        Layer wmsLayer = (Layer) BasicFactory.create(AVKey.LAYER_FACTORY, caps, layerParams);

        String s = layerParams.getStringValue(AVKey.TITLE);
        if (s != null)
            wmsLayer.setName(s);

        // Some wms servers are slow, so increase the timeouts and limits used by world wind's retrievers.
        wmsLayer.setValue(AVKey.URL_CONNECT_TIMEOUT, 30000);
        wmsLayer.setValue(AVKey.URL_READ_TIMEOUT, 30000);
        wmsLayer.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, 60000);
        ApplicationTemplate.insertBeforePlacenames(wwd, wmsLayer);

        layerParams.setValue(AVKey.LAYER, wmsLayer);

        Object key = serviceDataKey(layerParams);
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

        WorldWindow wwd = this.esgPanel.getWorldWindow();
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

        Object key = serviceDataKey(layerParams);
        this.wwLayerMap.remove(key);
    }
}
