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
 */

package net.java.joglutils.msg.nodes;

import java.awt.image.*;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.media.opengl.*;

import net.java.joglutils.msg.actions.*;
import net.java.joglutils.msg.elements.*;
import net.java.joglutils.msg.math.*;
import net.java.joglutils.msg.misc.*;

/** Represents a vertex/fragment shader. */
public class ShaderNode extends Node {

  private String vertexShaderCode;
  private String fragmentShaderCode;
  private Shader shader;
  private List<Shader> disposedShaders = new ArrayList<Shader>();
  private Map<String, Params> paramMap = new HashMap<String, Params>();

  static {
    // Enable the elements this node affects for known actions
    GLShaderElement.enable(GLRenderAction.getDefaultState());
  }

  /** Initializes this shader from the given String. No OpenGL work is
      done during this call; it is done lazily when the Shader is
      fetched. */
  public void setShader(String fragmentShaderCode) {
      disposeShader();
      this.vertexShaderCode = null;
      this.fragmentShaderCode = fragmentShaderCode;
  }

  /** Initializes this shader from the given String. No OpenGL work is
      done during this call; it is done lazily when the Shader is
      fetched. */
  public void setShader(String vertexShaderCode, String fragmentShaderCode) {
      disposeShader();
      this.vertexShaderCode = vertexShaderCode;
      this.fragmentShaderCode = fragmentShaderCode;
  }

  /**
   * Sets the uniform variable of the given name with the provided
   * integer value.
   *
   * @param name the name of the uniform variable to be set
   * @param i0 the first uniform parameter
   */
  public void setUniform(String name, int i0) {
    int[] iArr = new int[] { i0 };
    paramMap.put(name, new Params(iArr, 1));
  }

  /**
   * Sets the uniform variable of the given name with the provided
   * integer values.
   * <p>
   * No OpenGL work is done during this call; it is done lazily when
   * the Shader is fetched.
   *
   * @param name the name of the uniform variable to be set
   * @param i0 the first uniform parameter
   * @param i1 the second uniform parameter
   */
  public void setUniform(String name, int i0, int i1) {
    int[] iArr = new int[] { i0, i1 };
    paramMap.put(name, new Params(iArr, 2));
  }

  /**
   * Sets the uniform variable of the given name with the provided
   * integer values.
   * <p>
   * No OpenGL work is done during this call; it is done lazily when
   * the Shader is fetched.
   *
   * @param name the name of the uniform variable to be set
   * @param i0 the first uniform parameter
   * @param i1 the second uniform parameter
   * @param i2 the third uniform parameter
   */
  public void setUniform(String name, int i0, int i1, int i2) {
    int[] iArr = new int[] { i0, i1, i2 };
    paramMap.put(name, new Params(iArr, 3));
  }

  /**
   * Sets the uniform variable of the given name with the provided
   * integer values.
   * <p>
   * No OpenGL work is done during this call; it is done lazily when
   * the Shader is fetched.
   *
   * @param name the name of the uniform variable to be set
   * @param i0 the first uniform parameter
   * @param i1 the second uniform parameter
   * @param i2 the third uniform parameter
   * @param i3 the fourth uniform parameter
   */
  public void setUniform(String name,
                         int i0, int i1, int i2, int i3)
  {
    int[] iArr = new int[] { i0, i1, i2, i3 };
    paramMap.put(name, new Params(iArr, 4));
  }

  /**
   * Sets the uniform variable of the given name with the provided
   * float value.
   * <p>
   * No OpenGL work is done during this call; it is done lazily when
   * the Shader is fetched.
   *
   * @param name the name of the uniform variable to be set
   * @param f0 the first uniform parameter
   */
  public void setUniform(String name, float f0) {
    float[] fArr = new float[] { f0 };
    paramMap.put(name, new Params(fArr, 1));
  }

  /**
   * Sets the uniform variable of the given name with the provided
   * float values.
   * <p>
   * No OpenGL work is done during this call; it is done lazily when
   * the Shader is fetched.
   *
   * @param name the name of the uniform variable to be set
   * @param f0 the first uniform parameter
   * @param f1 the second uniform parameter
   */
  public void setUniform(String name, float f0, float f1) {
    float[] fArr = new float[] { f0, f1 };
    paramMap.put(name, new Params(fArr, 2));
  }

