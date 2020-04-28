/* Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.globe;

import gov.nasa.worldwind.applications.gos.ResourceUtil;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.examples.util.PowerOfTwoPaddedImage;
import gov.nasa.worldwind.layers.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.WWIO;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.InputStream;
import java.util.*;

/**
 * @author dcollins
 * @version $Id: LegendLayer.java 13909 2010-09-30 06:33:58Z pabercrombie $
 */
public class LegendLayer extends AbstractLayer
{
    public static class Legend extends AVListImpl
    {
        public final String imagePath;
        public final String displayName;
        public final Layer layer;

        public Legend(String imagePath, String displayName, Layer layer)
        {
            this.imagePath = imagePath;
            this.displayName = displayName;
            this.layer = layer;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || this.getClass() != o.getClass())
                return false;

            Legend that = (Legend) o;
            return this.imagePath.equals(that.imagePath) && this.displayName.equals(that.displayName)
                && this.layer == that.layer;
        }

        @Override
        public int hashCode()
        {
            int result = this.imagePath.hashCode();
            result = 31 * result + this.displayName.hashCode();
            result = 31 * result + (this.layer != null ? this.layer.hashCode() : 0);
            return result;
        }
    }

    protected Set<Legend> legendSet = new LinkedHashSet<Legend>(); // Use LinkedHashSet to get predictable render order.
    protected Map<Legend, ScreenAnnotation> annotationMap = new HashMap<Legend, ScreenAnnotation>();
    protected int leftMargin = 10;
    protected int bottomMargin = 10;
    protected int separation = 10;
    private static Point DUMMY_POINT = new Point();

    public LegendLayer()
    {
    }

    public boolean hasLegend(Legend legend)
    {
        return this.legendSet.contains(legend);
    }

    public void addLegend(Legend legend)
    {
        this.legendSet.add(legend);
    }

    public void removeLegend(Legend legend)
    {
        this.legendSet.remove(legend);
        this.annotationMap.remove(legend);
    }

    protected void doRender(DrawContext dc)
    {
        int x = this.leftMargin;
        int y = this.bottomMargin;

        for (Legend legend : this.legendSet)
        {
            int[] offset = this.drawLegend(dc, legend, x, y);
            if (offset != null)
            {
                x += offset[0];
                y += offset[1];
            }
        }
    }

    protected int[] drawLegend(DrawContext dc, Legend legend, int x, int y)
    {
        if (!legend.layer.isEnabled() || !legend.layer.isLayerActive(dc) || !legend.layer.isLayerInView(dc))
            return null;

        ScreenAnnotation sa = this.annotationMap.get(legend);
        if (sa == null && this.annotationMap.containsKey(legend))
            return null;

        if (sa == null)
        {
            this.createAnnotationForLegend(legend);
            return null;
        }

        Rectangle bounds = sa.getBounds(dc);

        // Put the Annotation's bottom left corner at the desired screen point. We have to adjust for Annotations
        // behavior of putting the center at the desired screen point.
        sa.setScreenPoint(new Point(x + bounds.width / 2, y));
        sa.render(dc);

        return new int[] {bounds.width + this.separation, 0};
    }

    protected void createAnnotationForLegend(final Legend legend)
    {
        // TODO: use FileStore retrieval and BasicWWTexture to handle the requests and threading.
        this.annotationMap.put(legend, null);

        ResourceUtil.getAppTaskService().execute(new Runnable()
        {
            public void run()
            {
                try
                {
                    InputStream is = WWIO.getBufferedInputStream(WWIO.openStream(legend.imagePath));
                    PowerOfTwoPaddedImage image = PowerOfTwoPaddedImage.fromBufferedImage(ImageIO.read(is));

                    ScreenAnnotation sa = new ScreenAnnotation(legend.displayName, DUMMY_POINT);
                    AnnotationAttributes attrs = sa.getAttributes();

                    // Configure frame appearance and size.
                    attrs.setFrameShape(FrameFactory.SHAPE_RECTANGLE);
                    attrs.setLeader(FrameFactory.LEADER_NONE);
                    attrs.setSize(new Dimension(image.getOriginalWidth(), 0));
                    attrs.setDrawOffset(DUMMY_POINT);
                    attrs.setAdjustWidthToText(AVKey.SIZE_FIXED);
                    attrs.setBackgroundColor(new Color(0xcc808080, true));
                    attrs.setCornerRadius(0);
                    attrs.setHighlightScale(1);
                    // Configure image appearance and placement.
                    attrs.setImageSource(image.getPowerOfTwoImage());
                    attrs.setImageRepeat(AVKey.REPEAT_NONE);
                    attrs.setImageOffset(new Point(0, 0));
                    attrs.setImageOpacity(1);
                    // Configure text appearance and placement.
                    attrs.setFont(Font.decode("Arial-PLAIN-12"));
                    attrs.setTextColor(Color.WHITE);
                    attrs.setTextAlign(AVKey.CENTER);
                    attrs.setInsets(new Insets(image.getOriginalHeight(), 0, 0, 0)); // Insets control text placement.
                    // Configure border appearance.
                    attrs.setBorderWidth(1);
                    attrs.setBorderColor(new Color(0xababab));

                    annotationMap.put(legend, sa);
                    firePropertyChange(AVKey.LAYER, null, LegendLayer.this);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }
}
