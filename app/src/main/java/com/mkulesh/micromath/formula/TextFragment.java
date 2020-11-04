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
package com.mkulesh.micromath.formula;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;

import com.mkulesh.micromath.dialogs.DialogTextSettings;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.TextProperties;
import com.mkulesh.micromath.properties.TextProperties.TextStyle;
import com.mkulesh.micromath.properties.TextPropertiesChangeIf;
import com.mkulesh.micromath.undo.FormulaState;
import com.mkulesh.micromath.utils.ClipboardManager;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomTextView;
import com.mkulesh.micromath.widgets.ScaledDimensions;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import androidx.annotation.NonNull;

public class TextFragment extends FormulaBase implements TextPropertiesChangeIf
{
    /*
     * Constants used to save/restore the instance state.
     */
    private static final String STATE_TEXT_PARAMETERS = "text_parameters";

    private CustomEditText textField = null;
    private CustomTextView numberField = null;
    private final TextProperties parameters = new TextProperties();

    // undo
    private FormulaState formulaState = null;

    /*--------------------------------------------------------*
     * Constructors
     *--------------------------------------------------------*/

    public TextFragment(FormulaList formulaList, int id)
    {
        super(formulaList, null, 0);
        setId(id);
        onCreate();
    }

    /*--------------------------------------------------------*
     * GUI constructors to avoid lint warning
     *--------------------------------------------------------*/

    public TextFragment(Context context)
    {
        super(null, null, 0);
    }

    public TextFragment(Context context, AttributeSet attrs)
    {
        super(null, null, 0);
    }

    /*--------------------------------------------------------*
     * Re-implementation for methods for Object superclass
     *--------------------------------------------------------*/

    @NonNull
    @Override
    public String toString()
    {
        return "Formula " + getBaseType().toString() + "(Id: " + getId() + ")";
    }

    /*--------------------------------------------------------*
     * Re-implementation for methods for FormulaBase superclass
     *--------------------------------------------------------*/

    @Override
    public BaseType getBaseType()
    {
        return BaseType.TEXT_FRAGMENT;
    }

    @Override
    public boolean enableObjectProperties()
    {
        return true;
    }

    @Override
    public void updateTextSize()
    {
        updateTextView();
    }

    /*--------------------------------------------------------*
     * Implementation for methods for FormulaChangeIf interface
     *--------------------------------------------------------*/

    @Override
    public void onObjectProperties(View owner)
    {
        if (owner == this)
        {
            DialogTextSettings d = new DialogTextSettings(getFormulaList().getActivity(), this, parameters);
            formulaState = getState();
            d.show();
        }
        super.onObjectProperties(owner);
    }

    @Override
    public void onTextPropertiesChange(boolean isChanged)
    {
        getFormulaList().finishActiveActionMode();
        if (isChanged)
        {
            if (formulaState != null)
            {
                getFormulaList().getUndoState().addEntry(formulaState);
            }
            updateTextView();
            getFormulaList().onManualInput();
        }
        formulaState = null;
    }

