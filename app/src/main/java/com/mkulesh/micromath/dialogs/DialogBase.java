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

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.CompatUtils;
import com.mkulesh.micromath.utils.ViewUtils;

import java.util.Locale;

public class DialogBase extends Dialog implements OnClickListener
{
    protected final SharedPreferences pref;
    final private TextView title;

    public DialogBase(Context context, int layoutId, int titleId)
    {
        super(context);
        setCanceledOnTouchOutside(true);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_base);
        final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(layoutId, (LinearLayout) findViewById(R.id.dialog_content_panel));

        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.drawableDialogWindow, outValue, true);
        getWindow().getDecorView().setBackgroundResource(outValue.resourceId);
        getWindow().getDecorView().setPadding(0, 0, 0, 0);

        title = (TextView) findViewById(R.id.dialog_title_text);
        title.setText(titleId);
        prepareTextStyle(title);

        final Button okButton = ((Button) findViewById(R.id.dialog_button_ok));
        okButton.setOnClickListener(this);
        prepareTextStyle(okButton);
        prepareButtonImage(okButton);

        final Button cancelButton = ((Button) findViewById(R.id.dialog_button_cancel));
        cancelButton.setOnClickListener(this);
        prepareTextStyle(cancelButton);
        prepareButtonImage(cancelButton);

        final View divider = findViewById(R.id.dialog_divider_view);
        CompatUtils.setDrawableColorAttr(context, divider.getBackground(), R.attr.colorDialogTitle);

        pref = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    private void prepareTextStyle(TextView v)
    {
        final String text = v.getText().toString().toUpperCase(Locale.getDefault());
        v.setText(text);
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.dialog_button_cancel)
        {
            closeDialog();
        }
    }

    public LinearLayout getRootLayout()
    {
        View root = this.findViewById(R.id.dialog_root_layout);
        return (root instanceof LinearLayout) ? (LinearLayout) root : null;
    }

    protected void closeDialog()
    {
        if (!ViewUtils.isHardwareKeyboardAvailable(getContext()))
        {
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
        }
        dismiss();
    }

    protected void maximize()
    {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(this.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        this.getWindow().setAttributes(lp);
    }

    protected void setButtonSelected(ImageButton b, boolean isSelected)
    {
        b.setSelected(isSelected);
        ViewUtils.setImageButtonColorAttr(getContext(), b,
                b.isSelected() ? R.attr.colorAccent : R.attr.colorDialogContent);
    }

    protected void setButtonEnabled(ImageButton b, boolean isEnabled)
    {
        b.setEnabled(isEnabled);
        ViewUtils.setImageButtonColorAttr(getContext(), b,
                b.isEnabled() ? R.attr.colorDialogContent : R.attr.colorDialogDisabledElement);
    }

    protected void prepareButtonImage(Button b)
    {
        for (Drawable d : b.getCompoundDrawables())
        {
            CompatUtils.setDrawableColorAttr(getContext(), d,
                    b.isEnabled() ? R.attr.colorDialogContent : R.attr.colorDialogDisabledElement);
        }
    }

    protected ColorPicker PrepareColorPicker(int color)
    {
        ColorPicker cp = (ColorPicker) findViewById(R.id.dialog_colorpicker);
        if (cp != null)
        {
            View v = findViewById(R.id.dialog_colorpicker_saturation_bar);
            if (v != null && v instanceof SaturationBar)
            {
                cp.addSaturationBar((SaturationBar) v);
            }
            v = findViewById(R.id.dialog_colorpicker_value_bar);
            if (v != null && v instanceof ValueBar)
            {
                cp.addValueBar((ValueBar) v);
            }
            v = findViewById(R.id.dialog_colorpicker_opacity_bar);
            if (v != null && v instanceof OpacityBar)
            {
                cp.addOpacityBar((OpacityBar) v);
            }
            cp.setColor(color);
            cp.setOldCenterColor(color);
        }
        return cp;
    }
}
