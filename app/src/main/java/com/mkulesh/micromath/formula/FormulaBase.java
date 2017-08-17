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

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.undo.FormulaState;
import com.mkulesh.micromath.utils.ClipboardManager;
import com.mkulesh.micromath.utils.CompatUtils;
import com.mkulesh.micromath.utils.IdGenerator;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.utils.XmlUtils;
import com.mkulesh.micromath.widgets.ContextMenuHandler;
import com.mkulesh.micromath.widgets.CustomEditText;
import com.mkulesh.micromath.widgets.CustomLayout;
import com.mkulesh.micromath.widgets.CustomTextView;
import com.mkulesh.micromath.widgets.FormulaChangeIf;
import com.mkulesh.micromath.widgets.ListChangeIf;
import com.mkulesh.micromath.widgets.ScaledDimensions;
import com.mkulesh.micromath.widgets.TextChangeIf;

public abstract class FormulaBase extends CustomLayout implements FormulaChangeIf
{
    /*
     * Constants used to save/restore the instance state.
     */
    private static final String STATE_TERM = "term_";
    private static final String STATE_FORMULA_ID = "formula_id";
    private static final String STATE_INRIGHTOFPREVIOUS = "in_right_of_previous";

    public enum BaseType
    {
        EQUATION(R.drawable.p_equation, R.string.math_new_equation),
        RESULT(R.drawable.p_result, R.string.math_new_result),
        PLOT_FUNCTION(R.drawable.p_plot_function, R.string.math_new_function_plot),
        PLOT_CONTOUR(R.drawable.p_plot_contour, R.string.math_new_contour_plot),
        TEXT_FRAGMENT(R.drawable.p_text_fragment, R.string.math_new_text_fragment),
        IMAGE_FRAGMENT(R.drawable.p_image_fragment, R.string.math_new_image_fragment),
        TERM(R.drawable.p_new_term, R.string.math_new_term);

        private final int imageId;
        private final int descriptionId;

        private BaseType(int imageId, int descriptionId)
        {
            this.imageId = imageId;
            this.descriptionId = descriptionId;
        }

        public int getImageId()
        {
            return imageId;
        }

        public int getDescriptionId()
        {
            return descriptionId;
        }
    };

    public enum FocusType
    {
        FIRST_EDITABLE,
        FIRST_EMPTY
    };

    public enum ValidationPassType
    {
        VALIDATE_SINGLE_FORMULA,
        VALIDATE_LINKS
    };

    private final FormulaList formulaList;
    protected TermField parentField = null;
    protected LinearLayout layout = null;
    protected ArrayList<View> elements = new ArrayList<View>();
    protected ArrayList<TermField> terms = new ArrayList<TermField>();
    protected final int termDepth;
    private boolean inRightOfPrevious = false;

    /*********************************************************
     * Constructors
     *********************************************************/

    public FormulaBase(FormulaList formulaList, LinearLayout layout, int termDepth)
    {
        super(formulaList.getContext());
        this.formulaList = formulaList;
        this.layout = layout;
        this.termDepth = termDepth;
        setSaveEnabled(false);
    }

    /*********************************************************
     * Primitives
     *********************************************************/

    /**
     * Procedure checks whether this formula is a root formula
     */
    public boolean isRootFormula()
    {
        return getBaseType() != BaseType.TERM;
    }

    /**
     * Getter for parent list of formulas
     */
    public FormulaList getFormulaList()
    {
        return formulaList;
    }

    /**
     * Setter for parent formula field
     */
    public void setParentField(TermField parent)
    {
        this.parentField = parent;
    }

    /**
     * Setter for parent formula field
     */
    public TermField getParentField()
    {
        return parentField;
    }

    /**
     * Procedure returns formula terms
     */
    public ArrayList<TermField> getTerms()
    {
        return terms;
    }

    /**
     * Procedure returns whether a new term is enabled for this formula
     */
    public boolean isNewTermEnabled()
    {
        return false;
    }

