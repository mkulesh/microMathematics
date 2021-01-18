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
import android.widget.RadioButton;

import com.mkulesh.micromath.plots.views.ColorMapView;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.ColorMapProperties;
import com.mkulesh.micromath.properties.ColorMapProperties.ColorMap;
import com.mkulesh.micromath.properties.ColorMapPropertiesChangeIf;
import com.mkulesh.micromath.widgets.HorizontalNumberPicker;

public class DialogColorMapSettings extends DialogBase
{
    private final ColorMapPropertiesChangeIf changeIf;
    private final ColorMapProperties parameters;
    private final HorizontalNumberPicker zLabelsNumber;
    private final RadioButton[] rButtons = new RadioButton[ColorMapProperties.ColorMap.values().length];
    private final ColorMapView[] cmBars = new ColorMapView[ColorMapProperties.ColorMap.values().length];

    public DialogColorMapSettings(Activity context, ColorMapPropertiesChangeIf changeIf, ColorMapProperties parameters)
    {
        super(context, R.layout.dialog_colormap_settings, R.string.dialog_colormap_settings_title);
        this.parameters = parameters;

        zLabelsNumber = findViewById(R.id.dialog_zlabels_number);
        zLabelsNumber.setValue(parameters.zLabelsNumber);
        zLabelsNumber.minValue = 0;

        rButtons[ColorMap.COOL.ordinal()] = findViewById(R.id.dialog_rbutton_cool);
        rButtons[ColorMap.FIRE.ordinal()] = findViewById(R.id.dialog_rbutton_fire);
        rButtons[ColorMap.COLDHOT.ordinal()] = findViewById(R.id.dialog_rbutton_coldhot);
        rButtons[ColorMap.RAINBOW.ordinal()] = findViewById(R.id.dialog_rbutton_rainbow);
        rButtons[ColorMap.EARTHSKY.ordinal()] = findViewById(R.id.dialog_rbutton_earthsky);
        rButtons[ColorMap.GREENBLUE.ordinal()] = findViewById(R.id.dialog_rbutton_greenblue);
        rButtons[ColorMap.GRAYSCALE.ordinal()] = findViewById(R.id.dialog_rbutton_grayscale);
        cmBars[ColorMap.COOL.ordinal()] = findViewById(R.id.dialog_cmbar_cool);
        cmBars[ColorMap.FIRE.ordinal()] = findViewById(R.id.dialog_cmbar_fire);
        cmBars[ColorMap.COLDHOT.ordinal()] = findViewById(R.id.dialog_cmbar_coldhot);
        cmBars[ColorMap.RAINBOW.ordinal()] = findViewById(R.id.dialog_cmbar_rainbow);
        cmBars[ColorMap.EARTHSKY.ordinal()] = findViewById(R.id.dialog_cmbar_earthsky);
        cmBars[ColorMap.GREENBLUE.ordinal()] = findViewById(R.id.dialog_cmbar_greenblue);
        cmBars[ColorMap.GRAYSCALE.ordinal()] = findViewById(R.id.dialog_cmbar_grayscale);
        for (int i = 0; i < rButtons.length; i++)
        {
            rButtons[i].setChecked(ColorMap.values()[i] == parameters.colorMap);
            rButtons[i].setOnClickListener(this);
            cmBars[i].setOnClickListener(this);
        }

        this.changeIf = changeIf;
    }

    @Override
    public void onClick(View v)
    {
        boolean isChanged = false;
        if (v instanceof RadioButton)
        {
            for (RadioButton rButton : rButtons)
            {
                rButton.setChecked(rButton == v);
            }
            return;
        }
        else if (v instanceof ColorMapView)
        {
            for (int i = 0; i < cmBars.length; i++)
            {
                rButtons[i].setChecked(cmBars[i] == v);
            }
            return;
        }
        else if (v.getId() == R.id.dialog_button_ok && changeIf != null)
        {
            ColorMap colorMap = ColorMap.GRAYSCALE;
            for (int i = 0; i < rButtons.length; i++)
            {
                if (rButtons[i].isChecked())
                {
                    colorMap = ColorMap.values()[i];
                    break;
                }
            }
            if (parameters.colorMap != colorMap)
            {
                isChanged = true;
                parameters.colorMap = colorMap;
            }
            if (parameters.zLabelsNumber != zLabelsNumber.getValue())
            {
                isChanged = true;
                parameters.zLabelsNumber = zLabelsNumber.getValue();
            }
        }
        changeIf.onColorMapPropertiesChange(isChanged);
        closeDialog();
    }
}
