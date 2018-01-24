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
import android.view.View;
import android.widget.LinearLayout;

import com.mkulesh.micromath.formula.terms.CommonFunctions;
import com.mkulesh.micromath.formula.terms.Comparators;
import com.mkulesh.micromath.formula.terms.FileOperations;
import com.mkulesh.micromath.formula.terms.TermTypeIf;
import com.mkulesh.micromath.formula.terms.Intervals;
import com.mkulesh.micromath.formula.terms.LogFunctions;
import com.mkulesh.micromath.formula.terms.NumberFunctions;
import com.mkulesh.micromath.formula.terms.ObsoleteFunctionIf;
import com.mkulesh.micromath.formula.terms.Operators;
import com.mkulesh.micromath.formula.terms.SeriesIntegrals;
import com.mkulesh.micromath.formula.terms.TrigonometricFunctions;
import com.mkulesh.micromath.formula.terms.UserFunctions;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.DocumentProperties;
import com.mkulesh.micromath.utils.ClipboardManager;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomLayout;
import com.mkulesh.micromath.widgets.CustomTextView;
import com.mkulesh.micromath.widgets.FocusChangeIf;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class FormulaTerm extends FormulaBase implements CalculatableIf
{
    private final FormulaBase formulaRoot;
    protected CustomLayout functionMainLayout = null;
    protected TermTypeIf termType = null;
    protected boolean useBrackets = false;

    /*********************************************************
     * Constructors
     *********************************************************/

    public FormulaTerm(TermField owner, LinearLayout layout) throws Exception
    {
        super(owner.getFormulaRoot().getFormulaList(), layout, owner.termDepth);
        this.formulaRoot = owner.getFormulaRoot();
        setParentField(owner);
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
        return termType == null ? "" : termType.getLowerCaseName();
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
    public abstract TermTypeIf.GroupType getGroupType();

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

    public static void addToPalette(Context context, LinearLayout paletteLayout, TermTypeIf.GroupType gType)
    {
        switch (gType)
        {
        case OPERATORS:
            addToPalette(context, Operators.OperatorType.values(), paletteLayout,
                    new PaletteButton.Category[]{ PaletteButton.Category.CONVERSION });
            break;
        case COMPARATORS:
            addToPalette(context, Comparators.ComparatorType.values(), paletteLayout,
                    new PaletteButton.Category[]{ PaletteButton.Category.COMPARATOR });
            break;
        case FILE_OPERATIONS:
            addToPalette(context, FileOperations.FunctionType.values(), paletteLayout,
                    new PaletteButton.Category[]{ PaletteButton.Category.TOP_LEVEL_TERM });
            break;
        case COMMON_FUNCTIONS:
            addToPalette(context, CommonFunctions.FunctionType.values(), paletteLayout,
                    new PaletteButton.Category[]{ PaletteButton.Category.CONVERSION });
            break;
        case TRIGONOMETRIC_FUNCTIONS:
            addToPalette(context, TrigonometricFunctions.FunctionType.values(), paletteLayout,
                    new PaletteButton.Category[]{ PaletteButton.Category.CONVERSION });
            break;
        case LOG_FUNCTIONS:
            addToPalette(context, LogFunctions.FunctionType.values(), paletteLayout,
                    new PaletteButton.Category[]{ PaletteButton.Category.CONVERSION });
            break;
        case NUMBER_FUNCTIONS:
            addToPalette(context, NumberFunctions.FunctionType.values(), paletteLayout,
                    new PaletteButton.Category[]{ PaletteButton.Category.CONVERSION });
            break;
        case USER_FUNCTIONS:
            addToPalette(context, UserFunctions.FunctionType.values(), paletteLayout,
                    new PaletteButton.Category[]{ PaletteButton.Category.CONVERSION });
            break;
        case INTERVALS:
            addToPalette(context, Intervals.IntervalType.values(), paletteLayout,
                    new PaletteButton.Category[]{ PaletteButton.Category.TOP_LEVEL_TERM });
            break;
        case SERIES_INTEGRALS:
            addToPalette(context, SeriesIntegrals.LoopType.values(), paletteLayout,
                    new PaletteButton.Category[]{ PaletteButton.Category.CONVERSION });
            break;
        }
    }

    public static TermTypeIf getTermTypeIf(Context context, CustomEditText text, String s, boolean ensureManualTrigger)
    {
        final boolean containsBracket = s.contains(
                context.getResources().getString(R.string.formula_function_start_bracket));
        final boolean ensureBracket = !ensureManualTrigger || (ensureManualTrigger && containsBracket);
        {
            final TermTypeIf t = getGeneralFunctionType(context, s, Operators.OperatorType.values());
            if (t != null)
            {
                return t;
            }
        }
        {
            final boolean enableComparator = (text == null || text.isComparatorEnabled());
            final TermTypeIf t = getGeneralFunctionType(context, s, Comparators.ComparatorType.values());
            if (enableComparator && t != null)
            {
                return t;
            }
        }
        {
            // This function group has manual trigger ("("): is has to be checked
            final boolean enableFileOperation = (ensureBracket) && (text == null || text.isFileOperationEnabled());
            final TermTypeIf t = getGeneralFunctionType(context, s, FileOperations.FunctionType.values());
            if (enableFileOperation && t != null)
            {
                return t;
            }
        }
        {
            // This function group has manual trigger (like "("): is has to be checked
            final boolean enableFunction = !ensureManualTrigger ||
                    (ensureManualTrigger && (containsBracket ||
                            containsTermTrigger(context, s, CommonFunctions.FunctionType.values())));
            final TermTypeIf t = getGeneralFunctionType(context, s, CommonFunctions.FunctionType.values());
            if (enableFunction && t != null)
            {
                return t;
            }
        }
        {
            // This function group has manual trigger ("("): is has to be checked
            final TermTypeIf t = getGeneralFunctionType(context, s, TrigonometricFunctions.FunctionType.values());
            if (ensureBracket && t != null)
            {
                return t;
            }
        }
        {
            // This function group has manual trigger ("("): is has to be checked
            final TermTypeIf t = getGeneralFunctionType(context, s, LogFunctions.FunctionType.values());
            if (ensureBracket && t != null)
            {
                return t;
            }
        }
        {
            // This function group has manual trigger ("("): is has to be checked
            final TermTypeIf t = getGeneralFunctionType(context, s, NumberFunctions.FunctionType.values());
            if (ensureBracket && t != null)
            {
                return t;
            }
        }
        {
            // This function group has manual trigger (like "(" or "["): is has to be checked
            final boolean enableFunction = !ensureManualTrigger ||
                    (ensureManualTrigger && (containsBracket ||
                            containsTermTrigger(context, s, UserFunctions.FunctionType.values())));
            final UserFunctions.FunctionType t = UserFunctions.getFunctionType(context, s);
            if (enableFunction && t != null)
            {
                return t;
            }
        }
        {
            final boolean enableInterval = (text == null || text.isIntervalEnabled());
            final TermTypeIf t = getGeneralFunctionType(context, s, Intervals.IntervalType.values());
            if (enableInterval && t != null)
            {
                return t;
            }
        }
        {
            final TermTypeIf t = getGeneralFunctionType(context, s, SeriesIntegrals.LoopType.values());
            if (t != null)
            {
                return t;
            }
        }
        return null;
    }

    public static String getOperatorCode(Context context, String code, boolean ensureManualTrigger)
    {
        final TermTypeIf f = getTermTypeIf(context, null, code, ensureManualTrigger);
        return (f != null) ? f.getLowerCaseName() : null;
    }

    public static FormulaTerm createTerm(TermTypeIf type, TermField termField, LinearLayout layout, String s,
                                         int textIndex) throws Exception
    {
        if (textIndex < 0 || textIndex > layout.getChildCount())
        {
            throw new Exception("cannot create " + type.toString() + " for invalid insertion index " + textIndex);
        }
        switch (type.getGroupType())
        {
        case OPERATORS:
            return new Operators(type, termField, layout, s, textIndex);
        case COMPARATORS:
            return new Comparators(type, termField, layout, s, textIndex);
        case FILE_OPERATIONS:
            return new FileOperations(type, termField, layout, s, textIndex);
        case COMMON_FUNCTIONS:
            return new CommonFunctions(type, termField, layout, s, textIndex);
        case TRIGONOMETRIC_FUNCTIONS:
            return new TrigonometricFunctions(type, termField, layout, s, textIndex);
        case LOG_FUNCTIONS:
            return new LogFunctions(type, termField, layout, s, textIndex);
        case NUMBER_FUNCTIONS:
            return new NumberFunctions(type, termField, layout, s, textIndex);
        case USER_FUNCTIONS:
            return new UserFunctions(type, termField, layout, s, textIndex);
        case INTERVALS:
            return new Intervals(type, termField, layout, s, textIndex);
        case SERIES_INTEGRALS:
            return new SeriesIntegrals(type, termField, layout, s, textIndex);
        }
        return null;
    }

    public static String createOperatorCode(Context contex, String code, String prevText)
    {
        String newValue = null;
        final TermTypeIf f = getTermTypeIf(contex, null, code, false);
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
                final UserFunctions.FunctionType t1 = (UserFunctions.FunctionType) f;
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

    private static <T extends TermTypeIf> void addToPalette(
            Context context, T[] buttons, LinearLayout paletteLayout, PaletteButton.Category[] categories)
    {
        for (final TermTypeIf b : buttons)
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

    public static List<TermTypeIf.GroupType> collectPaletteGroups()
    {
        final List<TermTypeIf.GroupType> gTypes =
                Arrays.asList(TermTypeIf.GroupType.values());
        Collections.sort(gTypes, new Comparator<TermTypeIf.GroupType>()
        {
            @Override
            public int compare(TermTypeIf.GroupType lhs, TermTypeIf.GroupType rhs)
            {
                return lhs.getPaletteOrder() > rhs.getPaletteOrder() ? 1 :
                        (lhs.getPaletteOrder() < rhs.getPaletteOrder()) ? -1 : 0;
            }
        });
        return gTypes;
    }

    private static <T extends TermTypeIf> TermTypeIf getGeneralFunctionType(Context context, String s, T[] items)
    {
        final Resources res = context.getResources();

        // cat the function name
        final String startBracket = res.getString(R.string.formula_function_start_bracket);
        final String fName = s.contains(startBracket)? s.substring(0, s.indexOf(startBracket)).trim() : null;

        // search the function name in the types array
        for (TermTypeIf f : items)
        {
            if (s.equals(f.getLowerCaseName()))
            {
                return f;
            }
            if (fName != null && fName.equals(f.getLowerCaseName()))
            {
                return f;
            }
            // Compatibility mode: search the function name in the array of obsolete functions
            if (DocumentProperties.getDocumentVersion() != DocumentProperties.LATEST_DOCUMENT_VERSION &&
                    f instanceof ObsoleteFunctionIf)
            {
                final ObsoleteFunctionIf of = (ObsoleteFunctionIf) f;
                if (DocumentProperties.getDocumentVersion() <= of.getObsoleteVersion() &&
                        of.getObsoleteCode() != null &&
                        s.equals(of.getObsoleteCode()))
                {
                    return f;
                }
            }
        }

        // if function is not yet found, check the short-cuts
        for (TermTypeIf f : items)
        {
            if (f.getShortCutId() != Palette.NO_BUTTON && s.contains(res.getString(f.getShortCutId())))
            {
                return f;
            }
        }

        return null;
    }

    public static <T extends TermTypeIf> boolean containsTermTrigger(Context context, String s, T[] items)
    {
        for (TermTypeIf f : items)
        {
            if (f.getShortCutId() != Palette.NO_BUTTON &&
                    s.contains(context.getResources().getString(f.getShortCutId())))
            {
                return true;
            }
        }
        return false;
    }
}
