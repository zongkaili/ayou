package com.idealsee.sdk.game;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yaolei on 17-3-24.
 */

public class ARPackages {
    private static final String STATUS = "status";
    private static final String J_DATA = "jdate";


    private String status;
    private List<ARPackageInfo> list;


    public ARPackages(JSONObject json) {
        init();
        try {
            if (json.has(STATUS)) {
                status = json.getString(STATUS);
            }
            if (json.has(J_DATA)) {
                JSONArray array = json.getJSONArray(J_DATA);
                int len = array.length();
                for (int i = 0; i < len; i++) {
                    JSONObject childJson = array.getJSONObject(i);
                    ARPackageInfo info = new ARPackageInfo(childJson);
                    list.add(info);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        status = "";
        list = new ArrayList<>();
    }

    public String getStatus() {
        return status;
    }

    public List<ARPackageInfo> getList() {
        return list;
    }

}
