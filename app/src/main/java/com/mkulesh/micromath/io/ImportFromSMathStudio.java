/*
 * microMathematics - Extended Visual Calculator
 * Copyright (C) 2014-2021 by Mikhail Kulesh
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
import android.util.Xml;

import com.mkulesh.micromath.formula.FormulaBase;
import com.mkulesh.micromath.formula.FormulaList;
import com.mkulesh.micromath.formula.terms.CommonFunctions;
import com.mkulesh.micromath.formula.terms.Comparators;
import com.mkulesh.micromath.formula.terms.Intervals;
import com.mkulesh.micromath.formula.terms.NumberFunctions;
import com.mkulesh.micromath.formula.terms.Operators;
import com.mkulesh.micromath.formula.terms.SeriesIntegrals;
import com.mkulesh.micromath.formula.terms.TermFactory;
import com.mkulesh.micromath.formula.terms.TermTypeIf;
import com.mkulesh.micromath.formula.terms.TrigonometricFunctions;
import com.mkulesh.micromath.formula.terms.UserFunctions;
import com.mkulesh.micromath.plots.PlotFunction;
import com.mkulesh.micromath.properties.DocumentProperties;
import com.mkulesh.micromath.properties.LineProperties;
import com.mkulesh.micromath.properties.PlotProperties;
import com.mkulesh.micromath.utils.AppLocale;
import com.mkulesh.micromath.utils.ViewUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

class ImportFromSMathStudio
{
    private final String SM_TAG_MATH_EXPRESSION = "e";
    private final String SM_TAG_MATH_OPERATOR = "operator";

    private static final class CodeMapValue
    {
        final TermTypeIf termType;
        final CharSequence[] terms;

        CodeMapValue(TermTypeIf termType, CharSequence[] terms)
        {
            this.termType = termType;
            this.terms = terms;
        }

        boolean isValidArgs(int args)
        {
            return termType == UserFunctions.FunctionType.IDENTITY || terms.length == args;
        }
    }

    private final String fileName;
    private final Map<String, CodeMapValue> codeMap = new HashMap<>();
    private final Map<String, String> textMap = new HashMap<>();
    private final PlotProperties plotProp = new PlotProperties();
    private final String defLanguage, prefLanguage;

    public ImportFromSMathStudio(Context context, String fileName)
    {
        this.fileName = fileName;

        codeMap.put("+", new CodeMapValue(
                Operators.OperatorType.PLUS, new CharSequence[]{ "rightTerm", "leftTerm" }));
        codeMap.put("-", new CodeMapValue(
                Operators.OperatorType.MINUS, new CharSequence[]{ "rightTerm", "leftTerm" }));
        codeMap.put("*", new CodeMapValue(
                Operators.OperatorType.MULT, new CharSequence[]{ "rightTerm", "leftTerm" }));
        codeMap.put("/", new CodeMapValue(
                Operators.OperatorType.DIVIDE, new CharSequence[]{ "rightTerm", "leftTerm" }));

        codeMap.put("≡", new CodeMapValue(
                Comparators.ComparatorType.EQUAL, new CharSequence[]{ "rightTerm", "leftTerm" }));
        codeMap.put("≠", new CodeMapValue(
                Comparators.ComparatorType.NOT_EQUAL, new CharSequence[]{ "rightTerm", "leftTerm" }));
        codeMap.put(">", new CodeMapValue(
                Comparators.ComparatorType.GREATER, new CharSequence[]{ "rightTerm", "leftTerm" }));
        codeMap.put("≥", new CodeMapValue(
                Comparators.ComparatorType.GREATER_EQUAL, new CharSequence[]{ "rightTerm", "leftTerm" }));
        codeMap.put("<", new CodeMapValue(
                Comparators.ComparatorType.LESS, new CharSequence[]{ "rightTerm", "leftTerm" }));
        codeMap.put("≤", new CodeMapValue(
                Comparators.ComparatorType.LESS_EQUAL, new CharSequence[]{ "rightTerm", "leftTerm" }));
        codeMap.put("|", new CodeMapValue(
                Comparators.ComparatorType.COMPARATOR_OR, new CharSequence[]{ "rightTerm", "leftTerm" }));
        codeMap.put("&", new CodeMapValue(
                Comparators.ComparatorType.COMPARATOR_AND, new CharSequence[]{ "rightTerm", "leftTerm" }));

        codeMap.put("(", new CodeMapValue(
                UserFunctions.FunctionType.IDENTITY, new CharSequence[]{ "argTerm" }));
        codeMap.put("^", new CodeMapValue(
                CommonFunctions.FunctionType.POWER, new CharSequence[]{ "rightTerm", "leftTerm" }));
        codeMap.put("!", new CodeMapValue(
                CommonFunctions.FunctionType.FACTORIAL, new CharSequence[]{ "argTerm" }));
        codeMap.put("abs", new CodeMapValue(
                CommonFunctions.FunctionType.ABS_LAYOUT, new CharSequence[]{ "argTerm" }));
        codeMap.put("sqrt", new CodeMapValue(
                CommonFunctions.FunctionType.SQRT_LAYOUT, new CharSequence[]{ "argTerm" }));
        codeMap.put("nthroot", new CodeMapValue(
                CommonFunctions.FunctionType.NTHRT_LAYOUT, new CharSequence[]{ "leftTerm", "rightTerm" }));
        codeMap.put("Conjugate", new CodeMapValue(
                CommonFunctions.FunctionType.CONJUGATE_LAYOUT, new CharSequence[]{ "argTerm" }));
        codeMap.put("Re", new CodeMapValue(
                CommonFunctions.FunctionType.RE, new CharSequence[]{ "argTerm" }));
        codeMap.put("Im", new CodeMapValue(
                CommonFunctions.FunctionType.IM, new CharSequence[]{ "argTerm" }));

        codeMap.put("atan", new CodeMapValue(
                TrigonometricFunctions.FunctionType.ATAN2, new CharSequence[]{ "argTerm2", "argTerm1" }));

        codeMap.put("Max", new CodeMapValue(
                NumberFunctions.FunctionType.MAX, new CharSequence[]{ "argTerm2", "argTerm1" }));
        codeMap.put("Min", new CodeMapValue(
                NumberFunctions.FunctionType.MIN, new CharSequence[]{ "argTerm2", "argTerm1" }));
        codeMap.put("Ceil", new CodeMapValue(
                NumberFunctions.FunctionType.CEIL, new CharSequence[]{ "argTerm" }));
        codeMap.put("Floor", new CodeMapValue(
                NumberFunctions.FunctionType.FLOOR, new CharSequence[]{ "argTerm" }));

        codeMap.put("sum", new CodeMapValue(
                SeriesIntegrals.LoopType.SUMMATION, new CharSequence[]{ "maxValue", "minValue", "index", "argTerm" }));
        codeMap.put("product", new CodeMapValue(
                SeriesIntegrals.LoopType.PRODUCT, new CharSequence[]{ "maxValue", "minValue", "index", "argTerm" }));
        codeMap.put("diff", new CodeMapValue(
                SeriesIntegrals.LoopType.DERIVATIVE, new CharSequence[]{ "index", "argTerm" }));
        codeMap.put("int", new CodeMapValue(
                SeriesIntegrals.LoopType.INTEGRAL, new CharSequence[]{ "maxValue", "minValue", "index", "argTerm" }));

        codeMap.put("range", new CodeMapValue(
                Intervals.IntervalType.EQUIDISTANT_INTERVAL, new CharSequence[]{ "nextValue", "maxValue", "minValue" }));

        textMap.put("#", "");
        textMap.put("\\0027\\", "'");
        textMap.put("\\0022\\", "\"");

        plotProp.initialize(context);

        defLanguage = Locale.getDefault().getISO3Language();
        prefLanguage = AppLocale.ContextWrapper.getPreferredLocale(context).getISO3Language();
    }

    public StringWriter convertToMmt(InputStream stream)
    {
        ViewUtils.Debug(this, "Converting SMath Studio file " + fileName);
        try
        {
            // Source document
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet
            factory.setExpandEntityReferences(false);
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document doc = builder.parse(stream);
            final Node objects = doc.getDocumentElement();
            List<Element> metadata = null;
            final List<Element> regions = new ArrayList<>();
            for (Node object = objects.getFirstChild(); object != null; object = object.getNextSibling())
            {
                if (object instanceof Element)
                {
                    final Element e = (Element) object;
                    if (e.getTagName().equals("settings"))
                    {
                        metadata = XmlUtils.getElements(e, "metadata");
                    }
                    else
                    {
                        regions.add(e);
                    }
                }
            }

            // Target document
            final StringWriter writer = new StringWriter();
            final XmlSerializer serializer = Xml.newSerializer();
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.setPrefix(FormulaList.XML_PROP_MMT, FormulaList.XML_MMT_SCHEMA);
            serializer.startTag(FormulaList.XML_NS, FormulaList.XML_MAIN_TAG);
            serializer.startTag(FormulaList.XML_NS, FormulaList.XML_LIST_TAG);
            serializer.attribute(FormulaList.XML_NS, DocumentProperties.XML_PROP_VERSION, "2");
            if (metadata != null)
            {
                parseMetadata(metadata, serializer);
            }
            serializer.attribute(FormulaList.XML_NS, DocumentProperties.XML_PROP_REDEFINE_ALLOWED, "true");

            Element prevRegion = null;
            for (Element e : regions)
            {
                if ("region".equals(e.getTagName()))
                {
                    parseRegion(e, prevRegion, serializer);
                    prevRegion = e;
                }
            }

            serializer.endTag(FormulaList.XML_NS, FormulaList.XML_LIST_TAG);
            serializer.endTag(FormulaList.XML_NS, FormulaList.XML_MAIN_TAG);
            serializer.endDocument();

            /*
            InputStream s = new ByteArrayInputStream(writer.toString().getBytes());
            BufferedReader reader = new BufferedReader(new InputStreamReader(s));
            while (reader.ready())
            {
                String line = reader.readLine();
                ViewUtils.Debug(this, line);
            }
            */

            return writer;
        }
        catch (Exception e)
        {
            ViewUtils.Debug(this, "Error: " + e.getLocalizedMessage());
            return null;
        }
    }

    private void parseMetadata(List<Element> metadata, XmlSerializer serializer) throws IOException
    {
        final String targetLanguage = getTargetLanguage(metadata, "metadata");
        for (Element e : metadata)
        {
            if (targetLanguage == null || XmlUtils.ensureAttribute(e, "lang", targetLanguage))
            {
                for (Element p : XmlUtils.getElements(e))
                {
                    if (p.getTagName().equals("title"))
                    {
                        serializer.attribute(FormulaList.XML_NS,
                                DocumentProperties.XML_PROP_TITLE, p.getTextContent());
                    }
                    if (p.getTagName().equals("author"))
                    {
                        serializer.attribute(FormulaList.XML_NS,
                                DocumentProperties.XML_PROP_AUTHOR, p.getTextContent());
                    }
                    if (p.getTagName().equals("description"))
                    {
                        serializer.attribute(FormulaList.XML_NS,
                                DocumentProperties.XML_PROP_DESCRIPTION, p.getTextContent());
                    }
                }
                break;
            }
        }
    }

    /*--------------------------------------------------------*
     * Parser methods
     *--------------------------------------------------------*/

    private void parseRegion(final Element e, final Element prevRegion, final XmlSerializer serializer) throws Exception
    {
        boolean inRightOfPrevious = false;
        try
        {
            if (prevRegion != null)
            {
                final int prevTop = Integer.parseInt(prevRegion.getAttribute("top"));
                final int prevButton = prevTop + Integer.parseInt(prevRegion.getAttribute("height"));
                final int thisTop = Integer.parseInt(e.getAttribute("top"));
                final int thisButton = thisTop + Integer.parseInt(e.getAttribute("height"));
                final int thisCenter = (thisTop + thisButton) / 2;
                if (thisCenter > prevTop && thisCenter < prevButton)
                {
                    inRightOfPrevious = true;
                }
            }
        }
        catch (Exception ex)
        {
            // nothing to do
        }

        final List<Element> elements = XmlUtils.getElements(e);
        final String targetLanguage = getTargetLanguage(elements, "text");
        for (Element en : elements)
        {
            if ("text".equals(en.getTagName()))
            {
                if (targetLanguage == null || XmlUtils.ensureAttribute(en, "lang", targetLanguage))
                {
                    parseTextFragment(en, inRightOfPrevious, serializer);
                }
            }
            else if ("math".equals(en.getTagName()))
            {
                parseMathExpression(en, inRightOfPrevious, serializer);
            }
            else if ("plot".equals(en.getTagName()))
            {
                parsePlot(en, inRightOfPrevious, serializer);
            }
        }
    }

    private String getTargetLanguage(List<Element> elements, final String key)
    {
        ArrayList<String> textLanguages = null;
        for (Element en : elements)
        {
            if (key.equals(en.getTagName()) && en.getAttribute("lang") != null)
            {
                if (textLanguages == null)
                {
                    textLanguages = new ArrayList<>();
                }
                textLanguages.add(en.getAttribute("lang"));
            }
        }
        if (textLanguages != null)
        {
            if (textLanguages.contains(prefLanguage))
            {
                return prefLanguage;
            }
            else if (textLanguages.contains(defLanguage))
            {
                return defLanguage;
            }
            else if (textLanguages.contains("eng"))
            {
                return "eng";
            }
        }
        return null;
    }

    private void parsePlot(Element e, boolean inRightOfPrevious, XmlSerializer serializer) throws Exception
    {
        if (!XmlUtils.ensureAttribute(e, "type", "2d"))
        {
            return;
        }
        final Element input = XmlUtils.getElement(XmlUtils.getElements(e), "input");
        if (input == null)
        {
            return;
        }

        final String term = FormulaBase.BaseType.PLOT_FUNCTION.toString().toLowerCase(Locale.ENGLISH);
        serializer.startTag(FormulaList.XML_NS, term);
        plotProp.writeToXml(serializer);
        serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_INRIGHTOFPREVIOUS,
                Boolean.toString(inRightOfPrevious));

        final List<Element> elements = XmlUtils.getElements(input, SM_TAG_MATH_EXPRESSION);
        if (!elements.isEmpty())
        {
            parsePlotFunctions(elements, serializer);
        }

        serializer.endTag(FormulaList.XML_NS, term);
    }

    private void parsePlotFunctions(List<Element> elements, XmlSerializer serializer) throws Exception
    {
        final Element last = XmlUtils.getLast(elements);
        if (last == null || last.getTextContent() == null)
        {
            return;
        }
        ExpressionProperties p = new ExpressionProperties(last);
        LineProperties lineProp = new LineProperties();
        if (p.text.equals("sys") && p.args > 2)
        {
            // Multiple functions
            XmlUtils.removeLast(elements);
            final int argNumber = p.args - 2;
            serializer.attribute(FormulaList.XML_NS, PlotFunction.XML_PROP_FUNCTIONS_NUMBER,
                    Integer.toString(argNumber));
            // remove two first arguments of sys function
            XmlUtils.removeLast(elements);
            XmlUtils.removeLast(elements);
            // add functions
            for (int i = 0; i < argNumber; i++)
            {
                final String suffix = Integer.toString(argNumber - i);
                LineProperties newProp = new LineProperties();
                parseTerm("yFunction" + suffix, elements, serializer, false, lineProp);
                addTextTag("xFunction" + suffix, "x", serializer);
                newProp.setNextDefault(lineProp);
                lineProp = newProp;
            }
        }
        else
        {
            // single function
            serializer.attribute(FormulaList.XML_NS, PlotFunction.XML_PROP_FUNCTIONS_NUMBER, "1");
            parseTerm("yFunction", elements, serializer, false, lineProp);
            addTextTag("xFunction", "x", serializer);
        }
        addTextTag("yMinValue", "", serializer);
        addTextTag("yMaxValue", "", serializer);
        addTextTag("xMinValue", "", serializer);
        addTextTag("xMaxValue", "", serializer);
    }

    private void parseTextFragment(Element e, boolean inRightOfPrevious, final XmlSerializer serializer) throws Exception
    {
        StringBuilder text = new StringBuilder();
        for (Element en : XmlUtils.getElements(e, "p"))
        {
            if (text.length() > 0)
            {
                text.append("\n\n");
            }
            text.append(en.getTextContent());
        }
        // TODO: get metadata
        final String term = FormulaBase.BaseType.TEXT_FRAGMENT.toString().toLowerCase(Locale.ENGLISH);
        serializer.startTag(FormulaList.XML_NS, term);
        serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_INRIGHTOFPREVIOUS,
                Boolean.toString(inRightOfPrevious));
        addTextTag(FormulaList.XML_PROP_TEXT, text.toString(), serializer);
        serializer.endTag(FormulaList.XML_NS, term);
    }

    private void parseMathExpression(Element e, boolean inRightOfPrevious, XmlSerializer serializer) throws Exception
    {
        final List<Element> elements = XmlUtils.getElements(e);
        final Element input = XmlUtils.getElement(elements, "input");
        final Element result = XmlUtils.getElement(elements, "result");
        if (input == null)
        {
            return;
        }
        if (result == null)
        {
            parseEquation(input, inRightOfPrevious, serializer);
        }
        else
        {
            parseResult(input, result, inRightOfPrevious, serializer);
        }
    }

    private void parseEquation(final Element input, boolean inRightOfPrevious, final XmlSerializer serializer) throws Exception
    {
        final List<Element> elements = XmlUtils.getElements(input, SM_TAG_MATH_EXPRESSION);
        final Element last = XmlUtils.removeLast(elements);
        if (last == null || last.getTextContent() == null)
        {
            return;
        }
        ExpressionProperties p = new ExpressionProperties(last);
        if (!p.isEqual(SM_TAG_MATH_OPERATOR, 2, ":"))
        {
            return;
        }
        final String term = FormulaBase.BaseType.EQUATION.toString().toLowerCase(Locale.ENGLISH);
        serializer.startTag(FormulaList.XML_NS, term);
        serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_INRIGHTOFPREVIOUS,
                Boolean.toString(inRightOfPrevious));
        parseTerm("rightTerm", elements, serializer, false);
        parseTerm("leftTerm", elements, serializer, true);
        serializer.endTag(FormulaList.XML_NS, term);
    }

    private void parseTerm(final CharSequence key, final List<Element> elements, final XmlSerializer serializer,
                           boolean asText) throws Exception
    {
        parseTerm(key, elements, serializer, asText, null);
    }

    private void parseTerm(final CharSequence key, final List<Element> elements, final XmlSerializer serializer,
                           boolean asText, LineProperties lineProp) throws Exception
    {
        final Element last = XmlUtils.removeLast(elements);
        if (last == null || last.getTextContent() == null)
        {
            return;
        }
        ExpressionProperties p = new ExpressionProperties(last);
        serializer.startTag(FormulaList.XML_NS, FormulaList.XML_TERM_TAG);
        serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_KEY, key.toString());
        if (lineProp != null)
        {
            lineProp.writeToXml(serializer);
        }
        CodeMapValue code = codeMap.get(p.text);
        if (p.isOperand())
        {
            // A text term
            final String newText = textMap.get(p.text);
            serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_TEXT,
                    newText != null ? newText : p.text);
        }
        else if (p.isEqual(SM_TAG_MATH_OPERATOR, 1, "-"))
        {
            // Minus sign
            parseNegativeTerm(elements, serializer);
        }
        else if (!asText && code != null && code.isValidArgs(p.args))
        {
            // A term from conversion table
            serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_CODE,
                    code.termType.getLowerCaseName());
            for (CharSequence t : code.terms)
            {
                parseTerm(t, elements, serializer, false);
            }
        }
        else if (p.isFunction())
        {
            final TermTypeIf fi = TermFactory.getTermMap().get(p.text);
            if (asText)
            {
                // Function name
                serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_TEXT,
                        makeFunctionName(elements, p));
            }
            else if (fi != null)
            {
                // Build-in function
                serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_CODE, p.text);
                parseTermArguments(p.args, elements, serializer);
            }
            else if (p.isArray() && !elements.isEmpty())
            {
                // Array
                final String arrayName = getArrayName(elements, p.args);
                if (!arrayName.isEmpty())
                {
                    serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_CODE,
                            UserFunctions.FunctionType.FUNCTION_INDEX.getLinkObject() + "." + arrayName +
                                    UserFunctions.FUNCTION_ARGS_MARKER + (p.args - 1));
                    parseTermArguments(p.args - 1, elements, serializer);
                    XmlUtils.removeLast(elements);
                }
                else
                {
                    // Array can not be resolved: convert as user function
                    serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_CODE,
                            UserFunctions.FunctionType.FUNCTION_LINK.getLinkObject() + "." + p.text +
                                    UserFunctions.FUNCTION_ARGS_MARKER + p.args);
                    parseTermArguments(p.args, elements, serializer);
                }
            }
            else
            {
                // User function
                serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_CODE,
                        UserFunctions.FunctionType.FUNCTION_LINK.getLinkObject() + "." + p.text +
                                UserFunctions.FUNCTION_ARGS_MARKER + p.args);
                parseTermArguments(p.args, elements, serializer);
            }
        }
        serializer.endTag(FormulaList.XML_NS, FormulaList.XML_TERM_TAG);
    }

    private String getArrayName(final List<Element> prevElements, int args) throws Exception
    {
        ArrayList<Element> elements = new ArrayList<>(prevElements.size());
        elements.addAll(prevElements);

        if (args > 1)
        {
            final StringWriter writer = new StringWriter();
            final XmlSerializer serializer = Xml.newSerializer();
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            parseTermArguments(args - 1, elements, serializer);
            serializer.endDocument();
        }

        final Element last = XmlUtils.getLast(elements);
        if (last != null)
        {
            ExpressionProperties p = new ExpressionProperties(last);
            if (p.isOperand())
            {
                return p.text;
            }
        }
        return "";
    }

    private void parseTermArguments(int args, List<Element> elements, XmlSerializer serializer) throws Exception
    {
        if (args == 1)
        {
            parseTerm("argTerm", elements, serializer, false);
        }
        else for (int i = 0; i < args; i++)
        {
            parseTerm("argTerm" + (args - i), elements, serializer, false);
        }
    }

    private void parseNegativeTerm(final List<Element> elements, final XmlSerializer serializer) throws Exception
    {
        final Element last = XmlUtils.getLast(elements);
        if (last == null)
        {
            return;
        }
        ExpressionProperties p = new ExpressionProperties(last);
        if (p.isOperand())
        {
            serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_TEXT, "-" + p.text);
            XmlUtils.removeLast(elements);
        }
        else
        {
            serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_CODE,
                    Operators.OperatorType.MULT.getLowerCaseName());
            addTextTag("leftTerm", "-1", serializer);
            parseTerm("rightTerm", elements, serializer, false);
        }
    }

    private void parseResult(final Element input, final Element result, boolean inRightOfPrevious, final XmlSerializer serializer) throws Exception
    {
        final String term = FormulaBase.BaseType.RESULT.toString().toLowerCase(Locale.ENGLISH);
        serializer.startTag(FormulaList.XML_NS, term);
        serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_INRIGHTOFPREVIOUS,
                Boolean.toString(inRightOfPrevious));
        final List<Element> inputElements = XmlUtils.getElements(input, SM_TAG_MATH_EXPRESSION);
        parseTerm("leftTerm", inputElements, serializer, false);
        if (XmlUtils.ensureAttribute(result, "action", "numeric"))
        {
            final List<Element> resultElements = XmlUtils.getElements(result, SM_TAG_MATH_EXPRESSION);
            parseResultTerm("rightTerm", resultElements, serializer);
        }
        serializer.endTag(FormulaList.XML_NS, term);
    }

    private void parseResultTerm(String key, List<Element> elements, XmlSerializer serializer) throws Exception
    {
        final Element last = XmlUtils.removeLast(elements);
        ExpressionProperties p = new ExpressionProperties(last);
        addTextTag(key, p.isOperand() ? p.text : "", serializer);
    }

    private void addTextTag(final String key, final String s, final XmlSerializer serializer) throws Exception
    {
        serializer.startTag(FormulaList.XML_NS, FormulaList.XML_TERM_TAG);
        serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_KEY, key);
        serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_TEXT, s);
        serializer.endTag(FormulaList.XML_NS, FormulaList.XML_TERM_TAG);
    }

    /*--------------------------------------------------------*
     * Helper methods
     *--------------------------------------------------------*/

    private static final class ExpressionProperties
    {
        final String type;
        final int args;
        final String text;

        ExpressionProperties(Element e)
        {
            final String attrTmp = e.getAttribute("type");
            type = attrTmp == null ? "" : attrTmp.trim();
            int argsTmp = -1;
            try
            {
                argsTmp = Integer.parseInt(e.getAttribute("args").trim());
            }
            catch (Exception ex)
            {
                // nothing to do
            }
            args = argsTmp;
            text = e.getTextContent() == null ? "" : e.getTextContent().trim();
        }

        boolean isEqual(final String type, final int args, final String text)
        {
            return this.type.equals(type) && this.args == args && this.text.equals(text);
        }

        boolean isOperand()
        {
            return type.equals("operand");
        }

        boolean isFunction()
        {
            return type.equals("function");
        }

        boolean isArray()
        {
            return text.equals("el");
        }
    }

    private String makeFunctionName(List<Element> elements, ExpressionProperties p)
    {
        ArrayList<String> args = new ArrayList<>();
        for (int i = 0; i < p.args; i++)
        {
            final Element arg = XmlUtils.removeLast(elements);
            if (arg != null)
            {
                ExpressionProperties argProp = new ExpressionProperties(arg);
                if (argProp.isOperand())
                {
                    args.add(0, argProp.text);
                }
            }
        }
        if (p.isArray() && !args.isEmpty())
        {
            StringBuilder retValue = new StringBuilder(args.get(0) + "[");
            for (int i = 1; i < args.size(); i++)
            {
                retValue.append(i > 1 ? "," : "").append(args.get(i));
            }
            return retValue + "]";
        }
        else
        {
            StringBuilder retValue = new StringBuilder(p.text + "(");
            for (int i = 0; i < args.size(); i++)
            {
                retValue.append(i > 0 ? "," : "").append(args.get(i));
            }
            return retValue + ")";
        }
    }
}
