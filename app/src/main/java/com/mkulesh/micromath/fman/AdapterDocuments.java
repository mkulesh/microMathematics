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

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Document;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.mkulesh.micromath.R;
import com.mkulesh.micromath.utils.CompatUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@SuppressLint("NewApi")
public class AdapterDocuments extends AdapterBaseImpl
{
    public final static String ORG_SCHEME = ContentResolver.SCHEME_CONTENT;
    public final static String PREF_TREE_ROOT_URI = "fman_tree_root_uri";
    public final static int REQUEST_OPEN_DOCUMENT_TREE = 935;

    private final static String[] projection = { Document.COLUMN_DOCUMENT_ID, Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_LAST_MODIFIED, Document.COLUMN_MIME_TYPE, Document.COLUMN_SIZE };

    final static class SAFItem extends AdapterIf.Item
    {
        // empty
    }

    private Uri uri;
    private SAFItem[] items;

    public AdapterDocuments(Context ctx_)
    {
        super(ctx_, 0);
    }

    @Override
    public String getScheme()
    {
        return AdapterDocuments.ORG_SCHEME;
    }

    @NonNull
    @Override
    public String toString()
    {
        return AdapterDocuments.ORG_SCHEME + ":" + getPath(uri, true);
    }

    private static boolean isTreeUri(Uri uri)
    {
        final String PATH_TREE = "tree";
        final List<String> paths = uri.getPathSegments();
        return paths.size() == 2 && PATH_TREE.equals(paths.get(0));
    }

    private static boolean isRootDoc(Uri uri)
    {
        final List<String> paths = uri.getPathSegments();
        if (paths.size() < 4)
            return true;
        String last = paths.get(paths.size() - 1);
        return last.lastIndexOf(':') == last.length() - 1;
    }

