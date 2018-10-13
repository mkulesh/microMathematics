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

import android.graphics.Color;

import com.mkulesh.micromath.math.CalculatedValue;

import java.util.ArrayList;

/**
 * Represents a palette, that is a sequence of colors. A palette assigns a color to each real number in the range 0
 * through 1, inclusive. The color is specified at several points in this range (including at least 0 and 1). These
 * points are referred to as "division points." Between these points, the color is determined by linear interpolation. A
 * palette can have color type HSB or RGB. For HSB colors, the interpolation is done in the HSB color space; for RGB
 * colors, the interpolation is done in the RGB color space.
 */
public class Palette
{

    enum ColorType
    {
        RGB,
        HSV
    }

    // data and parameters
    private final ColorType colorType;
    private boolean mirrorOutOfRangeComponents;
    private final ArrayList<Double> divisionPoints = new ArrayList<>();
    private final ArrayList<float[]> divisionPointColors = new ArrayList<>();
    private final float[] hsvConvertor = new float[3];

    /**
     * Create a palette of specified color type. For HSB color type, the palette is a rainbow spectrum. For the RGB
     * color type, the palette is a grayscale from white to black.
     */
    public Palette(ColorType colorType)
    {
        this.colorType = colorType;
        mirrorOutOfRangeComponents = true;
        divisionPoints.add(0.0);
        divisionPoints.add(1.0);
        switch (colorType)
        {
        case HSV:
            divisionPointColors.add(new float[]{ 0, 1, 1 });
            divisionPointColors.add(new float[]{ 1, 1, 1 });
            break;
        case RGB:
            divisionPointColors.add(new float[]{ 1, 1, 1 });
            divisionPointColors.add(new float[]{ 0, 0, 0 });
            break;
        }
    }

    /**
     * Removes a specified division point, one of the points where the color is specified explicitly.
     */
    public void join(int divisionPointIndex)
    {
        if (divisionPointIndex <= 0 || divisionPointIndex >= divisionPoints.size() - 1)
        {
            throw new IllegalArgumentException("Division point index out of range: " + divisionPointIndex);
        }
        divisionPoints.remove(divisionPointIndex);
        divisionPointColors.remove(divisionPointIndex);
    }

    /**
     * Adds a division point to the palette. The color associated to the point is obtained by interpolating between the
     * colors of the points that neighbor the new point.
     */
    public int split(double divisionPoint)
    {
        if (divisionPoint <= 0 || divisionPoint >= 1 || CalculatedValue.isInvalidReal(divisionPoint))
        {
            throw new IllegalArgumentException("Division point out of range: " + divisionPoint);
        }
        int index = 0;
        while (divisionPoint > divisionPoints.get(index))
        {
            index++;
        }
        if (Math.abs(divisionPoint - divisionPoints.get(index)) < 1e-15)
        {
            return -1;
        }
        final float ratio = (float) ((divisionPoint - divisionPoints.get(index - 1)) / (divisionPoints.get(index) - divisionPoints
                .get(index - 1)));
        final float[] c1 = divisionPointColors.get(index - 1);
        final float[] c2 = divisionPointColors.get(index);
        final float a = c1[0] + ratio * (c2[0] - c1[0]);
        final float b = c1[1] + ratio * (c2[1] - c1[1]);
        final float c = c1[2] + ratio * (c2[2] - c1[2]);
        float[] color = new float[]{ a, b, c };
        divisionPoints.add(index, divisionPoint);
        divisionPointColors.add(index, color);
        return index;
    }

    /**
     * Get the color that this palette assigns to a specified number.
     */
    public int getColor(double position, int alpha)
    {
        if (position < 0)
        {
            position = 0;
        }
        if (position > 1)
        {
            position = 1;
        }
        int pt = 1;
        while (position > divisionPoints.get(pt))
        {
            pt++;
        }
        final float ratio = (float) ((position - divisionPoints.get(pt - 1)) / (divisionPoints.get(pt) - divisionPoints
                .get(pt - 1)));
        final float[] c1 = divisionPointColors.get(pt - 1);
        final float[] c2 = divisionPointColors.get(pt);
        final float a = clamp1(c1[0] + ratio * (c2[0] - c1[0]));
        final float b = clamp2(c1[1] + ratio * (c2[1] - c1[1]));
        final float c = clamp2(c1[2] + ratio * (c2[2] - c1[2]));
        switch (colorType)
        {
        case HSV:
            hsvConvertor[0] = 360f * a;
            hsvConvertor[1] = b;
            hsvConvertor[2] = c;
            return Color.HSVToColor(alpha, hsvConvertor);
        case RGB:
            return Color.argb(alpha, (int) (255f * a), (int) (255f * b), (int) (255f * c));
        }
        return 0;
    }

