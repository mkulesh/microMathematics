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

import java.util.ArrayList;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.formula.FormulaBase.FocusType;
import com.mkulesh.micromath.formula.Palette.PaletteType;
import com.mkulesh.micromath.math.CalculatedValue;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.undo.FormulaState;
import com.mkulesh.micromath.utils.CompatUtils;
import com.mkulesh.micromath.utils.IdGenerator;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomLayout;
import com.mkulesh.micromath.widgets.FocusChangeIf;
import com.mkulesh.micromath.widgets.ScaledDimensions;
import com.mkulesh.micromath.widgets.TextChangeIf;

public class TermField implements TextChangeIf, FocusChangeIf, CalculatableIf
{
    /*
     * Constants used to save/restore the instance state.
     */
    private static final String STATE_TERM_ID = "_term_id";
    private static final String STATE_TEXT = "_text";
    private static final String STATE_CODE = "_code";
    private static final String STATE_INSTANCE = "_instance";

    public int MAX_LAYOUT_DEPTH = 15;
    public static final int NO_ERROR_ID = -1;

    private final FormulaBase formulaRoot, parentFormula;
    private final LinearLayout layout;
    private final CustomEditText text;
    private FormulaTerm term = null;
    private String termKey = null;
    public boolean isWritable = true;
    private boolean emptyOrAutoContent = true; // empty or automatically filled content
    private boolean textChangeDetectionEnabled = true;

    // content type and content parser
    public enum ContentType
    {
        INVALID,
        EMPTY,
        INFO_TEXT,
        TERM,
        EQUATION_NAME,
        NUMBER,
        ARGUMENT,
        VARIABLE_LINK
    }

    public enum BracketsType
    {
        NEVER,
        IFNECESSARY,
        ALWAYS
    }

    private ContentType contentType = ContentType.INVALID;
    private final TermParser parser = new TermParser();
    private Equation linkedVariable = null;

    // custom errors that can be set externally
    public enum ErrorNotification
    {
        COLOR,
        LAYOUT_BORDER,
        PARENT_LAYOUT
    }

    private String errorMsg = null;
    private ErrorNotification errorNotification = ErrorNotification.COLOR;
    private int errorId = NO_ERROR_ID;

    // public flags
    public BracketsType bracketsType = BracketsType.ALWAYS;
    public int termDepth = 0;

    /*********************************************************
     * Constructors
     *********************************************************/

    public TermField(FormulaBase formulaRoot, FormulaBase parentFormula, LinearLayout layout, int termDepth,
                     CustomEditText text)
    {
        super();

        MAX_LAYOUT_DEPTH = 25;
        if (Build.VERSION.SDK_INT < 23)
        {
            // This maximal layout depth was obtain for Lenovo P780
            MAX_LAYOUT_DEPTH = 15;
        }
        if (Build.VERSION.SDK_INT < 17)
        {
            // This maximal layout depth was obtain for Motorolla Xoom and Xtreamer Aiki
            MAX_LAYOUT_DEPTH = 9;
        }
        if (Build.VERSION.SDK_INT < 15)
        {
            // This maximal layout depth was obtain on Alcatel OT 911
            MAX_LAYOUT_DEPTH = 6;
        }

        this.formulaRoot = formulaRoot;
        this.parentFormula = parentFormula;
        this.layout = layout;
        this.termDepth = termDepth;
        termKey = text.getText().toString();
        this.text = text;
        this.text.setText("");
        this.text.setChangeIf(this, this);
        this.text.setId(IdGenerator.generateId());
        updateViewColor();
    }

    /*********************************************************
     * Methods used recursively for the formula tree
     *********************************************************/

    public void collectElemets(LinearLayout layout, ArrayList<View> out)
    {
        if (isTerm() && layout == this.layout)
        {
            term.collectElemets(layout, out);
        }
    }

