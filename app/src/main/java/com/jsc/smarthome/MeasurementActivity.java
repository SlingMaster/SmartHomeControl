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
import android.os.Environment;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.jsc.smarthome.html.CustomWebView;
import com.jsc.smarthome.html.JSConstants;
import com.jsc.smarthome.html.JSOut;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

//import java.security.acl.Permission;

public class MeasurementActivity extends AppCompatActivity {
    SharedPreferences preference;
    // Database Name
    final String FILE_DB = "/data_base.json";
    final static int REQUEST_CODE_CLEAR = 1;
    static String cur_ssid;
//    private static final String DATABASE_NAME = "data_base.json";
//    private static final String APP_DIR = "smarthome";

    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private boolean mVisible;


    //    final String FILE_DB = "/smarthome/data_base.json";
    //    final String FILE_DB = getDBPath(getApplicationContext());
    // "/Android/data/com.jsc.smarthome/files/data_base.json";
    ViewGroup webContainer;
    @Nullable
    CustomWebView newWebView;

    // js interface ------------
    protected JSOut jsOut;
    public JSONObject uiRequest;
    public static JSONArray jsonDataBaseArray = new JSONArray();

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
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
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
        setContentView(R.layout.activity_measurement);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        System.out.println("trace | Data Directory : " + Environment.getDataDirectory());
//        System.out.println("trace | External Storage : " + getDBPath(this.getApplicationContext());
        Permission.requestMultiplePermissions(this, Permission.PERMISSION_REQUEST_CODE);
//        checkPermissions();
        webContainer = findViewById(R.id.web_container);
        // load screen -----------------------------
        loadHtml(getResources().getString(R.string.test_url));


    }

    // ===================================
    public void showListBD(Context context) {
        if (jsonDataBaseArray.length() > 0) {
            Intent bdInten = new Intent(context, ListDataBaseActivity.class);
            bdInten.putExtra("jsonList", jsonDataBaseArray.toString());
            startActivityForResult(bdInten, REQUEST_CODE_CLEAR);
        } else {
            Toast.makeText(this, R.string.msg_list_records, Toast.LENGTH_LONG).show();
        }
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
    // WebView
    // ===================================
    @NonNull
    private CustomWebView createWebView() {
        CustomWebView view = new CustomWebView(this);
        view.setWebEventsListener(this::webViewEvents);
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
        newWebView = createWebView();
        webContainer.addView(newWebView);
//        newWebView.clearCache(preference.getBoolean("sw_clear_cache", false));
        newWebView.loadUrl(url);
    }

    // ===================================
    void onPageFinished() {
        Handler handler = new Handler();
//        Toast.makeText(getBaseContext(), "TimeOut • onPageFinished", Toast.LENGTH_SHORT).show();

        handler.postDelayed(() ->
                Toast.makeText(getBaseContext(), "TimeOut • onPageFinished",
                        Toast.LENGTH_SHORT).show(), 1000);

        // load json BD results ------------------------------
        String strBD = FileUtils.readFile(this, FILE_DB);
        System.out.println("trace | ========= Read BD File : " + FileUtils.readFile(this, FILE_DB));


        if (strBD != null) {
            jsonDataBaseArray = parseFileDataBase(FileUtils.readFile(this, FILE_DB));
        } else {

            Toast.makeText(getBaseContext(), "BD Empty", Toast.LENGTH_SHORT).show();
            // test showListBD ========
//            String jsonStr = "[{\"date\":\"Test\",\"time\":\"11:47\",\"value\":\"37.6\",\"attribute\":\"warm\",\"warmer\":true,\"delta\":\"Δ 1.2°C\",\"action\":\"save result\"},\"{\\\"date\\\":\\\"14 February 2018\\\",\\\"time\\\":\\\"12:47\\\",\\\"value\\\":\\\"35.0\\\",\\\"attribute\\\":\\\"cool\\\",\\\"warmer\\\":true,\\\"delta\\\":\\\"Δ -2.2°C\\\",\\\"action\\\":\\\"save result\\\"}\"]";
//            jsonDataBaseArray = parseFileDataBase(jsonStr);
//            showListBD(getApplicationContext());
            // ========================

            FileUtils.SaveFile(FILE_DB, jsonDataBaseArray.toString());
        }
    }

    // ===================================================
    // HTML APP request events
    // ===================================================
    public void webViewEvents(int request, final String jsonString) {
        JSONObject requestContent = new JSONObject();
        System.out.println("webViewEvents | Request : " + request + "\n" + jsonString);
        JSONObject uiRequest;
        try {
            uiRequest = new JSONObject(jsonString);
            if (uiRequest.has("request")) {
                requestContent = uiRequest.getJSONObject("request");
            } else {
                requestContent = uiRequest;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println("Request : " + request);
        switch (request) {
            case JSConstants.EVT_MAIN_TEST:
                assert newWebView != null;
                newWebView.callbackToUI(JSConstants.EVT_MAIN_TEST, CustomWebView.createResponse(requestContent, null));
                break;
            case JSConstants.EVT_READY:
                assert newWebView != null;
                newWebView.callbackToUI(JSConstants.CMD_INIT, CustomWebView.createResponse(requestContent, initData(this)));
                break;
            case JSConstants.CMD_MEASUREMENT_RESULT:
                System.out.println("trace | request : " + request);
                jsonDataBaseArray.put(jsonString);
                FileUtils.SaveFile(FILE_DB, jsonDataBaseArray.toString());
                break;
            case JSConstants.CMD_SHOW_LIST:
                jsonDataBaseArray = parseFileDataBase(FileUtils.readFile(this, FILE_DB));
                // test showListBD ========
                if (jsonDataBaseArray == null) {
                    String jsonStr = "[{\"date\":\"18 January 2018\",\"time\":\"11:47\",\"value\":\"37.6\",\"attribute\":\"warm\",\"warmer\":true,\"delta\":\"Δ 1.2°C\",\"action\":\"save result\"},\"{\\\"date\\\":\\\"14 February 2018\\\",\\\"time\\\":\\\"12:47\\\",\\\"value\\\":\\\"35.0\\\",\\\"attribute\\\":\\\"cool\\\",\\\"warmer\\\":true,\\\"delta\\\":\\\"Δ -2.2°C\\\",\\\"action\\\":\\\"save result\\\"}\"]";
                    jsonDataBaseArray = parseFileDataBase(jsonStr);
                }
                // ========================
                showListBD(getApplicationContext());
                break;
            case JSConstants.EVT_PAGE_FINISHED:
                onPageFinished();
                break;
            default:
                System.out.println("trace | Unsupported command : " + request);
                break;
        }
    }

    public JSONObject initData(@NonNull Context context) {
        boolean is_home_network;
        JSONObject obj = new JSONObject();

        String home_ssid = preference.getString("home_ssid", getResources().getString(R.string.ssid_default));
        is_home_network = home_ssid.equalsIgnoreCase(cur_ssid);

        System.out.println("trace | cur_ssid: " + cur_ssid + " | home_ssid:" + home_ssid + " | " + is_home_network);
        try {
            obj.put("android_os", android.os.Build.VERSION.SDK_INT);
            obj.put("language", "en");
            obj.put("esp_ip", preference.getString("esp_ip", getResources().getString(R.string.edit_esp_ip_default)));
            obj.put("measurement_interval", (Integer.parseInt(preference.getString("edit_measurement", "120"))));
            obj.put("is_home_network", is_home_network);
            obj.put("network", (is_home_network ? getResources().getString(R.string.home_network) : getResources().getString(R.string.guest_network)));
            obj.put("ssid", cur_ssid);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return obj;
    }

    // ===================================
//    public static JSONObject initData(@NonNull Context context) {
//        JSONObject obj = new JSONObject();
//        try {
//            obj.put("android_os", android.os.Build.VERSION.SDK_INT);
//            obj.put("language", "en");
//            obj.put("android_app", true);
//
//            // string to int ---------------------
////            String tempNumStr = preference.getString("start_day", "6");
////            int num = Integer.parseInt(tempNumStr);
////            obj.put("start_day", num);
//
//            // -----------------------------------
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return obj;
//    }

    // ===================================
    public static JSONArray parseFileDataBase(String jsonList) {
        // System.out.println("parseReadFile:" + jsonList);
        JSONArray json;
        try {
            json = new JSONArray(jsonList);
        } catch (JSONException e) {
            json = new JSONArray();
            e.printStackTrace();
        }
        return json;
    }

    // ===================================
    // Permissions
    // ===================================
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Permission.comparePermissions(this, requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        System.out.println("trace | Permissions Result OK");
    }

}