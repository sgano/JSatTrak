/*
 * Copyright (c) 2006 Erik Tollerud (erik.tollerud@gmail.com) All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *   
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *    
 * - Redistribution in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *   
 * The names of Erik Tollerud, Sun Microsystems, Inc. or the names of
 * contributors may not be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *    
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. ERIK TOLLERUD,
 * SUN MICROSYSTEMS, INC. ("SUN"), AND SUN'S LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL ERIK
 * TOLLERUD, SUN, OR SUN'S LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT 
 * OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF ERIK
 * TOLLERUD OR SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *   
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 */

package net.java.joglutils.lighting;

import javax.media.opengl.*;
import java.awt.Color;
import java.nio.*;

/**
 * This class encapsulates OpenGL material settings in an object-oriented interface.
 * The class operates in two modes:
 * <br> 1. If a {@link GL} Context is attached, setter methods apply the settings to the 
 * OpenGL state as well as the object. the apply() and retrieve() method can be 
 * used for all of the settings
 * <br> 2. If no {@link GL} Context is attached, apply(GL) and retrieve(GL) set / get
 * the OpenGL state from this object.
 * <br> Note: GL_LIGHTING must be enabled in the OpenGL context by the user - this object will not do so.
 * @author Erik J. Tollerud
 */
public class Material {
    GL attachedGL;
    private int face;
    
    private float[] ambient;
    private float[] diffuse;
    private float[] specular;
    private float shininess;
    private float[] emissive;
    
    
    /** Creates a new instance of Material from the OpenGL default material settings */
    public Material() {
        attachedGL = null;
        face = GL.GL_FRONT_AND_BACK;
        float[] localAmb = {0.2f,0.2f,0.2f,1.0f};
        ambient = localAmb;
        float[] localDiff = {0.8f,0.8f,0.8f,1.0f};
        diffuse = localDiff;
        float[] localSpec = {0.0f,0.0f,0.0f,1.0f};
        specular = localSpec;
        float[] localEm = {0.0f,0.0f,0.0f,1.0f};
        emissive = localEm;
        
        shininess = 0;
    }
    /**
     * Creates a new instance of material, with the specified GL context attached.
     * @param gl the OpenGL context to attach
     * @param face the face to use for configuring the material
     */
    public Material(GL gl, int face) {
        this.attachedGL = gl;
        this.face = face;
        this.specular = new float[4];
        this.ambient = new float[4];
        this.diffuse = new float[4];
        this.emissive = new float[4];
        this.retrieve();
    }
    /**
     * Creates a new instance of material, with the specified GL context attached. Settings are applied to front and back.
     * @param gl the OpenGL context to attach
     */
    public Material(GL gl) {
        this(gl,GL.GL_FRONT_AND_BACK);
    }
    /**
     * Attached the specified OpenGL context to this object
     * @param gl the OpenGL context to attach this to
     */
    public void setAttachedGL(GL gl) {
        this.attachedGL = gl;
    }
    /**
     * Returns the OpenGL context attached to this Lighting object
     * @return the attached OpenGL context, or null if there is no attached context
     */
    public GL getAttachedGL() {
        return this.attachedGL;
    }
    /** 
     * Detaches the currently attached OpenGL context from this object
     */
    public void detachGL() {
        this.attachedGL = null;
    }
    /**
     * Determines if an OpenGL context is attached.
     * @return true if an OpenGL context is attached to this object.
     */
    public boolean isAttached() {
        if (this.attachedGL == null)
            return false;
        return true;
    }
    
