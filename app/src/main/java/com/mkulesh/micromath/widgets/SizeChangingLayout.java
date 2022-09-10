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

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class SizeChangingLayout extends LinearLayout
{
    /*--------------------------------------------------------*
     * Creating
     *--------------------------------------------------------*/

    public SizeChangingLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public SizeChangingLayout(Context context)
    {
        super(context);
    }

    /*--------------------------------------------------------*
     * Size change monitoring
     *--------------------------------------------------------*/

    public interface SizeChangedIf
    {
        void onHeightChanged(int h);
    }

    private SizeChangedIf sizeChangedIf = null;

    public void setSizeChangedIf(SizeChangedIf sizeChangedIf)
    {
        this.sizeChangedIf = sizeChangedIf;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        if (sizeChangedIf != null)
        {
            sizeChangedIf.onHeightChanged(h);
        }
    }

}
