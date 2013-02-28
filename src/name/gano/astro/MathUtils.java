// Shawn E. Gano
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

/**
 * Various Math functions; many of them for vector and matrix operations for 3 dimensions.
 */
public class MathUtils
{

    /**
     * multiply two matrices 3x3
     *
     * @param a 3x3 matrix
     * @param b 3x3 matrix
     * @return a x b
     */
	public static double[][] mult(double[][] a, double[][] b)
	{
		double[][] c = new double[3][3];

		for (int i = 0; i < 3; i++) // row
		{
			for (int j = 0; j < 3; j++) // col
			{
				c[i][j] = 0.0;
				for (int k = 0; k < 3; k++)
				{
					c[i][j] += a[i][k] * b[k][j];
				}
			}
		}

		return c;

	} // mult 3x3 matrices

    /**
     * multiply matrix nxn by vector nx1
     *
     * @param a nxn matrix
     * @param b nx1 vector
     * @return a x b
     */
	public static double[] mult(double[][] a, double[] b)
	{
		double[] c = new double[b.length];

		for (int i = 0; i < b.length; i++) // row
		{
			c[i] = 0.0;
			for (int k = 0; k < b.length; k++)
			{
				c[i] += a[i][k] * b[k];
			}
		}

		return c;

	} // mult 3x3 matrices
	
	// dot product for 3D vectors
    /**
     * dot product for 3D vectors
     *
     * @param a 3x1 vector
     * @param b 3x1 vector
     * @return a dot b
     */
	public static double dot(double[] a, double[] b)
	{
		double c =0;;

		for (int i = 0; i < 3; i++) // row
		{
			c += a[i]*b[i];
		}

		return c;

	} // mult 3x3 matrices
	
	
    /**
     * transpose of 3x3 matrix
     *
     * @param a 3x3 matrix
     * @return a^T
     */
	public static double[][] transpose(double[][] a)
	{
		double[][] c = new double[3][3];
		for(int i=0;i<3;i++)
		{
			for(int k=0;k<3;k++)
			{
				c[i][k] = a[k][i];
			}
		}
		return c;
	}
	
	
    /**
     * vector subtraction
     *
     * @param a vector of length 3
     * @param b vector of length 3
     * @return a-b
     */
	public static double[] sub(double[] a, double[] b)
	{
		double[] c = new double[3];
		for(int i=0;i<3;i++)
		{
			c[i] = a[i] - b[i];
		}
		
		return c;
	}
	
    /**
     * vector addition
     *
     * @param a vector of length 3
     * @param b vector of length 3
     * @return a+b
     */
	public static double[] add(double[] a, double[] b)
	{
		double[] c = new double[3];
		for(int i=0;i<3;i++)
		{
			c[i] = a[i] + b[i];
		}
		
		return c;
	}
	
//	vector 2-norm
    /**
     * vector 2-norm
     *
     * @param a vector of length 3
     * @return norm(a)
     */
	public static double norm(double[] a)
	{
		double c = 0.0;
		
		for(int i=0;i<a.length;i++)
		{
			c += a[i]*a[i];
		}
		
		return Math.sqrt(c);
	}
	
//	multiply a vector times a scalar
    /**
     * multiply a vector times a scalar
     *
     * @param a a vector of length 3
     * @param b scalar
     * @return a * b
     */
	public static double[] scale(double[] a, double b)
	{
		double[] c = new double[3];
		
		for(int i=0;i<3;i++)
		{
			c[i] = a[i]*b;
		}
		
		return c;
	}
	
	// cross product or 2 3x1 vectors
    /**
     * cross product or 2 3x1 vectors
     *
     * @param left a vector of length 3
     * @param right a vector of length 3
     * @return a cross b
     */
	public static double[] cross (final double[] left, final double[] right)
	{
	  if ( (left.length!=3) || (right.length!=3) ) 
	  {
	    System.out.println("ERROR: Invalid dimension in Cross(Vector,Vector)");
	  }
	  
	  double[] Result = new double[3];
	  Result[0] = left[1]*right[2] - left[2]*right[1];
	  Result[1] = left[2]*right[0] - left[0]*right[2];
	  Result[2] = left[0]*right[1] - left[1]*right[0];
	  
	  return Result;
	} // cross


        //
	// Fractional part of a number (y=x-[x])
	//
    /**
     * Fractional part of a number (y=x-|x|)
     * @param x number
     * @return the fractional part of that number (e.g., for 5.3 would return 0.3)
     */
	public static double Frac(double x)
	{
		return x - Math.floor(x);
	};

	//
	// x mod y
	//
    /**
     * x mod y
     * @param x value
     * @param y value
     * @return x mod y
     */
	public static double Modulo(double x, double y)
	{
		return y * Frac(x / y);
	}
        
        // Elementary rotation matrix about x axis
        
        /**
         * Creates a unit vector from the given vector
         * @param vec any vector n-dimensional
         * @return unit vector (n-dimensional) with norm = 1
         */
        public static double[] UnitVector(double[] vec)
        {
            int n = vec.length;
            double[] unitVect = new double[n];
            
            double normVec = MathUtils.norm(vec);
            
            unitVect = MathUtils.scale(vec, 1.0/normVec);
            
            //System.out.println("Norm:" + MathUtils.norm(unitVect));
             
            return unitVect;
        } // UnitVector

    /**
     * Elementary rotation matrix about x axis
     *
     * @param Angle Angle in radians
     * @return Elementary rotation matrix about x axis
     */
	public static double[][] R_x(double Angle)
	{
		final double C = Math.cos(Angle);
		final double S = Math.sin(Angle);
		double[][] U = new double[3][3];
		U[0][0] = 1.0;
		U[0][1] = 0.0;
		U[0][2] = 0.0;
		U[1][0] = 0.0;
		U[1][1] = +C;
		U[1][2] = +S;
		U[2][0] = 0.0;
		U[2][1] = -S;
		U[2][2] = +C;
		return U;
	}

         // Elementary rotation matrix about y axis
    /**
     * Elementary rotation matrix about y axis
     *
     * @param Angle  Angle in radians
     * @return Elementary rotation matrix about y axis
     */
	public static double[][] R_y(double Angle)
	{
		final double C = Math.cos(Angle);
		final double S = Math.sin(Angle);
		double[][] U = new double[3][3];
		U[0][0] = +C;
		U[0][1] = 0.0;
		U[0][2] = -S;
		U[1][0] = 0.0;
		U[1][1] = 1.0;
		U[1][2] = 0.0;
		U[2][0] = +S;
		U[2][1] = 0.0;
		U[2][2] = +C;
		return U;
	}

         // Elementary rotation matrix about z axis
    /**
     * Elementary rotation matrix about z axis
     *
     * @param Angle Angle in radians
     * @return Elementary rotation matrix about z axis
     */
	public static double[][] R_z(double Angle)
	{
		final double C = Math.cos(Angle);
		final double S = Math.sin(Angle);
		double[][] U = new double[3][3];
		U[0][0] = +C;
		U[0][1] = +S;
		U[0][2] = 0.0;
		U[1][0] = -S;
		U[1][1] = +C;
		U[1][2] = 0.0;
		U[2][0] = 0.0;
		U[2][1] = 0.0;
		U[2][2] = 1.0;
		return U;
	}
        

}
