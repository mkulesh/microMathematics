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

/*
 * Class that holds the undo state of a single insertion.
 */
public final class InsertState implements Parcelable
{
    public int formulaId;
    public final int selectedId;

    public InsertState(int formulaIds, int selectedId)
    {
        super();
        this.formulaId = formulaIds;
        this.selectedId = selectedId;
    }

    private InsertState(Parcel in)
    {
        super();
        formulaId = in.readInt();
        selectedId = in.readInt();
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
        dest.writeInt(selectedId);
    }

    public static final Parcelable.Creator<InsertState> CREATOR = new Parcelable.Creator<InsertState>()
    {
        @Override
        public InsertState createFromParcel(Parcel in)
        {
            return new InsertState(in);
        }

        @Override
        public InsertState[] newArray(int size)
        {
            return new InsertState[size];
        }
    };
}
