/*
 * microMathematics - Extended Visual Calculator
 * Copyright (C) 2014-2026 by Mikhail Kulesh
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

import android.content.Intent;

import com.mkulesh.micromath.utils.ViewUtils;

import androidx.activity.result.ActivityResultLauncher;

public class ActivityCallback
{
    private static final ActivityCallback INSTANCE = new ActivityCallback();

    private ActivityResultLauncher<Intent> resultLauncher;

    private ActivityCallback()
    {
        // Private constructor to prevent instantiation.
    }

    public static ActivityCallback getInstance()
    {
        return INSTANCE;
    }

    public ActivityResultLauncher<Intent> getResultLauncher()
    {
        return resultLauncher;
    }

    public void setResultLauncher(ActivityResultLauncher<Intent> resultLauncher)
    {
        this.resultLauncher = resultLauncher;
    }
}