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

import org.apache.commons.math3.util.FastMath;

public final class SurfacePlotProjector
{
    private double scale_x, scale_y, scale_z; // 3D scaling factor
    private double distance; // distance to object
    private double _2D_scale; // 2D scaling factor
    private double rotation, elevation; // rotation and elevation angle
    private double sin_rotation, cos_rotation; // sin and cos of rotation angle
    private double sin_elevation, cos_elevation; // sin and cos of elevation angle
    private int _2D_trans_x, _2D_trans_y; // 2D translation
    private int x1, x2, y1, y2; // projection area
    private int center_x, center_y; // center of projection area
    private double tmpValue;
    private int trans_x, trans_y;
    private double factor;
    private double sx_cos, sy_cos, sz_cos;
    private double sx_sin, sy_sin, sz_sin;

    public double zmin, zmax;
    public double zfactor;

    private final double DEGTORAD = Math.PI / 180.0;

    /**
     * Default constructor.
     */
    SurfacePlotProjector()
    {
        setScaling(1);
        setRotationAngle(0);
        setElevationAngle(0);
        setDistance(10);
        set2DScaling(1);
        set2DTranslation(0, 0);
    }

    /**
     * Sets the projection area.
     */
    public void setProjectionArea(int width, int height)
    {
        x1 = 0;
        x2 = x1 + width;
        y1 = 0;
        y2 = y1 + height;
        center_x = (x1 + x2) / 2;
        center_y = (y1 + y2) / 2;
        trans_x = center_x + _2D_trans_x;
        trans_y = center_y + _2D_trans_y;
    }

    /**
     * Sets the rotation angle.
     */
    public void setRotationAngle(double angle)
    {
        rotation = angle;
        sin_rotation = FastMath.sin(angle * DEGTORAD);
        cos_rotation = FastMath.cos(angle * DEGTORAD);
        sx_cos = -scale_x * cos_rotation;
        sx_sin = -scale_x * sin_rotation;
        sy_cos = -scale_y * cos_rotation;
        sy_sin = scale_y * sin_rotation;
    }

    /**
     * Gets current rotation angle.
     */
    public double getRotationAngle()
    {
        return rotation;
    }

    /**
     * Gets the sine of rotation angle.
     */
    public double getSinRotationAngle()
    {
        return sin_rotation;
    }

    /**
     * Gets the cosine of rotation angle.
     */
    public double getCosRotationAngle()
    {
        return cos_rotation;
    }

    /**
     * Sets the elevation angle.
     */
    public void setElevationAngle(double angle)
    {
        elevation = angle;
        sin_elevation = FastMath.sin(angle * DEGTORAD);
        cos_elevation = FastMath.cos(angle * DEGTORAD);
        sz_cos = scale_z * cos_elevation;
        sz_sin = scale_z * sin_elevation;
    }

    /**
     * Gets current elevation angle.
     */
    public double getElevationAngle()
    {
        return elevation;
    }

    /**
     * Gets the sine of elevation angle.
     */
    public double getSinElevationAngle()
    {
        return sin_elevation;
    }

    /**
     * Gets the cosine of elevation angle.
     */
    public double getCosElevationAngle()
    {
        return cos_elevation;
    }

    /**
     * Sets the projector distance.
     */
    public void setDistance(double new_distance)
    {
        distance = new_distance;
        factor = distance * _2D_scale;
    }

    /**
     * Gets the projector distance.
     */
    public double getDistance()
    {
        return distance;
    }

    /**
     * Sets the scaling factor in x direction.
     */
    public void setXScaling(double scaling)
    {
        scale_x = scaling;
        sx_cos = -scale_x * cos_rotation;
        sx_sin = -scale_x * sin_rotation;
    }

    /**
     * Gets the scaling factor in x direction.
     */
    public double getXScaling()
    {
        return scale_x;
    }

    /**
     * Sets the scaling factor in y direction.
     */
    public void setYScaling(double scaling)
    {
        scale_y = scaling;
        sy_cos = -scale_y * cos_rotation;
        sy_sin = scale_y * sin_rotation;
    }

    /**
     * Gets the scaling factor in y direction.
     */
    public double getYScaling()
    {
        return scale_y;
    }

    /**
     * Sets the scaling factor in z direction.
     */
    public void setZScaling(double scaling)
    {
        scale_z = scaling;
        sz_cos = scale_z * cos_elevation;
        sz_sin = scale_z * sin_elevation;
    }

    /**
     * Gets the scaling factor in z direction.
     */
    public double getZScaling()
    {
        return scale_z;
    }

    /**
     * Sets the scaling factor in all direction.
     */
    public void setScaling(double x, double y, double z)
    {
        scale_x = x;
        scale_y = y;
        scale_z = z;

        sx_cos = -scale_x * cos_rotation;
        sx_sin = -scale_x * sin_rotation;
        sy_cos = -scale_y * cos_rotation;
        sy_sin = scale_y * sin_rotation;
        sz_cos = scale_z * cos_elevation;
        sz_sin = scale_z * sin_elevation;
    }

    /**
     * Sets the same scaling factor for all direction.
     */
    public void setScaling(double scaling)
    {
        scale_x = scale_y = scale_z = scaling;

        sx_cos = -scale_x * cos_rotation;
        sx_sin = -scale_x * sin_rotation;
        sy_cos = -scale_y * cos_rotation;
        sy_sin = scale_y * sin_rotation;
        sz_cos = scale_z * cos_elevation;
        sz_sin = scale_z * sin_elevation;
    }

    /**
     * Sets the 2D scaling factor.
     */
    public void set2DScaling(double scaling)
    {
        _2D_scale = scaling;
        factor = distance * _2D_scale;
    }

    /**
     * Gets the 2D scaling factor.
     */
    public double get2DScaling()
    {
        return _2D_scale;
    }

    /**
     * Sets the 2D translation.
     */
    public void set2DTranslation(int x, int y)
    {
        _2D_trans_x = x;
        _2D_trans_y = y;

        trans_x = center_x + _2D_trans_x;
        trans_y = center_y + _2D_trans_y;
    }

    /**
     * Sets the 2D x translation.
     */
    public void set2D_xTranslation(int x)
    {
        _2D_trans_x = x;
        trans_x = center_x + _2D_trans_x;
    }

    /**
     * Gets the 2D x translation.
     */
    public int get2D_xTranslation()
    {
        return _2D_trans_x;
    }

    /**
     * Sets the 2D y translation.
     */
    public void set2D_yTranslation(int y)
    {
        _2D_trans_y = y;
        trans_y = center_y + _2D_trans_y;
    }

    /**
     * Gets the 2D y translation.
     */
    public int get2D_yTranslation()
    {
        return _2D_trans_y;
    }

    /**
     * Projects 3D points.
     */
    public void project(Point p, double x, double y, double z)
    {
        // rotates
        tmpValue = x;
        x = x * sx_cos + y * sy_sin;
        y = tmpValue * sx_sin + y * sy_cos;

        // elevates and projects
        tmpValue = factor / (y * cos_elevation - z * sz_sin + distance);
        p.x = FastMath.round((float) (x * tmpValue)) + trans_x;
        p.y = FastMath.round((float) ((y * sin_elevation + z * sz_cos) * -tmpValue)) + trans_y;
    }

    /**
     * Sets the minimum and maximum value of z range. This values is used to compute a factor to normalized z values
     * into the range -10 .. +10.
     */
    public void setZRange(double zmin, double zmax)
    {
        this.zmin = zmin;
        this.zmax = zmax;
        zfactor = 20 / (zmax - zmin);
    }
}
