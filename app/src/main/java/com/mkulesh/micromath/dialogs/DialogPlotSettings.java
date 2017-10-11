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
package com.mkulesh.micromath.dialogs;

import android.app.Activity;
import android.view.View;
import android.widget.RadioButton;

import com.mkulesh.micromath.R;
import com.mkulesh.micromath.properties.PlotProperties;
import com.mkulesh.micromath.properties.PlotPropertiesChangeIf;
import com.mkulesh.micromath.widgets.HorizontalNumberPicker;

public class DialogPlotSettings extends DialogBase
{
    private final PlotPropertiesChangeIf changeIf;
    private final PlotProperties parameters;
    private final HorizontalNumberPicker pickerWidth, pickerHeight;

    public DialogPlotSettings(Activity context, PlotPropertiesChangeIf changeIf, PlotProperties parameters)
    {
        super(context, R.layout.dialog_plot_settings, R.string.dialog_plot_settings_title);
        this.parameters = parameters;

        pickerWidth = (HorizontalNumberPicker) findViewById(R.id.dialog_picker_width);
        pickerWidth.setValue(parameters.width);
        pickerWidth.minValue = 0;
        pickerHeight = (HorizontalNumberPicker) findViewById(R.id.dialog_picker_height);
        pickerHeight.setValue(parameters.height);
        pickerHeight.minValue = 0;

        switch (parameters.axesStyle)
        {
        case BOXED:
            ((RadioButton) findViewById(R.id.dialog_button_axes_boxed)).setChecked(true);
            break;
        case CROSSED:
            ((RadioButton) findViewById(R.id.dialog_button_axes_crossed)).setChecked(true);
            break;
        case NONE:
            ((RadioButton) findViewById(R.id.dialog_button_axes_none)).setChecked(true);
            break;
        }

        this.changeIf = changeIf;
    }

    @Override
    public void onClick(View v)
    {
        boolean isChanged = false;
        if (v.getId() == R.id.dialog_button_ok && changeIf != null)
        {
            if (parameters.width != pickerWidth.getValue())
            {
                isChanged = true;
                parameters.width = pickerWidth.getValue();
            }

            if (parameters.height != pickerHeight.getValue())
            {
                isChanged = true;
                parameters.height = pickerHeight.getValue();
            }

            PlotProperties.AxesStyle axesStyle;
            if (((RadioButton) findViewById(R.id.dialog_button_axes_boxed)).isChecked())
            {
                axesStyle = PlotProperties.AxesStyle.BOXED;
            }
            else if (((RadioButton) findViewById(R.id.dialog_button_axes_crossed)).isChecked())
            {
                axesStyle = PlotProperties.AxesStyle.CROSSED;
            }
            else
            {
                axesStyle = PlotProperties.AxesStyle.NONE;
            }
            if (parameters.axesStyle != axesStyle)
            {
                isChanged = true;
                parameters.axesStyle = axesStyle;
            }
        }
        changeIf.onPlotPropertiesChange(isChanged);
        closeDialog();
    }
}
