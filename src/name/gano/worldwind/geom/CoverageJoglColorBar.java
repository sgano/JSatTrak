/*
 * CoverageJoglColorBar.java
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
 */

package name.gano.worldwind.geom;

import com.sun.opengl.util.j2d.TextRenderer;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import javax.media.opengl.GL;
import jsattrak.coverage.CoverageAnalyzer;

/**
 *
 * @author sgano
 */
public class CoverageJoglColorBar implements Renderable 
{
        
    private int borderWidth = 20;
    private Dimension size = new Dimension(150, 10); // location on screen?
    private Color color = Color.white;
    
    private Font defaultFont = Font.decode("Arial-12-PLAIN");    
    private TextRenderer textRenderer = null;    
    private Vec4 locationCenter = null;
    
    CoverageAnalyzer ca;
    
    public CoverageJoglColorBar(CoverageAnalyzer ca)
    {
        this.ca= ca;
    }
       
    // Rendering
    public void render(DrawContext dc)
    {

        // first see if we even need to do anything
        if(!ca.isShowColorBar())
        {
            return; // do nothing

        }

        GL gl = dc.getGL();

        boolean attribsPushed = false;
        boolean modelviewPushed = false;
        boolean projectionPushed = false;

        try
        {
            gl.glPushAttrib(GL.GL_DEPTH_BUFFER_BIT | GL.GL_COLOR_BUFFER_BIT | GL.GL_ENABLE_BIT | GL.GL_TEXTURE_BIT | GL.GL_TRANSFORM_BIT | GL.GL_VIEWPORT_BIT | GL.GL_CURRENT_BIT);
            attribsPushed = true;

            gl.glDisable(GL.GL_TEXTURE_2D);		// no textures

            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
            gl.glDisable(GL.GL_DEPTH_TEST);

            double width = dc.getDrawableWidth();
            double height = dc.getDrawableHeight();

            // Load a parallel projection with xy dimensions (viewportWidth, viewportHeight)
            // into the GL projection matrix.
            java.awt.Rectangle viewport = dc.getView().getViewport();
            gl.glMatrixMode(javax.media.opengl.GL.GL_PROJECTION);
            gl.glPushMatrix();
            projectionPushed = true;
            gl.glLoadIdentity();
            double maxwh = width > height ? width : height;
            gl.glOrtho(0d, viewport.width, 0d, viewport.height, -0.6 * maxwh, 0.6 * maxwh);

            gl.glMatrixMode(GL.GL_MODELVIEW);
            gl.glPushMatrix();
            modelviewPushed = true;
            gl.glLoadIdentity();

            // Scale to a width x height space
            // located at the proper position on screen
            double scale = computeScale(viewport);
            Vec4 locationSW = computeLocation(viewport, scale);
            gl.glTranslated(locationSW.x(), locationSW.y(), locationSW.z());
            gl.glScaled(scale, scale, 1);

            // Compute scale size in real world
            Position groundPos = computeGroundPosition(dc, dc.getView());

            if(groundPos != null)
            {
                Vec4 groundTarget = dc.getGlobe().computePointFromPosition(groundPos);
                Double distance = dc.getView().getEyePoint().distanceTo3(groundTarget);
                Double pixelSize = dc.getView().computePixelSizeAtDistance(distance);
                Double scaleSize = pixelSize * width * scale;  // meter

                // Rounded division size
                int pot = (int)Math.floor(Math.log10(scaleSize));
                int digit = Integer.parseInt(scaleSize.toString().substring(0, 1));
                double divSize = digit * Math.pow(10, pot);
                if(digit >= 5)
                {
                    divSize = 5 * Math.pow(10, pot);
                }
                else if(digit >= 2)
                {
                    divSize = 2 * Math.pow(10, pot);
                }
                double divWidth = width * divSize / scaleSize;

                // HARD CODED VALUES TO HOW WIDE AND TALL COLOR BAR IS -- May want to make these variables
                divWidth = 130;
                height = 15;
                width = divWidth * scaleSize / divSize; // fix :]

                // Draw scale
                // Set color using current layer opacity
                double opacity = 0.9;
                Color backColor = this.getBackgroundColor(this.color);
                float[] colorRGB = backColor.getRGBColorComponents(null);
                gl.glColor4d(colorRGB[0], colorRGB[1], colorRGB[2], (double)backColor.getAlpha() / 255d * opacity);
                //gl.glTranslated((width - divWidth) / 2, 0d, 0d);
                gl.glTranslated(20, 0d, 0d);
                this.drawScale(dc, divWidth, height);

                colorRGB = this.color.getRGBColorComponents(null);
                gl.glColor4d(colorRGB[0], colorRGB[1], colorRGB[2], opacity);
                gl.glTranslated(-1d / scale, 1d / scale, 0d);
                this.drawScale(dc, divWidth, height);

                // Draw label
                //String label = String.format("%.0f ", divSize) + unitLabel;
                String label = ca.getUpperBoundLabel();
                gl.glLoadIdentity();
                gl.glDisable(GL.GL_CULL_FACE);
                drawLabel(label,
                        locationSW.add3(new Vec4(divWidth * scale, 0 * height * scale - height * scale / 2 - 10, 0)));

                //String label = String.format("%.0f ", divSize) + unitLabel;
                label = ca.getLowerBoundLabel();
                gl.glLoadIdentity();
                gl.glDisable(GL.GL_CULL_FACE);
                drawLabel(label,
                        locationSW.add3(new Vec4(0 + 12, 0 * height * scale - height * scale / 2 - 10, 0)));

            }
        }
        finally
        {
            if(projectionPushed)
            {
                gl.glMatrixMode(GL.GL_PROJECTION);
                gl.glPopMatrix();
            }
            if(modelviewPushed)
            {
                gl.glMatrixMode(GL.GL_MODELVIEW);
                gl.glPopMatrix();
            }
            if(attribsPushed)
            {
                gl.glPopAttrib();
            }
        } // finally
    } // render
        
