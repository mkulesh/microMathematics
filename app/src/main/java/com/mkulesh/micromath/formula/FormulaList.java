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

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mkulesh.micromath.BaseFragment;
import com.mkulesh.micromath.MainActivity;
import com.mkulesh.micromath.fman.FileUtils;
import com.mkulesh.micromath.formula.StoredFormula.StoredTerm;
import com.mkulesh.micromath.plots.ImageFragment;
import com.mkulesh.micromath.plots.PlotContour;
import com.mkulesh.micromath.plots.PlotFunction;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.properties.DocumentProperties;
import com.mkulesh.micromath.properties.DocumentPropertiesChangeIf;
import com.mkulesh.micromath.properties.TextProperties;
import com.mkulesh.micromath.ta.TestSession;
import com.mkulesh.micromath.undo.Coordinate;
import com.mkulesh.micromath.undo.DeleteState;
import com.mkulesh.micromath.undo.FormulaState;
import com.mkulesh.micromath.undo.InsertState;
import com.mkulesh.micromath.undo.ReplaceState;
import com.mkulesh.micromath.undo.UndoState;
import com.mkulesh.micromath.utils.ClipboardManager;
import com.mkulesh.micromath.utils.CompatUtils;
import com.mkulesh.micromath.utils.IdGenerator;
import com.mkulesh.micromath.utils.ViewUtils;
import com.mkulesh.micromath.widgets.ListChangeIf;
import com.mkulesh.micromath.widgets.ScaledDimensions;
import com.mkulesh.micromath.widgets.TwoDScrollView;

