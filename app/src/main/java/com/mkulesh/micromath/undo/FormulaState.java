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
package com.mkulesh.micromath.undo;

import android.os.Parcel;
import android.os.Parcelable;

import com.mkulesh.micromath.utils.ViewUtils;

/*
 * Class that holds the undo state of a single formula or term.
 */
public final class FormulaState implements Parcelable
{
    public int formulaId = ViewUtils.INVALID_INDEX;
    public int termId = ViewUtils.INVALID_INDEX;
    public final Parcelable data;

    public FormulaState(int formulaId, int termId, Parcelable data)
    {
        super();
        this.formulaId = formulaId;
        this.termId = termId;
        this.data = data;
    }

    private FormulaState(Parcel in)
    {
        super();
        formulaId = in.readInt();
        termId = in.readInt();
        data = in.readParcelable(getClass().getClassLoader());
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeInt(formulaId);
        dest.writeInt(termId);
        dest.writeParcelable(data, flags);
    }

    public static final Parcelable.Creator<FormulaState> CREATOR = new Parcelable.Creator<FormulaState>()
    {
        @Override
        public FormulaState createFromParcel(Parcel in)
        {
            return new FormulaState(in);
        }

        @Override
        public FormulaState[] newArray(int size)
        {
            return new FormulaState[size];
        }
    };
}
