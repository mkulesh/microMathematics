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

import java.util.Locale;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.formula.TermField.BracketsType;
import com.mkulesh.micromath.math.CalculatedValue;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomTextView;

public class FormulaTermOperator extends FormulaTerm
{
    /**
     * Supported operators
     */
    public enum OperatorType
    {
        PLUS(R.string.formula_operator_plus, R.drawable.p_operator_plus, R.string.math_operator_plus),
        MINUS(R.string.formula_operator_minus, R.drawable.p_operator_minus, R.string.math_operator_minus),
        MULT(R.string.formula_operator_mult, R.drawable.p_operator_mult, R.string.math_operator_mult),
        DIVIDE(R.string.formula_operator_divide, R.drawable.p_operator_divide, R.string.math_operator_divide),
        DIVIDE_SLASH(
                R.string.formula_operator_divide_slash,
                R.drawable.p_operator_divide_slash,
                R.string.math_operator_divide_slash),
        POWER(R.string.formula_operator_power, R.drawable.p_operator_power, R.string.math_operator_power);

        private final int symbolId;
        private final int imageId;
        private final int descriptionId;

        private OperatorType(int symbolId, int imageId, int descriptionId)
        {
            this.symbolId = symbolId;
            this.imageId = imageId;
            this.descriptionId = descriptionId;
        }

        public int getSymbolId()
        {
            return symbolId;
        }

        public int getImageId()
        {
            return imageId;
        }

        public int getDescriptionId()
        {
            return descriptionId;
        }
    };

    public static OperatorType getOperatorType(Context context, String s)
    {
        OperatorType retValue = null;
        for (OperatorType f : OperatorType.values())
        {
            if (s.equals(f.toString().toLowerCase(Locale.ENGLISH))
                    || s.contains(context.getResources().getString(f.getSymbolId())))
            {
                retValue = f;
                break;
            }
        }
        return retValue;
    }

    public static boolean isOperator(Context context, String s)
    {
        return getOperatorType(context, s) != null;
    }

    /**
     * Private attributes
     */
    private OperatorType operatorType = null;
    private TermField leftTerm = null, rightTerm = null;
    private boolean useBrackets = false;

    // Attention: this is not thread-safety declaration!
    private final CalculatedValue fVal = new CalculatedValue(), gVal = new CalculatedValue(),
            fDer = new CalculatedValue(), gDer = new CalculatedValue();

    /*********************************************************
     * Constructors
     *********************************************************/

    public FormulaTermOperator(TermField owner, LinearLayout layout, String s, int idx) throws Exception
    {
        super(owner.getFormulaRoot(), layout, owner.termDepth);
        setParentField(owner);
        onCreate(s, idx, owner.bracketsType);
    }

    /*********************************************************
     * GUI constructors to avoid lint warning
     *********************************************************/

    public FormulaTermOperator(Context context)
    {
        super();
    }

    public FormulaTermOperator(Context context, AttributeSet attrs)
    {
        super();
    }

    /*********************************************************
     * Re-implementation for methods for FormulaBase and FormulaTerm superclass's
     *********************************************************/

    @Override
    public CalculatedValue.ValueType getValue(CalculaterTask thread, CalculatedValue outValue) throws CancelException
    {
        if (operatorType != null && leftTerm != null && rightTerm != null)
        {
            leftTerm.getValue(thread, fVal);
            rightTerm.getValue(thread, gVal);
            switch (operatorType)
            {
            case PLUS:
                return outValue.add(fVal, gVal);
            case MINUS:
                return outValue.subtract(fVal, gVal);
            case MULT:
                return outValue.multiply(fVal, gVal);
            case DIVIDE:
            case DIVIDE_SLASH:
                return outValue.divide(fVal, gVal);
            case POWER:
                return outValue.pow(fVal, gVal);
            }
        }
        return outValue.invalidate(CalculatedValue.ErrorType.TERM_NOT_READY);
    }

