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

import com.mkulesh.micromath.math.CalculatedValue;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CustomEditText;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;

import javax.measure.DecimalMeasure;
import javax.measure.Measure;
import javax.measure.unit.Unit;

public class TermParser
{
    private CalculatedValue value = null;
    private String functionName = null;
    private ArrayList<String> functionArgs = null;
    private ArgumentHolderIf argumentHolder = null;
    private int argumentIndex = ViewUtils.INVALID_INDEX, linkedVariableId = -1;
    private double sign = 1.0;
    private boolean isArray = false;
    private Unit unit = null;
    private Pair<String, String> unitTags = null;

    public int errorId = TermField.NO_ERROR_ID;

    public static final String CONST_NAN = "NaN";
    public static final String CONST_INF = "∞";
    private static final String CONST_E = "e";
    private static final String CONST_PI1 = "π";
    private static final String CONST_PI2 = "pi";
    private static final String IMAGINARY_UNIT = "i";
    private static final String POSITIVE_SIGN = "+";
    private static final String NEGATIVE_SIGN = "-";
    public static final String UNIT_SEPARATOR = " ";

    public TermParser()
    {
        // empty
    }

    @Override
    public String toString()
    {
        return "value=" + (value == null ? "empty" : value.toString()) +
                ", functionName=" + (functionName == null ? "empty" : functionName) +
                ", functionArgs=" + (functionArgs == null ? "empty" : functionArgs.toString()) +
                ", argumentHolder=" + (argumentHolder == null ? "empty" : argumentHolder.toString()) +
                ", argumentIndex=" + argumentIndex +
                ", linkedVariableId=" + linkedVariableId +
                ", sign=" + sign +
                ", isArray=" + isArray +
                ", unit=" + (unit == null ? "empty" : unit.toString());
    }

    public CalculatedValue getValue()
    {
        return value;
    }

    public String getFunctionName()
    {
        return functionName;
    }

    public ArrayList<String> getFunctionArgs()
    {
        return functionArgs;
    }

    public ArgumentHolderIf getArgumentHolder()
    {
        return argumentHolder;
    }

    public int getArgumentIndex()
    {
        return argumentIndex;
    }

    public int getLinkedVariableId()
    {
        return linkedVariableId;
    }

    public double getSign()
    {
        return sign;
    }

    public boolean isArray()
    {
        return isArray;
    }

    public Unit getUnit()
    {
        return unit;
    }

    public Pair<String, String> getUnitTags()
    {
        return unitTags;
    }

