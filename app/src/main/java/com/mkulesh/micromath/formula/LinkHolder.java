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
import android.widget.LinearLayout;

import java.util.ArrayList;

public abstract class LinkHolder extends FormulaBase
{
    private final ArrayList<Equation> directIntervals = new ArrayList<>();
    private final ArrayList<Equation> allIntervals = new ArrayList<>();

    private final ArrayList<Equation> directFunctions = new ArrayList<>();
    private final ArrayList<Equation> allFunctions = new ArrayList<>();

    /*********************************************************
     * Constructors
     *********************************************************/

    public LinkHolder(FormulaList formulaList, LinearLayout layout, int termDepth)
    {
        super(formulaList, layout, termDepth);
    }

    /*********************************************************
     * GUI constructors to avoid lint warning
     *********************************************************/

    public LinkHolder(Context context)
    {
        super(null, null, 0);
    }

    /*********************************************************
     * Re-implementation for methods for FormulaBase superclass
     *********************************************************/

    @Override
    public boolean isContentValid(ValidationPassType type)
    {
        boolean isValid = true;
        switch (type)
        {
        case VALIDATE_SINGLE_FORMULA:
            directIntervals.clear();
            allIntervals.clear();
            directFunctions.clear();
            allFunctions.clear();
            isValid = super.isContentValid(type);
            break;
        case VALIDATE_LINKS:
            isValid = super.isContentValid(type);
            collectAllIntervals(null);
            collectAllFunctions(null);
            break;
        }
        return isValid;
    }

    /*********************************************************
     * LinkHolder-specific methods
     *********************************************************/

    /**
     * Procedure returns the list of directly linked intervals
     */
    public ArrayList<Equation> getDirectIntervals()
    {
        return directIntervals;
    }

    /**
     * Procedure returns the list of all linked intervals
     */
    public ArrayList<Equation> getAllIntervals()
    {
        return allIntervals;
    }

    /**
     * Procedure returns the list of all linked functions
     */
    public ArrayList<Equation> getAllFunctions()
    {
        return allFunctions;
    }

    /**
     * Procedure returns the list of indirectly linked intervals
     */
    public ArrayList<String> getIndirectIntervals()
    {
        ArrayList<String> retValue = new ArrayList<>();
        if (getDirectIntervals().size() != getAllIntervals().size())
        {
            for (Equation li : allIntervals)
            {
                if (!directIntervals.contains(li))
                {
                    retValue.add(li.getName());
                }
            }
        }
        return retValue;
    }

    /**
     * Procedure shall be called from a child term in order to inform this object that it depends on an interval or
     * function
     */
    public void addLinkedEquation(Equation linkedEquation)
    {
        if (linkedEquation == null)
        {
            return;
        }
        if (linkedEquation.isInterval() && !directIntervals.contains(linkedEquation))
        {
            directIntervals.add(linkedEquation);
        }
        else if (!linkedEquation.isInterval() && !directFunctions.contains(linkedEquation))
        {
            directFunctions.add(linkedEquation);
        }
    }

    /**
     * Procedure recursively collects linked intervals
     */
    protected ArrayList<Equation> collectAllIntervals(ArrayList<LinkHolder> callStack)
    {
        for (Equation li : directIntervals)
        {
            if (!allIntervals.contains(li))
            {
                allIntervals.add(li);
            }
        }

        // stack is used to prevent unlimited recursive calls
        ArrayList<LinkHolder> stack = new ArrayList<>();
        if (callStack != null)
        {
            stack.addAll(callStack);
        }
        stack.add(this);

        for (Equation e : directFunctions)
        {
            if (stack.contains(e))
            {
                continue;
            }
            ArrayList<Equation> tmpIntervals = e.collectAllIntervals(stack);
            for (Equation li : tmpIntervals)
            {
                if (!allIntervals.contains(li))
                {
                    allIntervals.add(li);
                }
            }
        }
        return allIntervals;
    }

    /**
     * Procedure recursively collects all linked functions
     */
    protected ArrayList<Equation> collectAllFunctions(ArrayList<LinkHolder> callStack)
    {
        // stack is used to prevent unlimited recursive calls
        ArrayList<LinkHolder> stack = new ArrayList<>();
        if (callStack != null)
        {
            stack.addAll(callStack);
        }
        stack.add(this);

        for (Equation e : directFunctions)
        {
            if (!allFunctions.contains(e))
            {
                allFunctions.add(e);
            }
            if (stack.contains(e))
            {
                continue;
            }
            ArrayList<Equation> tmpFunctions = e.collectAllFunctions(stack);
            for (Equation li : tmpFunctions)
            {
                if (!allFunctions.contains(li))
                {
                    allFunctions.add(li);
                }
            }
        }
        return allFunctions;
    }
}
