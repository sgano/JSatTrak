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

/*
 * StringTransferHandler.java is used by the 1.4
 * ExtendedDnDDemo.java example.
 */
import java.awt.datatransfer.*;
import javax.swing.*;
import java.io.IOException;

public abstract class StringTransferHandler extends TransferHandler implements java.io.Serializable
{
    
    protected abstract String exportString(JComponent c);
    protected abstract void importString(JComponent c, String str);
    protected abstract void cleanup(JComponent c, boolean remove);
    
    protected Transferable createTransferable(JComponent c)
    {
        return new StringSelection(exportString(c));
    }
    
    public int getSourceActions(JComponent c)
    {
        return COPY_OR_MOVE;
    }
    
    public boolean importData(JComponent c, Transferable t)
    {
        if (canImport(c, t.getTransferDataFlavors()))
        {
            try
            {
                
                String str = (String)t.getTransferData(DataFlavor.stringFlavor);
                importString(c, str);
                //System.out.println("HERE_last");
                return true;
            }
            catch (UnsupportedFlavorException ufe)
            {
                //ufe.printStackTrace();
            }
            catch (IOException ioe)
            {
                //ioe.printStackTrace();
            }
        }
        
        return false;
    }
    
    protected void exportDone(JComponent c, Transferable data, int action)
    {
        cleanup(c, action == MOVE);
    }
    
    public boolean canImport(JComponent c, DataFlavor[] flavors)
    {
        for (int i = 0; i < flavors.length; i++)
        {
            if (DataFlavor.stringFlavor.equals(flavors[i]))
            {
                return true;
            }
        }
        return false;
    }
}
