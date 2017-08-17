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
package com.mkulesh.micromath.dialogs;

import android.app.Activity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;

import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.TextProperties;
import com.mkulesh.micromath.properties.TextProperties.TextStyle;
import com.mkulesh.micromath.properties.TextPropertiesChangeIf;

public class DialogTextSettings extends DialogBase
{
    private final TextPropertiesChangeIf changeIf;
    private final TextProperties parameters;
    private final RadioButton[] rButtons = new RadioButton[TextStyle.values().length];
    private final CheckBox cbNumbering;

    public DialogTextSettings(Activity context, TextPropertiesChangeIf changeIf, TextProperties parameters)
    {
        super(context, R.layout.dialog_text_settings, R.string.dialog_text_settings_title);
        this.changeIf = changeIf;
        this.parameters = parameters;

        rButtons[TextStyle.CHAPTER.ordinal()] = (RadioButton) findViewById(R.id.dialog_text_style_chapter);
        rButtons[TextStyle.SECTION.ordinal()] = (RadioButton) findViewById(R.id.dialog_text_style_section);
        rButtons[TextStyle.SUBSECTION.ordinal()] = (RadioButton) findViewById(R.id.dialog_text_style_subsection);
        rButtons[TextStyle.SUBSUBSECTION.ordinal()] = (RadioButton) findViewById(R.id.dialog_text_style_subsubsection);
        rButtons[TextStyle.TEXT_BODY.ordinal()] = (RadioButton) findViewById(R.id.dialog_text_style_text_body);

        for (int i = 0; i < rButtons.length; i++)
        {
            rButtons[i].setChecked(TextStyle.values()[i] == parameters.textStyle);
            rButtons[i].setOnClickListener(this);
        }

        cbNumbering = (CheckBox) findViewById(R.id.dialog_text_style_numbering);
        cbNumbering.setChecked(parameters.numbering);
    }

    @Override
    public void onClick(View v)
    {
        boolean isChanged = false;
        if (v instanceof RadioButton)
        {
            for (int i = 0; i < rButtons.length; i++)
            {
                rButtons[i].setChecked(rButtons[i] == v);
            }
            return;
        }
        else if (v.getId() == R.id.dialog_button_ok && changeIf != null)
        {
            TextStyle textStyle = TextStyle.TEXT_BODY;
            for (int i = 0; i < rButtons.length; i++)
            {
                if (rButtons[i].isChecked())
                {
                    textStyle = TextStyle.values()[i];
                    break;
                }
            }
            if (parameters.textStyle != textStyle)
            {
                isChanged = true;
                parameters.textStyle = textStyle;
            }
            if (parameters.numbering != cbNumbering.isChecked())
            {
                isChanged = true;
                parameters.numbering = cbNumbering.isChecked();
            }
        }
        changeIf.onTextPropertiesChange(isChanged);
        closeDialog();
    }
}
