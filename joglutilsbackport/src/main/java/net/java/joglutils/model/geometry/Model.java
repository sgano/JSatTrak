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
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. GREG RODGERS,
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

package net.java.joglutils.model.geometry;

import java.util.Vector;

public class Model
{
    protected Vector<Material> materials = new Vector<Material>();
    protected Vector<Mesh> mesh = new Vector<Mesh>();
    protected String source;
    
    protected boolean renderModel = true;
    protected boolean centerModel = false;
    protected boolean renderModelBounds = false;
    protected boolean renderObjectBounds = false;
    protected boolean unitizeSize = true;
    protected boolean useTexture = true;
    protected boolean renderAsWireframe = false;
    protected boolean useLighting = true;
    
    /** Bounds of the model */
    protected Bounds bounds = new Bounds();
    /** Center point of the model */
    private Vec4 centerPoint = new Vec4(0.0f, 0.0f, 0.0f);

    // Constructor
    public Model(String source)
    {
        this.source = source;
    }

    // Add material
    public void addMaterial(Material mat)
    {
        materials.add(mat);
    }

    // Add a mesh
    public void addMesh(Mesh obj)
    {
        mesh.add(obj);
    }

    // Get material
    public Material getMaterial(int index)
    {
        return materials.get(index);
    }

    // Get a a mesh
    public Mesh getMesh(int index)
    {
        return mesh.get(index);
    }

    // Get the number of meshes
    public int getNumberOfMeshes()
    {
        return mesh.size();
    }

    // Get the number of materials
    public int getNumberOfMaterials()
    {
        return materials.size();
    }

    public String getSource() {
        return source;
    }

    public boolean isRenderModel() {
        return renderModel;
    }

    public void setRenderModel(boolean renderModel) {
        this.renderModel = renderModel;
    }

    public boolean isCentered() {
        return centerModel;
    }

    public void centerModelOnPosition(boolean centerModel) {
        this.centerModel = centerModel;
    }

    public boolean isRenderModelBounds() {
        return renderModelBounds;
    }

    public void setRenderModelBounds(boolean renderModelBounds) {
        this.renderModelBounds = renderModelBounds;
    }

    public boolean isRenderObjectBounds() {
        return renderObjectBounds;
    }

    public void setRenderObjectBounds(boolean renderObjectBounds) {
        this.renderObjectBounds = renderObjectBounds;
    }
    
    /** 
     * Returns the bounds of the model 
     *
     * @return Bounds of the model 
     */
    public Bounds getBounds() {
        return bounds;
    }
    
    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
    }
    
    /** 
     * Returns the center of the model bounds
     *
     * @return Center of the model
     */
    public Vec4 getCenterPoint() {
        return this.centerPoint;
    }

    public void setCenterPoint(Vec4 center) {
        this.centerPoint = center;
    }

    public boolean isUnitizeSize() {
        return unitizeSize;
    }

    public void setUnitizeSize(boolean unitizeSize) {
        this.unitizeSize = unitizeSize;
    }

    public boolean isUsingTexture() {
        return useTexture;
    }

    public void setUseTexture(boolean useTexture) {
        this.useTexture = useTexture;
    }

    public boolean isRenderingAsWireframe() {
        return renderAsWireframe;
    }

    public void setRenderAsWireframe(boolean renderAsWireframe) {
        this.renderAsWireframe = renderAsWireframe;
    }

    public boolean isUsingLighting() {
        return useLighting;
    }

    public void setUseLighting(boolean useLighting) {
        this.useLighting = useLighting;
    }
}
