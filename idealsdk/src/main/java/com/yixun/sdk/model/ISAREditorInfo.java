package com.yixun.sdk.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class ISAREditorInfo implements Serializable {

    private static final long serialVersionUID = 1000494849662055970L;

    private static final String EDITOR_ID = "editor_id"; // editor id
    private static final String EDITOR_EMAIL = "editor_mail";
    private static final String EDITOR_NAME = "editor_name";
    private static final String EDITOR_LOGO = "editor_logo";
    private static final String ADDRESS = "address";
    private static final String PHONE = "phone";
    private static final String VIP = "vip";
    private static final String BRAND = "brand";
    private static final String BRAND_PROFILE = "brand_profile";

    public String id;
    public String email;
    public String name;
    public String logo;
    public String address;
    public String phone;
    public String vip;
    public String brand;
    public String brand_profile;

    public ISAREditorInfo(JSONObject json) {
        init();
        try {
            if (json.has(EDITOR_ID)) {
                id = json.getString(EDITOR_ID);
            }

            if (json.has(EDITOR_EMAIL)) {
                email = json.getString(EDITOR_EMAIL);
            }

            if (json.has(EDITOR_NAME)) {
                name = json.getString(EDITOR_NAME);
            }

            if (json.has(EDITOR_LOGO)) {
                logo = json.getString(EDITOR_LOGO);
            }

            if (json.has(ADDRESS)) {
                address = json.getString(ADDRESS);
            }

            if (json.has(PHONE)) {
                phone = json.getString(PHONE);
            }
            
            if(json.has(VIP)){
                vip = json.getString(VIP);
            }

            if (json.has(BRAND)) {
                brand = json.getString(BRAND);
            }

            if (json.has(BRAND_PROFILE)) {
                brand_profile = json.getString(BRAND_PROFILE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void init() {
        id = "";
        email = "";
        name = "";
        logo = "";
        address = "";
        phone = "";
        vip = "";
        brand = "";
        brand_profile = "";
    }

}
