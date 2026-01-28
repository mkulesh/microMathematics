/*
 * JScience - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2006 - JScience (http://jscience.org/)
 * All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software is
 * freely granted, provided that this notice is preserved.
 */
package javax.measure.unit;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.measure.converter.MultiplyConverter;
import javax.measure.converter.RationalConverter;
import javax.measure.quantity.*;

/**
 * <p> This class contains SI (Système International d'Unités) base units,
 *     and derived units.</p>
 *     
 * <p> It also defines the 20 SI prefixes used to form decimal multiples and
 *     submultiples of SI units. For example:[code]
 *     import static org.jscience.physics.units.SI.*; // Static import.
 *     ...
 *     Unit<Pressure> HECTO_PASCAL = HECTO(PASCAL);
 *     Unit<Length> KILO_METER = KILO(METER);
 *     [/code]</p>
 *     
 * @author  <a href="mailto:jean-marie@dautelle.com">Jean-Marie Dautelle</a>
 * @version 4.2, August 26, 2006
 * @see <a href="http://en.wikipedia.org/wiki/SI">Wikipedia: SI</a>
 * @see <a href="http://en.wikipedia.org/wiki/SI_prefix">Wikipedia: SI prefix</a>
 */
public final class SI extends SystemOfUnits {

    /**
     * Holds collection of SI units.
     */
    private static HashSet<Unit<?>> UNITS = new HashSet<>();

    /**
     * Default constructor (prevents this class from being instantiated).
     */
    private SI() {
    }


    /**
     * Returns the unique instance of this class.
     *
     * @return the SI instance.
     */
    public static SI getInstance() {
        return INSTANCE;
    }
    private static final SI INSTANCE = new SI();
    
    ////////////////
    // BASE UNITS //
    ////////////////

    /**
     * The base unit for electric current quantities (<code>A</code>).
     * The Ampere is that constant current which, if maintained in two straight
     * parallel conductors of infinite length, of negligible circular
     * cross-section, and placed 1 metre apart in vacuum, would produce between
     * these conductors a force equal to 2 × 10-7 newton per metre of length.
     * It is named after the French physicist Andre Ampere (1775-1836).
     */
    public static final BaseUnit<ElectricCurrent> AMPERE = si(new BaseUnit<ElectricCurrent>(
            "A"));

    /**
     * The base unit for luminous intensity quantities (<code>cd</code>).
     * The candela is the luminous intensity, in a given direction,
     * of a source that emits monochromatic radiation of frequency
     * 540 × 1012 hertz and that has a radiant intensity in that
     * direction of 1/683 watt per steradian
     * @see <a href="http://en.wikipedia.org/wiki/Candela"> 
     *      Wikipedia: Candela</a>
     */
    public static final BaseUnit<LuminousIntensity> CANDELA = si(new BaseUnit<LuminousIntensity>(
            "cd"));

    /**
     * The base unit for thermodynamic temperature quantities (<code>K</code>).
     * The kelvin is the 1/273.16th of the thermodynamic temperature of the
     * triple point of water. It is named after the Scottish mathematician and
     * physicist William Thomson 1st Lord Kelvin (1824-1907)
     */
    public static final BaseUnit<Temperature> KELVIN = si(new BaseUnit<Temperature>(
            "K"));

    /**
     * The base unit for mass quantities (<code>kg</code>).
     * It is the only SI unit with a prefix as part of its name and symbol.
     * The kilogram is equal to the mass of an international prototype in the
     * form of a platinum-iridium cylinder kept at Sevres in France.
     * @see   #GRAM
     */
    public static final BaseUnit<Mass> KILOGRAM = si(new BaseUnit<Mass>("kg"));

    /**
     * The base unit for length quantities (<code>m</code>).
     * One meter was redefined in 1983 as the distance traveled by light in
     * a vacuum in 1/299,792,458 of a second.
     */
    public static final BaseUnit<Length> METRE = si(new BaseUnit<Length>("m"));

    /**
     * Equivalent to {@link #METRE} (American spelling).
     */
    public static final Unit<Length> METER = METRE;

    /**
     * The base unit for amount of substance quantities (<code>mol</code>).
     * The mole is the amount of substance of a system which contains as many
     * elementary entities as there are atoms in 0.012 kilogram of carbon 12.
     */
    public static final BaseUnit<AmountOfSubstance> MOLE = si(new BaseUnit<AmountOfSubstance>(
            "mol"));

