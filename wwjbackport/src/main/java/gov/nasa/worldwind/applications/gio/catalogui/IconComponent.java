/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

import gov.nasa.worldwind.util.Logging;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dcollins
 * @version $Id: IconComponent.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class IconComponent
{
    private String iconPath;
    private boolean problemReadingIcon = false;
    private static final Map<IconEntry, Icon> iconMap = new HashMap<IconEntry, Icon>();

    private static class IconEntry
    {
        private String path;
        private Class<?> cls;

        private IconEntry(String path, Class<?> cls)
        {
            this.path = path;
            this.cls = cls;
        }

        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            IconEntry iconEntry = (IconEntry) o;

            if (this.cls != null ? !this.cls.equals(iconEntry.cls) : iconEntry.cls != null)
                return false;
            //noinspection RedundantIfStatement
            if (this.path != null ? !this.path.equals(iconEntry.path) : iconEntry.path != null)
                return false;
            return true;
        }

        public int hashCode()
        {
            int result;
            result = (this.path != null ? this.path.hashCode() : 0);
            result = 31 * result + (this.cls != null ? this.cls.hashCode() : 0);
            return result;
        }
    }

    public IconComponent()
    {
    }

    public String getIconPath()
    {
        return this.iconPath;
    }

    public void setIconPath(String iconPath)
    {
        this.iconPath = iconPath;
        this.problemReadingIcon = false;
    }

    public boolean isProblemReadingIcon()
    {
        return this.problemReadingIcon;
    }

    public Icon getIcon()
    {
        return loadIcon(getClass());
    }

    protected Icon loadIcon(Class<?> cls)
    {
        Icon icon = null;
        if (!this.problemReadingIcon)
        {
            if (this.iconPath != null)
                icon = fetchIcon(this.iconPath, cls);
            this.problemReadingIcon = (icon == null);
        }
        return icon;
    }

    protected static Icon fetchIcon(String path, Class<?> cls)
    {
        if (path == null)
        {
            String message = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        IconEntry entry = new IconEntry(path, cls);
        Icon icon = iconMap.get(entry);
        if (icon == null && !iconMap.containsKey(entry))
        {
            icon = readIcon(path, cls);
            iconMap.put(entry, icon);
        }
        return icon;
    }

    protected static Icon readIcon(String path, Class<?> cls)
    {
        if (path == null)
        {
            String message = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return SwingUtils.readImageIcon(path, cls);
    }
}
