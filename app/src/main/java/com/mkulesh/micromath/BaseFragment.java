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
package com.mkulesh.micromath;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mkulesh.micromath.export.Exporter;
import com.mkulesh.micromath.fman.AdapterIf;
import com.mkulesh.micromath.fman.Commander;
import com.mkulesh.micromath.fman.FileType;
import com.mkulesh.micromath.fman.FileUtils;
import com.mkulesh.micromath.formula.FormulaList;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.FloatingButtonsSet;

import java.io.File;

abstract public class BaseFragment extends Fragment implements OnClickListener
{
    /**
     * Constants used to save/restore the instance state.
     */
    public static final String EXTERNAL_URI = "external_uri";
    public static final String POST_ACTION_ID = "post_action_id";
    public static final String FRAGMENT_NUMBER = "fragment_number";
    public static final String OPENED_FILE = "opened_file"; // Not used since version 2.14.3
    public static final String OPENED_URI = "opened_uri";
    public static final String OPENED_FILE_EMPTY = "";
    public static final String FILE_READING_OPERATION = "file_reading_operation";
    public static final String DEVELOPER_MODE = "developer_mode";

    /**
     * Class members.
     */
    public final static int WORKSHEET_FRAGMENT_ID = 0;
    public final static int INVALID_FRAGMENT_ID = -1;
    public final static int INVALID_ACTION_ID = -1;

    protected AppCompatActivity activity = null;
    protected View rootView = null;
    protected FormulaList formulas = null;
    protected int fragmentNumber = INVALID_FRAGMENT_ID;
    private Menu mainMenu = null;
    private boolean inOperation = false;
    private OnClickListener stopHandler = null;
    private FloatingButtonsSet primaryButtonsSet = null, secondaryButtonsSet = null;
    protected SharedPreferences preferences = null;

    /**
     * Abstract interface
     */
    abstract public void performAction(int itemId);

    abstract public void setXmlReadingResult(boolean success);

    public BaseFragment()
    {
        // Empty constructor required for fragment subclasses
    }

