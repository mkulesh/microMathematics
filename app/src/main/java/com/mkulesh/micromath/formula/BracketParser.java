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
package com.mkulesh.micromath.formula;

import android.content.Context;
import android.content.res.Resources;

import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.ViewUtils;

import java.util.ArrayList;

public class BracketParser
{
    public enum ParsingResult
    {
        NO_BRACKETS,
        PARSED_SUCCESSFULLY,
        PARSED_WITH_ERROR
    }

    /**
     * Brackets for function and array
     */
    public static final int FUNCTION_BRACKETS = 0;
    public static final int ARRAY_BRACKETS = 1;

    public static final int START_BRACKET_IDS[] = { R.string.formula_function_start_bracket,
            R.string.formula_function_start_index };
    public static final int END_BRACKET_IDS[] = { R.string.formula_function_end_bracket,
            R.string.formula_function_end_index };

    public int errorId = TermField.NO_ERROR_ID;
    public String name;
    public ArrayList<String> arguments;
    private int bracketsIndex = ViewUtils.INVALID_INDEX;

    public boolean isFunction()
    {
        return bracketsIndex == FUNCTION_BRACKETS;
    }

    public boolean isArray()
    {
        return bracketsIndex == ARRAY_BRACKETS;
    }

    public ParsingResult parse(String text, Resources resources)
    {
        setError(TermField.NO_ERROR_ID);

        bracketsIndex = ViewUtils.INVALID_INDEX;
        for (int i = 0; i < START_BRACKET_IDS.length; i++)
        {
            final int lbPosition = text.indexOf(resources.getString(START_BRACKET_IDS[i]));
            final int rbPosition = text.indexOf(resources.getString(END_BRACKET_IDS[i]));

            if (lbPosition < 0 && rbPosition < 0)
            {
                continue;
            }

            // check that there are no brackets of other type
            if (bracketsIndex != ViewUtils.INVALID_INDEX)
            {
                return setError(R.string.error_invalid_brackets_order);
            }

            // check that brackets are completed
            if ((lbPosition >= 0 && rbPosition < 0) || lbPosition < 0 && rbPosition >= 0)
            {
                return setError(R.string.error_brackets_not_completed);
            }

            // check that brackets have valid order
            if (lbPosition > rbPosition)
            {
                return setError(R.string.error_invalid_brackets_order);
            }

            // substract function name and arguments
            String fArgs = null;
            try
            {
                name = text.substring(0, lbPosition).trim();
                fArgs = text.substring(lbPosition + 1, rbPosition).trim();
            }
            catch (IndexOutOfBoundsException ex)
            {
                return setError(R.string.error_invalid_brackets_order);
            }

            if (fArgs.length() == 0)
            {
                return setError(R.string.error_empty_argument_list);
            }

            // check that name is valid
            if (name == null || name.length() == 0 || !isAlphaOrDigit(name))
            {
                return setError(R.string.error_invalid_variable_name);
            }

            // parse arguments
            arguments = getArcs(fArgs);
            if (arguments == null || arguments.isEmpty())
            {
                // getArcs sets the parsingErrorId in the case of an error
                return setError(errorId);
            }

            bracketsIndex = i;
        }

        return bracketsIndex == ViewUtils.INVALID_INDEX ? ParsingResult.NO_BRACKETS : ParsingResult.PARSED_SUCCESSFULLY;
    }

    private ParsingResult setError(int error)
    {
        errorId = error;
        bracketsIndex = ViewUtils.INVALID_INDEX;
        name = null;
        arguments = null;
        return ParsingResult.PARSED_WITH_ERROR;
    }

    private ArrayList<String> getArcs(String args)
    {
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
        if (tmpArgs.isEmpty())
        {
            tmpArgs = null;
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

    public static String removeBrackets(Context context, String s, int brIdx)
    {
        if (s != null)
        {
            final Resources res = context.getResources();
            final String endBracket = res.getString(BracketParser.END_BRACKET_IDS[brIdx]);
            if (s.contains(endBracket))
            {
                s = s.substring(0, s.lastIndexOf(endBracket)).trim();
            }
            final String startBracket = res.getString(BracketParser.START_BRACKET_IDS[brIdx]);
            if (s.contains(startBracket))
            {
                s = s.substring(s.indexOf(startBracket) + startBracket.length(), s.length());
            }
        }
        return s;
    }
}
