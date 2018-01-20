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
package com.mkulesh.micromath.formula.terms;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.mkulesh.micromath.formula.CalculaterTask;
import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.formula.FormulaTerm;
import com.mkulesh.micromath.formula.FormulaTermTypeIf;
import com.mkulesh.micromath.formula.TermField;
import com.mkulesh.micromath.formula.TermField.BracketsType;
import com.mkulesh.micromath.math.CalculatedValue;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomTextView;

import java.util.Locale;

public class Comparators extends FormulaTerm
{
    public FormulaTermTypeIf.GroupType getGroupType()
    {
        return FormulaTermTypeIf.GroupType.COMPARATORS;
    }

    /**
     * Supported comparators
     */
    public enum ComparatorType implements FormulaTermTypeIf
    {
        EQUAL(R.string.formula_comparator_equal, R.drawable.p_comparator_equal, R.string.math_comparator_equal),
        NOT_EQUAL(
                R.string.formula_comparator_not_equal,
                R.drawable.p_comparator_not_equal,
                R.string.math_comparator_not_equal),
        LESS(R.string.formula_comparator_less, R.drawable.p_comparator_less, R.string.math_comparator_less),
        LESS_EQUAL(
                R.string.formula_comparator_less_eq,
                R.drawable.p_comparator_less_eq,
                R.string.math_comparator_less_eq),
        GREATER(R.string.formula_comparator_greater, R.drawable.p_comparator_greater, R.string.math_comparator_greater),
        GREATER_EQUAL(
                R.string.formula_comparator_greater_eq,
                R.drawable.p_comparator_greater_eq,
                R.string.math_comparator_greater_eq),
        COMPARATOR_AND(R.string.formula_comparator_and, R.drawable.p_comparator_and, R.string.math_comparator_and),
        COMPARATOR_OR(R.string.formula_comparator_or, R.drawable.p_comparator_or, R.string.math_comparator_or);

        private final int shortCutId;
        private final int imageId;
        private final int descriptionId;
        private final String lowerCaseName;

        ComparatorType(int shortCutId, int imageId, int descriptionId)
        {
            this.shortCutId = shortCutId;
            this.imageId = imageId;
            this.descriptionId = descriptionId;
            this.lowerCaseName = name().toLowerCase(Locale.ENGLISH);
        }

        public GroupType getGroupType() { return GroupType.COMPARATORS; }

        public int getShortCutId()
        {
            return shortCutId;
        }

        public int getImageId()
        {
            return imageId;
        }

        public int getDescriptionId()
        {
            return descriptionId;
        }

        public String getLowerCaseName()
        {
            return lowerCaseName;
        }
    }

    public static ComparatorType getComparatorType(Context context, String s)
    {
        ComparatorType retValue = null;
        for (ComparatorType f : ComparatorType.values())
        {
            if (s.equals(f.getLowerCaseName())
                    || s.contains(context.getResources().getString(f.getShortCutId())))
            {
                retValue = f;
                break;
            }
        }
        return retValue;
    }

    /**
     * Private attributes
     */
    private TermField leftTerm = null, rightTerm = null;
    private CustomTextView operatorKey = null;

    // Attention: this is not thread-safety declaration!
    private final CalculatedValue leftTermValue = new CalculatedValue(), rightTermValue = new CalculatedValue();

    /*********************************************************
     * Constructors
     *********************************************************/

    public Comparators(TermField owner, LinearLayout layout, String s, int idx) throws Exception
    {
        super(owner.getFormulaRoot(), layout, owner.termDepth);
        setParentField(owner);
        onCreate(s, idx, owner.bracketsType);
    }

    /*********************************************************
     * GUI constructors to avoid lint warning
     *********************************************************/

    public Comparators(Context context)
    {
        super();
    }

    public Comparators(Context context, AttributeSet attrs)
    {
        super();
    }

    /*********************************************************
     * Common getters
     *********************************************************/

    public ComparatorType getComparatorType()
    {
        return (ComparatorType) termType;
    }

    /*********************************************************
     * Re-implementation for methods for FormulaBase and FormulaTerm superclass's
     *********************************************************/

    @Override
    public CalculatedValue.ValueType getValue(CalculaterTask thread, CalculatedValue outValue) throws CancelException
    {
        if (termType != null)
        {
            leftTerm.getValue(thread, leftTermValue);
            rightTerm.getValue(thread, rightTermValue);
            // Do not check invalid value since a comparator can handle it!
            switch (getComparatorType())
            {
            case EQUAL:
                return outValue.setValue((leftTermValue.getReal() == rightTermValue.getReal()) ? 1 : -1);
            case NOT_EQUAL:
                return outValue.setValue((leftTermValue.getReal() != rightTermValue.getReal()) ? 1 : -1);
            case LESS:
                return outValue.setValue((leftTermValue.getReal() < rightTermValue.getReal()) ? 1 : -1);
            case LESS_EQUAL:
                return outValue.setValue((leftTermValue.getReal() <= rightTermValue.getReal()) ? 1 : -1);
            case GREATER:
                return outValue.setValue((leftTermValue.getReal() > rightTermValue.getReal()) ? 1 : -1);
            case GREATER_EQUAL:
                return outValue.setValue((leftTermValue.getReal() >= rightTermValue.getReal()) ? 1 : -1);
            case COMPARATOR_AND:
                return outValue.setValue((leftTermValue.getReal() > 0 && rightTermValue.getReal() > 0) ? 1 : -1);
            case COMPARATOR_OR:
                return outValue.setValue((leftTermValue.getReal() > 0 || rightTermValue.getReal() > 0) ? 1 : -1);
            }
        }
        return outValue.invalidate(CalculatedValue.ErrorType.TERM_NOT_READY);
    }

