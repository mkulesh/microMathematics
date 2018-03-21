/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javax.measure.unit;

import java.io.Serializable;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.HashMap;

import javax.measure.MeasureFormat;
import javax.measure.converter.AddConverter;
import javax.measure.converter.ConversionException;
import javax.measure.converter.MultiplyConverter;
import javax.measure.converter.RationalConverter;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Quantity;

/**
 * <p> This class represents a determinate {@link javax.measure.quantity.Quantity
 *     quantity} (as of length, time, heat, or value) adopted as a standard
 *     of measurement.</p>
 *
 * <p> It is helpful to think of instances of this class as recording the
 *     history by which they are created. Thus, for example, the string
 *     "g/kg" (which is a dimensionless unit) would result from invoking
 *     the method toString() on a unit that was created by dividing a
 *     gram unit by a kilogram unit. Yet, "kg" divided by "kg" returns
 *     {@link #ONE} and not "kg/kg" due to automatic unit factorization.</p>
 *
 * <p> This class supports the multiplication of offsets units. The result is
 *     usually a unit not convertible to its {@link #getStandardUnit standard unit}.
 *     Such units may appear in derivative quantities. For example °C/m is an 
 *     unit of gradient, which is common in atmospheric and oceanographic
 *     research.</p>
 *
 * <p> Units raised at rational powers are also supported. For example
 *     the cubic root of "liter" is a unit compatible with meter.</p>
 *     
 * <p> Instances of this class are immutable.</p>
 *
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @author  <a href="mailto:steve@unidata.ucar.edu">Steve Emmerson</a>
 * @author  Martin Desruisseaux
 * @version 3.2, August 28, 2006
 * @see <a href="http://en.wikipedia.org/wiki/Units_of_measurement">
 *       Wikipedia: Units of measurement</a>
 */
public abstract class Unit<Q extends Quantity> implements Serializable {

    /**
     * Holds the dimensionless unit <code>ONE</code>.
     */
    public static final Unit<Dimensionless> ONE = new ProductUnit<>();

    /**
     * Holds the unique symbols collection (base unit or alternate units).
     */
    static final HashMap<String, Unit<?>> SYMBOL_TO_UNIT = new HashMap<>();

    /**
     * Default constructor.
     */
    protected Unit() {
    }

    //////////////////////////////////////////////////////
    // Contract methods (for sub-classes to implement). //
    //////////////////////////////////////////////////////

    /**
     * Returns the {@link BaseUnit base unit}, {@link AlternateUnit alternate
     * unit} or product of base units and alternate units this unit is derived
     * from. The standard unit identifies the "type" of 
     * {@link javax.measure.quantity.Quantity quantity} for which this unit is employed.
     * For example:[code]
     *    boolean isAngularVelocity(Unit<?> u) {
     *       return u.getStandardUnit().equals(RADIAN.divide(SECOND));
     *    }
     *    assert(REVOLUTION.divide(MINUTE).isAngularVelocity());  
     * [/code]
     * 
     * <p><i> Note: Having the same system unit is not sufficient to ensure
     *              that a converter exists between the two units
     *              (e.g. °C/m and K/m).</i></p>
     * @return the system unit this unit is derived from.
     */
    public abstract Unit<? super Q> getStandardUnit();

    
    /**
     * Returns the converter from this unit to its system unit.
     * 
     * @return <code>this.getConverterTo(this.getSystemUnit())</code>
     */
    public abstract UnitConverter toStandardUnit();

    /**
     * Returns the hash code for this unit.
     *
     * @return this unit hashcode value.
     */
    public abstract int hashCode();

    /**
     * Indicates if the specified unit can be considered equals to 
     * the one specified.
     *
     * @param that the object to compare to.
     * @return <code>true</code> if this unit is considered equal to 
     *         that unit; <code>false</code> otherwise.
     */
    public abstract boolean equals(Object that);

    /**
     * Indicates if this unit is a standard unit (base units and 
     * alternate units are standard units). The standard unit identifies 
     * the "type" of {@link javax.measure.quantity.Quantity quantity} for 
     * which the unit is employed.
     * 
     * @return <code>getStandardUnit().equals(this)</code>
     */
    public boolean isStandardUnit() {
        return getStandardUnit().equals(this);
    }

    /**
     * Indicates if this unit is compatible with the unit specified.
     * Units don't need to be equals to be compatible. For example:[code]
     *     RADIAN.equals(ONE) == false
     *     RADIAN.isCompatible(ONE) == true
     * [/code]
     * @param  that the other unit.
     * @return <code>this.getDimension().equals(that.getDimension())</code>
     * @see #getDimension()
     */
    public final boolean isCompatible(Unit<?> that) {
        return (this == that)
                || this.getStandardUnit().equals(that.getStandardUnit())
                || this.getDimension().equals(that.getDimension());
    }

