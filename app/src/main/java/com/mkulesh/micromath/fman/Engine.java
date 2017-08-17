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
package com.mkulesh.micromath.fman;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class Engine
{
    protected Handler handler = null;
    protected String errMsg = null;

    protected Engine()
    {
    }

    public void setHandler(Handler h)
    {
        handler = h;
    }

    protected Bundle wrap(String str)
    {
        Bundle b = new Bundle(1);
        b.putString(CommanderIf.MESSAGE_STRING, str);
        return b;
    }

    protected final void sendProgress(String s, int p1)
    {
        if (handler == null)
            return;
        Message msg = null;

        if (p1 < 0)
            msg = handler.obtainMessage(p1, -1, -1, wrap(s));
        else
            msg = handler.obtainMessage(CommanderIf.OPERATION_IN_PROGRESS, p1, -1, wrap(s));
        handler.sendMessage(msg);
    }

    protected final void sendProgress(String s, int p, String cookie)
    {
        if (handler == null)
            return;

        Message msg = null;
        if (p < 0)
            msg = handler.obtainMessage(p, -1, -1, wrap(s));
        else
            msg = handler.obtainMessage(CommanderIf.OPERATION_IN_PROGRESS, p, -1, wrap(s));
        Bundle b = msg.getData();
        b.putString(CommanderIf.NOTIFY_COOKIE, cookie);
        handler.sendMessage(msg);
    }

    protected final void error(String err)
    {
        if (errMsg == null)
            errMsg = err;
        else
            errMsg += "\n" + err;
    }

    protected final void sendError()
    {
        if (errMsg != null)
        {
            sendProgress(errMsg, CommanderIf.OPERATION_FAILED_REFRESH_REQUIRED);
        }
    }

    protected final void sendResult(String report)
    {
        if (errMsg != null)
            sendProgress(report + "\n - " + errMsg, CommanderIf.OPERATION_FAILED_REFRESH_REQUIRED);
        else
        {
            sendProgress(report, CommanderIf.OPERATION_COMPLETED_REFRESH_REQUIRED);
        }
    }
}
