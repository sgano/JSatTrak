/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.esg;

import gov.nasa.worldwind.applications.gio.catalogui.CatalogKey;
import gov.nasa.worldwind.applications.gio.catalogui.SwingUtils;
import gov.nasa.worldwind.applications.gio.ebrim.Address;
import gov.nasa.worldwind.applications.gio.ebrim.EmailAddress;
import gov.nasa.worldwind.applications.gio.ebrim.PersonName;
import gov.nasa.worldwind.applications.gio.ebrim.TelephoneNumber;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author dcollins
 * @version $Id: ServiceDetailsDialog.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class ServiceDetailsDialog extends JDialog
{
    private String contentPath;
    private String contentType;
    private String contentText;
    private AVList contentParams;
    private JPanel panel;
    private JEditorPane editorPane;
    private JScrollPane scrollPane;
    @SuppressWarnings({"FieldCanBeLocal"})
    private HyperlinkListener hyperlinkListener;
    private int maxHeight;
    private int maxWidth;
    private static final int DEFAULT_MAX_HEIGHT = 600;
    private static final int DEFAULT_MAX_WIDTH = Short.MAX_VALUE;

    protected ServiceDetailsDialog()
    {
        makeComponents();
        layoutComponents();
    }

    public static void showDialog(String contentPath, String contentType, AVList contentParams, boolean alwaysOnTop)
    {
        if (contentPath == null)
        {
            String message = "nullValue.ContentPathIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (contentType == null)
        {
            String message = "nullValue.ContentTypeIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        if (contentParams == null)
        {
            String message = "nullValue.ContentParamsIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        ServiceDetailsDialog dialog = new ServiceDetailsDialog();
        dialog.setContentPath(contentPath);
        dialog.setContentType(contentType);
        dialog.setContentParams(contentParams);
        dialog.setAlwaysOnTop(alwaysOnTop);
        dialog.doUpdate();

        Dimension size = dialog.getEditorPane().getPreferredSize();
        int hPadding = 16; // TODO: properly configure preferred size of editor
        int width = Math.min(dialog.maxWidth, size.width + hPadding);
        int height = Math.min(dialog.maxHeight, size.height);
        dialog.getEditorPane().setPreferredSize(new Dimension(width, height));
        dialog.pack();
        SwingUtils.centerWindowInDesktop(dialog);
        dialog.setVisible(true);
    }

    public String getContentPath()
    {
        return this.contentPath;
    }

    public void setContentPath(String contentPath)
    {
        if (contentPath == null)
        {
            String message = "nullValue.ContentPathIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }
        
        this.contentPath = contentPath;
        this.contentText = null;
    }

    public String getContentType()
    {
        return this.contentType;
    }

    public void setContentType(String contentType)
    {
        if (contentType == null)
        {
            String message = "nullValue.ContentTypeIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.contentType = contentType;
        this.contentText = null;
    }

    public AVList getContentParams()
    {
        return this.contentParams;
    }

    public void setContentParams(AVList contentParams)
    {
        if (contentParams == null)
        {
            String message = "nullValue.ContentParamsIsNull";
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.contentParams = contentParams;
    }

    protected JEditorPane getEditorPane()
    {
        return this.editorPane;
    }

    protected JScrollPane getScrollPane()
    {
        return this.scrollPane;
    }

    protected void doUpdate()
    {
        AVList params = initContentParams(this.contentParams);

        String type = this.contentType;
        if (type == null)
            type = "text/plain";

        String text = null;
        if (this.contentText == null)
            this.contentText = loadContent();
        if (this.contentText != null)
            text = replaceContentParams(this.contentText, params);

        this.editorPane.setContentType(type);
        this.editorPane.setText(text);
    }

    protected void onHyperlinkPressed(URL url)
    {
        if (url != null)
        {
            if (this.contentParams != null)
            {
                this.contentParams.firePropertyChange(CatalogKey.ACTION_COMMAND_BROWSE, null, url);
            }
        }
    }

    protected void onHyperlinkPressed(String propertyName)
    {
        if (propertyName != null)
        {
            if (this.contentParams != null)
            {
                Object o = this.contentParams.getValue(propertyName);
                if (o != null)
                {
                    if (o instanceof Object[])
                    {
                        for (Object v : (Object[]) o)
                            if (v != null)
                                this.contentParams.firePropertyChange(CatalogKey.ACTION_COMMAND_BROWSE, null, v);
                    }
                    else
                    {
                        this.contentParams.firePropertyChange(CatalogKey.ACTION_COMMAND_BROWSE, null, o);
                    }
                }
            }
        }
    }

    protected String loadContent()
    {
        StringBuilder sb = null;
        try
        {
            Object o = WWIO.getFileOrResourceAsStream(this.contentPath, getClass());
            if (o != null)
            {
                if (o instanceof Exception)
                {
                    throw (Exception) o;
                }
                else if (o instanceof InputStream)
                {
                    sb = new StringBuilder();
                    InputStream is = (InputStream) o;
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String line;
                    while ((line = br.readLine()) != null)
                    {
                        sb.append(line);
                    }
                }
            }
        }
        catch (Exception e)
        {
            String message = "esg.ExceptionWhileLoadingContent";
            Logging.logger().log(java.util.logging.Level.SEVERE, message, e);
        }
        return sb != null ? sb.toString() : null;
    }

    protected AVList initContentParams(AVList params)
    {
        if (params != null)
        {
            params = params.copy();
            if (params.getValue(CatalogKey.ABSTRACT) == null)
                params.setValue(CatalogKey.ABSTRACT, "None");
            if (params.getValue(CatalogKey.CONTACT_ADDRESS) == null)
                params.setValue(CatalogKey.CONTACT_ADDRESS, "None");
            if (params.getValue(CatalogKey.CONTACT_ADDRESS_TYPE) == null)
                params.setValue(CatalogKey.CONTACT_ADDRESS_TYPE, "None");
            if (params.getValue(CatalogKey.CONTACT_NAME) == null)
                params.setValue(CatalogKey.CONTACT_NAME, "None");
            if (params.getValue(CatalogKey.CONTACT_TELEPHONE_NUMBER) == null)
                params.setValue(CatalogKey.CONTACT_TELEPHONE_NUMBER, "None");
            if (params.getValue(CatalogKey.CONTACT_EMAIL_ADDRESSS) == null)
                params.setValue(CatalogKey.CONTACT_EMAIL_ADDRESSS, "None");
            if (params.getValue(CatalogKey.CONTENT_START_DATE) == null)
                params.setValue(CatalogKey.CONTENT_START_DATE, "None");
            if (params.getValue(CatalogKey.CONTENT_END_DATE) == null)
                params.setValue(CatalogKey.CONTENT_END_DATE, "None");
            if (params.getValue(CatalogKey.DESCRIPTION) == null)
                params.setValue(CatalogKey.DESCRIPTION, "None");
            if (params.getValue(CatalogKey.HARVEST_DATE) == null)
                params.setValue(CatalogKey.HARVEST_DATE, "None");
            if (params.getValue(CatalogKey.HARVEST_TYPE) == null)
                params.setValue(CatalogKey.HARVEST_TYPE, "None");
            if (params.getValue(CatalogKey.ID) == null)
                params.setValue(CatalogKey.ID, "None");
            if (params.getValue(CatalogKey.KEYWORDS) == null)
                params.setValue(CatalogKey.KEYWORDS, "None");
            if (params.getValue(CatalogKey.MODIFICATION_DATE) == null)
                params.setValue(CatalogKey.MODIFICATION_DATE, "");
            if (params.getValue(CatalogKey.MAX_LATITUDE) == null)
                params.setValue(CatalogKey.MAX_LATITUDE, "None");
            if (params.getValue(CatalogKey.MAX_LONGITUDE) == null)
                params.setValue(CatalogKey.MAX_LONGITUDE, "None");
            if (params.getValue(CatalogKey.MIN_LATITUDE) == null)
                params.setValue(CatalogKey.MIN_LATITUDE, "None");
            if (params.getValue(CatalogKey.MIN_LONGITUDE) == null)
                params.setValue(CatalogKey.MIN_LONGITUDE, "None");
            if (params.getValue(CatalogKey.NAME) == null)
                params.setValue(CatalogKey.NAME, "None");
            if (params.getValue(ESGKey.NATIONAL_APPLICATIONS) == null)
                params.setValue(ESGKey.NATIONAL_APPLICATIONS, "None");
            if (params.getValue(CatalogKey.ONLINE_RESOURCE) == null)
                params.setValue(CatalogKey.ONLINE_RESOURCE, "None");
            if (params.getValue(CatalogKey.ORIGINATOR) == null)
                params.setValue(CatalogKey.ORIGINATOR, "None");
            if (params.getValue(CatalogKey.SERVICE_TYPE) == null)
                params.setValue(CatalogKey.SERVICE_TYPE, "None");
            if (params.getValue(CatalogKey.TITLE) == null)
                params.setValue(CatalogKey.TITLE, "None");
            if (params.getValue(CatalogKey.VERSION) == null)
                params.setValue(CatalogKey.VERSION, "None");
        }
        return params;
    }

    protected String replaceContentParams(String content, AVList params)
    {
        StringBuilder sb = null;
        if (content != null)
        {
            sb = new StringBuilder(content);
            if (params != null)
            {
                Set<Map.Entry<String, Object>> entrySet = params.getEntries();
                if (entrySet != null)
                {
                    for (Map.Entry<String, Object> entry : entrySet)
                    {
                        if (entry != null)
                        {
                            String key = "${" + entry.getKey() + "}";
                            String value = formatValue(entry.getValue());
                            replaceAll(sb, key, value);
                        }
                    }
                }
            }
        }
        return sb != null ? sb.toString() : null;
    }

    protected void replaceAll(StringBuilder src, String key, String value)
    {
        if (src != null && key != null)
        {
            if (value == null)
                value = "";
            
            int keyLen = key.length();
            int valueLen = value.length();
            int i = 0;
            while ((i = src.indexOf(key, i)) != -1)
            {
                src.replace(i, i + keyLen, value);
                i += valueLen;
            }
        }
    }

    protected String formatValue(Object value)
    {
        String svalue = null;
        if (value != null)
        {
            if (value instanceof Object[])
                svalue = formatValueArray((Object[]) value);
            else if (value instanceof Date)
                svalue = formatDate((Date) value);
            else if (value instanceof Address)
                svalue = formatAddress((Address) value);
            else if (value instanceof EmailAddress)
                svalue = formatEmailAddress((EmailAddress) value);
            else if (value instanceof TelephoneNumber)
                svalue = formatTelephoneNumber((TelephoneNumber) value);
            else if (value instanceof PersonName)
                svalue = formatPersonName((PersonName) value);
            else
            {
                svalue = value.toString();
                if (svalue != null)
                    svalue = svalue.trim();
            }
        }
        return svalue;
    }

    protected String formatValueArray(Object[] value)
    {
        StringBuilder sb = null;
        if (value != null)
        {
            for (Object o : value)
            {
                if (o != null)
                {
                    String s = formatValue(o);
                    if (s != null)
                    {
                        s = s.trim();
                        if (s.length() > 0)
                        {
                            if (sb == null)
                                sb = new StringBuilder();
                            if (sb.length() > 0)
                                sb.append(", ");
                            sb.append(s);
                        }
                    }
                }
            }
        }
        return sb != null ? sb.toString() : null;
    }

    protected String formatDate(Date date)
    {
        String s = null;
        if (date != null)
        {
            DateFormat df = new SimpleDateFormat("MMMM dd, yyyy");
            s = df.format(date);
        }
        return s;
    }

    protected String formatAddress(Address address)
    {
        String s = null;
        if (address != null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(address.getStreetNumber());
            sb.append(" ");
            sb.append(address.getStreet());
            sb.append("<br>");
            sb.append(address.getCity());
            sb.append(" ");
            sb.append(address.getStateOrProvince());
            sb.append(", ");
            sb.append(address.getPostalCode());
            sb.append(" ");
            sb.append(address.getCountry());
            s = sb.toString();
        }
        return s;
    }

    protected String formatEmailAddress(EmailAddress emailAddress)
    {
        String s = null;
        if (emailAddress != null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(emailAddress.getType());
            sb.append(":");
            sb.append("<a href=\"");
            sb.append("mailto:");
            sb.append(emailAddress.getAddress());
            sb.append("\">");
            sb.append(emailAddress.getAddress());
            sb.append("</a>");
            s = sb.toString();
        }
        return s;
    }

    protected String formatTelephoneNumber(TelephoneNumber telephoneNumber)
    {
        String s = null;
        if (telephoneNumber != null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(telephoneNumber.getPhoneType());
            sb.append(":");
            sb.append(telephoneNumber.getCountryCode());
            sb.append("-");
            sb.append(telephoneNumber.getAreaCode());
            sb.append("-");
            sb.append(telephoneNumber.getNumber());
            sb.append("x");
            sb.append(telephoneNumber.getExtension());
            s = sb.toString();
        }
        return s;
    }

    protected String formatPersonName(PersonName personName)
    {
        String s = null;
        if (personName != null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append(personName.getFirstName());
            sb.append(" ");
            sb.append(personName.getMiddleName());
            sb.append(" ");
            sb.append(personName.getLastName());
            s = sb.toString();
        }
        return s;
    }

    private void makeComponents()
    {
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        this.panel = new JPanel();
        this.editorPane = new JEditorPane();
        this.editorPane.setEditable(false);
        this.hyperlinkListener = new EditorListener(this);
        this.editorPane.addHyperlinkListener(this.hyperlinkListener);

        this.maxHeight = DEFAULT_MAX_HEIGHT;
        this.maxWidth = DEFAULT_MAX_WIDTH;
    }

    private void layoutComponents()
    {
        getContentPane().setLayout(new BorderLayout());

        this.panel.setLayout(new BorderLayout());
        this.editorPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.scrollPane = new JScrollPane(this.editorPane);
        this.panel.add(this.scrollPane, BorderLayout.CENTER);

        getContentPane().add(this.panel, BorderLayout.CENTER);
    }

    protected static class EditorListener implements HyperlinkListener
    {
        private ServiceDetailsDialog serviceDetailsDialog;

        public EditorListener(ServiceDetailsDialog serviceDetailsDialog)
        {
            this.serviceDetailsDialog = serviceDetailsDialog;
        }

        public void hyperlinkUpdate(HyperlinkEvent e)
        {
            if (e != null)
            {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
                {
                    URL url = e.getURL();
                    if (url != null)
                    {
                        if (this.serviceDetailsDialog != null)
                            this.serviceDetailsDialog.onHyperlinkPressed(url);
                    }
                    else
                    {
                        String desc = e.getDescription();
                        if (desc != null)
                        {
                            if (this.serviceDetailsDialog != null)
                                this.serviceDetailsDialog.onHyperlinkPressed(desc);
                        }
                    }
                }
                if (e.getEventType() == HyperlinkEvent.EventType.ENTERED)
                {
                    Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
                    if (cursor != null)
                        if (this.serviceDetailsDialog != null)
                            this.serviceDetailsDialog.setCursor(cursor);
                }
                if (e.getEventType() == HyperlinkEvent.EventType.EXITED)
                {
                    Cursor cursor = Cursor.getDefaultCursor();
                    if (cursor != null)
                        if (this.serviceDetailsDialog != null)
                            this.serviceDetailsDialog.setCursor(cursor);
                }
            }
        }
    }
}
