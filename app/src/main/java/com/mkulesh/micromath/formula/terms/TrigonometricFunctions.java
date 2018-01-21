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
import android.content.res.Resources;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.mkulesh.micromath.formula.CalculatableIf;
import com.mkulesh.micromath.formula.CalculaterTask;
import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.formula.FormulaTermTypeIf;
import com.mkulesh.micromath.formula.Palette;
import com.mkulesh.micromath.formula.TermField;
import com.mkulesh.micromath.math.CalculatedValue;
import com.mkulesh.micromath.plus.R;

import org.apache.commons.math3.util.FastMath;

import java.util.Locale;

public class TrigonometricFunctions extends FunctionBase
{
    public FormulaTermTypeIf.GroupType getGroupType()
    {
        return FormulaTermTypeIf.GroupType.TRIGONOMETRIC_FUNCTIONS;
    }

    /**
     * Supported functions
     */
    public enum FunctionType implements FormulaTermTypeIf
    {
        SIN(1, R.drawable.p_function_sin, R.string.math_function_sin),
        ASIN(1, R.drawable.p_function_asin, R.string.math_function_asin),
        COS(1, R.drawable.p_function_cos, R.string.math_function_cos),
        ACOS(1, R.drawable.p_function_acos, R.string.math_function_acos),
        TAN(1, R.drawable.p_function_tan, R.string.math_function_tan),
        ATAN(1, R.drawable.p_function_atan, R.string.math_function_atan),
        ATAN2(2, R.drawable.p_function_atan2, R.string.math_function_atan2);

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

        public GroupType getGroupType() { return GroupType.TRIGONOMETRIC_FUNCTIONS; }

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

    public static FunctionType getFunctionType(Context context, String s)
    {
        String fName = null;
        final Resources res = context.getResources();

        // cat the function name
        final String startBracket = res.getString(R.string.formula_function_start_bracket);
        if (s.contains(startBracket))
        {
            fName = s.substring(0, s.indexOf(startBracket)).trim();
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

        return null;
    }

    /**
     * Private attributes
     */
    // Attention: this is not thread-safety declaration!
    private final CalculatedValue a0derVal = new CalculatedValue();

    /*********************************************************
     * Constructors
     *********************************************************/

    public TrigonometricFunctions(FormulaTermTypeIf type, TermField owner, LinearLayout layout, String s, int idx) throws Exception
    {
        super(owner, layout);
        termType = (type instanceof FunctionType)? (FunctionType) type : null;
        if (termType == null)
        {
            throw new Exception("cannot create " + getGroupType().toString() + " for unknown type");
        }
        createGeneralFunction(R.layout.formula_function_named, s, getFunctionType().getArgNumber(), idx);
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
            case ASIN:
                return outValue.asin(a0);

            case COS:
                return outValue.cos(a0);
            case ACOS:
                return outValue.acos(a0);

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
        case ASIN:
        case COS:
        case ACOS:
        case TAN:
        case ATAN:
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
            case ASIN: // (1.0 / sqrt(1.0 - a0 * a0)) * a0'
                outValue.multiply(a0, a0);
                outValue.subtract(CalculatedValue.ONE, outValue);
                outValue.sqrt(outValue);
                outValue.divide(CalculatedValue.ONE, outValue);
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
