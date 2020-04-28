/*
Copyright (C) 2001, 2007 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.sar;

import gov.nasa.worldwind.applications.sar.actions.*;

import javax.swing.*;
import java.awt.event.*;

/**
 * @author tag
 * @version $Id: PositionsContextMenu.java 11498 2009-06-08 19:25:06Z dcollins $
 */
public class PositionsContextMenu extends MouseAdapter
{
    private final PositionTable positionTable;

    public PositionsContextMenu(final PositionTable positionTable)
    {
        this.positionTable = positionTable;
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent)
    {
        this.checkPopup(mouseEvent);
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent)
    {
        this.checkPopup(mouseEvent);
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent)
    {
        this.checkPopup(mouseEvent);
    }

    private void checkPopup(MouseEvent e)
    {
        if (!e.isPopupTrigger())
            return;

        JMenuItem mi;
        JPopupMenu pum = new JPopupMenu();

        mi = new JMenuItem(new DeletePositionsAction(positionTable));
        pum.add(mi);

        pum.addSeparator();

        mi = new JMenuItem(new AppendPositionAction(positionTable));
        pum.add(mi);

        mi = new JMenuItem(new InsertPositionAction(true, positionTable));
        pum.add(mi);

        mi = new JMenuItem(new InsertPositionAction(false, positionTable));
        pum.add(mi);

        pum.addSeparator();

        mi = new JMenuItem(new AddOffsetToPositionsAction(positionTable));
        pum.add(mi);

        pum.show(positionTable, e.getX(), e.getY());
    }
}
