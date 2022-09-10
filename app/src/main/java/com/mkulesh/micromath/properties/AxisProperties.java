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

import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import com.mkulesh.micromath.formula.FormulaList;
import com.mkulesh.micromath.math.AxisTypeConverter;
import com.mkulesh.micromath.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.util.Locale;

public class AxisProperties implements Parcelable
{
    private static final String XML_PROP_XLABELSNUMBER = "xLabelsNumber";
    private static final String XML_PROP_YLABELSNUMBER = "yLabelsNumber";
    private static final String XML_PROP_GRIDLINECOLOR = "gridLineColor";
    private static final String XML_PROP_XTYPE = "xType";
    private static final String XML_PROP_YTYPE = "yType";

    public float scaleFactor = 1;
    public int gridLineColor = Color.GRAY;
    public int xLabelsNumber = 3;
    public int yLabelsNumber = 3;

    public AxisTypeConverter.Type xType = AxisTypeConverter.Type.LINEAR;
    public AxisTypeConverter.Type yType = AxisTypeConverter.Type.LINEAR;

    private int labelLineSize = 5;
    private int labelTextSize = 20;
    private int gridLineWidth = 1;

    /**
     * Parcelable interface
     */
    private AxisProperties(Parcel in)
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
        dest.writeInt(labelLineSize);
        dest.writeInt(labelTextSize);
        dest.writeInt(gridLineColor);
        dest.writeInt(gridLineWidth);
        dest.writeInt(xLabelsNumber);
        dest.writeInt(yLabelsNumber);
        dest.writeInt(xType.ordinal());
        dest.writeInt(yType.ordinal());
    }

    private void readFromParcel(Parcel in)
    {
        labelLineSize = in.readInt();
        labelTextSize = in.readInt();
        gridLineColor = in.readInt();
        gridLineWidth = in.readInt();
        xLabelsNumber = in.readInt();
        yLabelsNumber = in.readInt();
        xType = AxisTypeConverter.Type.values()[in.readInt()];
        yType = AxisTypeConverter.Type.values()[in.readInt()];
    }

    public static final Parcelable.Creator<AxisProperties> CREATOR = new Parcelable.Creator<AxisProperties>()
    {
        public AxisProperties createFromParcel(Parcel in)
        {
            return new AxisProperties(in);
        }

        public AxisProperties[] newArray(int size)
        {
            return new AxisProperties[size];
        }
    };

    /**
     * Default constructor
     */
    public AxisProperties()
    {
        // empty
    }

    public void assign(AxisProperties a)
    {
        scaleFactor = a.scaleFactor;
        gridLineColor = a.gridLineColor;
        xLabelsNumber = a.xLabelsNumber;
        yLabelsNumber = a.yLabelsNumber;
        labelLineSize = a.labelLineSize;
        labelTextSize = a.labelTextSize;
        gridLineWidth = a.gridLineWidth;
        xType = a.xType;
        yType = a.yType;
    }

    public void initialize(TypedArray a)
    {
        labelLineSize = a.getDimensionPixelSize(R.styleable.PlotViewExtension_labelLineSize, labelLineSize);
        labelTextSize = a.getDimensionPixelSize(R.styleable.PlotViewExtension_labelTextSize, labelTextSize);
        gridLineColor = a.getColor(R.styleable.PlotViewExtension_gridLineColor, gridLineColor);
        gridLineWidth = a.getDimensionPixelSize(R.styleable.PlotViewExtension_gridLineWidth, gridLineWidth);
        xLabelsNumber = a.getInt(R.styleable.PlotViewExtension_xLabelsNumber, xLabelsNumber);
        yLabelsNumber = a.getInt(R.styleable.PlotViewExtension_yLabelsNumber, yLabelsNumber);
        xType = AxisTypeConverter.Type.LINEAR;
        yType = AxisTypeConverter.Type.LINEAR;
    }

    public void readFromXml(XmlPullParser parser)
    {
        String attr = parser.getAttributeValue(null, XML_PROP_XLABELSNUMBER);
        if (attr != null)
        {
            xLabelsNumber = Integer.parseInt(attr);
        }
        attr = parser.getAttributeValue(null, XML_PROP_YLABELSNUMBER);
        if (attr != null)
        {
            yLabelsNumber = Integer.parseInt(attr);
        }
        attr = parser.getAttributeValue(null, XML_PROP_GRIDLINECOLOR);
        if (attr != null)
        {
            gridLineColor = Color.parseColor(attr);
        }
        attr = parser.getAttributeValue(null, XML_PROP_XTYPE);
        if (attr != null)
        {
            try
            {
                xType = AxisTypeConverter.Type.valueOf(attr.toUpperCase(Locale.ENGLISH));
            }
            catch (Exception e)
            {
                // nothing to do
            }
        }
        attr = parser.getAttributeValue(null, XML_PROP_YTYPE);
        if (attr != null)
        {
            try
            {
                yType = AxisTypeConverter.Type.valueOf(attr.toUpperCase(Locale.ENGLISH));
            }
            catch (Exception e)
            {
                // nothing to do
            }
        }
    }

    public void writeToXml(XmlSerializer serializer) throws Exception
    {
        serializer.attribute(FormulaList.XML_NS, XML_PROP_XLABELSNUMBER, String.valueOf(xLabelsNumber));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_YLABELSNUMBER, String.valueOf(yLabelsNumber));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_GRIDLINECOLOR, String.format("#%08X", gridLineColor));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_XTYPE, xType.toString().toLowerCase(Locale.ENGLISH));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_YTYPE, yType.toString().toLowerCase(Locale.ENGLISH));
    }

    public int getLabelLineSize()
    {
        return Math.round(labelLineSize * scaleFactor);
    }

    public int getLabelTextSize()
    {
        return Math.round(labelTextSize * scaleFactor);
    }

    public int getGridLineWidth()
    {
        return Math.round(gridLineWidth * scaleFactor);
    }
}
