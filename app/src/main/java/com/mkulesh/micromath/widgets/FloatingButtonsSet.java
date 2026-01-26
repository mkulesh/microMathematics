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
package com.mkulesh.micromath.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.CompatUtils;

public class FloatingButtonsSet extends LinearLayout
{

    public FloatingButtonsSet(Context context)
    {
        super(context);
        prepare();
    }

    public FloatingButtonsSet(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        prepare();
    }

    @SuppressLint("RtlHardcoded")
    private void prepare()
    {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.RIGHT);
    }

    public void activate(int id, OnClickListener handler)
    {
        for (int i = 0; i < getChildCount(); i++)
        {
            final View v = getChildAt(i);
            if (getContext() != null && id == v.getId())
            {
                v.setVisibility(View.VISIBLE);
                if (v instanceof FloatingActionButton)
                {
                    CompatUtils.setTooltip(v, getContext());
                }
                v.setOnClickListener(handler);
                if (v instanceof FloatingActionButton)
                {
                    CompatUtils.setDrawableColorAttr(getContext(),
                            ((FloatingActionButton) v).getDrawable(), R.attr.colorMicroMathFloatingIcon);
                }
            }
            else
            {
                v.setVisibility(View.GONE);
                v.setOnLongClickListener(null);
                v.setOnClickListener(null);
            }
        }
    }
}
