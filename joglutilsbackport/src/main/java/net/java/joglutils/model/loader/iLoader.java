/*
 * iLoader.java
 *
 * Created on February 27, 2008, 10:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package net.java.joglutils.model.loader;

import net.java.joglutils.model.ModelLoadException;
import net.java.joglutils.model.geometry.Model;

/**
 *
 * @author RodgersGB
 */
public interface iLoader {
    public Model load(String path) throws ModelLoadException;
}
