/*******************************************************************************
 * micro Mathematics - Extended visual calculator
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
package com.mkulesh.micromath.dialogs;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mkulesh.micromath.plus.R;

/**
 * A file chooser implemented in a Dialog.
 */
public class DialogLicenses extends DialogBase
{
    private int clickNumber = 0;
    private final String developerModeKey;

    public DialogLicenses(Context context, String developerModeKey)
    {
        super(context, R.layout.dialog_licenses, R.string.action_licenses);
        this.developerModeKey = developerModeKey;

        // Maximize the dialog.
        maximize();
        ((LinearLayout) findViewById(R.id.dialog_button_panel)).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.text_view_developer_mode)).setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.text_view_developer_mode)
        {
            clickNumber++;
            if (clickNumber > 9)
            {
                String message = getContext().getResources().getString(R.string.message_developer_mode);
                Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                SharedPreferences.Editor prefEditor = pref.edit();
                prefEditor.putBoolean(developerModeKey, true);
                prefEditor.commit();
            }
            return;
        }
        super.onClick(v);
    }
}
