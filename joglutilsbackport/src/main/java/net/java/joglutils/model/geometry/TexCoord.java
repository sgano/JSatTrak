/*
 * TexCoords.java
 *
 * Created on February 28, 2008, 10:29 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.java.joglutils.model.geometry;

/**
 *
 * @author RodgersGB
 */
public class TexCoord {
    public float u,v;
    
    public TexCoord() {
        u = v = 0;
    }
    
    public TexCoord(float u, float v) {
        this.u = u;
        this.v = v;
    }
}
