/*
 * TextureUtils.java
 *=====================================================================
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
 * Created on September 23, 2007, 1:13 PM
 *
 * Collection of static methods dealing with JOGL textures
 */

package name.gano.worldwind.texture;

import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;
import java.io.File;
import javax.media.opengl.GL;

/**
 *
 * @author Shawn
 */
public class TextureUtils
{
    
    public static Texture loadTexture(String fnm)
    {
        String fileName = "images/" + fnm;
        Texture tex = null;
        try
        {
            tex = TextureIO.newTexture( new File(fileName), false);
            tex.setTexParameteri(GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
            tex.setTexParameteri(GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
        }
        catch(Exception e)
        { 
            System.out.println("Error loading texture " + fileName);  
            e.printStackTrace();
        }
        
        return tex;
    }  // end of loadTexture()

    
}
