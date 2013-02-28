/*
 * BoldFontCellRenderer.java
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
 * 
 * Created on August 4, 2007, 11:16 PM
 *
 */

package jsattrak.gui;


import java.awt.Component;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
public class BoldFontCellRenderer extends DefaultTableCellRenderer implements java.io.Serializable
{
    
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
    {
        Component cell = super.getTableCellRendererComponent
                (table, value, isSelected, hasFocus, row, column);
        
        //cell.setBackground( Color.red );
        // You can also customize the Font and Foreground this way
        // cell.setForeground();
        // cell.setFont();
        
        
        
        
        
        // bold header row
        if(row == 4 || row == 8 || row ==12)
        {
            cell.setFont( new Font("Default",Font.BOLD,12) );
            
        }
        
        cell.setForeground(Color.BLACK); // fix problem when selecting and text turns white
        
        // background colors
        if(row >=4 && row < 8 )
        {
            //cell.setBackground( new Color(210,231,199)); // light green
            cell.setBackground( new Color(210,231,199)); // light green
        }
        else if(row >=12)
        {
            cell.setBackground( new Color(249,255,202)); // light green
        }
        else
        {
            cell.setBackground( Color.WHITE); // light green
        }
        
        // age of TLE
        try
        {
            if(row == 0 && column == 1)
            {
                String s =  table.getModel().getValueAt(0, 1 ).toString();
                double d = Double.parseDouble(s);
                
                if(Math.abs(d) > 12)//30
                {
                    cell.setBackground( new Color(255,0,0)); // red
                }
                else if(Math.abs(d) > 10)//25
                {
                    cell.setBackground( new Color(255,153,0)); // yellow
                }
                else if(Math.abs(d) > 8)//20
                {
                    cell.setBackground( new Color(255,204,0)); // yellow
                }
                else if(Math.abs(d) > 6)//15
                {
                    cell.setBackground( new Color(255,255,0)); // yellow
                }
                else if(Math.abs(d) > 4)//10
                {
                    cell.setBackground( new Color(204,255,0)); // yellow
                }
                else if(Math.abs(d) > 2) //5
                {
                    cell.setBackground( new Color(153,255,0)); // yellow
                }
                else
                {
                    cell.setBackground( new Color(0,255,0)); // green
                }
            }
        }
        catch(Exception ee)
        {
            //
        }
        
        
        return cell;
        
    } // getTableCellRendererComponent
    
} // BoldFontCellRenderer
