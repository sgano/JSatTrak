/**
 * File: ToggleFullscreen.java
 * 
 * This file is free to use and modify as it is for educational use.
 * brought to you by Game Programming Snippets (http://gpsnippets.blogspot.com/)
 * 
 * Revisions:
 * 1.1 Initial Revision 
 *
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
 * 
 */
package name.gano.swingx.fullscreen;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import jsattrak.utilities.J3DEarthComponent;

/**
 * This class was created to show how to setup a fullscreen window and how
 * to change the current screen resolution. Also how you can toggle a window
 * between fullscreen and windowed modes with a keypress.
 */
public class ToggleFullscreen extends JFrame {

	//a reference to the GraphicsDevice for changing resolution and making 
	//this window fullscreen.
	private GraphicsDevice device = null;
	
	//the resolution in which we want to change the monitor to.
	private DisplayMode dispMode = new DisplayMode(800, 600, 32, 60);
	
	//the original resolution before our program is run.
	private DisplayMode dispModeOld = null;

	//variable used to toggle between windowed and fullscreen.
	protected boolean fullscreen = false;
	
	//needed because the JFrame area changes.
	//private JPanel drawingArea = null;
        
        private J3DEarthComponent test; // panel were the component came from
        
        private KeyAdapter keyAdp;
        
        private Component contents; // so key listener can be removed
	
	/**
	 * The function that first removes the window border and
	 * title bar. Then makes the window fullscreen and finally
	 * sets the current display mode for the monitor to a custom
	 * resolution. 
	 * 
	 * @param gd A valid GraphicsDevice to be used for this 
	 *           application.
	 */
	public ToggleFullscreen(GraphicsDevice gd, Component contents, J3DEarthComponent test) 
        {
		super();

                this.test = test;
                this.contents = contents;
                
		//get a reference to the device.
		this.device = gd;
		
		//save the old display mode before changing it.
		dispModeOld = gd.getDisplayMode();

		//set fullscreen mode.
		setFullscreen(false);
		
		//drawing area here -- SEG
		
		//set the layout to ensure that the component is resized to the full
		//JFrame.
		getContentPane().setLayout(new BorderLayout() );
		
		//add the JPanel to the JFrame
		getContentPane().add(contents); // drawingArea
		
		//add keylistener so that we may exit.
                keyAdp = new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
					//do all cleanup before closing the program
					onExit();
				}else if( e.getKeyCode() == (KeyEvent.VK_F) ) {
					//toggle fullscreen mode.
					//setFullscreen( !isFullscreen() );
                                        // now just exit
                                        onExit();
				}
			}
		};
                
		this.addKeyListener(keyAdp);
                
                contents.addKeyListener(keyAdp); // add it to the WWJ oject too 

		//add the window listener to detect when the window is closing.
		addWindowListener(
			new WindowAdapter() {
				public void windowClosing( WindowEvent e ) {
					//do all cleanup before closing the program
					onExit();
				}
			});

		//initially set the window size.
		setSize(800,600);
		
		//center the window on screen.
		this.setLocationRelativeTo(null);
		
		
		//show the JFrame.
		setVisible(true);
                
                // auto matically go to full screen mode
                this.setFullscreen(true);
	}
	
	/**
	 * Used as a single function to save off information before exiting
	 * and to keep all cleanup code in the same place.
	 */
	public void onExit() {
		
                // NEED TO REMOVE KEY LISTENER FROM WWJ componet
                this.removeKeyListener(keyAdp);
                contents.removeKeyListener(keyAdp);
                         
		//immediately hide the window (no falling apart windows)
		setVisible(false);
		
//                this.removeAll();
                test.resetWWJdisplay();
                
		//cleanup and destroy the window threads
		dispose();
                
		//exit the application without an error
		//System.exit(0); // removed because it shouldn't close the  whole app when used internally
	}

	/**
	 * Method allows changing whether this window is displayed in fullscreen or
	 * windowed mode. 
	 * 
	 * @param fullscreen true = change to fullscreen, 
	 *                   false = change to windowed
	 */
	public void setFullscreen( boolean fullscreen ) {
		if( this.fullscreen != fullscreen ) { //are we actually changing modes.
			//change modes.
			this.fullscreen = fullscreen;
			
			// toggle fullscreen mode
			if( !fullscreen  ) { //change to windowed mode.
				
				//set the display mode back to the what it was when
				//the program was launched.
				device.setDisplayMode(dispModeOld);
				
				//hide the frame so we can change it.
				setVisible(false);
				
				//remove the frame from being displayable.
				dispose();
									
				//put the borders back on the frame.
				setUndecorated(false);
					
				//needed to unset this window as the fullscreen window.
				device.setFullScreenWindow(null);
				
				//make sure the size of the window is correct.
				setSize(800,600);
				
				//recenter window
				setLocationRelativeTo(null);
				
				//reset the display mode to what it was before 
				//we changed it.
				setVisible(true);
				
			}else{ //change to fullscreen.
				//hide everything
				setVisible(false);
				
				//remove the frame from being displayable.
				dispose();
				
				//remove borders around the frame
				setUndecorated(true);
				
				//make the window fullscreen.
				device.setFullScreenWindow(ToggleFullscreen.this);
	
				//attempt to change the screen resolution.
                                // SEG - remove this? so we don't change resolutions
				//device.setDisplayMode(dispMode);
				
				//show the frame
				setVisible(true);
				
			} // end if
			
			//make sure that the screen is refreshed.
			repaint();
		}
	}
	
	/**
	 * This method returns true is this frame is in fullscreen. False if in 
	 * windowed mode.
	 * 
	 * @return true = fullscreen, false = windowed.
	 */
	public boolean isFullscreen() {
		return fullscreen;
	}
	
	/**
	 * The main entry point for the application which creates a new
	 * ToggleFullscreen object by passing the default GraphicsDevice to
	 * the constructor.
	 * 
	 * @param args Command line parameters (could be used to toggle
	 *                                      fullscreen at start up.)
	 */
	public static void main(String[] args) 
        {
            
//            //create an Anonymous JPanel to do rendering
//		JPanel drawingArea = new JPanel() {
//			public void paint( Graphics g ) {
//				//display toggling instructions.
//				g.drawString("Press 'f' key to toggle fullscreen",10,20);
//			}
//		};
//		
//		//Get the default graphics configuration from the graphics environment
//		new ToggleFullscreen(GraphicsEnvironment.getLocalGraphicsEnvironment()
//				.getDefaultScreenDevice(),drawingArea);
		
	}

} // end of ToggleFullscreen class.