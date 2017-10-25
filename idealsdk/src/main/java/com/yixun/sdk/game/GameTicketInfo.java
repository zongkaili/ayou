package com.yixun.sdk.game;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yaolei on 16-11-28.
 */

public class GameTicketInfo {
    public static final String STATUS = "status";
    public static final String CODE = "code";
    public static final String DATA = "data";
    public static final String MSG = "msg";
    public static final String TICKET = "ticket";//单次游戏唯一标识
    public static final String USER = "user";//用户标识

    public String status;
    public String errorCode;
    public String errorMsg;
    public String ticket;
    public String user;

    private void init() {
        status = "";
        errorCode = "";
        errorMsg = "";
        ticket = "";
        user = "";
    }

    public GameTicketInfo(JSONObject json) {
        init();
        try {
            if (json.has(STATUS)) {
                status = json.getString(STATUS);
            }
            if (json.has(CODE)) {
                errorCode = json.getString(CODE);
            }
            if (json.has(MSG)) {
                errorMsg = json.getString(MSG);
            }
            if (json.has(DATA)) {
                JSONObject dataJson = json.getJSONObject(DATA);
                initData(dataJson);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initData(JSONObject dataJson) throws JSONException {
        if (dataJson.has(TICKET)) {
            ticket = dataJson.getString(TICKET);
        }

        if (dataJson.has(USER)) {
            user = dataJson.getString(USER);
        }
    }
}