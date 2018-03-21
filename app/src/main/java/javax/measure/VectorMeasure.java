/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2007 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javax.measure;

import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Quantity;
import javax.measure.unit.CompoundUnit;
import javax.measure.unit.Unit;

/**
 * <p> This class represents a measurement vector of two or more dimensions.
 *     For example:[code]
 *         VectorMeasure<Length> dimension = VectorMeasure.valueOf(12.0, 30.0, 40.0, MILLIMETER);
 *         VectorMeasure<Velocity> v2d = VectorMeasure.valueOf(-2.2, -3.0, KNOTS);
 *         VectorMeasure<ElectricCurrent> c2d = VectorMeasure.valueOf(-7.3, 3.5, NANOAMPERE);
 *     [/code]
 * </p>
 *     
 * <p> Subclasses may provide fixed dimensions specializations:[code]
 *         class Velocity2D extends VectorMeasure<Velocity> {
 *              public Velocity2D(double x, double y, Unit<Velocity> unit) {
 *                  ...
 *              }
 *         }
 *     [/code]</p>
 *     
 * <p> Measurement vectors may use {@link CompoundUnit compound units}:[code]
 *     VectorMeasure<Angle> latLong = VectorMeasure.valueOf(12.345, 22.23, DEGREE_ANGLE);
 *     Unit<Angle> HOUR_MINUTE_SECOND_ANGLE = DEGREE_ANGLE.compound(MINUTE_ANGLE).compound(SECOND_ANGLE);
 *     System.out.println(latLong.to(HOUR_MINUTE_SECOND_ANGLE));
 *     
 *     > [12°19'42", 22°12'48"] [/code]</p>
 *     
 * <p> Instances of this class (and sub-classes) are immutable.</p>    
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.3, October 3, 2007
 */
public abstract class VectorMeasure<Q extends Quantity> extends Measure<double[], Q> {
    
    /**
     * Default constructor (for sub-classes). 
     */
    protected VectorMeasure() {
    }

    /**
     * Returns a 2-dimensional measurement vector.
     * 
     * @param x the first vector component value.
     * @param y the second vector component value.
     * @param unit the measurement unit.
     */
    public static <Q extends Quantity> VectorMeasure<Q> valueOf(
            double x, double y, Unit<Q> unit) {
        return new TwoDimensional<>(x, y, unit);
    }

    /**
     * Returns a 3-dimensional measurement vector.
     * 
     * @param x the first vector component value.
     * @param y the second vector component value.
     * @param z the third vector component value.
     * @param unit the measurement unit.
     */
    public static <Q extends Quantity> VectorMeasure<Q> valueOf(
            double x, double y, double z, Unit<Q> unit) {
        return new ThreeDimensional<>(x, y, z, unit);
    }

    /**
     * Returns a multi-dimensional measurement vector.
     * 
     * @param components the vector component values.
     * @param unit the measurement unit.
     */
    public static <Q extends Quantity> VectorMeasure<Q> valueOf(double[] components, 
            Unit<Q> unit) {
        return new MultiDimensional<>(components, unit);
    }
    
    /**
     * Returns the measurement vector equivalent to this one but stated in the 
     * specified unit.
     * 
     * @param unit the new measurement unit.
     * @return the vector measure stated in the specified unit.
     */
    public abstract VectorMeasure<Q> to(Unit<Q> unit);

    /**
     * Returns the norm of this measurement vector stated in the specified
     * unit.
     * 
     * @param unit the unit in which the norm is stated.
     * @return <code>|this|</code>
     */
    public abstract double doubleValue(Unit<Q> unit);
    
    /**
     * Returns the <code>String</code> representation of this measurement
     * vector (for example <code>[2.3 m/s, 5.6 m/s]</code>).
     * 
     * @return the textual representation of the measurement vector.
     */
    public String toString() {
        double[] values = getValue();
        Unit<Q> unit = getUnit();
        StringBuffer tmp = new StringBuffer();
        tmp.append('[');
        for (double v : values) {
            if (tmp.length() > 1) {
                tmp.append(", ");
            }
            if (unit instanceof CompoundUnit) {
                MeasureFormat.DEFAULT.formatCompound(v, unit, tmp, null);
            } else {
                tmp.append(v).append(" ").append(unit);
            }
        }
        tmp.append("] ");
        return tmp.toString();
    }

