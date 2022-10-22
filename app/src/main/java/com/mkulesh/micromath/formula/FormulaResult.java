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
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import com.mkulesh.micromath.dialogs.DialogResultDetails;
import com.mkulesh.micromath.dialogs.DialogResultSettings;
import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.formula.TermField.ErrorNotification;
import com.mkulesh.micromath.math.CalculatedValue;
import com.mkulesh.micromath.math.EquationArrayResult;
import com.mkulesh.micromath.R;
import com.mkulesh.micromath.properties.ResultProperties;
import com.mkulesh.micromath.properties.ResultPropertiesChangeIf;
import com.mkulesh.micromath.ta.TestSession;
import com.mkulesh.micromath.undo.FormulaState;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomLayout;
import com.mkulesh.micromath.widgets.CustomTextView;
import com.mkulesh.micromath.widgets.FocusChangeIf;
import com.mkulesh.micromath.widgets.MatrixLayout;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.util.ArrayList;

public class FormulaResult extends CalculationResult implements ResultPropertiesChangeIf, FocusChangeIf
{
    private static final String STATE_RESULT_PROPERTIES = "result_properties";
    public static final String CELL_DOTS = "...";

    public enum ResultType
    {
        NONE,
        NAN,
        CONSTANT,
        ARRAY_1D,
        ARRAY_2D
    }

    private TermField leftTerm = null;
    private CustomTextView resultAssign = null;
    private ResultType resultType = ResultType.NONE;

    // Constant or invalid result
    private CalculatedValue constantResult = null;
    private TermField constantResultField = null;

    // Array and matrix results
    private EquationArrayResult arrayArgument = null, arrayResult = null;
    private MatrixLayout arrayResultMatrix = null;
    private final ArrayList<TermField> arrayResultTerms = new ArrayList<>();
    private CustomTextView leftBracket = null, rightBracket = null;

    private final ResultProperties properties = new ResultProperties();

    // undo
    private FormulaState formulaState = null;

    /*--------------------------------------------------------*
     * Constructors
     *--------------------------------------------------------*/

    public FormulaResult(FormulaList formulaList, int id)
    {
        super(formulaList, null, 0);
        setId(id);
        onCreate();
    }

    /*--------------------------------------------------------*
     * GUI constructors to avoid lint warning
     *--------------------------------------------------------*/

    public FormulaResult(Context context)
    {
        super(null, null, 0);
    }

    public FormulaResult(Context context, AttributeSet attrs)
    {
        super(null, null, 0);
    }

    /*--------------------------------------------------------*
     * Re-implementation for methods for FormulaBase superclass
     *--------------------------------------------------------*/

    @Override
    public BaseType getBaseType()
    {
        return BaseType.RESULT;
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
            if (isValid && !leftTerm.isEmpty())
            {
                String errorMsg = null;
                final ArrayList<String> indirectIntervals = getIndirectIntervals();
                if (!indirectIntervals.isEmpty() && !getDirectIntervals().isEmpty())
                {
                    isValid = false;
                    errorMsg = String.format(getContext().getResources().getString(R.string.error_indirect_intervals),
                            indirectIntervals.toString());
                }
                else if (getAllIntervals().size() > 2)
                {
                    isValid = false;
                    errorMsg = getContext().getResources().getString(R.string.error_ensure_double_interval);
                }
                leftTerm.setError(errorMsg, ErrorNotification.LAYOUT_BORDER, null);
            }
            break;
        }

