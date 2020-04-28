/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples.util;

import gov.nasa.worldwind.formats.shapefile.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

/**
 * Converts Shapefile geometry into World Wind renderable objects. Shapefile geometries are mapped to World Wind objects
 * as follows: <table> <tr><th>Shapefile Geomerty</th><th>World Wind Object</th></tr> <tr><td>Point</td><td>{@link
 * gov.nasa.worldwind.render.WWIcon}</td></tr> <tr><td>MultiPoint</td><td>List of {@link
 * gov.nasa.worldwind.render.WWIcon}</td></tr> <tr><td>Polyline</td><td>{@link gov.nasa.worldwind.render.SurfacePolylines}</td></tr>
 * <tr><td>Polygon</td><td>{@link gov.nasa.worldwind.render.SurfacePolygons}</td></tr> </table>
 * <p/>
 * Shapefiles do not contain a standard definition for color and other visual attributes. Though some Shapefiles contain
 * color information in each record's key-value attributes, ShapefileLoader does not attempt to interpret that
 * information. Instead, the World Wind renderable objects created by ShapefileLoader are assigned a random color.
 * Callers can replace or extent this behavior by defining a subclass of ShapefileLoader and overriding the following
 * methods: <ul> <li>{@link #createPointIconSource(gov.nasa.worldwind.formats.shapefile.ShapefileRecord)}</li>
 * <li>{@link #createPolylineAttributes(gov.nasa.worldwind.formats.shapefile.ShapefileRecord)}</li> <li>{@link
 * #createPolygonAttributes(gov.nasa.worldwind.formats.shapefile.ShapefileRecord)}</li></ul>.
 *
 * @author dcollins
 * @version $Id: ShapefileLoader.java 13791 2010-09-13 18:10:48Z tgaskins $
 */
public class ShapefileLoader
{
    protected static final RandomShapeAttributes randomAttrs = new RandomShapeAttributes();

    /** Constructs a ShapefileLoader, but otherwise does nothing. */
    public ShapefileLoader()
    {
    }

    /**
     * Creates a {@link gov.nasa.worldwind.layers.Layer} from a general Shapefile source. The source type may be one of
     * the following: <ul> <li>{@link java.io.InputStream}</li> <li>{@link java.net.URL}</li> <li>{@link
     * java.io.File}</li> <li>{@link String} containing a valid URL description or a file or resource name available on
     * the classpath.</li> </ul>
     *
     * @param source the source of the Shapefile.
     *
     * @return a Layer that renders the Shapefile's contents on the surface of the Globe.
     *
     * @throws IllegalArgumentException if the source is null or an empty string, or if the Shapefile's primitive type
     *                                  is unrecognized.
     */
    public Layer createLayerFromSource(Object source)
    {
        if (WWUtil.isEmpty(source))
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Shapefile shp = null;
        Layer layer = null;
        try
        {
            shp = new Shapefile(source);
            layer = this.createLayerFromShapefile(shp);
        }
        finally
        {
            WWIO.closeStream(shp, source.toString());
        }

        return layer;
    }

