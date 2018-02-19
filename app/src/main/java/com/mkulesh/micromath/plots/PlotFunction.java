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
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.mkulesh.micromath.dialogs.DialogAxisSettings;
import com.mkulesh.micromath.dialogs.DialogLineSettings;
import com.mkulesh.micromath.dialogs.DialogPlotSettings;
import com.mkulesh.micromath.dialogs.DialogRadioGroup;
import com.mkulesh.micromath.dialogs.DialogResultDetails;
import com.mkulesh.micromath.formula.CalculaterTask;
import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.formula.CalculationResult;
import com.mkulesh.micromath.formula.Equation;
import com.mkulesh.micromath.formula.FormulaList;
import com.mkulesh.micromath.formula.LinkHolder;
import com.mkulesh.micromath.formula.TermField;
import com.mkulesh.micromath.formula.TermField.BracketsType;
import com.mkulesh.micromath.formula.TermField.ErrorNotification;
import com.mkulesh.micromath.math.CalculatedValue;
import com.mkulesh.micromath.plots.views.FunctionPlotView;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.AxisPropertiesChangeIf;
import com.mkulesh.micromath.properties.LineProperties;
import com.mkulesh.micromath.properties.LinePropertiesChangeIf;
import com.mkulesh.micromath.properties.PlotPropertiesChangeIf;
import com.mkulesh.micromath.undo.FormulaState;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomLayout;
import com.mkulesh.micromath.widgets.CustomTextView;
import com.mkulesh.micromath.widgets.LineDrawable;
import com.mkulesh.micromath.widgets.ScaledDimensions;
import com.mkulesh.micromath.widgets.SizeChangingLayout;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PlotFunction extends CalculationResult implements SizeChangingLayout.SizeChangedIf,
        PlotPropertiesChangeIf, AxisPropertiesChangeIf, LinePropertiesChangeIf
{
    /*
     * Constants used to save/restore the instance state.
     */
    private static final String STATE_FUNCTIONVIEW_PARAMETERS = "functionview_parameters";
    private static final String STATE_FUNCTIONS_NUMBER = "functions_number";
    private static final String STATE_LINE_PARAMETERS = "line_parameters";
    public static final String XML_PROP_FUNCTIONS_NUMBER = "functionsNumber";

    // expandable layouts
    private LinearLayout xDataLayout = null, yDataLayout = null;

    // visual components
    private TermField xMin = null, xMax = null, yMin = null, yMax = null;
    private final ArrayList<CustomTextView> axes = new ArrayList<CustomTextView>();
    private FunctionPlotView functionView = null;
    private CustomTextView cornerView = null;

    // function data
    private final ArrayList<Function2D> functions = new ArrayList<Function2D>();
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
     * Re-implementation for methods for FormulaBase superclass
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
            if (isValid)
            {
                for (Function2D f : functions)
                {
                    if (!f.isContentValid(this))
                    {
                        isValid = false;
                    }
                }
            }
            break;
        }

        if (!isValid)
        {
            functionView.setFunctions(null);
            functionView.invalidate();
        }
        return isValid;
    }

    @Override
    public void updateTextSize()
    {
        super.updateTextSize();
        for (Function2D f : functions)
        {
            f.updateSettingsView();
        }
        updatePlotView();
    }

    @Override
    public boolean isNewTermEnabled()
    {
        return true;
    }

    /*********************************************************
     * Re-implementation for methods for CalculationResult superclass
     *********************************************************/

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
        functionView.setFunctions(null);
        functionView.invalidate();
    }

    @Override
    public void calculate(CalculaterTask thread) throws CancelException
    {
        for (Function2D f : functions)
        {
            f.calculate(thread);
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
            updatePlotBoundaries(functionView, xMin, xMax, yMin, yMax);
            ArrayList<FunctionIf> func = new ArrayList<FunctionIf>();
            for (Function2D f : functions)
            {
                func.add(f);
            }
            functionView.setFunctions(func);
            functionView.invalidate();
        }
        else
        {
            functionView.setFunctions(null);
            functionView.invalidate();
        }
    }

    /*********************************************************
     * Implementation for methods for FormulaChangeIf interface
     *********************************************************/

    @Override
    public void onTermSelection(View owner, boolean isSelected, ArrayList<View> list)
    {
        if (list == null)
        {
            if (owner == functionView)
            {
                // nothing to do
            }
            else if (axes.contains(owner))
            {
                list = new ArrayList<View>();
                for (int i = 0; i < axes.size(); i++)
                {
                    list.add(axes.get(i));
                }
            }
            else
            {
                for (Function2D f : functions)
                {
                    if (f.getSettingsView() == owner)
                    {
                        list = new ArrayList<View>();
                        list.add(owner);
                        break;
                    }
                }
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
        else if (axes.contains(owner))
        {
            DialogAxisSettings d = new DialogAxisSettings(getFormulaList().getActivity(), this,
                    functionView.getAxisParameters());
            formulaState = getState();
            d.show();
        }
        else
        {
            for (Function2D f : functions)
            {
                if (f.getSettingsView() == owner)
                {
                    DialogLineSettings d = new DialogLineSettings(getFormulaList().getActivity(), this,
                            f.getLineParameters());
                    formulaState = getState();
                    d.show();
                    break;
                }
            }
        }
        super.onObjectProperties(owner);
    }

    @Override
    public void onDelete(CustomEditText owner)
    {
        Function2D ownerFunc = findFunction(findTerm(owner));
        if (ownerFunc == null)
        {
            super.onDelete(null);
            return;
        }
        if (functions.size() == 1)
        {
            return;
        }
        getFormulaList().getUndoState().addEntry(getState());
        final int ownerIndex = functions.indexOf(ownerFunc);
        ownerFunc.erase();
        if (ownerIndex == 0)
        {
            functions.get(ownerIndex + 1).removeSeparator();
        }
        functions.remove(ownerFunc);
        reIndexTerms();
        if (ownerIndex <= functions.size())
        {
            final int newIdx = (ownerIndex > 0) ? ownerIndex - 1 : 0;
            functions.get(newIdx).y.requestFocus();
        }
    }

    @Override
    public boolean onNewTerm(TermField owner, String s, boolean requestFocus)
    {
        final String sep = getContext().getResources().getString(R.string.formula_term_separator);
        if (s == null || s.length() == 0 || !s.contains(sep))
        {
            // string does not contains the term separator: can not be processed
            return false;
        }

        // below, we will return true since the string is processed independently from
        // the result
        Function2D ownerFunc = findFunction(owner);
        if (ownerFunc == null)
        {
            return true;
        }

        // create and prepare new function
        Function2D f = ownerFunc.addNewAfter();
        if (f == null)
        {
            return true;
        }
        functions.add(f);
        reIndexTerms();
        updateTextSize();

        // remove comma and set focus
        if (ownerFunc.y.getText().contains(sep))
        {
            TermField.divideString(s, sep, ownerFunc.y, f.y);
        }
        if (requestFocus)
        {
            f.y.getEditText().requestFocus();
        }

        return true;
    }

    @Override
    public boolean enableDetails()
    {
        return functionView.getFunctions() != null && !functionView.getFunctions().isEmpty();
    }

    @Override
    public void onDetails(View owner)
    {
        if (!enableDetails())
        {
            return;
        }

        final ArrayList<FunctionIf> functions = functionView.getFunctions();

        if (functions.size() == 1)
        {
            showDetailsDialog(0);
        }
        else
        {
            final AppCompatActivity ctx = getFormulaList().getActivity();
            final DialogRadioGroup d = new DialogRadioGroup(ctx, R.string.dialog_function_selection, functions.size(),
                    new DialogRadioGroup.EventHandler()
                    {
                        public void onCreate(RadioButton[] rb)
                        {
                            for (int i = 0; i < rb.length; i++)
                            {
                                final String text = ctx.getString(R.string.dialog_function_selection) + " "
                                        + Integer.toString(i + 1) + ":";
                                rb[i].setText(text);
                                final Paint p = functions.get(i).getLineParameters().getPaint();
                                final int textWidth = (int) rb[i].getPaint().measureText(text);
                                rb[i].setCompoundDrawables(null, null,
                                        new LineDrawable(p, textWidth, (int) (p.getStrokeWidth() + 4)), null);
                                rb[i].setCompoundDrawablePadding(ctx.getResources().getDimensionPixelSize(
                                        R.dimen.dialog_content_padding));
                            }
                        }

                        public void onClick(int whichButton)
                        {
                            showDetailsDialog(whichButton);
                        }
                    });
            d.show();
        }
    }

    private void showDetailsDialog(int targetIdx)
    {
        final FunctionIf f = functionView.getFunctions().get(targetIdx);
        if (f.getXValues() != null && f.getYValues() != null)
        {
            DialogResultDetails d = new DialogResultDetails(getFormulaList().getActivity(),
                    f.getXValues(), f.getYValues(),
                    getFormulaList().getDocumentSettings(), null);
            d.show();
        }
    }

    /*********************************************************
     * PlotParametersChangeIf interface implementation
     *********************************************************/

    @Override
    public Dimension getDimension()
    {
        return Dimension.ONE_D;
    }

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
        updatePlotBoundaries(functionView, xMin, xMax, yMin, yMax);
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
            bundle.putInt(STATE_FUNCTIONS_NUMBER, functions.size());
            for (int i = 0; i < functions.size(); i++)
            {
                LineProperties lp = new LineProperties();
                lp.assign(functions.get(i).getLineParameters());
                bundle.putParcelable(STATE_LINE_PARAMETERS + String.valueOf(i), lp);
            }
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
            functionView.onRestoreInstanceState(bundle.getParcelable(STATE_FUNCTIONVIEW_PARAMETERS));
            ensureFunctionsNumber(bundle.getInt(STATE_FUNCTIONS_NUMBER, 0));
            for (int i = 0; i < functions.size(); i++)
            {
                LineProperties lp = (LineProperties) bundle.getParcelable(STATE_LINE_PARAMETERS + String.valueOf(i));
                if (lp != null)
                {
                    functions.get(i).getLineParameters().assign(lp);
                }
            }
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
            functionView.getPlotParameters().readFromXml(parser);
            functionView.getAxisParameters().readFromXml(parser);
            String attr = parser.getAttributeValue(null, XML_PROP_FUNCTIONS_NUMBER);
            if (attr != null)
            {
                ensureFunctionsNumber(Integer.parseInt(attr));
            }
            updatePlotView();
        }
        String key = parser.getAttributeValue(null, FormulaList.XML_PROP_KEY);
        if (FormulaList.XML_TERM_TAG.equalsIgnoreCase(parser.getName()) && key != null)
        {
            for (Function2D f : functions)
            {
                if (key.equals(f.y.getTermKey()))
                {
                    f.getLineParameters().readFromXml(parser);
                    f.updateSettingsView();
                }
            }
        }
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
            serializer.attribute(FormulaList.XML_NS, XML_PROP_FUNCTIONS_NUMBER, String.valueOf(functions.size()));
        }
        if (FormulaList.XML_TERM_TAG.equalsIgnoreCase(serializer.getName()) && key != null)
        {
            for (Function2D f : functions)
            {
                if (key.equals(f.y.getTermKey()))
                {
                    f.getLineParameters().writeToXml(serializer);
                }
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
        Function2D function = new Function2D();

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
        yDataLayout = (LinearLayout) layout.findViewById(R.id.plot_y_data_layout);

        // create graph area
        functionView = (FunctionPlotView) layout.findViewById(R.id.plot_function_view);
        functionView.prepare(getFormulaList().getActivity(), this);

        // create editable fields
        yMax = addTerm(this, (LinearLayout) layout.findViewById(R.id.plot_y_max_layout),
                (CustomEditText) layout.findViewById(R.id.plot_y_max_value), this, false);
        boundaries[0] = yMax;

        function.initializePrimaryY();

        yMin = addTerm(this, (LinearLayout) layout.findViewById(R.id.plot_y_min_layout),
                (CustomEditText) layout.findViewById(R.id.plot_y_min_value), this, false);
        boundaries[1] = yMin;

        xMin = addTerm(this, (LinearLayout) layout.findViewById(R.id.plot_x_min_layout),
                (CustomEditText) layout.findViewById(R.id.plot_x_min_value), this, false);
        boundaries[2] = xMin;

        function.initializePrimaryX();

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

        axes.add((CustomTextView) layout.findViewById(R.id.plot_x_axis1));
        axes.add((CustomTextView) layout.findViewById(R.id.plot_x_axis2));
        axes.add((CustomTextView) layout.findViewById(R.id.plot_y_axis1));
        axes.add((CustomTextView) layout.findViewById(R.id.plot_y_axis2));
        for (int i = 0; i < axes.size(); i++)
        {
            axes.get(i).prepare(CustomTextView.SymbolType.TEXT, getFormulaList().getActivity(), this);
        }

        functions.add(function);
        reIndexTerms();
        updatePlotView();
    }

    private Function2D findFunction(TermField owner)
    {
        if (owner != null)
        {
            for (Function2D f : functions)
            {
                if (f.y.equals(owner))
                {
                    return f;
                }
            }
        }
        return null;
    }

    private void ensureFunctionsNumber(int number)
    {
        Function2D ownerFunc = functions.get(0);
        while (functions.size() > number)
        {
            Function2D f = functions.get(functions.size() - 1);
            f.erase();
            functions.remove(f);
        }
        while (functions.size() < number)
        {
            functions.add(ownerFunc.addNewAfter());
        }
        reIndexTerms();
        updateTextSize();
    }

    private void reIndexTerms()
    {
        if (functions.size() == 1)
        {
            functions.get(0).x.setTermKey(getContext().getResources().getString(R.string.formula_x_function_key));
            functions.get(0).y.setTermKey(getContext().getResources().getString(R.string.formula_y_function_key));
        }
        else
        {
            // Re-index terms
            int i = 1;
            for (TermField t : terms)
            {
                for (Function2D f : functions)
                {
                    if (f.y.equals(t))
                    {
                        f.index = i++;
                        break;
                    }
                }
            }
            final String xKey = getContext().getResources().getString(R.string.formula_x_function_key);
            final String yKey = getContext().getResources().getString(R.string.formula_y_function_key);
            for (Function2D f : functions)
            {
                f.x.setTermKey(xKey + String.valueOf(f.index));
                f.y.setTermKey(yKey + String.valueOf(f.index));
            }

            Collections.sort(functions, new Comparator<Function2D>()
            {
                public int compare(Function2D s1, Function2D s2)
                {
                    if (s1.index == s2.index)
                    {
                        return 0;
                    }
                    return (s1.index < s2.index) ? -1 : 1;
                }
            });
        }
    }

    private void updatePlotView()
    {
        final float scale = getFormulaList().getDimen().getScaleFactor();
        functionView.setScale(scale);
        layout.getLayoutParams().width = Math.round(functionView.getPlotParameters().width * scale);
        layout.getLayoutParams().height = Math.round(functionView.getPlotParameters().height * scale);
        for (Function2D f : functions)
        {
            f.updateSettingsView();
        }
    }

    private boolean setEmptyBorders(int idx, TermField f1, TermField f2)
    {
        double[] minMaxValues = null;
        for (Function2D f : functions)
        {
            if (minMaxValues == null)
            {
                minMaxValues = new double[2];
                minMaxValues[FunctionIf.MIN] = f.getMinMaxValues(idx)[FunctionIf.MIN];
                minMaxValues[FunctionIf.MAX] = f.getMinMaxValues(idx)[FunctionIf.MAX];
            }
            else
            {
                minMaxValues[FunctionIf.MIN] = Math.min(minMaxValues[FunctionIf.MIN],
                        f.getMinMaxValues(idx)[FunctionIf.MIN]);
                minMaxValues[FunctionIf.MAX] = Math.max(minMaxValues[FunctionIf.MAX],
                        f.getMinMaxValues(idx)[FunctionIf.MAX]);
            }
        }
        return super.setEmptyBorders(minMaxValues, f1, f2);
    }

    /*********************************************************
     * Helper class that implements function interface
     *********************************************************/

    private class Function2D implements FunctionIf
    {
        private static final String X_FUNCTION_TAG = "X_FUNCTION_TAG";
        private static final String X_SEPARATOR_TAG = "X_SEPARATOR_TAG";
        private static final String Y_FUNCTION_TAG = "Y_FUNCTION_TAG";
        private static final String Y_SETTINGS_TAG = "Y_SETTINGS_TAG";

        private LinearLayout xLayout = null;
        private TermField x = null;
        private CustomTextView xSeparator = null;
        private LinearLayout yLayout = null;
        private TermField y = null;
        private CustomTextView settingsView = null;
        private Equation linkedInterval = null;
        private final CalculatedValue[] argValues = new CalculatedValue[1];
        private final double[] xMinMaxValues = new double[2];
        private final double[] yMinMaxValues = new double[2];
        private double[] xValues = new double[1];
        private double[] yValues = new double[1];
        private LineProperties lineParameters = new LineProperties();
        public int index = 0;

        @Override
        public Type getType()
        {
            return Type.FUNCTION_2D;
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
            return null;
        }

        @Override
        public double[] getMinMaxValues(int idx)
        {
            return (idx == 0) ? xMinMaxValues : yMinMaxValues;
        }

        @Override
        public LineProperties getLineParameters()
        {
            return lineParameters;
        }

        @Override
        public String[] getLabels()
        {
            return null;
        }

        public View getSettingsView()
        {
            return settingsView;
        }

        public void updateSettingsView()
        {
            final ScaledDimensions dim = getFormulaList().getDimen();
            lineParameters.scaleFactor = dim.getScaleFactor();
            lineParameters.preparePaint();
            settingsView.setHeight(dim.get(ScaledDimensions.Type.TEXT_SIZE));
            settingsView.setExternalPaint(lineParameters.getPaint());
        }

        private void initializeX(LinearLayout xLayout, int idx)
        {
            this.xLayout = xLayout;
            x = addTerm(PlotFunction.this, this.xLayout, idx, (CustomEditText) this.xLayout.getChildAt(0),
                    PlotFunction.this, 0);
            x.termDepth = 1;
            x.bracketsType = BracketsType.NEVER;
        }

        public void initializePrimaryX()
        {
            initializeX((LinearLayout) xDataLayout.findViewWithTag(X_FUNCTION_TAG), -1);
        }

        private void initializeY(LinearLayout yLayout, int idx)
        {
            this.yLayout = yLayout;
            y = addTerm(PlotFunction.this, this.yLayout, idx, (CustomEditText) this.yLayout.getChildAt(0),
                    PlotFunction.this, 0);
            y.termDepth = 1;
            y.bracketsType = BracketsType.NEVER;
        }

        public void initializePrimaryY()
        {
            initializeY((LinearLayout) yDataLayout.findViewWithTag(Y_FUNCTION_TAG), -1);
            settingsView = (CustomTextView) yDataLayout.findViewWithTag(Y_SETTINGS_TAG);
            settingsView.prepare(CustomTextView.SymbolType.HOR_LINE, getFormulaList().getActivity(), PlotFunction.this);
        }

        public Function2D addNewAfter()
        {
            int xViewIndex = ViewUtils.getViewIndex(xDataLayout, xLayout);
            int yViewIndex = ViewUtils.getViewIndex(yDataLayout, settingsView);
            if (xViewIndex < 0 || yViewIndex < 0)
            {
                return null;
            }

            Function2D f = new Function2D();
            f.lineParameters.setNextDefault(this.lineParameters);
            ArrayList<View> newTerms = new ArrayList<View>();
            inflateElements(newTerms, R.layout.plot_function_add_func, true);
            for (View t : newTerms)
            {
                final String tag = (String) t.getTag();
                if (Y_FUNCTION_TAG.equals(tag) && t instanceof LinearLayout)
                {
                    f.initializeY((LinearLayout) t, terms.indexOf(y) + 1);
                    yDataLayout.addView(t, ++yViewIndex);
                }
                else if (Y_SETTINGS_TAG.equals(tag) && t instanceof CustomTextView)
                {
                    f.settingsView = (CustomTextView) t;
                    f.settingsView.prepare(CustomTextView.SymbolType.HOR_LINE, getFormulaList().getActivity(),
                            PlotFunction.this);
                    yDataLayout.addView(t, ++yViewIndex);
                }
                else if (X_SEPARATOR_TAG.equals(tag) && t instanceof CustomTextView)
                {
                    f.xSeparator = (CustomTextView) t;
                    f.xSeparator.setText(getContext().getResources().getString(R.string.formula_term_separator));
                    xDataLayout.addView(t, ++xViewIndex);
                }
                else if (X_FUNCTION_TAG.equals(tag) && t instanceof LinearLayout)
                {
                    f.initializeX((LinearLayout) t, terms.indexOf(x) + 1);
                    xDataLayout.addView(t, ++xViewIndex);
                }
            }
            return f;
        }

        public void erase()
        {
            terms.remove(x);
            terms.remove(y);
            xDataLayout.removeView(xLayout);
            removeSeparator();
            yDataLayout.removeView(settingsView);
            yDataLayout.removeView(yLayout);
        }

        public void removeSeparator()
        {
            if (xSeparator != null)
            {
                xDataLayout.removeView(xSeparator);
                xSeparator = null;
            }
        }

        public boolean isContentValid(LinkHolder linkHolder)
        {
            linkedInterval = null;
            boolean isValid = true;
            for (Equation e : linkHolder.getDirectIntervals())
            {
                if (x.dependsOn(e) || y.dependsOn(e))
                {
                    if (linkedInterval != null)
                    {
                        isValid = false;
                        break;
                    }
                    linkedInterval = e;
                }
            }
            if (!x.isEmpty() && !y.isEmpty())
            {
                String errorMsg = null;
                final ArrayList<String> indirectIntervals = getIndirectIntervals();
                if (!indirectIntervals.isEmpty())
                {
                    isValid = false;
                    errorMsg = String.format(getContext().getResources().getString(R.string.error_indirect_intervals),
                            indirectIntervals.toString());
                }
                else if (!isValid)
                {
                    errorMsg = getContext().getResources().getString(R.string.error_ensure_single_interval);
                }
                y.setError(errorMsg, ErrorNotification.LAYOUT_BORDER, null);
                x.setError(errorMsg, ErrorNotification.LAYOUT_BORDER, null);
            }
            return isValid;
        }

        public void calculate(CalculaterTask thread) throws CancelException
        {
            final CalculatedValue calcVal = new CalculatedValue();
            xMinMaxValues[FunctionIf.MIN] = xMinMaxValues[FunctionIf.MAX] = Double.NaN;
            yMinMaxValues[FunctionIf.MIN] = yMinMaxValues[FunctionIf.MAX] = Double.NaN;
            if (argValues[0] == null)
            {
                argValues[0] = new CalculatedValue();
            }
            if (linkedInterval == null)
            {
                if (xValues.length != 1)
                {
                    xValues = new double[1];
                }
                calcVal.processRealTerm(thread, x);
                xValues[0] = calcVal.getReal();
                if (yValues.length != 1)
                {
                    yValues = new double[1];
                }
                calcVal.processRealTerm(thread, y);
                yValues[0] = calcVal.getReal();
                xMinMaxValues[FunctionIf.MIN] = xMinMaxValues[FunctionIf.MAX] = xValues[0];
                yMinMaxValues[FunctionIf.MIN] = yMinMaxValues[FunctionIf.MAX] = yValues[0];
            }
            else
            {
                final ArrayList<CalculatedValue> par = linkedInterval.getInterval(thread);
                if (par == null)
                {
                    return;
                }
                if (xValues.length != par.size())
                {
                    xValues = new double[par.size()];
                }
                if (yValues.length != par.size())
                {
                    yValues = new double[par.size()];
                }
                for (int i = 0; i < par.size(); i++)
                {
                    argValues[0].assign(par.get(i));
                    linkedInterval.setArgumentValues(argValues);
                    calcVal.processRealTerm(thread, x);
                    final double xVal = calcVal.getReal();
                    calcVal.processRealTerm(thread, y);
                    final double yVal = calcVal.getReal();
                    xValues[i] = xVal;
                    yValues[i] = yVal;
                    if (i == 0)
                    {
                        xMinMaxValues[FunctionIf.MIN] = xMinMaxValues[FunctionIf.MAX] = xVal;
                        yMinMaxValues[FunctionIf.MIN] = yMinMaxValues[FunctionIf.MAX] = yVal;
                    }
                    else
                    {
                        xMinMaxValues[FunctionIf.MIN] = Math.min(xMinMaxValues[FunctionIf.MIN], xVal);
                        xMinMaxValues[FunctionIf.MAX] = Math.max(xMinMaxValues[FunctionIf.MAX], xVal);
                        yMinMaxValues[FunctionIf.MIN] = Math.min(yMinMaxValues[FunctionIf.MIN], yVal);
                        yMinMaxValues[FunctionIf.MAX] = Math.max(yMinMaxValues[FunctionIf.MAX], yVal);
                    }
                }
            }
            updateEqualBorders(xMinMaxValues);
            updateEqualBorders(yMinMaxValues);
        }
    }
}
