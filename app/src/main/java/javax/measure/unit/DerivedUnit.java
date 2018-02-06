/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javax.measure.unit;

import javax.measure.quantity.Quantity;

/**
 * <p> This class identifies the units created by combining or transforming
 *     other units.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 3.1, April 22, 2006
 */
public abstract class DerivedUnit<Q extends Quantity> extends Unit<Q> {

    /**
     * Default constructor.
     */
    protected DerivedUnit() {
    }
}