    /**
     * The base unit for duration quantities (<code>s</code>).
     * It is defined as the duration of 9,192,631,770 cycles of radiation
     * corresponding to the transition between two hyperfine levels of
     * the ground state of cesium (1967 Standard).
     */
    public static final BaseUnit<Duration> SECOND = si(new BaseUnit<Duration>(
            "s"));

    ////////////////////////////////
    // SI DERIVED ALTERNATE UNITS //
    ////////////////////////////////

    /**
     * The derived unit for mass quantities (<code>g</code>).
     * The base unit for mass quantity is {@link #KILOGRAM}.
     */
    public static final Unit<Mass> GRAM = KILOGRAM.divide(1000);

    /**
     * The unit for plane angle quantities (<code>rad</code>).
     * One radian is the angle between two radii of a circle such that the
     * length of the arc between them is equal to the radius.
     */
    public static final AlternateUnit<Angle> RADIAN = si(new AlternateUnit<Angle>(
            "rad", Unit.ONE));

    /**
     * The unit for solid angle quantities (<code>sr</code>).
     * One steradian is the solid angle subtended at the center of a sphere by
     * an area on the surface of the sphere that is equal to the radius squared.
     * The total solid angle of a sphere is 4*Pi steradians.
     */
    public static final AlternateUnit<SolidAngle> STERADIAN = si(new AlternateUnit<SolidAngle>(
            "sr", Unit.ONE));

    /**
     * The unit for binary information (<code>bit</code>).
     */
    public static final BaseUnit<DataAmount> BIT = si(new BaseUnit<DataAmount>("bit"));

    /**
     * The unit for bit rate (<code>bps</code>).
     */
    public static final AlternateUnit<DataRate> BIT_PER_SECOND = si(new AlternateUnit<DataRate>(
            "bps", BIT.divide(SECOND)));
    /**
     * The derived unit for frequency (<code>Hz</code>).
     * A unit of frequency equal to one cycle per second.
     * After Heinrich Rudolf Hertz (1857-1894), German physicist who was the
     * first to produce radio waves artificially.
     */
    public static final AlternateUnit<Frequency> HERTZ = si(new AlternateUnit<Frequency>(
            "Hz", Unit.ONE.divide(SECOND)));

    /**
     * The derived unit for force (<code>N</code>).
     * One newton is the force required to give a mass of 1 kilogram an Force
     * of 1 metre per second per second. It is named after the English
     * mathematician and physicist Sir Isaac Newton (1642-1727).
     */
    public static final AlternateUnit<Force> NEWTON = si(new AlternateUnit<Force>(
            "N", METRE.times(KILOGRAM).divide(SECOND.pow(2))));

    /**
     * The derived unit for pressure, stress (<code>Pa</code>).
     * One pascal is equal to one newton per square meter. It is named after
     * the French philosopher and mathematician Blaise Pascal (1623-1662).
     */
    public static final AlternateUnit<Pressure> PASCAL = si(new AlternateUnit<Pressure>(
            "Pa", NEWTON.divide(METRE.pow(2))));

    /**
     * The derived unit for energy, work, quantity of heat (<code>J</code>).
     * One joule is the amount of work done when an applied force of 1 newton
     * moves through a distance of 1 metre in the direction of the force.
     * It is named after the English physicist James Prescott Joule (1818-1889).
     */
    public static final AlternateUnit<Energy> JOULE = si(new AlternateUnit<Energy>(
            "J", NEWTON.times(METRE)));

    /**
     * The derived unit for power, radiant, flux (<code>W</code>).
     * One watt is equal to one joule per second. It is named after the British
     * scientist James Watt (1736-1819).
     */
    public static final AlternateUnit<Power> WATT = si(new AlternateUnit<Power>(
            "W", JOULE.divide(SECOND)));

    /**
     * The derived unit for electric charge, quantity of electricity
     * (<code>C</code>).
     * One Coulomb is equal to the quantity of charge transferred in one second
     * by a steady current of one ampere. It is named after the French physicist
     * Charles Augustin de Coulomb (1736-1806).
     */
    public static final AlternateUnit<ElectricCharge> COULOMB = si(new AlternateUnit<ElectricCharge>(
            "C", SECOND.times(AMPERE)));

