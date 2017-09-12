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

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mkulesh.micromath.dialogs.DialogDocumentSettings;
import com.mkulesh.micromath.dialogs.DialogNewFormula;
import com.mkulesh.micromath.fman.AdapterIf;
import com.mkulesh.micromath.fman.Commander;
import com.mkulesh.micromath.fman.FileType;
import com.mkulesh.micromath.fman.FileUtils;
import com.mkulesh.micromath.formula.XmlLoaderTask;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.ta.TestSession;
import com.mkulesh.micromath.utils.CompatUtils;
import com.mkulesh.micromath.utils.ViewUtils;

import java.io.File;

public class MainFragmentWorksheet extends BaseFragment
{
    public static final String AUTOSAVE_FILE_NAME = "autosave.mmt";

    private Uri externalUri = null;
    protected boolean invalidateFile = false;
    private int postActionId = INVALID_ACTION_ID;
    private CharSequence[] assetFilter = null;

    public MainFragmentWorksheet()
    {
        // Empty constructor required for fragment subclasses
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.activity_fragment, container, false);
        externalUri = getArguments().getParcelable(EXTERNAL_URI);
        postActionId = getArguments().getInt(POST_ACTION_ID, INVALID_ACTION_ID);
        initializeFragment(WORKSHEET_FRAGMENT_ID);
        initializeFormula(savedInstanceState);
        initializeAssets(activity.getResources().getStringArray(R.array.asset_filter));
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(R.id.action_open).setVisible(true);
        menu.findItem(R.id.action_save).setVisible(true);
        menu.findItem(R.id.action_dev_mode).setVisible(isDeveloperMode());
    }

    @Override
    public void onPause()
    {
        saveFile(false);
        super.onPause();
    }

    @Override
    public void onResume()
    {
        if (invalidateFile)
        {
            setXmlReadingResult(false);
        }
        else if (((MainActivity) activity).getWorksheetName() != null)
        {
            setWorksheetName(((MainActivity) activity).getWorksheetName());
        }
        if (postActionId != INVALID_ACTION_ID)
        {
            performAction(postActionId);
            postActionId = INVALID_ACTION_ID;
        }
        super.onResume();
    }

    /*********************************************************
     * File handling
     *********************************************************/

    private void initializeAssets(String[] stringArray)
    {
        assetFilter = new CharSequence[isDeveloperMode() ? stringArray.length + 1 : stringArray.length];
        for (int i = 0; i < stringArray.length; i++)
        {
            assetFilter[i] = stringArray[i];
        }
        if (isDeveloperMode())
        {
            assetFilter[stringArray.length] = getResources().getString(R.string.autotest_directory);
        }
    }

    private void initializeFormula(Bundle savedInstanceState)
    {
        if (savedInstanceState != null && savedInstanceState.getString(FILE_READING_OPERATION) != null)
        {
            ViewUtils.Debug(this, "cannot restore state: state is saved before a reading operation is finished");
            savedInstanceState = null;
        }
        if (isFirstStart())
        {
            if (TestSession.isAutotestOnStart(activity))
            {
                if (((MainActivity) activity).checkStoragePermission(R.id.action_dev_autotest))
                {
                    performAction(R.id.action_dev_autotest);
                }
            }
            else
            {
                Uri resource = Uri.parse(getResources().getString(R.string.activity_welcome));
                formulas.readFromResource(resource, XmlLoaderTask.PostAction.CALCULATE);
            }
        }
        else if (postActionId != INVALID_ACTION_ID)
        {
            setOpenedFile(null);
        }
        else if (savedInstanceState != null)
        {
            try
            {
                formulas.readFromBundle(savedInstanceState);
            }
            catch (Exception e)
            {
                ViewUtils.Debug(this, "cannot restore state: " + e.getLocalizedMessage());
                formulas.clear();
                invalidateFile = true;
            }
        }
        else if (externalUri != null)
        {
            ViewUtils.Debug(this, "external uri is passed: " + externalUri.toString());
            if (formulas.readFromFile(externalUri))
            {
                setOpenedFile(externalUri);
            }
        }
        else
        {
            Uri uri = getOpenedFile();
            if (uri != null)
            {
                if (formulas.readFromFile(uri))
                {
                    setWorksheetName(FileUtils.getFileName(activity, uri));
                }
                else
                {
                    setOpenedFile(null);
                }
            }
        }
    }

    private void saveFile(boolean manualSave)
    {
        if (!manualSave)
        {
            if (formulas.getXmlLoaderTask() != null)
            {
                return;
            }
        }
        Uri uri = getOpenedFile();
        if (uri != null)
        {
            if (FileUtils.isAssetUri(uri))
            {
                // no need to save an asset: just ignore this case
            }
            else
            {
                formulas.writeToFile(uri);
            }
        }
        else if (manualSave)
        {
            saveFileAs(/* storeOpenedFileInfo= */true);
        }
        else
        {
            File file = new File(getActivity().getExternalFilesDir(null), AUTOSAVE_FILE_NAME);
            if (file != null)
            {
                uri = Uri.fromFile(file);
                if (formulas.writeToFile(uri))
                {
                    setOpenedFile(uri);
                }
            }
        }
    }

    public void openFile()
    {
        Commander commander = new Commander(activity, R.string.action_open, Commander.SelectionMode.OPEN, assetFilter,
                new Commander.OnFileSelectedListener()
                {
                    public void onSelectFile(Uri uri, FileType fileType, final AdapterIf adapter)
                    {
                        saveFile(false);
                        uri = FileUtils.ensureScheme(uri);
                        if (formulas.readFromFile(uri))
                        {
                            setOpenedFile(uri);
                        }
                        else
                        {
                            setOpenedFile(null);
                        }
                    }
                });
        commander.show();
    }

    @Override
    public void performAction(int itemId)
    {
        switch (itemId)
        {
        case R.id.action_undo:
            formulas.undo();
            break;
        case R.id.action_new:
            DialogNewFormula d1 = new DialogNewFormula(activity, formulas);
            d1.show();
            break;
        case R.id.action_discard:
            formulas.onDiscardFormula(formulas.getSelectedFormulaId());
            break;
        case R.id.action_document_settings:
            DialogDocumentSettings d2 = new DialogDocumentSettings(getActivity(), formulas,
                    formulas.getDocumentSettings());
            d2.show();
            break;
        case R.id.action_new_document:
            if (postActionId != R.id.action_new_document)
            {
                // postActionId == R.id.action_new_document means that the fragment is called from an
                // asset and we do not need to save anything
                saveFile(false);
            }
            formulas.clear();
            setOpenedFile(null);
            break;
        case R.id.action_open:
            openFile();
            break;
        case R.id.action_save:
            saveFile(true);
            break;
        case R.id.action_save_as:
            saveFileAs(/* storeOpenedFileInfo= */true);
            break;
        case R.id.action_export:
            export();
            break;
        case R.id.action_dev_autotest:
        {
            TestSession at = new TestSession(formulas, TestSession.Mode.TEST_SCRIPS);
            CompatUtils.executeAsyncTask(at);
            break;
        }
        case R.id.action_dev_export_doc:
        {
            TestSession at = new TestSession(formulas, TestSession.Mode.EXPORT_DOC);
            CompatUtils.executeAsyncTask(at);
            break;
        }
        case R.id.action_dev_take_screenshot:
        {
            TestSession at = new TestSession(formulas, TestSession.Mode.TAKE_SCREENSHOTS);
            CompatUtils.executeAsyncTask(at);
            break;
        }
        }
    }

    @Override
    public void setXmlReadingResult(boolean success)
    {
        if (!success)
        {
            setOpenedFile(null);
            externalUri = null;
            invalidateFile = false;
        }
    }

}