    /**
     * Procedure returns whether this formula shall be placed in right of the previous formula
     */
    public boolean isInRightOfPrevious()
    {
        return inRightOfPrevious;
    }

    /**
     * Procedure sets that this formula shall be placed in right of the previous formula
     */
    public void setInRightOfPrevious(boolean inRightOfPrevious)
    {
        this.inRightOfPrevious = inRightOfPrevious;
    }

    /*********************************************************
     * Re-implementation for methods for View superclass
     *********************************************************/

    @Override
    public void setSelected(boolean isSelected)
    {
        final int selectionColor = (isSelected) ? CompatUtils.getColor(getContext(),
                R.color.formula_selected_root_color) : CompatUtils.getColor(getContext(),
                R.color.formula_background_color);
        setBackgroundColor(selectionColor);
    }

    /*********************************************************
     * Methods to be (re)implemented in derived a class
     *********************************************************/

    /**
     * Getter that returns the type of this base formula
     */
    public abstract BaseType getBaseType();

    /*********************************************************
     * Implementation for methods for FormulaChangeIf interface
     *********************************************************/

    @Override
    public void onCreateContextMenu(View owner, ContextMenuHandler handler)
    {
        // empty
    }

    @Override
    public void onFocus(View v, boolean hasFocus)
    {
        if (hasFocus)
        {
            if (!(v instanceof CustomEditText))
            {
                getFormulaList().clearFocus();
            }
            final FormulaBase eq = (this instanceof FormulaTerm) ? ((FormulaTerm) this).getFormulaRoot() : this;
            getFormulaList().setSelectedFormula(eq.getId(), false);
            if (eq.isRootFormula() && !getFormulaList().getSelectedEquations().isEmpty())
            {
                boolean isSelected = getFormulaList().getSelectedEquations().contains(eq);
                if (!isSelected)
                {
                    eq.onTermSelection(null, true, null);
                }
                else if (getFormulaList().getSelectedEquations().size() > 1)
                {
                    eq.onTermSelection(null, false, null);
                }
            }
            else
            {
                // we shall finish active action mode if focus is moved to an elements
                // that is not an owner of this mode
                boolean finishActionMode = false;
                if (v instanceof CustomEditText && ((CustomEditText) v).getActionMode() == null)
                {
                    finishActionMode = true;
                }
                else if (v instanceof CustomTextView && ((CustomTextView) v).getActionMode() == null)
                {
                    finishActionMode = true;
                }
                if (finishActionMode)
                {
                    getFormulaList().finishActiveActionMode();
                }
            }
        }
        for (TermField t : terms)
        {
            if (t.getEditText() == v)
            {
                if (hasFocus)
                {
                    t.showParsingError();
                }
                else
                {
                    getFormulaList().onManualInput();
                }
            }
        }
        getFormulaList().updatePalette();
    }

    @Override
    public void onTermSelection(View owner, boolean isSelected, ArrayList<View> list)
    {
        // list is not empty and contains a single CustomEditText if we start action mode from EditText.
        // In this case, it shall be marked as selected
        if (list == null)
        {
            list = new ArrayList<View>();
            collectElemets(layout, list);
        }

        if (isSelected)
        {
            if (owner != null)
            {
                // null owner is used in case of selection expansion
                getFormulaList().finishActiveActionMode();
            }
            if (this instanceof FormulaTerm)
            {
                getFormulaList().setSelectedFormula(((FormulaTerm) this).getFormulaRoot().getId(), false);
            }
            else
            {
                getFormulaList().setSelectedFormula(getId(), false);
            }
            if (!(owner instanceof CustomEditText))
            {
                getFormulaList().clearFocus();
            }
        }
        else
        {
            ViewUtils.collectElemets(layout, list);
        }

        for (View v : list)
        {
            if (v instanceof CustomEditText)
            {
                v.setSelected(isSelected);
            }
            else
            {
                final int resId = (isSelected) ? R.drawable.formula_selected_term : R.drawable.formula_filled_border;
                CompatUtils.updateBackground(getContext(), v, resId);
            }
        }
        if (isRootFormula())
        {
            if (isEquationOwner(owner))
            {
                if (isSelected)
                {
                    getFormulaList().selectEquation(FormulaList.SelectionMode.ADD, this);
                }
                else
                {
                    getFormulaList().selectEquation(FormulaList.SelectionMode.CLEAR, this);
                }
            }
            getFormulaList().setObjectManipulator();
        }
        getFormulaList().setSelectedTerm(isSelected && isEquationOwner(owner) ? this : null);
        getFormulaList().updatePalette();
        updateTextColor();
    }

