// Shawn Gano, filters out a specific type of file
// Does compare exact file extension given, plus an all caps and all lowercase variants only
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
package name.gano.file;

import java.io.FilenameFilter;
import java.io.File;

public class FileTypeFilter implements FilenameFilter
{

    private String fileExtension;

    public FileTypeFilter(String fileExtension)
    {
        this.fileExtension = fileExtension;
    }

    public boolean accept(File dir, String name)
    {
        if(name.endsWith("." + fileExtension))
            return true;
        else if(name.endsWith("." + fileExtension.toUpperCase()))
            return true;
         else if(name.endsWith("." + fileExtension.toLowerCase()))
             return true;
        // no matches return false
        return false;
    }
}