  /**
   * Sets the uniform variable of the given name with the provided
   * float values.
   * <p>
   * No OpenGL work is done during this call; it is done lazily when
   * the Shader is fetched.
   *
   * @param name the name of the uniform variable to be set
   * @param f0 the first uniform parameter
   * @param f1 the second uniform parameter
   * @param f2 the third uniform parameter
   */
  public void setUniform(String name, float f0, float f1, float f2) {
    float[] fArr = new float[] { f0, f1, f2 };
    paramMap.put(name, new Params(fArr, 3));
  }

  /**
   * Sets the uniform variable of the given name with the provided
   * float values.
   * <p>
   * No OpenGL work is done during this call; it is done lazily when
   * the Shader is fetched.
   *
   * @param name the name of the uniform variable to be set
   * @param f0 the first uniform parameter
   * @param f1 the second uniform parameter
   * @param f2 the third uniform parameter
   * @param f3 the fourth uniform parameter
   */
  public void setUniform(String name,
                         float f0, float f1, float f2, float f3)
  {
    float[] fArr = new float[] { f0, f1, f2, f3 };
    paramMap.put(name, new Params(fArr, 4));
  }

  /**
   * Sets the uniform array variable of the given name with the provided
   * int array values.
   * <p>
   * No OpenGL work is done during this call; it is done lazily when
   * the Shader is fetched.
   *
   * @param name the name of the uniform variable to be set
   * @param count the number of int elements in the array
   * @param vals the array values to be set
   * @param off the offset into the vals array
   */
  public void setUniformArray1i(String name,
                                int count, int[] vals, int off)
  {
    paramMap.put(name, new Params(vals, 1, count, off));
  }

  /**
   * Sets the uniform array variable of the given name with the provided
   * int array values.
   * <p>
   * No OpenGL work is done during this call; it is done lazily when
   * the Shader is fetched.
   *
   * @param name the name of the uniform variable to be set
   * @param count the number of ivec2 elements in the array
   * @param vals the array values to be set
   * @param off the offset into the vals array
   */
  public void setUniformArray2i(String name,
                                int count, int[] vals, int off)
  {
    paramMap.put(name, new Params(vals, 2, count, off));
  }

  /**
   * Sets the uniform array variable of the given name with the provided
   * int array values.
   * <p>
   * No OpenGL work is done during this call; it is done lazily when
   * the Shader is fetched.
   *
   * @param name the name of the uniform variable to be set
   * @param count the number of ivec3 elements in the array
   * @param vals the array values to be set
   * @param off the offset into the vals array
   */
  public void setUniformArray3i(String name,
                                int count, int[] vals, int off)
  {
    paramMap.put(name, new Params(vals, 3, count, off));
  }

  /**
   * Sets the uniform array variable of the given name with the provided
   * int array values.
   * <p>
   * No OpenGL work is done during this call; it is done lazily when
   * the Shader is fetched.
   *
   * @param name the name of the uniform variable to be set
   * @param count the number of ivec4 elements in the array
   * @param vals the array values to be set
   * @param off the offset into the vals array
   */
  public void setUniformArray4i(String name,
                                int count, int[] vals, int off)
  {
    paramMap.put(name, new Params(vals, 4, count, off));
  }

  /**
   * Sets the uniform array variable of the given name with the provided
   * float array values.
   * <p>
   * No OpenGL work is done during this call; it is done lazily when
   * the Shader is fetched.
   *
   * @param name the name of the uniform variable to be set
   * @param count the number of float elements in the array
   * @param vals the array values to be set
   * @param off the offset into the vals array
   */
  public void setUniformArray1f(String name,
                                int count, float[] vals, int off)
  {
    paramMap.put(name, new Params(vals, 1, count, off));
  }

  /**
   * Sets the uniform array variable of the given name with the provided
   * float array values.
   * <p>
   * No OpenGL work is done during this call; it is done lazily when
   * the Shader is fetched.
   *
   * @param name the name of the uniform variable to be set
   * @param count the number of vec2 elements in the array
   * @param vals the array values to be set
   * @param off the offset into the vals array
   */
  public void setUniformArray2f(String name,
                                int count, float[] vals, int off)
  {
    paramMap.put(name, new Params(vals, 2, count, off));
  }

