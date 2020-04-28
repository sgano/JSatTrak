/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.ebrim;

import gov.nasa.worldwind.util.Logging;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author dcollins
 * @version $Id$
 */
public class PersonParser extends RegistryObjectParser implements Person
{
    private List<Address> addressList;
    private PersonName personName;
    private List<TelephoneNumber> telephoneNumberList;
    private List<EmailAddress> emailAddressList;
    public static final String ELEMENT_NAME = "Person";

    public PersonParser(String elementName, org.xml.sax.Attributes attributes)
    {
        super(elementName, attributes);

        this.addressList = new ArrayList<Address>();
        this.telephoneNumberList = new ArrayList<TelephoneNumber>();
        this.emailAddressList = new ArrayList<EmailAddress>();
    }

    protected void doStartElement(String name, org.xml.sax.Attributes attributes) throws Exception
    {
        super.doStartElement(name, attributes);

        if (AddressParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            AddressParser parser = new AddressParser(name, attributes);
            this.addressList.add(parser);
            setCurrentElement(parser);
        }
        else if (PersonNameParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            PersonNameParser parser = new PersonNameParser(name, attributes);
            this.personName = parser;
            setCurrentElement(parser);
        }
        else if (TelephoneNumberParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            TelephoneNumberParser parser = new TelephoneNumberParser(name, attributes);
            this.telephoneNumberList.add(parser);
            setCurrentElement(parser);
        }
        else if (EmailAddressParser.ELEMENT_NAME.equalsIgnoreCase(name))
        {
            EmailAddressParser parser = new EmailAddressParser(name, attributes);
            this.emailAddressList.add(parser);
            setCurrentElement(parser);
        }
    }

    public int getAddressCount()
    {
        return this.addressList.size();
    }

    public int getIndex(Address address)
    {
        return this.addressList.indexOf(address);
    }

    public Address getAddress(int index)
    {
        if (index < 0 || index >= this.addressList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.addressList.get(index);
    }

    public void setAddress(int index, Address address)
    {
        if (index < 0 || index >= this.addressList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.addressList.set(index, address);
    }

    public void addAddress(int index, Address address)
    {
        if (index < 0 || index > this.addressList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.addressList.add(index, address);
    }

    public void addAddress(Address address)
    {
        this.addressList.add(address);
    }

    public void addAddresses(Collection<? extends Address> c)
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.addressList.addAll(c);
    }

    public void removeAddress(int index)
    {
        if (index < 0 || index >= this.addressList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.addressList.remove(index);
    }

    public void clearAddresses()
    {
        this.addressList.clear();
    }

    public Iterator<Address> getAddressIterator()
    {
        return this.addressList.iterator();
    }

    public PersonName getPersonName()
    {
        return this.personName;
    }

    public void setPersonName(PersonName personName)
    {
        this.personName = personName;
    }

    public int getTelephoneNumberCount()
    {
        return this.telephoneNumberList.size();
    }

    public int getIndex(TelephoneNumber telephoneNumber)
    {
        return this.telephoneNumberList.indexOf(telephoneNumber);
    }

    public TelephoneNumber getTelephoneNumber(int index)
    {
        if (index < 0 || index >= this.telephoneNumberList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.telephoneNumberList.get(index);
    }

    public void setTelephoneNumber(int index, TelephoneNumber telephoneNumber)
    {
        if (index < 0 || index >= this.telephoneNumberList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.telephoneNumberList.set(index, telephoneNumber);
    }

    public void addTelephoneNumber(int index, TelephoneNumber telephoneNumber)
    {
        if (index < 0 || index > this.telephoneNumberList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.telephoneNumberList.add(index, telephoneNumber);
    }

    public void addTelephoneNumber(TelephoneNumber telephoneNumber)
    {
        this.telephoneNumberList.add(telephoneNumber);
    }

    public void addTelephoneNumbers(Collection<? extends TelephoneNumber> c)
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.telephoneNumberList.addAll(c);
    }

    public void removeTelephoneNumber(int index)
    {
        if (index < 0 || index >= this.telephoneNumberList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.telephoneNumberList.remove(index);
    }

    public void clearTelephoneNumbers()
    {
        this.telephoneNumberList.clear();
    }

    public Iterator<TelephoneNumber> getTelephoneNumberIterator()
    {
        return this.telephoneNumberList.iterator();
    }

    public int getEmailAddressCount()
    {
        return this.emailAddressList.size();
    }

    public int getIndex(EmailAddress emailAddress)
    {
        return this.emailAddressList.indexOf(emailAddress);
    }

    public EmailAddress getEmailAddress(int index)
    {
        if (index < 0 || index >= this.emailAddressList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        return this.emailAddressList.get(index);
    }

    public void setEmailAddress(int index, EmailAddress emailAddress)
    {
        if (index < 0 || index >= this.emailAddressList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.emailAddressList.set(index, emailAddress);
    }

    public void addEmailAddress(int index, EmailAddress emailAddress)
    {
        if (index < 0 || index > this.emailAddressList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.emailAddressList.add(index, emailAddress);
    }

    public void addEmailAddress(EmailAddress emailAddress)
    {
        this.emailAddressList.add(emailAddress);
    }

    public void addEmailAddresss(Collection<? extends EmailAddress> c)
    {
        if (c == null)
        {
            String message = Logging.getMessage("nullValue.CollectionIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.emailAddressList.addAll(c);
    }

    public void removeEmailAddress(int index)
    {
        if (index < 0 || index >= this.emailAddressList.size())
        {
            String message = Logging.getMessage("generic.ArgumentOutOfRange", index);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.emailAddressList.remove(index);
    }

    public void clearEmailAddresses()
    {
        this.emailAddressList.clear();
    }

    public Iterator<EmailAddress> getEmailAddressIterator()
    {
        return this.emailAddressList.iterator();
    }
}
