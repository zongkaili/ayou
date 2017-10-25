package com.yixun.sdk.game;

import com.yixun.ar.unity.ISARUnityMessageManager;
import com.yixun.sdk.server.ISARHttpClient;
import com.yixun.sdk.server.ISARHttpRequest;
import com.yixun.sdk.server.ISARHttpServerURL;
import com.yixun.sdk.util.ISARConstants;
import com.yixun.sdk.util.ISARThreadPool;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by zongkaili on 17-4-26.
 */

public class GameHttpClientHelper {
    private static final String TAG = GameHttpClientHelper.class.getSimpleName();
    private static GameHttpClientHelper mHttpClient = null;

    public interface HttpCallBack {
        void onSuccess();

        void onFailed();
    }

    public interface GetPhoneBindCodeCallBack {
        void onSuccess(GamePhoneBindInfo info);

        void onFailed();
    }

    public interface PostPhoneBindCallBack {
        void onSuccess(GamePhoneBindResponse info);

        void onFailed();
    }

    public interface GetGameStatusInfoCallBack {
        void onSuccess(GameStatusInfo info);

        void onFailed();
    }

    public interface GetGameTimesInfoCallBack {
        void onSuccess(GameTimesInfo info);

        void onFailed();
    }

    public interface GetGameInitCallBack {
        void onSuccess(GameInitInfo info);

        void onFailed(String msg, int code);
    }

    public interface GetGameTicketCallBack {
        void onSuccess(GameTicketInfo info);

        void onFailed();
    }

    public interface PostResouceInfoCallBack {
        void onSuccess(ResouceChangeInfo info);

        void onFailed();
    }

    public synchronized static GameHttpClientHelper getInstance() {
        if (null == mHttpClient)
            mHttpClient = new GameHttpClientHelper();

        return mHttpClient;
    }

    public Map<String, String> getHttpHeaderPrams() {
        Map<String, String> headerParam = new HashMap<>();
        headerParam.put("imei", ISARConstants.APP_IMEI);
        headerParam.put("User-Agent", ISARConstants.USER_AGENT);
        headerParam.put("version", ISARConstants.APP_VERSION_NAME);
        headerParam.put("appKey", ISARConstants.APP_KEY);
        headerParam.put("appid", ISARConstants.APP_ID);
        return headerParam;
    }

