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

package net.java.joglutils.msg.misc;

import javax.media.opengl.GL;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;
import static javax.media.opengl.GL.*;

/**
 * Represents an OpenGL shader program object, which can be constructed from
 * the source code for a vertex shader, a fragment shader, or both.
 * Contains convenience methods for enabling/disabling shader state.
 * <p>
 * Usage example:
 * <pre>
 *     String source =
 *         "uniform sampler2D myTex;" +
 *         "void main(void)" +
 *         "{" +
 *         "    vec4 src = texture2D(myTex, gl_TexCoord[0].st);" +
 *         "    gl_FragColor = src.bgra;" + // swizzle!
 *         "}";
 *     Shader shader = new Shader(source);
 *     shader.setUniform("myTex", 0); // myTex will be on texture unit 0
 *     ...
 *     shader.enable();
 *     texture.enable();
 *     texture.bind();
 *     ...
 *     texture.disable();
 *     shader.disable();
 * };
 * </pre>
 *
 * @author Chris Campbell
 */
public class Shader {
    
    /**
     * The handle to the OpenGL fragment program object.
     */
    private int id;
    
    /**
     * Creates a new shader program object and compiles/links the provided
     * fragment shader code into that object.
     * 
     * @param fragmentCode a {@code String} representing the fragment shader
     * source code to be compiled and linked
     * 
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public Shader(String fragmentCode)
        throws GLException
    {
        GL gl = GLU.getCurrentGL();
        id = createProgram(gl, null, fragmentCode);
    }
    
    /**
     * Creates a new shader program object and compiles/links the provided
     * vertex shader and fragment shader code into that object.
     * 
     * @param vertexCode a {@code String} representing the vertex shader
     * source code to be compiled and linked; this may be null if only a
     * fragment shader is going to be needed
     * @param fragmentCode a {@code String} representing the fragment shader
     * source code to be compiled and linked; this may be null if only a
     * vertex shader is going to be needed
     * 
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public Shader(String vertexCode, String fragmentCode)
        throws GLException
    {
        GL gl = GLU.getCurrentGL();
        id = createProgram(gl, vertexCode, fragmentCode);
    }
    
    /**
     * Compiles and links a new shader program using the given sources.  If
     * successful, this function returns a handle to the newly created shader
     * program; otherwise returns 0.
     * 
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    private static int createProgram(GL gl,
                                     String vertexShaderSource,
                                     String fragmentShaderSource)
        throws GLException
    {
        if (vertexShaderSource == null && fragmentShaderSource == null) {
            throw new GLException(
                "Either vertexShaderSource or fragmentShaderSource " +
                "must be specified");
        }
        
        int shaderProgram;
        int vertexShader = 0;
        int fragmentShader = 0;
        int[] success = new int[1];
        int[] infoLogLength = new int[1];

        if (vertexShaderSource != null) {
            vertexShader = compileShader(gl, vertexShaderSource, true);
            if (vertexShader == 0) {
                return 0;
            }
        }
        
        if (fragmentShaderSource != null) {
            fragmentShader = compileShader(gl, fragmentShaderSource, false);
            if (fragmentShader == 0) {
                if (vertexShader != 0) {
                    gl.glDeleteObjectARB(vertexShader);
                }
                return 0;
            }
        }

        // create the program object and attach it to the shader
        shaderProgram = gl.glCreateProgramObjectARB();
        if (vertexShader != 0) {
            gl.glAttachObjectARB(shaderProgram, vertexShader);
            // it is now safe to delete the shader object
            gl.glDeleteObjectARB(vertexShader);
        }
        if (fragmentShader != 0) {
            gl.glAttachObjectARB(shaderProgram, fragmentShader);
            // it is now safe to delete the shader object
            gl.glDeleteObjectARB(fragmentShader);
        }

        // link the program
        gl.glLinkProgramARB(shaderProgram);
        gl.glGetObjectParameterivARB(shaderProgram,
                                     GL_OBJECT_LINK_STATUS_ARB,
                                     success, 0);

        // print the linker messages, if necessary
        gl.glGetObjectParameterivARB(shaderProgram,
                                     GL_OBJECT_INFO_LOG_LENGTH_ARB,
                                     infoLogLength, 0);
        if (infoLogLength[0] > 1) {
            byte[] infoLog = new byte[1024];
            gl.glGetInfoLogARB(shaderProgram, 1024, null, 0, infoLog, 0);
            System.err.println("Linker message: " +
                               new String(infoLog));
        }

        if (success[0] == 0) {
            gl.glDeleteObjectARB(shaderProgram);
            return 0;
        }

        return shaderProgram;
    }
    
    /**
     * Compiles the given shader program.  If successful, this function returns
     * a handle to the newly created shader object; otherwise returns 0.
     * 
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    private static int compileShader(GL gl, String shaderSource, boolean vertex)
        throws GLException
    {
        int kind = vertex ? GL_VERTEX_SHADER_ARB : GL_FRAGMENT_SHADER_ARB;
        int shader;
        int[] success = new int[1];
        int[] infoLogLength = new int[1];
        
        // create the shader object and compile the shader source code
        shader = gl.glCreateShaderObjectARB(kind);
        gl.glShaderSourceARB(shader, 1, new String[] { shaderSource }, null, 0);
        gl.glCompileShaderARB(shader);
        gl.glGetObjectParameterivARB(shader,
                                     GL_OBJECT_COMPILE_STATUS_ARB,
                                     success, 0);

        // print the compiler messages, if necessary
        gl.glGetObjectParameterivARB(shader,
                                     GL_OBJECT_INFO_LOG_LENGTH_ARB,
                                     infoLogLength, 0);
        if (infoLogLength[0] > 1) {
            byte[] infoLog = new byte[1024];
            gl.glGetInfoLogARB(shader, 1024, null, 0, infoLog, 0);
            System.err.println((vertex ? "Vertex" : "Fragment") +
                               " compile message: " +
                               new String(infoLog));
        }

        if (success[0] == 0) {
            gl.glDeleteObjectARB(shader);
            return 0;
        }
        
        return shader;
    }
    
    /**
     * Returns the underlying OpenGL program object handle for this fragment
     * shader. Most applications will not need to access this, since it is
     * handled automatically by the enable() and dispose() methods.
     * 
     * @return the OpenGL program object handle for this fragment shader
     */
    public int getProgramObject() {
        return id;
    }
    
