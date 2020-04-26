// Shawn E. Gano
// Kepler routines
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
package name.gano.astro;

import jsattrak.utilities.StateVector;

public class Kepler
{
	
    /**
     *  Overload for state
     * @param GM Gravitational coefficient (gravitational constant * mass of central body)
     * @param kepElements Keplarian elements (see other state function)
     * @param dt Time since epoch (seconds?)
     * @return State vector (x,y,z,vx,vy,vz)
     */
    public static double[] state(double GM, double[] kepElements, double dt)
    {
        return state(GM, kepElements[0], kepElements[1],kepElements[2], kepElements[3], kepElements[4], kepElements[5], dt);
    }
    
    /**
     * Computes the satellite state vector from osculating Keplerian elements for elliptic orbits
     * <p>Notes:
     * <br> The semimajor axis a=Kep(0), dt and GM must be given in consistent units,
     *      e.g. [m], [s] and [m^3/s^2]. The resulting units of length and velocity
     *      are implied by the units of GM, e.g. [m] and [m/s].
     *
     * @param GM Gravitational coefficient (gravitational constant * mass of central body)
     * @param a Semimajor axis
     * @param e Eccentricity
     * @param i Inclination [rad]
     * @param Omega Longitude of the ascending node [rad] or is this RAAN? (I think it is RAAN)
     * @param omega Argument of pericenter [rad]
     * @param M0 Mean anomaly at epoch [rad]
     * @param dt Time since epoch (seconds?)
     * @return State vector (x,y,z,vx,vy,vz)
     */
	public static double[] state(double GM, double a, double e, double i, double Omega, double omega, double M0, double dt)
	{
		double M; // mean anomaly
		double n;
		double E, cosE, sinE; // eccentric anomaly
		double fac, R, V;

		// vectors for position (r) and velocity (v)
		double[] r = new double[3];
		double[] v = new double[3];
		double[] state = new double[6]; // full state

		// transformation matrices
		double[][] Rx = new double[3][3];
		// double[][] Ry = new double[3][3];
		double[][] Rz = new double[3][3];

		// rotation matrix
		double[][] PQW = new double[3][3];

		// Mean anomaly
		if (dt == 0.0)
		{
			M = M0;
		} else
		{
			n = Math.sqrt(GM / (a * a * a));
			M = M0 + n * dt;
		}

		// Eccentric anomaly
		E = EccAnom(M, e);
		cosE = Math.cos(E);
		sinE = Math.sin(E);

		// Perifocal coordinates
		fac = Math.sqrt((1.0 - e) * (1.0 + e));

		R = a * (1.0 - e * cosE); // Distance
		V = Math.sqrt(GM * a) / R; // Velocity

		// r
		r[0] = a * (cosE - e);
		r[1] = a * fac * sinE;
		r[2] = 0.0;

		// v
		v[0] = -V * sinE;
		v[1] = +V * fac * cosE;
		v[2] = 0.0;

		// Transformation to reference system (Gaussian vectors)
		Rx = MathUtils.R_x(-i);
		Rz = MathUtils.R_z(-Omega);

		// PQW = R_z(-Omega) * R_x(-i) * R_z(-omega);
		PQW = MathUtils.mult(Rz, Rx);
		Rz = MathUtils.R_z(-omega);
		PQW = MathUtils.mult(PQW, Rz);

		r = MathUtils.mult(PQW, r);
		v = MathUtils.mult(PQW, v);

		// State vector
		state[0] = r[0];
		state[1] = r[1];
		state[2] = r[2];
		state[3] = v[0];
		state[4] = v[1];
		state[5] = v[2];

		// return Stack(r,v);
		return state;

	} // state


    /**
     * Computes the eccentric anomaly for elliptic orbits
     *
     * @param M Mean anomaly in [rad]
     * @param e Eccentricity of the orbit [0,1]
     * @return Eccentric anomaly in [rad]
     */
	public static double EccAnom(double M, double e)
	{

		// Constants
		final int maxit = 15;
		final double eps = 100.0 * 2.22E-16; // JAVA FIND MACHINE PRECISION // 100.0*eps_mach

		// Variables

		int i = 0;
		double E, f;

		// Starting value
		M = MathUtils.Modulo(M, 2.0 * Math.PI);
		if (e < 0.8)
			E = M;
		else
			E = Math.PI;

		// Iteration
		do
		{
			f = E - e * Math.sin(E) - M;
			E = E - f / (1.0 - e * Math.cos(E));
			++i;
			if (i == maxit)
			{
				System.out.println(" convergence problems in EccAnom\n");
				break;
			}
		} while (Math.abs(f) > eps);

		return E;

	} // EccAnom

	
        /**
         * Computes the osculating Keplerian elements from the satellite state vector for elliptic orbits for Earth
         * @param StateVector j2K cartesian state vector object (t,x,y,z,dx,dy,dz)
         * @return Keplerian elements (a,e,i,Omega,omega,M) see state function for element definitions
         */
        public static double[] SingularOsculatingElementsEarth( StateVector state )
        { 
            double[] r = new double[] {state.state[1],state.state[2],state.state[3]};
            double[] v = new double[] {state.state[4],state.state[5],state.state[6]};
            
            return SingularOsculatingElements( AstroConst.GM_Earth, r, v );
        }
       
