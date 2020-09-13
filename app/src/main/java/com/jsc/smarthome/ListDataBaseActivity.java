
/*
 * Copyright (c) 2020.
 * JSC
 * Design and Programming by Alex Dovby
 */

package com.jsc.smarthome;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class ListDataBaseActivity extends AppCompatActivity {
    private ArrayList<String> arrayList;
    private MyCustomAdapter mAdapter;
    AlertDialog.Builder mAlertDialog;
    final int MENU_ID = 2;

    // =========================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_data_base);

        Intent intent = getIntent();
        String jsonListArray = intent.getStringExtra("jsonList");

        arrayList = new ArrayList<String>();

        ListView mList = findViewById(R.id.list);
        mAdapter = new MyCustomAdapter(getApplicationContext(), arrayList);
        mList.setAdapter(mAdapter);
        createJsonList(jsonListArray);

        // ----------------------------------------
        // dialog clear data base
        // ----------------------------------------
        Context context = ListDataBaseActivity.this;
        mAlertDialog = new AlertDialog.Builder(context);
        mAlertDialog.setIcon(R.drawable.ic_warning);
        mAlertDialog.setTitle(R.string.title_dialog);
        mAlertDialog.setMessage(R.string.message_dialog);

        mAlertDialog.setPositiveButton(R.string.button_all_records, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                // del all records ------
                sendTypeClear("all_records");
            }
        });
        mAlertDialog.setNegativeButton(R.string.button_last_records, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                // del last record ------
                sendTypeClear("last_record");
            }
        });

        mAlertDialog.setNeutralButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        mAlertDialog.setCancelable(true);
        // ----------------------------------------
    }

    // =========================================================
    private void sendTypeClear(String clearType) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("clear", clearType);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    // =========================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_clear, menu);
        menu.add(0, MENU_ID, 0, R.string.menu_item_clear)
                .setIcon(R.drawable.ic_delete)
                .setShowAsAction(
                        MenuItem.SHOW_AS_ACTION_ALWAYS
                                | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

    // =========================================================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        System.out.println("Menu id : " + id);
        if (id == MENU_ID) {
            System.out.println("Clear : " + id);
            mAlertDialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // BD List =================================================
    private void addItem(String jsonString) {
        //add the text in the arrayList
        arrayList.add(jsonString);
        //refresh the list
        mAdapter.notifyDataSetChanged();
    }

    // --------------------------
    private void createJsonList(String jsonList) {
        // System.out.println("createJsonList: " + jsonList);
        try {

            JSONArray jsonArray = new JSONArray(jsonList);
            for (int i = 0; i < jsonArray.length(); i++) {

                try {
                    JSONObject obj = new JSONObject(jsonArray.get(i).toString());
                    System.out.println("trace | reateJsonList #" + i + " | " + obj.optString("date"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                addItem(jsonArray.get(i).toString());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
