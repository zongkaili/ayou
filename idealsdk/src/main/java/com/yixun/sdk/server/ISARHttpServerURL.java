package com.yixun.sdk.server;

import java.util.HashMap;

/**
 * Created by hongen on 16-12-8.
 * <p>
 * 设置网络访问的url.
 */

public class ISARHttpServerURL {
    private static final int RELEASE_LEVEL = 0; // for release
    private static final int DEBUG_INNER_LEVEL = 1; // for inner test
    private static final int DEBUG_OUTER_LEVEL = 2; // for outer test
    // 设置网络访问级别
    private static final int SERVER_URL_LEVEL = RELEASE_LEVEL;

    private static final String BASE_URL_FOR_UNITY_RELEASE = "https://yxfile.idealsee.com";
    private static final String BASE_URL_OUTER = "https://oltest.yixun.idealsee.com"; // 外网测试
    private static final String BASE_URL_INNER = "https://10.0.1.33"; // 内网测试
    private static final String BASE_URL_RELEASE = "https://yixun.arhieason.com"; // release
    private static final String BASE_URL_CTCC = "http://118.194.63.239";
    private static final String BASE_URL_CMCC = "http://119.97.156.131";
    private static final String BASE_URL_CUCC = "http://182.118.12.243";
    public static String CDN_RESOURCE = "https://yixunfiles-ali.yixun.arhieason.com/MD5.FORMAT";
    public static String CDN_PIC = "https://yixunfiles-ali.yixun.arhieason.com/MD5.FORMAT?x-oss-process=image/resize,w_SIZE,limit_1,format,FORMAT";
    public static String CDN_PIC_FULL = "https://yixunfiles-ali.yixun.arhieason.com/MD5.FORMAT?x-oss-process=image/format,FORMAT";
    public static String CDN_RES_CROP = "https://yixunfiles-ali.yixun.arhieason.com/MD5.FORMAT?x-oss-process=image/crop,x_IX,y_IY,w_IW,h_IH,format,png";
    private static final String QINIU_URL_OUTER = "http://o6zq3p22t.bkt.clouddn.com"; // 外网测试
    private static final String QINIU_URL_INNER = "http://7xtnpe.com2.z0.glb.qiniucdn.com"; // 内网测试

    private static final String QINIU_URL_RELEASE = "http://7xsom5.com2.z0.glb.qiniucdn.com"; // release

    // http url

    private static final String SUB_URL_GET_ANIMATION = "sub_url_get_animation";
    private static final String SUB_URL_GET_THEME_VIDEOURL = "sub_url_get_theme_videourl";
    private static final String SUB_URL_GET_UPDATE_INFO = "sub_url_get_update_info";
    private static final String SUB_URL_POST_BEHAVIOR_LOG = "sub_url_post_behavior_log";
    private static final String SUB_URL_POST_BEHAVIOR_ERROR = "sub_url_post_behavior_error";
    private static final String SUB_URL_POST_RESOURCE_INIT = "sub_url_post_resource_init";
    private static final String SUB_URL_GET_SERACH_RESULT = "sub_url_get_search_result";
    private static final String SUB_URL_POST_PRAISE_TOPIC = "sub_url_post_praise_topic";
    private static final String SUB_URL_POST_FEED_BACK = "sub_url_post_feed_back";
    private static final String SUB_URL_POST_COMMENT = "sub_url_post_comment";
    private static final String SUB_URL_GET_COMMENT = "sub_url_get_comment";
    private static final String SUB_URL_GET_SERVER_MSG = "sub_url_get_server_msg";
    private static final String SUB_URL_POST_USER_LOGIN = "sub_url_post_urer_login";
    private static final String SUB_URL_GET_USER_LOGOUT = "sub_url_get_user_logout";
    private static final String SUB_URL_POST_CHECK_KEY = "sub_url_post_check_key";
    private static final String SUB_URL_GET_INIT = "sub_url_get_init";
    private static final String SUB_URL_POST_ACTIVITY_DATA = "sub_url_post_activity_data";
    private static final String SUB_URL_GET_AR_H5_URL = "sub_url_ar_h5_url";
    private static final String SUB_URL_GET_RANDOM_DATA = "sub_url_get_random_data";
    private static final String SUB_URL_GET_PERSONAL_INFO = "sub_url_get_personal_info";

