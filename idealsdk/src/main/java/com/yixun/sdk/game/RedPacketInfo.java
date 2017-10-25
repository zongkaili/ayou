package com.yixun.sdk.game;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class RedPacketInfo implements Serializable {

    private static final String WIDGET_ID = "id";
    private static final String RED_AUTH = "red_auth";
    private static final String GAME_DURATION = "red_game_duration";
    private static final String SHARED_CAN_AGAIN = "red_shared_can_again";

    public String widgetID = "";
    public String md5 = "";
    public int redAuth = 0;
    public int redGameDuration = 0;
    public boolean redSharedCanAgain = false;

    public RedPacketInfo(JSONObject json) {
        try {
            if (json.has(WIDGET_ID)) {
                widgetID = json.getString(WIDGET_ID);
            }
            if (json.has(RED_AUTH)) {
                redAuth = json.getInt(RED_AUTH);
            }
            if (json.has(GAME_DURATION)) {
                redGameDuration = json.getInt(GAME_DURATION);
            }
            if (json.has(SHARED_CAN_AGAIN)) {
                if (json.getInt(SHARED_CAN_AGAIN) == 1) {
                    redSharedCanAgain = true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
