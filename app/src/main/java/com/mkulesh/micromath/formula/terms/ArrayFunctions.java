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
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.mkulesh.micromath.dialogs.DialogMatrixSettings;
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
import com.mkulesh.micromath.properties.MatrixProperties;
import com.mkulesh.micromath.undo.FormulaState;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomLayout;
import com.mkulesh.micromath.widgets.CustomTextView;
import com.mkulesh.micromath.widgets.FocusChangeIf;
import com.mkulesh.micromath.widgets.MatrixLayout;

import java.io.InputStream;
import java.util.Locale;

import androidx.annotation.NonNull;

public class ArrayFunctions extends FunctionBase implements FocusChangeIf
{
    private static final String XML_PROP_ELEMENT = "element";

    public TermTypeIf.GroupType getGroupType()
    {
        return TermTypeIf.GroupType.ARRAY_FUNCTIONS;
    }

    /**
     * Supported functions
     */
    public enum FunctionType implements TermTypeIf
    {
        MATRIX(-1, R.drawable.p_function_matrix, R.string.math_function_matrix, R.layout.formula_matrix),
        READ(1, R.drawable.p_function_read, R.string.math_function_read, R.layout.formula_function_read),
        ROWS(1, R.drawable.p_function_rows, R.string.math_function_rows, R.layout.formula_function_array),
        COLS(1, R.drawable.p_function_cols, R.string.math_function_cols, R.layout.formula_function_array);

        private final int argNumber;
        private final int imageId;
        private final int descriptionId;
        private final int layoutId;
        public final String lowerCaseName;

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
            return this == MATRIX ? GroupType.USER_FUNCTIONS : GroupType.ARRAY_FUNCTIONS;
        }

        public int getShortCutId()
        {
            return this == MATRIX ? R.string.formula_array_matrix : Palette.NO_BUTTON;
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
            return this != READ || field.isArrayFunctionEnabled();
        }

        public PaletteButton.Category getPaletteCategory()
        {
            return this == MATRIX || this == READ ?
                    PaletteButton.Category.TOP_LEVEL_TERM : PaletteButton.Category.CONVERSION;
        }

