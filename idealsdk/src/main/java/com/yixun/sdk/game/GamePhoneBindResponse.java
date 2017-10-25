package com.yixun.sdk.game;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yaolei on 16-12-1.
 */

public class GamePhoneBindResponse {
    public static final int APP_DEMO_GOLD_REGISTER_SC_EXPIRED = 0xF00C; // 验证码过期
    public static final int APP_DEMO_GOLD_SC_ATTEMPT_LIMIT = 0xF00D; //验证码错误次数达到最大(5次)，此时会清理验证码缓存，验证码失效。
    public static final int APP_DEMO_GOLD_REGISTER_INVALID_SC = 0xF00E; // 验证码错误
    public static final int APP_DEMO_GOLD_REGISTER_TEL_CONFLICT = 0xF00F; //手机号已被使用
    public static final String DATA = "data";
    public static final String UID = "uid";
    public static final String CODE = "code";

    public String uid;
    public int code;

    private void init() {
        uid = "";
        code = 0;
    }

    public GamePhoneBindResponse(JSONObject json) {
        init();
        try {
            if (json.has(DATA)) {
                JSONObject dataJson = json.getJSONObject(DATA);
                if (dataJson.has(UID)) {
                    uid = dataJson.getString(UID);
                }
            }
            if (json.has(CODE)) {
                code = json.getInt(CODE);
            }
        } catch (JSONException e) {
            e.printStackTrace();

        }
    }
}
