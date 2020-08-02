/*
 * Copyright (C) 2014-2018 Mikhail Kulesh
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details. You should have received a copy of the GNU General
 * Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.mkulesh.micromath.io;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.view.View;

import com.mkulesh.micromath.fman.AdapterIf;
import com.mkulesh.micromath.fman.FileUtils;
import com.mkulesh.micromath.formula.Equation;
import com.mkulesh.micromath.formula.FormulaBase;
import com.mkulesh.micromath.formula.FormulaList;
import com.mkulesh.micromath.formula.FormulaListView;
import com.mkulesh.micromath.formula.FormulaResult;
import com.mkulesh.micromath.formula.TermField;
import com.mkulesh.micromath.formula.TextFragment;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.DocumentProperties;
import com.mkulesh.micromath.utils.ViewUtils;

import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Export interface: export to LaTeX
 */
public class ExportToMathJax extends ExportToLatex
{
    private final static int NEW_LINE_CODE = 10;

    public ExportToMathJax(Context context, OutputStream stream, final Uri uri, final AdapterIf adapter)
            throws Exception
    {
        super(context, stream, uri, adapter, null);
    }

    public void write(FormulaList formulas) throws Exception
    {
        final FormulaListView formulaListView = formulas.getFormulaListView();
        final DocumentProperties docProp = formulas.getDocumentSettings();

        writer.append("<!DOCTYPE html>\n");
        writer.append("<html><head>\n");
        writer.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");
        writer.append("<title>");
        writer.append(isPropEmpty(docProp.title) ? fileName : docProp.title);
        writer.append("</title>\n");

        writer.append("<script type=\"text/x-mathjax-config\">\n");
        writer.append("  MathJax.Hub.Config({tex2jax: {inlineMath: [['$','$']]}});\n");
        writer.append("</script>\n");
        writer.append("<script type=\"text/javascript\"\n");
        writer.append("  src=\"http://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML\">\n");
        writer.append("</script>\n");
        writer.append("<style>\n");
        writer.append("  td { margin: 0px; padding: 0px 10px 0px 10px; }\n");
        writer.append("</style>\n");
        writer.append("<style>\n");
        writer.append("  img { padding: 0px; display: block; max-width: 100%; }\n");
        writer.append("</style>\n");
        writer.append("</head><body>");

        if (!isPropEmpty(docProp.title))
        {
            writer.append("\n<h1>").append(String.valueOf(docProp.title)).append("</h1>");
        }

        if (!isPropEmpty(docProp.description))
        {
            writer.append("\n<em>").append(String.valueOf(docProp.description)).append("</em><br>");
        }

        final int n = formulaListView.getList().getChildCount();
        for (int i = 0; i < n; i++)
        {
            View v = formulaListView.getList().getChildAt(i);
            if (v instanceof FormulaListView.ListRow)
            {
                ArrayList<FormulaBase> row = new ArrayList<>();
                ((FormulaListView.ListRow) v).getFormulas(FormulaBase.class, row);
                if (row.size() == 0)
                {
                    // nothing to do
                }
                else
                {
                    writer.append("\n\n<table border = \"0\" cellspacing=\"0\" cellpadding=\"0\"><tr>");
                    for (int k = 0; k < row.size(); k++)
                    {
                        writer.append("\n  <td>");
                        writeFormulaBase(row.get(k), true);
                        writer.append("</td>");
                    }
                    writer.append("\n</tr></table><br>");
                }
            }
            else if (v instanceof TextFragment)
            {
                writeFormulaBase((FormulaBase) v, false);
            }
            else if (v instanceof FormulaBase)
            {
                writer.append("\n\n<table border = \"0\" cellspacing=\"0\" cellpadding=\"0\"><tr>");
                writer.append("\n  <td>");
                writeFormulaBase((FormulaBase) v, true);
                writer.append("</td>");
                writer.append("\n</tr></table><br>");
            }
        }
        writer.append("\n\n</body></html>\n");
        stream.write(writer.toString().getBytes());
    }

    protected void writeEquation(Equation f, boolean inLine)
    {
        writer.append(inLine ? "$$" : "\n\n$$");
        writeTermField(f.findTermWithKey(R.string.formula_left_term_key));
        writer.append(" := ");
        writeTermField(f.findTermWithKey(R.string.formula_right_term_key));
        writer.append(inLine ? "$$" : "$$");
    }

