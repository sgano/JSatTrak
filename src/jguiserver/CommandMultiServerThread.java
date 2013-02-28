/*
 *  A Thread to handle a socket connection - all input stream from client is sent to a
 *  beanshell interperator.
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
 */

package jguiserver;

import bsh.Interpreter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import jguiserver.GuiServer.CommandServerThread;

/**
 *
 * @author sgano
 */
public class CommandMultiServerThread extends Thread   
{
    private Socket socket = null;
    Interpreter bsh;
    
    private int connectionID;
    
    private String iniDate;
    
    CommandServerThread server; // used to handle disconnects (etc)

    public CommandMultiServerThread(Socket socket, Interpreter bsh, int ID, CommandServerThread server)
    {
        super("CommandMultiServerThread");
        this.socket = socket;
        this.bsh = bsh;
        this.connectionID = ID;
        this.server = server;
        
        iniDate = now();
    }
    
    // method to get current time
    public static final String DATE_FORMAT_NOW = "HH:mm:ss dd-MMM-yyyy";
    public static String now()
    {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());

    }

    public void run()
    {

        try
        {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                    socket.getInputStream()));

            String inputLine, outputLine;
            
            out.println("Connection Successful");

            while ((inputLine = in.readLine()) != null)
            {
                //outputLine = cp.processInput(inputLine);
                
                // check for an exit 
                if (inputLine.trim().equalsIgnoreCase("exit"))
                {
                    break;
                }
                
                // process command
                try
                {
                    Object rst = bsh.eval(inputLine); // run command in interperator
                    out.println(rst.toString()); // return result as a string
                }
                catch (Exception e)
                {
                    // if there is an error send this: (must send something back)
                    //out.println("ERROR: Check command syntax");
                    out.println("null"); // hmm might want to send more info, such as if the command worked or if it was a bad command? Maybe the tostring is causing problems
                }
                      
            } // read from client
            
            out.close();
            in.close();
            socket.close();
            // tell server we are closing
            server.processConnectionClose(this);

        }
        catch (IOException e)
        {
            e.printStackTrace();
            
             // tell server we are closing
            server.processConnectionClose(this);
        }
    }

    public

    int getConnectionID()
    {
        return connectionID;
    }
    
    public void close() throws Exception
    {
        socket.close();
    }
    
    public String getClientIP()
    {
        return socket.getInetAddress().toString();
    }

    public

    String getIniDate()
    {
        return iniDate;
    }
}
