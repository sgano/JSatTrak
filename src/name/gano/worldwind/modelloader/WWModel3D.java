/*
 * WWModel3D.java
 *
 * Created on February 14, 2008, 9:11 PM
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

package name.gano.worldwind.modelloader;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import net.java.joglutils.model.geometry.Model;

/**
 *
 * @author RodgersGB
 */
public class WWModel3D {
    private Position position;
    private Model model;
    
    // Adamson member additions for model size/orientation management
    
    public enum Axis {X, Y, Z};
    // For now we assume our models' are oriented along x or y axis
    private boolean followTerrain = false;
    private double length = 1.0;
    private double width = 2.0;  
    private double height = 2.0;
    
    private double headingOffset = 180.0;
    private double heading = 0.0;  // a.k.a. "yaw"
    private double pitch = 0.0;
    private double roll = 0.0;
    
    private double sizeScale = 1.0;
    
    /** Creates a new instance of WWModel3D */
    public WWModel3D(Model model, Position pos) {
        this.model = model;
        this.setPosition(pos);
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position pos) 
    {
    	this.position = pos;
    }
    
    public void setFollowTerrain(boolean state)
    	{followTerrain = state;}
    public boolean getFollowTerrain() 
    	{return followTerrain;}

    public Model getModel() {
        return model;
    }
    
    public void setPitch(double degrees)
    {
        pitch = degrees;
    }
    
    public double getPitch() 
    {
        return pitch;
    }
    
    // "heading" wr2 North (0.0)
    public void setHeading(double degrees)
    {
        heading = degrees;
    }
    
    public double getYaw() 
    {
        return ((heading + headingOffset) % 360.0);
    }
    
    public void setRoll(double degrees)
    {
        roll = degrees;
    }
    
    public double getRoll()
    {
        return roll;
    }
    
    
    public double getHeight()
    {
        return height;
    }

    public boolean isRealSize() {return (length > 0.0);}
    
    public void setSize(double width, double height, double length)
    {
        this.width = width;
        this.height = height;
        setLength(length);
    }
    
    void setLength(double lengthInMeters)
    {
        if (lengthInMeters < 0.0)
        {
            sizeScale = 1.0;
            return;
        }
        net.java.joglutils.model.geometry.Vec4 bMin = model.getBounds().min;
        net.java.joglutils.model.geometry.Vec4 bMax = model.getBounds().max;
        double pHeight = Math.abs(bMax.z - bMin.z);
        double pLength = Math.abs(bMax.x - bMin.x);
        double pWidth = Math.abs(bMax.y - bMin.y);
        if (pLength < pWidth)
        {
            double temp = pLength;
            pLength = pWidth;
            pWidth = temp;
        }
        sizeScale = lengthInMeters / pLength;  // meters per pixel for this model
        length = lengthInMeters;
        //width = pWidth * sizeScale;
        height = pHeight * sizeScale;    
    }  // end WWModel3D.setLength()
    
    public double computeSizeScale(DrawContext dc, Vec4 loc) 
    {
        /*if (length > 0.0)
        {
            // A real-world length (in meters) was set
            // for this model
            return sizeScale;  // meters per pixel for this model
        }
        else*/
        {
            // Here we use the max(width,height) to compute
            // a scaling factor to produce a constant size
            // (in the view) 3-D "icon" instead of an 
            // actual size model
            double d = loc.distanceTo3(dc.getView().getEyePoint());
            double pSize = dc.getView().computePixelSizeAtDistance(d);
            //if (pSize < 1.0) pSize = 1.0;
            double iconRadius = Math.sqrt(3*width*width) / 2.0;
            double s = (pSize * iconRadius) /  this.model.getBounds().getRadius();
            if ((length > 0.0) && (sizeScale > s)) s = sizeScale;
            return s;
        }
    }  // end WWModel3D.computeSizeScale()

}  // end class WWModel3D