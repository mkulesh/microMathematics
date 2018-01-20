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

import com.mkulesh.micromath.formula.terms.CommonFunctions;
import com.mkulesh.micromath.formula.terms.Comparators;
import com.mkulesh.micromath.formula.terms.FileOperations;
import com.mkulesh.micromath.formula.terms.FunctionBase;
import com.mkulesh.micromath.formula.terms.Intervals;
import com.mkulesh.micromath.formula.terms.LogFunctions;
import com.mkulesh.micromath.formula.terms.NumberFunctions;
import com.mkulesh.micromath.formula.terms.Operators;
import com.mkulesh.micromath.formula.terms.SeriesIntegrals;
import com.mkulesh.micromath.formula.terms.TrigonometricFunctions;
import com.mkulesh.micromath.formula.terms.UserFunctions;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.ClipboardManager;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomLayout;
import com.mkulesh.micromath.widgets.CustomTextView;
import com.mkulesh.micromath.widgets.FocusChangeIf;

import java.util.ArrayList;

public abstract class FormulaTerm extends FormulaBase implements CalculatableIf
{
    private final FormulaBase formulaRoot;
    protected CustomLayout functionMainLayout = null;
    protected FormulaTermTypeIf termType = null;
    protected boolean useBrackets = false;

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
     * Common getters
     *********************************************************/

    @Override
    public String toString()
    {
        return "Term " + getTermCode() + ", depth=" + termDepth;
    }

    @Override
    public BaseType getBaseType()
    {
        return BaseType.TERM;
    }

    /**
     * Returns code of this term. The code must be unique for a given term type
     */
    public String getTermCode()
    {
        return termType == null? "" : termType.getLowerCaseName();
    }

    /**
     * Returns the first term, if present
     */
    public TermField getArgumentTerm()
    {
        return getTerms().size() > 0 ? getTerms().get(0) : null;
    }

    /**
     * Returns the parent root formula
     */
    public FormulaBase getFormulaRoot()
    {
        return formulaRoot;
    }

    /**
     * Returns whether the brackets are used
     */
    public boolean isUseBrackets()
    {
        return useBrackets;
    }

    /*********************************************************
     * Methods to be Implemented in derived a class
     *********************************************************/

    /**
     * Returns term type
     */
    public abstract FormulaTermTypeIf.GroupType getGroupType();

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
     * Factory methods
     *********************************************************/

    public static void addToPalette(Context context, LinearLayout paletteLayout)
    {
        addToPalette(context, Intervals.IntervalType.values(), paletteLayout,
                new PaletteButton.Category[]{ PaletteButton.Category.TOP_LEVEL_TERM });
        addToPalette(context, Operators.OperatorType.values(), paletteLayout,
                new PaletteButton.Category[]{ PaletteButton.Category.CONVERSION });
        addToPalette(context, UserFunctions.FunctionType.values(), paletteLayout,
                new PaletteButton.Category[]{ PaletteButton.Category.CONVERSION });
        addToPalette(context, CommonFunctions.FunctionType.values(), paletteLayout,
                new PaletteButton.Category[]{ PaletteButton.Category.CONVERSION });
        addToPalette(context, TrigonometricFunctions.FunctionType.values(), paletteLayout,
                new PaletteButton.Category[]{ PaletteButton.Category.CONVERSION });
        addToPalette(context, LogFunctions.FunctionType.values(), paletteLayout,
                new PaletteButton.Category[]{ PaletteButton.Category.CONVERSION });
        addToPalette(context, NumberFunctions.FunctionType.values(), paletteLayout,
                new PaletteButton.Category[]{ PaletteButton.Category.CONVERSION });
        addToPalette(context, FileOperations.FunctionType.values(), paletteLayout,
                new PaletteButton.Category[]{ PaletteButton.Category.TOP_LEVEL_TERM });
        addToPalette(context, SeriesIntegrals.LoopType.values(), paletteLayout,
                new PaletteButton.Category[]{ PaletteButton.Category.CONVERSION });
        addToPalette(context, Comparators.ComparatorType.values(), paletteLayout,
                new PaletteButton.Category[]{ PaletteButton.Category.COMPARATOR });
    }

