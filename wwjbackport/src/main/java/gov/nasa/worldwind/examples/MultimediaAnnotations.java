/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.examples.util.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.pick.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.BasicDragger;

import javax.sound.sampled.*;
import java.awt.*;
import java.io.File;
import java.net.URL;

/**
 * Testing audio playback
 *
 * @author Patrick Murris
 * @version $Id: MultimediaAnnotations.java 11475 2009-06-06 01:39:21Z tgaskins $
 */
public class MultimediaAnnotations extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        private final static String HELLO_AUDIO_FILE_PATH = "http://simplythebest.net/sounds/WAV/events_WAV/event_WAV_files/welcome.wav";
        //private final static String IDENTIFICATION_AUDIO_FILE_PATH = "http://simplythebest.net/sounds/WAV/WAV_files/TV_show_WAV_files/identification.wav";
        //private final static String AUDIO_FILE_PATH = "demodata/boing.wav";

        private final static String MSH_IMAGE_FILE_PATH = "http://worldwind.arc.nasa.gov/java/demos/images/MountStHelens_01_800.jpg";
        private final static String MSH_IMAGE_FILE_PATH1 = "http://upload.wikimedia.org/wikipedia/commons/thumb/8/8c/Sthelens1.jpg/800px-Sthelens1.jpg";
        //private final static String IMAGE_FILE_PATH = "images/400x230-splash-nww.png";

        private final static String FILE_ATTACHMENT = "FileAttachment";
        private final static String PLAY_AUDIO_CMD = "play:audio";
        private final static String PLAY_IMAGE_CMD = "play:image";
        private final static String PLAY_SLIDES_CMD = "play:slides";

        private Annotation currentAnnotation;
        private Annotation lastPickedObject;
        private Color savedBorderColor;

        private AudioPlayerDialog playerDialog;
        private ImageViewerDialog viewerDialog;
        private SlideShowPlayer slideShowPlayer;

        public AppFrame()
        {
            super(true, true, false);

            // Create annotation layer
            AnnotationLayer layer = new AnnotationLayer();
            layer.setName("Annotations");
            insertBeforePlacenames(getWwd(), layer);
            getLayerPanel().update(getWwd());

            // Setup annotation attributes
            AnnotationAttributes aa = new AnnotationAttributes();
            aa.setBackgroundColor(Color.WHITE);
            aa.setBorderColor(Color.BLACK);
            aa.setSize(new Dimension(240, 0));
            aa.setHighlightScale(1);
            aa.setInsets(new Insets(12, 12, 12, 20));
            aa.setFont(Font.decode("SansSerif-PLAIN-14"));
            aa.setTextColor(Color.BLACK);

            // Create an annotation with an attached slide show
            GlobeAnnotation ga;
            ga = new GlobeAnnotation("<p>\n<b><font color=\"#664400\">MOUNT SAINT HELENS</font></b><br />"
                + "\n<i>Alt: 1404m</i></p><p>Mount St. Helens is an active stratovolcano located in Skamania "
                + "County, Washington.</p>", Position.fromDegrees(46.2000, -122.1882, 0), aa);
            // Attach an image/caption encoded string array to the annotation and add an hyperlink to view them
            String[] slideList = new String[]
            {
                MSH_IMAGE_FILE_PATH1 + ";Mt Saint Helens before May 1980",
                MSH_IMAGE_FILE_PATH + ";Mt Saint Helens May 1982",

            };
            ga.setValue(FILE_ATTACHMENT, slideList);
            ga.setText(ga.getText() + "<br /><a href=\"" + PLAY_SLIDES_CMD + "\">View slides</a>...");
            layer.addAnnotation(ga);

            // Add an annotation with sound
            ga = new GlobeAnnotation("<p>Welcome...</p>", Position.fromDegrees(44, -100, 0), aa);
            // Attach a sound file to the annotation and add an hyperlink for play back
            ga.setValue(FILE_ATTACHMENT, HELLO_AUDIO_FILE_PATH);
            ga.setText(ga.getText() + "<br /><a href=\"" + PLAY_AUDIO_CMD + "\">Play audio</a>...");
            layer.addAnnotation(ga);

            // Add select listener
            setupSelectListener();
        }

        private void setupSelectListener()
        {
            // Add a select listener to select or highlight annotations on rollover, and playback audio
            this.getWwd().addSelectListener(new SelectListener()
            {
                private BasicDragger dragger = new BasicDragger(getWwd());

                public void selected(SelectEvent event)
                {
                    if (event.hasObjects() && event.getTopObject() instanceof Annotation)
                    {
                        // Handle cursor change on hyperlink
                        if (event.getTopPickedObject().getValue(AVKey.URL) != null)
                            getWwd().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        else
                            getWwd().setCursor(Cursor.getDefaultCursor());
                    }

                    // Select/unselect on left click on annotations
                    if (event.getEventAction().equals(SelectEvent.LEFT_CLICK))
                    {
                        if (event.hasObjects())
                        {
                            if (event.getTopObject() instanceof Annotation)
                            {
                                // Check for text or url
                                PickedObject po = event.getTopPickedObject();
                                if(po.getValue(AVKey.TEXT) != null)
                                {
                                    // Check for audio play url command
                                    String url = (String)po.getValue(AVKey.URL);
                                    if (url != null && url.equals(PLAY_AUDIO_CMD))
                                        playAnnotationAudio((GlobeAnnotation)event.getTopObject(), event.getPickPoint());
                                        // Check for image play url command
                                    else if (url != null && url.equals(PLAY_IMAGE_CMD))
                                        playAnnotationImage((GlobeAnnotation)event.getTopObject(), event.getPickPoint());
                                        // Check for slides play command
                                    else if (url != null && url.equals(PLAY_SLIDES_CMD))
                                        playAnnotationSlides((GlobeAnnotation)event.getTopObject(), event.getPickPoint());


                                    if(AppFrame.this.currentAnnotation == event.getTopObject())
                                        return;
                                }
                                // Left click on an annotation - select
                                if(AppFrame.this.currentAnnotation != null)
                                {
                                    // Unselect current
                                    AppFrame.this.currentAnnotation.getAttributes().setBorderColor(AppFrame.this.savedBorderColor);
                                }
                                if(AppFrame.this.currentAnnotation != event.getTopObject())
                                {
                                    // Select new one if not current one already
                                    AppFrame.this.currentAnnotation = (Annotation)event.getTopObject();
                                    AppFrame.this.savedBorderColor = AppFrame.this.currentAnnotation.getAttributes().getBorderColor();
                                    AppFrame.this.currentAnnotation.getAttributes().setBorderColor(Color.YELLOW);
                                }
                                else
                                {
                                    // Clear current annotation
                                    AppFrame.this.currentAnnotation = null; // switch off
                                }
                            }
                            else
                                System.out.println("Left click on " + event.getTopObject());

                        }
                    }
                    // Highlight on rollover
                    else if (event.getEventAction().equals(SelectEvent.ROLLOVER) && !this.dragger.isDragging())
                    {
                        AppFrame.this.highlight(event.getTopObject());
                    }
                    // Have drag events drag the selected object.
                    else if (event.getEventAction().equals(SelectEvent.DRAG_END)
                        || event.getEventAction().equals(SelectEvent.DRAG))
                    {
                        if (event.hasObjects())
                        {
                            // If selected annotation delegate dragging computations to a dragger.
                            if(event.getTopObject() == AppFrame.this.currentAnnotation)
                                this.dragger.selected(event);
                        }

                        // We missed any roll-over events while dragging, so highlight any under the cursor now,
                        // or de-highlight the dragged shape if it's no longer under the cursor.
                        if (event.getEventAction().equals(SelectEvent.DRAG_END))
                        {
                            PickedObjectList pol = getWwd().getObjectsAtCurrentPosition();
                            if (pol != null)
                            {
                                AppFrame.this.highlight(pol.getTopObject());
                                AppFrame.this.getWwd().repaint();
                            }
                        }
                    }

                }
            });
        }

        private void highlight(Object o)
        {
            // Manage highlighting of Annotations.
            if (this.lastPickedObject == o)
                return; // same thing selected

            // Turn off highlight if on.
            if (this.lastPickedObject != null) // && this.lastPickedObject != this.currentAnnotation)
            {
                this.lastPickedObject.getAttributes().setHighlighted(false);
                this.lastPickedObject = null;
            }

            // Turn on highlight if object selected.
            if (o != null && o instanceof Annotation)
            {
                this.lastPickedObject = (Annotation) o;
                this.lastPickedObject.getAttributes().setHighlighted(true);
            }
        }

        private void playAnnotationAudio(GlobeAnnotation annotation, Point pickPoint)
        {
            String filePath = (String)annotation.getValue(FILE_ATTACHMENT);
            if (filePath == null)
                return;

            System.out.println("Play annotation audio: " + filePath);
            // Determine screen location
            Point cursorLocation = getCursorPoint(pickPoint);
            // Start audio player dialog
            try
            {
                URL url = getResourceURL(filePath);
                Clip clip = openAudioURL(url);
                AudioPlayer player = new AudioPlayer(clip);
                if (this.playerDialog == null)
                    this.playerDialog = new AudioPlayerDialog();
                playerDialog.setAudioPlayer(player);
                playerDialog.setAlwaysOnTop(true);
                playerDialog.setLocation(cursorLocation);
                playerDialog.setVisible(true);
                player.play(); // start playing right away
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        private void playAnnotationImage(GlobeAnnotation annotation, Point pickPoint)
        {
            String filePath = (String)annotation.getValue(FILE_ATTACHMENT);
            if (filePath == null)
                return;

            System.out.println("Play annotation image: " + filePath);
            // Determine screen location
            Point cursorLocation = getCursorPoint(pickPoint);
            // Start image viewer dialog
            if (this.viewerDialog == null)
                viewerDialog = new ImageViewerDialog();
            viewerDialog.getImageViewer().setImageURL(getResourceURL(filePath));
            viewerDialog.getImageViewer().setScaleMode(ImageViewer.SCALE_BEST_FIT);
            viewerDialog.setLocation(cursorLocation);
            viewerDialog.setAlwaysOnTop(true);
            viewerDialog.setDropEnabled(true);
            viewerDialog.setVisible(true);
        }

        private void playAnnotationSlides(GlobeAnnotation annotation, Point pickPoint)
        {
            String[] files = (String[])annotation.getValue(FILE_ATTACHMENT);
            if (files == null)
                return;

            System.out.println("Play annotation slides: " + files.length);
            // Determine screen location
            Point cursorLocation = getCursorPoint(pickPoint);
            // Start image viewer dialog
            if (this.slideShowPlayer == null)
                slideShowPlayer = new SlideShowPlayer();
            slideShowPlayer.clear();
            // Add slides
            for (String filePath : files)
            {
                String[] values = filePath.split(";");
                URL fileURL = getResourceURL(values[0]);
                String caption = values.length > 1 ? values[1] : filePath;
                slideShowPlayer.addSlide(new SlideShowPlayer.Slide(fileURL, caption));
            }
            slideShowPlayer.getImageViewer().setScaleMode(ImageViewer.SCALE_BEST_FIT);
            slideShowPlayer.setLocation(cursorLocation);
            slideShowPlayer.setAlwaysOnTop(true);
            slideShowPlayer.setDropEnabled(true);
            slideShowPlayer.setVisible(true);
        }

        private static Clip openAudioURL(java.net.URL url) throws Exception
        {
            Clip clip = null;

            AudioInputStream ais = null;
            try
            {
                ais = AudioSystem.getAudioInputStream(url);
                DataLine.Info info = new DataLine.Info(Clip.class, ais.getFormat());
                clip = (Clip) AudioSystem.getLine(info);
                clip.open(ais);
            }
            finally
            {
                if (ais != null)
                {
                    ais.close();
                }
            }

            return clip;
        }

        private URL getResourceURL(String path)
        {
            try
            {
                File f = new File(path);
                if (f.exists())
                    return f.toURI().toURL();

                URL url = this.getClass().getResource("/" + path);
                if (url != null)
                    return url;

                return new URL(path);
            }
            catch (Exception ignore) { }

            return null;
        }

        private Point getCursorPoint(Point pickPoint)
        {
            Point wwdLocation = getWwd().getLocationOnScreen();
            return new Point(wwdLocation.x + pickPoint.x,  wwdLocation.y + pickPoint.y);
        }

    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Multimedia Annotations", AppFrame.class);
    }
}
