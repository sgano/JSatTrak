/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.ows;

import java.util.Collection;

/**
 * @author dcollins
 * @version $Id: ExceptionType.java 5465 2008-06-24 00:17:03Z dcollins $
 */
public interface ExceptionType extends Iterable<ExceptionText>
{
    int getExceptionTextCount();

    int getIndex(ExceptionText exceptionText);

    ExceptionText getExceptionText(int index);

    void setExceptionText(int index, ExceptionText exceptionText);

    void addExceptionText(int index, ExceptionText exceptionText);

    void addExceptionText(ExceptionText exceptionText);

    void addExceptionTexts(Collection<? extends ExceptionText> c);

    void removeExceptionText(int index);

    void clearExceptionTexts();

    String getExceptionCode();

    void setExceptionCode(String exceptionCode);

    String getLocator();

    void setLocator(String locator);
}
