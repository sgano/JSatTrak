/*
 * Shawn E. Gano
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

package name.gano.astro.propogators.solvers;

import jsattrak.utilities.StateVector;

public interface OrbitProblem
{
        // this function advances the time 
        // this computes the velocity at the next time step  
        public double[] deriv(double[] pos, double[] v, double t);
	
	public void setVerbose(boolean verbose);
	
	public boolean getVerbose();
	
	//public Vector getEphemerisVector();
        
        public void addState2Ephemeris(StateVector state);
        
        //public boolean getStoreEphemeris();
        
} // Derivable
