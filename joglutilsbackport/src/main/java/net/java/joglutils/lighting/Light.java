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
import java.util.*;
import java.nio.*;

/**
 *
 * This class encapsulates the settings for an OpenGL light.  It has two modes:
 * <br> 1. if an object implementing {@link GL} is attached, the OpenGL state is
 * updated as changes to this Light are made, or on enable() or apply() calls.
 * <br> 2.if no object is atttached, enable(GL) and apply(GL) applies settings, but the OpenGL state
 * is not altered when this Light's settings are changed.
 * <br> Note: GL_LIGHTING must be enabled in the OpenGL context by the user - this object will not do so.
 * @author Erik J. Tollerud
 */
public class Light {
    
    static HashMap<GL,boolean[]> assignedLights = new HashMap<GL,boolean[]>();
    static HashMap<GL,Boolean> shaderBuiltin = new HashMap<GL,Boolean>();
    static HashMap<GL,Integer> shaderProgNums = new HashMap<GL,Integer>();
    
    private GL attachedGL;
    private int lightNumber;
    
    //Light defaults are determined by constructor
    private float[] ambient;
    private float[] diffuse;
    private float[] specular;
    private float[] lightPosition;
    private float lightW;
    private float[] spotDirection;
    private float spotCutoff;
    private float spotExponent;
    private float constantAttenuation;
    private float linearAttenuation;
    private float quadraticAttenuation;
    
    private boolean phongShaded;
    private boolean phongDiffColorMat;
    private boolean phongAmbColorMat;
    
    
    /**
     * Creates a new instance of Light with the default settings.
     */
    public Light() {
        attachedGL = null;
        lightNumber = -1;
        
        float[] localAmb = {0.0f,0.0f,0.0f,1.0f};
        ambient = localAmb;
        float[] localDiff = {1.0f,1.0f,1.0f,1.0f};
        diffuse = localDiff;
        float[] localSpec = {1.0f,1.0f,1.0f,1.0f};
        specular = localSpec;
        float[] localPos = {0,0,1};
        lightPosition = localPos;
        lightW = 0;
        float[] localSpotDir = {0,0,-1};
        spotDirection = localSpotDir;
        spotCutoff= 180;
        spotExponent = 0;
        constantAttenuation = 1;
        linearAttenuation = 0;
        quadraticAttenuation = 0;
        phongShaded = false;
        phongDiffColorMat = false;
        phongAmbColorMat = false;
        
    }
    /**
     * Creates a new instance of Light with default settings attached to the specified {@link GL} Context
     * @param gl the OpenGL context to attach the light to
     * @throws net.java.joglutils.lighting.LightingException if the light number is invalid or is already in use by another Light. Light is detached from any OpenGL Context.
     */
    public Light(GL gl)  throws LightingException {
        this();
        
        this.attachedGL = gl;
        this.lightNumber = this.findAndAssignFreeLightNumber(gl);
        if (this.lightNumber == -1) {
            this.attachedGL = null;
            throw new LightingException("No more Lights available in specified OpenGL context");
        }
        
        retrieve();
        
    }
    /**
     * Creates a new instance of Light with default settings attached to the specified {@link GL} Context with an explicit light number
     * @param gl the OpenGL context to attach the light to
     * @param lightNumber the light number to be used (must be on [0,7])
     * @throws net.java.joglutils.lighting.LightingException if the light number is invalid or is already in use by another Light. Light is detached from any OpenGL Context.
     */
    public Light(GL gl, int lightNumber) throws LightingException {
        this();
        if (lightNumber < 0 || lightNumber > maxNumberOfLightsInGL(gl))
            throw new LightingException("Requested light not availible in specified OpenGL context");
        
        this.attachedGL = gl;
        this.lightNumber = this.findAndAssignFreeLightNumber(gl);
        if (this.lightNumber == -1) {
            this.attachedGL = null;
            throw new LightingException("No more Lights available in specified OpenGL context");
        }
        
        retrieve();
    }
    
    protected void finalize() {
        //Must free light number if this Light is destroyed
        if (this.attachedGL != null)
            this.unassignLightNumber(this.attachedGL,this.lightNumber);
    }
    
    /**
     * Determines the OpenGL light identifier associated with this Light.  If no GL context is attached to this
     * object, the result is undefined (although generally reflects the last light used)
     * @return the OpenGL identifier for this light
     */
    public int getGLLightIdentifier() {
        return this.numToID(this.lightNumber);
    }
    
    /**
     * Determines the number of the light associated with this Light.  If no GL context is attached to this
     * object, the result is undefined (although generally reflects the last light used)
     * @return a number on [0,7] representing which light is used
     */
    public int getLightNumber() {
        return this.lightNumber;
    }
    /**
     * Sets the number of the OpenGL light to use in subsequent method calls
     * @param lightNumber the light number to apply to this Light (must be on [0,7])
     * @throws net.java.joglutils.lighting.LightingException in use by another Light object on the attached OpenGL Context. Light is detached from any OpenGL Context.
     */
    public void setLightNumber(int lightNumber) throws LightingException {
        if(lightNumber < 0 || lightNumber > 7)
            throw new LightingException("Attempted to assign a light number that was not on [0,7]");
        
        if (this.attachedGL != null) {
            if(!isLightNumberFree(this.attachedGL,lightNumber))
                throw new LightingException("Attempted to assign a light number that was not free on the attached OpenGL Context");
            
            this.unassignLightNumber(this.attachedGL,this.lightNumber);
            this.assignLightNumber(this.attachedGL,lightNumber);
        }
        this.lightNumber = lightNumber;
    }
    
    /**
     * Sets the OpenGL light to use in subsequent method calls from a GL identifier
     * @param lightID the OpenGL light ID (must be of the GL_LIGHTn family)
     * @throws net.java.joglutils.lighting.LightingException if the input is not a valid light or the light is in use by another Light on the attached OpenGL Context. Light is detached from any OpenGL Context.
     */
    public void setLightID(int lightID)  throws LightingException  {
        setLightNumber(this.idToNum(lightID));
    }
    