    @Override
    public void finishActionMode(View owner)
    {
        getFormulaList().selectEquation(FormulaList.SelectionMode.CLEAR_ALL, null);
        onTermSelection(owner, false, null);
        getFormulaList().setObjectManipulator();
    }

    @Override
    public FormulaChangeIf onExpandSelection(View owner, ContextMenuHandler handler)
    {
        FormulaBase retValue = null;
        if (isRootFormula() && (owner == null || isEquationOwner(owner)))
        {
            getFormulaList().selectAll();
        }
        else if (owner != null && owner instanceof CustomEditText)
        {
            retValue = this;
        }
        else if (parentField != null)
        {
            retValue = parentField.getParentFormula();
        }
        return retValue;
    }

    @Override
    public void onCopyToClipboard()
    {
        if (getFormulaList().onCopyToClipboard())
        {
            return;
        }
        ClipboardManager.copyToClipboard(getContext(), ClipboardManager.CLIPBOARD_TERM_OBJECT);
        getFormulaList().setStoredFormula(new StoredFormula(getBaseType(), onSaveInstanceState()));
    }

    @Override
    public void onPasteFromClipboard(View owner, String content)
    {
        if (content == null)
        {
            return;
        }
        if (getFormulaList().onPasteFromClipboard(content))
        {
            return;
        }

        TermField t = null;
        final boolean pasteIntoEditText = (owner != null && owner instanceof CustomEditText);
        if (pasteIntoEditText)
        {
            // paste into text edit
            t = findTerm((CustomEditText) owner);
        }
        else if (!isRootFormula())
        {
            // paste into parent term
            t = parentField;
        }

        if (t != null)
        {
            if (content.contains(ClipboardManager.CLIPBOARD_TERM_OBJECT))
            {
                StoredFormula s = getFormulaList().getStoredFormula();
                if (s == null)
                {
                    ViewUtils.Debug(this, "can not paste: stored formula is empty");
                    return;
                }
                if (s.getContentType() != StoredFormula.ContentType.FORMULA || s.getSingleData() == null)
                {
                    ViewUtils.Debug(this, "can not paste: clipboard object is not a formula");
                    return;
                }
                if (s.getSingleData().baseType == BaseType.TERM)
                {
                    // restore TERM
                    final boolean restoreFocos = (pasteIntoEditText && ((CustomEditText) owner).isFocused());
                    if (pasteIntoEditText)
                    {
                        // we shall store the term state before operation
                        getFormulaList().getUndoState().addEntry(t.getState());
                    }
                    else if (t == parentField)
                    {
                        // onTermDelete stores the term state
                        t.onTermDelete(removeElements(), null);
                    }
                    t.readStoredFormula(s, /*showError=*/true);
                    if (restoreFocos)
                    {
                        t.setEditableFocus(FocusType.FIRST_EDITABLE);
                    }
                }
                else
                {
                    // error: cannot paste a root formula into term
                    String error = getFormulaList().getActivity().getResources()
                            .getString(R.string.error_paste_root_into_term);
                    Toast.makeText(getFormulaList().getActivity(), error, Toast.LENGTH_LONG).show();
                }
            }
            else if (content.contains(ClipboardManager.CLIPBOARD_LIST_OBJECT))
            {
                String error = getFormulaList().getActivity().getResources()
                        .getString(R.string.error_paste_root_into_term);
                Toast.makeText(getFormulaList().getActivity(), error, Toast.LENGTH_LONG).show();
            }
            else
            {
                if (pasteIntoEditText)
                {
                    // we shall store the term state before operation
                    getFormulaList().getUndoState().addEntry(t.getState());
                }
                else if (t == parentField)
                {
                    // onTermDelete stores the term state
                    t.onTermDelete(removeElements(), null);
                }
                // restore text
                t.setText(content);
            }
        }
        getFormulaList().onManualInput();
    }

