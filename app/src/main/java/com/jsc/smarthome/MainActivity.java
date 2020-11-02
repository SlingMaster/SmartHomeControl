/*
 * Copyright (c) 2020.
 * JSC
 * Design and Programming by Alex Dovby
 */

package com.jsc.smarthome;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.jsc.smarthome.html.CustomWebView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    public static SharedPreferences preference;
    AlertDialog.Builder mAlertDialog;
    ViewGroup webContainer;
    CustomWebView newWebView;
    // ===================================
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            newWebView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LOW_PROFILE
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            );
        }
    };
    // ===================================
    private final Runnable mHideRunnable = this::hide;
    // ===================================
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        preference = PreferenceManager.getDefaultSharedPreferences(this);
        NavigationView navigationView = findViewById(R.id.nav_view);
        Permission.requestMultiplePermissions(this, Permission.PERMISSION_REQUEST_CODE);
        mAlertDialog = new AlertDialog.Builder(this);
        mAlertDialog.setIcon(R.drawable.ic_warning);
        mAlertDialog.setTitle(R.string.title_import_dialog);
        mAlertDialog.setMessage(R.string.message_import_dialog);

        mAlertDialog.setPositiveButton(R.string.button_yes, (dialog, arg1) -> {
            // import database ------
            // save database to application directory
            UpdateDB();
        });
        mAlertDialog.setNegativeButton(R.string.button_no, (dialog, arg1) -> dialog.cancel());
        mAlertDialog.setCancelable(true);
        // ----------------------------------------

        webContainer = findViewById(R.id.new_web_container);
        newWebView = createWebView();
        webContainer.addView(newWebView);
        newWebView.clearCache(preference.getBoolean("sw_clear_cache", false));
        // load screen -----------------------------
        loadHtml(getResources().getString(R.string.sh_url));

        // Set behavior of Navigation drawer
        // This method will trigger on item Click of navigation menu
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            boolean debug = preference.getBoolean("sw_dev_mode", false);
            // Set item in checked state
            menuItem.setChecked(true);
            int id = menuItem.getItemId();

            switch (id) {
                case R.id.nav_sh:
                    loadHtml(debug ? getResources().getString(R.string.debug_sh_url) : getResources().getString(R.string.sh_url));
                    break;
                case R.id.nav_br:
                    loadHtml(debug ? getResources().getString(R.string.debug_br_url) : getResources().getString(R.string.br_url));
                    break;
                case R.id.nav_stats:
                    loadHtml(debug ? getResources().getString(R.string.debug_stats_url) : getResources().getString(R.string.stats_url));
                    break;
                case R.id.nav_test:
                    showMeasurement(getApplicationContext());
                    break;
                case R.id.nav_export:
                    // read database from application directory --
                    File exportFile = new File(getExternalFilesDir(""), FileUtils.BD_EXPORT);
                    String strBD = FileUtils.readFromFile(getApplicationContext());
                    System.out.println("trace | readFromFile | • DB • " + strBD);
                    if (FileUtils.isAvailable() || !FileUtils.isReadOnly()) {
                        try {
                            FileOutputStream fos = new FileOutputStream(exportFile);
                            fos.write(strBD.getBytes());
                            fos.close();
                            Toast.makeText(getBaseContext(), getResources().getString(R.string.msg_export) + " : \n" + exportFile, Toast.LENGTH_LONG).show();
                        } catch (FileNotFoundException e) {
                            System.out.println("trace | File not found: " + e.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case R.id.nav_import:
                    // show dialog window
                    mAlertDialog.show();
                    break;
                case R.id.nav_settings:
                    showConfig(getApplicationContext());
                    break;
                default:
                    break;
            }

            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    // ===================================
    private void UpdateDB() {
        String mData = "";
        File importFile = new File(getExternalFilesDir(""), FileUtils.BD_EXPORT);
        if (FileUtils.isAvailable() || !FileUtils.isReadOnly()) {
            //read database from ExternalStorage to mData
            // \ExternalStorage\Android\data\package name
            try {
                FileInputStream fis = new FileInputStream(importFile);
                DataInputStream in = new DataInputStream(fis);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine;

                while ((strLine = br.readLine()) != null) {
                    mData += strLine;
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (mData.equalsIgnoreCase("")) {
            Toast.makeText(getBaseContext(), R.string.db_empty, Toast.LENGTH_LONG).show();
        } else {
            // System.out.println("trace | mData : " + mData);
            FileUtils.writeToFile(mData, getApplicationContext());
            Toast.makeText(getBaseContext(), R.string.db_updated, Toast.LENGTH_LONG).show();
        }
    }

    // ===================================
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    // ===================================
    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("trace | WEB ACTIVITY | • onDestroy");
        webContainer.removeAllViews();
        if (newWebView != null) {
            newWebView.destroy();
            newWebView = null;
        }
    }

    // ===================================
//    private void toggle() {
//        if (mVisible) {
//            hide();
//        } else {
//            show();
//        }
//    }

    // ===================================
    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    // ===================================
//    @SuppressLint("InlinedApi")
//    private void show() {
//        mVisible = true;
//        mHideHandler.removeCallbacks(mHidePart2Runnable);
//    }

    // ===================================
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    // ===================================
//    WebView
    // ===================================
    @NonNull
    private CustomWebView createWebView() {
        CustomWebView view = new CustomWebView(this);
        // view.setWebEventsListener(this::webViewEvents);
        // SIGNAL 11 SIGSEGV crash Android
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        return view;
    }

    // ===================================
    protected void loadHtml(String url) {
        if (newWebView != null) {
            System.out.println("trace | loadHtml:" + url);
            newWebView.loadUrl(url);
        }
    }

    // ===================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    // ===================================
    public void showMeasurement(Context context) {
        Intent intent = new Intent(context, MeasurementActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // ===================================
    public void showConfig(Context context) {
        Intent configIntent = new Intent(context, SettingsActivity.class);
        configIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(configIntent);
    }
}