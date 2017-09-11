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
package com.mkulesh.micromath.widgets;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class LineDrawable extends Drawable
{
    private final Paint mPaint;
    private final Path path = new Path();

    public LineDrawable(Paint mPaint, int w, int h)
    {
        this.mPaint = mPaint;
        setBounds(0, 0, w, h);
    }

    @Override
    public void draw(Canvas canvas)
    {
        final Rect b = getBounds();
        final float y = (b.height() - mPaint.getStrokeWidth()) / 2;

        path.reset();
        path.moveTo(0, y);
        path.lineTo(b.width(), y);
        canvas.drawPath(path, mPaint);
    }

    @Override
    protected boolean onLevelChange(int level)
    {
        invalidateSelf();
        return true;
    }

    @Override
    public void setAlpha(int alpha)
    {
    }

    @Override
    public void setColorFilter(ColorFilter cf)
    {
    }

    @Override
    public int getOpacity()
    {
        return PixelFormat.TRANSLUCENT;
    }
}