    @Override
    public DifferentiableType isDifferentiable(String var)
    {
        final int dGrad = Math.min(leftTerm.isDifferentiable(var).ordinal(), rightTerm.isDifferentiable(var).ordinal());
        return DifferentiableType.values()[dGrad];
    }

    @Override
    public CalculatedValue.ValueType getDerivativeValue(String var, CalculaterTask thread, CalculatedValue outValue)
            throws CancelException
    {
        if (operatorType != null && leftTerm != null && rightTerm != null)
        {
            leftTerm.getDerivativeValue(var, thread, fDer);
            rightTerm.getDerivativeValue(var, thread, gDer);
            switch (operatorType)
            {
            case PLUS:
                return outValue.add(fDer, gDer);
            case MINUS:
                return outValue.subtract(fDer, gDer);
            case MULT:
            {
                leftTerm.getValue(thread, fVal);
                rightTerm.getValue(thread, gVal);
                fDer.multiply(fDer, gVal);
                fVal.multiply(fVal, gDer);
                return outValue.add(fDer, fVal);
            }
            case DIVIDE:
            case DIVIDE_SLASH:
            {
                leftTerm.getValue(thread, fVal);
                rightTerm.getValue(thread, gVal);
                fDer.multiply(fDer, gVal);
                fVal.multiply(fVal, gDer);
                gDer.subtract(fDer, fVal);
                fDer.multiply(gVal, gVal);
                return outValue.divide(gDer, fDer);
            }
            case POWER:
            {
                leftTerm.getValue(thread, fVal);
                rightTerm.getValue(thread, gVal);
                if (fDer.isZero() && gDer.isZero())
                {
                    // the case a^a
                    return outValue.setValue(0.0);
                }
                else if (!fDer.isZero() && gDer.isZero())
                {
                    // the case f^a: result = g * f^(g-1) * fDer;
                    CalculatedValue tmp = new CalculatedValue();
                    tmp.subtract(gVal, CalculatedValue.ONE);
                    tmp.pow(fVal, tmp);
                    outValue.multiply(gVal, tmp);
                    return outValue.multiply(outValue, fDer);
                }
                else if (fDer.isZero() && !gDer.isZero())
                {
                    // the case a^g: result = f^g * log(f) * gDer;
                    CalculatedValue tmp = new CalculatedValue();
                    tmp.log(fVal);
                    outValue.pow(fVal, gVal);
                    outValue.multiply(outValue, tmp);
                    return outValue.multiply(outValue, gDer);
                }
                else
                {
                    // case f^g: result = f^g * {fDer * g / f  +  gDer * log(f)}
                    CalculatedValue tmp1 = new CalculatedValue(), tmp2 = new CalculatedValue();
                    tmp1.multiply(fDer, gVal);
                    tmp1.divide(tmp1, fVal);
                    tmp2.log(fVal);
                    tmp2.multiply(gDer, tmp2);
                    tmp1.add(tmp1, tmp2);
                    outValue.pow(fVal, gVal);
                    return outValue.multiply(outValue, tmp1);
                }
            }
            }
        }
        return outValue.invalidate(CalculatedValue.ErrorType.TERM_NOT_READY);
    }

    @Override
    public TermType getTermType()
    {
        return TermType.OPERATOR;
    }

    @Override
    public String getTermCode()
    {
        return getOperatorType().toString().toLowerCase(Locale.ENGLISH);
    }

