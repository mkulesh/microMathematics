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
package com.mkulesh.micromath.dialogs;

import android.app.Activity;
import android.view.View;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.ValueBar;
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

    public DialogAxisSettings(Activity context, AxisPropertiesChangeIf changeIf, AxisProperties parameters)
    {
        super(context, R.layout.dialog_axis_settings, R.string.dialog_axis_settings_title);
        this.parameters = parameters;

        xLabelsPicker = (HorizontalNumberPicker) findViewById(R.id.dialog_xlabels_number);
        xLabelsPicker.setValue(parameters.xLabelsNumber);
        xLabelsPicker.minValue = 0;
        yLabelsPicker = (HorizontalNumberPicker) findViewById(R.id.dialog_ylabels_number);
        yLabelsPicker.setValue(parameters.yLabelsNumber);
        yLabelsPicker.minValue = 0;

        gridLineColor = (ColorPicker) findViewById(R.id.dialog_color_picker);
        gridLineColor.addValueBar((ValueBar) findViewById(R.id.dialog_color_valuebar));
        gridLineColor.addOpacityBar((OpacityBar) findViewById(R.id.dialog_color_opacity));
        gridLineColor.setColor(parameters.gridLineColor);
        gridLineColor.setOldCenterColor(parameters.gridLineColor);

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
        }
        changeIf.onAxisPropertiesChange(isChanged);
        closeDialog();
    }
}
