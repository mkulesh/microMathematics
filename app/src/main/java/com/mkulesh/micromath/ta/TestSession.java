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

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Toast;

import com.mkulesh.micromath.export.Exporter;
import com.mkulesh.micromath.fman.AdapterFileSystem;
import com.mkulesh.micromath.fman.FileType;
import com.mkulesh.micromath.fman.FileUtils;
import com.mkulesh.micromath.formula.CalculaterTask;
import com.mkulesh.micromath.formula.FormulaList;
import com.mkulesh.micromath.formula.TextFragment;
import com.mkulesh.micromath.formula.XmlLoaderTask;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.ta.TestScript.NumberType;
import com.mkulesh.micromath.utils.SynchronizedBoolean;
import com.mkulesh.micromath.utils.ViewUtils;

public class TestSession extends AsyncTask<Void, Integer, Void>
{
    public enum Mode
    {
        TEST_SCRIPS,
        EXPORT_DOC,
        TAKE_SCREENSHOTS
    }

    public final static String REPORT_HTML_FILE = "autotest.html";
    public final static String TEST_CONFIGURATION = "autotest.cfg";
    public final static String EXPORT_DOC_DIR = "doc";
    public final static String TAKE_SCREENSHOTS_DIR = "screenshots";

    private final static int STEP_READ = 0;
    private final static int STEP_CALC = 1;
    private final static int STEP_EXPORT = 2;

    private final FormulaList formulas;
    private final Activity context;
    private final CharSequence[] scripts;
    private final SynchronizedBoolean isPublishRuns = new SynchronizedBoolean();
    private final ArrayList<TestScript> testScripts = new ArrayList<TestScript>();
    private final Mode mode;
    private TestScript testScript = null;

    public TestSession(FormulaList formulas, Mode mode)
    {
        this.formulas = formulas;
        context = formulas.getActivity();
        this.mode = mode;
        switch (mode)
        {
            case TEST_SCRIPS:
                scripts = context.getResources().getStringArray(R.array.autotest_scripts);
                break;
            case EXPORT_DOC:
                scripts = context.getResources().getStringArray(R.array.doc_export_scripts);
                break;
            case TAKE_SCREENSHOTS:
                scripts = context.getResources().getStringArray(R.array.take_screenshots_scripts);
                break;
            default:
                scripts = null;
                break;
        }
        formulas.setTaSession(this);
    }

    public static boolean isAutotestOnStart(Context context)
    {
        final File cfgFile = new File(context.getExternalFilesDir(null), TEST_CONFIGURATION);
        return cfgFile.exists();
    }

    @Override
    protected Void doInBackground(Void... t)
    {
        ViewUtils.Debug(this, "Autotest session is started, session contains " + scripts.length + " scripts");
        isPublishRuns.set(false);
        try
        {
            for (int script = 0; script < scripts.length; script++)
            {
                final Uri scriptUri = Uri.parse((String) scripts[script]);
                if (!FileUtils.isAssetUri(scriptUri))
                {
                    continue;
                }
                testScript = new TestScript(scriptUri.toString());
                for (int step = 0; step < STEP_EXPORT; step++)
                {
                    testScript.setState(TestScript.State.values()[step]);
                    TestScript.State currState = testScript.getState();
                    callPublish(step, script);
                    currState = testScript.waitStateChange(currState);
                    if (currState == TestScript.State.CALCULATE_FINISHED)
                    {
                        if (mode == Mode.EXPORT_DOC || mode == Mode.TAKE_SCREENSHOTS)
                        {
                            callPublish(STEP_EXPORT, script);
                        }
                        break;
                    }
                }
                testScript.finish();
                testScripts.add(testScript);
                testScript = null;
                Thread.sleep(500);
            }
        }
        catch (Exception e)
        {
            ViewUtils.Debug(this, "can not execute test script: " + e.getLocalizedMessage());
        }
        formulas.setTaSession(null);
        ViewUtils.Debug(this, getDescription());
        return null;
    }

