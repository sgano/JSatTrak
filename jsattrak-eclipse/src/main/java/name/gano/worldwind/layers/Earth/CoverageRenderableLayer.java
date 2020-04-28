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
package name.gano.worldwind.layers.Earth;

import gov.nasa.worldwind.layers.RenderableLayer;
import jsattrak.coverage.CoverageAnalyzer;
import name.gano.worldwind.geom.CoverageDataGeom;
import name.gano.worldwind.geom.CoverageJoglColorBar;

/**
 *
 * @author Shawn Gano
 */
public class CoverageRenderableLayer extends RenderableLayer
{

    CoverageJoglColorBar cb;
    CoverageDataGeom dataGeom;
    
    public CoverageRenderableLayer(CoverageAnalyzer ca)
    {
        super();
        // create colorbar object
        cb = new CoverageJoglColorBar(ca);
        
        // add geometry to layer
        dataGeom = new CoverageDataGeom(ca);
        this.addRenderable(dataGeom);
        
        if(ca != null)
        {
            if(ca.isShowColorBar())
            {
                this.addRenderable(cb);
            }
        }
    }
    
    
    public void updateNewCoverageObject(CoverageAnalyzer ca)
    {
        this.removeAllRenderables();
        
        cb = new CoverageJoglColorBar(ca);
        
        // add geometry to layer
        dataGeom = new CoverageDataGeom(ca);
        this.addRenderable(dataGeom);
        
        if(ca != null)
        {
            if(ca.isShowColorBar())
            {
                this.addRenderable(cb);
            }
        }
    } // updateNewCoverageObject
    
    @Override
    public String toString()
    {
        return "Coverage Data Layer";
    }
    
    
    
}

