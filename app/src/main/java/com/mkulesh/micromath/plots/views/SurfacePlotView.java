/*
 * Copyright (C) 2014-2018 Mikhail Kulesh
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details. You should have received a copy of the GNU General
 * Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.mkulesh.micromath.plots.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Toast;

import com.mkulesh.micromath.math.Vector2D;
import com.mkulesh.micromath.plots.FunctionIf;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.PlotProperties.TwoDPlotStyle;
import com.mkulesh.micromath.utils.ViewUtils;

@SuppressLint("RtlHardcoded")
public class SurfacePlotView extends PlotView
{
    // settings
    private static final int COLOR_MESH_LINES = 0x90FFFFFF;
    private static final int COLOR_WALL = Color.LTGRAY;
    private final boolean isDrawLabels = true;

    // internal constants
    private static final int TOP = 0;
    private static final int CENTER = 1;
    private static final int LEFT = 2;
    private static final int RIGHT = 3;

    // data
    private Point3D[] vertex;
    private final SurfacePlotProjector projector = new SurfacePlotProjector();

    // temporary variables used for drawing
    private final Rect rect = new Rect(), tmpRect = new Rect();
    private final Point p1 = new Point(), p2 = new Point();
    private int factor_x, factor_y; // conversion factors
    private final int[] poly_x = new int[5];
    private final int[] poly_y = new int[5];
    private final int[] color = new int[5];
    private final Point3D[] tmpVertex = new Point3D[4];
    private final Point3D cop = new Point3D(0, 0, 0); // center of projection
    private final float[] vertsValues = new float[12];
    private final int[] vertsColors = new int[12];
    private final double[][] cubeBounds = new double[][]{ { -10, -10, -10 }, { -10, 10, -10 }, { 10, 10, -10 },
            { 10, -10, -10 }, { -10, -10, 10 }, { -10, 10, 10 }, { 10, 10, 10 }, { 10, -10, 10 } };
    private final Vector2D labelCenter = new Vector2D(-10, -10);
    private Label[] xLabels = null;
    private Label[] yLabels = null;

    /*********************************************************
     * Creating
     *********************************************************/

    public SurfacePlotView(Context context)
    {
        super(context);
    }

    public SurfacePlotView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.prepare(attrs);
    }

    public SurfacePlotView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        this.prepare(attrs);
    }

    private void prepare(AttributeSet attrs)
    {
        setLayerType(View.LAYER_TYPE_NONE, null);
        plotParameters.twoDPlotStyle = TwoDPlotStyle.SURFACE;
    }

    /*********************************************************
     * Data
     *********************************************************/

    public void setArea(double minX, double maxX, double minY, double maxY)
    {
        updateLabels();
    }

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

    public void updateLabels()
    {
        if (functions == null || functions.isEmpty() || functions.get(0).getMinMaxValues(FunctionIf.X) == null
                || functions.get(0).getMinMaxValues(FunctionIf.Y) == null)
        {
            xLabels = null;
            yLabels = null;
        }
        else
        {
            xLabels = makeLabels(FunctionIf.X, axisParameters.xLabelsNumber,
                    functions.get(0).getMinMaxValues(FunctionIf.X));
            yLabels = makeLabels(FunctionIf.Y, axisParameters.yLabelsNumber,
                    functions.get(0).getMinMaxValues(FunctionIf.Y));
        }
    }

    @Override
    public void setFunction(FunctionIf function)
    {
        super.setFunction(function);
        if (function == null)
        {
            vertex = null;
        }
    }

    public void renderSurface(FunctionIf function)
    {
        vertex = null;
        if (function == null || function.getXValues() == null || function.getYValues() == null
                || function.getZValues() == null || function.getLabels() == null)
        {
            return;
        }

        final double xmin = function.getMinMaxValues(FunctionIf.X)[FunctionIf.MIN];
        final double xmax = function.getMinMaxValues(FunctionIf.X)[FunctionIf.MAX];
        final double ymin = function.getMinMaxValues(FunctionIf.Y)[FunctionIf.MIN];
        final double ymax = function.getMinMaxValues(FunctionIf.Y)[FunctionIf.MAX];

        final int calc_divisionsX = function.getXValues().length;
        final int calc_divisionsY = function.getYValues().length;
        vertex = new Point3D[calc_divisionsY * calc_divisionsX];

        final double xfactor = 20 / (xmax - xmin);
        final double yfactor = 20 / (ymax - ymin);
        double max = Double.NaN;
        double min = Double.NaN;
        for (int i = 0, k = 0; i < calc_divisionsX; i++)
        {
            final double x = function.getXValues()[i];
            for (int j = 0; j < calc_divisionsY; j++)
            {
                final double y = function.getYValues()[j];
                double v = function.getZValues()[i][j];
                if (Double.isInfinite(v))
                {
                    v = Double.NaN;
                }
                if (!Double.isNaN(v))
                {
                    if (Double.isNaN(max) || (v > max))
                    {
                        max = v;
                    }
                    else
                    {
                        if (Double.isNaN(min) || (v < min))
                        {
                            min = v;
                        }
                    }
                }
                vertex[k] = new Point3D((x - xmin) * xfactor - 10, (y - ymin) * yfactor - 10, v);
                k++;
            }
        }
        projector.setZRange(function.getMinMaxValues(FunctionIf.Z)[FunctionIf.MIN],
                function.getMinMaxValues(FunctionIf.Z)[FunctionIf.MAX]);
    }

    public boolean isRendered()
    {
        return vertex != null;
    }

    private Label[] makeLabels(int idx, int labelNumber, double[] minMaxValues)
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
            values[i] = ((double) i) * delta + minValue;
        }
        final String[] strValues = ViewUtils.catValues(values, significantDigits);
        // second, we dismiss the plot boundaries
        Label[] retValue = new Label[values.length];
        for (int i = 0; i < retValue.length; i++)
        {
            retValue[i] = new Label(idx, values[i], labelCenter);
            retValue[i].name = strValues[i];
        }
        return retValue;
    }

    /*********************************************************
     * Painting
     *********************************************************/

    private void getScaledPadding(Rect r)
    {
        final int arrowStroke = 2 * axisParameters.getLabelLineSize();
        r.set(getPaddingLeft() + arrowStroke, getPaddingTop(), getPaddingRight(), getPaddingBottom() + arrowStroke);
    }

    @Override
    protected void onDraw(Canvas can)
    {
        try
        {
            if (drawingCache == null)
            {
                final int bitmapWidth = this.getMeasuredWidth();
                final int bitmapHeight = this.getMeasuredHeight();
                drawingCache = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);

                final Canvas c = new Canvas(drawingCache);
                c.drawColor(android.graphics.Color.TRANSPARENT);

                // drawing area including component padding and label size (arrow size)
                getScaledPadding(tmpRect);
                rect.set(tmpRect.left, tmpRect.top, this.getRight() - this.getLeft() - tmpRect.right, this.getBottom()
                        - this.getTop() - tmpRect.bottom);

                paint.setStrokeWidth(0);
                paint.setAntiAlias(true);

                if (functions != null && !functions.isEmpty() && vertex != null)
                {
                    prepareProjector();
                    final int fontsize = Math.round((float) projector.get2DScaling());
                    paint.setTextSize(fontsize);
                    if (!plotParameters.isNoAxes())
                    {
                        drawBoxGridsLabels(c);
                    }
                    c.save();
                    c.clipRect(rect);
                    drawSurface(c);
                    if (!plotParameters.isNoAxes())
                    {
                        drawBoundingBox(c);
                    }
                    c.restore();
                }
                else
                {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setColor(getPaint().getColor());
                    c.drawRect(rect, paint);
                }

                // Test code to trace paddings:
                // paint.setColor(Color.BLUE);
                // paint.setStrokeWidth(0);
                // c.drawRect(0, 0, this.getWidth(), this.getHeight(), paint);
                // c.drawRect(getPaddingLeft(), getPaddingTop(), this.getRight() - this.getLeft() - getPaddingRight(),
                // this.getBottom() - this.getTop() - getPaddingBottom(), paint);
                // paint.setColor(Color.GREEN);
                // c.drawRect(rect, paint);

            }
            can.drawBitmap(drawingCache, 0, 0, paint);
        }
        catch (OutOfMemoryError ex)
        {
            String error = getContext().getResources().getString(R.string.error_out_of_memory);
            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
        }
        catch (Exception ex)
        {
            String error = getContext().getResources().getString(R.string.error_out_of_memory);
            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Prepare projector in order to fill whole area
     */
    private void prepareProjector()
    {
        // default projector settings
        projector.setProjectionArea(rect.width(), rect.height());
        projector.setScaling(1f);
        projector.setDistance(100);
        projector.set2DScaling(1);
        projector.setRotationAngle(plotParameters.rotation);
        projector.setElevationAngle(plotParameters.elevation);
        projector.set2DTranslation(0, 0);

        // calculate max size
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        for (int i = 0; i < 8; i++)
        {
            projector.project(p1, cubeBounds[i][0], cubeBounds[i][1], cubeBounds[i][2]);
            minX = Math.min(minX, p1.x);
            maxX = Math.max(maxX, p1.x);
            minY = Math.min(minY, p1.y);
            maxY = Math.max(maxY, p1.y);
        }

        // set new scaling
        if (rect.width() != 0 && rect.height() != 0)
        {
            final double ratioX = ((double) (maxX - minX)) / ((double) rect.width());
            final double ratioY = ((double) (maxY - minY)) / ((double) rect.height());
            final double ratio = 1.05f * Math.max(ratioX, ratioY);
            if (ratio != 0.0f)
            {
                projector.set2DScaling(projector.get2DScaling() / ratio);
            }
        }

        // align to center
        minX = Integer.MAX_VALUE;
        maxX = Integer.MIN_VALUE;
        minY = Integer.MAX_VALUE;
        maxY = Integer.MIN_VALUE;
        for (int i = 0; i < 8; i++)
        {
            projector.project(p1, cubeBounds[i][0], cubeBounds[i][1], cubeBounds[i][2]);
            minX = Math.min(minX, p1.x);
            maxX = Math.max(maxX, p1.x);
            minY = Math.min(minY, p1.y);
            maxY = Math.max(maxY, p1.y);
        }
        final int centerX = (minX + maxX) / 2;
        final int centerY = (minY + maxY) / 2;
        projector.set2DTranslation(rect.centerX() - centerX, rect.centerY() - centerY);
    }

    /**
     * Draws the bounding box of surface.
     */
    private void drawBoundingBox(Canvas canvas)
    {
        projector.project(p2, factor_x * 10, factor_y * 10, 10);
        paint.setColor(getPaint().getColor());
        projector.project(p1, -factor_x * 10, factor_y * 10, 10);
        drawLine(canvas, p2.x, p2.y, p1.x, p1.y);
        projector.project(p1, factor_x * 10, -factor_y * 10, 10);
        drawLine(canvas, p2.x, p2.y, p1.x, p1.y);
        projector.project(p1, factor_x * 10, factor_y * 10, -10);
        drawLine(canvas, p2.x, p2.y, p1.x, p1.y);
    }

    /**
     * Draws non-surface parts, i.e: bounding box, axis grids, axis labels, base plane.
     */
    private void drawBoxGridsLabels(Canvas canvas)
    {
        boolean x_left = false, y_left = false;
        int i;

        factor_x = factor_y = 1;
        projector.project(p1, 0, 0, -10);
        poly_x[0] = p1.x;
        projector.project(p1, 10.5f, 0, -10);
        y_left = p1.x > poly_x[0];
        i = p1.y;
        projector.project(p1, -10.5f, 0, -10);
        if (p1.y > i)
        {
            factor_x = -1;
            y_left = p1.x > poly_x[0];
        }
        projector.project(p1, 0, 10.5f, -10);
        x_left = p1.x > poly_x[0];
        i = p1.y;
        projector.project(p1, 0, -10.5f, -10);
        if (p1.y > i)
        {
            factor_y = -1;
            x_left = p1.x > poly_x[0];
        }

        // the left z plane
        final int zLabelsNumber = colorMapView.getColorMapParameters().zLabelsNumber;
        {
            projector.project(p1, -factor_x * 10, -factor_y * 10, -10);
            poly_x[0] = p1.x;
            poly_y[0] = p1.y;
            projector.project(p1, -factor_x * 10, -factor_y * 10, 10);
            poly_x[1] = p1.x;
            poly_y[1] = p1.y;
            projector.project(p1, factor_x * 10, -factor_y * 10, 10);
            poly_x[2] = p1.x;
            poly_y[2] = p1.y;
            projector.project(p1, factor_x * 10, -factor_y * 10, -10);
            poly_x[3] = p1.x;
            poly_y[3] = p1.y;
            poly_x[4] = poly_x[0];
            poly_y[4] = poly_y[0];
            paint.setColor(COLOR_WALL);
            drawFilledPolygon(canvas, 4, poly_x, poly_y, null);
            paint.setColor(getPaint().getColor());
            drawPolygon(canvas, 5, poly_x, poly_y);
            paint.setColor(axisParameters.gridLineColor);
            for (i = 1; i < zLabelsNumber + 1; i++)
            {
                final double delta = 20.0f / (zLabelsNumber + 1);
                projector.project(p1, -factor_x * 10, -factor_y * 10, -10 + i * delta);
                projector.project(p2, factor_x * 10, -factor_y * 10, -10 + i * delta);
                drawLine(canvas, p1.x, p1.y, p2.x, p2.y);
            }
            for (i = 1; i < axisParameters.xLabelsNumber + 1; i++)
            {
                final double delta = 20.0f / (axisParameters.xLabelsNumber + 1);
                projector.project(p1, (-10 + i * delta), -factor_y * 10, -10);
                projector.project(p2, (-10 + i * delta), -factor_y * 10, 10);
                drawLine(canvas, p1.x, p1.y, p2.x, p2.y);
            }
        }

        // the right z plane
        {
            projector.project(p1, -factor_x * 10, factor_y * 10, 10);
            poly_x[2] = p1.x;
            poly_y[2] = p1.y;
            projector.project(p1, -factor_x * 10, factor_y * 10, -10);
            poly_x[3] = p1.x;
            poly_y[3] = p1.y;
            poly_x[4] = poly_x[0];
            poly_y[4] = poly_y[0];
            paint.setColor(COLOR_WALL);
            drawFilledPolygon(canvas, 4, poly_x, poly_y, null);
            paint.setColor(getPaint().getColor());
            drawPolygon(canvas, 5, poly_x, poly_y);
            paint.setColor(axisParameters.gridLineColor);
            for (i = 1; i < zLabelsNumber + 1; i++)
            {
                final double delta = 20.0f / (zLabelsNumber + 1);
                projector.project(p1, -factor_x * 10, factor_y * 10, -10 + i * delta);
                projector.project(p2, -factor_x * 10, -factor_y * 10, -10 + i * delta);
                drawLine(canvas, p1.x, p1.y, p2.x, p2.y);
            }
            for (i = 1; i < axisParameters.yLabelsNumber + 1; i++)
            {
                final double delta = 20.0f / (axisParameters.yLabelsNumber + 1);
                projector.project(p1, -factor_x * 10, (-10 + i * delta), -10);
                projector.project(p2, -factor_x * 10, (-10 + i * delta), 10);
                drawLine(canvas, p1.x, p1.y, p2.x, p2.y);
            }
        }

        // base
        {
            for (i = 0; i < 4; i++)
            {
                projector.project(p1, cubeBounds[i][0], cubeBounds[i][1], cubeBounds[i][2]);
                poly_x[i] = p1.x;
                poly_y[i] = p1.y;
            }
            poly_x[4] = poly_x[0];
            poly_y[4] = poly_y[0];
            paint.setColor(COLOR_WALL);
            drawFilledPolygon(canvas, 4, poly_x, poly_y, null);
            paint.setColor(getPaint().getColor());
            drawPolygon(canvas, 5, poly_x, poly_y);
            for (i = 0; i <= axisParameters.xLabelsNumber + 1; i++)
            {
                final double delta = 20.0f / (axisParameters.xLabelsNumber + 1);
                if (i > 0 && i < axisParameters.xLabelsNumber + 1)
                {
                    projector.project(p1, (-10 + i * delta), factor_y * 10, -10);
                    projector.project(p2, (-10 + i * delta), -factor_y * 10, -10);
                    paint.setColor(axisParameters.gridLineColor);
                    drawLine(canvas, p1.x, p1.y, p2.x, p2.y);
                }
                if (xLabels != null && i < xLabels.length)
                {
                    projector.project(p1, (-10 + i * delta), factor_y * 10.5f, -10);
                    drawString(canvas, p1.x, p1.y, xLabels[i].name, x_left ? LEFT : RIGHT, TOP, 1);
                }
            }
            for (i = 0; i <= axisParameters.yLabelsNumber + 1; i++)
            {
                final double delta = 20.0f / (axisParameters.yLabelsNumber + 1);
                if (i > 0 && i < axisParameters.yLabelsNumber + 1)
                {
                    projector.project(p1, factor_x * 10, (-10 + i * delta), -10);
                    projector.project(p2, -factor_x * 10, (-10 + i * delta), -10);
                    paint.setColor(axisParameters.gridLineColor);
                    drawLine(canvas, p1.x, p1.y, p2.x, p2.y);
                }
                if (yLabels != null && i < yLabels.length)
                {
                    projector.project(p1, factor_x * 10.5f, (-10 + i * delta), -10);
                    drawString(canvas, p1.x, p1.y, yLabels[i].name, y_left ? LEFT : RIGHT, TOP, 1);
                }
            }
        }

        if (isDrawLabels)
        {
            final float labelsScale = 2;
            projector.project(p2, 0, factor_y * 13, -10);
            drawString(canvas, p2.x, p2.y, functions.get(0).getLabels()[FunctionIf.X], CENTER, TOP, labelsScale);
            projector.project(p2, factor_x * 13, 0, -10);
            drawString(canvas, p2.x, p2.y, functions.get(0).getLabels()[FunctionIf.Y], CENTER, TOP, labelsScale);
            projector.project(p2, -factor_x * 10, factor_y * 12, 0);
            drawString(canvas, p2.x, p2.y, functions.get(0).getLabels()[FunctionIf.Z], CENTER, TOP, labelsScale);
        }
    }

    /**
     * Plots a single plane
     */
    private void drawPlane(Canvas canvas, Point3D[] vertex, int verticescount)
    {
        final double[] minMaxValues = functions.get(0).getMinMaxValues(FunctionIf.Z);
        final double zmin = minMaxValues[FunctionIf.MIN];
        final double zmax = minMaxValues[FunctionIf.MAX];
        double result;
        int count = 0;
        double z = 0.0f;
        boolean low1 = (vertex[0].z < zmin);
        boolean valid1 = !low1 && (vertex[0].z <= zmax);
        int index = 1;
        for (int loop = 0; loop < verticescount; loop++)
        {
            final boolean low2 = (vertex[index].z < zmin);
            final boolean valid2 = !low2 && (vertex[index].z <= zmax);
            if ((valid1 || valid2) || (low1 ^ low2))
            {
                if (!valid1)
                {
                    if (low1)
                    {
                        result = zmin;
                    }
                    else
                    {
                        result = zmax;
                    }
                    final double ratio = (result - vertex[index].z) / (vertex[loop].z - vertex[index].z);
                    final double new_x = ratio * (vertex[loop].x - vertex[index].x) + vertex[index].x;
                    final double new_y = ratio * (vertex[loop].y - vertex[index].y) + vertex[index].y;
                    if (low1)
                    {
                        projector.project(p1, new_x, new_y, -10);
                    }
                    else
                    {
                        projector.project(p1, new_x, new_y, 10);
                    }
                    poly_x[count] = p1.x;
                    poly_y[count] = p1.y;
                    color[count] = colorMapView.getPaletteColor(result, minMaxValues, plotParameters.meshOpacity);
                    count++;
                    z += result;
                }
                if (valid2)
                {
                    vertex[index].projection(p1, projector);
                    poly_x[count] = p1.x;
                    poly_y[count] = p1.y;
                    color[count] = colorMapView.getPaletteColor(vertex[index].z, minMaxValues,
                            plotParameters.meshOpacity);
                    count++;
                    z += vertex[index].z;
                }
                else
                {
                    if (low2)
                    {
                        result = zmin;
                    }
                    else
                    {
                        result = zmax;
                    }
                    final double ratio = (result - vertex[loop].z) / (vertex[index].z - vertex[loop].z);
                    final double new_x = ratio * (vertex[index].x - vertex[loop].x) + vertex[loop].x;
                    final double new_y = ratio * (vertex[index].y - vertex[loop].y) + vertex[loop].y;
                    if (low2)
                    {
                        projector.project(p1, new_x, new_y, -10);
                    }
                    else
                    {
                        projector.project(p1, new_x, new_y, 10);
                    }
                    poly_x[count] = p1.x;
                    poly_y[count] = p1.y;
                    color[count] = colorMapView.getPaletteColor(result, minMaxValues, plotParameters.meshOpacity);
                    count++;
                    z += result;
                }
            }
            if (++index == verticescount)
            {
                index = 0;
            }
            valid1 = valid2;
            low1 = low2;
        }
        if (count > 0)
        {
            paint.setColor(colorMapView.getPaletteColor(z / count, minMaxValues, 255));
            if (plotParameters.meshFill)
            {
                drawFilledPolygon(canvas, count, poly_x, poly_y, color);
            }
            if (plotParameters.meshLines)
            {
                paint.setColor(COLOR_MESH_LINES);
            }

            if (plotParameters.meshLines || !plotParameters.meshFill)
            {
                poly_x[count] = poly_x[0];
                poly_y[count] = poly_y[0];
                count++;
                drawPolygon(canvas, count, poly_x, poly_y);
                paint.setAlpha(255);
            }
        }
    }

    /**
     * Determines whether a plane is plottable, i.e: does not have invalid vertex.
     */
    private static boolean isPointsValid(Point3D[] values)
    {
        return (!values[0].isInvalid() && !values[1].isInvalid() && !values[2].isInvalid() && !values[3].isInvalid());
    }

    /**
     * Plots an area of group of planes
     */
    private void drawArea(Canvas canvas, int start_lx, int start_ly, int end_lx, int end_ly, int sx, int sy)
    {
        final int calc_divisionsY = functions.get(0).getYValues().length - 1;
        start_lx *= calc_divisionsY + 1;
        sx *= calc_divisionsY + 1;
        end_lx *= calc_divisionsY + 1;

        for (int ly = start_ly; ly != end_ly; ly += sy)
        {
            tmpVertex[1] = vertex[start_lx + ly];
            tmpVertex[2] = vertex[start_lx + ly + sy];
            for (int lx = start_lx; lx != end_lx; lx += sx)
            {
                tmpVertex[0] = tmpVertex[1];
                tmpVertex[1] = vertex[lx + sx + ly];
                tmpVertex[3] = tmpVertex[2];
                tmpVertex[2] = vertex[lx + sx + ly + sy];
                if (isPointsValid(tmpVertex))
                {
                    drawPlane(canvas, tmpVertex, 4);
                }
            }
        }
    }

    /**
     * Creates a surface plot
     */
    private void drawSurface(Canvas canvas)
    {
        final int calc_divisionsX = functions.get(0).getXValues().length - 1;
        final int calc_divisionsY = functions.get(0).getYValues().length - 1;

        int sx, sy;
        int start_lx, end_lx;
        int start_ly, end_ly;

        // direction test
        final double distance = projector.getDistance() * projector.getCosElevationAngle();

        // cop : center of projection
        cop.x = distance * projector.getSinRotationAngle();
        cop.y = distance * projector.getCosRotationAngle();
        cop.z = projector.getDistance() * projector.getSinElevationAngle();
        cop.transform(projector);

        final boolean inc_x = cop.x > 0;
        final boolean inc_y = cop.y > 0;

        if (inc_x)
        {
            start_lx = 0;
            end_lx = calc_divisionsX;
            sx = 1;
        }
        else
        {
            start_lx = calc_divisionsX;
            end_lx = 0;
            sx = -1;
        }
        if (inc_y)
        {
            start_ly = 0;
            end_ly = calc_divisionsY;
            sy = 1;
        }
        else
        {
            start_ly = calc_divisionsY;
            end_ly = 0;
            sy = -1;
        }

        if ((cop.x > 10) || (cop.x < -10))
        {
            if ((cop.y > 10) || (cop.y < -10))
            {
                // without split
                drawArea(canvas, start_lx, start_ly, end_lx, end_ly, sx, sy);
            }
            else
            {
                // split in y direction
                int split_y = (int) ((cop.y + 10) * calc_divisionsY / 20);
                drawArea(canvas, start_lx, 0, end_lx, split_y, sx, 1);
                drawArea(canvas, start_lx, calc_divisionsY, end_lx, split_y, sx, -1);
            }
        }
        else
        {
            if ((cop.y > 10) || (cop.y < -10))
            {
                // split in x direction
                int split_x = (int) ((cop.x + 10) * calc_divisionsX / 20);
                drawArea(canvas, 0, start_ly, split_x, end_ly, 1, sy);
                drawArea(canvas, calc_divisionsX, start_ly, split_x, end_ly, -1, sy);
            }
            else
            {
                // split in both x and y directions
                int split_x = (int) ((cop.x + 10) * calc_divisionsX / 20);
                int split_y = (int) ((cop.y + 10) * calc_divisionsY / 20);
                drawArea(canvas, 0, 0, split_x, split_y, 1, 1);
                drawArea(canvas, 0, calc_divisionsY, split_x, split_y, 1, -1);
                drawArea(canvas, calc_divisionsX, 0, split_x, split_y, -1, 1);
                drawArea(canvas, calc_divisionsX, calc_divisionsY, split_x, split_y, -1, -1);
            }
        }
    }

    private void drawLine(Canvas canvas, int x1, int y1, int x2, int y2)
    {
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(x1, y1, x2, y2, paint);
    }

    private void drawFilledPolygon(Canvas canvas, int count, int[] poly_x, int[] poly_y, int[] col)
    {
        paint.setStyle(Paint.Style.FILL);
        if (count == 4)
        {
            drawVertices(canvas, count, poly_x, poly_y, col);
            return;
        }
        path.reset();
        path.moveTo(poly_x[0], poly_y[0]);
        for (int i = 1; i < count; i++)
        {
            path.lineTo(poly_x[i], poly_y[i]);
        }
        path.close();
        canvas.drawPath(path, paint);
    }

    private void drawPolygon(Canvas canvas, int count, int[] poly_x, int[] poly_y)
    {
        paint.setStyle(Paint.Style.STROKE);
        for (int i = 1; i < count; i++)
        {
            canvas.drawLine(poly_x[i - 1], poly_y[i - 1], poly_x[i], poly_y[i], paint);
        }
    }

    public void drawString(Canvas canvas, int x, int y, String s, int x_align, int y_align, float scale)
    {
        if (s == null)
        {
            return;
        }
        paint.setColor(getPaint().getColor());
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize((axisParameters.getLabelTextSize() - 3) * scale);
        paint.getTextBounds(s, 0, s.length(), tmpRect);
        switch (y_align)
        {
        case TOP:
            y += tmpRect.height();
            break;
        case CENTER:
            y += tmpRect.height() / 2;
            break;
        }
        switch (x_align)
        {
        case LEFT:
            // nothing to do
            break;
        case RIGHT:
            x -= tmpRect.width();
            break;
        case CENTER:
            x -= tmpRect.width() / 2;
            break;
        }
        canvas.drawText(s, x, y, paint);
    }

    protected void drawVertices(Canvas canvas, int count, int[] poly_x, int[] poly_y, int[] col)
    {
        vertsValues[0] = poly_x[0];
        vertsValues[1] = poly_y[0];
        vertsValues[2] = poly_x[1];
        vertsValues[3] = poly_y[1];
        vertsValues[4] = poly_x[2];
        vertsValues[5] = poly_y[2];
        vertsValues[6] = poly_x[0];
        vertsValues[7] = poly_y[0];
        vertsValues[8] = poly_x[3];
        vertsValues[9] = poly_y[3];
        vertsValues[10] = poly_x[2];
        vertsValues[11] = poly_y[2];
        if (col != null)
        {
            vertsColors[0] = col[0];
            vertsColors[1] = col[1];
            vertsColors[2] = col[2];
            vertsColors[3] = col[0];
            vertsColors[4] = col[3];
            vertsColors[5] = col[2];
            vertsColors[6] = 0xFF000000;
            vertsColors[7] = 0xFF000000;
            vertsColors[8] = 0xFF000000;
            vertsColors[9] = 0xFF000000;
            vertsColors[10] = 0xFF000000;
            vertsColors[11] = 0xFF000000;
        }
        else
        {
            for (int i = 0; i < vertsColors.length; i++)
            {
                vertsColors[i] = paint.getColor();
            }
        }
        canvas.drawVertices(Canvas.VertexMode.TRIANGLES, vertsValues.length, vertsValues, 0, null, 0, vertsColors, 0,
                null, 0, 0, paint);
    }
}
