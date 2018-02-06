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
 * This interface represents the speed of data-transmission.
 * The system unit for this quantity is "bit/s" (bit per second).
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 1.0, January 14, 2006
 */
public interface DataRate extends Quantity {

    /**
     * Holds the SI unit (Système International d'Unités) for this quantity.
     */
    public final static Unit<DataRate> UNIT = new ProductUnit<DataRate>(SI.BIT.divide(SI.SECOND));

}