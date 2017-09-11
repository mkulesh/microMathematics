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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;

import com.mkulesh.micromath.fman.FileUtils;
import com.mkulesh.micromath.formula.FormulaList;
import com.mkulesh.micromath.utils.ViewUtils;

public class ImageProperties implements Parcelable
{
    public static final int DEFAULT_SIZE = 100;
    public static final String EMPTY_FILE_NAME = "";
    public static final String XML_PROP_FILE_NAME = "fileName";
    public static final String XML_PROP_EMBEDDED = "embedded";
    public static final String XML_PROP_ORIGINAL_SIZE = "originalSize";
    public static final String XML_PROP_WIDTH = "width";
    public static final String XML_PROP_HEIGHT = "height";

    // attributes that are not stored within the state and XML
    private DisplayMetrics displayMetrics = null;

    // state- and XML-related attributes
    public String fileName = EMPTY_FILE_NAME;
    public boolean embedded = true;
    public boolean originalSize = true;
    public int width = DEFAULT_SIZE;
    public int height = DEFAULT_SIZE;

    // temporary properties
    public Uri parentDirectory = null;

    /**
     * Parcelable interface
     */
    public ImageProperties(Parcel in)
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
    }

    public void readFromParcel(Parcel in)
    {
        fileName = in.readString();
        embedded = Boolean.valueOf(in.readString());
        originalSize = Boolean.valueOf(in.readString());
        width = in.readInt();
        height = in.readInt();
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
            embedded = Boolean.valueOf(attr);
        }
        attr = parser.getAttributeValue(null, XML_PROP_ORIGINAL_SIZE);
        if (attr != null)
        {
            originalSize = Boolean.valueOf(attr);
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
    }

    public boolean isAsset()
    {
        return fileName.contains(FileUtils.ASSET_RESOURCE_PREFIX);
    }
}