    /**
     * The derived unit for electric potential difference, electromotive force
     * (<code>V</code>).
     * One Volt is equal to the difference of electric potential between two
     * points on a conducting wire carrying a constant current of one ampere
     * when the power dissipated between the points is one watt. It is named
     * after the Italian physicist Count Alessandro Volta (1745-1827).
     */
    public static final AlternateUnit<ElectricPotential> VOLT = si(new AlternateUnit<ElectricPotential>(
            "V", WATT.divide(AMPERE)));

    /**
     * The derived unit for capacitance (<code>F</code>).
     * One Farad is equal to the capacitance of a capacitor having an equal
     * and opposite charge of 1 coulomb on each plate and a potential difference
     * of 1 volt between the plates. It is named after the British physicist
     * and chemist Michael Faraday (1791-1867).
     */
    public static final AlternateUnit<ElectricCapacitance> FARAD = si(new AlternateUnit<ElectricCapacitance>(
            "F", COULOMB.divide(VOLT)));

    /**
     * The derived unit for electric resistance (<code>Ω</code> or 
     * <code>Ohm</code>).
     * One Ohm is equal to the resistance of a conductor in which a current of
     * one ampere is produced by a potential of one volt across its terminals.
     * It is named after the German physicist Georg Simon Ohm (1789-1854).
     */
    public static final AlternateUnit<ElectricResistance> OHM = si(new AlternateUnit<ElectricResistance>(
            "Ω", VOLT.divide(AMPERE)));

    /**
     * The derived unit for electric conductance (<code>S</code>).
     * One Siemens is equal to one ampere per volt. It is named after
     * the German engineer Ernst Werner von Siemens (1816-1892).
     */
    public static final AlternateUnit<ElectricConductance> SIEMENS = si(new AlternateUnit<ElectricConductance>(
            "S", AMPERE.divide(VOLT)));

    /**
     * The derived unit for magnetic flux (<code>Wb</code>).
     * One Weber is equal to the magnetic flux that in linking a circuit of one
     * turn produces in it an electromotive force of one volt as it is uniformly
     * reduced to zero within one second. It is named after the German physicist
     * Wilhelm Eduard Weber (1804-1891).
     */
    public static final AlternateUnit<MagneticFlux> WEBER = si(new AlternateUnit<MagneticFlux>(
            "Wb", VOLT.times(SECOND)));

    /**
     * The derived unit for magnetic flux density (<code>T</code>).
     * One Tesla is equal equal to one weber per square meter. It is named
     * after the Serbian-born American electrical engineer and physicist
     * Nikola Tesla (1856-1943).
     */
    public static final AlternateUnit<MagneticFluxDensity> TESLA = si(new AlternateUnit<MagneticFluxDensity>(
            "T", WEBER.divide(METRE.pow(2))));

    /**
     * The derived unit for inductance (<code>H</code>).
     * One Henry is equal to the inductance for which an induced electromotive
     * force of one volt is produced when the current is varied at the rate of
     * one ampere per second. It is named after the American physicist
     * Joseph Henry (1791-1878).
     */
    public static final AlternateUnit<ElectricInductance> HENRY = si(new AlternateUnit<ElectricInductance>(
            "H", WEBER.divide(AMPERE)));

    /**
     * The derived unit for Celsius temperature (<code>℃</code>).
     * This is a unit of temperature such as the freezing point of water
     * (at one atmosphere of pressure) is 0 ℃, while the boiling point is
     * 100 ℃.
     */
    public static final Unit<Temperature> CELSIUS = si(KELVIN.plus(273.15));

    /**
     * The derived unit for luminous flux (<code>lm</code>).
     * One Lumen is equal to the amount of light given out through a solid angle
     * by a source of one candela intensity radiating equally in all directions.
     */
    public static final AlternateUnit<LuminousFlux> LUMEN = si(new AlternateUnit<LuminousFlux>(
            "lm", CANDELA.times(STERADIAN)));

    /**
     * The derived unit for illuminance (<code>lx</code>).
     * One Lux is equal to one lumen per square meter.
     */
    public static final AlternateUnit<Illuminance> LUX = si(new AlternateUnit<Illuminance>(
            "lx", LUMEN.divide(METRE.pow(2))));

