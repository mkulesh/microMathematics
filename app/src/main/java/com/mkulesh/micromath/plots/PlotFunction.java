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
package com.mkulesh.micromath.plots;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.mkulesh.micromath.R;
import com.mkulesh.micromath.dialogs.DialogAxisSettings;
import com.mkulesh.micromath.dialogs.DialogLineSettings;
import com.mkulesh.micromath.dialogs.DialogPlotSettings;
import com.mkulesh.micromath.dialogs.DialogResultDetails;
import com.mkulesh.micromath.formula.CalculaterTask;
import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.formula.CalculationResult;
import com.mkulesh.micromath.formula.Equation;
import com.mkulesh.micromath.formula.FormulaList;
import com.mkulesh.micromath.formula.TermField;
import com.mkulesh.micromath.formula.TermField.ErrorNotification;
import com.mkulesh.micromath.math.ArgumentValueItem;
import com.mkulesh.micromath.math.Vector2D;
import com.mkulesh.micromath.plots.views.FunctionView;
import com.mkulesh.micromath.properties.AxisPropertiesChangeIf;
import com.mkulesh.micromath.properties.LinePropertiesChangeIf;
import com.mkulesh.micromath.properties.PlotPropertiesChangeIf;
import com.mkulesh.micromath.undo.FormulaState;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomLayout;
import com.mkulesh.micromath.widgets.CustomTextView;
import com.mkulesh.micromath.widgets.ScaledDimensions;
import com.mkulesh.micromath.widgets.SizeChangingLayout;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.util.ArrayList;

