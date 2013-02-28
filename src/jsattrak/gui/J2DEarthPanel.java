/**
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
 * 
 */
package jsattrak.gui;

import jsattrak.objects.GroundStation;
import java.awt.event.*;
import java.awt.*;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import java.awt.image.*;
import java.io.File;
import java.util.Hashtable;
import java.util.Vector;
import jsattrak.coverage.JSatTrakRenderable;
import jsattrak.objects.AbstractSatellite;
import jsattrak.utilities.LandMassRegions;
import name.gano.astro.bodies.Sun;
import name.gano.astro.time.Time;

public class J2DEarthPanel extends JPanel implements ComponentListener , java.io.Serializable
{
    // options
    private Color backgroundColor = new Color(236,233,216); //Color.LIGHT_GRAY;
        
    //private transient ImageIcon planetImage;
    //private JLabel imageMap;
    private J2dEarthLabel2 imageMap;
    
    public transient JPopupMenu popup;
    private transient JMenuItem latlonLinesMenu, moreOptionsMenu; // , earthPeMenu, earthNoaaMenu
    //private transient ImageIcon checkMark;
    
    //private int imageMapNum = 0; //0=blue marble, 1=NOAA
    
    
    private transient BufferedImage bimage; // stores full res Earth image map
    private transient BufferedImage bimageScaled; // stores scaled Earth image map - added for Earth Lights Option
    
    private String backgroundImagePath = "/images/Earth_PE_small.jpg"; // default image
    
    private double aspectRatio = 2.0; // width/height
    
    private int imageScalingOption = BufferedImage.SCALE_FAST;
    
    // toggle button references used to see if we should be zooming
    private transient JToggleButton zoomInToggleButton;
    private transient JToggleButton zoomOutToggleButton;
    private transient JToggleButton recenterToggleButton;
    
    // current time
    private transient Time currentTime;
    
    // hastable of all the satellites currently processing
    //Hashtable<String,SatelliteProps> satHash = new Hashtable();
     
     // app
     private transient JSatTrak app;
     
     // sun
     private Sun sun;
     
     
     // land mass  --  only hard coded for now
    // edit jsattrak.utilities.LandMassRegions to load either US map or World Map
    LandMassRegions landMass = new LandMassRegions();
    int indexSpacing = 1; // index increment for plotting regions (1=best, >1 faster)
//    boolean showLandMassOutlines = false;
//    Color landOutlineColor = Color.WHITE;
    
    // Earth Lights Mask data
    private transient BufferedImage earthLightsFullRes; // full res image stored
    private transient BufferedImage earthLightsCurrentMask; // current mask of earth lights
    private String earthLightsMaskImagePath = "/images/earth_lights_lrg.jpg"; //saves path to earth lights image in JAR
    
    // bean -- no inputs
    public J2DEarthPanel()
    {
        super(false); // no double buffering
        
        Hashtable<String,AbstractSatellite> satHash = new Hashtable<String,AbstractSatellite>();
        Hashtable<String,GroundStation> gsHash = new Hashtable<String, GroundStation>();
        iniObject(satHash,gsHash);
               
    }
    
    public J2DEarthPanel(Hashtable<String,AbstractSatellite> satHash, Hashtable<String,GroundStation> gsHash, JToggleButton zoomInToggleButton, JToggleButton zoomOutToggleButton, JToggleButton recenterToggleButton, Time currentTime, Sun sun, JSatTrak app)
    {
        super(false);//not double buffered
       
        // current time
        this.currentTime = currentTime;
        // parent app to show 2d earth window properties
        this.app = app;
        // sun
        this.sun = sun;
        
        iniObject(satHash,gsHash);
        
        // save buttons
        this.zoomInToggleButton = zoomInToggleButton;
        this.zoomOutToggleButton = zoomOutToggleButton;
        this.recenterToggleButton = recenterToggleButton;
        
        this.setBackground(backgroundColor);
        
        // TEMP try out the LandMassRegions -- move this to top class, so each map doesn't have their own? or leave it this way so each can be customized
//        if(showLandMassOutlines)
//        {
//            landMass = new LandMassRegions();
//        }
               
        // DEBUG
        //setShowEarthLightsMask(true);
        
    } // TwoDFrame
    
    private void iniObject(Hashtable<String,AbstractSatellite> satHash, Hashtable<String,GroundStation> gsHash)
    {
        //...Create the GUI and put it in the window...
        
        //Create the first label.
        ImageIcon planetImage = createImageIcon(backgroundImagePath,"Earth");//"/images/Earth_PE_small.png","Earth");
        bimage = getBufferedImage(planetImage);
        
        //imageMap = new JLabel(planetImage);
        imageMap = new J2dEarthLabel2(planetImage,aspectRatio, satHash, gsHash, backgroundColor, currentTime, sun, this);
        imageMap.setHorizontalAlignment(JLabel.CENTER);
        //JScrollPane tableScrollPane = new JScrollPane(imageMap);
        
        super.addComponentListener(this); // adds the listner to the whole internal window
        
        setLayout(new GridLayout(1, 1));
        add(imageMap);
        //add(new JLabel(planetImage));
        
        
        // add these lines so the split pane will resize correctly
        setMinimumSize(new Dimension(0, 0));
        setMaximumSize(new Dimension(0, 0));
        
        //...Then set the window size or call pack...
        //setSize(400,230);
        
        //Set the window's location.
        //setLocation(xOffset*openFrameCount, yOffset*openFrameCount);
        
        // Popup Menu
        popup = new JPopupMenu();
        ActionListener menuListener = new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                // handle the menu selection!
                //System.out.println("Popup menu item ["+ event.getActionCommand() + "] was pressed.");
                //latlonLinesMenu, earthPeMenu, earthNoaaMenu
                if( event.getSource() == latlonLinesMenu)
                {
                    // toggle lat long lines
                    imageMap.showLatLong = !imageMap.showLatLong; // toogle lat long
                    imageMap.repaint();
                } 
//                else if( event.getSource() == earthPeMenu)
//                {
//                    // Toolkit.getDefaultToolkit().getImage(getClass().getResource("/toolbarButtonGraphics/custom/sattrak.png"))
//                    backgroundImagePath = "/images/Earth_PE_small.png";
//                    ImageIcon temp = createImageIcon(backgroundImagePath,"Earth");
//                    //ImageIcon temp = createImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("../images/Earth_PE_small.png")),"Earth");
//                    bimage = getBufferedImage(temp);
//                    ScaleImageMap(); // repaint the image
//                    earthNoaaMenu.setIcon(null);
//                    earthPeMenu.setIcon(checkMark);
//                    
//                    imageMapNum = 0; //0=PE, 1=NOAA
//                    
//                }
//                else if( event.getSource() == earthNoaaMenu)
//                {
//                    backgroundImagePath = "/images/Earth_NOAA_NGDC_small.png";
//                    ImageIcon temp = createImageIcon(backgroundImagePath,"Earth");
//                    bimage = getBufferedImage(temp);
//                    ScaleImageMap(); // repaint the image
//                    earthNoaaMenu.setIcon(checkMark);
//                    earthPeMenu.setIcon(null);
//                    
//                    imageMapNum =1; //0=PE, 1=NOAA
//                }
                else if( event.getSource() == moreOptionsMenu)
                {
                    // open more options
                    getApp().open2dWindowOptions();
                    
                }
                
                
                
                
            }
        };
        
        // JMenuItem latlonLinesMenu, earthPeMenu, earthNoaaMenu
        latlonLinesMenu = new JMenuItem("Toggle Lat/Lon"); //, new ImageIcon("1.gif")
        popup.add( latlonLinesMenu );
        latlonLinesMenu.setHorizontalTextPosition(JMenuItem.RIGHT);
        latlonLinesMenu.addActionListener(menuListener);
        
        popup.addSeparator();
        
