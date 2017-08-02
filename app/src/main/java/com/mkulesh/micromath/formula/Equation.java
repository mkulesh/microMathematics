/*******************************************************************************
 * micro Mathematics - Extended visual calculator
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
package com.mkulesh.micromath.formula;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;

import com.mkulesh.micromath.dialogs.DialogResultDetails;
import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.formula.TermField.ErrorNotification;
import com.mkulesh.micromath.math.ArgumentValueItem;
import com.mkulesh.micromath.math.CalculatedValue;
import com.mkulesh.micromath.math.EquationArrayResult;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomTextView;

public class Equation extends CalculationResult implements ArgumentHolderIf, CalculatableIf
{
    private TermField leftTerm = null;
    private TermField rightTerm = null;
    private CalculatedValue[] argumentValues = null;

    /*********************************************************
     * Constant result
     *********************************************************/

    private class EquationConstantResult
    {
        private CalculatedValue value = null;

        public CalculatedValue getValue(CalculaterTask thread) throws CancelException
        {
            if (value == null)
            {
                value = new CalculatedValue();
                rightTerm.getValue(thread, value);
            }
            return value;
        }
    }

    private EquationConstantResult constantResult = null;
    private EquationArrayResult arrayResult = null;

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

    @Override
    public boolean enableObjectProperties()
    {
        return false;
    }

    @Override
    public boolean isContentValid(ValidationPassType type)
    {
        boolean isValid = super.isContentValid(type);
        constantResult = null;
        arrayResult = null;

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
            if (!isValid || isInterval())
            {
                break;
            }
            // check that the equation result can be cached
            if (isConstantResult())
            {
                constantResult = new EquationConstantResult();
                break;
            }
            // check that the equation can be calculated as an array
            if (isArray())
            {
                final String errorMsg = checkArrayResult();
                if (errorMsg == null)
                {
                    arrayResult = new EquationArrayResult(this, rightTerm);
                }
                else
                {
                    leftTerm.setError(errorMsg, ErrorNotification.LAYOUT_BORDER, null);
                }
            }
            break;
        }
        return isValid;
    }

    private boolean isConstantResult()
    {
        final ArrayList<Equation> linkedIntervals = getAllIntervals();
        final ArrayList<String> arguments = getArguments();
        return linkedIntervals.isEmpty() && (arguments == null || arguments.isEmpty());
    }

    private String checkArrayResult()
    {
        final Resources res = getContext().getResources();

        final ArrayList<String> arguments = getArguments();
        if (arguments.size() > EquationArrayResult.MAX_DIMENSION)
        {
            // error: invalid array dimension
            return String.format(res.getString(R.string.error_invalid_array_dimension), Integer.toString(200));
        }

        // Linked intervals are not allowed since all indexed variables in the right part
        // will be defined by term parser as arguments but not as a linked variables 
        final ArrayList<Equation> linkedIntervals = getAllIntervals();
        for (Equation e : linkedIntervals)
        {
            if (!arguments.contains(e.getName()))
            {
                // error: interval is not defined as index
                return String.format(res.getString(R.string.error_invalid_array_interval), e.getName());
            }
        }

        // check that all arguments are valid intervals
        for (String s : arguments)
        {
            FormulaBase f = getFormulaList().getFormula(s, 0, getId(), true);
            if (f == null || !(f instanceof Equation) || !((Equation) f).isInterval())
            {
                // error: index not an interval
                return String.format(res.getString(R.string.error_invalid_array_index), s);
            }
        }
        return null;
    }

    /*********************************************************
     * Implementation of ArgumentHolderIf interface
     *********************************************************/

    @Override
    public ArrayList<String> getArguments()
    {
        return leftTerm.getParser().getFunctionArgs();
    }

    @Override
    public int getArgumentIndex(String text)
    {
        if (text != null && getArguments() != null)
        {
            return getArguments().indexOf(text);
        }
        return ViewUtils.INVALID_INDEX;
    }

    @Override
    public CalculatedValue getArgumentValue(int idx)
    {
        if (argumentValues != null && idx < argumentValues.length && argumentValues[idx] != null)
        {
            return argumentValues[idx];
        }
        return CalculatedValue.NaN;
    }

    /*********************************************************
     * Re-implementation for methods for Calculatable interface
     *********************************************************/

    @Override
    public CalculatedValue.ValueType getValue(CalculaterTask thread, CalculatedValue outValue) throws CancelException
    {
        if (constantResult != null && argumentValues == null)
        {
            return outValue.assign(constantResult.getValue(thread));
        }
        else if (arrayResult != null && argumentValues != null)
        {
            return outValue.assign(arrayResult.getValue(argumentValues));
        }
        return rightTerm.getValue(thread, outValue);
    }

    @Override
    public DifferentiableType isDifferentiable(String var)
    {
        return rightTerm.isDifferentiable(var);
    }

    @Override
    public CalculatedValue.ValueType getDerivativeValue(String var, CalculaterTask thread, CalculatedValue outValue)
            throws CancelException
    {
        return rightTerm.getDerivativeValue(var, thread, outValue);
    }

    /*********************************************************
     * Equation-specific methods
     *********************************************************/

    @Override
    public void invalidateResult()
    {
        arrayResult = null;
    }

    @Override
    public void calculate(CalculaterTask thread) throws CancelException
    {
        if (arrayResult == null)
        {
            return;
        }
        arrayResult.calculate(thread, getArguments());
    }

    @Override
    public void showResult()
    {
        // empty
    }

    @Override
    public boolean enableDetails()
    {
        return arrayResult != null && arrayResult.getDimNumber() == 1 && arrayResult.getRawValues() != null;
    }

    @Override
    public void onDetails(View owner)
    {
        if (!enableDetails())
        {
            return;
        }

        final CalculatedValue[] values = arrayResult.getRawValues();
        final int n = values.length;
        ArrayList<ArgumentValueItem> calculatedItems = new ArrayList<ArgumentValueItem>(n);
        for (int i = 0; i < n; i++)
        {
            if (values[i] != null)
            {
                final ArgumentValueItem item = new ArgumentValueItem();
                item.argument.setValue(i);
                item.value.assign(values[i]);
                calculatedItems.add(item);
            }
        }
        DialogResultDetails d = new DialogResultDetails(getFormulaList().getActivity(), calculatedItems,
                getFormulaList().getDocumentSettings());
        d.show();
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
     * Procedure sets the list of argument values
     */
    public boolean setArgumentValues(CalculatedValue[] argumentValues)
    {
        this.argumentValues = argumentValues;
        return this.argumentValues != null;
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
     * Procedure checks whether this root formula represents an array
     */
    public boolean isArray()
    {
        return leftTerm.getParser().isArray();
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
    public double[] fillBoundedInterval(CalculaterTask thread, double[] targetValues, double[] minMaxValues)
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
        ArrayList<Double> newArr = new ArrayList<Double>();
        for (int i = 0; i < arr.size(); i++)
        {
            final double v = arr.get(i);
            if (minMaxValues[1] != Double.POSITIVE_INFINITY && v > minMaxValues[1])
            {
                break;
            }
            if ((minMaxValues[0] != Double.NEGATIVE_INFINITY && v >= minMaxValues[0])
                    || minMaxValues[0] == Double.NEGATIVE_INFINITY)
            {
                newArr.add(v);
            }
        }
        double[] retValues = (targetValues != null && targetValues.length == newArr.size()) ? targetValues
                : new double[newArr.size()];
        minMaxValues[0] = minMaxValues[1] = Double.NaN;
        for (int i = 0; i < retValues.length; i++)
        {
            final double v = newArr.get(i);
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
