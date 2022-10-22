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
package com.mkulesh.micromath.dialogs;

import android.app.Activity;
import android.view.View;
import android.widget.RadioButton;

import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.ResultProperties;
import com.mkulesh.micromath.properties.ResultPropertiesChangeIf;
import com.mkulesh.micromath.widgets.HorizontalNumberPicker;

import androidx.appcompat.widget.AppCompatEditText;

public class DialogResultSettings extends DialogBase
{
    private final ResultPropertiesChangeIf changeIf;
    private final ResultProperties properties;
    private final AppCompatEditText units;
    private final HorizontalNumberPicker radixPicker;
    private final HorizontalNumberPicker arrayLengthPicker;

    public DialogResultSettings(Activity context, ResultPropertiesChangeIf changeIf, ResultProperties properties)
    {
        super(context, R.layout.dialog_result_settings, R.string.dialog_result_title);

        this.changeIf = changeIf;
        this.properties = properties;

        units = findViewById(R.id.dialog_result_units_field);
        units.setText(properties.units);

        radixPicker = findViewById(R.id.dialog_result_radix);
        radixPicker.setVisibility(properties.showArrayLength ? View.GONE : View.VISIBLE);
        radixPicker.setValue(properties.radix);
        radixPicker.minValue = Character.MIN_RADIX;
        radixPicker.maxValue = Character.MAX_RADIX;

        final int[] radioIds = new int[]{
                R.id.dialog_result_field_hide,
                R.id.dialog_result_field_skip,
                R.id.dialog_result_field_real,
                R.id.dialog_result_field_fraction
        };
        for (int rId : radioIds)
        {
            final RadioButton r = findViewById(rId);
            r.setOnClickListener(this);
            if (radioIds[properties.resultFieldType.ordinal()] == rId)
            {
                r.setChecked(true);
                onClick(r);
            }
        }

        arrayLengthPicker = findViewById(R.id.dialog_result_array_length_picker);
        arrayLengthPicker.setVisibility(properties.showArrayLength ? View.VISIBLE : View.GONE);
        arrayLengthPicker.setValue(properties.arrayLength);
        arrayLengthPicker.minValue = 2;
    }

    @Override
    public void onClick(View v)
    {
        boolean isChanged = false;
        if (v.getId() == R.id.dialog_result_field_hide ||
                v.getId() == R.id.dialog_result_field_skip ||
                v.getId() == R.id.dialog_result_field_real)
        {
            radixPicker.setEnabled(true);
            return;
        }
        if (v.getId() == R.id.dialog_result_field_fraction)
        {
            radixPicker.setValue(10);
            radixPicker.setEnabled(false);
            return;
        }
        if (v.getId() == R.id.dialog_button_ok)
        {
            if (changeIf != null && properties != null)
            {
                if (units.getText() != null && !properties.units.equals(units.getText().toString()))
                {
                    properties.units = units.getText().toString();
                    isChanged = true;
                }

                ResultProperties.ResultFieldType resultFieldType;
                if (((RadioButton) findViewById(R.id.dialog_result_field_fraction)).isChecked())
                {
                    resultFieldType = ResultProperties.ResultFieldType.FRACTION;
                }
                else if (((RadioButton) findViewById(R.id.dialog_result_field_real)).isChecked())
                {
                    resultFieldType = ResultProperties.ResultFieldType.REAL;
                }
                else if (((RadioButton) findViewById(R.id.dialog_result_field_skip)).isChecked())
                {
                    resultFieldType = ResultProperties.ResultFieldType.SKIP;
                }
                else
                {
                    resultFieldType = ResultProperties.ResultFieldType.HIDE;
                }
                if (properties.resultFieldType != resultFieldType)
                {
                    isChanged = true;
                    properties.resultFieldType = resultFieldType;
                }

                if (properties.arrayLength != arrayLengthPicker.getValue())
                {
                    properties.arrayLength = arrayLengthPicker.getValue();
                    isChanged = true;
                }
                if (properties.radix != radixPicker.getValue())
                {
                    properties.radix = radixPicker.getValue();
                    isChanged = true;
                }
            }
        }
        if (changeIf != null)
        {
            changeIf.onResultPropertiesChange(isChanged);
        }
        closeDialog();
    }

}
