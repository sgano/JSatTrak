/*
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
package jsattrak.utilities;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.MultiLineTextRenderer;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.Logging;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.util.Hashtable;
import javax.media.opengl.GL;
import jsattrak.objects.AbstractSatellite;
import jsattrak.objects.GroundStation;
import name.gano.worldwind.geom.SphereObject;


/**
 * @author Shawn E. Gano 
 */
public class ECEFModelRenderable implements Renderable
{
    
    // hash of sat props
    Hashtable<String,AbstractSatellite> satHash;
    
    // ground stations
    Hashtable<String,GroundStation> gsHash;
    
    
    // save globe
    Globe globe;
    
    // altitude to plot ground trace 
    double groundTrackAlt = 10000;
    
    // sphere for Ground Station
    double sphereRadius = 100000;
    SphereObject sphere = new SphereObject(new Vec4(0,0,0,0), sphereRadius, true);
    
    // annotation
    //GlobeAnnotation annotation;
    
    
    /** Creates a new instance of OrbitModel
     * @param satHash
     * @param globe 
     */
    public ECEFModelRenderable(Hashtable<String,AbstractSatellite> satHash, Hashtable<String,GroundStation> gsHash, Globe globe)
    {
        this.satHash = satHash;
        this.gsHash = gsHash;
        this.globe = globe;
        
     }
    
    
    public void render(DrawContext dc)
    {
        if (dc == null)
        {
            String msg = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }
        
        javax.media.opengl.GL gl = dc.getGL();
        
        //gl.glEnable(GL.GL_TEXTURE_2D); // removed so the sun shading wouldn't effect line colors
        gl.glPushAttrib(javax.media.opengl.GL.GL_TEXTURE_BIT | javax.media.opengl.GL.GL_ENABLE_BIT | javax.media.opengl.GL.GL_CURRENT_BIT);
        gl.glMatrixMode(javax.media.opengl.GL.GL_MODELVIEW);

        // Added so that the colors wouldn't depend on sun shading
        gl.glDisable(GL.GL_TEXTURE_2D);
              
        // for each satellite
        for(AbstractSatellite sat : satHash.values() ) // search through all sat nodes
        {
            // set color
            Color satColor = sat.getSatColor();
            gl.glColor3f( satColor.getRed()/255.0f , satColor.getGreen()/255.0f , satColor.getBlue()/255.0f ); // COLOR
            
            // GROUND TRACK
            if (sat.isShowGroundTrack3d())
            {
                
                //System.out.println("here");
                
                Vec4 ptLoc;
                // ground trace - lag
                gl.glBegin(GL.GL_LINE_STRIP); //GL_LINE_STRIP
                for (int i = 0; i < sat.getNumGroundTrackLagPts(); i++)
                {
                    // add next Mean of Date lla
                    double[] lla = sat.getGroundTrackLlaLagPt(i);
                    
                    ptLoc = globe.computePointFromPosition(
                    Angle.fromRadiansLatitude(lla[0]), 
                    Angle.fromRadiansLongitude(lla[1]), groundTrackAlt);
                    
                    gl.glVertex3f( (float)ptLoc.x, (float)ptLoc.y , (float)ptLoc.z);
                    
                }
                gl.glEnd();

                // plot lead orbit ground track
                gl.glBegin(GL.GL_LINE_STRIP); //GL_LINE_STRIP
                for (int i = 0; i < sat.getNumGroundTrackLeadPts(); i++)
                {
                    // add next Mean of Date lla
                    double[] lla = sat.getGroundTrackLlaLeadPt(i);
                    
                    ptLoc = globe.computePointFromPosition(
                    Angle.fromRadiansLatitude(lla[0]), 
                    Angle.fromRadiansLongitude(lla[1]), groundTrackAlt);
                    
                    gl.glVertex3f( (float)ptLoc.x, (float)ptLoc.y , (float)ptLoc.z);
                }
                gl.glEnd();
            } // show ground trace
            
            // ECEF ORBIT TRACE
            if (sat.isShow3DOrbitTrace() && !sat.isShow3DOrbitTraceECI() )
            {
                Vec4 ptLoc;
                // ground trace - lag
                gl.glBegin(GL.GL_LINE_STRIP); //GL_LINE_STRIP
                for (int i = 0; i < sat.getNumGroundTrackLagPts(); i++)
                {
                    // add next Mean of Date lla
                    double[] lla = sat.getGroundTrackLlaLagPt(i);
                    
                    ptLoc = globe.computePointFromPosition(
                    Angle.fromRadiansLatitude(lla[0]), 
                    Angle.fromRadiansLongitude(lla[1]), lla[2]);
                    
                    gl.glVertex3f( (float)ptLoc.x, (float)ptLoc.y , (float)ptLoc.z);
                    
                }
                gl.glEnd();

                // plot lead orbit ground track
                gl.glBegin(GL.GL_LINE_STRIP); //GL_LINE_STRIP
                for (int i = 0; i < sat.getNumGroundTrackLeadPts(); i++)
                {
                    // add next Mean of Date lla
                    double[] lla = sat.getGroundTrackLlaLeadPt(i);
                    
                    ptLoc = globe.computePointFromPosition(
                    Angle.fromRadiansLatitude(lla[0]), 
                    Angle.fromRadiansLongitude(lla[1]), lla[2]);
                    
                    gl.glVertex3f( (float)ptLoc.x, (float)ptLoc.y , (float)ptLoc.z);
                }
                gl.glEnd();
                
            } // ecef orbit trace
            
            
        } // for each sat

        // for each GroundStation
        // for each satellite
        for(GroundStation gs : gsHash.values() ) // search through all sat nodes
        {
            if(gs.isShow3D())
            {
                // for now just plop down a sphere for the GS
                Vec4 pos = globe.computePointFromPosition(Angle.fromDegrees(gs.getLatitude()), Angle.fromDegrees(gs.getLongitude()), gs.getAltitude());
                
                sphere.setCenter(pos);
                //sphere.setCenter(-xyz[0], xyz[2], xyz[1]);
                sphere.render(dc);
                
                // create name --- Hmm this creates a lot of annotations every time step, is this a problem?
                // doesn't work to render and change attributes, maybe save one per GroundStation?
                if(gs.isShow3DName())
                {
                    AnnotationAttributes geoAttr = createFontAttribs(gs.getStationColor());
                    GlobeAnnotation an = new GlobeAnnotation(gs.getStationName(), Position.fromDegrees(gs.getLatitude(), gs.getLongitude(), gs.getAltitude()), geoAttr);
                    // annotation - without any attribs, gives a bubble box
                    // annotation doesn't strech well in GLCanvas
                    //GlobeAnnotation an = new GlobeAnnotation(gs.getStationName(), Position.fromDegrees(gs.getLatitude(), gs.getLongitude(), gs.getAltitude()));
                    an.render(dc);
                }
                
            } // show GS in 3D
        } // for each ground station
        
        // LOCATION OF SAT - and orientation
                   // plot position 
//         gl.glRotated(-90, 0.0, 1.0, 0.0); // needs to be before veleocty?
//        for(AbstractSatellite sat : satHash.values() )
//        {
//            double[] xyz = sat.getPosMOD();
//            if(xyz != null)
//            {
//                // 3D model is rendered Here
//                if(sat.isUse3dModel())
//                {
//                    // custom 3D object
//                    if(sat.getThreeDModel() != null) // make sure it is not null
//                    {
//                       //- 
//                        sat.getThreeDModel().render(dc); // render model
//                    }
//                }
//                else
//                {
//                    // default "sphere" for model
//                    sphere.setCenter(-xyz[0], xyz[2], xyz[1]);
//                    sphere.render(dc);
//                }
//            } // if pos is not null
//        }
//        
        gl.glPopAttrib();
        
     } // render
    
    
    private AnnotationAttributes createFontAttribs(Color textColor)
    {
        AnnotationAttributes geoAttr = new AnnotationAttributes();
            geoAttr.setFrameShape(FrameFactory.SHAPE_NONE);  // No frame
            geoAttr.setFont(Font.decode("Arial-ITALIC-12"));
            geoAttr.setTextColor(textColor);
            geoAttr.setTextAlign(MultiLineTextRenderer.ALIGN_CENTER);
            geoAttr.setDrawOffset(new Point(0, 5)); // centered just above
            geoAttr.setEffect(MultiLineTextRenderer.EFFECT_OUTLINE);  // Black outline
            geoAttr.setBackgroundColor(Color.BLACK);
            
            return geoAttr;
    } //createFontAttribs
    
    
}
