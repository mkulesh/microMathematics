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
package com.mkulesh.micromath.math;

import androidx.annotation.NonNull;

import com.mkulesh.micromath.formula.CalculaterTask;
import com.mkulesh.micromath.formula.CalculaterTask.CancelException;
import com.mkulesh.micromath.formula.TermField;
import com.mkulesh.micromath.formula.TermParser;
import com.mkulesh.micromath.properties.DocumentProperties;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.util.FastMath;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import javax.measure.DecimalMeasure;
import javax.measure.Measure;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

public class CalculatedValue
{
    public enum ErrorType
    {
        TERM_NOT_READY,
        INVALID_ARGUMENT,
        NOT_A_NUMBER,
        NOT_A_REAL,
        PASSED_COMPLEX,
        INCOMPATIBLE_UNIT
    }

    public enum ValueType
    {
        INVALID,
        REAL,
        COMPLEX
    }

    public enum PartType
    {
        RE,
        IM
    }

    public final static CalculatedValue NaN = new CalculatedValue(ValueType.INVALID, Double.NaN, 0.0);
    public final static CalculatedValue ONE = new CalculatedValue(ValueType.REAL, 1.0, 0.0);
    public final static CalculatedValue MINUS_ONE = new CalculatedValue(ValueType.REAL, -1.0, 0.0);
    private final static CalculatedValue ZERO = new CalculatedValue(ValueType.REAL, 0.0, 0.0);

    private ValueType valueType = ValueType.INVALID;
    private double real = Double.NaN;
    private double imaginary = 0.0;
    private Unit unit = null;

    /*********************************************************
     * Common methods
     *********************************************************/

    public CalculatedValue()
    {
        // empty
    }

    public CalculatedValue(ValueType valueType, double real, double imaginary)
    {
        this.valueType = valueType;
        this.real = real;
        this.imaginary = imaginary;
    }

    public CalculatedValue(double real, Unit u)
    {
        this.valueType = ValueType.REAL;
        this.real = real;
        this.imaginary = 0.0;
        unit = u;
    }

    public ValueType assign(CalculatedValue c)
    {
        valueType = c.valueType;
        real = c.real;
        imaginary = c.imaginary;
        unit = c.unit;
        return valueType;
    }

    public ValueType assign(CalculatedValue c, Unit u)
    {
        this.assign(c);
        unit = u;
        return valueType;
    }

    public Unit getUnit()
    {
        return unit;
    }

    public void setUnit(Unit unit)
    {
        this.unit = unit;
    }

    public void convertUnit(@NonNull Unit sourceUnit, @NonNull Unit targetUnit)
    {
        if (valueType == ValueType.INVALID)
        {
            return;
        }
        if (sourceUnit.isCompatible(targetUnit))
        {
            try
            {
                final Measure realV = DecimalMeasure.valueOf(real, sourceUnit);
                real = realV.doubleValue(targetUnit);
                if (isComplex())
                {
                    final Measure imaginaryV = DecimalMeasure.valueOf(imaginary, sourceUnit);
                    imaginary = imaginaryV.doubleValue(targetUnit);
                }
                unit = (targetUnit.toString() == null || targetUnit.toString().isEmpty()) ? null : targetUnit;
            }
            catch (Exception ex)
            {
                invalidate(ErrorType.INCOMPATIBLE_UNIT);
            }
        }
        else
        {
            invalidate(ErrorType.INCOMPATIBLE_UNIT);
        }
    }

    public void convertUnit(final Unit targetUnit, boolean toBase)
    {
        final Unit sourceUnit = getUnit();
        if (sourceUnit == null)
        {
            return;
        }
        Unit newUnit = targetUnit;
        if (newUnit == null && toBase)
        {
            ArrayList<Unit> ul = new ArrayList<>();
            for (Unit u : SI.getInstance().getUnits())
            {
                if (sourceUnit.isCompatible(u))
                {
                    ul.add(u);
                }
            }
            int minLen = Integer.MAX_VALUE;
            for (Unit u : ul)
            {
                if (sourceUnit.getStandardUnit().toString().equals(u.toString()))
                {
                    newUnit = u;
                    break;
                }
                if (newUnit == null || u.toString().length() < minLen)
                {
                    newUnit = u;
                    minLen = u.toString().length();
                }
            }
            // some special cases
            if (newUnit != null)
            {
                if (newUnit.equals(SI.BECQUEREL))
                {
                    newUnit = SI.HERTZ;
                }
                if (newUnit.equals(SI.BIT) && real >= 8)
                {
                    newUnit = NonSI.BYTE;
                }
            }
        }
        convertUnit(sourceUnit, newUnit == null ? sourceUnit.getStandardUnit() : newUnit);
    }

