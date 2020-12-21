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
package com.mkulesh.micromath.formula.terms;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.mkulesh.micromath.formula.CalculatableIf;
import com.mkulesh.micromath.formula.CalculaterTask;
import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.formula.FormulaTerm;
import com.mkulesh.micromath.formula.Palette;
import com.mkulesh.micromath.formula.PaletteButton;
import com.mkulesh.micromath.formula.TermField;
import com.mkulesh.micromath.math.CalculatedValue;
import com.mkulesh.micromath.R;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomTextView;
import com.mkulesh.micromath.widgets.ScaledDimensions;

import org.apache.commons.math3.util.CombinatoricsUtils;

import java.util.Locale;

public class CommonFunctions extends FunctionBase
{
    public TermTypeIf.GroupType getGroupType()
    {
        return TermTypeIf.GroupType.COMMON_FUNCTIONS;
    }

    /**
     * Supported functions
     */
    public enum FunctionType implements TermTypeIf
    {
        POWER(2, R.drawable.p_function_power, R.string.math_function_power,
                R.string.formula_function_power, R.layout.formula_function_pow),
        SQRT_LAYOUT(1, R.drawable.p_function_sqrt, R.string.math_function_sqrt,
                R.string.formula_function_sqrt_layout, R.layout.formula_function_sqrt),
        NTHRT_LAYOUT(2, R.drawable.p_function_nthrt, R.string.math_function_nthrt,
                R.string.formula_function_nthrt_layout, R.layout.formula_function_nthrt),
        FACTORIAL(1, R.drawable.p_function_factorial, R.string.math_function_factorial,
                R.string.formula_function_factorial_layout, R.layout.formula_function_factorial),
        ABS_LAYOUT(1, R.drawable.p_function_abs, R.string.math_function_abs,
                R.string.formula_function_abs_layout, R.layout.formula_function_noname),
        CONJUGATE_LAYOUT(1, R.drawable.p_function_conjugate, R.string.math_function_conjugate,
                R.string.formula_function_conjugate_layout, R.layout.formula_function_conjugate),
        RE(1, R.drawable.p_function_re, R.string.math_function_re),
        IM(1, R.drawable.p_function_im, R.string.math_function_im),
        SQRT(1, Palette.NO_BUTTON, Palette.NO_BUTTON),
        ABS(1, Palette.NO_BUTTON, Palette.NO_BUTTON);

        private final int argNumber;
        private final int imageId;
        private final int descriptionId;
        private final int shortCutId;
        private final int layoutId;
        private final String lowerCaseName;

        FunctionType(int argNumber, int imageId, int descriptionId)
        {
            this(argNumber, imageId, descriptionId, Palette.NO_BUTTON, R.layout.formula_function_named);
        }

        FunctionType(int argNumber, int imageId, int descriptionId, int shortCutId, int layoutId)
        {
            this.argNumber = argNumber;
            this.imageId = imageId;
            this.descriptionId = descriptionId;
            this.shortCutId = shortCutId;
            this.lowerCaseName = name().toLowerCase(Locale.ENGLISH);
            this.layoutId = layoutId;
        }

        public GroupType getGroupType()
        {
            return GroupType.COMMON_FUNCTIONS;
        }

        public int getShortCutId()
        {
            return shortCutId;
        }

        int getArgNumber()
        {
            return argNumber;
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

        int getLayoutId()
        {
            return layoutId;
        }

        public int getBracketId()
        {
            return R.string.formula_function_start_bracket;
        }

        public boolean isEnabled(CustomEditText field)
        {
            return true;
        }

        public PaletteButton.Category getPaletteCategory()
        {
            return PaletteButton.Category.CONVERSION;
        }

        public FormulaTerm createTerm(
                TermField termField, LinearLayout layout, String s, int textIndex) throws Exception
        {
            return new CommonFunctions(this, termField, layout, s, textIndex);
        }
    }

    /**
     * Private attributes
     */
    // Attention: this is not thread-safety declaration!
    private final CalculatedValue a0derVal = new CalculatedValue(), a1derVal = new CalculatedValue();

    /*--------------------------------------------------------*
     * Constructors
     *--------------------------------------------------------*/

    private CommonFunctions(FunctionType type, TermField owner, LinearLayout layout, String s, int idx) throws Exception
    {
        super(owner, layout);
        termType = type;
        createGeneralFunction(getFunctionType().getLayoutId(), s, getFunctionType().getArgNumber(), idx, owner.isPasteFromClipboard());
    }

    /*--------------------------------------------------------*
     * GUI constructors to avoid lint warning
     *--------------------------------------------------------*/

    public CommonFunctions(Context context)
    {
        super();
    }

    public CommonFunctions(Context context, AttributeSet attrs)
    {
        super();
    }