    /**
     * Procedure checks term field content and sets the corresponding content type
     */
    public ContentType checkContentType()
    {
        errorMsg = null;
        errorNotification = ErrorNotification.COLOR;
        errorId = NO_ERROR_ID;
        linkedVariable = null;
        if (text.isTextFragment() || text.isCalculatedValue())
        {
            contentType = ContentType.INFO_TEXT;
            updateViewColor();
            return contentType;
        }
        if (text.isEmptyEnabled() && isEmpty())
        {
            contentType = ContentType.EMPTY;
            updateViewColor();
            return contentType;
        }
        if (isTerm())
        {
            contentType = (term.isContentValid(FormulaBase.ValidationPassType.VALIDATE_SINGLE_FORMULA)) ? ContentType.TERM
                    : ContentType.INVALID;
            updateViewColor();
            return contentType;
        }
        contentType = ContentType.INVALID;
        parser.setText(this, formulaRoot, text);
        if (text.isEquationName())
        {
            // in this mode, only a name is allowed and shall be unique
            if (parser.getFunctionName() != null && parser.errorId == NO_ERROR_ID)
            {
                contentType = ContentType.EQUATION_NAME;
            }
        }
        else
        {
            // in this mode, numbers and function pointers are allowed
            if (parser.getValue() != null)
            {
                contentType = ContentType.NUMBER;
            }
            else if (parser.getArgumentHolder() != null && parser.getArgumentIndex() != ViewUtils.INVALID_INDEX)
            {
                contentType = ContentType.ARGUMENT;
            }
            else if (parser.getLinkedVariableId() >= 0)
            {
                final FormulaBase lv = formulaRoot.getFormulaList().getFormula(parser.getLinkedVariableId());
                if (lv != null && lv instanceof Equation)
                {
                    contentType = ContentType.VARIABLE_LINK;
                    linkedVariable = (Equation) lv;
                    if (formulaRoot instanceof LinkHolder)
                    {
                        ((LinkHolder) formulaRoot).addLinkedEquation(linkedVariable);
                    }
                }
            }
        }
        updateViewColor();
        return contentType;
    }

    /**
     * Procedure returns true if the calculation and content checking shall be skipped for this formula
     */
    boolean disableCalculation()
    {
        return (getFormulaRoot() instanceof CalculationResult)
                && ((CalculationResult) getFormulaRoot()).disableCalculation();
    }

    /**
     * Procedure calculates recursively the formula value
     */
    public CalculatedValue.ValueType getValue(CalculaterTask thread, CalculatedValue outValue) throws CancelException
    {
        if (thread != null)
        {
            thread.checkCancelation();
        }
        if (isTerm())
        {
            return term.getValue(thread, outValue);
        }
        else
        {
            switch (contentType)
            {
            case NUMBER:
                return outValue.assign(parser.getValue());
            case ARGUMENT:
                outValue.assign(parser.getArgumentHolder().getArgumentValue(parser.getArgumentIndex()));
                return outValue.multiply(parser.getSign());
            case VARIABLE_LINK:
                if (linkedVariable.isInterval())
                {
                    outValue.assign(linkedVariable.getArgumentValue(0));
                }
                else
                {
                    linkedVariable.getValue(thread, outValue);
                }
                return outValue.multiply(parser.getSign());
            default:
                return outValue.invalidate(CalculatedValue.ErrorType.TERM_NOT_READY);
            }
        }
    }

    /**
     * Procedure checks whether this term holds a differentiable equation with respect to given variable name
     */
    public DifferentiableType isDifferentiable(String var)
    {
        if (isTerm())
        {
            return term.isDifferentiable(var);
        }
        if (contentType == ContentType.ARGUMENT && parser.isArgumentInHolder(var))
        {
            return DifferentiableType.ANALYTICAL;
        }
        return DifferentiableType.INDEPENDENT;
    }

    /**
     * Procedure calculates recursively the derivative value
     */
    public CalculatedValue.ValueType getDerivativeValue(String var, CalculaterTask thread, CalculatedValue outValue)
            throws CancelException
    {
        if (isTerm())
        {
            return term.getDerivativeValue(var, thread, outValue);
        }
        if (contentType == ContentType.ARGUMENT && parser.isArgumentInHolder(var))
        {
            return outValue.setValue(parser.getSign());
        }
        return outValue.setValue(0.0);
    }

    /**
     * Procedure searches the focused term recursively
     */
    public TermField findFocusedTerm()
    {
        if (isTerm())
        {
            if (term == formulaRoot.getFormulaList().getSelectedTerm())
            {
                return this;
            }
            return term.findFocusedTerm();
        }
        return (text.isFocused() ? this : null);
    }

