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

package net.java.joglutils.model.loader;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import net.java.joglutils.model.ResourceRetriever;
import net.java.joglutils.model.geometry.Bounds;
import net.java.joglutils.model.geometry.Face;
import net.java.joglutils.model.geometry.Material;
import net.java.joglutils.model.geometry.Mesh;
import net.java.joglutils.model.geometry.Model;
import net.java.joglutils.model.geometry.TexCoord;
import net.java.joglutils.model.geometry.Vec4;

public class MaxLoader implements MaxConstants, iLoader {
// File reader
    private File file;
    private boolean loaded = false;
    private DataInputStream dataInputStream;

// Global chunks
    private Chunk currentChunk, tempChunk;

    /** Bounds of the model */
    private Bounds bounds = new Bounds();
    
    /** Center of the model */
    private Vec4 center = new Vec4(0.0f, 0.0f, 0.0f);     
    
    // Constructor
    public MaxLoader()
    {
        currentChunk = new Chunk();
        tempChunk = new Chunk();
    }

    public Model load(String source) {
        Model model = new Model(source);
        load(model);
        return model;
    }
    // Verified
    public boolean load(Model model)
    {
        try {
            InputStream stream = ResourceRetriever.getResourceAsInputStream(model.getSource());
            if (stream == null) {
                System.out.println("stream is null");
                return false;
            }
            
            dataInputStream = new DataInputStream(stream);
            if (dataInputStream == null) {
                System.out.println("dataInputStream is null");
                return false;
            } 
            
            readChunkHeader(currentChunk);
            
        } catch(IOException e) {
            System.out.println("IOException e:" + e);
            return false;
        } 

        
        if (currentChunk.id != TYPE_3DS_FILE) {
            System.err.println("Unable to load PRIMARY chuck from file!");
            return false;
        }

        processNextChunk(model, currentChunk);
	computeNormals(model);

        try {
            dataInputStream.close();

        } catch (IOException e) {
            System.err.println("Error:  File IO error in: Closing File");
            return false;
        }

        loaded = true;
        
        model.setBounds(this.bounds);
        model.setCenterPoint(this.center);

        return loaded;
    }

    /** 
     * Read the stream completely
     *
     * @param buffer 
     * @param offset
     * @param length
     * @throws IOException
     */
    private int readCompletely(byte buffer[], int offset, int length) throws IOException {
        dataInputStream.readFully(buffer, offset, length);
        return length;
    }

