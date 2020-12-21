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
package com.mkulesh.micromath;

import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mkulesh.micromath.dialogs.DialogDocumentSettings;
import com.mkulesh.micromath.dialogs.DialogNewFormula;
import com.mkulesh.micromath.io.XmlLoaderTask;
import com.mkulesh.micromath.R;
import com.mkulesh.micromath.utils.ViewUtils;

public class MainFragmentAsset extends BaseFragment
{
    public MainFragmentAsset()
    {
        // Empty constructor required for fragment subclasses
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        rootView = inflater.inflate(R.layout.activity_fragment, container, false);
        initializeFragment(getArguments().getInt(FRAGMENT_NUMBER));
        initializeFormula(savedInstanceState);
        return rootView;
    }

    private void initializeFormula(Bundle savedInstanceState)
    {
        if (savedInstanceState != null)
        {
            try
            {
                if (savedInstanceState.getString(FILE_READING_OPERATION) != null)
                {
                    throw new Exception("state is saved before a reading operation is finished");
                }
                formulas.readFromBundle(savedInstanceState);
            }
            catch (Exception e)
            {
                ViewUtils.Debug(this, "cannot restore state: " + e.getLocalizedMessage());
                formulas.clear();
                openAsset();
            }
        }
        else
        {
            openAsset();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        ((MainActivity) activity).updateFragmentInfo(this);
    }

    /*--------------------------------------------------------*
     * File handling
     *--------------------------------------------------------*/

    private void openAsset()
    {
        TypedArray resources = getResources().obtainTypedArray(R.array.activity_resources);
        if (fragmentNumber < resources.length() && fragmentNumber >= 0)
        {
            Uri resource = Uri.parse(resources.getString(fragmentNumber));
            formulas.readFromResource(resource, XmlLoaderTask.PostAction.CALCULATE);
        }
        resources.recycle();
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
            ((MainActivity) activity).selectWorksheet(R.id.action_new_document);
            break;
        case R.id.action_save_as:
            saveFileAs(/* storeOpenedFileInfo= */true);
            break;
        case R.id.action_export:
            export();
            break;
        }
    }

    @Override
    protected void onSaveFinished()
    {
        super.onSaveFinished();
        ((MainActivity) activity).selectWorksheet(INVALID_ACTION_ID);
    }

    @Override
    public void setXmlReadingResult(boolean success)
    {
        // nothing to do
    }
}
