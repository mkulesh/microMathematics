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
import com.mkulesh.micromath.R;
import com.mkulesh.micromath.widgets.CustomEditText;

import org.apache.commons.math3.util.FastMath;

import java.util.Locale;

public class LogFunctions extends FunctionBase
{
    public TermTypeIf.GroupType getGroupType()
    {
        return TermTypeIf.GroupType.LOG_FUNCTIONS;
    }

    /**
     * Supported functions
     */
    public enum FunctionType implements ObsoleteFunctionIf
    {
        EXP(1, R.drawable.p_function_exp, R.string.math_function_exp),
        LN(1, R.drawable.p_function_ln, R.string.math_function_ln, 1, "LOG"),
        LOG(2, R.drawable.p_function_log, R.string.math_function_log),
        LOG10(1, R.drawable.p_function_log10, R.string.math_function_log10),
        SINH(1, R.drawable.p_function_sinh, R.string.math_function_sinh),
        COSH(1, R.drawable.p_function_cosh, R.string.math_function_cosh),
        TANH(1, R.drawable.p_function_tanh, R.string.math_function_tanh),
        CSCH(1, R.drawable.p_function_csch, R.string.math_function_csch),
        SECH(1, R.drawable.p_function_sech, R.string.math_function_sech),
        COTH(1, R.drawable.p_function_coth, R.string.math_function_coth);

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
            return GroupType.LOG_FUNCTIONS;
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
            return new LogFunctions(this, termField, layout, s, textIndex);
        }
    }

    /**
     * Private attributes
     */
    // Attention: this is not thread-safety declaration!
    private final CalculatedValue a0derVal = new CalculatedValue(), tmpVal = new CalculatedValue();

    /*--------------------------------------------------------*
     * Constructors
     *--------------------------------------------------------*/

    private LogFunctions(FunctionType type, TermField owner, LinearLayout layout, String s, int idx) throws Exception
    {
        super(owner, layout);
        termType = type;
        createGeneralFunction(R.layout.formula_function_named, s, getFunctionType().getArgNumber(), idx, owner.isPasteFromClipboard());
    }

    /*--------------------------------------------------------*
     * GUI constructors to avoid lint warning
     *--------------------------------------------------------*/

    public LogFunctions(Context context)
    {
        super();
    }

    public LogFunctions(Context context, AttributeSet attrs)
    {
        super();
    }

    /*--------------------------------------------------------*
     * Common getters
     *--------------------------------------------------------*/

    private FunctionType getFunctionType()
    {
        return (FunctionType) termType;
    }

    /*--------------------------------------------------------*
     * Re-implementation for methods for FormulaBase and FormulaTerm superclass's
     *--------------------------------------------------------*/

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
            case SINH:
                return outValue.sinh(a0);
            case COSH:
                return outValue.cosh(a0);
            case TANH:
                return outValue.tanh(a0);

            case CSCH:
                return outValue.csch(a0);
            case SECH:
                return outValue.sech(a0);
            case COTH:
                return outValue.coth(a0);

            case EXP:
                return outValue.exp(a0);
            case LN:
                return outValue.log(a0);
            case LOG:
                outValue.log(a0);
                tmpVal.log(argVal[1]);
                return outValue.divide(outValue, tmpVal);
            case LOG10:
                return outValue.log10(a0);
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
        // for this function, derivative depends on the function itself
        if (getFunctionType() == FunctionType.LOG)
        {
            // log root is only differentiable if the base does not depend on the given argument
            retValue = argsProp;
            if (retValue != DifferentiableType.INDEPENDENT)
            {
                final DifferentiableType powValue =
                        DifferentiableType.values()[terms.get(1).isDifferentiable(var).ordinal()];
                retValue = (powValue == DifferentiableType.INDEPENDENT) ?
                        retValue : DifferentiableType.NONE;
            }
        }
        else
        {// for all other functions, derivative can be calculated analytically
            retValue = argsProp;
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
            case SINH: // cosh(a0) * a0'
                outValue.cosh(a0);
                return outValue.multiply(outValue, a0derVal);
            case COSH: // sinh(a0) * a0'
                outValue.sinh(a0);
                return outValue.multiply(outValue, a0derVal);
            case TANH: // (1.0 / (cosh(a0) * cosh(a0))) * a0'
                outValue.cosh(a0);
                outValue.multiply(outValue, outValue);
                outValue.divide(CalculatedValue.ONE, outValue);
                return outValue.multiply(outValue, a0derVal);

            case CSCH: // -1.0 * coth(a0) * csch(a0) * a0'
                outValue.coth(a0);
                tmpVal.csch(a0);
                outValue.multiply(outValue, tmpVal);
                outValue.multiply(CalculatedValue.MINUS_ONE, outValue);
                return outValue.multiply(outValue, a0derVal);
            case SECH: // -1.0 * tanh(a0) * sech(a0) * a0'
                outValue.tanh(a0);
                tmpVal.sech(a0);
                outValue.multiply(outValue, tmpVal);
                outValue.multiply(CalculatedValue.MINUS_ONE, outValue);
                return outValue.multiply(outValue, a0derVal);
            case COTH: // (-1.0 / (sinh(a0) * sinh(a0))) * a0'
                outValue.sinh(a0);
                outValue.multiply(outValue, outValue);
                outValue.divide(CalculatedValue.MINUS_ONE, outValue);
                return outValue.multiply(outValue, a0derVal);

            case EXP: // exp(a0) * a0'
                outValue.exp(a0);
                return outValue.multiply(outValue, a0derVal);
            case LN: // (1.0 / a0) * a0'
                outValue.divide(CalculatedValue.ONE, a0);
                return outValue.multiply(outValue, a0derVal);
            case LOG: // (1.0 / (a0 * FastMath.log(a1))) * a0'
                tmpVal.log(argVal[1]);
                outValue.multiply(a0, tmpVal);
                outValue.divide(CalculatedValue.ONE, outValue);
                return outValue.multiply(outValue, a0derVal);
            case LOG10: // (1.0 / (a0 * FastMath.log(10.0))) * a0'
                outValue.assign(a0);
                outValue.multiply(FastMath.log(10.0));
                outValue.divide(CalculatedValue.ONE, outValue);
                return outValue.multiply(outValue, a0derVal);
            }
        }
        return outValue.invalidate(CalculatedValue.ErrorType.TERM_NOT_READY);
    }
}
