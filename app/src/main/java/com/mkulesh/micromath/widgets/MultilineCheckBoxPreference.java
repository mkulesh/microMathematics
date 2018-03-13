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
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MultilineCheckBoxPreference extends CheckBoxPreference
{
    public MultilineCheckBoxPreference(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    protected void onBindView(View view)
    {
        super.onBindView(view);
        makeMultiline(view);
    }

    private void makeMultiline(View view)
    {
        if (view instanceof ViewGroup)
        {
            ViewGroup grp = (ViewGroup) view;
            for (int index = 0; index < grp.getChildCount(); index++)
            {
                makeMultiline(grp.getChildAt(index));
            }
        }
        else if (view instanceof TextView)
        {
            TextView t = (TextView) view;
            t.setSingleLine(false);
            t.setEllipsize(null);
        }
    }
}
