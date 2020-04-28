/* Copyright (C) 2001, 2008 United States Government as represented by
   the Administrator of the National Aeronautics and Space Administration.
   All Rights Reserved.
 */
package gov.nasa.worldwind.formats.tiff;

import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.ImageTypeSpecifier;

/**
 * @author brownrigg
 * @version $Id: GeotiffMetadataFormat.java 6539 2008-09-10 16:39:49Z rick $
 */

public class GeotiffMetadataFormat extends IIOMetadataFormatImpl {

    public GeotiffMetadataFormat() { super(null,0); }
    public boolean canNodeAppear(String elementName, ImageTypeSpecifier imageType) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
