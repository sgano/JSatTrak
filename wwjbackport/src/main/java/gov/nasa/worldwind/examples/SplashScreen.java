/*
Copyright (C) 2001, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.Version;
import gov.nasa.worldwind.util.Logging;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URI;

/**
 * Simple splash frame.
 * @author Patrick Murris
 * @version $Id: SplashScreen.java 5744 2008-07-31 17:55:07Z dcollins $
 */

public class SplashScreen extends JFrame
{
    public static final String NASA_WORLDWIND_SPLASH = "/images/400x230-splash-nww.png";
    public static final String SDK_VERSION = "Java SDK Version " + Version.getVersionNumber();

    private BufferedImage image;
    private String version;
    
    public SplashScreen()
    {
        this(NASA_WORLDWIND_SPLASH, SDK_VERSION);
    }

    public SplashScreen(String imagePath)
    {
        this(imagePath, null);
    }

    private SplashScreen(String imagePath, String version)
    {
        if (imagePath == null)
        {
            String message = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }
        this.version = version;
        this.setUndecorated(true);
        this.setSize(400, 230);
        // Load image
        URL imageURL = SplashScreen.class.getResource(imagePath);
        if (imageURL == null)
        {
            File file = new java.io.File(imagePath);
            if (file.exists())
            {
                try
                {
                    URI uri = file.toURI();
                    imageURL = uri.toURL();
                }
                catch (MalformedURLException e)
                {
                    imageURL = null;
                    Logging.logger().severe(Logging.getMessage("generic.ImageReadFailed", imagePath));
                }
            }
        }
        if (imageURL != null)
        {
            try
            {
                image = ImageIO.read(imageURL);
                this.setSize(image.getWidth(), image.getHeight());
            }
            catch (IOException e)
            {
                image = null;
                Logging.logger().severe(Logging.getMessage("generic.ImageReadFailed", imagePath));
            }
        }
        // Center frame
        Dimension parentSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (parentSize.width - getSize().width) / 2;
        int y = (parentSize.height - getSize().height) / 2;
        this.setLocation(x, y);
        this.setResizable(false);
        this.setVisible(true);
    }

    public void paint(Graphics g)
    {
        Graphics2D g2 = (Graphics2D)g;

        // Draw background image
        if (image != null)
            g2.drawImage(image, 0, 0, null);

        // Draw version string
        if (version != null && version.length() > 0)
        {
            Font font = Font.decode("Arial-Bold-14");
            FontRenderContext context = g2.getFontRenderContext();
            Rectangle2D bounds = font.getStringBounds(version, context);
            int x = (int)(getWidth() - bounds.getWidth()) / 2;
            int y = getHeight() / 2 + 32;
            g2.setFont(font);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.BLACK);
            g2.drawString(version, x + 1, y + 1);
            g2.setColor(Color.WHITE);
            g2.drawString(version, x, y);
        }
    }
}
