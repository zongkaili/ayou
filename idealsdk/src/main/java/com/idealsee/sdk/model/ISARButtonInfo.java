package com.idealsee.sdk.model;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ISARButtonInfo {
    public static final String BACKGROUND_COLOR = "color";
    public static final String START_TIME = "start_time";
    public static final String IMAGE_RADIUS = "radius";
    public static final String ACTION_TYPE = "type";
    public static final String ACTION = "action";
    public static final String MEDIA_TYPE = "media_type";
    public static final String FRONT_ICON = "icon";
    public static final String TYPE = "type";
    public static final String ACTION_ID = "id";
    public static final String MD5 = "md5";
    public static final String SHARE_WAY = "share_way";

    public float[] backgroundColor;
    public int startTime;
    public int radius;
    public int actionType;
    public String actionJson;
    public boolean showFrontIcon;
    public String md5;
    public String actionId;
    public int shareWay;

    public ISARButtonInfo() {
        backgroundColor = new float[]{255, 255, 255, 255};
        showFrontIcon = false;
        md5 = "";
        radius = 25;
        startTime = 0;
        actionType = 0;
        actionJson = "";
        actionId = "";
        shareWay = 0;
    }

    public ISARButtonInfo(JSONObject json) {
        this();
        try {
            if (json.has(BACKGROUND_COLOR)) {
                JSONArray array = json.getJSONArray(BACKGROUND_COLOR);
                for (int i = 0; i < array.length(); i++) {
                    if (!TextUtils.isEmpty(array.getString(i))) {
                        backgroundColor[i] = array.getInt(i);
                    }
                }
            }
            if (json.has(START_TIME) &&
                    !TextUtils.isEmpty(json.getString(START_TIME))) {
                startTime = json.getInt(START_TIME);
            }
            if (json.has(IMAGE_RADIUS) &&
                    !TextUtils.isEmpty(json.getString(IMAGE_RADIUS))) {
                radius = json.getInt(IMAGE_RADIUS);
            }
            if (json.has(ACTION)) {
                actionJson = json.getString(ACTION);
                JSONObject action = json.getJSONObject(ACTION);
                if (action.has(ACTION_TYPE)) {
                    actionType = action.getInt(ACTION_TYPE);
                }
                if (action.has(SHARE_WAY)) {
                    shareWay = action.getInt(SHARE_WAY);
                }
            }
            if (json.has(FRONT_ICON) &&
                    !TextUtils.isEmpty(json.getString(FRONT_ICON))) {
                showFrontIcon = json.getInt(FRONT_ICON) == 1;
            }
            if (json.has(MD5)) {
                md5 = json.getString(MD5);
            }

            if (json.has(ACTION_ID)) {
                actionId = json.getString(ACTION_ID);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(
                "color = (" + backgroundColor[0] + "," + backgroundColor[1] + "," + backgroundColor[2] + "," + backgroundColor[3] + ")\n");
        builder.append("show fornt icon = " + showFrontIcon + "\n");
        builder.append("icon url = " + md5 + "\n");
        builder.append("radius = " + radius + "\n");
        builder.append("start time = " + startTime + "\n");
        builder.append("action json = " + actionJson + "\n");
        builder.append("action type = " + actionType + "\n");
        builder.append("action share way = " + shareWay + "\n");
        builder.append("action id = " + actionId + "\n");
        builder.append("---------------------------------");
        return builder.toString();
    }
}
