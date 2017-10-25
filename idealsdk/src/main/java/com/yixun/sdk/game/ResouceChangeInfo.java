package com.yixun.sdk.game;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 检查资源更新时，返回的数据信息
 * Created by zongkaili on 17-5-2.
 */

public class ResouceChangeInfo implements Serializable{
    private static final String JDATA = "jdata";
    private static final String ADD_MD5S = "add_md5s";
    private static final String DELETE_MD5S = "delete_md5s";
    private static final String UPDATE_MD5S = "update_md5s";
    private static final String OP_DATETIME = "op_datetime";
    private static final String DAT_URL = "dat_url";
    private static final String PACKAGE_URL = "package_url";
    private static final String UNITY3D = "unity3d";
    private static final String UNITY3D_URL = "unity3d_url";

    public List<String> addMd5s;
    public List<String> deleteMd5s;
    public List<String> updateMd5s;
    public List<String> unity3d;//unity资源
    public String opDateTime;
    public String datUrl;
    public String unity3dUrl;
    public String packageUrl;

    private void init() {
        addMd5s = new ArrayList<>();
        deleteMd5s = new ArrayList<>();
        updateMd5s = new ArrayList<>();
        unity3d = new ArrayList<>();
        opDateTime = "";
        datUrl = "";
        unity3dUrl = "";
        packageUrl = "";
    }

    public ResouceChangeInfo(JSONObject json) {
        init();
        try {
            if(!json.has(JDATA))
                return;
            json = json.getJSONObject(JDATA);
            if (json.has(ADD_MD5S)) {
                JSONArray array = json.getJSONArray(ADD_MD5S);
                for (int i = 0; i < array.length(); i++) {
                    String childJson = array.getString(i);
                    addMd5s.add(childJson);
                }
            }
            if (json.has(DELETE_MD5S)) {
                JSONArray array = json.getJSONArray(DELETE_MD5S);
                for (int i = 0; i < array.length(); i++) {
                    String childJson = array.getString(i);
                    deleteMd5s.add(childJson);
                }
            }
            if (json.has(UPDATE_MD5S)) {
                JSONArray array = json.getJSONArray(UPDATE_MD5S);
                for (int i = 0; i < array.length(); i++) {
                    String childJson = array.getString(i);
                    updateMd5s.add(childJson);
                }
            }
            if (json.has(UNITY3D)) {
                JSONArray array = json.getJSONArray(UNITY3D);
                for (int i = 0; i < array.length(); i++) {
                    String childJson = array.getString(i);
                    unity3d.add(childJson);
                }
            }
            if (json.has(OP_DATETIME)) {
                opDateTime = json.getString(OP_DATETIME);
            }
            if (json.has(DAT_URL)) {
                datUrl = json.getString(DAT_URL);
            }
            if (json.has(UNITY3D_URL)) {
                unity3dUrl = json.getString(UNITY3D_URL);
            }
            if (json.has(PACKAGE_URL)) {
                packageUrl = json.getString(PACKAGE_URL);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
