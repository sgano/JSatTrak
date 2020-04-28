package net.java.joglutils.model.geometry;

/**
 * Provides a instance of a boundary object that stores the maximum and 
 * minimum extents of a bounding box in 3D space.
 * <p>
 *
 * @version $Id$
 */
public class Bounds { 
    
    /** A static representation of a large number to be used for initialization of the boundaries */
    public static final float LARGE = Float.MAX_VALUE;    
    
    /** The minimum point of the bounding box */
    public Vec4 min = new Vec4( LARGE,  LARGE,  LARGE);
    
    /** The maximum point of the bounding box */
    public Vec4 max = new Vec4(-LARGE, -LARGE, -LARGE);

    /**
     * Default constructor that creates an instance of the Bounds object with
     * the minimum and maximum bounding points initialized to the largest 
     * extents expected.
     */    
    public Bounds() {
        // Nothing
    }

    /**
     * Constructor that initializes the bounding box to the specified bounding 
     * values passed in as individual x,y,z points
     * 
     * @param x1 The x coordinate of the minimum boundary point
     * @param y1 The y coordinate of the minimum boundary point
     * @param z1 The z coordinate of the minimum boundary point
     * @param x2 The x coordinate of the maximum boundary point
     * @param y2 The y coordinate of the maximum boundary point
     * @param z2 The z coordinate of the maximum boundary point     
     */    
    public Bounds( float x1, float y1, float z1, 
                   float x2, float y2, float z2 ) {
        min.x = x1;
        min.y = y1;
        min.z = z1;
        
        max.x = x2;
        max.y = y2;
        max.z = z2;        
    }

    /**
     * Constructor that initializes the bounding box to the specified bounding 
     * values passed in as vectors
     * 
     * @param v1 The minimum boundary point specified by a Vec3 object
     * @param v2 The maximum boundary point specified by a Vec3 object
     */    
    public Bounds( Vec4 v1, Vec4 v2 ) {
        min.x = v1.x;
        min.y = v1.y;
        min.z = v1.z;
        
        max.x = v2.x;
        max.y = v2.y;
        max.z = v2.z;
    }
    
    /**
     * Recalculates the boundary limits based on the coordinates of a point
     * passed into it.  If any of the coordinates of the passed in point are
     * beyond the current bounding box limits then the bounding box is expanded.
     *
     * @param x The x coordinate of a point used to adjust the boundary box
     * @param y The y coordinate of a point used to adjust the boundary box
     * @param z The z coordinate of a point used to adjust the boundary box
     */
    public void calc( float x, float y, float z ) {
        // Compare x component and expand the bounding box as needed
        if (x > max.x) {
            max.x = x;
        }
        if (x < min.x) {
            min.x = x;
        }
        
        // Compare y component and expand the bounding box as needed
        if (y > max.y) {
            max.y = y;
        }
        if (y < min.y) {
            min.y = y;
        }
        
        // Compare z component and expand the bounding box as needed
        if (z > max.z) {
            max.z = z;
        }
        if (z < min.z) {
            min.z = z;
        }
    }
    
    /**
     * Recalculates the boundary limits based on the coordinates of a point
     * passed into it.  If any of the coordinates of the passed in point are
     * beyond the current bounding box limits then the bounding box is expanded.
     *
     * @param v The Vec3 instance of a point used to adjust the boundary box
     */
    public void calc( Vec4 v ) {
        calc(v.x, v.y, v.z);
    }
    
    public float getRadius() {
        return 0.5f*(float)Math.sqrt(Math.pow(max.x-min.x, 2) + Math.pow(max.y-min.y, 2) + Math.pow(max.z-min.z, 2));
    }
    
    /**
     * Generates the String to represent a Bounds object in a nice format for
     * output purposes.
     *
     * @return The String representation of a Bounds object
     */
    public String toString() {
        return "mininum: (" + min.x + ", " + min.y + ", " + min.z + ") "
             + "maximum: (" + max.x + ", " + max.y + ", " + max.z + ")";
    }
}