        public FormulaTerm createTerm(
                TermField termField, LinearLayout layout, String text, int textIndex, Object par) throws Exception
        {
            return new ArrayFunctions(this, termField, layout, text, textIndex, par);
        }
    }

    /**
     * Private attributes
     */
    private int startIndex = 0;
    private MatrixLayout matrix = null;
    private TermField argTerm = null;
    private FileReader fileReader = null;
    private Equation linkedArray = null;

    /*--------------------------------------------------------*
     * Constructors
     *--------------------------------------------------------*/

    private ArrayFunctions(FunctionType type, TermField owner, LinearLayout layout, String s, int idx, Object par) throws Exception
    {
        super(owner, layout);
        termType = type;
        startIndex = idx;
        if (getFunctionType() == FunctionType.MATRIX)
        {
            if (par instanceof MatrixProperties)
            {
                initMatrix((MatrixProperties)par);
            }
            else
            {
                final MatrixProperties dim = new MatrixProperties();
                dim.rows = 3;
                dim.cols = 3;
                initMatrix(dim);
            }
        }
        else
        {
            createGeneralFunction(getFunctionType().getLayoutId(), s, getFunctionType().getArgNumber(), idx);
        }
        if (argTerm == null && matrix == null)
        {
            throw new Exception("cannot initialize array terms");
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

    public FunctionType getFunctionType()
    {
        return (FunctionType) termType;
    }

    public boolean isMatrix()
    {
        return getFunctionType() == FunctionType.MATRIX && matrix != null;
    }

    private boolean isFile()
    {
        return getFunctionType() == FunctionType.READ && fileReader != null;
    }

    public boolean isArray()
    {
        return isMatrix() || isFile();
    }

    public MatrixProperties getArrayDimension()
    {
        return isMatrix() ? matrix.getDim() : (isFile() ? fileReader.getDim() : null);
    }

    public MatrixLayout getMatrixLayout()
    {
        return matrix;
    }

    /*--------------------------------------------------------*
     * Implementation for methods for FocusChangeIf interface
     *--------------------------------------------------------*/

    @Override
    public int onGetNextFocusId(CustomEditText owner, NextFocusType focusType)
    {
        return getNextFocusId(owner, focusType);
    }

    /*--------------------------------------------------------*
     * Re-implementation for methods for FormulaBase and FormulaTerm superclass's
     *--------------------------------------------------------*/

    @Override
    public boolean enableObjectProperties()
    {
        return isMatrix();
    }

    @Override
    public void onObjectProperties(View owner)
    {
        if (isMatrix())
        {
            final FormulaState formulaState = getState();
            final MatrixProperties dim = new MatrixProperties();
            dim.assign(matrix.getDim());
            final DialogMatrixSettings d = new DialogMatrixSettings(getFormulaList().getActivity(), isChanged ->
            {
                getFormulaList().finishActiveActionMode();
                if (!isChanged)
                {
                    return;
                }
                if (formulaState != null)
                {
                    getFormulaList().getUndoState().addEntry(formulaState);
                }
                if (dim.rows != matrix.getDim().rows || dim.cols != matrix.getDim().cols)
                {
                    final FormulaState[][] terms = matrix.getTermState();
                    clearAllTerms();
                    initMatrix(dim);
                    matrix.setTermState(terms);
                }
                updateTextSize();
            },
                    dim);
            d.show();
        }
    }

    @Override
    public void updateTextSize()
    {
        super.updateTextSize();
        if (isMatrix())
        {
            matrix.updateTextSize(getFormulaList().getDimen());
        }
    }

    @Override
    public void updateTextColor()
    {
        super.updateTextColor();
        if (isMatrix())
        {
            matrix.updateTextColor();
        }
    }

    @Override
    public int getNextFocusId(CustomEditText owner, FocusChangeIf.NextFocusType focusType)
    {
        if (isMatrix())
        {
            return super.getNextFocusId(owner, focusType, matrix.getTerms());
        }
        return super.getNextFocusId(owner, focusType);
    }

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
        case MATRIX:
            if (isMatrix())
            {
                final TermField tf = matrix.getTerm(getFirstIndex(), getSecondIndex());
                if (tf != null)
                {
                    return tf.getValue(thread, outValue);
                }
            }
            break;
        case READ:
            if (isFile())
            {
                return fileReader.getFileElement(outValue, getFirstIndex(), getSecondIndex());
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
            if (getFunctionType() == FunctionType.MATRIX)
            {
                // no special checks currently
            }
            else if (getFunctionType() == FunctionType.READ)
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

    @Override
    protected CustomTextView initializeSymbol(CustomTextView v)
    {
        if (getFunctionType() == FunctionType.MATRIX && v.getText() != null)
        {
            String t = v.getText().toString();
            if (t.equals(getContext().getResources().getString(R.string.formula_left_bracket_key)))
            {
                v.prepare(CustomTextView.SymbolType.LEFT_SQR_BRACKET, getFormulaRoot().getFormulaList().getActivity(),
                        this);
                v.setText("."); // this text defines view width/height
            }
            else if (t.equals(getContext().getResources().getString(R.string.formula_right_bracket_key)))
            {
                v.prepare(CustomTextView.SymbolType.RIGHT_SQR_BRACKET, getFormulaRoot().getFormulaList().getActivity(),
                        this);
                v.setText("."); // this text defines view width/height
            }
        }
        else
        {
            super.initializeSymbol(v);
        }
        return v;
    }

    public Parcelable onSaveInstanceState()
    {
        final Parcelable p = super.onSaveInstanceState();
        if (isArray() && p instanceof Bundle)
        {
            ((Bundle) p).putParcelable(TermField.STATE_DIMENSION, getArrayDimension());
        }
        return p;
    }

    private void initMatrix(@NonNull final MatrixProperties dim)
    {
        inflateElements(getFunctionType().getLayoutId(), true);
        initializeElements(startIndex);
        matrix = layout.findViewWithTag(getContext().getResources().getString(R.string.formula_matrix_key));
        if (matrix != null)
        {
            matrix.resize(dim.rows, dim.cols, R.layout.formula_matrix_cell,
                (int row, int col, final CustomLayout layout, final CustomEditText text) -> {
                    final TermField tf = addTerm(getFormulaRoot(), layout, -1, text, this, 0);
                    tf.bracketsType = TermField.BracketsType.NEVER;
                    tf.setTermKey(XML_PROP_ELEMENT + "[" + row + "][" + col + "]");
                    text.setTextWatcher(false);
                    text.setChangeIf(tf, this);
                    return tf;
                },
                getFormulaList().getDimen());
        }
    }

    @Override
    public boolean isConversionDisabled()
    {
        return isArray();
    }

    /*--------------------------------------------------------*
     * Methods implementing array operations
     *--------------------------------------------------------*/

    private int getFirstIndex()
    {
        if (getFormulaRoot() instanceof Equation)
        {
            Equation eq = (Equation) getFormulaRoot();
            final int argNumber = eq.getArgumentValues() != null ? eq.getArgumentValues().length : 0;
            if (argNumber == 1 || argNumber == 2)
            {
                return eq.getArgumentValue(0).getInteger();
            }
        }
        return -1;
    }

    private int getSecondIndex()
    {
        if (getFormulaRoot() instanceof Equation)
        {
            Equation eq = (Equation) getFormulaRoot();
            final int argNumber = eq.getArgumentValues() != null ? eq.getArgumentValues().length : 0;
            if (argNumber == 1 || argNumber == 2)
            {
                return (argNumber == 1) ? 0 : eq.getArgumentValue(1).getInteger();
            }
        }
        return -1;
    }

    public void prepareFileOperation()
    {
        if (isFile())
        {
            fileReader.prepare(argTerm.getText());
        }
    }

    public void finishFileOperation()
    {
        if (isFile())
        {
            fileReader.clear();
        }
    }

}
