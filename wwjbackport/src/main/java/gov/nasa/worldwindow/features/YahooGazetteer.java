/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwindow.features;

import gov.nasa.worldwind.exception.*;
import gov.nasa.worldwind.poi.*;
import gov.nasa.worldwindow.core.*;

import java.util.List;

/**
 * @author tag
 * @version $Id: YahooGazetteer.java 13273 2010-04-10 09:18:33Z tgaskins $
 */
public class YahooGazetteer extends AbstractFeature implements Gazetteer
{
    private Gazetteer gazetteer;

    public YahooGazetteer()
    {
        this(null);
    }

    public YahooGazetteer(Registry registry)
    {
        super("Gazeteer", Constants.FEATURE_GAZETTEER, null, registry);
    }

    @Override
    public void initialize(Controller controller)
    {
        super.initialize(controller);

        this.gazetteer = new gov.nasa.worldwind.poi.YahooGazetteer();
    }

    public List<PointOfInterest> findPlaces(String placeInfo) throws NoItemException, ServiceException
    {
        return this.gazetteer.findPlaces(placeInfo);
    }
}
