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
package com.mkulesh.micromath.plots.views;

import java.util.ArrayList;

import com.mkulesh.micromath.math.Vector2D;
import com.mkulesh.micromath.plots.FunctionIf;
import com.mkulesh.micromath.plots.FunctionIf.Type;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.ViewUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.Toast;

public abstract class PlanePlotView extends PlotView
{
    // data
    protected final PhysicalArea area = new PhysicalArea();
    private final Vector2D labelCenter = new Vector2D();
    private int arrowLength = 0, arrowStroke = 0;
    private Label[] xLabels = null;
    private Label[] yLabels = null;

    // temporary variables used for drawing
    protected final Rect rect = new Rect(), tmpRect = new Rect();
    protected final Point p1 = new Point(), p2 = new Point();
    protected final Vector2D tmpVec = new Vector2D();

    /*********************************************************
     * Creating
     *********************************************************/

    public PlanePlotView(Context context)
    {
        super(context);
    }

    public PlanePlotView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public PlanePlotView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    /*********************************************************
     * Data
     *********************************************************/

    @Override
    public void setArea(double minX, double maxX, double minY, double maxY)
    {
        area.set(minX, maxX, minY, maxY);
        updateLabels();
    }

    @Override
    public void setScale(float scaleFactor)
    {
        clearDrawingCache();
        axisParameters.scaleFactor = scaleFactor;
        if (colorMapView != null)
        {
            Rect paddings = new Rect();
            getScaledPadding(paddings);
            colorMapView.setPadding(paddings.left, paddings.top, paddings.left, paddings.bottom);
            colorMapView.setTextSize(TypedValue.COMPLEX_UNIT_PX, axisParameters.getLabelTextSize());
        }
    }

    @Override
    public void updateLabels()
    {
        if (plotParameters.isCrossedAxes())
        {
            labelCenter.x = (area.getMin().x > 0) ? area.getMin().x : (area.getMax().x < 0) ? area.getMax().x : 0.0;
            labelCenter.y = (area.getMin().y > 0) ? area.getMin().y : (area.getMax().y < 0) ? area.getMax().y : 0.0;
        }
        else
        {
            labelCenter.x = area.getMin().x;
            labelCenter.y = area.getMin().y;
        }

        this.xLabels = makeLabels(FunctionIf.X, axisParameters.xLabelsNumber);
        this.yLabels = makeLabels(FunctionIf.Y, axisParameters.yLabelsNumber);
    }

    protected void getScaledPadding(Rect r)
    {
        arrowStroke = 2 * axisParameters.getLabelLineSize();
        arrowLength = (plotParameters.isCrossedAxes()) ? 4 * arrowStroke : 0;
        r.set(getPaddingLeft() + arrowStroke, getPaddingTop() + arrowLength, getPaddingRight() + arrowLength,
                getPaddingBottom() + arrowStroke);
    }

    /*********************************************************
     * Labels
     *********************************************************/

    private Label[] makeLabels(int idx, int labelNumber)
    {
        if (labelNumber == 0)
        {
            return null;
        }
        Label[] retValue = null;
        final double minValue = area.getMin().get(idx);
        final double maxValue = area.getMax().get(idx);
        if (labelCenter.get(idx) > minValue && labelCenter.get(idx) < maxValue)
        {
            // Method 1: label center is not a boundary
            // first, we obtain values including label center
            final double delta = area.getDim().get(idx) / (double) labelNumber;
            ArrayList<Double> rawValues = new ArrayList<Double>();
            for (int i = 0; i < labelNumber; i++)
            {
                final double v = -1.0 * (double) (labelNumber - i) * delta;
                if (v >= (minValue + delta / 2) && v < 0.0)
                {
                    rawValues.add(v);
                }
            }
            if (idx == FunctionIf.X)
            {
                rawValues.add(0.0);
            }
            for (int i = 1; i <= labelNumber; i++)
            {
                final double v = (double) (i) * delta;
                if (v > 0.0 && v <= (maxValue - delta / 2))
                {
                    rawValues.add(v);
                }
            }
            // second, convert it to Labels array
            double[] values = new double[rawValues.size()];
            for (int i = 0; i < values.length; i++)
            {
                values[i] = rawValues.get(i);
            }
            final String[] strValues = ViewUtils.catValues(values, significantDigits);
            retValue = new Label[values.length];
            for (int i = 0; i < retValue.length; i++)
            {
                retValue[i] = new Label(idx, values[i], labelCenter);
                retValue[i].name = strValues[i];
            }
        }
        else
        {
            // Method 2: label center is on a boundary
            // first, we obtain values including plot boundaries in order to compare them
            // ViewUtils.catValues as well
            double[] values = new double[labelNumber + 2];
            final double delta = area.getDim().get(idx) / (double) (values.length - 1);
            for (int i = 1; i <= values.length; i++)
            {
                final double v = (double) (i - 1) * delta + minValue;
                values[i - 1] = v;
            }
            final String[] strValues = ViewUtils.catValues(values, significantDigits);
            // second, we dismiss the plot boundaries
            retValue = new Label[labelNumber];
            for (int i = 0; i < retValue.length; i++)
            {
                retValue[i] = new Label(idx, values[i + 1], labelCenter);
                retValue[i].name = strValues[i + 1];
            }
        }
        return retValue;
    }

