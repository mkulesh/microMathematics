/*
 * microMathematics - Extended Visual Calculator
 * Copyright (C) 2014-2022 by Mikhail Kulesh
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details. You should have received a copy of the GNU General
 * Public License along with this program.
 */
package com.mkulesh.micromath.formula.terms;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.mkulesh.micromath.formula.CalculaterTask;
import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.formula.Equation;
import com.mkulesh.micromath.formula.FormulaTerm;
import com.mkulesh.micromath.formula.Palette;
import com.mkulesh.micromath.formula.PaletteButton;
import com.mkulesh.micromath.formula.TermField;
import com.mkulesh.micromath.math.CalculatedValue;
import com.mkulesh.micromath.math.EquationArrayResult;
import com.mkulesh.micromath.R;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomTextView;

import org.apache.commons.math3.util.FastMath;

import java.util.Locale;

public class Intervals extends FormulaTerm
{
    public TermTypeIf.GroupType getGroupType()
    {
        return TermTypeIf.GroupType.INTERVALS;
    }

    /**
     * Supported functions
     */
    public enum IntervalType implements TermTypeIf
    {
        EQUIDISTANT_INTERVAL(
                R.string.formula_quidistant_interval,
                R.drawable.p_equidistant_interval,
                R.string.math_equidistant_interval);

        private final int shortCutId;
        private final int imageId;
        private final int descriptionId;
        private final String lowerCaseName;

        IntervalType(int shortCutId, int imageId, int descriptionId)
        {
            this.shortCutId = shortCutId;
            this.imageId = imageId;
            this.descriptionId = descriptionId;
            this.lowerCaseName = name().toLowerCase(Locale.ENGLISH);
        }

        public GroupType getGroupType()
        {
            return GroupType.INTERVALS;
        }

        public int getShortCutId()
        {
            return shortCutId;
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
            return Palette.NO_BUTTON;
        }

        public boolean isEnabled(CustomEditText field)
        {
            return field.isIntervalEnabled();
        }

        public PaletteButton.Category getPaletteCategory()
        {
            return PaletteButton.Category.TOP_LEVEL_TERM;
        }

        public FormulaTerm createTerm(
                TermField termField, LinearLayout layout, String text, int textIndex, Object par) throws Exception
        {
            return new Intervals(this, termField, layout, textIndex);
        }
    }

    /**
     * Private attributes
     */
    private TermField minValueTerm, nextValueTerm, maxValueTerm = null;

    // Attention: this is not thread-safety declaration!
    private final CalculatedValue minValue = new CalculatedValue(), nextValue = new CalculatedValue(),
            maxValue = new CalculatedValue();

    /*--------------------------------------------------------*
     * Constructors
     *--------------------------------------------------------*/

    private Intervals(IntervalType type, TermField owner, LinearLayout layout, int idx) throws Exception
    {
        super(owner, layout);
        termType = type;
        inflateElements(R.layout.formula_interval, true);
        initializeElements(idx);
        if (minValueTerm == null || nextValueTerm == null || maxValueTerm == null)
        {
            throw new Exception("cannot initialize function terms");
        }
    }

    /*--------------------------------------------------------*
     * GUI constructors to avoid lint warning
     *--------------------------------------------------------*/

    public Intervals(Context context)
    {
        super();
    }

    public Intervals(Context context, AttributeSet attrs)
    {
        super();
    }

    /*--------------------------------------------------------*
     * Re-implementation for methods for FormulaBase and FormulaTerm superclass's
     *--------------------------------------------------------*/

    @Override
    public CalculatedValue.ValueType getValue(CalculaterTask thread, CalculatedValue outValue) throws CancelException
    {
        if (getFormulaRoot() instanceof Equation)
        {
            minValue.processRealTerm(thread, minValueTerm);
            nextValue.processRealTerm(thread, nextValueTerm);
            maxValue.processRealTerm(thread, maxValueTerm);
            if (minValue.isNaN() || nextValue.isNaN() || maxValue.isNaN())
            {
                return outValue.invalidate(CalculatedValue.ErrorType.NOT_A_REAL);
            }
            final CalculatedValue calcDelta = getDelta(minValue.getReal(), nextValue.getReal(), maxValue.getReal());
            final CalculatedValue ravArg = ((Equation) getFormulaRoot()).getArgumentValue(0);
            if (calcDelta.isNaN() || ravArg.isNaN())
            {
                return outValue.invalidate(CalculatedValue.ErrorType.NOT_A_REAL);
            }
            final long idx = ravArg.getInteger();
            final int N = getNumberOfPoints(minValue.getReal(), maxValue.getReal(), calcDelta.getReal());
            if (idx == 0)
            {
                return outValue.setValue(minValue.getReal());
            }
            else if (idx == N)
            {
                return outValue.setValue(maxValue.getReal());
            }
            else if (idx > 0 && idx < N)
            {
                return outValue.setValue(minValue.getReal() + calcDelta.getReal() * (double) idx);
            }
        }
        return outValue.invalidate(CalculatedValue.ErrorType.TERM_NOT_READY);
    }

