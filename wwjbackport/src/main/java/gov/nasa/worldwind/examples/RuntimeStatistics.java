package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.*;

public class RuntimeStatistics extends ApplicationTemplate
{
    private static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, true);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Runtime Statistics", AppFrame.class);
    }
}
