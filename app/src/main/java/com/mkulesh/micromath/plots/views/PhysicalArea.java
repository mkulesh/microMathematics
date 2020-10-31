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
package com.mkulesh.micromath.plots.views;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

import com.mkulesh.micromath.math.Vector2D;

import androidx.annotation.NonNull;

public class PhysicalArea implements Parcelable
{

    private static final double NO_ZOOM_FACTOR = 1.0;

    /**
     * State attributes to be stored in Parcel
     */
    private Vector2D min = new Vector2D(-20.0, -20.0); // left-bottom corner of the physical area
    private Vector2D max = new Vector2D(20.0, 20.0); // right-tom corner of the physical area
    private Vector2D dim = new Vector2D(40.0, 40.0); // physical area dimensions
    private double zoom = NO_ZOOM_FACTOR; // current zoom

    /**
     * Parcelable interface
     */
    private PhysicalArea(Parcel in)
    {
        super();
        readFromParcel(in);
    }

    @Override
    public int describeContents()
    {
        // empty
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        min.writeToParcel(dest, flags);
        max.writeToParcel(dest, flags);
        dim.writeToParcel(dest, flags);
        dest.writeDouble(zoom);
    }

    private void readFromParcel(Parcel in)
    {
        min.readFromParcel(in);
        max.readFromParcel(in);
        dim.readFromParcel(in);
        zoom = in.readDouble();
    }

    public static final Parcelable.Creator<PhysicalArea> CREATOR = new Parcelable.Creator<PhysicalArea>()
    {
        public PhysicalArea createFromParcel(Parcel in)
        {
            return new PhysicalArea(in);
        }

        public PhysicalArea[] newArray(int size)
        {
            return new PhysicalArea[size];
        }
    };

    /**
     * Default constructor
     */
    public PhysicalArea()
    {
        super();
    }

    /**
     * Procedure selects the area for given min/max values
     */
    public void set(double minX, double maxX, double minY, double maxY)
    {
        min = new Vector2D(minX, minY);
        max = new Vector2D(maxX, maxY);
        dim = new Vector2D(maxX - minX, maxY - minY);
    }

    /**
     * Procedure returns left-bottom corner of the physical area
     */
    public Vector2D getMin()
    {
        return min;
    }

    /**
     * Procedure returns right-tom corner of the physical area
     */
    public Vector2D getMax()
    {
        return max;
    }

    /**
     * Procedure returns physical area dimensions
     */
    public Vector2D getDim()
    {
        return dim;
    }

    /**
     * Procedure checks that the given vector is inside of this area
     */
    public boolean isInside(Vector2D p)
    {
        return (p.x >= min.x && p.x <= max.x && p.y >= min.y && p.y <= max.y);
    }

    /**
     * Procedure returns the string representation of this area
     */
    @NonNull
    public String toString()
    {
        return "min [" + min.x + "," + min.y + "] max [" + max.x + "," + max.y + "] dim [" + dim.x + "," + dim.y + "]";
    }

    /**
     * Converts physical coordinate to screen point
     */
    public void toScreenPoint(Vector2D fp, Rect r, Point sp)
    {
        sp.x = r.left + (int) ((double) r.width() * (fp.x - min.x) / dim.x);
        sp.y = r.bottom - (int) ((double) r.height() * (fp.y - min.y) / dim.y);
    }
}
