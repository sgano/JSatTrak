/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.applications.gio.catalogui.CatalogKey;
import gov.nasa.worldwind.applications.gio.ebrim.*;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.Logging;

import java.util.*;

/**
 * @author dcollins
 * @version $Id: RegistryObjectUtils.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class RegistryObjectUtils
{
    public static void makeCommonParams(RegistryObject src, AVList dest)
    {
        makeCommonParams(src, dest, true);
    }

    public static void makeCommonParamsNoOverwrite(RegistryObject src, AVList dest)
    {
        makeCommonParams(src, dest, false);
    }

    private static void makeCommonParams(RegistryObject src, AVList dest, boolean overwrite)
    {
        if (src == null)
        {
            String message = "nullValue.SrcIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        InternationalString is = src.getDescription();
        if (is != null)
        {
            String s = getStringForLocale(is, Locale.getDefault());
            if (s != null)
                if (dest.getValue(CatalogKey.DESCRIPTION) == null || overwrite)
                    dest.setValue(CatalogKey.DESCRIPTION, s);
        }

        String s = src.getId();
        if (s != null)
            if (dest.getValue(CatalogKey.ID) == null || overwrite)
                dest.setValue(CatalogKey.ID, s);

        is = src.getName();
        if (is != null)
        {
            s = getStringForLocale(is, Locale.getDefault());
            if (s != null)
                if (dest.getValue(CatalogKey.NAME) == null || overwrite)
                    dest.setValue(CatalogKey.NAME, s);
        }

        for (Iterator<Slot> iter = src.getSlotIterator(); iter.hasNext(); )
        {
            Slot slot = iter.next();
            if (slot != null)
            {
                String name = slot.getName();
                if ("Abstract".equalsIgnoreCase(name))
                {
                    String[] sv = getValues(slot);
                    if (sv != null)
                        if (dest.getValue(CatalogKey.ABSTRACT) == null || overwrite)
                            dest.setValue(CatalogKey.ABSTRACT, sv);
                }
                else if ("Content Start Date".equalsIgnoreCase(name))
                {
                    s = getFirstValue(slot);
                    if (s != null)
                        if (dest.getValue(CatalogKey.CONTENT_START_DATE) == null || overwrite)
                            dest.setValue(CatalogKey.CONTENT_START_DATE, asDate(s));
                }
                else if ("Content End Date".equalsIgnoreCase(name))
                {
                    s = getFirstValue(slot);
                    if (s != null)
                        if (dest.getValue(CatalogKey.CONTENT_END_DATE) == null || overwrite)
                            dest.setValue(CatalogKey.CONTENT_END_DATE, asDate(s));
                }
                else if ("Harvest Date".equalsIgnoreCase(name))
                {
                    s = getFirstValue(slot);
                    if (s != null)
                        if (dest.getValue(CatalogKey.HARVEST_DATE) == null || overwrite)
                            dest.setValue(CatalogKey.HARVEST_DATE, asDate(s));
                }
                else if ("Harvest Type".equalsIgnoreCase(name))
                {
                    s = getFirstValue(slot);
                    if (s != null)
                        if (dest.getValue(CatalogKey.HARVEST_TYPE) == null || overwrite)
                            dest.setValue(CatalogKey.HARVEST_TYPE, s);
                }
                else if ("Keyword".equalsIgnoreCase(name) ||
                         "Keywords".equalsIgnoreCase(name) ||
                         "KeywordList".equalsIgnoreCase(name))
                {
                    String[] sv = getValues(slot);
                    if (sv != null)
                        if (dest.getValue(CatalogKey.KEYWORDS) == null || overwrite)
                            dest.setValue(CatalogKey.KEYWORDS, sv);
                }
                else if ("Modification Date".equalsIgnoreCase(name))
                {
                    s = getFirstValue(slot);
                    if (s != null)
                        if (dest.getValue(CatalogKey.MODIFICATION_DATE) == null || overwrite)
                            dest.setValue(CatalogKey.MODIFICATION_DATE, asDate(s));
                }
                else if ("Online Resource".equalsIgnoreCase(name))
                {
                    String[] sv = getValues(slot);
                    if (sv != null)
                        if (dest.getValue(CatalogKey.ONLINE_RESOURCE) == null || overwrite)
                            dest.setValue(CatalogKey.ONLINE_RESOURCE, sv);
                }
                else if ("Originator".equalsIgnoreCase(name))
                {
                    s = getFirstValue(slot);
                    if (s != null)
                        if (dest.getValue(CatalogKey.ORIGINATOR) == null || overwrite)
                            dest.setValue(CatalogKey.ORIGINATOR, s);
                }
                else if (name != null && (name.contains("title") || name.contains("Title")))
                {
                    s = getFirstValue(slot);
                    if (s != null)
                        if (dest.getValue(CatalogKey.TITLE) == null || overwrite)
                            dest.setValue(CatalogKey.TITLE, s);
                }
                else if ("Version".equalsIgnoreCase(name))
                {
                    s = getFirstValue(slot);
                    if (s != null)
                        if (dest.getValue(CatalogKey.VERSION) == null || overwrite)
                            dest.setValue(CatalogKey.VERSION, s);
                }
            }
        }
    }

    public static void makeClassificationParams(Iterator<ClassificationNode> src, AVList dest)
    {
        if (src == null)
        {
            String message = "nullValue.SrcIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String serviceType = null;
        List<String> nationalAppList = null;
        while (src.hasNext())
        {
            ClassificationNode cls = src.next();
            if (cls != null)
            {
                String s = cls.getCode();
                if (s != null)
                {
                    if (s.contains("wms") || s.contains("WMS"))
                        serviceType = CatalogKey.WMS;
                    else if (s.contains("wfs") || s.contains("WFS"))
                        serviceType = CatalogKey.WFS;
                    else if (s.contains("wcs") || s.contains("WCS"))
                        serviceType = CatalogKey.WCS;
                    // "1000" is the code for the "NASA National Applications" ClassificationScheme.
                    else if (s.contains("1000"))
                    {
                        InternationalString is = cls.getName();
                        if (is != null)
                        {
                            s = RegistryObjectUtils.getStringForLocale(is, Locale.getDefault());
                            if (nationalAppList == null)
                                nationalAppList = new ArrayList<String>();
                            nationalAppList.add(s);
                        }
                    }
                }
            }
        }

        dest.setValue(CatalogKey.SERVICE_TYPE, serviceType);

        String[] nationalAppArray = null;
        if (nationalAppList != null)
        {
            nationalAppArray = new String[nationalAppList.size()];
            nationalAppList.toArray(nationalAppArray);
        }
        dest.setValue(ESGKey.NATIONAL_APPLICATIONS, nationalAppArray);
    }

    public static void makePersonParams(Person src, AVList dest)
    {
        if (src == null)
        {
            String message = "nullValue.SrcIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (dest == null)
        {
            String message = "nullValue.DestIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        PersonName personName = src.getPersonName();
        if (personName != null)
            dest.setValue(CatalogKey.CONTACT_NAME, personName);

        Iterator<Address> addressIter = src.getAddressIterator();
        if (addressIter != null)
            dest.setValue(CatalogKey.CONTACT_ADDRESS, asArray(addressIter, new Address[0]));

        Iterator<EmailAddress> emailIter = src.getEmailAddressIterator();
        if (emailIter != null)
            dest.setValue(CatalogKey.CONTACT_EMAIL_ADDRESSS, asArray(emailIter, new EmailAddress[0]));

        Iterator<TelephoneNumber> telephoneNumberIterator = src.getTelephoneNumberIterator();
        if (telephoneNumberIterator != null)
            dest.setValue(CatalogKey.CONTACT_TELEPHONE_NUMBER, asArray(telephoneNumberIterator, new TelephoneNumber[0]));

        for (Iterator<Slot> iter = src.getSlotIterator(); iter.hasNext(); )
        {
            Slot slot = iter.next();
            if (slot != null)
            {
                String name = slot.getName();
                if ("AddressType".equalsIgnoreCase(name))
                {
                    String[] sv = getValues(slot);
                    if (sv != null)
                        dest.setValue(CatalogKey.CONTACT_ADDRESS_TYPE, sv);
                }
            }
        }
    }

    public static String[] getStrings(InternationalString is)
    {
        if (is == null)
        {
            String message = "nullValue.InternationalStringIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ArrayList<String> values = new ArrayList<String>();
        for (LocalizedString ls : is)
        {
            String s = ls != null ? ls.getValue() : null;
            values.add(s);
        }

        String[] array = new String[values.size()];
        values.toArray(array);
        return array;
    }

    public static String getFirstString(InternationalString is)
    {
        if (is == null)
        {
            String message = "nullValue.InternationalStringIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (LocalizedString ls : is)
        {
            if (ls != null)
            {
                String s = ls.getValue();
                if (s != null)
                    return s;
            }
        }
        return null;
    }

    public static String getStringForLocale(InternationalString is, Locale locale)
    {
        if (is == null)
        {
            String message = "nullValue.InternationalStringIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String s = null;
        LocalizedString first = null;
        LocalizedString match = null;

        for (LocalizedString ls : is)
        {
            if (ls != null)
            {
                if (first == null)
                    first = ls;

                String lang = ls.getLang();
                if (lang != null && locale != null)
                {
                    lang = lang.replaceAll("[ _-]", ".");
                    if (lang.matches(locale.toString()))
                    {
                        match = ls;
                        break;
                    }
                }
            }
        }

        if (match != null)
            s = match.getValue();
        else if (first != null)
            s = first.getValue();
        return s;
    }

    public static String[] getValues(Slot slot)
    {
        if (slot == null)
        {
            String message = "nullValue.SlotIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ArrayList<String> values = null;
        ValueList vl = slot.getValueList();
        if (vl != null)
        {
            values = new ArrayList<String>();
            for (Value v : vl)
            {
                String s = v != null ? v.getValue() : null;
                values.add(s);
            }
        }

        String[] array = null;
        if (values != null)
        {
            array = new String[values.size()];
            values.toArray(array);
        }
        return array;
    }

    public static String getFirstValue(Slot slot)
    {
        if (slot == null)
        {
            String message = "nullValue.SlotIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ValueList vl = slot.getValueList();
        if (vl != null)
        {
            for (Value v : vl)
            {
                if (v != null)
                {
                    String s = v.getValue();
                    if (s != null)
                        return s;
                }
            }
        }
        return null;
    }

    private static <T> T[] asArray(Iterator<T> iter, T[] a)
    {
        if (iter == null)
        {
            String message = Logging.getMessage("nullValue.Iterator");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        T obj = null;
        List<T> list = null;
        while (iter.hasNext())
        {
            obj = iter.next();
            if (obj != null)
            {
                if (list == null)
                    list = new ArrayList<T>();
                list.add(obj);
            }
        }

        T[] array = null;
        if (list != null && obj != null)
            array = list.toArray(a);
        return array;
    }

    private static Object asDate(String s)
    {
        if (s == null)
        {
            String message = Logging.getMessage("nullValue.StringIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Date date = ParserUtils.parseWMSDate(s);
        return date != null ? date : s;
    }
}