    public ValueType invalidate(ErrorType errorType)
    {
        valueType = ValueType.INVALID;
        real = Double.NaN;
        imaginary = 0.0;
        unit = null;
        return valueType;
    }

    public ValueType setValue(double real)
    {
        this.real = real;
        this.imaginary = 0.0;
        valueType = ValueType.REAL;
        return valueType;
    }

    public ValueType setValue(double real, Unit u)
    {
        this.setValue(real);
        this.unit = u;
        return valueType;
    }

    public ValueType setComplexValue(double real, double imaginary)
    {
        this.real = real;
        this.imaginary = imaginary;
        valueType = (imaginary != 0.0) ? ValueType.COMPLEX : ValueType.REAL;
        return valueType;
    }

    private ValueType setComplexValue(Complex c)
    {
        real = c.getReal();
        imaginary = c.getImaginary();
        valueType = (imaginary != 0.0) ? ValueType.COMPLEX : ValueType.REAL;
        return valueType;
    }

    public void merge(CalculatedValue m)
    {
        if (m == null || m.isNaN())
        {
            assign(ZERO);
        }
        else
        {
            assign(m);
        }
    }

    public static Integer toInteger(String s)
    {
        try
        {
            return Integer.valueOf(s);
        }
        catch (Exception e)
        {
            // nothing to do
        }
        return null;
    }

    /*********************************************************
     * Common getters
     *********************************************************/

    public ValueType getValueType()
    {
        return valueType;
    }

    public boolean isReal()
    {
        return valueType == ValueType.REAL;
    }

    public boolean isComplex()
    {
        return valueType == ValueType.COMPLEX;
    }

    public boolean isNaN()
    {
        switch (valueType)
        {
        case REAL:
            return isInvalidReal(real);
        case COMPLEX:
            return isInvalidReal(real) || isInvalidReal(imaginary);
        default:
            return true;
        }
    }

    public boolean isZero()
    {
        switch (valueType)
        {
        case REAL:
            return real == 0.0;
        case COMPLEX:
            return real == 0.0 && imaginary == 0.0;
        default:
            return false;
        }
    }

    public static boolean isInvalidReal(double v)
    {
        return Double.isNaN(v) || Double.isInfinite(v);
    }

    public double getReal()
    {
        return real;
    }

    public double getImaginary()
    {
        return imaginary;
    }

    public Complex getComplex()
    {
        return new Complex(real, imaginary);
    }

    public int getInteger()
    {
        return (int) real;
    }

    public double getPart(PartType type)
    {
        return type == PartType.RE ? real : imaginary;
    }

    private boolean unitExists(CalculatedValue f, CalculatedValue g)
    {
        return f.unit != null || g.unit != null;
    }

    /*********************************************************
     * Conversion methods
     *********************************************************/

    @NonNull
    public String toString()
    {
        return valueType.toString() + "[" + real + ", " + imaginary + "]" +
                (unit == null ? "?" : unit.toString());
    }

    public String getResultDescription(DocumentProperties doc)
    {
        String val;
        switch (valueType)
        {
        case INVALID:
            return TermParser.CONST_NAN;
        case REAL:
            if (Double.isNaN(real))
            {
                return TermParser.CONST_NAN;
            }
            val = formatValue(real, doc, false);
            if (unit != null)
            {
                val += " " + unit.toString();
            }
            return val;
        case COMPLEX:
            if (Double.isNaN(real) || Double.isNaN(imaginary))
            {
                return TermParser.CONST_NAN;
            }
            val = formatValue(real, doc, false) + formatValue(imaginary, doc, true) + "i";
            if (unit != null)
            {
                val += " " + unit.toString();
            }
            return val;
        }
        return "";
    }

    private String formatValue(double value, DocumentProperties doc, boolean addPlusSign)
    {
        if (Double.isInfinite(value))
        {
            if (value < 0)
            {
                return "-" + TermParser.CONST_INF;
            }
            else
            {
                return addPlusSign ? "+" + TermParser.CONST_INF : TermParser.CONST_INF;
            }
        }
        else
        {
            final double roundV = roundToNumberOfSignificantDigits(value, doc.significantDigits);
            if (roundV >= 0 && addPlusSign)
            {
                return "+" + roundV;
            }
            else
            {
                return Double.toString(roundV);
            }
        }
    }

