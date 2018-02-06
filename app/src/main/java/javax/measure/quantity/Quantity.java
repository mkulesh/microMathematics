/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javax.measure.quantity;

/**
 * <p> This interface represents any type of quantitative properties or 
 *     attributes of thing. Mass, time, distance, heat, and angular separation 
 *     are among the familiar examples of quantitative properties.</p>
 *     
 * <p> Distinct quantities have usually different physical dimensions; although 
 *     it is not required nor necessary, for example {@link Torque} and 
 *     {@link Energy} have same dimension but are of different nature 
 *     (vector for torque, scalar for energy).</p>
 * 
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.0, February 25, 2007
 * @see <a href="http://en.wikipedia.org/wiki/Quantity">Wikipedia: Quantity</a>
 * @see <a href="http://en.wikipedia.org/wiki/Dimensional_analysis">
 *      Wikipedia: Dimensional Analysis</a>
 */
public interface Quantity  {
    
    // No method - Tagging interface.
    
}
