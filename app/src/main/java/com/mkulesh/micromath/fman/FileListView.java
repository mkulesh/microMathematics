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
package com.mkulesh.micromath.fman;

import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mkulesh.micromath.R;
import com.mkulesh.micromath.utils.ViewUtils;

public class FileListView implements AdapterView.OnItemClickListener
{
    private final Commander commander;
    public final ListView listView;
    private final LinearLayout statusPanel;
    private final View statusPanelDivider;
    private final TextView statusBar;

    private int currentPosition = -1;
    public int adapterMode = 0;

    FileListView(Commander c)
    {
        commander = c;
        listView = commander.findViewById(R.id.fman_list_view);
        listView.setItemsCanFocus(false);
        listView.setFocusableInTouchMode(true);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setOnItemClickListener(this);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        {
            listView.setSelector(R.drawable.clickable_background_no_padding);
        }
        commander.registerForContextMenu(listView);
        statusPanel = commander.findViewById(R.id.fman_status_panel);
        statusPanelDivider = commander.findViewById(R.id.fman_status_panel_divider);
        statusBar = commander.findViewById(R.id.fman_status_bar);
    }

    public final AdapterIf getListAdapter()
    {
        return (AdapterIf) listView.getAdapter();
    }

    public final void Navigate(Uri uri, String posTo)
    {
        try
        {
            currentPosition = -1;
            listView.clearChoices();
            listView.invalidateViews();
            AdapterIf ca_new = null, ca = (AdapterIf) listView.getAdapter();
            String scheme = uri.getScheme();
            if (scheme == null)
            {
                scheme = "";
            }
            if (ca == null || !scheme.equals(ca.getScheme()))
            {
                ca_new = commander.CreateAdapter(uri);
                listView.setAdapter((ListAdapter) ca_new);
                applySettings();
                ca = ca_new;
            }
            ca.setMode(AdapterIf.MODE_SORTING | AdapterIf.MODE_SORT_DIR, adapterMode);
            ca.readSource(uri, Integer.toBinaryString(0) + (posTo == null ? "" : posTo));
            statusPanel.setVisibility((ca instanceof AdapterHome) ? View.GONE : View.VISIBLE);
            statusPanelDivider.setVisibility(statusPanel.getVisibility());
            statusBar.setText(ca.toString());
            ViewUtils.Debug(this, "Current directory: " + ca.getUri().getPath());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public final void applySettings()
    {
        try
        {
            AdapterIf ca = (AdapterIf) listView.getAdapter();
            if (ca == null)
                return;

            ca.setMode(AdapterIf.MODE_SORTING, AdapterIf.SORT_NAME);

            if (ca instanceof AdapterHome)
                ca.setMode(AdapterIf.MODE_ROOT, AdapterIf.BASIC_MODE);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public final void refreshList(String posto)
    {
        try
        {
            AdapterIf ca = (AdapterIf) listView.getAdapter();
            if (ca == null)
                return;
            listView.clearChoices();
            String cookie = Integer.toString(0);
            if (posto != null)
                cookie += posto;
            ca.readSource(null, cookie);
            listView.invalidateViews();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public final void askRedrawList()
    {
        listView.invalidateViews();
    }

    public final void setSelection(int i, int y_)
    {
        final ListView flv$ = listView;
        final int position$ = i, y$ = y_;
        flv$.post(new Runnable()
        {
            public void run()
            {
                flv$.setSelectionFromTop(position$, y$ > 0 ? y$ : flv$.getHeight() / 2);
            }
        });
        currentPosition = i;
    }

    public final void setSelection(String name)
    {
        AdapterIf ca = (AdapterIf) listView.getAdapter();
        if (ca != null)
        {
            int i, num = ((ListAdapter) ca).getCount();
            for (i = 0; i < num; i++)
            {
                String item_name = ca.getItemName(i, false);
                if (item_name != null && item_name.compareTo(name) == 0)
                {
                    setSelection(i, listView.getHeight() / 2);
                    break;
                }
            }
        }
    }

    public final int getSelected()
    {
        int pos = listView.getSelectedItemPosition();
        if (pos != AdapterView.INVALID_POSITION)
            return currentPosition = pos;
        return currentPosition;
    }

    public final void recoverAfterRefresh(String item_name)
    {
        try
        {
            if (FileUtils.str(item_name))
                setSelection(item_name);
            else
                setSelection(currentPosition > 0 ? currentPosition : 0, 0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * An AdapterView.OnItemClickListener implementation
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        currentPosition = position;
        AdapterIf ca = (AdapterIf) listView.getAdapter();
        ca.openItem(position);
    }
}
