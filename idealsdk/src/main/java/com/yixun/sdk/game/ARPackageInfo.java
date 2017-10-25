package com.yixun.sdk.game;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yaolei on 17-3-24.
 */

public class ARPackageInfo {
    private static final String SIZE = "size";
    private static final String D_URL = "durl";
    private static final String ID = "id";
    private static final String U_TIME = "utime";

    private String size;
    private String url;
    private String fileSuffix;
    private String id;
    private String time;

    public ARPackageInfo(JSONObject json) {
        init();
        try {
            if (json.has(SIZE)) {
                size = json.getString(SIZE);
            }
            if (json.has(D_URL)) {
                url = json.getString(D_URL);
                fileSuffix = url.substring(url.lastIndexOf("."));
            }
            if (json.has(ID)) {
                id = json.getString(ID);
            }
            if (json.has(U_TIME)) {
                time = json.getString(U_TIME);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        size = "";
        url = "";
        id = "";
        time = "";
    }

    public String getSize() {
        return size;
    }

    public String getUrl() {
        return url;
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public String getId() {
        return id;
    }

    public String getTime() {
        return time;
    }
}
