/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javax.measure.quantity;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

/**
 * This interface represents the luminous flux density per solid angle as
 * measured in a given direction relative to the emitting source. 
 * The system unit for this quantity is "cd" (candela).
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, January 14, 2006
 */
public interface LuminousIntensity extends Quantity {

    /**
     * Holds the SI unit (Système International d'Unités) for this quantity.
     */
    public final static Unit<LuminousIntensity> UNIT = SI.CANDELA;

}