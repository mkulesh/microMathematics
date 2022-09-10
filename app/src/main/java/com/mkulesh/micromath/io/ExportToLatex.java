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

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
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
import com.mkulesh.micromath.formula.FormulaTerm;
import com.mkulesh.micromath.formula.TermField;
import com.mkulesh.micromath.formula.TextFragment;
import com.mkulesh.micromath.formula.terms.CommonFunctions;
import com.mkulesh.micromath.formula.terms.FunctionBase;
import com.mkulesh.micromath.formula.terms.Intervals;
import com.mkulesh.micromath.formula.terms.Operators;
import com.mkulesh.micromath.formula.terms.Operators.OperatorType;
import com.mkulesh.micromath.formula.terms.UserFunctions;
import com.mkulesh.micromath.plots.ImageFragment;
import com.mkulesh.micromath.plots.PlotFunction;
import com.mkulesh.micromath.R;
import com.mkulesh.micromath.properties.TextProperties;
import com.mkulesh.micromath.utils.ViewUtils;

import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;

/**
 * Export interface: export to LaTeX
 */
class ExportToLatex
{
    final Context context;
    final OutputStream stream;
    final StringWriter writer = new StringWriter();
    final AdapterIf adapter;
    private final Exporter.Parameters exportParameters;
    String fileName = null;
    int figNumber = 1;
    private boolean currTextNumber = false;

    final String[][] greekTable = new String[][]{

            { "Α", "A" }, { "α", "\\alpha" },

            { "Β", "B" }, { "β", "\\beta" },

            { "Γ", "\\Gamma" }, { "γ", "\\gamma" },

            { "Δ", "\\Delta" }, { "δ", "\\delta" },

            { "Ε", "E" }, { "ε", "\\varepsilon" },

            { "Ζ", "Z" }, { "ζ", "\\zeta" },

            { "Η", "H" }, { "η", "\\eta" },

            { "Θ", "\\Theta" }, { "θ", "\\theta" },

            { "Ι", "I" }, { "ι", "\\iota" },

            { "Κ", "K" }, { "κ", "\\kappa" },

            { "Λ", "\\Lambda" }, { "λ", "\\lambda" },

            { "Μ", "M" }, { "μ", "\\mu" },

            { "Ν", "N" }, { "ν", "\\nu" },

            { "Ξ", "\\Xi" }, { "ξ", "\\xi" },

            { "Ο", "O" }, { "ο", "\\omicron" },

            { "Π", "\\Pi" }, { "π", "\\pi" },

            { "Ρ", "P" }, { "ρ", "\\rho" },

            { "Σ", "\\Sigma" }, { "σ", "\\sigma" }, { "ς", "\\varsigma" },

            { "Τ", "T" }, { "τ", "\\tau" },

            { "Υ", "\\Upsilon" }, { "υ", "\\upsilon" },

            { "Φ", "\\Phi" }, { "φ", "\\varphi" },

            { "Χ", "X" }, { "χ", "\\chi" },

            { "Ψ", "\\Psi" }, { "ψ", "\\psi" },

            { "Ω", "\\Omega" }, { "ω", "\\omega" } };

    private final String[][] supplementTable = new String[][]{
            { "°", "\\degree" } };

    public ExportToLatex(Context context, OutputStream stream, final Uri uri, final AdapterIf adapter,
                         final Exporter.Parameters exportParameters) throws Exception
    {
        this.context = context;
        this.stream = stream;
        this.adapter = adapter;
        this.exportParameters = exportParameters;
        fileName = FileUtils.getFileName(context, uri);
        if (fileName == null)
        {
            throw new Exception("file name is empty");
        }
        final int dotPos = fileName.indexOf(".");
        if (dotPos >= 0 && dotPos < fileName.length())
        {
            fileName = fileName.substring(0, dotPos);
        }
        if (skipImageLocale() && fileName.length() > 3)
        {
            if (fileName.endsWith("_en") || fileName.endsWith("_ru") ||
                    fileName.endsWith("_de") || fileName.endsWith("_br") ||
                    fileName.endsWith("_es"))
            {
                fileName = fileName.substring(0, fileName.length() - 3);
            }
        }
    }

    private boolean skipDocumentHeader()
    {
        return exportParameters != null && exportParameters.skipDocumentHeader;
    }

