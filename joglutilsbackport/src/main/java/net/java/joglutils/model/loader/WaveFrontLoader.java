/*
 * myWaveFrontLoader.java
 *
 * Created on March 16, 2008, 8:57 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.java.joglutils.model.loader;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;
import net.java.joglutils.model.ModelLoadException;
import net.java.joglutils.model.ResourceRetriever;
import net.java.joglutils.model.geometry.Bounds;
import net.java.joglutils.model.geometry.Face;
import net.java.joglutils.model.geometry.Material;
import net.java.joglutils.model.geometry.Mesh;
import net.java.joglutils.model.geometry.Model;
import net.java.joglutils.model.geometry.TexCoord;
import net.java.joglutils.model.geometry.Vec4;

/**
 *
 * @author RodgersGB
 */
public class WaveFrontLoader implements iLoader {
    public static final String VERTEX_DATA = "v ";
    public static final String NORMAL_DATA = "vn ";
    public static final String TEXTURE_DATA = "vt ";
    public static final String FACE_DATA = "f ";
    public static final String SMOOTHING_GROUP = "s ";
    public static final String GROUP = "g ";
    public static final String OBJECT = "o ";
    public static final String COMMENT = "#";
    public static final String EMPTY = "";
    
    int vertexTotal = 0;
    int textureTotal = 0;
    int normalTotal = 0;
    
    private DataInputStream dataInputStream;
    // the model
    private Model model = null;
    /** Bounds of the model */
    private Bounds bounds = new Bounds();
    /** Center of the model */
    private Vec4 center = new Vec4(0.0f, 0.0f, 0.0f);
    private String baseDir = null;
    
    /** Creates a new instance of myWaveFrontLoader */
    public WaveFrontLoader() {
    }

    int numComments = 0;
    public Model load(String path) throws ModelLoadException {
        model = new Model(path);
        Mesh mesh = null;
        
        baseDir = "";
        String tokens[] = path.split("/");
        for(int i = 0; i < tokens.length - 1; i++) {
            baseDir += tokens[i] + "/";
        }
        
        InputStream stream = null;
        try {
            stream = ResourceRetriever.getResourceAsInputStream(model.getSource());
            if (stream == null) {
                throw new ModelLoadException("Stream is null");
            }
        } catch(IOException e) {
            throw new ModelLoadException("Caught IO exception: " + e);
        }
        
        try {
            // Open a file handle and read the models data
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String  line = null;
            while((line = br.readLine()) != null) {
                if (lineIs(COMMENT, line)) {
                    // ignore comments
                    numComments++;
                    continue;
                }
                
                if (line.isEmpty()) {
                    // igonore empty lines
                    continue;
                }
                
                if (lineIs(GROUP, line)) {
                    if (mesh == null) {
                        mesh = new Mesh();
                    }
                    
                    mesh.name = parseName(line);
                }
                
                if (lineIs(OBJECT, line)) {
                    
                }
                
                if (lineIs(VERTEX_DATA, line)) {
                    if (mesh == null)
                        mesh = new Mesh();
                    
                    mesh.vertices = getPoints(VERTEX_DATA, line, br);
                    mesh.numOfVerts = mesh.vertices.length;
                }
                
                if (lineIs(TEXTURE_DATA, line)) {
                    if (mesh == null)
                        mesh = new Mesh();
                    
                    mesh.texCoords = getTexCoords(TEXTURE_DATA, line, br);
                    mesh.hasTexture = true;
                    mesh.numTexCoords = mesh.texCoords.length;
                }
                
                if (lineIs(NORMAL_DATA, line)) {
                    if (mesh == null)
                        mesh = new Mesh();
                    
                    mesh.normals = getPoints(NORMAL_DATA, line, br);
                }
                
                if (lineIs(FACE_DATA, line)) {
                    if (mesh == null)
                        mesh = new Mesh();
                    
                    mesh.faces = getFaces(line, mesh, br);
                    mesh.numOfFaces = mesh.faces.length;
                    
                    model.addMesh(mesh);
                    mesh = new Mesh();
                }
                
                if (lineIs("mtllib ", line)){
                    processMaterialLib(line);
                }
                
                if (lineIs("usemtl ", line)) {
                    processMaterialType(line, mesh);
                }
            }
        }
        catch (IOException e) {
            throw new ModelLoadException("Failed to find or read OBJ: " + stream);
        }
        model.addMesh(mesh);
        mesh = null;
        
        System.out.println(this.bounds.toString());
        model.setBounds(this.bounds);
        model.setCenterPoint(this.center);
        
        return model;
    }
    
