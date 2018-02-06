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
 * <p> This class represents a converter adding a constant offset 
 *     (approximated as a <code>double</code>) to numeric values.</p>
 *     
 * <p> Instances of this class are immutable.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.1, April 22, 2006
 */
public final class AddConverter extends UnitConverter {

    /**
     * Holds the offset.
     */
    private final double _offset;

    /**
     * Creates an add converter with the specified offset.
     *
     * @param  offset the offset value.
     * @throws IllegalArgumentException if offset is zero (or close to zero).
     */
    public AddConverter(double offset) {
        if ((float)offset == 0.0)
            throw new IllegalArgumentException("Identity converter not allowed");
        _offset = offset;
    }

    /**
     * Returns the offset value for this add converter.
     *
     * @return the offset value.
     */
    public double getOffset() {
        return _offset;
    }
    
    @Override
    public UnitConverter inverse() {
        return new AddConverter(- _offset);
    }

    @Override
    public double convert(double amount) {
        return amount + _offset;
    }

    @Override
    public boolean isLinear() {
        return false;
    }

    @Override
    public UnitConverter concatenate(UnitConverter converter) {
        if (converter instanceof AddConverter) {
            double offset = _offset + ((AddConverter)converter)._offset;
            return valueOf(offset);
        } else {
            return super.concatenate(converter);
        }
    }

    private static UnitConverter valueOf(double offset) {
        float asFloat = (float) offset;
        return asFloat == 0.0f ? UnitConverter.IDENTITY : new AddConverter(offset);
    }
    
    private static final long serialVersionUID = 1L;
}