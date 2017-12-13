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
package com.mkulesh.micromath.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * Class collecting version compatibility helper methods
 */
@SuppressLint("NewApi")
public class CompatUtils
{
    public static boolean isMarshMallowOrLater()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * Sets the text color, size, style, hint color, and highlight color from the specified TextAppearance resource.
     *
     * This method was deprecated in API level 23.
     */
    @SuppressWarnings("deprecation")
    public static final void setTextAppearance(Context context, TextView t, int resId)
    {
        if (isMarshMallowOrLater())
        {
            t.setTextAppearance(resId);
        }
        else
        {
            t.setTextAppearance(context, resId);
        }
    }

    /**
     * Returns a color associated with a particular resource ID.
     *
     * This method was deprecated in API level 23. Starting in M, the returned color will be styled for the specified
     * Context's theme.
     *
     * Note: Starting from Android Support Library 23, a new getColor() method has been added to ContextCompat.
     */
    @SuppressWarnings("deprecation")
    @ColorInt
    public static final int getColor(Context context, @ColorRes int id)
    {
        if (isMarshMallowOrLater())
        {
            return context.getColor(id);
        }
        else
        {
            return context.getResources().getColor(id);
        }
    }

    /**
     * Procedure returns theme color
     */
    @ColorInt
    public static int getThemeColorAttr(final Context context, @AttrRes int resId)
    {
        final TypedValue value = new TypedValue();
        context.getTheme().resolveAttribute(resId, value, true);
        return value.data;
    }

    @SuppressWarnings("deprecation")
    public static void setDrawerListener(DrawerLayout mDrawerLayout, ActionBarDrawerToggle mDrawerToggle)
    {

        if (isMarshMallowOrLater())
        {
            mDrawerLayout.removeDrawerListener(mDrawerToggle);
            mDrawerLayout.addDrawerListener(mDrawerToggle);
        }
        else
        {
            mDrawerLayout.setDrawerListener(mDrawerToggle);
        }
    }

    @SuppressWarnings("deprecation")
    public static Drawable getDrawable(Context context, int icon)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            return context.getResources().getDrawable(icon, context.getTheme());
        }
        else
        {
            return context.getResources().getDrawable(icon);
        }
    }

    /**
     * Procedure sets the background for given view as a drawable with given resource id
     */
    @SuppressWarnings("deprecation")
    public static void updateBackground(Context c, View v, int resId)
    {
        Drawable bg = null;

        if (resId >= 0)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                bg = c.getResources().getDrawable(resId, c.getTheme());
            }
            else
            {
                bg = c.getResources().getDrawable(resId);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            v.setBackground(bg);
        }
        else
        {
            v.setBackgroundDrawable(bg);
        }
    }

    /**
     * Procedure sets the background for given view as a drawable with given resource id
     */
    @SuppressWarnings("deprecation")
    public static void updateBackground(Context c, View v, int resId, int color)
    {
        Drawable bg = null;

        if (resId >= 0)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            {
                bg = c.getResources().getDrawable(resId, c.getTheme());
            }
            else
            {
                bg = c.getResources().getDrawable(resId);
            }
        }

        setDrawableColor(bg, getColor(c, color));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            v.setBackground(bg);
        }
        else
        {
            v.setBackgroundDrawable(bg);
        }
    }

    public static void setDrawableColor(Drawable drawable, int c)
    {
        if (drawable != null)
        {
            drawable.clearColorFilter();
            drawable.setColorFilter(c, PorterDuff.Mode.SRC_ATOP);
        }
    }

    @SafeVarargs
    public static <T> void executeAsyncTask(AsyncTask<T, ?, ?> asyncTask, T... params)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        }
        else
        {
            asyncTask.execute(params);
        }
    }

    public static void addOnMenuVisibilityListener(ActionBar actionBar, ActionBar.OnMenuVisibilityListener listener)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            actionBar.addOnMenuVisibilityListener(listener);
        }
    }

    public static boolean isExternalStorageEmulated()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
        {
            return Environment.isExternalStorageEmulated();
        }
        else
        {
            return false;
        }
    }

    /**
     * Procedure creates new dot-separated DecimalFormat
     */
    public static DecimalFormat getDecimalFormat(String format)
    {
        DecimalFormat df = new DecimalFormat(format);
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
        {
            dfs.setExponentSeparator("e");
        }
        df.setDecimalFormatSymbols(dfs);
        return df;
    }

    /**
     * Procedure retrieves the storage directory
     */
    public static String[] getStorageDirs(Context ctx)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            File[] ff = ctx.getExternalFilesDirs(null);
            if (ff == null)
                return null;
            String[] res = new String[ff.length];
            for (int i = 0; i < ff.length; i++)
            {
                if (ff[i] == null)
                    continue;
                String path = ff[i].getAbsolutePath();
                if (path == null)
                    continue;
                int pos = path.indexOf("Android");
                if (pos < 0)
                {
                    continue;
                }
                res[i] = path.substring(0, pos);
            }
            return res;
        }
        else
        {
            return null;
        }
    }

    public static Intent getDocTreeIntent()
    {
        if (isMarshMallowOrLater())
        {
            return new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        }
        return null;
    }
}
