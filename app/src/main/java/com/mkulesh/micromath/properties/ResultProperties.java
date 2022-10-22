/*
 * microMathematics - Extended Visual Calculator
 * Copyright (C) 2014-2022 by Mikhail Kulesh
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

import androidx.annotation.Nullable;

import com.mkulesh.micromath.formula.FormulaList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.util.Locale;

public class ResultProperties implements Parcelable
{
    public enum ResultFieldType
    {
        HIDE,
        SKIP,
        REAL,
        FRACTION
    }

    private static final String XML_PROP_RESULT_FIELD_TYPE = "resultFieldType";
    private static final String XML_PROP_ARRAY_LENGTH = "arrayLength";
    private static final String XML_PROP_RADIX = "radix";

    /**
     * Class members.
     */
    public ResultFieldType resultFieldType = ResultFieldType.REAL;
    public int arrayLength = 7;
    public int radix = 10;

    /**
     * Temporary attributes that are not a part of state
     */
    public boolean showArrayLength = false;

    /**
     * Parcelable interface
     */
    private ResultProperties(Parcel in)
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
        dest.writeInt(resultFieldType.ordinal());
        dest.writeInt(arrayLength);
        dest.writeInt(radix);
    }

    private void readFromParcel(Parcel in)
    {
        resultFieldType = ResultFieldType.values()[in.readInt()];
        arrayLength = in.readInt();
        radix = in.readInt();
    }

    public static final Parcelable.Creator<ResultProperties> CREATOR = new Parcelable.Creator<ResultProperties>()
    {
        @Override
        public ResultProperties createFromParcel(Parcel in)
        {
            return new ResultProperties(in);
        }

        @Override
        public ResultProperties[] newArray(int size)
        {
            return new ResultProperties[size];
        }
    };

    /**
     * Default constructor
     */
    public ResultProperties()
    {
        // empty
    }

    public void assign(@Nullable ResultProperties a)
    {
        if (a != null)
        {
            resultFieldType = a.resultFieldType;
            arrayLength = a.arrayLength;
            radix = a.radix;
        }
    }

    public void readFromXml(XmlPullParser parser)
    {
        // Back compatibility to the previous boolean format of this field
        String attr = parser.getAttributeValue(null, "hideResultField");
        if (attr != null && Boolean.parseBoolean(attr))
        {
            resultFieldType = ResultFieldType.HIDE;
        }
        attr = parser.getAttributeValue(null, "disableCalculation");
        if (attr != null && Boolean.parseBoolean(attr))
        {
            resultFieldType = ResultFieldType.SKIP;
        }
        attr = parser.getAttributeValue(null, XML_PROP_RESULT_FIELD_TYPE);
        if (attr != null)
        {
            try
            {
                resultFieldType = ResultFieldType.valueOf(attr.toUpperCase(Locale.ENGLISH));
            }
            catch (Exception e)
            {
                // Nothing to do
            }
        }
        attr = parser.getAttributeValue(null, XML_PROP_ARRAY_LENGTH);
        if (attr != null)
        {
            arrayLength = Integer.parseInt(attr);
        }
        attr = parser.getAttributeValue(null, XML_PROP_RADIX);
        if (attr != null)
        {
            radix = Integer.parseInt(attr);
        }
    }

    public void writeToXml(XmlSerializer serializer) throws Exception
    {
        serializer.attribute(FormulaList.XML_NS, XML_PROP_RESULT_FIELD_TYPE,
                resultFieldType.toString().toLowerCase(Locale.ENGLISH));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_ARRAY_LENGTH, String.valueOf(arrayLength));
        if (radix != 10)
        {
            serializer.attribute(FormulaList.XML_NS, XML_PROP_RADIX, String.valueOf(radix));
        }
    }
}
