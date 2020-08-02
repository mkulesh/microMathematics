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
package com.mkulesh.micromath.formula;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.undo.Coordinate;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CustomLayout;
import com.mkulesh.micromath.widgets.ListChangeIf;
import com.mkulesh.micromath.widgets.ListChangeIf.Position;

import java.util.ArrayList;

public class FormulaListView
{
    private final Context context;
    private final LinearLayout list;
    private boolean termDeleted = false;

    /**
     * Default constructor
     */
    public FormulaListView(Context context, LinearLayout layout)
    {
        this.context = context;
        list = layout;
        list.setSaveEnabled(false);
    }

    /**
     * Procedure returns the list of formulas
     */
    public LinearLayout getList()
    {
        return list;
    }

    /**
     * Procedure removes focus from any focusable elements
     */
    public void clearFocus()
    {
        list.requestFocus();
    }

    /**
     * Performs clean-up of the list
     */
    public void clear()
    {
        list.removeAllViews();
        clearFocus();
    }

    /**
     * Procedure changes the enabled state of the formula list view
     */
    public void setEnabled(boolean enabled)
    {
        list.setEnabled(enabled);
    }

    /**
     * Getter for the list of formulas of given type
     */
    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getFormulas(Class<T> c)
    {
        ArrayList<T> retValue = new ArrayList<>();
        final int n = list.getChildCount();
        for (int i = 0; i < n; i++)
        {
            View v = list.getChildAt(i);
            if (v instanceof ListRow)
            {
                ((ListRow) v).getFormulas(c, retValue);
            }
            else if (c.isInstance(v))
            {
                retValue.add((T) v);
            }
        }
        return retValue;
    }

    /**
     * Procedure adds given formula to the list with respect to the given coordinates
     */
    public void add(FormulaBase f, Coordinate coordinate)
    {
        if (coordinate == null)
        {
            return;
        }
        if (f == null || coordinate.row == ViewUtils.INVALID_INDEX)
        {
            return;
        }
        if (coordinate.col == ViewUtils.INVALID_INDEX)
        {
            addAsRow(f, coordinate.row);
        }
        else
        {
            addToRow(f, coordinate.row, coordinate.col);
        }
    }

    /**
     * Procedure adds given formula to the list with respect to the given target formula
     */
    public void add(FormulaBase f, FormulaBase target, Position position)
    {
        if (f == null)
        {
            return;
        }
        int rowIdx = ViewUtils.INVALID_INDEX;
        if (target != null)
        {
            rowIdx = getRowIndex(target.getId());
        }

        final boolean beforeOrAfter = (position == Position.BEFORE || position == Position.AFTER);
        if (rowIdx == ViewUtils.INVALID_INDEX && !beforeOrAfter)
        {
            // cannot be inserted left or right since line index is not known
        }
        else if (rowIdx == ViewUtils.INVALID_INDEX && beforeOrAfter)
        {
            // add to the end of list
            if (!f.isInRightOfPrevious() || list.getChildCount() == 0)
            {
                addAsRow(f, ViewUtils.INVALID_INDEX);
            }
            else
            {
                addToRow(f, list.getChildCount() - 1, ViewUtils.INVALID_INDEX);
            }
        }
        else if (rowIdx != ViewUtils.INVALID_INDEX)
        {
            if (beforeOrAfter)
            {
                addAsRow(f, rowIdx + ((position == Position.BEFORE) ? 0 : 1));
            }
            else
            {
                final View v = list.getChildAt(rowIdx);
                if (v instanceof ListRow)
                {
                    int colIdx = ((ListRow) v).getFormulaIndex(target.getId());
                    if (colIdx != ViewUtils.INVALID_INDEX)
                    {
                        colIdx += ((position == ListChangeIf.Position.RIGHT) ? 1 : 0);
                    }
                    addToRow(f, rowIdx, colIdx);
                }
                else
                {
                    addToRow(f, rowIdx, ((position == Position.LEFT) ? 0 : 1));
                }
            }
        }
    }

    /**
     * Add given formula as a row with given index
     */
    private void addAsRow(FormulaBase f, int rowIdx)
    {
        if (rowIdx >= 0 && rowIdx <= list.getChildCount())
        {
            list.addView(f, rowIdx);
        }
        else
        {
            list.addView(f);
        }
    }

