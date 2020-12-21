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
import android.view.ContextMenu;
import android.widget.AdapterView;

import java.util.Date;

public interface AdapterIf
{
    /**
     * An instance of the following Item class to be returned by ListAdapter's getItem() override
     */
    class Item
    {
        public String name = "";
        public Date date = null;
        public long size = -1;
        public boolean dir = false;
        String attr = "";
        public Object origin = null;
        int icon_id = -1;

        public Item()
        {
            // empty
        }
    }

    /**
     * Output modes.
     */
    int MODE_SORTING = 0x0030, SORT_NAME = 0x0000, SORT_SIZE = 0x0010, SORT_DATE = 0x0020,
            SORT_EXT = 0x0030, MODE_SORT_DIR = 0x0040, SORT_ASC = 0x0000, SORT_DSC = 0x0040, MODE_ATTR = 0x0300,
            SHOW_ATTR = 0x0100, ATTR_ONLY = 0x0200, MODE_ROOT = 0x0400, BASIC_MODE = 0x0000;

    /**
     * To initialize an adapter, the adapter creator calls the Init() method. This needed because only the default
     * constructor can be called during a creation of a foreign packaged class
     */
    void Init(CommanderIf c);

    /**
     * @return the scheme the adapter implements
     */
    String getScheme();

    /**
     * Just passive set the current URI without an attempt to obtain the list or the similar
     */
    void setUri(Uri uri);

    /**
     * Retrieve the current URI from the adapter
     */
    Uri getUri();

    /**
     * To set the desired adapter mode or pass some extra data.
     */
    int setMode(int mask, int mode);

    /**
     * @return current adapter's mode bits
     */
    int getMode();

    /**
     * Called when the user taps and holds on an item
     */
    void populateContextMenu(ContextMenu menu, AdapterView.AdapterContextMenuInfo acmi, int num);

    /**
     * The "main" method to obtain the current adapter's content
     */
    void readSource(Uri uri, String pass_back_on_done);

    /**
     * Tries to do something with the item
     */
    void openItem(int position);

    /**
     * Return the name of an item at the specified position
     */
    String getItemName(int position, boolean full);

    /**
     * Return the URI of an item at the specified position
     */
    Uri getItemUri(int position);

    /**
     * Renames an item
     */
    void renameItem(int position, String newName);

    /**
     * Creates new directory
     */
    void createFolder(String name);

    /**
     * Deletes the item from file system
     */
    boolean deleteItem(int position);

    /**
     * Performs a command
     */
    void doIt(int command_id, int position);

    /**
     * Returns Uri that corresponds to the file that shall be newly created
     */
    Uri newFile(String fileName);

    /**
     * Returns Uri of the item with given name
     */
    Uri getItemUri(String name);
}
