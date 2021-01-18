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

public class MatrixProperties implements Parcelable
{
    private static final String XML_PROP_ROWS = "rows";
    private static final String XML_PROP_COLS = "cols";

    public int rows = 0;
    public int cols = 0;

    /**
     * Parcelable interface
     */
    public MatrixProperties(Parcel in)
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
        dest.writeInt(rows);
        dest.writeInt(cols);
    }

    private void readFromParcel(Parcel in)
    {
        rows = in.readInt();
        cols = in.readInt();
    }

    public static final Creator<MatrixProperties> CREATOR = new Creator<MatrixProperties>()
    {
        public MatrixProperties createFromParcel(Parcel in)
        {
            return new MatrixProperties(in);
        }

        public MatrixProperties[] newArray(int size)
        {
            return new MatrixProperties[size];
        }
    };

    /**
     * Default constructor
     */
    public MatrixProperties()
    {
        // empty
    }

    public void assign(MatrixProperties a)
    {
        rows = a.rows;
        cols = a.cols;
    }

    public void readFromXml(XmlPullParser parser)
    {
        String attr = parser.getAttributeValue(null, XML_PROP_ROWS);
        if (attr != null)
        {
            rows = Integer.parseInt(attr);
        }
        attr = parser.getAttributeValue(null, XML_PROP_COLS);
        if (attr != null)
        {
            cols = Integer.parseInt(attr);
        }
    }

    public void writeToXml(XmlSerializer serializer) throws Exception
    {
        serializer.attribute(FormulaList.XML_NS, XML_PROP_ROWS, String.valueOf(rows));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_COLS, String.valueOf(cols));
    }

    public int getDimension()
    {
        return (rows == 1 || cols == 1) ? 1 : 2;
    }
}
