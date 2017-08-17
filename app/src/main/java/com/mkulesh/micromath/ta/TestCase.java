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
package com.mkulesh.micromath.ta;

import java.io.StringWriter;
import java.util.Calendar;

import com.mkulesh.micromath.utils.ViewUtils;

public class TestCase
{
    public final static String BEGIN_FIELD = "begin";
    public final static String RESULT_FIELD = "result";
    public final static String DESIRED_FIELD = "desired";
    public final static String END_FIELD = "end";

    public final static String[] PARAMETERS = { "TC", "Duration (ms)", "Result", "Desired", "Status" };

    private String beginField = null, resultField = null, desiredField = null, endField = null;
    private long startTime = 0, endTime = 0;

    public TestCase(String beginNumber)
    {
        this.beginField = beginNumber;
        startTime = Calendar.getInstance().getTimeInMillis();
    }

    public void finish(String endNumber)
    {
        this.endField = endNumber;
        endTime = Calendar.getInstance().getTimeInMillis();
        ViewUtils.Debug(this, getDescription());
    }

    public void setResultField(String resultField)
    {
        this.resultField = resultField;
    }

    public void setDesiredField(String desiredField)
    {
        this.desiredField = desiredField;
    }

    public boolean isPassed()
    {
        if (getError() != null)
        {
            return false;
        }
        return compareValues(resultField, desiredField);
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
            return "TC ERROR: \"" + BEGIN_FIELD + "\" field is not found";
        }
        if (resultField == null)
        {
            return "TC ERROR: \"" + RESULT_FIELD + "\" field is not found";
        }
        if (desiredField == null)
        {
            return "TC ERROR: \"" + DESIRED_FIELD + "\" field is not found";
        }
        if (endField == null)
        {
            return "TC ERROR: \"" + END_FIELD + "\" field is not found";
        }
        if (!compareValues(beginField, endField))
        {
            return "TC ERROR: begin and end fields have different values";
        }
        return null;
    }

    public String getDescription()
    {
        final String error = getError();
        if (error != null)
        {
            return error;
        }
        return PARAMETERS[0] + ": " + beginField + ", " + PARAMETERS[1] + ": " + (endTime - startTime) + ", "
                + PARAMETERS[2] + ": " + resultField + ", " + PARAMETERS[3] + ": " + desiredField + ", "
                + PARAMETERS[4] + ": " + (isPassed() ? "PASSED" : "FAILED");
    }

    public void publishHtmlReport(StringWriter writer)
    {
        final String error = getError();
        if (error != null)
        {
            String line = "    <tr><td colspan=\"" + PARAMETERS.length + "\"><font color=\"red\">" + error
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
    }
}
