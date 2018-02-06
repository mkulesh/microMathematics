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
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.mkulesh.micromath.dialogs.DialogResultDetails;
import com.mkulesh.micromath.dialogs.DialogResultSettings;
import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.formula.TermField.ErrorNotification;
import com.mkulesh.micromath.math.CalculatedValue;
import com.mkulesh.micromath.math.EquationArrayResult;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.ResultProperties;
import com.mkulesh.micromath.properties.ResultPropertiesChangeIf;
import com.mkulesh.micromath.ta.TestSession;
import com.mkulesh.micromath.undo.FormulaState;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomTextView;
import com.mkulesh.micromath.widgets.FocusChangeIf;
import com.mkulesh.micromath.widgets.ResultMatrixLayout;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.util.ArrayList;

import javax.measure.unit.Unit;

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
    private ResultMatrixLayout arrayResultMatrix = null;
    private CustomTextView leftBracket = null, rightBracket = null;

    private final ResultProperties properties = new ResultProperties();

    // undo
    private FormulaState formulaState = null;

    /*********************************************************
     * Constructors
     *********************************************************/

    public FormulaResult(FormulaList formulaList, int id)
    {
        super(formulaList, null, 0);
        setId(id);
        onCreate();
    }

    /*********************************************************
     * GUI constructors to avoid lint warning
     *********************************************************/

    public FormulaResult(Context context)
    {
        super(null, null, 0);
    }

    public FormulaResult(Context context, AttributeSet attrs)
    {
        super(null, null, 0);
    }

    /*********************************************************
     * Re-implementation for methods for FormulaBase superclass
     *********************************************************/

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
            arrayResultMatrix.updateTextColor(R.drawable.formula_term,
                    R.drawable.formula_term_background, R.attr.colorFormulaSelected);
        }
    }

    @Override
    public int getNextFocusId(CustomEditText owner, FocusChangeIf.NextFocusType focusType)
    {
        if (isArrayResult())
        {
            if (owner == leftTerm.getEditText() && focusType == FocusChangeIf.NextFocusType.FOCUS_RIGHT)
            {
                return arrayResultMatrix.getFirstFocusId();
            }
            if (owner == null && focusType == FocusChangeIf.NextFocusType.FOCUS_LEFT)
            {
                return arrayResultMatrix.getLastFocusId();
            }
        }
        return super.getNextFocusId(owner, focusType);
    }

    /*********************************************************
     * Implementation for methods for FocusChangeIf interface
     *********************************************************/

    @Override
    public int onGetNextFocusId(CustomEditText owner, NextFocusType focusType)
    {
        if (owner == null)
        {
            return R.id.main_list_view;
        }
        if (arrayResultMatrix != null)
        {
            int id = arrayResultMatrix.getNextFocusId(owner, focusType);
            if (id == ViewUtils.INVALID_INDEX)
            {
                id = getNextFocusId(constantResultField.getEditText(), focusType);
            }
            return id;
        }
        return getNextFocusId(owner, focusType);
    }

    /*********************************************************
     * Re-implementation for methods for CalculationResult superclass
     *********************************************************/

    @Override
    public void invalidateResult()
    {
        constantResultField.setText("");
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
                ta.setResult(leftTerm.getText(), fillResultString());
            }
            return;
        }
        ArrayList<Equation> linkedIntervals = getAllIntervals();
        if (linkedIntervals.isEmpty())
        {
            resultType = ResultType.CONSTANT;
            constantResult = new CalculatedValue();
            leftTerm.getValue(thread, constantResult);
            final Unit sourceUnit = constantResult.getUnit();
            final Unit targetUnit = leftTerm.getParser().getUnit();
            if (sourceUnit != null && targetUnit != null && !sourceUnit.equals(targetUnit))
            {
                constantResult.convertUnit(sourceUnit, targetUnit);
            }
        }
        else if (linkedIntervals.size() == 1)
        {
            final CalculatedValue[] argValues = new CalculatedValue[1];
            argValues[0] = new CalculatedValue();
            final ArrayList<Double> xValues = linkedIntervals.get(0).getInterval(thread);
            if (xValues != null && xValues.size() > 0)
            {
                final int xLength = xValues.size();
                resultType = ResultType.ARRAY_1D;
                arrayArgument = new EquationArrayResult(xLength);
                arrayResult = new EquationArrayResult(xLength, 1);
                for (int xIndex = 0; xIndex < xLength; xIndex++)
                {
                    final Double x = xValues.get(xIndex);
                    argValues[0].setValue(x);
                    arrayArgument.getValue1D(xIndex).setValue(x);
                    linkedIntervals.get(0).setArgumentValues(argValues);
                    leftTerm.getValue(thread, arrayResult.getValue2D(xIndex, 0));
                }
            }
            else
            {
                resultType = ResultType.NAN;
            }
        }
        else if (linkedIntervals.size() == 2)
        {
            final CalculatedValue[][] argValues = new CalculatedValue[2][1];
            argValues[0][0] = new CalculatedValue();
            final ArrayList<Double> xValues = linkedIntervals.get(0).getInterval(thread);
            argValues[1][0] = new CalculatedValue();
            final ArrayList<Double> yValues = linkedIntervals.get(1).getInterval(thread);
            if (xValues != null && xValues.size() > 0 && yValues != null && yValues.size() > 0)
            {
                final int xLength = xValues.size();
                final int yLength = yValues.size();
                resultType = ResultType.ARRAY_2D;
                arrayResult = new EquationArrayResult(xLength, yLength);
                for (int xIndex = 0; xIndex < xLength; xIndex++)
                {
                    argValues[0][0].setValue(xValues.get(xIndex));
                    linkedIntervals.get(0).setArgumentValues(argValues[0]);
                    for (int yIndex = 0; yIndex < yLength; yIndex++)
                    {
                        argValues[1][0].setValue(yValues.get(yIndex));
                        linkedIntervals.get(1).setArgumentValues(argValues[1]);
                        leftTerm.getValue(thread, arrayResult.getValue2D(xIndex, yIndex));
                    }
                }
            }
            else
            {
                resultType = ResultType.NAN;
            }
        }
        if (!leftTerm.isTerm() && ta != null)
        {
            ta.setResult(leftTerm.getText(), fillResultString());
        }
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
            constantResultField.setText(fillResultString());
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
            arrayResultMatrix.prepare(getFormulaList().getActivity(), this, this);
            arrayResultMatrix.updateTextSize(getFormulaList().getDimen());
            break;
        }
        }
    }

    @Override
    public boolean disableCalculation()
    {
        return properties.disableCalculation;
    }

    /*********************************************************
     * Implementation for methods for FormulaChangeIf interface
     *********************************************************/

    @Override
    public void onDetails(View owner)
    {
        if (enableDetails())
        {
            DialogResultDetails d = new DialogResultDetails(getFormulaList().getActivity(),
                    arrayArgument, arrayResult,
                    getFormulaList().getDocumentSettings());
            d.show();
        }
    }

    @Override
    public void onObjectProperties(View owner)
    {
        if (owner == this)
        {
            properties.showArrayLenght = isArrayResult();
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
        if (properties.disableCalculation)
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

    /*********************************************************
     * Read/write interface
     *********************************************************/

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
            properties.assign((ResultProperties) bundle.getParcelable(STATE_RESULT_PROPERTIES));
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
        if (FormulaList.XML_TERM_TAG.equalsIgnoreCase(serializer.getName()) &&
                key != null && key.equalsIgnoreCase(constantResultField.getTermKey()))
        {
            serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_TEXT, fillResultString());
            return true; // do not write everything for this term more
        }
        return false;
    }

    /*********************************************************
     * FormulaResult-specific methods
     *********************************************************/

    public boolean isResultVisible()
    {
        return !properties.hideResultField;
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
            CustomEditText v = (CustomEditText) layout.findViewById(R.id.formula_result_name);
            leftTerm = addTerm(this, (LinearLayout) layout.findViewById(R.id.result_function_layout), v, this, false);
            leftTerm.bracketsType = TermField.BracketsType.NEVER;
        }
        // create assign character
        {
            resultAssign = (CustomTextView) layout.findViewById(R.id.formula_result_assign);
            resultAssign.prepare(CustomTextView.SymbolType.TEXT, getFormulaList().getActivity(), this);
        }
        // create result term
        {
            CustomEditText v = (CustomEditText) layout.findViewById(R.id.formula_result_value);
            constantResultField = addTerm(this, layout, v, this, true);
            constantResultField.bracketsType = TermField.BracketsType.NEVER;
            constantResultField.isWritable = false;
            arrayResultMatrix = (ResultMatrixLayout) layout.findViewById(R.id.formula_result_table);
        }
        // brackets
        {
            leftBracket = (CustomTextView) layout.findViewById(R.id.formula_result_left_bracket);
            leftBracket.prepare(CustomTextView.SymbolType.LEFT_SQR_BRACKET, getFormulaList().getActivity(), this);
            leftBracket.setText("."); // this text defines view width/height

            rightBracket = (CustomTextView) layout.findViewById(R.id.formula_result_right_bracket);
            rightBracket.prepare(CustomTextView.SymbolType.RIGHT_SQR_BRACKET, getFormulaList().getActivity(), this);
            rightBracket.setText("."); // this text defines view width/height
        }
        updateResultView(false);
    }

    private void updateResultView(boolean checkContent)
    {
        if (checkContent)
        {
            if (isContentValid(ValidationPassType.VALIDATE_SINGLE_FORMULA))
            {
                isContentValid(ValidationPassType.VALIDATE_LINKS);
            }
        }
        showResult();
    }

    public void clearResult()
    {
        resultType = ResultType.NONE;
        constantResult = null;
        arrayArgument = null;
        arrayResult = null;
    }

    private void fillResultMatrix()
    {
        if (!isArrayResult())
        {
            return;
        }
        final int xValuesNumber = arrayResult.getDimensions()[0];
        final int rowsNumber = Math.min(xValuesNumber, properties.arrayLength + 1);
        final int yValuesNumber = arrayResult.getDimensions()[1];
        final int colsNumber = Math.min(yValuesNumber, properties.arrayLength + 1);

        arrayResultMatrix.resize(rowsNumber, colsNumber, R.layout.formula_result_cell);
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
                String resultStr = arrayResult.getValue2D(dataRowIdx, dataColIdx).getResultDescription(
                        getFormulaList().getDocumentSettings());
                arrayResultMatrix.setText(r, c, resultStr);
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

        ArrayList<ArrayList<String>> res = new ArrayList<ArrayList<String>>(rowsNumber);
        for (int r = 0; r < rowsNumber; r++)
        {
            int dataRowIdx = r;
            res.add(new ArrayList<String>(colsNumber));
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
                res.get(r).add(arrayResult.getValue2D(dataRowIdx, dataColIdx).getResultDescription(
                        getFormulaList().getDocumentSettings()));
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
            return constantResult.getResultDescription(getFormulaList().getDocumentSettings());
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

