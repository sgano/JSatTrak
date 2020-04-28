/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.ebrim;

import java.util.Collection;

/**
 * @author dcollins
 * @version $Id$
 */
public interface InternationalString extends Iterable<LocalizedString>
{
    int getLocalizedStringCount();

    int getIndex(LocalizedString ls);

    LocalizedString getLocalizedString(int index);

    void setLocalizedString(int index, LocalizedString ls);

    void addLocalizedString(int index, LocalizedString ls);

    void addLocalizedString(LocalizedString ls);

    void addLocalizedStrings(Collection<? extends LocalizedString> c);

    void removeLocalizedString(int index);

    void clearLocalizedStrings();
}
