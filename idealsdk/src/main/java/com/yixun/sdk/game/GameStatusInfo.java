package com.yixun.sdk.game;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yaolei on 16-11-28.
 */

public class GameStatusInfo {
    public static final int STATUS_OVER = 1;
    public static final int STATUS_OK = 0;
    private static final String DATA = "data";
    private static final String STATUS = "game_status";
    private static final String ACTIVITY = "activity";
    private static final String ADMITTANCE = "admittance";
    private static final String USER_NAME = "user_name";
    private static final String USER_UID = "uid";

    public boolean gameIsOpen;
    public String activityId;
    public String admittance;
    public String userName;
    public String userUid;

    private void init() {
        gameIsOpen = false;
        activityId = "";
        admittance = "";
        userName = "";
        userUid = "";
    }

    public GameStatusInfo(JSONObject json) {
        init();
        if(json == null)
            return;
        try {
            if (json.has(DATA)) {
                JSONObject dataJson = json.getJSONObject(DATA);
                if (dataJson.has(STATUS)) {
                    gameIsOpen = dataJson.getInt(STATUS) == 0;
                }
                if (dataJson.has(ACTIVITY)) {
                    activityId = dataJson.getString(ACTIVITY);
                }
                if (dataJson.has(ADMITTANCE)) {
                    admittance = dataJson.getString(ADMITTANCE);
                }
                if (dataJson.has(USER_NAME)) {
                    userName = dataJson.getString(USER_NAME);
                }
                if (dataJson.has(USER_UID)) {
                    userUid = dataJson.getString(USER_UID);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
