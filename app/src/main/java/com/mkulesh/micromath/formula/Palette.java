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
import android.content.SharedPreferences;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.preference.PreferenceManager;

import com.mkulesh.micromath.dialogs.DialogPaletteSettings;
import com.mkulesh.micromath.formula.PaletteButton.Category;
import com.mkulesh.micromath.formula.terms.TermFactory;
import com.mkulesh.micromath.formula.terms.TermTypeIf;
import com.mkulesh.micromath.formula.terms.UserFunctions;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.PaletteSettingsChangeIf;
import com.mkulesh.micromath.utils.ClipboardManager;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.FocusChangeIf;
import com.mkulesh.micromath.widgets.ListChangeIf;
import com.mkulesh.micromath.widgets.TextChangeIf;

import java.util.ArrayList;
import java.util.List;

/*********************************************************
 * This class implements symbol palette
 *********************************************************/
public class Palette implements OnClickListener, OnLongClickListener, TextChangeIf, FocusChangeIf, PaletteSettingsChangeIf
{
    public static final int NO_BUTTON = -1;
    public static final String VISIBLE_PALETTE_GROUPS = "visible_palette_groups";

    private final Context context;
    private final ListChangeIf listChangeIf;
    private final ArrayList<ArrayList<PaletteButton>> paletteBlock = new ArrayList<>();
    private final List<PaletteButton> termButtons = new ArrayList<>();
    private final LinearLayout paletteLayout;
    private final CustomEditText hiddenInput;
    private String lastHiddenInput = "";
    private List<String> visibleGroups = new ArrayList<>();

    public Palette(Context context, LinearLayout paletteLayout, ListChangeIf listChangeIf)
    {
        this.context = context;
        this.listChangeIf = listChangeIf;
        this.paletteLayout = paletteLayout;

        AppCompatImageButton paletteSettingsButton = paletteLayout.findViewById(R.id.palette_settings_button);
        paletteSettingsButton.setOnLongClickListener(this);
        paletteSettingsButton.setOnClickListener(this);
        ViewUtils.setImageButtonColorAttr(context, paletteSettingsButton, R.attr.colorMicroMathIcon);

        hiddenInput = paletteLayout.findViewById(R.id.hidden_edit_text);
        hiddenInput.setChangeIf(this, this);
        hiddenInput.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        enableHiddenInput(false);

        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        final String visibleGroupsStr = pref.getString(VISIBLE_PALETTE_GROUPS, "");
        ViewUtils.Debug(this, "Default visible palette groups: " + visibleGroupsStr);
        if (visibleGroupsStr.isEmpty() || visibleGroupsStr.contains(FormulaBase.class.getSimpleName()))
        {
            visibleGroups.add(FormulaBase.class.getSimpleName());
        }
        for (TermTypeIf.GroupType g : TermFactory.collectPaletteGroups())
        {
            if ((visibleGroupsStr.isEmpty() && g.isShowByDefault()) || visibleGroupsStr.contains(g.toString()))
            {
                visibleGroups.add(g.toString());
            }
        }
        addButtonsToPalette();
    }

    private void addButtonsToPalette()
    {
        // clear previous state
        termButtons.clear();
        paletteBlock.clear();
        for (int i = paletteLayout.getChildCount() - 1; i > 0; i--)
        {
            if (paletteLayout.getChildAt(i) instanceof PaletteButton)
            {
                paletteLayout.removeViewAt(i);
            }
        }

        // Add elements
        FormulaBase.addToPalette(context, termButtons);
        for (TermTypeIf.GroupType g : TermFactory.collectPaletteGroups())
        {
            TermFactory.addToPalette(context, termButtons, false, g);
        }

        // prepare all buttons
        for (int i = 0; i < Category.values().length; i++)
        {
            paletteBlock.add(new ArrayList<>());
        }
        for (PaletteButton pb : termButtons)
        {
            if (pb.getCategories() != null)
            {
                for (Category cat : pb.getCategories())
                {
                    paletteBlock.get(cat.ordinal()).add(pb);
                }
            }
            if (pb.hasImage() && visibleGroups.contains(pb.getGroup()))
            {
                pb.setOnLongClickListener(this);
                pb.setOnClickListener(this);
                paletteLayout.addView(pb);
            }
        }
    }

