/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwindow.core;

/**
 * @author tag
 * @version $Id: Constants.java 13825 2010-09-18 00:18:35Z tgaskins $
 */
public interface Constants
{
    // Names and titles
    static final String APPLICATION_DISPLAY_NAME = "gov.nasa.worldwindow.ApplicationDisplayName";

    // Services
    public static final String IMAGE_SERVICE = "gov.nasa.worldwindow.ImageService";

    // Core object IDs
    static final String APP_PANEL = "gov.nasa.worldwindow.AppPanel";
    static final String APP_FRAME = "gov.nasa.worldwindow.AppFrame";
    static final String APPLET_PANEL = "gov.nasa.worldwindow.AppletPanel";
    static final String CONTROLS_PANEL = "gov.nasa.worldwindow.ControlsPanel";
    static final String MENU_BAR = "gov.nasa.worldwindow.MenuBar";
    static final String NETWORK_STATUS_SIGNAL = "gov.nasa.worldwindow.NetworkStatusSignal";
    static final String TOOL_BAR = "gov.nasa.worldwindow.ToolBar";
    static final String STATUS_PANEL = "gov.nasa.worldwindow.StatusPanel";
    static final String WW_PANEL = "gov.nasa.worldwindow.WWPanel";

    // Miscellaneous
    static final String ACCELERATOR_SUFFIX = ".Accelerator";
    static final String ACTION_COMMAND = "gov.nasa.worldwindow.ActionCommand";
    static final String CONTEXT_MENU_INFO = "gov.nasa.worldwindow.ContextMenuString";
    static final String FILE_MENU = "gov.nasa.worldwindow.feature.FileMenu";
    static final String INFO_PANEL_TEXT = "gov.nasa.worldwindow.InfoPanelText";
    static final String ON_STATE = "gov.nasa.worldwindow.OnState";
    static final String RADIO_GROUP = "gov.nasa.worldwindow.StatusBarMessage";
    static final String STATUS_BAR_MESSAGE = "gov.nasa.worldwindow.StatusBarMessage";

    // Layer types
    static final String INTERNAL_LAYER = "gov.nasa.worldwindow.InternalLayer"; // application controls, etc.
    static final String ACTIVE_LAYER = "gov.nasa.worldwindow.ActiveLayer"; // force display in active layers
    static final String USER_LAYER = "gov.nasa.worldwindow.UserLayer"; // User-generated layers
    static final String SCREEN_LAYER = "gov.nasa.worldwindow.ScreenLayer";
    // in-screen application controls, etc.

    // Feature IDs
    static final String FEATURE = "gov.nasa.worldwindow.feature";
    static final String FEATURE_ID = "gov.nasa.worldwindow.FeatureID";
    static final String FEATURE_ACTIVE_LAYERS_PANEL = "gov.nasa.worldwindow.feature.ActiveLayersPanel";
    static final String FEATURE_COMPASS = "gov.nasa.worldwindow.feature.Compass";
    static final String FEATURE_CROSSHAIR = "gov.nasa.worldwindow.feature.Crosshair";
    static final String FEATURE_COORDINATES_DISPLAY = "gov.nasa.worldwindow.feature.CoordinatesDisplay";
    static final String FEATURE_EXTERNAL_LINK_CONTROLLER = "gov.nasa.worldwindow.feature.ExternalLinkController";
    static final String FEATURE_GAZETTEER = "gov.nasa.worldwindow.feature.Gazetteer";
    static final String FEATURE_GAZETTEER_PANEL = "gov.nasa.worldwindow.feature.GazetteerPanel";
    static final String FEATURE_GRATICULE = "gov.nasa.worldwindow.feature.Graticule";
    static final String FEATURE_ICON_CONTROLLER = "gov.nasa.worldwindow.feature.IconController";
    static final String FEATURE_IMPORT_IMAGERY = "gov.nasa.worldwindow.feature.ImportImagery";
    static final String FEATURE_INFO_PANEL_CONTROLLER = "gov.nasa.worldwindow.feature.InfoPanelController";
    static final String FEATURE_LAYER_MANAGER_DIALOG = "gov.nasa.worldwindow.feature.LayerManagerDialog";
    static final String FEATURE_LAYER_MANAGER = "gov.nasa.worldwindow.feature.LayerManager";
    static final String FEATURE_LAYER_MANAGER_PANEL = "gov.nasa.worldwindow.feature.LayerManagerPanel";
    static final String FEATURE_LATLON_GRATICULE = "gov.nasa.worldwindow.feature.LatLonGraticule";
    static final String FEATURE_MEASUREMENT = "gov.nasa.worldwindow.feature.Measurement";
    static final String FEATURE_MEASUREMENT_DIALOG = "gov.nasa.worldwindow.feature.MeasurementDialog";
    static final String FEATURE_MEASUREMENT_PANEL = "gov.nasa.worldwindow.feature.MeasurementPanel";
    static final String FEATURE_NAVIGATION = "gov.nasa.worldwindow.feature.Navigation";
    static final String FEATURE_OPEN_FILE = "gov.nasa.worldwindow.feature.OpenFile";
    static final String FEATURE_OPEN_URL = "gov.nasa.worldwindow.feature.OpenURL";
    static final String FEATURE_SCALE_BAR = "gov.nasa.worldwindow.feature.ScaleBar";
    static final String FEATURE_TOOLTIP_CONTROLLER = "gov.nasa.worldwindow.feature.ToolTipController";
    static final String FEATURE_UTM_GRATICULE = "gov.nasa.worldwindow.feature.UTMGraticule";
    static final String FEATURE_WMS_PANEL = "gov.nasa.worldwindow.feature.WMSPanel";
    static final String FEATURE_WMS_DIALOG = "gov.nasa.worldwindow.feature.WMSDialog";

    // Specific properties
    static final String FEATURE_OWNER_PROPERTY = "gov.nasa.worldwindow.FeatureOwnerProperty";
    static final String TOOL_BAR_ICON_SIZE_PROPERTY = "gov.nasa.worldwindow.ToolBarIconSizeProperty";
}
