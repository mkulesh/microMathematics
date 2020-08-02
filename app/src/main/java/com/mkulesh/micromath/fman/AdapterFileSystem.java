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

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.system.Os;

import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.CompatUtils;

import java.io.File;
import java.util.Arrays;

public class AdapterFileSystem extends AdapterBaseImpl
{
    public final static String ORG_SCHEME = "file";
    protected static final boolean HIDE_HIDDEN = false;

    private String dirName;
    protected FileItem[] items;

    public AdapterFileSystem(Context ctx_)
    {
        super(ctx_, 0);
        dirName = null;
        items = null;
    }

    @Override
    public String getScheme()
    {
        return "";
    }

    @Override
    public String toString()
    {
        return FileUtils.mbAddSl(dirName);
    }

    public String getDir()
    {
        return dirName;
    }

    /*
     * CommanderAdapter implementation
     */

    @Override
    public Uri getUri()
    {
        try
        {
            return Uri.parse(FileUtils.escapePath(toString()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void setUri(Uri uri)
    {
        String schm = uri.getScheme();
        if (FileUtils.str(schm) && !AdapterFileSystem.ORG_SCHEME.equals(schm))
            return;
        dirName = FileUtils.mbAddSl(uri.getPath());
    }

    @Override
    public boolean readSource(Uri d, String pass_back_on_done)
    {
        try
        {
            if (d != null)
            {
                if (d.getScheme() != null && !ORG_SCHEME.equals(d.getScheme()))
                {
                    dirName = DEFAULT_DIR;
                }
                else
                {
                    dirName = d.getPath();
                }
            }
            if (dirName == null)
            {
                notify(ctx.getString(R.string.fman_error_no_such_folder, (d == null ? "null" : d.toString())),
                        CommanderIf.OPERATION_FAILED);
                return false;
            }
            ListEngine reader = new ListEngine(this, simpleHandler, pass_back_on_done);
            reader.run();
            {
                File dir = reader.getDirFile();
                if (dir != null)
                {
                    dirName = dir.getAbsolutePath();
                    items = filesToItems(reader.getFiles());
                    parentLink = dir.getParent() == null ? SLS : PLS;
                    notifyDataSetChanged();
                }
            }
            return true;
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

    @SuppressLint("NewApi")
    protected FileItem[] filesToItems(File[] files_)
    {
        int num_files = files_.length;
        int num = num_files;
        if (HIDE_HIDDEN)
        {
            int cnt = 0;
            for (int i = 0; i < num_files; i++)
                if (!files_[i].isHidden())
                    cnt++;
            num = cnt;
        }
        FileItem[] items_ = new FileItem[num];
        int j = 0;
        for (int i = 0; i < num_files; i++)
        {
            File f = files_[i];
            if (!f.isHidden() || !HIDE_HIDDEN)
            {
                String fn = null;
                // follow soft link
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    String link_target = null;
                    try
                    {
                        link_target = Os.readlink(f.getAbsolutePath());
                    }
                    catch (Exception e)
                    {
                        // empty
                    }
                    if (link_target != null)
                    {
                        fn = f.getName();
                        f = new File(link_target);
                    }
                }
                FileItem f_item = new FileItem(f);
                if (fn != null)
                {
                    f_item.name = fn;
                    f_item.icon_id = f_item.dir ? R.drawable.fman_folder : R.drawable.fman_file_unknown;
                }
                // fill attributes
                if (f_item.dir)
                {
                    final File[] subFiles = ListEngine.listDirWithEmulated(f);
                    if (subFiles == null || subFiles.length == 0)
                    {
                        f_item.attr = "0 " + ctx.getString(R.string.dialog_list_items);
                    }
                    else
                    {
                        f_item.attr = subFiles.length + " "
                                + ctx.getString(R.string.dialog_list_items);
                    }
                }
                else
                {
                    f_item.attr = FileUtils.getMimeByExt(FileUtils.getFileExt(f_item.name), "");
                }
                items_[j++] = f_item;
            }
        }
        reSort(items_);
        return items_;
    }

    @Override
    public void openItem(int position)
    {
        if (position == 0)
        {
            if (parentLink == SLS)
                commander.Navigate(Uri.parse(AdapterHome.DEFAULT_LOC), null);
            else
            {
                if (dirName == null)
                    return;
                File cur_dir_file = new File(dirName);
                String parent_dir = cur_dir_file.getParent();
                commander.Navigate(Uri.parse(FileUtils.escapePath(parent_dir != null ? parent_dir : DEFAULT_DIR)),
                        cur_dir_file.getName());
            }
        }
        else
        {
            File file = items[position - 1].f();
            if (file == null)
                return;
            Uri open_uri = Uri.parse(FileUtils.escapePath(file.getAbsolutePath()));
            if (file.isDirectory())
                commander.Navigate(open_uri, null);
            else
                commander.Open(open_uri);
        }
    }

    @Override
    public Uri getItemUri(int position)
    {
        try
        {
            String item_name = getItemName(position, true);
            return Uri.parse(FileUtils.escapePath(item_name));
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
        if (position < 0 || items == null || position > items.length)
            return position == 0 ? parentLink : null;
        if (full)
            return position == 0 ? (new File(dirName)).getParent() : items[position - 1].f().getAbsolutePath();
        else
        {
            if (position == 0)
                return parentLink;
            FileItem item = items[position - 1];
            String name = item.name;
            if (name != null && item.dir)
            {
                return name.replace("/", "");
            }
            else
                return name;
        }
    }

    @Override
    public boolean renameItem(int position, String newName)
    {
        if (position <= 0 || position > items.length)
            return false;
        try
        {
            boolean ok = false;
            File f = items[position - 1].f();
            File new_file = new File(dirName, newName);
            if (new_file.exists())
            {
                if (f.equals(new_file))
                {
                    commander.showError(ctx.getString(R.string.fman_rename_error, f.getName()));
                    return false;
                }
                String old_ap = f.getAbsolutePath();
                String new_ap = new_file.getAbsolutePath();
                if (old_ap.equalsIgnoreCase(new_ap))
                {
                    File tmp_file = new File(dirName, newName + "_TMP_");
                    ok = f.renameTo(tmp_file);
                    ok = tmp_file.renameTo(new_file);
                }
                else
                {
                    commander.showError(ctx.getString(R.string.fman_rename_error, f.getName()));
                    return false;
                }
            }
            else
                ok = f.renameTo(new_file);
            if (ok)
                notifyRefr(newName);
            else
                notify(ctx.getString(R.string.fman_rename_error, f.getName()), CommanderIf.OPERATION_FAILED);
            return ok;
        }
        catch (SecurityException e)
        {
            commander.showError(ctx.getString(R.string.fman_error_sec_err, e.getMessage()));
            return false;
        }
    }

    @Override
    public void createFolder(String new_name)
    {
        try
        {
            if ((new File(dirName, new_name)).mkdir())
            {
                notifyRefr(new_name);
                return;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        notify(ctx.getString(R.string.fman_create_folder_error, new_name), CommanderIf.OPERATION_FAILED);
    }

    @Override
    public boolean deleteItem(int position)
    {
        if (position <= 0 || position > items.length)
        {
            return false;
        }
        FileItem[] list = new FileItem[1];
        list[0] = items[position - 1];
        try
        {
            DeleteEngine deleter = new DeleteEngine(this, simpleHandler, list);
            deleter.run();
        }
        catch (Exception e)
        {
            notify(e.getMessage(), CommanderIf.OPERATION_FAILED);
        }
        return false;
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
    public Uri newFile(String fileName)
    {
        Uri curr = getUri();
        return Uri.fromFile(new File(curr.getPath() + File.separator + fileName));
    }

    private static class ListEngine extends Engine
    {
        private String pass_back_on_done;
        private AdapterFileSystem a;
        private File[] files_ = null;
        private File dir = null;

        ListEngine(AdapterFileSystem a, Handler h, String pass_back_on_done_)
        {
            setHandler(h);
            this.a = a;
            pass_back_on_done = pass_back_on_done_;
        }

        public File getDirFile()
        {
            return dir;
        }

        public File[] getFiles()
        {
            return files_;
        }

        public void run()
        {
            String dir_name = a.getDir();
            while (true)
            {
                dir = new File(dir_name);
                files_ = listDirWithEmulated(dir);

                if (files_ != null)
                    break;
                if (dir == null || (dir_name = dir.getParent()) == null)
                {
                    sendProgress(a.ctx.getString(R.string.fman_error_no_such_folder, dir_name),
                            CommanderIf.OPERATION_FAILED, pass_back_on_done);
                    return;
                }
            }
            sendProgress(null, CommanderIf.OPERATION_COMPLETED, pass_back_on_done);
        }

        public static File[] listDirWithEmulated(File f)
        {
            File[] list = f.listFiles();

            if (list == null && CompatUtils.isExternalStorageEmulated())
            {
                final String currentPath = FileUtils.mbAddSl(f.getAbsolutePath());
                if (DEFAULT_DIR.startsWith(currentPath))
                {
                    final String suffix = DEFAULT_DIR.replace(currentPath, "");
                    if (suffix != null && !suffix.contains("/"))
                    {
                        list = new File[1];
                        list[0] = new File(DEFAULT_DIR);
                    }
                }
            }
            return list;
        }
    }

    private static class DeleteEngine extends Engine
    {
        private AdapterFileSystem a;
        private File[] mList;

        DeleteEngine(AdapterFileSystem a, Handler h, FileItem[] list)
        {
            setHandler(h);
            this.a = a;
            mList = new File[list.length];
            for (int i = 0; i < list.length; i++)
                mList[i] = list[i].f();
        }

        public void run()
        {
            if (mList == null || mList.length == 0)
            {
                return;
            }
            try
            {
                int cnt = deleteFiles(mList);
                if (cnt == 0)
                {
                    sendError();
                }
                else
                {
                    sendResult(a.ctx.getString(R.string.fman_delete_confirm));
                }
            }
            catch (Exception e)
            {
                sendProgress(e.getMessage(), CommanderIf.OPERATION_FAILED_REFRESH_REQUIRED);
            }
        }

        private final int deleteFiles(File[] l) throws Exception
        {
            int cnt = 0;
            for (int i = 0; i < l.length; i++)
            {
                File f = l[i];
                if (f.isDirectory() && f.listFiles() != null)
                {
                    cnt += deleteFiles(f.listFiles());
                }
                if (f.delete())
                {
                    cnt++;
                }
                else
                {
                    error(a.ctx.getString(R.string.fman_delete_error, f.getName()));
                    break;
                }
            }
            return cnt;
        }
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
                return Uri.fromFile(fi.f());
            }
        }
        return null;
    }

}