    @Override
    public void onPaletteVisibleChange(List<String> visibleGroups)
    {
        ViewUtils.Debug(this, "Visible palette groups: " + visibleGroups.toString());
        final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefEditor = pref.edit();
        prefEditor.putString(VISIBLE_PALETTE_GROUPS, visibleGroups.toString());
        prefEditor.commit();
        this.visibleGroups = visibleGroups;
        addButtonsToPalette();
        if (listChangeIf != null)
        {
            listChangeIf.updatePalette();
        }
    }

    /**
     * This procedure is used to enable/disable whole palette
     */
    public void setEnabled(boolean enabled)
    {
        paletteLayout.setEnabled(enabled);
        updateButtonsColor();
    }

    /**
     * This procedure is used to enable/disable palette buttons related to a formula term
     */
    public void setPaletteBlockEnabled(Category t, boolean enabled)
    {
        for (PaletteButton b : paletteBlock.get(t.ordinal()))
        {
            b.setEnabled(t, enabled);
        }
        updateButtonsColor();
    }

    /**
     * Procedure sets the background color for all buttons depending on enabled status
     */
    private void updateButtonsColor()
    {
        for (PaletteButton b : termButtons)
        {
            final boolean isEnabled = b.isEnabled() && paletteLayout.isEnabled();
            b.setColorAttr(isEnabled ? R.attr.colorMicroMathIcon : R.attr.colorPrimaryDark);
        }
    }

    @Override
    public void onClick(View b)
    {
        if (b instanceof AppCompatImageButton)
        {
            if (b.getId() == R.id.palette_settings_button)
            {
                DialogPaletteSettings d = new DialogPaletteSettings(context, this, visibleGroups);
                d.show();
            }
            else if (listChangeIf != null)
            {
                final PaletteButton pb = (PaletteButton) b;
                listChangeIf.onPalettePressed(pb.getCode());
            }
        }
    }

    @Override
    public boolean onLongClick(View b)
    {
        if (b instanceof AppCompatImageButton)
        {
            return ViewUtils.showButtonDescription(context, b);
        }
        return false;
    }

    public void enableHiddenInput(boolean hiddenInputEnabled)
    {
        hiddenInput.setTextWatcher(false);
        final int newVis = hiddenInputEnabled ? View.VISIBLE : View.GONE;
        if (hiddenInput.getVisibility() != newVis)
        {
            hiddenInput.setVisibility(newVis);
        }
        if (hiddenInput.getVisibility() != View.GONE)
        {
            lastHiddenInput = "";
            hiddenInput.setText(lastHiddenInput);
            hiddenInput.requestFocus();
            hiddenInput.setTextWatcher(true);
        }
        final LinearLayout hiddenInputPanel = paletteLayout.findViewById(R.id.hidden_edit_text_panel);
        hiddenInputPanel.setVisibility(hiddenInput.getVisibility());
    }

    @Override
    public void beforeTextChanged(String s, boolean isManualInput)
    {
        // empty
    }

    @Override
    public void onTextChanged(String s, boolean isManualInput)
    {
        if (s == null || listChangeIf == null)
        {
            lastHiddenInput = null;
            return;
        }

        if (lastHiddenInput != null && lastHiddenInput.equals(s))
        {
            return;
        }

        lastHiddenInput = s;

        if (ClipboardManager.isFormulaObject(s))
        {
            hiddenInput.setTextWatcher(false);
            listChangeIf.onPalettePressed(s);
            return;
        }

        final String termSep = context.getResources().getString(R.string.formula_term_separator);
        final TermTypeIf term = TermFactory.findTerm(context, null, s, /*ensureManualTrigger=*/ true);
        final String code = (termSep.equals(s)) ? FormulaBase.BaseType.TERM.toString() :
                ((term != null) ? term.getLowerCaseName() : null);
        if (code == null)
        {
            return;
        }

        if (UserFunctions.FunctionType.FUNCTION_LINK.toString().equalsIgnoreCase(code))
        {
            hiddenInput.setTextWatcher(false);
            listChangeIf.onPalettePressed(s);
            return;
        }

        for (PaletteButton b : termButtons)
        {
            if (b.isEnabled() && b.getCode() != null && b.getCode().equalsIgnoreCase(code))
            {
                hiddenInput.setTextWatcher(false);
                listChangeIf.onPalettePressed(b.getCode());
                break;
            }
        }
    }

    @Override
    public void onSizeChanged()
    {
        // empty
    }

    @Override
    public int onGetNextFocusId(CustomEditText owner, FocusChangeIf.NextFocusType focusType)
    {
        return R.id.main_list_view;
    }
}
