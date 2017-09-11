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
package com.mkulesh.micromath.formula;

import java.io.InputStream;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Xml;

import com.mkulesh.micromath.fman.FileUtils;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.TextProperties;
import com.mkulesh.micromath.utils.SynchronizedBoolean;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.utils.XmlUtils;
import com.mkulesh.micromath.widgets.ListChangeIf.Position;

public class XmlLoaderTask extends AsyncTask<Void, FormulaBase.BaseType, Void>
{
    public enum PostAction
    {
        NONE,
        CALCULATE,
        INTERRUPT
    };

    private final FormulaList list;
    private final InputStream stream;
    private final String name;
    private XmlPullParser parser = null;
    private int firstFormulaId = ViewUtils.INVALID_INDEX;
    private final SynchronizedBoolean isPublishRuns = new SynchronizedBoolean();
    private final SynchronizedBoolean isAborted = new SynchronizedBoolean();
    private final int[] headerNumber;

    // result of operation
    public String error = null;
    public PostAction postAction = null;

    XmlLoaderTask(FormulaList list, InputStream stream, String name, PostAction postAction)
    {
        this.list = list;
        this.stream = stream;
        this.name = name;
        this.postAction = postAction;
        this.headerNumber = TextProperties.getInitialNumber();
    }

    @Override
    protected void onPreExecute()
    {
        list.clear();
        list.setInOperation(/* owner= */this, /* inOperation= */true, /* stopHandler= */null);
    }

    @Override
    protected Void doInBackground(Void... arg0)
    {
        isPublishRuns.set(false);
        isAborted.set(false);
        try
        {
            if (Build.VERSION.SDK_INT > 10)
            {
                // on android version < 11, the Xml.newPullParser() has a bug:
                // it throws a UnsupportedOperationException at getting CDSECT
                // using nextToken() method (see ImageFragment.onStartReadXmlTag() method)...
                parser = Xml.newPullParser();
            }
            else
            {
                // ... Therefore, we use an other parser for Android versions 8-10.
                parser = XmlPullParserFactory.newInstance().newPullParser();
            }

            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(stream, null);
            parser.nextTag();
            boolean isValidFormat = false;
            if (parser.getAttributeCount() > 0)
            {
                String prop = parser.getAttributeValue(0);
                if (prop != null && FormulaList.XML_HTTP.equals(prop))
                {
                    isValidFormat = true;
                }
            }
            if (!isValidFormat)
            {
                error = String.format(list.getActivity().getResources().getString(R.string.error_unknown_file_format),
                        name);
                ViewUtils.Debug(this, error + ": " + FormulaList.XML_PROP_MMT + " key is not found");
                return null;
            }
            parser.require(XmlPullParser.START_TAG, FormulaList.XML_NS, FormulaList.XML_MAIN_TAG);
            while (parser.next() != XmlPullParser.END_TAG)
            {
                if (parser.getEventType() != XmlPullParser.START_TAG)
                {
                    continue;
                }
                final String n1 = parser.getName();
                if (n1.equals(FormulaList.XML_LIST_TAG))
                {
                    parser.require(XmlPullParser.START_TAG, FormulaList.XML_NS, FormulaList.XML_LIST_TAG);
                    list.getDocumentSettings().readFromXml(parser);
                    while (true)
                    {
                        if (parser.next() == XmlPullParser.END_TAG)
                        {
                            break;
                        }
                        if (parser.getEventType() != XmlPullParser.START_TAG)
                        {
                            continue;
                        }
                        final String n2 = parser.getName();
                        FormulaBase.BaseType t = null;
                        try
                        {
                            t = FormulaBase.BaseType.valueOf(n2.toUpperCase(Locale.ENGLISH));
                        }
                        catch (Exception ex)
                        {
                            // nothing to do
                        }
                        if (t != null)
                        {
                            isPublishRuns.set(true);
                            parser.require(XmlPullParser.START_TAG, FormulaList.XML_NS, n2);
                            publishProgress(t);
                            synchronized (isPublishRuns)
                            {
                                while (isPublishRuns.isSet())
                                {
                                    isPublishRuns.wait();
                                }
                            }
                            if (error != null)
                            {
                                return null;
                            }
                        }
                        else
                        {
                            XmlUtils.skipEntry(parser);
                        }
                        if (isAborted.isSet())
                        {
                            error = null;
                            postAction = PostAction.INTERRUPT;
                            return null;
                        }
                        try
                        {
                            Thread.sleep(25);
                        }
                        catch (InterruptedException e)
                        {
                            // nothing to do
                        }
                    }
                }
                else
                {
                    XmlUtils.skipEntry(parser);
                }
            }
        }
        catch (Exception e)
        {
            error = String.format(list.getActivity().getResources().getString(R.string.error_file_read), name);
            ViewUtils.Debug(this, error + ", " + e.getLocalizedMessage());
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(FormulaBase.BaseType... t)
    {
        if (isAborted.isSet())
        {
            isPublishRuns.set(false);
            return;
        }
        FormulaBase f = list.addBaseFormula(t[0]);
        try
        {
            f.readFromXml(parser);
        }
        catch (Exception e)
        {
            error = String.format(list.getActivity().getResources().getString(R.string.error_file_read), name);
            ViewUtils.Debug(this, error + ", " + e.getLocalizedMessage());
        }
        if (f != null)
        {
            if (f instanceof TextFragment)
            {
                ((TextFragment) f).numbering(headerNumber);
            }
            list.getFormulaListView().add(f, null, Position.AFTER); // add to the end
            if (firstFormulaId < 0)
            {
                firstFormulaId = f.getId();
            }
        }
        isPublishRuns.set(false);
    }

    @Override
    protected void onPostExecute(Void par)
    {
        FileUtils.closeStream(stream);
        if (list.getSelectedFormulaId() == ViewUtils.INVALID_INDEX)
        {
            list.setSelectedFormula(firstFormulaId, false);
        }
        list.setInOperation(/* owner= */this, /* inOperation= */false, /* stopHandler= */null);
    }

    public void abort()
    {
        ViewUtils.Debug(this, "trying to cancel XML loader task " + this.toString());
        isAborted.set(true);
    }
}
