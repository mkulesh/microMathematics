/*
 * microMathematics - Extended Visual Calculator
 * Copyright (C) 2014-2021 by Mikhail Kulesh
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details. You should have received a copy of the GNU General
 * Public License along with this program.
 */
package com.mkulesh.micromath.dialogs;

import android.net.Uri;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

import com.mkulesh.micromath.fman.Commander;
import com.mkulesh.micromath.fman.FileUtils;
import com.mkulesh.micromath.R;
import com.mkulesh.micromath.properties.ImageProperties;
import com.mkulesh.micromath.properties.ImagePropertiesChangeIf;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.HorizontalNumberPicker;

public class DialogImageSettings extends DialogBase implements OnLongClickListener
{
    private final AppCompatActivity activity;
    private final ImageProperties parameters;
    private final EditText fileName;
    private final CheckBox cbEmbedded;
    private final HorizontalNumberPicker pickerWidth, pickerHeight;
    private final RadioButton rOriginalSize;
    private final RadioButton rOriginalColor;
    private final ImagePropertiesChangeIf changeIf;

    public DialogImageSettings(AppCompatActivity activity, ImagePropertiesChangeIf changeIf, ImageProperties parameters)
    {
        super(activity, R.layout.dialog_image_settings, R.string.dialog_image_settings_title);
        this.activity = activity;
        this.parameters = parameters;

        fileName = findViewById(R.id.dialog_file_name);
        if (parameters.fileName != null)
        {
            fileName.setText(parameters.fileName);
        }

        ImageButton buttonSelectFile = findViewById(R.id.dialog_button_select_file);
        buttonSelectFile.setOnClickListener(this);
        buttonSelectFile.setOnLongClickListener(this);
        ViewUtils.setImageButtonColorAttr(activity, buttonSelectFile, R.attr.colorDialogContent);

        cbEmbedded = findViewById(R.id.dialog_checkbox_embedded);
        cbEmbedded.setChecked(parameters.embedded);

        pickerWidth = findViewById(R.id.dialog_picker_width);
        pickerWidth.setValue(parameters.width);
        pickerWidth.minValue = 0;
        pickerHeight = findViewById(R.id.dialog_picker_height);
        pickerHeight.setValue(parameters.height);
        pickerHeight.minValue = 0;

        rOriginalSize = findViewById(R.id.dialog_button_original_size);
        rOriginalSize.setOnClickListener(this);
        rOriginalSize.setChecked(parameters.originalSize);
        RadioButton rCustomSize = findViewById(R.id.dialog_button_custom_size);
        rCustomSize.setOnClickListener(this);
        rCustomSize.setChecked(!parameters.originalSize);
        onClick(parameters.originalSize ? rOriginalSize : rCustomSize);

        rOriginalColor = findViewById(R.id.dialog_button_original_color);
        rOriginalColor.setOnClickListener(this);
        rOriginalColor.setChecked(parameters.colorType == ImageProperties.ColorType.ORIGINAL);
        RadioButton rAutoColor = findViewById(R.id.dialog_button_auto_color);
        rAutoColor.setOnClickListener(this);
        rAutoColor.setChecked(parameters.colorType == ImageProperties.ColorType.AUTO);

        this.changeIf = changeIf;
    }

    @Override
    public void onClick(View v)
    {
        boolean isFileChanged = false, isImageChanged = false;
        if (v.getId() == R.id.dialog_button_original_size || v.getId() == R.id.dialog_button_custom_size)
        {
            pickerWidth.setEnabled(v.getId() == R.id.dialog_button_custom_size);
            pickerHeight.setEnabled(v.getId() == R.id.dialog_button_custom_size);
            return;
        }
        else if (v.getId() == R.id.dialog_button_select_file)
        {
            showFileChooserDialog();
            return;
        }
        else if (v.getId() == R.id.dialog_button_original_color || v.getId() == R.id.dialog_button_auto_color)
        {
            return;
        }
        else if (v.getId() == R.id.dialog_button_ok)
        {
            if (changeIf != null)
            {
                final String newName = fileName.getText().toString();
                if (!parameters.fileName.equals(newName) || parameters.embedded != cbEmbedded.isChecked())
                {
                    isFileChanged = true;
                    parameters.fileName = newName;
                    parameters.embedded = cbEmbedded.isChecked();
                }

                if (parameters.originalSize != rOriginalSize.isChecked() || parameters.width != pickerWidth.getValue()
                        || parameters.height != pickerHeight.getValue())
                {
                    isImageChanged = true;
                    parameters.originalSize = rOriginalSize.isChecked();
                    parameters.width = pickerWidth.getValue();
                    parameters.height = pickerHeight.getValue();
                }

                final ImageProperties.ColorType colorType = rOriginalColor.isChecked() ?
                        ImageProperties.ColorType.ORIGINAL : ImageProperties.ColorType.AUTO;
                if (parameters.colorType != colorType)
                {
                    isImageChanged = true;
                    parameters.colorType = colorType;
                }
            }
        }
        if (changeIf != null)
        {
            changeIf.onImagePropertiesChange(isFileChanged, isImageChanged);
        }
        closeDialog();
    }

    private void showFileChooserDialog()
    {

        Commander commander = new Commander(activity, R.string.action_open, Commander.SelectionMode.OPEN,
                activity.getResources().getStringArray(R.array.asset_filter),
                (uri, fileType, adapter) ->
                {
                    uri = FileUtils.ensureScheme(uri);
                    final boolean resolvePath = !FileUtils.isAssetUri(uri) && parameters.parentDirectory != null;
                    if (resolvePath && parameters.parentDirectory.getScheme().equals(uri.getScheme()))
                    {
                        fileName.setText(FileUtils.convertToRelativePath(parameters.parentDirectory, uri));
                    }
                    else
                    {
                        fileName.setText(uri.toString());
                    }
                });
        if (fileName.getText().length() > 0)
        {
            final String uName = fileName.getText().toString();
            final boolean resolvePath = !parameters.isAsset() && parameters.parentDirectory != null;
            final Uri imageUri = resolvePath ? FileUtils.catUri(getContext(), parameters.parentDirectory, uName) : Uri
                    .parse(uName);
            final Uri imageFolder = FileUtils.getParentUri(imageUri);
            if (imageFolder != null)
            {
                commander.Navigate(imageFolder, null);
            }
        }
        commander.show();
    }

    @Override
    public boolean onLongClick(View b)
    {
        return ViewUtils.showButtonDescription(getContext(), b);
    }
}
