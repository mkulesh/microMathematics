/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javax.measure.unit;

import java.util.Set;

/**
 * <p> This class represents a system of units, it groups units together 
 *     for historical or cultural reasons. Nothing prevents a unit from 
 *     belonging to several system of units at the same time
 *     (for example an imperial system would have many of the units 
 *     held by {@link NonSI}).</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.2, August 26, 2007
 */
public abstract class SystemOfUnits {
    
    /**
     * Returns a read only view over the units defined in this system.
     *
     * @return the collection of units.
     */
    public abstract Set<Unit<?>> getUnits();

}
