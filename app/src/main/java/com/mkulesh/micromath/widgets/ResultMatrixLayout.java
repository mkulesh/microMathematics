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
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.mkulesh.micromath.utils.CompatUtils;

import java.util.ArrayList;

public class ResultMatrixLayout extends TableLayout
{
    private int rowsNumber = 0;
    private int colsNumber = 0;
    private final ArrayList<CustomEditText> fields = new ArrayList<>();

    /*********************************************************
     * Creating
     *********************************************************/

    public ResultMatrixLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public ResultMatrixLayout(Context context)
    {
        super(context);
    }

    public int getRowsNumber()
    {
        return rowsNumber;
    }

    public int getColsNumber()
    {
        return colsNumber;
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
        return height/2;
    }

    public void resize (int rows, int cols, int cellLayoutId)
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

        final TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

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
                final CustomEditText cell = (CustomEditText)tableRow.getChildAt(col);
                if (cell != null)
                {
                    fields.add(cell);
                }
            }
        }

        setPadding(0,0,0,0);
        setBaselineAligned(true);
        setBaselineAlignedChildIndex(rowsNumber > 1? rowsNumber/2 : 0);
    }

    private CustomEditText getCell(int row, int col)
    {
        if (row < getChildCount())
        {
            final TableRow tr = (TableRow)getChildAt(row);
            if (tr != null && col < tr.getChildCount())
            {
                return (CustomEditText)tr.getChildAt(col);
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

    public void prepare(AppCompatActivity activity, FormulaChangeIf termChangeIf)
    {
        for (CustomEditText field : fields)
        {
            field.prepare(activity, termChangeIf);
        }
    }

    public void updateTextColor(int normalColor, int selectedColor)
    {
        for (CustomEditText field : fields)
        {
            int resId = normalColor;
            if (field.isSelected())
            {
                resId = selectedColor;
            }
            CompatUtils.updateBackground(getContext(), field, resId);
        }
    }
}
