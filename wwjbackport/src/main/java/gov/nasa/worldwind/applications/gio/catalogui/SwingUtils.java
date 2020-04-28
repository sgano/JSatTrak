package gov.nasa.worldwind.applications.gio.catalogui;

import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * @author dcollins
 * @version $Id: SwingUtils.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class SwingUtils
{
    public static void centerWindowInDesktop(Window window)
    {
        if (window == null)
        {
            String message = "nullValue.WindowIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(window.getGraphicsConfiguration());
        int desktopWidth = screenWidth - screenInsets.left - screenInsets.right;
        int desktopHeight = screenHeight - screenInsets.bottom - screenInsets.top;
        int frameWidth = window.getSize().width;
        int frameHeight = window.getSize().height;

        if (frameWidth > desktopWidth)
            frameWidth = Math.min(frameWidth, desktopWidth);
        if (frameHeight > desktopHeight)
            frameHeight = Math.min(frameHeight, desktopHeight);

        window.setPreferredSize(new Dimension(
            frameWidth,
            frameHeight));
        window.pack();
        window.setLocation(
            (desktopWidth - frameWidth) / 2 + screenInsets.left,
            (desktopHeight - frameHeight) / 2 + screenInsets.top);
    }

    public static void fitWindowInDesktop(Window window)
    {
        if (window == null)
        {
            String message = "nullValue.WindowIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        int screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(window.getGraphicsConfiguration());
        int desktopWidth = screenWidth - screenInsets.left - screenInsets.right;
        int desktopHeight = screenHeight - screenInsets.bottom - screenInsets.top;
        int frameWidth = window.getSize().width;
        int frameHeight = window.getSize().height;

        if (frameWidth > desktopWidth)
            frameWidth = Math.min(frameWidth, desktopWidth);
        if (frameHeight > desktopHeight)
            frameHeight = Math.min(frameHeight, desktopHeight);

        window.setPreferredSize(new Dimension(
            frameWidth,
            frameHeight));
        window.pack();

        int frameX = window.getX();
        int frameY = window.getY();

        if (window.getBounds().getMinX() < screenInsets.left)
            frameX = screenInsets.left;
        if (window.getBounds().getMinY() < screenInsets.top)
            frameY = screenInsets.top;
        if (window.getBounds().getMaxX() > (screenWidth - screenInsets.right))
            frameX = (int) (frameX + screenWidth - window.getBounds().getMaxX() - screenInsets.right);
        if (window.getBounds().getMaxY() > (screenHeight - screenInsets.bottom))
            frameY = (int) (frameY + screenHeight - window.getBounds().getMaxY() - screenInsets.bottom);

        window.setLocation(frameX, frameY);
    }

    public static void constrainMaximumWidth(Component c)
    {
        if (c == null)
        {
            String message = "nullValue.ComponentIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Dimension prefSize = c.getPreferredSize();
        if (prefSize != null)
        {
            int width = prefSize.width;
            int height = c.getMaximumSize() != null ? c.getMaximumSize().height : Short.MAX_VALUE;
            c.setMaximumSize(new Dimension(width, height));
        }
    }

    public static void constrainMaximumHeight(Component c)
    {
        if (c == null)
        {
            String message = "nullValue.ComponentIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Dimension prefSize = c.getPreferredSize();
        if (prefSize != null)
        {
            int width = c.getMaximumSize() != null ? c.getMaximumSize().width : Short.MAX_VALUE;
            int height = prefSize.height;
            c.setMaximumSize(new Dimension(width, height));
        }
    }

    public static void contrainMaximumSize(Component c)
    {
        if (c == null)
        {
            String message = "nullValue.ComponentIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Dimension prefSize = c.getPreferredSize();
        if (prefSize != null)
        {
            int width = prefSize.width;
            int height = prefSize.height;
            c.setMaximumSize(new Dimension(width, height));
        }
    }

    public static void invokeInEventThread(Runnable r)
    {
        if (r == null)
        {
            String message = Logging.getMessage("nullValue.RunnableIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (EventQueue.isDispatchThread())
            r.run();
        else
            EventQueue.invokeLater(r);
    }

    public static ImageIcon readImageIcon(String path, Class<?> cls)
    {
        if (path == null)
        {
            String message = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (cls == null)
            cls = SwingUtils.class;

        URL url = cls.getResource("/" + path);
        if (url == null)
        {
            String message = "catalog.CannotFindResource " + path;
            Logging.logger().severe(message);
        }

        ImageIcon image = null;
        try
        {
            if (url != null)
                image = new ImageIcon(url);
        }
        catch (Exception e)
        {
            String message = "catalog.ExceptionWhileReadingImageResource " + path;
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
        return image;
    }
}
