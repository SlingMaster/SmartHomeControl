/*
 * Copyright (c) 2020.
 * JSC
 * Design and Programming by Alex Dovby
 */

package com.jsc.smarthome;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.core.content.ContextCompat;

public class MyCustomAdapter extends BaseAdapter {
    private ArrayList<String> mListItems;
    private LayoutInflater mLayoutInflater;
    private Context context;

    MyCustomAdapter(Context context, ArrayList<String> arrayList) {
        this.context = context;
        mListItems = arrayList;

        //get the layout inflater
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        //getCount() represents how many items are in the list
        return mListItems.size();
    }

    @Override
    //get the data of an item from a specific position
    //i represents the position of the item in the list
    public Object getItem(int i) {
        return null;
    }

    @Override
    //get the position id of the item from the list
    public long getItemId(int i) {
        return 0;
    }

    @Override

    public View getView(int position, View view, ViewGroup viewGroup) {
        //check to see if the reused view is null or not, if is not null then reuse it
        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.list_item, viewGroup, false);
        }

        //get the string item from the position "position" from array list to put it on the TextView

        String stringItem = mListItems.get(position);
        // int bgColor = R.drawable.item_exo_bg;
        // int textColor = R.color.colorPrimary;
        int iconID = R.drawable.ic_link_test;


        if (stringItem != null) {
            TextView itemDate = view.findViewById(R.id.list_item_text_date);
            TextView itemTime = view.findViewById(R.id.list_item_text_time);
            TextView itemDelta = view.findViewById(R.id.list_item_text_delta);
            TextView itemValue = view.findViewById(R.id.list_item_text_value);

            // set icon -----------------------------------------
            ImageView itemIcon = view.findViewById(R.id.list_item_image_view);
            itemIcon.setImageResource(iconID);

            if (itemDate != null) {
                JSONObject obj = new JSONObject();
                try {
                    obj = new JSONObject(stringItem);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String date = obj.optString("date");
                if (date.length() < 7) {
                    itemDate.setText("");
                    itemValue.setTextColor(ContextCompat.getColor(context, R.color.colorIconsTitle));
                    itemValue.setText(date);
                    itemTime.setText("");
                    itemIcon.setImageResource(R.drawable.ic_title);
                    itemDelta.setText("");
                    view.setBackgroundResource(R.color.colorTitleList);
                    itemIcon.setColorFilter(getIconColor(obj.optString("")));

                } else {
                    itemDate.setText(date);
                    itemValue.setTextColor(ContextCompat.getColor(context, R.color.item_text_date));
                    itemTime.setText(obj.optString("time"));
                    String str_tmp = "Time: " + obj.optString("time");
                    itemTime.setText(str_tmp);
                    str_tmp = obj.optString("value") + "°C";
                    itemValue.setText(str_tmp);
                    itemIcon.setColorFilter(getIconColor(obj.optString("attribute")));
                    itemDelta.setText(obj.optString("delta"));
                    view.setBackgroundResource(R.color.colorPrimary);

                    if (obj.optBoolean("warmer")) {
                        itemDelta.setTextColor(ContextCompat.getColor(context, R.color.item_text_warmer));
                    } else {
                        itemDelta.setTextColor(ContextCompat.getColor(context, R.color.colorIconsTitle));
                    }
                }
            }
        }
        // this method must return the view corresponding to the data at the specified position.
        return view;
    }

    private Integer getIconColor(String attribute) {
        // System.out.println("attribute : |" + attribute + "|");
        switch (attribute) {
            case "hot":
                return Color.argb(255, 255, 0, 33);
            case "warm":
                return Color.argb(255, 255, 204, 0);
            default:
                return Color.argb(255, 0, 204, 255);
        }
    }
}