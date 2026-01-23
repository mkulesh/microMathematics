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
package com.mkulesh.micromath.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

/**
 * Class collecting version compatibility helper methods
 */
@SuppressLint("NewApi")
public class CompatUtils
{
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

    /**
     * Fix dialog icon color after dialog creation. Necessary for older Android Versions
     */
    public static void fixIconColor(@NonNull AlertDialog dialog, @AttrRes int resId)
    {
        final ImageView imageView = dialog.findViewById(android.R.id.icon);
        if (imageView != null)
        {
            setImageViewColorAttr(dialog.getContext(), imageView, resId);
        }
    }

    /**
     * Procedure sets ImageView background color given by attribute ID
     */
    public static void setImageViewColorAttr(Context context, ImageView b, @AttrRes int resId)
    {
        final int c = getThemeColorAttr(context, resId);
        b.clearColorFilter();
        b.setColorFilter(c, PorterDuff.Mode.SRC_ATOP);
    }

    public static void setDrawableColorAttr(Context c, Drawable drawable, @AttrRes int resId)
    {
        if (drawable != null)
        {
            drawable.clearColorFilter();
            drawable.setColorFilter(getThemeColorAttr(c, resId), PorterDuff.Mode.SRC_ATOP);
        }
    }

    @SafeVarargs
    public static <T> void executeAsyncTask(AsyncTask<T, ?, ?> asyncTask, T... params)
    {
        asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
    }

    public static boolean isExternalStorageEmulated()
    {
        return Environment.isExternalStorageEmulated();
    }

    /**
     * Procedure creates new dot-separated DecimalFormat
     */
    public static DecimalFormat getDecimalFormat(String format)
    {
        DecimalFormat df = new DecimalFormat(format);
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        dfs.setExponentSeparator("e");
        df.setDecimalFormatSymbols(dfs);
        return df;
    }

    //**********************************************************************************************
    // Jelly Bean: Build.VERSION_CODES.JELLY_BEAN = 16
    //**********************************************************************************************

    @SuppressWarnings("deprecation")
    public static void updateBackground(Context c, View v, @DrawableRes int drawableId)
    {
        Drawable bg;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            bg = c.getResources().getDrawable(drawableId, c.getTheme());
        }
        else
        {
            bg = c.getResources().getDrawable(drawableId);
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

    @SuppressWarnings("deprecation")
    public static void updateBackgroundAttr(Context c, View v, @DrawableRes int drawableId, @AttrRes int colorAttrId)
    {
        Drawable bg;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            bg = c.getResources().getDrawable(drawableId, c.getTheme());
        }
        else
        {
            bg = c.getResources().getDrawable(drawableId);
        }

        setDrawableColorAttr(c, bg, colorAttrId);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
        {
            v.setBackground(bg);
        }
        else
        {
            v.setBackgroundDrawable(bg);
        }
    }

    public static Uri getClipDataUri(Intent intent)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
        {
            return null;
        }
        if (intent.getClipData() == null)
        {
            return null;
        }
        for (int i = 0; i < intent.getClipData().getItemCount(); i++)
        {
            if (intent.getClipData().getItemAt(i).getUri() != null)
            {
                return intent.getClipData().getItemAt(i).getUri();
            }
        }
        return null;
    }

    //**********************************************************************************************
    // Kitkat: Build.VERSION_CODES.KITKAT = 19
    //**********************************************************************************************

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

    //**********************************************************************************************
    // MarshMallow: Build.VERSION_CODES.M = 23
    //**********************************************************************************************

    public static boolean isMarshMallowOrLater()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
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

    public static Intent getDocTreeIntent()
    {
        if (isMarshMallowOrLater())
        {
            return new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        }
        return null;
    }

    public static void requestStoragePermission(AppCompatActivity a, int reqId)
    {
        if (isROrLater())
        {
            // nothing to do
        }
        else if (isMarshMallowOrLater())
        {
            ViewUtils.Debug(a, "requesting storage permissions for Android M");
            a.requestPermissions(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE }, reqId);
        }
    }

    //**********************************************************************************************
    // Q: Build.VERSION_CODES.Q = 29
    //**********************************************************************************************

    public static File getStorageDir(final @NonNull Context context, final @NonNull String directory)
    {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q)
        {
            return new File(context.getFilesDir() + "/" + directory);
        }
        else
        {
            return new File(context.getExternalFilesDir(null) + "/" + directory);
        }
    }

    public static File getStorageFile(final @NonNull Context context, final @NonNull String file)
    {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q)
        {
            return new File(context.getFilesDir(), file);
        }
        else
        {
            return new File(context.getExternalFilesDir(null), file);
        }
    }

    //**********************************************************************************************
    // R: Build.VERSION_CODES.R = 30
    //**********************************************************************************************

    public static boolean isROrLater()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }

    @SuppressWarnings("deprecation")
    public static boolean isToastVisible(Toast toast)
    {
        if (isROrLater())
        {
            return toast != null;
        }
        else
        {
            return toast != null && toast.getView() != null && toast.getView().isShown();
        }
    }

    //**********************************************************************************************
    // Tiramisu: Build.VERSION_CODES.TIRAMISU = 33
    //**********************************************************************************************

    public static boolean isTiramisuOrLater()
    {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
    }

    @SuppressWarnings("deprecation")
    public static <T extends Parcelable> T getParcelable(@NonNull Bundle state, @NonNull String parName, @NonNull Class<T> clazz)
    {
        if (isTiramisuOrLater())
        {
            final T p = state.getParcelable(parName, clazz);
            if (p == null)
            {
                ViewUtils.Debug(state, "empty parcelable state in getParcelable for " + clazz.getCanonicalName());
            }
            return p;
        }
        else
        {
            return state.getParcelable(parName);
        }
    }

    @SuppressWarnings("deprecation")
    public static <T extends Parcelable> T readParcelable(@NonNull Parcel parcel, @Nullable ClassLoader loader, @NonNull Class<T> clazz)
    {
        if (isTiramisuOrLater())
        {
            final T p = parcel.readParcelable(loader, clazz);
            if (p == null)
            {
                ViewUtils.Debug(parcel, "empty parcelable state in readParcelable for " + clazz.getCanonicalName());
            }
            return p;
        }
        else
        {
            return parcel.readParcelable(loader);
        }
    }
}
