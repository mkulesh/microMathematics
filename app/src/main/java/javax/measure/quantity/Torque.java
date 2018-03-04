/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javax.measure.quantity;
import javax.measure.unit.ProductUnit;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

/**
 * This interface represents the moment of a force. The system unit for this
 * quantity is "N·m" (Newton-Meter).
 * 
 * <p> Note: The Newton-metre ("N·m") is also a way of exressing a Joule (unit
 *     of energy). However, torque is not energy. So, to avoid confusion, we
 *     will use the units "N·m" for torque and not "J". This distinction occurs
 *     due to the scalar nature of energy and the vector nature of torque.</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, January 14, 2006
 */
public interface Torque extends Quantity {

    /**
     * Holds the SI unit (Système International d'Unités) for this quantity.
     */
    Unit<Torque> UNIT =
        new ProductUnit<Torque>(SI.NEWTON.times(SI.METRE));

}