    /*********************************************************
     * Painting
     *********************************************************/

    protected abstract void drawContent(Canvas c, FunctionIf f);

    @Override
    protected void onDraw(Canvas can)
    {
        if (drawingCache == null)
        {
            try
            {
                final int bitmapWidth = this.getMeasuredWidth();
                final int bitmapHeight = this.getMeasuredHeight();

                drawingCache = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
                final Canvas c = new Canvas(drawingCache);
                c.drawColor(android.graphics.Color.TRANSPARENT);

                // rect including component padding and label size (arrow size)
                getScaledPadding(tmpRect);
                rect.set(tmpRect.left, tmpRect.top, this.getRight() - this.getLeft() - tmpRect.right, this.getBottom()
                        - this.getTop() - tmpRect.bottom);

                paint.setStyle(Paint.Style.STROKE);
                paint.setAntiAlias(true);

                // function
                if (functions != null && !functions.isEmpty())
                {
                    drawGrid(0, c, paint);
                    drawGrid(1, c, paint);

                    // function line
                    boolean repeadGrid = false;
                    for (FunctionIf f : functions)
                    {
                        if (f == null)
                        {
                            continue;
                        }
                        c.save();
                        c.clipRect(rect);
                        drawContent(c, f);
                        c.restore();
                        if (f.getType() == Type.FUNCTION_3D)
                        {
                            repeadGrid = true;
                        }
                    }

                    if (repeadGrid)
                    {
                        drawGrid(0, c, paint);
                        drawGrid(1, c, paint);
                    }

                    // labels
                    if (!plotParameters.isNoAxes())
                    {
                        drawLabeles(0, c, paint);
                        drawLabeles(1, c, paint);
                    }
                }

                // border
                drawBorder(c, paint);

                // Test code to trace paddings:
                // paint.setColor(Color.BLUE);
                // paint.setStrokeWidth(0);
                // c.drawRect(0, 0, this.getWidth(), this.getHeight(), paint);
                // c.drawRect(getPaddingLeft(), getPaddingTop(), this.getRight() - this.getLeft() - getPaddingRight(),
                // this.getBottom() - this.getTop() - getPaddingBottom(), paint);
                // paint.setColor(Color.GREEN);
                // c.drawRect(rect, paint);
            }
            catch (OutOfMemoryError ex)
            {
                String error = getContext().getResources().getString(R.string.error_out_of_memory);
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                return;
            }
            catch (Exception ex)
            {
                String error = getContext().getResources().getString(R.string.error_out_of_memory);
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                return;
            }
        }
        can.drawBitmap(drawingCache, 0, 0, paint);
    }

    protected void drawBorder(Canvas c, Paint p)
    {
        p.setStyle(Paint.Style.STROKE);
        p.setColor(getPaint().getColor());
        p.setStrokeWidth(strokeWidth);
        switch (plotParameters.axesStyle)
        {
        case BOXED:
            c.drawRect(rect, p);
            break;
        case CROSSED:
            drawCross(c, p);
            break;
        case NONE:
            // nothing to do
            break;
        }
    }

