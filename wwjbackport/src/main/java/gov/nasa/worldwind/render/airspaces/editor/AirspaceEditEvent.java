/* Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render.airspaces.editor;

import gov.nasa.worldwind.render.airspaces.Airspace;

import java.util.EventObject;

/**
 * @author dcollins
 * @version $Id: AirspaceEditEvent.java 8772 2009-02-05 23:50:46Z dcollins $
 */
public class AirspaceEditEvent extends EventObject
{
    private Airspace airspace;
    private AirspaceEditor editor;
    private AirspaceControlPoint controlPoint;

    public AirspaceEditEvent(Object source, Airspace airspace, AirspaceEditor editor, AirspaceControlPoint controlPoint)
    {
        super(source);
        this.airspace = airspace;
        this.editor = editor;
        this.controlPoint = controlPoint;
    }

    public AirspaceEditEvent(Object source, Airspace airspace, AirspaceEditor editor)
    {
        this(source, airspace, editor, null);
    }

    public Airspace getAirspace()
    {
        return this.airspace;
    }

    public AirspaceEditor getEditor()
    {
        return this.editor;
    }

    public AirspaceControlPoint getControlPoint()
    {
        return this.controlPoint;
    }
}
