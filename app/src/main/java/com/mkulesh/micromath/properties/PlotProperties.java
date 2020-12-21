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

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;

import com.mkulesh.micromath.formula.FormulaList;
import com.mkulesh.micromath.R;
import com.mkulesh.micromath.utils.ViewUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.util.Locale;

public class PlotProperties implements Parcelable
{
    private static final String XML_PROP_WIDTH = "width";
    private static final String XML_PROP_HEIGHT = "height";
    private static final String XML_PROP_AXES_STYLE = "axes_style";
    private static final String XML_PROP_MESH_LINES = "meshLines";
    private static final String XML_PROP_MESH_FILL = "meshFill";
    private static final String XML_PROP_MESH_OPACITY = "meshOpacity";
    private static final String XML_PROP_ROTATION = "rotation";
    private static final String XML_PROP_ELEVATION = "elevation";

    public enum AxesStyle
    {
        BOXED,
        CROSSED,
        NONE
    }

    public enum TwoDPlotStyle
    {
        CONTOUR,
        SURFACE
    }

    // attributes that are not stored within the state and XML
    private DisplayMetrics displayMetrics = null;
    public TwoDPlotStyle twoDPlotStyle = TwoDPlotStyle.CONTOUR;

    // state- and XML-related attributes
    public int width = 300;
    public int height = 300;
    public AxesStyle axesStyle = AxesStyle.BOXED;

    // state- and XML-related attributes for surface plot
    public boolean meshLines = false;
    public boolean meshFill = false;
    public int meshOpacity = 150;
    public int rotation = 35;
    public int elevation = 20;

    /**
     * Parcelable interface
     */
    private PlotProperties(Parcel in)
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
        dest.writeString(String.valueOf(meshLines));
        dest.writeString(String.valueOf(meshFill));
        dest.writeInt(meshOpacity);
        dest.writeInt(rotation);
        dest.writeInt(elevation);
    }

    private void readFromParcel(Parcel in)
    {
        width = in.readInt();
        height = in.readInt();
        axesStyle = AxesStyle.valueOf(in.readString());
        meshLines = Boolean.parseBoolean(in.readString());
        meshFill = Boolean.parseBoolean(in.readString());
        meshOpacity = in.readInt();
        rotation = in.readInt();
        elevation = in.readInt();
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
        meshLines = a.meshLines;
        meshFill = a.meshFill;
        meshOpacity = a.meshOpacity;
        rotation = a.rotation;
        elevation = a.elevation;
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
        attr = parser.getAttributeValue(null, XML_PROP_MESH_LINES);
        if (attr != null)
        {
            meshLines = Boolean.parseBoolean(attr);
        }
        attr = parser.getAttributeValue(null, XML_PROP_MESH_FILL);
        if (attr != null)
        {
            meshFill = Boolean.parseBoolean(attr);
        }
        attr = parser.getAttributeValue(null, XML_PROP_MESH_OPACITY);
        if (attr != null)
        {
            meshOpacity = Integer.parseInt(attr);
        }
        attr = parser.getAttributeValue(null, XML_PROP_ROTATION);
        if (attr != null)
        {
            rotation = Integer.parseInt(attr);
        }
        attr = parser.getAttributeValue(null, XML_PROP_ELEVATION);
        if (attr != null)
        {
            elevation = Integer.parseInt(attr);
        }
    }

    public void writeToXml(XmlSerializer serializer) throws Exception
    {
        serializer.attribute(FormulaList.XML_NS, XML_PROP_WIDTH,
                String.valueOf(ViewUtils.pxToDp(displayMetrics, width)));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_HEIGHT,
                String.valueOf(ViewUtils.pxToDp(displayMetrics, height)));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_AXES_STYLE, axesStyle.toString().toLowerCase(Locale.ENGLISH));
        if (twoDPlotStyle == TwoDPlotStyle.SURFACE)
        {
            serializer.attribute(FormulaList.XML_NS, XML_PROP_MESH_LINES, String.valueOf(meshLines));
            serializer.attribute(FormulaList.XML_NS, XML_PROP_MESH_FILL, String.valueOf(meshFill));
            serializer.attribute(FormulaList.XML_NS, XML_PROP_MESH_OPACITY, String.valueOf(meshOpacity));
            serializer.attribute(FormulaList.XML_NS, XML_PROP_ROTATION, String.valueOf(rotation));
            serializer.attribute(FormulaList.XML_NS, XML_PROP_ELEVATION, String.valueOf(elevation));
        }
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
