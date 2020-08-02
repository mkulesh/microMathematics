/*
 * Copyright (C) 2014-2018 Mikhail Kulesh
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details. You should have received a copy of the GNU General
 * Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.mkulesh.micromath.properties;

import android.os.Parcel;
import android.os.Parcelable;

import com.mkulesh.micromath.formula.FormulaList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class ResultProperties implements Parcelable
{
    public static final String XML_PROP_DISABLE_CALCULATION = "disableCalculation";
    public static final String XML_PROP_HIDE_RESULT_FIELD = "hideResultField";
    public static final String XML_PROP_ARRAY_LENGTH = "arrayLength";
    public static final String XML_PROP_UNITS = "units";

    /**
     * Class members.
     */
    public boolean disableCalculation = false;
    public boolean hideResultField = false;
    public int arrayLength = 7;
    public String units = "";

    /**
     * Temporary attributes that are not a part of state
     */
    public boolean showArrayLength = false;

    /**
     * Parcelable interface
     */
    public ResultProperties(Parcel in)
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
        dest.writeString(String.valueOf(disableCalculation));
        dest.writeString(String.valueOf(hideResultField));
        dest.writeInt(arrayLength);
        dest.writeString(units);
    }

    public void readFromParcel(Parcel in)
    {
        disableCalculation = Boolean.parseBoolean(in.readString());
        hideResultField = Boolean.parseBoolean(in.readString());
        arrayLength = in.readInt();
        units = in.readString();
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

    public void assign(ResultProperties a)
    {
        disableCalculation = a.disableCalculation;
        hideResultField = a.hideResultField;
        arrayLength = a.arrayLength;
        units = a.units;
    }

    public void readFromXml(XmlPullParser parser)
    {
        String attr = parser.getAttributeValue(null, XML_PROP_DISABLE_CALCULATION);
        if (attr != null)
        {
            disableCalculation = Boolean.parseBoolean(attr);
        }
        attr = parser.getAttributeValue(null, XML_PROP_HIDE_RESULT_FIELD);
        if (attr != null)
        {
            hideResultField = Boolean.parseBoolean(attr);
        }
        attr = parser.getAttributeValue(null, XML_PROP_ARRAY_LENGTH);
        if (attr != null)
        {
            arrayLength = Integer.parseInt(attr);
        }
        attr = parser.getAttributeValue(null, XML_PROP_UNITS);
        if (attr != null)
        {
            units = attr;
        }
    }

    public void writeToXml(XmlSerializer serializer) throws Exception
    {
        serializer.attribute(FormulaList.XML_NS, XML_PROP_DISABLE_CALCULATION, String.valueOf(disableCalculation));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_HIDE_RESULT_FIELD, String.valueOf(hideResultField));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_ARRAY_LENGTH, String.valueOf(arrayLength));
        if (!units.isEmpty())
        {
            serializer.attribute(FormulaList.XML_NS, XML_PROP_UNITS, units);
        }
    }
}
