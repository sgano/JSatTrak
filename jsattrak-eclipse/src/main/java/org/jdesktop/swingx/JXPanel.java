/*
 * $Id: JXPanel.java 4158 2012-02-03 18:29:40Z kschaefe $
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.jdesktop.swingx;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;
import javax.swing.RepaintManager;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

import org.jdesktop.beans.JavaBean;
import org.jdesktop.swingx.painter.AbstractPainter;
import org.jdesktop.swingx.painter.Painter;
import org.jdesktop.swingx.util.GraphicsUtilities;

/**
 * <p>
 * An extended {@code JPanel} that provides additional features. First, the
 * component is {@code Scrollable}, using reasonable defaults. Second, the
 * component is alpha-channel enabled. This means that the {@code JXPanel} can
 * be made fully or partially transparent. Finally, {@code JXPanel} has support
 * for {@linkplain Painter painters}.
 * </p>
 * <p>
 * A transparency example, this following code will show the black background of
 * the parent:
 * 
 * <pre>
 * JXPanel panel = new JXPanel();
 * panel.add(new JButton(&quot;Push Me&quot;));
 * panel.setAlpha(.5f);
 * 
 * container.setBackground(Color.BLACK);
 * container.add(panel);
 * </pre>
 * 
 * </p>
 * <p>
 * A painter example, this following code will show how to add a simple painter:
 * 
 * <pre>
 * JXPanel panel = new JXPanel();
 * panel.setBackgroundPainter(new PinstripePainter());
 * </pre>
 * 
 * </p>
 * 
 * @author rbair
 * @see Scrollable
 * @see Painter
 */
@JavaBean
@SuppressWarnings("nls")
public class JXPanel extends JPanel implements AlphaPaintable, BackgroundPaintable, Scrollable {
//    private boolean scrollableTracksViewportHeight = true;
//    private boolean scrollableTracksViewportWidth = true;
    
    private ScrollableSizeHint scrollableWidthHint = ScrollableSizeHint.FIT;
    private ScrollableSizeHint scrollableHeightHint = ScrollableSizeHint.FIT;
    
    /**
     * The alpha level for this component.
     */
    private float alpha = 1.0f;
    /**
     * If the old alpha value was 1.0, I keep track of the opaque setting because
     * a translucent component is not opaque, but I want to be able to restore
     * opacity to its default setting if the alpha is 1.0. Honestly, I don't know
     * if this is necessary or not, but it sounded good on paper :)
     * <p>TODO: Check whether this variable is necessary or not</p>
     */
    private boolean oldOpaque;
    /**
     * Indicates whether this component should inherit its parent alpha value
     */
    private boolean inheritAlpha = true;
    /**
     * Specifies the Painter to use for painting the background of this panel.
     * If no painter is specified, the normal painting routine for JPanel
     * is called. Old behavior is also honored for the time being if no
     * backgroundPainter is specified
     */
    @SuppressWarnings("rawtypes")
    private Painter backgroundPainter;
    
    private boolean paintBorderInsets = true;

    /**
     * The listener installed on the current backgroundPainter, if any.
     */
    private PropertyChangeListener painterChangeListener;
    
    /**
     * Creates a new <code>JXPanel</code> with a double buffer
     * and a flow layout.
     */
    public JXPanel() {
    }
    