    /*--------------------------------------------------------*
     * Common getters
     *--------------------------------------------------------*/

    public FunctionType getFunctionType()
    {
        return (FunctionType) termType;
    }

    /*--------------------------------------------------------*
     * Re-implementation for methods for FormulaBase and FormulaTerm superclass's
     *--------------------------------------------------------*/

    @Override
    protected String getFunctionLabel()
    {
        switch (getFunctionType())
        {
        case POWER:
        case SQRT_LAYOUT:
        case NTHRT_LAYOUT:
        case FACTORIAL:
        case ABS_LAYOUT:
        case CONJUGATE_LAYOUT:
            return "";
        default:
            return termType.getLowerCaseName();
        }
    }

    @Override
    public CalculatedValue.ValueType getValue(CalculaterTask thread, CalculatedValue outValue) throws CancelException
    {
        if (termType != null && terms.size() > 0)
        {
            ensureArgValSize();
            for (int i = 0; i < terms.size(); i++)
            {
                terms.get(i).getValue(thread, argVal[i]);
            }
            final CalculatedValue a0 = argVal[0];
            switch (getFunctionType())
            {
            case POWER:
                return outValue.pow(a0, argVal[1]);

            case SQRT:
            case SQRT_LAYOUT:
                return outValue.sqrt(a0);
            case NTHRT_LAYOUT:
                return outValue.nthRoot(argVal[1], a0.getInteger());

            case ABS:
            case ABS_LAYOUT:
                return outValue.abs(a0);
            case CONJUGATE_LAYOUT:
                return outValue.conj(a0);
            case RE:
                return outValue.setValue(a0.getReal());
            case IM:
                return outValue.setValue(a0.isComplex() ? a0.getImaginary() : 0.0);

            case FACTORIAL:
                if (a0.isComplex())
                {
                    return outValue.invalidate(CalculatedValue.ErrorType.PASSED_COMPLEX);
                }
                try
                {
                    return outValue.setValue(CombinatoricsUtils.factorialDouble((int) a0.getReal()));
                }
                catch (Exception e)
                {
                    return outValue.invalidate(CalculatedValue.ErrorType.NOT_A_NUMBER);
                }
            }
        }
        return outValue.invalidate(CalculatedValue.ErrorType.TERM_NOT_READY);
    }

    @Override
    public CalculatableIf.DifferentiableType isDifferentiable(String var)
    {
        if (termType == null)
        {
            return CalculatableIf.DifferentiableType.NONE;
        }
        CalculatableIf.DifferentiableType argsProp = CalculatableIf.DifferentiableType.INDEPENDENT;
        for (int i = 0; i < terms.size(); i++)
        {
            final int dGrad = Math.min(argsProp.ordinal(), terms.get(i).isDifferentiable(var).ordinal());
            argsProp = CalculatableIf.DifferentiableType.values()[dGrad];
        }

        CalculatableIf.DifferentiableType retValue = CalculatableIf.DifferentiableType.NONE;
        switch (getFunctionType())
        {
        // for these functions, derivative can be calculated analytically
        case POWER:
        case SQRT:
        case SQRT_LAYOUT:
        case ABS:
        case ABS_LAYOUT:
        case RE:
        case IM:
            retValue = argsProp;
            break;
        // for this function, derivative depends on the function itself
        case NTHRT_LAYOUT:
            // n-th root is only differentiable if the power does not depend on the given argument
            retValue = argsProp;
            if (retValue != CalculatableIf.DifferentiableType.INDEPENDENT)
            {
                final CalculatableIf.DifferentiableType powValue = CalculatableIf.DifferentiableType.values()[terms.get(0).isDifferentiable(var)
                        .ordinal()];
                retValue = (powValue == CalculatableIf.DifferentiableType.INDEPENDENT) ? retValue : CalculatableIf.DifferentiableType.NONE;
            }
            break;
        // these functions are not differentiable if contain the given argument
        case FACTORIAL:
        case CONJUGATE_LAYOUT:
            retValue = (argsProp == CalculatableIf.DifferentiableType.INDEPENDENT) ? CalculatableIf.DifferentiableType.INDEPENDENT
                    : CalculatableIf.DifferentiableType.NONE;
            break;
        }
        // set the error code to be displayed
        ErrorCode errorCode = ErrorCode.NO_ERROR;
        if (retValue == CalculatableIf.DifferentiableType.NONE)
        {
            errorCode = ErrorCode.NOT_DIFFERENTIABLE;
        }
        setErrorCode(errorCode, var);
        return retValue;
    }

