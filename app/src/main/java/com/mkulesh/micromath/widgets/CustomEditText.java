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
package com.mkulesh.micromath.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.Toast;

import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.ClipboardManager;

public class CustomEditText extends AppCompatEditText implements OnLongClickListener, OnFocusChangeListener
{
    private AppCompatActivity activity = null;
    private TextChangeIf textChangeIf = null;
    private TextWatcher textWatcher = new EditTextWatcher();
    private boolean textWatcherActive = true;

    private boolean toBeDeleted = false;
    private boolean emptyEnabled = false;
    private boolean intervalEnabled = false;
    private boolean complexEnabled = true;
    private boolean comparatorEnabled = false;
    private boolean textFragment = false;
    private boolean equationName = false;
    private boolean indexName = false;
    private boolean intermediateArgument = false;
    private boolean calculatedValue = false;
    private boolean newTermEnabled = false;
    private boolean requesFocusEnabled = true;

    // context menu handling
    private ContextMenuHandler menuHandler = null;
    private FormulaChangeIf formulaChangeIf = null;

    /*********************************************************
     * Creating
     *********************************************************/

    public CustomEditText(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        prepare(attrs);
    }

    public CustomEditText(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        prepare(attrs);
    }

    public CustomEditText(Context context)
    {
        super(context);
    }

