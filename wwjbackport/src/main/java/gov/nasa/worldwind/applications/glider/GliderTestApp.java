/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.applications.glider;

import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.util.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.beans.*;
import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.List;

/**
 * @author tag
 * @version $Id: GliderTestApp.java 13200 2010-03-11 19:27:48Z dcollins $
 */
public class GliderTestApp extends ApplicationTemplate
{
    public static class GliderAppPanel extends AppPanel
    {
        public GliderAppPanel(Dimension canvasSize, boolean includeStatusBar)
        {
            super(canvasSize, includeStatusBar);
        }

        @Override
        protected WorldWindowGLCanvas createWorldWindow()
        {
            return new GliderWorldWindow();
        }
    }

    public static class GliderAppFrame extends AppFrame
    {
        public GliderAppFrame()
        {
            super(true, true, false);
        }

        @Override
        protected AppPanel createAppPanel(Dimension canvasSize, boolean includeStatusBar)
        {
            return new GliderAppPanel(canvasSize, includeStatusBar);
        }
    }

//    private static Sector cloudSector = Sector
//        .fromDegrees(10.879667319999989, 48.55774732, -134.459224670811, -96.781144670811);

    private static LatLon nw = LatLon.fromDegrees(48.55774732, -134.459224670811);
    private static LatLon ne = nw.add(LatLon.fromDegrees(0, 0.036795 * 250));
    private static LatLon se = nw.add(LatLon.fromDegrees(-0.036795 * 200, 0.036795 * 250));
    private static LatLon sw = nw.add(LatLon.fromDegrees(-0.036795 * 200, 0));
    private static List<LatLon> corners = Arrays.asList(sw, se, ne, nw);

    private static String cloudImagePath =
//        "testData/Hawaii/Maui/imagery/landsat_maui-kahoolawe-geo.png";
    "testData/GLIDERTestImage.PNG";
//        "src/images/BMNG_world.topo.bathy.200405.3.2048x1024.jpg";
//    "src/images/earth-map-512x256.png";

    private static String currentPath = null;

    private static float[][] readLocations()
    {
        try
        {
            MappedByteBuffer bufferLon = WWIO.mapFile(new File("/Users/tag/No Backup/lon.txt"));
            MappedByteBuffer bufferLat = WWIO.mapFile(new File("/Users/tag/No Backup/lat.txt"));

            bufferLon.order(ByteOrder.BIG_ENDIAN);
            bufferLat.order(ByteOrder.BIG_ENDIAN);

            FloatBuffer latBuffer = bufferLat.asFloatBuffer();
            FloatBuffer lonBuffer = bufferLon.asFloatBuffer();

            float[] lats = new float[latBuffer.limit()];
            float[] lons = new float[lonBuffer.limit()];

            for (int i = 0; i < lats.length; i++)
            {
                lats[i] = latBuffer.get(i);
                lons[i] = lonBuffer.get(i);
            }

            return new float[][] {lats, lons};
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private static float[][] makeField(List<LatLon> corners, int width, int height, Angle angle)
    {
        Sector sector = Sector.boundingSector(corners);
        double dLat = sector.getDeltaLatDegrees() / (height - 1d);
        double dLon = sector.getDeltaLonDegrees() / (width - 1d);

        float[] lons = new float[width * height];
        float[] lats = new float[lons.length];

        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                lons[j * width + i] = (float) (sector.getMinLongitude().degrees + i * dLon);
                lats[j * width + i] = (float) (sector.getMaxLatitude().degrees - j * dLat);
            }
        }

        double cosAngle = angle.cos();
        double sinAngle = angle.sin();

        LatLon c = sector.getCentroid();
        float cx = (float) c.getLongitude().degrees;
        float cy = (float) c.getLatitude().degrees;

        for (int j = 0; j < height; j++)
        {
            for (int i = 0; i < width; i++)
            {
                int index = j * width + i;

                float x = lons[index];
                float y = lats[index];

                lons[index] = (float) ((x - cx) * cosAngle - (y - cy) * sinAngle + cx);
                lats[index] = (float) ((x - cx) * sinAngle + (y - cy) * cosAngle + cy);
            }
        }

        return new float[][] {lats, lons};
    }

