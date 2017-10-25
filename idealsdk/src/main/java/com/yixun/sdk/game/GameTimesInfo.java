package com.yixun.sdk.game;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yaolei on 16-11-28.
 */

public class GameTimesInfo {
    private static final String CODE = "code";
    private static final String MSG = "msg";
    private static final String STATUS = "status";
    private static final String DATA = "data";
    private static final String COUNT = "count";
    private static final String USER = "user";

    public int code;
    public String msg;
    public String status;
    public int count;
    public String user;

    public void init() {
        code = 0;
        msg = "";
        status = "";
        count = 0;
        user = "";
    }

    public GameTimesInfo(JSONObject json) {
        init();
        if (json == null)
            return;
        try {
            if (json.has(CODE)) {
                code = json.getInt(CODE);
            }
            if (json.has(MSG)) {
                msg = json.getString(MSG);
            }
            if (json.has(STATUS)) {
                status = json.getString(STATUS);
            }
            if (json.has(DATA)) {
                JSONObject dataJson = json.getJSONObject(DATA);
                if (dataJson.has(COUNT)) {
                    count = dataJson.getInt(COUNT);
                }
                if (dataJson.has(USER)) {
                    user = dataJson.getString(USER);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
