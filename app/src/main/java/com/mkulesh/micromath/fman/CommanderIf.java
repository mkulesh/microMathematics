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
package com.mkulesh.micromath.fman;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Message;

public interface CommanderIf
{
    int OPERATION_IN_PROGRESS = 0;
    int OPERATION_FAILED = -2;
    int OPERATION_COMPLETED = -3;
    int OPERATION_COMPLETED_REFRESH_REQUIRED = -4;
    int OPERATION_FAILED_REFRESH_REQUIRED = -7;

    String NOTIFY_COOKIE = "cookie", NOTIFY_POSTO = "posto", NOTIFY_URI = "uri",
            MESSAGE_STRING = "STRING";

    enum SelectionMode
    {
        OPEN,
        SAVE_AS,
        EXPORT
    }

    /**
     * @return current UI context
     */
    Context getContext();

    /**
     * Try to issue an indent
     */
    void issue(Intent in, int ret);

    /**
     * Shows given error
     */
    void showError(final String msg);

    /**
     * Navigate the current panel to the specified URI.
     */
    void Navigate(Uri uri, String positionTo);

    /**
     * Execute (launch) the specified item.
     */
    void Open(Uri uri);

    /**
     * Procedure completion notification.
     */
    boolean notifyMe(Message m);

    /**
     * Procedure returns the calling mode of this adapter.
     */
    SelectionMode getSelectionMode();
}
