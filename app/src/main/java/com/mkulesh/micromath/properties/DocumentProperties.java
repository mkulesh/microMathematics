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
package com.mkulesh.micromath.properties;

import android.content.Context;
import android.os.Bundle;

import com.mkulesh.micromath.formula.FormulaList;
import com.mkulesh.micromath.utils.CompatUtils;
import com.mkulesh.micromath.widgets.ScaledDimensions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.text.DecimalFormat;
import java.text.ParseException;

public class DocumentProperties
{
    /**
     * Constants used to save/restore the instance state.
     */
    private static final String STATE_DOCUMENT_REFORMAT = "document_reformat";
    private static final String STATE_DOCUMENT_TEXT_WIDTH = "document_text_width";
    private static final String STATE_DOCUMENT_SIGNIFICANT_DIGITS = "document_significant_digits";
    private static final String STATE_DOCUMENT_SCALE_FACTOR = "document_scale_factor";
    private static final String STATE_DOCUMENT_REDEFINE_ALLOWED = "document_redefine_allowed";

    /**
     * Constants used to write/read the XML file.
     */
    public static final String XML_PROP_TEXT_WIDTH = "textWidth";
    public static final String XML_PROP_SIGNIFICANT_DIGITS = "significantDigits";
    public static final String XML_PROP_SCALE = "scale";
    public static final String XML_PROP_REDEFINE_ALLOWED = "redefineAllowed";

    /**
     * Class members.
     */
    public boolean reformat = false; // not saved in xml
    public int textWidth = 60;
    public int significantDigits = 6;
    private ScaledDimensions scaledDimensions = null;
    public boolean redefineAllowed = false;

    /**
     * Default constructor
     */
    public DocumentProperties(Context context)
    {
        scaledDimensions = new ScaledDimensions(context);
    }

    /**
     * Procedure returns actual scaled dimensions
     */
    public ScaledDimensions getScaledDimensions()
    {
        return scaledDimensions;
    }

    public void readFromBundle(Bundle inState)
    {
        reformat = inState.getBoolean(STATE_DOCUMENT_REFORMAT);
        textWidth = inState.getInt(STATE_DOCUMENT_TEXT_WIDTH);
        significantDigits = inState.getInt(STATE_DOCUMENT_SIGNIFICANT_DIGITS);
        scaledDimensions.setScaleFactor(inState.getFloat(STATE_DOCUMENT_SCALE_FACTOR));
        redefineAllowed = inState.getBoolean(STATE_DOCUMENT_REDEFINE_ALLOWED);
    }

    public void writeToBundle(Bundle outState)
    {
        outState.putBoolean(STATE_DOCUMENT_REFORMAT, reformat);
        outState.putInt(STATE_DOCUMENT_TEXT_WIDTH, textWidth);
        outState.putInt(STATE_DOCUMENT_SIGNIFICANT_DIGITS, significantDigits);
        outState.putFloat(STATE_DOCUMENT_SCALE_FACTOR, scaledDimensions.getScaleFactor());
        outState.putBoolean(STATE_DOCUMENT_REDEFINE_ALLOWED, redefineAllowed);
    }

    public void readFromXml(XmlPullParser parser)
    {
        final DecimalFormat df = CompatUtils.getDecimalFormat("0.00000");
        String attr = parser.getAttributeValue(null, XML_PROP_TEXT_WIDTH);
        if (attr != null)
        {
            textWidth = Integer.parseInt(attr);
        }
        attr = parser.getAttributeValue(null, XML_PROP_SIGNIFICANT_DIGITS);
        if (attr != null)
        {
            significantDigits = Integer.parseInt(attr);
        }
        attr = parser.getAttributeValue(null, XML_PROP_SCALE);
        if (attr != null)
        {
            try
            {
                scaledDimensions.setScaleFactor(df.parse(attr).floatValue());
            }
            catch (ParseException e)
            {
                scaledDimensions.setScaleFactor(1.0f);
            }
        }
        attr = parser.getAttributeValue(null, XML_PROP_REDEFINE_ALLOWED);
        if (attr != null)
        {
            redefineAllowed = Boolean.parseBoolean(attr);
        }
    }

    public void writeToXml(XmlSerializer serializer) throws Exception
    {
        final DecimalFormat df = CompatUtils.getDecimalFormat("0.00000");
        serializer.attribute(FormulaList.XML_NS, XML_PROP_TEXT_WIDTH, String.valueOf(textWidth));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_SIGNIFICANT_DIGITS, String.valueOf(significantDigits));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_SCALE, df.format(scaledDimensions.getScaleFactor()));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_REDEFINE_ALLOWED, String.valueOf(redefineAllowed));
    }
}