    /**
     * Attached the specified OpenGL context to this object
     * @param gl the OpenGL context to attach this to
     * @throws net.java.joglutils.lighting.LightingException if the specified context has no free lights. Light is detached from any OpenGL Context.
     */
    public void setAttachedGL(GL gl) throws LightingException {
        this.unassignLightNumber(this.attachedGL,this.lightNumber);
        if(!this.hasFreeLights(gl)) {
            this.attachedGL = null;
            this.lightNumber = -1;
            throw new LightingException("attempted to attach Light object to OpenGL Context with no free lights");
        }
        
        this.lightNumber = findAndAssignFreeLightNumber(gl);
        this.attachedGL = gl;
        if (this.phongShaded) {
            if(!this.shaderBuiltin.containsKey(gl))
                this.initializePhongShader(gl);
        }
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
        this.unassignLightNumber(this.attachedGL,this.lightNumber);
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
     * Applies the settings on this Light to the attached {@link GL} Context.
     * Note that this will not apply Phong Shader settings.
     */
    public void apply() {
        apply(this.attachedGL,this.lightNumber);
        
    }
    /**
     * Reconfigure the settings for this Light from the state of the attached
     * {@link GL} Context. Note that this will not retrieve Phong Shader settings.
     */
    public void retrieve() {
        retrieve(this.attachedGL,this.lightNumber);
    }
    /**
     * Enables this Light on the attached {@link GL} Context. Note that GL_LIGHTING must be enabled in this context.
     */
    public void enable() {
        enable(this.attachedGL,this.lightNumber);
    }
    /**
     * Disables this Light on the attached {@link GL} Context.  Note that GL_LIGHTING must be enabled in this context.
     */
    public void disable() {
        disable(this.attachedGL,this.lightNumber);
    }
    /**
     * Applies the settings on this Light to the specified OpenGL context,
     * using the light number stored in the Light. Note that this will not apply Phong Shader settings.
     * @param gl the OpenGL context to use.
     * @throws net.java.joglutils.lighting.LightingException if the light number stored in this Light is invalid on the specified context.
     */
    public void apply(GL gl) throws LightingException {
        apply(gl,this.lightNumber);
    }
    /**
     * Reconfigures the settings on this Light from the state of the specified
     * OpenGL context, using the light number stored in the Light. Note that this will not retrieve Phong Shader settings.
     * @param gl the OpenGL context to use.
     * @throws net.java.joglutils.lighting.LightingException if the light number stored in this Light is invalid on the specified context.
     */
    public void retrieve(GL gl) throws LightingException {
        retrieve(gl,this.lightNumber);
        
    }
    /**
     * Enables the light number stored in this Light on the specified OpenGL Context.
     * @param gl the OpenGL context to use.
     * @throws net.java.joglutils.lighting.LightingException if the light number stored in this Light is invalid on the specified context.
     */
    public void enable(GL gl) throws LightingException {
        enable(gl,this.lightNumber);
    }
    /**
     * Disables the light number stored in this Light on the specified OpenGL Context.
     * @param gl the OpenGL context to use.
     * @throws net.java.joglutils.lighting.LightingException if the light number stored in this Light is invalid on the specified context.
     */
    public void disable(GL gl) throws LightingException {
        disable(gl,this.lightNumber);
    }
    /**
     * Applies the settings on this Light to the specified OpenGL context,
     * using the requested light number. Note that this will not apply Phong Shader settings.
     * @param gl the OpenGL context to use.
     * @param lightNumber the number of the light to use (should be on [0,7]).
     * @throws net.java.joglutils.lighting.LightingException if the requested light is not valid on the specified context.
     */
    public void apply(GL gl, int lightNumber) throws LightingException {
       if(!this.isLightNumberValid(gl, lightNumber))
            throw new LightingException("attempted to apply Light settings to invalid lightNumber for the requested OpenGL context");
        
        int lightID = numToID(lightNumber);
        
        //set color components
        gl.glLightfv(lightID,GL.GL_AMBIENT,ambient,0);
        gl.glLightfv(lightID,GL.GL_DIFFUSE,diffuse,0);
        gl.glLightfv(lightID,GL.GL_SPECULAR,specular,0);
        //OpenGL position information is contanied in Light lightPosition and light distance
        float[] position = new float[4];
        position[0] = lightPosition[0];
        position[1] = lightPosition[1];
        position[2] = lightPosition[2];
        position[3] = lightW;
        gl.glLightfv(lightID,GL.GL_POSITION,position,0);
        //set other parameters
        gl.glLightfv(lightID,GL.GL_SPOT_DIRECTION,spotDirection,0);
        gl.glLightf(lightID,GL.GL_SPOT_CUTOFF,spotCutoff);
        gl.glLightf(lightID,GL.GL_SPOT_EXPONENT,spotExponent);
        gl.glLightf(lightID,GL.GL_CONSTANT_ATTENUATION,constantAttenuation);
        gl.glLightf(lightID,GL.GL_LINEAR_ATTENUATION,linearAttenuation);
        gl.glLightf(lightID,GL.GL_QUADRATIC_ATTENUATION,quadraticAttenuation);
        
        this.lightNumber = lightNumber;
    }
    /**
     * Reconfigures the settings on this Light from the state of the specified
     * OpenGL context, using the requested light number. Note that this will not retrieve Phong Shader settings.
     * @param gl the OpenGL context to use.
     * @param lightNumber the number of the light to use (should be on [0,7]).
     * @throws net.java.joglutils.lighting.LightingException if the requested light is not valid on the specified context.
     */
    public void retrieve(GL gl, int lightNumber) throws LightingException{
        if(!this.isLightNumberValid(gl, lightNumber))
            throw new LightingException("attempted to retrieve Light settings to invalid lightNumber for the requested OpenGL context");
        
        int lightID = numToID(lightNumber);
        FloatBuffer buff = FloatBuffer.allocate(24);
        
        //get color components  - 4 each
        gl.glGetLightfv(lightID,GL.GL_AMBIENT,buff);
        buff.get(this.ambient);
        gl.glGetLightfv(lightID,GL.GL_DIFFUSE,buff);
        buff.get(this.diffuse);
        gl.glGetLightfv(lightID,GL.GL_SPECULAR,buff);
        buff.get(this.specular);
        //get light position - 4 (including distance)
        gl.glGetLightfv(lightID,GL.GL_POSITION,buff);
        buff.get(this.lightPosition);
        this.lightW = buff.get();
        //get spot direction- 3
        gl.glGetLightfv(lightID,GL.GL_SPOT_DIRECTION,buff);
        buff.get(this.spotDirection);
        //get individual floats - 1 each
        gl.glGetLightfv(lightID,GL.GL_SPOT_CUTOFF,buff);
        this.spotCutoff = buff.get();
        gl.glGetLightfv(lightID,GL.GL_SPOT_EXPONENT,buff);
        this.spotExponent = buff.get();
        gl.glGetLightfv(lightID,GL.GL_CONSTANT_ATTENUATION,buff);
        this.constantAttenuation = buff.get();
        gl.glGetLightfv(lightID,GL.GL_LINEAR_ATTENUATION,buff);
        this.linearAttenuation = buff.get();
        gl.glGetLightfv(lightID,GL.GL_QUADRATIC_ATTENUATION,buff);
        this.quadraticAttenuation = buff.get();
        
        this.lightNumber = lightNumber;
    }
    /**
     * Enables the requested light on the specified OpenGL Context.
     * @param gl the OpenGL context to use.
     * @param lightNumber the number of the light to use (should be on [0,7]).
     * @throws net.java.joglutils.lighting.LightingException if the requested light is not valid on the specified context.
     */
    public void enable(GL gl, int lightNumber) throws LightingException {
        if(!this.isLightNumberValid(gl, lightNumber))
            throw new LightingException("attempted to enable Light on with an invalid lightNumber for the requested OpenGL context");
        gl.glEnable(numToID(lightNumber));
        this.lightNumber = lightNumber;
        
        if(this.phongShaded) {
            //TODO:link in shader as just another one?
            if(!shaderBuiltin.containsKey(gl))
                initializePhongShader(gl);
            
            if(shaderBuiltin.get(gl)) {
                int progID = shaderProgNums.get(gl);
                gl.glUseProgram(progID+lightNumber);
                 
                int diffMatCol = gl.glGetUniformLocation(progID,"diffMatCol");
                int ambMatCol = gl.glGetUniformLocation(progID,"ambMatCol");
                
                gl.glUniform1i(diffMatCol,phongDiffColorMat ? 1 : 0);
                gl.glUniform1i(ambMatCol,phongAmbColorMat ? 1 : 0);
                      
                
                /*for light one program
                 int progID = shaderProgNums.get(gl);
                 
                int lightNumUniform = gl.glGetUniformLocation(progID,"lightNumber");
                gl.glUniform1i(lightNumUniform,lightNumber);
                 
                gl.glUseProgram(progID);
                 */
            } else {
                int progID = shaderProgNums.get(gl);
                 
                int diffMatCol = gl.glGetUniformLocationARB(progID,"diffMatCol");
                int ambMatCol = gl.glGetUniformLocationARB(progID,"ambMatCol");
                
                gl.glUniform1iARB(diffMatCol,phongDiffColorMat ? 1 : 0);
                gl.glUniform1iARB(ambMatCol,phongAmbColorMat ? 1 : 0);
                
                gl.glUseProgramObjectARB(progID+lightNumber);
                /*for light one program
                int progID = shaderProgNums.get(gl);
                 
                int lightNumUniform = gl.glGetUniformLocationARB(progID,"lightNumber");
                gl.glUniform1iARB(lightNumUniform,lightNumber);
                 
                gl.glUseProgramObjectARB(progID);
                 */
            }
        } else {
            Integer result = shaderProgNums.get(gl);
            if (result != null) {
                int lightID = result+lightNumber;
                java.nio.IntBuffer buffer = java.nio.IntBuffer.allocate(1);
                gl.glGetIntegerv(GL.GL_CURRENT_PROGRAM,buffer);
                int currProgram = buffer.get();
                
                if (currProgram == lightID)
                    gl.glUseProgramObjectARB(0);
            }
        }
        
    }
    /**
     * Disables the requested light on the specified OpenGL Context.
     * @param gl the OpenGL context to use.
     * @param lightNumber the number of the light to use (should be on [0,7]).
     * @throws net.java.joglutils.lighting.LightingException if the requested light is not valid on the specified context.
     */
    public void disable(GL gl, int lightNumber) throws LightingException {
        if(!this.isLightNumberValid(gl, lightNumber))
            throw new LightingException("attempted to disable Light on with an invalid lightNumber for the requested OpenGL context");
        gl.glDisable(numToID(lightNumber));
        this.lightNumber = lightNumber;
        
        if (this.phongShaded) {
            if(gl.isFunctionAvailable("glUseProgram"))
                gl.glUseProgram(0);
            else if(gl.isFunctionAvailable("glUseProgramObjectARB"))
                gl.glUseProgramObjectARB(0);
        }
    }
    
    //-----------------Setters and Getters-----------------
    //TODO: implement range checking on all of these
    
    /**
     * Sets the ambient (light "filling the room") color for this Light. Default is {0,0,0,1}.
     * @param ambient the Color to be copied into this Light. Later changes to the Color object will not be reflected in the Light.
     */
    public void setAmbient(Color ambient) {
        this.ambient = ambient.getRGBComponents(null);
        if (this.attachedGL != null)
            this.attachedGL.glLightfv(numToID(lightNumber),GL.GL_AMBIENT,this.ambient,0);
    }
    
    /**
     * Retrieves the ambient color for this Light.
     * @return a copy of the color used in this Light.
     */
    public Color getAmbient() {
        return new Color(ambient[0],ambient[1],ambient[2],ambient[3]);
    }
    
    /**
     * Sets the diffuse ("dull reflection") color for this Light. Default is {1,1,1,1}.
     * @param diffuse the Color to be copied into this Light. Later changes to the Color object will not be reflected in the Light.
     */
    public void setDiffuse(Color diffuse) {
        this.diffuse = diffuse.getRGBComponents(null);
        if (this.attachedGL != null)
            this.attachedGL.glLightfv(numToID(lightNumber),GL.GL_DIFFUSE,this.diffuse,0);
        
    }
    
    /**
     * Retrieves the diffuse color for this Light.
     * @return a copy of the color used in this Light.
     */
    public Color getDiffuse() {
        return new Color(diffuse[0],diffuse[1],diffuse[2],diffuse[3]);
    }
    
    /**
     * Sets the specular ("shiny reflection") color for this Light. Default is {1,1,1,1}.
     * @param specular the Color to be copied into this Light. Later changes to the Color object will not be reflected in the Light.
     */
    public void setSpecular(Color specular) {
        this.specular = specular.getRGBComponents(null);
        if (this.attachedGL != null)
            this.attachedGL.glLightfv(numToID(lightNumber),GL.GL_SPECULAR,this.specular,0);
    }
    
    /**
     * Retrieves the specular color for this Light.
     * @return a copy of the color used in this Light.
     */
    public Color getSpecular() {
        return new Color(specular[0],specular[1],specular[2],specular[3]);
    }
    
    /**
     * Specifies the direction vector for this Light.
     * (i.e. this the first 3 components specified when setting GL_POSITION with glLight)
     * Default is {0,0,1}.
     * @param lightPosition an array of three components in {x,y,z} format to be copied into the Light.
     */
    public void setLightPosition(float[] lightPosition) {
        if (this.attachedGL != null) {
            float[] position = new float[4];
            position[0] = lightPosition[0];
            position[1] = lightPosition[1];
            position[2] = lightPosition[2];
            position[3] = this.lightW;
            this.attachedGL.glLightfv(numToID(lightNumber),GL.GL_POSITION,position,0);
        }
        this.lightPosition = lightPosition.clone();
    }
    
    /**
     * Specifies the direction vector for this Light.
     * (i.e. this the first 3 components specified when setting GL_POSITION with glLight)
     * Default is {0,0,1}.
     * @param lightx the x component of the light position
     * @param lighty the y component of the light position
     * @param lightz the z component of the light position
     */
    public void setLightPosition(float lightx, float lighty, float lightz) {
        float[] pos = {lightx,lighty,lightz};
        setLightPosition(pos);
    }
    
    /**
     * Retrieves the direction vector for this light.
     * @return a copy of the position of this Light
     */
    public float[] getLightPosition() {
        return this.lightPosition.clone();
    }
    
    /**
     * Sets the spotlight direction for this Light. Default is {0,0,-1}.
     * @param spotDirection A 3-element array in {x,y,z} format specifying the spotlight direction vector.
     */
    public void setSpotDirection(float[] spotDirection) {
        if (this.attachedGL != null)
            this.attachedGL.glLightfv(numToID(lightNumber),GL.GL_SPOT_DIRECTION,spotDirection,0);
        this.spotDirection = spotDirection.clone();
    }
    
    /**
     * Sets the spotlight direction for this Light. Default is {0,0,-1}.
     * @param spotx the x coordinate of the spotlight direction vector.
     * @param spoty the y coordinate of the spotlight direction vector.
     * @param spotz the z coordinate of the spotlight direction vector.
     */
    public void setSpotDirection(float spotx, float spoty, float spotz) {
        float[] spotVec = {spotx, spoty, spotz};
        setSpotDirection(spotVec);
    }
    
    /**
     * Retrieves a vector indicating the spotlight direction used in this Light.
     * @return a copy of the spotlight direction of this Light
     */
    public float[] getSpotDirection() {
        return this.spotDirection.clone();
    }
    
    /**
     * Specifies the w-component to be used for this Light.  Default is 0.
     * (i.e. this the last component specified when setting GL_POSITION with glLight)
     * If 0, this light is treated as a direction source.
     * @param lightW the distance to be applied.
     */
    public void setLightW(float lightW) {
        if (this.attachedGL != null) {
            float[] position = new float[4];
            position[0] = this.lightPosition[0];
            position[1] = this.lightPosition[1];
            position[2] = this.lightPosition[2];
            position[3] = lightW;
            this.attachedGL.glLightfv(numToID(lightNumber),GL.GL_POSITION,position,0);
        }
        this.lightW = lightW;
    }
    
    /**
     * Retrieves the w-component of this Light.
     * @return the component.  If 0, this Light is a directional source.
     */
    public float getLightW() {
        return lightW;
    }
    /**
     * Sets this Light so that the light seems to be infinitely far away in terms of some of the calculations.
     */
    public void makeDirectional() {
        this.setLightW(0.0f);
    }
    
    /**
     * Sets the spotlight cutoff angle for this Light. Default is 180.
     * @param spotCutoff the angle to use.
     */
    public void setSpotCutoff(float spotCutoff) {
        if (this.attachedGL != null)
            this.attachedGL.glLightf(numToID(lightNumber),GL.GL_SPOT_CUTOFF,spotCutoff);
        this.spotCutoff = spotCutoff;
    }
    
    /**
     * Retrieves the spotlight cutoff angle for this Light.
     * @return the angle used in this Light.
     */
    public float getSpotCutoff() {
        return spotCutoff;
    }
    
    /**
     * Sets the spotlight exponent in this Light. Default is 0.
     * @param spotExponent the exponent to set on this Light.
     */
    public void setSpotExponent(float spotExponent) {
        if (this.attachedGL != null)
            this.attachedGL.glLightf(numToID(lightNumber),GL.GL_SPOT_EXPONENT,spotExponent);
        this.spotExponent = spotExponent;
    }
    
    /**
     * Retrieves the spotlight exponent in this Light.
     * @return the exponent used in this Light.
     */
    public float getSpotExponent() {
        return spotExponent;
    }
    
    /**
     * Sets the coefficient of the constant term in the attenuation equation for this Light.
     * Default is 1.
     * @param constantAttenuation the value to use for the constant coefficient.
     */
    public void setConstantAttenuation(float constantAttenuation) {
        if (this.attachedGL != null)
            this.attachedGL.glLightf(numToID(lightNumber),GL.GL_CONSTANT_ATTENUATION,constantAttenuation);
        this.constantAttenuation = constantAttenuation;
    }
    
    /**
     * Retrieves the coefficient of the constant term in the attenuation equation for this Light.
     * @return the constant coefficient.
     */
    public float getConstantAttenuation() {
        return constantAttenuation;
    }
    
    /**
     * Sets the coefficient of the linear term in the attenuation equation for this Light.
     * Default is 0.
     * @param linearAttenuation the value to use for the linear coefficient.
     */
    public void setLinearAttenuation(float linearAttenuation) {
        if (this.attachedGL != null)
            this.attachedGL.glLightf(numToID(lightNumber),GL.GL_LINEAR_ATTENUATION,linearAttenuation);
        this.linearAttenuation = linearAttenuation;
    }
    
    /**
     * Retrieves the coefficient of the linear term in the attenuation equation for this Light.
     * @return the linear coefficient.
     */
    public float getLinearAttenuation() {
        return linearAttenuation;
    }
    
    /**
     * Sets the coefficient of the quadratic term in the attenuation equation for this Light.
     * Default is 0.
     * @param quadraticAttenuation the value to use for the quadratic coefficient.
     */
    public void setQuadraticAttenuation(float quadraticAttenuation) {
        if (this.attachedGL != null)
            this.attachedGL.glLightf(numToID(lightNumber),GL.GL_QUADRATIC_ATTENUATION,quadraticAttenuation);
        this.quadraticAttenuation = quadraticAttenuation;
    }
    
    /**
     * Retrieves the coefficient of the quadratic term in the attenuation equation for this Light.
     * @return the quadratic coefficient.
     */
    public float getQuadraticAttenuation() {
        return quadraticAttenuation;
    }
    
    
    
    //------------------public statics---------------
    /**
     * Determines the maximum number of lights in the specified {@link GL} context
     * @param gl the OpenGL context to test
     * @return the maximum number of lights (highest possible in OpenGL is 8)
     */
    public static int maxNumberOfLightsInGL(GL gl) {
        java.nio.IntBuffer buff = java.nio.IntBuffer.allocate(1);
        gl.glGetIntegerv(gl.GL_MAX_LIGHTS,buff);
        return buff.get();
    }
    
    /**
     * Converts the specified OpenGL light ID number to a number
     * @return the appropriate light number (on [0,7])
     * @param lightID the OpenGL ID (should be one of GL_LIGHTn where n is on [0,7])
     * @throws net.java.joglutils.lighting.LightingException if the supplied lightID is not an OpenGL light ID
     */
    public static int idToNum(int lightID) throws LightingException {
        int retNum = -1;
        switch (lightID) {
            case GL.GL_LIGHT0:
                retNum = 0;
                break;
            case GL.GL_LIGHT1:
                retNum = 1;
                break;
            case GL.GL_LIGHT2:
                retNum = 2;
                break;
            case GL.GL_LIGHT3:
                retNum = 3;
                break;
            case GL.GL_LIGHT4:
                retNum = 4;
                break;
            case GL.GL_LIGHT5:
                retNum = 5;
                break;
            case GL.GL_LIGHT6:
                retNum = 6;
                break;
            case GL.GL_LIGHT7:
                retNum = 7;
                break;
            default:
                throw new LightingException("tried to determine light number of a non-ID int");
        }
        return retNum;
        
    }
    
    /**
     * Determines the OpenGL ID for the specified light number
     * @return the OpenGL ID for that light number (from the GL_LIGHTn family)
     * @param lightNum a number on [0,7] specifying the a light
     * @throws net.java.joglutils.lighting.LightingException if the supplied lightID is not an OpenGL light ID
     */
    public static int numToID(int lightNum) throws LightingException {
        int retID = -1;
        switch (lightNum) {
            case 0:
                retID = GL.GL_LIGHT0;
                break;
            case 1:
                retID = GL.GL_LIGHT1;
                break;
            case 2:
                retID = GL.GL_LIGHT2;
                break;
            case 3:
                retID = GL.GL_LIGHT3;
                break;
            case 4:
                retID = GL.GL_LIGHT4;
                break;
            case 5:
                retID = GL.GL_LIGHT5;
                break;
            case 6:
                retID = GL.GL_LIGHT6;
                break;
            case 7:
                retID = GL.GL_LIGHT7;
                break;
            default:
                throw new LightingException("tried to determine ID of a number not on [0,7]");
        }
        return retID;
    }
    
    /**
     * Determines if there is space in a specified OpenGL context for another Light to be attached
     * @param gl the openGL context to test
     * @return true if another Light object can be attached to this GL context
     */
    public static boolean hasFreeLights(GL gl) {
        boolean[] lights = assignedLights.get(gl);
        if (lights == null)
            return true;
        for (boolean b : lights) {
            if (!b)
                return true;
        }
        return false;
    }
    
    /**
     * Sets if phong per-fragment shading (as opposed to fixed-functionality per-vertex shading) is to be used with this Light.  Must come before the light is enabled.
     * This uses a vertex and fragment shader, so the fixed-functionality pipeline will not function normally.
     * @param usePhongShading if Phong should be used. If true and this Light is attached to a GL Context, initializes on this Light's context
     * @throws net.java.joglutils.lighting.LightingException if the current OpenGL context does not support GLSL shaders or the shader did not sucessfully compile and link
     */
    public void setPhongShaded(boolean usePhongShading) throws LightingException {
        if (this.attachedGL != null)
            initializePhongShader(this.attachedGL);
        
        this.phongShaded = usePhongShading;
    }
    
    /**
     * Tells if this Light is using Phong shading
     * @return true if this Light is in Phong shading mode
     */
    public boolean isPhongShaded() {
        return this.phongShaded;
    }
    
    /**
     * Sets whether or not the opengl color should be used for the phong color shader
     * @param ambient the ambient color material mode
     * @param diffuse the diffuse color material mode
     */
    public void setPhongColorMaterial(boolean ambient, boolean diffuse) {
        this.phongAmbColorMat = ambient;
        this.phongDiffColorMat = diffuse;
    }
    
    /**
     * Determines if color material is used for ambient in the Phong shader
     * @return true if color material is used for the phong ambient shader
     */
    public boolean isAmbientPhongColorMaterial() {
        return this.phongAmbColorMat;
    }
    
    /**
     * Determines if color material is used for diffuse in the Phong shader
     * @return true if color material is used for the phong diffuse shader
     */
    public boolean isDiffusePhongColorMaterial() {
        return this.phongDiffColorMat;
    }
    
    /**
     * Initializes Phong shading mode for this Light (instead of the fixed functionality Gouraud shading) on the current OpenGL context. Must be called before Phong shading can be activated.
     * @throws net.java.joglutils.lighting.LightingException if the OpenGL context does not support GLSL shaders or the shader did not sucessfully compile and link
     */
    public static void initializePhongShader() throws LightingException {
        initializePhongShader(javax.media.opengl.glu.GLU.getCurrentGL());
    }
    
    /**
     * Initializes Phong shading mode for this Light (instead of the fixed functionality Gouraud shading). Must be called on the context before Phong shading can be activated.
     * @param gl the openGL context on which to initialize the shader
     * @throws net.java.joglutils.lighting.LightingException if the OpenGL context does not support GLSL shaders or the shader did not sucessfully compile and link
     */
    public static void initializePhongShader(GL gl) throws LightingException {
        /* Old way that only works for light zero
        //program source
        final String[] fragSource = {
            "varying vec3 normalEye;\n",
            "varying vec3 positionEye;\n",
            "uniform int lightNum;\n",
            "\n",
            "void main()\n",
            "{\n",
            "\n",
            "vec3 N = normalize (normalEye);\n",
            "vec3 L = normalize (gl_LightSource[0].position.xyz);\n",
            "vec3 E = normalize (-positionEye);\n",
            "vec3 H = normalize (L + E);\n",
            "\n",
            "vec4 ambientColor = gl_FrontMaterial.ambient * gl_LightSource[0].ambient;\n",
            "vec4 diffuseColor = gl_FrontMaterial.diffuse * gl_LightSource[0].diffuse * max (dot (N, L), 0.0);\n",
            "vec4 specularColor = gl_FrontMaterial.specular * gl_LightSource[0]. specular * pow (max (dot (N, H), 0.0), gl_FrontMaterial.shininess);\n",
            "gl_FragColor = ambientColor + diffuseColor + specularColor;\n",
            "}\n"
        };
        final String[] vertSource={
            "varying vec3 normalEye;\n",
            "varying vec3 positionEye;\n",
            "\n",
            "void main()\n",
            "{\n",
            "normalEye = gl_NormalMatrix * gl_Normal;\n",
            "positionEye = (gl_ModelViewMatrix * gl_Vertex).xyz;\n",
            "gl_Position = ftransform ();\n",
            "}\n"
        };
         
        int[] vertLengths = new int[vertSource.length];
        for (int i=0; i < vertLengths.length;++i)
            vertLengths[i] = vertSource[i].length();
        int[] fragLengths = new int[fragSource.length];
        for (int i=0; i < fragLengths.length;++i)
            fragLengths[i] = fragSource[i].length();
         
         
        boolean builtin;
        int progID;
         
        if(gl.isFunctionAvailable("glCreateProgram"))
            builtin = true;
        else if (gl.isFunctionAvailable("glCreateProgramObjectARB"))
            builtin = false;
        else
            throw new LightingException("Could not generate Shader for Light - GLSL Shaders not supported by this GL Context");
         
        if(builtin) {
            progID = gl.glCreateProgram();
            int vertID = gl.glCreateShader(GL.GL_VERTEX_SHADER);
            int fragID = gl.glCreateShader(GL.GL_FRAGMENT_SHADER);
         
            gl.glShaderSource(vertID,vertSource.length,vertSource,vertLengths,0);
            gl.glShaderSource(fragID,fragSource.length,fragSource,fragLengths,0);
         
            gl.glCompileShader(vertID);
            gl.glCompileShader(fragID);
         
            gl.glAttachShader(progID,vertID);
            gl.glAttachShader(progID,fragID);
         
            gl.glLinkProgram(progID);
         
            int[] getProgArray = new int[1];
            gl.glGetProgramiv(progID,GL.GL_LINK_STATUS,getProgArray,0);
            if (getProgArray[0] == GL.GL_FALSE) {
                gl.glGetProgramiv(progID,GL.GL_INFO_LOG_LENGTH,getProgArray,0);
                int logLength = getProgArray[0];
                byte[] logArray = new byte[logLength];
                gl.glGetProgramInfoLog(progID,logLength,getProgArray,0,logArray,0);
                String log = new String(logArray);
                throw new LightingException("Shader Program could not link. Log:\n"+log);
            }
         
        } else {
            progID = gl.glCreateProgramObjectARB();
            int vertID = gl.glCreateShaderObjectARB(GL.GL_VERTEX_SHADER_ARB);
            int fragID = gl.glCreateShaderObjectARB(GL.GL_FRAGMENT_SHADER_ARB);
         
            gl.glShaderSourceARB(vertID,vertSource.length,vertSource,vertLengths,0);
            gl.glShaderSourceARB(fragID,fragSource.length,fragSource,fragLengths,0);
         
            gl.glCompileShaderARB(vertID);
            gl.glCompileShaderARB(fragID);
         
            gl.glAttachObjectARB(progID,vertID);
            gl.glAttachObjectARB(progID,fragID);
         
            gl.glLinkProgramARB(progID);
         
            int[] getProgArray = new int[1];
            gl.glGetObjectParameterivARB(progID,GL.GL_OBJECT_LINK_STATUS_ARB,getProgArray,0);
            if (getProgArray[0] == GL.GL_FALSE) {
                gl.glGetObjectParameterivARB(progID,GL.GL_OBJECT_INFO_LOG_LENGTH_ARB,getProgArray,0);
                int logLength = getProgArray[0];
                byte[] logArray = new byte[logLength];
                gl.glGetInfoLogARB(progID,logLength,getProgArray,0,logArray,0);
                String log = new String(logArray);
                throw new LightingException("Shader Program could not link. Log:\n"+log);
            }
        }
         */
        
        boolean builtin;
        int progID;
        
        if(gl.isFunctionAvailable("glCreateProgram"))
            builtin = true;
        else if (gl.isFunctionAvailable("glCreateProgramObjectARB"))
            builtin = false;
        else
            throw new LightingException("Could not generate Shader for Light - GLSL Shaders not supported by this GL Context");
        
        if(builtin) {
            progID = gl.glCreateProgram();
            //generate the other 7 shaders so that the numbers are in order
            for (int currID = 1+progID; currID < (progID+8); ++currID) {
                int result = gl.glCreateProgram();
                if (result != currID)
                    throw new LightingException("Couldn't generate shader programs in numerical order - can't use Phong Shading");
            }
            
            int vertID = gl.glCreateShader(GL.GL_VERTEX_SHADER);
            String[] vertSource = generatePhongVertexShaderSource();
            gl.glShaderSource(vertID,vertSource.length,vertSource,generateShaderLengths(vertSource),0);
            
            
            //generate program for each light and compile and link
            for (int currID = progID;currID < (8+progID);++currID) {
                
                int fragID = gl.glCreateShader(GL.GL_FRAGMENT_SHADER);
                String[] fragSource = generatePhongFragmentShaderSource(currID-progID);
                gl.glShaderSource(fragID,fragSource.length,fragSource,generateShaderLengths(fragSource),0);
                
                gl.glCompileShader(vertID);
                gl.glCompileShader(fragID);
                
                gl.glAttachShader(currID,vertID);
                gl.glAttachShader(currID,fragID);
                
                gl.glLinkProgram(currID);
                
                int[] getProgArray = new int[1];
                gl.glGetProgramiv(currID,GL.GL_LINK_STATUS,getProgArray,0);
                if (getProgArray[0] == GL.GL_FALSE) {
                    gl.glGetProgramiv(currID,GL.GL_INFO_LOG_LENGTH,getProgArray,0);
                    int logLength = getProgArray[0];
                    byte[] logArray = new byte[logLength];
                    gl.glGetProgramInfoLog(currID,logLength,getProgArray,0,logArray,0);
                    String log = new String(logArray);
                    throw new LightingException("Shader Program could not link. Log:\n"+log);
                }
            }
        } else {
            progID = gl.glCreateProgramObjectARB();
            //generate the other 7 shaders so that the numbers are in order
            for (int currID = 1+progID; currID < (progID+8); ++currID) {
                int result = gl.glCreateProgramObjectARB();
                if (result != currID)
                    throw new LightingException("Couldn't generate shader programs in numerical order - can't use Phong Shading");
            }
            
            int vertID = gl.glCreateShaderObjectARB(GL.GL_VERTEX_SHADER_ARB);
            String[] vertSource = generatePhongVertexShaderSource();
            gl.glShaderSourceARB(vertID,vertSource.length,vertSource,generateShaderLengths(vertSource),0);
            
            //TODO: do this in a way that doesn't depend on the numbers being in order
            //generate program for each light and compile and link
            for (int currID = progID;currID < (8+progID);++currID) {
                
                int fragID = gl.glCreateShaderObjectARB(GL.GL_FRAGMENT_SHADER_ARB);
                String[] fragSource = generatePhongFragmentShaderSource(currID-progID);
                gl.glShaderSourceARB(fragID,fragSource.length,fragSource,generateShaderLengths(fragSource),0);
                
                gl.glCompileShaderARB(vertID);
                gl.glCompileShaderARB(fragID);
                
                gl.glAttachObjectARB(currID,vertID);
                gl.glAttachObjectARB(currID,fragID);
                
                gl.glLinkProgramARB(currID);
                
                int[] getProgArray = new int[1];
                gl.glGetObjectParameterivARB(currID,GL.GL_OBJECT_LINK_STATUS_ARB,getProgArray,0);
                if (getProgArray[0] == GL.GL_FALSE) {
                    gl.glGetObjectParameterivARB(currID,GL.GL_OBJECT_INFO_LOG_LENGTH_ARB,getProgArray,0);
                    int logLength = getProgArray[0];
                    byte[] logArray = new byte[logLength];
                    gl.glGetInfoLogARB(currID,logLength,getProgArray,0,logArray,0);
                    String log = new String(logArray);
                    throw new LightingException("Shader Program could not link. Log:\n"+log);
                }
            }
        }
        
        shaderProgNums.put(gl,progID);
        shaderBuiltin.put(gl, builtin);
    }
    
    /**
     * Removes the Phong Shader program from the current GL Context
     *
     * @throws net.java.joglutils.lighting.LightingException if there is no Phong Shader
     **/
    
    public static void removePhongShader() throws LightingException  {
        removePhongShader(javax.media.opengl.glu.GLU.getCurrentGL());
    }
    
    /**
     * Removes the Phong Shader program from the specified GL Context
     *
     * @param gl the OpenGL context to remove the Phong shader program from
     * @throws net.java.joglutils.lighting.LightingException if there is no Phong Shader initialized on this context
     */
    public static void removePhongShader(GL gl) throws LightingException {
        if (shaderBuiltin.containsKey(gl)) {
            int progID = shaderProgNums.remove(gl);
            if(shaderBuiltin.remove(gl)) {
                java.nio.IntBuffer intBuff = java.nio.IntBuffer.allocate(3);
                for (int currID = progID; currID < (8+progID); ++currID) {
                    gl.glGetAttachedShaders(currID,2,intBuff,intBuff);
                    if (intBuff.get() != 2)
                        throw new LightingException("More than 2 shaders attached to the Light phong shader program.  Program ID Number:"+progID);
                    
                    gl.glDeleteShader(intBuff.get());
                    gl.glDeleteShader(intBuff.get());
                    
                    gl.glDeleteProgram(currID);
                    intBuff.clear();
                }
            } else {
                java.nio.IntBuffer intBuff = java.nio.IntBuffer.allocate(3);
                for (int currID = progID; currID < (8+progID); ++currID) {
                    gl.glGetAttachedObjectsARB(currID,2,intBuff,intBuff);
                    if (intBuff.get() != 2)
                        throw new LightingException("More than 2 shaders attached to the Light phong shader program.  Program ID Number:"+progID);
                    
                    gl.glDeleteObjectARB(intBuff.get());
                    gl.glDeleteObjectARB(intBuff.get());
                    
                    gl.glDeleteObjectARB(currID);
                    intBuff.clear();
                }
            }
        } else
            throw new LightingException("Tried to remove Light phong shader on a GL Context with no phong shader");
    }
    
    //----------- Private internal functions/methods below this point------------
    private static boolean isLightNumberValid(GL gl ,int lightNumber) {
        return (lightNumber > -1 && lightNumber < maxNumberOfLightsInGL(gl));
    }
    private static boolean isLightNumberFree(GL gl, int lightNumber) {
        boolean[] lights = assignedLights.get(gl);
        if (lights == null)
            return true;
        if (lightNumber >= lights.length || lightNumber < 0)
            return false;
        return (!lights[lightNumber]);
        
    }
    
    private static int findAndAssignFreeLightNumber(GL gl) {
        boolean[] lights = assignedLights.get(gl);
        if (lights == null) {
            lights = new boolean[maxNumberOfLightsInGL(gl)];
            lights[0] = true;
            assignedLights.put(gl,lights);
            return 0;
        }
        int i = 0;
        while(i < lights.length) {
            if(!lights[i])
                break;
            ++i;
        }
        if (i < lights.length) {
            lights[i] = true;
            assignedLights.put(gl,lights);
            return i;
        }
        return -1;
        
    }
    private static void assignLightNumber(GL gl, int lightNumber) {
        //No range checking
        boolean[] lights = assignedLights.get(gl);
        if (lights == null) {
            lights = new boolean[maxNumberOfLightsInGL(gl)];
            lights[lightNumber] = true;
        } else
            lights[lightNumber] = true;
        
        assignedLights.put(gl,lights);
    }
    
    private static void unassignLightNumber(GL gl, int lightNumber) {
        //No range checking
        boolean[] lights = assignedLights.get(gl);
        lights[lightNumber] = false;
        assignedLights.put(gl,lights);
    }
    
    private static String[] generatePhongFragmentShaderSource(int lightNumber) {
        String[] fragSource = {
            "varying vec3 normalEye;\n",
            "varying vec3 positionEye;\n",
            "uniform bool ambMatCol;\n",
            "uniform bool diffMatCol;\n",
            "\n",
            "void main()\n",
            "{\n",
            "\n",
            "vec3 N = normalize (normalEye);\n",
            "vec3 L = normalize (gl_LightSource["+lightNumber+"].position.xyz);\n",
            "vec3 E = normalize (-positionEye);\n",
            "vec3 H = normalize (L + E);\n",
            "\n",
            "vec4 ambientColor = (ambMatCol ? gl_Color : gl_FrontMaterial.ambient ) * (gl_LightSource["+lightNumber+"].ambient+gl_LightModel.ambient);\n",
            "vec4 diffuseColor = (diffMatCol ? gl_Color : gl_FrontMaterial.diffuse ) * gl_LightSource["+lightNumber+"].diffuse * max (dot (N, L), 0.0);\n",
            //"vec4 ambientColor = gl_FrontMaterial.ambient * (gl_LightSource["+lightNumber+"].ambient+gl_LightModel.ambient);\n",
            //"vec4 diffuseColor = gl_FrontMaterial.diffuse * gl_LightSource["+lightNumber+"].diffuse * max (dot (N, L), 0.0);\n",
            //"vec4 ambientColor = gl_Color * (gl_LightSource["+lightNumber+"].ambient+gl_LightModel.ambient);\n",
            //"vec4 diffuseColor = gl_Color * gl_LightSource["+lightNumber+"].diffuse * max (dot (N, L), 0.0);\n",
            "vec4 specularColor = gl_FrontMaterial.specular * gl_LightSource["+lightNumber+"]. specular * pow (max (dot (N, H), 0.0), gl_FrontMaterial.shininess);\n",
            "gl_FragColor = ambientColor + diffuseColor + specularColor;\n",
            "}\n"
        };
        return fragSource;
    }
    
    private static String[] generatePhongVertexShaderSource() {
        final String[] vertSource={
            "varying vec3 normalEye;\n",
            "varying vec3 positionEye;\n",
            "\n",
            "void main()\n",
            "{\n",
            "normalEye = gl_NormalMatrix * gl_Normal;\n",
            "positionEye = (gl_ModelViewMatrix * gl_Vertex).xyz;\n",
            "gl_Position = ftransform ();\n",
            "gl_FrontColor = gl_Color;\n",
            "}\n"
        };
        return vertSource;
    }
    
    private static int[] generateShaderLengths(String[] shaderSource) {
        int[] vertLengths = new int[shaderSource.length];
        for (int i=0; i < vertLengths.length;++i)
            vertLengths[i] = shaderSource[i].length();
        
        return vertLengths;
    }
    
}