  /**
   * Sets the uniform array variable of the given name with the provided
   * float array values.
   * <p>
   * No OpenGL work is done during this call; it is done lazily when
   * the Shader is fetched.
   *
   * @param name the name of the uniform variable to be set
   * @param count the number of vec3 elements in the array
   * @param vals the array values to be set
   * @param off the offset into the vals array
   */
  public void setUniformArray3f(String name,
                                int count, float[] vals, int off)
  {
    paramMap.put(name, new Params(vals, 3, count, off));
  }

  /**
   * Sets the uniform array variable of the given name with the provided
   * float array values.
   * <p>
   * No OpenGL work is done during this call; it is done lazily when
   * the Shader is fetched.
   *
   * @param name the name of the uniform variable to be set
   * @param count the number of vec4 elements in the array
   * @param vals the array values to be set
   * @param off the offset into the vals array
   * @throws GLException if no OpenGL context was current or if any
   * OpenGL-related errors occurred
   */
  public void setUniformArray4f(String name,
                                int count, float[] vals, int off)
  {
    paramMap.put(name, new Params(vals, 4, count, off));
  }

  /**
   * Sets the uniform matrix (or matrix array) variable of the given name
   * with the provided matrix values.
   * <p>
   * No OpenGL work is done during this call; it is done lazily when
   * the Shader is fetched.
   *
   * @param name the name of the uniform variable to be set
   * @param count the number of 2x2 matrices (mat2 elements) in the array
   * @param transpose if false, each matrix is assumed to be suppplied in
   * column major order; otherwise assumed to be supplied in row major order
   * @param vals the matrix values to be set
   * @param off the offset into the vals array
   */
  public void setUniformMatrices2f(String name,
                                   int count, boolean transpose,
                                   float[] vals, int off)
  {
    paramMap.put(name, new Params(vals, 2, count, off, true, transpose));
  }

  /**
   * Sets the uniform matrix (or matrix array) variable of the given name
   * with the provided matrix values.
   * <p>
   * No OpenGL work is done during this call; it is done lazily when
   * the Shader is fetched.
   *
   * @param name the name of the uniform variable to be set
   * @param count the number of 3x3 matrices (mat3 elements) in the array
   * @param transpose if false, each matrix is assumed to be suppplied in
   * column major order; otherwise assumed to be supplied in row major order
   * @param vals the matrix values to be set
   * @param off the offset into the vals array
   */
  public void setUniformMatrices3f(String name,
                                   int count, boolean transpose,
                                   float[] vals, int off)
  {
    paramMap.put(name, new Params(vals, 3, count, off, true, transpose));
  }

  /**
   * Sets the uniform matrix (or matrix array) variable of the given name
   * with the provided matrix values.
   * <p>
   * No OpenGL work is done during this call; it is done lazily when
   * the Shader is fetched.
   *
   * @param name the name of the uniform variable to be set
   * @param count the number of 4x4 matrices (mat4 elements) in the array
   * @param transpose if false, each matrix is assumed to be suppplied in
   * column major order; otherwise assumed to be supplied in row major order
   * @param vals the matrix values to be set
   * @param off the offset into the vals array
   */
  public void setUniformMatrices4f(String name,
                                   int count, boolean transpose,
                                   float[] vals, int off)
  {
    paramMap.put(name, new Params(vals, 4, count, off, true, transpose));
  }

  /** Fetches the Shader object associated with this ShaderNode.
      It is required to call this each frame during rendering.
      An OpenGL context must be current at the time this method is
      called or a GLException will be thrown. */
  public Shader getShader() throws GLException {
    lazyDispose();
    if (shader == null) {
        this.shader = new Shader(vertexShaderCode, fragmentShaderCode);
    }
    sendParams();
    return shader;
  }

  public void doAction(Action action) {
    if (ShaderElement.isEnabled(action.getState())) {
      ShaderElement.set(action.getState(), this);
    }
  }