    private boolean lineIs(String type, String line) {
        return line.startsWith(type);
    }
    
    private Vec4[] getPoints(String prefix, String currLine, BufferedReader br) throws IOException {
        Vector<Vec4> points = new Vector<Vec4>();
        boolean isVertices = prefix.equals(VERTEX_DATA);
        
        // we've already read in the first line (currLine)
        // so go ahead and parse it
        points.add(parsePoint(currLine));
        
        // parse through the rest of the points
        String line = null;
        while((line = br.readLine()) != null) {
            if (!lineIs(prefix, line))
                break;
            
            Vec4 point = parsePoint(line);
            if (isVertices) {
                // Calculate the bounds for the entire model
                bounds.calc(point);
            }
            points.add(point);
        }
        
        if (isVertices) {
            // Calculate the center of the model
            center.x = 0.5f * (bounds.max.x + bounds.min.x);
            center.y = 0.5f * (bounds.max.y + bounds.min.y);
            center.z = 0.5f * (bounds.max.z + bounds.min.z);
        }
        
        // return the points
        Vec4 values[] = new Vec4[points.size()];
        return points.toArray(values);
    }
    
    private TexCoord[] getTexCoords(String prefix, String currLine, BufferedReader br) throws IOException {
        Vector<TexCoord> texCoords = new Vector<TexCoord>();
        
        String s[] = currLine.split("\\s+");
        TexCoord texCoord = new TexCoord();
        texCoord.u = Float.parseFloat(s[1]);
        texCoord.v = Float.parseFloat(s[2]);
            
        texCoords.add(texCoord);
        
        // parse through the rest of the points
        String line = null;
        while((line = br.readLine()) != null) {
            if (!lineIs(prefix, line))
                break;
            
            s = line.split("\\s+");
        
            texCoord = new TexCoord();
            texCoord.u = Float.parseFloat(s[1]);
            texCoord.v = Float.parseFloat(s[2]);
            
            texCoords.add(texCoord);
        }
        
        // return the texture coordinates
        TexCoord values[] = new TexCoord[texCoords.size()];
        return texCoords.toArray(values);
    }
    
    private Face[] getFaces(String currLine, Mesh mesh, BufferedReader br) throws IOException {
        Vector<Face> faces = new Vector<Face>();
        
        faces.add(parseFace(currLine));
        
        // parse through the rest of the faces
        String line = null;
        while((line = br.readLine()) != null) {
            if (lineIs(SMOOTHING_GROUP, line)) {
                continue;
            }
            else if (lineIs("usemtl ", line)) {
                processMaterialType(line, mesh);
            }
            
            else if (lineIs(FACE_DATA, line)) {
                faces.add(parseFace(line));
            }
            
            else
                break;
        }        
        
        // return the faces
        Face values[] = new Face[faces.size()];
        return faces.toArray(values);
    }
    
    private Face parseFace(String line) {
        String s[] = line.split("\\s+");
        if (line.contains("//")) { // Pattern is present if obj has no texture
            for (int loop=1; loop < s.length; loop++) {
                s[loop] = s[loop].replaceAll("//","/-1/"); //insert -1 for missing vt data
            }
        }
        
        int vdata[] = new int[s.length-1];
        int vtdata[] = new int[s.length-1];
        int vndata[] = new int[s.length-1];
        Face face = new Face(s.length - 1);
        
        for (int loop = 1; loop < s.length; loop++) {
            String s1 = s[loop];
            String[] temp = s1.split("/");
            
            if (temp.length > 0) { // we have vertex data
                if (Integer.valueOf(temp[0]) < 0) {
                    //TODO handle relative vertex data
                }
                else {
                    face.vertIndex[loop-1] = Integer.valueOf(temp[0]) - 1 - this.vertexTotal;
                    //System.out.println("found vertex index: " + face.vertIndex[loop-1]);
                }
            }
            
            if (temp.length > 1) { // we have texture data
                if(Integer.valueOf(temp[1]) < 0) {
                    face.coordIndex[loop - 1] = 0;
                }
                else {
                    face.coordIndex[loop - 1] = Integer.valueOf(temp[1]) - 1 - this.textureTotal;
                    //System.out.println("found texture index: " + face.coordIndex[loop-1]);
                }
            }
            
            if (temp.length > 2) { // we have normal data
                face.normalIndex[loop-1] = Integer.valueOf(temp[2]) - 1 - this.normalTotal;
                //System.out.println("found normal index: " + face.normalIndex[loop-1]);
            }
        }
        
        return face;
    }
    
