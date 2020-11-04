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
package com.mkulesh.micromath.formula.terms;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.mkulesh.micromath.fman.FileUtils;
import com.mkulesh.micromath.formula.CalculatableIf;
import com.mkulesh.micromath.formula.CalculaterTask;
import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.formula.Equation;
import com.mkulesh.micromath.formula.FormulaBase;
import com.mkulesh.micromath.formula.FormulaTerm;
import com.mkulesh.micromath.formula.Palette;
import com.mkulesh.micromath.formula.PaletteButton;
import com.mkulesh.micromath.formula.TermField;
import com.mkulesh.micromath.math.CalculatedValue;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.widgets.CustomEditText;

import java.io.InputStream;
import java.util.Locale;

public class ArrayFunctions extends FunctionBase
{
    public TermTypeIf.GroupType getGroupType()
    {
        return TermTypeIf.GroupType.ARRAY_FUNCTIONS;
    }

    /**
     * Supported functions
     */
    public enum FunctionType implements TermTypeIf
    {
        READ(1, R.drawable.p_function_read, R.string.math_function_read, R.layout.formula_function_read),
        ROWS(1, R.drawable.p_function_rows, R.string.math_function_rows, R.layout.formula_function_array),
        COLS(1, R.drawable.p_function_cols, R.string.math_function_cols, R.layout.formula_function_array);

        private final int argNumber;
        private final int imageId;
        private final int descriptionId;
        private final int layoutId;
        private final String lowerCaseName;

        FunctionType(int argNumber, int imageId, int descriptionId, int layoutId)
        {
            this.argNumber = argNumber;
            this.imageId = imageId;
            this.descriptionId = descriptionId;
            this.layoutId = layoutId;
            this.lowerCaseName = name().toLowerCase(Locale.ENGLISH);
        }

        public GroupType getGroupType()
        {
            return GroupType.ARRAY_FUNCTIONS;
        }

        public int getShortCutId()
        {
            return Palette.NO_BUTTON;
        }

        int getArgNumber()
        {
            return argNumber;
        }

        public int getImageId()
        {
            return imageId;
        }

        public int getDescriptionId()
        {
            return descriptionId;
        }

        int getLayoutId()
        {
            return layoutId;
        }

        public String getLowerCaseName()
        {
            return lowerCaseName;
        }

        public int getBracketId()
        {
            return R.string.formula_function_start_bracket;
        }

        public boolean isEnabled(CustomEditText field)
        {
            return this != READ || field.isFileOperationEnabled();
        }

        public PaletteButton.Category getPaletteCategory()
        {
            return this == READ ? PaletteButton.Category.TOP_LEVEL_TERM : PaletteButton.Category.CONVERSION;
        }

        public FormulaTerm createTerm(
                TermField termField, LinearLayout layout, String s, int textIndex) throws Exception
        {
            return new ArrayFunctions(this, termField, layout, s, textIndex);
        }
    }

    /**
     * Private attributes
     */
    private TermField argTerm = null;
    private FileReader fileReader = null;
    private Equation linkedArray = null;

    /*--------------------------------------------------------*
     * Constructors
     *--------------------------------------------------------*/

    private ArrayFunctions(FunctionType type, TermField owner, LinearLayout layout, String s, int idx) throws Exception
    {
        super(owner, layout);
        termType = type;
        createGeneralFunction(getFunctionType().getLayoutId(), s, getFunctionType().getArgNumber(), idx);
        if (argTerm == null)
        {
            throw new Exception("cannot initialize function terms");
        }
    }

    /*--------------------------------------------------------*
     * GUI constructors to avoid lint warning
     *--------------------------------------------------------*/

    public ArrayFunctions(Context context)
    {
        super();
    }

    public ArrayFunctions(Context context, AttributeSet attrs)
    {
        super();
    }

    /*--------------------------------------------------------*
     * Common getters
     *--------------------------------------------------------*/

    private FunctionType getFunctionType()
    {
        return (FunctionType) termType;
    }

    /*--------------------------------------------------------*
     * Re-implementation for methods for FormulaBase and FormulaTerm superclass's
     *--------------------------------------------------------*/