    @Override
    public DifferentiableType isDifferentiable(String var)
    {
        return DifferentiableType.NONE;
    }

    @Override
    public CalculatedValue.ValueType getDerivativeValue(String var, CalculaterTask thread, CalculatedValue outValue)
            throws CancelException
    {
        return outValue.invalidate(CalculatedValue.ErrorType.TERM_NOT_READY);
    }

    @Override
    protected CustomTextView initializeSymbol(CustomTextView v)
    {
        if (v.getText() != null)
        {
            String t = v.getText().toString();
            if (t.equals(getContext().getResources().getString(R.string.formula_operator_key)))
            {
                operatorKey = v;
                v.prepare(CustomTextView.SymbolType.TEXT, getFormulaRoot().getFormulaList().getActivity(), this);
                updateOperatorKey();
            }
            else if (t.equals(getContext().getResources().getString(R.string.formula_left_bracket_key)))
            {
                v.prepare(CustomTextView.SymbolType.LEFT_BRACKET, getFormulaRoot().getFormulaList().getActivity(), this);
                v.setText("."); // this text defines view width/height
            }
            else if (t.equals(getContext().getResources().getString(R.string.formula_right_bracket_key)))
            {
                v.prepare(CustomTextView.SymbolType.RIGHT_BRACKET, getFormulaRoot().getFormulaList().getActivity(),
                        this);
                v.setText("."); // this text defines view width/height
            }
        }
        return v;
    }

    @Override
    protected CustomEditText initializeTerm(CustomEditText v, LinearLayout l)
    {
        if (v.getText() != null)
        {
            if (v.getText().toString().equals(getContext().getResources().getString(R.string.formula_left_term_key)))
            {
                leftTerm = addTerm(getFormulaRoot(), l, v, this, false);
            }
            if (v.getText().toString().equals(getContext().getResources().getString(R.string.formula_right_term_key)))
            {
                rightTerm = addTerm(getFormulaRoot(), l, v, this, false);
            }
        }
        return v;
    }

    /*********************************************************
     * Implementation for methods for FormulaChangeIf interface
     *********************************************************/

    @Override
    public void onDelete(CustomEditText owner)
    {
        if (parentField != null)
        {
            TermField t = findTerm(owner);
            TermField r = null;
            if (t != null)
            {
                r = (t == leftTerm) ? rightTerm : leftTerm;
            }
            parentField.onTermDelete(removeElements(), r);
        }
        getFormulaRoot().getFormulaList().onManualInput();
    }

    /*********************************************************
     * FormulaTermComparator-specific methods
     *********************************************************/

    /**
     * Procedure creates the formula layout
     */
    private void onCreate(String s, int idx, BracketsType bracketsType) throws Exception
    {
        if (idx < 0 || idx > layout.getChildCount())
        {
            throw new Exception("cannot create FormulaTermComparator for invalid insertion index " + idx);
        }
        termType = getComparatorType(getContext(), s);
        if (termType == null)
        {
            throw new Exception("cannot create FormulaTermComparator for unknown comparator");
        }
        useBrackets = bracketsType != BracketsType.NEVER;
        inflateElements(useBrackets ? R.layout.formula_operator_hor_brackets : R.layout.formula_operator_hor, true);
        initializeElements(idx);
        if (leftTerm == null || rightTerm == null)
        {
            throw new Exception("cannot initialize comparators terms");
        }
        // set texts for left and right parts
        TermField.divideString(s, getContext().getResources().getString(termType.getShortCutId()), leftTerm,
                rightTerm);
        // disable brackets of child terms in some cases
        switch (getComparatorType())
        {
        case EQUAL:
        case NOT_EQUAL:
        case GREATER:
        case GREATER_EQUAL:
        case LESS:
        case LESS_EQUAL:
            leftTerm.bracketsType = BracketsType.NEVER;
            rightTerm.bracketsType = BracketsType.NEVER;
            break;
        case COMPARATOR_AND:
        case COMPARATOR_OR:
            leftTerm.bracketsType = BracketsType.IFNECESSARY;
            leftTerm.getEditText().setComparatorEnabled(true);
            rightTerm.bracketsType = BracketsType.IFNECESSARY;
            rightTerm.getEditText().setComparatorEnabled(true);
            break;
        }
    }

    /**
     * If possible, changes the comparator type
     */
    public boolean changeComparatorType(ComparatorType newType)
    {
        if (operatorKey == null)
        {
            return false;
        }
        if (newType == ComparatorType.COMPARATOR_AND || newType == ComparatorType.COMPARATOR_OR
                || termType == ComparatorType.COMPARATOR_AND || termType == ComparatorType.COMPARATOR_OR)
        {
            return false;
        }
        termType = newType;
        updateOperatorKey();
        return true;
    }

    /**
     * Procedure sets the operator text depends on the current comparator type
     */
    private void updateOperatorKey()
    {
        switch (getComparatorType())
        {
        case EQUAL:
            operatorKey.setText("=");
            break;
        case NOT_EQUAL:
            operatorKey.setText("\u2260");
            break;
        case LESS:
            operatorKey.setText("<");
            break;
        case LESS_EQUAL:
            operatorKey.setText("\u2264");
            break;
        case GREATER:
            operatorKey.setText(">");
            break;
        case GREATER_EQUAL:
            operatorKey.setText("\u2265");
            break;
        case COMPARATOR_AND:
            operatorKey.setText(R.string.math_comparator_and_text);
            break;
        case COMPARATOR_OR:
            operatorKey.setText(R.string.math_comparator_or_text);
            break;
        }
    }
}
