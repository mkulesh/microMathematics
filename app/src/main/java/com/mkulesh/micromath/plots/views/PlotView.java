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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;

import com.mkulesh.micromath.R;
import com.mkulesh.micromath.properties.AxisProperties;
import com.mkulesh.micromath.properties.LineProperties;
import com.mkulesh.micromath.properties.PlotProperties;
import com.mkulesh.micromath.widgets.CustomTextView;
import com.mkulesh.micromath.widgets.FormulaChangeIf;

public abstract class PlotView extends CustomTextView
{
    /*
     * Constants used to save/restore the instance state.
     */
    private static final String STATE_LINE_PARAMETERS = "line_parameters";
    private static final String STATE_AXIS_PARAMETERS = "axis_parameters";
    private static final String STATE_PLOT_PARAMETERS = "plot_parameters";

    // settings
    protected final LineProperties lineParameters = new LineProperties();
    protected final AxisProperties axisParameters = new AxisProperties();
    protected final PlotProperties plotParameters = new PlotProperties();

    // cache
    protected Bitmap drawingCache = null;

    // data
    protected int significantDigits = 6;

    // temporary variables used for drawing
    protected final Paint paint = new Paint();
    protected final Path path = new Path();

    /*********************************************************
     * Creating
     *********************************************************/

    public PlotView(Context context)
    {
        super(context);
    }

    public PlotView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.prepare(attrs);
    }

    public PlotView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        this.prepare(attrs);
    }

    private void prepare(AttributeSet attrs)
    {
        if (attrs != null)
        {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.PlotViewExtension, 0, 0);
            lineParameters.initialize(a);
            axisParameters.initialize(a);
            a.recycle();
        }
    }

    public void prepare(AppCompatActivity activity, FormulaChangeIf termChangeIf)
    {
        super.prepare(SymbolType.EMPTY, activity, termChangeIf);
        plotParameters.initialize(getContext());
    }

    /*********************************************************
     * Read/write interface
     *********************************************************/

    /**
     * Parcelable interface: procedure writes the formula state
     */
    @SuppressLint("MissingSuperCall")
    public Parcelable onSaveInstanceState()
    {
        Bundle bundle = new Bundle();
        // line parameters
        {
            LineProperties lp = new LineProperties();
            lp.assign(lineParameters);
            bundle.putParcelable(STATE_LINE_PARAMETERS, lp);
        }
        // axis parameters
        {
            AxisProperties ap = new AxisProperties();
            ap.assign(axisParameters);
            bundle.putParcelable(STATE_AXIS_PARAMETERS, ap);
        }
        // plot parameters
        {
            PlotProperties pp = new PlotProperties();
            pp.assign(plotParameters);
            bundle.putParcelable(STATE_PLOT_PARAMETERS, pp);
        }
        return bundle;
    }

    /**
     * Parcelable interface: procedure reads the formula state
     */
    @SuppressLint("MissingSuperCall")
    public void onRestoreInstanceState(Parcelable state)
    {
        if (state == null)
        {
            return;
        }
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            lineParameters.assign((LineProperties) bundle.getParcelable(STATE_LINE_PARAMETERS));
            axisParameters.assign((AxisProperties) bundle.getParcelable(STATE_AXIS_PARAMETERS));
            plotParameters.assign((PlotProperties) bundle.getParcelable(STATE_PLOT_PARAMETERS));
        }
    }

    /*********************************************************
     * Properties
     *********************************************************/

    public LineProperties getLineParameters()
    {
        return lineParameters;
    }

    public AxisProperties getAxisParameters()
    {
        return axisParameters;
    }

    public PlotProperties getPlotParameters()
    {
        return plotParameters;
    }

    /*********************************************************
     * Data
     *********************************************************/

    public abstract void setArea(double minX, double maxX, double minY, double maxY);

    public abstract void setScale(float scaleFactor);

    public abstract void updateLabels();

    public void setSignificantDigits(int significantDigits)
    {
        this.significantDigits = significantDigits;
    }

    /*********************************************************
     * Painting
     *********************************************************/

    public void clearDrawingCache()
    {
        drawingCache = null;
    }

    @Override
    public void invalidate()
    {
        drawingCache = null;
        super.invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        drawingCache = null;
        super.onLayout(changed, left, top, right, bottom);
    }
}
