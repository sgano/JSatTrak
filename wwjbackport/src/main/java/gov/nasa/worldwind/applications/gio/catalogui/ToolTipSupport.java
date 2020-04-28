/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.catalogui;

import java.awt.*;
import java.util.ArrayList;

/**
 * @author dcollins
 * @version $Id: ToolTipSupport.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public class ToolTipSupport
{
    private StringBuilder sb;
    private int lineLength;
    private String lineSeparator;
    private static final int DEFAULT_LINE_LENGTH = 80;
    private static final String LINE_SEPARATOR = "<br>";

    public ToolTipSupport()
    {
        this.sb = new StringBuilder();
        this.lineLength = DEFAULT_LINE_LENGTH;
        //noinspection RedundantStringConstructorCall
        this.lineSeparator = new String(LINE_SEPARATOR);
    }

    public int getLineLength()
    {
        return this.lineLength;
    }

    public void setLineLength(int lineLength)
    {
        this.lineLength = lineLength;
    }

    public String getLineSeparator()
    {
        return this.lineSeparator;
    }

    public void setLineSeparator(String lineSeparator)
    {
        this.lineSeparator = lineSeparator;
    }

    public String getText()
    {
        String toolTipText = null;
        if (this.sb.length() > 0)
        {
            StringBuilder tmp = new StringBuilder();
            tmp.append("<html>");
            tmp.append(this.sb);
            tmp.append("</html>");
            toolTipText = tmp.toString();
        }
        return toolTipText;
    }

    public boolean hasText()
    {
        return this.sb.length() > 0;
    }

    public void append(String s)
    {
        append(s, false, null, 0, this.sb);
    }

    public void append(String s, int style)
    {
        append(s, false, null, style, this.sb);
    }

    public void append(String[] sv)
    {
        if (sv != null)
        {
            for (int i = 0; i < sv.length; i++)
            {
                if (i != 0)
                    this.sb.append(this.lineSeparator);
                append(sv[i], false, null, 0, this.sb);
            }
        }
    }

    public void append(String[] sv, int style)
    {
        if (sv != null)
        {
            for (int i = 0; i < sv.length; i++)
            {
                if (i != 0)
                    this.sb.append(this.lineSeparator);
                append(sv[i], false, null, style, this.sb);
            }
        }
    }

    public void appendWrapped(String s)
    {
        append(s, true, this.lineSeparator, 0, this.sb);
    }

    public void appendWrapped(String s, int style)
    {
        append(s, true, this.lineSeparator, style, this.sb);
    }

    public void appendLine()
    {
        this.sb.append(this.lineSeparator);
    }

    public void appendParagraph()
    {
        this.sb.append(this.lineSeparator);
        this.sb.append(this.lineSeparator);
    }

    public void append(Iterable<CatalogException> exceptions)
    {
        if (exceptions != null)
        {
            StringBuilder tmp = null;
            int i = 0;
            for (CatalogException e : exceptions)
            {
                if (e != null)
                {
                    if (e.getDescription() != null)
                    {
                        if (tmp == null)
                            tmp = new StringBuilder();
                        if (i != 0)
                            tmp.append(this.lineSeparator);
                        wrapText(e.getDescription(), this.lineLength, this.lineSeparator, tmp);
                    }
                    i++;
                }
            }

            if (i != 0)
            {
                this.sb.append("<font color=\"red\">");
                this.sb.append("<b>");
                this.sb.append(i);
                this.sb.append(i == 1 ? " error" : " errors");
                this.sb.append("</b>");
                if (tmp != null)
                {
                    this.sb.append(this.lineSeparator);
                    this.sb.append(tmp);
                }
                this.sb.append("</font>");
            }
        }
    }

    public void clear()
    {
        if (this.sb.length() > 0)
            this.sb.delete(0, this.sb.length());
    }

    private void append(String src, boolean wrap, String lineSeparator, int style, StringBuilder dest)
    {
        if (src != null)
        {
            if ((style & Font.BOLD) != 0)
                dest.append("<b>");
            if ((style & Font.ITALIC) != 0)
                dest.append("<i>");

            if (wrap && lineSeparator != null)
                wrapText(src, this.lineLength, lineSeparator, dest);
            else
                dest.append(src);

            if ((style & Font.BOLD) != 0)
                dest.append("</b>");
            if ((style & Font.ITALIC) != 0)
                dest.append("</i>");
        }
    }

    private void wrapText(String src, int lineLength, String lineSeparator, StringBuilder dest)
    {
        if (src == null)
            return;

        if (lineLength <= 0)
            return;

        if (src.length() <= lineLength)
        {
            dest.append(src);
            return;
        }

        char[] chars = src.toCharArray();
        ArrayList<String> lines = new ArrayList<String>();
        StringBuilder line = new StringBuilder();
        StringBuilder word = new StringBuilder();

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < chars.length; i++)
        {
            word.append(chars[i]);

            if (chars[i] == ' ')
            {
                if ((line.length() + word.length()) > lineLength)
                {
                    lines.add(line.toString());
                    line.delete(0, line.length());
                }

                line.append(word);
                word.delete(0, word.length());
            }
        }

        if (word.length() > 0)
        {
            if ((line.length() + word.length()) > lineLength)
            {
                lines.add(line.toString());
                line.delete(0, line.length());
            }
            line.append(word);
        }

        if (line.length() > 0)
        {
            lines.add(line.toString());
        }

        for (int i = 0; i < lines.size(); i++)
        {
            if (i != 0)
                dest.append(lineSeparator);
            dest.append(lines.get(i));
        }
    }
}
