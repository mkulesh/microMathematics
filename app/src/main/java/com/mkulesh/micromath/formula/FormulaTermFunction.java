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

import com.mkulesh.micromath.R;
import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.formula.TermField.ErrorNotification;
import com.mkulesh.micromath.utils.CompatUtils;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomLayout;
import com.mkulesh.micromath.widgets.CustomTextView;
import com.mkulesh.micromath.widgets.ScaledDimensions;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.FastMath;

import java.util.Locale;

public class FormulaTermFunction extends FormulaTerm
{
    /**
     * Supported functions
     */
    public enum FunctionType
    {
        IDENTITY(1, R.drawable.p_function_identity, R.string.math_function_identity),
        ABS_LAYOUT(1, R.drawable.p_function_abs, R.string.math_function_abs),
        SQRT_LAYOUT(1, R.drawable.p_function_sqrt, R.string.math_function_sqrt),
        FACTORIAL(1, R.drawable.p_function_factorial, R.string.math_function_factorial),
        SIN(1, R.drawable.p_function_sin, R.string.math_function_sin),
        ASIN(1, R.drawable.p_function_asin, R.string.math_function_asin),
        SINH(1, R.drawable.p_function_sinh, R.string.math_function_sinh),
        COS(1, R.drawable.p_function_cos, R.string.math_function_cos),
        ACOS(1, R.drawable.p_function_acos, R.string.math_function_acos),
        COSH(1, R.drawable.p_function_cosh, R.string.math_function_cosh),
        TAN(1, R.drawable.p_function_tan, R.string.math_function_tan),
        ATAN(1, R.drawable.p_function_atan, R.string.math_function_atan),
        TANH(1, R.drawable.p_function_tanh, R.string.math_function_tanh),
        EXP(1, R.drawable.p_function_exp, R.string.math_function_exp),
        LOG(1, R.drawable.p_function_log, R.string.math_function_log),
        LOG10(1, R.drawable.p_function_log10, R.string.math_function_log10),
        CEIL(1, R.drawable.p_function_ceil, R.string.math_function_ceil),
        FLOOR(1, R.drawable.p_function_floor, R.string.math_function_floor),
        RND(1, R.drawable.p_function_rnd, R.string.math_function_rnd),
        SQRT(1, Palette.NO_BUTTON, Palette.NO_BUTTON),
        ABS(1, Palette.NO_BUTTON, Palette.NO_BUTTON),
        SIGNUM(1, Palette.NO_BUTTON, Palette.NO_BUTTON),
        FUNCTION_LINK(-1, Palette.NO_BUTTON, Palette.NO_BUTTON);

        private final int argNumber;
        private final int imageId;
        private final int descriptionId;

        private FunctionType(int argNumber, int imageId, int descriptionId)
        {
            this.argNumber = argNumber;
            this.imageId = imageId;
            this.descriptionId = descriptionId;
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
    }

    /**
     * Some functions can be triggered from keyboard. This enumeration defines these triggers
     */
    enum Trigger
    {
        GENERAL(R.string.formula_function_start_bracket, null, true),
        ABS(R.string.formula_function_abs_layout, FunctionType.ABS_LAYOUT, true),
        SQRT(R.string.formula_function_sqrt_layout, FunctionType.SQRT_LAYOUT, true),
        FACTORIAL(R.string.formula_function_factorial_layout, FunctionType.FACTORIAL, false);

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

    public static final String FUNCTION_LINK_OBJECT = "content:com.mkulesh.micromath.link";

    public static FunctionType getFunctionType(Context context, String s)
    {
        FunctionType retValue = null;
        String fName = null;
        final Resources res = context.getResources();
        final String startBracket = res.getString(R.string.formula_function_start_bracket);
        if (s.contains(startBracket))
        {
            fName = s.substring(0, s.indexOf(startBracket)).trim();
        }
        for (FunctionType f : FunctionType.values())
        {
            if (s.equals(f.toString().toLowerCase(Locale.ENGLISH)))
            {
                retValue = f;
                break;
            }
            if (fName != null && fName.equals(f.toString().toLowerCase(Locale.ENGLISH)))
            {
                retValue = f;
                break;
            }
            if (fName != null && fName.length() == 0 && f == FunctionType.IDENTITY)
            {
                // an identity function (just brackets) is a special case of a function
                retValue = f;
                break;
            }
        }
        if (retValue == null)
        {
            for (Trigger t : Trigger.values())
            {
                if (s.contains(res.getString(t.getCodeId())))
                {
                    retValue = t.getFunctionType();
                    break;
                }
            }
        }
        if (retValue == null)
        {
            if (fName != null || s.contains(FUNCTION_LINK_OBJECT))
            {
                retValue = FunctionType.FUNCTION_LINK;
            }
        }
        return retValue;
    }

