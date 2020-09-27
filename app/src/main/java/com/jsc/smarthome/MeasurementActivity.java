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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

//import java.security.acl.Permission;

public class MeasurementActivity extends AppCompatActivity {
    SharedPreferences preference;
    final static int REQUEST_CODE_CLEAR = 1;
    static String cur_ssid;


    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private boolean mVisible;

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
        preference = PreferenceManager.getDefaultSharedPreferences(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        cur_ssid = getCurrentSsid(getApplicationContext());

        System.out.println("trace Create | Cur ssid : " + cur_ssid);
        System.out.println("trace | preference : measurement " + preference.getString("edit_measurement", "12"));
        System.out.println("trace | Data Directory : " + Environment.getDataDirectory());


        Permission.requestMultiplePermissions(this, Permission.PERMISSION_REQUEST_CODE);
        webContainer = findViewById(R.id.web_container);
        // load screen -----------------------------
        boolean debug = preference.getBoolean("sw_dev_mode", false);
        String url = debug ? getResources().getString(R.string.debug_test_url) : getResources().getString(R.string.test_url);
        System.out.println("trace | debug : " + debug + " | url : " + url);
        loadHtml(url);
    }

    // ===================================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CODE_CLEAR) {
                String idClear = data.getStringExtra("clear");
                if (idClear != null && idClear.equalsIgnoreCase("all_records")) {
                    jsonDataBaseArray = new JSONArray();
                    // test ===========
//                    String itemStr = "{\"date\":\"10 Desember 2019\",\"time\":\"11:47\",\"value\":\"32.6\",\"attribute\":\"cool\",\"warmer\":true,\"delta\":\"Δ 1.2°C\",\"action\":\"save result\"}\"";
//                    jsonDataBaseArray.put(itemStr);
                    //                    // ================
                } else {
                    jsonDataBaseArray.remove(jsonDataBaseArray.length() - 1);
                }
                FileUtils.writeToFile(jsonDataBaseArray.toString(), getApplicationContext());
            }
        }
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
        newWebView.clearCache(preference.getBoolean("sw_clear_cache", false));
        newWebView.loadUrl(url);
    }

    // ===================================
    void onPageFinished() {
//        Handler handler = new Handler();
//        Toast.makeText(getBaseContext(), "TimeOut • onPageFinished", Toast.LENGTH_SHORT).show();

//       handler.postDelayed(() ->
//                Toast.makeText(getBaseContext(), "TimeOut • onPageFinished",
//                        Toast.LENGTH_SHORT).show() ,1000);


        // load json BD results ------------------------------

        FileUtils.writeToFile("", getApplicationContext());
        String strBD = FileUtils.readFromFile(getApplicationContext());

        if (strBD.equalsIgnoreCase("")) {
            jsonDataBaseArray = new JSONArray();
            // restore database -------
//            jsonDataBaseArray.put("{\"date\":\"18 January 2018\",\"time\":\"11:47\",\"value\":\"37.6\",\"attribute\":\"warm\",\"warmer\":true,\"delta\":\"Δ 1.2°C\",\"action\":\"save result\"}");
//            jsonDataBaseArray.put("{\"date\":\"14 February 2018\",\"time\":\"12:47\",\"value\":\"35.0\",\"attribute\":\"cool\",\"warmer\":true,\"delta\":\"Δ 2.2°C\",\"action\":\"save result\"}");
//            jsonDataBaseArray.put("{\"date\":\"16 March 2018\",\"time\":\"10:20\",\"value\":\"38.5\",\"attribute\":\"warm\",\"warmer\":true,\"delta\":\"Δ 2.5°C\",\"action\":\"save result\"}");
//            jsonDataBaseArray.put("{\"date\":\"21 April 2018\",\"time\":\"18:02\",\"value\":\"37.2\",\"attribute\":\"warm\",\"warmer\":false,\"delta\":\"Δ -0.6°C\",\"action\":\"save result\"}");
//            jsonDataBaseArray.put("{\"date\":\"16 May 2018\",\"time\":\"09:48\",\"value\":\"33.1\",\"attribute\":\"cool\",\"warmer\":true,\"delta\":\"Δ 1.3°C\",\"action\":\"save result\"}");
//            jsonDataBaseArray.put("{\"date\":\"19 June 2018\",\"time\":\"11:05\",\"value\":\"35.9\",\"attribute\":\"cool\",\"warmer\":true,\"delta\":\"Δ 0.5°C\",\"action\":\"save result\"}");
//            jsonDataBaseArray.put("{\"date\":\"18 July 2018\",\"time\":\"11:46\",\"value\":\"26.0\",\"attribute\":\"cool\",\"warmer\":true,\"delta\":\"Δ 0.1°C\",\"action\":\"save result\"}");
//            jsonDataBaseArray.put("{\"date\":\"21 August 2018\",\"time\":\"11:23\",\"value\":\"35.4\",\"attribute\":\"cool\",\"warmer\":true,\"delta\":\"Δ 0.6°C\",\"action\":\"save result\"}");
//            jsonDataBaseArray.put("{\"date\":\"17 September 2018\",\"time\":\"14:12\",\"value\":\"33.6\",\"attribute\":\"cool\",\"warmer\":false,\"delta\":\"Δ -0.1°C\",\"action\":\"save result\"}");
//            jsonDataBaseArray.put("{\"date\":\"17 October 2018\",\"time\":\"07:59\",\"value\":\"38.9\",\"attribute\":\"warm\",\"warmer\":false,\"delta\":\"Δ -1.1°C\",\"action\":\"save result\"}");
//            jsonDataBaseArray.put("{\"date\":\"19 November 2018\",\"time\":\"12:33\",\"value\":\"34.1\",\"attribute\":\"cool\",\"warmer\":true,\"delta\":\"Δ 1.1°C\",\"action\":\"save result\"}");
//            jsonDataBaseArray.put("{\"date\":\"17 December 2018\",\"time\":\"11:31\",\"value\":\"39.8\",\"attribute\":\"warm\",\"warmer\":true,\"delta\":\"Δ 0.6°C\",\"action\":\"save result\"}");
            // 2019--------------------
            jsonDataBaseArray.put("{\"date\":\"18 January 2019\",\"time\":\"11:47\",\"value\":\"39.8\",\"attribute\":\"warm\",\"warmer\":true,\"delta\":\"Δ 1.2°C\",\"action\":\"save result\"}");
            jsonDataBaseArray.put("{\"date\":\"14 February 2019\",\"time\":\"12:47\",\"value\":\"38.6\",\"attribute\":\"warm\",\"warmer\":true,\"delta\":\"Δ 2.2°C\",\"action\":\"save result\"}");
            jsonDataBaseArray.put("{\"date\":\"16 March 2019\",\"time\":\"10:20\",\"value\":\"40.0\",\"attribute\":\"hot\",\"warmer\":true,\"delta\":\"Δ 2.5°C\",\"action\":\"save result\"}");
            jsonDataBaseArray.put("{\"date\":\"21 April 2019\",\"time\":\"11:02\",\"value\":\"36.0\",\"attribute\":\"cool\",\"warmer\":false,\"delta\":\"Δ -0.6°C\",\"action\":\"save result\"}");
            jsonDataBaseArray.put("{\"date\":\"16 May 2019\",\"time\":\"09:48\",\"value\":\"34.0\",\"attribute\":\"cool\",\"warmer\":true,\"delta\":\"Δ 1.3°C\",\"action\":\"save result\"}");
            jsonDataBaseArray.put("{\"date\":\"19 June 2019\",\"time\":\"11:05\",\"value\":\"37.0\",\"attribute\":\"warm\",\"warmer\":true,\"delta\":\"Δ 0.5°C\",\"action\":\"save result\"}");
            jsonDataBaseArray.put("{\"date\":\"18 July 2019\",\"time\":\"11:46\",\"value\":\"39.6\",\"attribute\":\"warm\",\"warmer\":true,\"delta\":\"Δ 0.1°C\",\"action\":\"save result\"}");
            jsonDataBaseArray.put("{\"date\":\"21 August 2019\",\"time\":\"11:23\",\"value\":\"35.2\",\"attribute\":\"cool\",\"warmer\":true,\"delta\":\"Δ 0.6°C\",\"action\":\"save result\"}");
            jsonDataBaseArray.put("{\"date\":\"17 September 2019\",\"time\":\"14:12\",\"value\":\"35.2\",\"attribute\":\"cool\",\"warmer\":false,\"delta\":\"Δ -0.1°C\",\"action\":\"save result\"}");
            jsonDataBaseArray.put("{\"date\":\"17 October 2019\",\"time\":\"07:59\",\"value\":\"36.4\",\"attribute\":\"cool\",\"warmer\":false,\"delta\":\"Δ -1.1°C\",\"action\":\"save result\"}");
            jsonDataBaseArray.put("{\"date\":\"19 November 2019\",\"time\":\"12:33\",\"value\":\"31.9\",\"attribute\":\"cool\",\"warmer\":true,\"delta\":\"Δ 1.1°C\",\"action\":\"save result\"}");
            jsonDataBaseArray.put("{\"date\":\"17 December 2019\",\"time\":\"11:31\",\"value\":\"32.0\",\"attribute\":\"cool\",\"warmer\":true,\"delta\":\"Δ 0.6°C\",\"action\":\"save result\"}");
            // 2020--------------------
            jsonDataBaseArray.put("{\"date\":\"09 January 2020\",\"time\":\"11:08\",\"value\":\"31.0\",\"attribute\":\"cool\",\"warmer\":false,\"delta\":\"Δ -0.4°C\",\"action\":\"save result\"}");
            jsonDataBaseArray.put("{\"date\":\"05 February 2020\",\"time\":\"11:19\",\"value\":\"36.0\",\"attribute\":\"cool\",\"warmer\":false,\"delta\":\"Δ -0.6°C\",\"action\":\"save result\"}");
            jsonDataBaseArray.put("{\"date\":\"05 March 2020\",\"time\":\"11:00\",\"value\":\"33.0\",\"attribute\":\"cool\",\"warmer\":false,\"delta\":\"Δ -0.2°C\",\"action\":\"save result\"}");
            jsonDataBaseArray.put("{\"date\":\"07 April 2020\",\"time\":\"12:01\",\"value\":\"--.-\",\"attribute\":\"cool\",\"warmer\":false,\"delta\":\"Δ 0.0°C\",\"action\":\"save result\"}");
            jsonDataBaseArray.put("{\"date\":\"06 May 2020\",\"time\":\"11:48\",\"value\":\"--.-\",\"attribute\":\"cool\",\"warmer\":false,\"delta\":\"Δ 0.0°C\",\"action\":\"save result\"}");
            jsonDataBaseArray.put("{\"date\":\"09 June 2020\",\"time\":\"11:30\",\"value\":\"38.0\",\"attribute\":\"warm\",\"warmer\":true,\"delta\":\"Δ 0.6°C\",\"action\":\"save result\"}");
            jsonDataBaseArray.put("{\"date\":\"09 July 2020\",\"time\":\"11:05\",\"value\":\"39.1\",\"attribute\":\"warm\",\"warmer\":false,\"delta\":\"Δ -0.4°C\",\"action\":\"save result\"}");
            jsonDataBaseArray.put("{\"date\":\"05 August 2020\",\"time\":\"10:20\",\"value\":\"33.7\",\"attribute\":\"cool\",\"warmer\":true,\"delta\":\"Δ 0.5°C\",\"action\":\"save result\"}");
            jsonDataBaseArray.put("{\"date\":\"07 September 2020\",\"time\":\"12:15\",\"value\":\"39.7\",\"attribute\":\"warm\",\"warmer\":false,\"delta\":\"Δ -0.4°C\",\"action\":\"save result\"}");

            FileUtils.writeToFile(jsonDataBaseArray.toString(), getApplicationContext());
        } else {
            jsonDataBaseArray = parseFileDataBase(strBD);
        }
        System.out.println("trace | Read From File : " + FileUtils.readFromFile(getApplicationContext()));
    }


    // ===================================================
    // HTML APP request events
    // ===================================================
    public void webViewEvents(int request, final String jsonString) {
        JSONObject requestContent = new JSONObject();
        System.out.println("trace | webViewEvents | Request : " + request + "\n" + jsonString);
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
        System.out.println("Request : " + request + " | requestContent " + requestContent);
        switch (request) {
            case JSConstants.EVT_MAIN_TEST:
                assert newWebView != null;
                newWebView.callbackToUI(JSConstants.EVT_MAIN_TEST, CustomWebView.createResponse(requestContent, null));
                break;
            case JSConstants.EVT_READY:
                assert newWebView != null;
                newWebView.callbackToUI(JSConstants.CMD_INIT, CustomWebView.createResponse(requestContent, initData(this)));
                break;
            case JSConstants.CMD_MEASUREMENT_START:
                System.out.println("trace | MEASUREMENT START | " + request + " \n" + jsonString);

                break;
            case JSConstants.CMD_MEASUREMENT_RESULT:
                jsonDataBaseArray.put(jsonString);
                FileUtils.writeToFile(jsonDataBaseArray.toString(), getApplicationContext());
                System.out.println("trace | request : " + request);
                showListBD(getApplicationContext());
                break;
            case JSConstants.CMD_SHOW_LIST:
                String strBD = FileUtils.readFromFile(getApplicationContext());
                jsonDataBaseArray = parseFileDataBase(strBD);
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
        System.out.println("trace | Start initData");
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean is_home_network;
        JSONObject obj = new JSONObject();

        String home_ssid = preference.getString("home_ssid", getResources().getString(R.string.ssid_default));
        assert home_ssid != null;
        is_home_network = home_ssid.equalsIgnoreCase(cur_ssid);
        System.out.println("trace | cur_ssid: " + cur_ssid + " | home_ssid:" + home_ssid + " | " + is_home_network);
        System.out.println("trace | interval:" + preference.getString("edit_measurement", "12") + " | " + preference.getString("esp_ip", "N/A"));
        try {
            obj.put("android_os", android.os.Build.VERSION.SDK_INT);
            obj.put("language", "en");
            obj.put("esp_ip", preference.getString("esp_ip", getResources().getString(R.string.edit_esp_ip_default)));
            obj.put("measurement_interval", (Integer.parseInt(Objects.requireNonNull(preference.getString("edit_measurement", "120")))));
            obj.put("is_home_network", is_home_network);
            obj.put("network", (is_home_network ? getResources().getString(R.string.home_network) : getResources().getString(R.string.guest_network)));
            obj.put("ssid", cur_ssid);
            System.out.println("trace | initData " + obj.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return obj;
    }

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

    // ===================================
    // utils
    // ===================================
    public static String getCurrentSsid(Context context) {
        String ssid = null;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo;
        if (cm != null) {
            networkInfo = cm.getActiveNetworkInfo();
            if (networkInfo == null) {
                return null;
            }

            if (networkInfo.isConnected()) {
                final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager != null) {
                    final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                    if (connectionInfo != null) {
                        ssid = connectionInfo.getSSID().replaceAll("\"", "");
                    }
                }
            }
        }
        return ssid;
    }
}