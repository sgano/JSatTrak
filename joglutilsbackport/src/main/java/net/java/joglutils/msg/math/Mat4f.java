/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 * 
 */

package net.java.joglutils.msg.math;

/** A (very incomplete) 4x4 matrix class. Representation assumes
    row-major order and multiplication by column vectors on the
    right. */

public class Mat4f {
  private float[] data;

  /** Creates new matrix initialized to the zero matrix */
  public Mat4f() {
    data = new float[16];
  }

  /** Creates new matrix initialized to argument's contents */
  public Mat4f(Mat4f arg) {
    this();
    set(arg);
  }

  /** Sets this matrix to the identity matrix */
  public void makeIdent() {
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        if (i == j) {
          set(i, j, 1.0f);
        } else {
          set(i, j, 0.0f);
        }
      }
    }
  }

  /** Sets this matrix to be equivalent to the given one */
  public void set(Mat4f arg) {
    float[] mine = data;
    float[] yours = arg.data;
    for (int i = 0; i < mine.length; i++) {
      mine[i] = yours[i];
    }
  }

  /** Gets the (i,j)th element of this matrix, where i is the row
      index and j is the column index */
  public float get(int i, int j) {
    return data[4 * i + j];
  }

  /** Sets the (i,j)th element of this matrix, where i is the row
      index and j is the column index */
  public void set(int i, int j, float val) {
    data[4 * i + j] = val;
  }

  /** Sets the translation component of this matrix (i.e., the three
      top elements of the third column) without touching any of the
      other parts of the matrix */
  public void setTranslation(Vec3f trans) {
    set(0, 3, trans.x());
    set(1, 3, trans.y());
    set(2, 3, trans.z());
  }

  /** Sets the rotation component of this matrix (i.e., the upper left
      3x3) without touching any of the other parts of the matrix */
  public void setRotation(Rotf rot) {
    rot.toMatrix(this);
  }

  /** Sets the upper-left 3x3 of this matrix assuming that the given
      x, y, and z vectors form an orthonormal basis */
  public void setRotation(Vec3f x, Vec3f y, Vec3f z) {
    set(0, 0, x.x());
    set(1, 0, x.y());
    set(2, 0, x.z());

    set(0, 1, y.x());
    set(1, 1, y.y());
    set(2, 1, y.z());

    set(0, 2, z.x());
    set(1, 2, z.y());
    set(2, 2, z.z());
  }

  /** Gets the upper left 3x3 of this matrix as a rotation. Currently
      does not work if there are scales. Ignores translation
      component. */
  public void getRotation(Rotf rot) {
    rot.fromMatrix(this);
  }

  /** Sets the elements (0, 0), (1, 1), and (2, 2) with the
      appropriate elements of the given three-dimensional scale
      vector. Does not perform a full multiplication of the upper-left
      3x3; use this with an identity matrix in conjunction with
      <code>mul</code> for that. */
  public void setScale(Vec3f scale) {
    set(0, 0, scale.x());
    set(1, 1, scale.y());
    set(2, 2, scale.z());
  }

  /** Inverts this matrix assuming that it represents a rigid
      transform (i.e., some combination of rotations and
      translations). Assumes column vectors. Algorithm: transposes
      upper left 3x3; negates translation in rightmost column and
      transforms by inverted rotation. */
  public void invertRigid() {
    float t;
    // Transpose upper left 3x3
    t = get(0, 1);
    set(0, 1, get(1, 0));
    set(1, 0, t);
    t = get(0, 2);
    set(0, 2, get(2, 0));
    set(2, 0, t);
    t = get(1, 2);
    set(1, 2, get(2, 1));
    set(2, 1, t);
    // Transform negative translation by this
    Vec3f negTrans = new Vec3f(-get(0, 3), -get(1, 3), -get(2, 3));
    Vec3f trans = new Vec3f();
    xformDir(negTrans, trans);
    set(0, 3, trans.x());
    set(1, 3, trans.y());
    set(2, 3, trans.z());
  }

  /** Performs general 4x4 matrix inversion.
      @throws SingularMatrixException if this matrix is singular (i.e., non-invertible)
  */
  public void invert() throws SingularMatrixException {
    invertGeneral(this);
  }

  /** Returns this * b; creates new matrix */
  public Mat4f mul(Mat4f b) {
    Mat4f tmp = new Mat4f();
    tmp.mul(this, b);
    return tmp;
  }

  /** this = a * b */
  public void mul(Mat4f a, Mat4f b) {
    for (int rc = 0; rc < 4; rc++)
      for (int cc = 0; cc < 4; cc++) {
        float tmp = 0.0f;
        for (int i = 0; i < 4; i++)
          tmp += a.get(rc, i) * b.get(i, cc);
        set(rc, cc, tmp);
      }
  }
  
  /** Transpose this matrix in place. */
  public void transpose() {
    float t;
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < i; j++) {
        t = get(i, j);
        set(i, j, get(j, i));
        set(j, i, t);
      }
    }
  }

  /** Multiply a 4D vector by this matrix. NOTE: src and dest must be
      different vectors. */
  public void xformVec(Vec4f src, Vec4f dest) {
    for (int rc = 0; rc < 4; rc++) {
      float tmp = 0.0f;
      for (int cc = 0; cc < 4; cc++) {
        tmp += get(rc, cc) * src.get(cc);
      }
      dest.set(rc, tmp);
    }
  }

  /** Transforms a 3D vector as though it had a homogeneous coordinate
      and assuming that this matrix represents only rigid
      transformations; i.e., is not a full transformation. NOTE: src
      and dest must be different vectors. */
  public void xformPt(Vec3f src, Vec3f dest) {
    for (int rc = 0; rc < 3; rc++) {
      float tmp = 0.0f;
      for (int cc = 0; cc < 3; cc++) {
        tmp += get(rc, cc) * src.get(cc);
      }
      tmp += get(rc, 3);
      dest.set(rc, tmp);
    }
  }
  
  /** Transforms src using only the upper left 3x3. NOTE: src and dest
      must be different vectors. */
  public void xformDir(Vec3f src, Vec3f dest) {
    for (int rc = 0; rc < 3; rc++) {
      float tmp = 0.0f;
      for (int cc = 0; cc < 3; cc++) {
        tmp += get(rc, cc) * src.get(cc);
      }
      dest.set(rc, tmp);
    }
  }

  /** Transforms the given line (origin plus direction) by this
      matrix. */
  public Line xformLine(Line line) {
    Vec3f pt = new Vec3f();
    Vec3f dir = new Vec3f();
    xformPt(line.getPoint(), pt);
    xformDir(line.getDirection(), dir);
    return new Line(dir, pt);
  }
  
  /** Copies data in column-major (OpenGL format) order into passed
      float array, which must have length 16 or greater. */
  public void getColumnMajorData(float[] out) {
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        out[4 * j + i] = get(i, j);
      }
    }
  }

  /** Returns the matrix data in row-major format, which is the
      opposite of OpenGL's convention. */
  public float[] getRowMajorData() {
    return data;
  }

  public Matf toMatf() {
    Matf out = new Matf(4, 4);
    for (int i = 0; i < 4; i++) {
      for (int j = 0; j < 4; j++) {
        out.set(i, j, get(i, j));
      }
    }
    return out;
  }

  public String toString() {
    String endl = System.getProperty("line.separator");
    return "(" +
      get(0, 0) + ", " + get(0, 1) + ", " + get(0, 2) + ", " + get(0, 3) + endl +
      get(1, 0) + ", " + get(1, 1) + ", " + get(1, 2) + ", " + get(1, 3) + endl +
      get(2, 0) + ", " + get(2, 1) + ", " + get(2, 2) + ", " + get(2, 3) + endl +
      get(3, 0) + ", " + get(3, 1) + ", " + get(3, 2) + ", " + get(3, 3) + ")";
  }

  //----------------------------------------------------------------------
  // Internals only below this point
  //

  // The following code was borrowed from Java 3D's vecmath implementation

  private void invertGeneral(Mat4f m1) {
    double temp[] = new double[16];
    double result[] = new double[16];
    int row_perm[] = new int[4];
    int i, r, c;

    // Use LU decomposition and backsubstitution code specifically
    // for floating-point 4x4 matrices.

    // Copy source matrix to t1tmp 
    temp[0] = m1.get(0, 0);
    temp[1] = m1.get(0, 1);
    temp[2] = m1.get(0, 2);
    temp[3] = m1.get(0, 3);
 
    temp[4] = m1.get(1, 0);
    temp[5] = m1.get(1, 1);
    temp[6] = m1.get(1, 2);
    temp[7] = m1.get(1, 3);
 
    temp[8] = m1.get(2, 0);
    temp[9] = m1.get(2, 1);
    temp[10] = m1.get(2, 2);
    temp[11] = m1.get(2, 3);
 
    temp[12] = m1.get(3, 0);
    temp[13] = m1.get(3, 1);
    temp[14] = m1.get(3, 2);
    temp[15] = m1.get(3, 3);

    // Calculate LU decomposition: Is the matrix singular? 
    if (!luDecomposition(temp, row_perm)) {
      // Matrix has no inverse 
      throw new SingularMatrixException();
    }

    // Perform back substitution on the identity matrix 
    for(i=0;i<16;i++) result[i] = 0.0;
    result[0] = 1.0; result[5] = 1.0; result[10] = 1.0; result[15] = 1.0;
    luBacksubstitution(temp, row_perm, result);

    set(0, 0, (float)result[0]);
    set(0, 1, (float)result[1]);
    set(0, 2, (float)result[2]);
    set(0, 3, (float)result[3]);

    set(1, 0, (float)result[4]);
    set(1, 1, (float)result[5]);
    set(1, 2, (float)result[6]);
    set(1, 3, (float)result[7]);
 
    set(2, 0, (float)result[8]);
    set(2, 1, (float)result[9]);
    set(2, 2, (float)result[10]);
    set(2, 3, (float)result[11]);
 
    set(3, 0, (float)result[12]);
    set(3, 1, (float)result[13]);
    set(3, 2, (float)result[14]);
    set(3, 3, (float)result[15]);
  }

  /**
   * Given a 4x4 array "matrix0", this function replaces it with the 
   * LU decomposition of a row-wise permutation of itself.  The input 
   * parameters are "matrix0" and "dimen".  The array "matrix0" is also 
   * an output parameter.  The vector "row_perm[4]" is an output 
   * parameter that contains the row permutations resulting from partial 
   * pivoting.  The output parameter "even_row_xchg" is 1 when the 
   * number of row exchanges is even, or -1 otherwise.  Assumes data 
   * type is always double.
   *
   * This function is similar to luDecomposition, except that it
   * is tuned specifically for 4x4 matrices.
   *
   * @return true if the matrix is nonsingular, or false otherwise.
   */
  //
  // Reference: Press, Flannery, Teukolsky, Vetterling, 
  //	      _Numerical_Recipes_in_C_, Cambridge University Press, 
  //	      1988, pp 40-45.
  //
  static boolean luDecomposition(double[] matrix0,
                                 int[] row_perm) {

    double row_scale[] = new double[4];

    // Determine implicit scaling information by looping over rows 
    {
      int i, j;
      int ptr, rs;
      double big, temp;

      ptr = 0;
      rs = 0;

      // For each row ... 
      i = 4;
      while (i-- != 0) {
        big = 0.0;

        // For each column, find the largest element in the row 
        j = 4;
        while (j-- != 0) {
          temp = matrix0[ptr++];
          temp = Math.abs(temp);
          if (temp > big) {
            big = temp;
          }
        }

        // Is the matrix singular? 
        if (big == 0.0) {
          return false;
        }
        row_scale[rs++] = 1.0 / big;
      }
    }

    {
      int j;
      int mtx;

      mtx = 0;

      // For all columns, execute Crout's method 
      for (j = 0; j < 4; j++) {
        int i, imax, k;
        int target, p1, p2;
        double sum, big, temp;

        // Determine elements of upper diagonal matrix U 
        for (i = 0; i < j; i++) {
          target = mtx + (4*i) + j;
          sum = matrix0[target];
          k = i;
          p1 = mtx + (4*i);
          p2 = mtx + j;
          while (k-- != 0) {
            sum -= matrix0[p1] * matrix0[p2];
            p1++;
            p2 += 4;
          }
          matrix0[target] = sum;
        }

        // Search for largest pivot element and calculate
        // intermediate elements of lower diagonal matrix L.
        big = 0.0;
        imax = -1;
        for (i = j; i < 4; i++) {
          target = mtx + (4*i) + j;
          sum = matrix0[target];
          k = j;
          p1 = mtx + (4*i);
          p2 = mtx + j;
          while (k-- != 0) {
            sum -= matrix0[p1] * matrix0[p2];
            p1++;
            p2 += 4;
          }
          matrix0[target] = sum;

          // Is this the best pivot so far? 
          if ((temp = row_scale[i] * Math.abs(sum)) >= big) {
            big = temp;
            imax = i;
          }
        }

        if (imax < 0) {
          throw new RuntimeException("Logic error: imax < 0");
        }

        // Is a row exchange necessary? 
        if (j != imax) {
          // Yes: exchange rows 
          k = 4;
          p1 = mtx + (4*imax);
          p2 = mtx + (4*j);
          while (k-- != 0) {
            temp = matrix0[p1];
            matrix0[p1++] = matrix0[p2];
            matrix0[p2++] = temp;
          }

          // Record change in scale factor 
          row_scale[imax] = row_scale[j];
        }

        // Record row permutation 
        row_perm[j] = imax;

        // Is the matrix singular 
        if (matrix0[(mtx + (4*j) + j)] == 0.0) {
          return false;
        }

        // Divide elements of lower diagonal matrix L by pivot 
        if (j != (4-1)) {
          temp = 1.0 / (matrix0[(mtx + (4*j) + j)]);
          target = mtx + (4*(j+1)) + j;
          i = 3 - j;
          while (i-- != 0) {
            matrix0[target] *= temp;
            target += 4;
          }
        }
      }
    }

    return true;
  }

  /**
   * Solves a set of linear equations.  The input parameters "matrix1",
   * and "row_perm" come from luDecompostionD4x4 and do not change
   * here.  The parameter "matrix2" is a set of column vectors assembled
   * into a 4x4 matrix of floating-point values.  The procedure takes each
   * column of "matrix2" in turn and treats it as the right-hand side of the
   * matrix equation Ax = LUx = b.  The solution vector replaces the
   * original column of the matrix.
   *
   * If "matrix2" is the identity matrix, the procedure replaces its contents
   * with the inverse of the matrix from which "matrix1" was originally
   * derived.
   */
  //
  // Reference: Press, Flannery, Teukolsky, Vetterling, 
  //	      _Numerical_Recipes_in_C_, Cambridge University Press, 
  //	      1988, pp 44-45.
  //
  static void luBacksubstitution(double[] matrix1,
                                 int[] row_perm,
                                 double[] matrix2) {

    int i, ii, ip, j, k;
    int rp;
    int cv, rv;
	
    //	rp = row_perm;
    rp = 0;

    // For each column vector of matrix2 ... 
    for (k = 0; k < 4; k++) {
      //	    cv = &(matrix2[0][k]);
      cv = k;
      ii = -1;

      // Forward substitution 
      for (i = 0; i < 4; i++) {
        double sum;

        ip = row_perm[rp+i];
        sum = matrix2[cv+4*ip];
        matrix2[cv+4*ip] = matrix2[cv+4*i];
        if (ii >= 0) {
          //		    rv = &(matrix1[i][0]);
          rv = i*4;
          for (j = ii; j <= i-1; j++) {
            sum -= matrix1[rv+j] * matrix2[cv+4*j];
          }
        }
        else if (sum != 0.0) {
          ii = i;
        }
        matrix2[cv+4*i] = sum;
      }

      // Backsubstitution 
      //	    rv = &(matrix1[3][0]);
      rv = 3*4;
      matrix2[cv+4*3] /= matrix1[rv+3];

      rv -= 4;
      matrix2[cv+4*2] = (matrix2[cv+4*2] -
                         matrix1[rv+3] * matrix2[cv+4*3]) / matrix1[rv+2];

      rv -= 4;
      matrix2[cv+4*1] = (matrix2[cv+4*1] -
                         matrix1[rv+2] * matrix2[cv+4*2] -
                         matrix1[rv+3] * matrix2[cv+4*3]) / matrix1[rv+1];

      rv -= 4;
      matrix2[cv+4*0] = (matrix2[cv+4*0] -
                         matrix1[rv+1] * matrix2[cv+4*1] -
                         matrix1[rv+2] * matrix2[cv+4*2] -
                         matrix1[rv+3] * matrix2[cv+4*3]) / matrix1[rv+0];
    }
  }
}
