/*
 * microMathematics - Extended Visual Calculator
 * Copyright (C) 2014-2026 by Mikhail Kulesh
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details. You should have received a copy of the GNU General
 * Public License along with this program.
 */

package com.mkulesh.micromath.ta;

import java.util.List;

import androidx.annotation.NonNull;

class TestPlotData
{
    final String id;
    String desired, result;

    TestPlotData(@NonNull String id)
    {
        this.id = id;
    }

    public boolean isTested()
    {
        return desired != null;
    }

    public boolean isPassed()
    {
        if (!isTested())
        {
            return true;
        }
        final String[] resTerms = result.replace("[", "").
                replace("]", "").split(",");
        final String[] desTerms = desired.replace("[", "").
                replace("]", "").split(",");
        if (resTerms.length != desTerms.length)
        {
            return false;
        }
        for (int i = 0; i < resTerms.length; i++)
        {
            final String t1 = removeTrailingZeros(resTerms[i]);
            final String t2 = removeTrailingZeros(desTerms[i]);
            if (!t1.equals(t2))
            {
                return false;
            }
        }
        return true;
    }

    private static String removeTrailingZeros(String numberStr)
    {
        if (numberStr == null || !numberStr.contains("."))
        {
            return numberStr;
        }
        // Remove trailing zeros, but only if they are in the fractional part.
        return numberStr.replaceAll("(\\.\\d*?)0+$", "$1");
    }

    public String publishHtmlReport(@NonNull String tc, int i)
    {
        String line = "    <tr>";
        line += "<td>" + tc + "</td>";
        line += "<td> Plot " + i + "</td>";
        line += "<td>" + result + "</td>";
        line += "<td>" + desired + "</td>";
        String status = "";
        if (isPassed())
        {
            status += "<font color=\"green\">PASSED</font>";
        }
        else
        {
            status += "<font color=\"red\">FAILED</font>";
        }
        line += "<td>" + status + "</td>";
        line += "</tr>\n";
        return line;
    }

    public static boolean plotsComplete(@NonNull final List<TestPlotData> plotData)
    {
        for (TestPlotData p : plotData)
        {
            if (p.isTested() && p.result == null)
            {
                return false;
            }
        }
        return true;
    }

    public static boolean plotsPassed(@NonNull final List<TestPlotData> plotData)
    {
        for (TestPlotData p : plotData)
        {
            if (!p.isPassed())
            {
                return false;
            }
        }
        return true;
    }
}