    /**
     * Returns the number of division points in the palette, always two or more.
     */
    public int getDivisionPointCount()
    {
        return divisionPoints.size();
    }

    /**
     * Returns a specified division points. The return value is the range 0.0 to 1.0. Divsion points are stored in
     * strictly increasing order.
     */
    public double getDivisionPoint(int index)
    {
        return divisionPoints.get(index);
    }

    /**
     * Sets the value of a specified division point. Does not apply to the first or last points, which always have
     * values 0.0 and 1.0. The new value must be strictly between the positions the neighboring division points.
     */
    public void setDivisionPoint(int index, double position)
    {
        if (index <= 0 || index >= divisionPoints.size() - 1)
        {
            throw new IllegalArgumentException("Index out of legal range");
        }
        if (position <= divisionPoints.get(index - 1) || position >= divisionPoints.get(index + 1))
        {
            throw new IllegalArgumentException("Division point position outside of legal range.");
        }
        if (position != divisionPoints.get(index))
        {
            divisionPoints.set(index, position);
        }
    }

    /**
     * Get the color associated with a given division point.
     */
    public int getDivisionPointColor(int index)
    {
        float[] components = divisionPointColors.get(index);
        float a = clamp1(components[0]);
        float b = clamp2(components[1]);
        float c = clamp2(components[2]);
        switch (colorType)
        {
        case HSV:
            hsvConvertor[0] = 360f * a;
            hsvConvertor[1] = b;
            hsvConvertor[2] = c;
            return Color.HSVToColor(hsvConvertor);
        case RGB:
            return Color.rgb((int) (255f * a), (int) (255f * b), (int) (255f * c));
        }
        return 0;
    }

    /**
     * Returns the color components for the division point at a specified index.
     */
    public float[] getDivisionPointColorComponents(int index)
    {
        return divisionPointColors.get(index).clone();
    }

    /**
     * Set the color components for the division point at a s specified index in the list of division points. These
     * components are the color data that is stored for each division point and that are used for interpolation between
     * division points. Note that when a color is actually computed, the component values must be in the range 0.0 to
     * 1.0. However, the values specified here do NOT have to be in this range. Values given here are used for
     * interpolation, and then the resulting values are transformed into the range 0.0 to 1.0 just before the color is
     * computed. This means that the value can effectively oscillate several times between two division points.
     */
    public void setDivisionPointColorComponents(int index, float c1, float c2, float c3)
    {
        float[] c = divisionPointColors.get(index);
        if (c1 == c[0] && c2 == c[1] && c3 == c[2])
        {
            return;
        }
        c[0] = c1;
        c[1] = c2;
        c[2] = c3;
    }

    /**
     * Return the color type of this palette, which is one of the constants ColorType.COLOR_TYPE_RGB or
     * ColorType.COLOR_TYPE_HSB.
     */
    public ColorType getColorType()
    {
        return colorType;
    }

    public boolean getMirrorOutOfRangeComponents()
    {
        return mirrorOutOfRangeComponents;
    }

    /**
     * Sets the value of the confusing mirrorOutOfRangeComponents property. This only has an effect when a
     * floating-point color component value that is outside the range 0.0 to 1.0 has to be transformed to a value within
     * that range. For a Hue, the whole-number part is simply discarded. For the other components however, the
     * transformation depends on the value of the mirrorOutOfRangeComponents property. If the property is false, the
     * whole-number part is discarded, but this results in a discontinuity at integer values. If the property is true,
     * this discontinuity is avoided by having the value oscillate with period 2 instead of cycle with period 1. The
     * default value is true, and this is never changed in the Mandelbrot application. So, really, you shouldn't even be
     * reading this.
     */
    public void setMirrorOutOfRangeComponents(boolean mirrorOutOfRangeComponents)
    {
        if (this.mirrorOutOfRangeComponents == mirrorOutOfRangeComponents)
        {
            return;
        }
        this.mirrorOutOfRangeComponents = mirrorOutOfRangeComponents;
    }

    private float clamp1(float x)
    {
        if (colorType == ColorType.HSV || !mirrorOutOfRangeComponents)
        {
            return x - (float) Math.floor(x);
        }
        else
        {
            return clamp2(x);
        }
    }

    private float clamp2(float x)
    {
        if (!mirrorOutOfRangeComponents)
        {
            return x - (float) Math.floor(x);
        }
        x = 2 * (x / 2 - (float) Math.floor(x / 2));
        if (x > 1)
        {
            x = 2 - x;
        }
        return x;
    }
}
