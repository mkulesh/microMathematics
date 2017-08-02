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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

import com.mkulesh.micromath.plots.FunctionIf;
import com.mkulesh.micromath.properties.PlotProperties.TwoDPlotStyle;

public class ContourPlotView extends PlanePlotView
{
    /*********************************************************
     * Creating
     *********************************************************/

    public ContourPlotView(Context context)
    {
        super(context);
    }

    public ContourPlotView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.prepare(attrs);
    }

    public ContourPlotView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        this.prepare(attrs);
    }

    private void prepare(AttributeSet attrs)
    {
        plotParameters.twoDPlotStyle = TwoDPlotStyle.CONTOUR;
    }

    @Override
    protected void drawContent(Canvas c, FunctionIf f)
    {
        final Paint p = paint;
        final double[] xVal = f.getXValues();
        final double[] yVal = f.getYValues();
        final double[][] zVal = f.getZValues();
        if (xVal == null || yVal == null || zVal == null)
        {
            return;
        }
        final double xRange = f.getMinMaxValues(FunctionIf.X)[FunctionIf.MAX]
                - f.getMinMaxValues(FunctionIf.X)[FunctionIf.MIN];
        final double yRange = f.getMinMaxValues(FunctionIf.Y)[FunctionIf.MAX]
                - f.getMinMaxValues(FunctionIf.Y)[FunctionIf.MIN];
        final double dx = (xVal.length <= 1) ? xRange : xRange / ((double) (2 * xVal.length - 2));
        final double dy = (yVal.length <= 1) ? yRange : yRange / ((double) (2 * yVal.length - 2));
        p.setStyle(Paint.Style.FILL);
        for (int i = 0; i < xVal.length; i++)
        {
            int prevBottom = -1;
            for (int j = 0; j < yVal.length; j++)
            {
                // x1
                tmpVec.x = xVal[i] - dx;
                if (tmpVec.x > area.getMax().x)
                {
                    continue;
                }
                if (tmpVec.x < area.getMin().x)
                {
                    tmpVec.x = area.getMin().x;
                }

                // y1
                tmpVec.y = yVal[j] - dy;
                if (tmpVec.y > area.getMax().y)
                {
                    continue;
                }
                if (tmpVec.y < area.getMin().y)
                {
                    tmpVec.y = area.getMin().y;
                }
                area.toScreenPoint(tmpVec, rect, p1);

                // x2
                tmpVec.x = xVal[i] + dx;
                if (tmpVec.x < area.getMin().x)
                {
                    continue;
                }
                if (tmpVec.x > area.getMax().x)
                {
                    tmpVec.x = area.getMax().x;
                }

                // y2
                tmpVec.y = yVal[j] + dy;
                if (tmpVec.y < area.getMin().y)
                {
                    continue;
                }
                if (tmpVec.y > area.getMax().y)
                {
                    tmpVec.y = area.getMax().y;
                }
                area.toScreenPoint(tmpVec, rect, p2);

                // z
                if (colorMapView != null)
                {
                    p.setColor(colorMapView.getPaletteColor(zVal[i][j], f.getMinMaxValues(FunctionIf.Z), 255));
                }

                // draw
                if (prevBottom >= 0)
                {
                    p1.y = prevBottom;
                }
                c.drawRect(p1.x, p1.y, p2.x, p2.y, paint);

                prevBottom = p2.y;
            }
        }
    }
}
