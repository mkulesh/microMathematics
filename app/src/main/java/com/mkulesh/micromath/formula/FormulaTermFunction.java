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

import java.util.ArrayList;
import java.util.Locale;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.FastMath;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.formula.TermField.ErrorNotification;
import com.mkulesh.micromath.math.CalculatedValue;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.CompatUtils;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomLayout;
import com.mkulesh.micromath.widgets.CustomTextView;
import com.mkulesh.micromath.widgets.ScaledDimensions;

public class FormulaTermFunction extends FormulaTerm
{
    /**
     * Supported functions
     */
    public enum FunctionType
    {
        IDENTITY(1, R.drawable.p_function_identity, R.string.math_function_identity, null),
        SQRT_LAYOUT(1, R.drawable.p_function_sqrt, R.string.math_function_sqrt, null),
        NTHRT_LAYOUT(2, R.drawable.p_function_nthrt, R.string.math_function_nthrt, null),
        FACTORIAL(1, R.drawable.p_function_factorial, R.string.math_function_factorial, null),
        ABS_LAYOUT(1, R.drawable.p_function_abs, R.string.math_function_abs, null),
        CONJUGATE_LAYOUT(1, R.drawable.p_function_conjugate, R.string.math_function_conjugate, null),
        RE(1, R.drawable.p_function_re, R.string.math_function_re, null),
        IM(1, R.drawable.p_function_im, R.string.math_function_im, null),
        SIN(1, R.drawable.p_function_sin, R.string.math_function_sin, null),
        ASIN(1, R.drawable.p_function_asin, R.string.math_function_asin, null),
        SINH(1, R.drawable.p_function_sinh, R.string.math_function_sinh, null),
        COS(1, R.drawable.p_function_cos, R.string.math_function_cos, null),
        ACOS(1, R.drawable.p_function_acos, R.string.math_function_acos, null),
        COSH(1, R.drawable.p_function_cosh, R.string.math_function_cosh, null),
        TAN(1, R.drawable.p_function_tan, R.string.math_function_tan, null),
        ATAN(1, R.drawable.p_function_atan, R.string.math_function_atan, null),
        ATAN2(2, R.drawable.p_function_atan2, R.string.math_function_atan2, null),
        TANH(1, R.drawable.p_function_tanh, R.string.math_function_tanh, null),
        EXP(1, R.drawable.p_function_exp, R.string.math_function_exp, null),
        LOG(1, R.drawable.p_function_log, R.string.math_function_log, null),
        LOG10(1, R.drawable.p_function_log10, R.string.math_function_log10, null),
        CEIL(1, R.drawable.p_function_ceil, R.string.math_function_ceil, null),
        FLOOR(1, R.drawable.p_function_floor, R.string.math_function_floor, null),
        RND(1, R.drawable.p_function_rnd, R.string.math_function_rnd, null),
        MAX(2, R.drawable.p_function_max, R.string.math_function_max, null),
        MIN(2, R.drawable.p_function_min, R.string.math_function_min, null),
        HYPOT(2, R.drawable.p_function_hypot, R.string.math_function_hypot, null),
        IF(3, R.drawable.p_function_if, R.string.math_function_if, null),
        SQRT(1, Palette.NO_BUTTON, Palette.NO_BUTTON, null),
        ABS(1, Palette.NO_BUTTON, Palette.NO_BUTTON, null),
        SIGNUM(1, Palette.NO_BUTTON, Palette.NO_BUTTON, null),
        FUNCTION_LINK(-1, Palette.NO_BUTTON, Palette.NO_BUTTON, "content:com.mkulesh.micromath.link"),
        FUNCTION_INDEX(-1, Palette.NO_BUTTON, Palette.NO_BUTTON, "content:com.mkulesh.micromath.index");

        private final int argNumber;
        private final int imageId;
        private final int descriptionId;
        private final String linkObject;

        private FunctionType(int argNumber, int imageId, int descriptionId, String linkObject)
        {
            this.argNumber = argNumber;
            this.imageId = imageId;
            this.descriptionId = descriptionId;
            this.linkObject = linkObject;
        }

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

        public String getLinkObject()
        {
            return linkObject;
        }

