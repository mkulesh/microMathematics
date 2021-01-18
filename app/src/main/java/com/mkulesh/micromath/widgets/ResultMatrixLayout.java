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
package com.mkulesh.micromath.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;

import androidx.annotation.AttrRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;

import com.mkulesh.micromath.utils.CompatUtils;
import com.mkulesh.micromath.utils.IdGenerator;
import com.mkulesh.micromath.utils.ViewUtils;

import java.util.ArrayList;

public class ResultMatrixLayout extends TableLayout
{
    static final class ElementTag
    {
        final int row;
        final int col;
        final int idx;

        ElementTag(int r, int c, int i)
        {
            row = r;
            col = c;
            idx = i;
        }
    }

    private int rowsNumber = 0;
    private int colsNumber = 0;
    private final ArrayList<CustomEditText> fields = new ArrayList<>();

    /*--------------------------------------------------------*
     * Creating
     *--------------------------------------------------------*/

    public ResultMatrixLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ResultMatrixLayout(Context context)
    {
        super(context);
    }

    @Override
    public int getBaseline()
    {
        int height = getPaddingTop();
        for (int row = 0; row < getChildCount(); row++)
        {
            final View child = getChildAt(row);
            height += child.getMeasuredHeight();
        }
        height += getPaddingBottom();
        return height / 2;
    }

    public void resize(int rows, int cols, int cellLayoutId)
    {
        if (rowsNumber == rows && colsNumber == cols)
        {
            return;
        }
        rowsNumber = rows;
        colsNumber = cols;

        removeAllViews();
        fields.clear();

        final TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(
                TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);

        final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);

        for (int row = 0; row < rowsNumber; row++)
        {
            final TableRow tableRow = new TableRow(getContext());
            tableRow.setLayoutParams(tableParams); // TableLayout is the parent view
            addView(tableRow);

            for (int col = 0; col < colsNumber; col++)
            {
                inflater.inflate(cellLayoutId, tableRow);
            }

            if (tableRow.getChildCount() > 0)
            {
                tableRow.setBaselineAligned(true);
                tableRow.setBaselineAlignedChildIndex(0);
            }

            for (int col = 0; col < tableRow.getChildCount(); col++)
            {
                final CustomEditText c = (CustomEditText) tableRow.getChildAt(col);
                if (c != null)
                {
                    c.setId(IdGenerator.generateId());
                    c.setTag(new ElementTag(row, col, fields.size()));
                    fields.add(c);
                }
            }
        }

        setPadding(0, 0, 0, 0);
        setBaselineAligned(true);
        setBaselineAlignedChildIndex(rowsNumber > 1 ? rowsNumber / 2 : 0);
    }

    private CustomEditText getCell(int row, int col)
    {
        if (row < getChildCount())
        {
            final TableRow tr = (TableRow) getChildAt(row);
            if (tr != null && col < tr.getChildCount())
            {
                return (CustomEditText) tr.getChildAt(col);
            }
        }
        return null;
    }

    public void setText(int row, int col, String text)
    {
        final CustomEditText cell = getCell(row, col);
        if (cell != null)
        {
            cell.setText(text);
        }
    }

    public void updateTextSize(ScaledDimensions dimen)
    {
        for (CustomEditText field : fields)
        {
            field.updateTextSize(dimen, 0, ScaledDimensions.Type.MATRIX_COLUMN_PADDING);
        }
    }

    public void prepare(AppCompatActivity activity, FormulaChangeIf termChangeIf, FocusChangeIf focusChangeIf)
    {
        for (CustomEditText field : fields)
        {
            field.prepare(activity, termChangeIf);
            field.setChangeIf(null, focusChangeIf);
        }
    }

    public void updateTextColor(@DrawableRes int normalDrawable, @DrawableRes int selectedDrawable, @AttrRes int colorAttr)
    {
        for (CustomEditText field : fields)
        {
            if (field.isSelected())
            {
                CompatUtils.updateBackgroundAttr(getContext(), field, selectedDrawable, colorAttr);
            }
            else
            {
                CompatUtils.updateBackground(getContext(), field, normalDrawable);
            }
        }
    }

    private boolean isCell(CustomEditText c)
    {
        return c != null && c.getTag() != null && c.getTag() instanceof ElementTag;
    }

    public int getFirstFocusId()
    {
        return fields.isEmpty() ? ViewUtils.INVALID_INDEX : fields.get(0).getId();
    }

    public int getLastFocusId()
    {
        return fields.isEmpty() ? ViewUtils.INVALID_INDEX : fields.get(fields.size() - 1).getId();
    }

    public int getNextFocusId(CustomEditText c, FocusChangeIf.NextFocusType focusType)
    {
        if (!isCell(c))
        {
            return ViewUtils.INVALID_INDEX;
        }
        ElementTag tag = (ElementTag) c.getTag();
        CustomEditText nextC = null;
        switch (focusType)
        {
        case FOCUS_DOWN:
            nextC = tag.row + 1 < rowsNumber ? getCell(tag.row + 1, tag.col) : null;
            break;
        case FOCUS_LEFT:
            nextC = tag.idx >= 1 ? fields.get(tag.idx - 1) : null;
            break;
        case FOCUS_RIGHT:
            nextC = tag.idx + 1 < fields.size() ? fields.get(tag.idx + 1) : null;
            break;
        case FOCUS_UP:
            nextC = tag.row >= 1 ? getCell(tag.row - 1, tag.col) : null;
            break;
        }
        return nextC == null ? ViewUtils.INVALID_INDEX : nextC.getId();
    }

    public void setText(String s, ScaledDimensions dimen)
    {
        for (CustomEditText field : fields)
        {
            field.setText(s);
            field.updateTextSize(dimen, 0, ScaledDimensions.Type.MATRIX_COLUMN_PADDING);
        }
    }
}