    // Verified
    void processNextChunk(Model model, Chunk previousChunk)
    {
	int version = 0;
	byte buffer[] = null;

	currentChunk = new Chunk();

        try {
            while (previousChunk.bytesRead < previousChunk.length) {
                readChunkHeader(currentChunk);

                switch (currentChunk.id) {
                    case TYPE_3DS_VERSION:
                        version = readInt(currentChunk);

                        if (version > 0x03)
                            System.err.println("This 3DS file is over version 3 so it may load incorrectly");
                    break;

                    case TYPE_MESH_DATA:
                        readChunkHeader(tempChunk);
                        buffer = new byte[tempChunk.length - tempChunk.bytesRead];
                        tempChunk.bytesRead += readCompletely(buffer, 0, buffer.length);
                        // TODO:DELETE tempChunk.bytesRead += dataInputStream.read(buffer, 0, tempChunk.length - tempChunk.bytesRead);
                        currentChunk.bytesRead += tempChunk.bytesRead;
                        processNextChunk(model, currentChunk);
                    break;

                    case TYPE_MATERIAL:
                        Material mat = new Material();
                        model.addMaterial(mat);
                        processNextMaterialChunk(model, mat, currentChunk);
                    break;

                    case TYPE_NAMED_OBJECT:
                        Mesh obj = new Mesh();
                        obj.name = readString(currentChunk);
                        model.addMesh(obj);
                        processNextObjectChunk(model, obj, currentChunk);
                    break;

                    case TYPE_KEY_FRAME:
                        processKeyFrame(currentChunk);
                    break;

                    default:
                        buffer = new byte[currentChunk.length - currentChunk.bytesRead];
                        currentChunk.bytesRead += readCompletely(buffer, 0, buffer.length);
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

    private void processKeyFrame(Chunk root) throws IOException {
        currentChunk = new Chunk();
        byte buffer[] = null;
        
        while (root.bytesRead < root.length) {
            readChunkHeader(currentChunk);
            
            switch (currentChunk.id) {
                default:
                    buffer = new byte[currentChunk.length - currentChunk.bytesRead];
                    currentChunk.bytesRead += readCompletely(buffer, 0, buffer.length);
                    break;
            }
            root.bytesRead += currentChunk.bytesRead;
        }
        currentChunk = root;
    }
    
    // Verified
    private void readChunkHeader(Chunk chunk) throws IOException
    {
        chunk.bytesRead = 0;
        chunk.id = this.readShort(chunk);
        chunk.length = this.readInt(chunk);
    }

    // Verified
    private void processNextObjectChunk(Model model, Mesh object, Chunk previousChunk)
    {
        byte buffer[] = null;
        int bytesread;

	currentChunk = new Chunk();

        try {
            while (previousChunk.bytesRead < previousChunk.length) {
                readChunkHeader(currentChunk);

                switch (currentChunk.id) {
                    case TYPE_TRIANGLE_OBJECT:
                        processNextObjectChunk(model, object, currentChunk);
                    break;

                    case TYPE_DIRECT_LIGHT:
                        buffer = new byte[currentChunk.length - currentChunk.bytesRead];
                        bytesread = readCompletely(buffer, 0, buffer.length);
                        currentChunk.bytesRead += bytesread;
                    break;
                    
                    case TYPE_POINT_LIST:
                        readVertices(object, currentChunk);
                    break;

                    case TYPE_FACE_LIST:
                        readFaceList(object, currentChunk);
                    break;

                    case TYPE_MAT_FACE_LIST:
                        readObjectMaterial(model, object, currentChunk);
                    break;

                    case TYPE_MAT_UV:
                        readUVCoordinates(object, currentChunk);
                    break;

                    default:
                        buffer = new byte[currentChunk.length - currentChunk.bytesRead];
                        bytesread = readCompletely(buffer, 0, buffer.length);
                        // TODO:DELETE currentChunk.bytesRead += dataInputStream.read(buffer, 0, currentChunk.length - currentChunk.bytesRead);
                        currentChunk.bytesRead += bytesread;
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
    private void processNextMaterialChunk(Model model, Material material, Chunk previousChunk)
    {
        byte buffer[] = null;

        currentChunk = new Chunk();

        try {
            while (previousChunk.bytesRead < previousChunk.length) {
                readChunkHeader(currentChunk);

                switch (currentChunk.id)
                {
		case TYPE_MATERIAL_NAME:
                    material.strName = readString(currentChunk);
                    buffer = new byte[currentChunk.length - currentChunk.bytesRead];
                    currentChunk.bytesRead += readCompletely(buffer, 0, buffer.length);
		break;

                case TYPE_MAT_AMBIENT:
                    material.ambientColor = readColor(currentChunk);
                    break;
                    
		case TYPE_MAT_DIFFUSE:
                    material.diffuseColor = readColor(currentChunk);
		break;
                
                case TYPE_MAT_SPECULAR:
                    material.specularColor = readColor(currentChunk);
                    break;

                case TYPE_MAT_SHININESS:
                    material.shininess = 1.0f+127.0f*readPercentage(currentChunk);
                    break;
                    
                case TYPE_MAT_SHININESS2:
                    material.shininess2 = 1.0f+127.0f*readPercentage(currentChunk);
                    break;
                    
                case TYPE_MAT_TRANSPARENCY:
                    material.transparency = readPercentage(currentChunk);
                    break;
                    
                case TYPE_MAT_2_SIDED:
                    buffer = new byte[currentChunk.length - currentChunk.bytesRead];
                    currentChunk.bytesRead += readCompletely(buffer, 0, buffer.length);
                    break;
                    
                case TYPE_MAT_XPFALL:
                    float xpf = readPercentage(currentChunk);
                    break;
                    
                case TYPE_MAT_REFBLUR:
                    float ref = readPercentage(currentChunk);
                    break;
                    
                case TYPE_MAT_SELF_ILPCT:
                    float il = readPercentage(currentChunk);
                    break;
                    
                case TYPE_MAT_SHADING:
                    int shading = readShort(currentChunk);
                    // Some kind of code for the rendering type: 1, 3, 4, etc.
                    break;
                    
		case TYPE_MAT_TEXMAP:
                    processNextMaterialChunk(model, material, currentChunk);
		break;

		case TYPE_MAT_MAPNAME:
                    material.strFile = readString(currentChunk);
                    buffer = new byte[currentChunk.length - currentChunk.bytesRead];
                    currentChunk.bytesRead += readCompletely(buffer, 0, buffer.length);
		break;

                default:
                    buffer = new byte[currentChunk.length - currentChunk.bytesRead];
                    currentChunk.bytesRead += readCompletely(buffer, 0, buffer.length);
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

//    // Verified
//    private void readObjectMaterial(Model model, Mesh object, Chunk previousChunk) throws IOException
//    {
//        String strMaterial = new String();
//        byte buffer[] = null;
//
//	strMaterial = readString(previousChunk);
//
//	for (int i=0; i<model.getNumberOfMaterials(); i++) {
//            if (strMaterial.equals(model.getMaterial(i).strName)) {
//                object.materialID = i;
//                if (model.getMaterial(i).strFile != null)
//                    object.hasTexture = true;
//                break;
//            }
//	}
//
//        try {
//            // Read the faces that are associated with this material and associated the
//            // material id with each of the faces defined (per 3DS specification)
//            int numOfFaces = myReadShort(dataInputStream);
//            previousChunk.bytesRead += 2;
//            
//            // Now loop over the number of faces to get their material ids and save this info
//            for (int i=0; i<numOfFaces; i++) {
//                int faceId = myReadShort(dataInputStream);
//                object.faces[faceId].materialID = object.materialID;
//                previousChunk.bytesRead += 2;
//            }
//            
//            buffer = new byte[previousChunk.length - previousChunk.bytesRead];
//            previousChunk.bytesRead += readCompletely(buffer, 0, buffer.length);
//            
//            // TODO:DELETE buffer = new byte[previousChunk.length - previousChunk.bytesRead];
//            // TODO:DELETE previousChunk.bytesRead += dataInputStream.read(buffer, 0, previousChunk.length - previousChunk.bytesRead);
//        }
//        catch (IOException e) {
//            System.err.println("Error: File IO error in: Read Object Material");
//            return;
//        }
//    }
    
    // mine
    private void readObjectMaterial(Model model, Mesh mesh, Chunk root) throws IOException {
        String strMaterial = null;
        byte buffer[] = null;
        
        strMaterial = readString(root);
        
        for (int i=0; i<model.getNumberOfMaterials(); i++) {
            if (strMaterial.equals(model.getMaterial(i).strName)) {
                mesh.materialID = i;
                Material mat = model.getMaterial(i);
                if (mat.strFile != null)
                    mesh.hasTexture = true;
                break;
            }
        }
        
        try {
            // Read the faces that are associated with this material and associated the
            // material id with each of the faces defined (per 3DS specification)
            int numOfFaces = this.readShort(root);
            
            // Now loop over the number of faces to get their material ids and save this info
            for (int i=0; i<numOfFaces; i++) {
                int faceId = readShort(root);
                mesh.faces[faceId].materialID = mesh.materialID;
            }
        }
        catch (IOException e) {
            System.err.println("Error: File IO error in: Read Object Material");
            return;
        }
    }

//    // Verified
//    private void readUVCoordinates(Mesh object, Chunk previousChunk)
//    {
//        try {
//            object.numTexCoords = myReadShort(dataInputStream);
//            // TODO:DELETE object.numTexVertex = swap(dataInputStream.readShort());
//            
//            previousChunk.bytesRead += 2;
//
//            object.texCoord = new TexCoord[object.numTexCoords];
//            for (int i=0; i<object.numTexCoords; i++) {
//                object.texCoord[i] = new TexCoord((float) mySwap(dataInputStream.readInt()),
//                                                   (float) mySwap(dataInputStream.readInt()));
//                // TODO:DELETE object.texVerts[i] = new Vec3(swap(dataInputStream.readFloat()),
//                //                                           swap(dataInputStream.readFloat()),
//                //                                           0);
//
//                previousChunk.bytesRead += 8;
//            }
//        }
//        catch (IOException e) {
//            System.err.println("Error: File IO error.");
//            return;
//        }
//    }
    
    // mine
    private void readUVCoordinates(Mesh mesh, Chunk root) throws IOException {
        mesh.numTexCoords = readShort(root);
        
        mesh.texCoords = new TexCoord[mesh.numTexCoords];
        for (int i=0; i<mesh.numTexCoords; i++)
            mesh.texCoords[i] = readPoint(root);
    }

//    // Verified
//    private void readVertices(Mesh object, Chunk previousChunk)
//    {
//        try {
//            object.numOfVerts = myReadShort(dataInputStream);
//            if (object.numOfVerts < 0) {
//                throw new java.lang.RuntimeException("Number of vertices is Negative: " +object.numOfVerts);
//            }            
//            // TODO:DELETE object.numOfVerts = swap(dataInputStream.readShort());
//            previousChunk.bytesRead += 2;
//
//            object.verts = new Vec3[object.numOfVerts];
//            // Create a Bounds instance for this object
//            object.bounds = new Bounds();
//            for (int i=0; i<object.numOfVerts; i++) {          
//                object.verts[i] = new Vec3(mySwap(dataInputStream.readInt()),
//                                           mySwap(dataInputStream.readInt()), 
//                                           mySwap(dataInputStream.readInt()));                      
//                // TODO:DELETE object.verts[i] = new Vec3(swap(dataInputStream.readFloat()),
//                //                                        swap(dataInputStream.readFloat()),
//                //                                        swap(dataInputStream.readFloat()));
//
//                // Calculate the bounds for this current object
//                object.bounds.calc(object.verts[i]);
//                
//                // Calculate the bounds for the entire model
//                bounds.calc(object.verts[i]);                
//                
//                previousChunk.bytesRead += 12;
//            }
//            
//            // Calculate the center of the model
//            center.x = 0.5f * (bounds.max.x + bounds.min.x);
//            center.y = 0.5f * (bounds.max.y + bounds.min.y);
//            center.z = 0.5f * (bounds.max.z + bounds.min.z);              
//        }
//        catch (IOException e) {
//            System.err.println("Error: File IO error in: Read Vertices");
//            return;
//        }
//    }
    
    // mine
    private void readVertices(Mesh object, Chunk previousChunk)
    {
        try {
            object.numOfVerts = readShort(previousChunk);
            if (object.numOfVerts < 0) {
                throw new java.lang.RuntimeException("Number of vertices is Negative: " +object.numOfVerts);
            }            

            object.vertices = new Vec4[object.numOfVerts];
            // Create a Bounds instance for this object
            //object.bounds = new Bounds();
            for (int i=0; i<object.numOfVerts; i++) {
                object.vertices[i] = readVertex(previousChunk); 

                // Calculate the bounds for this current object
                object.bounds.calc(object.vertices[i]);
                
                // Calculate the bounds for the entire model
                bounds.calc(object.vertices[i]);
            }
            
            // Calculate the center of the model
            center.x = 0.5f * (bounds.max.x + bounds.min.x);
            center.y = 0.5f * (bounds.max.y + bounds.min.y);
            center.z = 0.5f * (bounds.max.z + bounds.min.z);              
        }
        catch (IOException e) {
            System.err.println("Error: File IO error in: Read Vertices");
            return;
        }
    }

//    // Verified
//    private void readFaceList(Mesh object, Chunk previousChunk)
//    {
//        short index = 0;
//
//            object.numOfFaces = myReadShort(dataInputStream);
//            // TODO:DELETE object.numOfFaces = swap(dataInputStream.readShort());
//            
//            previousChunk.bytesRead += 2;
//
//            object.faces = new Face[object.numOfFaces];
//            for (int i=0; i<object.numOfFaces; i++) {
//                object.faces[i] = new Face();
//                object.faces[i].vertIndex[0] = myReadShort(dataInputStream);
//                // TODO:DELETE object.faces[i].vertIndex[0] = swap(dataInputStream.readShort());
//                object.faces[i].vertIndex[1] = myReadShort(dataInputStream);
//                // TODO:DELETE object.faces[i].vertIndex[1] = swap(dataInputStream.readShort());
//                object.faces[i].vertIndex[2] = myReadShort(dataInputStream);
//                // TODO:DELETE object.faces[i].vertIndex[2] = swap(dataInputStream.readShort());
// 
//                object.faces[i].coordIndex[0] = object.faces[i].vertIndex[0];                
//                object.faces[i].coordIndex[1] = object.faces[i].vertIndex[1];                 
//                object.faces[i].coordIndex[2] = object.faces[i].vertIndex[2];                 
//                
//                // Read in the extra face info
//                myReadShort(dataInputStream);
//                // TODO:DELETE dataInputStream.readShort();
//
//                // Account for how much data was read in (4 * 2bytes)
//                previousChunk.bytesRead += 8;
//            }
//    }
    
    private void readFaceList(Mesh object, Chunk root) throws IOException {
        short index = 0;
        
        object.numOfFaces = readShort(root);
        
        object.faces = new Face[object.numOfFaces];
        for (int i=0; i<object.numOfFaces; i++) {
            object.faces[i] = new Face(3);
            object.faces[i].vertIndex[0] = readShort(root);
            object.faces[i].vertIndex[1] = readShort(root);
            object.faces[i].vertIndex[2] = readShort(root);
            
            object.faces[i].coordIndex[0] = object.faces[i].vertIndex[0];
            object.faces[i].coordIndex[1] = object.faces[i].vertIndex[1];
            object.faces[i].coordIndex[2] = object.faces[i].vertIndex[2];
            
            
            // Read in the extra face info
            readShort(root); // Flags (?)
        }
    }
    
    /**
     * Reads a color from the input file.
     */
    protected Color readColor(Chunk c) throws IOException {
        Color color = null;
        
        readChunkHeader(tempChunk);
        switch(tempChunk.id) {
            case TYPE_COLOR_F:
            case TYPE_COLOR_LIN_F:
                color = new Color(readFloat(c),readFloat(c),readFloat(c));
                break;
            case TYPE_COLOR_I:
            case TYPE_COLOR_LIN_I:
                color = new Color(readUnsignedByte(c),readUnsignedByte(c),readUnsignedByte(c));
        }
        
        c.bytesRead += tempChunk.bytesRead;
        
        return color;
    }
    
    // Verified
    private void computeNormals(Model model)
    {
        Vec4 vVector1 = new Vec4();
        Vec4 vVector2 = new Vec4();
        //Vec4 vNormal = new Vec4();
        Vec4 vPoly[] = new Vec4[3];
        int numObjs = model.getNumberOfMeshes();

        for (int index=0; index<numObjs; index++) {
            Mesh object = model.getMesh(index);

            //Vec4 facenormals[] = new Vec4[object.numOfFaces];
            Vec4 tempNormals[] = new Vec4[object.numOfFaces];
            object.normals = new Vec4[object.numOfVerts];

            for (int i=0; i<object.numOfFaces; i++) {
                vPoly[0] = new Vec4(object.vertices[object.faces[i].vertIndex[0]]);
                vPoly[1] = new Vec4(object.vertices[object.faces[i].vertIndex[1]]);
                vPoly[2] = new Vec4(object.vertices[object.faces[i].vertIndex[2]]);

                vVector1 = vPoly[1];
                vVector2 = vPoly[2];
                vVector1.x -= vPoly[0].x;
                vVector1.y -= vPoly[0].y;
                vVector1.z -= vPoly[0].z;
                vVector2.x -= vPoly[0].x;
                vVector2.y -= vPoly[0].y;
                vVector2.z -= vPoly[0].z; 
                
                tempNormals[i] = new Vec4( vVector1.y*vVector2.z - vVector1.z*vVector2.y,
                                           vVector1.z*vVector2.x - vVector1.x*vVector2.z,
                                           vVector1.x*vVector2.y - vVector1.y*vVector2.x );
            }

            float vSumx = 0.0f;
            float vSumy = 0.0f;
            float vSumz = 0.0f;
            int shared=0;

            for (int i=0; i<object.numOfVerts; i++) {
                vSumx = 0.0f;
                vSumy = 0.0f;
                vSumz = 0.0f;
                shared=0;
                
                for (int j=0; j<object.numOfFaces; j++) {
                    if (object.faces[j].vertIndex[0] == i ||
                        object.faces[j].vertIndex[1] == i ||
                        object.faces[j].vertIndex[2] == i)
                    {
                        //////// temp ////////////
                        object.faces[j].normalIndex[0] = i;
                        object.faces[j].normalIndex[1] = i;
                        object.faces[j].normalIndex[2] = i;
                        //////////////////////////
                        
                        
                        // Add the vectors vSum and tempNormals
                        vSumx += tempNormals[j].x;
                        vSumy += tempNormals[j].y;
                        vSumz += tempNormals[j].z;
                        shared++;
                    }
                }

                // Divide the vector vSum by -shared
                vSumx /= -shared;
                vSumy /= -shared;
                vSumz /= -shared;

                object.normals[i] = new Vec4(vSumx, vSumy, vSumz);

                // Normalize
                float mag = (float)Math.sqrt(object.normals[i].x*object.normals[i].x +
                                             object.normals[i].y*object.normals[i].y +
                                             object.normals[i].z*object.normals[i].z);
                
                object.normals[i].x /= mag;
                object.normals[i].y /= mag;
                object.normals[i].z /= mag;
            }
        }
    }
    
    /**
     * Reads a String value from the input file.
     */
    protected String readString(Chunk c) throws IOException {
        DataInputStream in = dataInputStream;
        StringBuffer sb = new StringBuffer("");
        c.bytesRead++;
        byte ch = in.readByte();
        while(ch != (byte)0) {
            sb.append((char)ch);
            c.bytesRead++;
            ch = in.readByte();
        }
        return sb.toString();
    }

    /**
     * Reads a percentage value from the input file.  Returns as a float
     * between 0 and 1.
     */
    protected float readPercentage(Chunk c) throws IOException {
        float value = 0;
        readChunkHeader(tempChunk);
        
        if (tempChunk.id == TYPE_PERCENT_I) {
            value = (float)readShort(c) / 100.0f;
        } else if(tempChunk.id == TYPE_PERCENT_F) {
            value = readFloat(c);
        }
        
        c.bytesRead += tempChunk.bytesRead;
        
        return value;
    }
    
    private float readFloat(Chunk c) throws IOException {
        return Float.intBitsToFloat(readInt(c));
    }
    
    /**
     * Reads an unsigned byte (8-bit) value from the input file.
     */
    protected int readUnsignedByte(Chunk c) throws IOException {
        c.bytesRead++;
        return dataInputStream.readUnsignedByte();
    }
    
    protected int readByte(Chunk c) throws IOException {
        c.bytesRead++;
        return dataInputStream.read() & 0xff;
    }
    
    /**
     * Reads an int (32-bit) value from the input file.
     */
    private int readInt(Chunk c) throws IOException {
        DataInputStream in = dataInputStream;
        c.bytesRead += 4;
        return (int)(in.read() + (in.read() << 8) + (in.read() << 16) + (in.read() << 24));
    }

    /**
     * Reads a short (16-bit) value from the input file.
     */
    protected int readShort(Chunk c) throws IOException {
//        c.bytesRead += 2;
        int b1 = readByte(c);
        int b2 = readByte(c) << 8;
        
        return b1 | b2;
//        return (short)(dataInputStream.read()+(dataInputStream.read() << 8));
    }
    
    private Vec4 readVertex(Chunk c) throws IOException {
        return new Vec4(readFloat(c), readFloat(c), readFloat(c));
    }
    
    private TexCoord readPoint(Chunk c) throws IOException {
        return new TexCoord(readFloat(c), readFloat(c));
    }
    
//    private static short swap(short value)
//    {
//        int b1 = value & 0xff;
//        int b2 = (value >> 8) & 0xff;
//
//        return (short) (b1 << 8 | b2 << 0);
//    }
//    
//    private static int getNextByte(DataInputStream stream) {
//        try {
//            return stream.read() & 0xff;
//        } catch (Exception e) {
//            return 0;
//        }
//    }
//    
//    private static int myReadShort(DataInputStream stream) {
//        int b1 = getNextByte(stream);
//        int b2 = getNextByte(stream) << 8;
//        
//        return b1 | b2;
//    }    
//
//    private static int swap(int value)
//    {
//        int b1 = (value >>  0) & 0xff;
//        int b2 = (value >>  8) & 0xff;
//        int b3 = (value >> 16) & 0xff;
//        int b4 = (value >> 24) & 0xff;
//
//        return b1 << 24 | b2 << 16 | b3 << 8 | b4 << 0;
//    }
//
//    private static long swap(long value)
//    {
//        long b1 = (value >>  0) & 0xff;
//        long b2 = (value >>  8) & 0xff;
//        long b3 = (value >> 16) & 0xff;
//        long b4 = (value >> 24) & 0xff;
//        long b5 = (value >> 32) & 0xff;
//        long b6 = (value >> 40) & 0xff;
//        long b7 = (value >> 48) & 0xff;
//        long b8 = (value >> 56) & 0xff;
//
//        return b1 << 56 | b2 << 48 | b3 << 40 | b4 << 32 |
//                b5 << 24 | b6 << 16 | b7 <<  8 | b8 <<  0;
//    }
//
//    private static float swap(float value)
//    {
//        int intValue = Float.floatToIntBits(value);
//        intValue = swap(intValue);
//        return Float.intBitsToFloat(intValue);
//    }
//    
//    private static float mySwap(int value) {   
//        int intValue = swap(value);
//        return Float.intBitsToFloat(intValue);
//    }   
//    
//    private static double swap(double value)
//    {
//        long longValue = Double.doubleToLongBits(value);
//        longValue = swap(longValue);
//        return Double.longBitsToDouble(longValue);
//    }
    
    private class Chunk {
        public int id = 0;
        public int length = 0;
        public int bytesRead = 0;
    }
}