    /**
     * The derived unit for activity of a radionuclide (<code>Bq</code>).
     * One becquerel is the radiation caused by one disintegration per second.
     * It is named after the French physicist, Antoine-Henri Becquerel
     * (1852-1908).
     */
    public static final AlternateUnit<RadioactiveActivity> BECQUEREL = si(new AlternateUnit<RadioactiveActivity>(
            "Bq", Unit.ONE.divide(SECOND)));

    /**
     * The derived unit for absorbed dose, specific energy (imparted), kerma
     * (<code>Gy</code>).
     * One gray is equal to the dose of one joule of energy absorbed per one
     * kilogram of matter. It is named after the British physician
     * L. H. Gray (1905-1965).
     */
    public static final AlternateUnit<RadiationDoseAbsorbed> GRAY = si(new AlternateUnit<RadiationDoseAbsorbed>(
            "Gy", JOULE.divide(KILOGRAM)));

    /**
     * The derived unit for dose equivalent (<code>Sv</code>).
     * One Sievert is equal  is equal to the actual dose, in grays, multiplied
     * by a "quality factor" which is larger for more dangerous forms of
     * radiation. It is named after the Swedish physicist Rolf Sievert
     * (1898-1966).
     */
    public static final AlternateUnit<RadiationDoseEffective> SIEVERT = si(new AlternateUnit<RadiationDoseEffective>(
            "Sv", JOULE.divide(KILOGRAM)));

    /**
     * The derived unit for catalytic activity (<code>kat</code>).
     */
    public static final AlternateUnit<CatalyticActivity> KATAL = si(new AlternateUnit<CatalyticActivity>(
            "kat", MOLE.divide(SECOND)));

    //////////////////////////////
    // SI DERIVED PRODUCT UNITS //
    //////////////////////////////

    /**
     * The metric unit for velocity quantities (<code>m/s</code>).
     */
    public static final Unit<Velocity> METRES_PER_SECOND = si(new ProductUnit<Velocity>(
            METRE.divide(SECOND)));

    /**
     * Equivalent to {@link #METRES_PER_SECOND}.
     */
    public static final Unit<Velocity> METERS_PER_SECOND = METRES_PER_SECOND;

    /**
     * The metric unit for acceleration quantities (<code>m/s²</code>).
     */
    public static final Unit<Acceleration> METRES_PER_SQUARE_SECOND = si(new ProductUnit<Acceleration>(
            METRES_PER_SECOND.divide(SECOND)));

    /**
     * Equivalent to {@link #METRES_PER_SQUARE_SECOND}.
     */
    public static final Unit<Acceleration> METERS_PER_SQUARE_SECOND = METRES_PER_SQUARE_SECOND;

    /**
     * The metric unit for area quantities (<code>m²</code>).
     */
    public static final Unit<Area> SQUARE_METRE = si(new ProductUnit<Area>(
            METRE.times(METRE)));

    /**
     * The metric unit for volume quantities (<code>m³</code>).
     */
    public static final Unit<Volume> CUBIC_METRE = si(new ProductUnit<Volume>(
            SQUARE_METRE.times(METRE)));

    /**
     * Equivalent to <code>KILO(METRE)</code>.
     */
    public static final Unit<Length> KILOMETRE = METER.times(1000);

    /**
     * Equivalent to {@link #KILOMETRE}.
     */
    public static final Unit<Length> KILOMETER = KILOMETRE;

    /**
     * Equivalent to <code>CENTI(METRE)</code>.
     */
    public static final Unit<Length> CENTIMETRE = METRE.divide(100);

    /**
     * Equivalent to {@link #CENTIMETRE}.
     */
    public static final Unit<Length> CENTIMETER = CENTIMETRE;

    /**
     * Equivalent to <code>MILLI(METRE)</code>.
     */
    public static final Unit<Length> MILLIMETRE = METRE.divide(1000);

    /**
     * Equivalent to {@link #MILLIMETRE}.
     */
    public static final Unit<Length> MILLIMETER = MILLIMETRE;

    /////////////////
    // SI PREFIXES //
    /////////////////

    /**
     * Returns the specified unit multiplied by the factor
     * <code>10<sup>24</sup></code>
     *
     * @param  unit any unit.
     * @return <code>unit.multiply(1e24)</code>.
     */
    public static <Q extends Quantity> Unit<Q> YOTTA(Unit<Q> unit) {
        return unit.transform(E24);
    }

