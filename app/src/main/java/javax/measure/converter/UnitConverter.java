/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javax.measure.converter;

import java.io.Serializable;

/**
 * <p> This class represents a converter of numeric values.</p>
 * 
 * <p> It is not required for sub-classes to be immutable
 *     (e.g. currency converter).</p>
 *     
 * <p> Sub-classes must ensure unicity of the {@link #IDENTITY identity} 
 *     converter. In other words, if the result of an operation is equivalent
 *     to the identity converter, then the unique {@link #IDENTITY} instance 
 *     should be returned.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.1, April 22, 2006
 */
public abstract class UnitConverter implements Serializable {

    /**
     * Holds the identity converter (unique). This converter does nothing
     * (<code>ONE.convert(x) == x</code>).
     */
    public static final UnitConverter IDENTITY = new Identity();

    /**
     * Default constructor.
     */
    protected UnitConverter() {
    }

    /**
     * Returns the inverse of this converter. If <code>x</code> is a valid
     * value, then <code>x == inverse().convert(convert(x))</code> to within
     * the accuracy of computer arithmetic.
     *
     * @return the inverse of this converter.
     */
    public abstract UnitConverter inverse();

    /**
     * Converts a double value.
     *
     * @param  x the numeric value to convert.
     * @return the converted numeric value.
     * @throws ConversionException if an error occurs during conversion.
     */
    public abstract double convert(double x) throws ConversionException;

    /**
     * Indicates if this converter is linear. A converter is linear if
     * <code>convert(u + v) == convert(u) + convert(v)</code> and
     * <code>convert(r * u) == r * convert(u)</code>.
     * For linear converters the following property always hold:[code]
     *     y1 = c1.convert(x1);
     *     y2 = c2.convert(x2); 
     * then y1*y2 = c1.concatenate(c2).convert(x1*x2)[/code]
     *
     * @return <code>true</code> if this converter is linear;
     *         <code>false</code> otherwise.
     */
    public abstract boolean isLinear();

    /**
     * Indicates whether this converter is considered the same as the  
     * converter specified. To be considered equal this converter 
     * concatenated with the one specified must returns the {@link #IDENTITY}.
     *
     * @param  cvtr the converter with which to compare.
     * @return <code>true</code> if the specified object is a converter 
     *         considered equals to this converter;<code>false</code> otherwise.
     */
    public boolean equals(Object cvtr) {
        if (!(cvtr instanceof UnitConverter)) return false;
        return this.concatenate(((UnitConverter)cvtr).inverse()) == IDENTITY;        
    }

    /**
     * Returns a hash code value for this converter. Equals object have equal
     * hash codes.
     *
     * @return this converter hash code value.
     * @see    #equals
     */
    public int hashCode() {
        return Float.floatToIntBits((float)convert(1.0));
    }

    /**
     * Concatenates this converter with another converter. The resulting
     * converter is equivalent to first converting by the specified converter,
     * and then converting by this converter.
     * 
     * <p>Note: Implementations must ensure that the {@link #IDENTITY} instance
     *          is returned if the resulting converter is an identity 
     *          converter.</p> 
     * 
     * @param  converter the other converter.
     * @return the concatenation of this converter with the other converter.
     */
    public UnitConverter concatenate(UnitConverter converter) {
        return (converter == IDENTITY) ? this : new Compound(converter, this);
    }

    /**
     * This inner class represents the identity converter (singleton).
     */
    private static final class Identity extends UnitConverter {

        @Override
        public UnitConverter inverse() {
            return this;
        }

        @Override
        public double convert(double x) {
            return x;
        }

        @Override
        public boolean isLinear() {
            return true;
        }

        @Override
        public UnitConverter concatenate(UnitConverter converter) {
            return converter;
        }

        private static final long serialVersionUID = 1L;

    }

    /**
     * This inner class represents a compound converter.
     */
    private static final class Compound extends UnitConverter {

        /**
         * Holds the first converter.
         */
        private final UnitConverter _first;

        /**
         * Holds the second converter.
         */
        private final UnitConverter _second;

        /**
         * Creates a compound converter resulting from the combined
         * transformation of the specified converters.
         *
         * @param  first the first converter.
         * @param  second the second converter.
         */
        private Compound(UnitConverter first, UnitConverter second) {
            _first = first;
            _second = second;
        }

        @Override
        public UnitConverter inverse() {
            return new Compound(_second.inverse(), _first.inverse());
        }

        @Override
        public double convert(double x) {
            return _second.convert(_first.convert(x));
        }

        @Override
        public boolean isLinear() {
            return _first.isLinear() && _second.isLinear();
        }

        private static final long serialVersionUID = 1L;

    }
}