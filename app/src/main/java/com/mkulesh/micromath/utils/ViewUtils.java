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
package com.mkulesh.micromath.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.support.annotation.AttrRes;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewParent;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mkulesh.micromath.formula.FormulaBase;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.widgets.TwoDScrollView;

import org.apache.commons.math3.util.FastMath;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public final class ViewUtils
{
    /**
     * Class members.
     */
    public static final int INVALID_INDEX = -1;

    public static void Debug(Object o, String text)
    {
        //Log.d("microMath", o.getClass().getSimpleName() + ": " + text);
    }

    /**
     * Procedure returns the index of given view within given layout
     */
    public static int getViewIndex(LinearLayout layout, View v)
    {
        int textIndex = -1;
        for (int i = 0; i < layout.getChildCount(); i++)
        {
            if (layout.getChildAt(i) == v)
            {
                textIndex = i;
                break;
            }
        }
        return textIndex;
    }

    /**
     * Procedure returns the layout depth of this term related to mainView
     */
    public static int getLayoutDepth(LinearLayout l)
    {
        int retValue = 0;
        if (l == null)
        {
            return retValue;
        }
        ViewParent p = l.getParent();
        while (p != null)
        {
            if (p instanceof TwoDScrollView)
            {
                if (((TwoDScrollView) p).getId() == R.id.main_scroll_view)
                {
                    break;
                }
            }
            if (p instanceof FormulaBase && p.getParent() == null)
            {
                retValue += 2;
                break;
            }
            retValue++;
            p = p.getParent();
        }
        return retValue;
    }

    /**
     * Procedure returns an array of formatted values
     */
    public static String[] catValues(double[] values, int significantDigits)
    {
        final int strMaxLength = significantDigits;
        final int decMaxLength = Math.max(0, significantDigits - 2);
        final int expMaxLength = Math.max(0, significantDigits - 2);
        String[] strValues = new String[values.length];
        Set<String> trial = new HashSet<String>();

        // First run: try to find suitable simple decimal format
        // Second run: we shall use exponential format to ensure given maximum length
        for (int run = 0; run < 2; run++)
        {
            final int maxLength = (run == 0) ? decMaxLength : expMaxLength;
            boolean resultFound = false;
            for (int pos = 0; pos <= maxLength; pos++)
            {
                String format = (pos < 1) ? "0" : "0.";
                for (int k = 0; k < pos; k++)
                {
                    format += "0";
                }
                if (run == 1)
                {
                    format += "E0";
                }
                final DecimalFormat df = CompatUtils.getDecimalFormat(format);
                trial.clear();
                boolean hasDuplicate = false;
                int trialLength = 0;
                for (int i = 0; i < values.length; i++)
                {
                    String fValue = (values[i] != 0.0) ? df.format(values[i]) : "0";
                    if (fValue != null && (format.equals(fValue) || ("-" + format).equals(fValue)))
                    {
                        fValue = "0";
                    }
                    strValues[i] = fValue;
                    if (!trial.add(fValue))
                    {
                        hasDuplicate = true;
                    }
                    trialLength = Math.max(trialLength, fValue.length());
                }
                if (resultFound)
                {
                    return strValues;
                }
                if (!hasDuplicate && trialLength <= strMaxLength)
                {
                    resultFound = true;
                }
            }
        }
        return strValues;
    }

    /**
     * Procedure rounds the given value to the given number of significant digits see
     * http://stackoverflow.com/questions/202302
     *
     * Note: The maximum double value in Java is on the order of 10^308, while the minimum value is on the order of
     * 10^-324. Therefore, you can run into trouble when applying the function roundToSignificantFigures to something
     * that's within a few powers of ten of Double.MIN_VALUE.
     *
     * Consequently, the variable magnitude may become Infinity, and it's all garbage from then on out. Fortunately,
     * this is not an insurmountable problem: it is only the factor magnitude that's overflowing. What really matters is
     * the product num * magnitude, and that does not overflow. One way of resolving this is by breaking up the
     * multiplication by the factor magintude into two steps.
     */
    public static double roundToNumberOfSignificantDigits(double num, int n)
    {
        final double maxPowerOfTen = FastMath.floor(FastMath.log10(Double.MAX_VALUE));

        if (num == 0)
        {
            return 0;
        }

        try
        {
            return new BigDecimal(num).round(new MathContext(n, RoundingMode.HALF_EVEN)).doubleValue();
        }
        catch (ArithmeticException ex)
        {
            // nothing to do
        }

        final double d = FastMath.ceil(FastMath.log10(num < 0 ? -num : num));
        final int power = n - (int) d;

        double firstMagnitudeFactor = 1.0;
        double secondMagnitudeFactor = 1.0;
        if (power > maxPowerOfTen)
        {
            firstMagnitudeFactor = FastMath.pow(10.0, maxPowerOfTen);
            secondMagnitudeFactor = FastMath.pow(10.0, (double) power - maxPowerOfTen);
        }
        else
        {
            firstMagnitudeFactor = FastMath.pow(10.0, (double) power);
        }

        double toBeRounded = num * firstMagnitudeFactor;
        toBeRounded *= secondMagnitudeFactor;

        final long shifted = FastMath.round(toBeRounded);
        double rounded = ((double) shifted) / firstMagnitudeFactor;
        rounded /= secondMagnitudeFactor;
        return rounded;
    }

    /**
     * Procedure converts DP to pixels
     */
    public static int dpToPx(DisplayMetrics displayMetrics, int dp)
    {
        return (int) ((dp * displayMetrics.density) + 0.5);
    }

    /**
     * Procedure converts pixels tp DP
     */
    public static int pxToDp(DisplayMetrics displayMetrics, int px)
    {
        return (int) ((px / displayMetrics.density) + 0.5);
    }

    /**
     * Procedure collects all components from the given layout recursively
     */
    public static void collectElemets(LinearLayout layout, ArrayList<View> out)
    {
        for (int k = 0; k < layout.getChildCount(); k++)
        {
            View v = layout.getChildAt(k);
            out.add(v);
            if (v instanceof LinearLayout)
            {
                collectElemets((LinearLayout) v, out);
            }
        }
    }

    /**
     * Procedure hows toast that contains description of the given button
     */
    @SuppressLint("RtlHardcoded")
    public static boolean showButtonDescription(Context context, View button)
    {
        CharSequence contentDesc = button.getContentDescription();
        if (contentDesc != null && contentDesc.length() > 0)
        {
            int[] pos = new int[2];
            button.getLocationOnScreen(pos);

            Toast t = Toast.makeText(context, contentDesc, Toast.LENGTH_SHORT);
            t.setGravity(Gravity.TOP | Gravity.LEFT, 0, 0);
            t.getView().measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            final int x = pos[0] + button.getMeasuredWidth() / 2 - (t.getView().getMeasuredWidth() / 2);
            final int y = pos[1] - button.getMeasuredHeight() / 2 - t.getView().getMeasuredHeight()
                    - context.getResources().getDimensionPixelSize(R.dimen.activity_vertical_margin);
            t.setGravity(Gravity.TOP | Gravity.LEFT, x, y);
            t.show();
            return true;
        }
        return false;
    }

    /**
     * Procedure updates given view and a given parent layout
     */
    public static void invalidateLayout(View v, final LinearLayout l)
    {
        v.invalidate();
        l.post(new Runnable()
        {
            @Override
            public void run()
            {
                l.requestLayout();
            }
        });
    }

    /**
     * Procedure updates menu item color depends its enabled state
     */
    public static void updateMenuIconColor(Context context, MenuItem m)
    {
        CompatUtils.setDrawableColorAttr(context, m.getIcon(),
                        m.isEnabled() ? R.attr.colorMicroMathIcon : R.attr.colorPrimaryDark);
    }

    /**
     * Procedure sets ImageButton color given by attribute ID
     */
    public static void setImageButtonColorAttr(Context context, ImageButton b, @AttrRes int resId)
    {
        final int c = CompatUtils.getThemeColorAttr(context, resId);
        b.clearColorFilter();
        b.setColorFilter(c, PorterDuff.Mode.SRC_ATOP);
    }

    /**
     * Procedure checks whether the hard keyboard is available
     */
    public static boolean isHardwareKeyboardAvailable(Context context)
    {
        return context.getResources().getConfiguration().keyboard != Configuration.KEYBOARD_NOKEYS;
    }

    public static int getStatusBarHeight(final Activity activity)
    {
        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0)
        {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
