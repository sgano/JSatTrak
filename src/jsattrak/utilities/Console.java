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
 * 
 * Modified version of: http://www.artima.com/forums/flat.jsp?forum=1&thread=148613
 * Modifications by: Shawn Gano
 */

package jsattrak.utilities;

import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Console extends FilterOutputStream
{

    private static ArrayList<ConsoleListener> registeredListeners = new ArrayList<ConsoleListener>();

    static
    {
        // On its first use. Install the Console Listener
        PrintStream printStream =
                new PrintStream(
                new Console(new ByteArrayOutputStream()));
        System.setOut(printStream);
        // send errors to the console -- 23 March 2009 -- for web start debugging
        System.setErr(printStream);
    }

    public Console(OutputStream out)
    {
        super(out);
    }

    /* Override Ancestor method */
    public void write(byte b[]) throws IOException
    {
        String str = new String(b);
        logMessage(str);
    }

    /* Override Ancestor method */
    public void write(byte b[], int off, int len) throws IOException
    {
        String str = new String(b, off, len);
        logMessage(str);
    }

    /* Override Ancestor method */
    public void write(int b) throws IOException
    {
        String str = new String(new char[]{(char) b});
        logMessage(str);
    }

    public static void registerOutputListener(ConsoleListener listener)
    {
        // we don't register null listeners
        if (listener != null)
        {
            registeredListeners.add(listener);
        }
    }

    public static void removeOutputListener(ConsoleListener listener)
    {
        if (listener != null)
        {
            registeredListeners.remove(listener);
        }
    }

    private static void logMessage(String message)
    {
        // Log output to each listener
        int count = registeredListeners.size();
        for (int i = 0; i < count; i++)
        {
            ((ConsoleListener) registeredListeners.get(i)).logMessage(message);
        }
    }
}