    /**
     * Casts this unit to a parameterized unit of specified nature or 
     * throw a <code>ClassCastException</code> if the dimension of the 
     * specified quantity and this unit's dimension do not match.
     * For example:[code]
     * Unit<Length> LIGHT_YEAR = NonSI.C.times(NonSI.YEAR).asType(Length.class);
     * [/code]
     * 
     * @param  type the quantity class identifying the nature of the unit.
     * @return this unit parameterized with the specified type.
     * @throws ClassCastException if the dimension of this unit is different 
     *         from the specified quantity dimension.
     * @throws UnsupportedOperationException if the specified quantity class
     *         does not have a public static field named "UNIT" holding the 
     *         standard unit for the quantity.
     */
    @SuppressWarnings("unchecked")
    public final <T extends Quantity> Unit<T> asType(Class<T> type) throws ClassCastException {
        Dimension dim1 = this.getDimension();
        Unit<T> u = null;
        try {
            u = (Unit<T>)type.getField("UNIT").get(null);
        } catch (Exception e) {
            throw new Error(e);
        }
        Dimension dim2 = u.getDimension();
        if (!dim1.equals(dim2))
            throw new ClassCastException();
        return (Unit<T>)this;
    }
   
    /**
     * Returns the dimension of this unit (depends upon the current 
     * dimensional {@link Dimension.Model model}). 
     *
     * @return the dimension of this unit for the current model.
     */
    public final Dimension getDimension() {
        Unit<?> systemUnit = this.getStandardUnit();
        if (systemUnit instanceof BaseUnit)
            return Dimension.getModel().getDimension((BaseUnit<?>) systemUnit);
        if (systemUnit instanceof AlternateUnit)
            return ((AlternateUnit<?>) systemUnit).getParent().getDimension();
        // Product of units.
        ProductUnit<?> productUnit = (ProductUnit<?>) systemUnit;
        Dimension dimension = Dimension.NONE;
        for (int i = 0; i < productUnit.getUnitCount(); i++) {
            Unit<?> unit = productUnit.getUnit(i);
            Dimension d = unit.getDimension().pow(productUnit.getUnitPow(i))
                    .root(productUnit.getUnitRoot(i));
            dimension = dimension.times(d);
        }
        return dimension;
    }

    /**
     * Returns a converter of numeric values from this unit to another unit.
     *
     * @param  that the unit to which to convert the numeric values.
     * @return the converter from this unit to <code>that</code> unit.
     * @throws ConversionException if the conveter cannot be constructed
     *         (e.g. <code>!this.isCompatible(that)</code>).
     */
    public final UnitConverter getConverterTo(Unit<?> that)
            throws ConversionException {
        if (this.equals(that))
            return UnitConverter.IDENTITY;
        Unit<?> thisSystemUnit = this.getStandardUnit();
        Unit<?> thatSystemUnit = that.getStandardUnit();
        if (thisSystemUnit.equals(thatSystemUnit))
            return that.toStandardUnit().inverse().concatenate(
                    this.toStandardUnit());
        // Use dimensional transforms.
        if (!thisSystemUnit.getDimension()
                .equals(thatSystemUnit.getDimension()))
            throw new ConversionException(this + " is not compatible with "
                    + that);
        // Transform between SystemUnit and BaseUnits is Identity. 
        UnitConverter thisTransform = this.toStandardUnit().concatenate(
                transformOf(this.getBaseUnits()));
        UnitConverter thatTransform = that.toStandardUnit().concatenate(
                transformOf(that.getBaseUnits()));
        return thatTransform.inverse().concatenate(thisTransform);
    }

