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

public interface FormulaTermTypeIf
{
    enum GroupType
    {
        OPERATORS(10, true),
        COMPARATORS(90, true),
        FILE_OPERATIONS(70, false),
        COMMON_FUNCTIONS(30, true),
        TRIGONOMETRIC_FUNCTIONS(40, false),
        LOG_FUNCTIONS(50, false),
        NUMBER_FUNCTIONS(60, false),
        USER_FUNCTIONS(20, true),
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
}
