/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.airspaces.*;
import gov.nasa.worldwind.render.airspaces.Polygon;
import gov.nasa.worldwind.util.WWIO;

import java.io.*;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.zip.*;

/**
 * @author tag
 * @version $Id: ExtrudedShapes.java 14177 2010-12-03 00:28:05Z tgaskins $
 */
public class ExtrudedShapes extends ApplicationTemplate
{
    protected static final String DEMO_AIRSPACES_URL =
        "http://worldwind.arc.nasa.gov/java/demos/data/AirspaceBuilder-DemoShapes.zip";
    protected static ArrayList<Airspace> airspaces = new ArrayList<Airspace>();
    protected static String DEFAULT_IMAGE_URL =
        "http://worldwind.arc.nasa.gov/java/demos/Images/build123sm.jpg";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            try
            {
                RenderableLayer layer = new RenderableLayer();
                layer.setName("Extruded Shapes");
                layer.setPickEnabled(true);

                loadAirspacesFromURL(new URL(DEMO_AIRSPACES_URL), airspaces);
                String imagePath = saveURLToTempFile(new URL(DEFAULT_IMAGE_URL));

                ShapeAttributes sideAttributes = new BasicShapeAttributes();
                sideAttributes.setInteriorMaterial(Material.LIGHT_GRAY);
                sideAttributes.setOutlineMaterial(Material.DARK_GRAY);

                ShapeAttributes capAttributes = new BasicShapeAttributes(sideAttributes);
                capAttributes.setInteriorMaterial(Material.GRAY);

                int n = 0, m = 0;
                for (Airspace airspace : airspaces)
                {
                    if (airspace instanceof Polygon)
                    {
//                        if (n + 1 != 9)
//                        {
//                            ++n;
//                            continue;
//                        }

                        Polygon pgonAirspace = (Polygon) airspace;

                        ArrayList<String> textures = new ArrayList<String>();
                        for (LatLon location : pgonAirspace.getLocations())
                        {
                            textures.add(imagePath);
                        }

                        double height = 40; // building height
                        ExtrudedPolygon quad = new ExtrudedPolygon(pgonAirspace.getLocations(), height, textures);

                        quad.setSideAttributes(sideAttributes);
                        quad.setCapAttributes(capAttributes);

                        if (pgonAirspace.getLocations().size() == 4)
                        {
                            float[] texCoords = new float[] {0, 0, 1, 0, 1, 1, 0, 1};
                            quad.setCapTextureImageSource("images/32x32-icon-nasa.png", texCoords, 4);
                        }

                        layer.addRenderable(quad);

                        ++n;
                        m += ((Polygon) airspace).getLocations().size();
                    }
                }

//                for (int i = 0; i < 1000; i++)
//                {
//                    ExtrudedPolygon quad = new ExtrudedPolygon(((Polygon) airspaces.get(0)).getLocations(), 40);
//                    quad.setSideAttributes(sideAttributes);
//                    quad.setCapAttributes(capAttributes);
//                    ArrayList<String> textures = new ArrayList<String>(quad.getLocations().size());
//                    for (int j = 0; j < quad.getLocations().size(); j++)
//                    {
//                        textures.add(DEFAULT_IMAGE);
//                    }
//                    quad.setImageSources(textures);
//                    layer.addRenderable(quad);
//                    ++n;
//                    m += ((Polygon) airspaces.get(0)).getLocations().size();
//                }

                System.out.printf("NUM SHAPES = %d, NUM SIDES = %d\n", n, m);

                insertBeforePlacenames(this.getWwd(), layer);

                this.getLayerPanel().update(this.getWwd());

                View view = getWwd().getView();
                view.setEyePosition(Position.fromDegrees(47.656, -122.306, 1e3));

                layer.setPickEnabled(true);
                getWwd().addSelectListener(new SelectListener()
                {
                    public void selected(SelectEvent event)
                    {
                        if (event.getTopObject() instanceof ExtrudedPolygon)
                            System.out.println("EXTRUDED POLYGON");
                    }
                });
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    protected static String saveURLToTempFile(URL url)
    {
        File file = null;
        try
        {
            String prefix = WWIO.getFilename(WWIO.replaceSuffix(url.toString(), null));
            String suffix = WWIO.getSuffix(url.toString());
            file = File.createTempFile(prefix, "." + suffix);
            file.deleteOnExit();

            ByteBuffer buffer = WWIO.readURLContentToBuffer(url);
            WWIO.saveBuffer(buffer, file);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return (file != null) ? file.getAbsolutePath() : null;
    }

    protected static void loadAirspacesFromURL(URL url, Collection<Airspace> airspaces)
    {
        File file = null;
        try
        {
            file = File.createTempFile("AirspaceBuilder-TempFile", null);

            InputStream is = url.openStream();
            OutputStream os = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int length;
            while ((length = is.read(buffer)) > 0)
            {
                os.write(buffer, 0, length);
            }

            is.close();
            os.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (file == null)
            return;

        try
        {
            ZipFile zipFile = new ZipFile(file);

            ZipEntry entry = null;
            for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements(); entry = e.nextElement())
            {
                if (entry == null)
                    continue;

                String name = entry.getName();
                name = getFileName(name);

                if (!(name.startsWith("gov.nasa.worldwind.render.airspaces") && name.endsWith(".xml")))
                    continue;

                String[] tokens = name.split("-");

                try
                {
                    Class c = Class.forName(tokens[0]);
                    Airspace airspace = (Airspace) c.newInstance();
                    BufferedReader input = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
                    String s = input.readLine();
                    airspace.restoreState(s);
                    airspaces.add(airspace);

                    if (tokens.length >= 2)
                    {
                        airspace.setValue(AVKey.DISPLAY_NAME, tokens[1]);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static String getFileName(String s)
    {
        int index = s.lastIndexOf("/");
        if (index == -1)
            index = s.lastIndexOf("\\");
        if (index != -1 && index < s.length())
            return s.substring(index + 1, s.length());
        return s;
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Extruded Polygons on Ground", AppFrame.class);
    }
}
