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
import java.util.ArrayList;

import com.mkulesh.micromath.utils.ViewUtils;

public class TestScript
{
    public enum NumberType
    {
        TOTAL,
        PASSED,
        FAILED
    }

    public enum State
    {
        LOAD,
        CALCULATE,
        LOAD_FINISHED,
        CALCULATE_FINISHED
    }

    private class SynchronizedState
    {
        private State mystate = null;

        public SynchronizedState()
        {
            super();
        }

        public State get()
        {
            return mystate;
        }

        public void set(State mystate)
        {
            synchronized (this)
            {
                this.mystate = mystate;
                this.notifyAll();
            }
        }
    }

    private final String scriptName;
    private String scriptContent = null;
    private final SynchronizedState state = new SynchronizedState();
    private final ArrayList<TestCase> testCases = new ArrayList<TestCase>();
    private TestCase testCase = null;

    public TestScript(String scriptName)
    {
        this.scriptName = scriptName;
    }

    public void finish()
    {
        if (testCase != null)
        {
            testCases.add(testCase);
            testCase = null;
        }
        ViewUtils.Debug(this, getDescription());
    }

    public void setScriptContent(String content)
    {
        this.scriptContent = content;
    }

    public void setResult(String name, String value)
    {
        if (TestCase.BEGIN_FIELD.equals(name))
        {
            testCase = new TestCase(value);
        }
        else if (TestCase.RESULT_FIELD.equals(name))
        {
            if (testCase == null)
            {
                testCase = new TestCase(null);
            }
            testCase.setResultField(value);
        }
        else if (TestCase.DESIRED_FIELD.equals(name))
        {
            if (testCase == null)
            {
                testCase = new TestCase(null);
            }
            testCase.setDesiredField(value);
        }
        else if (TestCase.END_FIELD.equals(name))
        {
            if (testCase == null)
            {
                testCase = new TestCase(null);
            }
            testCase.finish(value);
            testCases.add(testCase);
            testCase = null;
        }
    }

    public void setState(State myState)
    {
        state.set(myState);
    }

    public State waitStateChange(State currState)
    {
        try
        {
            synchronized (state)
            {
                while (currState == state.get())
                {
                    state.wait();
                }
                return state.get();
            }
        }
        catch (Exception e)
        {
            return State.CALCULATE_FINISHED;
        }
    }

    public State getState()
    {
        return state.get();
    }

    public int getTestCaseNumber(NumberType numberType)
    {
        int n = 0;
        for (TestCase tc : testCases)
        {
            switch (numberType)
            {
            case TOTAL:
                n++;
                break;
            case PASSED:
                n += tc.isPassed() ? 1 : 0;
                break;
            case FAILED:
                n += !tc.isPassed() ? 1 : 0;
                break;
            }

        }
        return n;
    }

    public String getDescription()
    {
        final int failedNumber = getTestCaseNumber(NumberType.FAILED);
        return "Test script: " + scriptName + ", content: " + scriptContent + ", number of test cases: "
                + getTestCaseNumber(NumberType.TOTAL) + ", passed: " + getTestCaseNumber(NumberType.PASSED)
                + ", failed: " + failedNumber + ", status: " + (failedNumber == 0 ? "PASSED" : "FAILED");
    }

    public void publishHtmlReport(StringWriter writer)
    {
        final int failedNumber = getTestCaseNumber(NumberType.FAILED);
        writer.append("\n\n<h1>" + scriptContent + "</h1>\n");
        writer.append("<p><b>Name</b>: " + scriptName + "</p>\n");
        writer.append("<p><b>Number of test cases</b>: " + getTestCaseNumber(NumberType.TOTAL) + "</p>\n");
        writer.append("<table border = \"1\" cellspacing=\"0\" cellpadding=\"5\">\n");
        String title = "    <tr>";
        for (String t : TestCase.PARAMETERS)
        {
            title += "<td><b>" + t + "</b></td>";
        }
        title += "</tr>\n";
        writer.append(title);
        for (TestCase tc : testCases)
        {
            tc.publishHtmlReport(writer);
        }
        writer.append("</table>\n");
        String status = "<p><b>Status</b>: ";
        if (failedNumber == 0)
        {
            status += "<font color=\"green\">PASSED</font>";
        }
        else
        {
            status += "<font color=\"red\">FAILED</font>";
        }
        status += " (passed: " + getTestCaseNumber(NumberType.PASSED) + ", failed: " + failedNumber + ")</p>\n";
        writer.append(status);
    }
}
