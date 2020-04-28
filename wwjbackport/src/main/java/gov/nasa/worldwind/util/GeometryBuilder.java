/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.util;

import gov.nasa.worldwind.geom.Vec4;

import javax.media.opengl.GL;
import javax.media.opengl.glu.*;
import java.util.HashMap;

/**
 * @author dcollins
 * @version $Id: GeometryBuilder.java 13474 2010-06-18 23:49:58Z dcollins $
 */
public class GeometryBuilder
{
    public static final int OUTSIDE = 0;
    public static final int INSIDE = 1;

    public static final int COUNTER_CLOCKWISE = 0;
    public static final int CLOCKWISE = 1;

    public static final int TOP = 1;
    public static final int BOTTOM = 2;
    public static final int LEFT = 4;
    public static final int RIGHT = 8;

    private int orientation = OUTSIDE;
    private final GLU glu = new GLU();

    public GeometryBuilder()
    {
    }

    public int getOrientation()
    {
        return this.orientation;
    }

    public void setOrientation(int orientation)
    {
        this.orientation = orientation;
    }

    //**************************************************************//
    //********************  Sphere  ********************************//
    //**************************************************************//

    public IndexedTriangleArray tessellateSphere(float radius, int subdivisions)
    {
        if (radius < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "radius < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (subdivisions < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions < 0");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int[] indexArray = new int[ICOSAHEDRON_INDEX_COUNT];
        float[] vertexArray = new float[3 * ICOSAHEDRON_VERTEX_COUNT];
        System.arraycopy(icosahedronIndexArray, 0, indexArray, 0, ICOSAHEDRON_INDEX_COUNT);
        System.arraycopy(icosahedronVertexArray, 0, vertexArray, 0, 3 * ICOSAHEDRON_VERTEX_COUNT);

        // The static icosahedron tessellation is assumed to be viewed from the outside. If the orientation is set to
        // inside, then we must reverse the winding order for each triangle's indices.
        if (this.orientation == INSIDE)
        {
            for (int index = 0; index < ICOSAHEDRON_INDEX_COUNT; index += 3)
            {
                int tmp               = indexArray[index];
                indexArray[index]     = indexArray[index + 2];
                indexArray[index + 2] = tmp;
            }
        }

        // Start with a triangular tessellated icosahedron.
        IndexedTriangleArray ita = new IndexedTriangleArray(
            ICOSAHEDRON_INDEX_COUNT, indexArray, ICOSAHEDRON_VERTEX_COUNT, vertexArray);

        // Subdivide the icosahedron a specified number of times. The subdivison step computes midpoints between
        // adjacent vertices. These midpoints are not on the sphere, but must be moved onto the sphere. We normalize
        // each midpoint vertex to acheive this.
        for (int i = 0; i < subdivisions; i++)
        {
            this.subdivideIndexedTriangleArray(ita);

            vertexArray = ita.getVertices();
            for (int vertex = 0; vertex < ita.vertexCount; vertex++)
            {
                norm3AndSet(vertexArray, 3 * vertex);
            }
        }

        // Scale each vertex by the specified radius.
        vertexArray = ita.getVertices();
        for (int vertex = 0; vertex < ita.vertexCount; vertex++)
        {
            mul3AndSet(vertexArray, 3 * vertex, radius);            
        }

        return ita;
    }

    // Icosahedron tessellation taken from the
    // OpenGL Programming Guide, Chapter 2, Example 2-13: Drawing an Icosahedron.

    private static final int ICOSAHEDRON_INDEX_COUNT = 60;
    private static final int ICOSAHEDRON_VERTEX_COUNT = 12;
    private static final float X = 0.525731112119133606f;
    private static final float Z = 0.850650808352039932f;

    private static float[] icosahedronVertexArray =
    {
        -X,  0,  Z,
         X,  0,  Z,
        -X,  0, -Z,
         X,  0, -Z,
         0,  Z,  X,
         0,  Z, -X,
         0, -Z,  X,
         0, -Z, -X,
         Z,  X,  0,
        -Z,  X,  0,
         Z, -X,  0,
        -Z, -X,  0
    };

    private static int[] icosahedronIndexArray =
    {
        1,4,0,
        4,9,0,
        4,5,9,
        8,5,4,
        1,8,4,
        1,10,8,
        10,3,8,
        8,3,5,
        3,2,5,
        3,7,2,
        3,10,7,
        10,6,7,
        6,11,7,
        6,0,11,
        6,1,0,
        10,1,6,
        11,0,9,
        2,11,9,
        5,2,9,
        11,2,7
    };

    //**************************************************************//
    //********************  Cylinder            ********************//
    //**************************************************************//

    public int getCylinderVertexCount(int slices, int stacks)
    {
        return slices * (stacks + 1);
    }

    public int getCylinderIndexCount(int slices, int stacks)
    {
        return stacks * 2 * (slices + 1) + 2 * (stacks - 1);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public int getCylinderOutlineIndexCount(int slices, int stacks)
    {
        return slices * 4;
    }

    public int getCylinderDrawMode()
    {
        return GL.GL_TRIANGLE_STRIP;
    }

    public int getCylinderOutlineDrawMode()
    {
        return GL.GL_LINES;
    }

    public void makeCylinderVertices(float radius, float height, int slices, int stacks, float[] dest)
    {
        int numPoints = this.getCylinderVertexCount(slices, stacks);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y, z;
        float a;
        float dz, da;
        int i, j;
        int index;

        if (stacks != 0.0f)
            dz = height / (float) stacks;
        else
            dz = 0.0f;
        da = 2.0f * (float) Math.PI / (float) slices;

        for (i = 0; i < slices; i++)
        {
            a = i * da;
            x = (float) Math.sin(a);
            y = (float) Math.cos(a);
            z = 0.0f;
            for (j = 0; j <= stacks; j++)
            {
                index = j + i * (stacks + 1);
                index = 3 * index;
                dest[index]     = x * radius;
                dest[index + 1] = y * radius;
                dest[index + 2] = z;
                z += dz;
            }
        }
    }

    public void makeCylinderNormals(int slices, int stacks, float[] dest)
    {
        int numPoints = this.getCylinderVertexCount(slices, stacks);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y;
        float a;
        float da;
        float nsign;
        int i, j;
        int index;
        float[] norm;

        da = 2.0f * (float) Math.PI / (float) slices;
        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        norm = new float[3];

        for (i = 0; i < slices; i++)
        {
            a = i * da;
            x = (float) Math.sin(a);
            y = (float)Math.cos(a);
            norm[0] = x * nsign;
            norm[1] = y * nsign;
            norm[2] = 0.0f;
            this.norm3AndSet(norm, 0);

            for (j = 0; j <= stacks; j++)
            {
                index = j + i * (stacks + 1);
                index = 3 * index;
                System.arraycopy(norm, 0, dest, index, 3);
            }
        }
    }

    public void makeCylinderIndices(int slices, int stacks, int[] dest)
    {
        int numIndices = this.getCylinderIndexCount(slices, stacks);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numIndices)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int i, j;
        int vertex, index;

        index = 0;
        for (j = 0; j < stacks; j++)
        {
            if (j != 0)
            {
                if (this.orientation == INSIDE)
                    vertex = j + 1;
                else // (this.orientation == OUTSIDE)
                    vertex = j;
                dest[index++] = vertex;
                dest[index++] = vertex;
            }
            for (i = 0; i <= slices; i++)
            {
                if (i == slices)
                    vertex = j;
                else
                    vertex = j + i * (stacks + 1);
                if (this.orientation == INSIDE)
                {
                    dest[index++] = vertex + 1;
                    dest[index++] = vertex;
                }
                else // (this.orientation == OUTSIDE)
                {
                    dest[index++] = vertex;
                    dest[index++] = vertex + 1;
                }
            }
        }
    }

    public void makeCylinderOutlineIndices(int slices, int stacks, int[] dest)
    {
        int numIndices = this.getCylinderOutlineIndexCount(slices, stacks);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numIndices)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int i;
        int vertex, index;

