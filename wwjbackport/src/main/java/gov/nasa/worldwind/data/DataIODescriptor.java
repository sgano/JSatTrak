/* Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.data;

/**
 * @author dcollins
 * @version $Id: DataIODescriptor.java 9168 2009-03-05 00:40:03Z dcollins $
 */
public interface DataIODescriptor
{
    String getDescription();

    String[] getMimeTypes();

    String[] getSuffixes();

    boolean matchesMimeType(String mimeType);

    boolean matchesFormatSuffix(String suffix);
}