//        checkMark = createImageIcon("/icons/other/emblem-default.png","check");
//        
//        earthPeMenu = new JMenuItem("Blue Marble Image",checkMark);
//        popup.add(earthPeMenu);
//        earthPeMenu.setHorizontalTextPosition(JMenuItem.RIGHT);
//        earthPeMenu.addActionListener(menuListener);
//        
//        earthNoaaMenu = new JMenuItem("NOAA NGDC Image"); //,checkMark
//        popup.add(earthNoaaMenu);
//        earthNoaaMenu.setHorizontalTextPosition(JMenuItem.RIGHT);
//        earthNoaaMenu.addActionListener(menuListener);
//        
//        popup.addSeparator();
        
        moreOptionsMenu = new JMenuItem("More Options..."); //,checkMark
        popup.add(moreOptionsMenu);
        moreOptionsMenu.setHorizontalTextPosition(JMenuItem.RIGHT);
        moreOptionsMenu.addActionListener(menuListener);
        
        // menu properties
        //popup.setLabel("Justification");
        popup.setBorder(new BevelBorder(BevelBorder.RAISED));
        popup.addPopupMenuListener(new PopupPrintListener());
        
        MousePopupListener MouseHandler = new MousePopupListener();
        addMouseListener(MouseHandler);
        addMouseMotionListener(MouseHandler);
        addMouseWheelListener(MouseHandler);
                
    } // ini Object
    
    public void setSatHashTable(Hashtable<String,AbstractSatellite> satHash)
    {
        imageMap.setSatHashTable(satHash);
        
    } // TwoDFrame
    
    // called when component is repainted
 /*   public void paintComponent(Graphics g)
    {
        Graphics2D g2 = (Graphics2D)g;
                Dimension dim = getSize();
                int w = dim.width;
        int h = dim.height;
  
        //System.out.println("test");
        g2.setPaint(Color.black);
        //g.fill
        g2.drawRect(0,0,70,70);
  
  
    }
  */
    /** Returns an ImageIcon, or null if the path was invalid. */
    //protected static ImageIcon createImageIcon(String path, String description)
    public ImageIcon createImageIcon(String path, String description)        
    {
        java.net.URL imgURL = J2DEarthPanel.class.getResource(path);
        if (imgURL != null)
        {
            return new ImageIcon(imgURL, description); // first check in the JAR
        }
        else
        {
            if( (new File(path)).exists() ) // see if file exsists on system
            {
                ImageIcon imag =  new ImageIcon(path, description); // load from file system
                return imag; 
            }
            
            // Can't find file so revert to default - stored in JAR!
            System.out.println("Couldn't find 2d pixmap file: " + path + " -- Reverting to default");
            this.backgroundImagePath = "/images/Earth_PE_small.jpg";
            imgURL = J2DEarthPanel.class.getResource(backgroundImagePath);
            
            return new ImageIcon(imgURL, description);
        }
    }
    
    // Frame resized... scale the image!
    public void componentResized(ComponentEvent e)
    {
        //Component c = e.getComponent();
        //System.out.println("componentResized event from "+ c.getClass().getName()+ "; new size: "+ c.getSize().width+ ", "  + c.getSize().height);
        
        //BufferedImage bimage = getBufferedImage(planetImage);
        
        rescaleAndSetBackgroundImage();
        
    }
    
    public void drawFuzzyLine(Graphics2D g2, Shape path, int thickness)
    {
        //float minThreshehold = 0.05f; // smallest color value visible
        
//       g2.setColor( new Color(0,true));
//       g2.fillRect(0, 0, SIDE1, SIDE2);
       
        BasicStroke newStroke;
        
        for(int t=thickness;t>=1;t=t-2)
        {
            float c = 1.0f-1.0f*t/thickness;
            //if(c<minThreshehold)
            //    c= minThreshehold;
            //System.out.println("t="+t+"c="+c);
            g2.setColor(new Color(1-c,1-c,1-c,c)); // ,c
            //newStroke = new BasicStroke(t, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
            newStroke = new BasicStroke(t, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND);
            g2.setStroke(newStroke);
            g2.draw(path);
        } // for
    } // drawFuzzyLine
    
     /**
    * Takes value from blue channel and copies it to all color channels including alpha channel
    * meant to take a black and white image and using the black level as the transparency level
    * @param dimg buffered image
    */
   public static void makeBlueChannelAlphaValue(BufferedImage dimg)
    {
        //BufferedImage image = loadImage(ref);
//        BufferedImage dimg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
//        Graphics2D g = dimg.createGraphics();
//        //g.setComposite(AlphaComposite.Src);
//        g.drawImage(image, null, 0, 0);
//        g.dispose();
        for(int i = 0; i < dimg.getHeight(); i++)
        {
            for(int j = 0; j < dimg.getWidth(); j++)
            {
                
                int blue = dimg.getRGB(j, i) & 0x000000FF; // current blue value (should be same as other channels)
               
                    Color c = new Color(blue,blue,blue,blue); // 255-blue
                    dimg.setRGB(j, i, c.getRGB());    

//              int a2 = (dimg.getRGB(j, i) >> 24) & 0xff;
//		int r2 = (dimg.getRGB(j, i) >> 16) & 0xff;
//		int gr2 = (dimg.getRGB(j, i) >> 8) & 0xff;
//		int b2 = (dimg.getRGB(j, i) ) & 0xff;
                
            }
        }
        //return dimg;
    } // makeBlueChannelAlphaValue
   
   // assumes both day and night image are already scaled properly, 
   // this just computes a new image mask based on the current time and saves the image to
   // icon setting.
   // THIS IS VERY SLOW (AND MEMORY CONSUMING) -- SEEMS LIKE THERE SHOULD BE A WAY TO IMPROVE
   public void updateEarthLightMaskAndRecombineImage()
   {
       // this better be true if called
       if(this.isShowEarthLightsMask()) // need to scale Earth Lights mask 
       {
           // copy earthLightsCurrentMask so mask isn't applied over and over
           BufferedImage earthLightsCurrentMaskTemp  = new BufferedImage(earthLightsCurrentMask.getWidth(), 
                                                   earthLightsCurrentMask.getHeight(),
                                                   BufferedImage.TYPE_INT_ARGB);
           Graphics2D gg = earthLightsCurrentMaskTemp.createGraphics();
           gg.drawImage(earthLightsCurrentMask, 0, 0, null);
           gg.dispose();
           
            // --- create day/night mask ---
            BufferedImage dayNightMask = new BufferedImage(earthLightsCurrentMask.getWidth(), 
                                                   earthLightsCurrentMask.getHeight(),
                                                   BufferedImage.TYPE_INT_ARGB);
            Graphics2D maskG2D = dayNightMask.createGraphics();
            maskG2D.setColor(new Color(255, 255, 255)); // white
            // fill background with white
            maskG2D.fillRect(0, 0, earthLightsCurrentMask.getWidth(), earthLightsCurrentMask.getHeight());
            
            // get the shape of the dark region
            double[] lla = sun.getCurrentDarkLLA();
            Polygon[] pgons = imageMap.getFootPrintPolygons(lla[0], lla[1], lla[2], imageMap.getNumPtsSunFootPrint());
            
            // draw lines around edges
            int shadowThickness = (int)(imageMap.getZoomFactor()*earthLightsCurrentMask.getWidth()/20.0);// MAKE THIS SETABLE
            for(Polygon p : pgons)
            {
                drawFuzzyLine(maskG2D, p, shadowThickness);
            }
            // fill in polygons with black
            maskG2D.setColor(Color.BLACK);
            for(Polygon p : pgons)
            {
                maskG2D.fill(p);
            }
            
            // done with the 2d graphics object - clean it
            maskG2D.dispose();
            
            // last step, now take black and white image and make alpha channel that matches black intensity
            // had to do this because otherwise when drawing fuzzy line (over and over) tansparency adds up
            makeBlueChannelAlphaValue(dayNightMask);
            
            // --- apply mask to night image ---  
            Graphics2D topG2D = earthLightsCurrentMaskTemp.createGraphics();
            AlphaComposite ac =
                    AlphaComposite.getInstance(AlphaComposite.DST_OUT);
            topG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            topG2D.setComposite(ac);
            topG2D.drawImage(dayNightMask, 0, 0, earthLightsCurrentMask.getWidth(), earthLightsCurrentMask.getHeight(), null);
            topG2D.dispose();      
            
            // --- combine day/night images --- 
            BufferedImage combinedImage = new BufferedImage(earthLightsCurrentMask.getWidth(), 
                                                   earthLightsCurrentMask.getHeight(),
                                                   BufferedImage.TYPE_INT_RGB);
            Graphics2D g =  combinedImage.createGraphics();
            g.drawImage(bimageScaled, 0, 0, null);
            g.drawImage(earthLightsCurrentMaskTemp, 0, 0, null);
            //g.drawImage(dayNightMask,0,0,null);
            //g.drawImage(earthLightsCurrentMask,0,0,null); //earthLightsCurrentMask ,earthLightsFullRes
            g.dispose();
            
            // --- set image icon --- 
            imageMap.setIcon(new ImageIcon(combinedImage));
       } // if show earth light mask
   } // updateEarthLightMaskAndRecombineImage
    
    public void rescaleAndSetBackgroundImage()
    {
        // this is called when the screen has been resize, or zoomed
        // rescales background image and if neeeded earth lights and recreates background image
        bimageScaled = scaleImageMap(bimage);
        
        if(this.isShowEarthLightsMask()) // need to scale Earth Lights mask 
        {
            // first rescale lights image - insure it has an alpha channel
            earthLightsCurrentMask = this.scaleImageMap(earthLightsFullRes,BufferedImage.TYPE_INT_ARGB);
            //earthLightsCurrentMask = this.scaleImageMap(earthLightsFullRes);
            
            // does most of the work :] and sets image to Icon
            updateEarthLightMaskAndRecombineImage();    
        }
        else
        {
            // just set image icon
            imageMap.setIcon(new ImageIcon(bimageScaled));
        }
        
        // ALSO NEED A FUNCTION that doesn't scale... just updates image because Earth Light effect is enambled and new background image is required
        
    }
    
    // overloaded - default to RGB image
    public BufferedImage scaleImageMap(BufferedImage fullResImage)
    {
        return scaleImageMap(fullResImage, java.awt.image.BufferedImage.TYPE_INT_RGB);
    }
    
    /**
     * Scales the image map based on current window size - maintains aspect ratio 
     * of map, returns correctly scaled image. Also auto sets the imagemap lable the 
     * current scale size. Also scales for appropriate zoom level and center.
     * @param fullResImage Input image that is full res.
     * @param imageType image type
     * @return image scaled appropriatly
     */
    public BufferedImage scaleImageMap(BufferedImage fullResImage, int imageType)
    {
        
        //System.out.println("HERE");
        int newWidth=1, newHeight=1;
        if(imageMap.getHeight() != 0)
        {
            if(imageMap.getWidth() / imageMap.getHeight() > aspectRatio)
            {
                // label has larger aspect ratio, constraint by height
                newHeight = imageMap.getHeight();
                newWidth = (int) (imageMap.getHeight()*aspectRatio);
            }
            else
            {
                // label has lower aspect ratio
                newWidth = imageMap.getWidth();
                newHeight = (int) (imageMap.getWidth()*1.0/aspectRatio);
            }
            
            // take into account zoom: (might not be the best way as it can create a huge image in memory... but okay for now)
            // in future may want to calculate the part of the image that will be displayed an clip the rest somehow
            //newWidth = (int)Math.round(newWidth*imageMap.getZoomFactor());
            //newHeight = (int)Math.round(newHeight*imageMap.getZoomFactor());
            
            // first get the sub image that will be scaled up
            //BufferedImage midImage = bimage.getSubimage(int x, int y, int w, int h);
            // gunna need orginal image size, new image size, total image size... etc.
            // center lat,long... zoomfactor.
            
            int midHeight = (int)Math.round( fullResImage.getHeight()/imageMap.getZoomFactor() );
            int midWidth = (int)Math.round( fullResImage.getWidth()/imageMap.getZoomFactor() );
            // uper left corner of image
            int midXupLeft = (int)Math.round( (fullResImage.getWidth()-midWidth)/2.0 + fullResImage.getWidth()*imageMap.getCenterLong()/360.0);
            int midYupLeft = (int)Math.round( (fullResImage.getHeight()-midHeight)/2.0 - fullResImage.getHeight()*imageMap.getCenterLat()/180.0);
            
            // make sure sub image height/width are not <= 0!!
            if(midHeight <= 0)
            {
                midHeight = 1;
            }
            if(midWidth <= 0)
            {
                midWidth = 1;
            }
            
            // check that center lat/long and zoom will not call for a sub image
            // with bounds outside of image...
            // if so then readjust center/lat long so that edges are on map
            
            // check x direction for overflow of image
            if(midXupLeft < 0) // too close to left side
            {
                //System.out.println("midXupLeft ini: " + midXupLeft);
                
                // new center long value: (solve midXupLeft == 0, for centerLong)
                double newCenterLong = (0.0-(fullResImage.getWidth()-midWidth)/2.0)*360.0/fullResImage.getWidth();
                imageMap.setCenterLong( newCenterLong );
                                
                // recalc sub image calculations
                midXupLeft = (int)Math.round( (fullResImage.getWidth()-midWidth)/2.0 + fullResImage.getWidth()*imageMap.getCenterLong()/360.0);
                //System.out.println("midXupLeft final: " + midXupLeft);
            }
            else if(midXupLeft + midWidth > fullResImage.getWidth())
            {
                // solve  midXupLeft + midWidth == fullResImage.getWidth(), for centerLong
                // too close to right side
                double newCenterLong = ((fullResImage.getWidth()-midWidth)-(fullResImage.getWidth()-midWidth)/2.0)*360.0/fullResImage.getWidth();
                imageMap.setCenterLong( newCenterLong );
                
                // recalc sub image calculations
                midXupLeft = (int)Math.round( (fullResImage.getWidth()-midWidth)/2.0 + fullResImage.getWidth()*imageMap.getCenterLong()/360.0);
            }
            
            // check y direction for image corner not at the corner
            if(midYupLeft < 0) // too close to top
            {
                // midYupLeft =  (fullResImage.getHeight()-midHeight)/2.0 - fullResImage.getHeight()*imageMap.getCenterLat()/180.0;
                double newCenterLat =  -((0.0) - (fullResImage.getHeight()-midHeight)/2.0)*180.0/fullResImage.getHeight();
                imageMap.setCenterLat( newCenterLat );
                
                // recalc sub image calculations
                midYupLeft = (int)Math.round( (fullResImage.getHeight()-midHeight)/2.0 - fullResImage.getHeight()*imageMap.getCenterLat()/180.0);
            }
            else if(midYupLeft + midHeight >= fullResImage.getHeight())
            {
                // too close to bottom
                double newCenterLat =  -((fullResImage.getHeight()-midHeight) - (fullResImage.getHeight()-midHeight)/2.0)*180.0/fullResImage.getHeight();
                imageMap.setCenterLat( newCenterLat );
                
                // recalc sub image calculations
                midYupLeft = (int)Math.round( (fullResImage.getHeight()-midHeight)/2.0 - fullResImage.getHeight()*imageMap.getCenterLat()/180.0);
            }
            
            // get sub image that will be scaled to fit window
            BufferedImage midImage = fullResImage.getSubimage(midXupLeft, midYupLeft, midWidth, midHeight);
            
            
//            // draw region outlines if required:
//            if(showLandMassOutlines)
//            {
//                Graphics2D g2 = (Graphics2D)midImage.getGraphics();
//                drawLandMasses(g2, midImage.getWidth(), midImage.getHeight());
//            }
            
            // incon image used to set icon with
            //ImageIcon im; 
            // buffered image that will be returned            
            BufferedImage bim;
            
            // draw region outlines if required:
            if(landMass.isShowLandMassOutlines())
            {
                // get a scaled version of them image to workwith
                Image scaledImage = midImage.getScaledInstance(newWidth,newHeight,imageScalingOption);
                bim = new BufferedImage(newWidth, newHeight, imageType);
                bim.createGraphics().drawImage(scaledImage, 0, 0, null);
                
                Graphics2D g2 = (Graphics2D)bim.getGraphics();
                drawLandMasses(g2, newWidth, newHeight);
                g2.dispose(); // done drawing.
                
                //im = new ImageIcon(bim); // create image icon
            }
            else // no land mass drawing
            {
                // is this slow?
                Image scaledImage = midImage.getScaledInstance(newWidth,newHeight,imageScalingOption);
                bim = new BufferedImage(newWidth, newHeight, imageType);
                bim.createGraphics().drawImage(scaledImage, 0, 0, null);
                
                // just resize buffered image and get going
                //im = new ImageIcon(bim);
            }
             
            // let the imageMap know the current width and height for scale
            imageMap.setImageWidth(newWidth);
            imageMap.setImageHeight(newHeight);
            
            // set the icon of the imageMap
            //imageMap.setIcon(im);  
            // return buffered image
            return bim;
            
        }
        else
        {
            System.out.println("- 2D Image Height=0");
            System.err.println("- 2D Image Height=0");
            return null;
        }
        
        //return
    } // ScaleImageMap
    
    public BufferedImage getBufferedImage(ImageIcon img)
    {
        //Component c = e.getComponent();
        //System.out.println("componentResized event from "+ c.getClass().getName()+ "; new size: "+ c.getSize().width+ ", "  + c.getSize().height);
        
        
        Image image = img.getImage();
        BufferedImage bimageIN = null;
        
        boolean hasAlpha = hasAlpha(image);
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        try
        {
            // Determine the type of transparency of the new buffered image
            int transparency = Transparency.OPAQUE;
            if (hasAlpha)
            {
                transparency = Transparency.BITMASK;
            }
            
            // Create the buffered image
            GraphicsDevice gs = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gs.getDefaultConfiguration();
            bimageIN = gc.createCompatibleImage(
                    image.getWidth(null), image.getHeight(null), transparency);
        }
        catch (HeadlessException e2)
        {
            // The system does not have a screen
        }
        
        if (bimageIN == null)
        {
            // Create a buffered image using the default color model
            int type = BufferedImage.TYPE_INT_RGB;
            if (hasAlpha)
            {
                type = BufferedImage.TYPE_INT_ARGB;
            }
            bimageIN = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
        }
        
        // Copy image to buffered image
        Graphics g = bimageIN.createGraphics();
        
        // Paint the image onto the buffered image
        g.drawImage(image, 0, 0, null);
        g.dispose();
        
        return bimageIN;
        
    }
    
    
    public static boolean hasAlpha(Image image)
    {
        // If buffered image, the color model is readily available
        if (image instanceof BufferedImage)
        {
            BufferedImage bimage2 = (BufferedImage)image;
            return bimage2.getColorModel().hasAlpha();
        }
        
        // Use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
        PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
        try
        {
            pg.grabPixels();
        }
        catch (InterruptedException e)
        {
        }
        
        // Get the image's color model
        ColorModel cm = pg.getColorModel();
        return cm.hasAlpha();
    }
    
    public void itemStateChanged(ItemEvent e)
    {}
    public void componentHidden(ComponentEvent e)
    {}
    public void componentMoved(ComponentEvent e)
    {}
    public void componentShown(ComponentEvent e)
    {}

    public JSatTrak getApp()
    {
        return app;
    }
    
    // PopUp Menu ------------------------
