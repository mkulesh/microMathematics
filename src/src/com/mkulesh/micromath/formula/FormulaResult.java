/*******************************************************************************
 * micro Mathematics - Extended visual calculator
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

import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

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
import com.mkulesh.micromath.math.ArgumentValueItem;
import com.mkulesh.micromath.math.CalculatedValue;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.ResultProperties;
import com.mkulesh.micromath.properties.ResultPropertiesChangeIf;
import com.mkulesh.micromath.ta.TestSession;
import com.mkulesh.micromath.undo.FormulaState;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomTextView;

public class FormulaResult extends CalculationResult implements ResultPropertiesChangeIf
{
    private static final String STATE_RESULT_PROPERTIES = "result_properties";

    private TermField leftTerm = null;
    private TermField rightTerm = null;
    private String result = null;
    private ArrayList<ArgumentValueItem> calculatedItems = null;

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
                else if (getAllIntervals().size() > 1)
                {
                    isValid = false;
                    errorMsg = getContext().getResources().getString(R.string.error_ensure_single_interval);
                }
                leftTerm.setError(errorMsg, ErrorNotification.LAYOUT_BORDER, null);
            }
            break;
        }

        if (!isValid)
        {
            invalidateResult();
        }
        return disableCalculation() ? true : isValid;
    }

    @Override
    public void undo(FormulaState state)
    {
        super.undo(state);
        updateResultView(true);
        ViewUtils.invalidateLayout(layout, layout);
    }

    /*********************************************************
     * Re-implementation for methods for CalculationResult superclass
     *********************************************************/

    @Override
    public void invalidateResult()
    {
        result = null;
        calculatedItems = null;
        showResult();
    }

    @Override
    public void calculate(CalculaterTask thread) throws CancelException
    {
        if (disableCalculation())
        {
            return;
        }
        ArrayList<Equation> linkedIntervals = getAllIntervals();
        final CalculatedValue calcVal = new CalculatedValue();
        if (linkedIntervals.isEmpty())
        {
            leftTerm.getValue(thread, calcVal);
            result = calcVal.getResultDescription(getFormulaList().getDocumentSettings());
        }
        else if (linkedIntervals.size() == 1)
        {
            final CalculatedValue[] argValues = new CalculatedValue[linkedIntervals.size()];
            argValues[0] = new CalculatedValue();
            Equation linkedInterval = linkedIntervals.get(0);
            ArrayList<Double> values = linkedInterval.getInterval(thread);
            if (values != null && values.size() > 0)
            {
                // calculate values
                calculatedItems = new ArrayList<ArgumentValueItem>(values.size());
                for (Double v : values)
                {
                    argValues[0].setValue(v);
                    final ArgumentValueItem item = new ArgumentValueItem();
                    // x value
                    item.argument.assign(argValues[0]);
                    linkedInterval.setArgumentValues(argValues);
                    // y value
                    leftTerm.getValue(thread, item.value);
                    calculatedItems.add(item);
                }
                // make string representation
                result = makeResultArray();
            }
            else
            {
                result = TermParser.CONST_NAN;
            }
        }
        if (!leftTerm.isTerm())
        {
            final TestSession ta = getFormulaList().getTaSession();
            if (ta != null)
            {
                ta.setResult(leftTerm.getText(), result);
            }
        }
    }

    @Override
    public void showResult()
    {
        if (result != null)
        {
            rightTerm.setText(result);
        }
        else
        {
            rightTerm.setText("");
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
        if (calculatedItems != null)
        {
            DialogResultDetails d = new DialogResultDetails(getFormulaList().getActivity(), calculatedItems,
                    getFormulaList().getDocumentSettings());
            d.show();
        }
    }

    @Override
    public void onObjectProperties(View owner)
    {
        if (owner == this)
        {
            properties.showArrayLenght = calculatedItems != null;
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
            invalidateResult();
        }
        updateResultView(true);
        ViewUtils.invalidateLayout(layout, layout);
    }

    @Override
    public boolean enableDetails()
    {
        return calculatedItems != null;
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
        return false;
    }

    /*********************************************************
     * FormulaResult-specific methods
     *********************************************************/

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
            CustomTextView v = (CustomTextView) layout.findViewById(R.id.formula_result_assign);
            v.prepare(CustomTextView.SymbolType.TEXT, getFormulaList().getActivity(), this);
            v.setText(getContext().getResources().getString(R.string.formula_result_definition));
        }
        // create result term
        {
            CustomEditText v = (CustomEditText) layout.findViewById(R.id.formula_result_value);
            rightTerm = addTerm(this, layout, v, this, true);
            rightTerm.bracketsType = TermField.BracketsType.NEVER;
            rightTerm.isWritable = false;
        }
        updateResultView(false);
    }

    private void updateResultView(boolean checkContent)
    {
        int visibility = properties.hideResultField ? View.GONE : View.VISIBLE;
        if (rightTerm != null)
        {
            rightTerm.getEditText().setVisibility(visibility);
        }
        CustomTextView v = (CustomTextView) layout.findViewById(R.id.formula_result_assign);
        if (v != null)
        {
            v.setVisibility(visibility);
        }
        if (checkContent)
        {
            if (isContentValid(ValidationPassType.VALIDATE_SINGLE_FORMULA))
            {
                isContentValid(ValidationPassType.VALIDATE_LINKS);
            }
        }
        if (calculatedItems != null && !disableCalculation())
        {
            result = makeResultArray();
            showResult();
        }
    }

    private String makeResultArray()
    {
        String r = "[";
        final int nrLogged = properties.arrayLength - 1;
        final int length = calculatedItems.size();
        for (int i = 0; i < length; i++)
        {
            if (i < nrLogged && i < length - 1)
            {
                r += calculatedItems.get(i).value.getResultDescription(getFormulaList().getDocumentSettings());
                r += ", ";
            }
            else if (i == nrLogged && i < length - 1)
            {
                r += "..., ";
            }
            else if (i == length - 1)
            {
                r += calculatedItems.get(i).value.getResultDescription(getFormulaList().getDocumentSettings());
            }
        }
        r += "]";
        return r;
    }

}