    private Unit<?> getBaseUnits() {
        Unit<?> systemUnit = this.getStandardUnit();
        if (systemUnit instanceof BaseUnit) return systemUnit;
        if (systemUnit instanceof AlternateUnit) 
            return ((AlternateUnit<?>)systemUnit).getParent().getBaseUnits();
        if (systemUnit instanceof ProductUnit) {
            ProductUnit<?> productUnit = (ProductUnit<?>)systemUnit;
            Unit<?> baseUnits = ONE;
            for (int i = 0; i < productUnit.getUnitCount(); i++) {
                Unit<?> unit = productUnit.getUnit(i).getBaseUnits();
                unit = unit.pow(productUnit.getUnitPow(i));
                unit = unit.root(productUnit.getUnitRoot(i));
                baseUnits = baseUnits.times(unit);
            }
            return baseUnits;
        } else {
            throw new InternalError(
                    "System Unit cannot be an instance of " + this.getClass());            
        }        
    }
    private static UnitConverter transformOf(Unit<?> baseUnits) {
        if (baseUnits instanceof BaseUnit)
            return Dimension.getModel().getTransform((BaseUnit<?>) baseUnits);
        // Product of units.
        ProductUnit<?> productUnit = (ProductUnit<?>) baseUnits;
        UnitConverter converter = UnitConverter.IDENTITY;
        for (int i = 0; i < productUnit.getUnitCount(); i++) {
            Unit<?> unit = productUnit.getUnit(i);
            UnitConverter cvtr = transformOf(unit);
            if (!cvtr.isLinear())
                throw new ConversionException(baseUnits
                        + " is non-linear, cannot convert");
            if (productUnit.getUnitRoot(i) != 1)
                throw new ConversionException(productUnit
                        + " holds a base unit with fractional exponent");
            int pow = productUnit.getUnitPow(i);
            if (pow < 0) { // Negative power.
                pow = -pow;
                cvtr = cvtr.inverse();
            }
            for (int j = 0; j < pow; j++) {
                converter = converter.concatenate(cvtr);
            }
        }
        return converter;
    }

    /**
     * Returns a unit equivalent to this unit but used in expressions to 
     * distinguish between quantities of a different nature but of the same
     * dimensions.
     *     
     * <p> Examples of alternate units:[code]
     *     Unit<Angle> RADIAN = ONE.alternate("rad");
     *     Unit<Force> NEWTON = METER.times(KILOGRAM).divide(SECOND.pow(2)).alternate("N");
     *     Unit<Pressure> PASCAL = NEWTON.divide(METER.pow(2)).alternate("Pa");
     *     [/code]</p>
     *
     * @param symbol the new symbol for the alternate unit.
     * @return the alternate unit.
     * @throws UnsupportedOperationException if this unit is not a standard unit.
     * @throws IllegalArgumentException if the specified symbol is already 
     *         associated to a different unit.
     */
    public final <A extends Quantity> AlternateUnit<A> alternate(String symbol) {
        return new AlternateUnit<>(symbol, this);
    }

    /**
     * Returns the combination of this unit with the specified sub-unit.
     * Compound units are typically used for formatting purpose. 
     * Examples of compound units:[code]
     *   HOUR_MINUTE = NonSI.HOUR.compound(NonSI.MINUTE);
     *   DEGREE_MINUTE_SECOND_ANGLE = NonSI.DEGREE_ANGLE.compound(
     *       NonSI.DEGREE_MINUTE).compound(NonSI.SECOND_ANGLE);
     *  [/code]
     *
     * @param  subunit the sub-unit to combine with this unit.
     * @return the corresponding compound unit.
     */
    public final CompoundUnit<Q> compound(Unit<Q> subunit) {
        return new CompoundUnit<>(this, subunit);
    }

    /**
     * Returns the unit derived from this unit using the specified converter.
     * The converter does not need to be linear. For example:[code]
     * Unit<Dimensionless> DECIBEL = Unit.ONE.transform(
     *     new LogConverter(10).inverse().concatenate(
     *           new RationalConverter(1, 10)));[/code]
     *
     * @param operation the converter from the transformed unit to this unit.
     * @return the unit after the specified transformation.
     */
    public final Unit<Q> transform(UnitConverter operation) {
        if (this instanceof TransformedUnit) {
            TransformedUnit<Q> tf = (TransformedUnit<Q>) this;
            Unit<Q> parent = tf.getParentUnit();
            UnitConverter toParent = tf.toParentUnit().concatenate(operation);
            if (toParent == UnitConverter.IDENTITY)
                return parent;
            return new TransformedUnit<>(parent, toParent);
        }
        if (operation == UnitConverter.IDENTITY) 
            return this;
        return new TransformedUnit<>(this, operation);
    }

    /**
     * Returns the result of adding an offset to this unit. The returned unit
     * is convertible with all units that are convertible with this unit.
     *
     * @param  offset the offset added (expressed in this unit,
     *         e.g. <code>CELSIUS = KELVIN.plus(273.15)</code>).
     * @return <code>this.transform(new AddConverter(offset))</code>
     */
    public final Unit<Q> plus(double offset) {
        return transform(new AddConverter(offset));
    }