        index = 0;
        // Bottom ring
        for (i = 0; i < slices; i++)
        {
            vertex = i * (stacks + 1);
            dest[index++] = vertex;
            dest[index++] = (i != slices - 1) ? vertex + stacks + 1 : 0;
        }
        // Top ring
        for (i = 0; i < slices; i++)
        {
            vertex = i * (stacks + 1) + stacks;
            dest[index++] = vertex;
            dest[index++] = (i != slices - 1) ? vertex + stacks + 1 : stacks;
        }
//        // Vertical edges
//        for (i = 0; i < slices; i++)
//        {
//            vertex = i * (stacks + 1);
//            dest[index++] = vertex;
//            dest[index++] = vertex + stacks;
//        }
    }

    //**************************************************************//
    //********************  Partial Cylinder    ********************//
    //**************************************************************//

    public int getPartialCylinderVertexCount(int slices, int stacks)
    {
        return (slices + 1) * (stacks + 1);
    }

    public int getPartialCylinderIndexCount(int slices, int stacks)
    {
        return stacks * 2 * (slices + 1) + 2 * (stacks - 1);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public int getPartialCylinderOutlineIndexCount(int slices, int stacks)
    {
        return slices * 4;
    }

    public int getPartialCylinderDrawMode()
    {
        return GL.GL_TRIANGLE_STRIP;
    }

    public int getPartialCylinderOutlineDrawMode()
    {
        return GL.GL_LINES;
    }

    public void makePartialCylinderVertices(float radius, float height, int slices, int stacks,
                                            float start, float sweep, float[] dest)
    {
        int numPoints = this.getPartialCylinderVertexCount(slices, stacks);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y, z;
        float a;
        float dz, da;
        int i, j;
        int index;

        if (stacks != 0.0f)
            dz = height / (float) stacks;
        else
            dz = 0.0f;
        da = sweep / (float) slices;

        for (i = 0; i <= slices; i++)
        {
            a = i * da + start;
            x = (float) Math.sin(a);
            y = (float) Math.cos(a);
            z = 0.0f;
            for (j = 0; j <= stacks; j++)
            {
                index = j + i * (stacks + 1);
                index = 3 * index;
                dest[index]     = x * radius;
                dest[index + 1] = y * radius;
                dest[index + 2] = z;
                z += dz;
            }
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void makePartialCylinderNormals(float radius, float height, int slices, int stacks,
                                           float start, float sweep, float[] dest)
    {
        int numPoints = this.getPartialCylinderVertexCount(slices, stacks);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y;
        float a;
        float da;
        float nsign;
        int i, j;
        int index;
        float[] norm;

        da = sweep/ (float) slices;
        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        norm = new float[3];

        for (i = 0; i <= slices; i++)
        {
            a = i * da + start;
            x = (float) Math.sin(a);
            y = (float) Math.cos(a);
            norm[0] = x * nsign;
            norm[1] = y * nsign;
            norm[2] = 0.0f;
            this.norm3AndSet(norm, 0);

            for (j = 0; j <= stacks; j++)
            {
                index = j + i * (stacks + 1);
                index = 3 * index;
                System.arraycopy(norm, 0, dest, index, 3);
            }
        }
    }

    public void makePartialCylinderIndices(int slices, int stacks, int[] dest)
    {
        int numIndices = this.getPartialCylinderIndexCount(slices, stacks);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numIndices)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int i, j;
        int vertex, index;

        index = 0;
        for (j = 0; j < stacks; j++)
        {
            if (j != 0)
            {
                if (this.orientation == INSIDE)
                {
                    vertex = j + slices * (stacks + 1);
                    dest[index++] = vertex - 1;
                    vertex = j + 1;
                    dest[index++] = vertex;
                }
                else //(this.orientation == OUTSIDE)
                {
                    vertex = j + slices * (stacks + 1);
                    dest[index++] = vertex;
                    vertex = j;
                    dest[index++] = vertex;
                }
            }
            for (i = 0; i <= slices; i++)
            {
                vertex = j + i * (stacks + 1);
                if (this.orientation == INSIDE)
                {
                    dest[index++] = vertex + 1;
                    dest[index++] = vertex;
                }
                else //(this.orientation == OUTSIDE)
                {
                    dest[index++] = vertex;
                    dest[index++] = vertex + 1;
                }
            }
        }
    }


    public void makePartialCylinderOutlineIndices(int slices, int stacks, int[] dest)
    {
        int numIndices = this.getPartialCylinderOutlineIndexCount(slices, stacks);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numIndices)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int i;
        int vertex, index;

        index = 0;
        // Bottom ring
        for (i = 0; i < slices; i++)
        {
            vertex = i * (stacks + 1);
            dest[index++] = vertex;
            dest[index++] = vertex + stacks + 1;
        }
        // Top ring
        for (i = 0; i < slices; i++)
        {
            vertex = i * (stacks + 1) + stacks;
            dest[index++] = vertex;
            dest[index++] = vertex + stacks + 1;
        }
    }


    //**************************************************************//
    //********************  Disk                ********************//
    //**************************************************************//

    public int getDiskVertexCount(int slices, int loops)
    {
        return slices * (loops + 1);
    }

    public int getDiskIndexCount(int slices, int loops)
    {
        return loops * 2 * (slices + 1) + 2 * (loops - 1);
    }

    public int getDiskDrawMode()
    {
        return GL.GL_TRIANGLE_STRIP;
    }

    public void makeDiskVertices(float innerRadius, float outerRadius, int slices, int loops, float[] dest)
    {
        int numPoints = this.getDiskVertexCount(slices, loops);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y;
        float a, r;
        float da, dr;
        int s, l;
        int index;

        da = 2.0f * (float) Math.PI / (float) slices;
        dr = (outerRadius - innerRadius) / (float) loops;

        for (s = 0; s < slices; s++)
        {
            a = s * da;
            x = (float) Math.sin(a);
            y = (float) Math.cos(a);
            for (l = 0; l <= loops; l++)
            {
                index = l + s * (loops + 1);
                index = 3 * index;
                r = innerRadius + l * dr;
                dest[index]     = r * x;
                dest[index + 1] = r * y;
                dest[index + 2] = 0.0f;
            }
        }
    }

    public void makeDiskNormals(int slices, int loops, float[] dest)
    {
        int numPoints = this.getDiskVertexCount(slices, loops);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int s, l;
        int index;
        float nsign;
        float[] normal;

        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        normal = new float[3];
        normal[0] = 0.0f;
        normal[1] = 0.0f;
        //noinspection PointlessArithmeticExpression
        normal[2] = 1.0f * nsign;

        for (s = 0; s < slices; s++)
        {
            for (l = 0; l <= loops; l++)
            {
                index = l + s * (loops + 1);
                index = 3 * index;
                System.arraycopy(normal, 0, dest, index, 3);
            }
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void makeDiskVertexNormals(float innerRadius, float outerRadius, int slices, int loops,
                                      float[] srcVerts, float[] dest)
    {
        int numPoints = this.getDiskVertexCount(slices, loops);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (srcVerts == null)
        {
            String message = "nullValue.SourceVertexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int s, l;
        int index;
        float nsign;
        float[] norm, zero, tmp;

        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        norm = new float[3];
        zero = new float[3];
        tmp = new float[3];

        for (l = 0; l <= loops; l++)
        {
            // Normal vectors for first and last loops require a special case.
            if (l == 0 || l == loops)
            {
                // Closed disk: all slices share a common center point.
                if (l == 0 && innerRadius == 0.0f)
                {
                    // Compute common center point normal.
                    int nextSlice;
                    int adjacentLoop;
                    System.arraycopy(zero, 0, norm, 0, 3);
                    for (s = 0; s < slices; s++)
                    {
                        index = l + s * (loops + 1);
                        nextSlice = l + (s + 1) * (loops + 1);
                        if (s == slices - 1)
                            nextSlice = l;
                        adjacentLoop = index + 1;
                        this.facenorm(srcVerts, index, nextSlice + 1, adjacentLoop, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                    }
                    this.mul3AndSet(norm, 0, nsign);
                    this.norm3AndSet(norm, 0);
                    // Copy common normal to the first point of each slice.
                    for (s = 0; s < slices; s++)
                    {
                        index = l + s * (loops + 1);
                        System.arraycopy(norm, 0, dest, 3 * index, 3);
                    }
                }
                // Open disk: each slice has a unique starting point.
                else
                {
                    for (s = 0; s < slices; s++)
                    {
                        int prevSlice, nextSlice;
                        int adjacentLoop;
                        index = l + s * (loops + 1);
                        prevSlice = l + (s - 1) * (loops + 1);
                        nextSlice = l + (s + 1) * (loops + 1);

                        if (s == 0)
                            prevSlice = l + (slices - 1) * (loops + 1);
                        else if (s == slices - 1)
                            nextSlice = l;

                        if (l == 0)
                            adjacentLoop = index + 1;
                        else
                            adjacentLoop = index - 1;

                        System.arraycopy(zero, 0, norm, 0, 3);

                        // Add clockwise adjacent face.
                        if (l == 0)
                            this.facenorm(srcVerts, index, nextSlice, adjacentLoop, tmp);
                        else
                            this.facenorm(srcVerts, index, adjacentLoop, nextSlice, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        // Add counter-clockwise adjacent face.
                        if (l == 0)
                            this.facenorm(srcVerts, index, adjacentLoop, prevSlice, tmp);
                        else
                            this.facenorm(srcVerts, index, prevSlice, adjacentLoop, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);

                        // Normalize and place in output.
                        this.mul3AndSet(norm, 0, nsign);
                        this.norm3AndSet(norm, 0);
                        System.arraycopy(norm, 0, dest, 3 * index, 3);
                    }
                }
            }
            // Normal vectors for internal loops.
            else
            {
                for (s = 0; s < slices; s++)
                {
                    int prevSlice, nextSlice;
                    int prevLoop, nextLoop;
                    index = l + s * (loops + 1);
                    prevSlice = l + (s - 1) * (loops + 1);
                    nextSlice = l + (s + 1) * (loops + 1);

                    if (s == 0)
                        prevSlice = l + (slices - 1) * (loops + 1);
                    else if (s == slices - 1)
                        nextSlice = l;

                    prevLoop = index - 1;
                    nextLoop = index + 1;

                    System.arraycopy(zero, 0, norm, 0, 3);

                    // Add lower-left adjacent face.
                    this.facenorm(srcVerts, index, prevSlice, prevSlice - 1, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    this.facenorm(srcVerts, index, prevSlice - 1, prevLoop, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    // Add lower-right adjacent face.
                    this.facenorm(srcVerts, index, prevLoop, nextSlice - 1, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    this.facenorm(srcVerts, index, nextSlice - 1, nextSlice, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    // Add upper-right adjacent face.
                    this.facenorm(srcVerts, index, nextSlice, nextSlice + 1, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    this.facenorm(srcVerts, index, nextSlice + 1, nextLoop, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    // Add upper-left adjacent face.
                    this.facenorm(srcVerts, index, nextLoop, prevSlice + 1, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    this.facenorm(srcVerts, index, prevSlice + 1, prevSlice, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);

                    // Normalize and place in output.
                    this.mul3AndSet(norm, 0, nsign);
                    this.norm3AndSet(norm, 0);
                    System.arraycopy(norm, 0, dest, 3 * index, 3);
                }
            }
        }
    }

    public void makeDiskIndices(int slices, int loops, int[] dest)
    {
        int numIndices = this.getDiskIndexCount(slices, loops);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numIndices)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int s, l;
        int vertex, index;

        index = 0;
        for (l = 0; l < loops; l++)
        {
            if (l != 0)
            {
                if (this.orientation == INSIDE)
                {
                    vertex = l;
                    dest[index++] = vertex;
                    dest[index++] = vertex;
                }
                else // (this.orientation == OUTSIDE)
                {
                    vertex = l - 1;
                    dest[index++] = vertex;
                    vertex = l + 1;
                    dest[index++] = vertex;
                }
            }
            for (s = 0; s <= slices; s++)
            {
                if (s == slices)
                    vertex = l;
                else
                    vertex = l + s * (loops + 1);
                if (this.orientation == INSIDE)
                {
                    dest[index++] = vertex;
                    dest[index++] = vertex + 1;
                }
                else // (this.orientation == OUTSIDE)
                {
                    dest[index++] = vertex + 1;
                    dest[index++] = vertex;
                }
            }
        }
    }

    //**************************************************************//
    //********************  Partial Disk        ********************//
    //**************************************************************//

    public int getPartialDiskVertexCount(int slices, int loops)
    {
        return (slices + 1) * (loops + 1);
    }

    public int getPartialDiskIndexCount(int slices, int loops)
    {
        return loops * 2 * (slices + 1) + 2 * (loops - 1);
    }

    public int getPartialDiskDrawMode()
    {
        return GL.GL_TRIANGLE_STRIP;
    }

    public void makePartialDiskVertices(float innerRadius, float outerRadius, int slices, int loops,
                                        float start, float sweep, float[] dest)
    {
        int numPoints = this.getPartialDiskVertexCount(slices, loops);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y;
        float a, r;
        float da, dr;
        int s, l;
        int index;

        da = sweep / (float) slices;
        dr = (outerRadius - innerRadius) / (float) loops;

        for (s = 0; s <= slices; s++)
        {
            a = s * da + start;
            x = (float) Math.sin(a);
            y = (float) Math.cos(a);
            for (l = 0; l <= loops; l++)
            {
                index = l + s * (loops + 1);
                index = 3 * index;
                r = innerRadius + l * dr;
                dest[index]     = r * x;
                dest[index + 1] = r * y;
                dest[index + 2] = 0.0f;
            }
        }
    }

    public void makePartialDiskNormals(int slices, int loops, float[] dest)
    {
        int numPoints = this.getPartialDiskVertexCount(slices, loops);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int s, l;
        int index;
        float nsign;
        float[] normal;

        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        normal = new float[3];
        normal[0] = 0.0f;
        normal[1] = 0.0f;
        //noinspection PointlessArithmeticExpression
        normal[2] = 1.0f * nsign;

        for (s = 0; s <= slices; s++)
        {
            for (l = 0; l <= loops; l++)
            {
                index = l + s * (loops + 1);
                index = 3 * index;
                System.arraycopy(normal, 0, dest, index, 3);
            }
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void makePartialDiskVertexNormals(float innerRadius, float outerRadius, int slices, int loops,
                                             float start, float sweep, float[] srcVerts, float[] dest)
    {
        int numPoints = this.getPartialDiskVertexCount(slices, loops);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (srcVerts == null)
        {
            String message = "nullValue.SourceVertexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int s, l;
        int index;
        float nsign;
        float[] norm, zero, tmp;

        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        norm = new float[3];
        zero = new float[3];
        tmp = new float[3];

        for (l = 0; l <= loops; l++)
        {
            // Normal vectors for first and last loops require a special case.
            if (l == 0 || l == loops)
            {
                // Closed disk: all slices share a common center point.
                if (l == 0 && innerRadius == 0.0f)
                {
                    // Compute common center point normal.
                    int nextSlice;
                    int adjacentLoop;
                    System.arraycopy(zero, 0, norm, 0, 3);
                    for (s = 0; s < slices; s++)
                    {
                        index = l + s * (loops + 1);
                        nextSlice = l + (s + 1) * (loops + 1);
                        adjacentLoop = index + 1;
                        this.facenorm(srcVerts, index, nextSlice + 1, adjacentLoop, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                    }
                    this.mul3AndSet(norm, 0, nsign);
                    this.norm3AndSet(norm, 0);
                    // Copy common normal to the first point of each slice.
                    for (s = 0; s <= slices; s++)
                    {
                        index = l + s * (loops + 1);
                        System.arraycopy(norm, 0, dest, 3 * index, 3);
                    }
                }
                // Open disk: each slice has a unique starting point.
                else
                {
                    for (s = 0; s <= slices; s++)
                    {
                        int prevSlice, nextSlice;
                        int adjacentLoop;
                        index = l + s * (loops + 1);

                        if (l == 0)
                            adjacentLoop = index + 1;
                        else
                            adjacentLoop = index - 1;

                        System.arraycopy(zero, 0, norm, 0, 3);

                        if (s > 0)
                        {
                            prevSlice = l + (s - 1) * (loops + 1);
                            // Add counter-clockwise adjacent face.
                            if (l == 0)
                                this.facenorm(srcVerts, index, adjacentLoop, prevSlice, tmp);
                            else
                                this.facenorm(srcVerts, index, prevSlice, adjacentLoop, tmp);
                            this.add3AndSet(norm, 0, tmp, 0);
                        }
                        if (s < slices)
                        {
                            nextSlice = l + (s + 1) * (loops + 1);
                            // Add clockwise adjacent face.
                            if (l == 0)
                                this.facenorm(srcVerts, index, nextSlice, adjacentLoop, tmp);
                            else
                                this.facenorm(srcVerts, index, adjacentLoop, nextSlice, tmp);
                            this.add3AndSet(norm, 0, tmp, 0);
                        }

                        // Normalize and place in output.
                        this.mul3AndSet(norm, 0, nsign);
                        this.norm3AndSet(norm, 0);
                        System.arraycopy(norm, 0, dest, 3 * index, 3);
                    }
                }
            }
            // Normal vectors for internal loops.
            else
            {
                for (s = 0; s <= slices; s++)
                {
                    int prevSlice, nextSlice;
                    int prevLoop, nextLoop;
                    index = l + s * (loops + 1);
                    prevLoop = index - 1;
                    nextLoop = index + 1;

                    System.arraycopy(zero, 0, norm, 0, 3);
                    if (s > 0)
                    {
                        prevSlice = l + (s - 1) * (loops + 1);
                        // Add lower-left adjacent face.
                        this.facenorm(srcVerts, index, prevSlice, prevSlice - 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        this.facenorm(srcVerts, index, prevSlice - 1, prevLoop, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        // Add upper-left adjacent face.
                        this.facenorm(srcVerts, index, nextLoop, prevSlice + 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        this.facenorm(srcVerts, index, prevSlice + 1, prevSlice, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                    }
                    if (s < slices)
                    {
                        nextSlice = l + (s + 1) * (loops + 1);
                        // Add lower-right adjacent face.
                        this.facenorm(srcVerts, index, prevLoop, nextSlice - 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        this.facenorm(srcVerts, index, nextSlice - 1, nextSlice, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        // Add upper-right adjacent face.
                        this.facenorm(srcVerts, index, nextSlice, nextSlice + 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        this.facenorm(srcVerts, index, nextSlice + 1, nextLoop, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                    }

                    // Normalize and place in output.
                    this.mul3AndSet(norm, 0, nsign);
                    this.norm3AndSet(norm, 0);
                    System.arraycopy(norm, 0, dest, 3 * index, 3);
                }
            }
        }
    }

    public void makePartialDiskIndices(int slices, int loops, int[] dest)
    {
        int numIndices = this.getPartialDiskIndexCount(slices, loops);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "slices=" + slices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numIndices)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int s, l;
        int vertex, index;

        index = 0;
        for (l = 0; l < loops; l++)
        {
            if (l != 0)
            {
                if (this.orientation == INSIDE)
                {
                    vertex = l + slices * (loops + 1);
                    dest[index++] = vertex;
                    vertex = l;
                    dest[index++] = vertex;
                }
                else // (this.orientation == OUTSIDE)
                {
                    vertex = (l - 1) + slices * (loops + 1);
                    dest[index++] = vertex;
                    vertex = l;
                    dest[index++] = vertex + 1;
                }
            }
            for (s = 0; s <= slices; s++)
            {
                vertex = l + s * (loops + 1);
                if (this.orientation == INSIDE)
                {
                    dest[index++] = vertex;
                    dest[index++] = vertex + 1;
                }
                else // (this.orientation == OUTSIDE)
                {
                    dest[index++] = vertex + 1;
                    dest[index++] = vertex;
                }
            }
        }
    }

    //**************************************************************//
    //********************  Radial Wall         ********************//
    //**************************************************************//

    public int getRadialWallVertexCount(int pillars, int stacks)
    {
        return (pillars + 1) * (stacks + 1);
    }

    public int getRadialWallIndexCount(int pillars, int stacks)
    {
        return stacks * 2 * (pillars + 1) + 2 * (stacks - 1);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public int getRadialWallOutlineIndexCount(int pillars, int stacks)
    {
        return pillars * 4;
    }

    public int getRadialWallDrawMode()
    {
        return GL.GL_TRIANGLE_STRIP;
    }

    public int getRadialWallOutlineDrawMode()
    {
        return GL.GL_LINES;
    }

    public void makeRadialWallVertices(float innerRadius, float outerRadius, float height, float angle,
                                       int pillars, int stacks, float[] dest)
    {
        int numPoints = this.getRadialWallVertexCount(pillars, stacks);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "pillars=" + pillars
                + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y, z;
        float a, r;
        float dz, dr;
        int s, p;
        int index;

        a = angle;
        x = (float) Math.sin(a);
        y = (float) Math.cos(a);
        z = 0.0f;

        if (stacks != 0.0f)
            dz = height / (float) stacks;
        else
            dz = 0.0f;
        dr = (outerRadius - innerRadius) / (float) pillars;

        for (s = 0; s <= stacks; s++)
        {
            for (p = 0; p <= pillars; p++)
            {
                index = p + s * (pillars + 1);
                index = 3 * index;
                r = innerRadius + p * dr;
                dest[index]     = r * x;
                dest[index + 1] = r * y;
                dest[index + 2] = z;
            }
            z += dz;
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void makeRadialWallNormals(float innerRadius, float outerRadius, float height, float angle,
                                      int pillars, int stacks, float[] dest)
    {
        int numPoints = this.getRadialWallVertexCount(pillars, stacks);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "pillars=" + pillars
                + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y;
        float a;
        int s, p;
        int index;
        float nsign;
        float[] norm;

        a = angle;
        x = (float) Math.cos(a);
        y = (float) - Math.sin(a);

        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        norm = new float[3];
        norm[0] = x * nsign;
        norm[1] = y * nsign;
        norm[2] = 0.0f;
        this.norm3AndSet(norm, 0);

        for (s = 0; s <= stacks; s++)
        {
            for (p = 0; p <= pillars; p++)
            {
                index = p + s * (pillars + 1);
                index = 3 * index;
                System.arraycopy(norm, 0, dest, index, 3);
            }
        }
    }

    public void makeRadialWallIndices(int pillars, int stacks, int[] dest)
    {
        int numIndices = this.getRadialWallIndexCount(pillars, stacks);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "pillars=" + pillars
                    + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numIndices)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int p, s;
        int vertex, index;

        index = 0;
        for (s = 0; s < stacks; s++)
        {
            if (s != 0)
            {
                if (this.orientation == INSIDE)
                {
                    vertex = pillars + s * (pillars + 1);
                    dest[index++] = vertex;
                    vertex = s * (pillars + 1);
                    dest[index++] = vertex;
                }
                else // (this.orientation == OUTSIDE)
                {
                    vertex = pillars + (s - 1) * (pillars + 1);
                    dest[index++] = vertex;
                    vertex = (s + 1) * (pillars + 1);
                    dest[index++] = vertex;
                }
            }
            for (p = 0; p <= pillars; p++)
            {
                vertex = p + s * (pillars + 1);
                if (this.orientation == INSIDE)
                {
                    dest[index++] = vertex;
                    dest[index++] = vertex + (pillars + 1);
                }
                else // (this.orientation == OUTSIDE)
                {
                    dest[index++] = vertex + (pillars + 1);
                    dest[index++] = vertex;
                }
            }
        }
    }

    public void makeRadialWallOutlineIndices(int pillars, int stacks, int[] dest)
    {
        int numIndices = this.getRadialWallOutlineIndexCount(pillars, stacks);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "pillars=" + pillars
                + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numIndices)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int vertex;
        int index = 0;
        // Bottom
        for (int i = 0; i < pillars; i++)
        {
            vertex = i;
            dest[index++] = vertex;
            dest[index++] = vertex + 1;
        }
        // Top
        for (int i = 0; i < pillars; i++)
        {
            vertex = i + stacks * (pillars + 1);
            dest[index++] = vertex;
            dest[index++] = vertex + 1;
        }
    }

    //**************************************************************//
    //********************  Long Cylinder       ********************//
    //**************************************************************//

    public int getLongCylinderVertexCount(int arcSlices, int lengthSlices, int stacks)
    {
        int slices = 2 * (arcSlices + 1) + 2 * (lengthSlices - 1);
        return slices * (stacks + 1);
    }

    public int getLongCylinderIndexCount(int arcSlices, int lengthSlices, int stacks)
    {
        int slices = 2 * (arcSlices + 1) + 2 * (lengthSlices - 1);
        return stacks * 2 * (slices + 1) + 2 * (stacks - 1);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public int getLongCylinderOutlineIndexCount(int arcSlices, int lengthSlices, int stacks)
    {
        return (arcSlices + lengthSlices )* 2 * 4;
    }

    public int getLongCylinderDrawMode()
    {
        return GL.GL_TRIANGLE_STRIP;
    }

    public int getLongCylinderOutlineDrawMode()
    {
        return GL.GL_LINES;
    }

    public void makeLongCylinderVertices(float radius, float length, float height,
                                         int arcSlices, int lengthSlices, int stacks, float[] dest)
    {
        int numPoints = this.getLongCylinderVertexCount(arcSlices, lengthSlices, stacks);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "arcSlices=" + arcSlices
                    + " lengthSlices=" + lengthSlices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y, z;
        float a;
        float dy, dz, da;
        int i, j;
        int index;

        da = (float) Math.PI / (float) arcSlices;
        dy = length / (float) lengthSlices;
        if (stacks != 0.0f)
            dz = height / (float) stacks;
        else
            dz = 0.0f;
        z = 0.0f;
        index = 0;

        for (j = 0; j <= stacks; j++)
        {
            // Top arc
            for (i = 0; i <= arcSlices; i++)
            {
                a = i * da + (3.0f * (float) Math.PI / 2.0f);
                x = (float) Math.sin(a);
                y = (float) Math.cos(a);
                dest[index++] = x * radius;
                dest[index++] = y * radius + length;
                dest[index++] = z;
            }
            // Right side.
            for (i = lengthSlices - 1; i >= 1; i--)
            {
                dest[index++] = radius;
                dest[index++] = i * dy;
                dest[index++] = z;
            }
            // Bottom arc
            for (i = 0; i <= arcSlices; i++)
            {
                a = i * da + ((float) Math.PI / 2.0f);
                x = (float) Math.sin(a);
                y = (float) Math.cos(a);
                dest[index++] = x * radius;
                dest[index++] = y * radius;
                dest[index++] = z;
            }
            // Left side.
            for (i = 1; i < lengthSlices; i++)
            {
                dest[index++] = -radius;
                dest[index++] = i * dy;
                dest[index++] = z;
            }
            z += dz;
        }
    }

    public void makeLongCylinderNormals(int arcSlices, int lengthSlices, int stacks, float[] dest)
    {
        int numPoints = this.getLongCylinderVertexCount(arcSlices, lengthSlices, stacks);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "arcSlices=" + arcSlices
                    + " lengthSlices=" + lengthSlices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y;
        float a, da;
        float nsign;
        int i, j;
        int index;

        da = (float) Math.PI / (float) arcSlices;
        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        index = 0;

        for (j = 0; j <= stacks; j++)
        {
            // Top arc
            for (i = 0; i <= arcSlices; i++)
            {
                a = i * da + (3.0f * (float) Math.PI / 2.0f);
                x = (float) Math.sin(a);
                y = (float) Math.cos(a);
                dest[index++] = x * nsign;
                dest[index++] = y * nsign;
                dest[index++] = 0.0f;
            }
            // Right side.
            for (i = lengthSlices - 1; i >= 1; i--)
            {
                //noinspection PointlessArithmeticExpression
                dest[index++] = 1.0f * nsign;
                dest[index++] = 0.0f;
                dest[index++] = 0.0f;
            }
            // Bottom arc
            for (i = 0; i <= arcSlices; i++)
            {
                a = i * da + ((float) Math.PI / 2.0f);
                x = (float) Math.sin(a);
                y = (float) Math.cos(a);
                dest[index++] = x * nsign;
                dest[index++] = y * nsign;
                dest[index++] = 0.0f;
            }
            // Left side.
            for (i = 1; i < lengthSlices; i++)
            {
                dest[index++] = -1.0f * nsign;
                dest[index++] = 0.0f;
                dest[index++] = 0.0f;
            }
        }
    }

    public void makeLongCylinderIndices(int arcSlices, int lengthSlices, int stacks, int[] dest)
    {
        int numIndices = this.getLongCylinderIndexCount(arcSlices, lengthSlices, stacks);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "arcSlices=" + arcSlices
                    + " lengthSlices=" + lengthSlices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numIndices)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int slices;
        int i, j;
        int vertex, index;

        slices = 2 * (arcSlices + 1) + 2 * (lengthSlices - 1);
        index = 0;

        for (j = 0; j < stacks; j++)
        {
            if (j != 0)
            {
                if (this.orientation == INSIDE)
                {
                    vertex = (j - 1) * slices;
                    dest[index++] = vertex;
                    vertex = j * slices;
                    dest[index++] = vertex;
                }
                else // (this.orientation == OUTSIDE)
                {
                    vertex = (j - 1) * slices;
                    dest[index++] = vertex + slices;
                    vertex = (j - 1) * slices;
                    dest[index++] = vertex;
                }
            }
            for (i = 0; i <= slices; i++)
            {
                if (i == slices)
                    vertex = j * slices;
                else
                    vertex = i + j * slices;
                if (this.orientation == INSIDE)
                {
                    dest[index++] = vertex + slices;
                    dest[index++] = vertex;
                }
                else // (this.orientation == OUTSIDE)
                {
                    dest[index++] = vertex;
                    dest[index++] = vertex + slices;
                }
            }
        }
    }

    public void makeLongCylinderOutlineIndices(int arcSlices, int lengthSlices, int stacks, int[] dest)
    {
        int numIndices = this.getLongCylinderOutlineIndexCount(arcSlices, lengthSlices, stacks);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "arcSlices=" + arcSlices
                    + " lengthSlices=" + lengthSlices + " stacks=" + stacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numIndices)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int slices = 2 * (arcSlices + 1) + 2 * (lengthSlices - 1);
        int i;
        int vertex, index;

        index = 0;
        // Bottom ring
        for (i = 0; i < slices; i++)
        {
            vertex = i;
            dest[index++] = vertex;
            dest[index++] = (i != slices - 1) ? vertex + 1 : 0;
        }
        // Top ring
        for (i = 0; i < slices; i++)
        {
            vertex = i + slices * stacks;
            dest[index++] = vertex;
            dest[index++] = (i != slices - 1) ? vertex + 1 : slices * stacks;
        }
    }

    //**************************************************************//
    //********************  Long Disk           ********************//
    //**************************************************************//

    public int getLongDiskVertexCount(int arcSlices, int lengthSlices, int loops)
    {
        int slices = 2 * (arcSlices + 1) + 2 * (lengthSlices - 1);
        return slices * (loops + 1);
    }

    public int getLongDiskIndexCount(int arcSlices, int lengthSlices, int loops)
    {
        int slices = 2 * (arcSlices + 1) + 2 * (lengthSlices - 1);
        return loops * 2 * (slices + 1) + 2 * (loops - 1);
    }

    public int getLongDiskDrawMode()
    {
        return GL.GL_TRIANGLE_STRIP;
    }

    public void makeLongDiskVertices(float innerRadius, float outerRadius, float length,
                                     int arcSlices, int lengthSlices, int loops, float[] dest)
    {
        int numPoints = this.getLongDiskVertexCount(arcSlices, lengthSlices, loops);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "arcSlices=" + arcSlices
                    + " lengthSlices=" + lengthSlices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y;
        float a, r;
        float dy, da, dr;
        int s, l;
        int index;

        dy = length / (float) lengthSlices;
        da = (float) Math.PI / (float) arcSlices;
        dr = (outerRadius - innerRadius) / (float) loops;
        index = 0;

        for (l = 0; l <= loops; l++)
        {
            r = innerRadius + l * dr;
            // Top arc.
            for (s = 0; s <= arcSlices; s++)
            {
                a = s * da + (3.0f * (float) Math.PI / 2.0f);
                x = (float) Math.sin(a);
                y = (float) Math.cos(a);
                dest[index++] = x * r;
                dest[index++] = y * r + length;
                dest[index++] = 0.0f;
            }
            // Right side.
            for (s = lengthSlices - 1; s >= 1; s--)
            {
                dest[index++] = r;
                dest[index++] = s * dy;
                dest[index++] = 0.0f;
            }
            // Bottom arc.
            for (s = 0; s <= arcSlices; s++)
            {
                a = s * da + ((float) Math.PI / 2.0f);
                x = (float) Math.sin(a);
                y = (float) Math.cos(a);
                dest[index++] = x * r;
                dest[index++] = y * r;
                dest[index++] = 0.0f;
            }
            // Left side.
            for (s = 1; s < lengthSlices; s++)
            {
                dest[index++] = -r;
                dest[index++] = s * dy;
                dest[index++] = 0.0f;
            }
        }
    }

    public void makeLongDiskNormals(int arcSlices, int lengthSlices, int loops, float[] dest)
    {
        int numPoints = this.getLongDiskVertexCount(arcSlices, lengthSlices, loops);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "arcSlices=" + arcSlices
                    + " lengthSlices=" + lengthSlices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int slices;
        int s, l;
        int index;
        float nsign;
        float[] normal;

        slices = 2 * (arcSlices + 1) + 2 * (lengthSlices - 1);

        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        normal = new float[3];
        normal[0] = 0.0f;
        normal[1] = 0.0f;
        //noinspection PointlessArithmeticExpression
        normal[2] = 1.0f * nsign;

        for (l = 0; l <= loops; l++)
        {
            for (s = 0; s < slices; s++)
            {
                index = s + l * slices;
                index = 3 * index;
                System.arraycopy(normal, 0, dest, index, 3);
            }
        }
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void makeLongDiskVertexNormals(float innerRadius, float outerRadius, float length,
                                          int arcSlices, int lengthSlices, int loops,
                                          float[] srcVerts, float[] dest)
    {
        int numPoints = this.getLongDiskVertexCount(arcSlices, lengthSlices, loops);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "arcSlices=" + arcSlices
                    + " lengthSlices=" + lengthSlices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (srcVerts == null)
        {
            String message = "nullValue.SourceVertexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int slices;
        int s, l;
        int index;
        float nsign;
        float[] norm, zero, tmp;

        slices = 2 * (arcSlices + 1) + 2 * (lengthSlices - 1);
        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        norm = new float[3];
        zero = new float[3];
        tmp = new float[3];

        for (l = 0; l <= loops; l++)
        {
            // Normal vectors for first and last loops require a special case.
            if (l == 0 || l == loops)
            {
                // Closed disk: slices are collapsed.
                if (l == 0 && innerRadius == 0.0f)
                {
                    // Top arc.
                    {
                        // Compute common normal.
                        System.arraycopy(zero, 0, norm, 0, 3);
                        for (s = 0; s <= arcSlices; s++)
                        {
                            index = s;
                            this.facenorm(srcVerts, index, index + slices + 1, index + slices, tmp);
                            this.add3AndSet(norm, 0, tmp, 0);
                        }
                        index = arcSlices;
                        this.facenorm(srcVerts, index, index + 1, index + slices, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        index = 0;
                        this.facenorm(srcVerts, index, index + slices, index + slices - 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        this.mul3AndSet(norm, 0, nsign);
                        this.norm3AndSet(norm, 0);
                        // Copy common normal to the first point of each slice.
                        for (s = 0; s <= arcSlices; s++)
                        {
                            index = s;
                            System.arraycopy(norm, 0, dest, 3 * index, 3);
                        }
                    }
                    // Right and left sides.
                    {
                        int leftSideIndex;
                        for (s = 1; s < lengthSlices; s++)
                        {
                            // Compute common normal.
                            index = s + arcSlices;
                            leftSideIndex = slices - s;
                            System.arraycopy(zero, 0, norm, 0, 3);
                            this.facenorm(srcVerts, index, index + slices, index - 1, tmp);
                            this.add3AndSet(norm, 0, tmp, 0);
                            this.facenorm(srcVerts, index, index + 1, index + slices, tmp);
                            this.add3AndSet(norm, 0, tmp, 0);
                            if (s == 1)
                                this.facenorm(srcVerts, leftSideIndex, leftSideIndex - slices + 1,
                                        leftSideIndex + slices, tmp);
                            else
                                this.facenorm(srcVerts, leftSideIndex, leftSideIndex + 1, leftSideIndex + slices, tmp);
                            this.add3AndSet(norm, 0, tmp, 0);
                            this.facenorm(srcVerts, leftSideIndex, leftSideIndex + slices, leftSideIndex - 1, tmp);
                            this.add3AndSet(norm, 0, tmp, 0);
                            this.mul3AndSet(norm, 0, nsign);
                            this.norm3AndSet(norm, 0);
                            // Copy common normal to the first point of each slice.
                            System.arraycopy(norm, 0, dest, 3 * index, 3);
                            System.arraycopy(norm, 0, dest, 3 * leftSideIndex, 3);
                        }
                    }
                    // Bottom arc.
                    {
                        // Compute common normal.
                        System.arraycopy(zero, 0, norm, 0, 3);
                        for (s = 0; s <= arcSlices; s++)
                        {
                            index = s + arcSlices + lengthSlices;
                            this.facenorm(srcVerts, index, index + slices + 1, index + slices, tmp);
                            this.add3AndSet(norm, 0, tmp, 0);
                        }
                        index = arcSlices + lengthSlices;
                        this.facenorm(srcVerts, index, index + slices, index - 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        index = (2 * arcSlices) + lengthSlices;
                        this.facenorm(srcVerts, index, index + 1, index + slices, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        this.mul3AndSet(norm, 0, nsign);
                        this.norm3AndSet(norm, 0);
                        // Copy common normal to the first point of each slice.
                        for (s = 0; s <= arcSlices; s++)
                        {
                            index = s + arcSlices + lengthSlices;
                            System.arraycopy(norm, 0, dest, 3 * index, 3);
                        }
                    }
                }
                // Open disk: each slice has a unique starting point.
                else
                {
                    for (s = 0; s < slices; s++)
                    {
                        int prevSlice, nextSlice;
                        int adjacentLoop;
                        index = s + l * slices;
                        prevSlice = index - 1;
                        nextSlice = index + 1;

                        if (s == 0)
                            prevSlice = l * slices;
                        else if (s == slices - 1)
                            nextSlice = l;

                        if (l == 0)
                            adjacentLoop = index + slices;
                        else
                            adjacentLoop = index - slices;

                        System.arraycopy(zero, 0, norm, 0, 3);

                        // Add clockwise adjacent face.
                        if (l == 0)
                            this.facenorm(srcVerts, index, nextSlice, adjacentLoop, tmp);
                        else
                            this.facenorm(srcVerts, index, adjacentLoop, nextSlice, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        // Add counter-clockwise adjacent face.
                        if (l == 0)
                            this.facenorm(srcVerts, index, adjacentLoop, prevSlice, tmp);
                        else
                            this.facenorm(srcVerts, index, prevSlice, adjacentLoop, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);

                        // Normalize and place in output.
                        this.mul3AndSet(norm, 0, nsign);
                        this.norm3AndSet(norm, 0);
                        System.arraycopy(norm, 0, dest, 3 * index, 3);
                    }
                }
            }
            // Normal vectors for internal loops.
            else
            {
                for (s = 0; s < slices; s++)
                {
                    int prevSlice, nextSlice;
                    int prevLoop, nextLoop;
                    index = s + l * slices;
                    prevSlice = index - 1;
                    nextSlice = index + 1;

                    if (s == 0)
                        prevSlice = (slices - 1) + l * slices;
                    else if (s == slices - 1)
                        nextSlice = l * slices;

                    prevLoop = index - slices;
                    nextLoop = index + slices;

                    System.arraycopy(zero, 0, norm, 0, 3);

                    // Add lower-left adjacent face.
                    this.facenorm(srcVerts, index, prevSlice, prevSlice - slices, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    this.facenorm(srcVerts, index, prevSlice - slices, prevLoop, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    // Add lower-right adjacent face.
                    this.facenorm(srcVerts, index, prevLoop, nextSlice - slices, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    this.facenorm(srcVerts, index, nextSlice - slices, nextSlice, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    // Add upper-right adjacent face.
                    this.facenorm(srcVerts, index, nextSlice, nextSlice + slices, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    this.facenorm(srcVerts, index, nextSlice + slices, nextLoop, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    // Add upper-left adjacent face.
                    this.facenorm(srcVerts, index, nextLoop, prevSlice + slices, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);
                    this.facenorm(srcVerts, index, prevSlice + slices, prevSlice, tmp);
                    this.add3AndSet(norm, 0, tmp, 0);

                    // Normalize and place in output.
                    this.mul3AndSet(norm, 0, nsign);
                    this.norm3AndSet(norm, 0);
                    System.arraycopy(norm, 0, dest, 3 * index, 3);
                }
            }
        }
    }

    public void makeLongDiskIndices(int arcSlices, int lengthSlices, int loops, int[] dest)
    {
        int numIndices = this.getLongDiskIndexCount(arcSlices, lengthSlices, loops);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "arcSlices=" + arcSlices
                    + " lengthSlices=" + lengthSlices + " loops=" + loops);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numIndices)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int slices;
        int s, l;
        int vertex, index;

        slices = 2 * (arcSlices + 1) + 2 * (lengthSlices - 1);
        index = 0;

        for (l = 0; l < loops; l++)
        {
            if (l != 0)
            {
                if (this.orientation == INSIDE)
                {
                    vertex = (l - 1) * slices;
                    dest[index++] = vertex + slices;
                    vertex = (l - 1) * slices;
                    dest[index++] = vertex;
                }
                else // (this.orientation == OUTSIDE)
                {
                    vertex = (l - 1) * slices;
                    dest[index++] = vertex;
                    vertex = l * slices;
                    dest[index++] = vertex;
                }
            }
            for (s = 0; s <= slices; s++)
            {
                if (s == slices)
                    vertex = l * slices;
                else
                    vertex = s + l * slices;
                if (this.orientation == INSIDE)
                {
                    dest[index++] = vertex;
                    dest[index++] = vertex + slices;
                }
                else // (this.orientation == OUTSIDE)
                {
                    dest[index++] = vertex + slices;
                    dest[index++] = vertex;
                }
            }
        }
    }

    //**************************************************************//
    //********************  Polygon                 ****************//
    //**************************************************************//

    public int computePolygonWindingOrder2(int pos, int count, Vec4[] points)
    {
        float area;
        int order;

        area = this.computePolygonArea2(pos, count, points);
        if (area < 0.0f)
            order = CLOCKWISE;
        else
            order = COUNTER_CLOCKWISE;

        return order;
    }

    public float computePolygonArea2(int pos, int count, Vec4[] points)
    {
        if (pos < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "pos=" + pos);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (count < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "count=" + count);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (points == null)
        {
            String message = "nullValue.PointsIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (points.length < (pos + count))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "points.length < " + (pos + count));
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float area;
        int i;
        int coord, nextCoord;

        area = 0.0f;
        for (i = 0; i < count; i++)
        {
            coord = pos + i;
            nextCoord = (i == count - 1) ? (pos) : (pos + i + 1);
            area += points[coord].x     * points[nextCoord].y;
            area -= points[nextCoord].x * points[coord].y;
        }
        area /= 2.0f;

        return area;
    }

    public IndexedTriangleArray tessellatePolygon2(int pos, int count, float[] vertices)
    {
        if (count < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "count=" + count);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices == null)
        {
            String message = "nullValue.VertexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices.length < (pos + count))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "vertices.length=" + vertices.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        TessellatorCallback cb;
        GLUTessellatorSupport glts;
        double[] dvertices = new double[3 * count];
        int i;
        int srcIndex, destIndex;

        cb = new TessellatorCallback(this, count, vertices);

        glts = new GLUTessellatorSupport();
        glts.beginTessellation(this.glu, cb, new Vec4(0, 0, 1));
        try
        {
            this.glu.gluTessBeginPolygon(glts.getGLUtessellator(), null);
            this.glu.gluTessBeginContour(glts.getGLUtessellator());
            for (i = 0; i < count; i++)
            {
                srcIndex = 3 * (pos + i);
                destIndex = 3 * i;
                dvertices[destIndex] = vertices[srcIndex];
                dvertices[destIndex + 1] = vertices[srcIndex + 1];
                dvertices[destIndex + 2] = 0.0f;
                this.glu.gluTessVertex(glts.getGLUtessellator(), dvertices, destIndex, pos + i);
            }
            this.glu.gluTessEndContour(glts.getGLUtessellator());
            this.glu.gluTessEndPolygon(glts.getGLUtessellator());
        }
        finally
        {
            glts.endTessellation(this.glu);
        }

        return new IndexedTriangleArray(
            cb.getIndexCount(), cb.getIndices(),
            cb.getVertexCount(), cb.getVertices());
    }

    private static class TessellatorCallback extends GLUtessellatorCallbackAdapter
    {
        private GeometryBuilder gb;
        private int type;
        private int indexCount;
        private int primIndexCount;
        private int vertexCount;
        private int[] indices;
        private int[] primIndices;
        private float[] vertices;

        private TessellatorCallback(GeometryBuilder gb, int vertexCount, float[] vertices)
        {
            this.gb = gb;
            this.indexCount = 0;
            this.primIndexCount = 0;
            this.vertexCount = vertexCount;

            int initialCapacity = this.gb.nextPowerOfTwo(3 * vertexCount);
            this.indices = new int[initialCapacity];
            this.primIndices = new int[initialCapacity];
            this.vertices = this.gb.copyOf(vertices, initialCapacity);
        }

        public int getIndexCount()
        {
            return this.indexCount;
        }

        public int[] getIndices()
        {
            return this.indices;
        }

        public int getVertexCount()
        {
            return this.vertexCount;
        }

        public float[] getVertices()
        {
            return this.vertices;
        }

        protected void addTriangle(int i1, int i2, int i3)
        {
            // Triangle indices will be specified in counter-clockwise order. To reverse the ordering, we
            // swap the indices.

            int minCapacity, oldCapacity, newCapacity;

            minCapacity = this.indexCount + 3;
            oldCapacity = this.indices.length;
            while (minCapacity > oldCapacity)
            {
                newCapacity = 2 * oldCapacity;
                this.indices = this.gb.copyOf(this.indices, newCapacity);
                oldCapacity = minCapacity;
            }

            if (this.gb.orientation == GeometryBuilder.INSIDE)
            {
                this.indices[this.indexCount++] = this.primIndices[i1];
                this.indices[this.indexCount++] = this.primIndices[i3];
                this.indices[this.indexCount++] = this.primIndices[i2];
            }
            else // (this.gb.orientation == GeometryBuilder.OUTSIDE)
            {
                this.indices[this.indexCount++] = this.primIndices[i1];
                this.indices[this.indexCount++] = this.primIndices[i2];
                this.indices[this.indexCount++] = this.primIndices[i3];
            }
        }

        public void begin(int type)
        {
            this.type = type;
            this.primIndexCount = 0;
        }

        public void vertex(Object vertexData)
        {
            int minCapacity, oldCapacity, newCapacity;

            oldCapacity = this.primIndices.length;
            minCapacity = this.primIndexCount + 1;
            while (minCapacity > oldCapacity)
            {
                newCapacity = 2 * oldCapacity;
                this.primIndices = this.gb.copyOf(this.primIndices, newCapacity);
                oldCapacity = newCapacity;
            }

            int index = (Integer) vertexData;
            this.primIndices[this.primIndexCount++] = index;
        }

        public void end()
        {
            int i;

            if (this.type == GL.GL_TRIANGLES)
            {
                for (i = 2; i < this.primIndexCount; i++)
                {
                    if (((i + 1) % 3) == 0)
                        this.addTriangle(i - 2, i - 1, i);
                }
            }
            else if (this.type == GL.GL_TRIANGLE_STRIP)
            {
                for (i = 2; i < this.primIndexCount; i++)
                {
                    if ((i % 2) == 0)
                        this.addTriangle(i - 2, i - 1, i);
                    else
                        this.addTriangle(i - 1, i - 2, i);
                }
            }
            else if (this.type == GL.GL_TRIANGLE_FAN)
            {
                for (i = 2; i < this.primIndexCount; i++)
                {
                    this.addTriangle(0, i - 1, i);
                }
            }
        }

        public void combine(double[] coords, Object[] data, float[] weight, Object[] outData)
        {
            outData[0] = data[0];
        }
    }

    //**************************************************************//
    //********************  Indexed Triangle Array  ****************//
    //**************************************************************//

    public int getIndexedTriangleArrayDrawMode()
    {
        return GL.GL_TRIANGLES;
    }

    public static class IndexedTriangleArray
    {
        private int indexCount;
        private int vertexCount;
        private int[] indices;
        private float[] vertices;

        public IndexedTriangleArray(int indexCount, int[] indices, int vertexCount, float[] vertices)
        {
            this.indexCount = indexCount;
            this.indices = indices;
            this.vertexCount = vertexCount;
            this.vertices = vertices;
        }

        public int getIndexCount()
        {
            return this.indexCount;
        }

        public int[] getIndices()
        {
            return this.indices;
        }

        public int getVertexCount()
        {
            return this.vertexCount;
        }

        public float[] getVertices()
        {
            return this.vertices;
        }
    }

    public void subdivideIndexedTriangleArray(IndexedTriangleArray ita)
    {
        if (ita == null)
        {
            String message = "nullValue.IndexedTriangleArray";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int indexCount;
        int a, b, c;
        int ab, bc, ca;
        int i, j;
        HashMap<Edge, Integer> edgeMap;
        Edge e;
        Integer split;

        indexCount = ita.indexCount;
        edgeMap = new HashMap<Edge, Integer>();

        // Iterate over each triangle, and split the edge of each triangle. Each edge is split exactly once. The
        // index of the new vertex created by a split is stored in edgeMap.
        for (i = 0; i < indexCount; i += 3)
        {
            for (j = 0; j < 3; j++)
            {
                a = ita.indices[i + j];
                b = ita.indices[(j < 2) ? (i + j + 1) : i];
                e = new Edge(a, b);
                split = edgeMap.get(e);
                if (split == null)
                {
                    split = this.splitVertex(ita, a, b);
                    edgeMap.put(e, split);
                }
            }
        }

        // Iterate over each triangle, and create indices for four new triangles, replacing indices of the original
        // triangle.
        for (i = 0; i < indexCount; i += 3)
        {
            a = ita.indices[i];
            b = ita.indices[i + 1];
            c = ita.indices[i + 2];
            ab = edgeMap.get(new Edge(a, b));
            bc = edgeMap.get(new Edge(b, c));
            ca = edgeMap.get(new Edge(c, a));
            this.indexSplitTriangle(ita, i, a, b, c, ab, bc, ca);
        }
    }

    public IndexedTriangleArray subdivideIndexedTriangles(int indexCount, int[] indices,
                                                          int vertexCount, float[] vertices)
    {
        int numCoords = 3 * vertexCount;

        if (indices == null)
        {
            String message = "nullValue.IndexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (indices.length < indexCount)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "indices.length=" + indices.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices == null)
        {
            String message = "nullValue.VertexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices.length < numCoords)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "vertices.length=" + vertices.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        IndexedTriangleArray ita = new IndexedTriangleArray(indexCount, indices, vertexCount, vertices);
        this.subdivideIndexedTriangleArray(ita);
        
        return ita;
    }

    public void makeIndexedTriangleArrayNormals(IndexedTriangleArray ita, float[] dest)
    {
        if (ita == null)
        {
            String message = "nullValue.IndexedTriangleArray";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int numCoords = 3 * ita.vertexCount;

        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.makeIndexedTriangleArrayNormals(0, ita.indexCount, ita.indices, 0, ita.vertexCount, ita.vertices, dest);
    }

    public void makeIndexedTriangleArrayNormals(int indexPos, int indexCount, int[] indices,
                                                int vertexPos, int vertexCount, float[] vertices,
                                                float[] dest)
    {
        if (indices == null)
        {
            String message = "nullValue.IndexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (indices.length < (indexPos + indexCount))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "indices.length=" + indices.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices == null)
        {
            String message = "nullValue.VertexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices.length < (vertexPos + vertexCount))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "vertices.length=" + vertices.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < (vertexPos + vertexCount))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "dest.length=" + dest.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int i, v;
        int index;
        float nsign;
        float[] norm;
        int[] faceIndices;

        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        norm = new float[3];
        faceIndices = new int[3];

        // Compute the normal for each face, contributing that normal to each vertex of the face.
        for (i = 0; i < indexCount; i += 3)
        {
            faceIndices[0] = indices[indexPos + i];
            faceIndices[1] = indices[indexPos + i + 1];
            faceIndices[2] = indices[indexPos + i + 2];
            // Compute the normal for this face.
            this.facenorm(vertices, faceIndices[0], faceIndices[1], faceIndices[2], norm);
            // Add this face normal to the normal at each vertex.
            for (v = 0; v < 3; v++)
            {
                index = 3 * faceIndices[v];
                this.add3AndSet(dest, index, norm, 0);
            }
        }

        // Scale and normalize each vertex normal.
        for (v = 0; v < vertexCount; v++)
        {
            index = 3 * (vertexPos + v);
            this.mul3AndSet(dest, index, nsign);
            this.norm3AndSet(dest, index);
        }
    }

    public void makeIndexedTriangleStripNormals(int indexPos, int indexCount, int[] indices,
                                                int vertexPos, int vertexCount, float[] vertices,
                                                float[] dest)
    {
        if (indices == null)
        {
            String message = "nullValue.IndexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (indices.length < indexPos + indexCount)
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "indices.length=" + indices.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices == null)
        {
            String message = "nullValue.VertexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (vertices.length < 3 * (vertexPos + vertexCount))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "vertices.length=" + vertices.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < 3 * (vertexPos + vertexCount))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "dest.length=" + dest.length);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int i, v;
        int index;
        float nsign;
        float[] norm;
        int[] faceIndices;

        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        norm = new float[3];
        faceIndices = new int[3];

        // Compute the normal for each face, contributing that normal to each vertex of the face.
        for (i = 2; i < indexCount; i++)
        {
            if ((i % 2) == 0)
            {
                faceIndices[0] = indices[indexPos + i - 2];
                faceIndices[1] = indices[indexPos + i - 1];
                faceIndices[2] = indices[indexPos + i];
            }
            else
            {
                faceIndices[0] = indices[indexPos + i - 1];
                faceIndices[1] = indices[indexPos + i - 2];
                faceIndices[2] = indices[indexPos + i];
            }
            // Compute the normal for this face.
            this.facenorm(vertices, faceIndices[0], faceIndices[1], faceIndices[2], norm);
            // Add this face normal to the normal at each vertex.
            for (v = 0; v < 3; v++)
            {
                index = 3 * faceIndices[v];
                this.add3AndSet(dest, index, norm, 0);
            }
        }

        // Scale and normalize each vertex normal.
        for (v = 0; v < vertexCount; v++)
        {
            index = 3 * (vertexPos + v);
            this.mul3AndSet(dest, index, nsign);
            this.norm3AndSet(dest, index);
        }
    }

    private int splitVertex(IndexedTriangleArray ita, int a, int b)
    {
        int minCapacity, oldCapacity, newCapacity;

        oldCapacity = ita.vertices.length;
        minCapacity = 3 * (ita.vertexCount + 1);
        while (minCapacity > oldCapacity)
        {
            newCapacity = 2 * oldCapacity;
            ita.vertices = this.copyOf(ita.vertices, newCapacity);
            oldCapacity = newCapacity;
        }

        int s = ita.vertexCount;
        int is = 3 * s;
        int ia = 3 * a;
        int ib = 3 * b;
        ita.vertices[is]     = (ita.vertices[ia]     + ita.vertices[ib])     / 2.0f;
        ita.vertices[is + 1] = (ita.vertices[ia + 1] + ita.vertices[ib + 1]) / 2.0f;
        ita.vertices[is + 2] = (ita.vertices[ia + 2] + ita.vertices[ib + 2]) / 2.0f;
        ita.vertexCount++;

        return s;
    }

    private void indexSplitTriangle(IndexedTriangleArray ita, int original, int a, int b, int c, int ab, int bc, int ca)
    {
        int minCapacity, oldCapacity, newCapacity;

        // One of the new triangles will overwrite the original triangles, so we only need enough space to index
        // three new triangles.
        oldCapacity = ita.indices.length;
        minCapacity = ita.indexCount + 9;
        while (minCapacity > oldCapacity)
        {
            newCapacity = 2 * oldCapacity;
            ita.indices = this.copyOf(ita.indices, newCapacity);
            oldCapacity = newCapacity;
        }

        // Lower-left triangle.
        // This triangle replaces the original.
        ita.indices[original]     = a;
        ita.indices[original + 1] = ab;
        ita.indices[original + 2] = ca;

        // Center triangle.
        ita.indices[ita.indexCount++] = ab;
        ita.indices[ita.indexCount++] = bc;
        ita.indices[ita.indexCount++] = ca;

        // Lower-right triangle.
        ita.indices[ita.indexCount++] = ab;
        ita.indices[ita.indexCount++] = b;
        ita.indices[ita.indexCount++] = bc;

        // Upper triangle.
        ita.indices[ita.indexCount++] = ca;
        ita.indices[ita.indexCount++] = bc;
        ita.indices[ita.indexCount++] = c;
    }

    private static class Edge
    {
        public final int a;
        public final int b;

        public Edge(int a, int b)
        {
            this.a = a;
            this.b = b;
        }

        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            // Compares a non directed edge between two points. Therefore we must treat edge equivalence as
            // edge(ab)=edge(ab) OR edge(ab)=edge(ba).
            Edge that = (Edge) o;
            return (this.a == that.a && this.b == that.b)
                || (this.a == that.b && this.b == that.a);
        }

        public int hashCode()
        {
            // Represents the hash for a a non directed edge between two points. Therefore we use a non-commutative
            // hash so that hash(ab)=hash(ba).
            return this.a + this.b;
        }
    }

    //**************************************************************//
    //********************  Subdivision Points  ********************//
    //**************************************************************//

    public int getSubdivisionPointsVertexCount(int subdivisions)
    {
        return (1 << subdivisions) + 1;
    }

    public void makeSubdivisionPoints(float x1, float y1, float z1, float x2, float y2, float z2,
                                      int subdivisions, float[] dest)
    {
        int numPoints = this.getSubdivisionPointsVertexCount(subdivisions);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "subdivisions=" + subdivisions);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < numCoords)
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int first, last;
        int index;

        first = 0;
        last = numPoints - 1;

        index = 3 * first;
        dest[index]     = x1;
        dest[index + 1] = y1;
        dest[index + 2] = z1;

        index = 3 * last;
        dest[index]     = x2;
        dest[index + 1] = y2;
        dest[index + 2] = z2;

        this.subdivide(x1, y1, z1, x2, y2, z2, subdivisions, dest, first, last);
    }

    private void subdivide(float x1, float y1, float z1, float x2, float y2, float z2, int subdivisions,
                           float[] dest, int first, int last)
    {
        float x, y, z;
        int mid, index;

        if (subdivisions <= 0)
            return;
        
        x = (x1 + x2) / 2.0f;
        y = (y1 + y2) / 2.0f;
        z = (z1 + z2) / 2.0f;

        mid = (first + last) / 2;
        index = mid * 3;
        dest[index]     = x;
        dest[index + 1] = y;
        dest[index + 2] = z;

        if (subdivisions > 1)
        {
            this.subdivide(x1, y1, z1, x, y, z, subdivisions - 1, dest, first, mid);
            this.subdivide(x, y, z, x2, y2, z2, subdivisions - 1, dest, mid, last);
        }
    }

    //**************************************************************//
    //********************  Bilinear Surface ********************//
    //**************************************************************//

    public int getBilinearSurfaceFillIndexCount(int uStacks, int vStacks)
    {
        return vStacks * 2 * (uStacks + 1) + 2 * (vStacks - 1);
    }

    public int getBilinearSurfaceOutlineIndexCount(int uStacks, int vStacks, int mask)
    {
        int count = 0;
        if ((mask & TOP) != 0)
            count += 2 * uStacks;
        if ((mask & BOTTOM) != 0)
            count += 2 * uStacks;
        if ((mask & LEFT) != 0)
            count += 2 * vStacks;
        if ((mask & RIGHT) != 0)
            count += 2 * vStacks;

        return count;
    }

    public int getBilinearSurfaceVertexCount(int uStacks, int vStacks)
    {
        return (uStacks + 1) * (vStacks + 1);
    }

    public int getBilinearSurfaceFillDrawMode()
    {
        return GL.GL_TRIANGLE_STRIP;
    }

    public int getBilinearSurfaceOutlineDrawMode()
    {
        return GL.GL_LINES;
    }

    public void makeBilinearSurfaceFillIndices(int vertexPos, int uStacks, int vStacks, int destPos, int[] dest)
    {
        int numIndices = this.getBilinearSurfaceFillIndexCount(uStacks, vStacks);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "uStacks=" + uStacks
                + " vStacks=" + vStacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < (numIndices + destPos))
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int ui, vi;
        int vertex, index;

        index = destPos;
        for (vi = 0; vi < vStacks; vi++)
        {
            if (vi != 0)
            {
                if (this.orientation == INSIDE)
                {
                    vertex = uStacks + vi * (uStacks + 1);
                    dest[index++] = vertexPos + vertex;
                    vertex = vi * (uStacks + 1);
                    dest[index++] = vertexPos + vertex;
                }
                else // (this.orientation == OUTSIDE)
                {
                    vertex = uStacks + (vi - 1) * (uStacks + 1);
                    dest[index++] = vertexPos + vertex;
                    vertex = vi * (uStacks + 1) + (uStacks + 1);
                    dest[index++] = vertexPos + vertex;
                }
            }
            for (ui = 0; ui <= uStacks; ui++)
            {
                vertex = ui + vi * (uStacks + 1);
                if (this.orientation == INSIDE)
                {
                    dest[index++] = vertexPos + vertex;
                    dest[index++] = vertexPos + vertex + (uStacks + 1);
                }
                else // (this.orientation == OUTSIDE)
                {
                    dest[index++] = vertexPos + vertex + (uStacks + 1);
                    dest[index++] = vertexPos + vertex;
                }
            }
        }
    }

    public void makeBilinearSurfaceOutlineIndices(int vertexPos, int uStacks, int vStacks, int mask, int destPos,
        int[] dest)
    {
        int numIndices = this.getBilinearSurfaceOutlineIndexCount(uStacks, vStacks, mask);

        if (numIndices < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "uStacks=" + uStacks
                + " vStacks=" + vStacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < (numIndices + destPos))
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int ui, vi;
        int vertex, index;

        index = destPos;
        // Bottom row.
        if ((mask & BOTTOM) != 0)
        {
            for (ui = 0; ui < uStacks; ui++)
            {
                vertex = ui;
                dest[index++] = vertexPos + vertex;
                vertex = ui + 1;
                dest[index++] = vertexPos + vertex;
            }
        }
        // Right side.
        if ((mask & RIGHT) != 0)
        {
            for (vi = 0; vi < vStacks; vi++)
            {
                vertex = uStacks + vi * (uStacks + 1);
                dest[index++] = vertexPos + vertex;
                vertex = uStacks + (vi + 1) * (uStacks + 1);
                dest[index++] = vertexPos + vertex;
            }
        }
        // Top side.
        if ((mask & TOP) != 0)
        {
            for (ui = uStacks; ui > 0; ui--)
            {
                vertex = ui + vStacks * (uStacks + 1);
                dest[index++] = vertexPos + vertex;
                vertex = (ui - 1) + vStacks * (uStacks + 1);
                dest[index++] = vertexPos + vertex;
            }
        }
        // Left side.
        if ((mask & LEFT) != 0)
        {
            for (vi = vStacks; vi > 0; vi--)
            {
                vertex = vi * (uStacks + 1);
                dest[index++] = vertexPos + vertex;
                vertex = (vi - 1) * (uStacks + 1);
                dest[index++] = vertexPos + vertex;
            }
        }
    }

    public void makeBilinearSurfaceVertices(float[] control, int destPos, int uStacks, int vStacks, float[] dest)
    {
        int numPoints = this.getBilinearSurfaceVertexCount(uStacks, vStacks);
        int numCoords = 3 * numPoints;

        if (control == null)
        {
            String message = "nullValue.ControlPointArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (control.length < 12)
        {
            String message = "generic.ControlPointArrayInvalidLength " + control.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "uStacks=" + uStacks
                    + " vStacks=" + vStacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < (numCoords + 3 * destPos))
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        float x, y, z;
        float u, v;
        float du, dv;
        float oneMinusU, oneMinusV;
        int ui, vi;
        int index;

        du = 1.0f / (float) uStacks;
        dv = 1.0f / (float) vStacks;

        for (vi = 0; vi <= vStacks; vi++)
        {
            v = vi * dv;
            oneMinusV = 1.0f - v;
            for (ui = 0; ui <= uStacks; ui++)
            {
                u = ui * du;
                oneMinusU = 1.0f - u;
                index = ui + vi * (uStacks + 1);
                index = 3 * (destPos + index);
                x = oneMinusU * oneMinusV * control[0]  // Lower left control point
                  + u         * oneMinusV * control[3]  // Lower right control point
                  + u         * v         * control[6]  // Upper right control point
                  + oneMinusU * v         * control[9]; // Upper left control point
                y = oneMinusU * oneMinusV * control[1]
                  + u         * oneMinusV * control[4]
                  + u         * v         * control[7]
                  + oneMinusU * v         * control[10];
                z = oneMinusU * oneMinusV * control[2]
                  + u         * oneMinusV * control[5]
                  + u         * v         * control[8]
                  + oneMinusU * v         * control[11];
                dest[index]     = x;
                dest[index + 1] = y;
                dest[index + 2] = z;
            }
        }
    }

    public void makeBilinearSurfaceVertexNormals(int srcPos, int uStacks, int vStacks, float[] srcVerts,
                                                 int destPos, float dest[])
    {
        int numPoints = this.getBilinearSurfaceVertexCount(uStacks, vStacks);
        int numCoords = 3 * numPoints;

        if (numPoints < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "uStacks=" + uStacks
                    + " vStacks=" + vStacks);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (srcVerts == null)
        {
            String message = "nullValue.SourceVertexArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestinationArrayIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest.length < (numCoords + 3 * destPos))
        {
            String message = "generic.DestinationArrayInvalidLength " + dest.length;
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        int ui, vi;
        int index;
        int vprev, vnext;
        float nsign;
        float[] norm, zero, tmp;

        nsign = (this.orientation == OUTSIDE) ? 1.0f : -1.0f;
        norm = new float[3];
        zero = new float[3];
        tmp = new float[3];

        for (vi = 0; vi <= vStacks; vi++)
        {
            for (ui = 0; ui <= uStacks; ui++)
            {
                index = ui + vi * (uStacks + 1);
                index = srcPos + index;
                vprev = index - (uStacks + 1);
                vnext = index + (uStacks + 1);

                System.arraycopy(zero, 0, norm, 0, 3);

                // Adjacent faces below.
                if (vi > 0)
                {
                    // Adjacent faces below and to the left.
                    if (ui > 0)
                    {
                        this.facenorm(srcVerts, index, index - 1, vprev - 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        this.facenorm(srcVerts, index, vprev - 1, vprev, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                    }
                    // Adjacent faces below and to the right.
                    if (ui < uStacks)
                    {
                        this.facenorm(srcVerts, index, vprev, vprev + 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        this.facenorm(srcVerts, index, vprev + 1, index + 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                    }
                }

                // Adjacent faces above.
                if (vi < vStacks)
                {
                    // Adjacent faces above and to the left.
                    if (ui > 0)
                    {
                        this.facenorm(srcVerts, index, vnext, vnext - 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        this.facenorm(srcVerts, index, vnext - 1, index - 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                    }
                    // Adjacent faces above and to the right.
                    if (ui < uStacks)
                    {
                        this.facenorm(srcVerts, index, index + 1, vnext + 1, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                        this.facenorm(srcVerts, index, vnext + 1, vnext, tmp);
                        this.add3AndSet(norm, 0, tmp, 0);
                    }
                }

                // Normalize and place in output.
                this.mul3AndSet(norm, 0, nsign);
                this.norm3AndSet(norm, 0);
                System.arraycopy(norm, 0, dest, 3 * index, 3);
            }
        }
    }

    //**************************************************************//
    //********************  Geometry Support    ********************//
    //**************************************************************//

    public <T> void reversePoints(int pos, int count, T[] points)
    {
        if (pos < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "pos=" + pos);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (count < 0)
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", "count=" + count);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (points == null)
        {
            String message = "nullValue.PointsIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (points.length < (pos + count))
        {
            String message = Logging.getMessage("generic.ArrayInvalidLength", "points.length < " + (pos + count));
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        T tmp;
        int i, j, mid;

        for (i = 0, mid = count >> 1, j = count - 1; i < mid; i++, j--)
        {
            tmp = points[pos + i];
            points[pos + i] = points[pos + j];
            points[pos + j] = tmp;
        }
    }

    private int[] copyOf(int[] original, int newLength)
    {
        int[] copy;

        copy = new int[newLength];
        System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));

        return copy;
    }

    private float[] copyOf(float[] original, int newLength)
    {
        float[] copy;

        copy = new float[newLength];
        System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength));

        return copy;
    }

    private void facenorm(float[] srcVerts, int vertA, int vertB, int vertC, float[] dest)
    {
        int ia, ib, ic;
        float[] ab, ac;

        ia = 3 * vertA;
        ib = 3 * vertB;
        ic = 3 * vertC;
        ab = new float[3];
        ac = new float[3];

        this.sub3(srcVerts, ib, srcVerts, ia, ab, 0);
        this.sub3(srcVerts, ic, srcVerts, ia, ac, 0);
        this.cross3(ab, ac, dest);
        this.norm3AndSet(dest, 0);
    }

    private void add3AndSet(float[] a, int aPos, float[] b, int bPos)
    {
        a[aPos]     = a[aPos]     + b[bPos];
        a[aPos + 1] = a[aPos + 1] + b[bPos + 1];
        a[aPos + 2] = a[aPos + 2] + b[bPos + 2];
    }

    private void sub3(float[] a, int aPos, float[] b, int bPos, float[] dest, int destPos)
    {
        dest[destPos]     = a[aPos]     - b[bPos];
        dest[destPos + 1] = a[aPos + 1] - b[bPos + 1];
        dest[destPos + 2] = a[aPos + 2] - b[bPos + 2];
    }

    private void cross3(float[] a, float[] b, float[] dest)
    {
        dest[0] = a[1] * b[2] - a[2] * b[1];
        dest[1] = a[2] * b[0] - a[0] * b[2];
        dest[2] = a[0] * b[1] - a[1] * b[0];
    }

    private void mul3AndSet(float[] src, int srcPos, float c)
    {
        src[srcPos]     *= c;
        src[srcPos + 1] *= c;
        src[srcPos + 2] *= c;
    }

    private void norm3AndSet(float[] src, int srcPos)
    {
        float len;

        len = src[srcPos] * src[srcPos] + src[srcPos + 1] * src[srcPos + 1] + src[srcPos + 2] * src[srcPos + 2];
        if (len != 0.0f)
        {
            len = (float) Math.sqrt(len);
            src[srcPos]     /= len;
            src[srcPos + 1] /= len;
            src[srcPos + 2] /= len;
        }
    }

    private int nextPowerOfTwo(int n)
    {
        int i = 1;
        while (i < n)
            i <<= 1;
        return i;
    }
}
