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
package com.mkulesh.micromath.formula;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.math.AxisTypeConverter;
import com.mkulesh.micromath.math.CalculatedValue;
import com.mkulesh.micromath.plots.FunctionIf;
import com.mkulesh.micromath.plots.views.PlotView;
import com.mkulesh.micromath.properties.AxisProperties;
import com.mkulesh.micromath.undo.FormulaState;
import com.mkulesh.micromath.utils.ViewUtils;

import org.apache.commons.math3.util.FastMath;

import androidx.annotation.NonNull;

public abstract class CalculationResult extends LinkHolder
{
    /*--------------------------------------------------------*
     * Constructors
     *--------------------------------------------------------*/

    public CalculationResult(FormulaList formulaList, LinearLayout layout, int termDepth)
    {
        super(formulaList, layout, termDepth);
    }

    /*--------------------------------------------------------*
     * GUI constructors to avoid lint warning
     *--------------------------------------------------------*/

    public CalculationResult(Context context)
    {
        super(null, null, 0);
    }

    public CalculationResult(Context context, AttributeSet attrs)
    {
        super(null, null, 0);
    }

    /*--------------------------------------------------------*
     *  Methods to be (re)implemented in derived a class
     *--------------------------------------------------------*/

    /**
     * Procedure performs invalidation for this object
     */
    public abstract void invalidateResult();

    /**
     * Procedure performs calculation for this object.
     *
     * This method is called in a separate thread and shall not update any UI elements
     */
    public abstract void calculate(CalculaterTask thread) throws CancelException;

    /**
     * Procedure shows calculation result for this object.
     *
     * This method is called from UI thread
     */
    public abstract void showResult();

    /**
     * Procedure returns true if the calculation and content checking shall be skipped for this formula
     */
    public boolean disableCalculation()
    {
        return false;
    }

    /*--------------------------------------------------------*
     * Re-implementation for methods for Object superclass
     *--------------------------------------------------------*/

    @NonNull
    @Override
    public String toString()
    {
        return "Calculation " + getBaseType().toString() + "(Id: " + getId() + ")";
    }

    /*--------------------------------------------------------*
     * Re-implementation for methods for FormulaBase superclass
     *--------------------------------------------------------*/

    @Override
    public void undo(FormulaState state)
    {
        super.undo(state);
        invalidateResult();
    }

    @Override
    public boolean enableObjectProperties()
    {
        return true;
    }

    /*--------------------------------------------------------*
     * Helper methods
     *--------------------------------------------------------*/

    protected boolean setEmptyBorders(double[] minMaxValues, TermField fMin, TermField fMax)
    {
        if (minMaxValues == null || fMin == null || fMax == null)
        {
            return false;
        }
        if (minMaxValues.length != 2)
        {
            return false;
        }
        boolean isValid = true;
        final boolean updateMin = fMin.isEmptyOrAutoContent();
        final boolean updateMax = fMax.isEmptyOrAutoContent();
        if (updateMin || updateMax)
        {
            final CalculatedValue calcVal = new CalculatedValue();
            // inspect minimum value
            calcVal.setValue(minMaxValues[FunctionIf.MIN]);
            String strMin = calcVal.getResultDescription(getFormulaList().getDocumentSettings());
            if (updateMin && calcVal.isNaN())
            {
                isValid = false;
            }
            // inspect maximum value
            calcVal.setValue(minMaxValues[FunctionIf.MAX]);
            String strMax = calcVal.getResultDescription(getFormulaList().getDocumentSettings());
            if (updateMax && calcVal.isNaN())
            {
                isValid = false;
            }
            // round minimum and maximum values
            if (isValid)
            {
                String[] strValues = ViewUtils.catValues(minMaxValues,
                        getFormulaList().getDocumentSettings().significantDigits);
                strMin = strValues[FunctionIf.MIN];
                strMax = strValues[FunctionIf.MAX];
            }
            if (updateMin && strMin != null)
            {
                fMin.setTextChangeDetectionEnabled(false);
                fMin.setText(strMin);
                fMin.setTextChangeDetectionEnabled(true);
            }
            if (updateMax && strMax != null)
            {
                fMax.setTextChangeDetectionEnabled(false);
                fMax.setText(strMax);
                fMax.setTextChangeDetectionEnabled(true);
            }
        }
        return isValid;
    }

    protected void updateEqualBorders(double[] minMaxValues)
    {
        if (minMaxValues != null && minMaxValues.length == 2)
        {
            if (minMaxValues[FunctionIf.MIN] == minMaxValues[FunctionIf.MAX])
            {
                final double val = minMaxValues[FunctionIf.MIN];
                final double delta = Math.max(0.1 * val,
                        getFormulaList().getDocumentSettings().getPrecision());
                minMaxValues[FunctionIf.MIN] = val - delta;
                minMaxValues[FunctionIf.MAX] = val + delta;
            }
        }
    }

    protected void updatePlotBoundaries(PlotView view, TermField xMinTerm, TermField xMaxTerm, TermField yMinTerm,
                                        TermField yMaxTerm, AxisProperties prop)
    {
        try
        {
            final CalculatedValue xMinVal = new CalculatedValue();
            final CalculatedValue xMaxVal = new CalculatedValue();
            final CalculatedValue yMinVal = new CalculatedValue();
            final CalculatedValue yMaxVal = new CalculatedValue();
            xMinVal.processRealTerm(null, xMinTerm);
            xMaxVal.processRealTerm(null, xMaxTerm);
            yMinVal.processRealTerm(null, yMinTerm);
            yMaxVal.processRealTerm(null, yMaxTerm);
            if (prop != null)
            {
                view.setArea(AxisTypeConverter.toSpecialType(xMinVal.getReal(), prop.xType),
                        AxisTypeConverter.toSpecialType(xMaxVal.getReal(), prop.xType),
                        AxisTypeConverter.toSpecialType(yMinVal.getReal(), prop.yType),
                        AxisTypeConverter.toSpecialType(yMaxVal.getReal(), prop.yType));
            }
            else
            {
                view.setArea(xMinVal.getReal(), xMaxVal.getReal(), yMinVal.getReal(), yMaxVal.getReal());
            }
        }
        catch (CancelException e)
        {
            // nothing to do
        }
    }
}