    public void setText(TermField owner, FormulaBase formulaRoot, CustomEditText editText)
    {
        String inText = editText.getText().toString();
        value = null;
        functionName = null;
        functionArgs = null;
        argumentHolder = null;
        argumentIndex = ViewUtils.INVALID_INDEX;
        linkedVariableId = -1;
        sign = 1.0;
        isArray = false;
        unit = null;
        unitTags = null;
        errorId = TermField.NO_ERROR_ID;
        if (inText == null || inText.length() == 0)
        {
            return;
        }

        // For the file names, no additional check
        if (editText.isFileName())
        {
            return;
        }

        String text = inText.trim();

        // check for forbidden content
        if (CONST_NAN.equals(text) || CONST_INF.equals(text))
        {
            errorId = R.string.error_nan_value;
            return;
        }
        if ((editText.isIndexName() || editText.isEquationName()) && IMAGINARY_UNIT.equals(text))
        {
            errorId = R.string.error_forbidden_imaginary_unit;
            return;
        }

        // check units
        if (text.contains(UNIT_SEPARATOR))
        {
            final int sepPos = text.indexOf(UNIT_SEPARATOR);
            final String valuePart = text.substring(0, sepPos).trim();
            final String unitPart = text.substring(sepPos + UNIT_SEPARATOR.length()).trim();
            unit = parseUnits(unitPart);
            if (unit != null)
            {
                unitTags = new Pair<>(valuePart, unitPart);
                text = valuePart;
            }
        }

        // check if is a valid double value
        try
        {
            value = new CalculatedValue(CalculatedValue.ValueType.REAL, Double.parseDouble(text), 0.0);
            return;
        }
        catch (Exception ex)
        {
            value = null;
            // nothing to do: we will try to convert it to the function name
        }

        // check if is a valid complex value
        try
        {
            Complex cmplValue = complexValueOf(text);
            if (cmplValue != null)
            {
                if (!editText.isComplexEnabled())
                {
                    errorId = R.string.error_forbidden_complex;
                    return;
                }
                if (cmplValue.getImaginary() != 0.0)
                {
                    value = new CalculatedValue(CalculatedValue.ValueType.COMPLEX, cmplValue.getReal(),
                            cmplValue.getImaginary());
                }
                else
                {
                    value = new CalculatedValue(CalculatedValue.ValueType.REAL, cmplValue.getReal(), 0.0);
                }
                return;
            }
        }
        catch (Exception ex)
        {
            value = null;
            // nothing to do: we will try to convert it to the function name
        }

        // check for the sign
        if (text.startsWith("-"))
        {
            sign = -1.0;
            text = text.substring(1).trim();
        }

        // check if it is a constant
        if (CONST_E.equals(text))
        {
            value = new CalculatedValue(CalculatedValue.ValueType.REAL, sign * Math.E, 0.0);
            sign = +1.0;
            return;
        }
        else if (CONST_PI1.equals(text) || CONST_PI2.equals(text))
        {
            value = new CalculatedValue(CalculatedValue.ValueType.REAL, sign * Math.PI, 0.0);
            sign = +1.0;
            return;
        }

        // check if it is a function name
        BracketParser brPars = new BracketParser();
        switch (brPars.parse(text, formulaRoot.getContext().getResources()))
        {
        case NO_BRACKETS:
            functionName = text;
            break;
        case PARSED_SUCCESSFULLY:
            functionName = brPars.name;
            functionArgs = brPars.arguments;
            isArray = brPars.isArray();
            break;
        case PARSED_WITH_ERROR:
            errorId = brPars.errorId;
            return;
        }

        if (functionName != null)
        {
            // check for argument index
            if (checkArgumentIndex(owner, editText))
            {
                // found a valid argument
                return;
            }
            else if (errorId != TermField.NO_ERROR_ID)
            {
                // found an erroneous argument
                return;
            }

            // check for index name
            if (editText.isIndexName() && functionArgs != null)
            {
                // error: found a field that contains argument but it shall be a index
                errorId = R.string.error_forbidden_arguments;
                return;
            }

            // check for equation name
            if (editText.isEquationName())
            {
                final Equation fb = formulaRoot.searchLinkedEquation(functionName, Equation.ARG_NUMBER_ANY);
                if (fb != null && !formulaRoot.getFormulaList().getDocumentSettings().redefineAllowed)
                {
                    // error: we found an other equation with the same name as this equation definition:
                    // it is forbidden
                    errorId = R.string.error_duplicated_identifier;
                }
                else
                {
                    // this equation definition contains a valid and unique name
                }
                return;
            }

            // check the link to a variable
            final Equation fVar = formulaRoot.searchLinkedEquation(
                    functionName, Equation.ARG_NUMBER_ARRAY_OR_CONSTANT);
            if (fVar != null)
            {
                if (fVar.isArray() && editText.getArrayType() == CustomEditText.ArrayType.DISABLED)
                {
                    errorId = R.string.error_forbidden_array;
                    return;
                }
                linkedVariableId = fVar.getId();
                if (linkedVariableId >= 0)
                {
                    // we found a link to the valid constant or array
                    return;
                }
            }
        }

        // try to convert term as a pure unit
        {
            unit = parseUnits(text);
            if (unit != null)
            {
                unitTags = new Pair<>("", text);
                value = new CalculatedValue(CalculatedValue.ValueType.REAL, 1.0, 0.0);
                return;
            }
        }

        errorId = R.string.error_unknown_variable;
    }

