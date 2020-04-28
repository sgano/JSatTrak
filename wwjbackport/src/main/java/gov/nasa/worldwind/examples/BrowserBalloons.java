/*
Copyright (C) 2001, 2010 United States Government as represented by 
the Administrator of the National Aeronautics and Space Administration. 
All Rights Reserved. 
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.examples.util.HotSpotController;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * @author dcollins
 * @version $Id: BrowserBalloons.java 14148 2010-11-23 01:02:38Z pabercrombie $
 */
public class BrowserBalloons extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected HotSpotController hotSpotController;

        public AppFrame()
        {
            super(true, false, false);

            this.makePlaceBalloon();
            this.makeGoogleMapsBalloon();

            // Add a controller to send input events to the BrowserBalloons.
            this.hotSpotController = new HotSpotController(this.getWwd());

            // Size the World Window to provide enough screen space for both balloons and center it on the screen.
            Dimension size = new Dimension(1280, 800);
            this.setPreferredSize(size);
            this.pack();
            WWUtil.alignComponent(null, this, AVKey.CENTER);
        }

        protected void makePlaceBalloon()
        {
            String htmlString =
                "<html>"
                    + "<body>"
                    + "<img style=\"display:none\" src=\"$[tracking_href_2]\" />"
                    + "<div id=\"content\" name=\"cid=4747726673235102483&q=Pike+Place+Market&ui=earth&view=teaser\">"
                    + "</script>"
                    + "<table>"
                    + "<tr>"
                    + "<td>"
                    + "<div><table width=\"350\">"
                    + "<tr>"
                    + "<td colspan=\"2\">"
                    + "<font size=\"+1\">"
                    + "<b><a style=\"text-decoration: none\" href=\"http://maps.google.com/maps/place?cid=4747726673235102483\">Pike Place Market</a></b>"
                    + "</font>"
                    + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>1501 Pike Place"
                    + "Seattle, Washington 98101, United States</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>+1 206-682-7453</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>"
                    + "<br/><a href=\"http://www.panoramio.com/photo/12234553\"><img src=\"http://mw2.google.com/mw-panoramio/photos/small/12234553.jpg\"/></a>"
                    + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</div>"
                    + "</body>"
                    + "</html>";

            BrowserBalloon b = new BrowserBalloon(htmlString, Position.fromDegrees(47.609526, -122.341729));

            BalloonAttributes attrs = new BasicBalloonAttributes();
            attrs.setSize(Size.fromPixels(430, 400));
            attrs.setDrawOffset(new Offset(-0.5, 60.0, AVKey.FRACTION, AVKey.PIXELS));
            b.setAttributes(attrs);

            RenderableLayer layer = new RenderableLayer();
            layer.addRenderable(b);
            insertBeforeCompass(this.getWwd(), layer);
        }

        protected void makeGoogleMapsBalloon()
        {
            URL url = WWIO.makeURL(
                "http://maps.google.com/maps?f=q&source=s_q&hl=en&geocode=&q=New+York,+NY&sll=37.0625,-95.677068&sspn=64.624909,57.919922&ie=UTF8&hq=&hnear=New+York&z=10");
            String htmlString = getURLContent(url);

            BrowserBalloon b = new BrowserBalloon(htmlString, Position.fromDegrees(41.029643, -74.41864));
            b.setBaseURL(url);

            BalloonAttributes attrs = new BasicBalloonAttributes();
            attrs.setSize(Size.fromPixels(700, 500));
            attrs.setDrawOffset(new Offset(-0.5, 60.0, AVKey.FRACTION, AVKey.PIXELS));
            b.setAttributes(attrs);

            RenderableLayer layer = new RenderableLayer();
            layer.addRenderable(b);
            insertBeforeCompass(this.getWwd(), layer);
        }
    }

    protected static String getURLContent(URL url)
    {
        try
        {
            ByteBuffer buffer = WWIO.readURLContentToBuffer(url);
            return WWIO.byteBufferToString(buffer, null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args)
    {
        // Configure the initial view parameters so that the balloons are immediately visible.
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 63);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -83);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 9500000);
        Configuration.setValue(AVKey.INITIAL_HEADING, 27);
        Configuration.setValue(AVKey.INITIAL_PITCH, 30);

        start("World Wind Browser Balloons", AppFrame.class);
    }
}