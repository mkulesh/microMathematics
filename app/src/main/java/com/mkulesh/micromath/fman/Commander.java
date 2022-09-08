/*
 * microMathematics - Extended Visual Calculator
 * Copyright (C) 2014-2022 by Mikhail Kulesh
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
package com.mkulesh.micromath.fman;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.mkulesh.micromath.dialogs.DialogBase;
import com.mkulesh.micromath.dialogs.DialogRadioGroup;
import com.mkulesh.micromath.dialogs.SimpleDialog;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.CompatUtils;
import com.mkulesh.micromath.utils.ViewUtils;

public class Commander extends DialogBase implements CommanderIf
{
    /**
     * Interface definition for a callback to be invoked when a file is selected.
     */
    public interface OnFileSelectedListener
    {
        /**
         * Called when a file has been selected.
         */
        void onSelectFile(Uri uri, FileType fileType, final AdapterIf adapter);
    }

    public static final String PREF_LAST_SELECTED_PATH = "fman_last_selected_path";
    private static final String PREF_LAST_SELECTED_FILE_TYPE = "fman_last_selected_file_type";
    private static final String PREF_ADAPTER_MODE = "fman_adapter_mode";

    private static final int HOME_SCHEMA_H = AdapterHome.ORG_SCHEME.hashCode();
    private static final int FILE_SCHEMA_H = AdapterFileSystem.ORG_SCHEME.hashCode();
    private static final int SAF_SCHEMA_H = AdapterDocuments.ORG_SCHEME.hashCode();
    private static final int ASSETS_SCHEMA_H = AdapterAssets.ORG_SCHEME.hashCode();

    private final OnFileSelectedListener listener;
    private final AppCompatActivity context;
    private final FileListView fileListView;
    private final SelectionMode selectionMode;
    private final CharSequence[] assetFilter;

    private final Button okButton;
    private final EditText fileName;
    private Button fileTypeButton = null;
    private FileType fileType = null;

    private AdapterIf destAdapter = null;

    public Commander(AppCompatActivity context, int nameId, SelectionMode selectionMode, CharSequence[] assetFilter, OnFileSelectedListener listener)
    {
        // Call superclass constructor.
        super(context, R.layout.fman_commander_layout, nameId);

        maximize();

        // set attributes
        this.context = context;
        this.listener = listener;
        fileListView = new FileListView(this);
        fileListView.adapterMode = pref.getInt(PREF_ADAPTER_MODE, 0);
        fileListView.applySettings();

        ImageButton homeButton = findViewById(R.id.fman_action_home);
        homeButton.setOnClickListener(this);
        ViewUtils.setImageButtonColorAttr(context, homeButton, R.attr.colorDialogContent);
        homeButton.setOnLongClickListener(v -> ViewUtils.showButtonDescription(getContext(), v));

        this.selectionMode = selectionMode;
        this.assetFilter = assetFilter;
        okButton = findViewById(R.id.dialog_button_ok);

        // prepare file name field
        fileName = findViewById(R.id.dialog_file_new_name_value);
        fileName.addTextChangedListener(new EditTextWatcher());
        fileName.setText("");

        // set components visibility
        switch (selectionMode)
        {
        case OPEN:
            (findViewById(R.id.dialog_button_panel)).setVisibility(View.GONE);
            (findViewById(R.id.fman_file_type_layout)).setVisibility(View.GONE);
            (findViewById(R.id.dialog_file_new_name_layout)).setVisibility(View.GONE);
            (findViewById(R.id.dialog_content_panel)).setPadding(0, 0, 0, getContext().getResources()
                    .getDimensionPixelSize(R.dimen.dialog_panel_vertical_padding));
            break;
        case SAVE_AS:
        case EXPORT:
            (findViewById(R.id.dialog_button_panel)).setVisibility(View.VISIBLE);
            (findViewById(R.id.fman_file_type_layout))
                    .setVisibility(selectionMode == SelectionMode.EXPORT ? View.VISIBLE : View.GONE);
            (findViewById(R.id.dialog_file_new_name_layout)).setVisibility(View.VISIBLE);
            fileTypeButton = findViewById(R.id.fman_file_type_button);
            fileTypeButton.setOnClickListener(this);
            fileTypeButton.setOnLongClickListener(v -> ViewUtils.showButtonDescription(getContext(), v));
            prepareButtonImage(fileTypeButton);
            fileName.requestFocus();
            break;
        }

        // restore the last selected path and file type
        String lastPath = pref.getString(PREF_LAST_SELECTED_PATH, null);
        if (lastPath != null)
        {
            Navigate(Uri.parse(lastPath), null);
        }
        else
        {
            Navigate(Uri.parse(AdapterHome.DEFAULT_LOC), null);
        }
        try
        {
            fileType = FileType.valueOf(pref.getString(PREF_LAST_SELECTED_FILE_TYPE, null));
        }
        catch (Exception e)
        {
            fileType = FileType.PNG_IMAGE;
        }
        if (fileTypeButton != null)
        {
            fileTypeButton.setText(fileType.getDescriptionId());
        }
    }

    private boolean isFileSelected()
    {
        return getListAdapter() != null && fileName.getText().length() > 0 && getCurrentPath() != null;
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.dialog_button_ok && isFileSelected())
        {
            final String fName = fileName.getText().toString();
            Uri fUri = getListAdapter().getItemUri(fName);
            if (fUri == null)
            {
                fUri = getListAdapter().newFile(fName);

            }
            notifyListeners(fName, fUri);
            return;
        }
        else if (v.getId() == R.id.fman_action_home)
        {
            dispatchCommand(v.getId());
            return;
        }
        else if (v.getId() == R.id.fman_file_type_button)
        {
            final DialogRadioGroup d = new DialogRadioGroup(context, R.string.fman_file_type_selection,
                    FileType.values().length, new DialogRadioGroup.EventHandler()
            {
                public void onCreate(RadioButton[] rb)
                {
                    for (int i = 0; i < rb.length; i++)
                    {
                        rb[i].setText(FileType.values()[i].getDescriptionId());
                        rb[i].setChecked(i == fileType.ordinal());
                    }
                }

                public void onClick(int whichButton)
                {
                    fileType = FileType.values()[whichButton];
                    if (fileTypeButton != null)
                    {
                        fileTypeButton.setText(fileType.getDescriptionId());
                    }
                }
            });
            d.show();
            return;
        }
        closeDialog();
    }

    private AdapterIf getListAdapter()
    {
        return fileListView.getListAdapter();
    }

    public AdapterIf CreateAdapter(Uri uri)
    {
        AdapterIf ca = CreateAdapterInstance(uri);
        if (ca != null)
        {
            ca.Init(this);
        }
        return ca;
    }

    private AdapterIf CreateAdapterInstance(Uri uri)
    {
        String scheme = uri.getScheme();
        if (!FileUtils.str(scheme))
            return new AdapterFileSystem(context);
        final int scheme_h = scheme.hashCode();
        if (ASSETS_SCHEMA_H == scheme_h)
            return selectionMode == SelectionMode.OPEN? new AdapterAssets(context, assetFilter) : new AdapterHome(context);
        if (FILE_SCHEMA_H == scheme_h)
            return new AdapterFileSystem(context);
        if (HOME_SCHEMA_H == scheme_h)
            return new AdapterHome(context);
        if (SAF_SCHEMA_H == scheme_h && AdapterDocuments.isExternalStorageDocument(uri))
            return new AdapterDocuments(context);
        return new AdapterFileSystem(context);
    }

    private void changeSorting(int sort_mode)
    {
        AdapterIf ca = getListAdapter();

        int cur_mode = ca.setMode(0, 0);
        boolean asc = (cur_mode & AdapterIf.MODE_SORT_DIR) == AdapterIf.SORT_ASC;
        int sorted = cur_mode & AdapterIf.MODE_SORTING;

        if (sorted == sort_mode)
            ca.setMode(AdapterIf.MODE_SORT_DIR, asc ? AdapterIf.SORT_DSC : AdapterIf.SORT_ASC);
        else
            ca.setMode(AdapterIf.MODE_SORTING | AdapterIf.MODE_SORT_DIR, sort_mode | AdapterIf.SORT_ASC);

        fileListView.adapterMode = ca.getMode() & (AdapterIf.MODE_SORTING | AdapterIf.MODE_SORT_DIR);
    }

    private void NavigateInternal(Uri uri, String posTo)
    {
        fileListView.Navigate(uri, posTo);
        okButton.setEnabled(isFileSelected());
    }

    private void operationFinished()
    {
        if (null != destAdapter)
        {
            destAdapter = null;
        }
    }

    private void dispatchCommand(int id)
    {
        try
        {
            final int selectedPos = fileListView.getSelected();
            final String selectedItem = selectedPos < 0 ? null : getListAdapter().getItemName(selectedPos, false);

            switch (id)
            {
            case R.id.fman_action_rename:
            {

                if (selectedPos < 0 || selectedItem == null)
                {
                    showMessage(context.getString(R.string.fman_error_no_items));
                    return;
                }
                final SimpleDialog d = new SimpleDialog(context, R.layout.fman_input_dialog,
                        R.string.fman_rename_title, new SimpleDialog.EventHandler()
                {
                    public void onCreate(SimpleDialog d, LinearLayout dLayout)
                    {
                        final TextView prompt = dLayout.findViewById(R.id.fman_input_dialog_prompt);
                        prompt.setText(context.getString(R.string.fman_rename_dialog_prompt, selectedItem));
                        final EditText edit = dLayout.findViewById(R.id.fman_input_dialog_edit_field);
                        edit.setText(selectedItem);
                        edit.requestFocus();
                    }

                    public void onClick(LinearLayout dLayout, int whichButton)
                    {
                        if (whichButton == R.id.dialog_button_ok)
                        {
                            ViewUtils.Debug(Commander.this, "Renaming item " + selectedItem);
                            final EditText edit = dLayout.findViewById(R.id.fman_input_dialog_edit_field);
                            final String new_name = edit.getText().toString();
                            getListAdapter().renameItem(selectedPos, new_name);
                            fileListView.setSelection(new_name);
                        }
                    }
                });
                d.show();
                break;
            }
            case R.id.fman_action_delete:
            {
                if (selectedPos < 0 || selectedItem == null)
                {
                    showMessage(context.getString(R.string.fman_error_no_items));
                    return;
                }
                final SimpleDialog d = new SimpleDialog(context, R.layout.fman_message_dialog,
                        R.string.fman_delete_title, new SimpleDialog.EventHandler()
                {
                    public void onCreate(SimpleDialog d, LinearLayout dLayout)
                    {
                        final ImageView image = dLayout.findViewById(R.id.fman_message_dialog_icon);
                        d.setImage(image, R.drawable.ic_action_content_discard,
                                CompatUtils.getThemeColorAttr(context, R.attr.colorDialogContent));
                        final TextView prompt = dLayout.findViewById(R.id.fman_message_dialog_prompt);
                        prompt.setText(context.getString(R.string.fman_delete_dialog_prompt, selectedItem));
                    }

                    public void onClick(LinearLayout dLayout, int whichButton)
                    {
                        if (whichButton == R.id.dialog_button_ok)
                        {
                            ViewUtils.Debug(Commander.this, "Deleting item " + selectedItem);
                            if (getListAdapter().deleteItem(selectedPos))
                            {
                                fileListView.listView.clearChoices();
                            }
                        }
                    }
                });
                d.show();
                break;
            }
            case R.id.fman_action_create_folder:
                final SimpleDialog d = new SimpleDialog(context, R.layout.fman_input_dialog,
                        R.string.fman_create_folder_title, new SimpleDialog.EventHandler()
                {
                    public void onCreate(SimpleDialog d, LinearLayout dLayout)
                    {
                        final TextView prompt = dLayout.findViewById(R.id.fman_input_dialog_prompt);
                        prompt.setText(context.getString(R.string.fman_create_folder_dialog_prompt));
                        final EditText edit = dLayout.findViewById(R.id.fman_input_dialog_edit_field);
                        edit.setText("");
                        edit.requestFocus();
                    }

                    public void onClick(LinearLayout dLayout, int whichButton)
                    {
                        if (whichButton == R.id.dialog_button_ok)
                        {
                            final EditText edit = dLayout.findViewById(R.id.fman_input_dialog_edit_field);
                            final String new_name = edit.getText().toString();
                            getListAdapter().createFolder(new_name);
                            fileListView.setSelection(new_name);
                        }
                    }
                });
                d.show();
                break;
            case R.id.fman_action_home:
                Navigate(Uri.parse(AdapterHome.DEFAULT_LOC), null);
                break;
            case R.id.fman_action_sort_by_name:
                changeSorting(AdapterIf.SORT_NAME);
                break;
            case R.id.fman_action_sort_by_ext:
                changeSorting(AdapterIf.SORT_EXT);
                break;
            case R.id.fman_action_sort_by_size:
                changeSorting(AdapterIf.SORT_SIZE);
                break;
            case R.id.fman_action_sort_by_date:
                changeSorting(AdapterIf.SORT_DATE);
                break;
            case R.id.fman_action_refresh:
                fileListView.refreshList(null);
                break;
            default:
                getListAdapter().doIt(id, selectedPos);
                break;
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    /*
     * Context menu handling
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
    {
        try
        {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            if (info != null)
            {
                fileListView.setSelection(info.position, 0);
            }
            menu.setHeaderTitle(R.string.fman_operation);
            AdapterIf ca = getListAdapter();
            ca.populateContextMenu(menu, info, fileListView.getSelected());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, @NonNull MenuItem item)
    {
        if (featureId != Window.FEATURE_CONTEXT_MENU)
        {
            return false;
        }
        try
        {
            AdapterView.AdapterContextMenuInfo info;
            info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            if (info == null)
                return false;
            fileListView.setSelection(info.position, 0);
            dispatchCommand(item.getItemId());
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    /*
     * Commander interface implementation
     */
    @Override
    public void issue(Intent in, int ret)
    {
        if (in == null)
            return;
        try
        {
            if (ret == 0)
                context.startActivity(in);
            else
                context.startActivityForResult(in, ret);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void showError(final String errMsg)
    {
        try
        {
            final SimpleDialog d = new SimpleDialog(context, R.layout.fman_message_dialog,
                    R.string.fman_warning_dialog_title, new SimpleDialog.EventHandler()
            {
                public void onCreate(SimpleDialog d, LinearLayout dLayout)
                {
                    d.disableButton(R.id.dialog_button_ok);
                    final TextView prompt = dLayout.findViewById(R.id.fman_message_dialog_prompt);
                    prompt.setText(errMsg);
                }

                public void onClick(LinearLayout dLayout, int whichButton)
                {
                    // nothing to do
                }
            });
            d.show();
            return;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        showMessage(errMsg);
    }

    @Override
    public void Navigate(Uri uri, String posTo)
    {
        if (uri != null)
        {
            ViewUtils.Debug(this, "External navigation request: uri = " + uri.toString());
            NavigateInternal(uri, posTo);
        }
    }

    @Override
    public void Open(Uri uri)
    {
        if (uri != null && listener != null)
        {
            final int selectedPos = fileListView.getSelected();
            final String selectedItem = selectedPos < 0 ? null : getListAdapter().getItemName(selectedPos, false);
            notifyListeners(selectedItem, uri);
        }
    }

    @Override
    public void notifyMe(Message progress)
    {
        String string = null;
        try
        {
            if (progress.obj != null)
            {
                if (progress.obj instanceof Bundle)
                    string = ((Bundle) progress.obj).getString(MESSAGE_STRING);
                else if (progress.obj instanceof String)
                {
                    string = (String) progress.obj;
                }
            }
            Bundle b = progress.getData();
            String cookie = b != null ? b.getString(NOTIFY_COOKIE) : null;

            if (progress.what == OPERATION_IN_PROGRESS)
            {
                return;
            }

            operationFinished();
            switch (progress.what)
            {
            case OPERATION_FAILED:
            case OPERATION_FAILED_REFRESH_REQUIRED:
                if (FileUtils.str(string))
                    showError(string);
                if (progress.what == OPERATION_FAILED_REFRESH_REQUIRED)
                {
                    String posto = b != null ? b.getString(NOTIFY_POSTO) : null;
                    fileListView.refreshList(posto);
                }
                else
                {
                    fileListView.askRedrawList();
                }
                return;
            case OPERATION_COMPLETED_REFRESH_REQUIRED:
                String posto = b != null ? b.getString(NOTIFY_POSTO) : null;
                Uri uri = b != null ? (Uri) b.getParcelable(NOTIFY_URI) : null;
                if (uri != null)
                {
                    Navigate(uri, posto);
                    break;
                }
                fileListView.refreshList(posto);
                break;
            case OPERATION_COMPLETED:
                if (FileUtils.str(cookie))
                {
                    String item_name = cookie.substring(1);
                    fileListView.recoverAfterRefresh(item_name);
                }
                else
                {
                    fileListView.recoverAfterRefresh(null);
                }
                break;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void showMessage(String s)
    {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show();
    }

    /**
     * Text change processing class
     */
    private class EditTextWatcher implements TextWatcher
    {
        EditTextWatcher()
        {
            // empty
        }

        @Override
        public void afterTextChanged(Editable s)
        {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            okButton.setEnabled(s.length() > 0 && getCurrentPath() != null);
        }
    }

    private String getCurrentPath()
    {
        AdapterIf ca = getListAdapter();
        if (ca != null && !(ca instanceof AdapterHome))
        {
            return ca.getUri().getPath();
        }
        return null;
    }

    private void notifyListeners(final String fileName, final Uri uri)
    {
        final AdapterIf ca = getListAdapter();
        if (ca == null || listener == null || fileName == null || uri == null)
        {
            return;
        }
        // Verify if a confirmation dialog must be show.
        if (selectionMode != SelectionMode.OPEN)
        {
            if (ca.getItemUri(fileName) != null)
            {
                final SimpleDialog d = new SimpleDialog(context, R.layout.fman_message_dialog,
                        R.string.fman_warning_dialog_title, new SimpleDialog.EventHandler()
                {
                    public void onCreate(SimpleDialog d, LinearLayout dLayout)
                    {
                        final ImageView image = dLayout.findViewById(R.id.fman_message_dialog_icon);
                        d.setImage(image, R.drawable.ic_action_content_save,
                                CompatUtils.getThemeColorAttr(context, R.attr.colorDialogContent));
                        final TextView prompt = dLayout.findViewById(R.id.fman_message_dialog_prompt);
                        prompt.setText(context.getString(R.string.fman_overwrite_file, fileName));
                    }

                    public void onClick(LinearLayout dLayout, int whichButton)
                    {
                        if (whichButton == R.id.dialog_button_ok)
                        {
                            storePreferences();
                            closeDialog();
                            listener.onSelectFile(uri, fileType, ca);
                        }
                    }
                });
                d.show();
            }
            else
            {
                storePreferences();
                closeDialog();
                listener.onSelectFile(uri, fileType, ca);
            }
        }
        else
        {
            storePreferences();
            closeDialog();
            listener.onSelectFile(uri, fileType, ca);
        }
    }

    private void storePreferences()
    {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(PREF_LAST_SELECTED_PATH, getListAdapter().getUri().toString());
        editor.putString(PREF_LAST_SELECTED_FILE_TYPE, fileType.toString());
        editor.putInt(PREF_ADAPTER_MODE, fileListView.adapterMode);
        editor.commit();
    }

    public SelectionMode getSelectionMode()
    {
        return selectionMode;
    }

    public void setFileName(CharSequence name)
    {
        fileName.setText(name);
    }
}