    protected void drawCross(Canvas c, Paint p)
    {
        // horizontal line
        tmpVec.set(area.getMin().x, labelCenter.y);
        area.toScreenPoint(tmpVec, rect, p1);
        tmpVec.set(area.getMax().x, labelCenter.y);
        area.toScreenPoint(tmpVec, rect, p2);
        p2.x += arrowLength;
        c.drawLine(p1.x, p1.y, p2.x - 2 * strokeWidth, p2.y, p);
        drawHorArrowHead(c, p2, arrowStroke, arrowLength, p);
        // vertical line
        p.setStrokeWidth(strokeWidth);
        tmpVec.set(labelCenter.x, area.getMin().y);
        area.toScreenPoint(tmpVec, rect, p1);
        tmpVec.set(labelCenter.x, area.getMax().y);
        area.toScreenPoint(tmpVec, rect, p2);
        p2.y -= arrowLength;
        c.drawLine(p1.x, p1.y, p2.x, p2.y + 2 * strokeWidth, p);
        drawVerArrowHead(c, p2, arrowStroke, arrowLength, p);
    }

    protected void drawGrid(int idx, Canvas c, Paint p)
    {
        final Label[] labels = (idx == FunctionIf.X) ? xLabels : yLabels;
        if (labels == null)
        {
            return;
        }
        p.setStyle(Paint.Style.STROKE);
        p.setColor(axisParameters.gridLineColor);
        p.setStrokeWidth(axisParameters.getGridLineWidth());
        for (int i = 0; i < labels.length; i++)
        {
            area.toScreenPoint(labels[i].point, rect, p1);
            if (idx == FunctionIf.X)
            {
                c.drawLine(p1.x, rect.top, p1.x, rect.bottom, p);
            }
            else
            {
                c.drawLine(rect.left, p1.y, rect.right, p1.y, p);
            }
        }
    }

    protected void drawLabeles(int idx, Canvas c, Paint p)
    {
        final Label[] labels = (idx == FunctionIf.X) ? xLabels : yLabels;
        if (labels == null)
        {
            return;
        }
        p.set(getPaint());
        p.setColor(getPaint().getColor());
        p.setTextSize(axisParameters.getLabelTextSize());
        final int labelLineSize = axisParameters.getLabelLineSize();
        for (int i = 0; i < labels.length; i++)
        {
            area.toScreenPoint(labels[i].point, rect, p1);
            String label = labels[i].name;
            p.getTextBounds(label, 0, label.length(), tmpRect);
            p.setStrokeWidth(labelLineSize);
            if (idx == FunctionIf.X)
            {
                c.drawLine(p1.x, p1.y - labelLineSize, p1.x, p1.y + labelLineSize, p);
                tmpRect.offset(p1.x + axisParameters.getGridLineWidth() + 1, p1.y - labelLineSize - 2);
            }
            else
            {
                c.drawLine(p1.x - labelLineSize, p1.y, p1.x + labelLineSize, p1.y, p);
                tmpRect.offset(p1.x + labelLineSize + 2, p1.y - axisParameters.getGridLineWidth() - 2);
            }
            p.setStrokeWidth(1);
            c.drawText(label, tmpRect.left, tmpRect.bottom, p);
        }
    }

    protected void drawHorArrowHead(Canvas c, Point p0, int width, int lenght, Paint p)
    {
        p.setStrokeWidth(0);
        for (int i = 0; i < lenght / 2; i++)
        {
            c.drawLine(p0.x - i, p0.y, p0.x - lenght, p0.y - width, p);
            c.drawLine(p0.x - i, p0.y, p0.x - lenght, p0.y + width, p);
        }
    }

    protected void drawVerArrowHead(Canvas c, Point p0, int width, int lenght, Paint p)
    {
        p.setStrokeWidth(0);
        for (int i = 0; i < lenght / 2; i++)
        {
            c.drawLine(p0.x, p0.y + i, p0.x - width, p0.y + lenght, p);
            c.drawLine(p0.x, p0.y + i, p0.x + width, p0.y + lenght, p);
        }
    }
}
