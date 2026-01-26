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

import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.AppTask;
import com.mkulesh.micromath.utils.ViewUtils;

import java.util.ArrayList;

public class CalculaterTask extends AppTask implements Runnable, OnClickListener
{
    public static final class CancelException extends Exception
    {
        private static final long serialVersionUID = 4916095827341L;

        CancelException()
        {
            // empty
        }
    }

    private final FormulaList list;
    private final ArrayList<CalculationResult> formulas;

    CalculaterTask(FormulaList list, ArrayList<CalculationResult> formulas)
    {
        super();
        setBackgroundTask(this, this.getClass().getSimpleName());
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        this.list = list;
        this.formulas = formulas;
    }

    protected void onPreExecute()
    {
        list.getActivity().runOnUiThread(() ->
                list.setInOperation(/*owner=*/this, /*inOperation=*/true, /*stopHandler=*/this));
    }

    @Override
    public void run()
    {
        onPreExecute();
        for (CalculationResult f : formulas)
        {
            if (isCancelled())
            {
                break;
            }
            if (!f.isEmpty())
            {
                try
                {
                    f.calculate(this);
                }
                catch (CancelException e)
                {
                    break;
                }
                catch (OutOfMemoryError ex)
                {
                    String error = list.getActivity().getResources().getString(R.string.error_out_of_memory);
                    Toast.makeText(list.getActivity(), error, Toast.LENGTH_LONG).show();
                    break;
                }
                publishProgress(f);
            }
        }
        onPostExecute();
    }

    protected void publishProgress(CalculationResult f)
    {
        list.getActivity().runOnUiThread(() ->
        {
            if (f != null)
            {
                f.showResult();
            }
        });
    }

    protected void onPostExecute()
    {
        ViewUtils.Debug(this, "thread finished");
        list.getActivity().runOnUiThread(() ->
        {
            list.setInOperation(/*owner=*/this, /*inOperation=*/false, /*stopHandler=*/this);
            if (isCancelled())
            {
                String error = list.getActivity().getResources().getString(R.string.error_calculation_aborted);
                Toast.makeText(list.getActivity(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onClick(View v)
    {
        cancel();
    }

    public void checkCancelation() throws CancelException
    {
        if (isCancelled())
        {
            throw new CancelException();
        }
    }
}
