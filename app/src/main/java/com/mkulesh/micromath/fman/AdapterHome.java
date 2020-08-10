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

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.view.ContextMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.preference.PreferenceManager;

import com.mkulesh.micromath.fman.CommanderIf.SelectionMode;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.CompatUtils;

import java.util.ArrayList;

public class AdapterHome extends AdapterBaseImpl
{
    public final static String ORG_SCHEME = "home";
    public static final String DEFAULT_LOC = "home:";
    private final int[] LOCAL = { R.string.fman_local, R.string.fman_local_descr, R.drawable.fman_storage };
    private final int[] EXTERNAL = { R.string.fman_external, R.string.fman_external_descr, R.drawable.fman_sd_card };
    private final int[] SAF = { R.string.fman_saf, R.string.fman_saf_descr, R.drawable.fman_sd_card };
    private final int[] ASSETS = { R.string.fman_assets, R.string.fman_assets_descr, R.drawable.fman_assets };

    private Item[] items = null;

    private Item makeItem(int[] mode, String scheme)
    {
        Item item = new Item();
        item.name = s(mode[0]);
        item.attr = s(mode[1]);
        item.icon_id = mode[2];
        item.origin = scheme;
        return item;
    }

    public AdapterHome(Context ctx_)
    {
        super(ctx_, SHOW_ATTR | ATTR_ONLY);
        setCount(getNumItems());
    }

    @Override
    public String getScheme()
    {
        return AdapterHome.ORG_SCHEME;
    }

    @Override
    public int setMode(int mask, int val)
    {
        if ((mask & MODE_ATTR) == 0)
            super.setMode(mask, val);
        if ((mask & MODE_ROOT) != 0)
        {
            setCount(getNumItems());
            notifyDataSetChanged();
        }
        return mode;
    }

    @Override
    public String toString()
    {
        return DEFAULT_LOC;
    }

    /*
     * CommanderAdapter implementation
     */
    @Override
    public Uri getUri()
    {
        return Uri.parse(toString());
    }

    @Override
    public boolean readSource(Uri tmp_uri, String pbod)
    {
        try
        {
            items = null;
            ArrayList<Item> ia = new ArrayList<>();

            ia.add(makeItem(LOCAL, AdapterFileSystem.ORG_SCHEME));

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            {
                final String fs = FileUtils.mbAddSl(DEFAULT_DIR);
                String[] dirs = CompatUtils.getStorageDirs(ctx);
                if (dirs != null)
                {
                    for (String dir : dirs)
                    {
                        if (!FileUtils.str(dir))
                            continue;
                        if (fs.equals(dir))
                            continue;
                        Item item = makeItem(EXTERNAL, dir);
                        ia.add(item);
                    }
                }
            }
            else
            {
                String sec_st = FileUtils.getSecondaryStorage();
                if (FileUtils.str(sec_st))
                    ia.add(makeItem(EXTERNAL, sec_st));

            }
            if (CompatUtils.isMarshMallowOrLater())
            {
                Item item = makeItem(SAF, AdapterDocuments.ORG_SCHEME);
                item.dir = true;
                ia.add(item);
            }
            // Add Assets adapter if commander is called to open a file
            if (commander.getSelectionMode() == SelectionMode.OPEN)
            {
                Item item = makeItem(ASSETS, FileUtils.ASSET_RESOURCE_PREFIX);
                item.dir = true;
                ia.add(item);
            }

            items = new Item[ia.size()];
            ia.toArray(items);

            setCount(getNumItems());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        notify(pbod);
        return true;
    }

    @Override
    public void openItem(int position)
    {
        Item item = (Item) getItem(position);
        String uri_s = null;
        if (AdapterDocuments.ORG_SCHEME.equals(item.origin))
        {
            SharedPreferences saf_sp = PreferenceManager.getDefaultSharedPreferences(ctx);
            uri_s = saf_sp.getString(AdapterDocuments.PREF_TREE_ROOT_URI, null);
            if (uri_s == null)
            {
                commander.closeDialog();
                commander.issue(CompatUtils.getDocTreeIntent(), AdapterDocuments.REQUEST_OPEN_DOCUMENT_TREE);
                return;
            }
        }
        else if (AdapterFileSystem.ORG_SCHEME.equals(item.origin))
        {
            uri_s = DEFAULT_DIR;
        }
        else
        {
            String scheme = (String) item.origin;
            if (scheme.indexOf('/') >= 0)
                uri_s = scheme;
            else
                uri_s = item.origin + ":";
        }
        if (FileUtils.str(uri_s))
        {
            commander.Navigate(Uri.parse(uri_s), null);
        }
    }

    private int getNumItems()
    {
        return items == null ? 0 : items.length;
    }

    @Override
    public String getItemName(int position, boolean full)
    {
        return items != null ? "" : items[position].name;
    }

    /*
     * BaseAdapter implementation
     */
    @Override
    public Object getItem(int position)
    {
        return items[position];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Item item = (Item) getItem(position);
        if (item == null)
            return null;
        return getView(convertView, parent, item);
    }

    @Override
    public void populateContextMenu(ContextMenu menu, AdapterView.AdapterContextMenuInfo acmi, int num)
    {
        Item item = (Item) getItem(acmi.position);
        if (item == null)
            return;
        String schema = item.origin instanceof String ? (String) item.origin : null;
        if (!FileUtils.str(schema))
            return;
        if (AdapterDocuments.ORG_SCHEME.startsWith(schema))
        {
            menu.add(0, R.id.fman_action_open_saf, 0, R.string.fman_open_saf);
        }
    }

    @Override
    public void doIt(int command_id, int position)
    {
        if (position < 0 || position > items.length)
        {
            return;
        }
        Item item = items[position];
        try
        {
            String schema = item.origin instanceof String ? (String) item.origin : null;
            if (!FileUtils.str(schema))
            {
                return;
            }
            if (AdapterDocuments.ORG_SCHEME.startsWith(schema) && R.id.fman_action_open_saf == command_id)
            {
                commander.closeDialog();
                commander.issue(CompatUtils.getDocTreeIntent(), AdapterDocuments.REQUEST_OPEN_DOCUMENT_TREE);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
