/*
 * SunTerminatorPolyLineTimeDep.java
 * 
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
 * Created 20 June 2009
 */

package name.gano.worldwind.objects;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Polyline;
import java.awt.Color;
import java.util.Vector;
import javax.media.opengl.GL;
import jsattrak.gui.J2dEarthLabel2;
import name.gano.astro.bodies.Sun;

/**
 * A polyline object that is timedependant, representing the Sun's terminator
 * @author Shawn Gano
 */
public class SunTerminatorPolyLineTimeDep extends Polyline implements TimeDepRenderable
{
    private Sun sun;
    private int numPoints = 4;

    public SunTerminatorPolyLineTimeDep(Sun sun)
    {
        this.sun = sun;
        this.setColor(Color.DARK_GRAY);
        this.setAntiAliasHint(ANTIALIAS_DONT_CARE);
        this.setPathType(GREAT_CIRCLE); // because this ends up being a big circle and this saves us on points needed
        this.setClosed(true);
        this.setLineWidth(2.0);
        this.setFollowTerrain(true);

        updateMJD(numPoints);
    }

    /**
     * 
     * @param currentMJD
     */
    @Override
    public void updateMJD(double currentMJD)
    {
        // don't need to use currentMJD, just the fact the time has changed, sun object already has updated info

        // get new footprint points
        Vector<LatLon> llVec = J2dEarthLabel2.getFootPrintLatLonList(sun.getCurrentLLA()[0], sun.getCurrentLLA()[1], sun.getCurrentLLA()[2], numPoints);

        // update polyline
        this.setPositions(llVec,0.0);

    } // updateMJD

    @Override
    public void render(DrawContext dc)
    {
        javax.media.opengl.GL gl = dc.getGL();
        gl.glPushAttrib(javax.media.opengl.GL.GL_TEXTURE_BIT | javax.media.opengl.GL.GL_ENABLE_BIT | javax.media.opengl.GL.GL_CURRENT_BIT);

        // Added so that the colors wouldn't depend on sun shading
        gl.glDisable(GL.GL_TEXTURE_2D);

        super.render(dc);

        gl.glPopAttrib();

    }
}
