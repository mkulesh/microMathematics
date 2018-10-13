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
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;

import com.mkulesh.micromath.math.Vector2D;
import com.mkulesh.micromath.plots.FunctionIf;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.AxisProperties;
import com.mkulesh.micromath.properties.ColorMapProperties;
import com.mkulesh.micromath.properties.PlotProperties;
import com.mkulesh.micromath.utils.CompatUtils;
import com.mkulesh.micromath.widgets.CustomTextView;
import com.mkulesh.micromath.widgets.FormulaChangeIf;

import java.util.ArrayList;

public abstract class PlotView extends CustomTextView
{
    /*
     * Constants used to save/restore the instance state.
     */
    private static final String STATE_AXIS_PARAMETERS = "axis_parameters";
    private static final String STATE_PLOT_PARAMETERS = "plot_parameters";
    private static final String STATE_COLORMAP_PARAMETERS = "colormap_parameters";

    // settings
    protected final AxisProperties axisParameters = new AxisProperties();
    protected final PlotProperties plotParameters = new PlotProperties();
    protected ColorMapView colorMapView = null;

    // cache
    protected Bitmap drawingCache = null;

    // data
    protected ArrayList<FunctionIf> functions = null;
    protected int significantDigits = 6;

    /*********************************************************
     * Helper class that holds labels
     *********************************************************/
    public final class Label
    {
        Vector2D point = null;
        String name = null;

        public Label(int idx, double v, Vector2D lc)
        {
            point = (idx == FunctionIf.X) ? new Vector2D(v, lc.y) : new Vector2D(lc.x, v);
        }
    }

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
            axisParameters.initialize(a);
            a.recycle();
        }
    }

    public void prepare(AppCompatActivity activity, FormulaChangeIf termChangeIf)
    {
        super.prepare(SymbolType.EMPTY, activity, termChangeIf);
        getPaint().setColor(CompatUtils.getThemeColorAttr(activity, R.attr.colorFormulaNormal));
        plotParameters.initialize(getContext());
        if (colorMapView != null)
        {
            colorMapView.prepare(activity, termChangeIf);
        }
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
        // color map parameters
        if (colorMapView != null)
        {
            ColorMapProperties cp = new ColorMapProperties();
            cp.assign(colorMapView.getColorMapParameters());
            bundle.putParcelable(STATE_COLORMAP_PARAMETERS, cp);
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
            axisParameters.assign((AxisProperties) bundle.getParcelable(STATE_AXIS_PARAMETERS));
            plotParameters.assign((PlotProperties) bundle.getParcelable(STATE_PLOT_PARAMETERS));
            if (colorMapView != null)
            {
                colorMapView.getColorMapParameters().assign(
                        (ColorMapProperties) bundle.getParcelable(STATE_COLORMAP_PARAMETERS));
            }
        }
    }

    /*********************************************************
     * Properties
     *********************************************************/

    public AxisProperties getAxisParameters()
    {
        return axisParameters;
    }

    public PlotProperties getPlotParameters()
    {
        return plotParameters;
    }

    public ColorMapView getColorMapView()
    {
        return colorMapView;
    }

    public void setColorMapView(ColorMapView colorMapView)
    {
        this.colorMapView = colorMapView;
    }

    /*********************************************************
     * Data
     *********************************************************/

    public abstract void setArea(double minX, double maxX, double minY, double maxY);

    public abstract void setScale(float scaleFactor);

    public abstract void updateLabels();

    public ArrayList<FunctionIf> getFunctions()
    {
        return functions;
    }

    public void setFunctions(ArrayList<FunctionIf> functions)
    {
        this.functions = functions;
    }

    public void setFunction(FunctionIf function)
    {
        if (function == null)
        {
            functions = null;
            return;
        }

        if (functions == null)
        {
            functions = new ArrayList<>();
        }
        else
        {
            functions.clear();
        }
        functions.add(function);
        if (colorMapView != null)
        {
            colorMapView.setFunction(function);
        }
    }

    public void setSignificantDigits(int significantDigits)
    {
        this.significantDigits = significantDigits;
        if (colorMapView != null)
        {
            colorMapView.setSignificantDigits(significantDigits);
        }
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
        if (colorMapView != null)
        {
            colorMapView.invalidate();
        }
        super.invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        drawingCache = null;
        super.onLayout(changed, left, top, right, bottom);
    }
}