    /*********************************************************
     * Calculation methods
     *********************************************************/

    public void processRealTerm(CalculaterTask thread, TermField term) throws CancelException
    {
        term.getValue(thread, this);
        if (!isReal())
        {
            invalidate(ErrorType.NOT_A_REAL);
        }
    }

    public ValueType add(CalculatedValue f, CalculatedValue g)
    {
        if (unitExists(f, g))
        {
            if (f.unit == null || g.unit == null || !f.unit.isCompatible(g.unit))
            {
                return invalidate(ErrorType.INCOMPATIBLE_UNIT);
            }
            unit = f.unit;
        }
        else
        {
            unit = null;
        }
        if (f.isComplex() || g.isComplex())
        {
            return setComplexValue(f.real + g.real, f.imaginary + g.imaginary);
        }
        else
        {
            return setValue(f.real + g.real);
        }
    }

    public ValueType subtract(CalculatedValue f, CalculatedValue g)
    {
        if (unitExists(f, g))
        {
            if (f.unit == null || g.unit == null || !f.unit.isCompatible(g.unit))
            {
                return invalidate(ErrorType.INCOMPATIBLE_UNIT);
            }
            unit = f.unit;
        }
        else
        {
            unit = null;
        }
        if (f.isComplex() || g.isComplex())
        {
            return setComplexValue(f.real - g.real, f.imaginary - g.imaginary);
        }
        else
        {
            return setValue(f.real - g.real);
        }
    }

    public ValueType multiply(CalculatedValue f, CalculatedValue g)
    {
        if (unitExists(f, g))
        {
            if (f.unit == null)
            {
                unit = g.unit;
            }
            else if (g.unit == null)
            {
                unit = f.unit;
            }
            else
            {
                unit = f.unit.times(g.unit);
                if (unit != null && unit.isCompatible(Unit.ONE))
                {
                    unit = null;
                }
            }
        }
        else
        {
            unit = null;
        }
        if (f.isComplex() || g.isComplex())
        {
            return setComplexValue(f.real * g.real - f.imaginary * g.imaginary, f.real * g.imaginary + f.imaginary
                    * g.real);
        }
        else
        {
            return setValue(f.real * g.real);
        }
    }

    public ValueType multiply(double f)
    {
        real *= f;
        imaginary *= f;
        return valueType;
    }

    public ValueType divide(CalculatedValue f, CalculatedValue g)
    {
        if (unitExists(f, g))
        {
            if (f.unit == null)
            {
                unit = g.unit.inverse();
            }
            else if (g.unit == null)
            {
                unit = f.unit;
            }
            else if (f.unit.equals(g.unit))
            {
                unit = null;
            }
            else
            {
                unit = f.unit.divide(g.unit);
            }
        }
        else
        {
            unit = null;
        }
        if (f.isComplex() || g.isComplex())
        {
            final double c = g.real;
            final double d = g.imaginary;
            if (FastMath.abs(c) < FastMath.abs(d))
            {
                final double q = c / d;
                final double denominator = c * q + d;
                return setComplexValue((f.real * q + f.imaginary) / denominator, (f.imaginary * q - f.real)
                        / denominator);
            }
            else
            {
                final double q = d / c;
                final double denominator = d * q + c;
                return setComplexValue((f.imaginary * q + f.real) / denominator, (f.imaginary - f.real * q)
                        / denominator);
            }
        }
        else
        {
            return setValue(f.real / g.real);
        }
    }

    public Unit powUnit(CalculatedValue f, CalculatedValue g)
    {
        if (f.unit == null || g.unit != null || g.isComplex())
        {
            return null;
        }
        final int n = (int) g.real;
        if ((double) n != g.real)
        {
            return null;
        }
        return f.unit.pow(n);
    }

    public ValueType pow(CalculatedValue f, CalculatedValue g)
    {
        if (unitExists(f, g))
        {
            unit = powUnit(f, g);
            if (unit == null)
            {
                return invalidate(ErrorType.INCOMPATIBLE_UNIT);
            }
        }
        else
        {
            unit = null;
        }
        if (f.isComplex() || g.isComplex())
        {
            return setComplexValue(f.getComplex().pow(g.getComplex()));
        }
        else
        {
            return setValue(FastMath.pow(f.real, g.real));
        }
    }

