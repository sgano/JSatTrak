/**
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


/*
 * Copyright (c) 2005-2008 Substance Kirill Grouchnikov. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  o Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 *  o Neither the name of Substance Kirill Grouchnikov nor the names of
 *    its contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */


import java.awt.Window;
import javax.swing.*;

/**
 * @author Shawn E. Gano
 * @author kirillg
 * @author Daniel Stonier
 * @author Keith Woodward
 */
public class LafChanger
{

	public static void changeLaf(JFrame frame, String lafClassName)
    {

        final boolean wasDecoratedByOS = !frame.isUndecorated();

        try
        {
            UIManager.setLookAndFeel(lafClassName);

            boolean canBeDecoratedByLAF = UIManager.getLookAndFeel().getSupportsWindowDecorations();

             //System.out.println("------------------");
            for (Window window : Window.getWindows())
            {
                SwingUtilities.updateComponentTreeUI(window);

                // Search for JDialogs that can be updated by look and feel-----
                if( window instanceof JFrame)
                {
                    //System.out.println("Jframe Window:" + ((JFrame)window).getTitle() );
                }
                else if(window instanceof JDialog)
                {
                     //System.out.println("Jdialog Window:" +  ((JDialog)window).getTitle() );

                     if( canBeDecoratedByLAF !=  ((JDialog)window).isUndecorated() )
                     {
                        boolean wasVisible = ((JDialog)window).isVisible();
                        ((JDialog)window).setVisible(false);
                        ((JDialog)window).dispose();
                        if(!canBeDecoratedByLAF) //|| wasOriginallyDecoratedByOS
                        {
                            // see the java docs under the method
                            // JFrame.setDefaultLookAndFeelDecorated(boolean
                            // value) for description of these 2 lines:
                            ((JDialog)window).setUndecorated(false);
                            ((JDialog)window).getRootPane().setWindowDecorationStyle(
                                    JRootPane.NONE);

                        }
                        else
                        {
                            ((JDialog)window).setUndecorated(true);
                            ((JDialog)window).getRootPane().setWindowDecorationStyle(
                                    JRootPane.FRAME);
                        }
                        ((JDialog)window).setVisible(wasVisible);
                    }
                }
                else
                {
                    //System.out.println("NOT Jframe Window:" + window.getName() );
                } // End of Jdialog search that can be updated by current look and feel -----------
            }

            //boolean canBeDecoratedByLAF = UIManager.getLookAndFeel().getSupportsWindowDecorations();
            if (canBeDecoratedByLAF == wasDecoratedByOS)
            {
                boolean wasVisible = frame.isVisible();

                frame.setVisible(false);
                frame.dispose();
                if (!canBeDecoratedByLAF ) //|| wasOriginallyDecoratedByOS
                {
                    // see the java docs under the method
                    // JFrame.setDefaultLookAndFeelDecorated(boolean
                    // value) for description of these 2 lines:
                    frame.setUndecorated(false);
                    frame.getRootPane().setWindowDecorationStyle(
                            JRootPane.NONE);

                } else
                {
                    frame.setUndecorated(true);
                    frame.getRootPane().setWindowDecorationStyle(
                            JRootPane.FRAME);
                }
                frame.setVisible(wasVisible);
            // wasDecoratedByOS = !frame.isUndecorated();
            }
        } catch (ClassNotFoundException cnfe)
        {
            System.out.println("LAF main class '" + lafClassName + "' not found");
        } catch (Exception exc)
        {
            exc.printStackTrace();
        }


    }


}


