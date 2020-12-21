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

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.mkulesh.micromath.R;

public class DialogRadioGroup extends DialogBase
{
    public interface EventHandler
    {
        void onCreate(RadioButton[] radioButtons);

        void onClick(int whichButton);
    }

    private final EventHandler eventHandler;
    private final RadioGroup radioGroup;

    public DialogRadioGroup(Activity context, int titleId, int numButtons, EventHandler eventHandler)
    {
        super(context, R.layout.dialog_radio_group, titleId);
        (findViewById(R.id.dialog_button_panel)).setVisibility(View.GONE);

        this.eventHandler = eventHandler;

        final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        radioGroup = getRootLayout().findViewById(R.id.dialog_radio_group_view);
        for (int i = 0; i < numButtons; i++)
        {
            inflater.inflate(R.layout.dialog_radio_group_item, radioGroup);
        }

        RadioButton[] radioButtons = new RadioButton[numButtons];
        for (int i = 0; i < numButtons; i++)
        {
            radioButtons[i] = (RadioButton) radioGroup.getChildAt(i);
            radioButtons[i].setId(radioGroup.getId() + i + 1);
            radioButtons[i].setOnClickListener(this);
            if (i == 0)
            {
                radioButtons[i].setChecked(true);
            }
        }

        if (eventHandler != null)
        {
            eventHandler.onCreate(radioButtons);
        }
    }

    @Override
    public void onClick(View v)
    {
        final RadioButton selectedButton = findViewById(radioGroup.getCheckedRadioButtonId());
        if (v instanceof RadioButton && eventHandler != null && selectedButton != null)
        {
            eventHandler.onClick(selectedButton.getId() - radioGroup.getId() - 1);
        }
        closeDialog();
    }
}