    /*--------------------------------------------------------*
     * Read/write interface
     *--------------------------------------------------------*/

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
            TextProperties ip = new TextProperties();
            ip.assign(parameters);
            bundle.putParcelable(STATE_TEXT_PARAMETERS, ip);
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
            parameters.assign(bundle.getParcelable(STATE_TEXT_PARAMETERS));
            super.onRestoreInstanceState(bundle);
            updateTextView();
        }
    }

    @Override
    public boolean onStartReadXmlTag(XmlPullParser parser)
    {
        super.onStartReadXmlTag(parser);
        if (getBaseType().toString().equalsIgnoreCase(parser.getName()))
        {
            parameters.readFromXml(parser);
            updateTextView();
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
        }
        return false;
    }

    @Override
    public void onCopyToClipboard()
    {
        if (getFormulaList().getSelectedEquations().size() > 1)
        {
            super.onCopyToClipboard();
        }
        else
        {
            ClipboardManager.copyToClipboard(getContext(), textField.getText().toString());
        }
    }

    /*--------------------------------------------------------*
     * TextFragment-specific methods
     *--------------------------------------------------------*/

    /**
     * Procedure creates the formula layout
     */
    private void onCreate()
    {
        inflateRootLayout(R.layout.text_fragment, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        textField = layout.findViewById(R.id.text_fragment_text);
        addTerm(this, layout, textField, this, false);
        numberField = layout.findViewById(R.id.text_fragment_number);
        numberField.prepare(CustomTextView.SymbolType.TEXT, getFormulaList().getActivity(), this);
    }

    private void updateTextView()
    {
        final ScaledDimensions dimen = getFormulaList().getDimen();
        final int hor = dimen.get(ScaledDimensions.Type.HOR_ROOT_PADDING);
        final int vert = dimen.get(parameters.isHeader() ? ScaledDimensions.Type.HEADER_PADDING
                : ScaledDimensions.Type.VERT_ROOT_PADDING);
        layout.setPadding(hor, vert, hor, vert);
        textField.updateTextSize(dimen, parameters.getDepth(), ScaledDimensions.Type.HOR_TEXT_PADDING);
        textField.setTypeface(null, parameters.isHeader() ? Typeface.BOLD : Typeface.NORMAL);
        if (isNumbering())
        {
            numberField.updateTextSize(dimen, parameters.getDepth());
            numberField.setTypeface(null, parameters.isHeader() ? Typeface.BOLD : Typeface.NORMAL);
            numberField.setPadding(0, 0, vert, 0);
        }
    }

    public void numbering(final int[] number)
    {
        final int styleNumber = TextStyle.values().length;
        if (number.length != styleNumber)
        {
            return;
        }
        final int idx = getTextStyle().ordinal();
        if (isNumbering())
        {
            number[idx]++;
            for (int i = idx + 1; i < styleNumber; i++)
            {
                number[i] = 0;
            }
            StringBuilder nuberStr = new StringBuilder();
            for (int i = 0; i <= idx; i++)
            {
                if (number[i] == 0)
                {
                    continue;
                }
                if (nuberStr.length() != 0)
                {
                    nuberStr.append(".");
                }
                nuberStr.append(number[i]);
            }
            numberField.setVisibility(View.VISIBLE);
            numberField.setText(nuberStr.toString());
        }
        else
        {
            for (int i = idx; i < styleNumber; i++)
            {
                number[i] = 0;
            }
            numberField.setVisibility(View.GONE);
            numberField.setText("");
        }
    }

    public TextProperties.TextStyle getTextStyle()
    {
        return parameters.textStyle;
    }

    public CharSequence getNumber()
    {
        return numberField.getText();
    }

    public boolean isNumbering()
    {
        return parameters.numbering;
    }

    public void format(int width)
    {
        if (terms.isEmpty())
        {
            return;
        }

        char[] chars = terms.get(0).getText().toCharArray();
        final int space = 32;
        final int newLine = 10;
        boolean textStart = false;

        // first pass: replace new lines by spaces
        for (int i = 0; i < chars.length; i++)
        {
            if (!Character.isWhitespace(chars[i]))
            {
                textStart = true;
            }
            if (((int) chars[i]) == newLine)
            {
                if (!textStart)
                {
                    continue;
                }
                boolean textFollows = false;
                if (i + 1 < chars.length)
                {
                    if (!Character.isWhitespace(chars[i + 1]))
                    {
                        textFollows = true;
                    }
                }
                if (textFollows)
                {
                    chars[i] = space;
                }
                else
                {
                    for (; i < chars.length && Character.isWhitespace(chars[i]); i++) ;
                }
            }
        }

        // second pass: replace spaced by new lines
        int lastSpaceIdx = -1, lineStartIdx = 0;
        for (int i = 0; i < chars.length; i++)
        {
            final int currWidth = i - lineStartIdx;
            final int charCode = chars[i];
            if (!Character.isWhitespace(chars[i]))
            {
                textStart = true;
            }
            if (textStart && charCode == space && currWidth <= width)
            {
                lastSpaceIdx = i;
            }
            if (charCode == newLine)
            {
                textStart = false;
                lineStartIdx = i;
                lastSpaceIdx = -1;
                continue;
            }
            if (lastSpaceIdx >= 0 && currWidth >= width)
            {
                chars[lastSpaceIdx] = newLine;
                textStart = false;
                lineStartIdx = lastSpaceIdx;
                lastSpaceIdx = -1;
            }
        }

        terms.get(0).setText(new String(chars));
    }
}
