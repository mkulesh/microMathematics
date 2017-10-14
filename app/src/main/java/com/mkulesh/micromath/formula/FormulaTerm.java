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
import android.view.View;
import android.widget.LinearLayout;

import com.mkulesh.micromath.R;
import com.mkulesh.micromath.utils.ClipboardManager;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomTextView;
import com.mkulesh.micromath.widgets.FocusChangeIf;

import java.util.Locale;

public abstract class FormulaTerm extends FormulaBase implements CalculatableIf
{
    private final FormulaBase formulaRoot;

    enum TermType
    {
        OPERATOR,
        FUNCTION,
        INTERVAL
    }

    /*********************************************************
     * Constructors
     *********************************************************/

    public FormulaTerm(FormulaBase formulaRoot, LinearLayout layout, int termDepth)
    {
        super(formulaRoot.getFormulaList(), layout, termDepth);
        this.formulaRoot = formulaRoot;
    }

    public FormulaTerm()
    {
        super(null, null, 0);
        this.formulaRoot = null;
    }

    /*********************************************************
     * Re-implementation for methods for Object superclass
     *********************************************************/

    @Override
    public String toString()
    {
        return "Term " + getTermType().toString() + " " + getTermCode() + ", depth=" + termDepth;
    }

    @Override
    public BaseType getBaseType()
    {
        return BaseType.TERM;
    }

    /*********************************************************
     * Methods to be Implemented in derived a class
     *********************************************************/

    /**
     * Procedure returns the type of this term formula
     */
    public abstract TermType getTermType();

    /**
     * Procedure returns code of this term. The code must be unique for a given term type
     */
    public abstract String getTermCode();

    /**
     * Procedure will be called for a custom text view initialization
     */
    protected abstract CustomTextView initializeSymbol(CustomTextView v);

    /**
     * Procedure will be called for a custom edit term initialization
     */
    protected abstract CustomEditText initializeTerm(CustomEditText v, LinearLayout l);

    /*********************************************************
     * Implementation for methods for FormulaChangeIf interface
     *********************************************************/

    @Override
    public void onCopyToClipboard()
    {
        ClipboardManager.copyToClipboard(getContext(), ClipboardManager.CLIPBOARD_TERM_OBJECT);
        // the difference between this and super implementation: we should store additional term code for term:
        getFormulaList().setStoredFormula(new StoredFormula(getBaseType(), getTermCode(), onSaveInstanceState()));
    }

    @Override
    public boolean enableObjectProperties()
    {
        return false;
    }

    @Override
    public int getNextFocusId(CustomEditText owner, FocusChangeIf.NextFocusType focusType)
    {
        if (formulaRoot != null
                && owner != null
                && (focusType == FocusChangeIf.NextFocusType.FOCUS_UP || focusType == FocusChangeIf.NextFocusType.FOCUS_DOWN))
        {
            return formulaRoot.getNextFocusId(owner, focusType);
        }
        return super.getNextFocusId(owner, focusType);
    }

    /*********************************************************
     * FormulaTerm-specific methods
     *********************************************************/

    static public String getOperatorCode(Context contex, String code, boolean enableFunction)
    {
        FormulaTermOperator.OperatorType t1 = FormulaTermOperator.getOperatorType(contex, code);
        if (t1 != null)
        {
            return t1.toString().toLowerCase(Locale.ENGLISH);
        }
        FormulaTermFunction.FunctionType t3 = FormulaTermFunction.getFunctionType(contex, code);
        if (t3 != null && enableFunction)
        {
            return t3.toString().toLowerCase(Locale.ENGLISH);
        }
        FormulaTermInterval.IntervalType t4 = FormulaTermInterval.getIntervalType(contex, code);
        if (t4 != null)
        {
            return t4.toString().toLowerCase(Locale.ENGLISH);
        }
        return null;
    }

    public static FormulaTerm createTerm(TermType type, TermField termField, LinearLayout layout, String s,
                                         int textIndex) throws Exception
    {
        switch (type)
        {
        case OPERATOR:
            return new FormulaTermOperator(termField, layout, s, textIndex);
        case FUNCTION:
            return new FormulaTermFunction(termField, layout, s, textIndex);
        case INTERVAL:
            return new FormulaTermInterval(termField, layout, s, textIndex);
        }
        return null;
    }

    static public String createOperatorCode(Context contex, String code, String prevText)
    {
        String newValue = null;
        // operator
        final FormulaTermOperator.OperatorType t1 = FormulaTermOperator.getOperatorType(contex, code);
        if (t1 != null)
        {
            // for an operator, we add operator code to the end of line in order to move
            // existing text in the first term
            newValue = contex.getResources().getString(t1.getSymbolId());
            if (prevText != null)
            {
                newValue = prevText + newValue;
            }
        }
        // function
        final FormulaTermFunction.FunctionType t3 = FormulaTermFunction.getFunctionType(contex, code);
        if (newValue == null && t3 != null)
        {
            // for a function, we add operator code at the beginning of line in order to move
            // existing text in the function argument term
            newValue = (t3 == FormulaTermFunction.FunctionType.FUNCTION_LINK) ? code : t3.toString().toLowerCase(
                    Locale.ENGLISH);
            if (prevText != null)
            {
                if (t3 != FormulaTermFunction.FunctionType.FUNCTION_LINK)
                {
                    newValue += contex.getResources().getString(R.string.formula_function_start_bracket);
                }
                newValue += prevText;
            }
        }
        // interval
        final FormulaTermInterval.IntervalType t4 = FormulaTermInterval.getIntervalType(contex, code);
        if (newValue == null && t4 != null)
        {
            // for an interval, we add operator code at the beginning of line in order to move
            // existing text in the function argument term
            newValue = contex.getResources().getString(t4.getSymbolId());
            if (prevText != null)
            {
                newValue += prevText;
            }
        }
        return newValue;
    }

    /**
     * Getter for main term
     */
    public TermField getArgumentTerm()
    {
        return getTerms().size() > 0 ? getTerms().get(0) : null;
    }

    /**
     * Getter for parent root formula
     */
    public FormulaBase getFormulaRoot()
    {
        return formulaRoot;
    }

    /**
     * This procedure shall be called in order to prepare all visual elements
     */
    protected void initializeElements(int idx)
    {
        boolean[] isValid = new boolean[elements.size()];
        for (int i = 0; i < elements.size(); i++)
        {
            View v = elements.get(i);
            if (v instanceof CustomTextView)
            {
                isValid[i] = (initializeSymbol((CustomTextView) v) != null);
            }
            else if (v instanceof CustomEditText)
            {
                isValid[i] = (initializeTerm((CustomEditText) v, layout) != null);
            }
            else if (v instanceof LinearLayout)
            {
                initializeLayout((LinearLayout) v);
                isValid[i] = true;
            }
        }
        for (int i = elements.size() - 1; i >= 0; i--)
        {
            View v = elements.get(i);
            if (isValid[i])
            {
                layout.addView(v, idx);
            }
            else
            {
                elements.remove(v);
            }
        }
    }

    /**
     * This procedure performs recursive initialization of elements from included layouts
     */
    private void initializeLayout(LinearLayout l)
    {
        for (int k = 0; k < l.getChildCount(); k++)
        {
            View v = l.getChildAt(k);
            if (v instanceof CustomTextView)
            {
                initializeSymbol((CustomTextView) v);
            }
            if (v instanceof CustomEditText)
            {
                initializeTerm((CustomEditText) v, l);
            }
            if (v instanceof LinearLayout)
            {
                initializeLayout((LinearLayout) v);
            }
        }
    }
}
