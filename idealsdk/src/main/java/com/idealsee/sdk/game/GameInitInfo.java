package com.idealsee.sdk.game;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yaolei on 16-11-28.
 */

public class GameInitInfo {
    public static final String STATUS = "status";
    public static final String CODE = "code";
    public static final String DATA = "data";
    public static final String MSG = "msg";
    public static final String ACTIVITY_ID = "activity_id";
    public static final String DEADLINE = "deadline";//活动结束时刻
    public static final String GAME_CONFIG = "game_config";//游戏配置信息
    public static final String RED_ALLOT = "red_allot";//积分分配方式
    public static final String RED_GAME_DURATION = "red_game_duration";//单次游戏时长
    public static final String RED_PACKET_COUNT = "red_packet_count";//单次游戏红包总个数
    public static final String RED_RATIO = "red_ratio";//中奖难度
    public static final String RED_TOTAL_CASH = "red_total_cash";//单次游戏积分总数

    public String status;
    public String errorCode;
    public String errorMsg;
    public String activityId;
    public String deadline;
    public String gameConfig;
    public int redAllot;
    public int redGameDuration;
    public int redPacketCount;
    public int redRatio;
    public int redTotalCash;

    private void init() {
        status = "";
        errorCode = "";
        errorMsg = "";
        activityId = "";
        deadline = "";
        redAllot = 0;
        redGameDuration = 0;
        redPacketCount = 0;
        redRatio = 0;
        redTotalCash = 0;
    }

    public GameInitInfo(JSONObject json) {
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
        if (dataJson.has(ACTIVITY_ID)) {
            activityId = dataJson.getString(ACTIVITY_ID);
        }

        if (dataJson.has(DEADLINE)) {
            deadline = dataJson.getString(DEADLINE);
        }

        if (dataJson.has(GAME_CONFIG)) {
            gameConfig = dataJson.getJSONObject(GAME_CONFIG).toString();
            initGameConfig(dataJson.getJSONObject(GAME_CONFIG));
        }
    }

    private void initGameConfig(JSONObject dataJson) throws JSONException {
        if (dataJson.has(RED_ALLOT)) {
            redAllot = dataJson.getInt(RED_ALLOT);
        }

        if (dataJson.has(RED_GAME_DURATION)) {
            redGameDuration = dataJson.getInt(RED_GAME_DURATION);
        }

        if (dataJson.has(RED_PACKET_COUNT)) {
            redPacketCount = dataJson.getInt(RED_PACKET_COUNT);
        }
        if (dataJson.has(RED_RATIO)) {
            redRatio = dataJson.getInt(RED_RATIO);
        }

        if (dataJson.has(RED_TOTAL_CASH)) {
            redTotalCash = dataJson.getInt(RED_TOTAL_CASH);
        }
    }

}