/*
 * microMathematics - Extended Visual Calculator
 * Copyright (C) 2014-2022 by Mikhail Kulesh
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

import com.mkulesh.micromath.utils.ViewUtils;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TestCase
{
    public final static String BEGIN_FIELD = "begin";
    public final static String RESULT_FIELD = "result";
    public final static String DESIRED_FIELD = "desired";
    public final static String REGISTER_PLOT = "registerPlotForTestCase";
    public final static String RESULT_PLOT_BOUNDS = "resultPlotBounds";
    public final static String DESIRED_PLOT_BOUNDS = "desiredPlotBounds";
    public final static String END_FIELD = "end";

    public final static String[] PARAMETERS = { "TC", "Duration (ms)", "Result", "Desired", "Status" };

    private final String beginField;
    private String endField;
    private final ArrayList<String> resultField = new ArrayList<>();
    private final ArrayList<String> desiredField = new ArrayList<>();
    private final List<TestPlotData> plotData = new ArrayList<>();
    private final long startTime;
    private long endTime = 0;

    public TestCase(String beginNumber)
    {
        this.beginField = beginNumber;
        startTime = Calendar.getInstance().getTimeInMillis();
    }

    @NonNull
    @Override
    public String toString()
    {
        return "TC: " + beginField;
    }

    public void finish(String endNumber)
    {
        this.endField = endNumber;
        endTime = Calendar.getInstance().getTimeInMillis();
        ViewUtils.Debug(this, getDescription());
    }

    public void setDataField(@NonNull final String name, @Nullable final String value, @Nullable final String plotId)
    {
        if (RESULT_FIELD.equals(name))
        {
            resultField.add(value);
            ViewUtils.Debug(this, this + ", Set " + RESULT_FIELD + ": " + value);
        }
        else if (TestCase.DESIRED_FIELD.equals(name))
        {
            desiredField.add(value);
            ViewUtils.Debug(this, this + ", Set " + DESIRED_FIELD + ": " + value);
        }
        else if (REGISTER_PLOT.equals(name) && plotId != null)
        {
            synchronized (plotData)
            {
                plotData.add(new TestPlotData(plotId));
                ViewUtils.Debug(this, this + ", Set plot ID: " + plotId);
            }
        }
        else if (DESIRED_PLOT_BOUNDS.equals(name))
        {
            synchronized (plotData)
            {
                if (!plotData.isEmpty())
                {
                    final TestPlotData p = plotData.get(plotData.size() - 1);
                    p.desired = value;
                    ViewUtils.Debug(this, this + ", Set desired value for plot " + p.id + ": " + value);
                }
            }
        }
        else if (RESULT_PLOT_BOUNDS.equals(name) && plotId != null)
        {
            synchronized (plotData)
            {
                for (TestPlotData p : plotData)
                {
                    if (p.id.equals(plotId))
                    {
                        p.result = value;
                        ViewUtils.Debug(this, this + ", Set result value for plot " + p.id + ": " + value);
                        ViewUtils.Debug(this, getDescription());
                    }
                }
            }
        }
    }

    public boolean arePlotsComplete()
    {
        synchronized (plotData)
        {
            return TestPlotData.plotsComplete(plotData);
        }
    }

    private boolean arePlotsPassed()
    {
        synchronized (plotData)
        {
            return TestPlotData.plotsPassed(plotData);
        }
    }

    public boolean isPassed()
    {
        if (getError() != null)
        {
            return false;
        }
        return compareValues(resultField.get(0), desiredField.get(0)) && arePlotsPassed();
    }

    private boolean compareValues(String s1, String s2)
    {
        if (s1 == null || s2 == null)
        {
            return false;
        }
        return s1.equals(s2);
    }

    private String getError()
    {
        if (beginField == null)
        {
            return "FAILED: \"" + BEGIN_FIELD + "\" field is not found";
        }
        if (resultField.isEmpty())
        {
            return "FAILED: \"" + RESULT_FIELD + "\" field is not set";
        }
        if (resultField.size() > 1)
        {
            return "FAILED: \"" + RESULT_FIELD + "\" is set multiple time";
        }
        if (desiredField.isEmpty())
        {
            return "FAILED: \"" + DESIRED_FIELD + "\" field is not found";
        }
        if (desiredField.size() > 1)
        {
            return "FAILED: \"" + DESIRED_FIELD + "\" is set multiple time";
        }
        if (endField == null)
        {
            return "FAILED: \"" + END_FIELD + "\" field is not found";
        }
        if (!compareValues(beginField, endField))
        {
            return "FAILED: begin and end fields have different values";
        }
        if (!arePlotsComplete())
        {
            return "FAILED: plot data not complete";
        }
        return null;
    }

    private String getDescription()
    {
        final String error = getError();
        if (error != null)
        {
            return error;
        }
        return PARAMETERS[0] + ": " + beginField + ", " + PARAMETERS[1] + ": " + (endTime - startTime) + ", "
                + PARAMETERS[2] + ": " + resultField + ", " + PARAMETERS[3] + ": " + desiredField + ", "
                + PARAMETERS[4] + ": " + (arePlotsComplete() ? (isPassed() ? "PASSED" : "FAILED") : "PLOT_INCOMPLETE");
    }

    public void publishHtmlReport(StringWriter writer)
    {
        final String error = getError();
        if (error != null)
        {
            String line = "    <tr><td>" + beginField + "</td><td colspan=\""
                    + (PARAMETERS.length - 1)
                    + "\"><font color=\"red\">" + error
                    + "</font></td></tr>";
            writer.append(line);
            return;
        }
        String line = "    <tr>";
        line += "<td>" + beginField + "</td>";
        line += "<td>" + (endTime - startTime) + "</td>";
        line += "<td>" + resultField + "</td>";
        line += "<td>" + desiredField + "</td>";
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
        writer.append(line);
        for (int i = 0; i < plotData.size(); i++)
        {
            if (plotData.get(i).isTested())
            {
                writer.append(plotData.get(i).publishHtmlReport(beginField, i + 1));
            }
        }
    }
}
