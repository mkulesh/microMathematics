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

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.Toast;

import com.mkulesh.micromath.fman.AdapterIf;
import com.mkulesh.micromath.fman.FileType;
import com.mkulesh.micromath.fman.FileUtils;
import com.mkulesh.micromath.formula.FormulaList;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.ViewUtils;

import java.io.OutputStream;

public class Exporter
{
    public final static class Parameters
    {
        public boolean skipDocumentHeader = false;
        public boolean skipImageLocale = false;
        public String imageDirectory = "";
    }

    public static boolean write(FormulaList formulas, Uri uri, FileType fileType, final AdapterIf adapter,
                                final Exporter.Parameters exportParameters)
    {
        final String fName = FileUtils.getFileName(formulas.getActivity(), uri);
        if (formulas != null && fileType != null)
        {
            final Activity activity = formulas.getActivity();
            OutputStream stream = FileUtils.getOutputStream(activity, uri);
            if (stream == null)
            {
                return false;
            }
            try
            {
                switch (fileType)
                {
                case PNG_IMAGE:
                {
                    final ExportToImage ex = new ExportToImage(formulas.getActivity(), stream, uri);
                    ex.write(formulas.getFormulaListView(), Bitmap.CompressFormat.PNG);
                    break;
                }
                case JPEG_IMAGE:
                {
                    final ExportToImage ex = new ExportToImage(formulas.getActivity(), stream, uri);
                    ex.write(formulas.getFormulaListView(), Bitmap.CompressFormat.JPEG);
                    break;
                }
                case LATEX:
                {
                    final ExportToLatex ex = new ExportToLatex(formulas.getActivity(), stream, uri, adapter,
                            exportParameters);
                    ex.write(formulas);
                    break;
                }
                case MATHJAX:
                {
                    final ExportToMathJax ex = new ExportToMathJax(formulas.getActivity(), stream, uri, adapter);
                    ex.write(formulas);
                    break;
                }
                }
                FileUtils.closeStream(stream);

                final String message = String.format(activity.getResources().getString(R.string.message_file_written),
                        fName);
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
                return true;
            }
            catch (Exception e)
            {
                final String error = String.format(activity.getResources().getString(R.string.error_file_write), fName);
                ViewUtils.Debug(activity, error + ", " + e.getLocalizedMessage());
                Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
            }
        }
        return false;
    }
}
