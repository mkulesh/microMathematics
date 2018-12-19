package com.mkulesh.micromath.plots.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.Toast;

import com.mkulesh.micromath.R;
import com.mkulesh.micromath.math.Vector2D;
import com.mkulesh.micromath.properties.LineProperties.ShapeType;
import com.mkulesh.micromath.utils.ViewUtils;

import java.util.ArrayList;

public class FunctionView extends PlotView
{
    // data
    private ArrayList<Vector2D> values = null;
    private final PhysicalArea area = new PhysicalArea();
    private final Vector2D labelCenter = new Vector2D();
    private int arrowLength = 0, arrowStroke = 0;
    private Label[] xLabels = null;
    private Label[] yLabels = null;

    // temporary variables used for drawing
    private final Rect rect = new Rect(), tmpRect = new Rect();
    private final Point p1 = new Point(), p2 = new Point();
    private final Vector2D tmpVec = new Vector2D();
    private final Path shapePath = new Path();
    private final Paint shapePaint = new Paint();

    /*********************************************************
     * Creating
     *********************************************************/

    public FunctionView(Context context)
    {
        super(context);
    }

    public FunctionView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.prepare(attrs);
    }

    public FunctionView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        this.prepare(attrs);
    }

    private void prepare(AttributeSet attrs)
    {
        // Empty
    }

    /*********************************************************
     * Data
     *********************************************************/

    public void setFunction(ArrayList<Vector2D> values)
    {
        this.values = values;
    }

    public ArrayList<Vector2D> getFunction()
    {
        return values;
    }

    public void setArea(double minX, double maxX, double minY, double maxY)
    {
        area.set(minX, maxX, minY, maxY);
        updateLabels();
    }

    public void setScale(float scaleFactor)
    {
        clearDrawingCache();
        axisParameters.scaleFactor = scaleFactor;
    }

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

        this.xLabels = makeLabels(0, axisParameters.xLabelsNumber);
        this.yLabels = makeLabels(1, axisParameters.yLabelsNumber);
    }

    /*********************************************************
     * Painting
     *********************************************************/

    private void getScaledPadding(Rect r)
    {
        arrowStroke = 2 * axisParameters.getLabelLineSize();
        arrowLength = (plotParameters.isCrossedAxes()) ? 4 * arrowStroke : 0;
        r.set(getPaddingLeft() + arrowStroke, getPaddingTop() + arrowLength, getPaddingRight() + arrowLength,
                getPaddingBottom() + arrowStroke);
    }

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
                if (values != null && !values.isEmpty())
                {
                    drawGrid(0, c, paint);
                    drawGrid(1, c, paint);

                    // function line
                    c.save();
                    c.clipRect(rect);
                    drawFunction(c, values);
                    c.restore();

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

    private void drawBorder(Canvas c, Paint p)
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

    private void drawCross(Canvas c, Paint p)
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

    void drawFunction(Canvas c, ArrayList<Vector2D> f)
    {
        path.reset();
        final double xmax = area.getMax().x + Math.abs(area.getDim().x);
        final double ymax = area.getMax().y + Math.abs(area.getDim().y);
        final double xmin = area.getMin().x - Math.abs(area.getDim().x);
        final double ymin = area.getMin().y - Math.abs(area.getDim().y);

        final Paint linePaint = lineParameters.getPaint();
        float shapeSize = 0;
        if (lineParameters.shapeType != ShapeType.NONE)
        {
            shapePath.reset();
            shapePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            shapePaint.setColor(linePaint.getColor());
            shapePaint.setStrokeWidth(0);
            shapePaint.setAntiAlias(true);
            shapeSize = linePaint.getStrokeWidth() * ((float) lineParameters.shapeSize) / 200.0f;
            if (lineParameters.shapeType == ShapeType.SQUARE || lineParameters.shapeType == ShapeType.CROSS)
            {
                shapeSize /= Math.sqrt(2.0);
            }
        }

        int outside = 0;
        for (int i = 0; i < f.size(); i++)
        {
            boolean startPoint = (i == 0);

            // Prepare the function point
            final Vector2D value = f.get(i);
            double xv = value.x;
            {
                xv = (Double.isNaN(xv) || xv > xmax) ? xmax : ((xv < xmin) ? xmin : xv);
            }
            double yv = value.y;
            {
                yv = (Double.isNaN(yv) || yv > ymax) ? ymax : ((yv < ymin) ? ymin : yv);
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
            switch (lineParameters.shapeType)
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
                break;
            default:
                break;
            }
            p2.set(p1.x, p1.y);
        }

        c.drawPath(path, linePaint);
    }

    private class Label
    {
        Vector2D point = null;
        String name = null;

        public Label(int idx, double v, Vector2D lc)
        {
            point = (idx == 0) ? new Vector2D(v, lc.y) : new Vector2D(lc.x, v);
        }
    }

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
            if (idx == 0)
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

    private void drawGrid(int idx, Canvas c, Paint p)
    {
        final Label[] labels = (idx == 0) ? xLabels : yLabels;
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
            if (idx == 0)
            {
                c.drawLine(p1.x, rect.top, p1.x, rect.bottom, p);
            }
            else
            {
                c.drawLine(rect.left, p1.y, rect.right, p1.y, p);
            }
        }
    }

    private void drawLabeles(int idx, Canvas c, Paint p)
    {
        final Label[] labels = (idx == 0) ? xLabels : yLabels;
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
            if (idx == 0)
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

    private void drawHorArrowHead(Canvas c, Point p0, int width, int lenght, Paint p)
    {
        p.setStrokeWidth(0);
        for (int i = 0; i < lenght / 2; i++)
        {
            c.drawLine(p0.x - i, p0.y, p0.x - lenght, p0.y - width, p);
            c.drawLine(p0.x - i, p0.y, p0.x - lenght, p0.y + width, p);
        }
    }

    private void drawVerArrowHead(Canvas c, Point p0, int width, int lenght, Paint p)
    {
        p.setStrokeWidth(0);
        for (int i = 0; i < lenght / 2; i++)
        {
            c.drawLine(p0.x, p0.y + i, p0.x - width, p0.y + lenght, p);
            c.drawLine(p0.x, p0.y + i, p0.x + width, p0.y + lenght, p);
        }
    }

}