    /**
     * Creates a new <code>JXPanel</code> with <code>FlowLayout</code>
     * and the specified buffering strategy.
     * If <code>isDoubleBuffered</code> is true, the <code>JXPanel</code>
     * will use a double buffer.
     *
     * @param isDoubleBuffered  a boolean, true for double-buffering, which
     *        uses additional memory space to achieve fast, flicker-free 
     *        updates
     */
    public JXPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }
    
    /**
     * Create a new buffered JXPanel with the specified layout manager
     *
     * @param layout  the LayoutManager to use
     */
    public JXPanel(LayoutManager layout) {
        super(layout);
    }
    
    /**
     * Creates a new JXPanel with the specified layout manager and buffering
     * strategy.
     *
     * @param layout  the LayoutManager to use
     * @param isDoubleBuffered  a boolean, true for double-buffering, which
     *        uses additional memory space to achieve fast, flicker-free 
     *        updates
     */
    public JXPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public float getAlpha() {
        return alpha;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAlpha(float alpha) {
        if (alpha < 0f || alpha > 1f) {
            throw new IllegalArgumentException("invalid alpha value " + alpha);
        }
        
        float oldValue = getAlpha();
        this.alpha = alpha;
        
        if (getAlpha() < 1f) {
            if (oldValue == 1) {
                //it used to be 1, but now is not. Save the oldOpaque
                oldOpaque = isOpaque();
                setOpaque(false);
            }
            
            installRepaintManager();
        } else {
            uninstallRepaintManager();
            
            //restore the oldOpaque if it was true (since opaque is false now)
            if (oldOpaque) {
                setOpaque(true);
            }
        }
        
        firePropertyChange("alpha", oldValue, getAlpha());
        repaint();
    }
    
    void installRepaintManager() {
        RepaintManager manager = RepaintManager.currentManager(this);
        RepaintManager trm = SwingXUtilities.getTranslucentRepaintManager(manager);
        RepaintManager.setCurrentManager(trm);
    }
    
    void uninstallRepaintManager() {
        //TODO uninstall TranslucentRepaintManager when no more non-opaque JXPanel's exist
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public float getEffectiveAlpha() {
        float a = getAlpha();
        
        if (isInheritAlpha()) {
            for (Component c = getParent(); c != null; c = c.getParent()) {
                if (c instanceof AlphaPaintable) {
                    a = Math.min(((AlphaPaintable) c).getEffectiveAlpha(), a);
                    break;
                }
            }
        }
        
        return a;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isInheritAlpha() {
        return inheritAlpha;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setInheritAlpha(boolean val) {
        boolean oldValue = isInheritAlpha();
        inheritAlpha = val;
        firePropertyChange("inheritAlpha", oldValue, isInheritAlpha());
    }
    
    /**
     * Sets the horizontal sizing hint. The hint is used by the Scrollable implementation
     * to service the getScrollableTracksWidth.
     * 
     * @param hint the horizontal sizing hint, must not be null
     *   and must be vertical.
     * 
     * @throws IllegalArgumentException if track not horizontal 
     * @throws NullPointerException if null
     * 
     * @see #setScrollableHeightHint(ScrollableSizeHint)
     * @see ScrollableSizeHint
     */
    public final void setScrollableWidthHint(ScrollableSizeHint hint) {
        if (!hint.isHorizontalCompatible()) throw 
           new IllegalArgumentException("track must be horizontal, but was " + hint);
        ScrollableSizeHint oldValue = getScrollableWidthHint();
        if (oldValue == hint) return;
        this.scrollableWidthHint = hint;
        revalidate();
        firePropertyChange("scrollableWidthHint", oldValue, getScrollableWidthHint());
    }
    
    
    /**
     * Sets the vertical sizing hint. The hint is used by the Scrollable implementation
     * to service the getScrollableTracksHeight.
     * 
     * @param hint the vertical sizing hint, must not be null
     *   and must be vertical.
     * 
     * @throws IllegalArgumentException if track not vertical 
     * @throws NullPointerException if null
     * 
     * @see #setScrollableWidthHint(ScrollableSizeHint)
     * @see ScrollableSizeHint
     */
    public final void setScrollableHeightHint(ScrollableSizeHint hint) {
        if (!hint.isVerticalCompatible()) throw 
            new IllegalArgumentException("track must be vertical, but was " + hint);
        ScrollableSizeHint oldValue = getScrollableHeightHint();
        if (oldValue == hint) return;
        this.scrollableHeightHint = hint;
        revalidate();
        firePropertyChange("scrollableHeightHint", oldValue, getScrollableHeightHint());
    }
    
    protected ScrollableSizeHint getScrollableWidthHint() {
        return scrollableWidthHint;
    }
    
    protected ScrollableSizeHint getScrollableHeightHint() {
        return scrollableHeightHint;
        
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getScrollableTracksViewportHeight() {
        return scrollableHeightHint.getTracksParentSize(this, SwingConstants.VERTICAL);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getScrollableTracksViewportWidth() {
        return scrollableWidthHint.getTracksParentSize(this, SwingConstants.HORIZONTAL);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        if (orientation == SwingConstants.VERTICAL) {
            return visibleRect.height;
        } else if (orientation == SwingConstants.HORIZONTAL) {
            return visibleRect.width;
        } else {
            throw new IllegalArgumentException("invalid orientation"); //$NON-NLS-1$
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return getScrollableBlockIncrement(visibleRect, orientation, direction) / 10;
    }
    
    /**
     * 
     * Sets the vertical size tracking to either ScrollableSizeTrack.FIT or NONE, if the
     * boolean parameter is true or false, respectively.<p>
     * 
     * <b>NOTE</b>: this method is kept for backward compatibility only, for full 
     * control use setScrollableHeightHint.
     * 
     * @param scrollableTracksViewportHeight The scrollableTracksViewportHeight to set.
     * 
     * @see #setScrollableHeightHint(ScrollableSizeHint)
     */
    public void setScrollableTracksViewportHeight(boolean scrollableTracksViewportHeight) {
        setScrollableHeightHint(scrollableTracksViewportHeight ? 
                ScrollableSizeHint.FIT : ScrollableSizeHint.NONE);
    }
    /**
     * Sets the horizontal size tracking to either ScrollableSizeTrack.FIT or NONE, if the
     * boolean parameter is true or false, respectively.<p>
     * 
     * <b>NOTE</b>: this method is kept for backward compatibility only, for full 
     * control use setScrollableWidthHint.
     * 
     * 
     * @param scrollableTracksViewportWidth The scrollableTracksViewportWidth to set.
     * 
     * @see #setScrollableWidthHint(ScrollableSizeHint)
     */
    public void setScrollableTracksViewportWidth(boolean scrollableTracksViewportWidth) {
        setScrollableWidthHint(scrollableTracksViewportWidth ? 
                ScrollableSizeHint.FIT : ScrollableSizeHint.NONE);
    }

    /**
     * Sets the background color for this component by
     * 
     * @param bg
     *            the desired background <code>Color</code>
     * @see javax.swing.JComponent#getBackground()
     * @see #setOpaque(boolean)
     * 
    * @beaninfo
    *    preferred: true
    *        bound: true
    *    attribute: visualUpdate true
    *  description: The background color of the component.
     */
    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        
        SwingXUtilities.installBackground(this, bg);
    }
    
    /**
     * Sets a Painter to use to paint the background of this JXPanel.
     * 
     * @param p the new painter
     * @see #getBackgroundPainter()
     */
    @Override
    public void setBackgroundPainter(Painter p) {
        Painter old = getBackgroundPainter();
        if (old instanceof AbstractPainter) {
            ((AbstractPainter<?>) old).removePropertyChangeListener(painterChangeListener);
        }
        backgroundPainter = p;
        if (backgroundPainter instanceof AbstractPainter) {
            ((AbstractPainter<?>) backgroundPainter).addPropertyChangeListener(getPainterChangeListener());
        }
        firePropertyChange("backgroundPainter", old, getBackgroundPainter());
        repaint();
    }
    
    /**
     * @return a listener for painter change events
     */
    protected PropertyChangeListener getPainterChangeListener() {
        if (painterChangeListener == null) {
            painterChangeListener = new PropertyChangeListener() {
                
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    repaint();
                }
            };
        }
        return painterChangeListener;
    }

    /**
     * Returns the current background painter. The default value of this property 
     * is a painter which draws the normal JPanel background according to the current look and feel.
     * @return the current painter
     * @see #setBackgroundPainter(Painter)
     * @see #isPaintBorderInsets()
     */
    @Override
    public Painter getBackgroundPainter() {
        return backgroundPainter;
    }
    
    /**
     * Returns true if the background painter should paint where the border is
     * or false if it should only paint inside the border. This property is 
     * true by default. This property affects the width, height,
     * and initial transform passed to the background painter.
     */
    @Override
    public boolean isPaintBorderInsets() {
        return paintBorderInsets;
    }
    
    /**
     * Sets the paintBorderInsets property.
     * Set to true if the background painter should paint where the border is
     * or false if it should only paint inside the border. This property is true by default.
     * This property affects the width, height,
     * and initial transform passed to the background painter.
     * 
     * This is a bound property.
     */
    @Override
    public void setPaintBorderInsets(boolean paintBorderInsets) {
        boolean old = this.isPaintBorderInsets();
        this.paintBorderInsets = paintBorderInsets;
        firePropertyChange("paintBorderInsets", old, isPaintBorderInsets());
    }

    /**
     * Overridden paint method to take into account the alpha setting.
     * 
     * @param g
     *            the <code>Graphics</code> context in which to paint
     */
    @Override
    public void paint(Graphics g) {
        //short circuit painting if no transparency
        if (getAlpha() == 1f) {
            super.paint(g);
        } else {
            //the component is translucent, so we need to render to
            //an intermediate image before painting
            // TODO should we cache this image? repaint to same image unless size changes?
            BufferedImage img = GraphicsUtilities.createCompatibleTranslucentImage(getWidth(), getHeight());
            Graphics2D gfx = img.createGraphics();
            
            try {
                super.paint(gfx);
            } finally {
                gfx.dispose();
            }
            
            Graphics2D g2d = (Graphics2D) g;
            Composite oldComp = g2d.getComposite();
            
            try {
                Composite alphaComp = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getEffectiveAlpha());
                g2d.setComposite(alphaComp);
                //TODO should we cache the image?
                g2d.drawImage(img, null, 0, 0);
            } finally {
                g2d.setComposite(oldComp);
            }
        }
    }
    
    /**
     * Overridden to provide Painter support. It will call backgroundPainter.paint()
     * if it is not null, else it will call super.paintComponent().
     * 
     * @param g
     *            the <code>Graphics</code> context in which to paint
     */
    @Override
    protected void paintComponent(Graphics g) {
        if (backgroundPainter == null) {
            super.paintComponent(g);
        } else {
            if (isOpaque()) {
                super.paintComponent(g);
            }
            
            Graphics2D g2 = (Graphics2D) g.create();
            
            try {
                SwingXUtilities.paintBackground(this, g2);
            } finally {
                g2.dispose();
            }
            
            getUI().paint(g, this);
        }
    }
}
