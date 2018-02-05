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
package com.mkulesh.micromath.formula.terms;

import android.content.Context;
import android.content.res.Resources;

import com.mkulesh.micromath.formula.Palette;
import com.mkulesh.micromath.formula.PaletteButton;
import com.mkulesh.micromath.properties.DocumentProperties;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CustomEditText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A static helper class used to create terms
 */
public class TermFactory
{
    private static ArrayList<TermTypeIf> allTerms;

    /*********************************************************
     * Factory methods
     *********************************************************/

    public static void prepare()
    {
        allTerms = new ArrayList<>();

        allTerms.addAll(Arrays.asList(Operators.OperatorType.values()));
        allTerms.addAll(Arrays.asList(Comparators.ComparatorType.values()));
        allTerms.addAll(Arrays.asList(ArrayFunctions.FunctionType.values()));
        allTerms.addAll(Arrays.asList(CommonFunctions.FunctionType.values()));
        allTerms.addAll(Arrays.asList(TrigonometricFunctions.FunctionType.values()));
        allTerms.addAll(Arrays.asList(LogFunctions.FunctionType.values()));
        allTerms.addAll(Arrays.asList(NumberFunctions.FunctionType.values()));
        allTerms.addAll(Arrays.asList(UserFunctions.FunctionType.values()));
        allTerms.addAll(Arrays.asList(Intervals.IntervalType.values()));
        allTerms.addAll(Arrays.asList(SeriesIntegrals.LoopType.values()));

        ViewUtils.Debug(allTerms, "There are " + allTerms.size() + " terms");
    }

    /*********************************************************
     * Helper methods
     *********************************************************/

    public static TermTypeIf findTerm(Context context, CustomEditText text, String s, boolean ensureManualTrigger)
    {
        final Resources res = context.getResources();

        for (TermTypeIf f : allTerms)
        {
            if (text != null && !f.isEnabled(text))
            {
                continue;
            }

            if (ensureManualTrigger)
            {
                if (f.getShortCutId() != Palette.NO_BUTTON && s.contains(res.getString(f.getShortCutId())))
                {
                    // trigger found, nothing to do
                }
                else if (f.getBracketId() != Palette.NO_BUTTON && s.contains(res.getString(f.getBracketId())))
                {
                    // bracket found, nothing to do
                }
                else
                {
                    // ignore term since no bracket or trigger exists
                    continue;
                }
            }

            if (s.equals(f.getLowerCaseName()))
            {
                return f;
            }

            final UserFunctionIf uf = (f instanceof UserFunctionIf) ? (UserFunctionIf) f : null;
            if (uf != null && uf.isLink() && s.contains(uf.getLinkObject()))
            {
                return f;
            }

            if (f.getBracketId() != Palette.NO_BUTTON)
            {
                final String operator = res.getString(f.getBracketId());
                if (s.contains(operator))
                {
                    final String fName = s.substring(0, s.indexOf(operator)).trim();
                    if (fName.equals(f.getLowerCaseName()))
                    {
                        return f;
                    }
                }
            }

            // Compatibility mode: search the function name in the array of obsolete functions
            if (DocumentProperties.getDocumentVersion() != DocumentProperties.LATEST_DOCUMENT_VERSION &&
                    f instanceof ObsoleteFunctionIf)
            {
                final ObsoleteFunctionIf of = (ObsoleteFunctionIf) f;
                if (DocumentProperties.getDocumentVersion() <= of.getObsoleteVersion() &&
                        of.getObsoleteCode() != null &&
                        s.equals(of.getObsoleteCode()))
                {
                    return f;
                }
            }

            if (f.getShortCutId() != Palette.NO_BUTTON)
            {
                final String operator = res.getString(f.getShortCutId());
                if (s.contains(operator))
                {
                    final String fName = s.substring(0, s.indexOf(operator)).trim();
                    if (f == UserFunctions.FunctionType.IDENTITY && fName.length() > 0)
                    {
                        continue;
                    }
                    if (f == UserFunctions.FunctionType.FUNCTION_INDEX && fName.length() == 0)
                    {
                        continue;
                    }
                    return f;
                }
            }
        }

        return null;
    }

    public static void addToPalette(Context context, List<PaletteButton> paletteLayout,
                                    boolean ensureImageId, TermTypeIf.GroupType gType)
    {
        for (final TermTypeIf b : allTerms)
        {
            if (b.getGroupType() != gType)
            {
                continue;
            }
            if (ensureImageId && b.getImageId() == Palette.NO_BUTTON)
            {
                continue;
            }
            PaletteButton p = new PaletteButton(context, b);
            paletteLayout.add(p);
        }
    }

    public static List<TermTypeIf.GroupType> collectPaletteGroups()
    {
        final List<TermTypeIf.GroupType> gTypes =
                Arrays.asList(TermTypeIf.GroupType.values());
        Collections.sort(gTypes, new Comparator<TermTypeIf.GroupType>()
        {
            @Override
            public int compare(TermTypeIf.GroupType lhs, TermTypeIf.GroupType rhs)
            {
                return lhs.getPaletteOrder() > rhs.getPaletteOrder() ? 1 :
                        (lhs.getPaletteOrder() < rhs.getPaletteOrder()) ? -1 : 0;
            }
        });
        return gTypes;
    }
}
