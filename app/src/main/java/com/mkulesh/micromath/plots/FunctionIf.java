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
package com.mkulesh.micromath.plots;

import com.mkulesh.micromath.properties.LineProperties;

public interface FunctionIf
{
    enum Type
    {
        FUNCTION_2D,
        FUNCTION_3D
    }

    public final static int X = 0;
    public final static int Y = 1;
    public final static int Z = 2;
    public final static int MIN = 0;
    public final static int MAX = 1;

    /**
     * Procedure returns the type of the function
     */
    public Type getType();

    /**
     * Procedure returns the list of x values
     */
    public double[] getXValues();

    /**
     * Procedure returns the list of y values
     */
    public double[] getYValues();

    /**
     * Procedure returns the list of x values
     */
    public double[][] getZValues();

    /**
     * Procedure returns minimum and maximum values of the function
     */
    public double[] getMinMaxValues(int idx);

    /**
     * Procedure returns the line parameters for this function
     */
    public LineProperties getLineParameters();

    /**
     * Procedure returns symbolic labels for all three axes
     */
    public String[] getLabels();
}
