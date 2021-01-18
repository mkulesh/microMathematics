/*
 * microMathematics - Extended Visual Calculator
 * Copyright (C) 2014-2021 by Mikhail Kulesh
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details. You should have received a copy of the GNU General
 * Public License along with this program.
 */
package com.mkulesh.micromath.properties;

import android.os.Parcel;
import android.os.Parcelable;

import com.mkulesh.micromath.formula.FormulaList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.util.Locale;

public class ColorMapProperties implements Parcelable
{
    private static final String XML_PROP_ZLABELSNUMBER = "zLabelsNumber";
    private static final String XML_PROP_COLORMAP = "colorMap";

    public enum ColorMap
    {
        COOL,
        FIRE,
        COLDHOT,
        RAINBOW,
        EARTHSKY,
        GREENBLUE,
        GRAYSCALE
    }

    public int zLabelsNumber = 10;
    public ColorMap colorMap = ColorMap.COOL;

    /**
     * Parcelable interface
     */
    private ColorMapProperties(Parcel in)
    {
        super();
        readFromParcel(in);
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(colorMap.toString());
        dest.writeInt(zLabelsNumber);
    }

    private void readFromParcel(Parcel in)
    {
        colorMap = ColorMap.valueOf(in.readString());
        zLabelsNumber = in.readInt();
    }

    public static final Parcelable.Creator<ColorMapProperties> CREATOR = new Parcelable.Creator<ColorMapProperties>()
    {
        public ColorMapProperties createFromParcel(Parcel in)
        {
            return new ColorMapProperties(in);
        }

        public ColorMapProperties[] newArray(int size)
        {
            return new ColorMapProperties[size];
        }
    };

    /**
     * Default constructor
     */
    public ColorMapProperties()
    {
        // empty
    }

    public void assign(ColorMapProperties a)
    {
        zLabelsNumber = a.zLabelsNumber;
        colorMap = a.colorMap;
    }

    public void readFromXml(XmlPullParser parser)
    {
        String attr = parser.getAttributeValue(null, XML_PROP_ZLABELSNUMBER);
        if (attr != null)
        {
            zLabelsNumber = Integer.parseInt(attr);
        }
        attr = parser.getAttributeValue(null, XML_PROP_COLORMAP);
        if (attr != null)
        {
            try
            {
                colorMap = ColorMap.valueOf(attr.toUpperCase(Locale.ENGLISH));
            }
            catch (Exception e)
            {
                // nothing to do
            }
        }
    }

    public void writeToXml(XmlSerializer serializer) throws Exception
    {
        serializer.attribute(FormulaList.XML_NS, XML_PROP_ZLABELSNUMBER, String.valueOf(zLabelsNumber));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_COLORMAP, colorMap.toString().toLowerCase(Locale.ENGLISH));
    }
}