    @Override
    public CalculatedValue.ValueType getDerivativeValue(String var, CalculaterTask thread, CalculatedValue outValue)
            throws CancelException
    {
        if (termType != null && terms.size() > 0)
        {
            ensureArgValSize();
            for (int i = 0; i < terms.size(); i++)
            {
                terms.get(i).getValue(thread, argVal[i]);
            }
            final CalculatedValue a0 = argVal[0];
            terms.get(0).getDerivativeValue(var, thread, a0derVal);
            switch (getFunctionType())
            {
            // for these functions, derivative can be calculated analytically
            case POWER:
            {
                final CalculatedValue a1 = argVal[1];
                terms.get(1).getDerivativeValue(var, thread, a1derVal);
                if (a0derVal.isZero() && a1derVal.isZero())
                {
                    // the case a^a
                    return outValue.setValue(0.0);
                }
                else if (!a0derVal.isZero() && a1derVal.isZero())
                {
                    // the case f^a: result = g * f^(g-1) * a0derVal;
                    CalculatedValue tmp = new CalculatedValue();
                    tmp.subtract(a1, CalculatedValue.ONE);
                    tmp.pow(a0, tmp);
                    outValue.multiply(a1, tmp);
                    return outValue.multiply(outValue, a0derVal);
                }
                else if (a0derVal.isZero() && !a1derVal.isZero())
                {
                    // the case a^g: result = f^g * log(f) * a1derVal;
                    CalculatedValue tmp = new CalculatedValue();
                    tmp.log(a0);
                    outValue.pow(a0, a1);
                    outValue.multiply(outValue, tmp);
                    return outValue.multiply(outValue, a1derVal);
                }
                else
                {
                    // case f^g: result = f^g * {a0derVal * g / f  +  a1derVal * log(f)}
                    CalculatedValue tmp1 = new CalculatedValue(), tmp2 = new CalculatedValue();
                    tmp1.multiply(a0derVal, a1);
                    tmp1.divide(tmp1, a0);
                    tmp2.log(a0);
                    tmp2.multiply(a1derVal, tmp2);
                    tmp1.add(tmp1, tmp2);
                    outValue.pow(a0, a1);
                    return outValue.multiply(outValue, tmp1);
                }
            }

            case SQRT:
            case SQRT_LAYOUT: // (1.0 / (2.0 * √a0)) * a0'
                outValue.sqrt(a0);
                outValue.multiply(2.0);
                outValue.divide(CalculatedValue.ONE, outValue);
                return outValue.multiply(outValue, a0derVal);
            case NTHRT_LAYOUT: // ( n√ a1 )' = 1 / ( n n√ a1^(n-1) ) * a1'
            {
                final int n = a0.getInteger();
                terms.get(1).getDerivativeValue(var, thread, a1derVal);
                outValue.setValue(n - 1);
                outValue.pow(argVal[1], outValue);
                outValue.nthRoot(outValue, n);
                outValue.multiply(n);
                return outValue.divide(a1derVal, outValue);
            }
            case ABS:
            case ABS_LAYOUT: // not defined for complex number
                if (a0.isComplex())
                {
                    return outValue.invalidate(CalculatedValue.ErrorType.PASSED_COMPLEX);
                }
                return outValue.setValue((a0.getReal() >= 0 ? 1.0 : -1.0) * a0derVal.getReal());
            case RE:
                return outValue.setValue(a0derVal.getReal());
            case IM:
                return outValue.setValue(a0derVal.isComplex() ? a0derVal.getImaginary() : 0.0);

            // these functions are not differentiable if contain the given argument
            case FACTORIAL:
            case CONJUGATE_LAYOUT:
                CalculatableIf.DifferentiableType argsProp = CalculatableIf.DifferentiableType.INDEPENDENT;
                for (int i = 0; i < terms.size(); i++)
                {
                    final int dGrad = Math.min(argsProp.ordinal(), terms.get(i).isDifferentiable(var).ordinal());
                    argsProp = CalculatableIf.DifferentiableType.values()[dGrad];
                }
                if (argsProp == CalculatableIf.DifferentiableType.INDEPENDENT)
                {
                    return outValue.setValue(0.0);
                }
                else
                {
                    return outValue.invalidate(CalculatedValue.ErrorType.NOT_A_NUMBER);
                }
            }
        }
        return outValue.invalidate(CalculatedValue.ErrorType.TERM_NOT_READY);
    }

