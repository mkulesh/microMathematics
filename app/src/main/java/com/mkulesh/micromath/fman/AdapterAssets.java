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
import android.content.res.AssetManager;
import android.net.Uri;

import com.mkulesh.micromath.plus.R;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class AdapterAssets extends AdapterBaseImpl
{
    public final static String ORG_SCHEME = "asset";
    private String dirName;
    protected FileItem[] items;
    private final ArrayList<String> assetFilter = new ArrayList<>();
    private final AssetManager assetManager;

    public AdapterAssets(Context context, CharSequence[] assetFilter)
    {
        super(context, 0);
        dirName = null;
        items = null;
        readWriteAdapter = false;
        assetManager = context.getAssets();
        if (assetFilter != null)
        {
            for (CharSequence s : assetFilter)
            {
                this.assetFilter.add(s.toString());
            }
        }
    }

    @Override
    public String getScheme()
    {
        return AdapterDocuments.ORG_SCHEME;
    }

    @Override
    public String toString()
    {
        return dirName == null ? FileUtils.ASSET_RESOURCE_PREFIX : FileUtils.mbAddSl(dirName);
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
    public void setUri(Uri uri)
    {
        dirName = uri.getPath();
    }

    @Override
    public boolean readSource(Uri d, String pass_back_on_done)
    {
        try
        {
            items = null;
            if (d != null)
            {
                dirName = d.toString();
            }
            if (dirName == null)
            {
                notify(ctx.getString(R.string.fman_error_no_such_folder, (d == null ? "null" : d.toString())),
                        CommanderIf.OPERATION_FAILED);
                return false;
            }
            String assetsPath = dirName.replace(FileUtils.ASSET_RESOURCE_PREFIX, "");
            while (assetsPath.endsWith(SLS) && assetsPath.length() > 1)
            {
                assetsPath = assetsPath.substring(0, assetsPath.length() - 1);
            }
            final String[] assets = assetManager.list(assetsPath);
            final long appTimeStamp = FileUtils.getAppTimeStamp(ctx);
            if (assets != null && assets.length > 0)
            {
                ArrayList<FileItem> fileItems = new ArrayList<>();
                for (String asset : assets)
                {
                    if (assetsPath.length() == 0 && !assetFilter.contains(asset))
                    {
                        continue;
                    }
                    final String path = assetsPath.length() == 0 ? asset : assetsPath + SLS + asset;
                    final String ext = FileUtils.getFileExt(asset);
                    if (FileUtils.str(ext))
                    {
                        FileItem fi = new FileItem(new File(asset));
                        fi.attr = FileUtils.getMimeByExt(FileUtils.getFileExt(ext), "");
                        try
                        {
                            InputStream is = assetManager.open(path);
                            fi.size = is.available();
                            is.close();
                        }
                        catch (Exception e)
                        {
                            fi.size = -1;
                        }
                        fi.date = new Date(appTimeStamp);
                        fileItems.add(fi);
                    }
                    else
                    {
                        final String[] subAssets = assetManager.list(path);
                        if (subAssets != null && subAssets.length > 0)
                        {
                            FileItem fi = new FileItem(new File(asset));
                            fi.dir = true;
                            fi.attr = subAssets.length + " "
                                    + ctx.getString(R.string.dialog_list_items);
                            fi.date = new Date(appTimeStamp);
                            fi.size = -1;
                            fileItems.add(fi);
                        }
                    }
                }
                if (fileItems.size() > 0)
                {
                    items = new FileItem[fileItems.size()];
                    for (int i = 0; i < items.length; i++)
                    {
                        items[i] = fileItems.get(i);
                    }
                    reSort(items);
                }
                parentLink = FileUtils.ASSET_RESOURCE_PREFIX.equals(dirName) ? SLS : PLS;
                notifyDataSetChanged();
                return true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        catch (OutOfMemoryError err)
        {
            notify(s(R.string.error_out_of_memory), CommanderIf.OPERATION_FAILED);
        }
        return false;
    }

    private boolean isPositionValid(int position)
    {
        return items != null && position >= 0 && position <= items.length;
    }

    @Override
    public void openItem(int position)
    {
        if (position == 0)
        {
            if (parentLink == SLS)
            {
                commander.Navigate(Uri.parse(AdapterHome.DEFAULT_LOC), null);
            }
            else if (dirName != null)
            {
                Uri parent1 = FileUtils.getParentDirectory(getUri());
                Uri parent2 = parent1 == null ? Uri.parse(FileUtils.ASSET_RESOURCE_PREFIX) : parent1;
                commander.Navigate(parent2, null);
            }
        }
        else if (isPositionValid(position))
        {
            FileItem file = items[position - 1];
            if (file == null)
            {
                return;
            }
            if (file.dir)
            {
                commander.Navigate(getItemUri(position), null);
            }
            else
            {
                commander.Open(getItemUri(position));
            }
        }
    }

    @Override
    public Uri getItemUri(int position)
    {
        try
        {
            return Uri.parse(getItemName(position, true));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getItemName(int position, boolean full)
    {
        String retValue = null;
        if (!isPositionValid(position))
        {
            retValue = null;
        }
        if (full)
        {
            retValue = position == 0 ? toString() : toString() + items[position - 1].name;
        }
        else if (position == 0)
        {
            retValue = parentLink;
        }
        else
        {
            FileItem item = items[position - 1];
            String name = item.name;
            retValue = (name != null && item.dir) ? name.replace("/", "") : name;
        }
        return retValue;
    }

    @Override
    protected int getPredictedAttributesLength()
    {
        return 10; // "1024x1024"
    }

    /*
     *  ListAdapter implementation
     */

    @Override
    public int getCount()
    {
        if (items == null)
            return 1;
        return items.length + 1;
    }

    @Override
    public Object getItem(int position)
    {
        Item item = null;
        if (position == 0)
        {
            item = new Item();
            item.name = parentLink;
            item.dir = true;
        }
        else
        {
            if (items != null && position <= items.length)
            {
                synchronized (items)
                {
                    try
                    {
                        return items[position - 1];
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
            else
            {
                item = new Item();
                item.name = "???";
            }
        }
        return item;
    }

    @Override
    protected void reSort()
    {
        if (items == null)
            return;
        synchronized (items)
        {
            reSort(items);
        }
    }

    public void reSort(FileItem[] items_)
    {
        if (items_ == null)
            return;
        ItemComparator comp = new ItemComparator(mode & MODE_SORTING, true, ascending);
        Arrays.sort(items_, comp);
    }

    @Override
    public Uri getItemUri(String name)
    {
        if (items == null)
        {
            return null;
        }
        for (FileItem fi : items)
        {
            if (fi.name != null && fi.name.equals(name))
            {
                return Uri.parse(toString() + name);
            }
        }
        return null;
    }

}
