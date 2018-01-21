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
        OPERATORS(10),
        COMPARATORS(90),
        FILE_OPERATIONS(70),
        COMMON_FUNCTIONS(30),
        TRIGONOMETRIC_FUNCTIONS(40),
        LOG_FUNCTIONS(50),
        NUMBER_FUNCTIONS(60),
        USER_FUNCTIONS(20),
        INTERVALS(0),
        SERIES_INTEGRALS(80);

        private final int paletteOrder;

        GroupType(int paletteOrder)
        {
            this.paletteOrder = paletteOrder;
        }

        public int getPaletteOrder()
        {
            return paletteOrder;
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
