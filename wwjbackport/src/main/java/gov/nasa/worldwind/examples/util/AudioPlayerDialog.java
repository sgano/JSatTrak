/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples.util;

import gov.nasa.worldwind.avlist.AVKey;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Plays an audio file.
 *
 * @author Patrick Murris
 * @version $Id: AudioPlayerDialog.java 10542 2009-04-27 17:28:30Z dcollins $
 */
public class AudioPlayerDialog extends JFrame
{
    protected final String TITLE_TEXT = "Audio Player";
    protected final String STOP_TEXT = "Stop";
    protected final String PLAY_TEXT = "Play";
    protected final String PAUSE_TEXT = "Pause";

    private final AudioPlayerPanel panel;

    public AudioPlayerDialog()
    {
        this.setTitle(TITLE_TEXT);
        this.setLayout(new BorderLayout());      
        this.panel = new AudioPlayerPanel();
        this.add(this.panel, BorderLayout.CENTER);
        this.pack();
    }

    public AudioPlayer getAudioPlayer()
    {
        return this.panel.getAudioPlayer();
    }

    public void setAudioPlayer(AudioPlayer player)
    {
        this.panel.setAudioPlayer(player);
    }


    public class AudioPlayerPanel extends JPanel
    {
        private AudioPlayer player;

        private JButton playButton;
        private JButton stopButton;
        private JSlider positionSlider;
        private JLabel timeLabel;

        private boolean suspendEvents = false;

        public AudioPlayerPanel()
        {
            // Init components
            initComponents();

            // Start update timer
            Timer updateTimer = new Timer(100, new ActionListener()
            {
                public void actionPerformed(ActionEvent event)
                {
                    update();
                }
            });
            updateTimer.start();
        }

        public AudioPlayer getAudioPlayer()
        {
            return this.player;
        }

        public void setAudioPlayer(AudioPlayer player)
        {
            if (this.player != null)
                this.player.stop();
            
            this.player = player;
            this.update();
        }

        private void initComponents()
        {
            this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

            JPanel controlPanel = new JPanel();
            controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.LINE_AXIS));
            {
                stopButton = new JButton(STOP_TEXT);
                stopButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        if (player != null)
                            player.stop();
                        update();
                    }
                });
                controlPanel.add(stopButton);

                playButton = new JButton(PLAY_TEXT);
                playButton.setPreferredSize(new Dimension(80, 20));
                playButton.addActionListener(new ActionListener()
                {
                    public void actionPerformed(ActionEvent event)
                    {
                        if (player != null)
                        {
                            System.out.println("Audio player status: " + player.getStatus() + ", pos: " + player.getMillisecondPosition() + " / " + player.getMillisecondLength());
                            if(player.getStatus().equals(AVKey.PLAY))
                                player.pause();
                            else
                                player.play();
                            update();
                        }
                    }
                });
                controlPanel.add(playButton);
                controlPanel.add(Box.createHorizontalStrut(10));

                positionSlider = new JSlider(0, 1000, 0);
                positionSlider.setPreferredSize(new Dimension(100, 20));
                positionSlider.addChangeListener(new ChangeListener()
                {
                    public void stateChanged(ChangeEvent event)
                    {
                        if (player != null && !suspendEvents)
                        {
                            long newPos = (long)((float)positionSlider.getValue() / 1000f
                                * (float)player.getMillisecondLength());
                            player.setMillisecondPosition(newPos);
                            updateTimeLabel();
                        }
                    }
                });
                controlPanel.add(positionSlider);
                controlPanel.add(Box.createHorizontalStrut(4));

                timeLabel = new JLabel("00:00");
                controlPanel.add(timeLabel);
                controlPanel.add(Box.createHorizontalStrut(10));

            }
            this.add(controlPanel);
        }

        private void update()
        {
            if (this.suspendEvents)
                return;

            this.suspendEvents = true;
            {
                if (player != null)
                {
                    if (player.getStatus().equals(AVKey.STOP))
                        stopButton.setEnabled(false);
                    else
                        stopButton.setEnabled(true);

                    playButton.setEnabled(true);
                    if (player.getStatus().equals(AVKey.PLAY))
                        playButton.setText(PAUSE_TEXT);
                    else
                        playButton.setText(PLAY_TEXT);

                    positionSlider.setEnabled(true);
                    int pos = (int)((float)player.getMillisecondPosition() / player.getMillisecondLength() * 1000);
                    positionSlider.setValue(pos);

                    updateTimeLabel();
                }
                else
                {
                    stopButton.setEnabled(false);
                    playButton.setEnabled(false);
                    positionSlider.setEnabled(false);
                    updateTimeLabel();
                }
            }
            this.suspendEvents = false;
        }

        private void updateTimeLabel()
        {
            if (player != null)
            {
                int seconds = (int)(player.getMillisecondPosition() > 0 ?
                    player.getMillisecondPosition() : player.getMillisecondLength()) / 1000;
                int mm = seconds / 60;
                int ss = seconds % 60;
                timeLabel.setText(String.format("%1$02d:%2$02d", mm, ss));
            }
            else
            {
                timeLabel.setText("00:00");
            }
        }

    }
}
