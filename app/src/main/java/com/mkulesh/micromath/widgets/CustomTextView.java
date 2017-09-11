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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

import com.mkulesh.micromath.plus.R;

public class CustomTextView extends AppCompatTextView implements OnLongClickListener, OnClickListener
{

    public enum SymbolType
    {
        EMPTY,
        TEXT,
        LEFT_BRACKET,
        LEFT_SQR_BRACKET,
        RIGHT_BRACKET,
        RIGHT_SQR_BRACKET,
        PLUS,
        MINUS,
        MULT,
        HOR_LINE,
        VERT_LINE,
        SLASH,
        SUMMATION,
        PRODUCT,
        INTEGRAL
    };

    private SymbolType symbolType = SymbolType.TEXT;
    protected AppCompatActivity activity = null;
    protected final Paint paint = new Paint();
    private final RectF rect = new RectF();
    protected final Path path = new Path();
    private final RectF oval = new RectF();
    private boolean useExternalPaint = false;
    protected int strokeWidth = 0;

    // context menu handling
    private ContextMenuHandler menuHandler = null;
    private FormulaChangeIf formulaChangeIf = null;

    /*********************************************************
     * Creating
     *********************************************************/

    public CustomTextView(Context context)
    {
        super(context);
    }

    public CustomTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.prepare(attrs);
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        this.prepare(attrs);
    }

    private void prepare(AttributeSet attrs)
    {
        menuHandler = new ContextMenuHandler(getContext());
        if (attrs != null)
        {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomViewExtension, 0, 0);
            String s = a.getString(R.styleable.CustomViewExtension_symbol);
            if (s != null)
            {
                for (SymbolType f : SymbolType.values())
                {
                    if (s.equals(f.toString()))
                    {
                        symbolType = f;
                        break;
                    }
                }
            }
            menuHandler.initialize(a);
            a.recycle();
        }
    }

    public void prepare(SymbolType symbolType, AppCompatActivity activity, FormulaChangeIf termChangeIf)
    {
        this.symbolType = symbolType;
        this.activity = activity;
        this.formulaChangeIf = termChangeIf;
        this.setOnLongClickListener(this);
        this.setOnClickListener(this);
        paint.set(getPaint());
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        setSaveEnabled(false);
    }

    public void updateTextSize(ScaledDimensions dimen, int termDepth)
    {
        strokeWidth = dimen.get(ScaledDimensions.Type.STROKE_WIDTH);

        if (symbolType == SymbolType.SUMMATION || symbolType == SymbolType.PRODUCT || symbolType == SymbolType.INTEGRAL)
        {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, dimen.getTextSize(ScaledDimensions.Type.BIG_SYMBOL_SIZE, termDepth));
        }
        else
        {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, dimen.getTextSize(termDepth));
        }

        // Here is a bug on the old android versions:
        // http://stackoverflow.com/questions/9541196
        // TextView height doesn't change after shrinking the font size
        // Trick: reset text buffer
        setText(getText(), AppCompatTextView.BufferType.SPANNABLE);

        if (symbolType != null)
        {
            switch (symbolType)
            {
            case EMPTY:
                // nothing to do
                break;
            case HOR_LINE:
                setPadding(0, 0, 0, 0);
                setHeight(strokeWidth * 5);
                break;
            case LEFT_BRACKET:
            case LEFT_SQR_BRACKET:
            case RIGHT_BRACKET:
            case RIGHT_SQR_BRACKET:
                final int p1 = dimen.get(ScaledDimensions.Type.HOR_BRAKET_PADDING);
                setPadding(p1, 0, p1, 0);
                break;
            case SLASH:
                setPadding(0, 0, 0, 0);
                break;
            case INTEGRAL:
                final int p2 = dimen.get(ScaledDimensions.Type.HOR_SYMBOL_PADDING);
                setWidth(20 * strokeWidth);
                setHeight(60 * strokeWidth);
                setPadding(p2, 0, p2, 0);
            default:
                final int p3 = dimen.get(ScaledDimensions.Type.HOR_SYMBOL_PADDING);
                setPadding(p3, 0, p3, 0);
                break;
            }
        }
    }

    public SymbolType getSymbolType()
    {
        return symbolType;
    }

    public void setExternalPaint(Paint p)
    {
        if (p != null)
        {
            useExternalPaint = true;
            paint.set(p);
        }
        else
        {
            useExternalPaint = false;
            paint.reset();
        }

    }

    /*********************************************************
     * Painting
     *********************************************************/

    @Override
    public int getBaseline()
    {
        return (int) ((this.getMeasuredHeight() - getPaddingBottom() + getPaddingTop()) / 2);
    };

    @Override
    protected void onDraw(Canvas c)
    {
        if (symbolType == null)
        {
            return;
        }

        rect.set(getPaddingLeft(), getPaddingTop(), this.getRight() - this.getLeft() - getPaddingRight(),
                this.getBottom() - this.getTop() - getPaddingBottom());

        if (!useExternalPaint)
        {
            paint.setColor(getCurrentTextColor());
            paint.setStrokeWidth(strokeWidth);
        }

        switch (symbolType)
        {
        case EMPTY:
            // nothing to to
            break;
        case TEXT:
            super.onDraw(c);
            break;
        case LEFT_BRACKET:
            drawLeftBracket(c);
            break;
        case LEFT_SQR_BRACKET:
            drawLeftSqrBracket(c);
            break;
        case RIGHT_BRACKET:
            drawRightBracket(c);
            break;
        case RIGHT_SQR_BRACKET:
            drawRightSqrBracket(c);
            break;
        case PLUS:
            drawPlus(c);
            break;
        case MINUS:
            drawMinus(c);
            break;
        case MULT:
            drawMult(c);
            break;
        case HOR_LINE:
            drawHorLine(c);
            break;
        case VERT_LINE:
            drawVertLine(c);
            break;
        case SLASH:
            drawSlash(c);
            break;
        case SUMMATION:
            drawSummation(c);
            break;
        case PRODUCT:
            drawProduct(c);
            break;
        case INTEGRAL:
            drawIntegral(c);
            break;
        }

        // Test code to trace paddings:
        // paint.setColor(Color.BLUE);
        // paint.setStrokeWidth(0);
        // paint.setStyle(Paint.Style.STROKE);
        // c.drawRect(0, 0, this.getWidth(), this.getHeight(), paint);
        // paint.setColor(Color.GREEN);
        // c.drawRect(rect, paint);
    }

    private void drawLeftBracket(Canvas c)
    {
        paint.setStrokeWidth(1);
        rect.right += rect.width() * 1.5;
        for (int i = 1; i < strokeWidth + 1; i++)
        {
            rect.left += i;
            rect.right -= i;
            c.drawArc(rect, 0, 360, false, paint);
            rect.left -= i;
            rect.right += i;
        }
    }

    private void drawRightSqrBracket(Canvas c)
    {
        c.drawLine(rect.centerX(), rect.top, rect.centerX(), rect.bottom, paint);
        c.drawLine(rect.centerX(), rect.top, rect.left, rect.top, paint);
        c.drawLine(rect.centerX(), rect.bottom, rect.left, rect.bottom, paint);
    }

    private void drawRightBracket(Canvas c)
    {
        paint.setStrokeWidth(1);
        rect.left -= rect.width() * 1.5;
        for (int i = 1; i < strokeWidth + 1; i++)
        {
            rect.left += i;
            rect.right -= i;
            c.drawArc(rect, 0, 360, false, paint);
            rect.left -= i;
            rect.right += i;
        }
    }

    private void drawLeftSqrBracket(Canvas c)
    {
        c.drawLine(rect.centerX(), rect.top, rect.centerX(), rect.bottom, paint);
        c.drawLine(rect.centerX(), rect.top, rect.right, rect.top, paint);
        c.drawLine(rect.centerX(), rect.bottom, rect.right, rect.bottom, paint);
    }

    private void drawPlus(Canvas c)
    {
        float s = rect.width() / 2.0f;
        c.drawLine(rect.centerX() - s, rect.centerY(), rect.centerX() + s, rect.centerY(), paint);
        c.drawLine(rect.centerX(), rect.centerY() - s, rect.centerX(), rect.centerY() + s, paint);
    }

    private void drawMinus(Canvas c)
    {
        c.drawLine(rect.left, rect.centerY(), rect.right, rect.centerY(), paint);
    }

    private void drawMult(Canvas c)
    {
        paint.setStrokeWidth(1);
        c.drawPoint(rect.centerX(), rect.centerY(), paint);
        for (int i = 1; i < strokeWidth; i++)
        {
            c.drawCircle(rect.centerX(), rect.centerY(), i, paint);
        }
    }

    private void drawHorLine(Canvas c)
    {
        path.reset();
        path.moveTo(rect.left, rect.centerY());
        path.lineTo(rect.right, rect.centerY());
        c.drawPath(path, paint);
    }

    private void drawVertLine(Canvas c)
    {
        path.reset();
        path.moveTo(rect.centerX(), rect.top);
        path.lineTo(rect.centerX(), rect.bottom);
        c.drawPath(path, paint);
    }

    private void drawSlash(Canvas c)
    {
        c.drawLine(rect.left + strokeWidth, rect.bottom - strokeWidth, rect.right - strokeWidth,
                rect.top + strokeWidth, paint);
    }

    private void drawSummation(Canvas c)
    {
        final int sw1 = strokeWidth;
        final int sw2 = 2 * strokeWidth;
        final int sw3 = 3 * strokeWidth;
        final int sw4 = 4 * strokeWidth;
        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        path.reset();
        path.moveTo(rect.left, rect.top);
        path.lineTo(rect.left, rect.top + sw1);
        path.lineTo(rect.centerX(), rect.centerY());
        path.lineTo(rect.left, rect.bottom - sw1);
        path.lineTo(rect.left, rect.bottom);
        path.lineTo(rect.right - sw1, rect.bottom);
        path.lineTo(rect.right, rect.bottom - 2 * sw3);
        path.lineTo(rect.right - sw1 / 2, rect.bottom - 2 * sw3 - sw1 / 2);
        path.lineTo(rect.right - sw3, rect.bottom - sw3);
        path.lineTo(rect.left + sw3 + sw1 / 2, rect.bottom - sw3);
        path.lineTo(rect.centerX() + sw3, rect.centerY() - sw1);
        path.lineTo(rect.left + sw4, rect.top + sw2);
        path.lineTo(rect.right - sw4, rect.top + sw2);
        path.lineTo(rect.right - sw1 / 2, rect.top + 2 * sw3 + sw1 / 2);
        path.lineTo(rect.right, rect.top + 2 * sw3);
        path.lineTo(rect.right - sw1, rect.top);
        path.close();
        c.drawPath(path, paint);
    }

    private void drawProduct(Canvas c)
    {
        final int sw2 = 2 * strokeWidth;
        final int sw5 = 5 * strokeWidth;
        final int sw7 = 7 * strokeWidth;
        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);

        path.reset();
        path.moveTo(rect.left, rect.top);
        path.lineTo(rect.left + sw2, rect.top + sw2);
        path.lineTo(rect.left + sw2, rect.bottom - sw2);
        path.lineTo(rect.left, rect.bottom);
        path.lineTo(rect.left + sw7, rect.bottom);
        path.lineTo(rect.left + sw5, rect.bottom - sw2);
        path.lineTo(rect.left + sw5, rect.top + sw2);
        path.lineTo(rect.right - sw5, rect.top + sw2);
        path.lineTo(rect.right - sw5, rect.bottom - sw2);
        path.lineTo(rect.right - sw7, rect.bottom);
        path.lineTo(rect.right, rect.bottom);
        path.lineTo(rect.right - sw2, rect.bottom - sw2);
        path.lineTo(rect.right - sw2, rect.top + sw2);
        path.lineTo(rect.right, rect.top);
        path.close();
        c.drawPath(path, paint);
    }

    private void drawIntegral(Canvas c)
    {
        final float sw = strokeWidth;
        final float rad = rect.width() / 10f;

        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.FILL);
        path.reset();

        // top line
        final float r2 = rect.centerX() + 5f * rad;
        path.moveTo(rect.centerX() - 1.5f * sw, rect.centerY());
        path.lineTo(rect.centerX(), rect.top + 4 * rad);
        oval.set(rect.centerX() - 0.05f * sw, rect.top, r2 + 1.2f * sw, rect.top + 10 * rad);
        path.arcTo(oval, 200, 115);
        oval.set(r2 - 2 * rad, rect.top + 0.9f * rad, r2, rect.top + 3 * rad);
        path.arcTo(oval, 0, 359);
        oval.set(rect.centerX() + 3 * sw, rect.top + sw, r2 - 0.0f * sw, rect.top + 5 * rad);
        path.arcTo(oval, -30, -140);
        path.lineTo(rect.centerX() + 1.5f * sw, rect.centerY());

        // bottom line
        final float l2 = rect.centerX() - 5f * rad;
        path.lineTo(rect.centerX(), rect.bottom - 4 * rad);
        oval.set(l2 - 1.2f * sw, rect.bottom - 10 * rad, rect.centerX() + 0.05f * sw, rect.bottom);
        path.arcTo(oval, 20, 115);
        oval.set(l2, rect.bottom - 3 * rad, l2 + 2 * rad, rect.bottom - 0.9f * rad);
        path.arcTo(oval, 180, 359);
        oval.set(l2 + 0.0f * sw, rect.bottom - 5 * rad, rect.centerX() - 3 * sw, rect.bottom - sw);
        path.arcTo(oval, 150, -140);

        path.close();
        c.drawPath(path, paint);
    }

    /*********************************************************
     * Implementation for methods for OnClickListener interface
     *********************************************************/

    @Override
    public void onClick(View v)
    {
        if (formulaChangeIf != null)
        {
            formulaChangeIf.onFocus(v, true);
        }
    }

    /*********************************************************
     * Context menu handling
     *********************************************************/

    /**
     * Procedure returns the parent action mode or null if there are no related mode
     */
    public android.support.v7.view.ActionMode getActionMode()
    {
        return menuHandler.getActionMode();
    }

    @Override
    public boolean onLongClick(View view)
    {
        if (formulaChangeIf == null)
        {
            return false;
        }
        if (menuHandler.getActionMode() != null)
        {
            return true;
        }
        menuHandler.startActionMode(activity, this, formulaChangeIf);
        return true;
    }

}
