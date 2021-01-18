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
package com.mkulesh.micromath.math;

import com.mkulesh.micromath.formula.CalculaterTask;
import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.formula.Equation;
import com.mkulesh.micromath.formula.TermField;
import com.mkulesh.micromath.utils.ViewUtils;

import java.util.ArrayList;

/*--------------------------------------------------------*
 * Array result for equation
 *--------------------------------------------------------*/
public class EquationArrayResult
{
    public final static int MAX_DIMENSION = 0;

    private final static int D0 = 0;
    private final static int D1 = 1;
    private final static int D2 = 2;

    private int[] dimensions = null;
    private CalculatedValue[] values = null;
    private int[] idxValues = null;
    private final Equation equation;
    private final TermField equationTerm;

    public EquationArrayResult(int size)
    {
        this.equation = null;
        this.equationTerm = null;
        final int dimNumber = 1;
        final int[] dimValues = new int[dimNumber];
        dimValues[D0] = size;
        resize(dimValues);
    }

    public EquationArrayResult(int size1, int size2)
    {
        this.equation = null;
        this.equationTerm = null;
        final int dimNumber = 2;
        final int[] dimValues = new int[dimNumber];
        dimValues[D0] = size1;
        dimValues[D1] = size2;
        resize(dimValues);
    }

    public EquationArrayResult(Equation equation, TermField rightTerm)
    {
        this.equation = equation;
        this.equationTerm = rightTerm;
    }

    private int getDimNumber()
    {
        return dimensions == null ? ViewUtils.INVALID_INDEX : dimensions.length;
    }

    public int[] getDimensions()
    {
        return dimensions;
    }

    public CalculatedValue[] getRawValues()
    {
        return values;
    }

    public void calculate(CalculaterTask thread, ArrayList<String> arguments, final EquationArrayResult mergedArray) throws CancelException
    {
        values = null;

        final int dimNumber = arguments.size();
        if (dimNumber < 1 || dimNumber > MAX_DIMENSION)
        {
            return;
        }

        if (mergedArray != null && mergedArray.getDimNumber() != dimNumber)
        {
            return;
        }

        // collect intervals and dimensions
        final ArrayList<CalculatedValue[]> intervalValues = new ArrayList<>();
        final int[] dimValues = new int[dimNumber];
        final int[] fixedIndex = new int[dimNumber];
        final CalculatedValue[] argValues = new CalculatedValue[dimNumber];
        for (int dim = 0; dim < dimNumber; dim++)
        {
            final String arg = arguments.get(dim);
            if (arg == null)
            {
                return;
            }

            // Process an integer index
            final Integer numIndex = CalculatedValue.toInteger(arg);
            if (numIndex != null)
            {
                if (numIndex < 0)
                {
                    return;
                }
                final int size = numIndex + 1;
                dimValues[dim] = size;
                final CalculatedValue[] interval = new CalculatedValue[size];
                for (int i = 0; i < size; i++)
                {
                    interval[i] = new CalculatedValue(CalculatedValue.ValueType.REAL, i, 0.0);
                }
                fixedIndex[dim] = numIndex;
                intervalValues.add(interval);
            }
            else
            {
                final Equation e = equation.searchLinkedEquation(
                        arg, Equation.ARG_NUMBER_INTERVAL);
                if (e == null || !e.isInterval())
                {
                    return;
                }
                final CalculatedValue[] interval = e.getInterval();
                if (interval == null)
                {
                    return;
                }
                for (CalculatedValue c : interval)
                {
                    if (c.getInteger() < 0)
                    {
                        return;
                    }
                }
                final int lastIndex = interval[interval.length - 1].getInteger();
                if (lastIndex <= 0)
                {
                    return;
                }
                dimValues[dim] = lastIndex + 1;
                fixedIndex[dim] = Integer.MIN_VALUE;
                intervalValues.add(interval);
            }
            argValues[dim] = new CalculatedValue();
        }

        // merge mergedArray
        if (mergedArray != null)
        {
            final int[] mergedDimensions = mergedArray.getDimensions();
            for (int dim = 0; dim < dimNumber; dim++)
            {
                dimValues[dim] = Math.max(dimValues[dim], mergedDimensions[dim]);
            }
            // initialize array with zero
            resize(dimValues);
            mergeValues(mergedArray, mergedDimensions);
        }
        else
        {
            // initialize array with zero
            resize(dimValues);
        }


        // calculate array
        equation.setArgumentValues(argValues);
        for (final CalculatedValue d0 : intervalValues.get(D0))
        {
            final int i0 = d0.getInteger();
            if (i0 < 0)
            {
                continue;
            }
            argValues[D0].assign(d0);
            final boolean calc0 = fixedIndex[D0] < 0 || fixedIndex[D0] == i0;
            if (dimNumber == 1)
            {
                if (calc0)
                {
                    equationTerm.getValue(thread, values[i0]);
                }
                continue;
            }
            for (final CalculatedValue d1 : intervalValues.get(D1))
            {
                final int i1 = d1.getInteger();
                if (i1 < 0)
                {
                    continue;
                }
                argValues[D1].assign(d1);
                final boolean calc1 = fixedIndex[D1] < 0 || fixedIndex[D1] == i1;
                if (dimNumber == 2)
                {
                    if (calc0 && calc1)
                    {
                        equationTerm.getValue(thread, values[getIndex(i0, i1)]);
                    }
                    continue;
                }
                for (final CalculatedValue d2 : intervalValues.get(D2))
                {
                    final int i2 = d2.getInteger();
                    if (i2 < 0)
                    {
                        continue;
                    }
                    argValues[D2].assign(d2);
                    final boolean calc2 = fixedIndex[D2] < 0 || fixedIndex[D2] == i2;
                    if (calc0 && calc1 && calc2)
                    {
                        equationTerm.getValue(thread, values[getIndex(i0, i1, i2)]);
                    }
                }
            }
        }
    }

