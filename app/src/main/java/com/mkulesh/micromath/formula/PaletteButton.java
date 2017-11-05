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
package com.mkulesh.micromath.formula;

import android.content.Context;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.mkulesh.micromath.R;
import com.mkulesh.micromath.utils.ViewUtils;

import java.util.Arrays;

public class PaletteButton extends AppCompatImageButton
{
    public enum Category
    {
        INTERVAL,
        CONVERSION
    }

    private String code = null;
    private String shortCut = null;
    private Category[] categories = null;
    private final boolean[] enabled = new boolean[Category.values().length];

    public PaletteButton(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        enableAll();
    }

    public PaletteButton(Context context)
    {
        super(context);
        enableAll();
    }

    public PaletteButton(Context context, int shortCutId, int imageId, int descriptionId, String code)
    {
        super(context);
        final int buttonSize = context.getResources().getDimensionPixelSize(R.dimen.activity_toolbar_height) - 2
                * context.getResources().getDimensionPixelSize(R.dimen.activity_palette_vertical_padding);
        setImageResource(imageId);
        setBackgroundResource(R.drawable.clickable_background);
        setLayoutParams(new ViewGroup.LayoutParams(buttonSize, buttonSize));
        if (shortCutId != Palette.NO_BUTTON)
        {
            shortCut = context.getResources().getString(shortCutId);
        }
        if (descriptionId != Palette.NO_BUTTON)
        {
            String description = context.getResources().getString(descriptionId);
            if (shortCut != null)
            {
                description += " ('";
                description += shortCut;
                description += "')";
            }
            setContentDescription(description);
            setLongClickable(true);
        }
        this.code = code;
        enableAll();
        ViewUtils.setButtonIconColor(getContext(), this, R.color.micromath_icons);
    }

    public String getCode()
    {
        return code;
    }

    public String getShortCut()
    {
        return shortCut;
    }

    public Category[] getCategories()
    {
        return categories;
    }

    public void setCategories(Category[] categories)
    {
        this.categories = categories;
    }

    private void enableAll()
    {
        Arrays.fill(enabled, true);
    }

    public void setEnabled(Category t, boolean value)
    {
        enabled[t.ordinal()] = value;
        super.setEnabled(true);
        for (boolean en : enabled)
        {
            if (!en)
            {
                super.setEnabled(false);
                break;
            }
        }
    }
}
