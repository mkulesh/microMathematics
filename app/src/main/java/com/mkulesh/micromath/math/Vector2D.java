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
package com.mkulesh.micromath.math;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class that implements a 2D vector
 */
public class Vector2D implements Parcelable
{
    /**
     * State attributes to be stored in Parcel
     */
    public double x = 0.0;
    public double y = 0.0;

    /**
     * Parcelable interface
     */
    private Vector2D(Parcel in)
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
        dest.writeDouble(x);
        dest.writeDouble(y);
    }

    public void readFromParcel(Parcel in)
    {
        x = in.readDouble();
        y = in.readDouble();
    }

    public static final Parcelable.Creator<Vector2D> CREATOR = new Parcelable.Creator<Vector2D>()
    {
        public Vector2D createFromParcel(Parcel in)
        {
            return new Vector2D(in);
        }

        public Vector2D[] newArray(int size)
        {
            return new Vector2D[size];
        }
    };

    /**
     * Default constructor
     */
    public Vector2D()
    {
        super();
    }

    /**
     * Initialization constructor
     */
    public Vector2D(double x, double y)
    {
        super();
        this.x = x;
        this.y = y;
    }

    /**
     * Getter procedure
     */
    public double get(int idx)
    {
        return idx == 0 ? x : y;
    }

    /**
     * Setter procedure
     */
    public void set(double x, double y)
    {
        this.x = x;
        this.y = y;
    }
}