    public static FormulaTermTypeIf getTermTypeIf(Context context, CustomEditText text, String s, boolean ensureManualTrigger)
    {
        {
            final Operators.OperatorType t = Operators.getOperatorType(context, s);
            if (t != null)
            {
                return t;
            }
        }
        {
            final boolean enableComparator = (text == null || text.isComparatorEnabled());
            final Comparators.ComparatorType t = Comparators.getComparatorType(context, s);
            if (enableComparator && t != null)
            {
                return t;
            }
        }
        {
            // This function group has manual trigger ("("): is has to be checked
            final boolean enableFileOperation = (!ensureManualTrigger ||
                    (ensureManualTrigger && FunctionBase.containsGeneralTrigger(context, s))) &&
                    (text == null || text.isFileOperationEnabled());
            final FileOperations.FunctionType t = FileOperations.getFunctionType(context, s);
            if (enableFileOperation && t != null)
            {
                return t;
            }
        }
        {
            // This function group has manual trigger (like "("): is has to be checked
            final boolean enableFunction = !ensureManualTrigger ||
                    (ensureManualTrigger && CommonFunctions.containsTrigger(context, s));
            final CommonFunctions.FunctionType t = CommonFunctions.getFunctionType(context, s);
            if (enableFunction && t != null)
            {
                return t;
            }
        }
        {
            // This function group has manual trigger ("("): is has to be checked
            final boolean enableFunction = !ensureManualTrigger ||
                    (ensureManualTrigger && FunctionBase.containsGeneralTrigger(context, s));
            final TrigonometricFunctions.FunctionType t = TrigonometricFunctions.getFunctionType(context, s);
            if (enableFunction && t != null)
            {
                return t;
            }
        }
        {
            // This function group has manual trigger ("("): is has to be checked
            final boolean enableFunction = !ensureManualTrigger ||
                    (ensureManualTrigger && FunctionBase.containsGeneralTrigger(context, s));
            final LogFunctions.FunctionType t = LogFunctions.getFunctionType(context, s);
            if (enableFunction && t != null)
            {
                return t;
            }
        }
        {
            // This function group has manual trigger ("("): is has to be checked
            final boolean enableFunction = !ensureManualTrigger ||
                    (ensureManualTrigger && FunctionBase.containsGeneralTrigger(context, s));
            final NumberFunctions.FunctionType t = NumberFunctions.getFunctionType(context, s);
            if (enableFunction && t != null)
            {
                return t;
            }
        }
        {
            // This function group has manual trigger (like "(" or "["): is has to be checked
            final boolean enableFunction = !ensureManualTrigger ||
                    (ensureManualTrigger && UserFunctions.containsTrigger(context, s));
            final UserFunctions.FunctionType t = UserFunctions.getFunctionType(context, s);
            if (enableFunction && t != null)
            {
                return t;
            }
        }
        {
            final boolean enableInterval = (text == null || text.isIntervalEnabled());
            final Intervals.IntervalType t = Intervals.getIntervalType(context, s);
            if (enableInterval && t != null)
            {
                return t;
            }
        }
        {
            final SeriesIntegrals.LoopType t = SeriesIntegrals.getLoopType(context, s);
            if (t != null)
            {
                return t;
            }
        }
        return null;
    }

    public static String getOperatorCode(Context context, String code, boolean ensureManualTrigger)
    {
        final FormulaTermTypeIf f = getTermTypeIf(context, null, code, ensureManualTrigger);
        return (f != null)? f.getLowerCaseName() : null;
    }

    public static FormulaTerm createTerm(FormulaTermTypeIf.GroupType type, TermField termField, LinearLayout layout, String s,
                                         int textIndex) throws Exception
    {
        switch (type)
        {
        case OPERATORS:
            return new Operators(termField, layout, s, textIndex);
        case COMPARATORS:
            return new Comparators(termField, layout, s, textIndex);
        case FILE_OPERATIONS:
            return new FileOperations(termField, layout, s, textIndex);
        case COMMON_FUNCTIONS:
            return new CommonFunctions(termField, layout, s, textIndex);
        case TRIGONOMETRIC_FUNCTIONS:
            return new TrigonometricFunctions(termField, layout, s, textIndex);
        case LOG_FUNCTIONS:
            return new LogFunctions(termField, layout, s, textIndex);
        case NUMBER_FUNCTIONS:
            return new NumberFunctions(termField, layout, s, textIndex);
        case USER_FUNCTIONS:
            return new UserFunctions(termField, layout, s, textIndex);
        case INTERVALS:
            return new Intervals(termField, layout, s, textIndex);
        case SERIES_INTEGRALS:
            return new SeriesIntegrals(termField, layout, s, textIndex);
        }
        return null;
    }

