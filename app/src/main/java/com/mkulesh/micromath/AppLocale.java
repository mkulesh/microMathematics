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
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.preference.PreferenceManager;
import android.widget.RadioButton;

import com.mkulesh.micromath.dialogs.DialogRadioGroup;
import com.mkulesh.micromath.plus.R;

import java.util.Locale;

/*********************************************************
 * Handling of locale (language etc)
 *********************************************************/
public final class AppLocale
{
    public static final String PREF_APP_LANGUAGE = "app_language";

    public static class ContextWrapper extends android.content.ContextWrapper
    {
        public ContextWrapper(Context base)
        {
            super(base);
        }

        public static String getPreferredLanguage(Context context)
        {
            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            final String res = pref.getString(PREF_APP_LANGUAGE, "");
            return res.equals("")? Locale.getDefault().getLanguage() : res;
        }

        public static void setPreferredLanguage(final MainActivity activity)
        {
            final String prefLanguage = getPreferredLanguage(activity);

            final String[] languageCodes = activity.getResources().getStringArray(R.array.language_codes);
            final String[] languageNames = activity.getResources().getStringArray(R.array.language_names);
            final int languageNumber = Math.min(languageNames.length, languageCodes.length);

            final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);

            final DialogRadioGroup d = new DialogRadioGroup(activity, R.string.action_app_language,
                    languageNumber, new DialogRadioGroup.EventHandler()
            {
                public void onCreate(RadioButton[] rb)
                {
                    for (int i = 0; i < rb.length; i++)
                    {
                        rb[i].setText(languageNames[i]);
                        rb[i].setChecked(languageCodes[i].equals(prefLanguage));
                    }
                }

                public void onClick(int whichButton)
                {
                    if (whichButton < languageNumber)
                    {
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString(PREF_APP_LANGUAGE, languageCodes[whichButton]);
                        editor.commit();
                        activity.restartActivity();
                    }
                }
            });
            d.show();
        }

        @SuppressWarnings("deprecation")
        public static ContextWrapper wrap(Context context, Locale newLocale)
        {
            final Resources res = context.getResources();
            final Configuration configuration = res.getConfiguration();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            {
                configuration.setLocale(newLocale);
                LocaleList localeList = new LocaleList(newLocale);
                LocaleList.setDefault(localeList);
                configuration.setLocales(localeList);
                context = context.createConfigurationContext(configuration);
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            {
                configuration.setLocale(newLocale);
                context = context.createConfigurationContext(configuration);
            }
            else
            {
                configuration.locale = newLocale;
                res.updateConfiguration(configuration, res.getDisplayMetrics());
            }
            return new ContextWrapper(context);
        }
    }
}
