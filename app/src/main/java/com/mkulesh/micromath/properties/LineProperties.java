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

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Parcel;
import android.os.Parcelable;

import com.mkulesh.micromath.formula.FormulaList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.util.Locale;

public class LineProperties implements Parcelable
{
    public static final String XML_PROP_COLOR = "color";
    public static final String XML_PROP_WIDTH = "width";
    public static final String XML_PROP_LINESTYLE = "lineStyle";
    public static final String XML_PROP_SHAPETYPE = "shapeType";
    public static final String XML_PROP_SHAPESIZE = "shapeSize";

    public enum LineStyle
    {
        SOLID,
        DOTTED,
        DASHED,
        DASH_DOT
    }

    public enum ShapeType
    {
        NONE,
        SQUARE,
        CIRCLE,
        DIAMOND,
        CROSS
    }

    public float scaleFactor = 1;
    public int color = android.graphics.Color.BLUE;
    public int width = 3;
    public LineStyle lineStyle = LineStyle.SOLID;
    public ShapeType shapeType = ShapeType.NONE;
    public int shapeSize = 300;

    private Paint paint = new Paint();

    /**
     * Parcelable interface
     */
    public LineProperties(Parcel in)
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
        dest.writeInt(color);
        dest.writeInt(width);
        dest.writeString(lineStyle.toString());
        dest.writeString(shapeType.toString());
        dest.writeInt(shapeSize);
    }

    public void readFromParcel(Parcel in)
    {
        color = in.readInt();
        width = in.readInt();
        lineStyle = LineStyle.valueOf(in.readString());
        shapeType = ShapeType.valueOf(in.readString());
        shapeSize = in.readInt();
    }

    public static final Parcelable.Creator<LineProperties> CREATOR = new Parcelable.Creator<LineProperties>()
    {
        public LineProperties createFromParcel(Parcel in)
        {
            return new LineProperties(in);
        }

        public LineProperties[] newArray(int size)
        {
            return new LineProperties[size];
        }
    };

    /**
     * Default constructor
     */
    public LineProperties()
    {
        // empty
    }

    public void assign(LineProperties a)
    {
        scaleFactor = a.scaleFactor;
        color = a.color;
        width = a.width;
        lineStyle = a.lineStyle;
        shapeType = a.shapeType;
        shapeSize = a.shapeSize;
        preparePaint();
    }

    public void readFromXml(XmlPullParser parser)
    {
        String attr = parser.getAttributeValue(null, XML_PROP_COLOR);
        if (attr != null)
        {
            color = Color.parseColor(attr);
        }
        attr = parser.getAttributeValue(null, XML_PROP_WIDTH);
        if (attr != null)
        {
            width = Integer.parseInt(attr);
        }
        attr = parser.getAttributeValue(null, XML_PROP_LINESTYLE);
        if (attr != null)
        {
            try
            {
                lineStyle = LineStyle.valueOf(attr.toUpperCase(Locale.ENGLISH));
            }
            catch (Exception e)
            {
                // nothing to do
            }
        }
        attr = parser.getAttributeValue(null, XML_PROP_SHAPETYPE);
        if (attr != null)
        {
            try
            {
                shapeType = ShapeType.valueOf(attr.toUpperCase(Locale.ENGLISH));
            }
            catch (Exception e)
            {
                // nothing to do
            }
        }
        attr = parser.getAttributeValue(null, XML_PROP_SHAPESIZE);
        if (attr != null)
        {
            shapeSize = Integer.parseInt(attr);
        }
    }

    public void writeToXml(XmlSerializer serializer) throws Exception
    {
        serializer.attribute(FormulaList.XML_NS, XML_PROP_COLOR, String.format("#%08X", color));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_WIDTH, String.valueOf(width));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_LINESTYLE, lineStyle.toString().toLowerCase(Locale.ENGLISH));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_SHAPETYPE, shapeType.toString().toLowerCase(Locale.ENGLISH));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_SHAPESIZE, String.valueOf(shapeSize));
    }

    /**
     * Procedure prepares line paint object depending on current settings
     */
    public void preparePaint()
    {
        int w = Math.round(width * scaleFactor);
        if (w == 0)
        {
            w = 1;
        }
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(color);
        paint.setStrokeWidth(w);
        paint.setAntiAlias(true);
        switch (lineStyle)
        {
        case SOLID:
            paint.setPathEffect(null);
            break;
        case DOTTED:
            paint.setPathEffect(new DashPathEffect(new float[]{ 1.5f * w, 1.5f * w }, 0));
            break;
        case DASHED:
            paint.setPathEffect(new DashPathEffect(new float[]{ 6 * w, 6 * w }, 0));
            break;
        case DASH_DOT:
            paint.setPathEffect(new DashPathEffect(new float[]{ 6 * w, 2f * w, 1.5f * w, 2f * w }, 0));
            break;
        }
    }

    /**
     * Procedure returns line paint object
     */
    public Paint getPaint()
    {
        return paint;
    }

    public void setNextDefault(LineProperties lineParameters)
    {
        switch (lineParameters.lineStyle)
        {
        case SOLID:
            lineStyle = LineStyle.DASHED;
            break;
        case DASHED:
            lineStyle = LineStyle.DOTTED;
            break;
        case DOTTED:
            lineStyle = LineStyle.DASH_DOT;
            break;
        case DASH_DOT:
            lineStyle = LineStyle.SOLID;
        }

        final float[] hsv = new float[3];
        Color.colorToHSV(lineParameters.color, hsv);
        hsv[0] += 90;
        if (hsv[0] >= 360)
        {
            hsv[0] -= 360;
        }
        if (hsv[0] > 0 && hsv[0] < 180)
        {
            hsv[2] = Math.min(hsv[2], 0.6f);
        }
        color = Color.HSVToColor(Color.alpha(lineParameters.color), hsv);
    }
}