    @Override
    public void onDelete(CustomEditText owner)
    {
        if (isRootFormula() && getFormulaList().deleteSelectedEquations())
        {
            getFormulaList().onManualInput();
            return;
        }
        if (parentField != null)
        {
            parentField.onTermDelete(removeElements(), null);
        }
        getFormulaList().onManualInput();
    }

    @Override
    public void onObjectProperties(View owner)
    {
        // empty
    }

    @Override
    public void onNewFormula()
    {
        // empty
    }

    @Override
    public void onDetails(View owner)
    {
        // empty
    }

    @Override
    public boolean onNewTerm(TermField field, String s, boolean requestFocus)
    {
        return false;
    }

    @Override
    public boolean enableDetails()
    {
        return false;
    }

    /*********************************************************
     * Read/write interface
     *********************************************************/

    /**
     * Parcelable interface: procedure writes the formula state
     */
    @SuppressLint("MissingSuperCall")
    public Parcelable onSaveInstanceState()
    {
        Bundle bundle = new Bundle();
        bundle.putInt(STATE_FORMULA_ID, getId());
        bundle.putBoolean(STATE_INRIGHTOFPREVIOUS, inRightOfPrevious);
        for (int i = 0; i < terms.size(); i++)
        {
            terms.get(i).writeToBundle(bundle, STATE_TERM + i);
        }
        return bundle;
    }

