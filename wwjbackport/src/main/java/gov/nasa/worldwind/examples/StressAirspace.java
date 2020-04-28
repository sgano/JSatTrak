/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.layers.AirspaceLayer;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.airspaces.*;
import gov.nasa.worldwind.render.Material;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.*;


/**
 * @author jparsons
 * @version $Id$
 */
public class StressAirspace {

    private AirspaceLayer airspaceLayer;
    public static final String DRAW_AIRSPACE_EXTENT = "gov.nasa.worldwind.avkey.DrawAirspaceExtent";
    public static final String DRAW_AIRSPACE_WIREFRAME = "gov.nasa.worldwind.avkey.DrawAirspaceWireframe";
    public static final String AIRSPACE_LAYER_NAME = "Airspaces";
    private int counter=0;

    private static final Material[] materials = new Material[]
    {
        new Material(Color.ORANGE),
        new Material(Color.YELLOW),
        new Material(Color.GREEN),
        new Material(Color.MAGENTA),
        new Material(Color.CYAN),
        new Material(Color.LIGHT_GRAY),
        new Material(Color.GRAY),
        new Material(Color.DARK_GRAY),
        new Material(Color.BLACK),
        new Material(Color.RED),
        new Material(Color.PINK),
        new Material(Color.BLUE)
    };


    public StressAirspace()
    {
        // Construct a layer that will hold the airspaces and annotations.
        this.airspaceLayer = new AirspaceLayer();
        this.airspaceLayer.setName(AIRSPACE_LAYER_NAME);

        String s = Configuration.getStringValue(DRAW_AIRSPACE_EXTENT);
        if (s != null)
            this.airspaceLayer.setDrawExtents(Boolean.parseBoolean(s));

        s = Configuration.getStringValue(DRAW_AIRSPACE_WIREFRAME);
        if (s != null)
            this.airspaceLayer.setDrawWireframe(Boolean.parseBoolean(s));
    }


    public void clearAirspace()
    {
        counter=0;
        this.airspaceLayer.removeAllAirspaces();
    }

    public AirspaceLayer getAirspaceLayer()
    {
        return airspaceLayer;
    }

    public int size()
    {
        return counter;
    }

    public void addAirspace(Position pos)
    {
        Airspace newAirspace;
        switch ( size() % 4)
        {
            case 0:
                CappedCylinder cyl = new CappedCylinder();
                cyl.getAttributes().setMaterial(materials[(size()   % materials.length)] );
                cyl.getAttributes().setOpacity(0.8);
                cyl.setCenter(pos);
                double radius = 30000.0 + (30000.0 * Math.random());
                cyl.setRadius(radius);
                double altTop = 10000.0 + (100000.0 * Math.random());
                cyl.setAltitudes(5000.0, altTop);
                cyl.setTerrainConforming(true, true);
                newAirspace = cyl;
                break;
           case 1:
                Curtain curtain = new Curtain();
                curtain.getAttributes().setMaterial(materials[(size()   % materials.length)] );
                double endLat = (pos.getLatitude().getDegrees() > 0) ? ( pos.getLatitude().getDegrees() -1) : ( pos.getLatitude().getDegrees()  + 1);
                double endLon = (pos.getLongitude().getDegrees() > 0) ? ( pos.getLongitude().getDegrees() -1) : ( pos.getLongitude().getDegrees()  + 1);
                double endLat2 = (endLat > 0) ? ( endLat -1) : ( endLat  + 1);
                curtain.setLocations(Arrays.asList(pos, LatLon.fromDegrees(endLat, endLon), LatLon.fromDegrees(endLat2, endLon)));
                curtain.setAltitudes(1000.0, 100000.0);
                curtain.setTerrainConforming(true, false);
                newAirspace = curtain;
                break;
           case 2:
                Cake cake = new Cake();
                cake.getAttributes().setMaterial(materials[(size()   % materials.length)]);
                cake.getAttributes().setOpacity(0.8);
                cake.setLayers(Arrays.asList(
                    new Cake.Layer(pos, 20000.0, Angle.fromDegrees(190.0),
                        Angle.fromDegrees(170.0), 10000.0, 20000.0),
                    new Cake.Layer(pos, 25000.0, Angle.fromDegrees(190.0),
                        Angle.fromDegrees(90.0), 21000.0, 30000.0),
                    new Cake.Layer(pos, 12500.0, Angle.fromDegrees(270.0),
                        Angle.fromDegrees(60.0), 32000.0, 39000.0)));
                cake.getLayers().get(0).setTerrainConforming(false, false);
                cake.getLayers().get(1).setTerrainConforming(false, false);
                cake.getLayers().get(2).setTerrainConforming(false, true);
                newAirspace = cake;
                break;
           default:
                Route route = new Route();
                route.getAttributes().setOpacity(0.8);
                route.getAttributes().setMaterial(materials[(size()   % materials.length)]);
                route.setAltitudes(5000.0, 20000.0);
                route.setWidth(20000.0);
                endLat = (pos.getLatitude().getDegrees() > 0) ? ( pos.getLatitude().getDegrees() -1) : ( pos.getLatitude().getDegrees()  + 1);
                endLon = (pos.getLongitude().getDegrees() > 0) ? ( pos.getLongitude().getDegrees() -1) : ( pos.getLongitude().getDegrees()  + 1);
                endLat2 = (endLat > 0) ? ( endLat -1) : ( endLat  + 1);
                route.setLocations(Arrays.asList(
                    pos,
                    LatLon.fromDegrees(endLat, endLon),
                    LatLon.fromDegrees(endLat2, endLon)));
                route.setTerrainConforming(false, true);
                newAirspace = route;
                break;
        }

        this.airspaceLayer.addAirspace(newAirspace);
        counter++;
    }


}