    protected void writeFormulaResult(FormulaResult f, boolean inLine)
    {
        writer.append(inLine ? "$$" : "\n\n$$");
        appendFormulaResult(f);
        writer.append(inLine ? "$$" : "$$");
    }

    protected void writePlotFunction(FormulaBase f, boolean inLine)
    {
        Bitmap bitmap = null;
        try
        {
            bitmap = Bitmap.createBitmap(f.getMeasuredWidth(), f.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
            f.draw(new Canvas(bitmap));
        }
        catch (OutOfMemoryError e)
        {
            ViewUtils.Debug(this, "cannot save picture: " + e);
            return;
        }

        final String figName = fileName + "_fig" + figNumber + ".png";
        Uri figUri = adapter.getItemUri(figName);
        if (figUri == null)
        {
            figUri = adapter.newFile(figName);
        }
        try
        {
            FileUtils.ensureScheme(figUri);
            OutputStream fos = FileUtils.getOutputStream(context, figUri);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            FileUtils.closeStream(fos);
            figNumber++;
        }
        catch (Exception e)
        {
            ViewUtils.Debug(this, "cannot save picture: " + e);
            return;
        }
        if (!inLine)
        {
            writer.append("\n\n<center>");
        }
        writer.append("<img src=\"").append(figName).append("\"alt=\"Image\">");
        if (!inLine)
        {
            writer.append("</center>");
        }
    }

    protected void writeTextFragment(TextFragment f, boolean inLine)
    {
        final ArrayList<TermField> terms = f.getTerms();
        if (!terms.isEmpty())
        {
            String endTag = "";
            if (!inLine)
            {
                writer.append("\n\n");
                switch (f.getTextStyle())
                {
                case CHAPTER:
                    writer.append("<h1>");
                    endTag = "</h1>";
                    break;
                case SECTION:
                    writer.append("<h2>");
                    endTag = "</h2>";
                    break;
                case SUBSECTION:
                    writer.append("<h3>");
                    endTag = "</h3>";
                    break;
                case SUBSUBSECTION:
                    writer.append("<h4>");
                    endTag = "</h4>";
                    break;
                case TEXT_BODY:
                    writer.append("<p>");
                    endTag = "</p>";
                    break;
                }
                final CharSequence number = f.getNumber();
                if (number.length() > 0)
                {
                    writer.append(String.valueOf(number)).append(" ");
                }
            }
            writeHtmlText(terms.get(0).getText(), false);
            if (!inLine && endTag.length() > 0)
            {
                writer.append(endTag);
            }
        }
    }

    private void writeHtmlText(CharSequence text, boolean inEquation)
    {
        StringBuilder outStr = new StringBuilder();
        for (int i = 0; i < text.length(); i++)
        {
            final char c = text.charAt(i);
            boolean processed = false;

            if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.GREEK)
            {
                for (int k = 0; k < greekTable.length; k++)
                {
                    if (greekTable[k][0].charAt(0) == c)
                    {
                        outStr.append(inEquation ? "{" : "$").append(greekTable[k][1]).append(inEquation ? "}" : "$");
                        processed = true;
                        break;
                    }
                }
            }
            else if (c == NEW_LINE_CODE)
            {
                final int offset = getParagraphOffset(text, i);
                if (offset != ViewUtils.INVALID_INDEX)
                {
                    outStr.append("</p>\n\n<p>");
                    i += offset;
                    processed = true;
                }
            }
            if (!processed)
            {
                outStr.append(c);
            }
        }
        writer.append(outStr.toString());
    }

    private int getParagraphOffset(CharSequence text, int currIdx)
    {
        int i = currIdx, newLineNumber = 0;
        for (; i < text.length(); i++)
        {
            final char c = text.charAt(i);
            if (c == NEW_LINE_CODE)
            {
                newLineNumber++;
            }
            else if (!Character.isWhitespace(c))
            {
                break;
            }
        }
        return (newLineNumber > 1 && i > currIdx) ? i - currIdx - 1 : ViewUtils.INVALID_INDEX;
    }
}
