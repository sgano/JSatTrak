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

package name.gano.swingx.treetable;

import java.awt.Component;
import java.util.Hashtable;
import javax.swing.Icon;
import javax.swing.JTree;
//import org.jvnet.substance.api.renderers.SubstanceDefaultTreeCellRenderer;
// SEG v4.1.4 - update to newer version of substnace api
import org.pushingpixels.substance.api.renderers.SubstanceDefaultTreeCellRenderer;

/**
 *
 * @author sgano
 */
public class IconTreeTableNodeRenderer extends SubstanceDefaultTreeCellRenderer //DefaultTreeCellRenderer // to work with Substance LAF
{
    
    
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row,
            boolean hasFocus)
    {

        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
                row, hasFocus);

        Icon icon = ((CustomTreeTableNode) value).getIcon();

        if (icon == null)
        {
            Hashtable icons = (Hashtable) tree.getClientProperty("JTree.icons");
            String name = ((CustomTreeTableNode) value).getIconName();
            if ((icons != null) && (name != null))
            {
                icon = (Icon) icons.get(name);
                if (icon != null)
                {
                    setIcon(icon);
                }
            }
            
            // also set text of the first column correctly
            setText(((CustomTreeTableNode) value).getValueAt(0).toString());
        }
        else
        {
            setIcon(icon);
            // also set text of the first column correctly
            setText(((CustomTreeTableNode) value).getValueAt(0).toString());
        }

        return this;
    }
}
