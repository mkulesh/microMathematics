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
import com.mkulesh.micromath.properties.DocumentProperties;

import org.apache.commons.math3.util.FastMath;

import java.util.Locale;

public class LogFunctions extends FunctionBase
{
    public FormulaTermTypeIf.GroupType getGroupType()
    {
        return FormulaTermTypeIf.GroupType.LOG_FUNCTIONS;
    }

    /**
     * Supported functions
     */
    public enum FunctionType implements FormulaTermTypeIf
    {
        EXP(1, R.drawable.p_function_exp, R.string.math_function_exp),
        LN(1, R.drawable.p_function_ln, R.string.math_function_ln),
        LOG10(1, R.drawable.p_function_log10, R.string.math_function_log10),
        SINH(1, R.drawable.p_function_sinh, R.string.math_function_sinh),
        COSH(1, R.drawable.p_function_cosh, R.string.math_function_cosh),
        TANH(1, R.drawable.p_function_tanh, R.string.math_function_tanh);

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

        public GroupType getGroupType() { return GroupType.LOG_FUNCTIONS; }

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
     * Some functions are obsolete. This enumeration defines its back-compatibility
     */
    private enum ObsoleteCodes
    {
        LOG(1, FunctionType.LN);

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

    public LogFunctions(TermField owner, LinearLayout layout, String s, int idx) throws Exception
    {
        super(owner, layout);
        onCreate(s, idx);
    }

    /*********************************************************
     * GUI constructors to avoid lint warning
     *********************************************************/

    public LogFunctions(Context context)
    {
        super();
    }

    public LogFunctions(Context context, AttributeSet attrs)
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
            case SINH:
                return outValue.sinh(a0);
            case COSH:
                return outValue.cosh(a0);
            case TANH:
                return outValue.tanh(a0);

            case EXP:
                return outValue.exp(a0);
            case LN:
                return outValue.log(a0);
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

        CalculatableIf.DifferentiableType retValue = argsProp;

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
            }
        }
        return outValue.invalidate(CalculatedValue.ErrorType.TERM_NOT_READY);
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
            throw new Exception("cannot create LogFunction for invalid insertion index " + idx);
        }
        termType = getFunctionType(getContext(), s);
        if (termType == null)
        {
            throw new Exception("cannot create LogFunction for unknown function");
        }
        createGeneralFunction(R.layout.formula_function_named, s, getFunctionType().getArgNumber(), idx);
    }
}
