/* Copyright (C) 2001, 2009 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gos.html;

import gov.nasa.worldwind.applications.gos.OnlineResource;
import gov.nasa.worldwind.util.WWUtil;

/**
 * @author dcollins
 * @version $Id: HTMLFormatter.java 13555 2010-07-15 16:49:17Z dcollins $
 */
public class HTMLFormatter
{
    protected boolean enableAdvancedHtml = true;
    protected int numLinksInSeries;

    public HTMLFormatter()
    {
    }

    public boolean isEnableAdvancedHTML()
    {
        return this.enableAdvancedHtml;
    }

    public void setEnableAdvancedHTML(boolean enable)
    {
        this.enableAdvancedHtml = enable;
    }

    public void addImage(StringBuilder sb, Object source, String text)
    {
        sb.append("<img ");

        if (source != null)
            sb.append(" src=\"").append(source).append("\"");

        if (!WWUtil.isEmpty(text))
            sb.append(" alt=\"").append(text).append("\"");

        sb.append("/>");
    }

    public void addLineBreak(StringBuilder sb)
    {
        sb.append("<br/>");
    }

    public void addResourceHyperlink(StringBuilder sb, OnlineResource resource, String displayName, String color)
    {
        if (WWUtil.isEmpty(displayName) && resource != null)
            displayName = resource.getDisplayName();

        if (resource != null && resource.getURL() != null)
            this.beginHyperlink(sb, resource.getURL().toString());

        if (color != null)
            this.beginFont(sb, color);

        if (!WWUtil.isEmpty(displayName))
            sb.append(displayName);

        if (color != null)
            this.endFont(sb);

        if (resource != null && resource.getURL() != null)
            this.endHyperlink(sb);
    }

    public void addResourceHyperlink(StringBuilder sb, OnlineResource resource)
    {
        this.addResourceHyperlink(sb, resource, null, null);
    }

    public void addResourceHyperlinkInSeries(StringBuilder sb, OnlineResource resource, String displayText,
        String color)
    {
        if (this.numLinksInSeries > 0)
        {
            this.addSpace(sb);
            sb.append("-");
            this.addSpace(sb);
        }

        this.addResourceHyperlink(sb, resource, displayText, color);
        this.numLinksInSeries++;
    }

    public void addResourceHyperlinkInSeries(StringBuilder sb, OnlineResource resource)
    {
        this.addResourceHyperlinkInSeries(sb, resource, null, null);
    }

    public void addResourceImage(StringBuilder sb, OnlineResource resource)
    {
        this.addImage(sb, resource.getURL(), resource.getDisplayName());
    }

    public void addSpace(StringBuilder sb)
    {
        sb.append(this.isEnableAdvancedHTML() ? "&nbsp;" : " ");
    }

    public void addText(StringBuilder sb, String text, int maxCharacters)
    {
        boolean truncate = false;

        if (maxCharacters > 0)
        {
            int len = maxCharacters - 3;
            if (text.endsWith("..."))
                len = len - 3;

            if (len > 0 && len < text.length() - 1)
            {
                text = text.substring(0, len);
                text = text.trim();
                truncate = true;
            }
        }

        sb.append(text);

        if (truncate)
            sb.append("...");
    }

    public void beginFont(StringBuilder sb, String color)
    {
        sb.append("<font color=\"").append(color).append("\">");
    }

    public void endFont(StringBuilder sb)
    {
        sb.append("</font>");
    }

    public void beginHeading(StringBuilder sb, int level)
    {
        if (this.isEnableAdvancedHTML())
        {
            sb.append("<h").append(level).append(">");
        }
        else
        {
            sb.append("<b>");
        }
    }

    public void endHeading(StringBuilder sb, int level)
    {
        if (this.isEnableAdvancedHTML())
        {
            sb.append("</h").append(level).append(">");
        }
        else
        {
            sb.append("</b><br/>");
        }
    }

    public void beginHTMLBody(StringBuilder sb)
    {
        sb.append("<html><head/><body>");
    }

    public void endHTMLBody(StringBuilder sb)
    {
        sb.append("</body></html>");
    }

    public void beginHyperlink(StringBuilder sb, String href)
    {
        sb.append("<a href=\"").append(href).append("\">");
    }

    public void endHyperlink(StringBuilder sb)
    {
        sb.append("</a>");
    }

    public void beginHyperlinkSeries(StringBuilder sb)
    {
        this.numLinksInSeries = 0;
    }

    public void endHyperlinkSeries(StringBuilder sb)
    {
        this.numLinksInSeries = 0;
    }
}
