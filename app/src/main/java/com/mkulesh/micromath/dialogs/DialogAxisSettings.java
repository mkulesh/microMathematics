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
import android.widget.CheckBox;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.mkulesh.micromath.math.AxisTypeConverter;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.AxisProperties;
import com.mkulesh.micromath.properties.AxisPropertiesChangeIf;
import com.mkulesh.micromath.widgets.HorizontalNumberPicker;

public class DialogAxisSettings extends DialogBase
{
    private final AxisPropertiesChangeIf changeIf;
    private final AxisProperties parameters;
    private final HorizontalNumberPicker xLabelsPicker, yLabelsPicker;
    private final ColorPicker gridLineColor;
    private final CheckBox xTypeCheckBox, yTypeCheckBox;

    public DialogAxisSettings(Activity context, AxisPropertiesChangeIf changeIf, AxisProperties parameters)
    {
        super(context, R.layout.dialog_axis_settings, R.string.dialog_axis_settings_title);
        this.parameters = parameters;

        xLabelsPicker = findViewById(R.id.dialog_xlabels_number);
        xLabelsPicker.setValue(parameters.xLabelsNumber);
        xLabelsPicker.minValue = 0;
        yLabelsPicker = findViewById(R.id.dialog_ylabels_number);
        yLabelsPicker.setValue(parameters.yLabelsNumber);
        yLabelsPicker.minValue = 0;

        gridLineColor = PrepareColorPicker(parameters.gridLineColor);

        xTypeCheckBox = findViewById(R.id.dialog_xtype);
        yTypeCheckBox = findViewById(R.id.dialog_ytype);
        if (changeIf.getAxisType() == AxisPropertiesChangeIf.AxisType.EXTENDED)
        {
            xTypeCheckBox.setVisibility(View.VISIBLE);
            xTypeCheckBox.setChecked(parameters.xType == AxisTypeConverter.Type.LOG10);
            yTypeCheckBox.setVisibility(View.VISIBLE);
            yTypeCheckBox.setChecked(parameters.yType == AxisTypeConverter.Type.LOG10);
        }
        else
        {
            xTypeCheckBox.setVisibility(View.GONE);
            yTypeCheckBox.setVisibility(View.GONE);
        }

        this.changeIf = changeIf;
    }

    @Override
    public void onClick(View v)
    {
        boolean isChanged = false;
        if (v.getId() == R.id.dialog_button_ok && changeIf != null)
        {
            if (parameters.gridLineColor != gridLineColor.getColor())
            {
                isChanged = true;
                parameters.gridLineColor = gridLineColor.getColor();
            }

            if (parameters.xLabelsNumber != xLabelsPicker.getValue())
            {
                isChanged = true;
                parameters.xLabelsNumber = xLabelsPicker.getValue();
            }

            if (parameters.yLabelsNumber != yLabelsPicker.getValue())
            {
                isChanged = true;
                parameters.yLabelsNumber = yLabelsPicker.getValue();
            }

            if (changeIf.getAxisType() == AxisPropertiesChangeIf.AxisType.EXTENDED)
            {
                // X-axis type
                {
                    final AxisTypeConverter.Type type = xTypeCheckBox.isChecked() ?
                            AxisTypeConverter.Type.LOG10 : AxisTypeConverter.Type.LINEAR;
                    if (parameters.xType != type)
                    {
                        isChanged = true;
                        parameters.xType = type;
                    }
                }

                // Y-axis type
                {
                    final AxisTypeConverter.Type type = yTypeCheckBox.isChecked() ?
                            AxisTypeConverter.Type.LOG10 : AxisTypeConverter.Type.LINEAR;
                    if (parameters.yType != type)
                    {
                        isChanged = true;
                        parameters.yType = type;
                    }
                }
            }
            changeIf.onAxisPropertiesChange(isChanged);
        }
        closeDialog();
    }
}
