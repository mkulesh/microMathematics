/*
 * microMathematics - Extended Visual Calculator
 * Copyright (C) 2014-2022 by Mikhail Kulesh
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
package com.mkulesh.micromath.formula;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;

import com.mkulesh.micromath.dialogs.DialogResultDetails;
import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.formula.TermField.ErrorNotification;
import com.mkulesh.micromath.formula.terms.Intervals;
import com.mkulesh.micromath.formula.terms.UserFunctions;
import com.mkulesh.micromath.math.CalculatedValue;
import com.mkulesh.micromath.math.EquationArrayResult;
import com.mkulesh.micromath.R;
import com.mkulesh.micromath.properties.MatrixProperties;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomLayout;
import com.mkulesh.micromath.widgets.CustomTextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;

public class Equation extends CalculationResult implements ArgumentHolderIf, CalculatableIf
{
    public static final int ARG_NUMBER_ANY = -1;
    public static final int ARG_NUMBER_INTERVAL = Integer.MAX_VALUE - 1;
    public static final int ARG_NUMBER_ARRAY_OR_CONSTANT = Integer.MAX_VALUE;

    private TermField leftTerm = null;
    private TermField rightTerm = null;
    private CalculatedValue[] argumentValues = null;

    /*--------------------------------------------------------*
     * Constant result
     *--------------------------------------------------------*/

    private class EquationConstantResult
    {
        private CalculatedValue value = null;

        CalculatedValue getValue(CalculaterTask thread) throws CancelException
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

    /*--------------------------------------------------------*
     * Constructors
     *--------------------------------------------------------*/

    public Equation(FormulaList formulaList, int id)
    {
        super(formulaList, null, 0);
        setId(id);
        onCreate();
    }

    /*--------------------------------------------------------*
     * GUI constructors to avoid lint warning
     *--------------------------------------------------------*/

    public Equation(Context context)
    {
        super(null, null, 0);
    }

    public Equation(Context context, AttributeSet attrs)
    {
        super(null, null, 0);
    }

    /*--------------------------------------------------------*
     * Re-implementation for methods for Object superclass
     *--------------------------------------------------------*/

    @NonNull
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
        if (isIntervalTerm())
        {
            n += ": interval";
        }
        else if (isArray())
        {
            n += ": array";
        }
        return "Formula " + getBaseType().toString() + "(Id: " + getId() + ", Name: " + n + ")";
    }

    /*--------------------------------------------------------*
     * Re-implementation for methods for FormulaBase superclass
     *--------------------------------------------------------*/

    @Override
    public BaseType getBaseType()
    {
        return BaseType.EQUATION;
    }

    @Override
    public boolean enableObjectProperties()
    {
        return rightTerm != null && rightTerm.isTerm() && rightTerm.getTerm().enableObjectProperties();
    }

    @Override
    public void onObjectProperties(View owner)
    {
        if (enableObjectProperties())
        {
            rightTerm.getTerm().onObjectProperties(rightTerm.getTerm());
        }
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
            if (!isValid)
            {
                break;
            }
            // check that the equation is an interval
            if (isIntervalTerm())
            {
                arrayResult = new EquationArrayResult(this, null);
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
                    final CustomLayout termLayout = leftTerm.isTerm() ?
                            leftTerm.getTerm().getFunctionMainLayout() : null;
                    leftTerm.setError(errorMsg,
                            termLayout != null ? ErrorNotification.PARENT_LAYOUT : ErrorNotification.LAYOUT_BORDER,
                            termLayout);
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
        return linkedIntervals.isEmpty() && (arguments == null || arguments.isEmpty()) && !rightTerm.isArray();
    }

    private String checkArrayResult()
    {
        final Resources res = getContext().getResources();

        final ArrayList<String> arguments = getArguments();
        if (arguments == null)
        {
            // no arguments are expected when right term holds a matrix
            return null;
        }

        if (!arguments.isEmpty() && rightTerm.isArray() && TermField.isArrayTerm(rightTerm.getTerm().getTermCode()))
        {
            return getContext().getResources().getString(R.string.error_forbidden_arguments);
        }

        if (arguments.size() > EquationArrayResult.MAX_DIMENSION)
        {
            // error: invalid array dimension
            return String.format(res.getString(R.string.error_invalid_array_dimension),
                    Integer.toString(arguments.size()));
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

        // check that all arguments are valid intervals or integer constants
        for (String s : arguments)
        {
            final Integer numIndex = CalculatedValue.toInteger(s);
            if (numIndex != null && numIndex >= 0)
            {
                continue;
            }
            final Equation f = searchLinkedEquation(s, ARG_NUMBER_INTERVAL);
            if (f == null || !f.isInterval())
            {
                // error: index not an interval
                return String.format(res.getString(R.string.error_invalid_array_index), s);
            }
        }
        return null;
    }

    /*--------------------------------------------------------*
     * Implementation of ArgumentHolderIf interface
     *--------------------------------------------------------*/

    @Override
    public ArrayList<String> getArguments()
    {
        if (leftTerm.getIndexTerm() != null)
        {
            final ArrayList<String> arrArgs = new ArrayList<>();
            for (TermField t : leftTerm.getTerm().getTerms())
            {
                arrArgs.add(t.getText());
            }
            return arrArgs;
        }
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

    public CalculatedValue[] getArgumentValues()
    {
        return argumentValues;
    }


    /*--------------------------------------------------------*
     * Re-implementation for methods for Calculatable interface
     *--------------------------------------------------------*/

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

    /*--------------------------------------------------------*
     * Equation-specific methods
     *--------------------------------------------------------*/

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
        if (isIntervalTerm())
        {
            ((Intervals) rightTerm.getTerm()).getInterval(arrayResult, thread);
        }
        else
        {
            fileOperation(true);
            final MatrixProperties mp = rightTerm.getArrayDimension();
            if (getArguments() == null && mp != null)
            {
                final ArrayList<String> arguments = new ArrayList<>();
                arguments.add(String.valueOf(mp.rows));
                arguments.add(String.valueOf(mp.cols));
                arrayResult.calculate(thread, arguments, true, null);
            }
            else
            {
                arrayResult.calculate(thread, getArguments(), false, findPreviousArray());
            }
            fileOperation(false);
        }
    }

    private void fileOperation(boolean status)
    {
        // For premium version only
    }

    @Override
    public void showResult()
    {
        // empty
    }

    @Override
    public boolean enableDetails()
    {
        return arrayResult != null && arrayResult.isArray1D();
    }

    @Override
    public void onDetails()
    {
        if (enableDetails())
        {
            DialogResultDetails d = new DialogResultDetails(getFormulaList().getActivity(),
                    arrayResult,
                    getFormulaList().getDocumentSettings(), null);
            d.show();
        }
    }

    /*--------------------------------------------------------*
     * Equation-specific methods
     *--------------------------------------------------------*/

    /**
     * Procedure creates the formula layout
     */
    private void onCreate()
    {
        inflateRootLayout(R.layout.formula_equation, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        // create name term
        {
            CustomEditText v = layout.findViewById(R.id.formula_equation_name);
            leftTerm = addTerm(this, layout, v, this, false);
        }
        // create assign character
        {
            CustomTextView v = layout.findViewById(R.id.formula_equation_assign);
            v.prepare(CustomTextView.SymbolType.TEXT, getFormulaList().getActivity(), this);
        }
        // create value term
        {
            CustomEditText v = layout.findViewById(R.id.formula_equation_value);
            rightTerm = addTerm(this, layout, v, this, false);
            rightTerm.bracketsType = TermField.BracketsType.NEVER;
        }
    }

    /**
     * Procedure returns the parsed name of this formula
     */
    public String getName()
    {
        final UserFunctions indexTerm = leftTerm.getIndexTerm();
        return indexTerm != null ? indexTerm.getFunctionLabel() : leftTerm.getParser().getFunctionName();
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
        return isArray() || isIntervalTerm();
    }

    private boolean isIntervalTerm()
    {
        return (rightTerm.getTerm() != null && rightTerm.getTerm() instanceof Intervals);
    }

    /**
     * Procedure checks whether this root formula represents an array
     */
    public boolean isArray()
    {
        return leftTerm.getIndexTerm() != null || leftTerm.getParser().isArray() || rightTerm.isArray();
    }

    public int[] getArrayDimensions()
    {
        return arrayResult != null ? arrayResult.getDimensions() : null;
    }

    public EquationArrayResult getArrayResult()
    {
        return arrayResult;
    }

    /**
     * Procedure returns declared interval if this root formula represents an interval
     */
    public final CalculatedValue[] getInterval() throws CancelException
    {
        if (isInterval() && arrayResult != null && arrayResult.isArray1D())
        {
            return arrayResult.getRawValues();
        }
        return null;
    }

    /**
     * Procedure fills the given value array and array with minimum and maximum values from this interval
     */
    public double[] fillBoundedInterval(double[] targetValues, double[] minMaxValues)
            throws CancelException
    {
        if (!isInterval() || minMaxValues == null || minMaxValues.length != 2)
        {
            return null;
        }
        final CalculatedValue[] arr = getInterval();
        if (arr == null)
        {
            return null;
        }
        ArrayList<Double> newArr = new ArrayList<>();
        for (CalculatedValue calculatedValue : arr)
        {
            final double v = calculatedValue.getReal();
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
        if (argNumber == ARG_NUMBER_ANY)
        {
            return true;
        }
        // check argument number
        if (getArguments() != null && getArguments().size() == argNumber)
        {
            // normal function with arguments
            return true;
        }
        else if (isInterval() && argNumber == ARG_NUMBER_INTERVAL)
        {
            // an interval
            return true;
        }
        // an array or constant
        else
        {
            return argNumber == ARG_NUMBER_ARRAY_OR_CONSTANT && (isArray() || getArguments() == null);
        }
    }

    private EquationArrayResult findPreviousArray()
    {
        if (!isArray())
        {
            // this equation is not an array
            return null;
        }
        Equation prevArray = null;
        final ArrayList<Equation> eqList = getFormulaList().getFormulaListView().getFormulas(Equation.class);
        for (Equation eq : eqList)
        {
            if (eq == this)
            {
                break;
            }
            if (!eq.isArray())
            {
                continue;
            }
            if (eq.getArguments() != null &&
                    isEqual(eq.getName(), eq.getArguments().size(), eq.getId(), true))
            {
                prevArray = eq;
            }
            if (eq.getArguments() == null && eq.rightTerm.getArrayDimension() != null &&
                    isEqual(eq.getName(), eq.rightTerm.getArrayDimension().getDimension(), eq.getId(), true))
            {
                prevArray = eq;
            }
        }
        return (prevArray != null) ? prevArray.arrayResult : null;
    }
}