    /**
     * Returns the specified unit multiplied by the factor
     * <code>10<sup>21</sup></code>
     *
     * @param  unit any unit.
     * @return <code>unit.multiply(1e21)</code>.
     */
    public static <Q extends Quantity> Unit<Q> ZETTA(Unit<Q> unit) {
        return unit.transform(E21);
    }

    /**
     * Returns the specified unit multiplied by the factor
     * <code>10<sup>18</sup></code>
     *
     * @param  unit any unit.
     * @return <code>unit.multiply(1e18)</code>.
     */
    public static <Q extends Quantity> Unit<Q> EXA(Unit<Q> unit) {
        return unit.transform(E18);
    }

    /**
     * Returns the specified unit multiplied by the factor
     * <code>10<sup>15</sup></code>
     *
     * @param  unit any unit.
     * @return <code>unit.multiply(1e15)</code>.
     */
    public static <Q extends Quantity> Unit<Q> PETA(Unit<Q> unit) {
        return unit.transform(E15);
    }

    /**
     * Returns the specified unit multiplied by the factor
     * <code>10<sup>12</sup></code>
     *
     * @param  unit any unit.
     * @return <code>unit.multiply(1e12)</code>.
     */
    public static <Q extends Quantity> Unit<Q> TERA(Unit<Q> unit) {
        return unit.transform(E12);
    }

    /**
     * Returns the specified unit multiplied by the factor
     * <code>10<sup>9</sup></code>
     *
     * @param  unit any unit.
     * @return <code>unit.multiply(1e9)</code>.
     */
    public static <Q extends Quantity> Unit<Q> GIGA(Unit<Q> unit) {
        return unit.transform(E9);
    }

    /**
     * Returns the specified unit multiplied by the factor
     * <code>10<sup>6</sup></code>
     *
     * @param  unit any unit.
     * @return <code>unit.multiply(1e6)</code>.
     */
    public static <Q extends Quantity> Unit<Q> MEGA(Unit<Q> unit) {
        return unit.transform(E6);
    }

    /**
     * Returns the specified unit multiplied by the factor
     * <code>10<sup>3</sup></code>
     *
     * @param  unit any unit.
     * @return <code>unit.multiply(1e3)</code>.
     */
    public static <Q extends Quantity> Unit<Q> KILO(Unit<Q> unit) {
        return unit.transform(E3);
    }

    /**
     * Returns the specified unit multiplied by the factor
     * <code>10<sup>2</sup></code>
     *
     * @param  unit any unit.
     * @return <code>unit.multiply(1e2)</code>.
     */
    public static <Q extends Quantity> Unit<Q> HECTO(Unit<Q> unit) {
        return unit.transform(E2);
    }

    /**
     * Returns the specified unit multiplied by the factor
     * <code>10<sup>1</sup></code>
     *
     * @param  unit any unit.
     * @return <code>unit.multiply(1e1)</code>.
     */
    public static <Q extends Quantity> Unit<Q> DEKA(Unit<Q> unit) {
        return unit.transform(E1);
    }

    /**
     * Returns the specified unit multiplied by the factor
     * <code>10<sup>-1</sup></code>
     *
     * @param  unit any unit.
     * @return <code>unit.multiply(1e-1)</code>.
     */
    public static <Q extends Quantity> Unit<Q> DECI(Unit<Q> unit) {
        return unit.transform(Em1);
    }

    /**
     * Returns the specified unit multiplied by the factor
     * <code>10<sup>-2</sup></code>
     *
     * @param  unit any unit.
     * @return <code>unit.multiply(1e-2)</code>.
     */
    public static <Q extends Quantity> Unit<Q> CENTI(Unit<Q> unit) {
        return unit.transform(Em2);
    }

    /**
     * Returns the specified unit multiplied by the factor
     * <code>10<sup>-3</sup></code>
     *
     * @param  unit any unit.
     * @return <code>unit.multiply(1e-3)</code>.
     */
    public static <Q extends Quantity> Unit<Q> MILLI(Unit<Q> unit) {
        return unit.transform(Em3);
    }

    /**
     * Returns the specified unit multiplied by the factor
     * <code>10<sup>-6</sup></code>
     *
     * @param  unit any unit.
     * @return <code>unit.multiply(1e-6)</code>.
     */
    public static <Q extends Quantity> Unit<Q> MICRO(Unit<Q> unit) {
        return unit.transform(Em6);
    }

