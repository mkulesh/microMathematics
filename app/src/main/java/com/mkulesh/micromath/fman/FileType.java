/*
 * microMathematics - Extended Visual Calculator
 * Copyright (C) 2014-2021 by Mikhail Kulesh
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

import com.mkulesh.micromath.R;

public enum FileType
{
    PNG_IMAGE(R.string.fman_file_type_png_image),
    JPEG_IMAGE(R.string.fman_file_type_jpeg_image),
    LATEX(R.string.fman_file_type_latex),
    MATHJAX(R.string.fman_file_type_mathjax);

    private final int descriptionId;

    public int getDescriptionId()
    {
        return descriptionId;
    }

    FileType(int descriptionId)
    {
        this.descriptionId = descriptionId;
    }

}
