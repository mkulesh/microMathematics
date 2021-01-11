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

import android.widget.LinearLayout;

import com.mkulesh.micromath.formula.FormulaTerm;
import com.mkulesh.micromath.formula.PaletteButton;
import com.mkulesh.micromath.formula.TermField;
import com.mkulesh.micromath.widgets.CustomEditText;

public interface TermTypeIf
{
    enum GroupType
    {
        OPERATORS(20, true),
        COMPARATORS(90, true),
        ARRAY_FUNCTIONS(70, false),
        COMMON_FUNCTIONS(30, true),
        TRIGONOMETRIC_FUNCTIONS(40, false),
        LOG_FUNCTIONS(50, false),
        NUMBER_FUNCTIONS(60, false),
        USER_FUNCTIONS(10, true),
        INTERVALS(0, true),
        SERIES_INTEGRALS(80, true);

        private final int paletteOrder;
        private final boolean showByDefault;

        GroupType(int paletteOrder, boolean showByDefault)
        {
            this.paletteOrder = paletteOrder;
            this.showByDefault = showByDefault;
        }

        /**
         * Returns the order of the group in the toolbar
         */
        public int getPaletteOrder()
        {
            return paletteOrder;
        }

        /**
         * Returns whether this group shall be sown in the toolbar by default
         */
        public boolean isShowByDefault()
        {
            return showByDefault;
        }
    }

    /* Returns group term type of this object */
    GroupType getGroupType();

    /* Returns term name in lower case */
    String getLowerCaseName();

    /* Returns resource ID of the term icon */
    int getImageId();

    /* Returns resource ID of the term description */
    int getDescriptionId();

    /* Returns resource ID of the string short-cut */
    int getShortCutId();

    /* Returns bracket IDs related to this term or Palette.NO_BUTTON if no brackets are associated */
    int getBracketId();

    /* Checks whether this term is allowed for given edit field  */
    boolean isEnabled(CustomEditText field);

    /* Returns the palette category for this term  */
    PaletteButton.Category getPaletteCategory();

    /* Creates and returns associated view for this term  */
    FormulaTerm createTerm(TermField termField, LinearLayout layout, String text, int textIndex, Object par) throws Exception;
}
