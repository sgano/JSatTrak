/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.applications.gio.catalogui.CatalogException;
import gov.nasa.worldwind.applications.gio.catalogui.CatalogKey;
import gov.nasa.worldwind.applications.gio.csw.*;
import gov.nasa.worldwind.applications.gio.ebrim.ClassificationNode;
import gov.nasa.worldwind.applications.gio.ebrim.ExtrinsicObject;
import gov.nasa.worldwind.applications.gio.ebrim.Service;
import gov.nasa.worldwind.applications.gio.ebrim.User;
import gov.nasa.worldwind.applications.gio.filter.*;
import gov.nasa.worldwind.util.Logging;

import java.util.Iterator;

/**
 * @author dcollins
 * @version $Id: GetServiceCommon.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class GetServiceCommon
{
    private ESGResultModel resultModel;
    private ServicePackage servicePackage;

    public GetServiceCommon(ESGResultModel resultModel)
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
        handleResponse(response.getResponse());
    }

    protected void handleResponse(GetRecordsResponse response)
    {
        this.servicePackage.clearClassifications();
        User user = null;
        Geometry geometry = null;
        if (response != null)
        {
            SearchResults results = response.getSearchResults();
            if (results != null)
            {
                for (Object o : results)
                {
                    if (o != null)
                    {
                        if (o instanceof ClassificationNode)
                        {
                            this.servicePackage.addClassification((ClassificationNode) o);
                        }
                        else if (o instanceof User)
                        {
                            user = (User) o;
                        }
                        else if (o instanceof ExtrinsicObject)
                        {
                            if ("Geometry".equalsIgnoreCase(((ExtrinsicObject) o).getObjectType()))
                            {
                                if (geometry == null)
                                    geometry = new Geometry();
                                geometry.setExtrinsicObject((ExtrinsicObject) o);
                            }
                        }
                    }
                }
            }
        }
        this.servicePackage.setUser(user);
        this.servicePackage.setGeometry(geometry);
        makeResultParams(this.resultModel);
        makeResultExceptions(this.resultModel);
    }

    protected void makeResultParams(ESGResultModel resultModel)
    {
        if (resultModel == null)
            return;

        ServicePackage servicePackage = resultModel.getServicePackage();
        if (servicePackage == null)
            return;

        Iterator<ClassificationNode> classificationIter = servicePackage.getClassificationIterator();
        if (classificationIter != null)
            RegistryObjectUtils.makeClassificationParams(classificationIter, resultModel);

        User user = servicePackage.getUser();
        if (user != null)
            RegistryObjectUtils.makePersonParams(user, resultModel);

        // TODO: geometry
    }

    protected void makeResultExceptions(ESGResultModel resultModel)
    {
        if (resultModel == null)
            return;

        if (resultModel.getValue(CatalogKey.SERVICE_TYPE) == null)
        {
            String message = "Service is not properly classified as WMS, WFS, or WCS.";
            CatalogException e = new CatalogException(message, null);
            resultModel.addException(e);
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
            Query query = addQuery("rim:Service=srv" +
                    " rim:Classification=cls rim:ClassificationNode=clsNode" +
                    " rim:User=usr" +
                    " rim:ExtrinsicObject=eo" +
                    " rim:Association=assoc1,assoc2");
            ElementSetName elementSetName = query.addElementSetName(ElementSetType.FULL);
            elementSetName.setTypeNames("clsNode,usr,eo");
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
            equalTo.addExpression(new PropertyName("/$cls/@classifiedObject"));
            and.addComparisonOperator(equalTo);

            equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$cls/@classificationNode"));
            equalTo.addExpression(new PropertyName("/$clsNode/@id"));
            and.addComparisonOperator(equalTo);

            equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$srv/@id"));
            equalTo.addExpression(new PropertyName("/$assoc1/@targetObject"));
            and.addComparisonOperator(equalTo);

            equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$usr/@id"));
            equalTo.addExpression(new PropertyName("/$assoc1/@sourceObject"));
            and.addComparisonOperator(equalTo);

            equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$srv/@id"));
            equalTo.addExpression(new PropertyName("/$assoc2/@sourceObject"));
            and.addComparisonOperator(equalTo);

            equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$eo/@id"));
            equalTo.addExpression(new PropertyName("/$assoc2/@targetObject"));
            and.addComparisonOperator(equalTo);

            equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$assoc2/@associationType"));
            equalTo.addExpression(new Literal("HasFootprint"));
            and.addComparisonOperator(equalTo);
        }
    }
}