    private void callPublish(int step, int script) throws InterruptedException
    {
        isPublishRuns.set(true);
        publishProgress(step, script);
        synchronized (isPublishRuns)
        {
            while (isPublishRuns.isSet())
            {
                isPublishRuns.wait();
            }
        }
    }

    @Override
    protected void onProgressUpdate(Integer... t)
    {
        if (t == null || t.length == 0)
        {
            isPublishRuns.set(false);
            return;
        }
        final int step = t[0];
        final int script = t[1];
        if (script < scripts.length) switch (step)
        {
            case STEP_READ:
            {
                final String scriptName = (String) scripts[script];
                formulas.clear();
                formulas.readFromResource(Uri.parse(scriptName), XmlLoaderTask.PostAction.NONE);
                break;
            }
            case STEP_CALC:
            {
                final String scriptName = (String) scripts[script];
                testScript.setScriptContent(scriptName);
                if (mode == Mode.TEST_SCRIPS && formulas.getFormulaListView().getList().getChildCount() > 0)
                {
                    final View v = formulas.getFormulaListView().getList().getChildAt(0);
                    if (v instanceof TextFragment)
                    {
                        testScript.setScriptContent(((TextFragment) v).getTerms().get(0).getText());
                    }
                }
                ViewUtils.Debug(this, "Calculating test script: " + scriptName);
                formulas.calculate();
                break;
            }
            case STEP_EXPORT:
            {
                final String lastPath = Uri.parse((String) scripts[script]).getLastPathSegment();
                final String scriptName = lastPath.contains(".xml")?
                        lastPath.replace(".xml", "") : lastPath.replace(".mmt", "");
                if (mode == Mode.EXPORT_DOC)
                {
                    exportLatex(EXPORT_DOC_DIR, scriptName);
                }
                else if (mode == Mode.TAKE_SCREENSHOTS)
                {
                    takeScreenshot(TAKE_SCREENSHOTS_DIR, scriptName);
                }
                break;
            }
        }
        isPublishRuns.set(false);
    }

    private void exportLatex(String directory, String scriptName)
    {
        final File parent = new File(context.getExternalFilesDir(null) + "/" + directory);
        parent.mkdir();
        final File file = new File(parent, scriptName + ".tex");
        if (file == null)
        {
            return;
        }

        final Uri docUri = FileUtils.ensureScheme(Uri.fromFile(file));
        final Uri parentUri = FileUtils.ensureScheme(Uri.fromFile(parent));
        ViewUtils.Debug(this, "Exporting document " + scriptName + ", parent uri: " + parentUri.toString());
        final AdapterFileSystem adapter = new AdapterFileSystem(context);
        adapter.setUri(parentUri);
        final Exporter.Parameters exportParameters = new Exporter.Parameters();
        exportParameters.skipDocumentHeader = true;
        exportParameters.skipImageLocale = true;
        Exporter.write(formulas, docUri, FileType.LATEX, adapter, exportParameters);
    }

