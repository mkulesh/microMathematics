/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2007 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javax.measure;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;

import javax.measure.quantity.Quantity;
import javax.measure.unit.CompoundUnit;
import javax.measure.unit.Unit;

/**
 * <p> This class represents the result of a measurement stated in a 
 *     known unit.</p>
 * 
 * <p> There is no constraint upon the measurement value itself: scalars, 
 *     vectors, or even data sets are valid values as long as 
 *     an aggregate magnitude can be determined (see {@link Measurable}).</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.2, August 26, 2007
 */
public abstract class Measure<V, Q extends Quantity> implements Measurable<Q>,
        Serializable {

    /**
     * Default constructor.
     */
    protected Measure() {
    }

    /**
     * Returns the scalar measure for the specified <code>double</code>
     * stated in the specified unit.
     * 
     * @param doubleValue the measurement value.
     * @param unit the measurement unit.
     */
    public static <Q extends Quantity> Measure<java.lang.Double, Q> valueOf(
            double doubleValue, Unit<Q> unit) {
        return new Double<>(doubleValue, unit);
    }

    /**
     * Returns the scalar measure for the specified <code>double</code>
     * stated in the specified unit.
     * 
     * @param longValue the measurement value.
     * @param unit the measurement unit.
     */
    public static <Q extends Quantity> Measure<java.lang.Long, Q> valueOf(
            long longValue, Unit<Q> unit) {
        return new Long<>(longValue, unit);
    }

    /**
     * Returns the scalar measure for the specified <code>float</code>
     * stated in the specified unit.
     * 
     * @param floatValue the measurement value.
     * @param unit the measurement unit.
     */
    public static <Q extends Quantity> Measure<java.lang.Float, Q> valueOf(
            float floatValue, Unit<Q> unit) {
        return new Float<>(floatValue, unit);
    }

    /**
     * Returns the scalar measure for the specified <code>int</code>
     * stated in the specified unit.
     * 
     * @param intValue the measurement value.
     * @param unit the measurement unit.
     */
    public static <Q extends Quantity> Measure<java.lang.Integer, Q> valueOf(
            int intValue, Unit<Q> unit) {
        return new Integer<>(intValue, unit);
    }

    /**
     * Returns the measurement value of this measure.
     *    
     * @return the measurement value.
     */
    public abstract V getValue();

    /**
     * Returns the measurement unit of this measure.
     * 
     * @return the measurement unit.
     */
    public abstract Unit<Q> getUnit();

    /**
     * Returns the measure equivalent to this measure but stated in the 
     * specified unit. This method may result in lost of precision 
     * (e.g. measure of integral value).
     * 
     * @param unit the new measurement unit.
     * @return the measure stated in the specified unit.
     */
    public abstract Measure<V, Q> to(Unit<Q> unit);

    /**
     * Returns the value of this measure stated in the specified unit as 
     * a <code>double</code>. If the measure has too great a magnitude to 
     * be represented as a <code>double</code>, it will be converted to 
     * <code>Double.NEGATIVE_INFINITY</code> or
     * <code>Double.POSITIVE_INFINITY</code> as appropriate.
     * 
     * @param unit the unit in which this measure is stated.
     * @return the numeric value after conversion to type <code>double</code>.
     */
    public abstract double doubleValue(Unit<Q> unit);

    /**
     * Returns the estimated integral value of this measure stated in 
     * the specified unit as a <code>long</code>. 
     * 
     * <p> Note: This method differs from the <code>Number.longValue()</code>
     *           in the sense that the closest integer value is returned 
     *           and an ArithmeticException is raised instead
     *           of a bit truncation in case of overflow (safety critical).</p> 
     * 
     * @param unit the unit in which the measurable value is stated.
     * @return the numeric value after conversion to type <code>long</code>.
     * @throws ArithmeticException if this quantity cannot be represented 
     *         as a <code>long</code> number in the specified unit.
     */
    public long longValue(Unit<Q> unit) throws ArithmeticException {
        double doubleValue = doubleValue(unit);
        if (java.lang.Double.isNaN(doubleValue)
                || (doubleValue < java.lang.Long.MIN_VALUE)
                || (doubleValue > java.lang.Long.MAX_VALUE))
            throw new ArithmeticException(doubleValue + " " + unit
                    + " cannot be represented as long");
        return Math.round(doubleValue);
    }

    /**
     * Returns the value of this measure stated in the specified unit as a 
     * <code>float</code>. If the measure has too great a magnitude to be 
     * represented as a <code>float</code>, it will be converted to 
     * <code>Float.NEGATIVE_INFINITY</code> or
     * <code>Float.POSITIVE_INFINITY</code> as appropriate.
     * 
     * @param unit the unit in which the measure is stated.
     * @return the numeric value after conversion to type <code>float</code>.
     */
    public float floatValue(Unit<Q> unit) {
        return (float) doubleValue(unit);
    }

    /**
     * Returns the estimated integral value of this measure stated in 
     * the specified unit as a <code>int</code>. 
     * 
     * <p> Note: This method differs from the <code>Number.intValue()</code>
     *           in the sense that the closest integer value is returned 
     *           and an ArithmeticException is raised instead
     *           of a bit truncation in case of overflow (safety critical).</p> 
     * 
     * @param unit the unit in which the measurable value is stated.
     * @return the numeric value after conversion to type <code>int</code>.
     * @throws ArithmeticException if this quantity cannot be represented 
     *         as a <code>int</code> number in the specified unit.
     */
    public int intValue(Unit<Q> unit) {
        long longValue = longValue(unit);
        if ((longValue > java.lang.Integer.MAX_VALUE)
                || (longValue < java.lang.Integer.MIN_VALUE))
            throw new ArithmeticException("Overflow");
        return (int) longValue;
    }

    /**
     * Compares this measure against the specified object for 
     * strict equality (same unit and amount).
     * To compare measures stated using different units the  
     * {@link #compareTo} method should be used. 
     *
     * @param  obj the object to compare with.
     * @return <code>true</code> if both objects are identical (same 
     *         unit and same amount); <code>false</code> otherwise.
     */
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (!(obj instanceof Measure))
            return false;
        Measure that = (Measure) obj;
        return this.getUnit().equals(that.getUnit())
                && this.getValue().equals(that.getValue());
    }

    /**
     * Returns the hash code for this scalar.
     * 
     * @return the hash code value.
     */
    public int hashCode() {
        return getUnit().hashCode() + getValue().hashCode();
    }

    /**
     * Returns the <code>String</code> representation of this measure
     * The string produced for a given measure is always the same;
     * it is not affected by locale.  This means that it can be used
     * as a canonical string representation for exchanging data, 
     * or as a key for a Hashtable, etc.  Locale-sensitive
     * measure formatting and parsing is handled by the {@link
     * MeasureFormat} class and its subclasses.
     * 
     * @return the string representation of this measure.
     */
    public String toString() {
        if (getUnit() instanceof CompoundUnit)
            return MeasureFormat.DEFAULT.formatCompound(doubleValue(getUnit()),
                    getUnit(), new StringBuffer(), null).toString();
        return getValue() + " " + getUnit();
    }

    /**
     * Compares this measure to the specified measurable quantity.
     * This method compares the {@link Measurable#doubleValue(Unit)} of 
     * both this measure and the specified measurable stated in the 
     * same unit (this measure's {@link #getUnit() unit}).
     * 
     * @return  a negative integer, zero, or a positive integer as this measure
     *          is less than, equal to, or greater than the specified measurable
     *          quantity.
      * @return <code>Double.compare(this.doubleValue(getUnit()), 
      *         that.doubleValue(getUnit()))</code>
     */
    public int compareTo(Measurable<Q> that) {
        return java.lang.Double.compare(doubleValue(getUnit()), that
                .doubleValue(getUnit()));
    }

    /**
     * Holds scalar implementation for <code>double</code> values.
     */
    private static final class Double<Q extends Quantity> extends
            Measure<java.lang.Double, Q> {

        private final double _value;

        private final Unit<Q> _unit;

        public Double(double value, Unit<Q> unit) {
            _value = value;
            _unit = unit;
        }

        @Override
        public Unit<Q> getUnit() {
            return _unit;
        }

        @Override
        public java.lang.Double getValue() {
            return _value;
        }

        @Override
        public Measure<java.lang.Double, Q> to(Unit<Q> unit) {
            if ((unit == _unit) || (unit.equals(_unit)))
                return this;
            return new Double<>(doubleValue(unit), unit);
        }

        public double doubleValue(Unit<Q> unit) {
            if ((unit == _unit) || (unit.equals(_unit)))
                return _value;
            return _unit.getConverterTo(unit).convert(_value);
        }

        private static final long serialVersionUID = 1L;
    }

    /**
     * Holds scalar implementation for <code>long</code> values.
     */
    private static final class Long<Q extends Quantity> extends
            Measure<java.lang.Long, Q> {

        private final long _value;

        private final Unit<Q> _unit;

        public Long(long value, Unit<Q> unit) {
            _value = value;
            _unit = unit;
        }

        @Override
        public Unit<Q> getUnit() {
            return _unit;
        }

        @Override
        public java.lang.Long getValue() {
            return _value;
        }

        @Override
        public Measure<java.lang.Long, Q> to(Unit<Q> unit) {
            if ((unit == _unit) || (unit.equals(_unit)))
                return this;
            return new Long<>(longValue(unit), unit);
        }

        public double doubleValue(Unit<Q> unit) {
            if ((unit == _unit) || (unit.equals(_unit)))
                return _value;
            return _unit.getConverterTo(unit).convert(_value);
        }

        public long longValue(Unit<Q> unit) throws ArithmeticException {
            if ((unit == _unit) || (unit.equals(_unit)))
                return _value; // No conversion, returns value directly.
            return super.longValue(unit);
        }

        private static final long serialVersionUID = 1L;

    }

    /**
     * Holds scalar implementation for <code>float</code> values.
     */
    private static final class Float<Q extends Quantity> extends
            Measure<java.lang.Float, Q> {

        private final float _value;

        private final Unit<Q> _unit;

        public Float(float value, Unit<Q> unit) {
            _value = value;
            _unit = unit;
        }

        @Override
        public Unit<Q> getUnit() {
            return _unit;
        }

        @Override
        public java.lang.Float getValue() {
            return _value;
        }

        @Override
        public Measure<java.lang.Float, Q> to(Unit<Q> unit) {
            if ((unit == _unit) || (unit.equals(_unit)))
                return this;
            return new Float<>(floatValue(unit), unit);
        }

        public double doubleValue(Unit<Q> unit) {
            if ((unit == _unit) || (unit.equals(_unit)))
                return _value;
            return _unit.getConverterTo(unit).convert(_value);
        }

        private static final long serialVersionUID = 1L;
    }

    /**
     * Holds scalar implementation for <code>long</code> values.
     */
    private static final class Integer<Q extends Quantity> extends
            Measure<java.lang.Integer, Q> {

        private final int _value;

        private final Unit<Q> _unit;

        public Integer(int value, Unit<Q> unit) {
            _value = value;
            _unit = unit;
        }

        @Override
        public Unit<Q> getUnit() {
            return _unit;
        }

        @Override
        public java.lang.Integer getValue() {
            return _value;
        }

        @Override
        public Measure<java.lang.Integer, Q> to(Unit<Q> unit) {
            if ((unit == _unit) || (unit.equals(_unit)))
                return this;
            return new Integer<>(intValue(unit), unit);
        }

        public double doubleValue(Unit<Q> unit) {
            if ((unit == _unit) || (unit.equals(_unit)))
                return _value;
            return _unit.getConverterTo(unit).convert(_value);
        }

        public long longValue(Unit<Q> unit) throws ArithmeticException {
            if ((unit == _unit) || (unit.equals(_unit)))
                return _value; // No conversion, returns value directly.
            return super.longValue(unit);
        }

        private static final long serialVersionUID = 1L;

    }

    public static <Q extends Quantity> Measure<BigDecimal, Q> valueOf(
            BigDecimal decimal, Unit<Q> unit) {
        return DecimalMeasure.valueOf(decimal, unit);
    }

    public static <Q extends Quantity> Measure<BigDecimal, Q> valueOf(
            BigDecimal decimal, Unit<Q> unit, MathContext mathContext) {
        return DecimalMeasure.valueOf(decimal, unit);
    }

    public static <Q extends Quantity> Measure<double[], Q> valueOf(
            double[] components, Unit<Q> unit) {
        return VectorMeasure.valueOf(components, unit);
    }
}