/*
Copyright (C) 2001, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind;

import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.view.orbit.OrbitView;
import gov.nasa.worldwind.geom.Angle;

import javax.media.opengl.GL;

/**
 * Allows for stereo anaglyph display mode.
 *
 * @author Patrick Murris
 * @version $Id: AnaglyphSceneController.java 12896 2009-12-10 21:20:38Z patrickmurris $
 * Issues: <br />
 * <ul>
 * <li>Angle between the two eye vary when view is iterating/orbiting.</li>
 * <li>Only works with a pitched view.</li>
 * <li>Red (left eye) / Cyan (right eye) anaglyph only for now</li>
 * </ul>
 */
public class AnaglyphSceneController extends AbstractSceneController
{
    public static String DISPLAY_MODE_MONO = "AnaglyphSceneController_DISPLAY_MODE_MONO";
    public static String DISPLAY_MODE_STEREO = "AnaglyphSceneController_DISPLAY_MODE_STEREO";

    private String displayMode = DISPLAY_MODE_STEREO;
    private Angle focusAngle = Angle.fromDegrees(1.6);    // Angle between the two eye views - higher = more 3D effect
    private Angle viewMinPitch = Angle.fromDegrees(20);   // Pitch threshold for anaglyph mode

    public void setDisplayMode(String mode)
    {
        this.displayMode = mode;
    }

    public String getDisplayMode()
    {
        return this.displayMode;
    }

    public void setFocusAngle(Angle a)
    {
        this.focusAngle = a;
    }

    public Angle getFocusAngle()
    {
        return this.focusAngle;
    }

    public void setViewMinPitch(Angle a)
    {
        this.viewMinPitch = a;
    }

    public Angle getViewMinPitch()
    {
        return this.viewMinPitch;
    }

    public void doRepaint(DrawContext dc)
    {
        GL gl = dc.getGL();
        this.initializeFrame(dc);
        try
        {
            this.applyView(dc);
            dc.addPickPointFrustum();
            this.createTerrain(dc);
            this.preRender(dc);
            this.clearFrame(dc);
            this.pick(dc);
            this.clearFrame(dc);
            OrbitView view = (OrbitView)dc.getView();
            // Anaglyph mode only above some pitch angle
            if(displayMode.equals(DISPLAY_MODE_STEREO) && view.getPitch().degrees > viewMinPitch.degrees)
                gl.glColorMask(true, false, false, true);   // left eye in red only
            this.draw(dc);
            if(displayMode.equals(DISPLAY_MODE_STEREO) && view.getPitch().degrees > viewMinPitch.degrees)
            {
                // Move the view to the right eye
                // TODO: use a view model transform that would work with any pitch - instead of changing the heading
                Angle viewHeading = view.getHeading();
                    view.setHeading(view.getHeading().subtract(focusAngle));
                view.apply(dc);
                // Draw right eye frame green and blue only
                gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
                gl.glDisable(GL.GL_FOG);
                gl.glColorMask(false, true, true, true); // right eye in green/blue
                this.draw(dc);
                // Restore original view heading
                view.setHeading(viewHeading);
                view.apply(dc);
                gl.glColorMask(true, true, true, true);
            }
        }
        finally
        {
            this.finalizeFrame(dc);
        }
    }
}
