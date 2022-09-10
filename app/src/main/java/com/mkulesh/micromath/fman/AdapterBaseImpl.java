/*
 * microMathematics - Extended Visual Calculator
 * Copyright (C) 2014-2022 by Mikhail Kulesh
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details. You should have received a copy of the GNU General
 * Public License along with this program.
 */
package com.mkulesh.micromath.fman;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mkulesh.micromath.R;

import java.io.File;
import java.util.Date;

import androidx.annotation.NonNull;

public abstract class AdapterBaseImpl extends BaseAdapter implements AdapterIf
{
    static final String DEFAULT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();

    static final String SLS = File.separator;
    static final char SLC = File.separatorChar;
    public static final String PLS = "..";

    final Context ctx;
    private final LayoutInflater mInflater;
    private final java.text.DateFormat localeDateFormat;
    private final java.text.DateFormat localeTimeFormat;
    private final float density;

    CommanderIf commander = null;

    int mode = 0;
    boolean ascending = true;
    String parentLink = SLS;
    private int numItems = 0;

    boolean readWriteAdapter = true;

    static class SimpleHandler extends Handler
    {
        final CommanderIf cmd;

        SimpleHandler(CommanderIf c)
        {
            cmd = c;
        }

        @Override
        public void handleMessage(@NonNull Message msg)
        {
            try
            {
                cmd.notifyMe(msg);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    SimpleHandler simpleHandler = null;

    AdapterBaseImpl(Context ctx_, int mode_)
    {
        ctx = ctx_;
        mode = mode_;
        mInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        localeDateFormat = DateFormat.getDateFormat(ctx);
        localeTimeFormat = DateFormat.getTimeFormat(ctx);
        density = ctx.getResources().getDisplayMetrics().density;
    }

    /**
     * Implementation of BaseAdapter interface
     */

    @Override
    public int getCount()
    {
        return numItems;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Item item = (Item) getItem(position);
        if (item == null)
            return null;
        return getView(convertView, parent, item);
    }

    /**
     * Implementation of CommanderAdapter interface
     */

    @Override
    public void Init(CommanderIf c)
    {
        if (c != null)
        {
            commander = c;
            simpleHandler = new SimpleHandler(commander);
        }
    }

    @Override
    public void setUri(Uri uri)
    {
        // nothing to do
    }

    @Override
    public int setMode(int mask, int val)
    {
        mode &= ~mask;
        mode |= val;
        if ((mask & MODE_SORT_DIR) != 0 || (mask & MODE_SORTING) != 0)
        {
            if ((mask & MODE_SORT_DIR) != 0)
                ascending = (val & MODE_SORT_DIR) == SORT_ASC;
            reSort();
            notifyDataSetChanged();
        }
        return mode;
    }

    @Override
    public int getMode()
    {
        return mode;
    }

    @Override
    public void populateContextMenu(ContextMenu menu, AdapterView.AdapterContextMenuInfo acmi, int num)
    {
        try
        {
            if (readWriteAdapter)
            {
                menu.add(0, R.id.fman_action_refresh, 0, R.string.fman_refresh_title);
                menu.add(0, R.id.fman_action_create_folder, 0, R.string.fman_create_folder_title);
                if (num > 0)
                {
                    menu.add(0, R.id.fman_action_rename, 0, R.string.fman_rename_title);
                    menu.add(0, R.id.fman_action_delete, 0, R.string.fman_delete_title);
                }
            }
            MenuItem activeSort = null;
            menu.add(0, R.id.fman_action_sort_by_name, 0, R.string.fman_sort_by_name);
            if ((mode & MODE_SORTING) == SORT_NAME)
            {
                activeSort = menu.findItem(R.id.fman_action_sort_by_name);
            }
            menu.add(0, R.id.fman_action_sort_by_ext, 0, R.string.fman_sort_by_ext);
            if ((mode & MODE_SORTING) == SORT_EXT)
            {
                activeSort = menu.findItem(R.id.fman_action_sort_by_ext);
            }
            menu.add(0, R.id.fman_action_sort_by_size, 0, R.string.fman_sort_by_size);
            if ((mode & MODE_SORTING) == SORT_SIZE)
            {
                activeSort = menu.findItem(R.id.fman_action_sort_by_size);
            }
            menu.add(0, R.id.fman_action_sort_by_date, 0, R.string.fman_sort_by_date);
            if ((mode & MODE_SORTING) == SORT_DATE)
            {
                activeSort = menu.findItem(R.id.fman_action_sort_by_date);
            }
            if (activeSort != null)
            {
                activeSort.setCheckable(true);
                activeSort.setChecked(true);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public Uri getItemUri(int position)
    {
        return null;
    }

    @Override
    public void renameItem(int position, String newName)
    {
        // nothing to do
    }

    @Override
    public void createFolder(String new_name)
    {
        // nothing to do
    }

    @Override
    public boolean deleteItem(int position)
    {
        return false;
    }

    @Override
    public void doIt(int command_id, int position)
    {
        // to be implemented in derived classes
    }

    @Override
    public Uri newFile(String fileName)
    {
        return null;
    }

    @Override
    public Uri getItemUri(String name)
    {
        return null;
    }

    /**
     * Implementation internal functionality
     */

    private void notify(String s, String cookie)
    {
        if (simpleHandler == null)
            return;
        Message msg = simpleHandler.obtainMessage(s != null ? CommanderIf.OPERATION_FAILED
                : CommanderIf.OPERATION_COMPLETED, s);
        if (msg != null)
        {
            Bundle b = new Bundle();
            b.putString(CommanderIf.NOTIFY_COOKIE, cookie);
            msg.setData(b);
            msg.sendToTarget();
        }
    }

    void notify(String cookie)
    {
        notify(null, cookie);
    }

    private void notify(String s, int what, int arg1)
    {
        Message msg = Message.obtain(simpleHandler, what, arg1, -1, s);
        if (msg != null)
            msg.sendToTarget();
    }

    void notify(String s, int what)
    {
        notify(s, what, -1);
    }

    void notifyRefr(String item_name)
    {
        Message msg = simpleHandler.obtainMessage(CommanderIf.OPERATION_COMPLETED_REFRESH_REQUIRED, null);
        if (msg != null)
        {
            Bundle b = new Bundle();
            b.putString(CommanderIf.NOTIFY_POSTO, item_name);
            msg.setData(b);
            msg.sendToTarget();
        }
    }

    void setCount(int n)
    {
        numItems = n;
        notifyDataSetChanged();
    }

    private String getLocalDateTimeStr(Date date)
    {
        try
        {
            return localeDateFormat.format(date) + " " + localeTimeFormat.format(date);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return "(ERR)";
    }

    int getPredictedAttributesLength()
    {
        return 0;
    }

    View getView(View convertView, ViewGroup parent, Item item)
    {
        View row_view = null;
        try
        {
            final int parent_width = parent.getWidth();
            final boolean ao = (ATTR_ONLY & mode) != 0;
            final int icoWidth = (int) (density * ctx.getResources().getDimensionPixelSize(R.dimen.fman_item_icon_size));
            int sizeWidth = 0, dateWidth = 0, attrWidth = 0;

            if (convertView == null)
            {
                row_view = mInflater.inflate(R.layout.fman_file_item, parent, false);
            }
            else
            {
                row_view = convertView;
                row_view.setBackgroundColor(0); // transparent
            }

            ImageView imgView = row_view.findViewById(R.id.fman_fld_icon);
            TextView nameView = row_view.findViewById(R.id.fman_fld_name);
            TextView attrView = row_view.findViewById(R.id.fman_fld_attr);
            TextView dateView = row_view.findViewById(R.id.fman_fld_date);
            TextView sizeView = row_view.findViewById(R.id.fman_fld_size);

            String name = item.name, size = "", date = "";

            if (item.size >= 0)
                size = FileUtils.getHumanSize(item.size);
            if (item.date != null)
            {
                date = getLocalDateTimeStr(item.date);
            }

            if (ao)
            {
                sizeWidth = 0;
                dateWidth = 0;
                attrWidth = parent_width - icoWidth;
            }
            else
            {
                if (dateView != null)
                {
                    String sample_date = "M" + getLocalDateTimeStr(new Date(0));
                    dateWidth = (int) dateView.getPaint().measureText(sample_date) + 2;
                }
                if (sizeView != null)
                {
                    sizeWidth = (int) sizeView.getPaint().measureText("99999.9M") + 2;
                }
                if (attrView != null)
                {
                    // sizeWidth is pixels, but in what units the return of measureText() ???
                    int al = getPredictedAttributesLength();
                    if (al > 0)
                    {
                        attrWidth = parent_width - sizeWidth - dateWidth - icoWidth;
                    }
                    else
                    {
                        attrWidth = 0;
                    }
                }
            }

            if (imgView != null)
            {
                if (icoWidth > 0)
                {
                    imgView.setVisibility(View.VISIBLE);
                    {
                        try
                        {
                            //imgView.setMaxWidth( img_width );
                            RelativeLayout.LayoutParams rllp = (RelativeLayout.LayoutParams) imgView.getLayoutParams();
                            rllp.width = icoWidth;
                            rllp.height = icoWidth;
                            int ico_id;
                            if (item.icon_id != -1)
                                ico_id = item.icon_id;
                            else if (SLS.equals(item.name) || PLS.equals(item.name) || item.dir)
                                ico_id = R.drawable.fman_folder;
                            else
                                ico_id = getIconId(name);
                            imgView.setImageResource(ico_id);
                        }
                        catch (OutOfMemoryError e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
                else
                {
                    imgView.setVisibility(View.GONE);
                }
            }

            if (nameView != null)
            {
                nameView.setText(name != null ? name : "???");
            }
            if (dateView != null)
            {
                final boolean vis = !ao && (dateWidth > 0);
                dateView.setVisibility(vis ? View.VISIBLE : View.GONE);
                if (vis)
                {
                    dateView.setWidth(dateWidth);
                    dateView.setText(date);
                }
            }
            if (sizeView != null)
            {
                final boolean vis = !ao && (sizeWidth > 0);
                sizeView.setVisibility(vis ? View.VISIBLE : View.GONE);
                if (vis)
                {
                    sizeView.setWidth(sizeWidth);
                    sizeView.setText(size);
                }
            }
            if (attrView != null)
            {
                final boolean vis = attrWidth > 0;
                attrView.setVisibility(vis ? View.VISIBLE : View.GONE);
                if (vis)
                {
                    String attr_text = item.attr != null ? item.attr.trim() : "";
                    RelativeLayout.LayoutParams rllp = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    rllp.addRule(RelativeLayout.BELOW, R.id.fman_fld_name);
                    rllp.addRule(RelativeLayout.ALIGN_LEFT, R.id.fman_fld_name);
                    rllp.addRule(RelativeLayout.ALIGN_TOP, R.id.fman_fld_size);
                    attrView.setGravity(0x03); // LEFT
                    attrView.setLayoutParams(rllp);
                    if (FileUtils.str(item.attr))
                    {
                        attrView.setWidth(attrWidth);
                        attrView.setText(attr_text);
                    }
                    else
                    {
                        attrView.setWidth(0);
                        attrView.setText(attr_text);
                    }
                }
            }

            row_view.setTag(null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return row_view;
    }

    private static int getIconId(String file)
    {
        String cat = FileUtils.getCategoryByExt(FileUtils.getFileExt(file));
        if (FileUtils.C_UNKNOWN.equals(cat))
            return R.drawable.fman_file_unknown;
        if (FileUtils.C_AUDIO.equals(cat))
            return R.drawable.fman_file_audio;
        if (FileUtils.C_VIDEO.equals(cat))
            return R.drawable.fman_file_video;
        if (FileUtils.C_IMAGE.equals(cat))
            return R.drawable.fman_file_image;
        if (FileUtils.C_TEXT.equals(cat))
            return R.drawable.fman_file_text;
        if (FileUtils.C_BOOK.equals(cat))
            return R.drawable.fman_file_doc;
        if (FileUtils.C_OFFICE.equals(cat))
            return R.drawable.fman_file_doc;
        if (FileUtils.C_PDF.equals(cat))
            return R.drawable.fman_file_pdf;
        if (FileUtils.C_ZIP.equals(cat))
            return R.drawable.fman_file_zip;
        if (FileUtils.C_MARKUP.equals(cat))
            return R.drawable.fman_file_xml;
        if (FileUtils.C_APP.equals(cat))
            return R.drawable.fman_file_sys;
        if (FileUtils.C_DROID.equals(cat))
            return R.drawable.fman_file_apk;
        if (FileUtils.C_MICROMATH.equals(cat))
            return R.drawable.fman_file_mmt;
        if (FileUtils.C_SMATH_STUDIO.equals(cat))
            return R.drawable.fman_file_sm;
        return R.drawable.fman_file_unknown;
    }

    void reSort()
    {
        // to override by all the derives
    }

    final String s(int r_id)
    {
        return ctx.getString(r_id);
    }
}
