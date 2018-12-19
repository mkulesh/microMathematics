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

public class ArgumentValueItem
{
    public final double argument;
    public final double value;

    public ArgumentValueItem()
    {
        this.argument = Double.NaN;
        this.value = Double.NaN;
    }

    public ArgumentValueItem(double argument, double value)
    {
        this.argument = argument;
        this.value = value;
    }
}
