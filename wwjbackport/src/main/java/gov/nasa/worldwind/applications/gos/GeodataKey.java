/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos;

/**
 * @author dcollins
 * @version $Id: GeodataKey.java 13557 2010-07-17 20:56:32Z dcollins $
 */
public interface GeodataKey
{
    final String APP_THREAD_COUNT = "gov.nasa.worldwind.gos.AppThreadCount";
    final String BBOX = "gov.nasa.worldwind.gos.BBOX";

    final String CAPABILITIES = "gov.nasa.worldwind.gos.Capabilities";
    final String CONTENT_TYPE_LIST = "gov.nasa.worldwind.gos.ContentTypeList";
    final String CSW_SERVICE_URI = "gov.nasa.worldwind.gos.CSWServiceURI";

    final String DATA_CATEGORY_LIST = "gov.nasa.worldwind.gos.DataCategoryList";
    final String DISPLAY_NAME_SHORT = "gov.nasa.worldwind.gos.DisplayNameShort";
    final String DISPLAY_NAME_LONG = "gov.nasa.worldwind.gos.DisplayNameLong";

    final String GEODATA_CONTROLLER_CLASS_NAME = "gov.nasa.worldwind.gos.GeodataControllerClassName";

    final String IMAGE = "gov.nasa.worldwind.gos.Image";
    final String IMAGE_CACHE = "gov.nasa.worldwind.gos.ImageCache";
    final String IMAGE_CACHE_SIZE = "gov.nasa.worldwind.gos.ImageCacheSize";

    final String LEGEND = "gov.nasa.worldwind.gos.Legend";

    final String MAX_RECORDS = "gov.nasa.worldwind.gos.MaxRecords";
    final String METADATA = "gov.nasa.wordlwind.gos.Metadata";
    final String METADATA_URI = "gov.nasa.worldwind.gos.MetadataURI";
    final String MODIFIED_TIME = "gov.nasa.worldwind.gos.ModifiedTime";

    final String RECORD_FORMAT = "gov.nasa.worldwind.gos.RecordFormat";
    final String RECORD_PAGE_SIZE = "gov.nasa.worldwind.gos.RecordPageSize";
    final String RECORD_LIST_LAYER_NAME = "gov.nasa.worldwind.gos.RecordListLayerName";
    final String RECORD_START_INDEX = "gov.nasa.worldwind.gos.RecordStartIndex";
    final String RETRIEVED_LAYER_SUFFIX = "gov.nasa.worldwind.gos.RetrievedLayerSuffix";

    final String SEARCH_TEXT = "gov.nasa.worldwind.gos.SearchText";
    final String SERVICE = "gov.nasa.worldwind.gos.Service";
    final String SERVICE_STATUS = "gov.nasa.worldwind.gos.ServiceStatus";
    final String SERVICE_STATUS_URI = "gov.nasa.worldwind.gos.ServiceStatusURI";
    final String SERVICE_STATUS_METADATA = "gov.nasa.worldwind.gos.ServiceStatusMetadata";
    final String SERVICE_STATUS_METADATA_URI = "gov.nasa.worldwind.gos.ServiceStatusMetadataURI";
    final String SERVICE_TYPE = "gov.nasa.worldwind.gos.ServiceType";
    final String SHOW_RECORD_ANNOTATIONS = "gov.nasa.worldwind.gos.ShowGlobeAnnotations";
    final String SHOW_RECORD_BOUNDS = "gov.nasa.worldwind.gos.ShowGlobeBounds";
    final String SHOW_SEARCH_OPTIONS = "gov.nasa.worldwind.gos.ShowSearchOptions";
    final String SORT_ORDER = "gov.nasa.worldwind.gos.SortOrder";
    final String STATE_ERROR = "gov.nasa.worldwind.gos.StateError";
    final String STATE_NORMAL = "gov.nasa.worldwind.gos.StateNormal";
    final String STATE_WAITING = "gov.nasa.worldwind.gos.StateWaiting";

    final String UUID = "gov.nasa.worldwind.gos.UUID";

    final String VERSION_STRING = "gov.nasa.worldwind.gos.VersionString";

    final String WEBSITE = "gov.nasa.worldwind.gos.Website";
}