    @Override
    public DifferentiableType isDifferentiable(String var)
    {
        return DifferentiableType.NONE;
    }

    @Override
    public CalculatedValue.ValueType getDerivativeValue(String var, CalculaterTask thread, CalculatedValue outValue)
            throws CancelException
    {
        return outValue.invalidate(CalculatedValue.ErrorType.TERM_NOT_READY);
    }

    @Override
    protected CustomTextView initializeSymbol(CustomTextView v)
    {
        if (v.getText() != null)
        {
            String t = v.getText().toString();
            if (t.equals(getContext().getResources().getString(R.string.formula_left_bracket_key)))
            {
                v.prepare(CustomTextView.SymbolType.LEFT_SQR_BRACKET, getFormulaRoot().getFormulaList().getActivity(),
                        this);
                v.setText("."); // this text defines view width/height
            }
            else if (t.equals(getContext().getResources().getString(R.string.formula_right_bracket_key)))
            {
                v.prepare(CustomTextView.SymbolType.RIGHT_SQR_BRACKET, getFormulaRoot().getFormulaList().getActivity(),
                        this);
                v.setText("."); // this text defines view width/height
            }
            else if (t.equals(getContext().getResources().getString(R.string.formula_first_separator_key)))
            {
                v.prepare(CustomTextView.SymbolType.TEXT, getFormulaRoot().getFormulaList().getActivity(), this);
                v.setText(getContext().getResources().getString(R.string.formula_interval_first_separator));
            }
            else if (t.equals(getContext().getResources().getString(R.string.formula_second_separator_key)))
            {
                v.prepare(CustomTextView.SymbolType.TEXT, getFormulaRoot().getFormulaList().getActivity(), this);
                v.setText(getContext().getResources().getString(R.string.formula_interval_second_separator));
            }
        }
        return v;
    }

    @Override
    protected CustomEditText initializeTerm(CustomEditText v, LinearLayout l)
    {
        if (v.getText() != null)
        {
            if (v.getText().toString().equals(getContext().getResources().getString(R.string.formula_min_value_key)))
            {
                minValueTerm = addTerm(getFormulaRoot(), l, v, this, false);
                minValueTerm.bracketsType = TermField.BracketsType.NEVER;
            }
            else if (v.getText().toString()
                    .equals(getContext().getResources().getString(R.string.formula_next_value_key)))
            {
                nextValueTerm = addTerm(getFormulaRoot(), l, v, this, false);
                nextValueTerm.bracketsType = TermField.BracketsType.NEVER;
            }
            else if (v.getText().toString()
                    .equals(getContext().getResources().getString(R.string.formula_max_value_key)))
            {
                maxValueTerm = addTerm(getFormulaRoot(), l, v, this, false);
                maxValueTerm.bracketsType = TermField.BracketsType.NEVER;
            }
        }
        return v;
    }

    /*--------------------------------------------------------*
     * FormulaTermInterval-specific methods
     *--------------------------------------------------------*/

    /**
     * Procedure returns declared interval if this root formula represents an interval
     */
    public void getInterval(EquationArrayResult arrayResult, CalculaterTask thread) throws CancelException
    {
        minValue.processRealTerm(thread, minValueTerm);
        nextValue.processRealTerm(thread, nextValueTerm);
        maxValue.processRealTerm(thread, maxValueTerm);
        if (minValue.isNaN() || nextValue.isNaN() || maxValue.isNaN())
        {
            return;
        }
        final CalculatedValue calcDelta = getDelta(minValue.getReal(), nextValue.getReal(), maxValue.getReal());
        if (calcDelta.isNaN())
        {
            return;
        }
        final int N = getNumberOfPoints(minValue.getReal(), maxValue.getReal(), calcDelta.getReal());
        arrayResult.resize1D(N + 1);
        for (int idx = 0; idx <= N; idx++)
        {
            if (thread != null)
            {
                thread.checkCancelation();
            }
            final CalculatedValue cv = arrayResult.getValue1D(idx);
            if (idx == 0)
            {
                cv.setValue(minValue.getReal());
            }
            else if (idx == N)
            {
                cv.setValue(maxValue.getReal());
            }
            else
            {
                final double val = minValue.getReal() + calcDelta.getReal() * (double) idx;
                cv.setValue(val);
            }
        }
    }

    /**
     * Procedure checks and returns delta value
     */
    private CalculatedValue getDelta(final double min, final double next, final double max)
    {
        final CalculatedValue calcVal = new CalculatedValue();
        if (next <= min || max < next)
        {
            // error: invalid boundaries
            calcVal.invalidate(CalculatedValue.ErrorType.NOT_A_NUMBER);
        }
        else
        {
            calcVal.setValue(next - min);
        }
        return calcVal;
    }

    private int getNumberOfPoints(double min, double max, double delta)
    {
        final int N = (int) FastMath.floor(((max - min) / delta));
        final double d1 = min + delta * (double) N;
        final double eps = FastMath.pow(10, -1 * (getFormulaList().getDocumentSettings().significantDigits));
        return (max > d1 && (max - d1) > eps * delta) ? N + 1 : N;
    }
}
