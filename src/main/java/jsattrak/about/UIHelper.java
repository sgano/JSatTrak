/*
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
 * 
 * $Id: UIHelper.java,v 1.1 2005/05/25 23:13:23 rbair Exp $
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 */
package jsattrak.about;

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

public class UIHelper {
    public static ImageIcon readImageIcon(String fileName)
    {
        Image image = readImage(fileName);
        if (image == null)
            return null;

        return new ImageIcon(image);
    }

    public static Image readImage(String fileName)
    {
        URL url = UIHelper.class.getResource("images/" + fileName);
        if (url == null)
            return null;

        return java.awt.Toolkit.getDefaultToolkit().getImage(url);
    }
}
