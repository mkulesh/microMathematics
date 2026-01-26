/*
 * microMathematics - Extended Visual Calculator
 * Copyright (C) 2014-2026 by Mikhail Kulesh
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

package com.mkulesh.micromath.utils;

import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;

public class AppTask
{
    private final AtomicBoolean cancelled = new AtomicBoolean();
    private Runnable backgroundTask;
    private String backgroundTaskName;

    protected AppTask()
    {
        cancelled.set(false);
    }

    protected void setBackgroundTask(@NonNull Runnable backgroundTask, @NonNull String backgroundTaskName)
    {
        this.backgroundTask = backgroundTask;
        this.backgroundTaskName = backgroundTaskName;
    }

    public void start()
    {
        synchronized (cancelled)
        {
            cancelled.set(false);
        }
        if (backgroundTask != null && backgroundTaskName != null)
        {
            final Thread thread = new Thread(backgroundTask, backgroundTaskName);
            thread.start();
        }
        ViewUtils.Debug(this, "thread started");
    }

    public void cancel()
    {
        ViewUtils.Debug(this, "trying to cancel thread");
        synchronized (cancelled)
        {
            cancelled.set(true);
        }
    }

    public boolean isCancelled()
    {
        synchronized (cancelled)
        {
            return cancelled.get();
        }
    }
}
