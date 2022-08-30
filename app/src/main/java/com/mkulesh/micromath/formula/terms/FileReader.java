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
package com.mkulesh.micromath.formula.terms;

import android.content.Context;
import android.net.Uri;

import com.mkulesh.micromath.fman.FileUtils;
import com.mkulesh.micromath.formula.Equation;
import com.mkulesh.micromath.formula.FormulaBase;
import com.mkulesh.micromath.formula.TermParser;
import com.mkulesh.micromath.math.CalculatedValue;
import com.mkulesh.micromath.properties.MatrixProperties;

import org.apache.commons.math3.complex.Complex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/*--------------------------------------------------------*
 * File reader helper class
 *--------------------------------------------------------*/

final class FileReader
{
    private final Context context;
    private final FormulaBase rootFormula;
    private final ArrayList<ArrayList<String>> fileBuffer = new ArrayList<>();
    private final MatrixProperties dim = new MatrixProperties();

    FileReader(Context context, FormulaBase rootFormula)
    {
        this.context = context;
        this.rootFormula = rootFormula;
    }

    MatrixProperties getDim()
    {
        return dim;
    }

    InputStream openStream(final String name)
    {
        if (name == null || name.length() == 0)
        {
            return null;
        }

        Uri imageUri;
        if (name.contains(FileUtils.ASSET_RESOURCE_PREFIX))
        {
            imageUri = Uri.parse(name);
        }
        else
        {
            imageUri = FileUtils.catUri(context, rootFormula.getFormulaList().getParentDirectory(), name);
        }

        if (imageUri == null)
        {
            return null;
        }

        return FileUtils.getInputStream(context, imageUri, false);
    }

    void prepare(final String name)
    {
        fileBuffer.clear();
        final InputStream fileStream = openStream(name);
        if (fileStream == null)
        {
            return;
        }

        try
        {
            BufferedReader r = new BufferedReader(new InputStreamReader(fileStream));
            String line;
            while ((line = r.readLine()) != null)
            {
                // ignore empty strings
                final String trimmedString = line.trim();
                if (trimmedString.isEmpty())
                {
                    continue;
                }
                final String[] tokens = trimmedString.split("\\s+");
                final ArrayList<String> tokenList = new ArrayList<>(tokens.length);
                for (String s : tokens)
                {
                    final String s1 = s.trim();
                    if (!s1.isEmpty())
                    {
                        tokenList.add(s1);
                    }
                }
                fileBuffer.add(tokenList);
                dim.rows = fileBuffer.size();
                dim.cols = Math.max(dim.cols, tokenList.size());
            }
        }
        catch (IOException e)
        {
            // nothing to do
        }

        FileUtils.closeStream(fileStream);
    }

    void clear()
    {
        fileBuffer.clear();
    }

    CalculatedValue.ValueType getFileElement(CalculatedValue outValue, int a0, int a1)
    {
        if (rootFormula instanceof Equation)
        {
            String strValue = null;
            if (a0 >= 0 && a0 < fileBuffer.size())
            {
                final ArrayList<String> line = fileBuffer.get(a0);
                if (a1 >= 0 && a1 < line.size())
                {
                    strValue = line.get(a1);
                }
            }
            if (strValue != null)
            {
                try
                {
                    return outValue.setValue(Double.parseDouble(strValue));
                }
                catch (Exception ex)
                {
                    // nothing to do: we will try to convert it to complex
                }
                Complex cmplValue = TermParser.complexValueOf(strValue);
                if (cmplValue != null)
                {
                    if (cmplValue.getImaginary() != 0.0)
                    {
                        return outValue.setComplexValue(cmplValue.getReal(), cmplValue.getImaginary());
                    }
                    else
                    {
                        return outValue.setValue(cmplValue.getReal());
                    }
                }
            }
            return outValue.invalidate(CalculatedValue.ErrorType.NOT_A_NUMBER);
        }
        return outValue.invalidate(CalculatedValue.ErrorType.TERM_NOT_READY);
    }
}
