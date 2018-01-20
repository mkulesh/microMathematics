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
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.math.CalculatedValue;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.DocumentProperties;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomTextView;
import com.mkulesh.micromath.widgets.ScaledDimensions;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.FastMath;

import java.util.Locale;

public class FormulaTermCommFunction extends FormulaTermFunctionBase
{
    public FormulaTermTypeIf.GroupType getGroupType()
    {
        return FormulaTermTypeIf.GroupType.COMM_FUNCTION;
    }

    /**
     * Supported functions
     */
    public enum FunctionType implements FormulaTermTypeIf
    {
        POWER(2, R.drawable.p_function_power, R.string.math_function_power,
                R.string.formula_function_power),
        SQRT_LAYOUT(1, R.drawable.p_function_sqrt, R.string.math_function_sqrt,
                R.string.formula_function_sqrt_layout),
        NTHRT_LAYOUT(2, R.drawable.p_function_nthrt, R.string.math_function_nthrt,
                R.string.formula_function_nthrt_layout),
        FACTORIAL(1, R.drawable.p_function_factorial, R.string.math_function_factorial,
                R.string.formula_function_factorial_layout),
        ABS_LAYOUT(1, R.drawable.p_function_abs, R.string.math_function_abs,
                R.string.formula_function_abs_layout),
        CONJUGATE_LAYOUT(1, R.drawable.p_function_conjugate, R.string.math_function_conjugate,
                R.string.formula_function_conjugate_layout),
        RE(1, R.drawable.p_function_re, R.string.math_function_re),
        IM(1, R.drawable.p_function_im, R.string.math_function_im),
        SIN(1, R.drawable.p_function_sin, R.string.math_function_sin),
        ASIN(1, R.drawable.p_function_asin, R.string.math_function_asin),
        SINH(1, R.drawable.p_function_sinh, R.string.math_function_sinh),
        COS(1, R.drawable.p_function_cos, R.string.math_function_cos),
        ACOS(1, R.drawable.p_function_acos, R.string.math_function_acos),
        COSH(1, R.drawable.p_function_cosh, R.string.math_function_cosh),
        TAN(1, R.drawable.p_function_tan, R.string.math_function_tan),
        ATAN(1, R.drawable.p_function_atan, R.string.math_function_atan),
        ATAN2(2, R.drawable.p_function_atan2, R.string.math_function_atan2),
        TANH(1, R.drawable.p_function_tanh, R.string.math_function_tanh),
        EXP(1, R.drawable.p_function_exp, R.string.math_function_exp),
        LN(1, R.drawable.p_function_ln, R.string.math_function_ln),
        LOG10(1, R.drawable.p_function_log10, R.string.math_function_log10),
        CEIL(1, R.drawable.p_function_ceil, R.string.math_function_ceil),
        FLOOR(1, R.drawable.p_function_floor, R.string.math_function_floor),
        RANDOM(1, R.drawable.p_function_random, R.string.math_function_random),
        MAX(2, R.drawable.p_function_max, R.string.math_function_max),
        MIN(2, R.drawable.p_function_min, R.string.math_function_min),
        IF(3, R.drawable.p_function_if, R.string.math_function_if),
        SQRT(1, Palette.NO_BUTTON, Palette.NO_BUTTON),
        ABS(1, Palette.NO_BUTTON, Palette.NO_BUTTON),
        SIGN(1, Palette.NO_BUTTON, Palette.NO_BUTTON);

        private final int argNumber;
        private final int imageId;
        private final int descriptionId;
        private final int shortCutId;
        private final String lowerCaseName;

        FunctionType(int argNumber, int imageId, int descriptionId)
        {
            this(argNumber, imageId, descriptionId, Palette.NO_BUTTON);
        }

        FunctionType(int argNumber, int imageId, int descriptionId, int shortCutId)
        {
            this.argNumber = argNumber;
            this.imageId = imageId;
            this.descriptionId = descriptionId;
            this.shortCutId = shortCutId;
            this.lowerCaseName = name().toLowerCase(Locale.ENGLISH);
        }

