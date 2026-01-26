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
import android.widget.ImageButton;
import android.widget.RadioButton;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.LineProperties;
import com.mkulesh.micromath.properties.LinePropertiesChangeIf;
import com.mkulesh.micromath.utils.CompatUtils;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CustomTextView;
import com.mkulesh.micromath.widgets.HorizontalNumberPicker;

import java.util.HashMap;
import java.util.Map;

public class DialogLineSettings extends DialogBase
{
    private final LinePropertiesChangeIf changeIf;
    private final LineProperties parameters;
    private final HorizontalNumberPicker widthPicker;
    private final ColorPicker colorPicker;
    private final RadioButton[] radioButtons;
    private final CustomTextView[] radioLines;

    private final CheckBox pointShapesBox;
    private final HorizontalNumberPicker shapeSizePicker;
    private final HashMap<LineProperties.ShapeType, ImageButton> shapeTypeButtons = new HashMap<>();

    public DialogLineSettings(Activity context, LinePropertiesChangeIf changeIf, LineProperties parameters)
    {
        super(context, R.layout.dialog_line_settings, R.string.dialog_line_settings_title);
        this.parameters = parameters;

        widthPicker = findViewById(R.id.dialog_number_picker);
        widthPicker.setValue(parameters.width);
        widthPicker.minValue = 1;

        colorPicker = PrepareColorPicker(parameters.color);

        radioButtons = new RadioButton[4];
        radioButtons[0] = findViewById(R.id.dialog_button_line_style_solid);
        radioButtons[1] = findViewById(R.id.dialog_button_line_style_dotted);
        radioButtons[2] = findViewById(R.id.dialog_button_line_style_dashed);
        radioButtons[3] = findViewById(R.id.dialog_button_line_style_dash_dot);

        radioLines = new CustomTextView[radioButtons.length];
        radioLines[0] = findViewById(R.id.dialog_marker_line_style_solid);
        radioLines[1] = findViewById(R.id.dialog_marker_line_style_dotted);
        radioLines[2] = findViewById(R.id.dialog_marker_line_style_dashed);
        radioLines[3] = findViewById(R.id.dialog_marker_line_style_dash_dot);

        LineProperties l = new LineProperties();
        l.color = CompatUtils.getThemeColorAttr(getContext(), R.attr.colorDialogContent);
        l.width = ViewUtils.dpToPx(getContext().getResources().getDisplayMetrics(), 2);

        for (int i = 0; i < radioButtons.length; i++)
        {
            l.lineStyle = LineProperties.LineStyle.values()[i];
            l.preparePaint();
            radioLines[i].setExternalPaint(l.getPaint());
            radioLines[i].setOnClickListener(this);
            radioButtons[i].setChecked(l.lineStyle == parameters.lineStyle);
            radioButtons[i].setOnClickListener(this);
        }

        pointShapesBox = findViewById(R.id.dialog_plot_point_shapes);
        pointShapesBox.setOnClickListener(this);
        pointShapesBox.setChecked(parameters.shapeType != LineProperties.ShapeType.NONE);

        shapeSizePicker = findViewById(R.id.dialog_plot_point_shape_size);
        shapeSizePicker.setValue(parameters.shapeSize);
        shapeSizePicker.minValue = 100;
        shapeSizePicker.setEnabled(pointShapesBox.isChecked());

        shapeTypeButtons
                .put(LineProperties.ShapeType.SQUARE, (ImageButton) findViewById(R.id.dialog_plot_point_square));
        shapeTypeButtons
                .put(LineProperties.ShapeType.CIRCLE, (ImageButton) findViewById(R.id.dialog_plot_point_circle));
        shapeTypeButtons.put(LineProperties.ShapeType.DIAMOND,
                (ImageButton) findViewById(R.id.dialog_plot_point_diamond));
        shapeTypeButtons.put(LineProperties.ShapeType.CROSS, (ImageButton) findViewById(R.id.dialog_plot_point_cross));
        for (Map.Entry<LineProperties.ShapeType, ImageButton> e : shapeTypeButtons.entrySet())
        {
            ImageButton b = e.getValue();
            b.setOnClickListener(this);
            CompatUtils.setTooltip(b, getContext());
            setButtonEnabled(b, pointShapesBox.isChecked());
            if (b.isEnabled())
            {
                setButtonSelected(b, e.getKey() == parameters.shapeType);
            }
        }

        this.changeIf = changeIf;
    }

    @Override
    public void onClick(View v)
    {
        boolean isChanged = false;
        int radioIdx = -1;
        for (int i = 0; i < radioButtons.length; i++)
        {
            if (v == radioLines[i] || v == radioButtons[i])
            {
                radioIdx = i;
                break;
            }
        }
        if (radioIdx >= 0)
        {
            for (int i = 0; i < radioButtons.length; i++)
            {
                radioButtons[i].setChecked(i == radioIdx);
            }
            return;
        }
        if (v.getId() == R.id.dialog_plot_point_shapes)
        {
            if (shapeSizePicker != null)
            {
                shapeSizePicker.setEnabled(pointShapesBox.isChecked());
                for (ImageButton b : shapeTypeButtons.values())
                {
                    setButtonEnabled(b, pointShapesBox.isChecked());
                }
            }
            return;
        }
        if (shapeTypeButtons.containsValue(v))
        {
            for (ImageButton b : shapeTypeButtons.values())
            {
                setButtonSelected(b, v == b);
            }
            return;
        }
        if (v.getId() == R.id.dialog_button_ok && changeIf != null)
        {
            if (parameters.width != widthPicker.getValue())
            {
                isChanged = true;
                parameters.width = widthPicker.getValue();
            }

            if (parameters.color != colorPicker.getColor())
            {
                isChanged = true;
                parameters.color = colorPicker.getColor();
            }

            LineProperties.LineStyle lineStyle = null;
            for (int i = 0; i < radioButtons.length; i++)
            {
                if (radioButtons[i].isChecked())
                {
                    lineStyle = LineProperties.LineStyle.values()[i];
                    break;
                }
            }
            if (parameters.lineStyle != lineStyle && lineStyle != null)
            {
                isChanged = true;
                parameters.lineStyle = lineStyle;
            }

            LineProperties.ShapeType shapeType = LineProperties.ShapeType.NONE;
            if (pointShapesBox.isChecked())
            {
                for (Map.Entry<LineProperties.ShapeType, ImageButton> e : shapeTypeButtons.entrySet())
                {
                    if (e.getValue().isSelected())
                    {
                        shapeType = e.getKey();
                        break;
                    }
                }
            }
            if (parameters.shapeType != shapeType)
            {
                isChanged = true;
                parameters.shapeType = shapeType;
            }

            if (parameters.shapeSize != shapeSizePicker.getValue())
            {
                isChanged = true;
                parameters.shapeSize = shapeSizePicker.getValue();
            }

        }
        changeIf.onLinePropertiesChange(isChanged);
        closeDialog();
    }
}
