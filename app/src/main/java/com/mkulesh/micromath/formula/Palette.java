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
package com.mkulesh.micromath.formula;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v7.widget.AppCompatImageButton;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.ClipboardManager;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.FocusChangeIf;
import com.mkulesh.micromath.widgets.ListChangeIf;
import com.mkulesh.micromath.widgets.TextChangeIf;

import java.util.ArrayList;
import java.util.Locale;

/*********************************************************
 * This class implements symbol palette
 *********************************************************/
public class Palette implements OnClickListener, OnLongClickListener, TextChangeIf, FocusChangeIf
{
    static final int NO_BUTTON = -1;

    public enum PaletteType
    {
        NEW_TERM,
        UPDATE_INTERVAL,
        UPDATE_TERM,
        COMPARATORS
    }

    private class PaletteImageButton extends AppCompatImageButton
    {
        private String code = null;
        private final boolean[] enabled = new boolean[PaletteType.values().length];

        public PaletteImageButton(Context context, AttributeSet attrs)
        {
            super(context, attrs);
            enableAll();
        }

        public PaletteImageButton(Context context)
        {
            super(context);
            enableAll();
        }

        public PaletteImageButton(Context context, int imageId, int descriptionId, String code)
        {
            super(context);
            final int buttonSize = context.getResources().getDimensionPixelSize(R.dimen.activity_toolbar_height) - 2
                    * context.getResources().getDimensionPixelSize(R.dimen.activity_palette_vertical_padding);
            setImageResource(imageId);
            setBackgroundResource(R.drawable.clickable_background);
            setLayoutParams(new ViewGroup.LayoutParams(buttonSize, buttonSize));
            if (descriptionId != NO_BUTTON)
            {
                setContentDescription(context.getResources().getString(descriptionId));
                setLongClickable(true);
            }
            this.code = code;
            enableAll();
        }

        private void enableAll()
        {
            for (int i = 0; i < enabled.length; i++)
            {
                enabled[i] = true;
            }
        }

        public void setEnabled(PaletteType t, boolean value)
        {
            enabled[t.ordinal()] = value;
            super.setEnabled(true);
            for (int i = 0; i < enabled.length; i++)
            {
                if (!enabled[i])
                {
                    super.setEnabled(false);
                    break;
                }
            }
        }

        public String getCode()
        {
            return code;
        }
    }

    private final Context context;
    private final ListChangeIf listChangeIf;
    private final ArrayList<ArrayList<PaletteImageButton>> paletteBlock = new ArrayList<ArrayList<PaletteImageButton>>();
    private final LinearLayout paletteLayout;
    private final CustomEditText hiddenInput;
    private String lastHiddenInput = "";

    public Palette(Context context, LinearLayout paletteLayout, ListChangeIf listChangeIf)
    {
        this.context = context;
        this.listChangeIf = listChangeIf;
        this.paletteLayout = paletteLayout;

        hiddenInput = (CustomEditText) paletteLayout.findViewById(R.id.hidden_edit_text);
        hiddenInput.setChangeIf(this, this);
        hiddenInput.setVisibility(View.GONE);
        hiddenInput.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        for (int i = 0; i < PaletteType.values().length; i++)
        {
            paletteBlock.add(new ArrayList<PaletteImageButton>());
        }

        // list operations
        for (int i = 0; i < FormulaBase.BaseType.values().length; i++)
        {
            final FormulaBase.BaseType t = FormulaBase.BaseType.values()[i];
            if (t.getImageId() != NO_BUTTON)
            {
                PaletteImageButton p = new PaletteImageButton(context, t.getImageId(), t.getDescriptionId(),
                        t.toString());
                paletteLayout.addView(p);
                if (t == FormulaBase.BaseType.TERM)
                {
                    paletteBlock.get(PaletteType.NEW_TERM.ordinal()).add(p);
                    paletteBlock.get(PaletteType.UPDATE_TERM.ordinal()).add(p);
                }
            }
        }
        // intervals
        for (int i = 0; i < FormulaTermInterval.IntervalType.values().length; i++)
        {
            final FormulaTermInterval.IntervalType t = FormulaTermInterval.IntervalType.values()[i];
            if (t.getImageId() != NO_BUTTON)
            {
                PaletteImageButton p = new PaletteImageButton(context, t.getImageId(), t.getDescriptionId(), t
                        .toString().toLowerCase(Locale.ENGLISH));
                paletteLayout.addView(p);
                paletteBlock.get(PaletteType.UPDATE_INTERVAL.ordinal()).add(p);
                paletteBlock.get(PaletteType.UPDATE_TERM.ordinal()).add(p);
            }
        }
        // term operators
        for (int i = 0; i < FormulaTermOperator.OperatorType.values().length; i++)
        {
            final FormulaTermOperator.OperatorType t = FormulaTermOperator.OperatorType.values()[i];
            if (t.getImageId() != NO_BUTTON)
            {
                PaletteImageButton p = new PaletteImageButton(context, t.getImageId(), t.getDescriptionId(), t
                        .toString().toLowerCase(Locale.ENGLISH));
                paletteLayout.addView(p);
                paletteBlock.get(PaletteType.UPDATE_TERM.ordinal()).add(p);
            }
        }
        // functions
        for (int i = 0; i < FormulaTermFunction.FunctionType.values().length; i++)
        {
            final FormulaTermFunction.FunctionType t = FormulaTermFunction.FunctionType.values()[i];
            if (t.getImageId() != NO_BUTTON)
            {
                PaletteImageButton p = new PaletteImageButton(context, t.getImageId(), t.getDescriptionId(), t
                        .toString().toLowerCase(Locale.ENGLISH));
                paletteLayout.addView(p);
                paletteBlock.get(PaletteType.UPDATE_TERM.ordinal()).add(p);
            }
        }
        // loop operators
        for (int i = 0; i < FormulaTermLoop.LoopType.values().length; i++)
        {
            final FormulaTermLoop.LoopType t = FormulaTermLoop.LoopType.values()[i];
            if (t.getImageId() != NO_BUTTON)
            {
                PaletteImageButton p = new PaletteImageButton(context, t.getImageId(), t.getDescriptionId(), t
                        .toString().toLowerCase(Locale.ENGLISH));
                paletteLayout.addView(p);
                paletteBlock.get(PaletteType.UPDATE_TERM.ordinal()).add(p);
            }
        }
        // comparators
        for (int i = 0; i < FormulaTermComparator.ComparatorType.values().length; i++)
        {
            final FormulaTermComparator.ComparatorType t = FormulaTermComparator.ComparatorType.values()[i];
            if (t.getImageId() != NO_BUTTON)
            {
                PaletteImageButton p = new PaletteImageButton(context, t.getImageId(), t.getDescriptionId(), t
                        .toString().toLowerCase(Locale.ENGLISH));
                paletteLayout.addView(p);
                paletteBlock.get(PaletteType.COMPARATORS.ordinal()).add(p);
            }
        }

        // prepare all buttons
        for (int i = 0; i < paletteLayout.getChildCount(); i++)
        {
            View b = paletteLayout.getChildAt(i);
            if (b instanceof PaletteImageButton)
            {
                ((PaletteImageButton) b).setOnLongClickListener(this);
                ((PaletteImageButton) b).setOnClickListener(this);
            }
        }
    }

