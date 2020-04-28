/*
 * Copyright (c) 2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * - Redistribution of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 * 
 * - Redistribution in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 * 
 * Neither the name of Sun Microsystems, Inc. or the names of
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES,
 * INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN
 * MICROSYSTEMS, INC. ("SUN") AND ITS LICENSORS SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR
 * ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR
 * DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE
 * DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY,
 * ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE, EVEN IF
 * SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 * 
 * You acknowledge that this software is not designed or intended for use
 * in the design, construction, operation or maintenance of any nuclear
 * facility.
 * 
 */

package net.java.joglutils.msg.test;

import java.awt.BorderLayout;
import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.*;
import java.net.*;

import javax.swing.*;

import javax.media.opengl.*;

/**
 * A test implementing a 3D display shelf component.
 *
 * @author Kenneth Russell
 */

public class DisplayShelf {
  public static void main(String[] args) {
    Frame f = new Frame("Display Shelf test");
    f.setLayout(new BorderLayout());
    f.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
          new Thread(new Runnable() {
              public void run() {
                System.exit(0);
              }
            }).start();
        }
      });

    // The images to configure the shelf with
    String[] images = {
      "http://download.java.net/media/jogl/builds/ds_tmp/mzi.jsepedzf.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.wvbmknhn.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/mzi.oorrjicu.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.woofnkar.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.tapbaxpy.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/mzi.awlngumx.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.bpuzrjch.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.nqarjlzt.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.hgadlawz.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.sdfnrwzj.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.vtbicehh.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.lhgtckcs.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/mzi.tbwyqyqm.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.eimndamh.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.nxvdfcwt.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.njoydoqk.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/mzi.ikfbfqzh.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.niqwioqm.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/mzi.tqqldmqe.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/mzi.ynokefwv.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.jodjmgxs.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.yhdaeino.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.xmgrrxef.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/mzi.pahnmknr.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/mzi.sbkwhrik.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.hwbcjnfx.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.umbuvrfe.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/mzi.krksguze.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.jionwnuf.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.dgnjindw.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/mzi.wpfmtfzp.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.gcajwhco.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.glzycglj.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/mzi.pajmxsmk.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.lamcsbwx.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.nqvsikaq.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.elyzoipc.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.oidpsvzg.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.moyzjiht.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.qizpbris.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.uadqyjbr.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/mzi.pqzeferc.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.jhotijvb.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.asztraij.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/mzi.dricykdh.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.tpysowpf.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.cawuddxy.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/mzi.vmajyyha.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/mzi.tuyoxwib.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/mzi.sanzeosx.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/dj.zfqfgoas.200x200-75.jpg",
      "http://download.java.net/media/jogl/builds/ds_tmp/mzi.uswlslxx.200x200-75.jpg"
    };
    DefaultListModel model = new DefaultListModel();
    for (String str : images) {
      try {
        model.addElement(new URL(str));
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    DisplayShelfRenderer renderer = new DisplayShelfRenderer(model);
    GLCanvas canvas = new GLCanvas(new GLCapabilities(), null, renderer.getSharedContext(), null);
    canvas.setFocusable(true);
    canvas.addGLEventListener(renderer);
    f.add(canvas);
    GraphicsDevice dev = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    DisplayMode curMode = dev.getDisplayMode();
    int height = (int) (0.5f * curMode.getWidth());
    f.setSize(curMode.getWidth(), height);
    f.setLocation(0, (curMode.getHeight() - height) / 2);
    f.setVisible(true);
  }
}
