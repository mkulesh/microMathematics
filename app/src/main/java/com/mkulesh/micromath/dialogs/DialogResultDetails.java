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

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mkulesh.micromath.math.CalculatedValue;
import com.mkulesh.micromath.math.EquationArrayResult;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.DocumentProperties;
import com.mkulesh.micromath.utils.CompatUtils;

public class DialogResultDetails extends DialogBase
{
    // Internal Data Container used in the adapter
    private static class ArgumentValueItem
    {
        public final CalculatedValue argument;
        public final CalculatedValue value;

        private ArgumentValueItem(CalculatedValue argument, CalculatedValue value)
        {
            this.argument = argument;
            this.value = value;
        }

        private ArgumentValueItem(int idx, CalculatedValue value)
        {
            this.argument = new CalculatedValue(CalculatedValue.ValueType.REAL, idx, 0.0);
            this.value = value;
        }

        private ArgumentValueItem(double argument, double value)
        {
            this.argument = new CalculatedValue(CalculatedValue.ValueType.REAL, argument, 0.0);
            this.value = new CalculatedValue(CalculatedValue.ValueType.REAL, value, 0.0);
        }
    }

    private ArgumentValueAdapter argumentValueAdapter;

    public DialogResultDetails(Context context, EquationArrayResult args, EquationArrayResult vals, DocumentProperties doc)
    {
        super(context, R.layout.dialog_result_details, R.string.action_details);

        // Create data container
        final CalculatedValue[] arguments = args.getRawValues();
        final CalculatedValue[] values = vals.getRawValues();
        final int n = Math.min(arguments.length, values.length);
        final ArrayList<ArgumentValueItem> calculatedItems = new ArrayList<>(n);
        for (int i = 0; i < n; i++)
        {
            calculatedItems.add(new ArgumentValueItem(arguments[i], values[i]));
        }

        initialize(calculatedItems, doc);
    }

    public DialogResultDetails(Context context, EquationArrayResult vals, DocumentProperties doc)
    {
        super(context, R.layout.dialog_result_details, R.string.action_details);

        // Create data container
        final CalculatedValue[] values = vals.getRawValues();
        final int n = values.length;
        final ArrayList<ArgumentValueItem> calculatedItems = new ArrayList<>(n);
        for (int i = 0; i < n; i++)
        {
            calculatedItems.add(new ArgumentValueItem(i, values[i]));
        }

        initialize(calculatedItems, doc);
    }

    public DialogResultDetails(Context context, double[] args, double[] vals, DocumentProperties doc)
    {
        super(context, R.layout.dialog_result_details, R.string.action_details);

        // Create data container
        final int n = Math.min(args.length, vals.length);
        final ArrayList<ArgumentValueItem> calculatedItems = new ArrayList<>(n);
        for (int i = 0; i < n; i++)
        {
            calculatedItems.add(new ArgumentValueItem(args[i], vals[i]));
        }

        initialize(calculatedItems, doc);
    }

    private void initialize(ArrayList<ArgumentValueItem> calculatedItems, DocumentProperties doc)
    {
        // Maximize the dialog.
        maximize();
        ((LinearLayout) findViewById(R.id.dialog_button_panel)).setVisibility(View.GONE);

        argumentValueAdapter = new ArgumentValueAdapter(getContext(), calculatedItems, doc);
        ListView listView = (ListView) findViewById(R.id.result_details_listview);
        listView.setAdapter(argumentValueAdapter);

        // Show number of items
        final TextView itemsNumber = (TextView) findViewById(R.id.result_details_items_number);
        itemsNumber.setText(Integer.toString(calculatedItems.size()) + " "
                + getContext().getString(R.string.dialog_list_items));
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
            tvArgument.setText(item.argument.getResultDescription(doc));

            final TextView tvValue = (TextView) convertView.findViewById(R.id.result_details_item_value);
            tvValue.setText(item.value.getResultDescription(doc));

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
