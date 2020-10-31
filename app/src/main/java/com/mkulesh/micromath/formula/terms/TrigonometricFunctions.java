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
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.mkulesh.micromath.formula.CalculatableIf;
import com.mkulesh.micromath.formula.CalculaterTask;
import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.formula.FormulaTerm;
import com.mkulesh.micromath.formula.Palette;
import com.mkulesh.micromath.formula.PaletteButton;
import com.mkulesh.micromath.formula.TermField;
import com.mkulesh.micromath.math.CalculatedValue;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.widgets.CustomEditText;

import org.apache.commons.math3.util.FastMath;

import java.util.Locale;

public class TrigonometricFunctions extends FunctionBase
{
    public TermTypeIf.GroupType getGroupType()
    {
        return TermTypeIf.GroupType.TRIGONOMETRIC_FUNCTIONS;
    }

    /**
     * Supported functions
     */
    public enum FunctionType implements TermTypeIf
    {
        SIN(1, R.drawable.p_function_sin, R.string.math_function_sin),
        COS(1, R.drawable.p_function_cos, R.string.math_function_cos),
        TAN(1, R.drawable.p_function_tan, R.string.math_function_tan),
        CSC(1, R.drawable.p_function_csc, R.string.math_function_csc),
        SEC(1, R.drawable.p_function_sec, R.string.math_function_sec),
        COT(1, R.drawable.p_function_cot, R.string.math_function_cot),
        ASIN(1, R.drawable.p_function_asin, R.string.math_function_asin),
        ACOS(1, R.drawable.p_function_acos, R.string.math_function_acos),
        ATAN(1, R.drawable.p_function_atan, R.string.math_function_atan),
        ATAN2(2, R.drawable.p_function_atan2, R.string.math_function_atan2),
        ACSC(1, R.drawable.p_function_acsc, R.string.math_function_acsc),
        ASEC(1, R.drawable.p_function_asec, R.string.math_function_asec),
        ACOT(1, R.drawable.p_function_acot, R.string.math_function_acot);

        private final int argNumber;
        private final int imageId;
        private final int descriptionId;
        private final String lowerCaseName;

        FunctionType(int argNumber, int imageId, int descriptionId)
        {
            this.argNumber = argNumber;
            this.imageId = imageId;
            this.descriptionId = descriptionId;
            this.lowerCaseName = name().toLowerCase(Locale.ENGLISH);
        }

        public GroupType getGroupType()
        {
            return GroupType.TRIGONOMETRIC_FUNCTIONS;
        }

        public int getShortCutId()
        {
            return Palette.NO_BUTTON;
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
            return new TrigonometricFunctions(this, termField, layout, s, textIndex);
        }
    }

    /**
     * Private attributes
     */
    // Attention: this is not thread-safety declaration!
    private final CalculatedValue a0derVal = new CalculatedValue(), tmpVal = new CalculatedValue();

    /*********************************************************
     * Constructors
     *********************************************************/

    private TrigonometricFunctions(FunctionType type, TermField owner, LinearLayout layout, String s, int idx) throws Exception
    {
        super(owner, layout);
        termType = type;
        createGeneralFunction(R.layout.formula_function_named, s, getFunctionType().getArgNumber(), idx, owner.isPasteFromClipboard());
    }

    /*********************************************************
     * GUI constructors to avoid lint warning
     *********************************************************/

    public TrigonometricFunctions(Context context)
    {
        super();
    }

    public TrigonometricFunctions(Context context, AttributeSet attrs)
    {
        super();
    }

    /*********************************************************
     * Common getters
     *********************************************************/

    private FunctionType getFunctionType()
    {
        return (FunctionType) termType;
    }

    /*********************************************************
     * Re-implementation for methods for FormulaBase and FormulaTerm superclass's
     *********************************************************/

