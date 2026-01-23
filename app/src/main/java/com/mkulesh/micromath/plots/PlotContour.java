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
package com.mkulesh.micromath.plots;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.mkulesh.micromath.dialogs.DialogAxisSettings;
import com.mkulesh.micromath.dialogs.DialogColorMapSettings;
import com.mkulesh.micromath.dialogs.DialogPlotSettings;
import com.mkulesh.micromath.formula.CalculaterTask;
import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.formula.CalculationResult;
import com.mkulesh.micromath.formula.Equation;
import com.mkulesh.micromath.formula.FormulaList;
import com.mkulesh.micromath.formula.TermField;
import com.mkulesh.micromath.formula.TermField.ErrorNotification;
import com.mkulesh.micromath.math.CalculatedValue;
import com.mkulesh.micromath.plots.views.PlotView;
import com.mkulesh.micromath.plots.views.SurfacePlotView;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.AxisProperties;
import com.mkulesh.micromath.properties.AxisPropertiesChangeIf;
import com.mkulesh.micromath.properties.ColorMapPropertiesChangeIf;
import com.mkulesh.micromath.properties.LineProperties;
import com.mkulesh.micromath.properties.PlotProperties;
import com.mkulesh.micromath.properties.PlotProperties.TwoDPlotStyle;
import com.mkulesh.micromath.properties.PlotPropertiesChangeIf;
import com.mkulesh.micromath.undo.FormulaState;
import com.mkulesh.micromath.utils.CompatUtils;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CustomLayout;
import com.mkulesh.micromath.widgets.CustomTextView;
import com.mkulesh.micromath.widgets.SizeChangingLayout;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.util.ArrayList;
import java.util.Locale;

