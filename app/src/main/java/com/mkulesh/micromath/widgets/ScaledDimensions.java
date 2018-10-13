/*
 * Copyright (C) 2014-2018 Mikhail Kulesh
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details. You should have received a copy of the GNU General
 * Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.mkulesh.micromath.widgets;

import android.content.Context;
import android.content.res.Resources;

import com.mkulesh.micromath.plus.R;

public class ScaledDimensions
{
    public static final float MIN_SCALE_FACTOR = 0.1f;

    public enum Type
    {
        HOR_ROOT_PADDING,
        VERT_ROOT_PADDING,
        STROKE_WIDTH,
        TEXT_SIZE,
        TEXT_MIN_WIDTH,
        HOR_TEXT_PADDING,
        BIG_SYMBOL_SIZE,
        SMALL_SYMBOL_SIZE,
        HOR_SYMBOL_PADDING,
        HOR_BRAKET_PADDING,
        VERT_TERM_PADDING,
        HEADER_PADDING,
        MATRIX_COLUMN_PADDING
    }

    private float scaleFactor = 1.0f;
    private float depthStepSize;
    private float[] dimen;

    public ScaledDimensions(Context context)
    {
        final Resources r = context.getResources();
        scaleFactor = 1.0f;
        depthStepSize = r.getDimension(R.dimen.formula_depth_step);

        dimen = new float[Type.values().length];
        // Note that we shall store all configured in dp in order to perform the correct scaling later
        dimen[Type.HOR_ROOT_PADDING.ordinal()] = r.getDimension(R.dimen.formula_hor_root_padding);
        dimen[Type.VERT_ROOT_PADDING.ordinal()] = r.getDimension(R.dimen.formula_vert_root_padding);
        dimen[Type.STROKE_WIDTH.ordinal()] = r.getDimension(R.dimen.formula_stroke_width);
        dimen[Type.TEXT_SIZE.ordinal()] = r.getDimension(R.dimen.formula_text_size);
        dimen[Type.TEXT_MIN_WIDTH.ordinal()] = r.getDimension(R.dimen.formula_text_minwidth);
        dimen[Type.HOR_TEXT_PADDING.ordinal()] = r.getDimension(R.dimen.formula_hor_text_padding);
        dimen[Type.BIG_SYMBOL_SIZE.ordinal()] = r.getDimension(R.dimen.formula_big_symbol_size);
        dimen[Type.SMALL_SYMBOL_SIZE.ordinal()] = r.getDimension(R.dimen.formula_small_symbol_size);
        dimen[Type.HOR_SYMBOL_PADDING.ordinal()] = r.getDimension(R.dimen.formula_hor_symbol_padding);
        dimen[Type.HOR_BRAKET_PADDING.ordinal()] = r.getDimension(R.dimen.formula_hor_bracket_padding);
        dimen[Type.VERT_TERM_PADDING.ordinal()] = r.getDimension(R.dimen.formula_vert_term_padding);
        dimen[Type.HEADER_PADDING.ordinal()] = r.getDimension(R.dimen.formula_header_padding);
        dimen[Type.MATRIX_COLUMN_PADDING.ordinal()] = r.getDimension(R.dimen.formula_matrix_column_padding);
    }

    /**
     * Procedure shall be used in order to reset scale factor
     */
    public void reset()
    {
        scaleFactor = 1.0f;
    }

    /**
     * Procedure sets new scale factor
     */
    public void setScaleFactor(float scaleFactor)
    {
        this.scaleFactor = Math.max(MIN_SCALE_FACTOR, this.scaleFactor * scaleFactor);
    }

    public float getScaleFactor()
    {
        return scaleFactor;
    }

    public int get(Type type)
    {
        return Math.round(dimen[type.ordinal()] * scaleFactor);
    }

    public float getTextSize(int termDepth)
    {
        return (dimen[Type.TEXT_SIZE.ordinal()] - depthStepSize * termDepth) * scaleFactor;
    }

    public float getTextSize(Type type, int termDepth)
    {
        return (dimen[type.ordinal()] - depthStepSize * termDepth) * scaleFactor;
    }
}
