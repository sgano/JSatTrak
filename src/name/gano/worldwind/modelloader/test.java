/*
 * ModelLayerTest.java
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
 * toolshed - http://forum.worldwindcentral.com/showthread.php?t=15222&page=6
 */

package name.gano.worldwind.modelloader;

import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import java.util.Random;
import net.java.joglutils.model.ModelFactory;
import net.java.joglutils.model.geometry.Model;

/**
 *
 * @author RodgersGB
 */
public class test extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            try
            {
                Model3DLayer_new layer = new Model3DLayer_new();
//                layer.setMaitainConstantSize(true);
//                layer.setSize(300000);
                
                Random generator = new Random();
//                Model modelOBJ = ModelFactory.createModel("test/data/penguin.obj");
//                for (int i=0; i<100; i++) {
//                    layer.addModel(new WWModel3D(modelOBJ,
//                            new Position(Angle.fromDegrees(generator.nextInt()%80),
//                                         Angle.fromDegrees(generator.nextInt()%180),
//                                         500000)));
//                }
                
                Model model3DS = ModelFactory.createModel("test/data/globalstar/Globalstar.3ds");
                model3DS.setUseLighting(false); // turn off lighting!
//                for (int i=0; i<100; i++) {
//                    layer.addModel(new WWModel3D(model3DS,
//                            new Position(Angle.fromDegrees(generator.nextInt()%80),
//                                         Angle.fromDegrees(generator.nextInt()%180),
//                                         750000)));
//                }
                
                WWModel3D_new model3D = new WWModel3D_new(model3DS,
                            new Position(Angle.fromDegrees(0),
                                         Angle.fromDegrees(0),
                                         750000));
                model3D.setMaitainConstantSize(true);
                model3D.setSize(300000);
                
                layer.addModel(model3D);
                
                insertBeforeCompass(this.getWwd(), layer);
                this.getLayerPanel().update(this.getWwd());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("Model Layer Test", AppFrame.class);
    }
}
