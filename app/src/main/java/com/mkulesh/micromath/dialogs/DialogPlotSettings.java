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
import android.graphics.Color;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import com.larswerkman.holocolorpicker.OpacityBar;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.PlotProperties;
import com.mkulesh.micromath.properties.PlotPropertiesChangeIf;
import com.mkulesh.micromath.widgets.HorizontalNumberPicker;

public class DialogPlotSettings extends DialogBase
{
    private final PlotPropertiesChangeIf changeIf;
    private final PlotProperties parameters;
    private HorizontalNumberPicker pickerWidth = null, pickerHeight = null, pickerRotation = null,
            pickerElevation = null;
    private RadioButton rContour = null, rSurface = null;
    private OpacityBar pickerMeshOpacity = null;
    private CheckBox cbMeshLines = null, cbMeshFill = null;

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

        ((LinearLayout) findViewById(R.id.dialog_dimension_layout))
                .setVisibility((changeIf.getDimension() == PlotPropertiesChangeIf.Dimension.TWO_D) ? View.VISIBLE
                        : View.GONE);
        if (changeIf.getDimension() == PlotPropertiesChangeIf.Dimension.TWO_D)
        {
            rContour = (RadioButton) findViewById(R.id.dialog_button_contour);
            rContour.setOnClickListener(this);
            rSurface = (RadioButton) findViewById(R.id.dialog_button_surface);
            rSurface.setOnClickListener(this);
            switch (parameters.twoDPlotStyle)
            {
            case CONTOUR:
                rContour.setChecked(true);
                onClick(rContour);
                break;
            case SURFACE:
                rSurface.setChecked(true);
                onClick(rSurface);
                break;
            }

            cbMeshLines = (CheckBox) findViewById(R.id.dialog_checkbox_mesh_lines);
            cbMeshLines.setChecked(parameters.meshLines);
            cbMeshFill = (CheckBox) findViewById(R.id.dialog_checkbox_mesh_fill);
            cbMeshFill.setChecked(parameters.meshFill);
            pickerMeshOpacity = (OpacityBar) findViewById(R.id.dialog_colorpicker_mesh_opacity);
            pickerMeshOpacity.setColor(Color.BLACK);
            pickerMeshOpacity.setOpacity(parameters.meshOpacity);
            pickerRotation = (HorizontalNumberPicker) findViewById(R.id.dialog_picker_rotation);
            pickerRotation.setValue(parameters.rotation);
            pickerRotation.minValue = 0;
            pickerRotation.maxValue = 360;
            pickerElevation = (HorizontalNumberPicker) findViewById(R.id.dialog_picker_elevation);
            pickerElevation.setValue(parameters.elevation);
            pickerElevation.minValue = 0;
            pickerElevation.maxValue = 360;
        }

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
        if (v.getId() == R.id.dialog_button_contour)
        {
            final RadioButton rCrossed = (RadioButton) findViewById(R.id.dialog_button_axes_crossed);
            rCrossed.setVisibility(View.VISIBLE);
            ((LinearLayout) findViewById(R.id.dialog_surface_layout)).setVisibility(View.GONE);
            return;
        }
        else if (v.getId() == R.id.dialog_button_surface)
        {
            final RadioButton rCrossed = (RadioButton) findViewById(R.id.dialog_button_axes_crossed);
            if (rCrossed.isChecked())
            {
                ((RadioButton) findViewById(R.id.dialog_button_axes_boxed)).setChecked(true);
            }
            rCrossed.setVisibility(View.GONE);
            ((LinearLayout) findViewById(R.id.dialog_surface_layout)).setVisibility(View.VISIBLE);
            return;
        }
        else if (v.getId() == R.id.dialog_button_ok && changeIf != null)
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

            if (changeIf.getDimension() == PlotPropertiesChangeIf.Dimension.TWO_D)
            {
                PlotProperties.TwoDPlotStyle twoDPlotStyle;
                if (((RadioButton) findViewById(R.id.dialog_button_contour)).isChecked())
                {
                    twoDPlotStyle = PlotProperties.TwoDPlotStyle.CONTOUR;
                }
                else
                {
                    twoDPlotStyle = PlotProperties.TwoDPlotStyle.SURFACE;
                }
                if (parameters.twoDPlotStyle != twoDPlotStyle)
                {
                    isChanged = true;
                    parameters.twoDPlotStyle = twoDPlotStyle;
                }

                if (twoDPlotStyle == PlotProperties.TwoDPlotStyle.SURFACE)
                {
                    if (parameters.meshLines != cbMeshLines.isChecked())
                    {
                        isChanged = true;
                        parameters.meshLines = cbMeshLines.isChecked();
                    }
                    if (parameters.meshFill != cbMeshFill.isChecked())
                    {
                        isChanged = true;
                        parameters.meshFill = cbMeshFill.isChecked();
                    }
                    if (parameters.meshOpacity != pickerMeshOpacity.getOpacity())
                    {
                        isChanged = true;
                        parameters.meshOpacity = pickerMeshOpacity.getOpacity();
                    }
                    if (parameters.rotation != pickerRotation.getValue())
                    {
                        isChanged = true;
                        parameters.rotation = pickerRotation.getValue();
                    }
                    if (parameters.elevation != pickerElevation.getValue())
                    {
                        isChanged = true;
                        parameters.elevation = pickerElevation.getValue();
                    }
                }
            }
        }
        changeIf.onPlotPropertiesChange(isChanged);
        closeDialog();
    }
}
