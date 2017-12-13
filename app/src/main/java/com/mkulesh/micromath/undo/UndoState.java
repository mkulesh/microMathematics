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
package com.mkulesh.micromath.undo;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MenuItem;

import com.mkulesh.micromath.utils.ViewUtils;

import java.util.ArrayList;

public class UndoState
{
    private static final int MAX_ENTRY_NUMBER = 25;

    private static final String ENTRY_NUMBER = "entry_number";
    private static final String ENTRY_STATE = "entry_state";

    private final ArrayList<Parcelable> entrys = new ArrayList<Parcelable>();
    private MenuItem menuItem = null;
    private final Context context;

    /**
     * Default constructor
     */
    public UndoState(Context context)
    {
        this.context = context;
    }

    /*********************************************************
     * Read/write interface
     *********************************************************/

    /**
     * Parcelable interface: procedure writes the state
     */
    public Parcelable onSaveInstanceState()
    {
        Bundle bundle = new Bundle();
        final int n = entrys.size();
        bundle.putInt(ENTRY_NUMBER, n);
        for (int i = 0; i < n; i++)
        {
            bundle.putParcelable(ENTRY_STATE + i, entrys.get(i));
        }
        return bundle;
    }

    /**
     * Parcelable interface: procedure reads the state
     */
    public void onRestoreInstanceState(Parcelable state)
    {
        if (state == null)
        {
            return;
        }
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            final int n = bundle.getInt(ENTRY_NUMBER, 0);
            for (int i = 0; i < n; i++)
            {
                entrys.add(bundle.getParcelable(ENTRY_STATE + i));
            }
        }
    }

    /*********************************************************
     * Store/undo methods
     *********************************************************/

    /**
     * Procedure adds the entry to the stack
     */
    public void addEntry(Parcelable entry)
    {
        if ((entry instanceof DeleteState) && ((DeleteState) entry).getEntries().isEmpty())
        {
            return;
        }
        if ((entry instanceof ReplaceState) && ((ReplaceState) entry).getEntries().isEmpty())
        {
            return;
        }
        while (entrys.size() > MAX_ENTRY_NUMBER)
        {
            entrys.remove(0);
        }
        entrys.add(entry);
        updateMenuItemState(null);
    }

    /**
     * Procedure returns the last entry from the undo stack
     */
    public Parcelable resumeLastEntry()
    {
        if (entrys.size() > 0)
        {
            final int lastIdx = entrys.size() - 1;
            Parcelable lastEntry = entrys.get(lastIdx);
            entrys.remove(lastIdx);
            updateMenuItemState(null);
            return lastEntry;
        }
        return null;
    }

    /**
     * Procedure sets the enabled/disabled state of the linked "Undo" menu item
     */
    public void updateMenuItemState(MenuItem m)
    {
        if (m != null)
        {
            menuItem = m;
        }
        if (menuItem != null)
        {
            menuItem.setEnabled(!entrys.isEmpty());
            ViewUtils.updateMenuIconColor(context, menuItem);
        }
    }

    /**
     * Procedure clears the undo state
     */
    public void clear()
    {
        entrys.clear();
        updateMenuItemState(null);
    }
}
