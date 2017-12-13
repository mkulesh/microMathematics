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
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.CompatUtils;
import com.mkulesh.micromath.utils.ViewUtils;

public class CustomLayout extends LinearLayout
{
    public enum SymbolType
    {
        NONE,
        SQRT
    }

    public enum SpecialAllignment
    {
        NONE,
        TOP_OF_PREVIOUS,
        TOP_OF_NEXT,
        BELOW_THE_NEXT,
        BELOW_THE_PREVIOUS
    }

    private SpecialAllignment specialAllignment = SpecialAllignment.NONE;

    private SymbolType symbolType = SymbolType.NONE;
    private final Paint paint = new Paint();
    private final Rect mTmpContainerRect = new Rect();
    private final Rect mTmpChildRect = new Rect();
    private int symbolViewIndex = -1;
    private final Path path = new Path();
    private boolean verticalTermPadding = false;
    private int strokeWidth = 0, symbolSize = 0;
    private boolean contentValid = true;
    private boolean customFeaturesDisabled = false;
    private int textColor = Color.BLACK;

    /*********************************************************
     * Creating
     *********************************************************/

    public CustomLayout(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        prepare(attrs);
    }

    public CustomLayout(Context context)
    {
        super(context);
        prepare(null);
    }

    private void prepare(AttributeSet attrs)
    {
        setBaselineAligned(true);
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
            symbolViewIndex = a.getInteger(R.styleable.CustomViewExtension_symbolViewIndex, -1);
            final int specialAllignmentInt = a.getInteger(R.styleable.CustomViewExtension_specialAlignment, -1);
            if (specialAllignmentInt >= 0 && specialAllignmentInt < SpecialAllignment.values().length)
            {
                specialAllignment = SpecialAllignment.values()[specialAllignmentInt];
            }
            verticalTermPadding = a.getBoolean(R.styleable.CustomViewExtension_verticalTermPadding, false);
            a.recycle();
        }
        textColor = CompatUtils.getThemeColorAttr(getContext(), R.attr.colorFormulaNormal);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(textColor);
        paint.setAntiAlias(true);
        setWillNotDraw(false);
        setOrientation(LinearLayout.HORIZONTAL);
        setSaveEnabled(false);
    }

    private SpecialAllignment getSpecialAllignment(View child)
    {
        if (child instanceof CustomLayout)
        {
            return ((CustomLayout) child).specialAllignment;
        }
        return SpecialAllignment.NONE;
    }

    public void updateTextSize(ScaledDimensions dimen, int termDepth)
    {
        if (customFeaturesDisabled)
        {
            return;
        }
        strokeWidth = dimen.get(ScaledDimensions.Type.STROKE_WIDTH);
        symbolSize = dimen.get(ScaledDimensions.Type.SMALL_SYMBOL_SIZE);
        if (verticalTermPadding)
        {
            final int p = dimen.get(ScaledDimensions.Type.VERT_TERM_PADDING);
            setPadding(getPaddingLeft(), p, getPaddingRight(), p);
        }
        if (symbolType == SymbolType.SQRT)
        {
            final int p = dimen.get(ScaledDimensions.Type.HOR_SYMBOL_PADDING);
            setPadding(getPaddingLeft(), getPaddingTop(), p, getPaddingBottom());
        }
    }

    public void setContentValid(boolean contentValid)
    {
        this.contentValid = contentValid;
        invalidate();
    }

    public void setCustomFeaturesDisabled(boolean customFeaturesDisabled)
    {
        this.customFeaturesDisabled = customFeaturesDisabled;
    }

    /*********************************************************
     * Painting
     *********************************************************/

    private int topOfPreviousBaseLine(int currBaseLine, int currHeight, int prevBaseLine)
    {
        return currBaseLine + Math.max(0, (currHeight - currBaseLine) - prevBaseLine);
    }

    private int belowThePreviousBaseLine(int currHeight, int currBaseLine, int prevHeight, int prevBaseLine)
    {
        final int bl = currBaseLine + 2 * (currHeight - currBaseLine) / 3;
        return bl - Math.max(0, bl - (prevHeight - prevBaseLine));
    }

    @Override
    public int getBaseline()
    {
        if (customFeaturesDisabled)
        {
            return super.getBaseline();
        }

        int baseLine = -1, prevBaseLine = 0;
        for (int i = 0; i < getChildCount(); i++)
        {
            final View child = getChildAt(i);
            final SpecialAllignment spAl = getSpecialAllignment(child);
            if (child.getVisibility() == GONE || spAl == SpecialAllignment.BELOW_THE_NEXT
                    || spAl == SpecialAllignment.BELOW_THE_PREVIOUS)
            {
                continue;
            }
            final int currBaseLine = child.getBaseline();
            if (spAl == SpecialAllignment.TOP_OF_PREVIOUS)
            {
                final int tmpBaseLine = prevBaseLine
                        + topOfPreviousBaseLine(currBaseLine, child.getMeasuredHeight(), prevBaseLine);
                baseLine = Math.max(baseLine, tmpBaseLine + getPaddingTop());
            }
            else if (spAl == SpecialAllignment.TOP_OF_NEXT && (i + 1) < getChildCount())
            {
                final View nextChild = getChildAt(i + 1);
                if (nextChild.getVisibility() != GONE)
                {
                    final int nextBaseLine = nextChild.getBaseline();
                    final int tmpBaseLine = nextBaseLine
                            + topOfPreviousBaseLine(currBaseLine, child.getMeasuredHeight(), nextBaseLine);
                    baseLine = Math.max(baseLine, tmpBaseLine + getPaddingTop());
                }
            }
            else
            {
                baseLine = Math.max(baseLine, currBaseLine + getPaddingTop());
            }
            prevBaseLine = currBaseLine;
        }
        return baseLine;
    }

    /**
     * Ask all children to measure themselves and compute the measurement of this layout based on the children.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        if (customFeaturesDisabled)
        {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }

        final int count = getChildCount();

        // Measurement will ultimately be computing these values.
        int m1 = 0, m2 = 0, maxHeight = 0, maxWidth = 0, childState = 0, prevBaseLine = 0, prevHeight = 0;

        // The first pass: measure all children
        for (int i = 0; i < count; i++)
        {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE)
            {
                continue;
            }
            // Measure the child.
            measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
            childState = childState | ViewCompat.getMeasuredState(child);
        }

        // The second pass: compute new size
        boolean isAllignBelowTheNext = false;
        for (int i = 0; i < count; i++)
        {
            final View child = getChildAt(i);
            final SpecialAllignment spAl = getSpecialAllignment(child);
            if (child.getVisibility() == GONE)
            {
                continue;
            }
            if (spAl == SpecialAllignment.BELOW_THE_NEXT)
            {
                isAllignBelowTheNext = true;
                continue;
            }

            // calculate width always
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            maxWidth += (child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);

            // calculate height depending on the layout parameters
            if (lp.height == ViewGroup.LayoutParams.MATCH_PARENT)
            {
                continue;
            }
            final int currBaseLine = child.getBaseline();
            final int currHeight = child.getMeasuredHeight();
            if (spAl == SpecialAllignment.TOP_OF_PREVIOUS)
            {
                final int tmpBaseLine = prevBaseLine + topOfPreviousBaseLine(currBaseLine, currHeight, prevBaseLine);
                m1 = Math.max(m1, tmpBaseLine);
                m2 = Math.max(m2, currHeight - tmpBaseLine);
            }
            else if (spAl == SpecialAllignment.BELOW_THE_PREVIOUS)
            {
                final int tmpBaseLine = belowThePreviousBaseLine(currHeight, currBaseLine, prevHeight, prevBaseLine);
                m1 = Math.max(m1, tmpBaseLine);
                m2 = Math.max(m2, prevHeight - prevBaseLine + currHeight - tmpBaseLine);
            }
            else if (spAl == SpecialAllignment.TOP_OF_NEXT && (i + 1) < getChildCount())
            {
                final View nextChild = getChildAt(i + 1);
                if (nextChild.getVisibility() != GONE
                        && nextChild.getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT)
                {
                    final int nextBaseLine = nextChild.getBaseline();
                    final int tmpBaseLine = nextBaseLine
                            + topOfPreviousBaseLine(currBaseLine, currHeight, nextBaseLine);
                    m1 = Math.max(m1, tmpBaseLine);
                    m2 = Math.max(m2, currHeight - tmpBaseLine);
                }
            }
            else
            {
                m1 = Math.max(m1, currBaseLine);
                m2 = Math.max(m2, currHeight - currBaseLine);
            }
            prevBaseLine = currBaseLine;
            prevHeight = currHeight;
        }

        // Check against our minimum height and width
        maxHeight = Math.max(m1 + m2 + getPaddingTop() + getPaddingBottom(), getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth + getPaddingLeft() + getPaddingRight(), getSuggestedMinimumWidth());

        // Report our final dimensions.
        setMeasuredDimension(ViewCompat.resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                ViewCompat.resolveSizeAndState(maxHeight, heightMeasureSpec, childState << ViewCompat.MEASURED_HEIGHT_STATE_SHIFT));

        // The third pass: resize element if necessary
        if (isAllignBelowTheNext)
        {
            for (int i = 0; (i < count) && (i + 1 < count); i++)
            {
                final View child = getChildAt(i);
                final View nextChild = getChildAt(i + 1);
                if (getSpecialAllignment(child) == SpecialAllignment.BELOW_THE_NEXT && child.getVisibility() != GONE
                        && nextChild.getVisibility() != GONE
                        && nextChild.getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT)
                {
                    final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                    final int height = maxHeight - nextChild.getMeasuredHeight();
                    if (height > 0)
                    {
                        lp.height = height;
                        lp.width = nextChild.getMeasuredWidth();
                    }
                }
            }
        }
    }

    /**
     * Position all children within this layout.
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        if (customFeaturesDisabled)
        {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }

        final int count = getChildCount();

        // These are the top and bottom edges in which we are performing layout.
        final int parentTop = getPaddingTop();
        final int parentBottom = bottom - top - getPaddingBottom();
        final int baseLine = getBaseline();

        // These are the far left and right edges in which we are performing layout.
        int prevLeftPos = getPaddingLeft(), prevTopPos = baseLine, prevBottomPos = baseLine, prevBaseLine = 0;

        // the first pass
        boolean isAllignToTopOfNext = false, isAllignBelowTheNext = false;
        for (int i = 0; i < count; i++)
        {
            final View child = getChildAt(i);
            final SpecialAllignment spAl = getSpecialAllignment(child);
            if (child.getVisibility() == GONE)
            {
                continue;
            }

            // calculate width always
            final LayoutParams lp = (LayoutParams) child.getLayoutParams();
            final int width = child.getMeasuredWidth();
            mTmpContainerRect.left = prevLeftPos + lp.leftMargin;
            mTmpContainerRect.right = prevLeftPos + width + lp.rightMargin;

            // calculate height depending on the layout parameters
            final int currHeight = child.getMeasuredHeight();
            if (lp.height != LayoutParams.MATCH_PARENT)
            {
                final int currBaseLine = child.getBaseline();
                if (spAl == SpecialAllignment.TOP_OF_PREVIOUS)
                {
                    final int tmpBaseLine = topOfPreviousBaseLine(currBaseLine, currHeight, prevBaseLine);
                    mTmpContainerRect.top = prevTopPos - tmpBaseLine - lp.topMargin;
                    prevBaseLine = currBaseLine;
                }
                else if (spAl == SpecialAllignment.TOP_OF_NEXT)
                {
                    isAllignToTopOfNext = true;
                    mTmpContainerRect.top = baseLine - currBaseLine - lp.topMargin;
                    prevBaseLine = currBaseLine;
                }
                else if (spAl == SpecialAllignment.BELOW_THE_NEXT)
                {
                    isAllignBelowTheNext = true;
                    mTmpContainerRect.top = parentBottom - currHeight;
                }
                else if (spAl == SpecialAllignment.BELOW_THE_PREVIOUS)
                {
                    final int tmpBaseLine = belowThePreviousBaseLine(currHeight, currBaseLine, prevBottomPos
                            - prevTopPos, prevBaseLine);
                    mTmpContainerRect.top = prevBottomPos - tmpBaseLine;
                    prevBaseLine = currBaseLine;
                }
                else
                {
                    mTmpContainerRect.top = baseLine - currBaseLine - lp.topMargin;
                    prevBaseLine = currBaseLine;
                }
                mTmpContainerRect.bottom = mTmpContainerRect.top + currHeight + lp.bottomMargin;
            }
            else
            {
                mTmpContainerRect.top = parentTop + lp.topMargin;
                mTmpContainerRect.bottom = parentBottom - lp.bottomMargin;
            }

            // Use the child's gravity and size to determine its final
            // frame within its container.
            Gravity.apply(lp.gravity, width, currHeight, mTmpContainerRect, mTmpChildRect);

            // Place the child.
            child.layout(mTmpChildRect.left, mTmpChildRect.top, mTmpChildRect.right, mTmpChildRect.bottom);
            if (spAl != SpecialAllignment.BELOW_THE_NEXT)
            {
                prevTopPos = mTmpChildRect.top;
                prevBottomPos = mTmpChildRect.bottom;
                prevLeftPos = mTmpContainerRect.right;
            }
        }

        // the second pass if there is a child aligned to top of next
        if (isAllignToTopOfNext)
        {
            for (int i = 0; (i < count) && (i + 1 < count); i++)
            {
                final View child = getChildAt(i);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final View nextChild = getChildAt(i + 1);
                if (getSpecialAllignment(child) == SpecialAllignment.TOP_OF_NEXT && child.getVisibility() != GONE
                        && lp.height == ViewGroup.LayoutParams.WRAP_CONTENT && nextChild.getVisibility() != GONE
                        && nextChild.getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT)
                {
                    final int tmpBaseLine = topOfPreviousBaseLine(child.getBaseline(), child.getMeasuredHeight(),
                            nextChild.getBaseline());
                    final int width = child.getMeasuredWidth();
                    final int height = child.getMeasuredHeight();
                    mTmpContainerRect.left = child.getLeft();
                    mTmpContainerRect.right = child.getRight();
                    mTmpContainerRect.top = nextChild.getTop() - tmpBaseLine - lp.topMargin;
                    mTmpContainerRect.bottom = mTmpContainerRect.top + height + lp.bottomMargin;
                    Gravity.apply(lp.gravity, width, height, mTmpContainerRect, mTmpChildRect);
                    child.layout(mTmpChildRect.left, mTmpChildRect.top, mTmpChildRect.right, mTmpChildRect.bottom);
                }
            }
        }

        // the third pass if there is a child aligned below the next
        if (isAllignBelowTheNext)
        {
            for (int i = 0; (i < count) && (i + 1 < count); i++)
            {
                final View child = getChildAt(i);
                final View nextChild = getChildAt(i + 1);
                if (getSpecialAllignment(child) == SpecialAllignment.BELOW_THE_NEXT && child.getVisibility() != GONE
                        && nextChild.getVisibility() != GONE
                        && nextChild.getLayoutParams().height == ViewGroup.LayoutParams.WRAP_CONTENT)
                {
                    mTmpChildRect.left = nextChild.getLeft();
                    mTmpChildRect.right = nextChild.getRight();
                    mTmpChildRect.top = nextChild.getBottom();
                    mTmpChildRect.bottom = parentBottom;
                    child.layout(mTmpChildRect.left, mTmpChildRect.top, mTmpChildRect.right, mTmpChildRect.bottom);
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas c)
    {
        if (customFeaturesDisabled)
        {
            super.onDraw(c);
            return;
        }

        if (symbolType != null)
        {
            switch (symbolType)
            {
            case SQRT:
                drawSqrt(c);
                break;
            default:
                break;
            }
        }

        if (!contentValid)
        {
            paint.setStyle(Style.STROKE);
            paint.setStrokeWidth(ViewUtils.dpToPx(getContext().getResources().getDisplayMetrics(), 1));
            paint.setColor(CompatUtils.getThemeColorAttr(getContext(), R.attr.colorFormulaInvalid));
            c.drawRect(1, 1, this.getRight() - this.getLeft() - 1, this.getBottom() - this.getTop() - 1, paint);
        }

        // mTmpContainerRect.set(0, 0, this.getRight() - this.getLeft(), this.getBottom() - this.getTop());
        // paint.setStrokeWidth(1);
        // paint.setColor(Color.BLACK);
        // c.drawRect(mTmpContainerRect, paint);
        //
        // mTmpContainerRect.set(getPaddingLeft(), getPaddingTop(), this.getRight() - this.getLeft() - getPaddingRight(),
        //         this.getBottom() - this.getTop() - getPaddingBottom());
        // paint.setStrokeWidth(1);
        // paint.setColor(Color.MAGENTA);
        // c.drawRect(mTmpContainerRect, paint);
    }

    private void drawSqrt(Canvas c)
    {
        int top = strokeWidth;
        int right = symbolSize;
        if (symbolViewIndex >= 0 && symbolViewIndex < getChildCount())
        {
            final View child = getChildAt(symbolViewIndex);
            if (child != null && child.getVisibility() != GONE)
            {
                top = Math.max(0, strokeWidth + child.getTop() - getPaddingTop());
                right = child.getLeft();
            }
        }
        final int bottom = this.getBottom() - this.getTop();
        final int left = Math.max(0, right - symbolSize);
        final int yc = getBaseline();

        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(textColor);
        path.reset();
        path.moveTo(left, yc + symbolSize / 4);
        path.lineTo(left + symbolSize / 3, yc);
        path.lineTo(left + 2 * symbolSize / 3, bottom);
        path.lineTo(right, top);
        path.lineTo(this.getRight() - this.getLeft() - getPaddingRight(), top);
        path.lineTo(this.getRight() - this.getLeft() - getPaddingRight(), top + getPaddingTop());
        c.drawPath(path, paint);
    }
}