    public ValueType abs(CalculatedValue g)
    {
        unit = g.unit;
        return setValue(g.isComplex() ? FastMath.hypot(g.real, g.imaginary) : FastMath.abs(g.real));
    }

    public ValueType sqrt(CalculatedValue g)
    {
        if (g.isComplex() || (g.isReal() && g.real < 0))
        {
            if (g.unit != null)
            {
                invalidate(ErrorType.INCOMPATIBLE_UNIT);
            }
            else
            {
                unit = null;
            }
            return setComplexValue(g.getComplex().sqrt());
        }
        else
        {
            if (g.unit != null)
            {
                unit = g.unit.root(2);
            }
            else
            {
                unit = null;
            }
            return setValue(FastMath.sqrt(g.real));
        }
    }

    public ValueType sin(CalculatedValue g)
    {
        if (g.isComplex())
        {
            return setComplexValue(g.getComplex().sin());
        }
        else
        {
            return setValue(FastMath.sin(g.real));
        }
    }

    public ValueType csc(CalculatedValue g)
    {
        if (g.isComplex())
        {
            return setComplexValue(g.getComplex().sin().reciprocal());
        }
        else
        {
            return setValue(1.0f / FastMath.sin(g.real));
        }

    }

    public ValueType asin(CalculatedValue g)
    {
        if (g.isComplex())
        {
            return setComplexValue(g.getComplex().asin());
        }
        else
        {
            return setValue(FastMath.asin(g.real));
        }
    }

    public ValueType sinh(CalculatedValue g)
    {
        if (g.isComplex())
        {
            return setComplexValue(g.getComplex().sinh());
        }
        else
        {
            return setValue(FastMath.sinh(g.real));
        }
    }

    public ValueType csch(CalculatedValue g)
    {
        if (g.isComplex())
        {
            return setComplexValue(g.getComplex().sinh().reciprocal());
        }
        else
        {
            return setValue(1.0f / FastMath.sinh(g.real));
        }
    }

    public ValueType cos(CalculatedValue g)
    {
        if (g.isComplex())
        {
            return setComplexValue(g.getComplex().cos());
        }
        else
        {
            return setValue(FastMath.cos(g.real));
        }
    }

    public ValueType sec(CalculatedValue g)
    {
        if (g.isComplex())
        {
            return setComplexValue(g.getComplex().cos().reciprocal());
        }
        else
        {
            return setValue(1.0f / FastMath.cos(g.real));
        }
    }

    public ValueType acos(CalculatedValue g)
    {
        if (g.isComplex())
        {
            return setComplexValue(g.getComplex().acos());
        }
        else
        {
            return setValue(FastMath.acos(g.real));
        }
    }

    public ValueType cosh(CalculatedValue g)
    {
        if (g.isComplex())
        {
            return setComplexValue(g.getComplex().cosh());
        }
        else
        {
            return setValue(FastMath.cosh(g.real));
        }
    }

    public ValueType sech(CalculatedValue g)
    {
        if (g.isComplex())
        {
            return setComplexValue(g.getComplex().cosh().reciprocal());
        }
        else
        {
            return setValue(1.0f / FastMath.cosh(g.real));
        }
    }

    public ValueType tan(CalculatedValue g)
    {
        if (g.isComplex())
        {
            return setComplexValue(g.getComplex().tan());
        }
        else
        {
            return setValue(FastMath.tan(g.real));
        }
    }

    public ValueType cot(CalculatedValue g)
    {
        if (g.isComplex())
        {
            return setComplexValue(g.getComplex().tan().reciprocal());
        }
        else
        {
            return setValue(1.0f / FastMath.tan(g.real));
        }
    }

    public ValueType atan(CalculatedValue g)
    {
        if (g.isComplex())
        {
            return setComplexValue(g.getComplex().atan());
        }
        else
        {
            return setValue(FastMath.atan(g.real));
        }
    }

    public ValueType tanh(CalculatedValue g)
    {
        if (g.isComplex())
        {
            return setComplexValue(g.getComplex().tanh());
        }
        else
        {
            return setValue(FastMath.tanh(g.real));
        }
    }

    public ValueType coth(CalculatedValue g)
    {
        if (g.isComplex())
        {
            return setComplexValue(g.getComplex().tanh().reciprocal());
        }
        else
        {
            return setValue(1.0f / FastMath.tanh(g.real));
        }
    }

