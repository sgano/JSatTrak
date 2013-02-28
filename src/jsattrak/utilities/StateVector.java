/*
 * State Vector - Shawn Gano
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

import java.io.Serializable;

public class StateVector implements Serializable
{
	public double[] state; // t,x,y,z,dx,dy,dz - seven elements
	
	public StateVector()
	{
		state = new double[7];
	}
	
	public StateVector(double[] newStateWithTime)
	{
		state = new double[7];
		for(int i=0;i<7;i++)
		{
			state[i] = newStateWithTime[i];
		}
	}
        
        // constructor given a new state that doesn't include time (x,y,z,dx,dy,dz), and time
        public StateVector(double[] newState, double time)
	{
		state = new double[7];
                
                state[0] = time;
		for(int i=1;i<7;i++)
		{
			state[i] = newState[i-1];
		}
	}
	
	public StateVector(double t, double x, double y, double z, double dx, double dy, double dz)
	{
		state = new double[7];
		state[0] = t;
		state[1] = x;
		state[2] = y;
		state[3] = z;
		state[4] = dx;
		state[5] = dy;
		state[6] = dz;
	}
        
        public String toString()
        {
            return "" + state[0] + ","+ state[1] + ","+ state[2] + ","+ state[3] + ","+ state[4] + ","+ state[5] + ","+ state[6];
        }
}
