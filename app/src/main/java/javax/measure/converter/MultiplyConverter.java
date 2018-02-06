/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javax.measure.converter;

/**
 * <p> This class represents a converter multiplying numeric values by a 
 *     constant scaling factor (approximated as a <code>double</code>). 
 *     For exact scaling conversions {@link RationalConverter} is preferred.</p>
 *      
 * <p> Instances of this class are immutable.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.1, April 22, 2006
 */
public final class MultiplyConverter extends UnitConverter {

    /**
     * Holds the scale factor.
     */
    private final double _factor;

    /**
     * Creates a multiply converter with the specified scale factor.
     *
     * @param  factor the scale factor.
     * @throws IllegalArgumentException if offset is one (or close to one).
     */
    public MultiplyConverter(double factor) {
        if ((float)factor == 1.0)
            throw new IllegalArgumentException("Identity converter not allowed");
        _factor = factor;
    }

    /**
     * Returns the scale factor.
     *
     * @return the scale factor.
     */
    public double getFactor() {
        return _factor;
    }

    @Override
    public UnitConverter inverse() {
        return new MultiplyConverter(1.0 / _factor);
    }

    @Override
    public double convert(double amount) {
        return _factor * amount;
    }

    @Override
    public boolean isLinear() {
        return true;
    }

    @Override
    public UnitConverter concatenate(UnitConverter converter) {
        if (converter instanceof MultiplyConverter) {
            double factor = _factor * ((MultiplyConverter) converter)._factor;
            return valueOf(factor);
        } else if (converter instanceof RationalConverter) {
            double factor = _factor
                    * ((RationalConverter) converter).getDividend()
                    / ((RationalConverter) converter).getDivisor();
            return valueOf(factor);
        } else {
            return super.concatenate(converter);
        }
    }

    private static UnitConverter valueOf(double factor) {
        float asFloat = (float) factor;
        return asFloat == 1.0f ? UnitConverter.IDENTITY
                : new MultiplyConverter(factor);
    }

    private static final long serialVersionUID = 1L;
}