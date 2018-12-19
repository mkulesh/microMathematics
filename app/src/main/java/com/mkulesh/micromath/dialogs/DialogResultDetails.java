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
package com.mkulesh.micromath.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mkulesh.micromath.R;
import com.mkulesh.micromath.formula.TermParser;
import com.mkulesh.micromath.math.ArgumentValueItem;
import com.mkulesh.micromath.properties.DocumentProperties;
import com.mkulesh.micromath.utils.CompatUtils;

import java.util.ArrayList;

/**
 * A file chooser implemented in a Dialog.
 */
public class DialogResultDetails extends DialogBase
{
    private final ArgumentValueAdapter argumentValueAdapter;

    public DialogResultDetails(Context context, ArrayList<ArgumentValueItem> calculatedItems, DocumentProperties doc)
    {
        super(context, R.layout.dialog_result_details, R.string.action_details);

        // Maximize the dialog.
        maximize();
        ((LinearLayout) findViewById(R.id.dialog_button_panel)).setVisibility(View.GONE);

        argumentValueAdapter = new ArgumentValueAdapter(context, calculatedItems, doc);
        ListView listView = (ListView) findViewById(R.id.result_details_listview);
        listView.setAdapter(argumentValueAdapter);

        // Show number of items
        final TextView itemsNumber = (TextView) findViewById(R.id.result_details_items_number);
        itemsNumber.setText(Integer.toString(calculatedItems.size()) + " "
                + context.getString(R.string.dialog_list_items));
    }

    private final class ArgumentValueAdapter extends ArrayAdapter<ArgumentValueItem>
    {
        private final DocumentProperties doc;

        public ArgumentValueAdapter(Context context, ArrayList<ArgumentValueItem> list, DocumentProperties doc)
        {
            super(context, 0, list);
            this.doc = doc;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            // Get the data item for this position
            ArgumentValueItem item = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null)
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_result_details_item, parent,
                        false);
            }

            // Lookup view for data population
            final TextView tvArgument = (TextView) convertView.findViewById(R.id.result_details_item_argument);
            tvArgument.setText(TermParser.doubleToString(item.argument, doc));

            final TextView tvValue = (TextView) convertView.findViewById(R.id.result_details_item_value);
            tvValue.setText(TermParser.doubleToString(item.value, doc));

            // To avoid a bug on some Android versions, set color to
            // TextView's instead of parent layout
            final int selectionColor = (position % 2 == 0) ? CompatUtils.getColor(getContext(),
                    R.color.formula_selected_root_color) : CompatUtils.getColor(getContext(),
                    R.color.dialog_panel_color);
            tvArgument.setBackgroundColor(selectionColor);
            tvValue.setBackgroundColor(selectionColor);

            return convertView;
        }
    }
}
