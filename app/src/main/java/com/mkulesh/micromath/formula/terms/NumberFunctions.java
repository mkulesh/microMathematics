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
import org.apache.commons.math3.util.Precision;

import java.util.Locale;

public class NumberFunctions extends FunctionBase
{
    public TermTypeIf.GroupType getGroupType()
    {
        return TermTypeIf.GroupType.NUMBER_FUNCTIONS;
    }

    /**
     * Supported functions
     */
    public enum FunctionType implements ObsoleteFunctionIf
    {
        MAX(2, R.drawable.p_function_max, R.string.math_function_max),
        MIN(2, R.drawable.p_function_min, R.string.math_function_min),
        MOD(2, R.drawable.p_function_mod, R.string.math_function_mod),
        PERC(2, R.drawable.p_function_perc, R.string.math_function_perc),
        RANDOM(1, R.drawable.p_function_random, R.string.math_function_random, 1, "RND"),
        CEIL(1, R.drawable.p_function_ceil, R.string.math_function_ceil),
        FLOOR(1, R.drawable.p_function_floor, R.string.math_function_floor),
        ROUND(2, R.drawable.p_function_round, R.string.math_function_round),
        TRUNC(1, R.drawable.p_function_trunc, R.string.math_function_trunc),
        SIGN(1, R.drawable.p_function_sign, R.string.math_function_sign, 1, "SIGNUM");

        private final int argNumber;
        private final int imageId;
        private final int descriptionId;
        private final String lowerCaseName;
        private final int obsoleteVersion;
        private final String obsoleteCode;

        FunctionType(int argNumber, int imageId, int descriptionId)
        {
            this(argNumber, imageId, descriptionId, Integer.MIN_VALUE, null);
        }

        FunctionType(int argNumber, int imageId, int descriptionId, int obsoleteVersion, String obsoleteCode)
        {
            this.argNumber = argNumber;
            this.imageId = imageId;
            this.descriptionId = descriptionId;
            this.lowerCaseName = name().toLowerCase(Locale.ENGLISH);
            this.obsoleteVersion = obsoleteVersion;
            this.obsoleteCode = obsoleteCode == null ? null : obsoleteCode.toLowerCase(Locale.ENGLISH);
        }

        public GroupType getGroupType()
        {
            return GroupType.NUMBER_FUNCTIONS;
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

        public int getObsoleteVersion()
        {
            return obsoleteVersion;
        }

        public String getObsoleteCode()
        {
            return obsoleteCode;
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
            return new NumberFunctions(this, termField, layout, s, textIndex);
        }
    }

    /**
     * Private attributes
     */
    // Attention: this is not thread-safety declaration!
    private final CalculatedValue tmpVal = new CalculatedValue();

    /*********************************************************
     * Constructors
     *********************************************************/

    private NumberFunctions(FunctionType type, TermField owner, LinearLayout layout, String s, int idx) throws Exception
    {
        super(owner, layout);
        termType = type;
        createGeneralFunction(R.layout.formula_function_named, s, getFunctionType().getArgNumber(), idx, owner.isPasteFromClipboard());
    }

    /*********************************************************
     * GUI constructors to avoid lint warning
     *********************************************************/

    public NumberFunctions(Context context)
    {
        super();
    }

    public NumberFunctions(Context context, AttributeSet attrs)
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
            case CEIL:
                return outValue.ceil(a0);
            case FLOOR:
                return outValue.floor(a0);
            case ROUND:
            {
                final CalculatedValue a1 = argVal[1];
                if (a0.isComplex() || a1.isComplex())
                {
                    return outValue.invalidate(CalculatedValue.ErrorType.PASSED_COMPLEX);
                }
                return outValue.setValue(Precision.round(a0.getReal(), a1.getInteger()));
            }
            case TRUNC:
            {
                if (a0.isComplex())
                {
                    return outValue.invalidate(CalculatedValue.ErrorType.PASSED_COMPLEX);
                }
                return outValue.setValue(a0.getInteger());
            }

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

            case SIGN:
                if (a0.isComplex())
                {
                    return outValue.invalidate(CalculatedValue.ErrorType.PASSED_COMPLEX);
                }
                return outValue.setValue(FastMath.signum(a0.getReal()));

            case MOD:
            {
                final CalculatedValue a1 = argVal[1];
                if (a0.isComplex() || a1.isComplex())
                {
                    return outValue.invalidate(CalculatedValue.ErrorType.PASSED_COMPLEX);
                }
                return outValue.setValue(a0.getReal() % a1.getReal());
            }

            case PERC:
            {
                tmpVal.assign(argVal[1]);
                tmpVal.multiply(0.01);
                return outValue.multiply(tmpVal, a0);
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

        CalculatableIf.DifferentiableType retValue = (argsProp == CalculatableIf.DifferentiableType.INDEPENDENT) ?
                CalculatableIf.DifferentiableType.INDEPENDENT : CalculatableIf.DifferentiableType.NONE;

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
            terms.get(0).getDerivativeValue(var, thread, tmpVal);
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
        return outValue.invalidate(CalculatedValue.ErrorType.TERM_NOT_READY);
    }
}