        public GroupType getGroupType() { return GroupType.COMM_FUNCTION; }

        public int getShortCutId() { return shortCutId; }

        public int getArgNumber()
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
    }

    /**
     * Some functions can be triggered from keyboard. This enumeration defines these triggers
     */
    private enum Trigger
    {
        GENERAL(null, true),
        ABS(FunctionType.ABS_LAYOUT, true),
        SQRT(FunctionType.SQRT_LAYOUT, true),
        POWER(FunctionType.POWER, false),
        NTHRT(FunctionType.NTHRT_LAYOUT, true),
        FACTORIAL(FunctionType.FACTORIAL, false),
        CONJUGATE(FunctionType.CONJUGATE_LAYOUT, false);

        private final FunctionType functionType;
        private final boolean isBeforeText;

        Trigger(FunctionType functionType, boolean isBeforeText)
        {
            this.functionType = functionType;
            this.isBeforeText = isBeforeText;
        }

        public int getCodeId()
        {
            return functionType != null? functionType.getShortCutId() : R.string.formula_function_start_bracket;
        }

        public FunctionType getFunctionType()
        {
            return functionType;
        }

        public boolean isBeforeText()
        {
            return isBeforeText;
        }
    }

    /**
     * Some functions are obsolete. This enumeration defines its back-compatibility
     */
    private enum ObsoleteCodes
    {
        LOG(1, FunctionType.LN),
        RND(1, FunctionType.RANDOM),
        SIGNUM(1, FunctionType.SIGN);

        private final int lastDocumentVersion;
        private final FunctionType functionType;
        private final String lowerCaseName;

        ObsoleteCodes(int lastDocumentVersion, FunctionType functionType)
        {
            this.lastDocumentVersion = lastDocumentVersion;
            this.functionType = functionType;
            this.lowerCaseName = name().toLowerCase(Locale.ENGLISH);
        }

        public int getLastDocumentVersion()
        {
            return lastDocumentVersion;
        }

        public FunctionType getFunctionType()
        {
            return functionType;
        }

        public String getLowerCaseName()
        {
            return lowerCaseName;
        }
    }

    public static FunctionType getFunctionType(Context context, String s)
    {
        String fName = null;
        final Resources res = context.getResources();

        // cat the function name
        for (int id : BracketParser.START_BRACKET_IDS)
        {
            final String startBracket = res.getString(id);
            if (s.contains(startBracket))
            {
                fName = s.substring(0, s.indexOf(startBracket)).trim();
                break;
            }
        }

        // search the function name in the types array
        for (FunctionType f : FunctionType.values())
        {
            if (s.equals(f.getLowerCaseName()))
            {
                return f;
            }
            if (fName != null && fName.equals(f.getLowerCaseName()))
            {
                return f;
            }
        }

        // Compatibility mode: search the function name in the array of obsolete functions
        if (DocumentProperties.getDocumentVersion() != DocumentProperties.LATEST_DOCUMENT_VERSION)
        {
            for (ObsoleteCodes obs : ObsoleteCodes.values())
            {
                if (DocumentProperties.getDocumentVersion() <= obs.getLastDocumentVersion() &&
                        s.equals(obs.getLowerCaseName()))
                {
                    return obs.getFunctionType();
                }
            }
        }

        // if function is not yet found, check the trigger
        for (Trigger t : Trigger.values())
        {
            if (t.getFunctionType() != null && s.contains(res.getString(t.getCodeId())))
            {
                return t.getFunctionType();
            }
        }

        return null;
    }

