/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.applications.gio.catalogui.CatalogKey;
import gov.nasa.worldwind.applications.gio.catalogui.ResultList;
import gov.nasa.worldwind.applications.gio.catalogui.ResultModel;
import gov.nasa.worldwind.applications.gio.csw.*;
import gov.nasa.worldwind.applications.gio.ebrim.Service;
import gov.nasa.worldwind.applications.gio.ebrim.ServiceParser;
import gov.nasa.worldwind.applications.gio.filter.*;
import gov.nasa.worldwind.applications.gio.ows.ExceptionReport;
import gov.nasa.worldwind.applications.gio.xml.ElementParser;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.Logging;

import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * @author dcollins
 * @version $Id: GetServices.java 6662 2008-09-16 17:50:41Z dcollins $
 */
public class GetServices
{
    private AVList queryParams;
    private ResultList outResultList;
    private List<ResultModel> serviceList;

    public GetServices(AVList queryParams, ResultList resultList)
    {
        if (queryParams == null)
        {
            String message = "nullValue.QueryParamsIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (resultList == null)
        {
            String message = "nullValue.ResultListIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.queryParams = queryParams;
        this.outResultList = resultList;
    }

    public Request getRequest() throws Exception
    {
        return new Request(this.queryParams);
    }

    public ExceptionReport executeRequest(CSWConnectionPool connectionPool) throws Exception
    {
        if (connectionPool == null)
        {
            String message = "nullValue.ConnectionPoolIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return doExecuteRequest(connectionPool.getConnection());
    }

    protected ExceptionReport doExecuteRequest(CSWConnection conn) throws Exception
    {
        Request request = getRequest();
        ResponseParser response = new ResponseParser(this);
        QueryUtils.executeQuery(conn, request, response);

        this.outResultList.clear();
        if (this.serviceList != null)
        {
            sortServiceList(CatalogKey.TITLE);
            this.outResultList.addAll(this.serviceList);
            this.serviceList = null;

            // Search for more information about each Service.
            for (ResultModel resultModel : this.outResultList)
                resultModel.firePropertyChange(ESGKey.ACTION_COMMAND_GET_SERVICE_INFO, null, resultModel);
        }

        if (response.hasExceptions())
            return response.getExceptionReport();
        return null;
    }

    protected void addService(Service service)
    {
        if (service == null)
        {
            String message = Logging.getMessage("nullValue.ServiceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        
        ESGResultModel resultModel = new ESGResultModel();
        ServicePackage servicePackage = new ServicePackage();
        servicePackage.setService(service);
        resultModel.setServicePackage(servicePackage);
        makeResultParams(resultModel);

        if (this.serviceList == null)
            this.serviceList = new ArrayList<ResultModel>();
        this.serviceList.add(resultModel);
    }

    protected void sortServiceList(String key)
    {
        if (key != null)
        {
            final String sortKey = key;
            Comparator<ResultModel> comparator = new Comparator<ResultModel>()
            {
                public int compare(ResultModel r1, ResultModel r2)
                {
                    if (r1 == null && r2 == null)
                        return 0;
                    else if (r1 == null)
                        return 1;
                    else if (r2 == null)
                        return -1;

                    Object o1 = r1.getValue(sortKey);
                    Object o2 = r2.getValue(sortKey);
                    if (o1 == null && o2 == null)
                        return 0;
                    else if (o1 == null)
                        return 1;
                    else if (o2 == null)
                        return -1;

                    return String.CASE_INSENSITIVE_ORDER.compare(o1.toString(), o2.toString());
                }
            };

            Collections.sort(this.serviceList, comparator);
        }
    }

    protected void makeResultParams(ESGResultModel resultModel)
    {
        if (resultModel == null)
            return;

        ServicePackage servicePackage = resultModel.getServicePackage();
        if (servicePackage == null)
            return;

        Service service = servicePackage.getService();
        if (service == null)
            return;

        RegistryObjectUtils.makeCommonParams(service, resultModel);

        // Note that this is a service, but we don't know what type yet.
        resultModel.setValue(CatalogKey.SERVICE_TYPE, null);
        // Provide a non-null value for UI elements looking for this action.
        resultModel.setValue(ESGKey.ACTION_COMMAND_SHOW_SERVICE_DETAILS, resultModel);
    }

    protected static class Request extends GetRecords
    {
        public Request(AVList params) throws Exception
        {
            if (params == null)
            {
                String message = "nullValue.ParamsIsNull";
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            setResultType(ResultType.RESULTS);
            setOutputFormat("text/xml");
            setOutputSchema("EBRIM");
            setStartPosition(1);
            setMaxRecords(Short.MAX_VALUE);
            // TODO: prevent query injection
            buildQuery(params);
        }

        private void buildQuery(AVList params) throws Exception
        {
            String keywordText = params.getStringValue(CatalogKey.KEYWORD_TEXT);
            boolean minDateEnabled = getBooleanValue(params, CatalogKey.MIN_DATE_ENABLED);
            boolean maxDateEnabled = getBooleanValue(params, CatalogKey.MAX_DATE_ENABLED);
            boolean bboxEnabled = getBooleanValue(params, CatalogKey.BBOX_ENABLED);
            boolean isSimpleQuery = getBooleanValue(params, CatalogKey.SIMPLE_QUERY);
            boolean isWMS = getBooleanValue(params, CatalogKey.WMS_ENABLED);
            boolean isWFS = getBooleanValue(params, CatalogKey.WFS_ENABLED);
            boolean isWCS = getBooleanValue(params, CatalogKey.WCS_ENABLED);

            String queryTypeNames = "rim:Service=srv";
            String elementTypeNames = "srv";
            if (isWMS || isWFS || isWCS)
            {
                queryTypeNames += " rim:Classification=cls rim:ClassificationNode=clsNode";
            }
            if (!isSimpleQuery)
            {
                if (bboxEnabled)
                {
                    queryTypeNames += " rim:ExtrinsicObject=eo rim:Association=assoc";
                }
            }

            Query query = addQuery(queryTypeNames);
            ElementSetName elementSetName = query.addElementSetName(ElementSetType.FULL);
            elementSetName.setTypeNames(elementTypeNames);
            Constraint constraint = query.addConstraint();
            constraint.setVersion("1.1.0");
            Filter filter = constraint.addFilter();

            BinaryLogicalOperator logicOps = new And();
            filter.addLogicalOperator(logicOps);

            if (keywordText != null && keywordText.length() > 0)
            {
                addKeywords(logicOps, keywordText);
            }

            if (isWMS || isWFS || isWCS)
            {
                addServiceTypes(logicOps, isWMS, isWFS, isWCS);
            }

            if (!isSimpleQuery)
            {
                if (minDateEnabled || maxDateEnabled)
                {
                    Date minDate = minDateEnabled ? getDateValue(params, CatalogKey.MIN_DATE) : null;
                    Date maxDate = maxDateEnabled ? getDateValue(params, CatalogKey.MAX_DATE) : null;
                    if (minDate != null || maxDate != null)
                    {
                        addDateRange(logicOps, minDate, maxDate);
                    }
                }

                if (bboxEnabled)
                {
                    Angle minLat = getAngleValue(params, CatalogKey.MIN_LATITUDE);
                    Angle maxLat = getAngleValue(params, CatalogKey.MAX_LATITUDE);
                    Angle minLon = getAngleValue(params, CatalogKey.MIN_LONGITUDE);
                    Angle maxLon = getAngleValue(params, CatalogKey.MAX_LONGITUDE);
                    if (minLat != null && maxLat != null && minLon != null && maxLon != null)
                    {
                        Sector bounds = new Sector(minLat, maxLat, minLon, maxLon);
                        addBoundingBox(logicOps, bounds);
                    }
                }
            }
        }

        private void addKeywords(BinaryLogicalOperator logicOps, String keywordText)
                throws Exception
        {
            List<String> keywords = null;
            keywordText = keywordText.trim();
            if (keywordText.length() > 0)
            {
                String[] tokens = keywordText.split("[ ,]");
                if (tokens != null && tokens.length > 0)
                {
                    for (String s : tokens)
                    {
                        if (s != null)
                        {
                            s = s.trim();
                            if (s.length() > 0)
                            {
                                if (keywords == null)
                                    keywords = new ArrayList<String>();
                                keywords.add("*" + s + "*");
                            }
                        }
                    }
                }
            }

            // WMS 1.1 Service/Title
            // WFS 1.1 ServiceIdentification/Name
            //"/$srv/Name/LocalizedString/@value"

            // WMS 1.1 Service/Name
            // WFS 1.1 ServiceIdentification/Title
            //"/$srv/Slot[@name='Title']/ValueList/Value"

            // WMS 1.1 Service/Abstract
            // WFS 1.1 ServiceIdentification/Abstract
            //"/$srv/Description/LocalizedString/@value"

            // WMS 1.1 Service/KeywordList/Keyword
            //"/$srv/Slot[@name='KeywordList']/ValueList/Value"

            // WFS 1.1 ServiceIdentification/Keywords/Keyword
            //"/$service/Slot[@name='Keyword']/ValueList/Value"
            //"/$service/Slot[@name='Keywords']/ValueList/Value"

            if (keywords != null && keywords.size() > 0)
            {
                for (String s : keywords)
                {
                    PropertyIsLike like = new PropertyIsLike();
                    like.addPropertyName("/$srv/Description/LocalizedString/@value");
                    like.addLiteral(s);
                    logicOps.addComparisonOperator(like);
                }
            }
        }

        private void addServiceTypes(BinaryLogicalOperator logicOps, boolean isWMS, boolean isWFS, boolean isWCS)
                throws Exception
        {
            PropertyIsEqualTo equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$srv/@id"));
            equalTo.addExpression(new PropertyName("/$cls/@classifiedObject"));
            logicOps.addComparisonOperator(equalTo);

            equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$cls/@classificationNode"));
            equalTo.addExpression(new PropertyName("/$clsNode/@id"));
            logicOps.addComparisonOperator(equalTo);

            Or or = new Or();
            logicOps.addLogicalOperator(or);

            if (isWMS)
            {
                PropertyIsLike like = new PropertyIsLike();
                like.addPropertyName("/$clsNode/@code");
                like.addLiteral("*WMS*");
                or.addComparisonOperator(like);
            }
            if (isWFS)
            {
                PropertyIsLike like = new PropertyIsLike();
                like.addPropertyName("/$clsNode/@code");
                like.addLiteral("*WFS*");
                or.addComparisonOperator(like);
            }
            if (isWCS)
            {
                PropertyIsLike like = new PropertyIsLike();
                like.addPropertyName("/$clsNode/@code");
                like.addLiteral("*WCS*");
                or.addComparisonOperator(like);
            }
        }

        private void addDateRange(BinaryLogicalOperator logicOps, Date minDate, Date maxDate)
                throws Exception
        {
            String propertyName = "/$srv/Slot[@name='Harvest Date']/ValueList/Value";
            if (minDate != null)
            {
                String min = ParserUtils.formatAsOGCDate(minDate);
                PropertyIsGreaterThanOrEqualTo gequal = new PropertyIsGreaterThanOrEqualTo();
                gequal.addExpression(new PropertyName(propertyName));
                gequal.addExpression(new Literal(min));
                logicOps.addComparisonOperator(gequal);

            }
            if (maxDate != null)
            {
                String max = ParserUtils.formatAsOGCDate(maxDate);
                PropertyIsLessThanOrEqualTo lequal = new PropertyIsLessThanOrEqualTo();
                lequal.addExpression(new PropertyName(propertyName));
                lequal.addExpression(new Literal(max));
                logicOps.addComparisonOperator(lequal);
            }
        }

        private void addBoundingBox(BinaryLogicalOperator logicOps, Sector bounds) throws Exception
        {
            PropertyIsEqualTo equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$srv/@id"));
            equalTo.addExpression(new PropertyName("/$assoc/@sourceObject"));
            logicOps.addComparisonOperator(equalTo);

            equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$eo/@id"));
            equalTo.addExpression(new PropertyName("/$assoc/@targetObject"));
            logicOps.addComparisonOperator(equalTo);

            equalTo = new PropertyIsEqualTo();
            equalTo.addExpression(new PropertyName("/$assoc/@associationType"));
            equalTo.addExpression(new Literal("Offers"));
            logicOps.addComparisonOperator(equalTo);

            BBOX bbox = new BBOX();
            bbox.addPropertyName("/$eo/Slot[@name='FootPrint']/ValueList/Value");
            bbox.addEnvelope(bounds);
            logicOps.addSpatialOperator(bbox);
        }
    }

    protected static class ResponseParser extends SAXResponseParser
    {
        private GetServices getServices;

        public ResponseParser(GetServices getServices)
        {
            this.getServices = getServices;
        }

        protected void doStartDocument(String name, org.xml.sax.Attributes attributes)
        {
            GetRecordsResponseParser parser = new GetRecordsResponseParser(name, attributes, this.getServices);
            setDocumentElement(parser);
        }
    }

    protected static class GetRecordsResponseParser extends ElementParser
    {
        private GetServices getServices;
        private Service currentService = null;

        public GetRecordsResponseParser(String name, org.xml.sax.Attributes attributes, GetServices getServices)
        {
            super(name, attributes);
            this.getServices = getServices;
        }

        protected void doStartElement(String name, org.xml.sax.Attributes attributes)
        {
            // Skip the "SearchStatus" element.
            // Skip the "SearchResults" element.

            if (ServiceParser.ELEMENT_NAME.equalsIgnoreCase(name))
            {
                ServiceParser parser = new ServiceParser(name, attributes);
                this.currentService = parser;
                setCurrentElement(parser);
            }
        }

        protected void doEndElement(String name)
        {
            if (ServiceParser.ELEMENT_NAME.equalsIgnoreCase(name))
            {
                if (this.currentService != null)
                {
                    this.getServices.addService(this.currentService);
                    this.currentService = null;
                }
            }
        }
    }

    protected static boolean getBooleanValue(AVList avList, String key)
    {
        Object o = avList.getValue(key);
        if (o == null)
            return false;

        if (o instanceof Boolean)
            return (Boolean) o;

        String v = AVListImpl.getStringValue(avList, key);
        //noinspection SimplifiableIfStatement
        if (v == null)
            return false;

        return Boolean.parseBoolean(v);
    }

    protected static Date getDateValue(AVList avList, String key)
    {
        Object o = avList.getValue(key);
        if (o == null)
            return null;

        if (o instanceof Date)
            return (Date) o;

        Long l = AVListImpl.getLongValue(avList, key);
        if (l != null)
            return new Date(l);

        String v = AVListImpl.getStringValue(avList, key);
        if (v == null)
            return null;

        try
        {
            return DateFormat.getDateInstance().parse(v);
        }
        catch (java.text.ParseException e)
        {
            Logging.logger().log(Level.SEVERE, "Configuration.ConversionError", v);
            return null;
        }
    }

    protected static Angle getAngleValue(AVList avList, String key)
    {
        Object o = avList.getValue(key);
        if (o == null)
            return null;

        if (o instanceof Angle)
            return (Angle) o;

        Double d = AVListImpl.getDoubleValue(avList, key);
        if (d != null)
            return Angle.fromDegrees(d);

        return null;
    }
}
