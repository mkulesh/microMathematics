/*******************************************************************************
 * microMathematics Plus - Extended visual calculator
 * *****************************************************************************
 * Copyright (C) 2014-2017 Mikhail Kulesh
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.mkulesh.micromath.io;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Tutorial: http://developer.android.com/training/basics/network-ops/xml.html
public class XmlUtils
{
    public static void skipEntry(XmlPullParser parser) throws Exception
    {
        if (parser.getEventType() != XmlPullParser.START_TAG)
        {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0)
        {
            switch (parser.next())
            {
            case XmlPullParser.END_TAG:
                depth--;
                break;
            case XmlPullParser.START_TAG:
                depth++;
                break;
            }
        }
    }

    public static void skipTagContent(XmlPullParser parser) throws IOException, XmlPullParserException
    {
        final int depth = parser.getDepth();
        while (true)
        {
            parser.next();
            if (parser.getEventType() == XmlPullParser.END_TAG && parser.getDepth() == depth)
            {
                break;
            }
        }
    }

    static boolean ensureAttribute(Element e, String type, String s)
    {
        return e.getAttribute(type) != null && e.getAttribute(type).equals(s);
    }

    static List<Element> getElements(final Element e, final String name)
    {
        List<Element> retValue = new ArrayList<>();
        for (Node object = e.getFirstChild(); object != null; object = object.getNextSibling())
        {
            if (object instanceof Element)
            {
                final Element en = (Element) object;
                if (name == null || name.equals(en.getTagName()))
                {
                    retValue.add(en);
                }
            }
        }
        return retValue;
    }

    static List<Element> getElements(final Element e)
    {
        return getElements(e, null);
    }

    static Element getElement(final List<Element> list, final String name)
    {
        for (Element en : list)
        {
            if (name.equals(en.getTagName()))
            {
                return en;
            }
        }
        return null;
    }

    static Element getLast(final List<Element> elements)
    {
        if (!elements.isEmpty())
        {
            Element e = elements.get(elements.size() - 1);
            if (e == null)
            {
                return null;
            }
            if (e.getTextContent() == null)
            {
                return null;
            }
            return e;
        }
        return null;
    }

    static Element removeLast(final List<Element> elements)
    {
        if (!elements.isEmpty())
        {
            Element e = elements.get(elements.size() - 1);
            elements.remove(elements.size() - 1);
            return e;
        }
        return null;
    }
}
