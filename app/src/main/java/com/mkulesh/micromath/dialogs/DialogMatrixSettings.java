/*
 * microMathematics - Extended Visual Calculator
 * Copyright (C) 2014-2021 by Mikhail Kulesh
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

import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.MatrixProperties;
import com.mkulesh.micromath.properties.MatrixPropertiesChangeIf;
import com.mkulesh.micromath.widgets.HorizontalNumberPicker;

import androidx.annotation.NonNull;

public class DialogMatrixSettings extends DialogBase
{
    @NonNull
    private final MatrixPropertiesChangeIf changeIf;
    private final MatrixProperties properties;
    private final HorizontalNumberPicker rowsPicker;
    private final HorizontalNumberPicker colsPicker;

    public DialogMatrixSettings(Activity context, @NonNull MatrixPropertiesChangeIf changeIf, MatrixProperties properties)
    {
        super(context, R.layout.dialog_matrix_settings, R.string.dialog_matrix_title);

        this.changeIf = changeIf;
        this.properties = properties;

        rowsPicker = findViewById(R.id.dialog_matrix_rows);
        rowsPicker.setValue(properties.rows);
        rowsPicker.minValue = 1;

        colsPicker = findViewById(R.id.dialog_matrix_cols);
        colsPicker.setValue(properties.cols);
        colsPicker.minValue = 1;
    }

    @Override
    public void onClick(View v)
    {
        boolean isChanged = false;
        if (v.getId() == R.id.dialog_button_ok)
        {
            if (properties != null)
            {
                if (properties.rows != rowsPicker.getValue())
                {
                    properties.rows = rowsPicker.getValue();
                    isChanged = true;
                }
                if (properties.cols != colsPicker.getValue())
                {
                    properties.cols = colsPicker.getValue();
                    isChanged = true;
                }
            }
            changeIf.onMatrixPropertiesChange(isChanged);
        }
        closeDialog();
    }
}