    /**
     * Parcelable interface: procedure reads the formula state
     */
    @SuppressLint("MissingSuperCall")
    public void onRestoreInstanceState(Parcelable state)
    {
        if (state == null)
        {
            return;
        }
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            if (IdGenerator.enableIdRestore)
            {
                setId(bundle.getInt(STATE_FORMULA_ID));
                IdGenerator.compareAndSet(getId());
            }
            inRightOfPrevious = bundle.getBoolean(STATE_INRIGHTOFPREVIOUS);
            for (int i = 0; i < terms.size(); i++)
            {
                if (terms.get(i).isWritable)
                {
                    terms.get(i).readFromBundle(bundle, STATE_TERM + i);
                }
            }
        }
    }

    /**
     * XML interface: callback on start of an xml tag reading
     */
    public boolean onStartReadXmlTag(XmlPullParser parser)
    {
        if (getBaseType().toString().equalsIgnoreCase(parser.getName()))
        {
            String attr = parser.getAttributeValue(null, FormulaList.XML_PROP_INRIGHTOFPREVIOUS);
            if (attr != null)
            {
                inRightOfPrevious = Boolean.valueOf(attr);
            }
        }
        return false;
    }

    /**
     * XML interface: procedure reads the formula state
     */
    public void readFromXml(XmlPullParser parser) throws Exception
    {
        if (onStartReadXmlTag(parser))
        {
            return;
        }
        while (parser.next() != XmlPullParser.END_TAG)
        {
            if (parser.getEventType() != XmlPullParser.START_TAG)
            {
                continue;
            }
            final String n = parser.getName();
            boolean termFound = false;
            if (onStartReadXmlTag(parser))
            {
                termFound = true;
            }
            else if (n.equalsIgnoreCase(FormulaList.XML_TERM_TAG))
            {
                final String key = parser.getAttributeValue(null, FormulaList.XML_PROP_KEY);
                for (TermField t : terms)
                {
                    if (t.getTermKey() != null && key != null && t.getTermKey().equals(key))
                    {
                        t.readFromXml(parser);
                        termFound = true;
                        break;
                    }
                }
            }
            if (!termFound)
            {
                XmlUtils.skipEntry(parser);
            }
        }
    }

    /**
     * XML interface: callback on start of an xml tag reading
     */
    public boolean onStartWriteXmlTag(XmlSerializer serializer, String key) throws Exception
    {
        if (inRightOfPrevious && getBaseType().toString().equalsIgnoreCase(serializer.getName()))
        {
            serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_INRIGHTOFPREVIOUS,
                    String.valueOf(inRightOfPrevious));
        }
        return false;
    }

    /**
     * XML interface: procedure returns string that contains XML representation of this formula
     */
    public void writeToXml(XmlSerializer serializer, String key) throws Exception
    {
        if (onStartWriteXmlTag(serializer, key))
        {
            return;
        }
        for (TermField t : terms)
        {
            serializer.startTag(FormulaList.XML_NS, FormulaList.XML_TERM_TAG);
            serializer.attribute(FormulaList.XML_NS, FormulaList.XML_PROP_KEY, t.getTermKey());
            if (onStartWriteXmlTag(serializer, t.getTermKey()))
            {
                continue;
            }
            if (t.isWritable)
            {
                t.writeToXml(serializer);
            }
            serializer.endTag(FormulaList.XML_NS, FormulaList.XML_TERM_TAG);
        }
    }

    /*********************************************************
     * Undo feature
     *********************************************************/

    /**
     * Procedure returns undo state for this formula
     */
    public FormulaState getState()
    {
        if (isRootFormula())
        {
            return new FormulaState(getId(), ViewUtils.INVALID_INDEX, onSaveInstanceState());
        }
        else if (getParentField() != null)
        {
            return getParentField().getState();
        }
        return null;
    }

    /**
     * Procedure applies the given undo state
     */
    public void undo(FormulaState state)
    {
        if (state == null)
        {
            return;
        }
        if (state.data instanceof Bundle)
        {
            for (TermField t : terms)
            {
                t.clear();
            }
            onRestoreInstanceState(state.data);
        }
    }

    /*********************************************************
     * FormulaBase-specific methods
     *********************************************************/

    /**
     * Procedure inflates layout with given resource ID
     */
    protected void inflateRootLayout(int resId, int w, int h)
    {
        layout = this;
        layout.setLayoutParams(new LayoutParams(w, h));
        final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(resId, layout);
        for (int i = 0; i < layout.getChildCount(); i++)
        {
            elements.add(layout.getChildAt(i));
        }
    }

    /**
     * Procedure inflates elements from the given resource into elements vector
     */
    protected void inflateElements(int resId, boolean removeFromLayout)
    {
        inflateElements(elements, resId, removeFromLayout);
    }

    /**
     * Procedure inflates elements from the given resource into the given vector
     */
    protected void inflateElements(ArrayList<View> out, int resId, boolean removeFromLayout)
    {
        final int lastIdx = layout.getChildCount();
        final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(resId, layout);
        for (int i = lastIdx; i < layout.getChildCount(); i++)
        {
            out.add(layout.getChildAt(i));
        }
        if (removeFromLayout)
        {
            for (View v : out)
            {
                layout.removeView(v);
            }
        }
    }

    /**
     * Procedure collects all elements that belong to the given layout into output vector
     */
    protected void collectElemets(LinearLayout layout, ArrayList<View> out)
    {
        for (View v : elements)
        {
            if ((View) v.getParent() == layout)
            {
                if (!out.contains(v))
                {
                    out.add(v);
                }
            }
        }
        for (TermField t : terms)
        {
            t.collectElemets(layout, out);
        }
    }

    /**
     * Procedure removes all elements for this term and returns the index of the first removed element
     */
    protected int removeElements()
    {
        int minIdx = layout.getChildCount() + 1;
        ArrayList<View> toRemove = new ArrayList<View>();
        collectElemets(layout, toRemove);
        for (View v : toRemove)
        {
            int idx = ViewUtils.getViewIndex(layout, v);
            if (idx >= 0)
            {
                minIdx = Math.min(minIdx, idx);
            }
            layout.removeView(v);
        }
        return minIdx;
    }

    /**
     * Procedure adds a term to the term list
     */
    protected TermField addTerm(FormulaBase formulaBase, LinearLayout l, CustomEditText editText,
            FormulaChangeIf termChangeIf, boolean addDepth)
    {
        return addTerm(formulaBase, l, -1, editText, termChangeIf, ((addDepth) ? 1 : 0));
    }

    /**
     * Procedure adds a term to the term list with given index
     */
    protected TermField addTerm(FormulaBase formulaBase, LinearLayout l, int idx, CustomEditText editText,
            FormulaChangeIf termChangeIf, int addDepth)
    {
        editText.prepare(formulaBase.getFormulaList().getActivity(), termChangeIf);
        TermField termField = new TermField(formulaBase, this, l, termDepth + addDepth, editText);
        if (idx < 0)
        {
            terms.add(termField);
        }
        else
        {
            terms.add(idx, termField);
        }
        return termField;
    }

    /**
     * Procedure check that the current formula depth has no conflicts with allowed formula depth
     */
    public boolean checkFormulaDepth()
    {
        boolean retValue = true;
        for (TermField t : terms)
        {
            if (!t.checkFormulaDepth())
            {
                retValue = false;
            }
        }
        return retValue;
    }

    /**
     * Returns term field object related to the given edit
     */
    protected TermField findTerm(CustomEditText editText)
    {
        for (TermField t : terms)
        {
            if (editText == t.getEditText())
            {
                return t;
            }
        }
        return null;
    }

    /**
     * Returns term field object with given key
     */
    public TermField findTermWithKey(int keyId)
    {
        try
        {
            String key = getContext().getResources().getString(keyId);
            for (TermField t : terms)
            {
                if (t.getTermKey() != null && t.getTermKey().equals(key))
                {
                    return t;
                }
            }
        }
        catch (Exception ex)
        {
            // nothing to do
        }
        return null;
    }

    /**
     * Returns term field object with given id
     */
    protected TermField findTermWithId(int termId)
    {
        for (TermField t : terms)
        {
            if (t.getTermId() == termId)
            {
                return t;
            }
            if (t.getTerm() != null)
            {
                TermField f = t.getTerm().findTermWithId(termId);
                if (f != null)
                {
                    return f;
                }
            }
        }
        return null;
    }

    /**
     * Procedure searches the focused term recursively
     */
    protected TermField findFocusedTerm()
    {
        for (TermField t : terms)
        {
            TermField f = t.findFocusedTerm();
            if (f != null)
            {
                return f;
            }
        }
        return null;
    }

    /**
     * Procedure checks the given owner is the main equation owner (root view)
     */
    static boolean isEquationOwner(View owner)
    {
        return !(owner instanceof CustomEditText);
    }

    /**
     * Procedure checks that all declared terms are valid
     */
    public boolean isContentValid(ValidationPassType type)
    {
        boolean isValid = true;
        switch (type)
        {
        case VALIDATE_SINGLE_FORMULA:
            // an empty root formula will be considered as valid
            if (isRootFormula() && isEmpty())
            {
                return true;
            }
            // for a non-empty formula, we check the content validity
            for (TermField t : terms)
            {
                if (t.checkContentType() == TermField.ContentType.INVALID)
                {
                    isValid = false;
                }
            }
            break;
        case VALIDATE_LINKS:
            isValid = true;
            break;
        }
        return isValid;
    }

    /**
     * Procedure checks that all declared terms are empty
     */
    public boolean isEmpty()
    {
        for (TermField t : terms)
        {
            if (!t.isEmpty())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Procedure sets the focus to the first editable element
     */
    public boolean setEditableFocus(FocusType type)
    {
        for (TermField t : terms)
        {
            if (t.setEditableFocus(type))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Procedure recursively updates the text size of the given view
     */
    private void updateTextSize(View v, ScaledDimensions dimen)
    {
        if (isRootFormula())
        {
            final int hor = dimen.get(ScaledDimensions.Type.HOR_ROOT_PADDING);
            final int vert = dimen.get(ScaledDimensions.Type.VERT_ROOT_PADDING);
            layout.setPadding(hor, vert, hor, vert);
        }
        if (v instanceof CustomTextView)
        {
            ((CustomTextView) v).updateTextSize(dimen, termDepth);
        }
        if (v instanceof CustomLayout)
        {
            ((CustomLayout) v).updateTextSize(dimen, termDepth);
        }
        if (v instanceof ViewGroup)
        {
            final ViewGroup l = (ViewGroup) v;
            for (int k = 0; k < l.getChildCount(); k++)
            {
                updateTextSize(l.getChildAt(k), dimen);
            }
        }
    }

    /**
     * Procedure updates the text size of this formula depending on layout depth
     */
    public void updateTextSize()
    {
        final ScaledDimensions dimen = getFormulaList().getDimen();
        for (View v : elements)
        {
            updateTextSize(v, dimen);
        }
        for (TermField t : terms)
        {
            t.updateTextSize();
        }
    }

    /**
     * Procedure updates the text color of this formula
     */
    public void updateTextColor()
    {
        for (TermField t : terms)
        {
            t.updateTextColor();
        }
    }

    /**
     * Procedure returns the ID of the next focused EditText relative to the given owner
     */
    public int getNextFocusId(CustomEditText owner, TextChangeIf.NextFocusType focusType)
    {
        FormulaBase f = null;
        // Process UP/DOWN
        if (isRootFormula() && owner != null)
        {
            if (focusType == TextChangeIf.NextFocusType.FOCUS_UP)
            {
                f = getFormulaList().getFormulaListView().getFormula(getId(), ListChangeIf.Position.BEFORE);
            }
            else if (focusType == TextChangeIf.NextFocusType.FOCUS_DOWN)
            {
                f = getFormulaList().getFormulaListView().getFormula(getId(), ListChangeIf.Position.AFTER);
            }
            if (f != null)
            {
                return f.getNextFocusId(null, focusType);
            }
        }
        // Process LEFT/RIGHT
        final int n = terms.size();
        int i = 0;
        for (i = 0; i < n; i++)
        {
            TermField t = terms.get(i);
            if (t.getEditText() == owner)
            {
                break;
            }
        }
        TermField t = null;
        if (i < n)
        {
            if (focusType == TextChangeIf.NextFocusType.FOCUS_LEFT && i > 0)
            {
                t = terms.get(i - 1);
            }
            else if (focusType == TextChangeIf.NextFocusType.FOCUS_RIGHT && i < n - 1)
            {
                t = terms.get(i + 1);
            }
        }
        else if (owner == null && terms.size() > 0)
        {
            if (focusType == TextChangeIf.NextFocusType.FOCUS_LEFT)
            {
                t = terms.get(n - 1);
            }
            if (focusType == TextChangeIf.NextFocusType.FOCUS_RIGHT || focusType == TextChangeIf.NextFocusType.FOCUS_UP
                    || focusType == TextChangeIf.NextFocusType.FOCUS_DOWN)
            {
                t = terms.get(0);
            }
        }
        if (t != null)
        {
            return t.isTerm() ? t.getTerm().getNextFocusId(null, focusType) : t.getTermId();
        }
        else if (parentField != null)
        {
            return parentField.onGetNextFocusId(null, focusType);
        }
        else if (isRootFormula())
        {
            if (focusType == TextChangeIf.NextFocusType.FOCUS_LEFT)
            {
                f = getFormulaList().getFormulaListView().getFormula(getId(), ListChangeIf.Position.LEFT);
            }
            else if (focusType == TextChangeIf.NextFocusType.FOCUS_RIGHT)
            {
                f = getFormulaList().getFormulaListView().getFormula(getId(), ListChangeIf.Position.RIGHT);
            }
            if (f != null)
            {
                return f.getNextFocusId(null, focusType);
            }
        }
        return R.id.main_list_view;
    }
}