    private double toViewportScale = 0.2;

    private double computeScale(java.awt.Rectangle viewport)
    {
        return Math.min(1d, (this.toViewportScale) * viewport.width / this.size.width);
    }

    private Vec4 computeLocation(java.awt.Rectangle viewport, double scale)
    {
        double scaledWidth = scale * this.size.width;
        double scaledHeight = scale * this.size.height;

        double x = 0;
        double y = 0;

        if(this.locationCenter != null)
        {
            x = viewport.getWidth() - scaledWidth / 2 - this.borderWidth;
            y = viewport.getHeight() - scaledHeight / 2 - this.borderWidth;
        }
        else
        {
            x = 0d + this.borderWidth;
            y = 0d + this.borderWidth;
        }

        return new Vec4(x, y, 0);
    }
        
    private Position computeGroundPosition(DrawContext dc, View view)
    {
        if(view == null)
        {
            return null;
        }
        Position groundPos = view.computePositionFromScreenPoint(
                view.getViewport().getWidth() / 2, view.getViewport().getHeight() / 2);
        if(groundPos == null)
        {
            return null;
        }
        double elevation = dc.getGlobe().getElevation(groundPos.getLatitude(), groundPos.getLongitude());
        return new Position(
                groundPos.getLatitude(),
                groundPos.getLongitude(),
                elevation * dc.getVerticalExaggeration());
    }
        
    private final float[] compArray = new float[4];

    private Color getBackgroundColor(Color color)
    {
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), compArray);
        if(compArray[2] > 0.5)
        {
            return new Color(0, 0, 0, 0.7f);
        }
        else
        {
            return new Color(1, 1, 1, 0.7f);
        }
    }
        
    // Draw scale graphic
    private void drawScale(DrawContext dc, double width, double height)
    {

        GL gl = dc.getGL();

        // draw lines at end points
        gl.glBegin(GL.GL_LINE_STRIP);
        gl.glVertex3d(-1, height, 0);
        gl.glVertex3d(-1, -height / 2, 0);
        gl.glEnd();
        gl.glBegin(GL.GL_LINE_STRIP);
        gl.glVertex3d(width, height, 0);
        gl.glVertex3d(width, -height / 2, 0);
        gl.glEnd();

        for(int i = 0; i < width; i++)
        {
            Color c = ca.getColorMap().getColor(i, 0, width);
            gl.glColor3f(c.getRed() / 255.0f, c.getGreen() / 255.0f, c.getBlue() / 255.0f);
            gl.glBegin(GL.GL_LINE_STRIP);
            gl.glVertex3f((float)i, (float)height, 0f);
            gl.glVertex3f((float)i, 0f, 0f);
            gl.glEnd();
        }

    }
    
    // Draw the scale label
    private void drawLabel(String text, Vec4 screenPoint)
    {
        if(this.textRenderer == null)
        {
            this.textRenderer = new TextRenderer(this.defaultFont, true, true);
        }

        Rectangle2D nameBound = this.textRenderer.getBounds(text);
        int x = (int)(screenPoint.x() - nameBound.getWidth() / 2d);
        int y = (int)screenPoint.y();

        this.textRenderer.begin3DRendering();

        this.textRenderer.setColor(this.getBackgroundColor(this.color));
        this.textRenderer.draw(text, x + 1, y - 1);
        this.textRenderer.setColor(this.color);
        this.textRenderer.draw(text, x, y);

        this.textRenderer.end3DRendering();

    }
        
} 
