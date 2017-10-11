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
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;

import com.mkulesh.micromath.R;
import com.mkulesh.micromath.formula.FormulaList;
import com.mkulesh.micromath.utils.ViewUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.util.Locale;

public class PlotProperties implements Parcelable
{
    public static final String XML_PROP_WIDTH = "width";
    public static final String XML_PROP_HEIGHT = "height";
    public static final String XML_PROP_AXES_STYLE = "axes_style";

    public enum AxesStyle
    {
        BOXED,
        CROSSED,
        NONE
    }

    private DisplayMetrics displayMetrics = null;
    public int width = 300;
    public int height = 300;
    public AxesStyle axesStyle = AxesStyle.BOXED;

    /**
     * Parcelable interface
     */
    public PlotProperties(Parcel in)
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
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeString(axesStyle.toString());
    }

    public void readFromParcel(Parcel in)
    {
        width = in.readInt();
        height = in.readInt();
        axesStyle = AxesStyle.valueOf(in.readString());
    }

    public static final Parcelable.Creator<PlotProperties> CREATOR = new Parcelable.Creator<PlotProperties>()
    {
        public PlotProperties createFromParcel(Parcel in)
        {
            return new PlotProperties(in);
        }

        public PlotProperties[] newArray(int size)
        {
            return new PlotProperties[size];
        }
    };

    /**
     * Default constructor
     */
    public PlotProperties()
    {
        // empty
    }

    public void assign(PlotProperties a)
    {
        width = a.width;
        height = a.height;
        axesStyle = a.axesStyle;
    }

    public void initialize(Context context)
    {
        // Auto-setup of plot size depending on display size
        displayMetrics = context.getResources().getDisplayMetrics();
        final int plotSize = Math.min(displayMetrics.heightPixels, displayMetrics.widthPixels) - 2
                * context.getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin);
        width = plotSize;
        height = plotSize;
    }

    public void readFromXml(XmlPullParser parser)
    {
        String attr = parser.getAttributeValue(null, XML_PROP_WIDTH);
        if (attr != null)
        {
            width = ViewUtils.dpToPx(displayMetrics, Integer.parseInt(attr));
        }
        attr = parser.getAttributeValue(null, XML_PROP_HEIGHT);
        if (attr != null)
        {
            height = ViewUtils.dpToPx(displayMetrics, Integer.parseInt(attr));
        }
        attr = parser.getAttributeValue(null, XML_PROP_AXES_STYLE);
        if (attr != null)
        {
            try
            {
                axesStyle = AxesStyle.valueOf(attr.toUpperCase(Locale.ENGLISH));
            }
            catch (Exception e)
            {
                // nothing to do
            }
        }
    }

    public void writeToXml(XmlSerializer serializer) throws Exception
    {
        serializer.attribute(FormulaList.XML_NS, XML_PROP_WIDTH,
                String.valueOf(ViewUtils.pxToDp(displayMetrics, width)));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_HEIGHT,
                String.valueOf(ViewUtils.pxToDp(displayMetrics, height)));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_AXES_STYLE, axesStyle.toString().toLowerCase(Locale.ENGLISH));
    }

    public boolean isCrossedAxes()
    {
        return axesStyle == AxesStyle.CROSSED;
    }

    public boolean isNoAxes()
    {
        return axesStyle == AxesStyle.NONE;
    }
}
