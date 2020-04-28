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
public interface Organization extends RegistryObject
{
    int getAddressCount();

    int getIndex(Address address);

    Address getAddress(int index);

    void setAddress(int index, Address address);

    void addAddress(int index, Address address);

    void addAddress(Address address);

    void addAddresses(Collection<? extends Address> c);

    void removeAddress(int index);

    void clearAddresses();

    Iterator<Address> getAddressIterator();

    int getTelephoneNumberCount();

    int getIndex(TelephoneNumber telephoneNumber);

    TelephoneNumber getTelephoneNumber(int index);

    void setTelephoneNumber(int index, TelephoneNumber telephoneNumber);

    void addTelephoneNumber(int index, TelephoneNumber telephoneNumber);

    void addTelephoneNumber(TelephoneNumber telephoneNumber);

    void addTelephoneNumbers(Collection<? extends TelephoneNumber> c);

    void removeTelephoneNumber(int index);

    void clearTelephoneNumbers();

    Iterator<TelephoneNumber> getTelephoneNumberIterator();

    int getEmailAddressCount();

    int getIndex(EmailAddress emailAddress);

    EmailAddress getEmailAddress(int index);

    void setEmailAddress(int index, EmailAddress emailAddress);

    void addEmailAddress(int index, EmailAddress emailAddress);

    void addEmailAddress(EmailAddress emailAddress);

    void addEmailAddresss(Collection<? extends EmailAddress> c);

    void removeEmailAddress(int index);

    void clearEmailAddresses();

    Iterator<EmailAddress> getEmailAddressIterator();

    String getParent();

    void setParent(String parent);

    String getPrimaryContact();

    void setPrimaryContact(String primaryContact);
}
