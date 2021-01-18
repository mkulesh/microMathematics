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

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;

import com.mkulesh.micromath.fman.FileUtils;
import com.mkulesh.micromath.formula.FormulaList;
import com.mkulesh.micromath.utils.ViewUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.util.Locale;

public class ImageProperties implements Parcelable
{
    private static final int DEFAULT_SIZE = 100;
    private static final String EMPTY_FILE_NAME = "";
    private static final String XML_PROP_FILE_NAME = "fileName";
    private static final String XML_PROP_EMBEDDED = "embedded";
    private static final String XML_PROP_ORIGINAL_SIZE = "originalSize";
    private static final String XML_PROP_WIDTH = "width";
    private static final String XML_PROP_HEIGHT = "height";
    private static final String XML_PROP_COLORTYPE = "colorType";

    // attributes that are not stored within the state and XML
    private DisplayMetrics displayMetrics = null;

    public enum ColorType
    {
        ORIGINAL,
        AUTO
    }

    // state- and XML-related attributes
    public String fileName = EMPTY_FILE_NAME;
    public boolean embedded = false;
    public boolean originalSize = true;
    public int width = DEFAULT_SIZE;
    public int height = DEFAULT_SIZE;
    public ColorType colorType = ColorType.ORIGINAL;

    // temporary properties
    public Uri parentDirectory = null;

    /**
     * Parcelable interface
     */
    private ImageProperties(Parcel in)
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
        dest.writeString(fileName);
        dest.writeString(String.valueOf(embedded));
        dest.writeString(String.valueOf(originalSize));
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeString(colorType.toString());
    }

    private void readFromParcel(Parcel in)
    {
        fileName = in.readString();
        embedded = Boolean.parseBoolean(in.readString());
        originalSize = Boolean.parseBoolean(in.readString());
        width = in.readInt();
        height = in.readInt();
        colorType = ColorType.valueOf(in.readString());
    }

    public static final Parcelable.Creator<ImageProperties> CREATOR = new Parcelable.Creator<ImageProperties>()
    {
        @Override
        public ImageProperties createFromParcel(Parcel in)
        {
            return new ImageProperties(in);
        }

        @Override
        public ImageProperties[] newArray(int size)
        {
            return new ImageProperties[size];
        }
    };

    /**
     * Default constructor
     */
    public ImageProperties()
    {
        // empty
    }

    public void assign(ImageProperties a)
    {
        fileName = a.fileName;
        embedded = a.embedded;
        originalSize = a.originalSize;
        width = a.width;
        height = a.height;
        colorType = a.colorType;
    }

    public void initialize(Context context)
    {
        displayMetrics = context.getResources().getDisplayMetrics();
    }

    public void readFromXml(XmlPullParser parser)
    {
        String attr = parser.getAttributeValue(null, XML_PROP_FILE_NAME);
        fileName = (attr == null) ? EMPTY_FILE_NAME : attr;
        attr = parser.getAttributeValue(null, XML_PROP_EMBEDDED);
        if (attr != null)
        {
            embedded = Boolean.parseBoolean(attr);
        }
        attr = parser.getAttributeValue(null, XML_PROP_ORIGINAL_SIZE);
        if (attr != null)
        {
            originalSize = Boolean.parseBoolean(attr);
        }
        attr = parser.getAttributeValue(null, XML_PROP_WIDTH);
        if (attr != null)
        {
            width = ViewUtils.dpToPx(displayMetrics, Integer.parseInt(attr));
        }
        attr = parser.getAttributeValue(null, XML_PROP_HEIGHT);
        if (attr != null)
        {
            height = ViewUtils.dpToPx(displayMetrics, Integer.parseInt(attr));
        }
        attr = parser.getAttributeValue(null, XML_PROP_COLORTYPE);
        if (attr != null)
        {
            try
            {
                colorType = ColorType.valueOf(attr.toUpperCase(Locale.ENGLISH));
            }
            catch (Exception e)
            {
                // nothing to do
            }
        }
    }

    public void writeToXml(XmlSerializer serializer) throws Exception
    {
        serializer.attribute(FormulaList.XML_NS, XML_PROP_FILE_NAME, fileName);
        serializer.attribute(FormulaList.XML_NS, XML_PROP_EMBEDDED, String.valueOf(embedded));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_ORIGINAL_SIZE, String.valueOf(originalSize));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_WIDTH,
                String.valueOf(ViewUtils.pxToDp(displayMetrics, width)));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_HEIGHT,
                String.valueOf(ViewUtils.pxToDp(displayMetrics, height)));
        serializer.attribute(FormulaList.XML_NS, XML_PROP_COLORTYPE,
                colorType.toString().toLowerCase(Locale.ENGLISH));
    }

    public boolean isAsset()
    {
        return fileName.contains(FileUtils.ASSET_RESOURCE_PREFIX);
    }
}
