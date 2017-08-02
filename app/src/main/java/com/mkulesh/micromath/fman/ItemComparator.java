/*******************************************************************************
 * micro Mathematics - Extended visual calculator
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
package com.mkulesh.micromath.fman;

import java.util.Comparator;

import com.mkulesh.micromath.fman.AdapterIf.Item;

public class ItemComparator implements Comparator<Item>
{
    int type;
    boolean case_ignore, ascending;

    public ItemComparator(int type_, boolean case_ignore_, boolean ascending_)
    {
        type = type_;
        case_ignore = case_ignore_ && (type_ == AdapterIf.SORT_EXT || type_ == AdapterIf.SORT_NAME);
        ascending = ascending_;
    }

    @Override
    public int compare(Item f1, Item f2)
    {
        boolean f1IsDir = f1.dir;
        boolean f2IsDir = f2.dir;
        if (f1IsDir != f2IsDir)
            return f1IsDir ? -1 : 1;
        int ext_cmp = 0;
        switch (type)
        {
        case AdapterIf.SORT_EXT:
            ext_cmp = case_ignore ? FileUtils.getFileExt(f1.name).compareToIgnoreCase(FileUtils.getFileExt(f2.name))
                    : FileUtils.getFileExt(f1.name).compareTo(FileUtils.getFileExt(f2.name));
            break;
        case AdapterIf.SORT_SIZE:
            ext_cmp = f1.size - f2.size < 0 ? -1 : 1;
            break;
        case AdapterIf.SORT_DATE:
            if (f1.date != null && f2.date != null)
                ext_cmp = f1.date.compareTo(f2.date);
            break;
        }
        if (ext_cmp == 0)
            ext_cmp = case_ignore ? f1.name.compareToIgnoreCase(f2.name) : f1.name.compareTo(f2.name);
        return ascending ? ext_cmp : -ext_cmp;
    }
}