    private void mergeValues(EquationArrayResult mergedArray, int[] mergedDimensions)
    {
        for (int i0 = 0; i0 < mergedDimensions[D0]; i0++)
        {
            if (mergedDimensions.length == 1)
            {
                values[i0].merge(mergedArray.values[i0]);
                continue;
            }
            for (int i1 = 0; i1 < mergedDimensions[D1]; i1++)
            {
                if (mergedDimensions.length == 2)
                {
                    values[getIndex(i0, i1)].merge(mergedArray.values[mergedArray.getIndex(i0, i1)]);
                    continue;
                }
                for (int i2 = 0; i2 < mergedDimensions[D2]; i2++)
                {
                    values[getIndex(i0, i1, i2)].merge(mergedArray.values[mergedArray.getIndex(i0, i1, i2)]);
                }
            }
        }
    }

    private int getIndex(int i0, int i1)
    {
        return i0 * dimensions[1] + i1;
    }

    private int getIndex(int i0, int i1, int i2)
    {
        return (i0 * dimensions[1] + i1) * dimensions[2] + i2;
    }

    public void resize1D(int size)
    {
        final int dimNumber = 1;
        final int[] dimValues = new int[dimNumber];
        dimValues[D0] = size;
        resize(dimValues);
    }

    private void resize(int[] dimValues)
    {
        dimensions = dimValues;
        int size = 1;
        for (int dim : dimensions)
        {
            size *= dim;
        }
        values = new CalculatedValue[size];
        for (int i = 0; i < size; i++)
        {
            values[i] = new CalculatedValue(CalculatedValue.ValueType.REAL, 0.0, 0.0);
        }
        idxValues = new int[dimensions.length];
    }

    public CalculatedValue getValue1D(int idx)
    {
        final int dimNumber = getDimNumber();
        if (values == null || dimNumber != 1 || idx < 0 || idx >= dimensions[D0])
        {
            return CalculatedValue.NaN;
        }
        return values[idx];
    }

    public CalculatedValue getValue2D(int idx1, int idx2)
    {
        final int dimNumber = getDimNumber();
        if (values == null || dimNumber != 2 || idx1 < 0 || idx1 >= dimensions[D0] || idx2 < 0 || idx2 >= dimensions[D1])
        {
            return CalculatedValue.NaN;
        }
        return values[getIndex(idx1, idx2)];
    }

    public CalculatedValue getValue(CalculatedValue[] argValues)
    {
        final int dimNumber = getDimNumber();
        if (values == null || argValues.length != dimNumber)
        {
            return CalculatedValue.NaN;
        }

        for (int i = 0; i < dimNumber; i++)
        {
            final CalculatedValue argValue = argValues[i];
            if (!argValue.isReal())
            {
                return CalculatedValue.NaN;
            }
            final int idx = argValue.getInteger();
            if (idx < 0 || idx >= dimensions[i])
            {
                return CalculatedValue.NaN;
            }
            idxValues[i] = idx;
        }
        switch (dimNumber)
        {
        case 1:
            return values[idxValues[D0]];
        case 2:
            return values[getIndex(idxValues[D0], idxValues[D1])];
        case 3:
            return values[getIndex(idxValues[D0], idxValues[D1], idxValues[D2])];
        default:
            return CalculatedValue.NaN;
        }
    }

    public boolean isArray1D()
    {
        return getDimNumber() == 1 && values != null && values.length > 0;
    }
}