    /**
     * This procedure is used to enable/disable whole palette
     */
    public void setEnabled(boolean enabled)
    {
        paletteLayout.setEnabled(enabled);
        updateButtonsColor();
    }

    /**
     * This procedure is used to enable/disable palette buttons related to a formula term
     */
    public void setPaletteBlockEnabled(PaletteType t, boolean enabled)
    {
        for (PaletteImageButton b : paletteBlock.get(t.ordinal()))
        {
            b.setEnabled(t, enabled);
        }
        updateButtonsColor();
    }

    /**
     * Procedure sets the background color for all buttons depending on enabled status
     */
    private void updateButtonsColor()
    {
        for (int i = 0; i < paletteLayout.getChildCount(); i++)
        {
            if (!(paletteLayout.getChildAt(i) instanceof PaletteImageButton))
            {
                continue;
            }
            PaletteImageButton b = (PaletteImageButton) paletteLayout.getChildAt(i);
            b.clearColorFilter();
            if (!b.isEnabled() || !paletteLayout.isEnabled())
            {
                b.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
            }
        }
    }

    @Override
    public void onClick(View b)
    {
        if (b instanceof PaletteImageButton && listChangeIf != null)
        {
            listChangeIf.onPalettePressed(((PaletteImageButton) b).getCode());
        }
    }

    @Override
    public boolean onLongClick(View b)
    {
        if (b instanceof PaletteImageButton)
        {
            return ViewUtils.showButtonDescription(context, b);
        }
        return false;
    }

    public void enableHiddenInput(boolean hiddenInputEnabled)
    {
        hiddenInput.setTextWatcher(false);
        final int newVis = hiddenInputEnabled ? View.VISIBLE : View.GONE;
        if (hiddenInput.getVisibility() != newVis)
        {
            hiddenInput.setVisibility(newVis);
        }
        if (hiddenInput.getVisibility() != View.GONE)
        {
            lastHiddenInput = "";
            hiddenInput.setText(lastHiddenInput);
            hiddenInput.requestFocus();
            hiddenInput.setTextWatcher(true);
        }
    }

    @Override
    public void beforeTextChanged(String s, boolean isManualInput)
    {
        // empty
    }

    @Override
    public void onTextChanged(String s, boolean isManualInput)
    {
        if (s == null || listChangeIf == null)
        {
            lastHiddenInput = null;
            return;
        }

        if (lastHiddenInput != null && lastHiddenInput.equals(s))
        {
            return;
        }

        lastHiddenInput = s;

        if (ClipboardManager.isFormulaObject(s))
        {
            hiddenInput.setTextWatcher(false);
            listChangeIf.onPalettePressed(s);
            return;
        }

        final String termSep = context.getResources().getString(R.string.formula_term_separator);
        final String code = (termSep.equals(s)) ? FormulaBase.BaseType.TERM.toString() : FormulaTerm.getOperatorCode(
                context, s, FormulaTermFunction.isConversionEnabled(context, s));
        if (code == null)
        {
            return;
        }

        if (FormulaTermFunction.FunctionType.FUNCTION_LINK.toString().equalsIgnoreCase(code))
        {
            hiddenInput.setTextWatcher(false);
            listChangeIf.onPalettePressed(s);
            return;
        }

        for (int i = 0; i < paletteLayout.getChildCount(); i++)
        {
            if (paletteLayout.getChildAt(i) instanceof PaletteImageButton)
            {
                PaletteImageButton b = (PaletteImageButton) paletteLayout.getChildAt(i);
                if (b.isEnabled() && b.getCode() != null && b.getCode().equalsIgnoreCase(code))
                {
                    hiddenInput.setTextWatcher(false);
                    listChangeIf.onPalettePressed(b.getCode());
                    break;
                }
            }
        }
    }

    @Override
    public void onSizeChanged()
    {
        // empty
    }

    @Override
    public int onGetNextFocusId(CustomEditText owner, FocusChangeIf.NextFocusType focusType)
    {
        return R.id.main_list_view;
    }
}
