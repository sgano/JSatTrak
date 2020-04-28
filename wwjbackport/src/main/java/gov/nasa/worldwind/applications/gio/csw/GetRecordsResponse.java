/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.csw;

/**
 * @author dcollins
 * @version $Id: GetRecordsResponse.java 5465 2008-06-24 00:17:03Z dcollins $
 */
public interface GetRecordsResponse
{
    RequestId getRequestId();

    void setRequestId(RequestId requestId);

    RequestStatus getSearchStatus();

    void setSearchStatus(RequestStatus searchStatus);

    SearchResults getSearchResults();

    void setSearchResults(SearchResults searchResults);

    String getVersion();

    void setVersion(String version);
}
