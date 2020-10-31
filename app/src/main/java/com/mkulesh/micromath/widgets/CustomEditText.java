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
package com.mkulesh.micromath.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.Toast;

import androidx.annotation.AttrRes;
import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;

import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.ClipboardManager;
import com.mkulesh.micromath.utils.CompatUtils;

public class CustomEditText extends AppCompatEditText implements OnLongClickListener, OnFocusChangeListener
{
    public enum ArrayType
    {
        DISABLED,
        OPTIONAL,
        MANDATORY
    }

    private AppCompatActivity activity = null;
    private TextChangeIf textChangeIf = null;
    private FocusChangeIf focusChangeIf = null;
    private final TextWatcher textWatcher = new EditTextWatcher();
    private boolean textWatcherActive = true;

    private boolean toBeDeleted = false;
    private boolean textFragment = false;
    private boolean equationName = false;
    private boolean indexName = false;
    private boolean intermediateArgument = false;
    private boolean calculatedValue = false;
    private boolean requesFocusEnabled = true;
    private boolean fileName = false;

    // custom content types
    private boolean emptyEnabled = false;
    private boolean intervalEnabled = false;
    private boolean complexEnabled = true;
    private boolean comparatorEnabled = false;
    private boolean newTermEnabled = false;
    private boolean fileOperationEnabled = false;
    private ArrayType arrayType = ArrayType.DISABLED;

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

    private void prepare(AttributeSet attrs)
    {
        menuHandler = new ContextMenuHandler(getContext());
        if (attrs != null)
        {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.CustomViewExtension, 0, 0);
            textFragment = a.getBoolean(R.styleable.CustomViewExtension_textFragment, false);
            equationName = a.getBoolean(R.styleable.CustomViewExtension_equationName, false);
            indexName = a.getBoolean(R.styleable.CustomViewExtension_indexName, false);
            intermediateArgument = a.getBoolean(R.styleable.CustomViewExtension_intermediateArgument, false);
            calculatedValue = a.getBoolean(R.styleable.CustomViewExtension_calculatedValue, false);
            fileName = a.getBoolean(R.styleable.CustomViewExtension_fileName, false);
            // custom content types
            emptyEnabled = a.getBoolean(R.styleable.CustomViewExtension_emptyEnabled, false);
            intervalEnabled = a.getBoolean(R.styleable.CustomViewExtension_intervalEnabled, false);
            complexEnabled = a.getBoolean(R.styleable.CustomViewExtension_complexEnabled, true);
            newTermEnabled = a.getBoolean(R.styleable.CustomViewExtension_newTermEnabled, false);
            fileOperationEnabled = a.getBoolean(R.styleable.CustomViewExtension_fileOperationEnabled, false);
            final int arrayTypeInt = a.getInteger(R.styleable.CustomViewExtension_arrayType, -1);
            if (arrayTypeInt >= 0 && arrayTypeInt < ArrayType.values().length)
            {
                arrayType = ArrayType.values()[arrayTypeInt];
            }

            // menu
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
        else
        {
            this.prepareFloatingActionMode();
        }
        this.setOnFocusChangeListener(this);
        setSaveEnabled(false);
    }

    private void prepareFloatingActionMode()
    {
        if (CompatUtils.isMarshMallowOrLater())
        {
            setCustomInsertionActionModeCallback(new ActionMode.Callback()
            {
                public boolean onPrepareActionMode(ActionMode mode, Menu menu)
                {
                    return true;
                }

                public void onDestroyActionMode(ActionMode mode)
                {
                    // empty
                }

                public boolean onCreateActionMode(ActionMode mode, Menu menu)
                {
                    // Call onLongClick direct if the text is empty
                    if (isTextFragment() && getText().length() == 0)
                    {
                        onLongClick(null);
                    }
                    return true;
                }

                public boolean onActionItemClicked(ActionMode mode, MenuItem item)
                {
                    return true;
                }
            });
        }
    }

    /*********************************************************
     * Custom content types
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

    public boolean isNewTermEnabled()
    {
        return newTermEnabled;
    }

    public boolean isFileOperationEnabled()
    {
        return fileOperationEnabled;
    }

    public ArrayType getArrayType()
    {
        return arrayType;
    }

    /*********************************************************
     * Interface
     *********************************************************/

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

