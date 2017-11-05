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
package com.mkulesh.micromath.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.CompatUtils;
import com.mkulesh.micromath.utils.ViewUtils;

public class HorizontalNumberPicker extends LinearLayout implements OnClickListener, OnLongClickListener
{
    private EditText editText = null;
    private ImageButton bDecrease = null, bIncrease = null;
    private TextView description = null;
    public int minValue = 1;
    public int maxValue = Integer.MAX_VALUE;

    /*********************************************************
     * Creating
     *********************************************************/

    public HorizontalNumberPicker(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        prepare(attrs);
    }

    public HorizontalNumberPicker(Context context)
    {
        super(context);
        prepare(null);
    }

    private void prepare(AttributeSet attrs)
    {
        setBaselineAligned(false);
        setVerticalGravity(Gravity.CENTER_VERTICAL);
        setOrientation(HORIZONTAL);
        final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.horizontal_number_picker, this);
        editText = ((EditText) findViewById(R.id.edit_text_value));
        if (attrs != null)
        {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.HorizontalNumberPicker, 0, 0);
            CharSequence label = a.getText(R.styleable.HorizontalNumberPicker_label);
            if (label != null)
            {
                ((TextView) findViewById(R.id.label_text)).setText(label);
            }
            editText.setMinimumWidth(a.getDimensionPixelSize(R.styleable.HorizontalNumberPicker_minWidth, 0));
            a.recycle();
        }

        bDecrease = (ImageButton) findViewById(R.id.button_decrease);
        bDecrease.setOnClickListener(this);
        bDecrease.setOnLongClickListener(this);
        updateViewColor(bDecrease);

        bIncrease = (ImageButton) findViewById(R.id.button_increase);
        bIncrease.setOnClickListener(this);
        bIncrease.setOnLongClickListener(this);
        updateViewColor(bIncrease);

        description = (TextView) findViewById(R.id.label_text);
    }

    @Override
    public void onClick(View v)
    {
        int inc = 0;
        if (v.getId() == R.id.button_decrease)
        {
            inc = -1;
        }
        else if (v.getId() == R.id.button_increase)
        {
            inc = 1;
        }
        if (inc != 0)
        {
            if (editText.getText().length() != 0)
            {
                int value = Integer.valueOf(editText.getText().toString()) + inc;
                if (value >= minValue && value <= maxValue)
                {
                    editText.setText(String.valueOf(value));
                }
            }
            else if (inc > 0)
            {
                editText.setText(String.valueOf(inc));
            }
        }
    }

    public void setValue(int value)
    {
        editText.setText(String.valueOf(value));
    }

    public int getValue()
    {
        final int r = Integer.valueOf(editText.getText().toString());
        return ((r < minValue) ? minValue : ((r > maxValue) ? maxValue : r));
    }

    @Override
    public void setEnabled(boolean enabled)
    {
        editText.setEnabled(enabled);
        updateViewColor(editText);
        bDecrease.setEnabled(enabled);
        updateViewColor(bDecrease);
        bIncrease.setEnabled(enabled);
        updateViewColor(bIncrease);
        description.setEnabled(enabled);
        updateViewColor(description);
        super.setEnabled(enabled);
    }

    private void updateViewColor(View v)
    {
        if (v instanceof ImageButton)
        {
            ImageButton b = (ImageButton) v;
            b.clearColorFilter();
            if (!b.isEnabled() || !v.isEnabled())
            {
                b.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
            }
        }
        else if (v instanceof TextView)
        {
            TextView b = (TextView) v;
            b.setTextColor(b.isEnabled() ? CompatUtils.getColor(getContext(), R.color.dialog_content_color)
                    : Color.GRAY);
        }
    }

    @Override
    public boolean onLongClick(View b)
    {
        return ViewUtils.showButtonDescription(getContext(), b);
    }
}
