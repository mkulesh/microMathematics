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

package com.mkulesh.micromath;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.AppLocale;
import com.mkulesh.micromath.utils.AppTheme;
import com.mkulesh.micromath.utils.ViewUtils;

import java.util.Locale;

public class SettingsActivity extends AppCompatPreferenceActivity
{
    @Override
    @SuppressWarnings("deprecation")
    protected void onCreate(Bundle savedInstanceState)
    {
        setTheme(AppTheme.getTheme(AppTheme.ActivityType.SETTINGS_ACTIVITY));
        super.onCreate(savedInstanceState);
        setupActionBar();
        addPreferencesFromResource(R.xml.preferences);
        prepareListPreference((ListPreference) findPreference("app_language"));
    }

    private void setupActionBar()
    {
        ViewGroup rootView = (ViewGroup) findViewById(R.id.action_bar_root); //id from appcompat
        if (rootView != null)
        {
            View view = getLayoutInflater().inflate(R.layout.activity_toolbar, rootView, false);
            rootView.addView(view, 0);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.action_app_settings);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        final Locale prefLocale = AppLocale.ContextWrapper.getPreferredLocale(newBase);
        ViewUtils.Debug(this, "Settings locale: " + prefLocale.toString());
        super.attachBaseContext(AppLocale.ContextWrapper.wrap(newBase, prefLocale));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        if (id == android.R.id.home)
        {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void prepareListPreference(final ListPreference listPreference)
    {
        if (listPreference == null)
        {
            return;
        }

        if (listPreference.getValue() == null)
        {
            // to ensure we don't get a null value
            // set first value by default
            listPreference.setValueIndex(0);
        }

        listPreference.setSummary(listPreference.getEntry().toString());
        listPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener()
        {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue)
            {
                listPreference.setValue(newValue.toString());
                preference.setSummary(listPreference.getEntry().toString());
                return true;
            }
        });
    }
}
