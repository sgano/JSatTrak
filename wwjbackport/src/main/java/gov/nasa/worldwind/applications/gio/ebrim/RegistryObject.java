/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.ebrim;

import java.util.Collection;
import java.util.Iterator;

/**
 * @author dcollins
 * @version $Id$
 */
public interface RegistryObject extends Identifiable
{
    Name getName();

    void setName(Name name);

    Description getDescription();

    void setDescription(Description description);

    VersionInfo getVersionInfo();

    void setVersionInfo(VersionInfo versionInfo);

    int getClassificationCount();

    int getIndex(Classification classification);

    Classification getClassification(int index);

    void setClassification(int index, Classification classification);

    void addClassification(int index, Classification classification);

    void addClassification(Classification classification);

    void addClassifications(Collection<? extends Classification> c);

    void removeClassification(int index);

    void clearClassifications();

    Iterator<Classification> getClassificationIterator();

    int getExternalIdentifierCount();

    int getIndex(ExternalIdentifier externalIdentifier);

    ExternalIdentifier getExternalIdentifier(int index);

    void setExternalIdentifier(int index, ExternalIdentifier externalIdentifier);

    void addExternalIdentifier(int index, ExternalIdentifier externalIdentifier);

    void addExternalIdentifier(ExternalIdentifier externalIdentifier);

    void addExternalIdentifiers(Collection<? extends ExternalIdentifier> c);

    void removeExternalIdentifier(int index);

    void clearExternalIdentifiers();

    Iterator<ExternalIdentifier> getExternalIdentifierIterator();

    String getLid();

    void setLid(String lid);

    String getObjectType();

    void setObjectType(String objectType);

    String getStatus();

    void setStatus(String status);
}
