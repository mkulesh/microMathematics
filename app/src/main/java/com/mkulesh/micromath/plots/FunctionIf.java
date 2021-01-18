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
package com.mkulesh.micromath.plots;

import com.mkulesh.micromath.properties.LineProperties;

public interface FunctionIf
{
    enum Type
    {
        FUNCTION_2D,
        FUNCTION_3D
    }

    int X = 0;
    int Y = 1;
    int Z = 2;
    int MIN = 0;
    int MAX = 1;

    /**
     * Procedure returns the type of the function
     */
    Type getType();

    /**
     * Procedure returns the list of x values
     */
    double[] getXValues();

    /**
     * Procedure returns the list of y values
     */
    double[] getYValues();

    /**
     * Procedure returns the list of x values
     */
    double[][] getZValues();

    /**
     * Procedure returns minimum and maximum values of the function
     */
    double[] getMinMaxValues(int idx);

    /**
     * Procedure returns the line parameters for this function
     */
    LineProperties getLineParameters();

    /**
     * Procedure returns symbolic labels for all three axes
     */
    String[] getLabels();
}