    /**
     * Sets the OpenGL State in the supplied context based on the settings in this Material.
     * @param gl the OpenGL Context upon which to apply the settings from this Material.
     */
    public void apply(GL gl) {
        gl.glMaterialfv(face,GL.GL_SPECULAR,specular,0);
        gl.glMaterialfv(face,GL.GL_EMISSION,emissive,0);
        gl.glMaterialfv(face,GL.GL_AMBIENT,ambient,0);
        gl.glMaterialfv(face,GL.GL_DIFFUSE,diffuse,0);
        gl.glMaterialf(face,GL.GL_SHININESS,shininess);
    }
    /**
     * Sets the settings in this Material from the specified OpenGL context's state.
     * @param gl the OpenGL Context to use in setting this Material's settings.
     */
    public void retrieve(GL gl) {
        int retrievalFace = face;
        if (face == GL.GL_FRONT_AND_BACK)
            retrievalFace = GL.GL_FRONT;
        
        FloatBuffer buff = FloatBuffer.allocate(17);
        
        gl.glGetMaterialfv(retrievalFace,gl.GL_SPECULAR,buff);
        buff.get(this.specular);
        gl.glGetMaterialfv(retrievalFace,gl.GL_EMISSION,buff);
        buff.get(this.emissive);
        gl.glGetMaterialfv(retrievalFace,gl.GL_AMBIENT,buff);
        buff.get(this.ambient);
        gl.glGetMaterialfv(retrievalFace,gl.GL_DIFFUSE,buff);
        buff.get(this.diffuse);
        gl.glGetMaterialfv(retrievalFace,gl.GL_SHININESS,buff);
        this.shininess = buff.get();
    }
    /**
     * Sets the state on the attached OpenGL Context to match this Material.
     * @throws sddm.lighting.LightingException if no OpenGL Context is attached.
     */
    public void apply() throws LightingException {
        if (attachedGL == null)
            throw new LightingException("Tried to apply material settings with no attached GL Context");
        this.apply(this.attachedGL);
    }
    /**
     * Sets this Material object from the attached OpenGL state.
     * @throws sddm.lighting.LightingException if no OpenGL Context is attached.
     */
    public void retrieve() throws LightingException {
        if (attachedGL == null)
            throw new LightingException("Tried to retrieve material settings with no attached GL Context");
        this.retrieve(attachedGL);
    }
    /**
     * Specifies the face for subsequent apply method calls to apply and retrieve the material settings.  If
     * GL_FRONT_AND_BACK, will retrieve from GL_FRONT.
     * @param face the face to apply material settings upon.  Must be GL.GL_FRONT_AND_BACK, GL.GL_FRONT, or GL.GL_BACK
     * @throws sddm.lighting.LightingException if an invalid input is provided
     */
    public void setFace(int face) throws LightingException {
        if(face==GL.GL_FRONT_AND_BACK)
            this.face = GL.GL_FRONT_AND_BACK;
        else if(face == GL.GL_FRONT)
            this.face = GL.GL_FRONT;
        else if(face == GL.GL_BACK)
            this.face = GL.GL_BACK;
        else
            throw new LightingException("Attempted to set face to an invalid value");
    }
    
    /**
     * Determines what face the material settings are applied to or retrieved from.
     * @return the face used - can be GL.GL_FRONT_AND_BACK, GL.GL_FRONT, or GL.GL_BACK
     */
    public int getFace() {
        return face;
    }
    
    /**
     * Sets this object's specular color from an input Color. Default is {0,0,0,1}.
     * @param specular the color that the specular color is copied from - later changes to the Color object will not be reflected in this Material.
     */
    public void setSpecular(Color specular) {
        if (this.attachedGL != null)
            attachedGL.glMaterialfv(face,GL.GL_SPECULAR,specular.getRGBComponents(null),0);
        this.specular = specular.getRGBComponents(null);
    }
    
    /**
     * Retrieves the specular color from this Material.
     * @return a new Color with components copied from this Material.
     */
    public Color getSpecular() {
        return new Color(specular[0],specular[1],specular[2],specular[3]);
    }
    
    /**
     * Sets the shininess for this Material. Defaults to 0.
     * @param shininess the value to use for shininess
     */
    public void setShininess(float shininess) {
        if (this.attachedGL != null)
            attachedGL.glMaterialf(face,GL.GL_SHININESS,shininess);
        this.shininess = shininess;
    }
    
    /**
     * Retrieves the shininess from this Material.
     * @return the shininess value
     */
    public float getShininess() {
        return shininess;
    }
    