        if (!isValid)
        {
            clearResult();
            showResult();
        }
        return disableCalculation() || isValid;
    }

    @Override
    public void undo(FormulaState state)
    {
        super.undo(state);
        updateResultView(true);
        ViewUtils.invalidateLayout(layout, layout);
    }

    @Override
    public void updateTextSize()
    {
        super.updateTextSize();
        if (isArrayResult())
        {
            arrayResultMatrix.updateTextSize(getFormulaList().getDimen());
        }
    }

    @Override
    public void updateTextColor()
    {
        super.updateTextColor();
        if (isArrayResult())
        {
            arrayResultMatrix.updateTextColor();
        }
    }

    @Override
    public int getNextFocusId(CustomEditText owner, FocusChangeIf.NextFocusType focusType)
    {
        if (isArrayResult())
        {
            return super.getNextFocusId(owner, focusType, arrayResultTerms);
        }
        return super.getNextFocusId(owner, focusType);
    }

    /*--------------------------------------------------------*
     * Implementation for methods for FocusChangeIf interface
     *--------------------------------------------------------*/

    @Override
    public int onGetNextFocusId(CustomEditText owner, NextFocusType focusType)
    {
        if (owner == null)
        {
            return R.id.main_list_view;
        }
        return getNextFocusId(owner, focusType);
    }

    /*--------------------------------------------------------*
     * Re-implementation for methods for CalculationResult superclass
     *--------------------------------------------------------*/

    @Override
    public void invalidateResult()
    {
        if (!disableCalculation())
        {
            constantResultField.setText("");
        }
        arrayResultMatrix.setText("", getFormulaList().getDimen());
    }

    @Override
    public void calculate(CalculaterTask thread) throws CancelException
    {
        clearResult();
        final TestSession ta = getFormulaList().getTaSession();

        if (disableCalculation())
        {
            if (!leftTerm.isTerm() && ta != null)
            {
                if (resultType == ResultType.NONE)
                {
                    ta.setResult(leftTerm.getText(), constantResultField.getText());
                }
                else
                {
                    ta.setResult(leftTerm.getText(), fillResultString());
                }
            }
            return;
        }

        // Inspect linked array
        final Equation arrayLink = leftTerm.getLinkedArray();
        final int[] arrayDimension = (arrayLink != null && arrayLink.getArrayResult() != null) ? arrayLink.getArrayDimensions() : null;
        ArrayList<Equation> linkedIntervals = getAllIntervals();

        if (arrayLink != null && arrayDimension != null)
        {
            // Directly copy previously calculated array, no re-calculation is necessary
            collectArrayResults(arrayLink, arrayDimension);
        }
        else if (linkedIntervals.isEmpty())
        {
            // trigger re-calculations for constant
            resultType = ResultType.CONSTANT;
            constantResult = new CalculatedValue();
            leftTerm.getValue(thread, constantResult);
        }
        else
        {
            // trigger re-calculations for linked intervals
            collectIntervalResults(thread, linkedIntervals);
        }

        if (!leftTerm.isTerm() && ta != null)
        {
            ta.setResult(leftTerm.getText(), fillResultString());
        }
    }

    private void collectArrayResults(final Equation arrayLink, final int[] arrayDimension)
    {
        resultType = ResultType.NAN;

        // A vector
        if (arrayDimension.length == 1)
        {
            final int xLength = arrayDimension[0];
            resultType = ResultType.ARRAY_1D;
            arrayArgument = new EquationArrayResult(xLength);
            arrayResult = new EquationArrayResult(xLength, 1);
            for (int xIndex = 0; xIndex < xLength; xIndex++)
            {
                arrayArgument.getValue1D(xIndex).setValue(xIndex);
                arrayResult.getValue2D(xIndex, 0).assign(arrayLink.getArrayResult().getValue1D(xIndex));
            }
            return;
        }

        // A matrix
        if (arrayDimension.length == 2)
        {
            final int xLength = arrayDimension[0];
            final int yLength = arrayDimension[1];
            resultType = ResultType.ARRAY_2D;
            arrayResult = new EquationArrayResult(xLength, yLength);
            for (int xIndex = 0; xIndex < xLength; xIndex++)
            {
                for (int yIndex = 0; yIndex < yLength; yIndex++)
                {
                    arrayResult.getValue2D(xIndex, yIndex).assign(arrayLink.getArrayResult().getValue2D(xIndex, yIndex));
                }
            }
        }

        // Not a vector/matrix: nothing to do
    }

    private void collectIntervalResults(CalculaterTask thread, ArrayList<Equation> linkedIntervals) throws CancelException
    {
        resultType = ResultType.NAN;

        // A vector
        if (linkedIntervals.size() == 1)
        {
            final CalculatedValue[] argValues = new CalculatedValue[1];
            argValues[0] = new CalculatedValue();
            final CalculatedValue[] xValues = linkedIntervals.get(0).getInterval();
            if (xValues != null)
            {
                final int xLength = xValues.length;
                resultType = ResultType.ARRAY_1D;
                arrayArgument = new EquationArrayResult(xLength);
                arrayResult = new EquationArrayResult(xLength, 1);
                for (int xIndex = 0; xIndex < xLength; xIndex++)
                {
                    argValues[0].assign(xValues[xIndex]);
                    arrayArgument.getValue1D(xIndex).assign(argValues[0]);
                    linkedIntervals.get(0).setArgumentValues(argValues);
                    leftTerm.getValue(thread, arrayResult.getValue2D(xIndex, 0));
                }
            }
            return;
        }

        // A matrix
        if (linkedIntervals.size() == 2)
        {
            final CalculatedValue[][] argValues = new CalculatedValue[2][1];
            argValues[0][0] = new CalculatedValue();
            final CalculatedValue[] xValues = linkedIntervals.get(0).getInterval();
            argValues[1][0] = new CalculatedValue();
            final CalculatedValue[] yValues = linkedIntervals.get(1).getInterval();
            if (xValues != null && yValues != null)
            {
                final int xLength = xValues.length;
                final int yLength = yValues.length;
                resultType = ResultType.ARRAY_2D;
                arrayResult = new EquationArrayResult(xLength, yLength);
                for (int xIndex = 0; xIndex < xLength; xIndex++)
                {
                    argValues[0][0].assign(xValues[xIndex]);
                    linkedIntervals.get(0).setArgumentValues(argValues[0]);
                    for (int yIndex = 0; yIndex < yLength; yIndex++)
                    {
                        argValues[1][0].assign(yValues[yIndex]);
                        linkedIntervals.get(1).setArgumentValues(argValues[1]);
                        leftTerm.getValue(thread, arrayResult.getValue2D(xIndex, yIndex));
                    }
                }
            }
        }

        // Not a vector/matrix: nothing to do
    }

    @Override
    public void showResult()
    {
        final int visibility = isResultVisible() ? View.VISIBLE : View.GONE;
        resultAssign.setVisibility(visibility);

        switch (resultType)
        {
        case NONE:
        case NAN:
        case CONSTANT:
        {
            leftBracket.setVisibility(View.GONE);
            constantResultField.getEditText().setVisibility(visibility);
            arrayResultMatrix.setVisibility(View.GONE);
            rightBracket.setVisibility(View.GONE);
            if (!disableCalculation())
            {
                constantResultField.setText(fillResultString());
            }
            break;
        }
        case ARRAY_1D:
        case ARRAY_2D:
        {
            leftBracket.setVisibility(visibility);
            constantResultField.getEditText().setVisibility(View.GONE);
            arrayResultMatrix.setVisibility(visibility);
            rightBracket.setVisibility(visibility);
            fillResultMatrix();
            break;
        }
        }
    }

    @Override
    public boolean disableCalculation()
    {
        return properties.resultFieldType == ResultProperties.ResultFieldType.SKIP;
    }

    /*--------------------------------------------------------*
     * Implementation for methods for FormulaChangeIf interface
     *--------------------------------------------------------*/

    @Override
    public void onDetails()
    {
        if (enableDetails())
        {
            DialogResultDetails d = new DialogResultDetails(getFormulaList().getActivity(),
                    arrayArgument, arrayResult,
                    getFormulaList().getDocumentSettings(), properties);
            d.show();
        }
    }

    @Override
    public void onObjectProperties(View owner)
    {
        if (owner == this)
        {
            properties.showArrayLength = isArrayResult();
            DialogResultSettings d = new DialogResultSettings(getFormulaList().getActivity(), this, properties);
            formulaState = getState();
            d.show();
        }
        super.onObjectProperties(owner);
    }

    @Override
    public void onResultPropertiesChange(boolean isChanged)
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
        if (disableCalculation())
        {
            clearResult();
        }
        updateResultView(true);
        ViewUtils.invalidateLayout(layout, layout);
    }

    @Override
    public boolean enableDetails()
    {
        return resultType == ResultType.ARRAY_1D;
    }

    /*--------------------------------------------------------*
     * Read/write interface
     *--------------------------------------------------------*/

    /**
     * Parcelable interface: procedure writes the formula state
     */
    @Override
    public Parcelable onSaveInstanceState()
    {
        Parcelable state = super.onSaveInstanceState();
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            ResultProperties rp = new ResultProperties();
            rp.assign(properties);
            bundle.putParcelable(STATE_RESULT_PROPERTIES, rp);
        }
        return state;
    }

    /**
     * Parcelable interface: procedure reads the formula state
     */
    @Override
    public void onRestoreInstanceState(Parcelable state)
    {
        if (state == null)
        {
            return;
        }
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            properties.assign(bundle.getParcelable(STATE_RESULT_PROPERTIES));
            super.onRestoreInstanceState(bundle);
            updateResultView(false);
        }
    }

    @Override
    public boolean onStartReadXmlTag(XmlPullParser parser)
    {
        super.onStartReadXmlTag(parser);
        if (getBaseType().toString().equalsIgnoreCase(parser.getName()))
        {
            properties.readFromXml(parser);
            updateResultView(false);
        }
        return false;
    }

    @Override
    public boolean onStartWriteXmlTag(XmlSerializer serializer, String key) throws Exception
    {
        super.onStartWriteXmlTag(serializer, key);
        if (getBaseType().toString().equalsIgnoreCase(serializer.getName()))
        {
            properties.writeToXml(serializer);
        }
        // The calculation results shall be stored within *.mmt file as well.
        if (!disableCalculation() && FormulaList.XML_TERM_TAG.equalsIgnoreCase(serializer.getName()) &&
                key != null && key.equalsIgnoreCase(constantResultField.getTermKey()))
        {
            serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_TEXT, fillResultString());
            return true; // do not write everything for this term more
        }
        return false;
    }

    /*--------------------------------------------------------*
     * FormulaResult-specific methods
     *--------------------------------------------------------*/

    public boolean isResultVisible()
    {
        return properties.resultFieldType != ResultProperties.ResultFieldType.HIDE;
    }

    private boolean isFractionResult()
    {
        return properties.resultFieldType == ResultProperties.ResultFieldType.FRACTION;
    }

    public boolean isArrayResult()
    {
        return resultType == ResultType.ARRAY_1D || resultType == ResultType.ARRAY_2D;
    }

    /**
     * Procedure creates the formula layout
     */
    private void onCreate()
    {
        inflateRootLayout(R.layout.formula_result, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        // create name term
        {
            CustomEditText v = layout.findViewById(R.id.formula_result_name);
            leftTerm = addTerm(this, layout.findViewById(R.id.result_function_layout), v, this, false);
            leftTerm.bracketsType = TermField.BracketsType.NEVER;
        }
        // create assign character
        {
            resultAssign = layout.findViewById(R.id.formula_result_assign);
            resultAssign.prepare(CustomTextView.SymbolType.TEXT, getFormulaList().getActivity(), this);
        }
        // create result term
        {
            CustomEditText v = layout.findViewById(R.id.formula_result_value);
            constantResultField = addTerm(this, layout, v, this, true);
            constantResultField.bracketsType = TermField.BracketsType.NEVER;
            arrayResultMatrix = layout.findViewById(R.id.formula_result_table);
        }
        // brackets
        {
            leftBracket = layout.findViewById(R.id.formula_result_left_bracket);
            leftBracket.prepare(CustomTextView.SymbolType.LEFT_SQR_BRACKET, getFormulaList().getActivity(), this);
            leftBracket.setText("."); // this text defines view width/height

            rightBracket = layout.findViewById(R.id.formula_result_right_bracket);
            rightBracket.prepare(CustomTextView.SymbolType.RIGHT_SQR_BRACKET, getFormulaList().getActivity(), this);
            rightBracket.setText("."); // this text defines view width/height
        }
        updateResultView(false);
    }

    private void updateResultView(boolean checkContent)
    {
        // Allow to manually edit result field that is not calculated
        constantResultField.isWritable = disableCalculation();
        if (checkContent)
        {
            if (isContentValid(ValidationPassType.VALIDATE_SINGLE_FORMULA))
            {
                isContentValid(ValidationPassType.VALIDATE_LINKS);
            }
        }
        showResult();
    }

    private void clearResult()
    {
        resultType = ResultType.NONE;
        constantResult = null;
        arrayArgument = null;
        arrayResult = null;
    }

    private void fillResultMatrix()
    {
        arrayResultTerms.clear();
        if (!isArrayResult())
        {
            return;
        }
        final int xValuesNumber = arrayResult.getDimensions()[0];
        final int rowsNumber = Math.min(xValuesNumber, properties.arrayLength + 1);
        final int yValuesNumber = arrayResult.getDimensions()[1];
        final int colsNumber = Math.min(yValuesNumber, properties.arrayLength + 1);

        arrayResultMatrix.resize(rowsNumber, colsNumber, R.layout.formula_result_cell,
            (int row, int col, final CustomLayout layout, final CustomEditText text) -> {
                final TermField tf = new TermField(this, this, layout, 0, text);
                text.prepare(getFormulaList().getActivity(), this);
                text.setTextWatcher(false);
                text.setChangeIf(tf, this);
                return tf;
            },
            getFormulaList().getDimen());
        arrayResultTerms.add(leftTerm);
        arrayResultTerms.addAll(arrayResultMatrix.getTerms());

        for (int r = 0; r < rowsNumber; r++)
        {
            int dataRowIdx = r;
            if (xValuesNumber > properties.arrayLength)
            {
                // before the last line
                if (r + 2 == rowsNumber)
                {
                    for (int c = 0; c < colsNumber; c++)
                    {
                        arrayResultMatrix.setText(r, c, CELL_DOTS);
                    }
                    continue;
                }
                // the last line
                if (r + 1 == rowsNumber)
                {
                    dataRowIdx = xValuesNumber - 1;
                }
            }
            for (int c = 0; c < colsNumber; c++)
            {
                int dataColIdx = c;
                if (yValuesNumber > properties.arrayLength)
                {
                    // before the last column
                    if (c + 2 == colsNumber)
                    {
                        arrayResultMatrix.setText(r, c, CELL_DOTS);
                        continue;
                    }
                    // the last line
                    if (c + 1 == colsNumber)
                    {
                        dataColIdx = yValuesNumber - 1;
                    }
                }
                final CalculatedValue value = arrayResult.getValue2D(dataRowIdx, dataColIdx);
                arrayResultMatrix.setText(r, c, value.getResultDescription(
                        getFormulaList().getDocumentSettings(), CalculatedValue.DEF_RADIX, isFractionResult()));
            }
        }
    }

    public ArrayList<ArrayList<String>> fillResultMatrixArray()
    {
        if (!isArrayResult())
        {
            return null;
        }
        final int xValuesNumber = arrayResult.getDimensions()[0];
        final int rowsNumber = Math.min(xValuesNumber, properties.arrayLength + 1);
        final int yValuesNumber = arrayResult.getDimensions()[1];
        final int colsNumber = Math.min(yValuesNumber, properties.arrayLength + 1);

        ArrayList<ArrayList<String>> res = new ArrayList<>(rowsNumber);
        for (int r = 0; r < rowsNumber; r++)
        {
            int dataRowIdx = r;
            res.add(new ArrayList<>(colsNumber));
            if (xValuesNumber > properties.arrayLength)
            {
                // before the last line
                if (r + 2 == rowsNumber)
                {
                    for (int c = 0; c < colsNumber; c++)
                    {
                        res.get(r).add(CELL_DOTS);
                    }
                    continue;
                }
                // the last line
                if (r + 1 == rowsNumber)
                {
                    dataRowIdx = xValuesNumber - 1;
                }
            }
            for (int c = 0; c < colsNumber; c++)
            {
                int dataColIdx = c;
                if (yValuesNumber > properties.arrayLength)
                {
                    // before the last column
                    if (c + 2 == colsNumber)
                    {
                        res.get(r).add(CELL_DOTS);
                        continue;
                    }
                    // the last line
                    if (c + 1 == colsNumber)
                    {
                        dataColIdx = yValuesNumber - 1;
                    }
                }
                final CalculatedValue value = arrayResult.getValue2D(dataRowIdx, dataColIdx);
                res.get(r).add(value.getResultDescription(
                        getFormulaList().getDocumentSettings(), CalculatedValue.DEF_RADIX, isFractionResult()));
            }
        }
        return res;
    }

    private String fillResultString()
    {
        if (resultType == ResultType.NAN)
        {
            return TermParser.CONST_NAN;
        }

        if (resultType == ResultType.CONSTANT)
        {
            return constantResult.getResultDescription(
                    getFormulaList().getDocumentSettings(), properties.radix, isFractionResult());
        }

        if (isArrayResult())
        {
            final ArrayList<ArrayList<String>> res = fillResultMatrixArray();
            if (res != null)
            {
                return res.toString();
            }
        }
        return "";
    }
}