public class PlotContour extends CalculationResult implements SizeChangingLayout.SizeChangedIf, PlotPropertiesChangeIf,
        AxisPropertiesChangeIf, ColorMapPropertiesChangeIf
{
    /*
     * Constants used to save/restore the instance state.
     */
    private static final String XML_PROP_PLOT_STYLE = "plotStyle";
    private static final String STATE_PLOT_STYLE = "two_d_plot_style";
    private static final String STATE_FUNCTIONVIEW_PARAMETERS = "functionview_parameters";

    // visual components
    private TwoDPlotStyle twoDPlotStyle = TwoDPlotStyle.CONTOUR;
    private TermField yMin = null, yMax = null, xMin = null, functionTerm = null, xMax = null;
    private final ArrayList<CustomTextView> axes = new ArrayList<>();
    private PlotView functionView = null;
    private LinearLayout functionViewLayout = null;
    private CustomTextView cornerView1 = null, cornerView2 = null;

    // function data
    private final Function3D function = new Function3D();
    private final TermField[] boundaries = new TermField[4];

    // undo
    private FormulaState formulaState = null;

    /*--------------------------------------------------------*
     * Constructors
     *--------------------------------------------------------*/

    public PlotContour(FormulaList formulaList, int id)
    {
        super(formulaList, null, 0);
        setId(id);
        onCreate();
    }

    /*--------------------------------------------------------*
     * GUI constructors to avoid lint warning
     *--------------------------------------------------------*/

    public PlotContour(Context context)
    {
        super(null, null, 0);
    }

    public PlotContour(Context context, AttributeSet attrs)
    {
        super(null, null, 0);
    }

    /*--------------------------------------------------------*
     * Re-implementation for methods for FormulaBase superclass
     *--------------------------------------------------------*/

    @Override
    public BaseType getBaseType()
    {
        return BaseType.PLOT_CONTOUR;
    }

    @Override
    public boolean isContentValid(ValidationPassType type)
    {
        boolean isValid = super.isContentValid(type);

        switch (type)
        {
        case VALIDATE_SINGLE_FORMULA:
            break;
        case VALIDATE_LINKS:
            // additional checks for intervals validity
            if (isValid && functionTerm != null && !functionTerm.isEmpty())
            {
                String errorMsg = null;
                final ArrayList<String> indirectIntervals = getIndirectIntervals();
                if (!indirectIntervals.isEmpty())
                {
                    isValid = false;
                    errorMsg = String.format(getContext().getResources().getString(R.string.error_indirect_intervals),
                            indirectIntervals.toString());
                }
                else if (getDirectIntervals().size() > 2)
                {
                    isValid = false;
                    errorMsg = getContext().getResources().getString(R.string.error_ensure_double_interval);
                }
                functionTerm.setError(errorMsg, ErrorNotification.LAYOUT_BORDER, null);
            }
            break;
        }

        if (!isValid)
        {
            functionView.setFunction(null);
            functionView.invalidate();
        }
        return isValid;
    }

    @Override
    public void updateTextSize()
    {
        super.updateTextSize();
        updatePlotView();
    }

    /*--------------------------------------------------------*
     * Re-implementation for methods for CalculationResult superclass
     *--------------------------------------------------------*/

    @Override
    public void invalidateResult()
    {
        for (TermField t : boundaries)
        {
            if (t.isEmptyOrAutoContent())
            {
                t.setText("");
            }
        }
        functionView.setFunction(null);
        functionView.invalidate();
    }

    @Override
    public void calculate(CalculaterTask thread) throws CancelException
    {
        function.calculate(thread);
        if (functionView instanceof SurfacePlotView)
        {
            ((SurfacePlotView) functionView).renderSurface(function);
        }
    }

    @Override
    public void showResult()
    {
        final boolean b1 = setEmptyBorders(FunctionIf.X, xMin, xMax);
        final boolean b2 = setEmptyBorders(FunctionIf.Y, yMin, yMax);
        if (b1 && b2)
        {
            functionView.setSignificantDigits(getFormulaList().getDocumentSettings().significantDigits);
            functionView.setFunction(function);
            if (functionView instanceof SurfacePlotView)
            {
                final SurfacePlotView spv = (SurfacePlotView) functionView;
                if (!spv.isRendered())
                {
                    spv.renderSurface(function);
                }
            }
            // updatePlotBoundaries needs valid function set
            updatePlotBoundaries(functionView, xMin, xMax, yMin, yMax, null);
        }
        else
        {
            functionView.setFunction(null);
        }
        functionView.invalidate();
    }

    /*--------------------------------------------------------*
     * Implementation for methods for FormulaChangeIf interface
     *--------------------------------------------------------*/

    @Override
    public void onTermSelection(View owner, boolean isSelected, ArrayList<View> list)
    {
        if (list == null)
        {
            if (owner == functionView)
            {
                // nothing to do
            }
            else if (owner == functionView.getColorMapView())
            {
                list = new ArrayList<>();
                list.add(owner);
            }
            else if (axes.contains(owner))
            {
                list = new ArrayList<>();
                list.addAll(axes);
            }
        }
        super.onTermSelection(owner, isSelected, list);
    }

    @Override
    public void onObjectProperties(View owner)
    {
        if (owner == this)
        {
            DialogPlotSettings d = new DialogPlotSettings(getFormulaList().getActivity(), this,
                    functionView.getPlotParameters());
            formulaState = getState();
            d.show();
        }
        if (owner == functionView.getColorMapView())
        {
            DialogColorMapSettings d = new DialogColorMapSettings(getFormulaList().getActivity(), this, functionView
                    .getColorMapView().getColorMapParameters());
            formulaState = getState();
            d.show();
        }
        else if (axes.contains(owner))
        {
            DialogAxisSettings d = new DialogAxisSettings(getFormulaList().getActivity(), this,
                    functionView.getAxisParameters());
            formulaState = getState();
            d.show();
        }
        super.onObjectProperties(owner);
    }

    /*--------------------------------------------------------*
     * PlotParametersChangeIf interface implementation
     *--------------------------------------------------------*/

    @Override
    public Dimension getDimension()
    {
        return Dimension.TWO_D;
    }

    @Override
    public AxisType getAxisType()
    {
        return AxisType.LINEAR;
    }

    @Override
    public void onAxisPropertiesChange(boolean isChanged)
    {
        if (!isChanged)
        {
            formulaState = null;
            return;
        }
        if (formulaState != null)
        {
            getFormulaList().getUndoState().addEntry(formulaState);
            formulaState = null;
        }
        updatePlotBoundaries(functionView, xMin, xMax, yMin, yMax, null);
        ViewUtils.invalidateLayout(functionView, layout);
    }

    @Override
    public void onPlotPropertiesChange(boolean isChanged)
    {
        getFormulaList().finishActiveActionMode();
        if (!isChanged)
        {
            formulaState = null;
            return;
        }
        if (formulaState != null)
        {
            getFormulaList().getUndoState().addEntry(formulaState);
            formulaState = null;
        }
        if (twoDPlotStyle != functionView.getPlotParameters().twoDPlotStyle)
        {
            setTwoDPlotStyle(functionView.getPlotParameters().twoDPlotStyle);
            showResult();
        }
        functionView.updateLabels();
        updatePlotView();
        ViewUtils.invalidateLayout(functionView, layout);
    }

    @Override
    public void onColorMapPropertiesChange(boolean isChanged)
    {
        if (!isChanged)
        {
            formulaState = null;
            return;
        }
        if (formulaState != null)
        {
            getFormulaList().getUndoState().addEntry(formulaState);
            formulaState = null;
        }
        functionView.getColorMapView().setFunction(function);
        ViewUtils.invalidateLayout(functionView, layout);
    }

    /*--------------------------------------------------------*
     * SizeChangedIf interface implementation
     *--------------------------------------------------------*/

    @Override
    public void onHeightChanged(int h)
    {
        cornerView1.getLayoutParams().height = h;
        cornerView2.getLayoutParams().height = h;
        cornerView1.post(() ->
        {
            cornerView1.requestLayout();
            cornerView2.requestLayout();
        });
    }

    /*--------------------------------------------------------*
     * Read/write interface
     *--------------------------------------------------------*/

    /**
     * Parcelable interface: procedure writes the formula state
     */
    public Parcelable onSaveInstanceState()
    {
        Parcelable state = super.onSaveInstanceState();
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            bundle.putString(STATE_PLOT_STYLE, twoDPlotStyle.toString());
            bundle.putParcelable(STATE_FUNCTIONVIEW_PARAMETERS, functionView.onSaveInstanceState());
        }
        return state;
    }

    /**
     * Parcelable interface: procedure reads the formula state
     */
    public void onRestoreInstanceState(Parcelable state)
    {
        if (state == null)
        {
            return;
        }
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            final String attr = bundle.getString(STATE_PLOT_STYLE);
            if (attr != null)
            {
                twoDPlotStyle = TwoDPlotStyle.valueOf(attr);
            }
            if (twoDPlotStyle != functionView.getPlotParameters().twoDPlotStyle)
            {
                setTwoDPlotStyle(twoDPlotStyle);
            }
            functionView.onRestoreInstanceState(CompatUtils.getParcelable(
                    bundle, STATE_FUNCTIONVIEW_PARAMETERS, Bundle.class));
            super.onRestoreInstanceState(bundle);
            updatePlotView();
        }
    }

    @Override
    public boolean onStartReadXmlTag(XmlPullParser parser)
    {
        super.onStartReadXmlTag(parser);
        if (getBaseType().toString().equalsIgnoreCase(parser.getName()))
        {
            String attr = parser.getAttributeValue(null, XML_PROP_PLOT_STYLE);
            if (attr != null)
            {
                try
                {
                    twoDPlotStyle = TwoDPlotStyle.valueOf(attr.toUpperCase(Locale.ENGLISH));
                    if (twoDPlotStyle != functionView.getPlotParameters().twoDPlotStyle)
                    {
                        setTwoDPlotStyle(twoDPlotStyle);
                    }
                }
                catch (Exception e)
                {
                    // nothing to do
                }
            }
            functionView.getPlotParameters().readFromXml(parser);
            functionView.getAxisParameters().readFromXml(parser);
            functionView.getColorMapView().getColorMapParameters().readFromXml(parser);
        }
        String key = parser.getAttributeValue(null, FormulaList.XML_PROP_KEY);
        if (FormulaList.XML_TERM_TAG.equalsIgnoreCase(parser.getName()) && key != null)
        {
            // contour-specific attributes
        }
        updatePlotView();
        return false;
    }

    @Override
    public boolean onStartWriteXmlTag(XmlSerializer serializer, String key) throws Exception
    {
        super.onStartWriteXmlTag(serializer, key);
        if (getBaseType().toString().equalsIgnoreCase(serializer.getName()))
        {
            serializer.attribute(FormulaList.XML_NS, XML_PROP_PLOT_STYLE,
                    twoDPlotStyle.toString().toLowerCase(Locale.ENGLISH));
            functionView.getPlotParameters().writeToXml(serializer);
            functionView.getAxisParameters().writeToXml(serializer);
            functionView.getColorMapView().getColorMapParameters().writeToXml(serializer);
        }
        if (FormulaList.XML_TERM_TAG.equalsIgnoreCase(serializer.getName()) && key != null)
        {
            // contour-specific attributes
        }
        return false;
    }

    /*--------------------------------------------------------*
     * PlotContour-specific methods
     *--------------------------------------------------------*/

    private void setTwoDPlotStyle(TwoDPlotStyle style)
    {
        PlotProperties currPlotParameters = null;
        AxisProperties currAxisParameters = null;
        final PlotView contourView = layout.findViewById(R.id.plot_contour_view);
        contourView.getPlotParameters().twoDPlotStyle = TwoDPlotStyle.CONTOUR;
        final PlotView surfaceView = layout.findViewById(R.id.plot_surface_view);
        surfaceView.getPlotParameters().twoDPlotStyle = TwoDPlotStyle.SURFACE;
        switch (style)
        {
        case CONTOUR:
            functionView = contourView;
            currPlotParameters = surfaceView.getPlotParameters();
            currAxisParameters = surfaceView.getAxisParameters();
            surfaceView.setVisibility(View.GONE);
            surfaceView.setFunction(null);
            functionViewLayout.setBaselineAlignedChildIndex(0);
            break;
        case SURFACE:
            functionView = surfaceView;
            currPlotParameters = contourView.getPlotParameters();
            currAxisParameters = contourView.getAxisParameters();
            contourView.setVisibility(View.GONE);
            surfaceView.setFunction(null);
            functionViewLayout.setBaselineAlignedChildIndex(1);
            break;
        }
        functionView.setVisibility(View.VISIBLE);
        if (currPlotParameters != null)
        {
            functionView.getPlotParameters().assign(currPlotParameters);
        }
        if (currAxisParameters != null)
        {
            functionView.getAxisParameters().assign(currAxisParameters);
        }
        twoDPlotStyle = style;
    }

    /**
     * Procedure creates the formula layout
     */
    private void onCreate()
    {
        inflateRootLayout(R.layout.plot_contour, 300, 300);
        if (layout instanceof CustomLayout)
        {
            CustomLayout cl = (CustomLayout) layout;
            cl.setCustomFeaturesDisabled(true);
            cl.setBaselineAligned(false);
            cl.setBaselineAlignedChildIndex(1);
        }
        functionViewLayout = layout.findViewById(R.id.plot_function_view_layout);

        LinearLayout xDataLayout = layout.findViewById(R.id.plot_x_data_layout);
        if (xDataLayout instanceof SizeChangingLayout)
        {
            ((SizeChangingLayout) xDataLayout).setSizeChangedIf(this);
        }
        cornerView1 = layout.findViewById(R.id.plot_corner_view1_id);
        cornerView2 = layout.findViewById(R.id.plot_corner_view2_id);

        // create graph area
        final PlotView contourView = layout.findViewById(R.id.plot_contour_view);
        contourView.setColorMapView(layout.findViewById(R.id.plot_colormap_view));
        contourView.prepare(getFormulaList().getActivity(), this);
        final PlotView surfaceView = layout.findViewById(R.id.plot_surface_view);
        surfaceView.setColorMapView(layout.findViewById(R.id.plot_colormap_view));
        surfaceView.prepare(getFormulaList().getActivity(), this);
        functionView = contourView;

        // create editable fields
        yMax = addTerm(this, layout.findViewById(R.id.plot_y_max_layout),
                layout.findViewById(R.id.plot_y_max_value), this, false);
        boundaries[0] = yMax;

        yMin = addTerm(this, layout.findViewById(R.id.plot_y_min_layout),
                layout.findViewById(R.id.plot_y_min_value), this, false);
        boundaries[1] = yMin;

        xMin = addTerm(this, layout.findViewById(R.id.plot_x_min_layout),
                layout.findViewById(R.id.plot_x_min_value), this, false);
        boundaries[2] = xMin;

        functionTerm = addTerm(this, layout.findViewById(R.id.plot_function_layout),
                layout.findViewById(R.id.plot_function_term), this, false);
        functionTerm.termDepth = 1;

        xMax = addTerm(this, layout.findViewById(R.id.plot_x_max_layout),
                layout.findViewById(R.id.plot_x_max_value), this, false);
        boundaries[3] = xMax;

        for (TermField t : terms)
        {
            t.bracketsType = TermField.BracketsType.NEVER;
        }
        for (TermField t : boundaries)
        {
            t.termDepth = 2;
        }

        axes.add(layout.findViewById(R.id.plot_x_axis1));
        axes.add(layout.findViewById(R.id.plot_x_axis2));
        axes.add(layout.findViewById(R.id.plot_y_axis));
        for (int i = 0; i < axes.size(); i++)
        {
            axes.get(i).prepare(CustomTextView.SymbolType.TEXT, getFormulaList().getActivity(), this);
        }

        updatePlotView();
    }

    private void updatePlotView()
    {
        final float scale = getFormulaList().getDimen().getScaleFactor();
        functionView.setScale(scale);
        layout.getLayoutParams().width = Math.round(functionView.getPlotParameters().width * scale);
        layout.getLayoutParams().height = Math.round(functionView.getPlotParameters().height * scale);
    }

    private boolean setEmptyBorders(int idx, TermField f1, TermField f2)
    {
        return super.setEmptyBorders(function.getMinMaxValues(idx), f1, f2);
    }

    /*--------------------------------------------------------*
     * Helper class that implements function interface
     *--------------------------------------------------------*/

    private class Function3D implements FunctionIf
    {
        private double[] xValues = new double[1];
        private double[] yValues = new double[1];
        private double[][] zValues = new double[1][1];
        private final double[][] minMaxValues = new double[3][2];
        private final CalculatedValue[][] argValues = new CalculatedValue[2][1];
        private final String[] labels = new String[3];

        @Override
        public Type getType()
        {
            return Type.FUNCTION_3D;
        }

        @Override
        public double[] getXValues()
        {
            return xValues;
        }

        @Override
        public double[] getYValues()
        {
            return yValues;
        }

        @Override
        public double[][] getZValues()
        {
            return zValues;
        }

        @Override
        public double[] getMinMaxValues(int idx)
        {
            return (idx < minMaxValues.length) ? minMaxValues[idx] : null;
        }

        @Override
        public LineProperties getLineParameters()
        {
            return null;
        }

        @Override
        public String[] getLabels()
        {
            return labels;
        }

        Function3D()
        {
            for (int i = 0; i < 3; i++)
            {
                minMaxValues[i][FunctionIf.MIN] = minMaxValues[i][FunctionIf.MAX] = 0.0;
            }
        }

        void calculate(CalculaterTask thread) throws CancelException
        {
            final CalculatedValue calcVal = new CalculatedValue();
            final ArrayList<Equation> linkedIntervals = getDirectIntervals();
            if (linkedIntervals.size() != 1 && linkedIntervals.size() != 2)
            {
                return;
            }
            final boolean isFunction1D = linkedIntervals.size() == 1;

            // prepare axis and minimum and maximum values
            minMaxValues[FunctionIf.X][FunctionIf.MIN] = Double.NEGATIVE_INFINITY;
            if (!xMin.isEmpty())
            {
                calcVal.processRealTerm(thread, xMin);
                minMaxValues[FunctionIf.X][FunctionIf.MIN] = calcVal.getReal();
            }
            minMaxValues[FunctionIf.X][FunctionIf.MAX] = Double.POSITIVE_INFINITY;
            if (!xMax.isEmpty())
            {
                calcVal.processRealTerm(thread, xMax);
                minMaxValues[FunctionIf.X][FunctionIf.MAX] = calcVal.getReal();
            }
            final Equation xValuesEq = linkedIntervals.get(0);
            xValues = xValuesEq.fillBoundedInterval(xValues, minMaxValues[FunctionIf.X]);
            if (xValues == null)
            {
                return;
            }

            minMaxValues[FunctionIf.Y][FunctionIf.MIN] = Double.NEGATIVE_INFINITY;
            if (!yMin.isEmpty())
            {
                calcVal.processRealTerm(thread, yMin);
                minMaxValues[FunctionIf.Y][FunctionIf.MIN] = calcVal.getReal();
            }
            minMaxValues[FunctionIf.Y][FunctionIf.MAX] = Double.POSITIVE_INFINITY;
            if (!yMax.isEmpty())
            {
                calcVal.processRealTerm(thread, yMax);
                minMaxValues[FunctionIf.Y][FunctionIf.MAX] = calcVal.getReal();
            }
            final Equation yValuesEq = isFunction1D ? linkedIntervals.get(0) : linkedIntervals.get(1);
            yValues = yValuesEq.fillBoundedInterval(yValues, minMaxValues[FunctionIf.Y]);
            if (yValues == null)
            {
                return;
            }

            // labels
            labels[FunctionIf.X] = xValuesEq.getName();
            labels[FunctionIf.Y] = yValuesEq.getName();
            labels[FunctionIf.Z] = "";

            // calculate z values
            if (argValues[0][0] == null)
            {
                argValues[0][0] = new CalculatedValue();
                argValues[1][0] = new CalculatedValue();
            }
            zValues = new double[xValues.length][yValues.length];
            if (isFunction1D)
            {
                calculate1D(xValuesEq, thread);
            }
            else
            {
                calculate2D(xValuesEq, yValuesEq, thread);
            }
            updateEqualBorders(minMaxValues[FunctionIf.Z]);
        }

        private void calculate1D(Equation xValuesEq, CalculaterTask thread) throws CancelException
        {
            final CalculatedValue calcVal = new CalculatedValue();
            for (int i = 0; i < xValues.length; i++)
            {
                for (int j = 0; j < yValues.length; j++)
                {
                    zValues[i][j] = Double.NaN;
                }

                argValues[0][0].setValue(xValues[i]);
                xValuesEq.setArgumentValues(argValues[0]);
                calcVal.processRealTerm(thread, functionTerm);
                final double zVal = calcVal.getReal();
                zValues[i][i] = zVal;
                if (i == 0)
                {
                    minMaxValues[FunctionIf.Z][FunctionIf.MIN] = minMaxValues[FunctionIf.Z][FunctionIf.MAX] = zVal;
                }
                else
                {
                    minMaxValues[FunctionIf.Z][FunctionIf.MIN] = Math.min(
                            minMaxValues[FunctionIf.Z][FunctionIf.MIN], zVal);
                    minMaxValues[FunctionIf.Z][FunctionIf.MAX] = Math.max(
                            minMaxValues[FunctionIf.Z][FunctionIf.MAX], zVal);
                }
            }
        }

        private void calculate2D(Equation xValuesEq, Equation yValuesEq, CalculaterTask thread) throws CancelException
        {
            final CalculatedValue calcVal = new CalculatedValue();
            for (int i = 0; i < xValues.length; i++)
            {
                argValues[0][0].setValue(xValues[i]);
                xValuesEq.setArgumentValues(argValues[0]);
                for (int j = 0; j < yValues.length; j++)
                {
                    argValues[1][0].setValue(yValues[j]);
                    yValuesEq.setArgumentValues(argValues[1]);
                    calcVal.processRealTerm(thread, functionTerm);
                    final double zVal = calcVal.getReal();
                    zValues[i][j] = zVal;
                    if (i == 0 && j == 0)
                    {
                        minMaxValues[FunctionIf.Z][FunctionIf.MIN] = minMaxValues[FunctionIf.Z][FunctionIf.MAX] = zVal;
                    }
                    else
                    {
                        minMaxValues[FunctionIf.Z][FunctionIf.MIN] = Math.min(
                                minMaxValues[FunctionIf.Z][FunctionIf.MIN], zVal);
                        minMaxValues[FunctionIf.Z][FunctionIf.MAX] = Math.max(
                                minMaxValues[FunctionIf.Z][FunctionIf.MAX], zVal);
                    }
                }
            }
        }
    }
}
