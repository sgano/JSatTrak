/*
 * SaveImageFile.java
 * Utility class to help save images to a file -- lets you specify compression if the format supports it
 * 
 * =====================================================================
 *   This file is part of JSatTrak.
 *
 *   Copyright 2007-2013 Shawn E. Gano
 *   
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *   
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * =====================================================================
 */

package name.gano.file;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

/**
 *
 * @author Shawn Gano, 13 November 2008
 */
public class SaveImageFile 
{
    public static float DEFAULT_COMPRESSION = 0.75f; 
    
    /**
     * saves image, returns Eception if everything went fine this is null, otherwise it is the exception
     * @param format image type, e.g.: jpg, jpeg, gif, png
     * @param file what to save the image as
     * @param buff image
     * @return
     */
    public static Exception saveImage(String format,File file, BufferedImage buff)
    {  
        return saveImage(format,file, buff, SaveImageFile.DEFAULT_COMPRESSION);
    }
    
    /**
     * saves image, returns Eception if everything went fine this is null, otherwise it is the exception
     * @param format image type, e.g.: jpg, jpeg, gif, png
     * @param file what to save the image as
     * @param buff image
     * @param compressionQuality  0.0f-1.0f , 1 = best quality
     * @return
     */
    public static Exception saveImage(String format,File file, BufferedImage buff, float compressionQuality)
    {        
        Iterator iter = ImageIO.getImageWritersByFormatName(format);
        
        ImageWriter writer = (ImageWriter)iter.next();
        // instantiate an ImageWriteParam object with default compression options
        ImageWriteParam iwp = writer.getDefaultWriteParam();
        
        if(format.equalsIgnoreCase("jpg") || format.equalsIgnoreCase("JPEG") )
        {
            iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            iwp.setCompressionQuality(compressionQuality);   // an integer between 0 and 1
        // 1 specifies minimum compression and maximum quality
        }
        
        // write file
        try
        {
            FileImageOutputStream output = new FileImageOutputStream(file);
            writer.setOutput(output);
            IIOImage image = new IIOImage(buff, null, null);
            writer.write(null, image, iwp);
            output.close(); // Fixed SEG - 22 Dec 2008
        }
        catch(Exception e)
        {
            return e;
        }
        
        // everything went smoothly
        return null;
    }

}
