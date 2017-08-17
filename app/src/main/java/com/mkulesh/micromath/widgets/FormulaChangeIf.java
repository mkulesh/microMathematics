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

import java.util.ArrayList;

import android.view.View;

import com.mkulesh.micromath.formula.TermField;

public interface FormulaChangeIf
{
    /**
     * Procedure will be called if the context menu is activated
     */
    void onCreateContextMenu(View owner, ContextMenuHandler handler);

    /**
     * Procedure will be called if a term is focused
     */
    void onFocus(View v, boolean hasFocus);

    /**
     * Procedure will be called if a term shall be selected/unselected
     */
    void onTermSelection(View owner, boolean isSelected, ArrayList<View> list);

    /**
     * Procedure will be called if action mode is finished
     */
    void finishActionMode(View owner);

    /**
     * Procedure will be called if current selection shall be expanded
     */
    FormulaChangeIf onExpandSelection(View owner, ContextMenuHandler handler);

    /**
     * Procedure will be called if a term shall be copied to clipboard
     */
    void onCopyToClipboard();

    /**
     * Procedure will be called if a term shall be pasted from clipboard
     */
    void onPasteFromClipboard(View owner, String content);

    /**
     * Procedure will be called if the formula is newly created by manual input
     */
    public void onNewFormula();

    /**
     * Procedure will be called if a term shall be deleted
     */
    void onDelete(CustomEditText owner);

    /**
     * Procedure will be called if a object property dialog for given owner term shall be called
     */
    void onObjectProperties(View owner);

    /**
     * Procedure will be called if a details for given owner term shall be displayed
     */
    void onDetails(View owner);

    /**
     * Procedure will be called if the given field obtains a command aimed to be expanded by a new term
     */
    boolean onNewTerm(TermField owner, String s, boolean requestFocus);

    /**
     * Procedure returns whether the object properties are enabled for this formula
     */
    boolean enableObjectProperties();

    /**
     * Procedure returns whether the object details view is enabled for this formula
     */
    boolean enableDetails();
}