    public ValueType exp(CalculatedValue g)
    {
        if (g.isComplex())
        {
            return setComplexValue(g.getComplex().exp());
        }
        else
        {
            return setValue(FastMath.exp(g.real));
        }
    }

    public ValueType log(CalculatedValue g)
    {
        if (g.isComplex() || g.real <= 0.0)
        {
            return setComplexValue(g.getComplex().log());
        }
        else
        {
            return setValue(FastMath.log(g.real));
        }
    }

    public ValueType log10(CalculatedValue g)
    {
        if (g.isComplex() || g.real <= 0.0)
        {
            return setComplexValue(g.getComplex().log().divide(FastMath.log(10.0)));
        }
        else
        {
            return setValue(FastMath.log10(g.real));
        }
    }

    public ValueType ceil(CalculatedValue g)
    {
        if (g.isComplex())
        {
            return setComplexValue(FastMath.ceil(g.real), FastMath.ceil(g.imaginary));
        }
        else
        {
            return setValue(FastMath.ceil(g.real));
        }
    }

    public ValueType floor(CalculatedValue g)
    {
        if (g.isComplex())
        {
            return setComplexValue(FastMath.floor(g.real), FastMath.floor(g.imaginary));
        }
        else
        {
            return setValue(FastMath.floor(g.real));
        }
    }

    public ValueType random(CalculatedValue g)
    {
        if (g.isComplex())
        {
            return setComplexValue(FastMath.random() * g.real, FastMath.random() * g.imaginary);
        }
        else
        {
            return setValue(FastMath.random() * g.real);
        }
    }

    public ValueType nthRoot(CalculatedValue g, int n)
    {
        try
        {
            if (g.unit != null)
            {
                unit = g.unit.root(n);
            }
            else
            {
                unit = null;
            }
            final List<Complex> roots = g.getComplex().nthRoot(n);
            for (Complex root : roots)
            {
                if (FastMath.abs(root.getImaginary()) < 1E-15)
                {
                    return setValue(root.getReal());
                }
            }
            if (!roots.isEmpty())
            {
                return setComplexValue(roots.get(0));
            }
        }
        catch (Exception ex)
        {
            // nothing to do
        }
        return invalidate(ErrorType.NOT_A_NUMBER);
    }

    public ValueType conj(CalculatedValue g)
    {
        if (g.isComplex())
        {
            return setComplexValue(g.real, -1.0 * g.imaginary);
        }
        else
        {
            return setValue(g.real);
        }
    }

    /**
     * Procedure rounds the given value to the given number of significant digits see
     * http://stackoverflow.com/questions/202302
     *
     * Note: The maximum double value in Java is on the order of 10^308, while the minimum value is on the order of
     * 10^-324. Therefore, you can run into trouble when applying the function roundToSignificantFigures to something
     * that's within a few powers of ten of Double.MIN_VALUE.
     *
     * Consequently, the variable magnitude may become Infinity, and it's all garbage from then on out. Fortunately,
     * this is not an insurmountable problem: it is only the factor magnitude that's overflowing. What really matters is
     * the product num * magnitude, and that does not overflow. One way of resolving this is by breaking up the
     * multiplication by the factor magintude into two steps.
     */
    private static double roundToNumberOfSignificantDigits(double num, int n)
    {
        if (num == 0)
        {
            return 0;
        }

        try
        {
            return new BigDecimal(num).round(new MathContext(n, RoundingMode.HALF_EVEN)).doubleValue();
        }
        catch (ArithmeticException ex)
        {
            // nothing to do
        }

        final double maxPowerOfTen = FastMath.floor(FastMath.log10(Double.MAX_VALUE));
        final double d = FastMath.ceil(FastMath.log10(num < 0 ? -num : num));
        final int power = n - (int) d;

        double firstMagnitudeFactor = 1.0;
        double secondMagnitudeFactor = 1.0;
        if (power > maxPowerOfTen)
        {
            firstMagnitudeFactor = FastMath.pow(10.0, maxPowerOfTen);
            secondMagnitudeFactor = FastMath.pow(10.0, (double) power - maxPowerOfTen);
        }
        else
        {
            firstMagnitudeFactor = FastMath.pow(10.0, (double) power);
        }

        double toBeRounded = num * firstMagnitudeFactor;
        toBeRounded *= secondMagnitudeFactor;

        final long shifted = FastMath.round(toBeRounded);
        double rounded = ((double) shifted) / firstMagnitudeFactor;
        rounded /= secondMagnitudeFactor;
        return rounded;
    }
}
