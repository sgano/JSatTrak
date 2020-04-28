/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.ows;

import java.util.Collection;

/**
 * @author dcollins
 * @version $Id: ExceptionReport.java 5465 2008-06-24 00:17:03Z dcollins $
 */
public interface ExceptionReport extends Iterable<ExceptionType>
{
    int getExceptionCount();

    int getIndex(ExceptionType e);

    ExceptionType getException(int index);

    void setException(int index, ExceptionType e);

    void addException(int index, ExceptionType e);

    void addException(ExceptionType e);

    void addExceptions(Collection<? extends ExceptionType> c);

    void removeException(int index);

    void clearExceptions();

    String getVersion();

    void setVersion(String version);

    String getLang();

    void setLang(String lang);
}
