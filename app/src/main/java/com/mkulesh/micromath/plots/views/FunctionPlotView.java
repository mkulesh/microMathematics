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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;

import com.mkulesh.micromath.plots.FunctionIf;
import com.mkulesh.micromath.properties.LineProperties.ShapeType;

public class FunctionPlotView extends PlanePlotView
{
    private final Path shapePath = new Path();
    private final Paint shapePaint = new Paint();

    /*--------------------------------------------------------*
     * Creating
     *--------------------------------------------------------*/

    public FunctionPlotView(Context context)
    {
        super(context);
    }

    public FunctionPlotView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.prepare(attrs);
    }

    public FunctionPlotView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        this.prepare(attrs);
    }

    private void prepare(AttributeSet attrs)
    {
        // empty
    }

    @Override
    protected void drawContent(Canvas c, FunctionIf f)
    {
        final double[] xVal = f.getXValues();
        final double[] yVal = f.getYValues();
        if (xVal == null || yVal == null || f.getLineParameters() == null)
        {
            return;
        }
        path.reset();

        final int n = Math.min(xVal.length, yVal.length);
        final double xmax = area.getMax().x + Math.abs(area.getDim().x);
        final double ymax = area.getMax().y + Math.abs(area.getDim().y);
        final double xmin = area.getMin().x - Math.abs(area.getDim().x);
        final double ymin = area.getMin().y - Math.abs(area.getDim().y);

        final Paint linePaint = f.getLineParameters().getPaint();
        float shapeSize = 0;
        if (f.getLineParameters().shapeType != ShapeType.NONE)
        {
            shapePath.reset();
            shapePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            shapePaint.setColor(linePaint.getColor());
            shapePaint.setStrokeWidth(0);
            shapePaint.setAntiAlias(true);
            shapeSize = linePaint.getStrokeWidth() * ((float) f.getLineParameters().shapeSize) / 200.0f;
            if (f.getLineParameters().shapeType == ShapeType.SQUARE
                    || f.getLineParameters().shapeType == ShapeType.CROSS)
            {
                shapeSize /= Math.sqrt(2.0);
            }
        }

        int outside = 0;
        for (int i = 0; i < n; i++)
        {
            boolean startPoint = (i == 0);

            // Prepare the function point
            double xv = xVal[i];
            {
                xv = (Double.isNaN(xv) || xv > xmax) ? xmax : (Math.max(xv, xmin));
            }
            double yv = yVal[i];
            {
                yv = (Double.isNaN(yv) || yv > ymax) ? ymax : (Math.max(yv, ymin));
            }
            tmpVec.set(xv, yv);

            // Check whether the function point is inside if the plotting area
            if (!area.isInside(tmpVec))
            {
                outside++;
            }
            else
            {
                outside = 0;
            }

            // For the 2-nd point outside the area, move point to the new position
            // instead of the line
            if (outside >= 2)
            {
                startPoint = true;
            }

            // Convert to screen coordinates
            area.toScreenPoint(tmpVec, rect, p1);

            if (startPoint)
            {
                path.moveTo(p1.x, p1.y);
            }
            else if (!p1.equals(p2.x, p2.y))
            {
                path.lineTo(p1.x, p1.y);
            }

            // plot a shape
            switch (f.getLineParameters().shapeType)
            {
            case CIRCLE:
                c.drawCircle(p1.x, p1.y, shapeSize, shapePaint);
                break;
            case CROSS:
                c.drawLine(p1.x - shapeSize, p1.y - shapeSize, p1.x + shapeSize, p1.y + shapeSize, linePaint);
                c.drawLine(p1.x - shapeSize, p1.y + shapeSize, p1.x + shapeSize, p1.y - shapeSize, linePaint);
                break;
            case DIAMOND:
                shapePath.rewind();
                shapePath.moveTo(p1.x, p1.y - shapeSize);
                shapePath.lineTo(p1.x + shapeSize, p1.y);
                shapePath.lineTo(p1.x, p1.y + shapeSize);
                shapePath.lineTo(p1.x - shapeSize, p1.y);
                c.drawPath(shapePath, shapePaint);
                break;
            case SQUARE:
                c.drawRect(p1.x - shapeSize, p1.y - shapeSize, p1.x + shapeSize, p1.y + shapeSize, shapePaint);
                break;
            case NONE:
            default:
                break;
            }
            p2.set(p1.x, p1.y);
        }

        c.drawPath(path, linePaint);
    }
}
