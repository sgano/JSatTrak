/*
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

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.Hashtable;
import javax.swing.Icon;
import javax.swing.plaf.metal.MetalIconFactory;

class TextIcons extends MetalIconFactory.TreeLeafIcon {

  protected String label;

  private static Hashtable<String,String> labels;

  protected TextIcons() {
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    super.paintIcon(c, g, x, y);
    if (label != null) {
      FontMetrics fm = g.getFontMetrics();

      int offsetX = (getIconWidth() - fm.stringWidth(label)) / 2;
      int offsetY = (getIconHeight() - fm.getHeight()) / 2 - 2;

      g.drawString(label, x + offsetX, y + offsetY + fm.getHeight());
    }
  }

  public static Icon getIcon(String str) {
//    if (labels == null) {
//      labels = new Hashtable<String,String>();
//      setDefaultSet();
//    }
    TextIcons icon = new TextIcons();
    icon.label = (String) labels.get(str);
    return icon;
  }

  public static void setLabelSet(String ext, String label) {
//    if (labels == null) {
//      labels = new Hashtable<String,String>();
//      setDefaultSet();
//    }
    labels.put(ext, label);
  }

//  private static void setDefaultSet() {
//    labels.put("c", "C");
//    labels.put("java", "J");
//    labels.put("html", "H");
//    labels.put("htm", "H");
//
//    // and so on
//    /*
//     * labels.put("txt" ,"TXT"); labels.put("TXT" ,"TXT"); labels.put("cc"
//     * ,"C++"); labels.put("C" ,"C++"); labels.put("cpp" ,"C++");
//     * labels.put("exe" ,"BIN"); labels.put("class" ,"BIN");
//     * labels.put("gif" ,"GIF"); labels.put("GIF" ,"GIF");
//     * 
//     * labels.put("", "");
//     */
//  }
}