    public static String createOperatorCode(Context contex, String code, String prevText)
    {
        String newValue = null;
        final FormulaTermTypeIf f = getTermTypeIf(contex, null, code, false);
        if (f != null)
        {
            switch (f.getGroupType())
            {
            case OPERATORS:
                // for an operator, we add operator code to the end of line in order to move
                // existing text in the first term
                newValue = contex.getResources().getString(f.getShortCutId());
                if (prevText != null)
                {
                    newValue = prevText + newValue;
                }
                break;
            case COMPARATORS:
                // for a comparator, we add operator code to the end of line in order to move
                // existing text in the first term
                newValue = contex.getResources().getString(f.getShortCutId());
                if (prevText != null)
                {
                    newValue = prevText + newValue;
                }
                break;
            case FILE_OPERATIONS:
                // for the file operation, we do not transfer previous text
                newValue = f.getLowerCaseName() +
                        contex.getResources().getString(R.string.formula_function_start_bracket);
                break;
            case COMMON_FUNCTIONS:
            case TRIGONOMETRIC_FUNCTIONS:
            case LOG_FUNCTIONS:
            case NUMBER_FUNCTIONS:
                // for a function, we add operator code at the beginning of line in order to move
                // existing text in the function argument term
                newValue = f.getLowerCaseName();
                if (prevText != null)
                {
                    newValue += contex.getResources().getString(R.string.formula_function_start_bracket);
                    newValue += prevText;
                }
                break;
            case USER_FUNCTIONS:
                // for a function, we add operator code at the beginning of line in order to move
                // existing text in the function argument term
                final UserFunctions.FunctionType t1 = (UserFunctions.FunctionType)f;
                newValue = (t1 == UserFunctions.FunctionType.FUNCTION_LINK) ? code : f.getLowerCaseName();
                if (prevText != null)
                {
                    if (t1 != UserFunctions.FunctionType.FUNCTION_LINK)
                    {
                        newValue += contex.getResources().getString(R.string.formula_function_start_bracket);
                    }
                    newValue += prevText;
                }
                break;
            case INTERVALS:
                // for an interval, we add operator code at the beginning of line in order to move
                // existing text in the function argument term
                newValue = contex.getResources().getString(f.getShortCutId());
                if (prevText != null)
                {
                    newValue += prevText;
                }
                break;
            case SERIES_INTEGRALS:
                // for a loop, we add operator code at the beginning of line in order to move
                // existing text in the function argument term
                newValue = contex.getResources().getString(f.getShortCutId());
                if (prevText != null)
                {
                    newValue += prevText;
                }
                break;
            }
        }
        return newValue;
    }

    /*********************************************************
     * FormulaTerm-specific methods
     *********************************************************/

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

    /**
     * Procedure adds new argument layout for this function
     */
    protected TermField addArgument(TermField startField, int argLayoutId, int addDepth)
    {
        // target layout where terms will be added
        View expandable = startField.getLayout();
        if (expandable == null)
        {
            return null;
        }
        LinearLayout expandableLayout = (LinearLayout) expandable;

        // view index of the field within the target layout and within the terms vector
        int viewIndex = -1;
        if (startField.isTerm())
        {
            ArrayList<View> list = new ArrayList<>();
            startField.getTerm().collectElemets(expandableLayout, list);
            for (View l : list)
            {
                viewIndex = Math.max(viewIndex, ViewUtils.getViewIndex(expandableLayout, l));
            }
        }
        else
        {
            viewIndex = ViewUtils.getViewIndex(expandableLayout, startField.getEditText());
        }
        int termIndex = terms.indexOf(startField);
        if (viewIndex < 0 || termIndex < 0)
        {
            return null;
        }

        // collect terms to be added
        ArrayList<View> newTerms = new ArrayList<>();
        inflateElements(newTerms, argLayoutId, true);
        TermField newArg = null;
        for (View t : newTerms)
        {
            if (t instanceof CustomTextView)
            {
                ((CustomTextView) t).prepare(CustomTextView.SymbolType.TEXT, getFormulaRoot().getFormulaList()
                        .getActivity(), this);
            }
            else if (t instanceof CustomEditText)
            {
                newArg = addTerm(getFormulaRoot(), expandableLayout, ++termIndex, (CustomEditText) t, this, addDepth);
                newArg.bracketsType = TermField.BracketsType.NEVER;
            }
            expandableLayout.addView(t, ++viewIndex);
        }
        reIndexTerms();
        return newArg;
    }

