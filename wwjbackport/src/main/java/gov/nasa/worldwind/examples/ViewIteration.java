/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.*;
import java.util.ArrayList;

/**
 * @author tag
 * @version $Id: ViewIteration.java 12530 2009-08-29 17:55:54Z jterhorst $
 */
public class ViewIteration extends ApplicationTemplate
{
    public static class AppFrame extends JFrame
    {
        static ArrayList<Position> path;

        static
        {
            path = new ArrayList<Position>();
            path.add(Position.fromDegrees(0, 0, 1e5));
            path.add(Position.fromDegrees(0, 10, 1e5));
            path.add(Position.fromDegrees(0, 20, 1e5));
            path.add(Position.fromDegrees(0, 30, 1e5));
            path.add(Position.fromDegrees(0, 40, 1e5));
            path.add(Position.fromDegrees(0, 50, 1e5));
            path.add(Position.fromDegrees(0, 60, 1e5));
            path.add(Position.fromDegrees(0, 70, 1e5));
        }

        private int pathPosition = 0;

        private PathAction[] pathActions =
            new PathAction[]{
                new GoToLatLonFromCurrent("Zero", LatLon.ZERO),
                new FollowPath("Follow"),
                new Heading("Heading"),
                new Forward("Forward"),
                new Backwards("Backwards"),
            };

        private Dimension canvasSize = new Dimension(800, 600);
        private ApplicationTemplate.AppPanel wwjPanel;

        public AppFrame()
        {
            // Create the WorldWindow.
            this.wwjPanel = new ApplicationTemplate.AppPanel(this.canvasSize, true);
            this.wwjPanel.setPreferredSize(canvasSize);

            JPanel controlPanel = makeControlPanel();
            controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            this.getContentPane().add(wwjPanel, BorderLayout.CENTER);
            this.getContentPane().add(controlPanel, BorderLayout.WEST);
            this.pack();

            // Center the application on the screen.
            Dimension prefSize = this.getPreferredSize();
            Dimension parentSize;
            java.awt.Point parentLocation = new java.awt.Point(0, 0);
            parentSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = parentLocation.x + (parentSize.width - prefSize.width) / 2;
            int y = parentLocation.y + (parentSize.height - prefSize.height) / 2;
            this.setLocation(x, y);
            this.setResizable(true);
        }

        private JPanel makeControlPanel()
        {
            JPanel innerPanel = new JPanel(new GridLayout(8, 1));
            innerPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Go To"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

            for (PathAction pa : pathActions)
            {
                JButton btn = new JButton(pa);
                innerPanel.add(btn);
            }

            JPanel cp = new JPanel(new BorderLayout());
            cp.add(innerPanel, BorderLayout.CENTER);

            return cp;
        }

        private abstract class PathAction extends AbstractAction
        {
            PathAction(String name)
            {
                super(name);
            }
        }

        //
        // Specific Actions
        //

        private class Forward extends PathAction
        {
            public Forward(String name)
            {
                super(name);
            }

            public void actionPerformed(ActionEvent actionEvent)
            {
                if (pathPosition < path.size() - 1)
                {
                    BasicOrbitView view = (BasicOrbitView) wwjPanel.getWwd().getView();
                    view.setHeading(Angle.fromDegrees(90));
                    view.addEyePositionAnimator(4000, view.getEyePosition(), path.get(++pathPosition));

                }
            }
        }

        private class Backwards extends PathAction
        {
            public Backwards(String name)
            {
                super(name);
            }

            public void actionPerformed(ActionEvent actionEvent)
            {
                if (pathPosition > 0)
                {
                    BasicOrbitView view = (BasicOrbitView) wwjPanel.getWwd().getView();

                    view.addEyePositionAnimator(4000, view.getEyePosition(), path.get(--pathPosition));
                }
            }
        }

        private class Heading extends PathAction
        {
            public Heading(String name)
            {
                super(name);
            }

            public void actionPerformed(ActionEvent actionEvent)
            {
                Angle heading;
                if (pathPosition == 0)
                    heading = computeHeading(path.get(0), path.get(1));
                else
                    heading = computeHeading(path.get(pathPosition - 1), path.get(pathPosition));

                BasicOrbitView view = (BasicOrbitView) wwjPanel.getWwd().getView();
                view.addHeadingAnimator(view.getHeading(), heading);
            }
        }

        private Angle computeHeading(Position pa, Position pb)
        {
            return LatLon.greatCircleAzimuth(pa, pb);
        }

        private class GoToLatLonFromCurrent extends PathAction
        {
            private final LatLon latlon;

            GoToLatLonFromCurrent(String name, LatLon latlon)
            {
                super(name);
                this.latlon = latlon;
            }

            public void actionPerformed(ActionEvent actionEvent)
            {
                BasicOrbitView view = (BasicOrbitView) wwjPanel.getWwd().getView();
                view.addEyePositionAnimator(
                    4000, view.getEyePosition(), new Position(this.latlon, view.getEyePosition().getElevation()));
                
            }
        }

        private class FollowPath extends PathAction
        {
            ArrayList<Position> path = new ArrayList<Position>();

            FollowPath(String name)
            {
                super(name);
                path.add(Position.fromDegrees(0, 0, 1e5));
                path.add(Position.fromDegrees(1, 3, 1e5));
                path.add(Position.fromDegrees(2, 4, 1e5));
                path.add(Position.fromDegrees(3, 5, 1e5));
            }

            private class PosToPos implements Runnable
            {
                Position pos;

                PosToPos(Position pos)
                {
                    this.pos = pos;
                }

                public void run()
                {
                    final BasicOrbitView view = (BasicOrbitView) wwjPanel.getWwd().getView();
                    view.addEyePositionAnimator(4000, view.getEyePosition(), pos);
                }
            }

            public void actionPerformed(ActionEvent actionEvent)
            {
                for (Position p : path)
                {
                    final BasicOrbitView view = (BasicOrbitView) wwjPanel.getWwd().getView();
                    view.addEyePositionAnimator(4000,
                            view.getEyePosition(), new Position(p, view.getEyePosition().getElevation()));
                }
            }
        }
    }

    public static void main(String[] args)
    {
        try
        {
            AppFrame frame = new AppFrame();
            frame.setTitle("World Wind View Paths");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