    public static boolean isExternalStorageDocument(Uri uri)
    {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static String getPath(Uri u, boolean dir)
    {
        try
        {
            final List<String> paths = u.getPathSegments();
            if (paths.size() < 4)
                return null;
            String path_part = paths.get(3);
            int col_pos = path_part.lastIndexOf(':');
            String volume, path_root = null, sub_path, full_path;
            volume = paths.get(1);
            sub_path = path_part.substring(col_pos + 1);

            if (volume.startsWith("primary") && !CompatUtils.isROrLater())
            {
                return DEFAULT_DIR + "/" + sub_path;
            }
            else
            {
                try
                {
                    File probe;
                    volume = volume.substring(0, volume.length() - 1);
                    if (CompatUtils.isMarshMallowOrLater())
                    {
                        full_path = "/mnt/media_rw/" + volume + "/" + sub_path;
                        probe = new File(full_path);
                        if (dir ? probe.isDirectory() : probe.isFile())
                            return full_path;
                    }
                    else
                    {
                        path_root = FileUtils.getSecondaryStorage();
                        if (path_root != null)
                        {
                            full_path = FileUtils.mbAddSl(path_root) + sub_path;
                            probe = new File(full_path);
                            if (dir ? probe.isDirectory() : probe.isFile())
                                return full_path;
                        }
                    }
                }
                catch (Exception e)
                {
                    // empty
                }
                if (path_root == null)
                    path_root = volume; // better than nothing
            }
            return path_root + "/" + sub_path;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static Uri getParent(Uri u)
    {
        if (u == null) return null;
        final List<String> paths = u.getPathSegments();
        final int n = paths.size();
        if (n != 4) return null;
        String doc_segm = paths.get(3);
        int doc_col_pos = doc_segm.lastIndexOf(':');
        if (doc_col_pos < 0)
        { // not a system storage
            int last_sl_pos = doc_segm.lastIndexOf('/');
            if (last_sl_pos < 0)
                return null;
            doc_segm = doc_segm.substring(0, last_sl_pos);
            return u.buildUpon().path(null).appendPath(paths.get(0)).appendPath(paths.get(1)).appendPath(paths.get(2)).appendPath(doc_segm).build();
        }
        if (doc_col_pos == doc_segm.length() - 1)
            return null;    // already the top
        String doc_subpath = doc_segm.substring(doc_col_pos + 1);

        String tree_segm = paths.get(1);
        int tree_col_pos = tree_segm.lastIndexOf(':');
        if (tree_col_pos > 0)
        {
            String tree_subpath = tree_segm.substring(tree_col_pos + 1);
            if (tree_subpath.equals(doc_subpath))
            {  // will that work? modifying the tree path...
                int sl_pos = tree_subpath.lastIndexOf(SLC);
                tree_subpath = sl_pos > 0 ? tree_subpath.substring(0, sl_pos) : "";
                tree_segm = tree_segm.substring(0, tree_col_pos + 1) + tree_subpath;
            }
        }
        int sl_pos = doc_subpath.lastIndexOf(SLC);
        doc_subpath = sl_pos > 0 ? doc_subpath.substring(0, sl_pos) : "";
        doc_segm = doc_segm.substring(0, doc_col_pos + 1) + doc_subpath;
        return u.buildUpon().path(null).appendPath(paths.get(0)).appendPath(tree_segm).appendPath(paths.get(2)).appendPath(doc_segm).build();
    }

    @Override
    public Uri getUri()
    {
        return uri;
    }

    @Override
    public void setUri(Uri uri_)
    {
        if (this.uri == null && isTreeUri(uri_))
        {
            try
            {
                ctx.getContentResolver().takePersistableUriPermission(uri_,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            this.uri = DocumentsContract.buildDocumentUriUsingTree(uri_, DocumentsContract.getTreeDocumentId(uri_));
        }
        else
        {
            this.uri = uri_;
        }
    }

    private ArrayList<SAFItem> getChildren(Uri u)
    {
        return getChildren(ctx, u);
    }

    private static ArrayList<SAFItem> getChildren(Context ctx, Uri u)
    {
        Cursor c = null;
        try
        {
            ContentResolver cr = ctx.getContentResolver();
            String document_id = DocumentsContract.getDocumentId(u);
            Uri children_uri = DocumentsContract.buildChildDocumentsUriUsingTree(u, document_id);
            c = cr.query(children_uri, projection, null, null, null);
            if (c != null)
            {
                ArrayList<SAFItem> tmp_list = new ArrayList<>();
                if (c.getCount() == 0)
                {
                    return tmp_list;
                }
                int ici = c.getColumnIndex(Document.COLUMN_DOCUMENT_ID);
                int nci = c.getColumnIndex(Document.COLUMN_DISPLAY_NAME);
                int sci = c.getColumnIndex(Document.COLUMN_SIZE);
                int mci = c.getColumnIndex(Document.COLUMN_MIME_TYPE);
                int dci = c.getColumnIndex(Document.COLUMN_LAST_MODIFIED);
                c.moveToFirst();
                do
                {
                    SAFItem item = new SAFItem();
                    String id = c.getString(ici);
                    item.origin = DocumentsContract.buildDocumentUriUsingTree(u, id);
                    item.attr = c.getString(mci);
                    item.dir = Document.MIME_TYPE_DIR.equals(item.attr);
                    item.name = c.getString(nci);
                    item.size = c.getLong(sci);
                    item.date = new Date(c.getLong(dci));
                    if (item.dir)
                    {
                        item.size = -1;
                        item.attr = getDirItemsNumber(ctx, (Uri) item.origin) + " "
                                + ctx.getString(R.string.dialog_list_items);
                    }
                    tmp_list.add(item);
                }
                while (c.moveToNext());
                return tmp_list;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (c != null)
            {
                c.close();
            }
        }
        return null;
    }

    private static int getDirItemsNumber(Context ctx, Uri u)
    {
        int itemsNumber = 0;
        Cursor c = null;
        try
        {
            ContentResolver cr = ctx.getContentResolver();
            String dirId = DocumentsContract.getDocumentId(u);
            Uri dirUri = DocumentsContract.buildChildDocumentsUriUsingTree(u, dirId);
            c = cr.query(dirUri, projection, null, null, null);
            if (c != null)
            {
                itemsNumber = c.getCount();
            }
        }
        catch (Exception e)
        {
            // notning to do;
        }
        finally
        {
            if (c != null)
            {
                c.close();
            }
        }
        return itemsNumber;
    }

    @Override
    public void readSource(Uri tmp_uri, String pass_back_on_done)
    {
        try
        {
            if (tmp_uri != null)
            {
                setUri(tmp_uri);
            }
            if (uri == null)
            {
                return;
            }
            ArrayList<SAFItem> tmp_list = getChildren(uri);
            if (tmp_list == null)
            {
                commander.Navigate(Uri.parse(AdapterHome.DEFAULT_LOC), null);
                return;
            }
            items = new SAFItem[tmp_list.size()];
            tmp_list.toArray(items);
            reSort(items);
            super.setCount(items.length);
            parentLink = isRootDoc(uri) ? SLS : PLS;
            notifyDataSetChanged();
            notify(pass_back_on_done);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        catch (OutOfMemoryError err)
        {
            notify(s(R.string.error_out_of_memory), CommanderIf.OPERATION_FAILED);
        }
    }

    @Override
    public void openItem(int position)
    {
        if (position == 0)
        {
            Uri uri_to_go = null;
            if (parentLink == SLS)
                uri_to_go = Uri.parse(AdapterHome.DEFAULT_LOC);
            else
            {
                //0000-0000:folder%2Fsubfolder
                uri_to_go = getParent(uri);
                if (uri_to_go == null)
                    uri_to_go = Uri.parse(AdapterHome.DEFAULT_LOC);
            }
            String pos_to = null;
            String cur_path = getPath(uri, true);
            if (cur_path != null)
                pos_to = cur_path.substring(cur_path.lastIndexOf('/'));
            commander.Navigate(uri_to_go, pos_to);
        }
        else
        {
            Item item = items[position - 1];
            if (item.dir)
                commander.Navigate((Uri) item.origin, null);
            else
            {
                Uri to_open;
                String full_name = getItemName(position, true);
                if (full_name != null && full_name.charAt(0) == '/')
                    to_open = Uri.parse(FileUtils.escapePath(full_name));
                else
                    to_open = (Uri) item.origin;
                commander.Open(to_open);
            }
        }
    }

    @Override
    public Uri getItemUri(int position)
    {
        try
        {
            return (Uri) items[position - 1].origin;
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
        if (position == 0)
            return parentLink;
        if (position < 0 || items == null || position > items.length)
            return null;
        SAFItem item = items[position - 1];
        if (full)
        {
            Uri item_uri = (Uri) item.origin;
            return getPath(item_uri, item.dir);
        }
        else
            return item.name;
    }

    @Override
    public void renameItem(int position, String newName)
    {
        ContentResolver cr = ctx.getContentResolver();
        Item item = items[position - 1];
        Uri new_uri = null;
        try
        {
            new_uri = DocumentsContract.renameDocument(cr, (Uri) item.origin, newName);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return;
        }
        if (new_uri == null)
        {
            return;
        }
        item.origin = new_uri;
        notifyRefr(newName);
    }

    @Override
    public void createFolder(String new_name)
    {
        try
        {
            Uri new_uri = DocumentsContract.createDocument(ctx.getContentResolver(), uri, Document.MIME_TYPE_DIR,
                    new_name);
            if (new_uri != null)
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
        Item[] list = new Item[1];
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

    static class DeleteEngine extends Engine
    {
        private final AdapterDocuments a;
        private final Item[] mList;

        DeleteEngine(AdapterDocuments a, Handler h, Item[] list)
        {
            setHandler(h);
            this.a = a;
            mList = list;
        }

        void run()
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

        private int deleteFiles(Item[] l) throws Exception
        {
            int cnt = 0;
            for (Item item : l)
            {
                DocumentsContract.deleteDocument(a.ctx.getContentResolver(), (Uri) item.origin);
                cnt++;
            }
            return cnt;
        }
    }

    @Override
    protected int getPredictedAttributesLength()
    {
        return 24; // "application/octet-stream"
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
                return items[position - 1];
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

    private void reSort(Item[] items_)
    {
        if (items_ == null)
            return;
        ItemComparator comp = new ItemComparator(mode & MODE_SORTING, true, ascending);
        Arrays.sort(items_, comp);
    }

    public static void saveTreeRootURI(Context ctx, Uri uri)
    {
        SharedPreferences saf_sp = PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = saf_sp.edit();
        editor.putString(PREF_TREE_ROOT_URI, uri != null ? uri.toString() : null);
        if (uri != null)
        {
            editor.putString(Commander.PREF_LAST_SELECTED_PATH, uri.toString());
        }
        editor.commit();
    }

    @Override
    public Uri newFile(String fileName)
    {
        try
        {
            Uri curr = getUri();
            String mime = FileUtils.getMimeByExt(FileUtils.getFileExt(fileName), "*/*");
            return DocumentsContract.createDocument(ctx.getContentResolver(), curr, mime, fileName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Uri getItemUri(String name)
    {
        for (SAFItem fi : items)
        {
            if (fi.name != null && fi.name.equals(name))
            {
                return (Uri) fi.origin;
            }
        }
        return null;
    }

    public static Uri withAppendedPath(Context ctx, Uri parent, String name)
    {
        ArrayList<SAFItem> entries = getChildren(ctx, parent);
        for (SAFItem fi : entries)
        {
            if (fi.name != null && fi.name.equals(name))
            {
                return (Uri) fi.origin;
            }
        }
        return null;
    }
}
