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
package com.mkulesh.micromath.plots.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;

import com.mkulesh.micromath.plots.FunctionIf;
import com.mkulesh.micromath.plots.views.Palette.ColorType;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.ColorMapProperties;
import com.mkulesh.micromath.properties.ColorMapProperties.ColorMap;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CustomTextView;
import com.mkulesh.micromath.widgets.FormulaChangeIf;

public class ColorMapView extends CustomTextView
{
    // the color bar width will be defined used this symbol
    private static final String COLOR_BAR_WIDTH_PATTERN = "___";

    // drawing data and parameters
    private final ColorMapProperties colorMapParameters = new ColorMapProperties();
    private FunctionIf function = null;
    private Label[] labels = null;
    private int significantDigits = 6;

    // temporary variables used for drawing
    private final Paint paint = new Paint();
    private final Rect rect = new Rect(), tmpRect = new Rect();
    private final double[] minMaxValues = new double[2];
    private final Palette coolPalette = new Palette(ColorType.RGB), firePalette = new Palette(ColorType.RGB),
            coldHotPalette = new Palette(ColorType.RGB), rainbowPalette = new Palette(ColorType.HSV),
            earthSkyPalette = new Palette(ColorType.RGB), grayscalePalette = new Palette(ColorType.RGB),
            greenBluePalette = new Palette(ColorType.RGB);
    private boolean isHorizontal = false;

    /*********************************************************
     * Creating
     *********************************************************/

    public ColorMapView(Context context)
    {
        super(context);
    }

