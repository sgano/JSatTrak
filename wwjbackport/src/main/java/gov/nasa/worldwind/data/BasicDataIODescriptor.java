/* Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.util.*;

/**
 * @author dcollins
 * @version $Id: BasicDataIODescriptor.java 9168 2009-03-05 00:40:03Z dcollins $
 */
public class BasicDataIODescriptor implements DataIODescriptor
{
    private final String description;
    private final String[] mimeTypes;
    private final String[] suffixes;

    public BasicDataIODescriptor(String description, String[] mimeTypes, String[] suffixes)
    {
        this.description = description;
        this.mimeTypes = (mimeTypes != null) ? copyOf(mimeTypes) : new String[0];
        this.suffixes = (suffixes != null) ? copyOf(suffixes) : new String[0];
    }

    public BasicDataIODescriptor(String[] mimeTypes, String[] suffixes)
    {
        this(null, mimeTypes, suffixes);
    }

    protected BasicDataIODescriptor(String description)
    {
        this(description, null, null);
    }

    public String getDescription()
    {
        return this.description;
    }

    public String[] getMimeTypes()
    {
        String[] copy = new String[mimeTypes.length];
        System.arraycopy(mimeTypes, 0, copy, 0, mimeTypes.length);
        return copy;
    }

    public String[] getSuffixes()
    {
        String[] copy = new String[suffixes.length];
        System.arraycopy(suffixes, 0, copy, 0, suffixes.length);
        return copy;
    }

    public boolean matchesMimeType(String mimeType)
    {
        if (mimeType == null)
        {
            String message = Logging.getMessage("nullValue.MimeTypeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (String s : this.mimeTypes)
        {
            if (mimeType.equalsIgnoreCase(s))
            {
                return true;
            }
        }
        return false;
    }

    public boolean matchesFormatSuffix(String formatSuffix)
    {
        if (formatSuffix == null)
        {
            String message = Logging.getMessage("nullValue.FormatS uffixIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (String s : this.suffixes)
        {
            if (formatSuffix.equalsIgnoreCase(s))
            {
                return true;
            }
        }
        return false;
    }

    //**************************************************************//
    //********************  Utilities  *****************************//
    //**************************************************************//

    public static String getSuffixFor(Object input)
    {
        String path = pathFor(input);
        if (path == null)
            return null;

        return WWIO.getSuffix(path);
    }

    public static String createCombinedDescription(Iterable<? extends DataIODescriptor> descriptors)
    {
        if (descriptors == null)
        {
            String message = Logging.getMessage("nullValue.DataIODescriptorsIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Collect all the unique format suffixes available in all descriptors. If a descriptor does not publish any
        // format suffixes, then collect it's description.
        java.util.Set<String> suffixSet = new java.util.TreeSet<String>();
        java.util.Set<String> descriptionSet = new java.util.TreeSet<String>();
        for (DataIODescriptor descriptor : descriptors)
        {
            String[] names = descriptor.getSuffixes();
            if (names != null && names.length > 0)
            {
                suffixSet.addAll(java.util.Arrays.asList(names));
            }
            else if (descriptor.getDescription() != null)
            {
                descriptionSet.add(descriptor.getDescription());
            }
        }

        // Create a string representaiton of the format suffixes (or description if no suffixes are available) for
        // all descriptors.
        StringBuilder sb = new StringBuilder();
        for (String suffix : suffixSet)
        {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append("*.").append(suffix);
        }
        for (String description : descriptionSet)
        {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(description);
        }
        return sb.toString();
    }

    protected static String pathFor(Object input)
    {
        if (input instanceof String)
        {
            return (String) input;
        }
        else if (input instanceof java.io.File)
        {
            return ((java.io.File) input).getPath();
        }
        else if (input instanceof java.net.URI)
        {
            return input.toString();
        }
        else if (input instanceof java.net.URL)
        {
            return input.toString();
        }

        return null;
    }

    protected static String[] copyOf(String[] array)
    {
        String[] copy = new String[array.length];
        for (int i = 0; i < array.length; i++)
            copy[i] = array[i].toLowerCase();
        return copy;
    }
}
