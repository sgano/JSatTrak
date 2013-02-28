/*
 * CustomSunPositionProvider.java
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
 * Created on 10 June 2009
 */
package name.gano.worldwind.sunshader;

import gov.nasa.worldwind.examples.sunlight.SunPositionProvider;
import gov.nasa.worldwind.geom.LatLon;
import name.gano.astro.bodies.Sun;

/**
 *
 * @author sgano
 */
public class CustomSunPositionProvider implements SunPositionProvider
{
    private Sun sun;

    public CustomSunPositionProvider(Sun sun)
    {
        this.sun = sun;
    }

    public LatLon getPosition()
	{
		return LatLon.fromRadians(sun.getCurrentLLA()[0], sun.getCurrentLLA()[1]);
	}
}
