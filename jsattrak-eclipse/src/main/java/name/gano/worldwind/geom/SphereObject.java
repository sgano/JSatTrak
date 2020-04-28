// created using gov.nasa.worldwind.geom.Sphere as a template
// this is a mutable object as opposed to the imutable one
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

package name.gano.worldwind.geom;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Plane;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;
import java.awt.Color;

/**
 * Represents a sphere in three dimensional space.
 * <p/>
 * Instances of <code>Sphere</code> are immutable. </p>
 *
 * @author Tom Gaskins, Shawn E. Gano
 * @version $Id: SphereObject.java 2126 2007-09-21 21:00:42Z sgano $
 */
public final class SphereObject implements Extent, Renderable
{
    //public final static SphereObject UNIT_SPHERE = new SphereObject(Vec4.ZERO, 1);

    private Vec4 center;
    private  double radius;  // final  -- SEG
    
    private boolean sphereFilled = false;
    
    private Color sphereColor = Color.WHITE;
    
    private int numDivisions = 8; // number of horizontal and verticle divisions in the sphere

    boolean plotFixedAxis = false;
    
    // improve performance keep from creating a new one all the time
    javax.media.opengl.glu.GLUquadric quadric;
    
    /**
     * Creates a sphere that completely contains a set of points.
     *
     * @param points the <code>Vec4</code>s to be enclosed by the new Sphere
     * @return a <code>Sphere</code> encompassing the given array of <code>Vec4</code>s
     * @throws IllegalArgumentException if <code>points</code> is null or empty
     */
    public static SphereObject createBoundingSphere(Vec4 points[])
    {
        if (points == null)
        {
            String message = Logging.getMessage("nullValue.PointsArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (points.length < 1)
        {
            String message = Logging.getMessage("Geom.Sphere.NoPointsSpecified");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Creates the sphere around the axis aligned bounding box of the input points.
        Vec4[] extrema = composeExtrema(points);
        Vec4 center = new Vec4(
            (extrema[0].x + extrema[1].x) / 2.0,
            (extrema[0].y + extrema[1].y) / 2.0,
            (extrema[0].z + extrema[1].z) / 2.0);
        double radius = extrema[0].distanceTo3(extrema[1]) / 2.0;

        return new SphereObject(center, radius, false);
    }

    /**
     * Creates a new <code>Sphere</code> from a given center and radius. <code>radius</code> must be positive (that is,
     * greater than zero), and <code>center</code> may not be null.
     *
     * @param center the center of the new sphere
     * @param radius the radius of the new sphere
     * @param isfilled if the sphere is filled (else wireframe)
     * @throws IllegalArgumentException if <code>center</code> is null or if <code>radius</code> is non-positive
     */
    public SphereObject(Vec4 center, double radius, boolean isfilled)
    {
        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (radius <= 0)
        {
            String message = Logging.getMessage("Geom.Sphere.RadiusIsZeroOrNegative", radius);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.center = center;
        this.radius = radius;
        this.sphereFilled = isfilled;
    }
    

    /**
     * Calculate the extrema of a given array of <code>Vec4</code>s. The resulting array is always of length 2, with the
     * first element containing the minimum extremum, and the second containing the maximum. The minimum extremum is
     * composed by taking the smallest x, y and z values from all the <code>Vec4</code>s in the array. These values are
     * not necessarily taken from the same <code>Vec4</code>. The maximum extrema is composed in the same fashion.
     *
     * @param points any array of <code>Vec4</code>s
     * @return a array with length of 2, comprising the most extreme values in the given array
     * @throws IllegalArgumentException if <code>points</code> is null
     */
    public static Vec4[] composeExtrema(Vec4 points[])
    {
        if (points == null)
        {
            String message = Logging.getMessage("nullValue.PointsArrayIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (points.length == 0)
            return null;

        double xmin = points[0].x;
        double ymin = points[0].y;
        double zmin = points[0].z;
        double xmax = xmin;
        double ymax = ymin;
        double zmax = zmin;

        for (int i = 1; i < points.length; i++)
        {
            double x = points[i].x;
            if (x > xmax)
            {
                xmax = x;
            }
            else if (x < xmin)
            {
                xmin = x;
            }

            double y = points[i].y;
            if (y > ymax)
            {
                ymax = y;
            }
            else if (y < ymin)
            {
                ymin = y;
            }

            double z = points[i].z;
            if (z > zmax)
            {
                zmax = z;
            }
            else if (z < zmin)
            {
                zmin = z;
            }
        }

        return new Vec4[] {new Vec4(xmin, ymin, zmin), new Vec4(xmax, ymax, zmax)};
    }

    /**
     * Obtains the radius of this <code>Sphere</code>. The radus is the distance from the center to the surface. If an
     * object's distance to this sphere's center is less than or equal to the radius, then that object is at least
     * partially within this <code>Sphere</code>.
     *
     * @return the radius of this sphere
     */
    public final double getRadius()
    {
        return this.radius;
    }
    
    
    /**
     * Sets the radius of the sphere - doesn't automatically repaint.
     * @param radius radius of the sphere
     */
    public void setRadius(double radius)
    {
        this.radius = radius;
    }

    /**
     * Obtains the diameter of this <code>Sphere</code>. The diameter is twice the radius.
     *
     * @return the diameter of this <code>Sphere</code>
     */
    public final double getDiameter()
    {
        return 2 * this.radius;
    }

    /**
     * Obtains the center of this <code>Sphere</code>.
     *
     * @return the <code>Vec4</code> situated at the center of this <code>Sphere</code>
     */
    public final Vec4 getCenter()
    {
        return this.center;
    }

    /**
     * Obtains the intersections of this sphere with a line. The returned array may be either null or of zero length if
     * no intersections are discovered. It does not contain null elements and will have a size of 2 at most. Tangential
     * intersections are marked as such. <code>line</code> is considered to have infinite length in both directions.
     *
     * @param line the <code>Line</code> with which to intersect this <code>Sphere</code>
     * @return an array containing all the intersections of this <code>Sphere</code> and <code>line</code>
     * @throws IllegalArgumentException if <code>line</code> is null
     */
    public final Intersection[] intersect(Line line)
    {
        if (line == null)
        {
            String message = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        double a = line.getDirection().getLengthSquared3();
        double b = 2 * line.selfDot();
        double c = line.getOrigin().getLengthSquared3() - this.radius * this.radius;

        double discriminant = SphereObject.discriminant(a, b, c);
        if (discriminant < 0)
            return null;

        double discriminantRoot = Math.sqrt(discriminant);
        if (discriminant == 0)
        {
            Vec4 p = line.getPointAt((-b - discriminantRoot) / (2 * a));
            return new Intersection[] {new Intersection(p, true)};
        }
        else // (discriminant > 0)
        {
            Vec4 near = line.getPointAt((-b - discriminantRoot) / (2 * a));
            Vec4 far = line.getPointAt((-b + discriminantRoot) / (2 * a));
            return new Intersection[] {new Intersection(near, false), new Intersection(far, false)};
        }
    }

    /**
     * Calculates a discriminant. A discriminant is useful to determine the number of roots to a quadratic equation. If
     * the discriminant is less than zero, there are no roots. If it equals zero, there is one root. If it is greater
     * than zero, there are two roots.
     *
     * @param a the coefficient of the second order pronumeral
     * @param b the coefficient of the first order pronumeral
     * @param c the constant parameter in the quadratic equation
     * @return the discriminant "b squared minus 4ac"
     */
    private static double discriminant(double a, double b, double c)
    {
        return b * b - 4 * a * c;
    }

    /**
     * tests for intersetion with a <code>Frustum</code>. This operation is commutative, so
     * <code>someSphere.intersects(frustum)</code> and <code>frustum.intersects(someSphere)</code> are equivalent.
     *
     * @param frustum the <code>Frustum</code> with which to test for intersection
     * @return true if either <code>frustum</code> or this <code>Sphere</code> wholly or partially contain the other,
     *         false otherwise.
     * @throws IllegalArgumentException if <code>frustum</code> is null
     */
    public boolean intersects(Frustum frustum)
    {
       if (frustum == null)
        {
            String message = Logging.getMessage("nullValue.FrustumIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return frustum.intersects(this);
    }

    /**
     * Tests for intersection with a <code>Line</code>.
     *
     * @param line the <code>Line</code> with which to test for intersection
     * @return true if <code>line</code> intersects or makes a tangent with the surface of this <code>Sphere</code>
     * @throws IllegalArgumentException if <code>line</code> is null
     */
    public boolean intersects(Line line)
    {
        if (line == null)
        {
            String msg = Logging.getMessage("nullValue.LineIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        return line.distanceTo(this.center) <= this.radius;
    }

    /**
     * Tests for intersection with a <code>Plane</code>.
     *
     * @param plane the <code>Plane</code> with which to test for intersection
     * @return true if <code>plane</code> intersects or makes a tangent with the surface of this <code>Sphere</code>
     * @throws IllegalArgumentException if <code>plane</code> is null
     */
    public boolean intersects(Plane plane)
    {
        if (plane == null)
        {
            String msg = Logging.getMessage("nullValue.PlaneIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        double dq1 = plane.dot(this.center);
        return dq1 <= this.radius;
    }

    /**
     * Causes this <code>Sphere</code> to render itself using the <code>DrawContext</code> provided. <code>dc</code> may
     * not be null.
     *
     * @param dc the <code>DrawContext</code> to be used
     * @throws IllegalArgumentException if <code>dc</code> is null
     */
    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        javax.media.opengl.GL gl = dc.getGL();
         

        gl.glPushAttrib(javax.media.opengl.GL.GL_TEXTURE_BIT | javax.media.opengl.GL.GL_ENABLE_BIT
            | javax.media.opengl.GL.GL_CURRENT_BIT);
        gl.glDisable(javax.media.opengl.GL.GL_TEXTURE_2D);
        
        gl.glColor3d( sphereColor.getRed()/255.0 , sphereColor.getGreen()/255.0 , sphereColor.getBlue()/255.0 ); // COLOR 

        gl.glMatrixMode(javax.media.opengl.GL.GL_MODELVIEW);
        gl.glPushMatrix();
        gl.glTranslated(this.center.x, this.center.y, this.center.z);
        
        //javax.media.opengl.glu.GLUquadric quadric = dc.getGLU().gluNewQuadric();
        if(quadric == null)
        {
            quadric = dc.getGLU().gluNewQuadric();
        }
             
        // fill in:
        //dc.getGLU().gluQuadricDrawStyle(quadric, javax.media.opengl.glu.GLU.GLU_FILL);
        // or just lines:// javax.media.opengl.glu.GLU.GLU_LINE
        
        if(sphereFilled)
        {
            dc.getGLU().gluQuadricDrawStyle(quadric, javax.media.opengl.glu.GLU.GLU_FILL);
        }
        else // lines
        {
            dc.getGLU().gluQuadricDrawStyle(quadric, javax.media.opengl.glu.GLU.GLU_LINE);
        }
        
        // this line is the PERFORMANCE HIT
        // http://www.gamedev.net/community/forums/topic.asp?topic_id=479204
        // http://lists.apple.com/archives/Mac-opengl/2003/Nov/msg00053.html
        // OR MAYBE don't use spheres... use "dots" or label markers in WWJ?? (clickable?)
        dc.getGLU().gluSphere(quadric, this.radius, numDivisions, numDivisions);
        
        // Draw in axis
        if(plotFixedAxis)
        {
            gl.glLineWidth(3.0f);
            gl.glColor3d(1, 0, 0); // COLOR 
            gl.glBegin(gl.GL_LINES);
                gl.glVertex3d(radius * 3, 0, 0);
                gl.glVertex3d(0, 0, 0);
            gl.glEnd();
            // Draw in axis
            gl.glColor3d(0, 1, 0); // COLOR 
            gl.glBegin(gl.GL_LINES);
                gl.glVertex3d(0, radius * 3, 0);
                gl.glVertex3d(0, 0, 0);
            gl.glEnd();
            // Draw in axis
            gl.glColor3d(0, 0, 1); // COLOR 
            gl.glBegin(gl.GL_LINES);
                gl.glVertex3d(0, 0, radius * 3);
                gl.glVertex3d(0, 0, 0);
            gl.glEnd();
        }
        
        gl.glPopMatrix();
        dc.getGLU().gluDeleteQuadric(quadric);

        gl.glPopAttrib();
    }

    
    // SEG 
    public void bumpRadius()
    {
         this.radius =  this.radius *1.25;
    }
    
    
    @Override
    public String toString()
    {
        return "Sphere: center = " + this.center.toString() + " radius = " + Double.toString(this.radius);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        final SphereObject sphere = (SphereObject) o;

        if (Double.compare(sphere.radius, radius) != 0)
            return false;
        if (!center.equals(sphere.center))
            return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        result = center.hashCode();
        temp = radius != +0.0d ? Double.doubleToLongBits(radius) : 0L;
        result = 29 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

//    public final boolean intersects(Line line)
//    {
//        if (line == null)
//        {
//            String message = WorldWind.retrieveErrMsg("nullValue.LineIsNull");
//            WorldWind.logger().log(java.util.logging.Level.FINE, message);
//            throw new IllegalArgumentException(message);
//        }
//
//        double a = line.getDirection().getLengthSquared();
//        double b = 2 * line.selfDot();
//        double c = line.getOrigin().selfDot() - this.radius * this.radius;
//
//        double discriminant = Sphere.discriminant(a, b, c);
//        if (discriminant < 0)
//        {
//            return false;
//        }
//
//        return true;
//
//    }

    public boolean isSphereFilled()
    {
        return sphereFilled;
    }

    public void setSphereFilled(boolean sphereFilled)
    {
        this.sphereFilled = sphereFilled;
    }

    public Color getColor()
    {
        return sphereColor;
    }

    public void setColor(Color sphereColor)
    {
        this.sphereColor = sphereColor;
    }

    public int getNumDivisions()
    {
        return numDivisions;
    }

    public void setNumDivisions(int numDivisions)
    {
        this.numDivisions = numDivisions;
    }

    public void setCenter(Vec4 center)
    {
        this.center = center;
    }
    
    public void setCenter(double x, double y, double z)
    {
        this.center = new Vec4(x,y,z,0.0);
    }

	@Override
	public double getEffectiveRadius(Plane plane) {
		// TODO Auto-generated method stub
		return 0;
	}

	
}
