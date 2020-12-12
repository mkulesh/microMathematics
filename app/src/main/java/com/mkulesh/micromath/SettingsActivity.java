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
package com.mkulesh.micromath;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;

import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.AppLocale;
import com.mkulesh.micromath.utils.AppTheme;
import com.mkulesh.micromath.utils.CompatUtils;
import com.mkulesh.micromath.utils.ViewUtils;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setTheme(AppTheme.getTheme(this, AppTheme.ThemeType.SETTINGS_THEME));
        super.onCreate(savedInstanceState);
        setupActionBar();
        getSupportFragmentManager().beginTransaction().replace(
                android.R.id.content, new MyPreferenceFragment()).commit();
        setTitle(R.string.action_app_settings);
    }

    public static class MyPreferenceFragment extends PreferenceFragmentCompat
    {
        @Override
        public void onCreatePreferences(Bundle bundle, String s)
        {
            addPreferencesFromResource(R.xml.preferences);
            prepareListPreference(getActivity(), findPreference("app_language"));
            prepareListPreference(getActivity(), findPreference("app_theme"));
            prepareListPreference(getActivity(), findPreference("zoom_mode"));
            tintIcons(getActivity(), getPreferenceScreen());
        }
    }

    private static void tintIcons(final Context c, Preference preference)
    {
        if (preference instanceof PreferenceGroup)
        {
            PreferenceGroup group = ((PreferenceGroup) preference);
            for (int i = 0; i < group.getPreferenceCount(); i++)
            {
                tintIcons(c, group.getPreference(i));
            }
        }
        else
        {
            CompatUtils.setDrawableColorAttr(c, preference.getIcon(), android.R.attr.textColorSecondary);
        }
    }

    private void setupActionBar()
    {
        ViewGroup rootView = findViewById(R.id.action_bar_root); //id from appcompat
        if (rootView != null)
        {
            View view = getLayoutInflater().inflate(R.layout.activity_toolbar, rootView, false);
            rootView.addView(view, 0);

            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // activate toolbar separator, if necessary
        {
            final int sepColor = CompatUtils.getThemeColorAttr(this, R.attr.colorToolBarSeparator);
            if (sepColor != Color.TRANSPARENT && findViewById(R.id.toolbar_separator) != null)
            {
                findViewById(R.id.toolbar_separator).setVisibility(View.VISIBLE);
            }
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
    public void applyOverrideConfiguration(android.content.res.Configuration overrideConfiguration)
    {
        // See https://stackoverflow.com/questions/55265834/change-locale-not-work-after-migrate-to-androidx:
        // There is an issue in new app compat libraries related to night mode that is causing to
        // override the configuration on android 21 to 25. This can be fixed as follows
        if (overrideConfiguration != null) {
            int uiMode = overrideConfiguration.uiMode;
            overrideConfiguration.setTo(getBaseContext().getResources().getConfiguration());
            overrideConfiguration.uiMode = uiMode;
        }
        super.applyOverrideConfiguration(overrideConfiguration);
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

    private static void prepareListPreference(final Activity activity, final ListPreference listPreference)
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

        if (listPreference.getEntry() != null)
        {
            listPreference.setSummary(listPreference.getEntry().toString());
        }
        listPreference.setOnPreferenceChangeListener((preference, newValue) ->
        {
            listPreference.setValue(newValue.toString());
            preference.setSummary(listPreference.getEntry().toString());
            if (activity != null)
            {
                final Intent intent = activity.getIntent();
                activity.finish();
                activity.startActivity(intent);
            }
            return true;
        });
    }
}