//  An inner class to check whether mouse events are the popup trigger
    class MousePopupListener extends MouseAdapter
    {
        double[] prevll = { Double.NaN , Double.NaN };

        public void mousePressed(MouseEvent e)
        {
            checkPopup(e);
            
            // do something here to see if zoom was enabled.. and not right click
            // if so ... zoom in/out (set factor, and rescale image... etc).
            if(e.getButton() == 2) // middle button prints out lat/long
            {
                System.out.println("Loc (x,y): " + e.getX() + ", " + e.getY() );
                double[] ll =  imageMap.findLLfromXY(e.getX(), e.getY(), imageMap.getLastTotalWidth(), imageMap.getLastTotalHeight(), imageMap.getImageWidth(), imageMap.getImageHeight());
                
                // in degrees
                System.out.println("Loc (l,l): " + ll[0] + ", " + ll[1] +"\n");
            }
            
            if(e.getButton() == 1 && zoomInToggleButton.isSelected())
            {
                // get lat/long click first before setting new zoom factor
                double[] ll =  imageMap.findLLfromXY(e.getX(), e.getY(), imageMap.getLastTotalWidth(), imageMap.getLastTotalHeight(), imageMap.getImageWidth(), imageMap.getImageHeight());
                
                //System.out.println("Zoom In");
                // new zoom factor
                double newZoomFactor = imageMap.getZoomFactor()*imageMap.getZoomIncrementMultiplier();
                //newZoomFactor = Math.round(newZoomFactor*100.0)/100.0;
                imageMap.setZoomFactor( newZoomFactor );
                //System.out.println("Zoom Factor: " + newZoomFactor);
                
                // set center lat / long where clicked
                imageMap.setCenterLat(ll[0]);
                imageMap.setCenterLong(ll[1]);
                
                // rescale
                rescaleAndSetBackgroundImage();
                
            } // zoom in
            
            if(e.getButton() == 1 && zoomOutToggleButton.isSelected())
            {
                // get lat/long click first before setting new zoom factor
                double[] ll =  imageMap.findLLfromXY(e.getX(), e.getY(), imageMap.getLastTotalWidth(), imageMap.getLastTotalHeight(), imageMap.getImageWidth(), imageMap.getImageHeight());
                    
                //System.out.println("Zoom Out");
                double newZoomFactor =  imageMap.getZoomFactor()/imageMap.getZoomIncrementMultiplier();
                imageMap.setZoomFactor( newZoomFactor );
                             
                if(imageMap.getZoomFactor() <= 1.00001) // don't let it be less than 1
                {
                    imageMap.setZoomFactor(1.0);
                    
                    // reset center lat/long to 0.0 -- recenter image
                    imageMap.setCenterLat(0.0);
                    imageMap.setCenterLong(0.0); 
                }
                else
                {
                    // set center lat / long where clicked
                    imageMap.setCenterLat(ll[0]);
                    imageMap.setCenterLong(ll[1]);
                }
                
                //System.out.println("Zoom Factor: " + imageMap.getZoomFactor());
                
                // rescale
                rescaleAndSetBackgroundImage();
            }
            
            if(e.getButton() == 1 && recenterToggleButton.isSelected())
            {
                // just recenter
                double[] ll =  imageMap.findLLfromXY(e.getX(), e.getY(), imageMap.getLastTotalWidth(), imageMap.getLastTotalHeight(), imageMap.getImageWidth(), imageMap.getImageHeight());
                
                // save lat,long
                imageMap.setCenterLat(ll[0]);
                imageMap.setCenterLong(ll[1]);
                    
                // rescale
                rescaleAndSetBackgroundImage();
                
            }
            
            // double click - reset zoom back to 1
            if(e.getClickCount() == 2 && e.getButton() == 1)
            {
                // set zoomFactor back to 1.0 and repaint/rescale
                imageMap.setZoomFactor(1.0);
                
                // reset center lat/long to 0.0 -- recenter image
                imageMap.setCenterLat(0.0);
                imageMap.setCenterLong(0.0);
                
                // rescale
                rescaleAndSetBackgroundImage();
            }
            
            // if middle button hit, turn off zoom in/out  buttons
            if(e.getButton() == e.BUTTON2)
            {
                zoomInToggleButton.setSelected(false);
                zoomOutToggleButton.setSelected(false); 
                recenterToggleButton.setSelected(false);
            }
            
        } // mousePressed
        
        public void mouseClicked(MouseEvent e)
        {
            checkPopup(e);
        }
        
        public void mouseReleased(MouseEvent e)
        {
            checkPopup(e);

            prevll[0] = Double.NaN;
            prevll[1] = Double.NaN;
        }

        public void mouseDragged(MouseEvent e)
        {
            // acquire new lat and long
            double[] ll =  imageMap.findLLfromXY(e.getX(), e.getY(), imageMap.getLastTotalWidth(), imageMap.getLastTotalHeight(), imageMap.getImageWidth(), imageMap.getImageHeight());

            // shift to new lat and long
            if( Double.isNaN(prevll[0]) || Double.isNaN(prevll[1]) )
            {
                prevll = ll;
            }
            else
            {
                imageMap.setShiftLat( prevll[0] - ll[0] );
                imageMap.setShiftLong( prevll[1] - ll[1] );
            }

            // rescale
            rescaleAndSetBackgroundImage();
        }

        public void mouseWheelMoved(MouseWheelEvent e)
        {
            // get lat/long click first before setting new zoom factor
            double[] ll =  imageMap.findLLfromXY(e.getX(), e.getY(), imageMap.getLastTotalWidth(), imageMap.getLastTotalHeight(), imageMap.getImageWidth(), imageMap.getImageHeight());
            if( e.getWheelRotation() > 0 )
            {
                // new zoom factor
                double newZoomFactor = imageMap.getZoomFactor()*imageMap.getZoomIncrementMultiplier();
                //newZoomFactor = Math.round(newZoomFactor*100.0)/100.0;
                imageMap.setZoomFactor( newZoomFactor );
                //System.out.println("Zoom Factor: " + newZoomFactor);

                // set center lat / long where clicked
                imageMap.setCenterLat(ll[0]);
                imageMap.setCenterLong(ll[1]);
            }   // Image zoom in
            else
            {
                double newZoomFactor =  imageMap.getZoomFactor()/imageMap.getZoomIncrementMultiplier();
                imageMap.setZoomFactor( newZoomFactor );

                if(imageMap.getZoomFactor() <= 1.00001) // don't let it be less than 1
                {
                    imageMap.setZoomFactor(1.0);

                    // reset center lat/long to 0.0 -- recenter image
                    imageMap.setCenterLat(0.0);
                    imageMap.setCenterLong(0.0);
                }
                else
                {
                    // set center lat / long where clicked
                    imageMap.setCenterLat(ll[0]);
                    imageMap.setCenterLong(ll[1]);
                }
            }   // Image zoom out
            rescaleAndSetBackgroundImage();
        }
        
        private void checkPopup(MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
                popup.show(J2DEarthPanel.this, e.getX(), e.getY());
            }
        }
    } // MousePopupListener class
    
    // An inner class to show when popup events occur
    class PopupPrintListener implements PopupMenuListener
    {
        public void popupMenuWillBecomeVisible(PopupMenuEvent e)
        {
            //System.out.println("Popup menu will be visible!");
        }
        
        public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
        {
            //System.out.println("Popup menu will be invisible!");
        }
        
        public void popupMenuCanceled(PopupMenuEvent e)
        {
            //System.out.println("Popup menu is hidden!");
        }
    }  // PopupPrintListener class
    
    
    
    
    // get and set Options for saving and loading file
    public boolean getShowLatLonLines()
    {
        return imageMap.showLatLong;
    }
    
    public void setShowLatLonLines(boolean bool)
    {
        imageMap.showLatLong = bool;
        imageMap.repaint();
    }
    
    public String getTwoDMap()
    {
        //return imageMapNum; //0=PE, 1=NOAA
        return backgroundImagePath;
    }
    
    //public void setTwoDMap(int map)
    public void setTwoDMap(String map)
    {
        if(map.equalsIgnoreCase(backgroundImagePath))
        {
            // do nothing
            return;
        }
        
        backgroundImagePath = map;
        ImageIcon temp = createImageIcon(backgroundImagePath,"Earth");
        bimage = getBufferedImage(temp);
        rescaleAndSetBackgroundImage(); // repaint the image
        
//        switch(map)
//        {
//            case 0:
//                ImageIcon temp = createImageIcon("/images/Earth_PE_small.png","Earth");
//                bimage = getBufferedImage(temp);
//                ScaleImageMap(); // repaint the image
//                earthNoaaMenu.setIcon(null);
//                earthPeMenu.setIcon(checkMark);
//                imageMapNum = 0; //0=PE, 1=NOAA
//                break;
//            case 1:
//                temp = createImageIcon("/images/Earth_NOAA_NGDC_small.png","Earth");
//                bimage = getBufferedImage(temp);
//                ScaleImageMap(); // repaint the image
//                earthNoaaMenu.setIcon(checkMark);
//                earthPeMenu.setIcon(null);
//                imageMapNum = 1; //0=PE, 1=NOAA
//                break;
//            default:
//                temp = createImageIcon("/images/Earth_NOAA_NGDC_small.png","Earth");
//                bimage = getBufferedImage(temp);
//                ScaleImageMap(); // repaint the image
//                earthNoaaMenu.setIcon(checkMark);
//                earthPeMenu.setIcon(null);
//                imageMapNum = 1; //0=PE, 1=NOAA
//                break;
//        } // switch
    } // setTwoDMap

    public Color getBackgroundColor()
    {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor)
    {
        this.setBackground(backgroundColor);
        
        this.backgroundColor = backgroundColor;
        imageMap.setBackgroundColor(backgroundColor);
    }