    /**
     * Enables this shader program in the current GL context's state.
     * 
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public void enable() throws GLException {
        GL gl = GLU.getCurrentGL();
        gl.glUseProgramObjectARB(id);
    }
    
    /**
     * Disables this shader program in the current GL context's state.
     * 
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public void disable() throws GLException {
        GL gl = GLU.getCurrentGL();
        gl.glUseProgramObjectARB(0);
    }
    
    /**
     * Disposes the native resources used by this program object.
     *
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public void dispose() throws GLException {
        GL gl = GLU.getCurrentGL();
        gl.glDeleteObjectARB(id);
        id = 0;
    }

    /**
     * Sets the uniform variable of the given name with the provided
     * integer value.
     *
     * @param name the name of the uniform variable to be set
     * @param i0 the first uniform parameter
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public void setUniform(String name, int i0)
        throws GLException
    {
        GL gl = GLU.getCurrentGL();
        int loc = gl.glGetUniformLocationARB(id, name);
        gl.glUniform1iARB(loc, i0);
    }
    
    /**
     * Sets the uniform variable of the given name with the provided
     * integer values.
     *
     * @param name the name of the uniform variable to be set
     * @param i0 the first uniform parameter
     * @param i1 the second uniform parameter
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public void setUniform(String name, int i0, int i1)
        throws GLException
    {
        GL gl = GLU.getCurrentGL();
        int loc = gl.glGetUniformLocationARB(id, name);
        gl.glUniform2iARB(loc, i0, i1);
    }
    
    /**
     * Sets the uniform variable of the given name with the provided
     * integer values.
     *
     * @param name the name of the uniform variable to be set
     * @param i0 the first uniform parameter
     * @param i1 the second uniform parameter
     * @param i2 the third uniform parameter
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public void setUniform(String name, int i0, int i1, int i2)
        throws GLException
    {
        GL gl = GLU.getCurrentGL();
        int loc = gl.glGetUniformLocationARB(id, name);
        gl.glUniform3iARB(loc, i0, i1, i2);
    }
    
    /**
     * Sets the uniform variable of the given name with the provided
     * integer values.
     *
     * @param name the name of the uniform variable to be set
     * @param i0 the first uniform parameter
     * @param i1 the second uniform parameter
     * @param i2 the third uniform parameter
     * @param i3 the fourth uniform parameter
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public void setUniform(String name, int i0, int i1, int i2, int i3)
        throws GLException
    {
        GL gl = GLU.getCurrentGL();
        int loc = gl.glGetUniformLocationARB(id, name);
        gl.glUniform4iARB(loc, i0, i1, i2, i3);
    }
    
    /**
     * Sets the uniform variable of the given name with the provided
     * float value.
     *
     * @param name the name of the uniform variable to be set
     * @param f0 the first uniform parameter
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public void setUniform(String name, float f0)
        throws GLException
    {
        GL gl = GLU.getCurrentGL();
        int loc = gl.glGetUniformLocationARB(id, name);
        gl.glUniform1fARB(loc, f0);
    }
    
    /**
     * Sets the uniform variable of the given name with the provided
     * float values.
     *
     * @param name the name of the uniform variable to be set
     * @param f0 the first uniform parameter
     * @param f1 the second uniform parameter
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public void setUniform(String name, float f0, float f1)
        throws GLException
    {
        GL gl = GLU.getCurrentGL();
        int loc = gl.glGetUniformLocationARB(id, name);
        gl.glUniform2fARB(loc, f0, f1);
    }
    
    /**
     * Sets the uniform variable of the given name with the provided
     * float values.
     *
     * @param name the name of the uniform variable to be set
     * @param f0 the first uniform parameter
     * @param f1 the second uniform parameter
     * @param f2 the third uniform parameter
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public void setUniform(String name, float f0, float f1, float f2)
        throws GLException
    {
        GL gl = GLU.getCurrentGL();
        int loc = gl.glGetUniformLocationARB(id, name);
        gl.glUniform3fARB(loc, f0, f1, f2);
    }
    
    /**
     * Sets the uniform variable of the given name with the provided
     * float values.
     *
     * @param name the name of the uniform variable to be set
     * @param f0 the first uniform parameter
     * @param f1 the second uniform parameter
     * @param f2 the third uniform parameter
     * @param f3 the fourth uniform parameter
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public void setUniform(String name, float f0, float f1, float f2, float f3)
        throws GLException
    {
        GL gl = GLU.getCurrentGL();
        int loc = gl.glGetUniformLocationARB(id, name);
        gl.glUniform4fARB(loc, f0, f1, f2, f3);
    }
    
    /**
     * Sets the uniform array variable of the given name with the provided
     * int array values.
     *
     * @param name the name of the uniform variable to be set
     * @param count the number of int elements in the array
     * @param vals the array values to be set
     * @param off the offset into the vals array
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public void setUniformArray1i(String name, int count, int[] vals, int off)
        throws GLException
    {
        GL gl = GLU.getCurrentGL();
        int loc = gl.glGetUniformLocationARB(id, name);
        gl.glUniform1ivARB(loc, count, vals, off);
    }
    
    /**
     * Sets the uniform array variable of the given name with the provided
     * int array values.
     *
     * @param name the name of the uniform variable to be set
     * @param count the number of ivec2 elements in the array
     * @param vals the array values to be set
     * @param off the offset into the vals array
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public void setUniformArray2i(String name, int count, int[] vals, int off)
        throws GLException
    {
        GL gl = GLU.getCurrentGL();
        int loc = gl.glGetUniformLocationARB(id, name);
        gl.glUniform2ivARB(loc, count, vals, off);
    }
    
    /**
     * Sets the uniform array variable of the given name with the provided
     * int array values.
     *
     * @param name the name of the uniform variable to be set
     * @param count the number of ivec3 elements in the array
     * @param vals the array values to be set
     * @param off the offset into the vals array
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public void setUniformArray3i(String name, int count, int[] vals, int off)
        throws GLException
    {
        GL gl = GLU.getCurrentGL();
        int loc = gl.glGetUniformLocationARB(id, name);
        gl.glUniform3ivARB(loc, count, vals, off);
    }
    
    /**
     * Sets the uniform array variable of the given name with the provided
     * int array values.
     *
     * @param name the name of the uniform variable to be set
     * @param count the number of ivec4 elements in the array
     * @param vals the array values to be set
     * @param off the offset into the vals array
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public void setUniformArray4i(String name, int count, int[] vals, int off)
        throws GLException
    {
        GL gl = GLU.getCurrentGL();
        int loc = gl.glGetUniformLocationARB(id, name);
        gl.glUniform4ivARB(loc, count, vals, off);
    }

    /**
     * Sets the uniform array variable of the given name with the provided
     * float array values.
     *
     * @param name the name of the uniform variable to be set
     * @param count the number of float elements in the array
     * @param vals the array values to be set
     * @param off the offset into the vals array
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public void setUniformArray1f(String name,
                                  int count, float[] vals, int off)
        throws GLException
    {
        GL gl = GLU.getCurrentGL();
        int loc = gl.glGetUniformLocationARB(id, name);
        gl.glUniform1fvARB(loc, count, vals, off);
    }
    
    /**
     * Sets the uniform array variable of the given name with the provided
     * float array values.
     *
     * @param name the name of the uniform variable to be set
     * @param count the number of vec2 elements in the array
     * @param vals the array values to be set
     * @param off the offset into the vals array
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public void setUniformArray2f(String name,
                                  int count, float[] vals, int off)
        throws GLException
    {
        GL gl = GLU.getCurrentGL();
        int loc = gl.glGetUniformLocationARB(id, name);
        gl.glUniform2fvARB(loc, count, vals, off);
    }
    
    /**
     * Sets the uniform array variable of the given name with the provided
     * float array values.
     *
     * @param name the name of the uniform variable to be set
     * @param count the number of vec3 elements in the array
     * @param vals the array values to be set
     * @param off the offset into the vals array
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public void setUniformArray3f(String name,
                                  int count, float[] vals, int off)
        throws GLException
    {
        GL gl = GLU.getCurrentGL();
        int loc = gl.glGetUniformLocationARB(id, name);
        gl.glUniform3fvARB(loc, count, vals, off);
    }
    
    /**
     * Sets the uniform array variable of the given name with the provided
     * float array values.
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
        throws GLException
    {
        GL gl = GLU.getCurrentGL();
        int loc = gl.glGetUniformLocationARB(id, name);
        gl.glUniform4fvARB(loc, count, vals, off);
    }

    /**
     * Sets the uniform matrix (or matrix array) variable of the given name
     * with the provided matrix values.
     *
     * @param name the name of the uniform variable to be set
     * @param count the number of 2x2 matrices (mat2 elements) in the array
     * @param transpose if false, each matrix is assumed to be suppplied in
     * column major order; otherwise assumed to be supplied in row major order
     * @param vals the matrix values to be set
     * @param off the offset into the vals array
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public void setUniformMatrices2f(String name,
                                     int count, boolean transpose,
                                     float[] vals, int off)
        throws GLException
    {
        GL gl = GLU.getCurrentGL();
        int loc = gl.glGetUniformLocationARB(id, name);
        gl.glUniformMatrix2fvARB(loc, count, transpose, vals, off);
    }

    /**
     * Sets the uniform matrix (or matrix array) variable of the given name
     * with the provided matrix values.
     *
     * @param name the name of the uniform variable to be set
     * @param count the number of 3x3 matrices (mat3 elements) in the array
     * @param transpose if false, each matrix is assumed to be suppplied in
     * column major order; otherwise assumed to be supplied in row major order
     * @param vals the matrix values to be set
     * @param off the offset into the vals array
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public void setUniformMatrices3f(String name,
                                     int count, boolean transpose,
                                     float[] vals, int off)
        throws GLException
    {
        GL gl = GLU.getCurrentGL();
        int loc = gl.glGetUniformLocationARB(id, name);
        gl.glUniformMatrix3fvARB(loc, count, transpose, vals, off);
    }

    /**
     * Sets the uniform matrix (or matrix array) variable of the given name
     * with the provided matrix values.
     *
     * @param name the name of the uniform variable to be set
     * @param count the number of 4x4 matrices (mat4 elements) in the array
     * @param transpose if false, each matrix is assumed to be suppplied in
     * column major order; otherwise assumed to be supplied in row major order
     * @param vals the matrix values to be set
     * @param off the offset into the vals array
     * @throws GLException if no OpenGL context was current or if any
     * OpenGL-related errors occurred
     */
    public void setUniformMatrices4f(String name,
                                     int count, boolean transpose,
                                     float[] vals, int off)
        throws GLException
    {
        GL gl = GLU.getCurrentGL();
        int loc = gl.glGetUniformLocationARB(id, name);
        gl.glUniformMatrix4fvARB(loc, count, transpose, vals, off);
    }
}