    @Override
    protected String getFunctionLabel()
    {
        return termType.getLowerCaseName();
    }

    @Override
    public CalculatedValue.ValueType getValue(CalculaterTask thread, CalculatedValue outValue) throws CancelException
    {
        switch (getFunctionType())
        {
        case READ:
            if (fileReader != null)
            {
                return fileReader.getFileElement(outValue);
            }
            break;
        case ROWS:
        case COLS:
            if (linkedArray != null)
            {
                final int[] dim = linkedArray.getArrayDimensions();
                if (dim != null)
                {
                    if (dim.length == 1)
                    {
                        // linked array is a vector
                        return outValue.setValue((getFunctionType() == FunctionType.ROWS) ? dim[0] : 1);
                    }
                    else if (dim.length == 2)
                    {
                        // linked array is a matrix
                        return outValue.setValue((getFunctionType() == FunctionType.ROWS) ? dim[0] : dim[1]);
                    }
                }
            }
            break;
        }
        return outValue.invalidate(CalculatedValue.ErrorType.TERM_NOT_READY);
    }

    @Override
    public CalculatableIf.DifferentiableType isDifferentiable(String var)
    {
        return CalculatableIf.DifferentiableType.NONE;
    }

    @Override
    public CalculatedValue.ValueType getDerivativeValue(String var, CalculaterTask thread, CalculatedValue outValue)
            throws CancelException
    {
        return outValue.invalidate(CalculatedValue.ErrorType.TERM_NOT_READY);
    }

    @Override
    public boolean isContentValid(FormulaBase.ValidationPassType type)
    {
        String errorMsg = null;
        switch (type)
        {
        case VALIDATE_SINGLE_FORMULA:
            linkedArray = null;
            // Do not call directly super.isContentValid(type) since this function shall NOT register any
            // dependencies from interval (call addLinkedEquation)
            for (TermField t : terms)
            {
                if (t.checkContentType(/*registerLinkedEquation=*/false) == TermField.ContentType.INVALID)
                {
                    return false;
                }
            }
            if (getFunctionType() == FunctionType.READ)
            {
                if (fileReader == null)
                {
                    fileReader = new FileReader(getContext(), getFormulaRoot());
                }
                fileReader.clear();
                final InputStream fileStream = fileReader.openStream(argTerm.getText());
                if (fileStream == null)
                {
                    errorMsg = String.format(getContext().getResources().getString(R.string.error_file_read),
                            argTerm.getText());
                }
                else
                {
                    // ok
                    FileUtils.closeStream(fileStream);
                }
            }
            else
            {
                final Equation arrayLink = argTerm.getLinkedArray();
                if (arrayLink != null)
                {
                    // ok
                    linkedArray = arrayLink;
                }
                else
                {
                    errorMsg = String.format(getContext().getResources().getString(R.string.error_unknown_array),
                            argTerm.getText());
                }
            }
            break;
        case VALIDATE_LINKS:
            break;
        }

        if (parentField != null && functionMainLayout != null)
        {
            parentField.setError(errorMsg, TermField.ErrorNotification.PARENT_LAYOUT, functionMainLayout);
        }

        return errorMsg == null;
    }

    @Override
    protected CustomEditText initializeTerm(CustomEditText v, LinearLayout l)
    {
        if (v.getText() != null)
        {
            final String val = v.getText().toString();
            if (val.equals(getContext().getResources().getString(R.string.formula_arg_term_key)))
            {
                argTerm = addTerm(getFormulaRoot(), l, -1, v, this, 0);
                argTerm.bracketsType = TermField.BracketsType.NEVER;
            }
        }
        return v;
    }

    /*--------------------------------------------------------*
     * Methods related to file operation
     *--------------------------------------------------------*/

    public void prepareFileOperation()
    {
        if (getFunctionType() == FunctionType.READ && fileReader != null)
        {
            fileReader.prepare(argTerm.getText());
        }
    }

    public void finishFileOperation()
    {
        if (getFunctionType() == FunctionType.READ && fileReader != null)
        {
            fileReader.clear();
        }
    }

}