//    public int getImageMapNum()
//    {
//        return imageMapNum;
//    }

//    public void setImageMapNum(int imageMapNum)
//    {
//        this.imageMapNum = imageMapNum;
//        
//        if( imageMapNum == 0)
//        {
//            ImageIcon temp = createImageIcon("/images/Earth_PE_small.png","Earth");
//            bimage = getBufferedImage(temp);
//            ScaleImageMap(); // repaint the image
//            earthNoaaMenu.setIcon(null);
//            earthPeMenu.setIcon(checkMark);
//            
//            imageMapNum = 0; //0=PE, 1=NOAA
//            
//        }
//        
//        if( imageMapNum == 1)
//        {
//            ImageIcon temp = createImageIcon("/images/Earth_NOAA_NGDC_small.png","Earth");
//            bimage = getBufferedImage(temp);
//            ScaleImageMap(); // repaint the image
//            earthNoaaMenu.setIcon(checkMark);
//            earthPeMenu.setIcon(null);
//            
//            imageMapNum =1; //0=PE, 1=NOAA
//        }
//    } // setImageMapNum
    
    
    // Graw on landmasses
    private void drawLandMasses(Graphics2D g2, int width, int height)
    {
        //System.out.println("drawing land masses");
        
        // --- land mass -------------------
        if(landMass.isShowLandMassOutlines())
        {
            g2.setPaint(landMass.getLandOutlineColor());
            
            // drawing disconnects across international date line not accounted for
            for(String key : landMass.getLandMassHash().keySet())
            {
                Vector<float[]> vec = landMass.getLandMassVector(key);
                
                if( vec.size() > 0 )
                {
                    
                    //System.out.println("here");
                    
                    float[] lastLL = vec.elementAt(0);
                    //int[] lastXY = imageMap.findXYfromLL(lastLL[0], lastLL[1], w, h, imageWidth, imageHeight);
                    int[] lastXY = imageMap.findXYfromLL(lastLL[0], lastLL[1], width, height, width, height);
                    
                    float[] currLL;
                    int[] currXY;
                    
                    // speed increase
                    int[] xPts = new int[vec.size() + 1];
                    int[] yPts = new int[vec.size() + 1];
                    int ptsCount = 0; // points to draw stored up (reset when discontinutiy is hit)
                    // first point
                    xPts[ptsCount] = lastXY[0];
                    yPts[ptsCount] = lastXY[1];
                    ptsCount++;
                    
                    
                    for(int i=1;i<vec.size();i+=indexSpacing)
                    {
                        currLL = vec.elementAt(i);
                        //currXY = imageMap.findXYfromLL(currLL[0], currLL[1], w, h, imageWidth, imageHeight);
                        currXY = imageMap.findXYfromLL(currLL[0], currLL[1], width, height, width, height);
                        
                        // slow drawing
                        //g2.drawLine(lastXY[0],lastXY[1],currXY[0],currXY[1]);
                        // new way
                        xPts[ptsCount] = currXY[0];
                        yPts[ptsCount] = currXY[1];
                        ptsCount++;
                        
                        // save current to old
                        lastXY = currXY;
                        
                    }
                    
                    // first position
                    currLL = vec.elementAt(0);
                    currXY = imageMap.findXYfromLL(currLL[0], currLL[1], width, height, width, height);
                    
                    // only draw end point connection if not too far away
                    if(Math.sqrt( Math.pow(currXY[0] - lastXY[0],2)+Math.pow(currXY[1] - lastXY[1],2)) > 15)
                    {
                        //System.out.println(">15 detected: " + key + ", end");
                    }
                    else
                    {
                        // connect back to first
                        // old way
                        //g2.drawLine(lastXY[0],lastXY[1],currXY[0],currXY[1]);
                        // new way
                        xPts[ptsCount] = currXY[0];
                        yPts[ptsCount] = currXY[1];
                        ptsCount++;
                    }
                    
                    // NEW FASTER Drawing of lines
                    g2.drawPolyline(xPts, yPts, ptsCount);
                    
                }
                
            } // for each land mass
            
        } // show land mass
        // ---- end: land mass --------------
    }
    
    public double getZoomIncrementMultiplier()
    {
        return imageMap.getZoomIncrementMultiplier();
    }

    public void setZoomIncrementMultiplier(double zoomIncrementMultiplier)
    {
        imageMap.setZoomIncrementMultiplier(zoomIncrementMultiplier);
    }

    public int getImageScalingOption()
    {
        return imageScalingOption;
    }

    public void setImageScalingOption(int imageScalingOption)
    {
        this.imageScalingOption = imageScalingOption;
    }
    
    public boolean isShowDateTime()
    {
        return imageMap.isShowDateTime();
    }

    public void setShowDateTime(boolean showDateTime)
    {
        imageMap.setShowDateTime(showDateTime);
    }
    
    public int getXDateTimeOffset()
    {
        return imageMap.getXDateTimeOffset();
    }

    public void setXDateTimeOffset(int xDateTimeOffset)
    {
        imageMap.setXDateTimeOffset( xDateTimeOffset );
    }

    public int getYDateTimeOffset()
    {
        return imageMap.getYDateTimeOffset();
    }

    public void setYDateTimeOffset(int yDateTimeOffset)
    {
        imageMap.setYDateTimeOffset(yDateTimeOffset);
    }

    public Color getDateTimeColor()
    {
        return imageMap.getDateTimeColor();
    }

    public void setDateTimeColor(Color dateTimeColor)
    {
        imageMap.setDateTimeColor(dateTimeColor);
    }
    
