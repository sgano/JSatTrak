/*
 * iModel.java
 *
 * Created on February 27, 2008, 9:55 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.java.joglutils.model;

import net.java.joglutils.model.geometry.Model;

/**
 *
 * @author RodgersGB
 */
public interface iModel3DRenderer {
    public void render(Object context, Model model);
    public void debug(boolean value);
}