    private static ArrayList<LatLon> makeBorder(float[][] field, int width, int height, ArrayList<LatLon> latLons)
    {
        for (int i = 0; i < width; i++)
            latLons.add(LatLon.fromDegrees(field[0][i], field[1][i]));
        for (int i = 2 * width - 1; i < height * width; i += width)
            latLons.add(LatLon.fromDegrees(field[0][i], field[1][i]));
        for (int i = width * height - 2; i > width * (height - 1); i--)
            latLons.add(LatLon.fromDegrees(field[0][i], field[1][i]));
        for (int i = width * (height - 2); i > 0; i-= width)
            latLons.add(LatLon.fromDegrees(field[0][i], field[1][i]));

        return latLons;
    }

    public static void main(String[] args)
    {
        final ImageUtil.AlignedImage projectedImage;
        final String imageName;
        final BufferedImage testImage;
        final ArrayList<LatLon> latLons = new ArrayList<LatLon>();

        final AppFrame frame = start("GLIDER Test Application", GliderAppFrame.class);

        try
        {
            final File imageFile = new File(cloudImagePath);
            testImage = ImageIO.read(imageFile);
            long start = System.currentTimeMillis();
            float[][] field = makeField(corners, testImage.getWidth(), testImage.getHeight(), Angle.fromDegrees(15));
            makeBorder(field, testImage.getWidth(), testImage.getHeight(), latLons);
            projectedImage = GliderImage.alignImage(ImageIO.read(imageFile), field[0], field[1]);
            System.out.printf("Image projected, %d ms\n", System.currentTimeMillis() - start);
            imageName = imageFile.getName();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }

        frame.getWwd().addPropertyChangeListener(GliderImage.GLIDER_IMAGE_SOURCE, new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                frame.getLayerPanel().update(frame.getWwd());
            }
        });

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                final GliderImage image = new GliderImage(imageName, projectedImage, 100);
//                final GliderImage image = new GliderImage(imageName, testImage, corners, 100);
//                image.setOpacity(0.7);
                final GliderRegionOfInterest regionOfInterest = new GliderRegionOfInterest(latLons, Color.RED);
                image.addRegionOfInterest(regionOfInterest);

                final javax.swing.Timer timer = new Timer(1000, new ActionListener()
                {
                    @SuppressWarnings({"StringEquality"})
                    public void actionPerformed(ActionEvent evt)
                    {
                        try
                        {
                            if (((GliderWorldWindow) ((GliderAppFrame) frame).getWwd()).getImages().size() == 0)
                            {
                                System.out.println("ADDING");
//                                GliderImage image = new GliderImage(imageName, projectedImage, corners, 100);
//                                image.setOpacity(0.7);
//                                GliderRegionOfInterest regionOfInterest = new GliderRegionOfInterest(image.getCorners(),
//                                    Color.RED);
//                                image.addRegionOfInterest(regionOfInterest);
                                ((GliderWorldWindow) ((GliderAppFrame) frame).getWwd()).addImage(image);
                                image.releaseImageSource();
                            }
//                            else
//                            {
////                                    image.setImageSource(image.getName(), testImage); // could also use addImage() here
//                                System.out.println("REMOVING");
//                                GliderImage image
//                                    = ((GliderWorldWindow) ((GliderAppFrame) frame).getWwd()).getImages().iterator().next();
//                                ((GliderWorldWindow) ((GliderAppFrame) frame).getWwd()).removeImage(image);
//                            }
//
//                            for (GliderRegionOfInterest roi : image.getRegionsOfInterest().regions)
//                            {
//                                if (roi.getColor().equals(Color.RED))
//                                    roi.setColor(Color.GREEN);
//                                else
//                                    roi.setColor(Color.RED);
//                            }
//                            image.removeRegionOfInterest(regionOfInterest);
//                            image.addRegionOfInterest(regionOfInterest);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
//
//                        GliderRegionOfInterest.RegionSet rs = image.getRegionsOfInterest();
//                        if (rs == null || rs.regions.size() == 0)
//                            return;
//
//                        GliderRegionOfInterest r = (GliderRegionOfInterest) rs.regions.toArray()[0];
//                        ArrayList<LatLon> newLocations = new ArrayList<LatLon>();
//                        LatLon delta = LatLon.fromDegrees(0.0, 0.1);
//                        for (LatLon ll : r.getLocations())
//                        {
//                            newLocations.add(ll.add(delta));
//                        }
//                        r.setLocations(newLocations);
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        });
    }
}
