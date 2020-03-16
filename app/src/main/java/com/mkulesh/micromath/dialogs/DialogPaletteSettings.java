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
package com.mkulesh.micromath.dialogs;

import android.content.Context;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageButton;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.mkulesh.micromath.formula.FormulaBase;
import com.mkulesh.micromath.formula.PaletteButton;
import com.mkulesh.micromath.formula.terms.TermFactory;
import com.mkulesh.micromath.formula.terms.TermTypeIf;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.PaletteSettingsChangeIf;
import com.mkulesh.micromath.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

public class DialogPaletteSettings extends DialogBase implements View.OnLongClickListener
{
    final PaletteSettingsChangeIf changeIf;
    final List<TermTypeIf.GroupType> groups;
    final LinearLayout paletteView;

    public DialogPaletteSettings(Context context, PaletteSettingsChangeIf changeIf, List<String> visibleGroups)
    {
        super(context, R.layout.dialog_palette_settings, R.string.dialog_palette_settings_title);
        this.changeIf = changeIf;
        this.groups = TermFactory.collectPaletteGroups();

        final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        paletteView = getRootLayout().findViewById(R.id.dialog_palette_view);

        // Base elements
        {
            inflater.inflate(R.layout.dialog_palette_settings_item, paletteView);
            final LinearLayout itemLayout = (LinearLayout) paletteView.getChildAt(paletteView.getChildCount() - 1);
            final LinearLayout buttonLayout = itemLayout.findViewById(R.id.dialog_palette_settings_buttons);
            final List<PaletteButton> termButtons = new ArrayList<>();
            FormulaBase.addToPalette(context, termButtons);
            for (PaletteButton b : termButtons)
            {
                buttonLayout.addView(b);
            }
            prepareGroup(context, itemLayout, FormulaBase.class.getSimpleName(), visibleGroups);
        }

        // Term elements
        for (TermTypeIf.GroupType g : groups)
        {
            inflater.inflate(R.layout.dialog_palette_settings_item, paletteView);
            final LinearLayout itemLayout = (LinearLayout) paletteView.getChildAt(paletteView.getChildCount() - 1);
            final LinearLayout buttonLayout = itemLayout.findViewById(R.id.dialog_palette_settings_buttons);
            final List<PaletteButton> termButtons = new ArrayList<>();
            TermFactory.addToPalette(context, termButtons, true, g);
            for (PaletteButton b : termButtons)
            {
                buttonLayout.addView(b);
            }
            prepareGroup(context, itemLayout, g.toString(), visibleGroups);
        }
    }

    private void prepareGroup(Context context, LinearLayout itemLayout, String s, List<String> visibleGroups)
    {
        final AppCompatCheckBox cb = itemLayout.findViewById(R.id.dialog_palette_settings_checkbox);
        cb.setTag(s);
        cb.setChecked(visibleGroups.contains(s));
        final LinearLayout buttonLayout = itemLayout.findViewById(R.id.dialog_palette_settings_buttons);
        for (int i = 0; i < buttonLayout.getChildCount(); i++)
        {
            if (buttonLayout.getChildAt(i) instanceof AppCompatImageButton)
            {
                final AppCompatImageButton b = (AppCompatImageButton) buttonLayout.getChildAt(i);
                b.setOnLongClickListener(this);
                ViewUtils.setImageButtonColorAttr(context, b, R.attr.colorDialogContent);
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        boolean isChanged = false;
        if (v.getId() == R.id.dialog_button_ok)
        {
            List<String> visibleGroups = new ArrayList<>();
            for (int i = 0; i < paletteView.getChildCount(); i++)
            {
                final LinearLayout itemLayout = (LinearLayout) paletteView.getChildAt(i);
                final AppCompatCheckBox cb = itemLayout.findViewById(R.id.dialog_palette_settings_checkbox);
                if (cb.isChecked())
                {
                    visibleGroups.add(cb.getTag().toString());
                }
            }
            changeIf.onPaletteVisibleChange(visibleGroups);
        }
        closeDialog();
    }

    @Override
    public boolean onLongClick(View b)
    {
        return ViewUtils.showButtonDescription(getContext(), b);
    }
}
