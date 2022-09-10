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
package com.mkulesh.micromath.formula;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;

import androidx.annotation.AttrRes;
import androidx.appcompat.widget.AppCompatImageButton;

import com.mkulesh.micromath.formula.terms.TermTypeIf;
import com.mkulesh.micromath.R;
import com.mkulesh.micromath.utils.ViewUtils;

import java.util.Arrays;

public class PaletteButton extends AppCompatImageButton
{
    public enum Category
    {
        NEW_TERM,
        TOP_LEVEL_TERM,
        CONVERSION,
        INDEX,
        COMPARATOR
    }

    private String group = "";
    private String code = null;
    private int imageId = Palette.NO_BUTTON;
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

    public PaletteButton(Context context, TermTypeIf b)
    {
        this(context, b.getGroupType().toString(), b.getLowerCaseName(),
                b.getImageId(), b.getDescriptionId(), b.getShortCutId());
        this.categories = new Category[1];
        this.categories[0] = b.getPaletteCategory();
    }

    public PaletteButton(Context context, String group, String code, int imageId, int descriptionId, int shortCutId)
    {
        super(context);
        final int buttonSize = context.getResources().getDimensionPixelSize(R.dimen.activity_toolbar_height) - 2
                * context.getResources().getDimensionPixelSize(R.dimen.activity_palette_vertical_padding);
        setLayoutParams(new ViewGroup.LayoutParams(buttonSize, buttonSize));

        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.selectableItemBackground, outValue, true);
        setBackgroundResource(outValue.resourceId);

        this.group = group;
        this.code = code;
        this.imageId = imageId;
        if (hasImage())
        {
            setImageResource(imageId);
        }

        String shortCut = null;
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
        enableAll();
        ViewUtils.setImageButtonColorAttr(getContext(), this,
                isEnabled() ? R.attr.colorMicroMathIcon : R.attr.colorPrimaryDark);
    }

    public String getGroup()
    {
        return group;
    }

    public String getCode()
    {
        return code;
    }

    public boolean hasImage()
    {
        return imageId != Palette.NO_BUTTON;
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

    /*--------------------------------------------------------*
     * Performance optimization: fast color settings
     *--------------------------------------------------------*/

    private int colorAttrId = Integer.MIN_VALUE;

    public void setColorAttr(@AttrRes int attrId)
    {
        if (this.colorAttrId != attrId)
        {
            this.colorAttrId = attrId;
            ViewUtils.setImageButtonColorAttr(getContext(), this, attrId);
        }
    }
}