    protected void prepare(AttributeSet attrs)
    {
        menuHandler = new ContextMenuHandler(getContext());
        if (attrs != null)
        {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomViewExtension, 0, 0);
            emptyEnabled = a.getBoolean(R.styleable.CustomViewExtension_emptyEnabled, false);
            intervalEnabled = a.getBoolean(R.styleable.CustomViewExtension_intervalEnabled, false);
            complexEnabled = a.getBoolean(R.styleable.CustomViewExtension_complexEnabled, true);
            comparatorEnabled = a.getBoolean(R.styleable.CustomViewExtension_comparatorEnabled, false);
            textFragment = a.getBoolean(R.styleable.CustomViewExtension_textFragment, false);
            equationName = a.getBoolean(R.styleable.CustomViewExtension_equationName, false);
            indexName = a.getBoolean(R.styleable.CustomViewExtension_indexName, false);
            intermediateArgument = a.getBoolean(R.styleable.CustomViewExtension_intermediateArgument, false);
            calculatedValue = a.getBoolean(R.styleable.CustomViewExtension_calculatedValue, false);
            newTermEnabled = a.getBoolean(R.styleable.CustomViewExtension_newTermEnabled, false);
            menuHandler.initialize(a);
            a.recycle();
        }
    }

    public void prepare(AppCompatActivity activity, FormulaChangeIf termChangeIf)
    {
        this.activity = activity;
        this.formulaChangeIf = termChangeIf;
        if (!textFragment)
        {
            this.setOnLongClickListener(this);
        }
        this.setOnFocusChangeListener(this);
        setSaveEnabled(false);
    }

    /*********************************************************
     * Interface
     *********************************************************/

    public boolean isEmptyEnabled()
    {
        return emptyEnabled;
    }

    public boolean isIntervalEnabled()
    {
        return intervalEnabled;
    }

    public boolean isComplexEnabled()
    {
        return complexEnabled;
    }

    public boolean isComparatorEnabled()
    {
        return comparatorEnabled;
    }

    public void setComparatorEnabled(boolean comparatorEnabled)
    {
        this.comparatorEnabled = comparatorEnabled;
    }

    public boolean isTextFragment()
    {
        return textFragment;
    }

    public boolean isEquationName()
    {
        return equationName;
    }

    public boolean isIndexName()
    {
        return indexName;
    }

    public boolean isIntermediateArgument()
    {
        return intermediateArgument;
    }

    public boolean isCalculatedValue()
    {
        return calculatedValue;
    }

    public boolean isNewTermEnabled()
    {
        return newTermEnabled;
    }

    public boolean isConversionEnabled()
    {
        return !isEquationName() && !isIndexName() && !isIntermediateArgument() && !isTextFragment()
                && !isCalculatedValue();
    }

    public void updateTextSize(ScaledDimensions dimen, int termDepth)
    {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, dimen.getTextSize(termDepth));
        final int p = dimen.get(ScaledDimensions.Type.HOR_TEXT_PADDING);
        setPadding(p, 0, p, 0);
        updateMinimumWidth(dimen);
    }

    public void updateMinimumWidth(ScaledDimensions dimen)
    {
        setMinimumWidth(length() == 0 ? dimen.get(ScaledDimensions.Type.TEXT_MIN_WIDTH) : 0);
    }

    public void setRequestFocusEnabled(boolean requestFocusEnabled)
    {
        this.requesFocusEnabled = requestFocusEnabled;
    }

    public boolean isRequestFocusEnabled()
    {
        return requesFocusEnabled;
    }

    /*********************************************************
     * Painting
     *********************************************************/

    @Override
    public int getBaseline()
    {
        return (int) ((this.getMeasuredHeight() - getPaddingBottom() + getPaddingTop()) / 2);
    };

    /*********************************************************
     * Editing
     *********************************************************/

    /**
     * Set the text watcher interface
     */
    public void setTextChangeIf(TextChangeIf textChangeIf)
    {
        this.textChangeIf = textChangeIf;
        setTextWatcher(true);
    }

    /**
     * Procedure activates/deactivates text watcher for this term field
     */
    public void setTextWatcher(boolean active)
    {
        if (active)
        {
            addTextChangedListener(textWatcher);
        }
        else
        {
            removeTextChangedListener(textWatcher);
        }
    }

    /**
     * Temporary activating/deactivating of text watcher
     */
    public void setTextWatcherActive(boolean textWatcherActive)
    {
        this.textWatcherActive = textWatcherActive;
    }

    /**
     * Text change processing class
     */
    private class EditTextWatcher implements TextWatcher
    {
        EditTextWatcher()
        {
            // empty
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {
            if (!textWatcherActive)
            {
                return;
            }
            if (textChangeIf != null)
            {
                textChangeIf.beforeTextChanged(s.toString(), true);
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            if (!textWatcherActive)
            {
                return;
            }
            if (textChangeIf != null)
            {
                textChangeIf.onTextChanged(s.toString(), true);
            }
        }

        @Override
        public void afterTextChanged(Editable s)
        {
        }
    };

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        if (textChangeIf != null)
        {
            textChangeIf.onSizeChanged();
        }
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs)
    {
        try
        {
            return new TermInputConnection(super.onCreateInputConnection(outAttrs), true);
        }
        catch (StackOverflowError ex)
        {
            // the StackOverflowError in this procedure was observed on Alcatel OT (android version 10)
            return null;
        }
    }

    public boolean processDelKey(KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_DEL
                && getText().length() == 0 && !toBeDeleted)
        {
            toBeDeleted = true;
            if (formulaChangeIf != null)
            {
                formulaChangeIf.onDelete(this);
                return true;
            }
        }
        return false;
    }

    private class TermInputConnection extends InputConnectionWrapper
    {
        public TermInputConnection(InputConnection target, boolean mutable)
        {
            super(target, mutable);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event)
        {
            if (CustomEditText.this.processDelKey(event))
            {
                return true;
            }
            return super.sendKeyEvent(event);
        }

        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength)
        {
            if (beforeLength != 0 && afterLength == 0 && CustomEditText.this.getText().length() == 0)
            {
                final KeyEvent event = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
                if (CustomEditText.this.processDelKey(event))
                {
                    return true;
                }
            }
            return super.deleteSurroundingText(beforeLength, afterLength);
        }
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event)
    {
        if (processDelKey(event))
        {
            return true;
        }
        return super.dispatchKeyEventPreIme(event);
    }

    @Override
    public boolean dispatchKeyShortcutEvent(KeyEvent event)
    {
        if (formulaChangeIf != null && event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_V)
        {
            final String input = ClipboardManager.readFromClipboard(getContext(), true);
            if (isTextFragment() && input != null)
            {
                if (ClipboardManager.isFormulaObject(input))
                {
                    final String error = getContext().getResources().getString(R.string.error_paste_term_into_text);
                    Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
                }
                else if (getText().length() > 0)
                {
                    final int start = Math.max(getSelectionStart(), 0);
                    final int end = Math.max(getSelectionEnd(), 0);
                    getText().replace(Math.min(start, end), Math.max(start, end), input, 0, input.length());
                }
                else
                {
                    setText(input);
                }
                return true;
            }
            formulaChangeIf.onPasteFromClipboard(this, input);
            return true;
        }
        return super.dispatchKeyShortcutEvent(event);
    }

    /*********************************************************
     * Navigation
     *********************************************************/

    @SuppressLint("NewApi")
    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        if (!requesFocusEnabled)
        {
            return;
        }
        if (hasFocus && textChangeIf != null)
        {
            setNextFocusDownId(textChangeIf.onGetNextFocusId(this, TextChangeIf.NextFocusType.FOCUS_DOWN));
            setNextFocusLeftId(textChangeIf.onGetNextFocusId(this, TextChangeIf.NextFocusType.FOCUS_LEFT));
            setNextFocusRightId(textChangeIf.onGetNextFocusId(this, TextChangeIf.NextFocusType.FOCUS_RIGHT));
            setNextFocusUpId(textChangeIf.onGetNextFocusId(this, TextChangeIf.NextFocusType.FOCUS_UP));
            if (Build.VERSION.SDK_INT >= 11)
            {
                setNextFocusForwardId(textChangeIf.onGetNextFocusId(this, TextChangeIf.NextFocusType.FOCUS_RIGHT));
            }
        }
        if (formulaChangeIf != null)
        {
            formulaChangeIf.onFocus(v, hasFocus);
        }
    }

    /*********************************************************
     * Context menu handling
     *********************************************************/

    /**
     * Procedure returns the parent action mode or null if there are no related mode
     */
    public android.support.v7.view.ActionMode getActionMode()
    {
        return menuHandler.getActionMode();
    }

    @Override
    public boolean onLongClick(View view)
    {
        if (formulaChangeIf == null)
        {
            return false;
        }
        if (menuHandler.getActionMode() != null)
        {
            return true;
        }
        menuHandler.startActionMode(activity, view, formulaChangeIf);
        return true;
    }

    @Override
    public boolean onTextContextMenuItem(int id)
    {
        if (isTextFragment() && id == android.R.id.selectAll)
        {
            this.selectAll();
            // null for input view means that we will start ActionMode without owner: 
            // the root formula will be selected instead of owner term
            this.onLongClick(null);
            return true;
        }
        if (isTextFragment() && id == android.R.id.paste)
        {
            final String input = ClipboardManager.readFromClipboard(getContext(), true);
            if (ClipboardManager.isFormulaObject(input))
            {
                final String error = getContext().getResources().getString(R.string.error_paste_term_into_text);
                Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
                return true;
            }
        }
        return super.onTextContextMenuItem(id);
    }
}