    public static String getFunctionString(Context context, FunctionType t)
    {
        switch (t)
        {
        case FUNCTION_LINK:
            return FUNCTION_LINK_OBJECT;
        default:
            return t.toString().toLowerCase(Locale.ENGLISH);
        }
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
    private TermField argTerm = null;
    private CustomLayout functionMainLayout = null;
    private String functionLinkName = "unknown";
    private Equation linkedFunction = null;

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
    public double getValue(CalculaterTask thread) throws CancelException
    {
        if (functionType != null)
        {
            double v = argTerm.getValue(thread);
            switch (functionType)
            {
            case IDENTITY:
                return v;
            case SIN:
                return FastMath.sin(v);
            case ASIN:
                return FastMath.asin(v);
            case SINH:
                return FastMath.sinh(v);

            case COS:
                return FastMath.cos(v);
            case ACOS:
                return FastMath.acos(v);
            case COSH:
                return FastMath.cosh(v);

            case TAN:
                return FastMath.tan(v);
            case ATAN:
                return FastMath.atan(v);
            case TANH:
                return FastMath.tanh(v);

            case EXP:
                return FastMath.exp(v);
            case LOG:
                return FastMath.log(v);
            case LOG10:
                return FastMath.log10(v);

            case SQRT:
            case SQRT_LAYOUT:
                return FastMath.sqrt(v);
            case ABS:
            case ABS_LAYOUT:
                return FastMath.abs(v);
            case CEIL:
                return FastMath.ceil(v);
            case FLOOR:
                return FastMath.floor(v);
            case RND:
                return FastMath.random() * v;
            case SIGNUM:
                return FastMath.signum(v);

            case FACTORIAL:
                try
                {
                    return CombinatoricsUtils.factorialDouble((int) v);
                }
                catch (Exception e)
                {
                    return Double.NaN;
                }

            case FUNCTION_LINK:
                if (linkedFunction != null)
                {
                    linkedFunction.setArgument(v);
                    return linkedFunction.getValue(thread);
                }
                break;
            }
        }
        return Double.NaN;
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
        if (functionType == FunctionType.FUNCTION_LINK)
        {
            t += "." + functionLinkName;
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
            if (isValid && functionType == FunctionType.FUNCTION_LINK)
            {
                FormulaBase f = getFormulaRoot().getFormulaList().getFormula(functionLinkName, 1,
                        getFormulaRoot().getId(), false);
                if (f != null && f instanceof Equation)
                {
                    linkedFunction = (Equation) f;
                }
                int errorCode = 0;
                if (linkedFunction == null)
                {
                    // unknown function
                    errorCode = 1;
                    isValid = false;
                }
                else if (linkedFunction.getId() == getFormulaRoot().getId())
                {
                    // recursive call
                    errorCode = 2;
                    isValid = false;
                }
                setErrorCode(errorCode, functionLinkName);
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
                if (functionType == FunctionType.FUNCTION_LINK)
                {
                    v.setText(functionLinkName);
                }
                else if (functionType == FunctionType.SQRT_LAYOUT)
                {
                    v.setText("");
                }
                else if (functionType == FunctionType.FACTORIAL)
                {
                    v.setText(res.getString(R.string.formula_function_factorial_layout));
                }
                else
                {
                    v.setText(getFunctionLabel());
                }
                v.prepare(CustomTextView.SymbolType.TEXT, getFormulaRoot().getFormulaList().getActivity(), this);
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
            if (v.getText().toString().equals(getContext().getResources().getString(R.string.formula_arg_term_key)))
            {
                argTerm = addTerm(getFormulaRoot(), l, v, this, false);
                argTerm.bracketsType = (functionType == FunctionType.FACTORIAL) ? TermField.BracketsType.ALWAYS
                        : TermField.BracketsType.NEVER;
            }
        }
        return v;
    }

    @Override
    public void updateTextSize()
    {
        super.updateTextSize();
        if (functionTerm != null)
        {
            if (functionType == FunctionType.SQRT_LAYOUT)
            {
                functionTerm.setWidth(getFormulaList().getDimen().get(ScaledDimensions.Type.SMALL_SYMBOL_SIZE));
                functionTerm.setPadding(0, 0, 0, 0);
            }
            else
            {
                functionTerm.setPadding(0, 0,
                        getFormulaList().getDimen().get(ScaledDimensions.Type.HOR_SYMBOL_PADDING), 0);
            }
        }
    }

    /*********************************************************
     * Implementation for methods for FormulaChangeIf interface
     *********************************************************/

    @Override
    public void onDelete(CustomEditText owner)
    {
        CharSequence remainingText = "";
        final TermField ownerTerm = findTerm(owner);
        if (ownerTerm != null && functionTerm != null)
        {
            remainingText = getFunctionLabel();
        }
        if (parentField != null)
        {
            parentField.onTermDeleteWithText(removeElements(), remainingText);
        }
        else
        {
            super.onDelete(null);
        }
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
        functionType = getFunctionType(getContext(), s);
        if (functionType == null)
        {
            throw new Exception("cannot create FormulaFunction for unknown function");
        }
        switch (functionType)
        {
        case FUNCTION_LINK:
            functionLinkName = getFunctionLinkName(s);
            if (functionLinkName == null)
            {
                throw new Exception("cannot create FormulaFunction since function link is invalid");
            }
            inflateElements(R.layout.formula_function_named, true);
            break;
        case SQRT_LAYOUT:
            inflateElements(R.layout.formula_function_sqrt, true);
            break;
        case FACTORIAL:
            inflateElements(R.layout.formula_function_factorial, true);
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
        if (argTerm == null)
        {
            throw new Exception("cannot initialize function term");
        }

        // store the main layout in order to show errors 
        final String functionMainTag = getContext().getResources().getString(R.string.function_main_layout);
        final View functionMainView = layout.findViewWithTag(functionMainTag);
        if (functionMainView != null)
        {
            functionMainLayout = (CustomLayout) functionMainView;
            functionMainLayout.setTag("");
        }

        // set texts for left and right parts (in editing mode only)
        final String startBracket = getContext().getResources().getString(R.string.formula_function_start_bracket);
        final String endBracket = getContext().getResources().getString(R.string.formula_function_end_bracket);
        if (s.contains(startBracket) && s.endsWith(endBracket))
        {
            s = s.substring(0, s.indexOf(endBracket)).trim();
        }
        for (Trigger t : Trigger.values())
        {
            String opCode = getContext().getResources().getString(t.getCodeId());
            int opPosition = s.indexOf(opCode);
            if (opPosition >= 0)
            {
                try
                {
                    if (t.isBeforeText())
                    {
                        argTerm.setText(s.subSequence(opPosition + opCode.length(), s.length()));
                    }
                    else
                    {
                        argTerm.setText(s.subSequence(0, opPosition));
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
    private String getFunctionLinkName(String s)
    {
        try
        {
            if (s.contains(getContext().getResources().getString(R.string.formula_function_start_bracket)))
            {
                String opCode = getContext().getResources().getString(R.string.formula_function_start_bracket);
                return s.substring(0, s.indexOf(opCode)).trim();
            }
            if (s.contains(FUNCTION_LINK_OBJECT))
            {
                String opCode = FUNCTION_LINK_OBJECT + ".";
                return s.substring(s.indexOf(opCode) + opCode.length(), s.length());
            }
        }
        catch (Exception ex)
        {
            // nothig to do
        }
        return null;
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

    private void setErrorCode(int errorCode, String addInfo)
    {
        if (functionTerm != null)
        {
            final int colorId = R.color.formula_text_color;
            functionTerm.setTextColor(CompatUtils.getColor(getContext(), colorId));
        }
        if (parentField != null)
        {
            String errorMsg = null;
            if (errorCode == 1)
            {
                errorMsg = String.format(getContext().getResources().getString(R.string.error_unknown_function),
                        addInfo);
            }
            else if (errorCode == 2)
            {
                errorMsg = getContext().getResources().getString(R.string.error_recursive_call);
            }
            else if (errorCode == 3)
            {
                errorMsg = String.format(getContext().getResources().getString(R.string.error_not_differentiable),
                        addInfo);
            }
            parentField.setError(errorMsg, ErrorNotification.PARENT_LAYOUT, functionMainLayout);
        }
    }

    private String getFunctionLabel()
    {
        switch (functionType)
        {
        case FUNCTION_LINK:
            return functionLinkName;
        case IDENTITY:
        case SQRT_LAYOUT:
        case FACTORIAL:
        case ABS_LAYOUT:
            return "";
        default:
            return functionType.toString().toLowerCase(Locale.ENGLISH);
        }
    }

}
