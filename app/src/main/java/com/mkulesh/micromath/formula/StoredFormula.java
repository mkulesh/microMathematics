/*
 * microMathematics - Extended Visual Calculator
 * Copyright (C) 2014-2022 by Mikhail Kulesh
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details. You should have received a copy of the GNU General
 * Public License along with this program.
 */
package com.mkulesh.micromath.formula;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.mkulesh.micromath.formula.FormulaBase.BaseType;
import com.mkulesh.micromath.utils.CompatUtils;

import java.util.ArrayList;

public class StoredFormula
{
    enum ContentType
    {
        FORMULA,
        LIST
    }

    /*
     * Helper class that holds the clipboard state of a single formula.
     */
    public static class StoredTerm implements Parcelable
    {
        public FormulaBase.BaseType baseType;
        public String termCode;
        public Parcelable data;

        StoredTerm(Parcel in)
        {
            super();
            readFromParcel(in);
        }

        StoredTerm(FormulaBase.BaseType baseType, String termCode, Parcelable data)
        {
            super();
            this.baseType = baseType;
            this.termCode = termCode;
            this.data = data;
        }

        @Override
        public int describeContents()
        {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags)
        {
            dest.writeString(baseType.toString());
            dest.writeString(termCode);
            dest.writeParcelable(data, 0);
        }

        void readFromParcel(Parcel in)
        {
            baseType = FormulaBase.BaseType.valueOf(in.readString());
            termCode = in.readString();
            data = CompatUtils.readParcelable(in, getClass().getClassLoader(), Parcelable.class);
        }

        public static final Parcelable.Creator<StoredTerm> CREATOR = new Parcelable.Creator<StoredTerm>()
        {
            public StoredTerm createFromParcel(Parcel in)
            {
                return new StoredTerm(in);
            }

            public StoredTerm[] newArray(int size)
            {
                return new StoredTerm[size];
            }
        };
    }

    /*
     * Constants used to save/restore the instance state.
     */
    private static final String STATE_CONTENT_TYPE = "contentType";
    private static final String STATE_DATA = "data";
    private ContentType contentType;
    private StoredTerm[] data;

    /*--------------------------------------------------------*
     * Constructors
     *--------------------------------------------------------*/

    public StoredFormula()
    {
        // empty
    }

    public StoredFormula(BaseType baseType, Parcelable data)
    {
        this.contentType = ContentType.FORMULA;
        this.data = new StoredTerm[1];
        this.data[0] = new StoredTerm(baseType, "", data);
    }

    public StoredFormula(BaseType baseType, String termCode, Parcelable data)
    {
        this.contentType = ContentType.FORMULA;
        this.data = new StoredTerm[1];
        this.data[0] = new StoredTerm(baseType, termCode, data);
    }

    public StoredFormula(ArrayList<FormulaBase.BaseType> types, ArrayList<Parcelable> data)
    {
        this.contentType = ContentType.LIST;
        this.data = new StoredTerm[data.size()];
        for (int i = 0; i < data.size(); i++)
        {
            this.data[i] = new StoredTerm(types.get(i), "", data.get(i));
        }
    }

    /*--------------------------------------------------------*
     * Read/write interface
     *--------------------------------------------------------*/

    /**
     * Parcelable interface: procedure writes the formula state
     */
    public Parcelable onSaveInstanceState()
    {
        Bundle bundle = new Bundle();
        bundle.putString(STATE_CONTENT_TYPE, contentType.toString());
        bundle.putParcelableArray(STATE_DATA, data);
        return bundle;
    }

    /**
     * Parcelable interface: procedure reads the formula state
     */
    @SuppressWarnings("deprecation")
    public void onRestoreInstanceState(Parcelable state)
    {
        if (state == null)
        {
            return;
        }
        if (state instanceof Bundle)
        {
            Bundle bundle = (Bundle) state;
            contentType = ContentType.valueOf(bundle.getString(STATE_CONTENT_TYPE));
            if (CompatUtils.isTiramisuOrLater())
            {
                data = bundle.getParcelableArray(STATE_DATA, StoredTerm.class);
            }
            else
            {
                data = (StoredTerm[]) bundle.getParcelableArray(STATE_DATA);
            }
        }
    }

    /*--------------------------------------------------------*
     * StoredFormula-specific methods
     *--------------------------------------------------------*/

    public ContentType getContentType()
    {
        return contentType;
    }

    public StoredTerm[] getArrayData()
    {
        return data;
    }

    public StoredTerm getSingleData()
    {
        return (data != null && data.length > 0) ? data[0] : null;
    }
}
