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

public interface ListChangeIf
{
    enum FormulaType
    {
        EQUATION,
        RESULT,
        PLOT_FUNCTION,
        TEXT_FRAGMENT,
        IMAGE_FRAGMENT
    }

    enum Position
    {
        BEFORE,
        AFTER,
        LEFT,
        RIGHT
    }

    /**
     * Procedure creates a formula with given type and position
     */
    void onNewFormula(Position position, FormulaType formulaType);

    /**
     * Procedure deletes the formula with given ID
     */
    void onDiscardFormula(int id);

    /**
     * Procedure will be called if a scale event happens
     */
    void onScale(float scaleFactor);

    /**
     * Procedure is called if a palette button is pressed
     */
    void onPalettePressed(String code);

    /**
     * Procedure stores all equations from selectedEquations array into clipboard
     */
    boolean onCopyToClipboard();

    /**
     * Procedure restores all equations from clipboard into the view: all selected equations will be replaced
     */
    boolean onPasteFromClipboard(String content);

    /**
     * Procedure is called on manual input
     */
    void onManualInput();

    /**
     * Procedure is called if palette update is required
     */
    void updatePalette();
}