    /**
     * Sets this object's emissive color from an input Color. Default is {0,0,0,1}.
     * @param emissive the color that the specular color is copied from - later changes to the Color object will not be reflected in this Material.
     */
    public void setEmissive(Color emissive) {
        if (this.attachedGL != null)
            attachedGL.glMaterialfv(face,GL.GL_EMISSION,emissive.getRGBComponents(null),0);
        this.emissive = emissive.getRGBComponents(null);
    }
    
    /**
     * This retrieves the emissive color from this Material.
     * @return a new Color with components copied from this Material.
     */
    public Color getEmissive() {
        return new Color(emissive[0],emissive[1],emissive[2],emissive[3]);
    }
    
    /**
     * Sets this object's ambient color from an input Color. Defaults to {0.2,0.2,0.2,1.0}.
     * @param ambient the color that the specular color is copied from - later changes to the Color object will not be reflected in this Material.
     */
    public void setAmbient(Color ambient) {
         if (this.attachedGL != null)
            attachedGL.glMaterialfv(face,GL.GL_AMBIENT,ambient.getRGBComponents(null),0);
        this.ambient = ambient.getRGBComponents(null);
    }
    
    /**
     * This retrieves the ambient color from this Material.
     * @return a new Color with components copied from this Material.
     */
    public Color getAmbient() {
        return new Color(ambient[0],ambient[1],ambient[2],ambient[3]);
    }
    
    /**
     * Sets this object's diffuse color from an input Color. Defaults to {0.8,0.8,0.8,1.0}.
     * @param diffuse the color that the specular color is copied from - later changes to the Color object will not be reflected in this Material.
     */
    public void setDiffuse(Color diffuse) {
        if (this.attachedGL != null)
            attachedGL.glMaterialfv(face,GL.GL_DIFFUSE,diffuse.getRGBComponents(null),0);
        this.diffuse = diffuse.getRGBComponents(null);
    }
    
    /**
     * This retrieves the diffuse color from this Material.
     * @return a new Color with components copied from this Material.
     */
    public Color getDiffuse() {
        return new Color(diffuse[0],diffuse[1],diffuse[2],diffuse[3]);
    }
    /**
     * Sets the light model parameter GL_LIGHT_MODEL_AMBIENT for the attached GL context. This applies for all lighted fragments on this GL context.
     * @param ambient the ambient color to be applied.
     * @throws sddm.lighting.LightingException if no OpenGL Context is attached.
     */
    public void applyGlobalAmbient(Color ambient) throws LightingException {
        if (this.attachedGL == null)
            throw new LightingException("tried to set global ambient color on object with no attached GL Context");
        this.applyGlobalAmbient(this.attachedGL,ambient);
    }
    /**
     * Retrieves the GL_LIGHT_MODEL_AMBIENT color from the attached OpenGL context.
     * @throws sddm.lighting.LightingException if no OpenGL Context is attached.
     * @return the ambient color in the specified OpenGL Context.
     */
    public Color getGlobalAmbient() throws LightingException {
        if (this.attachedGL == null)
            throw new LightingException("tried to get global ambient color on object with with no attached GL Context");
        return this.getGlobalAmbient(this.attachedGL);
    } 
    
    /**
     * Sets the light model parameter GL_LIGHT_MODEL_AMBIENT for the specified GL context. This applies for all lighted fragments on this GL context.
     * @param gl the OpenGL context to apply this color to.
     * @param ambient the ambient color to be applied.
     */
    public static void applyGlobalAmbient(GL gl, Color ambient) {
        gl.glLightModelfv(gl.GL_LIGHT_MODEL_AMBIENT,ambient.getRGBComponents(null),0);
    }
    /**
     * Retrieves the GL_LIGHT_MODEL_AMBIENT color from a specified OpenGL context.
     * @param gl the OpenGL Context from which to get the color.
     * @return the ambient color in the specified OpenGL Context.
     */
    public static Color getGlobalAmbient(GL gl) {
        FloatBuffer buff = FloatBuffer.allocate(4);
        gl.glGetFloatv(gl.GL_LIGHT_MODEL_AMBIENT, buff);
        return new Color(buff.get(),buff.get(),buff.get(),buff.get());
    }
    
}
