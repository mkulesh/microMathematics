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
import android.content.res.TypedArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;

import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.ClipboardManager;
import com.mkulesh.micromath.utils.ViewUtils;

import java.util.ArrayList;

public class ContextMenuHandler
{
    enum Type
    {
        EXPAND(R.id.context_menu_expand),
        CUT(R.id.context_menu_cut),
        COPY(R.id.context_menu_copy),
        PASTE(R.id.context_menu_paste);

        private final int resId;

        Type(int resId)
        {
            this.resId = resId;
        }

        int getResId()
        {
            return resId;
        }
    }

    private final boolean[] enabled = new boolean[Type.values().length];
    private final Context context;
    private FormulaChangeIf formulaChangeIf = null;
    private androidx.appcompat.view.ActionMode actionMode = null;
    private View actionModeOwner = null;

    public ContextMenuHandler(Context context)
    {
        this.context = context;
        for (int i = 0; i < Type.values().length; i++)
        {
            enabled[i] = true;
        }
    }

    public void initialize(TypedArray a)
    {
        enabled[Type.EXPAND.ordinal()] = a.getBoolean(R.styleable.CustomViewExtension_contextMenuExpand, true);
        enabled[Type.CUT.ordinal()] = a.getBoolean(R.styleable.CustomViewExtension_contextMenuCut, true);
        enabled[Type.COPY.ordinal()] = a.getBoolean(R.styleable.CustomViewExtension_contextMenuCopy, true);
        enabled[Type.PASTE.ordinal()] = a.getBoolean(R.styleable.CustomViewExtension_contextMenuPaste, true);
    }

    private boolean isMenuEmpty()
    {
        for (int i = 0; i < Type.values().length; i++)
        {
            if (enabled[i])
            {
                return false;
            }
        }
        return true;
    }

    public androidx.appcompat.view.ActionMode getActionMode()
    {
        return actionMode;
    }

    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback()
    {
        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu)
        {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
            for (int i = 0; i < menu.size(); i++)
            {
                ViewUtils.updateMenuIconColor(context, menu.getItem(i));
            }
            for (int i = 0; i < Type.values().length; i++)
            {
                menu.findItem(Type.values()[i].getResId()).setVisible(enabled[i]);
            }
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu)
        {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item)
        {
            if (formulaChangeIf != null)
            {
                if (processMenu(item.getItemId()))
                {
                    mode.finish();
                }
            }
            return true;
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode)
        {
            actionMode = null;
            if (formulaChangeIf != null)
            {
                formulaChangeIf.finishActionMode(actionModeOwner);
            }
        }
    };

    public void startActionMode(AppCompatActivity activity, View actionModeOwner, FormulaChangeIf formulaChangeIf)
    {
        this.actionModeOwner = actionModeOwner;
        this.formulaChangeIf = formulaChangeIf;

        ArrayList<View> list = null;
        if (this.actionModeOwner != null && this.actionModeOwner instanceof CustomEditText)
        {
            list = new ArrayList<>();
            list.add(this.actionModeOwner);
        }

        this.formulaChangeIf.onTermSelection(this.actionModeOwner, true, list);
        if (isMenuEmpty())
        {
            this.formulaChangeIf.onObjectProperties(this.actionModeOwner);
            this.formulaChangeIf.onTermSelection(this.actionModeOwner, false, list);
            return;
        }
        actionMode = activity.startSupportActionMode(actionModeCallback);
    }

    private boolean processMenu(int itemId)
    {
        switch (itemId)
        {
        case R.id.context_menu_expand:
            FormulaChangeIf newIf = formulaChangeIf.onExpandSelection(actionModeOwner, this);
            if (newIf != null)
            {
                formulaChangeIf = newIf;
                formulaChangeIf.onTermSelection(null, true, null);
                actionModeOwner = null;
            }
            return false;
        case R.id.context_menu_cut:
            if (actionModeOwner != null && actionModeOwner instanceof CustomEditText)
            {
                CustomEditText t = (CustomEditText) actionModeOwner;
                ClipboardManager.copyToClipboard(context, t.getText().toString());
                if (t.getText().length() == 0)
                {
                    formulaChangeIf.onDelete(t);
                    actionModeOwner = null;
                }
                else
                {
                    t.setText("");
                }
            }
            else
            {
                formulaChangeIf.onCopyToClipboard();
                formulaChangeIf.onDelete(null);
            }
            break;
        case R.id.context_menu_copy:
            if (actionModeOwner != null && actionModeOwner instanceof CustomEditText)
            {
                CustomEditText t = (CustomEditText) actionModeOwner;
                ClipboardManager.copyToClipboard(context, t.getText().toString());
            }
            else
            {
                formulaChangeIf.onCopyToClipboard();
            }
            break;
        case R.id.context_menu_paste:
            formulaChangeIf.onPasteFromClipboard(actionModeOwner, ClipboardManager.readFromClipboard(context, true));
            break;
        default:
            break;
        }
        return true;
    }
}
