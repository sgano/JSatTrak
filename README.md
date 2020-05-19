# JSatTrak
Java Satellite Tracker

This is a fork of a great application created and developed by Shawn E. Gano (shawn@gano.name), whose development started in 2008.
The original site can be found at: https://www.gano.name/shawn/JSatTrak/
The main repository can be found at: https://github.com/sgano/JSatTrak

The main differences found in this fork are the support for OpenJDK 8 and usage of Maven and the full source code of:
 - WWJ (0.6 from https://github.com/gb96/WorldWind-JAVA) 
 - JOGL-UTILS (from https://github.com/sgothel/jogl-utils) 

These two projects does not exist in Maven repositories for such versions. For that reason, once you clone the repository you will find three projects: 
a) jsattrak-eclipse, b) wwjbackport and c) joglutilsbackport. Other important difference is the use of Eclipse instead of Netbeans. Finally, was added a source code for a windows installer.

There is an important thing to notice, this project runs with Java 8 (Both Oracle and OpenJDK), so if you have other versions you must set JAVA_HOME environment variable first.

Now, with one mvn command the project is built: `mvn clean install package`. Thus, the main idea is to have a easy way to keep the project alive.

