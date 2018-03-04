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

import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.math.CalculatedValue;

public interface CalculatableIf
{
    enum DifferentiableType
    {
        NONE,
        NUMERICAL,
        ANALYTICAL,
        INDEPENDENT
    }

    /**
     * Procedure calculates recursively the formula value
     */
    CalculatedValue.ValueType getValue(CalculaterTask thread, CalculatedValue outValue) throws CancelException;

    /**
     * Procedure checks whether this term holds a differentiable equation with respect to given variable name
     */
    DifferentiableType isDifferentiable(String var);

    /**
     * Procedure calculates recursively the derivative value
     */
    CalculatedValue.ValueType getDerivativeValue(String var, CalculaterTask thread, CalculatedValue outValue)
            throws CancelException;

}
