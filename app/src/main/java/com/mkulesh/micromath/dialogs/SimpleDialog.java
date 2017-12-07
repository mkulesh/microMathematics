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

import android.app.Activity;
import android.graphics.PorterDuff;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.mkulesh.micromath.R;

public class SimpleDialog extends DialogBase
{

    public interface EventHandler
    {
        void onCreate(SimpleDialog d, LinearLayout dLayout);

        void onClick(LinearLayout dLayout, int whichButton);
    }

    private final EventHandler eventHandler;

    public SimpleDialog(Activity context, int layoutId, int titleId, EventHandler eventHandler)
    {
        super(context, layoutId, titleId);

        this.eventHandler = eventHandler;
        if (eventHandler != null)
        {
            eventHandler.onCreate(this, getRootLayout());
        }
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.dialog_button_cancel || v.getId() == R.id.dialog_button_ok)
        {
            if (eventHandler != null)
            {
                eventHandler.onClick(getRootLayout(), v.getId());
            }
        }
        closeDialog();
    }

    public void disableButton(int id)
    {
        View button = findViewById(id);
        final View other = findViewById(id == R.id.dialog_button_ok ? R.id.dialog_button_cancel : R.id.dialog_button_ok);
        if (button != null && other != null)
        {
            button.setVisibility(View.GONE);
            LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, getContext().getResources().getDimensionPixelSize(R.dimen.dialog_buttons_margin));
            other.setLayoutParams(lp);
            findViewById(R.id.dialog_button_devider).setVisibility(View.GONE);
        }
    }

    public void setImage(final ImageView image, int res, int color)
    {
        image.setVisibility(View.VISIBLE);
        image.setImageResource(res);
        image.clearColorFilter();
        image.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }
}
