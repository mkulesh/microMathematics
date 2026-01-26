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
package com.mkulesh.micromath.io;

import android.net.Uri;
import android.os.StrictMode;
import android.util.Xml;

import com.mkulesh.micromath.fman.FileUtils;
import com.mkulesh.micromath.formula.FormulaBase;
import com.mkulesh.micromath.formula.FormulaList;
import com.mkulesh.micromath.formula.TextFragment;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.DocumentProperties;
import com.mkulesh.micromath.properties.TextProperties;
import com.mkulesh.micromath.utils.AppTask;
import com.mkulesh.micromath.utils.SynchronizedBoolean;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.ListChangeIf.Position;

import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

import androidx.annotation.Nullable;

public class XmlLoaderTask extends AppTask implements Runnable
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
    private final int[] headerNumber;

    // result of operation
    public String error = null;
    public PostAction postAction = null;
    private FileFormat fileFormat = FileFormat.INVALID;

    public XmlLoaderTask(FormulaList list, Uri uri, PostAction postAction)
    {
        super();
        setBackgroundTask(this, this.getClass().getSimpleName());
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        this.list = list;
        this.uri = uri;
        this.name = FileUtils.getFileName(list.getActivity(), uri);
        this.postAction = postAction;
        this.headerNumber = TextProperties.getInitialNumber();
    }

    protected void onPreExecute()
    {
        list.getActivity().runOnUiThread(() ->
        {
            list.newDocument();
            list.setInOperation(/* owner= */this, /* inOperation= */true, /* stopHandler= */null);
        });
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

        if (FormulaList.XML_MMT_SCHEMA.equals(prop))
        {
            return FileFormat.MMT;
        }
        else if (FormulaList.XML_SM_SCHEMA.equals(prop))
        {
            return FileFormat.SMATH_STUDIO;
        }
        return FileFormat.INVALID;
    }

    @Override
    public void run()
    {
        onPreExecute();
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
                onPostExecute();
                return;
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
                    publishProgress(true, null);
                    ViewUtils.Debug(this, "Document version: " + DocumentProperties.getDocumentVersion());
                    while (parser.next() != XmlPullParser.END_TAG)
                    {
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
                            parser.require(XmlPullParser.START_TAG, FormulaList.XML_NS, n2);
                            publishProgress(false, t);
                        }
                        else
                        {
                            XmlUtils.skipEntry(parser);
                        }
                        if (isCancelled())
                        {
                            error = null;
                            postAction = PostAction.INTERRUPT;
                            onPostExecute();
                            return;
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
        onPostExecute();
    }

    protected void publishProgress(final boolean isHeader, @Nullable FormulaBase.BaseType t)
    {
        final SynchronizedBoolean isPublishRuns = new SynchronizedBoolean();
        isPublishRuns.set(true);
        list.getActivity().runOnUiThread(() ->
        {
            if (isHeader)
            {
                list.getDocumentSettings().readFromXml(parser);
                isPublishRuns.set(false);
                return;
            }
            if (t == null)
            {
                isPublishRuns.set(false);
                return;
            }
            FormulaBase f = list.addBaseFormula(t);
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
        });
        isPublishRuns.waitUntil(false);
    }

    protected void onPostExecute()
    {
        ViewUtils.Debug(this, "thread finished");
        list.getActivity().runOnUiThread(() ->
        {
            if (stream != null)
            {
                FileUtils.closeStream(stream);
            }
            DocumentProperties.setDocumentVersion(DocumentProperties.LATEST_DOCUMENT_VERSION);
            if (list.getSelectedFormulaId() == ViewUtils.INVALID_INDEX)
            {
                list.setSelectedFormula(firstFormulaId, false);
            }
            list.setInOperation(/* owner= */this, /* inOperation= */false, /* stopHandler= */null);
        });
    }

    public boolean isMmtOpened()
    {
        return fileFormat == XmlLoaderTask.FileFormat.MMT && error == null;
    }
}