    @Override
    protected String getFunctionLabel()
    {
        return termType.getLowerCaseName();
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
            case SIN:
                return outValue.sin(a0);
            case CSC:
                return outValue.csc(a0);
            case ASIN:
                return outValue.asin(a0);
            case ACSC:
            {
                outValue.divide(CalculatedValue.ONE, a0);
                return outValue.asin(outValue);
            }

            case COS:
                return outValue.cos(a0);
            case SEC:
                return outValue.sec(a0);
            case ACOS:
                return outValue.acos(a0);
            case ASEC:
            {
                outValue.divide(CalculatedValue.ONE, a0);
                return outValue.acos(outValue);
            }

            case TAN:
                return outValue.tan(a0);
            case COT:
                return outValue.cot(a0);
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
            case ACOT:
            {
                outValue.atan(a0);
                tmpVal.setValue(FastMath.PI / 2.0f);
                return outValue.subtract(tmpVal, outValue);
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
        case SIN:
        case CSC:
        case ASIN:
        case ACSC:
        case COS:
        case SEC:
        case ACOS:
        case ASEC:
        case TAN:
        case COT:
        case ATAN:
        case ACOT:
            retValue = argsProp;
            break;
        // these functions are not differentiable if contain the given argument
        case ATAN2:
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
            case SIN: // cos(a0) * a0'
                outValue.cos(a0);
                return outValue.multiply(outValue, a0derVal);
            case CSC: // -csc(a0) * cot(a0) * a0'
                outValue.csc(a0);
                tmpVal.cot(a0);
                outValue.multiply(outValue, tmpVal);
                outValue.multiply(-1.0);
                return outValue.multiply(outValue, a0derVal);
            case ASIN: // (1.0 / sqrt(1.0 - a0 * a0)) * a0'
                outValue.multiply(a0, a0);
                outValue.subtract(CalculatedValue.ONE, outValue);
                outValue.sqrt(outValue);
                outValue.divide(CalculatedValue.ONE, outValue);
                return outValue.multiply(outValue, a0derVal);
            case ACSC: // -1/(z^2 * sqrt(1 - 1/z^2))
                outValue.multiply(a0, a0);
                outValue.divide(CalculatedValue.ONE, outValue);
                outValue.subtract(CalculatedValue.ONE, outValue);
                outValue.sqrt(outValue);
                outValue.multiply(outValue, a0);
                outValue.multiply(outValue, a0);
                outValue.divide(CalculatedValue.ONE, outValue);
                outValue.multiply(-1.0);
                return outValue.multiply(outValue, a0derVal);
            case COS: // -1 * sin(a0) * a0'
                outValue.sin(a0);
                outValue.multiply(-1.0);
                return outValue.multiply(outValue, a0derVal);
            case SEC: // sec(a0) * tan(a0) * a0'
                outValue.sec(a0);
                tmpVal.tan(a0);
                outValue.multiply(outValue, tmpVal);
                return outValue.multiply(outValue, a0derVal);
            case ACOS: // (-1.0 / sqrt(1.0 - a0 * a0)) * a0'
                outValue.multiply(a0, a0);
                outValue.subtract(CalculatedValue.ONE, outValue);
                outValue.sqrt(outValue);
                outValue.divide(CalculatedValue.MINUS_ONE, outValue);
                return outValue.multiply(outValue, a0derVal);
            case ASEC: // +1/(z^2 * sqrt(1 - 1/z^2))
                outValue.multiply(a0, a0);
                outValue.divide(CalculatedValue.ONE, outValue);
                outValue.subtract(CalculatedValue.ONE, outValue);
                outValue.sqrt(outValue);
                outValue.multiply(outValue, a0);
                outValue.multiply(outValue, a0);
                outValue.divide(CalculatedValue.ONE, outValue);
                return outValue.multiply(outValue, a0derVal);
            case TAN: // (1.0 + tan(a0) * tan(a0)) * a0'
                outValue.tan(a0);
                outValue.multiply(outValue, outValue);
                outValue.add(CalculatedValue.ONE, outValue);
                return outValue.multiply(outValue, a0derVal);
            case COT: // - csc^2(a0) * a0
                outValue.csc(a0);
                tmpVal.csc(a0);
                outValue.multiply(outValue, tmpVal);
                outValue.multiply(-1.0);
                return outValue.multiply(outValue, a0derVal);
            case ATAN: // (1.0 / (1.0 + a0 * a0)) * a0'
                outValue.multiply(a0, a0);
                outValue.add(CalculatedValue.ONE, outValue);
                outValue.divide(CalculatedValue.ONE, outValue);
                return outValue.multiply(outValue, a0derVal);
            case ACOT: // (-1.0 / (1.0 + a0 * a0)) * a0'
                outValue.multiply(a0, a0);
                outValue.add(CalculatedValue.ONE, outValue);
                outValue.divide(CalculatedValue.ONE, outValue);
                outValue.multiply(-1.0);
                return outValue.multiply(outValue, a0derVal);
            // these functions are not differentiable if contain the given argument
            case ATAN2:
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
}
