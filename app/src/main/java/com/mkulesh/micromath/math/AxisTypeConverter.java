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

import org.apache.commons.math3.util.FastMath;

public final class AxisTypeConverter
{
    public enum Type
    {
        LINEAR,
        LOG10
    }

    public static double toSpecialType(final double value, Type type)
    {
        return (type == Type.LINEAR) ? value : FastMath.log10(value);
    }

    public static double toBaseType(final double value, Type type)
    {
        return (type == Type.LINEAR) ? value : FastMath.pow(10.0, value);
    }

    public static void toBaseType(final double[] values, Type type)
    {
        for (int i = 0; i < values.length; i++)
        {
            values[i] = toBaseType(values[i], type);
        }
    }

    public static double[] cloneToBaseType(final double[] values, Type type)
    {
        final double[] retValue = new double[values.length];
        System.arraycopy(values, 0, retValue, 0, values.length);
        toBaseType(retValue, type);
        return retValue;
    }
}