    public ColorMapView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.prepare(attrs);
    }

    public ColorMapView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        this.prepare(attrs);
    }

    private void prepare(AttributeSet attrs)
    {
        setText(COLOR_BAR_WIDTH_PATTERN);
        if (attrs != null)
        {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ColorMapViewExtension, 0, 0);
            isHorizontal = a.getBoolean(R.styleable.ColorMapViewExtension_isHorizontal, false);
            try
            {
                colorMapParameters.colorMap = ColorMap.valueOf(a
                        .getString(R.styleable.ColorMapViewExtension_paletteName));
            }
            catch (Exception ex)
            {
                // empty
            }
            a.recycle();
        }
        preparePalette();
    }

    public void prepare(AppCompatActivity activity, FormulaChangeIf termChangeIf)
    {
        this.prepare(null);
        super.prepare(SymbolType.EMPTY, activity, termChangeIf);
    }

    private void preparePalette()
    {
        // cool
        coolPalette.split(0.17);
        coolPalette.split(0.83);
        coolPalette.setDivisionPointColorComponents(0, 1, 1, 1);
        coolPalette.setDivisionPointColorComponents(1, 0, 1, 1);
        coolPalette.setDivisionPointColorComponents(2, 0, 0, 1);
        coolPalette.setDivisionPointColorComponents(3, 0, 0, 0);

        // fire
        firePalette.split(0.17);
        firePalette.split(0.83);
        firePalette.setDivisionPointColorComponents(0, 1, 1, 1);
        firePalette.setDivisionPointColorComponents(1, 1, 1, 0);
        firePalette.setDivisionPointColorComponents(2, 1, 0, 0);
        firePalette.setDivisionPointColorComponents(3, 0, 0, 0);

        // cold and hot
        coldHotPalette.split(0.08);
        coldHotPalette.split(0.38);
        coldHotPalette.split(0.50);
        coldHotPalette.split(0.62);
        coldHotPalette.split(0.92);
        coldHotPalette.setDivisionPointColorComponents(0, 0.8f, 1, 1);
        coldHotPalette.setDivisionPointColorComponents(1, 0, 1, 1);
        coldHotPalette.setDivisionPointColorComponents(2, 0, 0, 1);
        coldHotPalette.setDivisionPointColorComponents(3, 0, 0, 0);
        coldHotPalette.setDivisionPointColorComponents(4, 1, 0, 0);
        coldHotPalette.setDivisionPointColorComponents(5, 1, 1, 0);
        coldHotPalette.setDivisionPointColorComponents(6, 1, 1, 0.8f);

        // rainbow
        // no special settings necessary

        // earth and sky
        earthSkyPalette.split(0.15);
        earthSkyPalette.split(0.33);
        earthSkyPalette.split(0.67);
        earthSkyPalette.split(0.85);
        earthSkyPalette.setDivisionPointColorComponents(0, 1, 1, 0);
        earthSkyPalette.setDivisionPointColorComponents(1, 1, 0.8f, 0);
        earthSkyPalette.setDivisionPointColorComponents(2, 0.53f, 0.12f, 0.075f);
        earthSkyPalette.setDivisionPointColorComponents(3, 0, 0, 0.6f);
        earthSkyPalette.setDivisionPointColorComponents(4, 0, 0.4f, 1);
        earthSkyPalette.setDivisionPointColorComponents(5, 0, 1, 1);

        // from green to blue
        greenBluePalette.split(0.23);
        greenBluePalette.split(0.5);
        greenBluePalette.split(0.72);
        greenBluePalette.setDivisionPointColorComponents(0, 0, 1, 0);
        greenBluePalette.setDivisionPointColorComponents(1, 1, 1, 0);
        greenBluePalette.setDivisionPointColorComponents(2, 1, 0.5f, 0.5f);
        greenBluePalette.setDivisionPointColorComponents(3, 1, 0, 1);
        greenBluePalette.setDivisionPointColorComponents(4, 0, 0, 1);

        // grayscale
        // no special settings necessary
    }

    /*********************************************************
     * Properties
     *********************************************************/

    public ColorMapProperties getColorMapParameters()
    {
        return colorMapParameters;
    }

    public void setFunction(FunctionIf function)
    {
        this.function = function;
        if (function != null && function.getType() == FunctionIf.Type.FUNCTION_3D
                && function.getMinMaxValues(FunctionIf.Z) != null)
        {
            labels = makeLabels(colorMapParameters.zLabelsNumber, function.getMinMaxValues(FunctionIf.Z));
        }
        else
        {
            labels = null;
        }
        String maxString = null;
        if (labels != null)
        {
            for (Label l : labels)
            {
                if (maxString == null)
                {
                    maxString = l.name;
                }
                else if (l.name.length() > maxString.length())
                {
                    maxString = l.name;
                }
            }
        }
        setText(COLOR_BAR_WIDTH_PATTERN + ((maxString != null) ? maxString + "0" : ""));
    }

    public void setSignificantDigits(int significantDigits)
    {
        this.significantDigits = significantDigits;
    }

    /*********************************************************
     * Painting
     *********************************************************/

    @Override
    protected void onDraw(Canvas can)
    {
        rect.set(getPaddingLeft(), getPaddingTop(), this.getRight() - this.getLeft() - getPaddingRight(),
                this.getBottom() - this.getTop() - getPaddingBottom());

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(0);
        paint.setAntiAlias(true);

        if (isHorizontal)
        {
            drawHorizontalBar(can, paint);
        }
        else
        {
            drawVerticalBar(can, paint);
        }
        if (function != null && function.getMinMaxValues(FunctionIf.Z) != null)
        {
            drawLabels(can, paint, function.getMinMaxValues(FunctionIf.Z));
        }

        // Test code to trace paddings:
        // paint.setStyle(Paint.Style.STROKE);
        // paint.setStrokeWidth(0);
        // paint.setColor(Color.BLUE);
        // can.drawRect(0, 0, this.getWidth(), this.getHeight(), paint);
        // paint.setColor(Color.GREEN);
        // can.drawRect(getPaddingLeft(), getPaddingTop(), this.getRight() - this.getLeft() - getPaddingRight(),
        // this.getBottom() - this.getTop() - getPaddingBottom(), paint);
    }

    /**
     * procedure returns a color that corresponds to the given value z
     */
    public int getPaletteColor(double z, double[] minMaxValues, int alpha)
    {
        final double delta = Math.abs(minMaxValues[FunctionIf.MAX] - minMaxValues[FunctionIf.MIN]);
        switch (colorMapParameters.colorMap)
        {
        case COOL:
            return coolPalette.getColor((z - minMaxValues[FunctionIf.MIN]) / delta, alpha);
        case FIRE:
            return firePalette.getColor((z - minMaxValues[FunctionIf.MIN]) / delta, alpha);
        case COLDHOT:
            return coldHotPalette.getColor((z - minMaxValues[FunctionIf.MIN]) / delta, alpha);
        case RAINBOW:
            return rainbowPalette.getColor((z - minMaxValues[FunctionIf.MIN]) / delta, alpha);
        case EARTHSKY:
            return earthSkyPalette.getColor((z - minMaxValues[FunctionIf.MIN]) / delta, alpha);
        case GREENBLUE:
            return greenBluePalette.getColor((z - minMaxValues[FunctionIf.MIN]) / delta, alpha);
        case GRAYSCALE:
            return grayscalePalette.getColor((z - minMaxValues[FunctionIf.MIN]) / delta, alpha);
        }
        return 0;
    }

    private void drawHorizontalBar(Canvas c, Paint p)
    {
        final int xMin = rect.left;
        final int xMax = rect.right;
        minMaxValues[FunctionIf.MIN] = xMin;
        minMaxValues[FunctionIf.MAX] = xMax;
        for (int x = xMin; x < xMax; x++)
        {
            p.setColor(getPaletteColor(x, minMaxValues, 255));
            c.drawLine(x, rect.top, x, rect.bottom, p);
        }
    }

    private void drawVerticalBar(Canvas c, Paint p)
    {
        p.setTextSize(getTextSize());
        p.getTextBounds(COLOR_BAR_WIDTH_PATTERN, 0, COLOR_BAR_WIDTH_PATTERN.length(), tmpRect);
        final int yMin = Math.min(rect.top, rect.bottom);
        final int yMax = Math.max(rect.top, rect.bottom);
        minMaxValues[FunctionIf.MIN] = yMin;
        minMaxValues[FunctionIf.MAX] = yMax;
        for (int y = yMin; y < yMax; y++)
        {
            p.setColor(getPaletteColor(y, minMaxValues, 255));
            final int yl = yMax - (y - yMin);
            c.drawLine(rect.left, yl, rect.left + tmpRect.width() - 1, yl, p);
        }
    }

    /*********************************************************
     * Helper class that holds labels
     *********************************************************/
    private final class Label
    {
        double y = 0;
        String name = null;

        public Label(double y, String name)
        {
            this.y = y;
            this.name = name;
        }
    }

    private Label[] makeLabels(int labelNumber, double[] minMaxValues)
    {
        final double minValue = minMaxValues[FunctionIf.MIN];
        final double maxValue = minMaxValues[FunctionIf.MAX];
        final double delta = Math.abs(maxValue - minValue) / (double) (labelNumber + 1);
        if (delta == 0.0)
        {
            return null;
        }

        double[] values = new double[labelNumber + 2];
        for (int i = 0; i < values.length; i++)
        {
            values[i] = (double) (i) * delta + minValue;
        }
        final String[] strValues = ViewUtils.catValues(values, significantDigits);
        // second, we dismiss the plot boundaries
        Label[] retValue = new Label[values.length];
        for (int i = 0; i < retValue.length; i++)
        {
            retValue[i] = new Label(values[i], strValues[i]);
        }
        return retValue;
    }

    private void drawLabels(Canvas c, Paint p, double[] minMaxValues)
    {
        if (labels == null)
        {
            return;
        }
        p.setColor(getPaint().getColor());
        p.setStyle(Paint.Style.FILL);
        p.setTextSize(getTextSize());
        p.getTextBounds(COLOR_BAR_WIDTH_PATTERN, 0, COLOR_BAR_WIDTH_PATTERN.length(), tmpRect);
        final int xOffset = rect.left + tmpRect.width() + 1;
        final double delta = Math.abs(minMaxValues[FunctionIf.MAX] - minMaxValues[FunctionIf.MIN]);
        for (int i = 0; i < labels.length; i++)
        {
            final int y = rect.bottom
                    - (int) ((double) rect.height() * (labels[i].y - minMaxValues[FunctionIf.MIN]) / delta);
            final String label = labels[i].name;
            p.getTextBounds(label, 0, label.length(), tmpRect);
            int yOffset = y + (int) ((double) tmpRect.height() * (double) i / (double) (labels.length - 1));
            tmpRect.offset(xOffset, yOffset);
            c.drawText(label, tmpRect.left, tmpRect.bottom, p);
        }
    }
}
