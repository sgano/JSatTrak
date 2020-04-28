/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwindow.features;

import gov.nasa.worldwindow.core.*;

/**
 * @author tag
 * @version $Id: AbstractOpenResourceFeature.java 13825 2010-09-18 00:18:35Z tgaskins $
 */
public abstract class AbstractOpenResourceFeature extends AbstractFeature implements NetworkActivitySignal.NetworkUser
{
    protected Thread loadingThread;

    protected AbstractOpenResourceFeature(String s, String featureID, String largeIconPath, Registry registry)
    {
        super(s, featureID, largeIconPath, registry);
    }

    public boolean hasNetworkActivity()
    {
        return this.loadingThread != null && this.loadingThread.isAlive();
    }

    protected Thread runOpenThread(final Object source)
    {
        this.loadingThread = new Thread()
        {
            @Override
            public void run()
            {
                getController().getNetworkActivitySignal().addNetworkUser(AbstractOpenResourceFeature.this);

                try
                {
                    new SimpleImporter(source, getController()).startImport();
                }
                finally
                {
                    controller.getNetworkActivitySignal().removeNetworkUser(AbstractOpenResourceFeature.this);
                }
            }
        };

        this.loadingThread.start();

        return this.loadingThread;
    }
}