    private static final String SUB_URL_GET_GAME_INIT = "sub_url_get_gold_init";
    private static final String SUB_URL_GOLD_GAME = "sub_url_gold_game";
    private static final String SUB_URL_GET_MY_GOLD_INFO = "sub_url_get_my_gold_info";
    private static final String SUB_URL_GET_CHECK_RESOUCE_INFO = "sub_url_get_check_resouce_info";
    private static final String SUB_URL_GET_GAME_TIMES = "sub_url_get_game_times";
    private static final String SUB_URL_GET_GAME_TICKET = "sub_url_get_game_ticket";
    private static final String SUB_URL_POST_GAME_RESULT = "sub_url_post_game_result";

    private static String sHttpUrl = BASE_URL_RELEASE;
    private static String sHttpQiNiuUrl = QINIU_URL_RELEASE;

    private static HashMap<String, String> sUrlMap = new HashMap<>();

    static {
        switch (SERVER_URL_LEVEL) {
            case RELEASE_LEVEL:
                sHttpUrl = BASE_URL_RELEASE;
                sHttpQiNiuUrl = QINIU_URL_RELEASE;
                break;
            case DEBUG_INNER_LEVEL:
                sHttpUrl = BASE_URL_INNER;
                sHttpQiNiuUrl = QINIU_URL_INNER;
                break;
            case DEBUG_OUTER_LEVEL:
                sHttpUrl = BASE_URL_OUTER;
                sHttpQiNiuUrl = QINIU_URL_OUTER;
                break;
            default:
                sHttpUrl = BASE_URL_RELEASE;
                sHttpQiNiuUrl = QINIU_URL_RELEASE;
                break;
        }
    }

    static {
        sUrlMap.put(SUB_URL_GET_ANIMATION, "/api/resource/animation_v4");
        sUrlMap.put(SUB_URL_GET_THEME_VIDEOURL, "/api/video_url/resolve");
        sUrlMap.put(SUB_URL_GET_UPDATE_INFO, "/api/app/version");
        sUrlMap.put(SUB_URL_POST_RESOURCE_INIT, "/api/resource/init");
        sUrlMap.put(SUB_URL_POST_BEHAVIOR_ERROR, "/behavior/error");
        sUrlMap.put(SUB_URL_GET_SERACH_RESULT, "/api/resource/search");
        sUrlMap.put(SUB_URL_POST_PRAISE_TOPIC, "/api/resource/collection/");
        sUrlMap.put(SUB_URL_POST_FEED_BACK, "/api/feedback");
        sUrlMap.put(SUB_URL_POST_COMMENT, "/api/resource/comment");
        sUrlMap.put(SUB_URL_GET_COMMENT, "/api/resource/comment");
        sUrlMap.put(SUB_URL_GET_SERVER_MSG, "/api/msg");
        sUrlMap.put(SUB_URL_POST_USER_LOGIN, "/api/editor/login");
        sUrlMap.put(SUB_URL_GET_USER_LOGOUT, "/api/editor/logout");
        sUrlMap.put(SUB_URL_POST_CHECK_KEY, "/api/app/appkey/check");
        sUrlMap.put(SUB_URL_GET_INIT, "/api/app/init2");
        sUrlMap.put(SUB_URL_POST_ACTIVITY_DATA, "/api/mobile/activity/push/data");
        sUrlMap.put(SUB_URL_GET_AR_H5_URL, "/api/resource/recognize");
        sUrlMap.put(SUB_URL_GET_RANDOM_DATA, "/api/resource/random");
        sUrlMap.put(SUB_URL_GET_PERSONAL_INFO, "/api/editor/info");
        sUrlMap.put(SUB_URL_GOLD_GAME, "/api/demoGold/gameProcedure");
        sUrlMap.put(SUB_URL_GET_MY_GOLD_INFO, "/api/demoGold/gameStat");
        sUrlMap.put(SUB_URL_GET_CHECK_RESOUCE_INFO, "/web/sdk/resource");
        sUrlMap.put(SUB_URL_POST_BEHAVIOR_LOG, "/web/sdk/logs");
        sUrlMap.put(SUB_URL_GET_GAME_INIT, "/api/demo/game/init");
        sUrlMap.put(SUB_URL_GET_GAME_TIMES, "/api/demo/game/user/times");
        sUrlMap.put(SUB_URL_GET_GAME_TICKET, "/api/demo/game/ticket");
        sUrlMap.put(SUB_URL_POST_GAME_RESULT, "/api/demo/game/result");
    }