    @Override
    protected CustomTextView initializeSymbol(CustomTextView v)
    {
        if (v.getText() != null)
        {
            String t = v.getText().toString();
            if (t.equals(getContext().getResources().getString(R.string.formula_operator_key)))
            {
                switch (operatorType)
                {
                case PLUS:
                    v.prepare(CustomTextView.SymbolType.PLUS, getFormulaRoot().getFormulaList().getActivity(), this);
                    v.setText("..");
                    break;
                case MINUS:
                    v.prepare(CustomTextView.SymbolType.MINUS, getFormulaRoot().getFormulaList().getActivity(), this);
                    v.setText("..");
                    break;
                case MULT:
                    v.prepare(CustomTextView.SymbolType.MULT, getFormulaRoot().getFormulaList().getActivity(), this);
                    v.setText(".");
                    break;
                case DIVIDE:
                    v.prepare(CustomTextView.SymbolType.HOR_LINE, getFormulaRoot().getFormulaList().getActivity(), this);
                    v.setText("_");
                    break;
                case DIVIDE_SLASH:
                    v.prepare(CustomTextView.SymbolType.SLASH, getFormulaRoot().getFormulaList().getActivity(), this);
                    v.setText("_");
                    break;
                case POWER:
                    v.prepare(CustomTextView.SymbolType.TEXT, getFormulaRoot().getFormulaList().getActivity(), this);
                    v.setText("_");
                    break;
                }
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
                boolean addDepth = (operatorType == OperatorType.DIVIDE) ? true : false;
                leftTerm = addTerm(getFormulaRoot(), l, v, this, addDepth);
            }
            if (v.getText().toString().equals(getContext().getResources().getString(R.string.formula_right_term_key)))
            {
                int addDepth = 0;
                if (operatorType == OperatorType.DIVIDE)
                {
                    addDepth = 1;
                }
                if (operatorType == OperatorType.POWER)
                {
                    addDepth = 3;
                }
                rightTerm = addTerm(getFormulaRoot(), l, -1, v, this, addDepth);
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
     * FormulaTermOperator-specific methods
     *********************************************************/

    /**
     * Procedure creates the formula layout
     */
    private void onCreate(String s, int idx, BracketsType bracketsType) throws Exception
    {
        if (idx < 0 || idx > layout.getChildCount())
        {
            throw new Exception("cannot create FormulaTermOperator for invalid insertion index " + idx);
        }
        operatorType = getOperatorType(getContext(), s);
        if (operatorType == null)
        {
            throw new Exception("cannot create FormulaTermOperator for unknown operator");
        }
        switch (operatorType)
        {
        case PLUS:
        case MINUS:
            useBrackets = bracketsType != BracketsType.NEVER;
            inflateElements(useBrackets ? R.layout.formula_operator_hor_brackets : R.layout.formula_operator_hor, true);
            break;
        case MULT:
        case DIVIDE_SLASH:
            useBrackets = bracketsType == BracketsType.ALWAYS;
            inflateElements(useBrackets ? R.layout.formula_operator_hor_brackets : R.layout.formula_operator_hor, true);
            break;
        case DIVIDE:
            useBrackets = bracketsType == BracketsType.ALWAYS;
            inflateElements(useBrackets ? R.layout.formula_operator_vert_brackets : R.layout.formula_operator_vert,
                    true);
            break;
        case POWER:
            useBrackets = false;
            inflateElements(R.layout.formula_operator_pow, true);
            break;
        }
        initializeElements(idx);
        if (leftTerm == null || rightTerm == null)
        {
            throw new Exception("cannot initialize operator terms");
        }
        // set texts for left and right parts
        TermField.divideString(s, getContext().getResources().getString(operatorType.getSymbolId()), leftTerm,
                rightTerm);
        // disable brackets of child terms in some cases
        switch (operatorType)
        {
        case DIVIDE:
        case PLUS:
            leftTerm.bracketsType = BracketsType.NEVER;
            rightTerm.bracketsType = BracketsType.NEVER;
            break;
        case DIVIDE_SLASH:
        case MULT:
            leftTerm.bracketsType = BracketsType.IFNECESSARY;
            rightTerm.bracketsType = BracketsType.IFNECESSARY;
            break;
        case MINUS:
            leftTerm.bracketsType = BracketsType.NEVER;
            rightTerm.bracketsType = BracketsType.IFNECESSARY;
            break;
        case POWER:
            leftTerm.bracketsType = BracketsType.ALWAYS;
            rightTerm.bracketsType = BracketsType.NEVER;
            break;
        }
    }

    /**
     * Returns operator type
     */
    public OperatorType getOperatorType()
    {
        return operatorType;
    }

    /**
     * Returns whether the brackets are used
     */
    public boolean isUseBrackets()
    {
        return useBrackets;
    }
}