        /**
         * Computes the osculating Keplerian elements from the satellite state vector for elliptic orbits
         * @param GM Gravitational coefficient (gravitational constant * mass of central body)
         * @param state j2K cartesian state vector (x,y,z,dx,dy,dz)
         * @return Keplerian elements (a,e,i,Omega,omega,M) see state function for element definitions
         */
        public static double[] SingularOsculatingElements( double GM, double[] state )
        { 
            double[] r = new double[] {state[0],state[1],state[2]};
            double[] v = new double[] {state[3],state[4],state[5]};
            
            return SingularOsculatingElements( GM, r, v );
        }
        
    /**
     * Computes the osculating Keplerian elements from the satellite state vector for elliptic orbits
     *
     * @param GM Gravitational coefficient (gravitational constant * mass of central body)
     * @param r State vector (x,y,z)
     * @param v State vector (vx,vy,vz)
     * @return Keplerian elements (a,e,i,Omega,omega,M) see state function for element definitions
     */
        public static double[] SingularOsculatingElements( double GM, double[] r, double[] v )
        { 
            // Variables
            
            double[] h = new double[3];
            double  H, u, R;
            double  eCosE, eSinE, e2, E, nu;
            double  a,e,i,Omega,omega,M;
                        
            h = MathUtils.cross(r,v);                         // Areal velocity
            H = MathUtils.norm(h);
            
            Omega = Math.atan2( h[0], -h[1] );                     // Long. ascend. node
            Omega = Omega % (Math.PI*2.0);
            i     = Math.atan2( Math.sqrt(h[0]*h[0]+h[1]*h[1]), h[2] ); // Inclination
            u     = Math.atan2( r[2]*H, -r[0]*h[1]+r[1]*h[0] );    // Arg. of latitude
            
            R  = MathUtils.norm(r);                                      // Distance
            
            //System.out.println("R="+R);
            //System.out.println("v dot v="+(MathUtils.dot(v,v)));
            //System.out.println("GM="+GM);
            
            a = 1.0 / (2.0/R-MathUtils.dot(v,v)/GM);                     // Semi-major axis
            
            eCosE = 1.0-R/a;                                   // e*cos(E)
            eSinE = MathUtils.dot(r,v)/Math.sqrt(GM*a);                       // e*sin(E)
            
            e2 = eCosE*eCosE +eSinE*eSinE;
            e  = Math.sqrt(e2);                                     // Eccentricity
            E  = Math.atan2(eSinE,eCosE);                           // Eccentric anomaly
            
            M  = (E-eSinE)%(2.0*Math.PI);                          // Mean anomaly
            
            nu = Math.atan2(Math.sqrt(1.0-e2)*eSinE, eCosE-e2);          // True anomaly
            
            omega = (u-nu)%(2.0*Math.PI);                          // Arg. of perihelion
            
            // Keplerian elements vector
            
            //System.out.println("Real a = " + a);
            
            return (new double[] {a,e,i,Omega,omega,M});
        } // SingularElements

        
    /**
     * calculate oculating orbital period of a eliptical orbit from position and velocity
     *
     * @param GM Gravitational coefficient (gravitational constant * mass of central body)
     * @param r State vector (x,y,z)
     * @param v State vector (vx,vy,vz)
     * @return oculating orbital period
     */
        public static double CalculatePeriod( double GM, double[] r, double[] v )
        { 
            double R  = MathUtils.norm(r); // current radius 
            
            double a = 1.0 / (2.0/R-MathUtils.dot(v,v)/GM);                     // Semi-major axis
            
            return ( 2.0*Math.PI*Math.sqrt(a*a*a/GM) );
        } // CalculatePeriod

} // Kepler