    @Override
    protected CustomTextView initializeSymbol(CustomTextView v)
    {
        final Resources res = getContext().getResources();
        if (v.getText() != null)
        {
            String t = v.getText().toString();
            if (t.equals(res.getString(R.string.formula_operator_key)))
            {
                v.prepare(CustomTextView.SymbolType.TEXT, getFormulaRoot().getFormulaList().getActivity(), this);
                switch (getFunctionType())
                {
                case POWER:
                    v.setText("_");
                    break;
                case FACTORIAL:
                    v.setText(res.getString(R.string.formula_function_factorial_layout));
                    break;
                case CONJUGATE_LAYOUT:
                    v.prepare(CustomTextView.SymbolType.HOR_LINE, getFormulaRoot().getFormulaList().getActivity(), this);
                    v.setText("_");
                    break;
                default:
                    v.setText(getFunctionLabel());
                    break;
                }
                functionTerm = v;
            }
            else if (t.equals(res.getString(R.string.formula_left_bracket_key)))
            {
                CustomTextView.SymbolType s = (termType == FunctionType.ABS_LAYOUT) ? CustomTextView.SymbolType.VERT_LINE
                        : CustomTextView.SymbolType.LEFT_BRACKET;
                v.prepare(s, getFormulaRoot().getFormulaList().getActivity(), this);
                v.setText("."); // this text defines view width/height
            }
            else if (t.equals(res.getString(R.string.formula_right_bracket_key)))
            {
                CustomTextView.SymbolType s = (termType == FunctionType.ABS_LAYOUT) ? CustomTextView.SymbolType.VERT_LINE
                        : CustomTextView.SymbolType.RIGHT_BRACKET;
                v.prepare(s, getFormulaRoot().getFormulaList().getActivity(), this);
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
            final String val = v.getText().toString();
            if (val.equals(getContext().getResources().getString(R.string.formula_arg_term_key)))
            {
                final TermField t = addTerm(getFormulaRoot(), l, -1, v, this, 0);
                t.bracketsType = (termType == FunctionType.FACTORIAL || termType == FunctionType.CONJUGATE_LAYOUT) ? TermField.BracketsType.ALWAYS
                        : TermField.BracketsType.NEVER;
            }
            else if (termType == FunctionType.NTHRT_LAYOUT
                    && val.equals(getContext().getResources().getString(R.string.formula_left_term_key)))
            {
                final TermField t = addTerm(getFormulaRoot(), l, -1, v, this, 3);
                t.bracketsType = TermField.BracketsType.NEVER;
            }
            else if (termType == FunctionType.NTHRT_LAYOUT
                    && val.equals(getContext().getResources().getString(R.string.formula_right_term_key)))
            {
                final TermField t = addTerm(getFormulaRoot(), l, -1, v, this, 0);
                t.bracketsType = TermField.BracketsType.NEVER;
            }
            else if (termType == FunctionType.POWER
                    && val.equals(getContext().getResources().getString(R.string.formula_left_term_key)))
            {
                final TermField t = addTerm(getFormulaRoot(), l, v, this, false);
                t.bracketsType = TermField.BracketsType.ALWAYS;
            }
            else if (termType == FunctionType.POWER
                    && val.equals(getContext().getResources().getString(R.string.formula_right_term_key)))
            {
                final TermField t = addTerm(getFormulaRoot(), l, -1, v, this, 3);
                t.bracketsType = TermField.BracketsType.NEVER;
            }
        }
        return v;
    }

    @Override
    public void updateTextSize()
    {
        super.updateTextSize();
        final int hsp = getFormulaList().getDimen().get(ScaledDimensions.Type.HOR_SYMBOL_PADDING);
        if (functionTerm != null)
        {
            if (termType == FunctionType.SQRT_LAYOUT || termType == FunctionType.NTHRT_LAYOUT)
            {
                functionTerm.setWidth(getFormulaList().getDimen().get(ScaledDimensions.Type.SMALL_SYMBOL_SIZE));
                functionTerm.setPadding(0, 0, 0, 0);
            }
            else if (termType == FunctionType.CONJUGATE_LAYOUT)
            {
                functionTerm.setPadding(hsp, 0, hsp, 0);
            }
            else if (getFunctionLabel().isEmpty())
            {
                functionTerm.setPadding(0, 0, hsp, 0);
            }
        }
        if (termType == FunctionType.NTHRT_LAYOUT)
        {
            View nthrtPoverLayout = layout.findViewById(R.id.nthrt_power_layout);
            if (nthrtPoverLayout != null)
            {
                nthrtPoverLayout.setPadding(hsp, 0, hsp, 0);
            }
        }
    }

    @Override
    public TermField getArgumentTerm()
    {
        if (termType == FunctionType.NTHRT_LAYOUT)
        {
            return terms.get(1);
        }
        return super.getArgumentTerm();
    }

    /*--------------------------------------------------------*
     * Implementation for methods for FormulaChangeIf interface
     *--------------------------------------------------------*/

    @Override
    protected boolean isRemainingTermOnDelete()
    {
        return termType == FunctionType.NTHRT_LAYOUT || terms.size() <= 1 || !isNewTermEnabled();
    }
}
