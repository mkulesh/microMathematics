/*******************************************************************************
 * microMathematics Plus - Extended visual calculator
 * *****************************************************************************
 * Copyright (C) 2014-2017 Mikhail Kulesh
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.mkulesh.micromath.formula;

import java.util.ArrayList;

import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.mkulesh.micromath.plus.R;

public class CalculaterTask extends AsyncTask<Void, CalculationResult, Void> implements OnClickListener
{
    public static final class CancelException extends Exception
    {
        private static final long serialVersionUID = 4916095827341L;

        public CancelException()
        {
            // empty
        }
    }

    private final FormulaList list;
    private final ArrayList<CalculationResult> formulas;

    CalculaterTask(FormulaList list, ArrayList<CalculationResult> formulas)
    {
        this.list = list;
        this.formulas = formulas;
    }

    @Override
    protected void onPreExecute()
    {
        list.setInOperation(/*owner=*/this, /*inOperation=*/true, /*stopHandler=*/this);
    }

    @Override
    protected Void doInBackground(Void... params)
    {
        for (CalculationResult f : formulas)
        {
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
                publishProgress(f);
            }
        }
        return null;
    }

    @Override
    protected void onProgressUpdate(CalculationResult... formula)
    {
        CalculationResult f = formula[0];
        if (f != null)
        {
            f.showResult();
        }
    }

    @Override
    protected void onCancelled()
    {
        list.setInOperation(/*owner=*/this, /*inOperation=*/false, /*stopHandler=*/this);
        String error = list.getActivity().getResources().getString(R.string.error_calculation_aborted);
        Toast.makeText(list.getActivity(), error, Toast.LENGTH_LONG).show();
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(Void result)
    {
        list.setInOperation(/*owner=*/this, /*inOperation=*/false, /*stopHandler=*/this);
    }

    @Override
    public void onClick(View v)
    {
        cancel(false);
    }

    public void checkCancelation() throws CancelException
    {
        if (isCancelled())
        {
            throw new CancelException();
        }
    }
}