    // Holds 2-dimensional implementation.
    private static class TwoDimensional<Q extends Quantity> extends VectorMeasure<Q> {
        
        private final double _x;
        
        private final double _y;
        
        private final Unit<Q> _unit;
        
        private TwoDimensional(double x, double y, Unit<Q> unit) {
            _x = x;
            _y = y;
            _unit = unit;
            
        }
        @Override
        public double doubleValue(Unit<Q> unit) {
            double norm = Math.sqrt(_x * _x + _y * _y); 
            if ((unit == _unit) || (unit.equals(_unit)))
                return norm;
            return _unit.getConverterTo(unit).convert(norm);            
        }

        @Override
        public Unit<Q> getUnit() {
            return _unit;
        }

        @Override
        public double[] getValue() {
            return new double[] { _x, _y };
        }

        @Override
        public TwoDimensional<Q> to(Unit<Q> unit) {
            if ((unit == _unit) || (unit.equals(_unit)))
                return this;
            UnitConverter cvtr = _unit.getConverterTo(unit);
            return new TwoDimensional<>(cvtr.convert(_x), cvtr.convert(_y), unit);
        } 

        private static final long serialVersionUID = 1L;

    }
    
    // Holds 3-dimensional implementation.
    private static class ThreeDimensional<Q extends Quantity> extends VectorMeasure<Q> {
        
        private final double _x;
        
        private final double _y;
        
        private final double _z;
        
        private final Unit<Q> _unit;
        
        private ThreeDimensional(double x, double y, double z, Unit<Q> unit) {
            _x = x;
            _y = y;
            _z = z;
            _unit = unit;
            
        }
        @Override
        public double doubleValue(Unit<Q> unit) {
            double norm = Math.sqrt(_x * _x + _y * _y + _z * _z); 
            if ((unit == _unit) || (unit.equals(_unit)))
                return norm;
            return _unit.getConverterTo(unit).convert(norm);            
        }

        @Override
        public Unit<Q> getUnit() {
            return _unit;
        }

        @Override
        public double[] getValue() {
            return new double[] { _x, _y, _z };
        }

        @Override
        public ThreeDimensional<Q> to(Unit<Q> unit) {
            if ((unit == _unit) || (unit.equals(_unit)))
                return this;
            UnitConverter cvtr = _unit.getConverterTo(unit);
            return new ThreeDimensional<>(cvtr.convert(_x), cvtr.convert(_y), cvtr.convert(_z), unit);
        } 

        private static final long serialVersionUID = 1L;

    }
    // Holds multi-dimensional implementation.
    private static class MultiDimensional<Q extends Quantity> extends VectorMeasure<Q> {
        
        private final double[] _components;
        
        private final Unit<Q> _unit;
        
        private MultiDimensional(double[] components, Unit<Q> unit) {
            _components = components.clone();
            _unit = unit;            
        }
        
        @Override
        public double doubleValue(Unit<Q> unit) {
            double normSquare = _components[0] * _components[0];
            for (int i=1, n=_components.length; i < n;) {
                double d = _components[i++];
                normSquare += d * d;
            }
            if ((unit == _unit) || (unit.equals(_unit)))
                return Math.sqrt(normSquare);
            return _unit.getConverterTo(unit).convert(Math.sqrt(normSquare));            
        }

        @Override
        public Unit<Q> getUnit() {
            return _unit;
        }

        @Override
        public double[] getValue() {
            return _components.clone();
        }

        @Override
        public MultiDimensional<Q> to(Unit<Q> unit) {
            if ((unit == _unit) || (unit.equals(_unit)))
                return this;
            UnitConverter cvtr = _unit.getConverterTo(unit);
            double[] newValues = new double[_components.length];
            for (int i=0; i < _components.length; i++) {
                newValues[i] = cvtr.convert(_components[i]);
            }
            return new MultiDimensional<>(newValues, unit);
        } 

        private static final long serialVersionUID = 1L;

    }
}