    public void doPostDecreaseGameTimes(final String auth, final GameTimesInfo gameTimesInfo, final HttpCallBack postHttpCallBack) {
        ISARThreadPool.INSTANCE.execute(new Runnable() {
            @Override
            public void run() {
                ISARHttpRequest request = new ISARHttpRequest(ISARHttpServerURL.getGameUrl());
                request.setHeaderParams(getHttpHeaderPrams());
                Map<String, String> argsMap = new TreeMap<>();
                argsMap.put("section", GameSignFormatUtil.GAME_POST_DEC_TIMES + "");
                argsMap.put("auth_key", auth);
                argsMap.put("imei", ISARConstants.APP_IMEI);
                argsMap.put("activity", "");
                argsMap.put("yx_token", "");
                argsMap = GameSignFormatUtil.getFormatArgsMap(argsMap);
                request.setBodyParams(argsMap);
                int code = 0;
                try {
                    code = ISARHttpClient.getInstance().postGameInfo(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (code == HttpURLConnection.HTTP_OK) {
                    postHttpCallBack.onSuccess();
                } else {
                    postHttpCallBack.onFailed();
                }
            }
        });
    }

    public void doPostGameResult(final String activityId,
                                 final String md5,
                                 final int score,
                                 final String ticket,
                                 final String user,
                                 final GameTimesInfo gameTimesInfo,
                                 final HttpCallBack postHttpCallBack) {
        ISARThreadPool.INSTANCE.execute(new Runnable() {
            @Override
            public void run() {
                ISARHttpRequest request = new ISARHttpRequest(ISARHttpServerURL.getPostGameResultUrl());
                request.setHeaderParams(getHttpHeaderPrams());
                Map<String, String> argsMap = new TreeMap<>();
                argsMap.put("activity_id", activityId);
                argsMap.put("app_id", ISARConstants.APP_ID);
                argsMap.put("md5", md5);//主题图片md5
                argsMap.put("score", score + "");//游戏结果分数
                argsMap.put("ticket", ticket);//单次游戏标识
                argsMap.put("user", user);//用户标识：当前指代用户手机号
                argsMap = GameSignFormatUtil.getFormatArgsMap(argsMap);
                request.setBodyParams(argsMap);
                int code = 0;
                try {
                    code = ISARHttpClient.getInstance().postGameInfo(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (code == HttpURLConnection.HTTP_OK) {
                    postHttpCallBack.onSuccess();
                } else {
                    postHttpCallBack.onFailed();
                }
            }
        });
    }

    public void doGetPhoneBindCode(final String auth, final String phoneString, final GetPhoneBindCodeCallBack getPhoneBindCodeCallBack) {
        ISARThreadPool.INSTANCE.execute(new Runnable() {
            @Override
            public void run() {
                ISARHttpRequest request = new ISARHttpRequest(ISARHttpServerURL.getGameUrl());
                request.setHeaderParams(getHttpHeaderPrams());
                Map<String, String> argsMap = new TreeMap<>();
                argsMap.put("section", GameSignFormatUtil.GAME_GET_CODE + "");
                argsMap.put("auth_key", auth);
                argsMap.put("imei", ISARConstants.APP_IMEI);
                argsMap.put("tel", phoneString);
                argsMap = GameSignFormatUtil.getFormatArgsMap(argsMap);
                request.setUrlParams(argsMap);
                GamePhoneBindInfo info = null;
                try {
                    info = ISARHttpClient.getInstance().getGamePhoneBindInfo(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (null != info && info.utcTimeTmp > 0) {
                    getPhoneBindCodeCallBack.onSuccess(info);
                } else {
                    getPhoneBindCodeCallBack.onFailed();
                }
            }
        });
    }

    public void doPostPhoneBind(final String auth, final String phoneNum, final String code, final PostPhoneBindCallBack postPhoneBindCallBack) {
        ISARThreadPool.INSTANCE.execute(new Runnable() {
            @Override
            public void run() {
                ISARHttpRequest request = new ISARHttpRequest(ISARHttpServerURL.getGameUrl());
                request.setHeaderParams(getHttpHeaderPrams());
                Map<String, String> argsMap = new TreeMap<>();
                argsMap.put("section", GameSignFormatUtil.GAME_POST_BIND + "");
                argsMap.put("auth_key", auth);
                argsMap.put("imei", ISARConstants.APP_IMEI);
                argsMap.put("sc", code);
                argsMap.put("tel", phoneNum);
                argsMap = GameSignFormatUtil.getFormatArgsMap(argsMap);
                request.setBodyParams(argsMap);
                GamePhoneBindResponse gamePhoneBindResponse = null;
                try {
                    ISARHttpClient.getInstance().postGamePhoneBind(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (null != gamePhoneBindResponse) {
                    postPhoneBindCallBack.onSuccess(gamePhoneBindResponse);
                } else {
                    postPhoneBindCallBack.onFailed();
                }
            }
        });
    }

    public void doPostIncreaseGameTimes(final String auth, final GameTimesInfo gameTimesInfo, final HttpCallBack postHttpCallBack) {
        ISARThreadPool.INSTANCE.execute(new Runnable() {
            @Override
            public void run() {
                ISARHttpRequest request = new ISARHttpRequest(ISARHttpServerURL.getGameUrl());
                request.setHeaderParams(getHttpHeaderPrams());
                Map<String, String> argsMap = new TreeMap<>();
                argsMap.put("section", GameSignFormatUtil.GAME_POST_ADD_TIMES + "");
                argsMap.put("auth_key", auth);
                argsMap.put("imei", ISARConstants.APP_IMEI);
                argsMap.put("activity", "");
                argsMap.put("yx_token", "");
                argsMap = GameSignFormatUtil.getFormatArgsMap(argsMap);
                request.setBodyParams(argsMap);
                int code = 0;
                try {
                    code = ISARHttpClient.getInstance().postGameInfo(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (code == HttpURLConnection.HTTP_OK) {
                    postHttpCallBack.onSuccess();
                }
            }
        });
    }

    public void doPostCountShareTimes(final String auth, final GameTimesInfo gameTimesInfo, final HttpCallBack postHttpCallBack) {
        ISARThreadPool.INSTANCE.execute(new Runnable() {
            @Override
            public void run() {
                ISARHttpRequest request = new ISARHttpRequest(ISARHttpServerURL.getGameUrl());
                request.setHeaderParams(getHttpHeaderPrams());
                Map<String, String> argsMap = new TreeMap<>();
                argsMap.put("section", GameSignFormatUtil.GAME_POST_COUNT_TIMES + "");
                argsMap.put("auth_key", auth);
                argsMap.put("imei", ISARConstants.APP_IMEI);
                argsMap.put("activity", "");
                argsMap.put("yx_token", "");
                argsMap = GameSignFormatUtil.getFormatArgsMap(argsMap);
                request.setBodyParams(argsMap);
                int code = 0;
                try {
                    ISARHttpClient.getInstance().postGameInfo(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (code != HttpURLConnection.HTTP_OK) {
                    postHttpCallBack.onSuccess();
                }
            }
        });
    }

    public void checkGameStatus(final String auth, final GetGameStatusInfoCallBack callBack) {
        ISARThreadPool.INSTANCE.execute(new Runnable() {
            @Override
            public void run() {
                ISARHttpRequest request = new ISARHttpRequest(ISARHttpServerURL.getGameUrl());
                request.setHeaderParams(getHttpHeaderPrams());
                Map<String, String> urlParams = new TreeMap<>();
                urlParams.put("section", GameSignFormatUtil.GAME_GET_STATUS + "");
                urlParams.put("auth_key", auth);
                urlParams.put("imei", ISARConstants.APP_IMEI);
                urlParams = GameSignFormatUtil.getFormatArgsMap(urlParams);
                request.setUrlParams(urlParams);
                GameStatusInfo info = null;
                try {
                    info = ISARHttpClient.getInstance().getGameStatus(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (null != info) {
                    callBack.onSuccess(info);
                } else {
                    callBack.onFailed();
                }
            }
        });
    }

    public void doGetCheckGameTimes(final String activityId, final String user, final GetGameTimesInfoCallBack callBack) {
        ISARThreadPool.INSTANCE.execute(new Runnable() {
            @Override
            public void run() {
                ISARHttpRequest request = new ISARHttpRequest(ISARHttpServerURL.getGameTimesUrl());
                request.setHeaderParams(getHttpHeaderPrams());
                Map<String, String> urlParams = new TreeMap<>();
                urlParams.put("activity_id", activityId);
                urlParams.put("app_id", ISARConstants.APP_ID);
                urlParams.put("user", user);
                urlParams = GameSignFormatUtil.getFormatArgsMap(urlParams);
                request.setUrlParams(urlParams);
                GameTimesInfo gameTimesInfo = null;
                try {
                    gameTimesInfo = ISARHttpClient.getInstance().getGameTimes(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (null != gameTimesInfo && gameTimesInfo.status == "success") {
                    callBack.onSuccess(gameTimesInfo);
                } else {
                    callBack.onFailed();
                }
            }
        });
    }

    public void initRedPacketGame(final GetGameInitCallBack callBack) {
        ISARThreadPool.INSTANCE.execute(new Runnable() {
            @Override
            public void run() {
                GameInitInfo gameInitInfo = null;
                ISARHttpRequest request = new ISARHttpRequest(ISARHttpServerURL.getGameInitUrl());
                request.setHeaderParams(getHttpHeaderPrams());
                Map<String, String> urlParams = new HashMap<>(5);
                urlParams.put("app_id", ISARConstants.APP_ID);
                request.setUrlParams(urlParams);
                try {
                    gameInitInfo = ISARHttpClient.getInstance().getGameInitInfo(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (gameInitInfo != null) {
                    if (gameInitInfo.status.equals("success")) {
                        ISARUnityMessageManager.setAllowMovePage(false);
                        if (gameInitInfo != null)
                            ISARUnityMessageManager.prepareGameData(gameInitInfo.gameConfig);

                        callBack.onSuccess(gameInitInfo);
                    } else {
                        callBack.onFailed(gameInitInfo.errorMsg, Integer.valueOf(gameInitInfo.errorCode));
                    }
                } else {
                    callBack.onFailed("game init failed!", 0);
                }
            }
        });
    }

    /**
     * 获取单次游戏唯一标识
     *
     * @param activityId 　活动id
     * @param md5
     * @param user       　用户绑定电话号码
     * @param callBack   获取数据成功与否的回调
     */
    public void getGameTickerInfo(final String activityId, final String md5, final String user, final GetGameTicketCallBack callBack) {
        ISARThreadPool.INSTANCE.execute(new Runnable() {
            @Override
            public void run() {
                GameTicketInfo gameTicketInfo = null;
                ISARHttpRequest request = new ISARHttpRequest(ISARHttpServerURL.getGameTicketUrl());
                request.setHeaderParams(getHttpHeaderPrams());
                Map<String, String> urlParams = new HashMap<>(5);
                urlParams.put("activity_id", activityId);
                urlParams.put("app_id", ISARConstants.APP_ID);
                urlParams.put("md5", md5);
                urlParams.put("user", user);
                request.setUrlParams(urlParams);
                try {
                    gameTicketInfo = ISARHttpClient.getInstance().getGameTicketInfo(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (gameTicketInfo != null && gameTicketInfo.status.equals("success")) {
                    callBack.onSuccess(gameTicketInfo);
                } else {
                    callBack.onFailed();
                }
            }
        });
    }

    public void doPostCheckResouceInfo(final String md5s, final String op_datetime, final PostResouceInfoCallBack callBack) {
        ISARThreadPool.INSTANCE.execute(new Runnable() {
            @Override
            public void run() {
                ResouceChangeInfo resouceChangeInfo = null;
                ISARHttpRequest request = new ISARHttpRequest(ISARHttpServerURL.getCheckResouceInfoUrl());
                request.setHeaderParams(getHttpHeaderPrams());
                Map<String, String> argsParams = new HashMap<>(1);
                argsParams.put("ctype", "android");
                argsParams.put("md5s", md5s);//TODO
                argsParams.put("op_datetime", op_datetime);
                request.setBodyParams(argsParams);
                try {
                    resouceChangeInfo = ISARHttpClient.getInstance().postResouceInfo(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (resouceChangeInfo == null) {
                    callBack.onFailed();
                } else {
                    callBack.onSuccess(resouceChangeInfo);
                }
            }
        });
    }

    public void doUploadUserLog(final HttpCallBack callBack) {
        ISARThreadPool.INSTANCE.execute(new Runnable() {

            @Override
            public void run() {
                ISARHttpRequest request = new ISARHttpRequest(ISARHttpServerURL.getUserBehaviorLogUrl());
                request.setHeaderParams(getHttpHeaderPrams());
                Map<String, String> argsParams = new TreeMap<>();
                argsParams.put("ctype", "android");
                Map<String, String> fileParams = new TreeMap<>();
                fileParams.put("package", ISARConstants.APP_BEHAVIOR_LOG_FILE);
                request.setBodyParams(argsParams);
                request.setFileParams(fileParams);
                int code = 0;
                try {
                    code = ISARHttpClient.getInstance().postUploadUserLog(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (code == HttpURLConnection.HTTP_OK) {
                    callBack.onSuccess();
                } else {
                    callBack.onFailed();
                }
            }
        });
    }
}
