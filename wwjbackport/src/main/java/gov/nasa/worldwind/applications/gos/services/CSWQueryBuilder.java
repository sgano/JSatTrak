/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.services;

import gov.nasa.worldwind.applications.gos.GeodataKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.WWUtil;

import java.text.*;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: CSWQueryBuilder.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public class CSWQueryBuilder
{
    protected AVList params;

    public CSWQueryBuilder(AVList params)
    {
        this.params = (params != null) ? params.copy() : null;
    }

    public String getGetRecordsString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<csw:GetRecords xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\" version=\"2.0.2\" service=\"CSW\" resultType=\"results\"");

        // Set the starting record position in the result feed.
        Object o = this.params.getValue(GeodataKey.RECORD_START_INDEX);
        if (o != null)
        {
            Integer i = WWUtil.convertStringToInteger(o.toString());
            if (i != null)
                sb.append(" startPosition=\"").append(i + 1).append("\"");
        }

        // Set the maximum number of records in the result feed.
        o = this.params.getValue(GeodataKey.RECORD_PAGE_SIZE);
        if (o != null)
            sb.append(" maxRecords=\"").append(o).append("\"");

        sb.append(">");

        this.addQuery(sb);

        sb.append("</csw:GetRecords>");

        return sb.toString();
    }

    protected void addQuery(StringBuilder sb)
    {
        sb.append("<csw:Query typeNames=\"csw:Record\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:gml=\"http://www.opengis.net/gml\">");
        sb.append("<csw:ElementSetName>full</csw:ElementSetName>");

        // Set the query constraint.
        this.addConstraint(sb);

        // Set the result feed sort order.
        Object o = this.params.getValue(GeodataKey.SORT_ORDER);
        if (o != null)
            this.addSortBy(sb, o.toString());

        sb.append("</csw:Query>");
    }

    protected void addConstraint(StringBuilder sb)
    {
        StringBuilder filter = new StringBuilder();
        int filterCount = 0;

        // Set the bounding box to search, and the spatial relationship between the bounding box and the database
        // records.
        Object o = this.params.getValue(GeodataKey.BBOX);
        if (o != null && o instanceof Sector)
            filterCount += this.addBoundsFilter(filter, (Sector) o);

        // Set the content types to search.
        o = this.params.getValue(GeodataKey.CONTENT_TYPE_LIST);
        if (o != null && o instanceof Iterable)
            filterCount += this.addContentTypeFilter(filter, (Iterable) o);

        // Set the data categories to search.
        o = this.params.getValue(GeodataKey.DATA_CATEGORY_LIST);
        if (o != null && o instanceof Iterable)
            filterCount += this.addDataCategoryFilter(filter, (Iterable) o);

        // Set the modified date.
        o = this.params.getValue(GeodataKey.MODIFIED_TIME);
        if (o != null && o instanceof Long)
            filterCount += this.addDateModifiedFilter(filter, (Long) o);

        // Set the record format to search.
        o = this.params.getValue(GeodataKey.RECORD_FORMAT);
        if (o != null)
            filterCount += this.addRecordFormatFilter(filter, o.toString());

        // Set the text to be searched within record metadata. Search for any word specified in the search text.
        o = this.params.getValue(GeodataKey.SEARCH_TEXT);
        if (o != null)
            filterCount += this.addSearchTextFilter(filter, o.toString());

        if (filterCount <= 0)
            return;

        sb.append("<csw:Constraint version=\"1.1.0\">");
        sb.append("<ogc:Filter>");

        if (filterCount > 1)
            sb.append("<ogc:And>");

        sb.append(filter);

        if (filterCount > 1)
            sb.append("</ogc:And>");

        sb.append("</ogc:Filter>");
        sb.append("</csw:Constraint>");
    }

    protected void addSortBy(StringBuilder sb, String sortKey)
    {
        if (WWUtil.isEmpty(sortKey))
            return;

        // Relevance is the default sort order.
        if (sortKey.equalsIgnoreCase("relevance"))
            return;

        if (sortKey.equalsIgnoreCase("title"))
            this.addSortByProperty(sb, "dc:title", "ASC");
        else if (sortKey.equalsIgnoreCase("dateDescending"))
            this.addSortByProperty(sb, "dct:modified", "DESC");
        else if (sortKey.equalsIgnoreCase("areaAscending"))
            this.addSortByProperty(sb, "ows:BoundingBox", "ASC");
        else if (sortKey.equalsIgnoreCase("areaDescending"))
            this.addSortByProperty(sb, "ows:BoundingBox", "DESC");
    }

    protected void addSortByProperty(StringBuilder sb, String propertyName, String sortOrder)
    {
        sb.append("<ogc:SortBy>");
        sb.append("<ogc:SortProperty>");
        sb.append("<ogc:PropertyName>").append(propertyName).append("</ogc:PropertyName>");
        sb.append("<ogc:SortOrder>").append(sortOrder).append("</ogc:SortOrder>");
        sb.append("</ogc:SortProperty>");
        sb.append("</ogc:SortBy>");
    }

    protected int addBoundsFilter(StringBuilder sb, Sector sector)
    {
        sb.append("<ogc:BBOX>");
        sb.append("<ogc:PropertyName>ows:BoundingBox</ogc:PropertyName>");
        sb.append("<gml:Envelope>");
        sb.append("<gml:lowerCorner>").append(sector.getMinLongitude().degrees).append(" ").append(sector.getMinLatitude().degrees).append("</gml:lowerCorner>");
        sb.append("<gml:upperCorner>").append(sector.getMaxLongitude().degrees).append(" ").append(sector.getMaxLatitude().degrees).append("</gml:upperCorner>");
        sb.append("</gml:Envelope>");
        sb.append("</ogc:BBOX>");

        return 1;
    }

    protected int addContentTypeFilter(StringBuilder sb, Iterable iterable)
    {
        ArrayList<Object> typeList = new ArrayList<Object>();

        for (Object type : iterable)
        {
            if (type != null && !WWUtil.isEmpty(type.toString()))
                typeList.add(type);
        }

        if (typeList.isEmpty())
            return 0;

        if (typeList.size() > 1)
            sb.append("<ogc:Or>");

        for (Object type : typeList)
        {
            sb.append("<ogc:PropertyIsEqualTo>");
            sb.append("<ogc:PropertyName>dc:type</ogc:PropertyName>");
            sb.append("<ogc:Literal>").append(type).append("</ogc:Literal>");
            sb.append("</ogc:PropertyIsEqualTo>");
        }

        if (typeList.size() > 1)
            sb.append("</ogc:Or>");

        return 1;
    }

    protected int addDataCategoryFilter(StringBuilder sb, Iterable iterable)
    {
        ArrayList<Object> subjectList = new ArrayList<Object>();

        for (Object subject : iterable)
        {
            if (subject != null && !WWUtil.isEmpty(subject.toString()))
                subjectList.add(subject);
        }

        if (subjectList.isEmpty())
            return 0;

        if (subjectList.size() > 1)
            sb.append("<ogc:Or>");

        for (Object subject : subjectList)
        {
            sb.append("<ogc:PropertyIsEqualTo>");
            sb.append("<ogc:PropertyName>dc:subject</ogc:PropertyName>");
            sb.append("<ogc:Literal>").append(subject).append("</ogc:Literal>");
            sb.append("</ogc:PropertyIsEqualTo>");
        }

        if (subjectList.size() > 1)
            sb.append("</ogc:Or>");

        return 1;
    }

    protected int addDateModifiedFilter(StringBuilder sb, long dateModified)
    {
        DateFormat formatter = new SimpleDateFormat("y-M-d");
        String dateString = formatter.format(new Date(dateModified));

        sb.append("<ogc:PropertyIsGreaterThan>");
        sb.append("<ogc:PropertyName>dct:modified</ogc:PropertyName>");
        sb.append("<ogc:Literal>").append(dateString).append("</ogc:Literal>");
        sb.append("</ogc:PropertyIsGreaterThan>");

        return 1;
    }

    protected int addRecordFormatFilter(StringBuilder sb, String format)
    {
        if (WWUtil.isEmpty(format))
            return 0;

        sb.append("<ogc:PropertyIsEqualTo>");
        sb.append("<ogc:PropertyName>dct:hasFormat</ogc:PropertyName>");
        sb.append("<ogc:Literal>").append(format).append("</ogc:Literal>");
        sb.append("</ogc:PropertyIsEqualTo>");

        return 1;
    }

    protected int addSearchTextFilter(StringBuilder sb, String text)
    {
        if (WWUtil.isEmpty(text))
            return 0;

        sb.append("<ogc:PropertyIsLike wildCard=\"*\" escape=\"\\\" singleChar=\"?\">");
        sb.append("<ogc:PropertyName>AnyText</ogc:PropertyName>");
        sb.append("<ogc:Literal>").append(text).append("</ogc:Literal>");
        sb.append("</ogc:PropertyIsLike>");

        return 1;
    }
}
