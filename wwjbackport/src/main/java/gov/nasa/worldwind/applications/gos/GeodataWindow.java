/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.applications.gos.event.SearchListener;
import gov.nasa.worldwind.avlist.AVList;

import javax.swing.*;

/**
 * @author dcollins
 * @version $Id: GeodataWindow.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public interface GeodataWindow
{
    boolean isEnabled();

    void setEnabled(boolean enabled);

    String getContentState();

    void setContentState(String state);

    JComponent getContentComponent(String state);

    void setContentComponent(String state, JComponent component);

    RecordList getRecordList();

    void setRecordList(RecordList recordList, AVList searchParams);

    GeodataController getGeodataController();

    void setGeodataController(GeodataController geodataController);

    WorldWindow getWorldWindow();

    void setWorldWindow(WorldWindow wwd);

    void addSearchListener(SearchListener listener);

    void removeSearchListener(SearchListener listener);

    SearchListener[] getSearchListeners();
}