  /** Resets the OpenGL state of this node without explicitly
      disposing of any resources. This should only be called when you
      know you are using this ShaderNode across the destruction and
      re-creation of OpenGL contexts and know how to re-initialize the
      ShaderNode from its previous state. */
  public void resetGL(GLResetAction action) {
    disposeShader();
    synchronized(this) {
      disposedShaders.clear();
    }
  }

  private synchronized void disposeShader() {
    if (shader != null) {
      disposedShaders.add(shader);
      shader = null;
    }
  }

  private void lazyDispose() {
    while (!disposedShaders.isEmpty()) {
      Shader s = null;
      synchronized (this) {
        s = disposedShaders.remove(disposedShaders.size() - 1);
      }
      s.dispose();
    }
  }
  
  /**
   * Sends the stored uniform parameters down to GL.
   * An OpenGL context must be current at the time this method is
   * called or a GLException will be thrown.
   */
  private void sendParams() {
    if (!paramMap.isEmpty()) {
      // FIXME: the shader needs to be enabled prior to setting uniforms,
      // but doing so here may lead to too many enable/disable calls
      shader.enable();
      for (String name : paramMap.keySet()) {
        Params params = paramMap.get(name);
        if (params.isMatrix) {
          switch (params.vecSize) {
            case 2:
              shader.setUniformMatrices2f(name, params.numElems,
                                          params.transpose,
                                          params.fArr,
                                          params.offset);
              break;
            case 3:
              shader.setUniformMatrices3f(name, params.numElems,
                                          params.transpose,
                                          params.fArr,
                                          params.offset);
              break;
            case 4:
              shader.setUniformMatrices4f(name, params.numElems,
                                          params.transpose,
                                          params.fArr,
                                          params.offset);
              break;
            default:
              continue;
          }
        } else if (params.fArr != null) {
          switch (params.vecSize) {
            case 1:
              shader.setUniformArray1f(name, params.numElems,
                                       params.fArr, params.offset);
              break;
            case 2:
              shader.setUniformArray2f(name, params.numElems,
                                       params.fArr, params.offset);
              break;
            case 3:
              shader.setUniformArray3f(name, params.numElems,
                                       params.fArr, params.offset);
              break;
            case 4:
              shader.setUniformArray4f(name, params.numElems,
                                       params.fArr, params.offset);
              break;
            default:
              continue;
          }
        } else if (params.iArr != null) {
          switch (params.vecSize) {
            case 1:
              shader.setUniformArray1i(name, params.numElems,
                                       params.iArr, params.offset);
              break;
            case 2:
              shader.setUniformArray2i(name, params.numElems,
                                       params.iArr, params.offset);
              break;
            case 3:
              shader.setUniformArray3i(name, params.numElems,
                                       params.iArr, params.offset);
              break;
            case 4:
              shader.setUniformArray4i(name, params.numElems,
                                       params.iArr, params.offset);
              break;
            default:
              continue;
          }
        }
      }
      // FIXME: see above...
      shader.disable();
      paramMap.clear();
    }
  }
  
  /**
   * A small class to encapsulate int or float data passed in through
   * the various setUniform*() methods.
   */
  private static class Params {
    private float[] fArr;
    private int[] iArr;
    private int vecSize; // 1, 2, 3, or 4
    private int numElems;
    private int offset;
    private boolean isMatrix;
    private boolean transpose;

    Params(float[] fArr, int vecSize) {
      this(fArr, vecSize, 1, 0);
    }

    Params(float[] fArr, int vecSize, int numElems, int offset) {
      this(fArr, vecSize, numElems, offset, false, false);
    }

    Params(float[] fArr, int vecSize, int numElems, int offset,
           boolean isMatrix, boolean transpose)
    {
      this.fArr = fArr;
      this.vecSize = vecSize;
      this.numElems = numElems;
      this.offset = offset;
      this.isMatrix = isMatrix;
      this.transpose = transpose;
    }

    Params(int[] iArr, int vecSize) {
      this(iArr, vecSize, 1, 0);
    }

    Params(int[] iArr, int vecSize, int numElems, int offset) {
      this.iArr = iArr;
      this.vecSize = vecSize;
      this.numElems = numElems;
      this.offset = offset;
      this.isMatrix = false;
      this.transpose = false;
    }
  }
}
