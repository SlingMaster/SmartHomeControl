/*
 * Copyright (c) 2020.
 * JSC
 * Design and Programming by Alex Dovby
 */

package com.jsc.smarthome;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;
import com.jsc.smarthome.html.CustomWebView;

import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity {
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private boolean mVisible;
    public static SharedPreferences preference;
    private AppBarConfiguration mAppBarConfiguration;
    ViewGroup webContainer;
    @Nullable
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
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
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
        mVisible = true;
        setContentView(R.layout.activity_main);
        preference = PreferenceManager.getDefaultSharedPreferences(this);

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

//        DrawerLayout drawer = findViewById(R.id.drawer_layout);
//        NavigationView navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
//        mAppBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_settings)
//                .setDrawerLayout(drawer)
//                .build();
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
//        NavigationUI.setupWithNavController(navigationView, navController);


//        mainRelative.addView(сюда);
//        setContentView(R.layout.activity_web);
        webContainer = findViewById(R.id.new_web_container);
        newWebView = createWebView();
        webContainer.addView(newWebView);
        // load screen -----------------------------
        loadHtml(getResources().getString(R.string.sh_url));



//        newWebView.clearCache(preference.getBoolean("sw_clear_cache", false));


        // Set behavior of Navigation drawer
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                // Set item in checked state
                menuItem.setChecked(true);
                int id = menuItem.getItemId();
//                int cmd;
                switch (id) {
                    case R.id.nav_br:
                        loadHtml(getResources().getString(R.string.br_url));
                        break;
                    case R.id.nav_sh:
                        loadHtml(getResources().getString(R.string.sh_url));
                        break;
                    case R.id.nav_stats:
                        loadHtml(getResources().getString(R.string.stats_url));
                        break;
                    case R.id.nav_test:
                        showMeasurement(getApplicationContext());
                        break;
                    case R.id.nav_settings:
                        showConfig(getApplicationContext());
                        break;
                    default:
                        break;
                }
                System.out.println("Press menu : " + id);
//            Toast.makeText(getApplicationContext(), "Press menu" + id, Toast.LENGTH_LONG).show();
//            showConfig(getApplicationContext());
//            mWeb.sendCommand(cmd);
                // Closing drawer on item click
//            drawer.closeDrawers();
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

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
    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    // ===================================
    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    // ===================================
    @SuppressLint("InlinedApi")
    private void show() {
        mVisible = true;
        mHideHandler.removeCallbacks(mHidePart2Runnable);
    }

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
//        view.setWebEventsListener(this::webViewEvents);
        // SIGNAL 11 SIGSEGV crash Android
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        return view;
    }

    // ===================================
    protected void loadHtml(String url) {
//        String root = preference.getBoolean("sw_debug_mode", false)
//                ? getResources().getString(R.string.root_debug) :
//                getResources().getString(R.string.root);
        System.out.println("trace | loadHtml:" + url);

        newWebView.loadUrl(url);
    }



    // ===================================================
    // HTML APP request events
    // ===================================================
//    public void webViewEvents(int request, final String jsonString) {
//        JSONObject requestContent = new JSONObject();
//        System.out.println("webViewEvents | Request : " + request + "\n" + jsonString);
//        JSONObject uiRequest;
//        try {
//            uiRequest = new JSONObject(jsonString);
//            if (uiRequest.has("request")) {
//                requestContent = uiRequest.getJSONObject("request");
//            } else {
//                requestContent = uiRequest;
//            }
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        System.out.println("Request : " + request);
//        switch (request) {
//            case JSConstants.EVT_MAIN_TEST:
//                assert newWebView != null;
//                newWebView.callbackToUI(JSConstants.EVT_MAIN_TEST, CustomWebView.createResponse(requestContent, null));
//                break;
//            case JSConstants.EVT_READY:
//                assert newWebView != null;
//                newWebView.callbackToUI(JSConstants.CMD_INIT, CustomWebView.createResponse(requestContent, initData(this)));
//                break;
//            case JSConstants.EVT_PAGE_FINISHED:
//                onPageFinished();
//                break;
//            default:
//                System.out.println("Unsupported command : " + request);
//                break;
//        }
//    }




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
    // ===================================

//    @Override
//    public boolean onSupportNavigateUp() {
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
//        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
//                || super.onSupportNavigateUp();
//    }
}