        public boolean isLink()
        {
            return linkObject != null;
        }
    }

    /**
     * Some functions can be triggered from keyboard. This enumeration defines these triggers
     */
    enum Trigger
    {
        GENERAL(R.string.formula_function_start_bracket, null, true),
        INDEX(R.string.formula_function_start_index, FunctionType.FUNCTION_INDEX, true),
        ABS(R.string.formula_function_abs_layout, FunctionType.ABS_LAYOUT, true),
        SQRT(R.string.formula_function_sqrt_layout, FunctionType.SQRT_LAYOUT, true),
        NTHRT(R.string.formula_function_nthrt_layout, FunctionType.NTHRT_LAYOUT, true),
        FACTORIAL(R.string.formula_function_factorial_layout, FunctionType.FACTORIAL, false),
        CONJUGATE(R.string.formula_function_conjugate_layout, FunctionType.CONJUGATE_LAYOUT, false);

        private final int codeId;
        private final FunctionType functionType;
        private final boolean isBeforeText;

        private Trigger(int codeId, FunctionType functionType, boolean isBeforeText)
        {
            this.codeId = codeId;
            this.functionType = functionType;
            this.isBeforeText = isBeforeText;
        }

        public int getCodeId()
        {
            return codeId;
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
     * Error codes that can be generated by function term
     */
    enum ErrorCode
    {
        NO_ERROR(-1),
        UNKNOWN_FUNCTION(R.string.error_unknown_function),
        UNKNOWN_ARRAY(R.string.error_unknown_array),
        NOT_AN_ARRAY(R.string.error_not_an_array),
        NOT_A_FUNCTION(R.string.error_not_a_function),
        RECURSIVE_CALL(R.string.error_recursive_call),
        NOT_DIFFERENTIABLE(R.string.error_not_differentiable);

        private final int descriptionId;

        private ErrorCode(int descriptionId)
        {
            this.descriptionId = descriptionId;
        }

        public String getDescription(Context context)
        {
            return context.getResources().getString(descriptionId);
        }
    }

    public static final String FUNCTION_ARGS_MARKER = ":";

    public static FunctionType getFunctionType(Context context, String s)
    {
        String fName = null;
        final Resources res = context.getResources();

        // cat the function name
        int bracketId = ViewUtils.INVALID_INDEX;
        for (int id : BracketParser.START_BRACKET_IDS)
        {
            final String startBracket = res.getString(id);
            if (s.contains(startBracket))
            {
                fName = s.substring(0, s.indexOf(startBracket)).trim();
                bracketId = id;
                break;
            }
        }

        // search the function name in the types array
        for (FunctionType f : FunctionType.values())
        {
            if (f.isLink() && s.contains(f.getLinkObject()))
            {
                return f;
            }
            if (s.equals(f.toString().toLowerCase(Locale.ENGLISH)))
            {
                return f;
            }
            if (fName != null && fName.equals(f.toString().toLowerCase(Locale.ENGLISH)))
            {
                return f;
            }
        }

        // special case (just brackets)
        if (fName != null && fName.length() == 0 && bracketId != ViewUtils.INVALID_INDEX)
        {
            if (bracketId == BracketParser.START_BRACKET_IDS[BracketParser.FUNCTION_BRACKETS])
            {
                // an identity function (just brackets) is a special case of a function
                return FunctionType.IDENTITY;
            }
            else
            {
                // index only valid if fName not empty
                return null;
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

        // default case: function link
        if (fName != null)
        {
            return FunctionType.FUNCTION_LINK;
        }

        return null;
    }

    public static String getFunctionString(Context context, FunctionType t)
    {
        return t.isLink() ? t.getLinkObject() : t.toString().toLowerCase(Locale.ENGLISH);
    }

    public static boolean isFunction(Context context, String s)
    {
        return getFunctionType(context, s) != null;
    }

    public static boolean isConversionEnabled(Context context, String s)
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
    private FunctionType functionType = null;
    private CustomTextView functionTerm = null;
    private CustomLayout functionMainLayout = null;
    private String functionLinkName = "unknown";
    private Equation linkedFunction = null;

    // Attention: this is not thread-safety declaration!
    private final CalculatedValue a0derVal = new CalculatedValue(), a1derVal = new CalculatedValue();
    private CalculatedValue[] argVal = null;

    /*********************************************************
     * Constructors
     *********************************************************/

    public FormulaTermFunction(TermField owner, LinearLayout layout, String s, int idx) throws Exception
    {
        super(owner.getFormulaRoot(), layout, owner.termDepth);
        setParentField(owner);
        onCreate(s, idx);
    }

    /*********************************************************
     * GUI constructors to avoid lint warning
     *********************************************************/

    public FormulaTermFunction(Context context)
    {
        super();
    }

    public FormulaTermFunction(Context context, AttributeSet attrs)
    {
        super();
    }

    /*********************************************************
     * Re-implementation for methods for FormulaBase and FormulaTerm superclass's
     *********************************************************/

    @Override
    public CalculatedValue.ValueType getValue(CalculaterTask thread, CalculatedValue outValue) throws CancelException
    {
        if (functionType != null && terms.size() > 0)
        {
            ensureArgValSize();
            for (int i = 0; i < terms.size(); i++)
            {
                terms.get(i).getValue(thread, argVal[i]);
            }
            final CalculatedValue a0 = argVal[0];
            switch (functionType)
            {
            case IDENTITY:
                return outValue.assign(a0);
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
            case LOG:
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
            case RND:
                return outValue.random(a0);

            case MAX:
            case MIN:
            {
                final CalculatedValue a1 = argVal[1];
                if (a0.isComplex() || a1.isComplex())
                {
                    return outValue.invalidate(CalculatedValue.ErrorType.PASSED_COMPLEX);
                }
                final double res = (functionType == FunctionType.MAX) ? FastMath.max(a0.getReal(), a1.getReal())
                        : FastMath.min(a0.getReal(), a1.getReal());
                return outValue.setValue(res);
            }
            case HYPOT:
                return outValue.hypot(a0, argVal[1]);

            case IF:
                if (a0.isComplex())
                {
                    return outValue.invalidate(CalculatedValue.ErrorType.PASSED_COMPLEX);
                }
                return outValue.assign((a0.getReal() > 0) ? argVal[1] : argVal[2]);

            case SIGNUM:
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

            case FUNCTION_LINK:
            case FUNCTION_INDEX:
                if (linkedFunction != null && linkedFunction.setArgumentValues(argVal))
                {
                    return linkedFunction.getValue(thread, outValue);
                }
                break;
            }
        }
        return outValue.invalidate(CalculatedValue.ErrorType.TERM_NOT_READY);
    }

    @Override
    public DifferentiableType isDifferentiable(String var)
    {
        if (functionType == null)
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
        switch (functionType)
        {
        // for these functions, derivative can be calculated analytically
        case IDENTITY:
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
        case LOG:
        case LOG10:
        case SQRT:
        case SQRT_LAYOUT:
        case ABS:
        case ABS_LAYOUT:
        case RE:
        case IM:
        case HYPOT:
            retValue = argsProp;
            break;
        // for this function, derivative depends on the function itself
        case FUNCTION_LINK:
            if (linkedFunction != null)
            {
                final int dGrad = Math.min(argsProp.ordinal(), linkedFunction.isDifferentiable(var).ordinal());
                retValue = DifferentiableType.values()[dGrad];
            }
            break;
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
        case RND:
        case MAX:
        case MIN:
        case IF:
        case SIGNUM:
        case FACTORIAL:
        case CONJUGATE_LAYOUT:
        case FUNCTION_INDEX:
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
        if (functionType != null && terms.size() > 0)
        {
            ensureArgValSize();
            for (int i = 0; i < terms.size(); i++)
            {
                terms.get(i).getValue(thread, argVal[i]);
            }
            final CalculatedValue a0 = argVal[0];
            terms.get(0).getDerivativeValue(var, thread, a0derVal);
            switch (functionType)
            {
            // for these functions, derivative can be calculated analytically
            case IDENTITY:
                return outValue.assign(a0derVal);
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
            case LOG: // (1.0 / a0) * a0'
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
            case HYPOT: // (a0 * a0' + a1 * a1')/hypot(a0, a1)
            {
                final CalculatedValue a1 = argVal[1];
                terms.get(1).getDerivativeValue(var, thread, a1derVal);
                final CalculatedValue tmp = new CalculatedValue();
                tmp.multiply(a1, a1derVal);
                outValue.multiply(a0, a0derVal);
                outValue.add(outValue, tmp);
                tmp.hypot(a0, a1);
                return outValue.divide(outValue, tmp);
            }
            case FUNCTION_LINK:
                if (linkedFunction != null && linkedFunction.setArgumentValues(argVal))
                {
                    if (linkedFunction.getArguments() == null || linkedFunction.getArguments().size() != terms.size())
                    {
                        return outValue.setValue(0.0);
                    }
                    outValue.setValue(0.0);
                    final CalculatedValue tmp = new CalculatedValue();
                    for (int i = 0; i < terms.size(); i++)
                    {
                        final String s = linkedFunction.getArguments().get(i);
                        linkedFunction.getDerivativeValue(s, thread, tmp);
                        if (i > 0)
                        {
                            terms.get(i).getDerivativeValue(var, thread, a0derVal);
                        }
                        tmp.multiply(tmp, a0derVal);
                        outValue.add(outValue, tmp);
                    }
                    return outValue.getValueType();
                }
                break;
            // these functions are not differentiable if contain the given argument
            case ATAN2:
            case CEIL:
            case FLOOR:
            case RND:
            case MAX:
            case MIN:
            case IF:
            case SIGNUM:
            case FACTORIAL:
            case CONJUGATE_LAYOUT:
            case FUNCTION_INDEX:
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
    public TermType getTermType()
    {
        return TermType.FUNCTION;
    }

    @Override
    public String getTermCode()
    {
        String t = getFunctionString(getContext(), getFunctionType());
        if (functionType.isLink())
        {
            t += "." + functionLinkName;
            if (terms.size() > 1)
            {
                t += FUNCTION_ARGS_MARKER + terms.size();
            }
        }
        return t;
    }

    @Override
    public boolean isContentValid(ValidationPassType type)
    {
        boolean isValid = true;
        switch (type)
        {
        case VALIDATE_SINGLE_FORMULA:
            linkedFunction = null;
            isValid = super.isContentValid(type);
            if (isValid && functionType.isLink())
            {
                FormulaBase f = getFormulaRoot().getFormulaList().getFormula(functionLinkName, terms.size(),
                        getFormulaRoot().getId(), false);
                ErrorCode errorCode = ErrorCode.NO_ERROR;
                if (f == null || !(f instanceof Equation))
                {
                    errorCode = (functionType == FunctionType.FUNCTION_LINK) ? ErrorCode.UNKNOWN_FUNCTION
                            : ErrorCode.UNKNOWN_ARRAY;
                    isValid = false;
                }
                else if (f.getId() == getFormulaRoot().getId())
                {
                    errorCode = ErrorCode.RECURSIVE_CALL;
                    isValid = false;
                }
                else if (functionType == FunctionType.FUNCTION_LINK && ((Equation) f).isArray())
                {
                    errorCode = ErrorCode.NOT_A_FUNCTION;
                    isValid = false;
                }
                else if (functionType == FunctionType.FUNCTION_INDEX && !((Equation) f).isArray()
                        && !((Equation) f).isInterval())
                {
                    errorCode = ErrorCode.NOT_AN_ARRAY;
                    isValid = false;
                }
                else
                {
                    linkedFunction = (Equation) f;
                }
                setErrorCode(errorCode, functionLinkName + "[" + terms.size() + "]");
                if (getFormulaRoot() instanceof LinkHolder && linkedFunction != null)
                {
                    if (!linkedFunction.isInterval())
                    {
                        ((LinkHolder) getFormulaRoot()).addLinkedEquation(linkedFunction);
                    }
                }
            }
            break;
        case VALIDATE_LINKS:
            isValid = super.isContentValid(type);
            break;
        }
        return isValid;
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
                switch (functionType)
                {
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
                CustomTextView.SymbolType s = (functionType == FunctionType.ABS_LAYOUT) ? CustomTextView.SymbolType.VERT_LINE
                        : CustomTextView.SymbolType.LEFT_BRACKET;
                v.prepare(s, getFormulaRoot().getFormulaList().getActivity(), this);
                v.setText("."); // this text defines view width/height
            }
            else if (t.equals(res.getString(R.string.formula_right_bracket_key)))
            {
                CustomTextView.SymbolType s = (functionType == FunctionType.ABS_LAYOUT) ? CustomTextView.SymbolType.VERT_LINE
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
            if (functionType == FunctionType.FUNCTION_INDEX
                    && val.equals(getContext().getResources().getString(R.string.formula_arg_term_key)))
            {
                final TermField t = addTerm(getFormulaRoot(), l, -1, v, this, getArgumentDepth());
                t.bracketsType = TermField.BracketsType.NEVER;
            }
            else if (functionType != FunctionType.FUNCTION_INDEX
                    && val.equals(getContext().getResources().getString(R.string.formula_arg_term_key)))
            {
                final TermField t = addTerm(getFormulaRoot(), l, -1, v, this, 0);
                t.bracketsType = (functionType == FunctionType.FACTORIAL || functionType == FunctionType.CONJUGATE_LAYOUT) ? TermField.BracketsType.ALWAYS
                        : TermField.BracketsType.NEVER;
            }
            else if (functionType == FunctionType.NTHRT_LAYOUT
                    && val.equals(getContext().getResources().getString(R.string.formula_left_term_key)))
            {
                final TermField t = addTerm(getFormulaRoot(), l, -1, v, this, 3);
                t.bracketsType = TermField.BracketsType.NEVER;
            }
            else if (functionType == FunctionType.NTHRT_LAYOUT
                    && val.equals(getContext().getResources().getString(R.string.formula_right_term_key)))
            {
                final TermField t = addTerm(getFormulaRoot(), l, -1, v, this, 0);
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
            if (functionType == FunctionType.SQRT_LAYOUT || functionType == FunctionType.NTHRT_LAYOUT)
            {
                functionTerm.setWidth(getFormulaList().getDimen().get(ScaledDimensions.Type.SMALL_SYMBOL_SIZE));
                functionTerm.setPadding(0, 0, 0, 0);
            }
            else if (functionType == FunctionType.CONJUGATE_LAYOUT)
            {
                functionTerm.setPadding(hsp, 0, hsp, 0);
            }
            else if (functionType == FunctionType.FUNCTION_INDEX)
            {
                functionTerm.setPadding(0, 0, 0, 0);
            }
            else
            {
                functionTerm.setPadding(0, 0, hsp, 0);
            }
        }
        if (functionType == FunctionType.NTHRT_LAYOUT)
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
        if (functionType == FunctionType.NTHRT_LAYOUT)
        {
            return terms.get(1);
        }
        return super.getArgumentTerm();
    }

    /*********************************************************
     * Implementation for methods for FormulaChangeIf interface
     *********************************************************/

    @Override
    public void onDelete(CustomEditText owner)
    {
        final TermField ownerTerm = findTerm(owner);

        if (functionType == FunctionType.NTHRT_LAYOUT || owner == null || terms.size() <= 1 || !isNewTermEnabled())
        {
            // search remaining text or term
            TermField remainingTerm = null;
            CharSequence remainingText = "";
            if (ownerTerm != null)
            {
                if (functionTerm != null)
                {
                    remainingText = getFunctionLabel();
                }
                for (TermField t : terms)
                {
                    if (t == ownerTerm)
                    {
                        continue;
                    }
                    if (t.isTerm())
                    {
                        remainingTerm = t;
                    }
                    else if (!t.isEmpty())
                    {
                        remainingText = t.getText();
                    }
                }
            }
            if (parentField != null && remainingTerm != null)
            {
                parentField.onTermDelete(removeElements(), remainingTerm);
            }
            else if (parentField != null)
            {
                parentField.onTermDeleteWithText(removeElements(), remainingText);
            }
            else
            {
                super.onDelete(null);
            }
        }
        else if (isNewTermEnabled())
        {
            if (parentField == null || ownerTerm == null)
            {
                return;
            }

            TermField prevTerm = deleteArgument(ownerTerm,
                    getContext().getResources().getString(R.string.formula_term_separator), /*storeUndoState=*/true);

            getFormulaRoot().getFormulaList().onManualInput();
            if (prevTerm != null)
            {
                prevTerm.requestFocus();
            }
        }
    }

    @Override
    public boolean isNewTermEnabled()
    {
        return functionType.isLink();
    }

    @Override
    public boolean onNewTerm(TermField owner, String s, boolean requestFocus)
    {
        final String sep = getContext().getResources().getString(R.string.formula_term_separator);
        if (s == null || s.length() == 0 || !s.contains(sep))
        {
            // string does not contains the term separator: can not be processed
            return false;
        }

        // below, we will return true since the string is processed independently from
        // the result
        if (!isNewTermEnabled())
        {
            return true;
        }

        TermField newArg = addArgument(owner, R.layout.formula_function_add_arg, getArgumentDepth());
        if (newArg == null)
        {
            return true;
        }

        updateTextSize();
        if (owner.getText().contains(sep))
        {
            TermField.divideString(s, sep, owner, newArg);
        }
        isContentValid(ValidationPassType.VALIDATE_SINGLE_FORMULA);
        if (requestFocus)
        {
            newArg.getEditText().requestFocus();
        }
        return true;
    }

    /*********************************************************
     * FormulaTermFunction-specific methods
     *********************************************************/

    private int getArgumentDepth()
    {
        return functionType == FunctionType.FUNCTION_INDEX ? 3 : 0;
    }

    /**
     * Procedure creates the formula layout
     */
    private void onCreate(String s, int idx) throws Exception
    {
        if (idx < 0 || idx > layout.getChildCount())
        {
            throw new Exception("cannot create FormulaFunction for invalid insertion index " + idx);
        }
        functionType = getFunctionType(getContext(), s);
        if (functionType == null)
        {
            throw new Exception("cannot create FormulaFunction for unknown function");
        }
        int argNumber = functionType.getArgNumber();
        switch (functionType)
        {
        case FUNCTION_LINK:
            functionLinkName = getFunctionLinkName(s, functionType.getLinkObject(),
                    R.string.formula_function_start_bracket);
            if (functionLinkName == null)
            {
                throw new Exception("cannot create FormulaFunction(FUNCTION_LINK) since function name is invalid");
            }
            inflateElements(R.layout.formula_function_named, true);
            argNumber = getArgNumber(s, functionLinkName);
            break;
        case FUNCTION_INDEX:
            functionLinkName = getFunctionLinkName(s, functionType.getLinkObject(),
                    R.string.formula_function_start_index);
            if (functionLinkName == null)
            {
                throw new Exception("cannot create FormulaFunction(INDEX) since function name is invalid");
            }
            inflateElements(R.layout.formula_function_index, true);
            argNumber = getArgNumber(s, functionLinkName);
            break;
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
        case IDENTITY:
            inflateElements(R.layout.formula_function_noname, true);
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

        // store the main layout in order to show errors
        final String functionMainTag = getContext().getResources().getString(R.string.function_main_layout);
        final View functionMainView = layout.findViewWithTag(functionMainTag);
        if (functionMainView != null)
        {
            functionMainLayout = (CustomLayout) functionMainView;
            functionMainLayout.setTag("");
        }

        // add additional arguments
        while (terms.size() < argNumber)
        {
            TermField newTerm = addArgument(terms.get(terms.size() - 1), R.layout.formula_function_add_arg,
                    getArgumentDepth());
            if (newTerm == null)
            {
                break;
            }
        }
        if (functionType.getArgNumber() > 0 && terms.size() != functionType.getArgNumber())
        {
            throw new Exception("invalid size for argument list");
        }

        // special text properties
        if (functionType == FunctionType.IF)
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
                    // nothig to do
                }
                break;
            }
        }
    }

    /**
     * Procedure extracts linked function name from given string
     */
    private String getFunctionLinkName(String s, String linkName, int bracketId)
    {
        try
        {
            if (s.contains(getContext().getResources().getString(bracketId)))
            {
                String opCode = getContext().getResources().getString(bracketId);
                return s.substring(0, s.indexOf(opCode)).trim();
            }
            if (s.contains(linkName))
            {
                final String opCode = linkName + ".";
                final String nameAndArgs = s.substring(s.indexOf(opCode) + opCode.length(), s.length());
                if (nameAndArgs != null && nameAndArgs.length() > 0)
                {
                    final int argsMarker = nameAndArgs.indexOf(FUNCTION_ARGS_MARKER);
                    if (argsMarker > 0)
                    {
                        return nameAndArgs.substring(0, argsMarker);
                    }
                }
                return nameAndArgs;
            }
        }
        catch (Exception ex)
        {
            // nothig to do
        }
        return null;
    }

    /**
     * Procedure extracts number of arguments from given string
     */
    private int getArgNumber(String s, String functionName)
    {
        try
        {
            String opCode = null;
            for (FunctionType f : FunctionType.values())
            {
                if (f.isLink() && s.contains(f.getLinkObject()))
                {
                    opCode = f.getLinkObject() + ".";
                    break;
                }
            }
            if (opCode != null)
            {
                final String nameAndArgs = s.substring(s.indexOf(opCode) + opCode.length(), s.length());
                if (nameAndArgs != null && nameAndArgs.length() > 0)
                {
                    final int argsMarker = nameAndArgs.indexOf(FUNCTION_ARGS_MARKER);
                    if (argsMarker > 0)
                    {
                        final String argsStr = nameAndArgs.substring(argsMarker + FUNCTION_ARGS_MARKER.length(),
                                nameAndArgs.length());
                        return Integer.parseInt(argsStr);
                    }
                }
                return 1;
            }
            else if (!s.contains(getContext().getResources().getString(R.string.formula_term_separator)))
            {
                FormulaBase f = getFormulaRoot().getFormulaList().getFormula(functionName, ViewUtils.INVALID_INDEX,
                        getFormulaRoot().getId(), true);
                if (f != null && f instanceof Equation)
                {
                    ArrayList<String> args = ((Equation) f).getArguments();
                    if (args != null && !args.isEmpty())
                    {
                        return args.size();
                    }
                }
            }
        }
        catch (Exception ex)
        {
            // nothig to do
        }
        return 1;
    }

    /**
     * Returns function type
     */
    public FunctionType getFunctionType()
    {
        return functionType;
    }

    /**
     * Returns function term
     */
    public CustomTextView getFunctionTerm()
    {
        return functionTerm;
    }

    private void setErrorCode(ErrorCode errorCode, String addInfo)
    {
        if (functionTerm != null)
        {
            functionTerm.setTextColor(CompatUtils.getColor(getContext(), R.color.formula_text_color));
        }
        if (parentField != null)
        {
            String errorMsg = null;
            switch (errorCode)
            {
            case NO_ERROR:
                // nothing to do
                break;
            case UNKNOWN_FUNCTION:
            case UNKNOWN_ARRAY:
            case NOT_AN_ARRAY:
            case NOT_A_FUNCTION:
            case NOT_DIFFERENTIABLE:
                errorMsg = String.format(errorCode.getDescription(getContext()), addInfo);
                break;
            case RECURSIVE_CALL:
                errorMsg = errorCode.getDescription(getContext());
                break;
            }
            parentField.setError(errorMsg, ErrorNotification.PARENT_LAYOUT, functionMainLayout);
        }
    }

    private void ensureArgValSize()
    {
        final int termsSize = terms.size();
        if (argVal == null || argVal.length != termsSize)
        {
            argVal = new CalculatedValue[termsSize];
            for (int i = 0; i < termsSize; i++)
            {
                argVal[i] = new CalculatedValue();
            }
        }
    }

    private String getFunctionLabel()
    {
        switch (functionType)
        {
        case FUNCTION_LINK:
        case FUNCTION_INDEX:
            return functionLinkName;
        case IDENTITY:
        case SQRT_LAYOUT:
        case NTHRT_LAYOUT:
        case FACTORIAL:
        case ABS_LAYOUT:
        case CONJUGATE_LAYOUT:
            return "";
        default:
            return functionType.toString().toLowerCase(Locale.ENGLISH);
        }
    }

}