    /**
     * Returns the specified unit multiplied by the factor
     * <code>10<sup>-9</sup></code>
     *
     * @param  unit any unit.
     * @return <code>unit.multiply(1e-9)</code>.
     */
    public static <Q extends Quantity> Unit<Q> NANO(Unit<Q> unit) {
        return unit.transform(Em9);
    }

    /**
     * Returns the specified unit multiplied by the factor
     * <code>10<sup>-12</sup></code>
     *
     * @param  unit any unit.
     * @return <code>unit.multiply(1e-12)</code>.
     */
    public static <Q extends Quantity> Unit<Q> PICO(Unit<Q> unit) {
        return unit.transform(Em12);
    }

    /**
     * Returns the specified unit multiplied by the factor
     * <code>10<sup>-15</sup></code>
     *
     * @param  unit any unit.
     * @return <code>unit.multiply(1e-15)</code>.
     */
    public static <Q extends Quantity> Unit<Q> FEMTO(Unit<Q> unit) {
        return unit.transform(Em15);
    }

    /**
     * Returns the specified unit multiplied by the factor
     * <code>10<sup>-18</sup></code>
     *
     * @param  unit any unit.
     * @return <code>unit.multiply(1e-18)</code>.
     */
    public static <Q extends Quantity> Unit<Q> ATTO(Unit<Q> unit) {
        return unit.transform(Em18);
    }

    /**
     * Returns the specified unit multiplied by the factor
     * <code>10<sup>-21</sup></code>
     *
     * @param  unit any unit.
     * @return <code>unit.multiply(1e-21)</code>.
     */
    public static <Q extends Quantity> Unit<Q> ZEPTO(Unit<Q> unit) {
        return unit.transform(Em21);
    }

    /**
     * Returns the specified unit multiplied by the factor
     * <code>10<sup>-24</sup></code>
     *
     * @param  unit any unit.
     * @return <code>unit.multiply(1e-24)</code>.
     */
    public static <Q extends Quantity> Unit<Q> YOCTO(Unit<Q> unit) {
        return unit.transform(Em24);
    }

    /////////////////////
    // Collection View //
    /////////////////////

    /**
     * Returns a read only view over theunits defined in this class.
     *
     * @return the collection of SI units.
     */
    public Set<Unit<?>> getUnits() {
        return Collections.unmodifiableSet(UNITS);
    }

    /**
     * Adds a new unit to the collection.
     *
     * @param  unit the unit being added.
     * @return <code>unit</code>.
     */
    private static <U extends Unit<?>> U si(U unit) {
        UNITS.add(unit);
        return unit;
    }

    // Holds prefix converters (optimization).

    static final MultiplyConverter E24 = new MultiplyConverter(1E24);

    static final MultiplyConverter E21 = new MultiplyConverter(1E21);

    static final RationalConverter E18 = new RationalConverter(1000000000000000000L, 1);

    static final RationalConverter E15 = new RationalConverter(1000000000000000L, 1);

    static final RationalConverter E12 = new RationalConverter(1000000000000L,1);

    static final RationalConverter E9 = new RationalConverter(1000000000L, 1);

    static final RationalConverter E6 = new RationalConverter(1000000L, 1);

    static final RationalConverter E3 = new RationalConverter(1000L, 1);

    static final RationalConverter E2 = new RationalConverter(100L, 1);

    static final RationalConverter E1 = new RationalConverter(10L, 1);

    static final RationalConverter Em1 = new RationalConverter(1, 10L);

    static final RationalConverter Em2 = new RationalConverter(1, 100L);

    static final RationalConverter Em3 = new RationalConverter(1, 1000L);

    static final RationalConverter Em6 = new RationalConverter(1, 1000000L);

    static final RationalConverter Em9 = new RationalConverter(1, 1000000000L);

    static final RationalConverter Em12 = new RationalConverter(1, 1000000000000L);

    static final RationalConverter Em15 = new RationalConverter(1, 1000000000000000L);

    static final RationalConverter Em18 = new RationalConverter(1, 1000000000000000000L);

    static final MultiplyConverter Em21 = new MultiplyConverter(1E-21);

    static final MultiplyConverter Em24 = new MultiplyConverter(1E-24);
}