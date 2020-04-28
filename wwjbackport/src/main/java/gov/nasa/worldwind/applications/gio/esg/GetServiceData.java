/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.applications.gio.catalogui.CatalogKey;
import gov.nasa.worldwind.applications.gio.csw.*;
import gov.nasa.worldwind.applications.gio.ebrim.Association;
import gov.nasa.worldwind.applications.gio.ebrim.ExternalLink;
import gov.nasa.worldwind.applications.gio.ebrim.ExtrinsicObject;
import gov.nasa.worldwind.applications.gio.ebrim.Service;
import gov.nasa.worldwind.applications.gio.filter.*;
import gov.nasa.worldwind.util.Logging;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dcollins
 * @version $Id: GetServiceData.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class GetServiceData extends GetRecords
{
    private ESGResultModel resultModel;
    private ServicePackage servicePackage;
    private Map<String, ServiceData> serviceDataMap;
    @SuppressWarnings({"FieldCanBeLocal"})
    private Map<String, ExternalLink> externalLinkMap;
    @SuppressWarnings({"FieldCanBeLocal"})
    private Map<String, String> associationMap;

    public static final String SERVICE_DATA_REQUEST = "ServiceDataRequest";
    public static final String SERVICE_DATA_LINKS_REQUEST = "ServiceDataLinksRequest";

    public GetServiceData(ESGResultModel resultModel)
    {
        if (resultModel == null)
        {
            String message = "nullValue.ResultModelIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (resultModel.getServicePackage() == null)
        {
            String message = "nullValue.ResultModelServicePackageIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (resultModel.getServicePackage().getService() == null)
        {
            String message = "nullValue.ResultModelServiceIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.resultModel = resultModel;
        this.servicePackage = resultModel.getServicePackage();
    }

    public Request getRequest(String requestType) throws Exception
    {
        if (requestType == null)
        {
            String message = "nullValue.RequestTypeIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (SERVICE_DATA_REQUEST.equalsIgnoreCase(requestType))
            return new ServiceDataRequest(this.servicePackage.getService());
        else if (SERVICE_DATA_LINKS_REQUEST.equalsIgnoreCase(requestType))
            return new ServiceDataLinksRequest(this.servicePackage.getService());
        return null;
    }

    public void executeRequest(CSWConnectionPool connectionPool) throws Exception
    {
        if (connectionPool == null)
        {
            String message = "nullValue.ConnectionPoolIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        doExecuteServiceDataRequest(connectionPool.getConnection());
        doExecuteServiceDataLinksRequest(connectionPool.getConnection());
    }

    protected void doExecuteServiceDataRequest(CSWConnection conn) throws Exception
    {
        Request request = getRequest(SERVICE_DATA_REQUEST);
        GetRecordsDocumentParser response = new GetRecordsDocumentParser("EBRIM");
        QueryUtils.executeQueryLogExceptions(conn, request, response, this.resultModel);
        setServiceData(response.getResponse());
    }

    protected void doExecuteServiceDataLinksRequest(CSWConnection conn) throws Exception
    {
        Request request = getRequest(SERVICE_DATA_LINKS_REQUEST);
        GetRecordsDocumentParser response = new GetRecordsDocumentParser("EBRIM");
        QueryUtils.executeQueryLogExceptions(conn, request, response, this.resultModel);
        setServiceDataLinks(response.getResponse());
    }

    protected void setServiceData(GetRecordsResponse response)
    {
        this.servicePackage.clearServiceData();
        this.serviceDataMap = new HashMap<String, ServiceData>();
        if (response != null)
        {
            SearchResults results = response.getSearchResults();
            if (results != null)
            {
                for (Object o : results)
                {
                    if (o != null && o instanceof ExtrinsicObject)
                    {
                        String objectType = ((ExtrinsicObject) o).getObjectType();
                        if ("Layer".equalsIgnoreCase(objectType) ||
                            "FeatureType".equalsIgnoreCase(objectType) ||
                            "CoverageOfferingBrief".equalsIgnoreCase(objectType))
                        {
                            ServiceData serviceData = new ServiceData();
                            serviceData.setExtrinsicObject((ExtrinsicObject) o);
                            serviceData.setService(this.servicePackage.getService());
                            makeServiceDataParams(serviceData);
                            this.servicePackage.addServiceData(serviceData);
                            this.serviceDataMap.put(((ExtrinsicObject) o).getId(), serviceData);
                        }
                    }
                }
            }
        }
    }

    protected void setServiceDataLinks(GetRecordsResponse response)
    {
        this.externalLinkMap = new HashMap<String, ExternalLink>();
        this.associationMap = new HashMap<String, String>();
        if (response != null)
        {
            SearchResults results = response.getSearchResults();
            if (results != null)
            {
                for (Object o : results)
                {
                    if (o != null)
                    {
                        if (o instanceof ExternalLink)
                            this.externalLinkMap.put(((ExternalLink) o).getId(), (ExternalLink) o);
                        else if (o instanceof Association)
                            this.associationMap.put(((Association) o).getSourceObject(), ((Association) o).getTargetObject());
                    }
                }
            }
        }

        if (this.serviceDataMap != null)
        {
            for (ExternalLink elnk : this.externalLinkMap.values())
            {
                String targetObject = this.associationMap.get(elnk.getId());
                if (targetObject != null)
                {
                    ServiceData serviceData = this.serviceDataMap.get(targetObject);
                    if (serviceData != null)
                    {
                        ServiceDataLink serviceDataLink = new ServiceDataLink();
                        serviceDataLink.setExternalLink(elnk);
                        makeServiceDataLinksParams(serviceDataLink);
                        serviceData.addLink(serviceDataLink);
                    }
                }
            }
        }
    }

    protected void makeServiceDataParams(ServiceData serviceData)
    {
        if (serviceData == null)
            return;

        ExtrinsicObject eo = serviceData.getExtrinsicObject();
        if (eo == null)
            return;

        RegistryObjectUtils.makeCommonParams(eo, serviceData);
    }

    protected void makeServiceDataLinksParams(ServiceDataLink serviceDataLink)
    {
        if (serviceDataLink == null)
            return;

        ExternalLink elnk = serviceDataLink.getExternalLink();
        if (elnk == null)
            return;

        RegistryObjectUtils.makeCommonParams(elnk, serviceDataLink);

        String s = elnk.getExternalURI();
        if (s != null)
            serviceDataLink.setValue(CatalogKey.URI, s);

        // Provide a non-null value for UI elements looking for this action.        
        serviceDataLink.setValue(CatalogKey.ACTION_COMMAND_BROWSE, serviceDataLink);
    }

    protected static class ServiceDataRequest extends GetRecords
    {
        public ServiceDataRequest(Service service) throws Exception
        {
            if (service == null)
            {
                String message = Logging.getMessage("nullValue.ServiceIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (service.getId() == null)
            {
                String message = "nullValue.ServiceIdIsNull";
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            setResultType(ResultType.RESULTS);
            setOutputFormat("text/xml");
            setOutputSchema("EBRIM");
            setStartPosition(1);
            setMaxRecords(Short.MAX_VALUE);
            buildQuery(service.getId());
        }

        protected void buildQuery(String id) throws Exception
        {
            Query query = addQuery("rim:Service=srv rim:ExtrinsicObject=eo rim:Association=assoc");
            ElementSetName elementSetName = query.addElementSetName(ElementSetType.FULL);
            elementSetName.setTypeNames("eo");
            Constraint constraint = query.addConstraint();
            constraint.setVersion("1.1.0");
            Filter filter = constraint.addFilter();

            And and = new And();
            filter.addLogicalOperator(and);

            PropertyIsEqualTo equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$srv/@id"));
            equalTo.addExpression(new Literal(id));
            and.addComparisonOperator(equalTo);

            equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$srv/@id"));
            equalTo.addExpression(new PropertyName("/$assoc/@sourceObject"));
            and.addComparisonOperator(equalTo);

            equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$eo/@id"));
            equalTo.addExpression(new PropertyName("/$assoc/@targetObject"));
            and.addComparisonOperator(equalTo);

            equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$assoc/@associationType"));
            equalTo.addExpression(new Literal("Offers"));
            and.addComparisonOperator(equalTo);
        }
    }

    protected static class ServiceDataLinksRequest extends GetRecords
    {
        public ServiceDataLinksRequest(Service service) throws Exception
        {
            if (service == null)
            {
                String message = Logging.getMessage("nullValue.ServiceIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
            if (service.getId() == null)
            {
                String message = "nullValue.ServiceIdIsNull";
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            setResultType(ResultType.RESULTS);
            setOutputFormat("text/xml");
            setOutputSchema("EBRIM");
            setStartPosition(1);
            setMaxRecords(Short.MAX_VALUE);
            buildQuery(service.getId());
        }

        protected void buildQuery(String id) throws Exception
        {
            Query query = addQuery("rim:Service=srv rim:ExtrinsicObject=eo rim:ExternalLink=elnk " +
                                   "rim:Association=assoc1,assoc2");
            ElementSetName elementSetName = query.addElementSetName(ElementSetType.FULL);
            elementSetName.setTypeNames("elnk,assoc2");
            Constraint constraint = query.addConstraint();
            constraint.setVersion("1.1.0");
            Filter filter = constraint.addFilter();

            And and = new And();
            filter.addLogicalOperator(and);

            PropertyIsEqualTo equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$srv/@id"));
            equalTo.addExpression(new Literal(id));
            and.addComparisonOperator(equalTo);

            equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$srv/@id"));
            equalTo.addExpression(new PropertyName("/$assoc1/@sourceObject"));
            and.addComparisonOperator(equalTo);

            equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$eo/@id"));
            equalTo.addExpression(new PropertyName("/$assoc1/@targetObject"));
            and.addComparisonOperator(equalTo);

            equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$assoc1/@associationType"));
            equalTo.addExpression(new Literal("Offers"));
            and.addComparisonOperator(equalTo);

            equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$elnk/@id"));
            equalTo.addExpression(new PropertyName("/$assoc2/@sourceObject"));
            and.addComparisonOperator(equalTo);

            equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$eo/@id"));
            equalTo.addExpression(new PropertyName("/$assoc2/@targetObject"));
            and.addComparisonOperator(equalTo);

            equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$assoc2/@associationType"));
            equalTo.addExpression(new Literal("ExternallyLinks"));
            and.addComparisonOperator(equalTo);
        }
    }
}