    public static Unit parseUnits(final String text)
    {
        if (text == null || text.isEmpty())
        {
            return null;
        }
        try
        {
            // There are two different symbols μ: standard and from greek keyboard.
            // Replace keyboard symbol by the standard one:
            final String unitText = text.replace(/*from Greek Small Letter Mu*/ 'μ', /*to Micro sign*/ 'µ');
            final Measure conv = DecimalMeasure.valueOf("1" + UNIT_SEPARATOR + unitText);
            if (conv != null && conv.getUnit() != null)
            {
                return conv.getUnit();
            }
        }
        catch (Exception ex)
        {
            // nothing to do
        }
        return null;
    }

    private boolean checkArgumentIndex(TermField owner, CustomEditText editText)
    {
        argumentHolder = owner.findArgumentHolder(functionName);
        // no argument holder is found
        if (argumentHolder == null || !(argumentHolder instanceof FormulaBase))
        {
            return false;
        }
        if (editText.isEquationName())
        {
            // get the parent holder in the case of an equation name
            final TermField parentHolderTerm = ((FormulaBase) argumentHolder).getParentField();
            argumentHolder = (parentHolderTerm != null) ? parentHolderTerm.findArgumentHolder(functionName) : null;
            if (argumentHolder != null)
            {
                errorId = R.string.error_duplicated_identifier;
            }
            argumentHolder = null;
            return false;
        }
        if (editText.isIntermediateArgument())
        {
            // get the parent holder in the case of an intermediate argument
            final TermField parentHolderTerm = ((FormulaBase) argumentHolder).getParentField();
            argumentHolder = (parentHolderTerm != null) ? parentHolderTerm.findArgumentHolder(functionName) : null;
            if (argumentHolder == null)
            {
                return false;
            }
        }
        // obtain argument index
        argumentIndex = argumentHolder.getArgumentIndex(functionName);
        if (argumentIndex == ViewUtils.INVALID_INDEX)
        {
            // should never happen since findArgumentHolder already checks the argument index
            argumentHolder = null;
        }
        return (argumentHolder != null && argumentIndex != ViewUtils.INVALID_INDEX);
    }

    public boolean isArgumentInHolder(String var)
    {
        if (argumentHolder != null)
        {
            final ArrayList<String> args = argumentHolder.getArguments();
            if (args != null && getArgumentIndex() >= 0 && getArgumentIndex() < args.size())
            {
                final String arg = args.get(getArgumentIndex());
                if (var != null && arg != null)
                {
                    return var.equals(arg);
                }
            }
        }
        return false;
    }

    public static Complex complexValueOf(String text)
    {
        // text shall contain imaginary unit
        if (text == null || !text.contains(IMAGINARY_UNIT))
        {
            return null;
        }

        // imaginary unit shall be the last character
        final int unitPos = text.indexOf(IMAGINARY_UNIT);
        if (unitPos != text.length() - 1)
        {
            return null;
        }

        // search for +/- sign before imaginary unit
        int signPos = text.lastIndexOf(POSITIVE_SIGN);
        if (signPos < 0)
        {
            signPos = text.lastIndexOf(NEGATIVE_SIGN);
        }
        if (signPos < 0)
        {
            signPos = 0;
        }

        // split real and imaginary part
        String rePart = "", imPart = "";
        try
        {
            rePart = (signPos > 0) ? text.substring(0, signPos) : "0.0";
            imPart = (unitPos > signPos) ? text.substring(signPos, unitPos) : "1.0";
            if (imPart.equals(POSITIVE_SIGN) || imPart.equals(NEGATIVE_SIGN))
            {
                imPart += "1.0";
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            return null;
        }

        // convert both parts
        try
        {
            return new Complex(Double.parseDouble(rePart), Double.parseDouble(imPart));
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}
