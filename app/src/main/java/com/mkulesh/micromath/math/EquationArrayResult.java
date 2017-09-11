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
package com.mkulesh.micromath.math;

import java.util.ArrayList;

import com.mkulesh.micromath.formula.CalculaterTask;
import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.formula.Equation;
import com.mkulesh.micromath.formula.FormulaBase;
import com.mkulesh.micromath.formula.TermField;
import com.mkulesh.micromath.utils.ViewUtils;

/*********************************************************
 * Array result for equation
 *********************************************************/
public class EquationArrayResult
{
    public final static int MAX_DIMENSION = 3;

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

    public int getDimNumber()
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

    public void calculate(CalculaterTask thread, ArrayList<String> arguments) throws CancelException
    {
        values = null;

        final int dimNumber = arguments.size();
        if (dimNumber < 1 || dimNumber > MAX_DIMENSION)
        {
            return;
        }

        // collect intervals and dimensions
        final ArrayList<ArrayList<Double>> intervalValues = new ArrayList<ArrayList<Double>>();
        final int[] dimValues = new int[dimNumber];
        final CalculatedValue[] argValues = new CalculatedValue[dimNumber];
        for (int dim = 0; dim < dimNumber; dim++)
        {
            final FormulaBase f = equation.getFormulaList().getFormula(arguments.get(dim), 0, equation.getId(), true);
            if (f == null || !(f instanceof Equation) || !((Equation) f).isInterval())
            {
                return;
            }
            final Equation e = (Equation) f;
            ArrayList<Double> interval = e.getInterval(thread);
            if (interval == null || interval.isEmpty())
            {
                return;
            }
            final int lastIndex = interval.get(interval.size() - 1).intValue();
            if (lastIndex <= 0)
            {
                return;
            }
            dimValues[dim] = lastIndex + 1;
            intervalValues.add(interval);
            argValues[dim] = new CalculatedValue();
        }

        // initialize array with zero
        resize(dimValues);

        // calculate array
        equation.setArgumentValues(argValues);
        for (Double d0 : intervalValues.get(D0))
        {
            final int i0 = d0.intValue();
            argValues[D0].setValue(d0);
            if (dimNumber == 1)
            {
                equationTerm.getValue(thread, values[i0]);
                continue;
            }
            for (Double d1 : intervalValues.get(D1))
            {
                final int i1 = d1.intValue();
                argValues[D1].setValue(d1);
                if (dimNumber == 2)
                {
                    equationTerm.getValue(thread, values[getIndex(i0, i1)]);
                    continue;
                }
                for (Double d2 : intervalValues.get(D2))
                {
                    final int i2 = d2.intValue();
                    argValues[D2].setValue(d2);
                    equationTerm.getValue(thread, values[getIndex(i0, i1, i2)]);
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
}
