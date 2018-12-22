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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mkulesh.micromath.fman.AdapterDocuments;
import com.mkulesh.micromath.formula.StoredFormula;
import com.mkulesh.micromath.plus.R;
import com.mkulesh.micromath.utils.AppLocale;
import com.mkulesh.micromath.utils.AppTheme;
import com.mkulesh.micromath.utils.CompatUtils;
import com.mkulesh.micromath.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
{
    /*
     * Constants used to save/restore the instance state.
     */
    private static final String STATE_WORKSHEET_NAME = "worksheet_name";
    private static final String STATE_STORED_FORMULA = "stored_formula";

    private static final int STORAGE_PERMISSION_REQID = 255;
    private static final int SETTINGS_ACTIVITY_REQID = 256;
    public static final String EXIT_CONFIRM = "exit_confirm";
    private static final String SHORTCUT_NEW_DOCUMENT = "com.mkulesh.micromath.plus.NEW_DOCUMENT";

    private Dialog storagePermissionDialog = null;
    private int storagePermissionAction = ViewUtils.INVALID_INDEX;

    private StoredFormula storedFormula = null;
    private CharSequence worksheetName = null;

    private Toolbar mToolbar = null;
    private ArrayList<android.support.v7.view.ActionMode> activeActionModes = null;
    private DrawerLayout mDrawerLayout;
    private NavigationView navigationView = null;
    private CharSequence[] activityTitles = null, activitySubtitles = null, activityResources = null;
    private final ArrayList<MenuItem> activityMenuItems = new ArrayList<>();
    private ActionBarDrawerToggle mDrawerToggle;
    private Uri externalUri = null;
    private Toast exitToast = null;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setTheme(AppTheme.getTheme(this, AppTheme.ThemeType.MAIN_THEME));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PackageManager pm = getPackageManager();
        ViewUtils.Debug(
                this,
                "App started, android version " + Build.VERSION.SDK_INT + ", installation source: "
                        + pm.getInstallerPackageName(getPackageName()));

        // Action bar (v7 compatibility library): use Toolbar
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
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

        // Action bar drawer
        mDrawerLayout = findViewById(R.id.main_drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        if (navigationView != null)
        {
            prepareNavigationView();
        }

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_open)
        {
            public void onDrawerClosed(View view)
            {
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView)
            {
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        CompatUtils.setDrawerListener(mDrawerLayout, mDrawerToggle);

        // context menu
        activeActionModes = new ArrayList<>();

        Intent intent = getIntent();
        boolean intentProcessed = false;
        if (intent != null)
        {
            if (SHORTCUT_NEW_DOCUMENT.equals(intent.getAction()))
            {
                ViewUtils.Debug(this, "Called with shortcut intent: " + intent.toString());
                selectWorksheet(R.id.action_new_document);
                intentProcessed = true;
            }
            else if (intent.getData() != null)
            {
                ViewUtils.Debug(this, "Called with external UIR: " + intent.toString());
                externalUri = intent.getData();
                selectWorksheet(BaseFragment.INVALID_ACTION_ID);
                intentProcessed = true;
            }
        }
        if (!intentProcessed && savedInstanceState == null)
        {
            selectWorksheet(BaseFragment.INVALID_ACTION_ID);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.activity_main_actions, menu);
        return true;
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {
        storagePermissionAction = ViewUtils.INVALID_INDEX;

        BaseFragment baseFragment = getVisibleFragment();

        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(menuItem))
        {
            if (baseFragment != null && menuItem.getItemId() == android.R.id.home)
            {
                baseFragment.hideKeyboard();
            }
            return true;
        }

        if (baseFragment == null)
        {
            return true;
        }
        switch (menuItem.getItemId())
        {
        case R.id.action_undo:
        case R.id.action_new:
        case R.id.action_discard:
        case R.id.action_document_settings:
        case R.id.action_new_document:
            baseFragment.performAction(menuItem.getItemId());
            return true;
        case R.id.action_open:
        case R.id.action_save:
        case R.id.action_save_as:
        case R.id.action_export:
        case R.id.action_dev_autotest:
        case R.id.action_dev_export_doc:
        case R.id.action_dev_take_screenshot:
            if (checkStoragePermission(menuItem.getItemId()))
            {
                baseFragment.performAction(menuItem.getItemId());
            }
            return true;
        case R.id.action_app_settings:
        {
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivityForResult(settings, SETTINGS_ACTIVITY_REQID);
            return true;
        }
        case R.id.action_exit:
            finish();
            return true;
        case android.R.id.home:
            onBackPressed();
            return false;
        default:
            return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onBackPressed()
    {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!preferences.getBoolean(EXIT_CONFIRM, false))
        {
            finish();
        }
        else if (exitToast != null && exitToast.getView().isShown())
        {
            exitToast.cancel();
            finish();
        }
        else
        {
            exitToast = Toast.makeText(this, R.string.action_exit_confirm, Toast.LENGTH_LONG);
            exitToast.show();
        }
    }

    public void restartActivity()
    {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    @Override
    protected void attachBaseContext(Context newBase)
    {
        final Locale prefLocale = AppLocale.ContextWrapper.getPreferredLocale(newBase);
        ViewUtils.Debug(this, "Application locale: " + prefLocale.toString());
        super.attachBaseContext(AppLocale.ContextWrapper.wrap(newBase, prefLocale));
    }

    /*********************************************************
     * Instance state
     *********************************************************/

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        if (storedFormula != null)
        {
            outState.putParcelable(STATE_STORED_FORMULA, storedFormula.onSaveInstanceState());
        }
        if (getWorksheetName() != null)
        {
            outState.putCharSequence(STATE_WORKSHEET_NAME, getWorksheetName());
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle inState)
    {
        try
        {
            final Parcelable s = inState.getParcelable(STATE_STORED_FORMULA);
            if (s != null)
            {
                storedFormula = new StoredFormula();
                storedFormula.onRestoreInstanceState(s);
            }
        }
        catch (Exception e)
        {
            ViewUtils.Debug(this, e.getLocalizedMessage());
            storedFormula = null;
        }
        CharSequence w = inState.getCharSequence(STATE_WORKSHEET_NAME);
        if (w != null)
        {
            setWorksheetName(-1, w);
        }
        super.onRestoreInstanceState(inState);
    }

    /*********************************************************
     * Context menu handling
     *********************************************************/

    public android.support.v7.view.ActionMode getActionMode()
    {
        if (!activeActionModes.isEmpty())
        {
            return activeActionModes.get(activeActionModes.size() - 1);
        }
        return null;
    }

    @Override
    public void onSupportActionModeStarted(android.support.v7.view.ActionMode mode)
    {
        mToolbar.setVisibility(View.INVISIBLE);
        super.onSupportActionModeStarted(mode);
        activeActionModes.add(mode);
        final BaseFragment f = getVisibleFragment();
        if (f != null)
        {
            f.updateModeTitle();
        }
    }

    @Override
    public void onSupportActionModeFinished(android.support.v7.view.ActionMode mode)
    {
        super.onSupportActionModeFinished(mode);
        mToolbar.setVisibility(View.VISIBLE);
        activeActionModes.remove(mode);
        final BaseFragment f = getVisibleFragment();
        if (f != null)
        {
            f.enableObjectPropertiesButton(false);
            f.enableObjectDetailsButton(false);
        }
    }

    /**
     * Procedure enforces the currently active action mode to be finished
     */
    public void finishActiveActionMode()
    {
        for (android.support.v7.view.ActionMode mode : activeActionModes)
        {
            mode.finish();
        }
        final BaseFragment f = getVisibleFragment();
        if (f != null)
        {
            f.enableObjectPropertiesButton(false);
            f.enableObjectDetailsButton(false);
        }
    }

    /*********************************************************
     * Formula clipboard
     *********************************************************/

    /**
     * Procedure stores given formula into the internal clipboard
     */
    public void setStoredFormula(StoredFormula term)
    {
        this.storedFormula = term;
    }

    /**
     * Procedure return a stored formula from the internal clipboard
     */
    public StoredFormula getStoredFormula()
    {
        return storedFormula;
    }

    /*********************************************************
     * Navigation drawer
     *********************************************************/

    private void prepareNavigationView()
    {
        activityTitles = getResources().getStringArray(R.array.activity_titles);
        activitySubtitles = getResources().getStringArray(R.array.activity_subtitles);
        activityResources = getResources().getStringArray(R.array.activity_resources);

        for (int i = 0; i < navigationView.getMenu().size(); i++)
        {
            final MenuItem m = navigationView.getMenu().getItem(i);
            if (m.getItemId() == R.id.nav_group_examples)
            {
                for (int j = 0; j < Math.min(m.getSubMenu().size(), activitySubtitles.length); j++)
                {
                    final MenuItem m1 = m.getSubMenu().getItem(j);
                    activityMenuItems.add(m1);
                    m1.setTitle(activitySubtitles[m1.getOrder()]);
                }
            }
            else if (m.getItemId() == R.id.nav_group_etc)
            {
                for (int j = 0; j < Math.min(m.getSubMenu().size(), activityTitles.length); j++)
                {
                    final MenuItem m1 = m.getSubMenu().getItem(j);
                    activityMenuItems.add(m1);
                    m1.setTitle(activityTitles[m1.getOrder()]);
                }
            }
            else if (m.getOrder() < activityTitles.length)
            {
                activityMenuItems.add(m);
                m.setTitle(activityTitles[m.getOrder()]);
            }
        }

        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener()
                {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem)
                    {
                        selectNavigationItem(menuItem, BaseFragment.INVALID_ACTION_ID);
                        return true;
                    }
                });

        updateVersionInfo();
    }

    private void updateVersionInfo()
    {
        TextView versionInfo = null;
        for (int i = 0; i < navigationView.getHeaderCount(); i++)
        {
            versionInfo = navigationView.getHeaderView(i).findViewById(R.id.navigation_view_header_version);
            if (versionInfo != null)
            {
                break;
            }
        }
        if (versionInfo != null && versionInfo.getText().length() == 0)
        {
            try
            {
                final PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
                final String verInfo = "v." + pi.versionName;
                versionInfo.setText(verInfo);
            }
            catch (Exception e)
            {
                ViewUtils.Debug(this, "Can not obtain app version: " + e.getLocalizedMessage());
            }
        }
    }

    public void updateFragmentInfo(BaseFragment fragment)
    {
        if (fragment != null)
        {
            final int position = fragment.getFragmentNumber();
            if (position >= 0 && position < activityTitles.length)
            {
                mToolbar.setTitle(activityTitles[position]);
            }
            if (position >= 0 && position < activitySubtitles.length)
            {
                mToolbar.setSubtitle(activitySubtitles[position]);
            }
            for (MenuItem m : activityMenuItems)
            {
                m.setChecked(m.getOrder() == position);
            }
        }
    }

    public void selectWorksheet(int postActionId)
    {
        selectNavigationItem(navigationView.getMenu().getItem(0), postActionId);
    }

    public CharSequence getWorksheetName()
    {
        return worksheetName;
    }

    public void setWorksheetName(int position, CharSequence name)
    {
        this.worksheetName = name;
        if (position == BaseFragment.WORKSHEET_FRAGMENT_ID)
        {
            mToolbar.setSubtitle(worksheetName);
        }
    }

    @SuppressLint("RestrictedApi")
    public BaseFragment getVisibleFragment()
    {
        FragmentManager fragmentManager = MainActivity.this.getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();
        for (Fragment fragment : fragments)
        {
            if (fragment != null && fragment.isVisible() && (fragment instanceof BaseFragment))
            {
                return (BaseFragment) fragment;
            }
        }
        return null;
    }

    public void selectNavigationItem(MenuItem menuItem, int postActionId)
    {
        final int position = menuItem.getOrder();
        for (MenuItem m : activityMenuItems)
        {
            m.setChecked(m.getOrder() == position);
        }
        mDrawerLayout.closeDrawers();

        Fragment fragment = null;
        final CharSequence res = (position >= 0 && position < activityResources.length) ?
                activityResources[position] : null;
        if (position == BaseFragment.WORKSHEET_FRAGMENT_ID)
        {
            fragment = new MainFragmentWorksheet();
            Bundle args = new Bundle();
            if (externalUri != null)
            {
                args.putParcelable(MainFragmentWorksheet.EXTERNAL_URI, externalUri);
            }
            args.putInt(MainFragmentWorksheet.POST_ACTION_ID, postActionId);
            fragment.setArguments(args);
        }
        else if (res != null && res.toString().contains(".mmt"))
        {
            fragment = new MainFragmentAsset();
            Bundle args = new Bundle();
            args.putInt(MainFragmentAsset.FRAGMENT_NUMBER, position);
            fragment.setArguments(args);
        }
        else if (res != null && res.toString().contains("https:"))
        {
            try
            {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(android.net.Uri.parse(res.toString()));
                startActivity(intent);
            }
            catch (Exception e)
            {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }

        if (fragment != null)
        {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.main_content_frame, fragment);
            transaction.commit();
        }
    }

    /**
     * Storage permission handling
     */
    @SuppressLint("NewApi")
    public boolean checkStoragePermission(int action)
    {
        if (CompatUtils.isMarshMallowOrLater())
        {
            final boolean granted = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            if (granted)
            {
                return true;
            }
            ViewUtils.Debug(this, "storage permissions are not granted");
            storagePermissionAction = action;
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE))
            {
                if (isFinishing() || (storagePermissionDialog != null && storagePermissionDialog.isShowing()))
                {
                    return false;
                }
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setIcon(storagePermissionAction == R.id.action_open ? R.drawable.ic_action_content_open
                        : R.drawable.ic_action_content_save);
                alert.setTitle(getString(R.string.allow_storage_access_title));
                alert.setMessage(getString(R.string.allow_storage_access_description));
                alert.setNegativeButton(getString(R.string.dialog_navigation_cancel),
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                // nothing to do
                            }
                        });
                alert.setPositiveButton(getString(R.string.allow_storage_access_grant),
                        new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                requestStoragePermission();
                            }
                        });
                storagePermissionDialog = alert.show();
            }
            else
            {
                requestStoragePermission();
            }
            return false;
        }
        return true;
    }

    @SuppressLint("NewApi")
    private void requestStoragePermission()
    {
        ViewUtils.Debug(this, "requesting storage permissions");
        requestPermissions(new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE }, STORAGE_PERMISSION_REQID);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
        case STORAGE_PERMISSION_REQID:
        {
            // If request is cancelled, the result arrays are empty.
            if (storagePermissionAction != ViewUtils.INVALID_INDEX && grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                ViewUtils.Debug(this, "permission was granted, performing file operation action");
                final BaseFragment f = getVisibleFragment();
                if (f != null)
                {
                    f.performAction(storagePermissionAction);
                }
            }
            else
            {
                String error = getResources().getString(R.string.allow_storage_access_description);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
            return;
        }
        default:
            // nothing to do
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AdapterDocuments.REQUEST_OPEN_DOCUMENT_TREE && data != null)
        {
            Uri uri = data.getData();
            AdapterDocuments.saveURI(this, uri);
        }
        else if (requestCode == SETTINGS_ACTIVITY_REQID)
        {
            restartActivity();
        }
    }
}
