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
package com.mkulesh.micromath.plots.views;

import android.graphics.Point;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;

import com.mkulesh.micromath.math.Vector2D;

public class PhysicalArea implements Parcelable
{

    public static final double NO_ZOOM_FACTOR = 1.0;

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
    public PhysicalArea(Parcel in)
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
     * Copy constructor
     */
    public PhysicalArea(PhysicalArea area)
    {
        super();
        assign(area);
    }

    /**
     * Assign procedure
     */
    public void assign(PhysicalArea area)
    {
        min.assign(area.min);
        max.assign(area.max);
        dim.assign(area.dim);
        zoom = area.zoom;
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

    /**
     * Converts physical length to screen length along X-axe
     */
    public int toScreenXLength(double flength, Rect r)
    {
        return (int) ((double) r.width() * flength / dim.x);
    }

    /**
     * Converts physical length to screen length along Y-axe
     */
    public int toScreenYLength(double flength, Rect r)
    {
        return (int) ((double) r.height() * flength / dim.y);
    }

    /**
     * Procedure checks whether this area is zoomed
     */
    public boolean isZoomed()
    {
        return zoom != NO_ZOOM_FACTOR;
    }

    /**
     * Procedure returns current zoom factor
     */
    public double getZoom()
    {
        return zoom;
    }

    /**
     * Procedure applies new scaling parameters for this area
     */
    public void scale(PhysicalArea src, double scaleFactor, double maxScale, double dx, double dy)
    {
        // calculate new zoom factor
        zoom = src.getDim().x / getDim().x;
        zoom *= scaleFactor;
        zoom = Math.max(NO_ZOOM_FACTOR, Math.min(zoom, maxScale));

        // calculate new dimension
        dim.x = src.dim.x / zoom;
        dim.y = src.dim.y / zoom;

        // calculate new focus
        double focusX = (max.x + min.x) / 2.0 - dx * dim.x;
        double focusY = (max.y + min.y) / 2.0 - dy * dim.y;
        focusX = Math.max(Math.min(focusX, src.max.x - dim.x / 2), src.min.x + dim.x / 2);
        focusY = Math.max(Math.min(focusY, src.max.y - dim.y / 2), src.min.y + dim.y / 2);

        // update the area
        min.x = (src.max.x + src.min.x) / 2.0 - dim.x / 2.0 + focusX;
        max.x = (src.max.x + src.min.x) / 2.0 + dim.x / 2.0 + focusX;
        min.y = (src.max.y + src.min.y) / 2.0 - dim.y / 2.0 + focusY;
        max.y = (src.max.y + src.min.y) / 2.0 + dim.y / 2.0 + focusY;
    }

}
