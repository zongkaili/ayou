package com.idealsee.sdk.game;

import com.idealsee.sdk.util.Logger;
import com.idealsee.sdk.util.ISARConstants;
import com.idealsee.sdk.util.ISARStringUtil;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

/**
 * Created by yaolei on 16-11-26.
 */

public class GameSignFormatUtil {
    private static final String TAG = GameSignFormatUtil.class.getName();
    private static final String NONESTRING = "nonestr";
    public static final int INIT_ACTIVITY_ID = 1;
    public static final int INIT_DOCS = 2;
    public static final int INIT_AUTH_KET = 4;
    public static final int GAME_GET_CODE = 1;
    public static final int GAME_GET_STATUS = 4;
    public static final int GAME_GET_TIMES = 8;
    public static final int GAME_POST_BIND = 2;
    public static final int GAME_POST_DEC_TIMES = 16;
    public static final int GAME_POST_RESULT = 32;
    public static final int GAME_POST_ADD_TIMES = 64;
    public static final int GAME_POST_COUNT_TIMES = 128;
    public static final int COIN_GET_RANK_COINNUM = 1;
    public static final int COIN_GET_AWARDS = 2;
    public static final int COIN_GET_DEADLINE =4;
    public static final int COIN_GET_RANK_INFO = 8;

    private static String getUUIDString() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString().replace("-", "");
    }

    public static Map<String, String> getFormatArgsMap(Map<String, String> params) {
        String noneString = getUUIDString();
        Map<String, String> args = new TreeMap<>();
        args.putAll(params);
        args.put(NONESTRING, noneString);
        StringBuilder signStr = new StringBuilder();
        Set<Map.Entry<String, String>> allSet = args.entrySet();
        for (Map.Entry<String, String> entry : allSet) {
            signStr.append("&").append(entry.getKey()).append("=").append(entry.getValue());
        }
        signStr.append(ISARConstants.GAME_SIGN_KEY);
        String finalSign = ISARStringUtil.getMD5(signStr.toString().replaceFirst("&", ""));
        Logger.LOGD(TAG + "request Sign String :" + signStr.toString().replaceFirst("&", ""));
        args.put("sign", finalSign);
        return args;
    }
}
