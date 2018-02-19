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

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Xml;

import com.mkulesh.micromath.io.ImportFromSMathStudio;
import com.mkulesh.micromath.fman.FileUtils;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.DocumentProperties;
import com.mkulesh.micromath.properties.TextProperties;
import com.mkulesh.micromath.utils.SynchronizedBoolean;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.utils.XmlUtils;
import com.mkulesh.micromath.widgets.ListChangeIf.Position;

import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

public class XmlLoaderTask extends AsyncTask<Void, FormulaBase.BaseType, Void>
{
    public enum PostAction
    {
        NONE,
        CALCULATE,
        INTERRUPT
    }

    public enum FileFormat
    {
        INVALID,
        MMT,
        SMATH_STUDIO
    }

    private final FormulaList list;
    private final Uri uri;
    private InputStream stream = null;
    private final String name;
    private XmlPullParser parser = null;
    private int firstFormulaId = ViewUtils.INVALID_INDEX;
    private final SynchronizedBoolean isPublishRuns = new SynchronizedBoolean();
    private final SynchronizedBoolean isAborted = new SynchronizedBoolean();
    private final int[] headerNumber;

    // result of operation
    public String error = null;
    public PostAction postAction = null;
    private FileFormat fileFormat = FileFormat.INVALID;

    XmlLoaderTask(FormulaList list, Uri uri, PostAction postAction)
    {
        this.list = list;
        this.uri = uri;
        this.name = FileUtils.getFileName(list.getActivity(), uri);
        this.postAction = postAction;
        this.headerNumber = TextProperties.getInitialNumber();
    }

    @Override
    protected void onPreExecute()
    {
        list.clear();
        list.setInOperation(/* owner= */this, /* inOperation= */true, /* stopHandler= */null);
    }

    private FileFormat getFileFormat()
    {
        InputStream is = FileUtils.getInputStream(list.getActivity(), uri);
        String prop = null;
        try
        {
            final XmlPullParser p = Xml.newPullParser();
            p.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            p.setInput(is, null);
            p.nextTag();
            if (p.getAttributeCount() > 0)
            {
                prop = p.getAttributeValue(0);
            }
        }
        catch (Exception e)
        {
            ViewUtils.Debug(this, "Can not define file format: " + e.getLocalizedMessage());
        }
        FileUtils.closeStream(is);

        if (prop != null && FormulaList.XML_MMT_SCHEMA.equals(prop))
        {
            return FileFormat.MMT;
        }
        else if (prop != null && FormulaList.XML_SM_SCHEMA.equals(prop))
        {
            return FileFormat.SMATH_STUDIO;
        }
        return FileFormat.INVALID;
    }

    @Override
    protected Void doInBackground(Void... arg0)
    {
        isPublishRuns.set(false);
        isAborted.set(false);
        try
        {
            fileFormat = getFileFormat();
            stream = FileUtils.getInputStream(list.getActivity(), uri);
            if (fileFormat == FileFormat.MMT)
            {
                // nothing to do
            }
            else if (fileFormat == FileFormat.SMATH_STUDIO)
            {
                ImportFromSMathStudio importer = new ImportFromSMathStudio(list.getContext(), name);
                InputStream s = new ByteArrayInputStream(importer.convertToMmt(stream).toString().getBytes());
                FileUtils.closeStream(stream);
                stream = s;
            }
            else
            {
                error = String.format(list.getActivity().getResources().getString(R.string.error_unknown_file_format),
                        name);
                ViewUtils.Debug(this, error);
                return null;
            }

            parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(stream, null);
            parser.nextTag();
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
                    ViewUtils.Debug(this, "Document version: " + DocumentProperties.getDocumentVersion());
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
            if (error == null)
            {
                error = String.format(list.getActivity().getResources().getString(R.string.error_file_read), name);
            }
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
        DocumentProperties.setDocumentVersion(DocumentProperties.LATEST_DOCUMENT_VERSION);
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

    public boolean isMmtOpened()
    {
        return fileFormat == XmlLoaderTask.FileFormat.MMT && error == null;
    }
}
