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
package com.mkulesh.micromath.export;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.widget.LinearLayout;

import com.mkulesh.micromath.formula.FormulaListView;
import com.mkulesh.micromath.plus.R;

import java.io.OutputStream;

public class ExportToImage
{
    private final OutputStream stream;

    public ExportToImage(Context context, OutputStream stream, final Uri uri)
    {
        this.stream = stream;
    }

    public void write(FormulaListView formulaListView, Bitmap.CompressFormat format) throws Exception
    {
        final LinearLayout f = formulaListView.getList();
        Bitmap bitmap = null;
        try
        {
            bitmap = Bitmap.createBitmap(f.getMeasuredWidth(), f.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            final Canvas canvas = new Canvas(bitmap);
            f.setBackgroundResource(R.color.micromath_background);
            f.draw(canvas);
        }
        catch (OutOfMemoryError e)
        {
            throw new Exception(e.getLocalizedMessage());
        }
        finally
        {
            f.setBackgroundResource(android.R.color.transparent);
        }
        bitmap.compress(format, 100, stream);
        stream.flush();
    }
}
