JSatTrak
http://www.gano.name/shawn/JSatTrak
Contact: shawn@gano.name
 

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

Created Using the NetBeans IDE

When building for distrbution include these directories:
  data/
  plugins/
  and for windows use launch4j to make .exe and include in the root: gluegen-rt.dll, jogl.dll, jogl_awt.dll, jogl_cg.dll

Using the license feature in netbeans: (use template file: license-JSatTrak.txt)
http://bigallan.blogspot.com/2008/02/using-license-functionality-in-netbeans.html

Similar Apps:
Nasa online tracker for shuttle/ISS:
http://spaceflight.nasa.gov/realdata/tracking/

J-Track:
http://science.nasa.gov/realtime/jtrack/Spacecraft.html

Good site that has STS TLE for alll mission (past/preset/future planned)
http://spaceflight.nasa.gov/realdata/sightings/SSapplications/Post/JavaSSOP/orbit/SHUTTLE/SVPOST.html

Real time data: (add this main link to website)
http://spaceflight.nasa.gov/realdata/

Notes:
not good to use rk78 in a solver loop because direvatives inaccurate, because solution changes slightly near end.?

NOTE: in beanshell jar (i.e. bsh-2.0b4.jar) need to replace desktop.bsh, with one included with this source -- it allows bean shell desktop to be reopened
      THIS BEANSHELL problem may be fixed in a newer version as I wrote the authore about this and gave him my solution


run from linux: add to VM Options
-Djava.library.path="/home/sgano/Desktop/sgano/Java/Libraries/"

To make jogl display correctly in Ubuntu Linux:
sudo apt-get install driconf
run driconf and set "Enable S3TC"

=====================
changes to WWJ to make internal windows work:
 http://forum.worldwindcentral.com/showthread.php?t=12360&highlight=WorldWindowGLJPanel
 in (WorldWindowGLAutoDrawable.initDrawable()) comment line: this.drawable.setAutoSwapBufferMode(fals e); 
TerrainProfileLayer:  -- works now my bug reports and fixes are in the release!
- Fix clipping plane problem:
in AbstractView.java (gov.nasa.worldwind.view)  --  change the code to this:
private static final double MIN_FAR_CLIP_DISTANCE = 10000000000d;
THIS SHOULD BE FIXED in WWJ >v0.4.1 (see: http://issues.worldwind.arc.nasa.gov/browse/WWJ-9)
I STILL WANT LIGHTING ADDED TO WWJ - SO I CAN SIMULATE sun/dark side of earth!!
- and also views for model centric views... but I think that will work in the next release too.

Jar signing commands:
NOTE: to unsign a jar (such as worldwind.jar) - open jar in winzip and delete everything in the meta-inf directory
      then resign -- fixed issue with "web start complaining about jars signed with multiple keys)
/usr/java/jdk1.6.0_01/bin/keytool -genkey -keystore myKeys -alias jdc
/usr/java/jdk1.6.0_01/bin/jarsigner -keystore myKeys JSatTrak.jar jdc
