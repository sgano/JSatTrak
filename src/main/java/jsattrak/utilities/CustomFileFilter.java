/**
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
package jsattrak.utilities;

import java.io.File;
import javax.swing.filechooser.*;

/* ImageFilter.java  is used to filter out only image files in Save As dialog */
public class CustomFileFilter extends FileFilter  implements java.io.Serializable
{
	
    
    private String fileExtension;
    private String description;
    
    public CustomFileFilter(String fileExtension, String description)
    {
    	this.fileExtension = fileExtension;
    	this.description = description;
    }

    //Accept all directories and all gif, jpg, tiff, or png files.
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equalsIgnoreCase(fileExtension) )//||
            {
                return true;
            } 
            else 
            {
                return false;
            }
        }

        return false;
    }

    //The description of this filter
    public String getDescription() {
        return description;
    }
    
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1)
        {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }
}