    public static String getHttpUrl() {
        return sHttpUrl;
    }

    public static String getHttpQiNiuUrl() {
        return sHttpQiNiuUrl;
    }

    public static String getUnityFileUrl() {
        return BASE_URL_FOR_UNITY_RELEASE;
    }

    /**
     * get single search url for normal state.
     *
     * @return the url of search
     */
    public static String getSingleSearchUrl() {
        String url = sHttpUrl + sUrlMap.get(SUB_URL_GET_SERACH_RESULT);
        return url;
    }

    /**
     * 获取CTCC的服务器匹配url.
     *
     * @return
     */
    public static String getCTCCSingleSearchUrl() {
        String url = BASE_URL_CTCC + sUrlMap.get(SUB_URL_GET_SERACH_RESULT);
        return url;
    }

    /**
     * 获取CMCC的服务器匹配url.
     *
     * @return
     */
    public static String getCMCCSingleSearchUrl() {
        String url = BASE_URL_CMCC + sUrlMap.get(SUB_URL_GET_SERACH_RESULT);
        return url;
    }

    /**
     * 获取CUCC的服务器匹配url.
     *
     * @return
     */
    public static String getCUCCSingleSearchUrl() {
        String url = BASE_URL_CUCC + sUrlMap.get(SUB_URL_GET_SERACH_RESULT);
        return url;
    }

    /**
     * 得到下载Animation时的url.
     *
     * @return
     */
    public static String getAnimationUrl() {
        String url = sHttpUrl + sUrlMap.get(SUB_URL_GET_ANIMATION);
        return url;
    }

    /**
     * 得到视频的真实地址.
     *
     * @return
     */
    public static String getThemeVideoUrl() {
        String url = sHttpUrl + sUrlMap.get(SUB_URL_GET_THEME_VIDEOURL);
        return url;
    }

    /**
     * 获取相似主题的h5链接.
     *
     * @return
     */
    public static String getSimilarThemeUrl() {
        String url = sHttpUrl + sUrlMap.get(SUB_URL_GET_AR_H5_URL);
        return url;
    }

    /**
     * 得到key校验的地址.
     *
     * @return
     */
    public static String getCheckAppKeyUrl() {
        String url = sHttpUrl + sUrlMap.get(SUB_URL_POST_CHECK_KEY);
        return url;
    }

    public static String getAppInitUrl() {
        return sHttpUrl + sUrlMap.get(SUB_URL_GET_INIT);
    }

    /**
     * 得到获取发现列表的url.
     *
     * @return
     */
    public static String getRandomInfoListUrl() {
        String url = sHttpUrl + sUrlMap.get(SUB_URL_GET_RANDOM_DATA);
        return url;
    }

    /**
     * 得到获取个人信息列表的url.
     *
     * @return
     */
    public static String getPersonalInfoListUrl() {
        String url = sHttpUrl + sUrlMap.get(SUB_URL_GET_PERSONAL_INFO);
        return url;
    }


    public static String getGameInitUrl() {
        String url = sHttpUrl + sUrlMap.get(SUB_URL_GET_GAME_INIT);
        return url;
    }

    public static String getGameUrl() {
        String url = sHttpUrl + sUrlMap.get(SUB_URL_GOLD_GAME);
        return url;
    }

    public static String getGameTimesUrl() {
        String url = sHttpUrl + sUrlMap.get(SUB_URL_GET_GAME_TIMES);
        return url;
    }

    public static String getGameTicketUrl() {
        String url = sHttpUrl + sUrlMap.get(SUB_URL_GET_GAME_TICKET);
        return url;
    }

    public static String getPostGameResultUrl() {
        String url = sHttpUrl + sUrlMap.get(SUB_URL_POST_GAME_RESULT);
        return url;
    }

    public static String getGoldInfoUrl() {
        String url = sHttpUrl + sUrlMap.get(SUB_URL_GET_MY_GOLD_INFO);
        return url;
    }

    public static String getCheckResouceInfoUrl() {
        String url = sHttpUrl + sUrlMap.get(SUB_URL_GET_CHECK_RESOUCE_INFO);
        return url;
    }

    public static String getUserBehaviorLogUrl() {
        String url = sHttpUrl + sUrlMap.get(SUB_URL_POST_BEHAVIOR_LOG);
        return url;
    }
}