    /**
     * Returns the result of multiplying this unit by an exact factor. 
     *
     * @param  factor the exact scale factor
     *         (e.g. <code>KILOMETER = METER.times(1000)</code>).
     * @return <code>this.transform(new RationalConverter(factor, 1))</code>
     */
    public final Unit<Q> times(long factor) {
        return transform(new RationalConverter(factor, 1));
    }

    /**
     * Returns the result of multiplying this unit by a an approximate factor
     *
     * @param  factor the approximate factor (e.g. 
     *         <code>ELECTRON_MASS = KILOGRAM.times(9.10938188e-31)</code>).
     * @return <code>this.transform(new MultiplyConverter(factor))</code>
     */
    public final Unit<Q> times(double factor) {
        return transform(new MultiplyConverter(factor));
    }

    /**
     * Returns the product of this unit with the one specified.
     *
     * @param  that the unit multiplicand.
     * @return <code>this * that</code>
     */
    public final Unit<? extends Quantity> times(Unit<?> that) {
        return ProductUnit.getProductInstance(this, that);
    }

    /**
     * Returns the inverse of this unit.
     *
     * @return <code>1 / this</code>
     */
    public final Unit<? extends Quantity> inverse() {
        return ProductUnit.getQuotientInstance(ONE, this);
    }

    /**
     * Returns the result of dividing this unit by an exact divisor.
     *
     * @param  divisor the exact divisor.
     *         (e.g. <code>QUART = GALLON_LIQUID_US.divide(4)</code>).
     * @return <code>this.transform(new RationalConverter(1 , divisor))</code>
     */
    public final Unit<Q> divide(long divisor) {
        return transform(new RationalConverter(1, divisor));
    }

    /**
     * Returns the result of dividing this unit by an approximate divisor.
     *
     * @param  divisor the approximate divisor.
     * @return <code>this.transform(new MultiplyConverter(1.0 / divisor))</code>
     */
    public final Unit<Q> divide(double divisor) {
        return transform(new MultiplyConverter(1.0 / divisor));
    }

    /**
     * Returns the quotient of this unit with the one specified.
     *
     * @param  that the unit divisor.
     * @return <code>this / that</code>
     */
    public final Unit<? extends Quantity> divide(Unit<?> that) {
        return this.times(that.inverse());
    }

    /**
     * Returns a unit equals to the given root of this unit.
     *
     * @param  n the root's order.
     * @return the result of taking the given root of this unit.
     * @throws ArithmeticException if <code>n == 0</code>.
     */
    public final Unit<? extends Quantity> root(int n) {
        if (n > 0) {
            return ProductUnit.getRootInstance(this, n);
        } else if (n == 0) {
            throw new ArithmeticException("Root's order of zero");
        } else { // n < 0
            return ONE.divide(this.root(-n));
        }
    }

    /**
     * Returns a unit equals to this unit raised to an exponent.
     *
     * @param  n the exponent.
     * @return the result of raising this unit to the exponent.
     */
    public final Unit<? extends Quantity> pow(int n) {
        if (n > 0) {
            return this.times(this.pow(n - 1));
        } else if (n == 0) {
            return ONE;
        } else { // n < 0
            return ONE.divide(this.pow(-n));
        }
    }

    /**
     * Returns a unit instance that is defined from the specified
     * character sequence using the {@link UnitFormat#getInstance()
     * standard unit format}.
     * <p> Examples of valid entries (all for meters per second squared) are:
     *     <code><ul>
     *       <li>m*s-2</li>
     *       <li>m/s²</li>
     *       <li>m·s-²</li>
     *       <li>m*s**-2</li>
     *       <li>m^+1 s^-2</li>
     *     </ul></code></p>
     *
     * @param  csq the character sequence to parse.
     * @return <code>UnitFormat.getStandardInstance().parse(csq, new ParsePosition(0))</code>
     * @throws IllegalArgumentException if the specified character sequence
     *         cannot be correctly parsed (e.g. symbol unknown).
     */
    public static Unit<? extends Quantity> valueOf(CharSequence csq) {
        try {
            return UnitFormat.getInstance()
                    .parseProductUnit(csq, new ParsePosition(0));
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    //////////////////////
    // GENERAL CONTRACT //
    //////////////////////

    /**
     * Returns the standard <code>String</code> representation of this unit.
     * This representation is not affected by locale. Locale-sensitive
     * unit formatting and parsing is handled by the {@link MeasureFormat} 
     * class and its subclasses.    
     *  
     * @return <code>UnitFormat.getStandardInstance().format(this)</code>
     */
    public final String toString() {
        return UnitFormat.getInstance().format(this);
    }
}