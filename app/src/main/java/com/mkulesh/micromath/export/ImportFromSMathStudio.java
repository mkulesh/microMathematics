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
package com.mkulesh.micromath.export;

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
import com.mkulesh.micromath.properties.DocumentProperties;
import com.mkulesh.micromath.utils.ViewUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class ImportFromSMathStudio
{
    private final String SM_TAG_REGION = "region";
    private final String SM_TAG_TEXT_FRAGMENT = "text";
    private final String SM_TAG_TEXT_FRAGMENT_TEXT = "p";
    private final String SM_TAG_MATH = "math";
    private final String SM_TAG_MATH_INPUT = "input";
    private final String SM_TAG_MATH_RESULT = "result";
    private final String SM_TAG_MATH_EXPRESSION = "e";
    private final String SM_TAG_MATH_EQUATION = ":";
    private final String SM_TAG_MATH_OPERAND = "operand";
    private final String SM_TAG_MATH_OPERATOR = "operator";
    private final String SM_TAG_MATH_FUNCTION = "function";
    private final String SM_TAG_MATH_INDEX = "el";

    private final String fileName;

    private final class CodeMapValue
    {
        final TermTypeIf termType;
        final CharSequence[] terms;

        CodeMapValue(TermTypeIf termType, CharSequence[] terms)
        {
            this.termType = termType;
            this.terms = terms;
        }

        public boolean isValidArgs(int args)
        {
            return termType == UserFunctions.FunctionType.IDENTITY || terms.length == args;
        }
    }

    private final Map<String, CodeMapValue> codeMap = new HashMap<>();

    public ImportFromSMathStudio(String fileName)
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
    }

    public StringWriter convertToMmt(InputStream stream)
    {
        ViewUtils.Debug(this, "Converting SMath Studio file " + fileName);
        try
        {
            final StringWriter writer = new StringWriter();
            final XmlSerializer serializer = Xml.newSerializer();
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.setPrefix(FormulaList.XML_PROP_MMT, FormulaList.XML_MMT_SCHEMA);
            serializer.startTag(FormulaList.XML_NS, FormulaList.XML_MAIN_TAG);
            serializer.startTag(FormulaList.XML_NS, FormulaList.XML_LIST_TAG);
            serializer.attribute(FormulaList.XML_NS, DocumentProperties.XML_PROP_VERSION, "2");
            serializer.attribute(FormulaList.XML_NS, DocumentProperties.XML_PROP_REDEFINE_ALLOWED, "true");

            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document doc = builder.parse(stream);
            final Node objects = doc.getDocumentElement();
            Element prevRegion = null;
            for (Node object = objects.getFirstChild(); object != null; object = object.getNextSibling())
            {
                if (!(object instanceof Element))
                {
                    continue;
                }
                final Element e = (Element) object;
                if (SM_TAG_REGION.equals(e.getTagName()))
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

    /*********************************************************
     * Parser methods
     *********************************************************/

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
                if (thisButton > prevTop && thisTop < prevButton)
                {
                    inRightOfPrevious = true;
                }
            }
        }
        catch (Exception ex)
        {
            // nothing to do
        }

        for (Element en : getElements(e))
        {
            if (SM_TAG_TEXT_FRAGMENT.equals(en.getTagName()))
            {
                parseTextFragment(en, inRightOfPrevious, serializer);
            }
            else if (SM_TAG_MATH.equals(en.getTagName()))
            {
                parseMathExpression(en, inRightOfPrevious, serializer);
            }
        }
    }

    private void parseTextFragment(Element e, boolean inRightOfPrevious, final XmlSerializer serializer) throws Exception
    {
        StringBuilder text = new StringBuilder();
        for (Element en : getElements(e, SM_TAG_TEXT_FRAGMENT_TEXT))
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
        serializer.startTag(FormulaList.XML_NS, FormulaList.XML_TERM_TAG);
        serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_KEY, FormulaList.XML_PROP_TEXT);
        serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_TEXT, text.toString());
        serializer.endTag(FormulaList.XML_NS, FormulaList.XML_TERM_TAG);
        serializer.endTag(FormulaList.XML_NS, term);
    }

    private void parseMathExpression(Element e, boolean inRightOfPrevious, XmlSerializer serializer) throws Exception
    {
        final List<Element> elements = getElements(e);
        final Element input = getElement(elements, SM_TAG_MATH_INPUT);
        final Element result = getElement(elements, SM_TAG_MATH_RESULT);
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
        final List<Element> elements = getElements(input, SM_TAG_MATH_EXPRESSION);
        final Element last = removeLast(elements);
        if (last == null || last.getTextContent() == null)
        {
            return;
        }
        ExpressionProperties p = new ExpressionProperties(last);
        if (!p.isEqual(SM_TAG_MATH_OPERATOR, 2, SM_TAG_MATH_EQUATION))
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

    private void parseTerm(final CharSequence key, final List<Element> elements, final XmlSerializer serializer, boolean asText) throws Exception
    {
        final Element last = removeLast(elements);
        if (last == null || last.getTextContent() == null)
        {
            return;
        }
        ExpressionProperties p = new ExpressionProperties(last);
        serializer.startTag(FormulaList.XML_NS, FormulaList.XML_TERM_TAG);
        serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_KEY, key.toString());
        CodeMapValue code = codeMap.get(p.text);
        if (p.isOperand())
        {
            // A text term
            serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_TEXT,
                    p.text.equals("#") ? "" : p.text);
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
                    ViewUtils.Debug(this, "arrayName = " + arrayName);
                    serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_CODE,
                            UserFunctions.FunctionType.FUNCTION_INDEX.getLinkObject() + "." + arrayName +
                                    UserFunctions.FUNCTION_ARGS_MARKER + Integer.toString(p.args - 1));
                    parseTermArguments(p.args - 1, elements, serializer);
                    removeLast(elements);
                }
                else
                {
                    // Array can not be resolved: convert as user function
                    serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_CODE,
                            UserFunctions.FunctionType.FUNCTION_LINK.getLinkObject() + "." + p.text +
                                    UserFunctions.FUNCTION_ARGS_MARKER + Integer.toString(p.args));
                    parseTermArguments(p.args, elements, serializer);
                }
            }
            else
            {
                // User function
                serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_CODE,
                        UserFunctions.FunctionType.FUNCTION_LINK.getLinkObject() + "." + p.text +
                                UserFunctions.FUNCTION_ARGS_MARKER + Integer.toString(p.args));
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

        final Element last = getLast(elements);
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
            parseTerm("argTerm" + Integer.toString(args - i), elements, serializer, false);
        }
    }

    private void parseNegativeTerm(final List<Element> elements, final XmlSerializer serializer) throws Exception
    {
        final Element last = getLast(elements);
        if (last == null)
        {
            return;
        }
        ExpressionProperties p = new ExpressionProperties(last);
        if (p.isOperand())
        {
            serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_TEXT, "-" + p.text);
            removeLast(elements);
        }
        else
        {
            serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_CODE,
                    Operators.OperatorType.MULT.getLowerCaseName());
            serializer.startTag(FormulaList.XML_NS, FormulaList.XML_TERM_TAG);
            serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_KEY, "leftTerm");
            serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_TEXT, "-1");
            serializer.endTag(FormulaList.XML_NS, FormulaList.XML_TERM_TAG);
            parseTerm("rightTerm", elements, serializer, false);
        }
    }

    private void parseResult(final Element input, final Element result, boolean inRightOfPrevious, final XmlSerializer serializer) throws Exception
    {
        final String term = FormulaBase.BaseType.RESULT.toString().toLowerCase(Locale.ENGLISH);
        serializer.startTag(FormulaList.XML_NS, term);
        serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_INRIGHTOFPREVIOUS,
                Boolean.toString(inRightOfPrevious));
        final List<Element> inputElements = getElements(input, SM_TAG_MATH_EXPRESSION);
        parseTerm("leftTerm", inputElements, serializer, false);
        final String resultAction = result.getAttribute("action");
        if (resultAction != null && resultAction.equals("numeric"))
        {
            final List<Element> resultElements = getElements(result, SM_TAG_MATH_EXPRESSION);
            parseResultTerm("rightTerm", resultElements, serializer);
        }
        serializer.endTag(FormulaList.XML_NS, term);
    }

    private void parseResultTerm(String key, List<Element> elements, XmlSerializer serializer) throws Exception
    {
        final Element last = removeLast(elements);
        ExpressionProperties p = new ExpressionProperties(last);
        serializer.startTag(FormulaList.XML_NS, FormulaList.XML_TERM_TAG);
        serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_KEY, key);
        serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_TEXT, p.isOperand() ? p.text : "");
        serializer.endTag(FormulaList.XML_NS, FormulaList.XML_TERM_TAG);
    }

    /*********************************************************
     * Helper methods
     *********************************************************/

    private final class ExpressionProperties
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
                argsTmp = Integer.valueOf(e.getAttribute("args").trim());
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
            return type.equals(SM_TAG_MATH_OPERAND);
        }

        boolean isFunction()
        {
            return type.equals(SM_TAG_MATH_FUNCTION);
        }

        boolean isArray()
        {
            return text.equals(SM_TAG_MATH_INDEX);
        }
    }

    private List<Element> getElements(final Element e, final String name)
    {
        List<Element> retValue = new ArrayList<>();
        for (Node object = e.getFirstChild(); object != null; object = object.getNextSibling())
        {
            if (object instanceof Element)
            {
                final Element en = (Element) object;
                if (name == null || name.equals(en.getTagName()))
                {
                    retValue.add(en);
                }
            }
        }
        return retValue;
    }

    private List<Element> getElements(final Element e)
    {
        return getElements(e, null);
    }

    private Element getElement(final List<Element> list, final String name)
    {
        for (Element en : list)
        {
            if (name.equals(en.getTagName()))
            {
                return en;
            }
        }
        return null;
    }

    private Element getLast(final List<Element> elements)
    {
        if (!elements.isEmpty())
        {
            Element e = elements.get(elements.size() - 1);
            if (e == null)
            {
                return null;
            }
            if (e.getTextContent() == null)
            {
                return null;
            }
            return e;
        }
        return null;
    }

    private Element removeLast(final List<Element> elements)
    {
        if (!elements.isEmpty())
        {
            Element e = elements.get(elements.size() - 1);
            elements.remove(elements.size() - 1);
            return e;
        }
        return null;
    }

    private String makeFunctionName(List<Element> elements, ExpressionProperties p)
    {
        ArrayList<String> args = new ArrayList<>();
        for (int i = 0; i < p.args; i++)
        {
            final Element arg = removeLast(elements);
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
            String retValue = args.get(0) + "[";
            for (int i = 1; i < args.size(); i++)
            {
                retValue += (i > 1? "," : "") + args.get(i);
            }
            return retValue + "]";
        }
        else
        {
            String retValue = p.text + "(";
            for (int i = 0; i < args.size(); i++)
            {
                retValue += (i > 0? "," : "") + args.get(i);
            }
            return retValue + ")";
        }
    }
}
