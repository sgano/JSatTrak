/*
 * KeyPollTimer.java
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

package name.gano.worldwind.view;

// Used verbatim - except to declare class as public!
/*
Copyright (C) 2001, 2006 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/

import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.WorldWind;

import java.beans.*;

/**
 * @author dcollins
 * @version $Id: KeyPollTimer.java 5113 2008-04-21 23:46:50Z tgaskins $
 */
public class KeyPollTimer implements java.awt.event.KeyListener
{
    private java.util.Timer timer;
    private java.util.TimerTask timerTask;
    private final int period;
    private final java.awt.event.ActionListener listener;
    private java.util.ArrayList<Integer> keys = new java.util.ArrayList<Integer>();
    private java.util.Stack<Integer> keyStack = new java.util.Stack<Integer>();
    private int modifiers;

    public KeyPollTimer(int period, java.util.Collection<Integer> keys, java.awt.event.ActionListener listener)
    {
        if (period < 0)
        {
            String message = Logging.getMessage("Awt.KeyPollTimer.PeriodLessThanZero");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (keys == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        this.timer = new java.util.Timer();
        this.period = period;
        this.listener = listener;
        this.keys.addAll(keys);

        WorldWind.addPropertyChangeListener(WorldWind.SHUTDOWN_EVENT, new PropertyChangeListener()
        {
            public void propertyChange(PropertyChangeEvent propertyChangeEvent)
            {
                KeyPollTimer.this.timer.cancel();
                KeyPollTimer.this.timer = null;
            }
        });
    }

    public synchronized boolean isRunning()
    {
        return this.timerTask != null;
    }

    public void keyTyped(java.awt.event.KeyEvent e)
    {
    }

    public void keyPressed(java.awt.event.KeyEvent e)
    {
        if (e == null)
            return;

        if (this.onKeyEvent(e))
        {
            if (this.timerTask == null)
                this.start();
        }
    }

    public void keyReleased(java.awt.event.KeyEvent e)
    {
        if (e == null)
            return;

        if (this.onKeyEvent(e))
        {
            if (this.timerTask != null && this.keyStack.empty())
                this.stop();
        }
    }

    private synchronized boolean onKeyEvent(java.awt.event.KeyEvent e)
    {
        this.modifiers = e.getModifiersEx();

        Integer keyCode = e.getKeyCode();
        if (this.keys.contains(keyCode))
        {
            if (this.keyStack.contains(keyCode))
                this.keyStack.remove(keyCode);
            if (e.getID() == java.awt.event.KeyEvent.KEY_PRESSED)
                this.keyStack.push(keyCode);
            return true;
        }

        return false;
    }

    public synchronized void start()
    {
        if (this.timerTask == null)
        {
            this.timerTask = new java.util.TimerTask()
            {
                public void run()
                {
                    long time = System.currentTimeMillis();
                    if (time - this.scheduledExecutionTime() >= 2 * KeyPollTimer.this.period)
                        return;
                    KeyPollTimer.this.updateAndNotify(KeyPollTimer.this.listener);
                }
            };
            if (this.timer == null)
                this.timer = new java.util.Timer();
                
            this.timer.schedule(timerTask, 0, this.period);
        }
    }

    public synchronized void stop()
    {
        if (this.timerTask != null)
        {
            this.timerTask.cancel();
            this.timerTask = null;
        }
    }

    private synchronized void updateAndNotify(java.awt.event.ActionListener listener)
    {
        if (listener == null)
            return;
        if (this.keyStack.empty())
            return;
        listener.actionPerformed(new java.awt.event.ActionEvent(keyStack.peek(), 0, null, modifiers));
    }
}