    /**
     * Add given formula to the row with given index
     */
    private void addToRow(FormulaBase f, int rowIdx, int colIdx)
    {
        if (rowIdx >= 0 && rowIdx < list.getChildCount())
        {
            setTermDeleted(false);
            View v = list.getChildAt(rowIdx);
            ListRow row = null;
            if (v instanceof ListRow)
            {
                row = (ListRow) v;
            }
            else
            {
                list.removeView(v);
                row = new ListRow(context);
                list.addView(row, rowIdx);
                row.addView(v);
                if (v instanceof FormulaBase)
                {
                    // check that the current formula depth has no conflicts with allowed formula depth
                    ((FormulaBase) v).checkFormulaDepth();
                }
            }
            if (colIdx == ViewUtils.INVALID_INDEX)
            {
                row.addView(f);
            }
            else
            {
                row.addView(f, colIdx);
            }
            if (f != null && f instanceof FormulaBase)
            {
                // check that the current formula depth has no conflicts with allowed formula depth
                f.checkFormulaDepth();
            }
            if (termDeleted)
            {
                Toast.makeText(context, context.getResources().getString(R.string.error_max_layout_depth),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void setTermDeleted(boolean termDeleted)
    {
        this.termDeleted = termDeleted;
    }

    /**
     * Procedure deletes the given formula from the list
     */
    public FormulaBase delete(FormulaBase f)
    {
        final int idx = getRowIndex(f.getId());
        if (idx == ViewUtils.INVALID_INDEX)
        {
            return null;
        }
        View v = list.getChildAt(idx);
        if (v instanceof ListRow)
        {
            ListRow row = (ListRow) v;
            FormulaBase selectedFormula = row.deleteFromView(f);
            if (selectedFormula == null && row.getChildCount() == 0)
            {
                list.removeView(row);
                return getNextFormula(list, idx);
            }
            return selectedFormula;
        }
        else if (v instanceof FormulaBase)
        {
            list.removeView(v);
            return getNextFormula(list, idx);
        }
        return null;
    }

    /**
     * Replace the given formula by the new one
     */
    public boolean replace(FormulaBase oldFormula, FormulaBase newFormula)
    {
        if (oldFormula == null)
        {
            return false;
        }
        final int n = list.getChildCount();
        for (int i = 0; i < n; i++)
        {
            View v = list.getChildAt(i);
            if (v instanceof ListRow)
            {
                if (((ListRow) v).replaceFormula(oldFormula, newFormula))
                {
                    return true;
                }
            }
            else if (v instanceof FormulaBase && v == oldFormula)
            {
                list.removeView(v);
                list.addView(newFormula, i);
                return true;
            }
        }
        return false;
    }

    /**
     * Procedure returns a formula with given offset related to the formula with given id
     */
    public FormulaBase getFormula(int id, Position position)
    {
        final int idx = getRowIndex(id);
        if (idx == ViewUtils.INVALID_INDEX)
        {
            return null;
        }
        View v = list.getChildAt(idx);
        if (v instanceof ListRow)
        {
            ListRow row = (ListRow) v;
            FormulaBase f = row.getFormula(id, position);
            if (f != null)
            {
                return f;
            }
        }
        v = getNextView(list, idx, position);
        if (v instanceof ListRow)
        {
            ListRow row = (ListRow) v;
            if (row.getChildCount() > 0)
            {
                v = (position == Position.BEFORE || position == Position.LEFT) ? row
                        .getChildAt(row.getChildCount() - 1) : row.getChildAt(0);
            }
        }
        if (v instanceof FormulaBase)
        {
            return (FormulaBase) v;
        }
        return null;
    }

    /**
     * Procedure returns the index of the list line that contains the formula with given ID
     */
    private int getRowIndex(int id)
    {
        final int n = list.getChildCount();
        for (int i = 0; i < n; i++)
        {
            final View v = list.getChildAt(i);
            if (v instanceof ListRow)
            {
                if (((ListRow) v).getFormulaIndex(id) != ViewUtils.INVALID_INDEX)
                {
                    return i;
                }
            }
            else if (v instanceof FormulaBase)
            {
                if (v.getId() == id)
                {
                    return i;
                }
            }
        }
        return ViewUtils.INVALID_INDEX;
    }

    /**
     * Procedure returns full coordinates of the formula with given ID
     */
    public Coordinate getCoordinate(FormulaBase f)
    {
        Coordinate coordinate = new Coordinate();
        final int id = f.getId();
        coordinate.row = getRowIndex(id);
        if (coordinate.row != ViewUtils.INVALID_INDEX)
        {
            View v = list.getChildAt(coordinate.row);
            if (v instanceof ListRow)
            {
                coordinate.col = ((ListRow) v).getFormulaIndex(id);
            }
        }
        return coordinate;
    }

    /**
     * Procedure searches a root formula with given properties
     */
    public FormulaBase getFormula(String name, int argNumber, int rootId, boolean excludeRoot, boolean searchAll)
    {
        if (name == null)
        {
            return null;
        }
        int idx = getRowIndex(rootId);
        if (idx == ViewUtils.INVALID_INDEX || searchAll)
        {
            idx = list.getChildCount() - 1;
        }
        for (int i = idx; i >= 0; i--)
        {
            final View vRow = list.getChildAt(i);
            if (vRow instanceof ListRow)
            {
                ListRow row = ((ListRow) vRow);
                int col = row.getFormulaIndex(rootId);
                if (col == ViewUtils.INVALID_INDEX || searchAll)
                {
                    col = row.getChildCount() - 1;
                }
                for (int j = col; j >= 0; j--)
                {
                    final View vCol = row.getChildAt(j);
                    if (vCol instanceof Equation)
                    {
                        final Equation f = (Equation) vCol;
                        if (f.isEqual(name, argNumber, rootId, excludeRoot))
                        {
                            return f;
                        }
                    }
                }
            }
            else if (vRow instanceof Equation)
            {
                final Equation f = (Equation) vRow;
                if (f.isEqual(name, argNumber, rootId, excludeRoot))
                {
                    return f;
                }
            }
        }
        return null;
    }

    /*********************************************************
     * Helper static methods
     *********************************************************/

    private static FormulaBase getNextFormula(LinearLayout l, int idx)
    {
        if (idx == ViewUtils.INVALID_INDEX)
        {
            return null;
        }
        int targetIdx = ViewUtils.INVALID_INDEX;
        if (idx < l.getChildCount())
        {
            targetIdx = idx;
        }
        else if ((idx - 1) < l.getChildCount())
        {
            targetIdx = idx - 1;
        }
        else if (l.getChildCount() > 0)
        {
            targetIdx = l.getChildCount() - 1;
        }
        FormulaBase f = null;
        if (targetIdx != ViewUtils.INVALID_INDEX)
        {
            View v = l.getChildAt(targetIdx);
            if (v instanceof ListRow)
            {
                ListRow row = (ListRow) v;
                if (row.getChildCount() > 0)
                {
                    v = row.getChildAt(0);
                }
            }
            if (v instanceof FormulaBase)
            {
                f = (FormulaBase) v;
            }
        }
        return f;
    }

    private static View getNextView(LinearLayout l, int idx, Position position)
    {
        View v = null;
        if (idx > 0 && (position == ListChangeIf.Position.BEFORE || position == ListChangeIf.Position.LEFT))
        {
            v = l.getChildAt(idx - 1);
        }
        else if (idx < l.getChildCount() - 1
                && (position == ListChangeIf.Position.AFTER || position == ListChangeIf.Position.RIGHT))
        {
            v = l.getChildAt(idx + 1);
        }
        return v;
    }

    /*********************************************************
     * Helper class that holds a single row
     *********************************************************/

    public static final class ListRow extends CustomLayout
    {
        /**
         * Default constructor
         */
        public ListRow(Context context)
        {
            super(context);
            setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
            setGravity(Gravity.LEFT);
        }

        /**
         * Default constructor to avoid Lint warning
         */
        public ListRow(Context context, AttributeSet attrs)
        {
            super(context, attrs);
        }

        @Override
        public void addView(View child)
        {
            super.addView(child);
            reIndex();
        }

        @Override
        public void addView(View child, int index)
        {
            super.addView(child, index);
            reIndex();
        }

        @Override
        public void removeView(View view)
        {
            if (view instanceof FormulaBase)
            {
                ((FormulaBase) view).setInRightOfPrevious(false);
            }
            super.removeView(view);
            reIndex();
        }

        /**
         * Procedure deletes given formula from this row
         */
        public FormulaBase deleteFromView(FormulaBase f)
        {
            final int idx = getFormulaIndex(f.getId());
            removeView(f);
            return getNextFormula(this, idx);
        }

        /**
         * Replace the given formula by the new one
         */
        public boolean replaceFormula(FormulaBase oldFormula, FormulaBase newFormula)
        {
            boolean retValue = false;
            final int n = getChildCount();
            for (int i = 0; i < n; i++)
            {
                View v = getChildAt(i);
                if (v == oldFormula)
                {
                    removeView(v);
                    addView(newFormula, i);
                    retValue = true;
                    break;
                }
            }
            return retValue;
        }

        /**
         * Getter for the list of formulas of given type
         */
        @SuppressWarnings("unchecked")
        public <T> void getFormulas(Class<T> c, ArrayList<T> retValue)
        {
            final int n = getChildCount();
            for (int i = 0; i < n; i++)
            {
                View v = getChildAt(i);
                if (c.isInstance(v))
                {
                    retValue.add((T) v);
                }
            }
        }

        /**
         * Procedure returns a formula with given offset related to the formula with given id
         */
        public FormulaBase getFormula(int id, Position position)
        {
            if (position == Position.BEFORE || position == Position.AFTER)
            {
                return null;
            }
            final int idx = getFormulaIndex(id);
            if (idx != ViewUtils.INVALID_INDEX)
            {
                View v = getNextView(this, idx, position);
                if (v != null && v instanceof FormulaBase)
                {
                    return (FormulaBase) v;
                }
            }
            return null;
        }

        /**
         * Procedure returns the index of the formula with given ID
         */
        private int getFormulaIndex(int id)
        {
            final int n = getChildCount();
            for (int i = 0; i < n; i++)
            {
                final View v = getChildAt(i);
                if (v instanceof FormulaBase)
                {
                    if (v.getId() == id)
                    {
                        return i;
                    }
                }
            }
            return ViewUtils.INVALID_INDEX;
        }

        /**
         * Procedure updates "InRightOfPrevious" property for all formulas
         */
        private void reIndex()
        {
            final int n = getChildCount();
            for (int i = 0; i < n; i++)
            {
                final View v = getChildAt(i);
                if (v instanceof FormulaBase)
                {
                    ((FormulaBase) v).setInRightOfPrevious(i > 0);
                }
            }
        }
    }
}
