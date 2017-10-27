package com.idealsee.sdk.game;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yaolei on 16-11-28.
 */

public class GamePhoneBindInfo {
    private static final String DATA = "data";
    private static final String UTC_TT = "utc_tt";
    private static final String EXPIRED = "expired";
    private static final String SC_LEFT = "sc_left";

    public long utcTimeTmp;
    public long expiredTimeTmp;
    public int requesCodeTimesLeft;

    public void init() {
        utcTimeTmp = 0L;
        expiredTimeTmp = 0L;
        requesCodeTimesLeft = 0;
    }

    public GamePhoneBindInfo(JSONObject json) {
        init();
        try {
            if (json.has(DATA)) {
                JSONObject dataJson = json.getJSONObject(DATA);
                if (dataJson.has(UTC_TT)) {
                    utcTimeTmp = dataJson.getLong(UTC_TT);
                }
                if (dataJson.has(EXPIRED)) {
                    expiredTimeTmp = dataJson.getLong(EXPIRED);
                }
                if (dataJson.has(SC_LEFT)) {
                    requesCodeTimesLeft = dataJson.getInt(SC_LEFT);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();

        }
    }
}