    public boolean isConversionEnabled()
    {
        return !isEquationName() && !isIndexName() && !isIntermediateArgument() && !isTextFragment()
                && !isCalculatedValue() && !isFileName();
    }

    public void updateTextSize(ScaledDimensions dimen, int termDepth, ScaledDimensions.Type paddingType)
    {
        setTextSize(TypedValue.COMPLEX_UNIT_PX, dimen.getTextSize(termDepth));
        final int p = dimen.get(paddingType);
        setPadding(p, 0, p, 0);
        updateMinimumWidth(dimen);
    }

    public void updateMinimumWidth(ScaledDimensions dimen)
    {
        final int newWidth = length() == 0 ? dimen.get(ScaledDimensions.Type.TEXT_MIN_WIDTH) : 0;
        final int prevWidth = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ?
                getMinimumWidth() : Integer.MIN_VALUE;
        if (prevWidth != newWidth)
        {
            setMinimumWidth(newWidth);
        }
    }

    public void setRequestFocusEnabled(boolean requestFocusEnabled)
    {
        this.requesFocusEnabled = requestFocusEnabled;
    }

    public boolean isRequestFocusEnabled()
    {
        return requesFocusEnabled;
    }

    public boolean isFileName()
    {
        return fileName;
    }

    /*********************************************************
     * Painting
     *********************************************************/

    @Override
    public int getBaseline()
    {
        return ((this.getMeasuredHeight() - getPaddingBottom() + getPaddingTop()) / 2);
    }

    /*********************************************************
     * Editing
     *********************************************************/

    /**
     * Set the text watcher interface
     */
    public void setChangeIf(TextChangeIf textChangeIf, FocusChangeIf focusChangeIf)
    {
        this.textChangeIf = textChangeIf;
        this.focusChangeIf = focusChangeIf;
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
    }

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

    private boolean processDelKey(KeyEvent event)
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
        TermInputConnection(InputConnection target, boolean mutable)
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

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        if (!requesFocusEnabled)
        {
            return;
        }
        if (hasFocus && focusChangeIf != null)
        {
            setNextFocusDownId(focusChangeIf.onGetNextFocusId(this, FocusChangeIf.NextFocusType.FOCUS_DOWN));
            setNextFocusLeftId(focusChangeIf.onGetNextFocusId(this, FocusChangeIf.NextFocusType.FOCUS_LEFT));
            setNextFocusRightId(focusChangeIf.onGetNextFocusId(this, FocusChangeIf.NextFocusType.FOCUS_RIGHT));
            setNextFocusUpId(focusChangeIf.onGetNextFocusId(this, FocusChangeIf.NextFocusType.FOCUS_UP));
            setNextFocusForwardId(focusChangeIf.onGetNextFocusId(this, FocusChangeIf.NextFocusType.FOCUS_RIGHT));
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
    public androidx.appcompat.view.ActionMode getActionMode()
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
        if (!CompatUtils.isMarshMallowOrLater() && isTextFragment() && id == android.R.id.selectAll)
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

    @Override
    protected void onSelectionChanged(int selStart, int selEnd)
    {
        super.onSelectionChanged(selEnd, selEnd);
        if (CompatUtils.isMarshMallowOrLater() &&
                isTextFragment() && getText().length() > 0 &&
                hasSelection() && selEnd - selStart == getText().length())
        {
            // null for input view means that we will start ActionMode without owner:
            // the root formula will be selected instead of owner term
            this.onLongClick(null);
        }
    }

    /*********************************************************
     * Performance optimization: fast color settings
     *********************************************************/

    private int backgroundDrawableId = Integer.MIN_VALUE;
    private int backgroundAttrId = Integer.MIN_VALUE;

    public void setBackgroundAttr(@DrawableRes int drawableId, @AttrRes int attrId)
    {
        if (this.backgroundDrawableId != drawableId)
        {
            this.backgroundDrawableId = drawableId;
            setBackgroundResource(drawableId);
        }
        if (this.backgroundAttrId != attrId)
        {
            this.backgroundAttrId = attrId;
            CompatUtils.updateBackgroundAttr(getContext(), this, drawableId, attrId);
        }
    }
}
