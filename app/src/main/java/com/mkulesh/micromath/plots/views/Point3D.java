/*
 * Copyright (C) 2014-2018 Mikhail Kulesh
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details. You should have received a copy of the GNU General
 * Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.mkulesh.micromath.plots.views;

import android.graphics.Point;

public final class Point3D
{
    public double x, y, z;

    /**
     * Default constructor.
     */
    Point3D(double ix, double iy, double iz)
    {
        x = ix;
        y = iy;
        z = iz;
    }

    /**
     * Determines whether this vertex is invalid, i.e has invalid coordinates value.
     */
    public final boolean isInvalid()
    {
        return Double.isNaN(z);
    }

    /**
     * Gets the 2D projection of the vertex.
     */
    public void projection(Point p, SurfacePlotProjector projector)
    {
        projector.project(p, x, y, (z - projector.zmin) * projector.zfactor - 10);
    }

    /**
     * Transforms coordinate values to fit the scaling factor of the projector. This routine is only used for
     * transforming center of projection in Surface Plotter.
     */
    public final void transform(SurfacePlotProjector projector)
    {
        x = x / projector.getXScaling();
        y = y / projector.getYScaling();
        z = (projector.zmax - projector.zmin) * (z / projector.getZScaling() + 10) / 20 + projector.zmin;
    }
}
