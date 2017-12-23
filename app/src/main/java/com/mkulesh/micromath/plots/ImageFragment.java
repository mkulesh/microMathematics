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
package com.mkulesh.micromath.plots;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import com.mkulesh.micromath.dialogs.DialogImageSettings;
import com.mkulesh.micromath.formula.FormulaBase;
import com.mkulesh.micromath.formula.FormulaList;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.ImageProperties;
import com.mkulesh.micromath.properties.ImagePropertiesChangeIf;
import com.mkulesh.micromath.undo.FormulaState;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CustomImageView;
import com.mkulesh.micromath.widgets.CustomLayout;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.util.ArrayList;

public class ImageFragment extends FormulaBase implements ImagePropertiesChangeIf
{
    /*
     * Constants used to save/restore the instance state.
     */
    private static final String STATE_IMAGE_PARAMETERS = "image_parameters";
    private static final String STATE_IMAGE_VIEW = "image_view";

    private CustomImageView imageView = null;
    private final ImageProperties parameters = new ImageProperties();

    // undo
    private FormulaState formulaState = null;

    /*********************************************************
     * Constructors
     *********************************************************/

    public ImageFragment(FormulaList formulaList, int id)
    {
        super(formulaList, null, 0);
        setId(id);
        onCreate();
    }

    /*********************************************************
     * GUI constructors to avoid lint warning
     *********************************************************/

    public ImageFragment(Context context)
    {
        super(null, null, 0);
    }

    public ImageFragment(Context context, AttributeSet attrs)
    {
        super(null, null, 0);
    }

    /*********************************************************
     * Re-implementation for methods for Object superclass
     *********************************************************/

    @Override
    public String toString()
    {
        return "Formula " + getBaseType().toString() + "(Id: " + getId() + ")";
    }

    /*********************************************************
     * Re-implementation for methods for FormulaBase superclass
     *********************************************************/

    @Override
    public BaseType getBaseType()
    {
        return BaseType.IMAGE_FRAGMENT;
    }

    @Override
    public boolean enableObjectProperties()
    {
        return true;
    }

    @Override
    public void updateTextSize()
    {
        super.updateTextSize();
        updateImageView();
    }

    @Override
    public void undo(FormulaState state)
    {
        super.undo(state);
        if (!parameters.embedded)
        {
            imageView.loadImage(parameters);
        }
        updateImageView();
        ViewUtils.invalidateLayout(imageView, layout);
    }

    /*********************************************************
     * Implementation for methods for FormulaChangeIf interface
     *********************************************************/

    @Override
    public void onTermSelection(View owner, boolean isSelected, ArrayList<View> list)
    {
        if (list == null && owner == imageView)
        {
            list = new ArrayList<>();
            list.add(owner);
        }
        super.onTermSelection(owner, isSelected, list);
    }

    @Override
    public void onObjectProperties(View owner)
    {
        if (owner == this)
        {
            DialogImageSettings d = new DialogImageSettings(getFormulaList().getActivity(), this, parameters);
            formulaState = getState();
            d.show();
        }
        super.onObjectProperties(owner);
    }

    @Override
    public void onImagePropertiesChange(boolean isFileChanged, boolean isSizeChanged)
    {
        getFormulaList().finishActiveActionMode();
        if (isFileChanged || isSizeChanged)
        {
            if (formulaState != null)
            {
                getFormulaList().getUndoState().addEntry(formulaState);
            }
            if (isFileChanged)
            {
                imageView.loadImage(parameters);
            }
            updateImageView();
            ViewUtils.invalidateLayout(imageView, layout);
        }
        formulaState = null;
    }

    @Override
    public void onNewFormula()
    {
        DialogImageSettings d = new DialogImageSettings(getFormulaList().getActivity(), this, parameters);
        d.show();
    }

    /*********************************************************
     * Read/write interface
     *********************************************************/

    /**
     * Parcelable interface: procedure writes the formula state
     */
    @Override
    public Parcelable onSaveInstanceState()
    {
        Parcelable state = super.onSaveInstanceState();
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            ImageProperties ip = new ImageProperties();
            ip.assign(parameters);
            bundle.putParcelable(STATE_IMAGE_PARAMETERS, ip);
            bundle.putParcelable(STATE_IMAGE_VIEW, imageView.onSaveInstanceState());
        }
        return state;
    }

    /**
     * Parcelable interface: procedure reads the formula state
     */
    @Override
    public void onRestoreInstanceState(Parcelable state)
    {
        if (state == null)
        {
            return;
        }
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            imageView.onRestoreInstanceState(bundle.getParcelable(STATE_IMAGE_VIEW));
            parameters.assign((ImageProperties) bundle.getParcelable(STATE_IMAGE_PARAMETERS));
            super.onRestoreInstanceState(bundle);
            updateImageView();
        }
    }

    @Override
    public boolean onStartReadXmlTag(XmlPullParser parser)
    {
        super.onStartReadXmlTag(parser);
        if (getBaseType().toString().equalsIgnoreCase(parser.getName()))
        {
            parameters.readFromXml(parser);
            if (parameters.embedded)
            {
                imageView.readFromXml(parser);
            }
            else
            {
                imageView.loadImage(parameters);
            }
            updateImageView();
        }
        return false;
    }

    @Override
    public boolean onStartWriteXmlTag(XmlSerializer serializer, String key) throws Exception
    {
        super.onStartWriteXmlTag(serializer, key);
        if (getBaseType().toString().equalsIgnoreCase(serializer.getName()))
        {
            parameters.writeToXml(serializer);
            if (parameters.embedded)
            {
                imageView.writeToXml(serializer);
            }
        }
        return false;
    }

    /*********************************************************
     * ImageFragment-specific methods
     *********************************************************/

    /**
     * Procedure creates the formula layout
     */
    private void onCreate()
    {
        inflateRootLayout(R.layout.image_fragment, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        imageView = (CustomImageView) layout.findViewById(R.id.image_view);
        imageView.prepare(getFormulaList().getActivity(), this);
        parameters.initialize(getContext());
        parameters.width = imageView.getOriginalWidth();
        parameters.height = imageView.getOriginalHeight();

        // obtain parent document
        parameters.parentDirectory = getFormulaList().getParentDirectory();

        updateTextSize();
    }

    private void updateImageView()
    {
        final float scale = getFormulaList().getDimen().getScaleFactor();
        int width = parameters.width;
        int height = parameters.height;
        if (parameters.originalSize)
        {
            width = imageView.getOriginalWidth();
            height = imageView.getOriginalHeight();
        }
        imageView.getLayoutParams().width = Math.round(width * scale);
        imageView.getLayoutParams().height = Math.round(height * scale);
        ((CustomLayout) layout).setContentValid(imageView.getImageType() != CustomImageView.ImageType.NONE);
    }
}
