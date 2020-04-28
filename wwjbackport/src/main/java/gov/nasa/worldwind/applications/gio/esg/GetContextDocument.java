/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.applications.gio.csw.*;
import gov.nasa.worldwind.applications.gio.ebrim.ExtrinsicObject;
import gov.nasa.worldwind.applications.gio.ebrim.Service;
import gov.nasa.worldwind.applications.gio.filter.*;
import gov.nasa.worldwind.util.Logging;

/**
 * @author dcollins
 * @version $Id: GetContextDocument.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class GetContextDocument
{
    private ESGResultModel resultModel;
    private ServicePackage servicePackage;
    
    public static final EmptyContextDocument EMPTY_CONTEXT_DOCUMENT = new EmptyContextDocument();

    private static class EmptyContextDocument extends ContextDocument
    {
        public EmptyContextDocument()
        {
        }
    }

    public GetContextDocument(ESGResultModel resultModel)
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

    public Request getRequest() throws Exception
    {
        return new Request(this.servicePackage.getService());
    }

    public void executeRequest(CSWConnectionPool connectionPool) throws Exception
    {
        if (connectionPool == null)
        {
            String message = "nullValue.ConnectionPoolIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        doExecuteRequest(connectionPool.getConnection());
    }

    protected void doExecuteRequest(CSWConnection conn) throws Exception
    {
        Request request = getRequest();
        GetRecordsDocumentParser response = new GetRecordsDocumentParser("EBRIM");
        QueryUtils.executeQueryLogExceptions(conn, request, response, this.resultModel);
        setContextDocument(response.getResponse());
    }

    protected void setContextDocument(GetRecordsResponse response)
    {
        ContextDocument contextDocument = null;
        DatasetDescription datasetDescription = null;
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
                        if ("ContextDocument".equalsIgnoreCase(objectType))
                        {
                            if (contextDocument == null)
                                contextDocument = new ContextDocument();
                            contextDocument.setExtrinsicObject((ExtrinsicObject) o);
                        }
                        else if ("Dataset Description".equalsIgnoreCase(objectType))
                        {
                            if (datasetDescription == null)
                                datasetDescription = new DatasetDescription();
                            datasetDescription.setExtrinsicObject((ExtrinsicObject) o);
                        }
                    }
                }
            }
        }
        if (contextDocument != null)
            contextDocument.setDatasetDescription(datasetDescription);
        if (contextDocument == null)
            contextDocument = EMPTY_CONTEXT_DOCUMENT;
        this.servicePackage.setContextDocument(contextDocument);
        makeResultParams(this.resultModel);
    }

    protected void makeResultParams(ESGResultModel resultModel)
    {
        if (resultModel == null)
            return;

        ServicePackage servicePackage = resultModel.getServicePackage();
        if (servicePackage == null)
            return;

        ContextDocument contextDocument = servicePackage.getContextDocument();
        if (contextDocument != null)
        {
            DatasetDescription datasetDescription = contextDocument.getDatasetDescription();
            if (datasetDescription != null && datasetDescription.getExtrinsicObject() != null)
                RegistryObjectUtils.makeCommonParamsNoOverwrite(datasetDescription.getExtrinsicObject(), resultModel);

            if (contextDocument.getExtrinsicObject() != null)
                RegistryObjectUtils.makeCommonParamsNoOverwrite(contextDocument.getExtrinsicObject(), resultModel);
        }
    }

    protected static class Request extends GetRecords
    {
        public Request(Service service) throws Exception
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
            Query query = addQuery("rim:Service=srv rim:ExtrinsicObject=eo1,eo2 rim:Association=assoc1,assoc2");
            ElementSetName elementSetName = query.addElementSetName(ElementSetType.FULL);
            elementSetName.setTypeNames("eo1,eo2");
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
            equalTo.addExpression(new PropertyName("/$eo1/@id"));
            equalTo.addExpression(new PropertyName("/$assoc1/@targetObject"));
            and.addComparisonOperator(equalTo);

            equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$assoc1/@associationType"));
            equalTo.addExpression(new Literal("HasContext"));
            and.addComparisonOperator(equalTo);

            equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$eo1/@id"));
            equalTo.addExpression(new PropertyName("/$assoc2/@targetObject"));
            and.addComparisonOperator(equalTo);

            equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$eo2/@id"));
            equalTo.addExpression(new PropertyName("/$assoc2/@sourceObject"));
            and.addComparisonOperator(equalTo);

            equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$assoc2/@associationType"));
            equalTo.addExpression(new Literal("Describes"));
            and.addComparisonOperator(equalTo);
        }
    }
}
