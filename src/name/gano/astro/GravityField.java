/*
 * Gravity Field Methods
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

/**
 *
 * @author sgano
 */
public class GravityField 
{

    
    /**
     * Computes the acceleration due to the harmonic gravity field of the central body
     * @param r Satellite position vector in the inertial system
     * @param E Transformation matrix to body-fixed system
     * @param GM Gravitational coefficient
     * @param R_ref Reference radius (equatorial)
     * @param CS Spherical harmonics coefficients (un-normalized)
     * @param n_max Maximum degree 
     * @param m_max Maximum order (m_max<=n_max; m_max=0 for zonals, only)
     * @return Acceleration (a=d^2r/dt^2)
     */
    public static double[] AccelHarmonic(final double[] r, final double[][] E, double GM, double R_ref, final double[][] CS, int n_max, int m_max )
	{
	  // Local variables
	  int      n,m;                           // Loop counters
	  double   r_sqr, rho, Fac;               // Auxiliary quantities
	  double   x0,y0,z0;                      // Normalized coordinates
	  double   ax,ay,az;                      // Acceleration vector 
	  double   C,S;                           // Gravitational coefficients
	  double[] r_bf = new double[3];          // Body-fixed position
	  double[] a_bf = new double[3];          // Body-fixed acceleration
	  
	  double[][] V = new double[n_max+2][n_max+2]; // Harmonic functions
	  double[][] W = new double[n_max+2][n_max+2]; // work array (0..n_max+1,0..n_max+1)
	       
	  // Body-fixed position 
	  r_bf = MathUtils.mult(E,r); 
	  
	  // Auxiliary quantities
	  r_sqr =  MathUtils.dot(r_bf,r_bf);               // Square of distance
	  rho   =  R_ref*R_ref / r_sqr;
	    
	  x0 = R_ref * r_bf[0] / r_sqr;          // Normalized
	  y0 = R_ref * r_bf[1] / r_sqr;          // coordinates
	  z0 = R_ref * r_bf[2] / r_sqr;
	  
	  
	  //
	  // Evaluate harmonic functions 
	  //   V_nm = (R_ref/r)^(n+1) * P_nm(sin(phi)) * cos(m*lambda)
	  // and 
	  //   W_nm = (R_ref/r)^(n+1) * P_nm(sin(phi)) * sin(m*lambda)
	  // up to degree and order n_max+1
	  //
	  
	  // Calculate zonal terms V(n,0); set W(n,0)=0.0
	  V[0][0] = R_ref / Math.sqrt(r_sqr);
	  W[0][0] = 0.0;
	        
	  V[1][0] = z0 * V[0][0];
	  W[1][0] = 0.0;
	  
	  for (n=2; n<=n_max+1; n++) 
	  {
	    V[n][0] = ( (2*n-1) * z0 * V[n-1][0] - (n-1) * rho * V[n-2][0] ) / n;
	    W[n][0] = 0.0;
	  }
	  
	  // Calculate tesseral and sectorial terms 
	  for(m=1; m<=m_max+1; m++) 
	  {
	      
	    // Calculate V(m,m) .. V(n_max+1,m)
	  
	    V[m][m] = (2*m-1) * ( x0*V[m-1][m-1] - y0*W[m-1][m-1] );
	    W[m][m] = (2*m-1) * ( x0*W[m-1][m-1] + y0*V[m-1][m-1] );
	  
	    if (m<=n_max) 
	    {
	      V[m+1][m] = (2*m+1) * z0 * V[m][m];
	      W[m+1][m] = (2*m+1) * z0 * W[m][m];
	    }
	  
	    for (n=m+2; n<=n_max+1; n++) 
	    {
	      V[n][m] = ( (2*n-1)*z0*V[n-1][m] - (n+m-1)*rho*V[n-2][m] ) / (n-m);
	      W[n][m] = ( (2*n-1)*z0*W[n-1][m] - (n+m-1)*rho*W[n-2][m] ) / (n-m);
	    }
	  
	  }
	  
	  //
	  // Calculate accelerations ax,ay,az
	  //
	  
	  ax = 0.0;
	  ay = 0.0;
	  az = 0.0;
	  
	  for (m=0; m<=m_max; m++)
	  {
	    for (n=m; n<=n_max ; n++)
	    {
	      if (m==0) 
	      {
	        C = CS[n][0];   // = C_n,0
	        ax -=       C * V[n+1][1];
	        ay -=       C * W[n+1][1];
	        az -= (n+1)*C * V[n+1][0];
	      }
	      else 
	      { 
	        C = CS[n][m];   // = C_n,m 
	        S = CS[m-1][n]; // = S_n,m 
	        Fac = 0.5 * (n-m+1) * (n-m+2);
	        ax +=   + 0.5 * ( - C * V[n+1][m+1] - S * W[n+1][m+1] )
	                + Fac * ( + C * V[n+1][m-1] + S * W[n+1][m-1] );
	        ay +=   + 0.5 * ( - C * W[n+1][m+1] + S * V[n+1][m+1] ) 
	                + Fac * ( - C * W[n+1][m-1] + S * V[n+1][m-1] );
	        az += (n-m+1) * ( - C * V[n+1][m]   - S * W[n+1][m]   );
	      }
	    }
	  }
	  
	  // Body-fixed acceleration
	  double gmr2 = (GM/(R_ref*R_ref));
	  a_bf[0] = ax*gmr2;
	  a_bf[1] = ay*gmr2;
	  a_bf[2] = az*gmr2;
	  
	  
	  
	  // Inertial acceleration 
	  
	  return  MathUtils.mult(MathUtils.transpose(E),a_bf);
	         
	} // AccelHarmonic
    
    
    /**
     * Computes the gravitational perturbational acceleration due to a point mass
     * @param r Satellite position vector 
     * @param s Point mass position vector
     * @param GM Gravitational coefficient of point mass
     * @return Acceleration (a=d^2r/dt^2)
     */
    public static double[] AccelPointMass(final double[] r, final double[] s, double GM)
	{    

	   double[] d = new double[3];
	  
	   //  Relative position vector of satellite w.r.t. point mass 
	  
	   d = MathUtils.sub(r , s);
	  
	   // Acceleration 
	  
	   return  MathUtils.scale( MathUtils.add(MathUtils.scale(d,1.0/Math.pow(MathUtils.norm(d),3)) , MathUtils.scale(s,1.0/Math.pow(MathUtils.norm(s),3))),(-GM) );

	}
    
}
