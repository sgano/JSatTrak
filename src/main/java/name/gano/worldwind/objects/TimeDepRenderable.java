/*
 * TimeDepRenderable.java
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
 * Created 20 June 2009
 */

package name.gano.worldwind.objects;

import gov.nasa.worldwind.render.Renderable;

/**
 * Makes and object tim dependant and provides and interface for updating the currnet time
 * @author Shawn Gano
 */
public interface TimeDepRenderable extends Renderable
{
    public void updateMJD(double currentMJD);
}