    protected void initializeFragment(int number)
    {
        fragmentNumber = number;
        activity = (AppCompatActivity) getActivity();
        formulas = new FormulaList(this, rootView);

        setHasOptionsMenu(true);

        primaryButtonsSet = (FloatingButtonsSet) rootView.findViewById(R.id.main_flb_set_primary);
        secondaryButtonsSet = (FloatingButtonsSet) rootView.findViewById(R.id.main_flb_set_secondary);

        preferences = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        boolean delayedSetInOperationCall = (mainMenu != menu);
        mainMenu = menu;
        if (delayedSetInOperationCall)
        {
            setInOperation(inOperation, stopHandler);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        if (formulas.getXmlLoaderTask() != null)
        {
            outState.putString(FILE_READING_OPERATION, FILE_READING_OPERATION);
            formulas.stopXmlLoaderTask();
        }
        else
        {
            formulas.writeToBundle(outState);
        }
    }

    public Uri getOpenedFile()
    {
        Uri uri = null;
        // clear settings of previous version
        String str = preferences.getString(OPENED_FILE, null);
        if (str != null)
        {
            SharedPreferences.Editor prefEditor = preferences.edit();
            prefEditor.putString(OPENED_FILE, null);
            prefEditor.putString("default_directory", null);
            prefEditor.putString("last_selected_file_type", null);
            prefEditor.commit();
            if (str != null && !str.equals(OPENED_FILE_EMPTY))
            {
                uri = Uri.fromFile(new File(str));
            }
        }
        else
        {
            str = preferences.getString(OPENED_URI, OPENED_FILE_EMPTY);
            uri = str.equals(OPENED_FILE_EMPTY) ? null : Uri.parse(str);
        }
        if (uri != null)
        {
            ViewUtils.Debug(this, "currently opened uri: " + uri.toString());
        }
        return uri;
    }

    protected void setOpenedFile(Uri uri)
    {
        SharedPreferences.Editor prefEditor = preferences.edit();
        prefEditor.putString(OPENED_URI, (uri == null) ? OPENED_FILE_EMPTY : uri.toString());
        prefEditor.commit();
        if (uri == null)
        {
            String[] subtitles = getResources().getStringArray(R.array.activity_subtitles);
            CharSequence subTitle = (fragmentNumber < subtitles.length) ? subtitles[fragmentNumber] : "";
            setWorksheetName(subTitle);
        }
        else
        {
            setWorksheetName(FileUtils.getFileName(activity, uri));
        }
    }

    protected void setWorksheetName(CharSequence name)
    {
        ((MainActivity) activity).setWorksheetName(fragmentNumber, name);
    }

    protected void onSaveFinished()
    {
        // default implementation is empty
    }

    protected void saveFileAs(final boolean storeOpenedFileInfo)
    {
        Commander commander = new Commander(activity, R.string.action_save_as, Commander.SelectionMode.SAVE_AS, null,
                new Commander.OnFileSelectedListener()
                {
                    public void onSelectFile(Uri uri, FileType fileType, final AdapterIf adapter)
                    {
                        uri = FileUtils.ensureScheme(uri);
                        if (formulas.writeToFile(uri))
                        {
                            if (storeOpenedFileInfo)
                            {
                                setOpenedFile(uri);
                            }
                            onSaveFinished();
                        }
                    }
                });
        commander.setFileName(((MainActivity) activity).getWorksheetName());
        commander.show();
    }

    protected void export()
    {
        Commander commander = new Commander(activity, R.string.action_export, Commander.SelectionMode.EXPORT, null,
                new Commander.OnFileSelectedListener()
                {
                    public void onSelectFile(Uri uri, FileType fileType, final AdapterIf adapter)
                    {
                        uri = FileUtils.ensureScheme(uri);
                        formulas.setSelectedFormula(ViewUtils.INVALID_INDEX, false);
                        final boolean res = Exporter.write(formulas, uri, fileType, adapter, null);
                        String mime = null;
                        if (res)
                        {
                            switch (fileType)
                            {
                            case JPEG_IMAGE:
                                mime = "image/jpeg";
                                break;
                            case LATEX:
                                break;
                            case MATHJAX:
                                mime = "text/html";
                                break;
                            case PNG_IMAGE:
                                mime = "image/png";
                                break;
                            }
                        }
                        if (mime != null)
                        {
                            try
                            {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(uri, mime);
                                startActivity(intent);
                            }
                            catch (Exception e)
                            {
                                Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
        commander.show();
    }

    public void calculate()
    {
        formulas.calculate();
    }

    public void setInOperation(boolean inOperation, OnClickListener stopHandler)
    {
        this.inOperation = inOperation;
        this.stopHandler = stopHandler;
        if (mainMenu == null)
        {
            return;
        }

        // update menu items
        for (int i = 0; i < mainMenu.size(); i++)
        {
            MenuItem m = mainMenu.getItem(i);

            if (m.getItemId() == R.id.action_exit)
            {
                continue;
            }

            m.setEnabled(!inOperation);

            // update undo button
            if (m.getItemId() == R.id.action_undo && !inOperation)
            {
                formulas.getUndoState().updateMenuItemState(m);
            }

            // update save button for work-sheet fragment
            if (fragmentNumber == WORKSHEET_FRAGMENT_ID && m.getItemId() == R.id.action_save)
            {
                final String str = preferences.getString(OPENED_URI, OPENED_FILE_EMPTY);
                final Uri uri = str.equals(OPENED_FILE_EMPTY) ? null : Uri.parse(str);
                m.setVisible(!FileUtils.isAssetUri(uri));
            }
            // update buttons background
            ViewUtils.setMenuIconColor(getContext(), m, R.color.micromath_icons);
        }

        // update floating buttons
        if (!inOperation)
        {
            primaryButtonsSet.activate(R.id.main_flb_action_play, this);
        }
        else if (inOperation)
        {
            if (stopHandler != null)
            {
                primaryButtonsSet.activate(R.id.main_flb_action_stop, stopHandler);
            }
            else
            {
                primaryButtonsSet.activate(-1, null);
            }
        }

        // update progress bar
        final ProgressBar progressBar = (ProgressBar) activity.findViewById(R.id.main_progress_bar);
        if (progressBar != null)
        {
            progressBar.setVisibility(inOperation ? View.VISIBLE : View.GONE);
        }
    }

    public boolean isInOperation()
    {
        return inOperation;
    }

    public boolean isFirstStart()
    {
        return !preferences.contains(OPENED_FILE) && !preferences.contains(OPENED_URI);
    }

    public void enableObjectPropertiesButton(boolean flag)
    {
        secondaryButtonsSet.activate(flag ? R.id.main_flb_object_properties : -1, this);
    }

    public void enableObjectDetailsButton(boolean flag)
    {
        if (!isInOperation())
        {
            primaryButtonsSet.activate(flag ? R.id.main_flb_action_details : R.id.main_flb_action_play, this);
        }
    }

    @Override
    public void onClick(View b)
    {
        if (b.getId() == R.id.main_flb_action_play)
        {
            hideKeyboard();
            calculate();
        }
        else if (b.getId() == R.id.main_flb_object_properties)
        {
            hideKeyboard();
            formulas.callObjectManipulator(FormulaList.Manipulator.PROPERTY);
        }
        else if (b.getId() == R.id.main_flb_action_details)
        {
            hideKeyboard();
            formulas.callObjectManipulator(FormulaList.Manipulator.DETAILS);
        }
    }

    public void updateModeTitle()
    {
        android.support.v7.view.ActionMode mode = ((MainActivity) activity).getActionMode();
        if (mode != null)
        {
            final int selected = formulas.getSelectedEquations().size();
            final int total = formulas.getEquationsNumber();
            if (selected == 0)
            {
                mode.setTitle("");
            }
            else
            {
                mode.setTitle(String.valueOf(selected) + "/" + String.valueOf(total));
            }
            Menu m = mode.getMenu();
            if (m != null)
            {
                MenuItem mi = m.findItem(R.id.context_menu_expand);
                if (mi != null)
                {
                    mi.setVisible(total > selected);
                }
            }
        }
    }

    public void hideKeyboard()
    {
        formulas.showSoftKeyboard(false);
    }

    public boolean isDeveloperMode()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            if (preferences.getBoolean(DEVELOPER_MODE, false))
            {
                return true;
            }
        }
        return false;
    }

    public int getFragmentNumber()
    {
        return fragmentNumber;
    }
}
