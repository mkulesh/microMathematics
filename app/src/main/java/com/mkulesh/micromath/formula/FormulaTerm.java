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

import android.view.View;
import android.widget.LinearLayout;

import com.mkulesh.micromath.formula.terms.TermTypeIf;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.ClipboardManager;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomLayout;
import com.mkulesh.micromath.widgets.CustomTextView;
import com.mkulesh.micromath.widgets.FocusChangeIf;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

public abstract class FormulaTerm extends FormulaBase implements CalculatableIf
{
    private final FormulaBase formulaRoot;
    protected CustomLayout functionMainLayout = null;
    protected TermTypeIf termType = null;
    protected boolean useBrackets = false;

    /*--------------------------------------------------------*
     * Constructors
     *--------------------------------------------------------*/

    protected FormulaTerm(TermField owner, LinearLayout layout) throws Exception
    {
        super(owner.getFormulaRoot().getFormulaList(), layout, owner.termDepth);
        this.formulaRoot = owner.getFormulaRoot();
        setParentField(owner);
    }

    protected FormulaTerm()
    {
        super(null, null, 0);
        this.formulaRoot = null;
    }

    /*--------------------------------------------------------*
     * Common getters
     *--------------------------------------------------------*/

    @NonNull
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

    /*--------------------------------------------------------*
     * Methods to be Implemented in derived a class
     *--------------------------------------------------------*/

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

    /*--------------------------------------------------------*
     * Implementation for methods for FormulaChangeIf interface
     *--------------------------------------------------------*/

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

    /*--------------------------------------------------------*
     * FormulaTerm-specific methods
     *--------------------------------------------------------*/

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
        LinearLayout expandable = startField.getLayout();
        if (expandable == null)
        {
            return null;
        }

        // view index of the field within the target layout and within the terms vector
        int viewIndex = -1;
        if (startField.isTerm())
        {
            ArrayList<View> list = new ArrayList<>();
            startField.getTerm().collectElemets(expandable, list);
            for (View l : list)
            {
                viewIndex = Math.max(viewIndex, ViewUtils.getViewIndex(expandable, l));
            }
        }
        else
        {
            viewIndex = ViewUtils.getViewIndex(expandable, startField.getEditText());
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
                newArg = addTerm(getFormulaRoot(), expandable, ++termIndex, (CustomEditText) t, this, addDepth);
                newArg.bracketsType = TermField.BracketsType.NEVER;
            }
            expandable.addView(t, ++viewIndex);
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
        LinearLayout expandable = owner.getLayout();
        if (expandable == null)
        {
            return null;
        }

        // view index of the field within the parent layout
        int startIndex = ViewUtils.getViewIndex(expandable, owner.getEditText());
        if (startIndex < 0)
        {
            return null;
        }

        // how much views shall be deleted:
        int count = 1;
        {
            final String termKey = getContext().getResources().getString(R.string.formula_arg_term_key);
            final boolean firstTerm = owner.getTermKey().equals(termKey + 1);
            if (firstTerm && startIndex + 1 < expandable.getChildCount()
                    && expandable.getChildAt(startIndex + 1) instanceof CustomTextView)
            {
                final CustomTextView next = ((CustomTextView) expandable.getChildAt(startIndex + 1));
                if (next.getText().toString().equals(sep))
                {
                    count++;
                }
            }
            else if (!firstTerm && startIndex >= 1
                    && expandable.getChildAt(startIndex - 1) instanceof CustomTextView)
            {
                final CustomTextView prev = ((CustomTextView) expandable.getChildAt(startIndex - 1));
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
        expandable.removeViews(startIndex, count);
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
                t.setTermKey(getContext().getResources().getString(R.string.formula_arg_term_key) + i++);
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

    protected boolean splitIntoTerms(final String src, final TermTypeIf t, boolean pasteFromClipboard)
    {
        if (src == null || src.isEmpty() || (t != null && t.getLowerCaseName().equals(src.toLowerCase(Locale.ENGLISH))))
        {
            return false;
        }
        try
        {
            String sep = "";
            int sepPosition = -1;
            if (t != null)
            {
                if (pasteFromClipboard)
                {
                    sep = getContext().getResources().getString(R.string.formula_term_separator);
                    sepPosition = src.indexOf(sep);
                }
                if (sepPosition < 0 && t.getShortCutId() != Palette.NO_BUTTON)
                {
                    sep = getContext().getResources().getString(t.getShortCutId());
                    sepPosition = src.indexOf(sep);
                }
            }
            if (sepPosition < 0)
            {
                // no separator found: put whole text into the first term
                final TermField term = getArgumentTerm();
                if (term != null)
                {
                    term.setText(src);
                    return true;
                }
            }
            else
            {
                final String[] args = src.split(Pattern.quote(sep));
                boolean isChanged = false;
                for (int i = 0, j = 0; i < args.length && j < terms.size(); i++, j++)
                {
                    if (args[i] != null && args[i].length() > 0)
                    {
                        terms.get(j).setText(args[i]);
                        isChanged = true;
                    }
                    else if (terms.size() < args.length)
                    {
                        j--;
                    }
                }
                return isChanged;
            }
        }
        catch (Exception ex)
        {
            // nothing to do
        }
        return false;
    }

    CustomLayout getFunctionMainLayout()
    {
        return functionMainLayout;
    }
}
