/*
 * Copyright (c) 2006 Greg Rodgers All Rights Reserved.
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
 * The names of Greg Rodgers, Sun Microsystems, Inc. or the names of
 * contributors may not be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. GREG ,
 * SUN MICROSYSTEMS, INC. ("SUN"), AND SUN'S LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL GREG
 * RODGERS, SUN, OR SUN'S LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT
 * OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
 * PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF GREG
 * RODGERS OR SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 *
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 */

package net.java.joglutils.ThreeDS;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Loader3DS
{
// Primary Chunk, at the beginning of each file
    private static final int PRIMARY = 0x4D4D;

// Main Chunks
    private static final int EDITOR = 0x3D3D;
    private static final int VERSION = 0x0002;
    private static final int EDITKEYFRAME = 0xB000;

// Sub defines of EDITOR
    private static final int MATERIAL = 0xAFFF;
    private static final int OBJECT = 0x4000;

// Sub defines of MATERIAL
    private static final int MATNAME = 0xA000;
    private static final int MATDIFFUSE = 0xA020;
    private static final int MATMAP = 0xA200;
    private static final int MATMAPFILE = 0xA300;

    private static final int OBJECT_MESH = 0x4100;

// Sub defines of OBJECT_MESH
    private static final int OBJECT_VERTICES = 0x4110;
    private static final int OBJECT_FACES = 0x4120;
    private static final int OBJECT_MATERIAL = 0x4130;
    private static final int OBJECT_UV = 0x4140;

// File reader
    private File file;
    private boolean loaded = false;
    private FileInputStream fileInputStream;
    private DataInputStream dataInputStream;

// Global chunks
    private Chunk currentChunk, tempChunk;

    // Constructor
    public Loader3DS()
    {
        currentChunk = new Chunk();
        tempChunk = new Chunk();
    }

    // Verified
    public boolean load(Model3DS model, String fileName)
    {
        String strMessage;

        file = new File(fileName);
        try {
            fileInputStream = new FileInputStream(file);
            dataInputStream = new DataInputStream(fileInputStream);
        } catch (IOException e) {
            System.err.println("Error:  File IO error in: Import 3DS");
            return false;
        }

        readChunkHeader(currentChunk);

        if (currentChunk.id != PRIMARY) {
            System.err.println("Unable to load PRIMARY chuck from file!");
            return false;
        }

	processNextChunk(model, currentChunk);

	computeNormals(model);

        try {
            dataInputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            System.err.println("Error:  File IO error in: Closing File");
            return false;
        }

        loaded = true;

        return loaded;
    }

    // Verified
    void processNextChunk(Model3DS model, Chunk previousChunk)
    {
	int version = 0;
	byte buffer[] = null;

	currentChunk = new Chunk();

        try {
            while (previousChunk.bytesRead < previousChunk.length) {
                readChunkHeader(currentChunk);

                switch (currentChunk.id) {
                    case VERSION:
                        version = swap(dataInputStream.readInt());
                        currentChunk.bytesRead += 4;

                        if (version > 0x03)
                            System.err.println("This 3DS file is over version 3 so it may load incorrectly");
                    break;

                    case EDITOR:
                        readChunkHeader(tempChunk);
                        buffer = new byte[tempChunk.length - tempChunk.bytesRead];
                        tempChunk.bytesRead += dataInputStream.read(buffer, 0, tempChunk.length - tempChunk.bytesRead);
                        currentChunk.bytesRead += tempChunk.bytesRead;
                        processNextChunk(model, currentChunk);
                    break;

                    case MATERIAL:
                        Material mat = new Material();
                        model.addMaterial(mat);
                        processNextMaterialChunk(model, mat, currentChunk);
                    break;

                    case OBJECT:
                        Obj obj = new Obj();
                        obj.strName = getString(currentChunk);
                        model.addObject(obj);
                        processNextObjectChunk(model, obj, currentChunk);
                    break;

//                    case EDITKEYFRAME:
//                    break;

                    default:
                        buffer = new byte[currentChunk.length - currentChunk.bytesRead];
                        currentChunk.bytesRead += dataInputStream.read(buffer, 0, currentChunk.length - currentChunk.bytesRead);
                    break;
                }
                previousChunk.bytesRead += currentChunk.bytesRead;
            }
        }
        catch (IOException e) {
            System.err.println("Error:  File IO error in: Process Next Chunk");
            return;
        }
	currentChunk = previousChunk;
    }

    // Verified
    private void readChunkHeader(Chunk chunk)
    {
        byte buffer[] = new byte[2];

        try {
            chunk.id = swap(dataInputStream.readShort());
            chunk.id &= 0x0000FFFF;
            chunk.bytesRead = 2;
            chunk.length = swap(dataInputStream.readInt());
            chunk.bytesRead += 4;

        } catch (IOException e) {
            System.err.println("Error:  File IO error in: Read Chunk Header");
            return;
        }
    }

    // Verified
    private void processNextObjectChunk(Model3DS model, Obj object, Chunk previousChunk)
    {
        byte buffer[] = null;

	currentChunk = new Chunk();

        try {
            while (previousChunk.bytesRead < previousChunk.length) {
                readChunkHeader(currentChunk);

                switch (currentChunk.id) {
                    case OBJECT_MESH:
                        processNextObjectChunk(model, object, currentChunk);
                    break;

                    case OBJECT_VERTICES:
                        readVertices(object, currentChunk);
                    break;

                    case OBJECT_FACES:
                        readFaceList(object, currentChunk);
                    break;

                    case OBJECT_MATERIAL:
                        readObjectMaterial(model, object, currentChunk);
                    break;

                    case OBJECT_UV:
                        readUVCoordinates(object, currentChunk);
                    break;

                    default:
                        buffer = new byte[currentChunk.length - currentChunk.bytesRead];
                        currentChunk.bytesRead += dataInputStream.read(buffer, 0, currentChunk.length - currentChunk.bytesRead);
                    break;
                }
                previousChunk.bytesRead += currentChunk.bytesRead;
            }
        }
        catch (IOException e) {
            System.err.println("Error:  File IO error in: Process Next Object Chunk");
            return;
        }

	currentChunk = previousChunk;
    }

    // Verified
    private void processNextMaterialChunk(Model3DS model, Material material, Chunk previousChunk)
    {
        byte buffer[] = null;

        currentChunk = new Chunk();

        try {
            while (previousChunk.bytesRead < previousChunk.length) {
                readChunkHeader(currentChunk);

                switch (currentChunk.id)
                {
		case MATNAME:
                    material.strName = getString(currentChunk);
                    buffer = new byte[currentChunk.length - currentChunk.bytesRead];
                    currentChunk.bytesRead += dataInputStream.read(buffer, 0, currentChunk.length - currentChunk.bytesRead);
		break;

		case MATDIFFUSE:
                    readColorChunk(material, currentChunk);
		break;

		case MATMAP:
                    processNextMaterialChunk(model, material, currentChunk);
		break;

		case MATMAPFILE:
                    material.strFile = getString(currentChunk);
                    buffer = new byte[currentChunk.length - currentChunk.bytesRead];
                    currentChunk.bytesRead += dataInputStream.read(buffer, 0, currentChunk.length - currentChunk.bytesRead);
		break;

                default:
                    buffer = new byte[currentChunk.length - currentChunk.bytesRead];
                    currentChunk.bytesRead += dataInputStream.read(buffer, 0, currentChunk.length - currentChunk.bytesRead);
                break;
                }

                previousChunk.bytesRead += currentChunk.bytesRead;
            }
        } catch (IOException e) {
            System.err.println("Error:  File IO error in: Process Next Material Chunk");
            return;
        }
	currentChunk = previousChunk;
    }

    // Verified
    private void readObjectMaterial(Model3DS model, Obj object, Chunk previousChunk)
    {
        String strMaterial = new String();
        byte buffer[] = null;

	strMaterial = getString(previousChunk);

	for (int i=0; i<model.getNumberOfMaterials(); i++) {
            if (strMaterial.equals(model.getMaterial(i).strName)) {
                object.materialID = i;
                if (model.getMaterial(i).strFile.length() > 0)
                    object.hasTexture = true;
                break;
            }
	}

        try {
            buffer = new byte[previousChunk.length - previousChunk.bytesRead];
            previousChunk.bytesRead += dataInputStream.read(buffer, 0, previousChunk.length - previousChunk.bytesRead);
        }
        catch (IOException e) {
            System.err.println("Error: File IO error in: Read Object Material");
            return;
        }
    }

    // Verified
    private void readUVCoordinates(Obj object, Chunk previousChunk)
    {
        try {
            object.numTexVertex = swap(dataInputStream.readShort());
            previousChunk.bytesRead += 2;

            object.texVerts = new Vec3[object.numTexVertex];
            for (int i=0; i<object.numTexVertex; i++) {
                object.texVerts[i] = new Vec3(swap(dataInputStream.readFloat()),
                                              swap(dataInputStream.readFloat()),
                                              0);

                previousChunk.bytesRead += 8;
            }
        }
        catch (IOException e) {
            System.err.println("Error: File IO error.");
            return;
        }
    }

    // Verified
    private void readVertices(Obj object, Chunk previousChunk)
    {
        try {
            object.numOfVerts = swap(dataInputStream.readShort());
            previousChunk.bytesRead += 2;

            object.verts = new Vec3[object.numOfVerts];
            for (int i=0; i<object.numOfVerts; i++) {
                object.verts[i] = new Vec3(swap(dataInputStream.readFloat()),
                                           swap(dataInputStream.readFloat()),
                                           swap(dataInputStream.readFloat()));

                previousChunk.bytesRead += 12;
            }
        }
        catch (IOException e) {
            System.err.println("Error: File IO error in: Read Vertices");
            return;
        }
    }

    // Verified
    private void readFaceList(Obj object, Chunk previousChunk)
    {
        short index = 0;

        try {
            object.numOfFaces = swap(dataInputStream.readShort());
            previousChunk.bytesRead += 2;

            object.faces = new Face[object.numOfFaces];
            for (int i=0; i<object.numOfFaces; i++) {
                object.faces[i] = new Face();
                object.faces[i].vertIndex[0] = swap(dataInputStream.readShort());
                object.faces[i].vertIndex[1] = swap(dataInputStream.readShort());
                object.faces[i].vertIndex[2] = swap(dataInputStream.readShort());

                // Read in the extra face info
                dataInputStream.readShort();

                // Account for how much data was read in (4 * 2bytes)
                previousChunk.bytesRead += 8;
            }
        }
        catch (IOException e) {
            System.err.println("Error: File IO error in: Read Face List");
            return;
        }
    }

    // Verified
    private void readColorChunk(Material material, Chunk chunk)
    {
	readChunkHeader(tempChunk);
        try {
            tempChunk.bytesRead += dataInputStream.read(material.color, 0, tempChunk.length - tempChunk.bytesRead);
        } catch (IOException e) {
            System.err.println("Error: File IO error in: Read Face List");
            return;
        }

	chunk.bytesRead += tempChunk.bytesRead;
    }

    // Verified
    private void computeNormals(Model3DS model)
    {
        Vec3 vVector1 = new Vec3();
        Vec3 vVector2 = new Vec3();
        Vec3 vNormal = new Vec3();
        Vec3 vPoly[] = new Vec3[3];
        int numObjs = model.getNumberOfObjects();

        for (int index=0; index<numObjs; index++) {
            Obj object = model.getObject(index);

            Vec3 normals[] = new Vec3[object.numOfFaces];
            Vec3 tempNormals[] = new Vec3[object.numOfFaces];
            object.normals = new Vec3[object.numOfVerts];

            for (int i=0; i<object.numOfFaces; i++) {
                vPoly[0] = new Vec3(object.verts[object.faces[i].vertIndex[0]]);
                vPoly[1] = new Vec3(object.verts[object.faces[i].vertIndex[1]]);
                vPoly[2] = new Vec3(object.verts[object.faces[i].vertIndex[2]]);

                vVector1 = vPoly[0];
                // Subtract vPoly[2] from vVector1
                vVector1.x -= vPoly[2].x;
                vVector1.y -= vPoly[2].y;
                vVector1.z -= vPoly[2].z;

                vVector2 = vPoly[2];
                // Subtract vPoly[1] from vVector2
                vVector2.x -= vPoly[1].x;
                vVector2.y -= vPoly[1].y;
                vVector2.z -= vPoly[1].z;

                normals[i] = vVector1;

                // Compute the cross product
                normals[i].x = normals[i].y*vVector1.z - normals[i].z*vVector1.y;
                normals[i].y = normals[i].z*vVector1.x - normals[i].x*vVector1.z;
                normals[i].z = normals[i].x*vVector1.y - normals[i].y*vVector1.x;

                tempNormals[i] = new Vec3(normals[i]);

                // Normalize
                float mag = (float)Math.sqrt(normals[i].x*normals[i].x + normals[i].y*normals[i].y + normals[i].z*normals[i].z);
                normals[i].x /= mag;
                normals[i].y /= mag;
                normals[i].z /= mag;
            }

            Vec3 vSum = new Vec3(0,0,0);
            Vec3 vZero = new Vec3(0,0,0);
            int shared=0;

            for (int i=0; i<object.numOfVerts; i++) {
                for (int j=0; j<object.numOfFaces; j++)  {
                    if (object.faces[j].vertIndex[0] == i ||
                        object.faces[j].vertIndex[1] == i ||
                        object.faces[j].vertIndex[2] == i)
                    {
                        // Add the vectors vSum and tempNormals
                        vSum.x += tempNormals[j].x;
                        vSum.y += tempNormals[j].y;
                        vSum.z += tempNormals[j].z;
                        shared++;
                    }
                }

                // Divide the vector vSum by -shared
                vSum.x /= -shared;
                vSum.y /= -shared;
                vSum.z /= -shared;

                object.normals[i] = new Vec3(vSum);

                // Normalize
                float mag = (float)Math.sqrt(object.normals[i].x*object.normals[i].x +
                                             object.normals[i].y*object.normals[i].y +
                                             object.normals[i].z*object.normals[i].z);
                object.normals[i].x /= mag;
                object.normals[i].y /= mag;
                object.normals[i].z /= mag;

                vSum = vZero;
                shared = 0;
            }
        }
    }

    private String getString(Chunk chunk)
    {
        int index = 0, bytesRead = 0;
        boolean read = true;
        byte buffer[] = new byte[256];

        try {
            while (read) {
                bytesRead += dataInputStream.read(buffer, index, 1);
                if (buffer[index] == 0x00)
                    break;
                index++;
            }
        }
        catch (IOException e) {
            System.err.println("Error: File IO error in: Get String");
            return "";
        }
        chunk.bytesRead += bytesRead;
        return new String(buffer).trim();
    }

    private static short swap(short value)
    {
        int b1 = value & 0xff;
        int b2 = (value >> 8) & 0xff;

        return (short) (b1 << 8 | b2 << 0);
    }

    private static int swap(int value)
    {
        int b1 = (value >>  0) & 0xff;
        int b2 = (value >>  8) & 0xff;
        int b3 = (value >> 16) & 0xff;
        int b4 = (value >> 24) & 0xff;

        return b1 << 24 | b2 << 16 | b3 << 8 | b4 << 0;
    }

    private static long swap(long value)
    {
        long b1 = (value >>  0) & 0xff;
        long b2 = (value >>  8) & 0xff;
        long b3 = (value >> 16) & 0xff;
        long b4 = (value >> 24) & 0xff;
        long b5 = (value >> 32) & 0xff;
        long b6 = (value >> 40) & 0xff;
        long b7 = (value >> 48) & 0xff;
        long b8 = (value >> 56) & 0xff;

        return b1 << 56 | b2 << 48 | b3 << 40 | b4 << 32 |
                b5 << 24 | b6 << 16 | b7 <<  8 | b8 <<  0;
    }

    private static float swap(float value)
    {
        int intValue = Float.floatToIntBits(value);
        intValue = swap(intValue);
        return Float.intBitsToFloat(intValue);
    }

    private static double swap(double value)
    {
        long longValue = Double.doubleToLongBits(value);
        longValue = swap(longValue);
        return Double.longBitsToDouble(longValue);
    }
}

