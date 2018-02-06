/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javax.measure.unit;

import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Quantity;

/**
 * <p> This class represents the multi-radix units (such as "hour:min:sec"). 
 *     Instances of this class are created using the {@link Unit#compound
 *     Unit.compound} method.</p>
 *      
 * <p> Examples of compound units:[code]
 *     Unit<Duration> HOUR_MINUTE_SECOND = HOUR.compound(MINUTE).compound(SECOND);
 *     Unit<Angle> DEGREE_MINUTE_ANGLE = DEGREE_ANGLE.compound(MINUTE_ANGLE);
 *     [/code]</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.1, April 22, 2006
 */
public final class CompoundUnit<Q extends Quantity> extends DerivedUnit<Q> {

    /**
     * Holds the higher unit.
     */
    private final Unit<Q> _high;

    /**
     * Holds the lower unit.
     */
    private final Unit<Q> _low;

    /**
     * Creates a compound unit from the specified units. 
     *
     * @param  high the high unit.
     * @param  low the lower unit(s)
     * @throws IllegalArgumentException if both units do not the same system
     *         unit.
     */
    CompoundUnit(Unit<Q> high, Unit<Q> low) {
        if (!high.getStandardUnit().equals(low.getStandardUnit()))
            throw new IllegalArgumentException(
                    "Both units do not have the same system unit");
        _high = high;
        _low = low;
        
    }

    /**
     * Returns the lower unit of this compound unit.
     *
     * @return the lower unit.
     */
    public Unit<Q> getLower() {
        return _low;
    }

    /**
     * Returns the higher unit of this compound unit.
     *
     * @return the higher unit.
     */
    public Unit<Q> getHigher() {
        return _high;
    }

    /**
     * Indicates if this compound unit is considered equals to the specified 
     * object (both are compound units with same composing units in the 
     * same order).
     *
     * @param  that the object to compare for equality.
     * @return <code>true</code> if <code>this</code> and <code>that</code>
     *         are considered equals; <code>false</code>otherwise. 
     */
    public boolean equals(Object that) {
        if (this == that)
            return true;
        if (!(that instanceof CompoundUnit))
            return false;
        CompoundUnit<?> thatUnit = (CompoundUnit<?>) that;
        return this._high.equals(thatUnit._high)
                && this._low.equals(thatUnit._low);
    }

    @Override
    public int hashCode() {
        return _high.hashCode() ^ _low.hashCode();
    }

    @Override
    public Unit<? super Q> getStandardUnit() {
        return _low.getStandardUnit(); 
    }

    @Override
    public UnitConverter toStandardUnit() {
        return _low.toStandardUnit();
    }

    private static final long serialVersionUID = 1L;
}