    private boolean skipImageLocale()
    {
        return exportParameters != null && exportParameters.skipImageLocale;
    }

    private String getImageDirectory()
    {
        return exportParameters != null ? exportParameters.imageDirectory : "";
    }

    public void write(FormulaList formulas) throws Exception
    {
        final FormulaListView formulaListView = formulas.getFormulaListView();

        writer.append("% This is auto-generated file: do not edit!\n");
        try
        {
            final PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            writer.append("% Exported from ")
                    .append(context.getResources().getString(pi.applicationInfo.labelRes))
                    .append(", version ")
                    .append(pi.versionName)
                    .append("\n");
        }
        catch (NameNotFoundException e)
        {
            ViewUtils.Debug(context, "can not write package info: " + e.getLocalizedMessage());
        }

        if (!skipDocumentHeader())
        {
            writer.append("\\documentclass[a4paper,10pt]{article}\n");
            writer.append("\\usepackage[utf8]{inputenc}\n");
            writer.append("\\usepackage{graphicx}\n");
            writer.append("\\usepackage{amssymb}\n");
            writer.append("\\usepackage{amsmath}\n");
            writer.append("% If you use russian, please uncomment the line below\n");
            writer.append("% \\usepackage[russian]{babel}\n\n");
            writer.append("\\voffset=-20mm \\textwidth= 170mm \\textheight=255mm \\oddsidemargin=0mm\n");
            writer.append("\\begin{document}");
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
                    writer.append("\n\\begin{center}\\begin{tabular}{");
                    for (int k = 0; k < row.size(); k++)
                    {
                        writer.append("c");
                    }
                    writer.append("}");
                    for (int k = 0; k < row.size(); k++)
                    {
                        writer.append("\n  ");
                        writeFormulaBase(row.get(k), true);
                        if (k < row.size() - 1)
                        {
                            writer.append(" &");
                        }
                        else
                        {
                            writer.append(" \\cr");
                        }
                    }
                    writer.append("\n\\end{tabular}\\end{center}");
                }
            }
            else if (v instanceof FormulaBase)
            {
                writeFormulaBase((FormulaBase) v, false);
            }
        }

        if (!skipDocumentHeader())
        {
            writer.append("\n\n\\end{document}\n");
        }
        stream.write(writer.toString().getBytes());
    }

    void writeFormulaBase(FormulaBase f, boolean inLine)
    {
        if (f instanceof Equation)
        {
            writeEquation((Equation) f, inLine);
        }
        else if (f instanceof FormulaResult)
        {
            writeFormulaResult((FormulaResult) f, inLine);
        }
        else if (f instanceof PlotFunction || f instanceof ImageFragment)
        {
            writePlotFunction(f, inLine);
        }
        else if (f instanceof TextFragment)
        {
            writeTextFragment((TextFragment) f, inLine);
        }
    }

    void writeEquation(Equation f, boolean inLine)
    {
        writer.append(inLine ? "$" : "\n\\begin{center}\\begin{tabular}{c}\n  $");
        writeTermField(f.findTermWithKey(R.string.formula_left_term_key));
        writer.append(" := ");
        writeTermField(f.findTermWithKey(R.string.formula_right_term_key));
        writer.append(inLine ? "$" : "$\n\\end{tabular}\\end{center}");
    }

    void appendFormulaResult(FormulaResult f)
    {
        writeTermField(f.findTermWithKey(R.string.formula_left_term_key));
        if (f.isResultVisible())
        {
            writer.append(" = ");
            if (f.isArrayResult())
            {
                final ArrayList<ArrayList<String>> res = f.fillResultMatrixArray();
                if (res != null)
                {
                    writer.append("\\begin{bmatrix}");
                    for (ArrayList<String> row : res)
                    {
                        for (int i = 0; i < row.size(); i++)
                        {
                            writer.append(FormulaResult.CELL_DOTS.equals(row.get(i)) ? "\\dots" : row.get(i));
                            writer.append(i + 1 < row.size() ? "&" : "\\\\");
                        }
                    }
                    writer.append("\\end{bmatrix}");
                }
                else
                {
                    writeTermField(f.findTermWithKey(R.string.formula_right_term_key));
                }
            }
            else
            {
                writeTermField(f.findTermWithKey(R.string.formula_right_term_key));
            }
        }
    }

    void writeFormulaResult(FormulaResult f, boolean inLine)
    {
        writer.append(inLine ? "$" : "\n\\begin{center}\\begin{tabular}{c}\n  $");
        appendFormulaResult(f);
        writer.append(inLine ? "$" : "$\n\\end{tabular}\\end{center}");
    }

    void writePlotFunction(FormulaBase f, boolean inLine)
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
            writer.append("\n\\begin{center}");
        }
        writer.append("\\begin{tabular}{c} \\includegraphics[width=0.45\\textwidth]{");
        if (!getImageDirectory().isEmpty())
        {
            writer.append(getImageDirectory()).append("/");
        }
        writer.append(figName).append("} \\end{tabular}");
        if (!inLine)
        {
            writer.append("\\end{center}");
        }
    }

    void writeTextFragment(TextFragment f, boolean inLine)
    {
        final ArrayList<TermField> terms = f.getTerms();
        if (!terms.isEmpty())
        {
            if (currTextNumber)
            {
                if (f.getTextStyle() != TextProperties.TextStyle.TEXT_BODY || !f.isNumbering())
                {
                    writer.append("\n\\end{enumerate}");
                    currTextNumber = false;
                }
            }
            if (!inLine)
            {
                writer.append("\n\n");
            }
            String endTag = "";
            final String astrix = f.isNumbering()? "" : "*";
            switch (f.getTextStyle())
            {
            case CHAPTER:
                writer.append("\\chapter").append(astrix).append("{");
                endTag = "}";
                break;
            case SECTION:
                writer.append("\\section").append(astrix).append("{");
                endTag = "}";
                break;
            case SUBSECTION:
                writer.append("\\subsection").append(astrix).append("{");
                endTag = "}";
                break;
            case SUBSUBSECTION:
                writer.append("\\subsubsection").append(astrix).append("{");
                endTag = "}";
                break;
            case TEXT_BODY:
                if (f.isNumbering() && f.getNumber() != null)
                {
                    if (!currTextNumber)
                    {
                        writer.append("\\begin{enumerate}\n");
                    }
                    writer.append("\\item ");
                    currTextNumber = true;
                }
                break;
            }
            writeText(terms.get(0).getText(), false);
            if (endTag.length() > 0)
            {
                writer.append(endTag);
            }
        }
    }

    void writeTermField(TermField t)
    {
        if (t == null)
        {
            return;
        }
        if (!t.isTerm())
        {
            if (t.isEmpty())
            {
                writer.append("{\\Box}");
            }
            else
            {
                writeText(t.getText(), true);
            }
        }
        else if (t.getTerm() != null)
        {
            final FormulaTerm term = t.getTerm();
            switch (term.getGroupType())
            {
            case OPERATORS:
                writeTermOperator((Operators) term);
                break;
            case TRIGONOMETRIC_FUNCTIONS:
            case LOG_FUNCTIONS:
            case NUMBER_FUNCTIONS:
                writeTermFunctionBase((FunctionBase) term);
                break;
            case COMMON_FUNCTIONS:
                writeTermFunction((CommonFunctions) term);
                break;
            case USER_FUNCTIONS:
                writeTermFunction((UserFunctions) term);
                break;
            case INTERVALS:
                writeTermInterval((Intervals) term);
                break;
            }
        }
    }

    private void writeTermOperator(Operators f)
    {
        Operators.OperatorType operatorType = f.getOperatorType();
        if (f.isUseBrackets())
        {
            writer.append("\\left( ");
        }
        if (operatorType == OperatorType.DIVIDE)
        {
            writer.append("\\frac{");
        }
        writeTermField(f.findTermWithKey(R.string.formula_left_term_key));
        switch (operatorType)
        {
        case DIVIDE:
            writer.append("}{");
            break;
        case DIVIDE_SLASH:
            writer.append(" / ");
            break;
        case MINUS:
            writer.append(" - ");
            break;
        case MULT:
            writer.append(" \\cdot ");
            break;
        case PLUS:
            writer.append(" + ");
            break;
        }
        writeTermField(f.findTermWithKey(R.string.formula_right_term_key));
        if (operatorType == OperatorType.DIVIDE)
        {
            writer.append("}");
        }
        if (f.isUseBrackets())
        {
            writer.append(" \\right)");
        }
    }

    private void writeTermFunctionBase(FunctionBase f)
    {
        final ArrayList<TermField> terms = f.getTerms();
        if (f.getFunctionTerm() != null)
        {
            writeText(f.getFunctionTerm().getText(), true);
        }
        writer.append(" \\left( ");
        for (int i = 0; i < terms.size(); i++)
        {
            if (i > 0)
            {
                writer.append(",\\, ");
            }
            writeTermField(terms.get(i));
        }
        writer.append("\\right) ");
    }

    private void writeTermFunction(CommonFunctions f)
    {
        CommonFunctions.FunctionType functionType = f.getFunctionType();
        final ArrayList<TermField> terms = f.getTerms();
        switch (functionType)
        {
        case SQRT_LAYOUT:
            writer.append("\\sqrt{");
            writeTermField(terms.get(0));
            writer.append("} ");
            break;
        case NTHRT_LAYOUT:
            if (terms.size() == 2)
            {
                writer.append("\\sqrt[\\leftroot{-3}\\uproot{3}");
                writeTermField(terms.get(0));
                writer.append("]{");
                writeTermField(terms.get(1));
                writer.append("} ");
            }
            break;
        case CONJUGATE_LAYOUT:
            writer.append("\\overline{");
            writeTermField(terms.get(0));
            writer.append("} ");
            break;
        case RE:
            writer.append("\\Re\\left( ");
            writeTermField(terms.get(0));
            writer.append(" \\right) ");
            break;
        case IM:
            writer.append("\\Im\\left( ");
            writeTermField(terms.get(0));
            writer.append(" \\right) ");
            break;
        case ABS_LAYOUT:
            writer.append(" \\left| ");
            writeTermField(terms.get(0));
            writer.append(" \\right| ");
            break;
        case FACTORIAL:
            writeTermField(terms.get(0));
            writer.append("! ");
            break;
        case POWER:
            writer.append("{");
            writeTermField(terms.get(0));
            writer.append("}^{");
            writeTermField(terms.get(1));
            writer.append("}");
            break;
        default:
            writeTermFunctionBase(f);
            break;
        }
    }

    private void writeTermFunction(UserFunctions f)
    {
        UserFunctions.FunctionType functionType = f.getFunctionType();
        final ArrayList<TermField> terms = f.getTerms();
        if (f.getFunctionTerm() != null)
        {
            writeText(f.getFunctionTerm().getText(), true);
        }
        writer.append(" \\left( ");
        for (int i = 0; i < terms.size(); i++)
        {
            if (i > 0)
            {
                writer.append(",\\, ");
            }
            writeTermField(terms.get(i));
        }
        writer.append("\\right) ");
    }

    private void writeTermInterval(Intervals f)
    {
        writer.append("\\left[ ");
        writeTermField(f.findTermWithKey(R.string.formula_min_value_key));
        writer.append(",\\, ");
        writeTermField(f.findTermWithKey(R.string.formula_next_value_key));
        writer.append(" \\,..\\, ");
        writeTermField(f.findTermWithKey(R.string.formula_max_value_key));
        writer.append(" \\right]");
    }

    private void writeText(CharSequence text, boolean inEquation)
    {
        StringBuilder outStr = new StringBuilder();
        for (int i = 0; i < text.length(); i++)
        {
            final char c = text.charAt(i);
            boolean processed = false;

            final Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
            if (block == Character.UnicodeBlock.GREEK)
            {
                for (String[] strings : greekTable)
                {
                    if (strings[0].charAt(0) == c)
                    {
                        outStr.append(inEquation ? "{" : "$").append(strings[1]).append(inEquation ? "}" : "$");
                        processed = true;
                        break;
                    }
                }
            }
            else if (!(this instanceof ExportToMathJax) && block == Character.UnicodeBlock.LATIN_1_SUPPLEMENT)
            {
                for (String[] strings : supplementTable)
                {
                    if (strings[0].charAt(0) == c)
                    {
                        outStr.append(inEquation ? "{" : "$").append(strings[1]).append(inEquation ? "}" : "$");
                        processed = true;
                        break;
                    }
                }
            }
            else if (c == '_')
            {
                outStr.append("\\_");
                processed = true;
            }
            else if (c == '"')
            {
                outStr.append("''");
                processed = true;
            }
            if (!processed)
            {
                outStr.append(c);
            }
        }
        writer.append(outStr.toString());
    }

    boolean isPropEmpty(CharSequence prop)
    {
        return prop == null || prop.length() == 0;
    }
}
