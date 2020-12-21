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
import android.view.View;
import android.widget.CheckBox;

import androidx.appcompat.widget.AppCompatEditText;

import com.mkulesh.micromath.R;
import com.mkulesh.micromath.properties.DocumentProperties;
import com.mkulesh.micromath.properties.DocumentPropertiesChangeIf;
import com.mkulesh.micromath.widgets.HorizontalNumberPicker;

public class DialogDocumentSettings extends DialogBase
{
    private final DocumentPropertiesChangeIf changeIf;
    private final DocumentProperties documentSettings;
    private final AppCompatEditText author, title, description;
    private final HorizontalNumberPicker textWidthPicker, significantDigitsPicker;
    private final CheckBox reformatBox, redefineAllowed;

    public DialogDocumentSettings(Activity context, DocumentPropertiesChangeIf changeIf,
                                  DocumentProperties documentSettings)
    {
        super(context, R.layout.dialog_document_settings, R.string.action_document_settings);

        this.changeIf = changeIf;
        this.documentSettings = documentSettings;

        author = findViewById(R.id.dialog_text_document_author);
        author.setText(documentSettings.author);
        title = findViewById(R.id.dialog_text_document_title);
        title.setText(documentSettings.title);
        description = findViewById(R.id.dialog_text_document_description);
        description.setText(documentSettings.description);

        reformatBox = findViewById(R.id.dialog_checkbox_reformat);
        reformatBox.setOnClickListener(this);
        reformatBox.setChecked(documentSettings.reformat);

        textWidthPicker = findViewById(R.id.dialog_text_width_picker);
        textWidthPicker.setValue(documentSettings.textWidth);
        textWidthPicker.minValue = 1;
        textWidthPicker.setEnabled(reformatBox.isChecked());

        significantDigitsPicker = findViewById(R.id.dialog_text_significant_digits);
        significantDigitsPicker.setValue(documentSettings.significantDigits);
        final int[] significantDigitsLimit = context.getResources().getIntArray(R.array.significant_digits_limit);
        if (significantDigitsLimit.length > 1)
        {
            significantDigitsPicker.minValue = significantDigitsLimit[0];
            significantDigitsPicker.maxValue = significantDigitsLimit[1];
        }

        redefineAllowed = findViewById(R.id.dialog_checkbox_redefine_allowed);
        redefineAllowed.setChecked(documentSettings.redefineAllowed);
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.dialog_checkbox_reformat)
        {
            if (textWidthPicker != null)
            {
                textWidthPicker.setEnabled(reformatBox.isChecked());
            }
            return;
        }
        if (v.getId() == R.id.dialog_button_ok)
        {
            if (changeIf != null && documentSettings != null)
            {
                boolean isChanged = false;
                documentSettings.author = author.getText().toString();
                documentSettings.title = title.getText().toString();
                documentSettings.description = description.getText().toString();
                if (documentSettings.reformat != reformatBox.isChecked())
                {
                    documentSettings.reformat = reformatBox.isChecked();
                    isChanged = true;
                }
                if (documentSettings.textWidth != textWidthPicker.getValue())
                {
                    documentSettings.textWidth = textWidthPicker.getValue();
                    isChanged = true;
                }
                if (documentSettings.significantDigits != significantDigitsPicker.getValue())
                {
                    documentSettings.significantDigits = significantDigitsPicker.getValue();
                    isChanged = true;
                }
                if (documentSettings.redefineAllowed != redefineAllowed.isChecked())
                {
                    documentSettings.redefineAllowed = redefineAllowed.isChecked();
                    isChanged = true;
                }
                changeIf.onDocumentPropertiesChange(isChanged);
            }
        }
        closeDialog();
    }

}