public class PlotFunction extends CalculationResult implements SizeChangingLayout.SizeChangedIf,
        PlotPropertiesChangeIf, AxisPropertiesChangeIf, LinePropertiesChangeIf
{
    /*
     * Constants used to save/restore the instance state.
     */
    private static final String STATE_FUNCTIONVIEW_PARAMETERS = "functionview_parameters";

    // visual components
    private TermField xMin = null, xMax = null, yMin = null, yMax = null;
    private TermField xFunc = null, yFunc = null;
    private FunctionView functionView = null;
    private CustomTextView yFuncSettings = null;
    private final ArrayList<CustomTextView> axes = new ArrayList<CustomTextView>();
    private LinearLayout xDataLayout = null;
    private CustomTextView cornerView = null;

    // function data
    private final ArrayList<Vector2D> values = new ArrayList<Vector2D>();
    private final TermField[] boundaries = new TermField[4];

    // undo
    private FormulaState formulaState = null;

    /*********************************************************
     * Constructors
     *********************************************************/

    public PlotFunction(FormulaList formulaList, int id)
    {
        super(formulaList, null, 0);
        setId(id);
        onCreate();
    }

    /*********************************************************
     * GUI constructors to avoid lint warning
     *********************************************************/

    public PlotFunction(Context context)
    {
        super(null, null, 0);
    }

    public PlotFunction(Context context, AttributeSet attrs)
    {
        super(null, null, 0);
    }

    /*********************************************************
     * Re-implementation for methods for CalculationResult superclass
     *********************************************************/

    @Override
    public BaseType getBaseType()
    {
        return BaseType.PLOT_FUNCTION;
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
            if (isValid && !xFunc.isEmpty() && !yFunc.isEmpty())
            {
                String errorMsg = null;
                final ArrayList<String> indirectIntervals = getIndirectIntervals();
                if (!indirectIntervals.isEmpty())
                {
                    isValid = false;
                    errorMsg = String.format(getContext().getResources().getString(R.string.error_indirect_intervals),
                            indirectIntervals.toString());
                }
                else if (getDirectIntervals().size() > 1)
                {
                    isValid = false;
                    errorMsg = getContext().getResources().getString(R.string.error_ensure_single_interval);
                }
                yFunc.setError(errorMsg, ErrorNotification.LAYOUT_BORDER, null);
                xFunc.setError(errorMsg, ErrorNotification.LAYOUT_BORDER, null);
                break;
            }
        }

        if (!isValid)
        {
            functionView.setFunction(null);
            functionView.invalidate();
        }
        return isValid;
    }

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
        values.clear();
        ArrayList<Equation> linkedIntervals = getDirectIntervals();
        if (linkedIntervals.isEmpty())
        {
            values.add(new Vector2D(xFunc.getValue(thread), yFunc.getValue(thread)));
        }
        else if (linkedIntervals.size() == 1)
        {
            Equation linkedInterval = linkedIntervals.get(0);
            ArrayList<Double> par = linkedInterval.getInterval(thread);
            if (par != null)
            {
                for (int i = 0; i < par.size(); i++)
                {
                    linkedInterval.setArgument(par.get(i));
                    values.add(new Vector2D(xFunc.getValue(thread), yFunc.getValue(thread)));
                }
            }
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
            try
            {
                functionView
                        .setArea(xMin.getValue(null), xMax.getValue(null), yMin.getValue(null), yMax.getValue(null));
            }
            catch (CancelException e)
            {
                // nothing to do
            }
            functionView.setFunction(values);
            functionView.invalidate();
        }
        else
        {
            functionView.setFunction(null);
            functionView.invalidate();
        }
    }

    @Override
    public void onTermSelection(View owner, boolean isSelected, ArrayList<View> list)
    {
        if (list == null)
        {
            if (owner == yFuncSettings || owner == functionView)
            {
                // nothing to do
            }
            else if (axes.contains(owner))
            {
                list = new ArrayList<View>();
                for (int i = 0; i < axes.size(); i++)
                    list.add(axes.get(i));
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
        else if (owner == yFuncSettings)
        {
            DialogLineSettings d = new DialogLineSettings(getFormulaList().getActivity(), this,
                    functionView.getLineParameters());
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

    @Override
    public void updateTextSize()
    {
        super.updateTextSize();
        yFuncSettings.setHeight(getFormulaList().getDimen().get(ScaledDimensions.Type.TEXT_SIZE));
        updatePlotView();
    }

    @Override
    public boolean enableDetails()
    {
        return functionView.getFunction() != null && !functionView.getFunction().isEmpty();
    }

    @Override
    public void onDetails(View owner)
    {
        if (!enableDetails())
        {
            return;
        }

        final ArrayList<Vector2D> function = functionView.getFunction();
        final int n = function.size();
        ArrayList<ArgumentValueItem> calculatedItems = new ArrayList<ArgumentValueItem>(n);
        for (Vector2D v : function)
        {
            calculatedItems.add(new ArgumentValueItem(v.x, v.y));
        }
        DialogResultDetails d = new DialogResultDetails(getFormulaList().getActivity(), calculatedItems,
                getFormulaList().getDocumentSettings());
        d.show();
    }

    /*********************************************************
     * PlotParametersChangeIf interface implementation
     *********************************************************/

    @Override
    public void onLinePropertiesChange(boolean isChanged)
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
        updatePlotView();
        ViewUtils.invalidateLayout(functionView, layout);
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
        try
        {
            functionView.setArea(xMin.getValue(null), xMax.getValue(null), yMin.getValue(null), yMax.getValue(null));
        }
        catch (CancelException e)
        {
            // nothing to do
        }
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
        functionView.updateLabels();
        updatePlotView();
        ViewUtils.invalidateLayout(functionView, layout);
    }

    /*********************************************************
     * SizeChangedIf interface implementation
     *********************************************************/

    @Override
    public void onSizeChanged(SizeChangingLayout owner, int w, int h)
    {
        cornerView.getLayoutParams().width = LayoutParams.MATCH_PARENT;
        cornerView.getLayoutParams().height = h;
        cornerView.post(new Runnable()
        {
            public void run()
            {
                cornerView.requestLayout();
            }
        });
    }

    /*********************************************************
     * Read/write interface
     *********************************************************/

    /**
     * Parcelable interface: procedure writes the formula state
     */
    public Parcelable onSaveInstanceState()
    {
        Parcelable state = super.onSaveInstanceState();
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
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
            super.onRestoreInstanceState(bundle);
            functionView.onRestoreInstanceState(bundle.getParcelable(STATE_FUNCTIONVIEW_PARAMETERS));
            updatePlotView();
        }
    }

    @Override
    public boolean onStartReadXmlTag(XmlPullParser parser)
    {
        super.onStartReadXmlTag(parser);
        if (getBaseType().toString().equalsIgnoreCase(parser.getName()))
        {
            functionView.getPlotParameters().readFromXml(parser);
            functionView.getAxisParameters().readFromXml(parser);
        }
        String key = parser.getAttributeValue(null, FormulaList.XML_PROP_KEY);
        if (FormulaList.XML_TERM_TAG.equalsIgnoreCase(parser.getName()) && key != null)
        {
            if (key.equals(yFunc.getTermKey()))
            {
                functionView.getLineParameters().readFromXml(parser);
            }
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
            functionView.getPlotParameters().writeToXml(serializer);
            functionView.getAxisParameters().writeToXml(serializer);
        }
        if (FormulaList.XML_TERM_TAG.equalsIgnoreCase(serializer.getName()) && key != null)
        {
            if (key.equals(yFunc.getTermKey()))
            {
                functionView.getLineParameters().writeToXml(serializer);
            }
        }
        return false;
    }

    /*********************************************************
     * PlotFunction-specific methods
     *********************************************************/

    /**
     * Procedure creates the formula layout
     */
    private void onCreate()
    {
        inflateRootLayout(R.layout.plot_function, 300, 300);
        if (layout instanceof CustomLayout)
        {
            CustomLayout cl = (CustomLayout) layout;
            cl.setCustomFeaturesDisabled(true);
            cl.setBaselineAligned(false);
            cl.setBaselineAlignedChildIndex(1);
        }

        // expandable layouts for x and y data
        xDataLayout = (LinearLayout) layout.findViewById(R.id.plot_x_data_layout);
        if (xDataLayout instanceof SizeChangingLayout)
        {
            ((SizeChangingLayout) xDataLayout).setSizeChangedIf(this);
        }
        cornerView = (CustomTextView) layout.findViewById(R.id.plot_corner_view_id);

        // create graph area
        functionView = (FunctionView) layout.findViewById(R.id.plot_function_view);
        functionView.prepare(getFormulaList().getActivity(), this);

        // create editable fields
        yMax = addTerm(this, (LinearLayout) layout.findViewById(R.id.plot_y_max_layout),
                (CustomEditText) layout.findViewById(R.id.plot_y_max_value), this, false);
        boundaries[0] = yMax;

        yFunc = addTerm(this, (LinearLayout) layout.findViewById(R.id.plot_y_function_layout),
                (CustomEditText) layout.findViewById(R.id.plot_y_function_value), this, false);
        yFunc.termDepth = 1;

        yMin = addTerm(this, (LinearLayout) layout.findViewById(R.id.plot_y_min_layout),
                (CustomEditText) layout.findViewById(R.id.plot_y_min_value), this, false);
        boundaries[1] = yMin;

        xMin = addTerm(this, (LinearLayout) layout.findViewById(R.id.plot_x_min_layout),
                (CustomEditText) layout.findViewById(R.id.plot_x_min_value), this, false);
        boundaries[2] = xMin;

        xFunc = addTerm(this, (LinearLayout) layout.findViewById(R.id.plot_x_function_layout),
                (CustomEditText) layout.findViewById(R.id.plot_x_function_value), this, false);
        xFunc.termDepth = 1;

        xMax = addTerm(this, (LinearLayout) layout.findViewById(R.id.plot_x_max_layout),
                (CustomEditText) layout.findViewById(R.id.plot_x_max_value), this, false);
        boundaries[3] = xMax;

        for (TermField t : terms)
        {
            t.bracketsType = TermField.BracketsType.NEVER;
        }
        for (TermField t : boundaries)
        {
            t.termDepth = 2;
        }

        yFuncSettings = (CustomTextView) layout.findViewById(R.id.plot_y_function_settings);
        yFuncSettings.prepare(CustomTextView.SymbolType.HOR_LINE, getFormulaList().getActivity(), this);

        axes.add((CustomTextView) layout.findViewById(R.id.plot_x_axis1));
        axes.add((CustomTextView) layout.findViewById(R.id.plot_x_axis2));
        axes.add((CustomTextView) layout.findViewById(R.id.plot_y_axis1));
        axes.add((CustomTextView) layout.findViewById(R.id.plot_y_axis2));
        for (int i = 0; i < axes.size(); i++)
        {
            axes.get(i).prepare(CustomTextView.SymbolType.TEXT, getFormulaList().getActivity(), this);
        }
        updatePlotView();
    }

    private void updatePlotView()
    {
        functionView.clearDrawingCache();
        final ScaledDimensions dim = getFormulaList().getDimen();
        final float scale = getFormulaList().getDimen().getScaleFactor();
        functionView.getAxisParameters().scaleFactor = scale;
        functionView.getLineParameters().scaleFactor = scale;
        functionView.getLineParameters().preparePaint();
        layout.getLayoutParams().width = Math.round(functionView.getPlotParameters().width * scale);
        layout.getLayoutParams().height = Math.round(functionView.getPlotParameters().height * scale);
        yFuncSettings.setHeight(dim.get(ScaledDimensions.Type.TEXT_SIZE));
        yFuncSettings.setExternalPaint(functionView.getLineParameters().getPaint());
    }

    private boolean setEmptyBorders(int idx, TermField f1, TermField f2)
    {
        double[] minMaxValues = null;
        for (Vector2D val : values)
        {
            final double v = val.get(idx);
            if (minMaxValues == null)
            {
                minMaxValues = new double[2];
                minMaxValues[FunctionIf.MIN] = minMaxValues[FunctionIf.MAX] = v;
            }
            minMaxValues[FunctionIf.MIN] = Math.min(minMaxValues[FunctionIf.MIN], v);
            minMaxValues[FunctionIf.MAX] = Math.max(minMaxValues[FunctionIf.MAX], v);
        }
        updateEqualBorders(minMaxValues);
        return super.setEmptyBorders(minMaxValues, f1, f2);
    }
}
