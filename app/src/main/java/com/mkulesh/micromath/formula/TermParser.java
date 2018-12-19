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

import com.mkulesh.micromath.R;
import com.mkulesh.micromath.properties.DocumentProperties;
import com.mkulesh.micromath.utils.ViewUtils;

import java.util.ArrayList;

public class TermParser
{
    private Double value = null;
    private String functionName = null;
    private ArrayList<String> functionArgs = null;
    private int argumentIndex = -1, linkedVariableId = -1;
    private double sign = 1.0;

    public int errorId = TermField.NO_ERROR_ID;

    public static final String CONST_NAN = "NaN";
    public static final String CONST_INF = "∞";
    public static final String CONST_E = "e";
    public static final String CONST_PI1 = "π";
    public static final String CONST_PI2 = "pi";
    public static final String IMAGINARY_UNIT = "i";

    public boolean ensureEquationName = false;

    public TermParser()
    {
        // empty
    }

    public Double getValue()
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

    public void setText(FormulaBase formulaRoot, String inText)
    {
        value = null;
        functionName = null;
        functionArgs = null;
        argumentIndex = -1;
        linkedVariableId = -1;
        sign = 1.0;
        errorId = TermField.NO_ERROR_ID;
        if (inText == null || inText.length() == 0)
        {
            return;
        }

        String text = inText.trim();

        // check for the sign
        if (text.startsWith("-"))
        {
            sign = -1.0;
            text = text.substring(1).trim();
        }

        // check for forbidden content
        if (CONST_NAN.equals(text) || CONST_INF.equals(text))
        {
            errorId = R.string.error_nan_value;
            return;
        }
        if (IMAGINARY_UNIT.equals(text))
        {
            errorId = R.string.error_forbidden_imaginary_unit;
            return;
        }

        // check if is a valid double value
        try
        {
            value = Double.parseDouble(text);
            return;
        }
        catch (Exception ex)
        {
            value = null;
            // nothing to do: we will try to convert it to the function name
        }

        // check if it is a constant
        if (CONST_E.equals(text))
        {
            value = Math.E;
            return;
        }
        else if (CONST_PI1.equals(text) || CONST_PI2.equals(text))
        {
            value = Math.PI;
            return;
        }

        // check if it is a function name
        String fName = null;
        ArrayList<String> fArgs = null;
        if ((text.contains("(") && !text.contains(")")) || (text.contains(")") && !text.contains("(")))
        {
            errorId = R.string.error_brackets_not_completed;
            return;
        }
        else if (!text.contains("(") && !text.contains(")"))
        {
            fName = text;
        }
        else
        {
            fArgs = getArcs(text);
            if (fArgs == null || fArgs.isEmpty())
            {
                // getArcs sets the parsingErrorId in the case of an error
                return;
            }
            try
            {
                fName = text.substring(0, text.indexOf("(")).trim();
            }
            catch (IndexOutOfBoundsException ex)
            {
                errorId = R.string.error_invalid_variable_name;
                return;
            }
        }
        if (fName == null || fName.length() == 0 || !isAlphaOrDigit(fName))
        {
            errorId = R.string.error_invalid_variable_name;
            return;
        }
        functionName = fName;
        functionArgs = fArgs;

        // check for argument index
        if (functionName != null && formulaRoot instanceof Equation)
        {
            argumentIndex = ((Equation) formulaRoot).isArgument(functionName) ? 0 : -1;
        }

        // check for equation definition and linked formula
        if (functionName != null && argumentIndex < 0)
        {
            // check for equation name
            if (ensureEquationName)
            {
                final FormulaBase fb = formulaRoot.getFormulaList().getFormula(functionName, ViewUtils.INVALID_INDEX,
                        formulaRoot.getId(), true);
                // check for argument number
                if (functionArgs != null && functionArgs.size() > 1)
                {
                    // error: more than one argument is not allowed in this version
                    errorId = R.string.error_ensure_single_argument;
                }
                else if (fb != null && !formulaRoot.getFormulaList().getDocumentSettings().redefineAllowed)
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
            final FormulaBase fb = formulaRoot.getFormulaList().getFormula(functionName, 0, formulaRoot.getId(), true);
            if (fb != null && fb instanceof Equation)
            {
                ArrayList<String> args = ((Equation) fb).getArguments();
                if (args == null || args.isEmpty())
                {
                    linkedVariableId = fb.getId();
                    if (linkedVariableId >= 0)
                    {
                        // we found a link to the valid constant
                        return;
                    }
                }
            }
        }

        if (argumentIndex < 0 && linkedVariableId < 0)
        {
            errorId = R.string.error_unknown_variable;
        }
    }

    private ArrayList<String> getArcs(String text)
    {
        final int lbPosition = text.indexOf("(");
        final int rbPosition = text.indexOf(")");
        if (lbPosition > rbPosition)
        {
            errorId = R.string.error_invalid_brackets_order;
            return null;
        }
        String args = null;
        try
        {
            args = text.substring(lbPosition + 1, rbPosition);
        }
        catch (IndexOutOfBoundsException ex)
        {
            errorId = R.string.error_invalid_brackets_order;
            return null;
        }
        if (args.length() == 0)
        {
            errorId = R.string.error_empty_argument_list;
            return null;
        }
        ArrayList<String> tmpArgs = new ArrayList<String>();
        while (true)
        {
            int cmPosition = args.indexOf(",");
            if (cmPosition < 0)
            {
                tmpArgs.add(args.trim());
                break;
            }
            try
            {
                tmpArgs.add(args.substring(0, cmPosition).trim());
                args = args.substring(cmPosition + 1, args.length());
            }
            catch (IndexOutOfBoundsException ex)
            {
                errorId = R.string.error_invalid_comma_position;
                return null;
            }
        }
        for (String s : tmpArgs)
        {
            if (s == null || s.length() == 0)
            {
                errorId = R.string.error_empty_argument;
                return null;
            }
            if (!isAlphaOrDigit(s))
            {
                errorId = R.string.error_invalid_argument;
                return null;
            }

        }
        return tmpArgs;
    }

    private boolean isAlphaOrDigit(String name)
    {
        final char[] chars = name.toCharArray();
        for (char c : chars)
        {
            if (!Character.isLetterOrDigit(c))
            {
                return false;
            }
        }
        return true;
    }

    public static boolean isInvalidReal(double v)
    {
        return Double.isNaN(v) || Double.isInfinite(v);
    }

    public static String doubleToString(double v, DocumentProperties doc)
    {
        if (Double.isNaN(v))
        {
            return CONST_NAN;
        }
        if (Double.isInfinite(v))
        {
            return (v < 0) ? "-" + CONST_INF : CONST_INF;
        }
        final double roundV = ViewUtils.roundToNumberOfSignificantDigits(v, doc.significantDigits);
        return Double.toString(roundV);
    }
}