    public static boolean containsTrigger(Context context, String s)
    {
        for (Trigger t : Trigger.values())
        {
            if (s.contains(context.getResources().getString(t.getCodeId())))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Private attributes
     */
    // Attention: this is not thread-safety declaration!
    private final CalculatedValue a0derVal = new CalculatedValue(), a1derVal = new CalculatedValue();

    /*********************************************************
     * Constructors
     *********************************************************/

    public FormulaTermCommFunction(TermField owner, LinearLayout layout, String s, int idx) throws Exception
    {
        super(owner, layout);
        onCreate(s, idx);
    }

    /*********************************************************
     * GUI constructors to avoid lint warning
     *********************************************************/

    public FormulaTermCommFunction(Context context)
    {
        super();
    }

    public FormulaTermCommFunction(Context context, AttributeSet attrs)
    {
        super();
    }

    /*********************************************************
     * Common getters
     *********************************************************/

    public FunctionType getFunctionType()
    {
        return (FunctionType) termType;
    }

    /*********************************************************
     * Re-implementation for methods for FormulaBase and FormulaTerm superclass's
     *********************************************************/

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
            case SIN:
                return outValue.sin(a0);
            case ASIN:
                return outValue.asin(a0);
            case SINH:
                return outValue.sinh(a0);

            case COS:
                return outValue.cos(a0);
            case ACOS:
                return outValue.acos(a0);
            case COSH:
                return outValue.cosh(a0);

            case TAN:
                return outValue.tan(a0);
            case ATAN:
                return outValue.atan(a0);
            case ATAN2:
            {
                final CalculatedValue a1 = argVal[1];
                if (a0.isComplex() || a1.isComplex())
                {
                    return outValue.invalidate(CalculatedValue.ErrorType.PASSED_COMPLEX);
                }
                return outValue.setValue(FastMath.atan2(a0.getReal(), a1.getReal()));
            }
            case TANH:
                return outValue.tanh(a0);

            case EXP:
                return outValue.exp(a0);
            case LN:
                return outValue.log(a0);
            case LOG10:
                return outValue.log10(a0);

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

            case CEIL:
                return outValue.ceil(a0);
            case FLOOR:
                return outValue.floor(a0);
            case RANDOM:
                return outValue.random(a0);

            case MAX:
            case MIN:
            {
                final CalculatedValue a1 = argVal[1];
                if (a0.isComplex() || a1.isComplex())
                {
                    return outValue.invalidate(CalculatedValue.ErrorType.PASSED_COMPLEX);
                }
                final double res = (termType == FunctionType.MAX) ? FastMath.max(a0.getReal(), a1.getReal())
                        : FastMath.min(a0.getReal(), a1.getReal());
                return outValue.setValue(res);
            }

            case IF:
                if (a0.isComplex())
                {
                    return outValue.invalidate(CalculatedValue.ErrorType.PASSED_COMPLEX);
                }
                return outValue.assign((a0.getReal() > 0) ? argVal[1] : argVal[2]);

            case SIGN:
                if (a0.isComplex())
                {
                    return outValue.invalidate(CalculatedValue.ErrorType.PASSED_COMPLEX);
                }
                return outValue.setValue(FastMath.signum(a0.getReal()));

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
    public DifferentiableType isDifferentiable(String var)
    {
        if (termType == null)
        {
            return DifferentiableType.NONE;
        }
        DifferentiableType argsProp = DifferentiableType.INDEPENDENT;
        for (int i = 0; i < terms.size(); i++)
        {
            final int dGrad = Math.min(argsProp.ordinal(), terms.get(i).isDifferentiable(var).ordinal());
            argsProp = DifferentiableType.values()[dGrad];
        }

        DifferentiableType retValue = DifferentiableType.NONE;
        switch (getFunctionType())
        {
        // for these functions, derivative can be calculated analytically
        case POWER:
        case SIN:
        case ASIN:
        case SINH:
        case COS:
        case ACOS:
        case COSH:
        case TAN:
        case ATAN:
        case TANH:
        case EXP:
        case LN:
        case LOG10:
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
            if (retValue != DifferentiableType.INDEPENDENT)
            {
                final DifferentiableType powValue = DifferentiableType.values()[terms.get(0).isDifferentiable(var)
                        .ordinal()];
                retValue = (powValue == DifferentiableType.INDEPENDENT) ? retValue : DifferentiableType.NONE;
            }
            break;
        // these functions are not differentiable if contain the given argument
        case ATAN2:
        case CEIL:
        case FLOOR:
        case RANDOM:
        case MAX:
        case MIN:
        case IF:
        case SIGN:
        case FACTORIAL:
        case CONJUGATE_LAYOUT:
            retValue = (argsProp == DifferentiableType.INDEPENDENT) ? DifferentiableType.INDEPENDENT
                    : DifferentiableType.NONE;
            break;
        }
        // set the error code to be displayed
        ErrorCode errorCode = ErrorCode.NO_ERROR;
        if (retValue == DifferentiableType.NONE)
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
            case SIN: // cos(a0) * a0'
                outValue.cos(a0);
                return outValue.multiply(outValue, a0derVal);
            case ASIN: // (1.0 / sqrt(1.0 - a0 * a0)) * a0'
                outValue.multiply(a0, a0);
                outValue.subtract(CalculatedValue.ONE, outValue);
                outValue.sqrt(outValue);
                outValue.divide(CalculatedValue.ONE, outValue);
                return outValue.multiply(outValue, a0derVal);
            case SINH: // cosh(a0) * a0'
                outValue.cosh(a0);
                return outValue.multiply(outValue, a0derVal);
            case COS: // -1 * sin(a0) * a0'
                outValue.sin(a0);
                outValue.multiply(-1.0);
                return outValue.multiply(outValue, a0derVal);
            case ACOS: // (-1.0 / sqrt(1.0 - a0 * a0)) * a0'
                outValue.multiply(a0, a0);
                outValue.subtract(CalculatedValue.ONE, outValue);
                outValue.sqrt(outValue);
                outValue.divide(CalculatedValue.MINUS_ONE, outValue);
                return outValue.multiply(outValue, a0derVal);
            case COSH: // sinh(a0) * a0'
                outValue.sinh(a0);
                return outValue.multiply(outValue, a0derVal);
            case TAN: // (1.0 + tan(a0) * tan(a0)) * a0'
                outValue.tan(a0);
                outValue.multiply(outValue, outValue);
                outValue.add(CalculatedValue.ONE, outValue);
                return outValue.multiply(outValue, a0derVal);
            case ATAN: // (1.0 / (1.0 + a0 * a0)) * a0'
                outValue.multiply(a0, a0);
                outValue.add(CalculatedValue.ONE, outValue);
                outValue.divide(CalculatedValue.ONE, outValue);
                return outValue.multiply(outValue, a0derVal);
            case TANH: // (1.0 / (cosh(a0) * cosh(a0))) * a0'
                outValue.cosh(a0);
                outValue.multiply(outValue, outValue);
                outValue.divide(CalculatedValue.ONE, outValue);
                return outValue.multiply(outValue, a0derVal);
            case EXP: // exp(a0) * a0'
                outValue.exp(a0);
                return outValue.multiply(outValue, a0derVal);
            case LN: // (1.0 / a0) * a0'
                outValue.divide(CalculatedValue.ONE, a0);
                return outValue.multiply(outValue, a0derVal);
            case LOG10: // (1.0 / (a0 * FastMath.log(10.0))) * a0'
                outValue.assign(a0);
                outValue.multiply(FastMath.log(10.0));
                outValue.divide(CalculatedValue.ONE, outValue);
                return outValue.multiply(outValue, a0derVal);
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
                outValue.multiply((double) n);
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
            case ATAN2:
            case CEIL:
            case FLOOR:
            case RANDOM:
            case MAX:
            case MIN:
            case IF:
            case SIGN:
            case FACTORIAL:
            case CONJUGATE_LAYOUT:
                DifferentiableType argsProp = DifferentiableType.INDEPENDENT;
                for (int i = 0; i < terms.size(); i++)
                {
                    final int dGrad = Math.min(argsProp.ordinal(), terms.get(i).isDifferentiable(var).ordinal());
                    argsProp = DifferentiableType.values()[dGrad];
                }
                if (argsProp == DifferentiableType.INDEPENDENT)
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
            else
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

    /*********************************************************
     * Implementation for methods for FormulaChangeIf interface
     *********************************************************/

    @Override
    protected boolean isRemainingTermOnDelete()
    {
        return termType == FormulaTermCommFunction.FunctionType.NTHRT_LAYOUT || terms.size() <= 1 || !isNewTermEnabled();
    }

    @Override
    public boolean isNewTermEnabled()
    {
        return false;
    }

    /*********************************************************
     * FormulaTermFunction-specific methods
     *********************************************************/

    /**
     * Procedure creates the formula layout
     */
    private void onCreate(String s, int idx) throws Exception
    {
        if (idx < 0 || idx > layout.getChildCount())
        {
            throw new Exception("cannot create FormulaFunction for invalid insertion index " + idx);
        }
        termType = getFunctionType(getContext(), s);
        if (termType == null)
        {
            throw new Exception("cannot create FormulaFunction for unknown function");
        }
        int argNumber = getFunctionType().getArgNumber();
        switch (getFunctionType())
        {
        case SQRT_LAYOUT:
            inflateElements(R.layout.formula_function_sqrt, true);
            break;
        case NTHRT_LAYOUT:
            inflateElements(R.layout.formula_function_nthrt, true);
            break;
        case FACTORIAL:
            inflateElements(R.layout.formula_function_factorial, true);
            break;
        case CONJUGATE_LAYOUT:
            inflateElements(R.layout.formula_function_conjugate, true);
            break;
        case ABS_LAYOUT:
            inflateElements(R.layout.formula_function_noname, true);
            break;
        case POWER:
            inflateElements(R.layout.formula_function_pow, true);
            break;
        default:
            inflateElements(R.layout.formula_function_named, true);
            break;
        }
        initializeElements(idx);
        if (terms.isEmpty())
        {
            throw new Exception("argument list is empty");
        }

        initializeMainLayout();

        // add additional arguments
        while (terms.size() < argNumber)
        {
            TermField newTerm = addArgument(terms.get(terms.size() - 1), R.layout.formula_function_add_arg, 0);
            if (newTerm == null)
            {
                break;
            }
        }
        if (getFunctionType().getArgNumber() > 0 && terms.size() != getFunctionType().getArgNumber())
        {
            throw new Exception("invalid size for argument list");
        }

        // special text properties
        if (termType == FunctionType.IF)
        {
            terms.get(0).getEditText().setComparatorEnabled(true);
        }

        // set texts for left and right parts (in editing mode only)
        for (int brIdx = 0; brIdx < BracketParser.START_BRACKET_IDS.length; brIdx++)
        {
            final String startBracket = getContext().getResources().getString(BracketParser.START_BRACKET_IDS[brIdx]);
            final String endBracket = getContext().getResources().getString(BracketParser.END_BRACKET_IDS[brIdx]);
            if (s.contains(startBracket) && s.endsWith(endBracket))
            {
                s = s.substring(0, s.indexOf(endBracket)).trim();
            }
        }
        for (Trigger t : Trigger.values())
        {
            String opCode = getContext().getResources().getString(t.getCodeId());
            final int opPosition = s.indexOf(opCode);
            final TermField term = getArgumentTerm();
            if (opPosition >= 0 && term != null)
            {
                try
                {
                    if (t.isBeforeText())
                    {
                        term.setText(s.subSequence(opPosition + opCode.length(), s.length()));
                    }
                    else
                    {
                        term.setText(s.subSequence(0, opPosition));
                    }
                    isContentValid(ValidationPassType.VALIDATE_SINGLE_FORMULA);
                }
                catch (Exception ex)
                {
                    // nothing to do
                }
                break;
            }
        }
    }
}