    /**
     * Procedure sets the focused term recursively
     */
    public boolean setEditableFocus(FocusType type)
    {
        if (isTerm())
        {
            return term.setEditableFocus(type);
        }
        else if (type == FormulaBase.FocusType.FIRST_EDITABLE || isEmpty())
        {
            text.requestFocus();
            formulaRoot.getFormulaList().showSoftKeyboard(true);
            return true;
        }
        return false;
    }

    /**
     * Procedure updates the text size of this term depending on layout depth
     */
    public void updateTextSize()
    {
        if (isTerm())
        {
            term.updateTextSize();
        }
        else
        {
            text.updateTextSize(formulaRoot.getFormulaList().getDimen(), termDepth,
                    ScaledDimensions.Type.HOR_TEXT_PADDING);
        }
    }

    /**
     * Procedure updates the text color of this term depending on layout depth
     */
    public void updateTextColor()
    {
        if (isTerm())
        {
            term.updateTextColor();
        }
        else
        {
            updateViewColor();
        }
    }

    /*********************************************************
     * Implementation of TextChangeIf interface
     *********************************************************/

    @Override
    public void beforeTextChanged(String s, boolean isManualInput)
    {
        if (isManualInput)
        {
            if (text.isNewTermEnabled())
            {
                formulaRoot.getFormulaList().getUndoState().addEntry(parentFormula.getState());
            }
            else
            {
                formulaRoot.getFormulaList().getUndoState().addEntry(getState());
            }
        }
    }

    @Override
    public void onTextChanged(String s, boolean isManualInput)
    {
        boolean converted = false;
        final boolean isEmpty = (s == null || s.length() == 0);
        if (textChangeDetectionEnabled)
        {
            emptyOrAutoContent = isEmpty;
        }
        if (!isEmpty && text.isConversionEnabled())
        {
            term = convertToTerm(s, null, FormulaTermFunction.isConversionEnabled(getContext(), s));
            if (term != null)
            {
                converted = true;
                requestFocus();
            }
        }
        if (!isEmpty && !converted && text.isNewTermEnabled())
        {
            if (parentFormula.onNewTerm(this, s, true))
            {
                return;
            }
        }
        if (!isTerm())
        {
            // Do not call isContentValid since it deletes content of edit text
            // that causes unlimited recursive call of onTextChanged
            checkContentType();
        }
    }

    @Override
    public void onSizeChanged()
    {
        if (!isTerm() && text.isFocused())
        {
            formulaRoot.getFormulaList().getFormulaScrollView().scrollToChild(text);
        }
    }

    @Override
    public int onGetNextFocusId(CustomEditText owner, FocusChangeIf.NextFocusType focusType)
    {
        return parentFormula.getNextFocusId(text, focusType);
    }

    /*********************************************************
     * Read/write interface
     *********************************************************/

    /**
     * Parcelable interface: procedure writes the formula state
     */
    public void writeToBundle(Bundle bundle, String pref)
    {
        bundle.putInt(pref + STATE_TERM_ID, getTermId());
        if (!isTerm())
        {
            bundle.putString(pref + STATE_TEXT, isEmptyOrAutoContent() ? "" : getText());
            bundle.putString(pref + STATE_CODE, "");
        }
        else
        {
            bundle.putString(pref + STATE_TEXT, "");
            bundle.putString(pref + STATE_CODE, term.getTermCode());
            bundle.putParcelable(pref + STATE_INSTANCE, term.onSaveInstanceState());
        }
    }

    /**
     * Parcelable interface: procedure reads the formula state
     */
    public void readFromBundle(Bundle bundle, String pref)
    {
        if (IdGenerator.enableIdRestore)
        {
            text.setId(bundle.getInt(pref + STATE_TERM_ID));
            IdGenerator.compareAndSet(getTermId());
        }
        setText(bundle.getString(pref + STATE_TEXT));
        final String termCode = bundle.getString(pref + STATE_CODE);
        if (termCode != null && termCode.length() > 0)
        {
            term = convertToTerm(termCode, bundle.getParcelable(pref + STATE_INSTANCE), true);
        }
    }

