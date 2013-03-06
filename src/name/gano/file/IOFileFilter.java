// Shawn Gano, filters out a specific type of file
// case in-sensative
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

import java.io.File;

/**
 *
 * @author sgano
 */
public class IOFileFilter implements java.io.FileFilter
{
    private String[] fileTypes;
    private boolean acceptDirs = false;

    public IOFileFilter(String... fileExtensions)
    {
        fileTypes = fileExtensions;
    }


    public boolean accept(File f)
    {
        if(f.isDirectory())
        {
            if(acceptDirs)
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        String name = f.getName().toLowerCase();
        
        for(String ext : fileTypes)
        {
            if( name.endsWith(ext.toLowerCase()))
            {
                return true;
            }
        }

        return false;
    }//end accept

    /**
     * @return the fileTypes
     */
    public String[] getFileTypes()
    {
        return fileTypes;
    }

    /**
     * @param fileTypes the fileTypes to set
     */
    public void setFileTypes(String[] fileTypes)
    {
        this.fileTypes = fileTypes;
    }

    /**
     * @return the acceptDirs
     */
    public boolean isAcceptDirs()
    {
        return acceptDirs;
    }

    /**
     * @param acceptDirs the acceptDirs to set
     */
    public void setAcceptDirs(boolean acceptDirs)
    {
        this.acceptDirs = acceptDirs;
    }
}//end class IOFileFilter