    /**
     * Procedure deletes argument layout for given term and returns the previous term
     */
    protected TermField deleteArgument(TermField owner, String sep, boolean storeUndoState)
    {
        // target layout where terms will be deleted
        View expandable = owner.getLayout();
        if (expandable == null)
        {
            return null;
        }
        LinearLayout expandableLayout = (LinearLayout) expandable;

        // view index of the field within the parent layout
        int startIndex = ViewUtils.getViewIndex(expandableLayout, owner.getEditText());
        if (startIndex < 0)
        {
            return null;
        }

        // how much views shall be deleted:
        int count = 1;
        {
            final String termKey = getContext().getResources().getString(R.string.formula_arg_term_key);
            final boolean firstTerm = owner.getTermKey().equals(termKey + String.valueOf(1));
            if (firstTerm && startIndex + 1 < expandableLayout.getChildCount()
                    && expandableLayout.getChildAt(startIndex + 1) instanceof CustomTextView)
            {
                final CustomTextView next = ((CustomTextView) expandableLayout.getChildAt(startIndex + 1));
                if (next.getText().toString().equals(sep))
                {
                    count++;
                }
            }
            else if (!firstTerm && startIndex >= 1
                    && expandableLayout.getChildAt(startIndex - 1) instanceof CustomTextView)
            {
                final CustomTextView prev = ((CustomTextView) expandableLayout.getChildAt(startIndex - 1));
                if (prev.getText().toString().equals(sep))
                {
                    startIndex--;
                    count++;
                }
            }
        }

        if (storeUndoState && parentField != null)
        {
            getFormulaList().getUndoState().addEntry(parentField.getState());
        }
        int prevIndex = terms.indexOf(owner);
        prevIndex--;
        terms.remove(owner);
        expandableLayout.removeViews(startIndex, count);
        reIndexTerms();

        return (prevIndex >= 0) ? terms.get(prevIndex) : null;
    }

    /**
     * Procedure performs re-index of terms
     */
    private void reIndexTerms()
    {
        if (terms.size() == 1)
        {
            terms.get(0).setTermKey(getContext().getResources().getString(R.string.formula_arg_term_key));
        }
        else
        {
            int i = 1;
            for (TermField t : terms)
            {
                t.setTermKey(getContext().getResources().getString(R.string.formula_arg_term_key) + String.valueOf(i++));
            }
        }
    }

    /**
     * Check whether this term depends on given equation
     */
    public boolean dependsOn(Equation e)
    {
        for (TermField t : terms)
        {
            if (t.dependsOn(e))
            {
                return true;
            }
        }
        return false;
    }

    protected void initializeMainLayout()
    {
        // store the main layout in order to show errors
        final String functionMainTag = getContext().getResources().getString(R.string.function_main_layout);
        final View functionMainView = layout.findViewWithTag(functionMainTag);
        if (functionMainView != null)
        {
            functionMainLayout = (CustomLayout) functionMainView;
            functionMainLayout.setTag("");
        }
    }

    private static <T extends FormulaTermTypeIf> void addToPalette(
            Context context, T[] buttons, LinearLayout paletteLayout, PaletteButton.Category[] categories)
    {
        for (final FormulaTermTypeIf b : buttons)
        {
            if (b.getImageId() != Palette.NO_BUTTON)
            {
                PaletteButton p = new PaletteButton(context,
                        b.getShortCutId(), b.getImageId(), b.getDescriptionId(),
                        b.getLowerCaseName());
                paletteLayout.addView(p);
                p.setCategories(categories);
            }
        }
    }
}