    public boolean takeScreenshot(String directory, String scriptName)
    {
        final File parent = new File(context.getExternalFilesDir(null) + "/" + directory);
        parent.mkdir();
        final File file = new File(parent, scriptName + ".png");
        if (file == null)
        {
            return false;
        }

        final Uri uri = FileUtils.ensureScheme(Uri.fromFile(file));
        OutputStream stream = FileUtils.getOutputStream(context, uri);
        if (stream == null)
        {
            return false;
        }

        try
        {
            /* grab whole window */
            final View v1 = context.getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            final Bitmap bitmap1 = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            /* skip status bar in screenshot */
            final int contentViewTop = ViewUtils.getStatusBarHeight(context);
            final Bitmap bitmap2 = Bitmap.createBitmap(
                    bitmap1, 0, contentViewTop, bitmap1.getWidth(), bitmap1.getHeight() - contentViewTop, null, true);

            /* write into file */
            bitmap2.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.flush();
            stream.close();

            final String message = String.format(context.getResources().getString(R.string.message_file_written),
                    file.getName());
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
        catch (Exception e)
        {
            final String error = String.format(context.getResources().getString(R.string.error_file_write), scriptName);
            ViewUtils.Debug(context, error + ", " + e.getLocalizedMessage());
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
        }
        return true;
    }

    @Override
    protected void onPostExecute(Void result)
    {
        super.onPostExecute(result);
        if (mode == Mode.EXPORT_DOC || mode == Mode.TAKE_SCREENSHOTS)
        {
            return;
        }
        File file = new File(context.getExternalFilesDir(null), REPORT_HTML_FILE);
        try
        {
            if (file != null)
            {
                final FileOutputStream stream = new FileOutputStream(file);
                final StringWriter writer = new StringWriter();
                publishHtmlReport(writer);
                stream.write(writer.toString().getBytes());
                stream.close();
                final String message = String.format(context.getResources().getString(R.string.message_file_written),
                        REPORT_HTML_FILE);
                Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            }
        }
        catch (Exception e)
        {
            final String error = String.format(context.getResources().getString(R.string.error_file_write),
                    REPORT_HTML_FILE);
            Toast.makeText(context, error, Toast.LENGTH_LONG).show();
            file = null;
        }
        if (isAutotestOnStart(context))
        {
            formulas.getActivity().finish();
        }
        else if (file != null)
        {
            try
            {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "text/html");
                formulas.getActivity().startActivity(intent);
            }
            catch (Exception e)
            {
                Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public void setInOperation(AsyncTask owner, boolean inOperation)
    {
        if (testScript != null && !inOperation)
        {
            if (owner instanceof XmlLoaderTask)
            {
                testScript.setState(TestScript.State.LOAD_FINISHED);
            }
            else if (owner instanceof CalculaterTask)
            {
                testScript.setState(TestScript.State.CALCULATE_FINISHED);
            }
        }
    }

    public void setResult(String name, String value)
    {
        if (testScript != null)
        {
            testScript.setResult(name, value);
        }
    }

    public int getTestCaseNumber(NumberType numberType)
    {
        int n = 0;
        for (TestScript ts : testScripts)
        {
            n += ts.getTestCaseNumber(numberType);
        }
        return n;
    }

    public String getDescription()
    {
        final int failedNumber = getTestCaseNumber(NumberType.FAILED);
        return "Test session: number of scrips: " + testScripts.size() + ", number of test cases: "
                + getTestCaseNumber(NumberType.TOTAL) + ", passed: " + getTestCaseNumber(NumberType.PASSED)
                + ", failed: " + failedNumber + ", status: " + (failedNumber == 0 ? "PASSED" : "FAILED");
    }

    public void publishHtmlReport(StringWriter writer) throws Exception
    {
        writer.append("<!DOCTYPE html>\n");
        writer.append("<html><head>\n");
        writer.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");
        writer.append("<title>Test session</title>\n");
        writer.append("</head><body>");

        writer.append("<p>Device information: " + android.os.Build.DEVICE + ", model " + android.os.Build.MODEL
                + ", OS version " + System.getProperty("os.version") + ", API level "
                + Integer.toString(android.os.Build.VERSION.SDK_INT) + "</p>");

        final PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        writer.append("<p>App version: " + context.getResources().getString(pi.applicationInfo.labelRes) + ", "
                + pi.versionName + "</p>");

        for (TestScript ts : testScripts)
        {
            ts.publishHtmlReport(writer);
        }

        writer.append("\n\n<h1>Summary</h1>\n");
        final int failedNumber = getTestCaseNumber(NumberType.FAILED);
        writer.append("<p><b>Number of scrips</b>: " + testScripts.size() + "</p>\n");
        writer.append("<p><b>Number of test cases</b>: " + getTestCaseNumber(NumberType.TOTAL) + "</p>\n");
        writer.append("<p><b>Passed</b>: " + getTestCaseNumber(NumberType.PASSED) + "</p>\n");
        writer.append("<p><b>Failed</b>: " + failedNumber + "</p>\n");
        String status = "<p><b>Status</b>: ";
        if (failedNumber == 0)
        {
            status += "<font color=\"green\">PASSED</font>";
        }
        else
        {
            status += "<font color=\"red\">FAILED</font>";
        }
        status += "</p>\n\n";
        writer.append(status);
        writer.append("</body></html>\n");
    }
}
