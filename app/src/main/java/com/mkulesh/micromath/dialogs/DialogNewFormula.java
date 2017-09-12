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
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.ImageButton;

import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.ListChangeIf;

import java.util.HashMap;
import java.util.Map;

public class DialogNewFormula extends DialogBase implements OnLongClickListener
{
    public static final String LAST_INSERTED_POSITION = "last_inserted_position";
    public static final String LAST_INSERTED_OBJECT = "last_inserted_object";
    public static final String LAST_INSERTED_EMPTY = "empty";

    private final ListChangeIf changeIf;
    private final HashMap<ListChangeIf.Position, ImageButton> positionButtons = new HashMap<ListChangeIf.Position, ImageButton>();
    private final HashMap<ListChangeIf.FormulaType, ImageButton> objectButtons = new HashMap<ListChangeIf.FormulaType, ImageButton>();

    public DialogNewFormula(Activity context, ListChangeIf listChangeIf)
    {
        super(context, R.layout.dialog_new_formula, R.string.math_new_element);

        // position buttons
        positionButtons.put(ListChangeIf.Position.BEFORE, (ImageButton) findViewById(R.id.dialog_button_insert_before));
        positionButtons.put(ListChangeIf.Position.AFTER, (ImageButton) findViewById(R.id.dialog_button_insert_after));
        positionButtons.put(ListChangeIf.Position.LEFT, (ImageButton) findViewById(R.id.dialog_button_insert_left));
        positionButtons.put(ListChangeIf.Position.RIGHT, (ImageButton) findViewById(R.id.dialog_button_insert_right));
        for (ImageButton b : positionButtons.values())
        {
            b.setSelected(false);
            b.setOnClickListener(this);
            b.setOnLongClickListener(this);
        }
        String str = pref.getString(LAST_INSERTED_POSITION, LAST_INSERTED_EMPTY);
        try
        {
            ListChangeIf.Position insertType = ListChangeIf.Position.valueOf(str);
            positionButtons.get(insertType).setSelected(true);
        }
        catch (Exception e)
        {
            positionButtons.get(ListChangeIf.Position.AFTER).setSelected(true);
        }

        // object buttons
        objectButtons.put(ListChangeIf.FormulaType.EQUATION,
                (ImageButton) findViewById(R.id.dialog_button_new_equation));
        objectButtons.put(ListChangeIf.FormulaType.RESULT, (ImageButton) findViewById(R.id.dialog_button_new_result));
        objectButtons.put(ListChangeIf.FormulaType.PLOT_FUNCTION,
                (ImageButton) findViewById(R.id.dialog_button_new_function_plot));
        objectButtons.put(ListChangeIf.FormulaType.PLOT_CONTOUR,
                (ImageButton) findViewById(R.id.dialog_button_new_contour_plot));
        objectButtons.put(ListChangeIf.FormulaType.TEXT_FRAGMENT,
                (ImageButton) findViewById(R.id.dialog_button_new_text_fragment));
        objectButtons.put(ListChangeIf.FormulaType.IMAGE_FRAGMENT,
                (ImageButton) findViewById(R.id.dialog_button_new_image_fragment));
        for (ImageButton b : objectButtons.values())
        {
            b.setSelected(false);
            b.setOnClickListener(this);
            b.setOnLongClickListener(this);
        }
        str = pref.getString(LAST_INSERTED_OBJECT, LAST_INSERTED_EMPTY);
        try
        {
            ListChangeIf.FormulaType formulaType = ListChangeIf.FormulaType.valueOf(str);
            objectButtons.get(formulaType).setSelected(true);
        }
        catch (Exception e)
        {
            objectButtons.get(ListChangeIf.FormulaType.EQUATION).setSelected(true);
        }

        this.changeIf = listChangeIf;
    }

    @Override
    public void onClick(View v)
    {
        if (positionButtons.containsValue(v))
        {
            // position buttons
            for (ImageButton b : positionButtons.values())
            {
                b.setSelected(v == b);
            }
            return;
        }
        else if (objectButtons.containsValue(v))
        {
            // object buttons
            for (ImageButton b : objectButtons.values())
            {
                b.setSelected(v == b);
            }
            return;
        }
        else if (v.getId() == R.id.dialog_button_ok && changeIf != null)
        {
            // inspect position buttons
            ListChangeIf.Position insertType = ListChangeIf.Position.AFTER;
            for (Map.Entry<ListChangeIf.Position, ImageButton> e : positionButtons.entrySet())
            {
                if (e.getValue().isSelected())
                {
                    insertType = e.getKey();
                    SharedPreferences.Editor prefEditor = pref.edit();
                    prefEditor.putString(LAST_INSERTED_POSITION, insertType.toString());
                    prefEditor.commit();
                }
            }
            // inspect object buttons
            ListChangeIf.FormulaType formulaType = ListChangeIf.FormulaType.EQUATION;
            for (Map.Entry<ListChangeIf.FormulaType, ImageButton> e : objectButtons.entrySet())
            {
                if (e.getValue().isSelected())
                {
                    formulaType = e.getKey();
                    SharedPreferences.Editor prefEditor = pref.edit();
                    prefEditor.putString(LAST_INSERTED_OBJECT, formulaType.toString());
                    prefEditor.commit();
                }
            }
            changeIf.onNewFormula(insertType, formulaType);
        }
        closeDialog();
    }

    @Override
    public boolean onLongClick(View b)
    {
        return ViewUtils.showButtonDescription(getContext(), b);
    }

}
