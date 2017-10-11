package com.mkulesh.micromath.math;

public class ArgumentValueItem
{
    public final double argument;
    public final double value;

    public ArgumentValueItem()
    {
        this.argument = Double.NaN;
        this.value = Double.NaN;
    }

    public ArgumentValueItem(double argument, double value)
    {
        this.argument = argument;
        this.value = value;
    }
}