    /**
     * Creates a {@link gov.nasa.worldwind.layers.Layer} from a general Shapefile.
     *
     * @param shp the Shapefile to create a layer for.
     *
     * @return a Layer that renders the Shapefile's contents on the surface of the Globe.
     *
     * @throws IllegalArgumentException if the Shapefile is null, or if the Shapefile's primitive type is unrecognized.
     */
    public Layer createLayerFromShapefile(Shapefile shp)
    {
        if (shp == null)
        {
            String message = Logging.getMessage("nullValue.ShapefileIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Layer layer = null;

        // TODO: Replace usage IconLayer and WWIcon with RenderableLayer and PointPlacemark after PointPlacemark
        // performance improvements are implemented.
        if (Shapefile.isPointType(shp.getShapeType()))
        {
            layer = new IconLayer();
            this.addIconsForPoints(shp, (IconLayer) layer);
        }
        else if (Shapefile.isMultiPointType(shp.getShapeType()))
        {
            layer = new IconLayer();
            this.addIconsForMultiPoints(shp, (IconLayer) layer);
        }
        else if (Shapefile.isPolylineType(shp.getShapeType()))
        {
            layer = new RenderableLayer();
            this.addRenderablesForPolylines(shp, (RenderableLayer) layer);
        }
        else if (Shapefile.isPolygonType(shp.getShapeType()))
        {
            layer = new RenderableLayer();
            this.addRenderablesForPolygons(shp, (RenderableLayer) layer);
        }
        else
        {
            Logging.logger().warning(Logging.getMessage("generic.UnrecognizedShapeType", shp.getShapeType()));
        }

        return layer;
    }

    //**************************************************************//
    //********************  Geometry Conversion  *******************//
    //**************************************************************//

    protected void addIconsForPoints(Shapefile shp, IconLayer layer)
    {
        while (shp.hasNext())
        {
            ShapefileRecord record = shp.nextRecord();
            double[] point = ((ShapefileRecordPoint) record).getPoint();
            String iconSource = this.createPointIconSource(record);
            layer.addIcon(this.createPoint(record, Position.fromDegrees(point[1], point[0], 0), iconSource));
        }
    }

    protected void addIconsForMultiPoints(Shapefile shp, IconLayer layer)
    {
        while (shp.hasNext())
        {
            ShapefileRecord record = shp.nextRecord();
            Iterable<double[]> iterable = ((ShapefileRecordMultiPoint) record).getPoints(0);
            String iconSource = this.createPointIconSource(record);

            for (double[] point : iterable)
            {
                layer.addIcon(this.createPoint(record, Position.fromDegrees(point[1], point[0], 0), iconSource));
            }
        }
    }

    protected void addRenderablesForPolylines(Shapefile shp, RenderableLayer layer)
    {
        // TODO: Replace construction of one SurfacePolylines with one SurfacePolyline per record after SurfacePolyline
        // performance improvements are implemented.

        // Reads all records from the Shapefile, but ignores each records unique information. We do this to create one
        // WWJ object representing the entire shapefile, which as of 8/10/2010 is required to display very large
        // polyline Shapefiles. To create one WWJ object for each Shapefile record, replace this method's contents with
        // the following:
        //
        //while (shp.hasNext())
        //{
        //    ShapefileRecord record = shp.nextRecord();
        //    ShapeAttributes attrs = this.createPolylineAttributes(record);
        //    layer.addRenderable(this.createPolyline(record, attrs));
        //}

        while (shp.hasNext())
        {
            shp.nextRecord();
        }

        ShapeAttributes attrs = this.createPolylineAttributes(null);
        layer.addRenderable(this.createPolyline(shp, attrs));
    }

    protected void addRenderablesForPolygons(Shapefile shp, RenderableLayer layer)
    {
        while (shp.hasNext())
        {
            ShapefileRecord record = shp.nextRecord();
            ShapeAttributes attrs = this.createPolygonAttributes(record);
            layer.addRenderable(this.createPolygon(record, attrs));
        }
    }

    //**************************************************************//
    //********************  Primitive Geometry Construction  *******//
    //**************************************************************//

    @SuppressWarnings({"UnusedDeclaration"})
    protected WWIcon createPoint(ShapefileRecord record, Position pos, String iconSource)
    {
        return new UserFacingIcon(iconSource, pos);
    }

    protected Renderable createPolyline(ShapefileRecord record, ShapeAttributes attrs)
    {
        SurfacePolylines shape = new SurfacePolylines(
            Sector.fromDegrees(((ShapefileRecordPolyline) record).getBoundingRectangle()),
            record.getCompoundPointBuffer());
        shape.setAttributes(attrs);

        return shape;
    }

    protected Renderable createPolyline(Shapefile shp, ShapeAttributes attrs)
    {
        SurfacePolylines shape = new SurfacePolylines(Sector.fromDegrees(shp.getBoundingRectangle()),
            shp.getPointBuffer());
        shape.setAttributes(attrs);

        return shape;
    }

    protected Renderable createPolygon(ShapefileRecord record, ShapeAttributes attrs)
    {
        SurfacePolygons shape = new SurfacePolygons(
            Sector.fromDegrees(((ShapefileRecordPolygon) record).getBoundingRectangle()),
            record.getCompoundPointBuffer());
        shape.setAttributes(attrs);
        // Configure the SurfacePolygons as a single large polygon.
        shape.setPolygonRingGroups(new int[] {0});

        return shape;
    }

    //**************************************************************//
    //********************  Attribute Construction  ****************//
    //**************************************************************//

    @SuppressWarnings({"UnusedDeclaration"})
    protected String createPointIconSource(ShapefileRecord record)
    {
        return "images/load-dot.png";
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected ShapeAttributes createPolylineAttributes(ShapefileRecord record)
    {
        return randomAttrs.nextPolylineAttributes();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    protected ShapeAttributes createPolygonAttributes(ShapefileRecord record)
    {
        return randomAttrs.nextPolygonAttributes();
    }
}
