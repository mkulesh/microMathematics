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
package com.mkulesh.micromath.formula;

import android.content.Context;
import android.util.AttributeSet;

import com.mkulesh.micromath.R;
import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.formula.TermField.ErrorNotification;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomTextView;

import java.util.ArrayList;

public class Equation extends LinkHolder implements CalculatableIf
{
    private TermField leftTerm = null;
    private TermField rightTerm = null;
    private double argumentValue = 0.0;

    /*********************************************************
     * Constant result
     *********************************************************/

    private class EquationConstantResult
    {
        private Double value = null;

        public Double getValue(CalculaterTask thread) throws CancelException
        {
            if (value == null)
            {
                value = rightTerm.getValue(thread);
            }
            return value;
        }
    }

    private EquationConstantResult constantResult = null;

    /*********************************************************
     * Constructors
     *********************************************************/

    public Equation(FormulaList formulaList, int id)
    {
        super(formulaList, null, 0);
        setId(id);
        onCreate();
    }

    /*********************************************************
     * GUI constructors to avoid lint warning
     *********************************************************/

    public Equation(Context context)
    {
        super(null, null, 0);
    }

    public Equation(Context context, AttributeSet attrs)
    {
        super(null, null, 0);
    }

    /*********************************************************
     * Re-implementation for methods for Object superclass
     *********************************************************/

    @Override
    public String toString()
    {
        String n = getName();
        if (n == null)
        {
            n = "<EMPTY>";
        }
        if (getArguments() != null)
        {
            n += getArguments().toString();
        }
        if (isInterval())
        {
            n += ": interval";
        }
        return "Formula " + getBaseType().toString() + "(Id: " + getId() + ", Name: " + n + ")";
    }

    /*********************************************************
     * Re-implementation for methods for FormulaBase superclass
     *********************************************************/

    @Override
    public BaseType getBaseType()
    {
        return BaseType.EQUATION;
    }

    public boolean enableObjectProperties()
    {
        return false;
    }

    @Override
    public boolean isContentValid(ValidationPassType type)
    {
        boolean isValid = super.isContentValid(type);
        constantResult = null;

        switch (type)
        {
        case VALIDATE_SINGLE_FORMULA:
            break;
        case VALIDATE_LINKS:
            // additional checks for recursive links
            if (isValid && !leftTerm.isEmpty())
            {
                String errorMsg = null;
                final ArrayList<Equation> allFunctions = getAllFunctions();
                if (allFunctions.contains(this))
                {
                    isValid = false;
                    errorMsg = getContext().getResources().getString(R.string.error_recursive_call);
                }
                leftTerm.setError(errorMsg, ErrorNotification.LAYOUT_BORDER, null);
            }
            // check that the equation result can be cashed
            if (isValid)
            {
                final ArrayList<Equation> linkedIntervals = getAllIntervals();
                final ArrayList<String> arguments = getArguments();
                if (!isInterval() && linkedIntervals.isEmpty() && (arguments == null || arguments.isEmpty()))
                {
                    constantResult = new EquationConstantResult();
                }
            }
            break;
        }
        return isValid;
    }

    /*********************************************************
     * Re-implementation for methods for Calculatable interface
     *********************************************************/

    @Override
    public double getValue(CalculaterTask thread) throws CancelException
    {
        if (constantResult != null)
        {
            return constantResult.getValue(thread);
        }
        return rightTerm.getValue(thread);
    }

    /*********************************************************
     * Equation-specific methods
     *********************************************************/

    /**
     * Procedure creates the formula layout
     */
    private void onCreate()
    {
        inflateRootLayout(R.layout.formula_equation, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        // create name term
        {
            CustomEditText v = (CustomEditText) layout.findViewById(R.id.formula_equation_name);
            leftTerm = addTerm(this, layout, v, this, false);
        }
        // create assign character
        {
            CustomTextView v = (CustomTextView) layout.findViewById(R.id.formula_equation_assign);
            v.prepare(CustomTextView.SymbolType.TEXT, getFormulaList().getActivity(), this);
        }
        // create value term
        {
            CustomEditText v = (CustomEditText) layout.findViewById(R.id.formula_equation_value);
            rightTerm = addTerm(this, layout, v, this, false);
            rightTerm.bracketsType = TermField.BracketsType.NEVER;
        }
    }

    /**
     * Procedure returns the parsed name of this formula
     */
    public String getName()
    {
        return leftTerm.getParser().getFunctionName();
    }

    /**
     * Procedure returns the parsed arguments of this formula
     */
    public ArrayList<String> getArguments()
    {
        return leftTerm.getParser().getFunctionArgs();
    }

    /**
     * Procedure returns whether the given string is an argument
     */
    public boolean isArgument(String text)
    {
        if (text != null && getArguments() != null)
        {
            return (getArguments().indexOf(text) >= 0);
        }
        return false;
    }

    public void setArgument(double value)
    {
        argumentValue = value;
    }

    public double getArgument()
    {
        return argumentValue;
    }

    /**
     * Procedure checks whether this root formula represents an interval
     */
    public boolean isInterval()
    {
        FormulaTerm t = rightTerm.getTerm();
        return (t != null && t instanceof FormulaTermInterval);
    }

    /**
     * Procedure returns declared interval if this root formula represents an interval
     */
    public ArrayList<Double> getInterval(CalculaterTask thread) throws CancelException
    {
        FormulaTerm t = rightTerm.getTerm();
        if (t != null && t instanceof FormulaTermInterval)
        {
            return ((FormulaTermInterval) t).getInterval(thread);
        }
        return null;
    }

    /**
     * Procedure fills the given value array and array with minimum and maximum values from this interval
     */
    public double[] fillInterval(CalculaterTask thread, double[] targetValues, double[] minMaxValues)
            throws CancelException
    {
        if (!isInterval() || minMaxValues == null || minMaxValues.length != 2)
        {
            return null;
        }
        final ArrayList<Double> arr = getInterval(thread);
        if (arr == null || arr.isEmpty())
        {
            return null;
        }
        double[] retValues = (targetValues != null && targetValues.length == arr.size()) ? targetValues
                : new double[arr.size()];
        minMaxValues[0] = minMaxValues[1] = Double.NaN;
        for (int i = 0; i < retValues.length; i++)
        {
            final double v = arr.get(i);
            retValues[i] = v;
            if (i == 0)
            {
                minMaxValues[0] = minMaxValues[1] = v;
            }
            else
            {
                minMaxValues[0] = Math.min(minMaxValues[0], v);
                minMaxValues[1] = Math.max(minMaxValues[1], v);
            }
        }
        return retValues;
    }

    /**
     * Checks that the given equation has given properties
     */
    public boolean isEqual(String name, int argNumber, int rootId, boolean excludeRoot)
    {
        if (getName() == null)
        {
            return false;
        }
        if ((excludeRoot && getId() == rootId) || !getName().equals(name))
        {
            return false;
        }
        // argument number does not matter
        if (argNumber == ViewUtils.INVALID_INDEX)
        {
            return true;
        }
        // check argument number
        if (getArguments() != null && getArguments().size() == argNumber)
        {
            // normal function with arguments
            return true;
        }
        else if (getArguments() == null && argNumber == 0)
        {
            // a constant
            return true;
        }
        else if (isInterval() && argNumber == 1)
        {
            // an interval
            return true;
        }
        return false;
    }
}
