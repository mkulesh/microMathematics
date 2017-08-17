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
package com.mkulesh.micromath.fman;

import java.io.File;
import java.util.Date;

public class FileItem extends AdapterIf.Item
{
    public FileItem(String name)
    {
        this(new File(name));
    }

    public FileItem(File f)
    {
        origin = f;
        dir = f.isDirectory();
        name = f.getName();
        if (!dir)
        {
            size = f.length();
        }
        long msFileDate = f.lastModified();
        if (msFileDate != 0)
        {
            date = new Date(msFileDate);
        }
    }

    public File f()
    {
        return origin != null ? (File) origin : null;
    }
}