    private Vec4 parsePoint(String line) {
        Vec4 point = new Vec4();
        
        final String s[] = line.split("\\s+");
        
        point.x = Float.parseFloat(s[1]);
        point.y = Float.parseFloat(s[2]);
        point.z = Float.parseFloat(s[3]);
        
        return point;
    }
    
    private String parseName(String line) {
        String name;
        
        final String s[] = line.split("\\s+");
        
        name = s[1];
        
        return name;
    }
    
    private void processMaterialLib(String mtlData) {
        String s[] = mtlData.split("\\s+");
        
        Material mat = new Material();
        InputStream stream = null;
        try {
            stream = ResourceRetriever.getResourceAsInputStream(baseDir + s[1]);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        if(stream == null) {
            try {
                stream = new FileInputStream(baseDir + s[1]);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
                return;
            }
        }
        loadMaterialFile(stream);
    }
    
    private void processMaterialType(String line, Mesh mesh) {
        String s[] = line.split("\\s+");
        
        int materialID = -1;
        boolean hasTexture = false;
        
        for(int i = 0; i < model.getNumberOfMaterials(); i++){
            Material mat = model.getMaterial(i);
            
            if(mat.strName.equals(s[1])){
                materialID = i;
                if(mat.strFile != null)
                    hasTexture = true;
                else
                    hasTexture = false;
                break;
            }
        }
        
        if(materialID != -1)
            mesh.materialID = materialID;
    }
    
    public Material loadMaterialFile(InputStream stream) {
        Material mat = null;
        int texId = 0;
        
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            String line;
            
            while((line = br.readLine()) != null){
                
                String parts[] = line.trim().split("\\s+");
                
                if(parts[0].equals("newmtl")){
                    if(mat != null)
                        model.addMaterial(mat);
                    
                    mat = new Material();
                    mat.strName = parts[1];
                    mat.textureId = texId++;
                    
                } else if(parts[0].equals("Ks"))
                    mat.specularColor = parseColor(line);
                
                else if(parts[0].equals("Ns")) {
                    if (parts.length > 1)
                        mat.shininess = Float.valueOf(parts[1]);
                }
                else if(parts[0].equals("d"))
                    ;
                else if(parts[0].equals("illum"))
                    ;
                else if(parts[0].equals("Ka"))
                    mat.ambientColor = parseColor(line);
                else if(parts[0].equals("Kd"))
                    mat.diffuseColor = parseColor(line);
                else if(parts[0].equals("map_Kd")) {
                    if (parts.length > 1)
                        mat.strFile = /*baseDir + */parts[1];
                }
                
                else if(parts[0].equals("map_Ka")) {
                    if (parts.length > 1)
                        mat.strFile = /*baseDir + */parts[1];
                }
            }
            
            br.close();
            model.addMaterial(mat);
            
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return mat;
    }
    
    private Color parseColor(String line) {
        String parts[] = line.trim().split("\\s+");
        
        Color color = new Color(Float.valueOf(parts[1]),
                Float.valueOf(parts[2]),Float.valueOf(parts[3]));
        
        return color;
    }
    
    public static void main(String[] args) {
        WaveFrontLoader loader = new WaveFrontLoader();
        try {
            loader.load("C:\\Documents and Settings\\RodgersGB\\My Documents\\Projects\\JOGLUTILS\\src\\net\\java\\joglutils\\examples\\models\\obj\\penguin.obj");
        } catch (ModelLoadException ex) {
            ex.printStackTrace();
        }
    }
}