//    public void prepare2Save()
//    {
//        // routines to prepare object for saving to make file size smaller
//        this.setSize(1,1);
//    }
     public double getCenterLat()
    {
        return imageMap.getCenterLat();
    }

    public void setCenterLat(double centerLat)
    {
        imageMap.setCenterLat(centerLat);
    }

    public double getCenterLong()
    {
        return imageMap.getCenterLong();
    }

    public void setCenterLong(double centerLong)
    {
        imageMap.setCenterLong(centerLong);
    }

    public double getZoomFactor()
    {
        return imageMap.getZoomFactor();
    }

    public void setZoomFactor(double zoomFactor)
    {
        imageMap.setZoomFactor(zoomFactor);
    }
    
    public boolean isDrawSun()
    {
        return imageMap.isDrawSun();
    }

    public void setDrawSun(boolean drawSun)
    {
        imageMap.setDrawSun(drawSun);
    }

    public Color getSunColor()
    {
        return imageMap.getSunColor();
    }

    public void setSunColor(Color sunColor)
    {
        imageMap.setSunColor(sunColor);
    }

    public int getNumPtsSunFootPrint()
    {
        return imageMap.getNumPtsSunFootPrint();
    }

    public void setNumPtsSunFootPrint(int numPtsSunFootPrint)
    {
        imageMap.setNumPtsSunFootPrint(numPtsSunFootPrint);
    }

    public float getSunAlpha()
    {
        return imageMap.getSunAlpha();
    }

    public void setSunAlpha(float sunAlpha)
    {
        imageMap.setSunAlpha(sunAlpha);
    }
    
    public String getPathRegionFiles()
    {
        return landMass.getRootDir();
    }
    
    public String getRegionFileName()
    {
        return landMass.getDataFileName();
    }
    
    public boolean isRegionDrawOn()
    {
        return landMass.isShowLandMassOutlines();
    }
    
    public Color getRegionLineColor()
    {
        return landMass.getLandOutlineColor();
    }  
    
    public void setRegionDrawingOptions(boolean bool, String fileName, Color color)
    {
        // save old data
        String oldName = getRegionFileName(); // save old
        boolean preBool = landMass.isShowLandMassOutlines();
        Color prevColor = landMass.getLandOutlineColor();
        
        //save data
        landMass.setRegionOptions(bool,fileName,color);
        
        // see if any changes
        if(!oldName.equalsIgnoreCase(fileName) || preBool != bool || prevColor != color)
        {
            rescaleAndSetBackgroundImage(); // repaint the image
        }
        
    } // setRegionDrawingOptions
    
    /**
     * Add renderable object to the 2D Earth Label
     * @param renderable
     */
    public void addRenderableObject(JSatTrakRenderable renderable)
    {
        if(!checkIfIncludesRenderableObject(renderable))
        {
            imageMap.addRenderableObject(renderable);
        }
    }
    
    public boolean checkIfIncludesRenderableObject(JSatTrakRenderable renderable)
    {
        // check to see if object is in list
        for(JSatTrakRenderable ren : imageMap.getRenderableObjects())
        {
            //if(ren instanceof CoverageAnalyzer)
            if(ren.equals(renderable))
            {   
                return true;
            }
        }
        return false;
    }
    
    public void removeRenderableObject(JSatTrakRenderable renderable)
    {
        Vector<JSatTrakRenderable> renVec = imageMap.getRenderableObjects();
        for(int i=0;i<renVec.size();i++)
        {
            //if(ren instanceof CoverageAnalyzer)
            if(renVec.get(i).equals(renderable))
            {   
                // remove it
                renVec.remove(renderable);
            }
        }
    }
    
    // earth lights options and data - images stored in the Panel
    public boolean isShowEarthLightsMask()
    {
        return imageMap.isShowEarthLightsMask();
    }

    public void setShowEarthLightsMask(boolean showEarthLights)
    {
        // see if the value has changed, if so take care of this
        if( showEarthLights != isShowEarthLightsMask())
        {
            // set value to the label 
            imageMap.setShowEarthLightsMask(showEarthLights);
            
            if(showEarthLights) // if show effect
            {
                // do we need to load the image? (could have already been loaded)
                if(earthLightsFullRes == null)
                {
                    ImageIcon lightsImage = createImageIcon(earthLightsMaskImagePath,"Earth Lights");
                    earthLightsFullRes = getBufferedImage(lightsImage);
                }
                
                // need to resize and rescale image
                rescaleAndSetBackgroundImage();
            }
            else// no light effect
            {
                // remove data
                //earthLightsFullRes = null; // delete // save in case we need it later
                this.earthLightsCurrentMask = null; // delete
                
                // need to resize and rescale image - reset!
                rescaleAndSetBackgroundImage();
                
            } // no light effect
                
        } // if there is a change
        
        
        
    } // setShowEarthLightsMask
    
    public J2dEarthLabel2 getImageMap()
    {
        return imageMap;
    }
    
    
    /**
     * Performs an offscreen rendering of panel, works best when used with "getNonDisplayed2DEarthPanel()" function in JSatTrak class
     * @return
     */
    public BufferedImage offScreenRender()
    {
        BufferedImage buff = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB );
        Graphics g = buff.getGraphics();
        getImageMap().paintComponent(g);
        return buff;
    }
    
}
