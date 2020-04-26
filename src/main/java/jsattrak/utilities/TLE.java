/*
 * TLE.java
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
 * Created on July 24, 2007, 3:02 PM
 *
 * Contains a Two Line Element (Norad) for a satellite
 */

package jsattrak.utilities;

/**
 *
 * @author ganos
 */
public class TLE implements java.io.Serializable
{
    String line0 = ""; // name line
    String line1 = ""; // first line
    String line2 = ""; // second line
    
    /** Creates a new instance of TLE */
    public TLE()
    {
    }
    
    public TLE(String name, String l1, String l2)
    {
        line0 = name;
        line1 = l1;
        line2 = l2;
    }
    
    public String getSatName()
    {
        return line0;
    }
    
    public String getLine1()
    {
        return line1;
    }
    
    public String getLine2()
    {
        return line2;
    }
             
    
} // TLE