    /**
     * XML interface: procedure reads the formula state
     */
    public void readFromXml(XmlPullParser parser) throws Exception
    {
        final String text = parser.getAttributeValue(null, FormulaList.XML_PROP_TEXT);
        final String termCode = parser.getAttributeValue(null, FormulaList.XML_PROP_CODE);
        parser.require(XmlPullParser.START_TAG, FormulaList.XML_NS, parser.getName());
        boolean finishTag = true;
        if (termCode == null)
        {
            setText(text == null ? "" : text);
        }
        else
        {
            term = convertToTerm(termCode, null, true);
            if (isTerm())
            {
                setText("");
                term.readFromXml(parser);
                finishTag = false;
            }
            else
            {
                throw new Exception("can not create term");
            }
        }
        if (finishTag)
        {
            while (parser.next() != XmlPullParser.END_TAG);
        }
    }

    /**
     * XML interface: procedure returns string that contains XML representation of this term
     */
    public void writeToXml(XmlSerializer serializer) throws Exception
    {
        if (!isTerm())
        {
            serializer
                    .attribute(FormulaList.XML_NS, FormulaList.XML_PROP_TEXT, isEmptyOrAutoContent() ? "" : getText());
        }
        else
        {
            serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_CODE, term.getTermCode());
            term.writeToXml(serializer, getTermKey());
        }
    }

    /*********************************************************
     * Undo feature
     *********************************************************/

    /**
     * Procedure stores undo state for this term
     */
    public FormulaState getState()
    {
        Bundle bundle = new Bundle();
        writeToBundle(bundle, "");
        return new FormulaState(formulaRoot.getId(), getTermId(), bundle);
    }

    public void undo(FormulaState state)
    {
        if (state.data instanceof Bundle)
        {
            clear();
            readFromBundle((Bundle) state.data, "");
        }
    }

    /*********************************************************
     * TermField-specific methods
     *********************************************************/

    /**
     * Procedure returns the context for this term field
     */
    public Context getContext()
    {
        return formulaRoot.getFormulaList().getContext();
    }

    /**
     * Procedure returns the parent layout
     */
    public LinearLayout getLayout()
    {
        return layout;
    }

    /**
     * Procedure returns root formula for this term
     */
    public FormulaBase getFormulaRoot()
    {
        return formulaRoot;
    }

    /**
     * Procedure returns parent formula
     */
    public FormulaBase getParentFormula()
    {
        return parentFormula;
    }

    /**
     * Procedure sets given text for this term field
     */
    public void setText(CharSequence s)
    {
        if (textChangeDetectionEnabled)
        {
            // this check duplicates the same check in the onTextChanged since onTextChanged is not always called
            emptyOrAutoContent = (s == null || s.length() == 0);
        }
        text.setTextWatcherActive(false);
        if (s == null)
        {
            s = "";
        }
        text.setText(s);
        onTextChanged(s.toString(), false);
        text.setTextWatcherActive(true);
    }

    /**
     * Procedure actual text for this term field
     */
    public String getText()
    {
        return text.getText().toString();
    }

    /**
     * Procedure returns associated edit text component
     */
    public CustomEditText getEditText()
    {
        return text;
    }

    /**
     * Procedure checks whether there is a valid term
     */
    public boolean isTerm()
    {
        return term != null;
    }

    /**
     * Procedure checks whether this term is empty
     */
    public boolean isEmpty()
    {
        return !isTerm() && text.getText().length() == 0;
    }

    /**
     * Procedure returns the associated term
     */
    public FormulaTerm getTerm()
    {
        return term;
    }

    /**
     * Procedure returns the term id
     */
    public int getTermId()
    {
        return text.getId();
    }

    /**
     * Procedure returns the unique key of this term
     */
    public String getTermKey()
    {
        return termKey;
    }

    /**
     * Procedure sets the unique key of this term
     */
    public void setTermKey(String termKey)
    {
        this.termKey = termKey;
    }

    /**
     * Procedure returns the type of parsed content
     */
    public ContentType getContentType()
    {
        return contentType;
    }

    /**
     * Procedure returns associated parser
     */
    public TermParser getParser()
    {
        return parser;
    }

    /**
     * Procedure returns whether this field empty or contains automatically filled content
     */
    public boolean isEmptyOrAutoContent()
    {
        return !isTerm() && (isEmpty() || emptyOrAutoContent);
    }

    public void setTextChangeDetectionEnabled(boolean textChangeDetectionEnabled)
    {
        this.textChangeDetectionEnabled = textChangeDetectionEnabled;
    }

    /**
     * Procedure updates the border and color of edit text depends on its content
     */
    private void updateViewColor()
    {
        // flag whether an error is detected: for a formula that will be not calculated,
        // errors will be not shown
        final boolean errorDetected = disableCalculation() ? false : (errorId != NO_ERROR_ID || errorMsg != null);

        // layout border
        if (layout instanceof CustomLayout)
        {
            ((CustomLayout) layout).setContentValid(true);
            if (isTerm() && errorNotification == ErrorNotification.LAYOUT_BORDER && errorDetected)
            {
                ((CustomLayout) layout).setContentValid(false);
                return;
            }
        }

        // text border
        {
            int resId = R.drawable.formula_filled_border;
            if (text.isSelected())
            {
                resId = R.drawable.formula_selected_term;
            }
            else if (errorDetected)
            {
                resId = R.drawable.formula_invalid_content_border;
            }
            else if (isEmpty())
            {
                resId = (text.isEmptyEnabled()) ? R.drawable.formula_enabled_empty_border
                        : R.drawable.formula_invalid_content_border;
            }
            CompatUtils.updateBackground(getContext(), text, resId);
        }

        // text color
        {
            int resId = R.color.formula_text_color;
            if (!disableCalculation() && contentType == ContentType.INVALID
                    && errorNotification == ErrorNotification.COLOR)
            {
                resId = R.color.formula_invalid_content_color;
            }
            else if (text.isCalculatedValue() || (!isEmpty() && isEmptyOrAutoContent()))
            {
                resId = R.color.formula_calculated_value_color;
            }
            else if (text.isTextFragment())
            {
                resId = R.color.formula_text_fragment_color;
            }
            text.setTextColor(CompatUtils.getColor(getContext(), resId));
        }

        // update minimum width depending on content
        text.updateMinimumWidth(formulaRoot.getFormulaList().getDimen());
    }

    /**
     * Procedure converts this term field to the term with given type
     */
    protected FormulaTerm convertToTerm(FormulaTerm.TermType type, String s)
    {
        FormulaTerm t = null;
        try
        {
            final int textIndex = ViewUtils.getViewIndex(layout, text); // store view index before it will be removed
            text.setTextWatcher(false);
            if (text.isFocused())
            {
                formulaRoot.getFormulaList().clearFocus();
            }
            layout.removeView(text);
            t = FormulaTerm.createTerm(type, this, layout, s, textIndex);
            t.updateTextSize();
        }
        catch (Exception ex)
        {
            ViewUtils.Debug(this, ex.getLocalizedMessage());
            layout.addView(text);
            text.setTextWatcher(true);
        }
        return t;
    }

    /**
     * Procedure check that the current formula depth has no conflicts with allowed formula depth
     */
    public boolean checkFormulaDepth()
    {
        repairTermDepth(false);
        final int layoutDepth = ViewUtils.getLayoutDepth(layout);
        return (layoutDepth <= MAX_LAYOUT_DEPTH);
    }

    /**
     * Procedure check that the term has no conflicts with allowed formula depth
     */
    private void repairTermDepth(boolean showToast)
    {
        if (isTerm() && !term.checkFormulaDepth())
        {
            clear();
            if (showToast)
            {
                Toast.makeText(getContext(), getContext().getResources().getString(R.string.error_max_layout_depth),
                        Toast.LENGTH_SHORT).show();
            }
            else
            {
                formulaRoot.getFormulaList().getFormulaListView().setTermDeleted(true);
            }
        }
    }

    /**
     * Procedure converts this term field to an other term type
     */
    protected FormulaTerm convertToTerm(String s, Parcelable p, boolean enableFunction)
    {
        term = null;
        if (FormulaTermOperator.isOperator(getContext(), s))
        {
            term = convertToTerm(FormulaTerm.TermType.OPERATOR, s);
        }
        else if (text.isComparatorEnabled() && FormulaTermComparator.isComparator(getContext(), s))
        {
            term = convertToTerm(FormulaTerm.TermType.COMPARATOR, s);
        }
        else if (enableFunction && FormulaTermFunction.isFunction(getContext(), s))
        {
            term = convertToTerm(FormulaTerm.TermType.FUNCTION, s);
        }
        else if (text.isIntervalEnabled() && FormulaTermInterval.isInterval(getContext(), s))
        {
            term = convertToTerm(FormulaTerm.TermType.INTERVAL, s);
        }
        else if (FormulaTermLoop.isLoop(getContext(), s))
        {
            term = convertToTerm(FormulaTerm.TermType.LOOP, s);
        }
        repairTermDepth(true);
        if (isTerm())
        {
            setText("");
            if (p != null)
            {
                term.onRestoreInstanceState(p);
            }
        }
        return term;
    }

    /**
     * Procedure adds the given operator code to this term
     */
    public void addOperatorCode(String code)
    {
        if (FormulaBase.BaseType.TERM.toString().equals(code.toUpperCase(Locale.ENGLISH)))
        {
            code = getContext().getResources().getString(R.string.formula_term_separator);
        }
        if (text.isNewTermEnabled())
        {
            formulaRoot.getFormulaList().getUndoState().addEntry(parentFormula.getState());
            if (parentFormula.onNewTerm(this, code, true))
            {
                return;
            }
        }

        if (FormulaTerm.getOperatorCode(getContext(), code, true) == null)
        {
            return;
        }

        formulaRoot.getFormulaList().getUndoState().addEntry(getState());

        // comparator change
        FormulaTermComparator.ComparatorType t2 = FormulaTermComparator.getComparatorType(getContext(), code);
        if (isTerm() && t2 != null)
        {
            if (getTerm() instanceof FormulaTermComparator)
            {
                if (((FormulaTermComparator) getTerm()).changeComparatorType(t2))
                {
                    return;
                }
            }
        }

        text.setRequestFocusEnabled(false);
        Bundle savedState = null;
        if (isTerm())
        {
            savedState = new Bundle();
            writeToBundle(savedState, "savedState");
            clear();
        }
        String newValue = FormulaTerm.createOperatorCode(getContext(), code, getText());
        if (newValue != null)
        {
            onTextChanged(newValue, false);
        }
        if (isTerm() && !term.getTerms().isEmpty() && savedState != null)
        {
            final TermField tf = term.getArgumentTerm();
            if (tf != null)
            {
                tf.readFromBundle(savedState, "savedState");
            }
        }
        repairTermDepth(true);
        text.setRequestFocusEnabled(true);
        requestFocus();
    }

    /**
     * Procedure reads this term field from the given bundle
     */
    public void readStoredFormula(StoredFormula s, boolean showError)
    {
        if (text.isConversionEnabled())
        {
            term = convertToTerm(s.getSingleData().termCode, s.getSingleData().data, true);
        }
        else if (showError)
        {
            final String error = formulaRoot.getFormulaList().getActivity().getResources()
                    .getString(R.string.error_paste_term_into_text);
            Toast.makeText(formulaRoot.getFormulaList().getActivity(), error, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Callback used to indicate that a child term terminates itself
     */
    public void onTermDelete(int idx, TermField remainingTerm)
    {
        formulaRoot.getFormulaList().getUndoState().addEntry(getState());
        deleteTerm(idx, remainingTerm, "");
    }

    public void onTermDeleteWithText(int idx, CharSequence newText)
    {
        formulaRoot.getFormulaList().getUndoState().addEntry(getState());
        deleteTerm(idx, null, newText);
    }

    /**
     * Procedure deletes the term and restores the edit text field
     */
    private void deleteTerm(int idx, TermField remainingTerm, CharSequence newText)
    {
        if (!isTerm())
        {
            // onTermDelete can be only called from the valid term
            return;
        }
        term = null;
        layout.addView(text, idx);
        text.setSelected(false);
        if (remainingTerm != null)
        {
            setText(remainingTerm.getText());
            if (remainingTerm.term != null)
            {
                Parcelable p = remainingTerm.term.onSaveInstanceState();
                term = convertToTerm(remainingTerm.term.getTermCode(), p, true);
            }
        }
        else
        {
            setText(newText);
        }
        if (!isTerm())
        {
            text.setTextWatcher(true);
            updateTextSize();
        }
        checkContentType();
        requestFocus();
    }

    /**
     * Procedure deletes content of this term field
     */
    public void clear()
    {
        boolean flag = text.isRequestFocusEnabled();
        text.setRequestFocusEnabled(false);
        if (isTerm())
        {
            deleteTerm(term.removeElements(), null, "");
        }
        else
        {
            setText("");
        }
        text.setRequestFocusEnabled(flag);
    }

    /**
     * If there is a parsing error, it will be shown
     */
    public void showParsingError()
    {
        final String errMsg = findErrorMsg();
        if (errMsg != null)
        {
            Toast.makeText(formulaRoot.getContext(), errMsg, Toast.LENGTH_SHORT).show();
        }
        else if (errorId != NO_ERROR_ID)
        {
            Toast.makeText(formulaRoot.getContext(), formulaRoot.getContext().getResources().getString(errorId),
                    Toast.LENGTH_SHORT).show();
        }
        else if (!isEmpty() && contentType == ContentType.INVALID && parser.errorId != NO_ERROR_ID)
        {
            Toast.makeText(formulaRoot.getContext(), formulaRoot.getContext().getResources().getString(parser.errorId),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Procedure sets the error data into the term
     */
    public void setError(String errorMsg, ErrorNotification errorNotification, CustomLayout parentLayout)
    {
        this.errorId = NO_ERROR_ID;
        this.errorMsg = errorMsg;
        this.errorNotification = errorNotification;
        if (parentLayout != null)
        {
            parentLayout.setContentValid(true);
        }
        if (errorId != NO_ERROR_ID || errorMsg != null)
        {
            contentType = ContentType.INVALID;
            if (!disableCalculation() && errorNotification == ErrorNotification.PARENT_LAYOUT)
            {
                parentLayout.setContentValid(false);
            }
        }
        updateViewColor();
    }

    public void requestFocus()
    {
        if (text.isRequestFocusEnabled())
        {
            if (!setEditableFocus(FormulaBase.FocusType.FIRST_EMPTY))
            {
                setEditableFocus(FormulaBase.FocusType.FIRST_EDITABLE);
            }
        }
    }

    /**
     * Check whether this term is enabled for the given palette
     */
    public boolean isEnabledInPalette(PaletteType pt)
    {
        if (isTerm() && term instanceof FormulaTermInterval && pt != PaletteType.NEW_TERM)
        {
            return false;
        }
        switch (pt)
        {
        case NEW_TERM:
            return parentFormula.isNewTermEnabled() && text.isNewTermEnabled();
        case UPDATE_INTERVAL:
            return text.isIntervalEnabled();
        case UPDATE_TERM:
            return text.isConversionEnabled();
        case COMPARATORS:
            return text.isComparatorEnabled();
        }
        return false;
    }

    /**
     * Check whether this term depends on given equation
     */
    public boolean dependsOn(Equation e)
    {
        if (isTerm())
        {
            return term.dependsOn(e);
        }
        else if (contentType == ContentType.VARIABLE_LINK && linkedVariable != null
                && linkedVariable instanceof Equation)
        {
            return linkedVariable.getId() == e.getId();
        }
        return false;
    }

    /**
     * Procedure search an owner argument holder that defines (holds) the given argument
     */
    public ArgumentHolderIf findArgumentHolder(String argumentName)
    {
        FormulaBase parent = getParentFormula();
        while (parent != null)
        {
            if (parent instanceof ArgumentHolderIf)
            {
                ArgumentHolderIf argHolder = (ArgumentHolderIf) parent;
                if (argHolder.getArgumentIndex(argumentName) != ViewUtils.INVALID_INDEX)
                {
                    return argHolder;
                }
            }
            if (parent.getParentField() != null)
            {
                parent = parent.getParentField().getParentFormula();
            }
            else
            {
                break;
            }
        }
        return null;
    }

    /**
     * Procedure search an externally set error in the parent terms
     */
    public String findErrorMsg()
    {
        if (errorMsg != null)
        {
            return errorMsg;
        }
        FormulaBase parent = getParentFormula();
        while (parent != null)
        {
            final TermField tf = parent.getParentField();
            if (tf != null)
            {
                if (tf.errorMsg != null)
                {
                    return tf.errorMsg;
                }
                parent = tf.getParentFormula();
            }
            else
            {
                break;
            }
        }
        return null;
    }

    /**
     * Procedure divides the given source string into two targets using the given divider
     */
    public static void divideString(String src, String div, TermField leftTarget, TermField rightTarget)
    {
        if (div != null && src != null)
        {
            int opPosition = src.indexOf(div);
            if (opPosition >= 0)
            {
                try
                {
                    leftTarget.setText(src.subSequence(0, opPosition));
                    rightTarget.setText(src.subSequence(opPosition + div.length(), src.length()));
                }
                catch (Exception ex)
                {
                    // nothing to do
                }
            }
        }
    }
}