import org.xmlpull.v1.XmlSerializer;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class FormulaList implements OnClickListener, ListChangeIf, DocumentPropertiesChangeIf
{
    /**
     * Constants used to save/restore the instance state.
     */
    private static final String STATE_FORMULA_NUMBER = "formula_number";
    private static final String STATE_FORMULA_TYPE = "formula_type_";
    private static final String STATE_FORMULA_STATE = "formula_state_";
    private static final String STATE_SELECTED_LINE = "selected_line";
    private static final String STATE_UNDO_STATE = "undo_state";

    /**
     * Constants used to write/read the XML file.
     */
    public static final String XML_NS = null;
    public static final String XML_HTTP = "http://micromath.mkulesh.com";
    public static final String XML_PROP_MMT = "mmt";
    public static final String XML_PROP_KEY = "key";
    public static final String XML_PROP_CODE = "code";
    public static final String XML_PROP_TEXT = "text";
    public static final String XML_PROP_INRIGHTOFPREVIOUS = "inRightOfPrevious";
    public static final String XML_MAIN_TAG = "micromath";
    public static final String XML_LIST_TAG = "formulaList";
    public static final String XML_TERM_TAG = "term";

    /**
     * Enumerations.
     */
    public enum SelectionMode
    {
        ADD,
        CLEAR,
        CLEAR_ALL
    }

    public enum Manipulator
    {
        PROPERTY,
        DETAILS
    }

    /**
     * Class members.
     */
    private FormulaBase selectedTerm = null;
    private final ArrayList<FormulaBase> selectedEquations = new ArrayList<FormulaBase>();

    private final BaseFragment fragment;
    private final AppCompatActivity activity;
    private final TwoDScrollView formulaScrollView;
    private final FormulaListView formulaListView;
    private final DocumentProperties documentSettings;
    private final Palette palette;
    private int selectedFormulaId = ViewUtils.INVALID_INDEX;
    private XmlLoaderTask xmlLoaderTask = null;
    private final UndoState undoState = new UndoState();
    private TestSession taSession = null;

    @SuppressLint("UseSparseArrays")
    private final HashMap<Integer, FormulaBase> formulas = new HashMap<Integer, FormulaBase>();

    /*********************************************************
     * Constructors
     *********************************************************/

    public FormulaList(BaseFragment fragment, View rootView)
    {
        super();
        this.fragment = fragment;
        this.activity = (AppCompatActivity) fragment.getActivity();

        formulaScrollView = (TwoDScrollView) rootView.findViewById(R.id.main_scroll_view);
        formulaScrollView.setScaleListener(activity, this);
        formulaScrollView.setSaveEnabled(false);
        formulaListView = new FormulaListView(fragment.getActivity(), formulaScrollView.getMainLayout());

        LinearLayout paletteView = (LinearLayout) rootView.findViewById(R.id.main_palette_view);
        palette = new Palette(getContext(), paletteView, this);
        updatePalette();

        documentSettings = new DocumentProperties(getContext());
    }

    /*********************************************************
     * Primitives
     *********************************************************/

    /**
     * Procedure return the main activity object
     */
    public AppCompatActivity getActivity()
    {
        return activity;
    }

    /**
     * Procedure return the related context
     */
    public Context getContext()
    {
        return activity;
    }

    /**
     * Getter for the main scroll view object
     */
    public TwoDScrollView getFormulaScrollView()
    {
        return formulaScrollView;
    }

    /**
     * Getter for the main list view object
     */
    public FormulaListView getFormulaListView()
    {
        return formulaListView;
    }

    /**
     * Procedure returns the formula having given id
     */
    public FormulaBase getFormula(int id)
    {
        return formulas.get(id);
    }

    /**
     * Procedure return the ID of the selected formula
     */
    public int getSelectedFormulaId()
    {
        return selectedFormulaId;
    }

    /**
     * Procedure returns actual document settings
     */
    public DocumentProperties getDocumentSettings()
    {
        return documentSettings;
    }

    /**
     * Procedure returns actual scaled dimensions
     */
    public ScaledDimensions getDimen()
    {
        return documentSettings.getScaledDimensions();
    }

    /**
     * Procedure removes focus from any focusable elements
     */
    public void clearFocus()
    {
        formulaListView.clearFocus();
    }

    /**
     * Check whether an operation that blocks the user interface is currently performed
     */
    public boolean isInOperation()
    {
        return fragment.isInOperation();
    }

    /**
     * Procedure returns the selected term
     */
    public FormulaBase getSelectedTerm()
    {
        return selectedTerm;
    }

    /**
     * Procedure sets the selected term
     */
    public void setSelectedTerm(FormulaBase selectedTerm)
    {
        this.selectedTerm = (selectedEquations.isEmpty()) ? selectedTerm : null;
    }

    /**
     * Procedure returns the list of selected equations
     */
    public ArrayList<FormulaBase> getSelectedEquations()
    {
        return selectedEquations;
    }

    public int getEquationsNumber()
    {
        return formulas.size();
    }

    public void setTaSession(TestSession autoTest)
    {
        this.taSession = autoTest;
    }

    public TestSession getTaSession()
    {
        return taSession;
    }

    /*********************************************************
     * Access to MainActivity
     *********************************************************/

    /**
     * Procedure stores given formula into the internal clipboard
     */
    public void setStoredFormula(StoredFormula term)
    {
        if (activity instanceof MainActivity)
        {
            ((MainActivity) activity).setStoredFormula(term);
        }
    }

    /**
     * Procedure return a stored formula from the internal clipboard
     */
    public StoredFormula getStoredFormula()
    {
        if (activity instanceof MainActivity)
        {
            return ((MainActivity) activity).getStoredFormula();
        }
        return null;
    }

    /**
     * Procedure enforces the currently active action mode to be finished
     */
    public void finishActiveActionMode()
    {
        if (activity instanceof MainActivity)
        {
            ((MainActivity) activity).finishActiveActionMode();
        }
    }

    /*********************************************************
     * Implementation for interfaces
     *********************************************************/

    @Override
    public void onClick(View v)
    {
        selectedFormulaId = ViewUtils.INVALID_INDEX;
        for (Map.Entry<Integer, FormulaBase> f : formulas.entrySet())
        {
            if (f == v)
            {
                setSelectedFormula(f.getValue().getId(), false);
                break;
            }
        }
    }

    @Override
    public void onNewFormula(Position position, FormulaType formulaType)
    {
        if (isInOperation())
        {
            return;
        }
        FormulaBase f = null;
        switch (formulaType)
        {
        case EQUATION:
            f = addBaseFormula(FormulaBase.BaseType.EQUATION);
            break;
        case RESULT:
            f = addBaseFormula(FormulaBase.BaseType.RESULT);
            break;
        case PLOT_FUNCTION:
            f = addBaseFormula(FormulaBase.BaseType.PLOT_FUNCTION);
            break;
        case PLOT_CONTOUR:
            f = addBaseFormula(FormulaBase.BaseType.PLOT_CONTOUR);
            break;
        case TEXT_FRAGMENT:
            f = addBaseFormula(FormulaBase.BaseType.TEXT_FRAGMENT);
            break;
        case IMAGE_FRAGMENT:
            f = addBaseFormula(FormulaBase.BaseType.IMAGE_FRAGMENT);
            break;
        default:
            break;
        }
        if (f != null)
        {
            getUndoState().addEntry(new InsertState(f.getId(), selectedFormulaId));
            formulaListView.add(f, formulas.get(selectedFormulaId), position);
            setSelectedFormula(f.getId(), true);
            f.onNewFormula();
        }
    }

    @Override
    public void onDiscardFormula(int id)
    {
        if (isInOperation())
        {
            return;
        }
        final FormulaBase f = getFormula(id);
        if (f != null)
        {
            formulaListView.clearFocus();
            DeleteState deleteState = new DeleteState();
            final FormulaBase selectedFormula = deleteFormula(f, deleteState);
            getUndoState().addEntry(deleteState);
            if (selectedFormula != null)
            {
                setSelectedFormula(selectedFormula.getId(), false);
            }
            else
            {
                setSelectedFormula(ViewUtils.INVALID_INDEX, false);
            }
        }
        onManualInput();
    }

    @Override
    public void onScale(float scaleFactor)
    {
        if (isInOperation())
        {
            return;
        }
        getDimen().setScaleFactor(scaleFactor);
        for (Map.Entry<Integer, FormulaBase> m : formulas.entrySet())
        {
            m.getValue().updateTextSize();
        }
    }

    @Override
    public void onPalettePressed(String code)
    {
        if (isInOperation())
        {
            return;
        }
        FormulaType t = null;
        try
        {
            t = FormulaType.valueOf(code.toUpperCase(Locale.ENGLISH));
        }
        catch (Exception ex)
        {
            // nothing to do
        }
        if (t != null)
        {
            // list operations
            onNewFormula(Position.AFTER, t);
        }
        else
        {
            // term operations
            FormulaBase s = formulas.get(selectedFormulaId);
            if (s != null)
            {
                TermField tf = s.findFocusedTerm();
                if (tf != null)
                {
                    if (ClipboardManager.isFormulaObject(code) && tf.getTerm() != null)
                    {
                        tf.getTerm().onPasteFromClipboard(null, code);
                    }
                    else
                    {
                        tf.addOperatorCode(code);
                    }
                }
            }
            onManualInput();
        }
        finishActiveActionMode();
    }

    @Override
    public boolean onCopyToClipboard()
    {
        if (selectedEquations.isEmpty())
        {
            return false;
        }
        ClipboardManager.copyToClipboard(getContext(), ClipboardManager.CLIPBOARD_LIST_OBJECT);
        ArrayList<FormulaBase.BaseType> types = new ArrayList<FormulaBase.BaseType>();
        ArrayList<Parcelable> data = new ArrayList<Parcelable>();
        final ArrayList<FormulaBase> fList = formulaListView.getFormulas(FormulaBase.class);
        for (FormulaBase f : fList)
        {
            if (selectedEquations.contains(f))
            {
                types.add(f.getBaseType());
                data.add(f.onSaveInstanceState());
            }
        }
        setStoredFormula(new StoredFormula(types, data));
        return true;
    }

    @Override
    public boolean onPasteFromClipboard(String content)
    {
        if (selectedEquations.isEmpty() || !content.contains(ClipboardManager.CLIPBOARD_LIST_OBJECT))
        {
            return false;
        }
        StoredFormula s = getStoredFormula();
        if (s == null)
        {
            return false;
        }
        StoredTerm[] data = s.getArrayData();
        if (s.getContentType() != StoredFormula.ContentType.LIST || data == null)
        {
            return false;
        }
        int dataIdx = 0;
        FormulaBase lastInserted = null;
        final ArrayList<FormulaBase> fList = formulaListView.getFormulas(FormulaBase.class);
        ReplaceState replaceState = new ReplaceState();
        for (int viewIdx = 0; viewIdx < fList.size(); viewIdx++)
        {
            if (dataIdx >= data.length)
            {
                break;
            }
            final FormulaBase f = fList.get(viewIdx);
            if (selectedEquations.contains(f))
            {
                lastInserted = replace(f, null, data[dataIdx].baseType, data[dataIdx].data);
                if (lastInserted != null)
                {
                    replaceState.store(lastInserted.getId(), f);
                }
                selectedEquations.remove(f);
                dataIdx++;
                if (selectedEquations.isEmpty())
                {
                    for (; dataIdx < data.length; dataIdx++)
                    {
                        lastInserted = replace(null, lastInserted, data[dataIdx].baseType, data[dataIdx].data);
                        if (lastInserted != null)
                        {
                            replaceState.store(lastInserted.getId(), null);
                        }
                    }
                }
            }
        }
        getUndoState().addEntry(replaceState);
        if (lastInserted != null)
        {
            setSelectedFormula(lastInserted.getId(), false);
        }
        onManualInput();
        return true;
    }

    @Override
    public void onManualInput()
    {
        if (isInOperation())
        {
            return;
        }
        isContentValid();
    }

    @Override
    public void onDocumentPropertiesChange(boolean isChanged)
    {
        if (documentSettings.reformat)
        {
            for (Map.Entry<Integer, FormulaBase> m : formulas.entrySet())
            {
                FormulaBase f = m.getValue();
                if (f instanceof TextFragment)
                {
                    ((TextFragment) f).format(documentSettings.textWidth);
                }
            }
        }
        if (isChanged)
        {
            calculate();
        }
    }

    /*********************************************************
     * Read/write interface
     *********************************************************/

    /**
     * Procedure reads a file from resource folder
     */
    public void readFromResource(Uri uri, XmlLoaderTask.PostAction postAction)
    {
        if (!formulas.isEmpty())
        {
            return;
        }
        InputStream is = FileUtils.getInputStream(activity, uri);
        if (is != null)
        {
            readFromStream(is, FileUtils.getFileName(activity, uri), postAction);
        }
    }

    /**
     * Parcelable interface: procedure writes the formula state
     */
    public void writeToBundle(Bundle outState)
    {
        final ArrayList<FormulaBase> fList = formulaListView.getFormulas(FormulaBase.class);
        final int n = fList.size();
        outState.putInt(STATE_FORMULA_NUMBER, n);
        int selectedLine = ViewUtils.INVALID_INDEX;
        for (int i = 0; i < n; i++)
        {
            FormulaBase f = fList.get(i);
            outState.putString(STATE_FORMULA_TYPE + i, f.getBaseType().toString());
            outState.putParcelable(STATE_FORMULA_STATE + i, f.onSaveInstanceState());
            if (f.getId() == selectedFormulaId)
            {
                selectedLine = i;
            }
        }
        outState.putInt(STATE_SELECTED_LINE, selectedLine);
        outState.putParcelable(STATE_UNDO_STATE, undoState.onSaveInstanceState());
        documentSettings.writeToBundle(outState);
    }

    /**
     * Parcelable interface: procedure reads the formula state
     */
    public void readFromBundle(Bundle inState)
    {
        clear();
        IdGenerator.enableIdRestore = true;
        final int n = inState.getInt(STATE_FORMULA_NUMBER, 0);
        final int selectedLine = inState.getInt(STATE_SELECTED_LINE, 0);
        documentSettings.readFromBundle(inState);
        FormulaBase selectedFormula = null;
        for (int i = 0; i < n; i++)
        {
            final FormulaBase.BaseType t = FormulaBase.BaseType.valueOf(inState.getString(STATE_FORMULA_TYPE + i));
            FormulaBase f = addBaseFormula(t, inState.getParcelable(STATE_FORMULA_STATE + i));
            formulaListView.add(f, null, Position.AFTER); // add to the end
            if (selectedLine == i)
            {
                selectedFormula = f;
            }
        }
        if (selectedFormula != null)
        {
            setSelectedFormula(selectedFormula.getId(), false);
        }
        undoState.onRestoreInstanceState(inState.getParcelable(STATE_UNDO_STATE));
        IdGenerator.enableIdRestore = false;
        isContentValid();
        updatePalette();
    }

    /**
     * XML interface: procedure reads this list from the given input stream
     */
    public void readFromStream(InputStream stream, String name, XmlLoaderTask.PostAction postAction)
    {
        xmlLoaderTask = new XmlLoaderTask(this, stream, name, postAction);
        ViewUtils.Debug(this, "started XML loader task: " + xmlLoaderTask.toString());
        getUndoState().clear();
        CompatUtils.executeAsyncTask(xmlLoaderTask);
    }

    /**
     * XML interface: procedure reads this list from the given file Uri
     */
    public boolean readFromFile(Uri uri)
    {
        InputStream is = FileUtils.getInputStream(activity, uri);
        if (is != null)
        {
            readFromStream(is, FileUtils.getFileName(activity, uri), XmlLoaderTask.PostAction.NONE);
            // do not close is since it will be closed by reading thread
            return true;
        }
        return false;
    }

    /**
     * XML interface: procedure writes this list into the given stream
     */
    public boolean writeToStream(OutputStream stream, String name)
    {
        try
        {
            final StringWriter writer = new StringWriter();
            final XmlSerializer serializer = Xml.newSerializer();
            serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            serializer.setOutput(writer);
            serializer.startDocument("UTF-8", true);
            serializer.setPrefix(FormulaList.XML_PROP_MMT, FormulaList.XML_HTTP);
            serializer.startTag(FormulaList.XML_NS, FormulaList.XML_MAIN_TAG);
            serializer.startTag(FormulaList.XML_NS, XML_LIST_TAG);
            documentSettings.writeToXml(serializer);
            final ArrayList<FormulaBase> fList = formulaListView.getFormulas(FormulaBase.class);
            for (FormulaBase f : fList)
            {
                final String term = f.getBaseType().toString().toLowerCase(Locale.ENGLISH);
                serializer.startTag(FormulaList.XML_NS, term);
                f.writeToXml(serializer, String.valueOf(f.getId()));
                serializer.endTag(FormulaList.XML_NS, term);
            }
            serializer.endTag(FormulaList.XML_NS, XML_LIST_TAG);
            serializer.endTag(FormulaList.XML_NS, FormulaList.XML_MAIN_TAG);
            serializer.endDocument();
            stream.write(writer.toString().getBytes());
            return true;
        }
        catch (Exception e)
        {
            final String error = String.format(activity.getResources().getString(R.string.error_file_write), name);
            Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
        }
        return false;
    }

    /**
     * XML interface: procedure writes this list into the given file Uri
     */
    public boolean writeToFile(Uri uri)
    {
        String fName = FileUtils.getFileName(activity, uri);
        try
        {
            OutputStream os = FileUtils.getOutputStream(activity, uri);
            if (os == null)
            {
                return false;
            }
            final boolean retValue = writeToStream(os, fName);
            FileUtils.closeStream(os);
            return retValue;
        }
        catch (Exception e)
        {
            final String error = String.format(activity.getResources().getString(R.string.error_file_write), fName);
            Toast.makeText(activity, error, Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /*********************************************************
     * Undo feature
     *********************************************************/

    /**
     * Procedure returns undo state container
     */
    public UndoState getUndoState()
    {
        return undoState;
    }

    /**
     * Procedure performs undo
     */
    public void undo()
    {
        final Parcelable entry = undoState.resumeLastEntry();
        if (entry == null)
        {
            return;
        }
        IdGenerator.enableIdRestore = true;
        if (entry instanceof FormulaState)
        {
            final FormulaState s = (FormulaState) entry;
            if (s != null)
            {
                final FormulaBase f = getFormula(s.formulaId);
                if (f != null && s.termId != ViewUtils.INVALID_INDEX)
                {
                    final TermField t = f.findTermWithId(s.termId);
                    if (t != null)
                    {
                        t.undo(s);
                    }
                }
                else if (f != null && s.termId == ViewUtils.INVALID_INDEX)
                {
                    f.undo(s);
                }
            }
        }
        else if (entry instanceof InsertState)
        {
            final InsertState s = (InsertState) entry;
            if (s != null)
            {
                final FormulaBase f = getFormula(s.formulaId);
                if (f != null)
                {
                    formulas.remove(f.getId());
                    formulaListView.delete(f);
                }

                setSelectedFormula(s.selectedId, false);
            }
        }
        else if (entry instanceof DeleteState)
        {
            final DeleteState ds = (DeleteState) entry;
            for (DeleteState.EntryState s : ds.getEntries())
            {
                FormulaBase f = addBaseFormula(s.type, s.data);
                if (f != null)
                {
                    formulaListView.add(f, s.coordinate);
                    setSelectedFormula(f.getId(), false);
                }
            }
        }
        else if (entry instanceof ReplaceState)
        {
            final ReplaceState rs = (ReplaceState) entry;
            int lastInsertedId = ViewUtils.INVALID_INDEX;
            for (ReplaceState.EntryState s : rs.getEntries())
            {
                final FormulaBase f = getFormula(s.formulaId);
                if (s.data != null)
                {
                    FormulaBase newFormula = replace(f, null, s.type, s.data);
                    if (newFormula != null)
                    {
                        lastInsertedId = newFormula.getId();
                    }
                }
                else if (f != null)
                {
                    formulas.remove(f.getId());
                    formulaListView.delete(f);
                }
            }
            setSelectedFormula(lastInsertedId, false);
        }
        onManualInput();
        IdGenerator.enableIdRestore = false;
    }

    /*********************************************************
     * FormulaList-specific methods
     *********************************************************/

    /**
     * Set that an operation blocking the user interface is currently performed
     */
    @SuppressWarnings("rawtypes")
    public void setInOperation(AsyncTask owner, boolean inOperation, OnClickListener stopHandler)
    {
        fragment.setInOperation(inOperation, stopHandler);
        formulaListView.setEnabled(!inOperation);
        palette.setEnabled(!inOperation);
        if (!inOperation && owner instanceof XmlLoaderTask)
        {
            XmlLoaderTask t = (XmlLoaderTask) owner;
            fragment.setXmlReadingResult(t.error == null);
            if (t.error != null)
            {
                isContentValid();
                Toast.makeText(getActivity(), t.error, Toast.LENGTH_LONG).show();
            }
            else if (t.postAction == XmlLoaderTask.PostAction.CALCULATE)
            {
                calculate();
            }
            else if (t.postAction == XmlLoaderTask.PostAction.INTERRUPT)
            {
                // nothing to do
            }
            else
            {
                isContentValid();
            }
            if (xmlLoaderTask != null)
            {
                ViewUtils.Debug(this, "terminated XML loader task: " + xmlLoaderTask.toString());
                xmlLoaderTask = null;
            }
            updatePalette();
        }
        if (taSession != null)
        {
            taSession.setInOperation(owner, inOperation);
        }
    }

    /**
     * Performs clean-up of the list
     */
    public void clear()
    {
        selectedFormulaId = ViewUtils.INVALID_INDEX;
        formulaListView.clear();
        formulas.clear();
        getDimen().reset();
        IdGenerator.reset();
    }

    /**
     * Procedure creates a formula with given type
     */
    public FormulaBase deleteFormula(FormulaBase f, DeleteState state)
    {
        Coordinate coordinate = formulaListView.getCoordinate(f);
        final int prewRowCount = formulaListView.getList().getChildCount();
        FormulaBase selectedFormula = formulaListView.delete(f);
        if (formulaListView.getList().getChildCount() != prewRowCount)
        {
            coordinate.col = ViewUtils.INVALID_INDEX;
        }
        state.store(f, coordinate);
        formulas.remove(f.getId());
        return selectedFormula;
    }

    /**
     * Procedure creates a formula with given type
     */
    public FormulaBase addBaseFormula(FormulaBase.BaseType type)
    {
        return addBaseFormula(type, null);
    }

    /**
     * Procedure creates a formula with given type and given stored date
     */
    public FormulaBase addBaseFormula(FormulaBase.BaseType type, Parcelable p)
    {
        FormulaBase f = createFormula(type);
        if (f != null)
        {
            f.onRestoreInstanceState(p);
            f.updateTextSize();
            formulas.put(f.getId(), f);
        }
        return f;
    }

    /**
     * Procedure creates a formula with given type
     */
    private FormulaBase createFormula(FormulaBase.BaseType type)
    {
        FormulaBase f = null;
        final int id = IdGenerator.generateId();
        switch (type)
        {
        case EQUATION:
            f = new Equation(this, id);
            break;
        case RESULT:
            f = new FormulaResult(this, id);
            break;
        case PLOT_FUNCTION:
            f = new PlotFunction(this, id);
            break;
        case PLOT_CONTOUR:
            f = new PlotContour(this, id);
            break;
        case TEXT_FRAGMENT:
            f = new TextFragment(this, id);
            break;
        case IMAGE_FRAGMENT:
            f = new ImageFragment(this, id);
            break;
        case TERM:
            break;
        }
        if (f != null)
        {
            f.setOnClickListener(this);
        }
        return f;
    }

    /**
     * Procedure searches a root formula with given properties
     */
    public FormulaBase getFormula(String name, int argNumber, int rootId, boolean excludeRoot)
    {
        if (name == null)
        {
            return null;
        }
        return getFormulaListView().getFormula(name, argNumber, rootId, excludeRoot, !documentSettings.redefineAllowed);
    }

    /**
     * Procedure sets the formula with given ID as selected
     */
    public void setSelectedFormula(int id, boolean setFocus)
    {
        if (selectedFormulaId == id && id != ViewUtils.INVALID_INDEX)
        {
            return;
        }
        selectedFormulaId = id;
        for (Map.Entry<Integer, FormulaBase> m : formulas.entrySet())
        {
            FormulaBase f = m.getValue();
            if (f.getId() == selectedFormulaId)
            {
                f.setSelected(true);
                if (setFocus)
                {
                    f.setEditableFocus(FormulaBase.FocusType.FIRST_EDITABLE);
                }
            }
            else
            {
                f.setSelected(false);
            }
        }
    }

    /**
     * Procedure marks all equations as selected
     */
    public void selectAll()
    {
        for (Map.Entry<Integer, FormulaBase> m : formulas.entrySet())
        {
            if (selectedEquations.contains(m.getValue()))
            {
                continue;
            }
            m.getValue().onTermSelection(null, true, null);
        }
        fragment.updateModeTitle();
    }

    /**
     * Procedure sets the selected term or equation
     */
    public void selectEquation(SelectionMode mode, FormulaBase f)
    {
        switch (mode)
        {
        case ADD:
            if (f != null && f.isRootFormula() && !selectedEquations.contains(f))
            {
                selectedEquations.add(f);
            }
            break;
        case CLEAR:
            if (f != null && f.isRootFormula() && selectedEquations.contains(f))
            {
                selectedEquations.remove(f);
            }
            break;
        case CLEAR_ALL:
            if (!selectedEquations.isEmpty())
            {
                ArrayList<FormulaBase> toBeCleared = new ArrayList<FormulaBase>();
                for (FormulaBase e : selectedEquations)
                {
                    toBeCleared.add(e);
                }
                selectedEquations.clear();
                for (FormulaBase e : toBeCleared)
                {
                    e.onTermSelection(null, false, null);
                }
            }
            setSelectedTerm(null);
            break;
        }
        fragment.updateModeTitle();
    }

    /**
     * Procedure deletes all equations stored within the selectedEquations vector
     */
    public boolean deleteSelectedEquations()
    {
        if (selectedEquations.isEmpty())
        {
            return false;
        }
        formulaListView.clearFocus();
        // search for the last formula before first deleted that will still in the view
        int selectedFormulaId = ViewUtils.INVALID_INDEX;
        final ArrayList<FormulaBase> fList = formulaListView.getFormulas(FormulaBase.class);
        if (selectedEquations.size() < fList.size())
        {
            boolean equationFound = false;
            for (int i = fList.size() - 1; i >= 0; i--)
            {
                final FormulaBase f = fList.get(i);
                if (selectedEquations.contains(f))
                {
                    equationFound = true;
                    if (selectedFormulaId != ViewUtils.INVALID_INDEX)
                    {
                        break;
                    }
                }
                else
                {
                    selectedFormulaId = f.getId();
                    if (equationFound)
                    {
                        break;
                    }
                }
            }
        }
        // delete all selected formulas
        DeleteState deleteState = new DeleteState();
        for (int i = fList.size() - 1; i >= 0; i--)
        {
            final FormulaBase f = fList.get(i);
            if (selectedEquations.contains(f))
            {
                selectedEquations.remove(f);
                fList.remove(i);
                deleteFormula(f, deleteState);
            }
        }
        getUndoState().addEntry(deleteState);
        selectedEquations.clear();
        // restore focus
        setSelectedFormula(selectedFormulaId, false);
        return true;
    }

    /**
     * The given formula will be replaced by given stored term object
     */
    private FormulaBase replace(FormulaBase oldFormula, FormulaBase afterThis, FormulaBase.BaseType type,
                                Parcelable data)
    {
        if (data == null)
        {
            return null;
        }
        if (oldFormula != null)
        {
            formulas.remove(oldFormula.getId());
        }
        FormulaBase newFormula = createFormula(type);
        if (newFormula != null)
        {
            if (!formulaListView.replace(oldFormula, newFormula))
            {
                newFormula.onRestoreInstanceState(data);
                final Position pos = (newFormula.isInRightOfPrevious() ? Position.RIGHT : Position.AFTER);
                formulaListView.add(newFormula, afterThis, pos);
            }
            else
            {
                final boolean inRightOfPrevious = newFormula.isInRightOfPrevious();
                newFormula.onRestoreInstanceState(data);
                newFormula.setInRightOfPrevious(inRightOfPrevious);
            }
            newFormula.updateTextSize();
            formulas.put(newFormula.getId(), newFormula);
        }
        return newFormula;
    }

    /**
     * Procedure performs validity check for all formulas
     */
    private boolean isContentValid()
    {
        boolean isValid = true;
        final ArrayList<FormulaBase> fList = formulaListView.getFormulas(FormulaBase.class);
        ArrayList<Integer> invalidFormulas = new ArrayList<Integer>();
        // first pass - validate single formulas
        for (FormulaBase m : fList)
        {
            if (!m.isContentValid(FormulaBase.ValidationPassType.VALIDATE_SINGLE_FORMULA))
            {
                invalidFormulas.add(m.getId());
                isValid = false;
            }
        }
        // second pass - validate links
        for (FormulaBase m : fList)
        {
            if (invalidFormulas.contains(m.getId()))
            {
                continue;
            }
            if (!m.isContentValid(FormulaBase.ValidationPassType.VALIDATE_LINKS))
            {
                isValid = false;
            }
        }
        // last pass: re-numbering text headers
        numbering();
        return isValid;
    }

    /**
     * Procedure performs re-numeration of headers
     */
    private void numbering()
    {
        final ArrayList<TextFragment> textList = formulaListView.getFormulas(TextFragment.class);
        final int[] headerNumber = TextProperties.getInitialNumber();
        for (TextFragment m : textList)
        {
            m.numbering(headerNumber);
        }
    }

    /**
     * Procedure performs calculation for all result formulae
     */
    public void calculate()
    {
        final ArrayList<CalculationResult> fList = formulaListView.getFormulas(CalculationResult.class);
        for (CalculationResult f : fList)
        {
            f.invalidateResult();
        }
        if (isContentValid())
        {
            CalculaterTask calculaterTask = new CalculaterTask(this, fList);
            CompatUtils.executeAsyncTask(calculaterTask);
        }
    }

    /**
     * This procedure is used to enable/disable palette buttons related to current mode/selection
     */
    public void updatePalette()
    {
        FormulaBase s = formulas.get(selectedFormulaId);
        TermField term = null;
        if (s != null)
        {
            term = s.findFocusedTerm();
        }
        for (Palette.PaletteType pt : Palette.PaletteType.values())
        {
            boolean enabled = false;
            boolean hiddenInputEnabled = false;
            if (term != null)
            {
                enabled = term.isEnabledInPalette(pt);
                if (term.isTerm())
                {
                    hiddenInputEnabled = true;
                }
            }
            palette.setPaletteBlockEnabled(pt, enabled);
            palette.enableHiddenInput(hiddenInputEnabled);
        }
    }

    public XmlLoaderTask getXmlLoaderTask()
    {
        return xmlLoaderTask;
    }

    public void stopXmlLoaderTask()
    {
        if (xmlLoaderTask != null)
        {
            xmlLoaderTask.abort();
        }
    }

    public void setObjectManipulator()
    {
        fragment.enableObjectPropertiesButton(getObjectManipulator(Manipulator.PROPERTY) != null);
        fragment.enableObjectDetailsButton(getObjectManipulator(Manipulator.DETAILS) != null);
    }

    private FormulaBase getObjectManipulator(Manipulator manipulator)
    {
        if (selectedEquations.size() == 1)
        {
            FormulaBase f = selectedEquations.get(0);
            if (manipulator == Manipulator.PROPERTY && f.enableObjectProperties())
            {
                return f;
            }
            if (manipulator == Manipulator.DETAILS && f.enableDetails())
            {
                return f;
            }
        }
        return null;
    }

    public void callObjectManipulator(Manipulator manipulator)
    {
        FormulaBase f = getObjectManipulator(manipulator);
        if (manipulator == Manipulator.PROPERTY && f != null)
        {
            f.onObjectProperties(f);
        }
        if (manipulator == Manipulator.DETAILS && f != null)
        {
            f.onDetails(f);
        }
    }

    public void showSoftKeyboard(boolean flag)
    {
        if (ViewUtils.isHardwareKeyboardAvailable(getContext()))
        {
            return;
        }
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (flag)
        {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
        else
        {
            ViewUtils.Debug(this, "request to hide keyboard");
            imm.hideSoftInputFromWindow(formulaListView.getList().getWindowToken(), 0);
        }
    }
}
