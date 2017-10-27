package com.idealsee.sdk.model;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.idealsee.sdk.util.ISARNetUtil;

public class ISARPersonalInfo implements Serializable {

    private static final long serialVersionUID = -3834752442959971759L;

    private static final String EDITOR_ID = "editor_id"; // editor id
    private static final String ADDRESS = "address";
    private static final String PHONE = "phone";
    private static final String BRAND = "brand"; //
    private static final String BRAND_PROFILE = "brand_profile";
    private static final String EDITOR_LOGO = "editor_logo";
    private static final String RESOURCE_NUM = "resource_num";
    private static final String LATITUDE = "latitude"; //维度
    private static final String LONGITUDE = "longitude"; //经度

    private String mEditorId;
    private String mAddress;
    private String mPhone;
    private String mBrand;
    private String mBrandProfile;
    private String mEditorLogo;
    private String mResourceNum;
    private double mLatitude;
    private double mLongitude;

    /**
     * Constructor.
     * 
     * @param json
     *            json string
     */
    public ISARPersonalInfo(JSONObject json) {
        try {
            if (json.has(EDITOR_ID)) {
                mEditorId = json.getString(EDITOR_ID);
            }
            if (json.has(ADDRESS)) {
                mAddress = json.getString(ADDRESS);
            }
            if (json.has(PHONE)) {
                mPhone = json.getString(PHONE);
            }
            if (json.has(BRAND)) {
                mBrand = json.getString(BRAND);
            }
            if (json.has(BRAND_PROFILE)) {
                mBrandProfile = json.getString(BRAND_PROFILE);
            }
            if (json.has(EDITOR_LOGO)) {
                mEditorLogo = json.getString(EDITOR_LOGO);
                if (null != this.mEditorLogo && !"".equals(this.mEditorLogo)) {
                    this.mEditorLogo = ISARNetUtil.getUrlFromMD5(this.mEditorLogo, 200);
                }
            }
            if (json.has(RESOURCE_NUM)) {
                mResourceNum = json.getString(RESOURCE_NUM);
            }
            if (json.has(LONGITUDE)) {
                mLongitude = json.getDouble(LONGITUDE);
            }
            if(json.has(LATITUDE)) {
                mLatitude = json.getDouble(LATITUDE);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public String getmEditorId() {
        return mEditorId;
    }

    public void setmEditorId(String mEditorId) {
        this.mEditorId = mEditorId;
    }

    public String getmAddress() {
        return mAddress;
    }

    public void setmAddress(String mAddress) {
        this.mAddress = mAddress;
    }

    public String getmPhone() {
        return mPhone;
    }

    public void setmPhone(String mPhone) {
        this.mPhone = mPhone;
    }

    public String getmBrand() {
        return mBrand;
    }

    public void setmBrand(String mBrand) {
        this.mBrand = mBrand;
    }

    public String getmBrandProfile() {
        return mBrandProfile;
    }

    public void setmBrandProfile(String mBrandProfile) {
        this.mBrandProfile = mBrandProfile;
    }

    public String getmEditorLogo() {
        return mEditorLogo;
    }

    public void setmEditorLogo(String mEditorLogo) {
        this.mEditorLogo = mEditorLogo;
    }

    public String getmResourceNum() {
        return mResourceNum;
    }

    public void setmResourceNum(String mResourceNum) {
        this.mResourceNum = mResourceNum;
    }

    /**
     * @return the mLatitude
     */
    public double getmLatitude() {
        return mLatitude;
    }

    /**
     * @return the mLongitude
     */
    public double getmLongitude() {
        return mLongitude;
    }

    /**
     * @param mLatitude the mLatitude to set
     */
    public void setmLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
    }

    /**
     * @param mLongitude the mLongitude to set
     */
    public void setmLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    @Override
    public boolean equals(Object other) {
        // 先检查是否其自反性，后比较other是否为空。这样效率高
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!(other instanceof ISARPersonalInfo)) {
            return false;
        }

        final ISARPersonalInfo info = (ISARPersonalInfo) other;

        if (!mEditorId.equals(info.mEditorId)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = mEditorId.hashCode();
        result = 29 * result;
        return result;
    }

    /**
     * format string.
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("mEditorId=" + mEditorId + ",");
        sb.append("mAddress=" + mAddress + ",");
        sb.append("mPhone=" + mPhone + ",");
        sb.append("mBrand=" + mBrand + ",");
        sb.append("mBrandProfile=" + mBrandProfile + ",");
        sb.append("mEditorLogo=" + mEditorLogo + ",");
        sb.append("mResourceNum=" + mResourceNum + ",");
        return sb.toString();
    }

}
