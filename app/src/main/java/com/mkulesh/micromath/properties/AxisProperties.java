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

import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import com.mkulesh.micromath.formula.FormulaList;
import com.mkulesh.micromath.plus.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class AxisProperties implements Parcelable
{
    public static final String XML_PROP_XLABELSNUMBER = "xLabelsNumber";
    public static final String XML_PROP_YLABELSNUMBER = "yLabelsNumber";
    public static final String XML_PROP_GRIDLINECOLOR = "gridLineColor";

    public float scaleFactor = 1;
    public int gridLineColor = Color.GRAY;
    public int xLabelsNumber = 3;
    public int yLabelsNumber = 3;

    private int labelLineSize = 5;
    private int labelTextSize = 20;
    private int gridLineWidth = 1;

    /**
     * Parcelable interface
     */
    public AxisProperties(Parcel in)
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
    }

    public void readFromParcel(Parcel in)
    {
        labelLineSize = in.readInt();
        labelTextSize = in.readInt();
        gridLineColor = in.readInt();
        gridLineWidth = in.readInt();
        xLabelsNumber = in.readInt();
        yLabelsNumber = in.readInt();
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
    }

    public void initialize(TypedArray a)
    {
        labelLineSize = a.getDimensionPixelSize(R.styleable.PlotViewExtension_labelLineSize, labelLineSize);
        labelTextSize = a.getDimensionPixelSize(R.styleable.PlotViewExtension_labelTextSize, labelTextSize);
        gridLineColor = a.getColor(R.styleable.PlotViewExtension_gridLineColor, gridLineColor);
        gridLineWidth = a.getDimensionPixelSize(R.styleable.PlotViewExtension_gridLineWidth, gridLineWidth);
        xLabelsNumber = a.getInt(R.styleable.PlotViewExtension_xLabelsNumber, xLabelsNumber);
        yLabelsNumber = a.getInt(R.styleable.PlotViewExtension_yLabelsNumber, yLabelsNumber);
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
    }

    public void writeToXml(XmlSerializer serializer) throws Exception
    {
        serializer.attribute(FormulaList.XML_NS, XML_PROP_XLABELSNUMBER, String.valueOf(xLabelsNumber));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_YLABELSNUMBER, String.valueOf(yLabelsNumber));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_GRIDLINECOLOR, String.format("#%08X", gridLineColor));
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
