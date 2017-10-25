/**
 * Copyright © 2013成都理想境界科技有限公司. All rights reserved.
 * 项目名称: Idealsee-AR2
 * 类名称: RandomInfo
 * 类描述:
 * 创建人: ly
 * 创建时间: 2013-12-17 下午6:08:26
 * 修改人:
 * 修改时间:
 * 备注:
 *
 * @version
 */

package com.yixun.sdk.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.yixun.sdk.util.Logger;
import com.yixun.sdk.util.ISARNetUtil;

/**
 * This is the model of ar theme.
 *
 * @author ly
 */
public class ISARRandomInfo implements Serializable {
    private static final long serialVersionUID = -4813636704255743632L;
    private static final String PAGES = "pages";
    private static final String FLOAT_SCREEN = "floating_screen";
    private static final String TOPIC_PICTURE = "topic_picture";
    private static final String IF_COLLECTION = "if_collection"; // if has praised, false is not
    private static final String VIDEO_SHARE_TITLE = "video_share_title"; // video share title, it is
    // array
    private static final String PICTURE_ANGLE = "picture_angle"; // it is a object

    private static final String ROTATE_X = "rotate_x";
    private static final String ROTATE_Y = "rotate_y";
    private static final String ROTATE_Z = "rotate_z";
    private static final String SHOW_RESOURCE_PICTURE = "show_resource_picture";

    private static final String RESOURCE_ID = "resource_id";
    private static final String H5_HAS_BACK_IMAGE = "h5_has_back_image"; // 0 is not have
    private static final String HEIGHT = "height";
    private static final String HTML5_SHARE_TARGET_URL = "h5_share_target_url"; // h5 target url for
    // share
    private static final String LOOK_STATS_SHARE_TITLE = "looker_share_title";
    private static final String LOOK_STATS_SHARE_URL = "looker_stats_share_url";
    private static final String COLLECTION_COUNT = "collection_count";
    private static final String AUTO_PAGING = "auto_paging"; // auto paging, have not used
    private static final String HTML5_SHARE_TITLE = "h5_share_title"; // h5 share title, it is array
    private static final String EDITOR_LOGO = "editor_logo"; // editor author icon
    private static final String WIDTH = "width";
    private static final String PAGING_LOOP = "paging_loop"; // paging loop, have not used
    private static final String HTML5_SHARE_IMG_URL = "h5_share_img_url"; // h5 share image, image
    // that is shared
    private static final String H5_SHARED_SWITCH = "h5_shared_switch"; // check if use h5 share, 1
    // using h5 share, 0 is not
    private static final String IF_PUBLISHED = "if_published"; // check theme is published, true is
    // published, false is not
    private static final String AR_IMAGE_SIZE = "ar_img_size"; // have not used
    private static final String RESOURCE_TAG_TITLE = "resource_tag_title";
    private static final String EDITOR_ID = "editor_id"; // editor id for personal page
    private static final String MD5 = "md5"; // theme md5
    private static final String PICTURE_SHARE_MD5 = "picture_share_img_md5"; // theme share md5
    private static final String PICTURE_SHARE_TITLE = "picture_share_title"; // picture share title,
    // it is array
    private static final String NO_ANIMATION = "no_animation"; // check animation, 1 is no
    // animation, 0 is have
    private static final String H5_HAS_TURNING_TIP = "h5_has_turning_tip"; // have not used
    private static final String RESOURCE_TAG_CONTENT = "resource_tag_content";
    private static final String RESOURCE_TYPE = "resource_type";

    // discover page random info
    private static final String CATE_NAME = "cate_name";
    private static final String COMMENT_COUNT = "comment_count";
    private static final String MEDIA_SRC = "media_src";
    private static final String COMPANY_NAME = "company_name";

    public int collectionCount;
    public int height;
    public int width;
    public int commentCount;
    public int mResourceType; // 0 is html5
    private boolean mIsNoAnimation = false; // 1 is no animation, 0 is animation
    public boolean isShareAsH5 = true;
    public boolean isPraise;
    public String resourceId = "";
    public String cateName = "";
    public String mediaSrc = "";
    public String resourceTagTitle = ""; // name of theme
    public String resourceTagContent = "";
    public String themePicMd5 = "";
    public String sharePicMd5 = "";
    public String companyName = ""; // company name
    public String mEditorId = "";
    public String mEditorLogoUrl = "";
    public String mH5ShareImgUrl = "";
    public String mH5ShareTargetUrl = "";
    public String mLookStatsShareUrl = "";
    public String themePicPath = "";
    public String sharePicPath = "";
    public List<String> mFloatScreenUrl = null;
    public List<String> mH5ShareTitle;
    public List<String> mPictureShareTitle;
    public List<String> mVideoShareTitle;
    public List<String> mLookShareTitle;

    public int mXRotate;
    public int mYRotate;
    public int mZRotate;
    public int mShowResoursePic;

    // for list view loading
    public boolean mHasLoaded;

    public ISARRandomInfo() {
    }

    /**
     * Constructor.
     *
     * @param json json string
     */
    public ISARRandomInfo(JSONObject json) {
        try {
            if (json.has(PAGES)) {
                JSONArray pages = json.getJSONArray(PAGES);
                initPages(pages);
            }

            if (json.has(TOPIC_PICTURE)) {
                JSONObject topic = json.getJSONObject(TOPIC_PICTURE);
                Logger.LOGD("zongRandomInfo  topic:" + topic.toString());
                initTopicPicture(topic);
            } else {
                initTopicPicture(json);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void initPages(JSONArray pages) throws JSONException {
        int size = pages.length();
        this.mFloatScreenUrl = new ArrayList<String>(size);
        for (int i = 0; i < size; i++) {
            JSONObject pageContent = pages.getJSONObject(i);
            if (pageContent.has(FLOAT_SCREEN)) {
                String pageImageMd5 = pageContent.getString(FLOAT_SCREEN);
                mFloatScreenUrl.add(pageImageMd5);
            } else {
                mFloatScreenUrl.add("");
            }
        }
    }

    private void initTopicPicture(JSONObject json) throws JSONException {
        if (json.has(COLLECTION_COUNT)) {
            this.collectionCount = json.getInt(COLLECTION_COUNT);
        }

        if (json.has(RESOURCE_ID)) {
            this.resourceId = json.getString(RESOURCE_ID);
        }

        if (json.has(CATE_NAME)) {
            this.cateName = json.getString(CATE_NAME);
        }

        if (json.has(HEIGHT)) {
            this.height = json.getInt(HEIGHT);
        }

        if (json.has(WIDTH)) {
            this.width = json.getInt(WIDTH);
        }

        if (json.has(COMMENT_COUNT)) {
            this.commentCount = json.getInt(COMMENT_COUNT);
        }

        if (json.has(MD5)) {
            this.themePicMd5 = json.getString(MD5);
        }

        if (json.has(PICTURE_SHARE_MD5)) {
            this.sharePicMd5 = json.getString(PICTURE_SHARE_MD5);
        }

        if (json.has(MEDIA_SRC)) {
            this.mediaSrc = ISARNetUtil.getUrlFromMD5(this.themePicMd5, 300);
        } else {
            // if no media_src, it is animation random info, need combine with qiniu url
            this.mediaSrc = ISARNetUtil.getUrlFromMD5(this.themePicMd5, 200);
        }

        if (json.has(RESOURCE_TAG_TITLE)) {
            this.resourceTagTitle = json.getString(RESOURCE_TAG_TITLE);
        }

        if (json.has(RESOURCE_TAG_CONTENT)) {
            this.resourceTagContent = json.getString(RESOURCE_TAG_CONTENT);
        }

        if (json.has(COMPANY_NAME)) {
            this.companyName = json.getString(COMPANY_NAME);
        }

        if (json.has(IF_COLLECTION)) {
            this.isPraise = json.getInt(IF_COLLECTION) == 1;
        }

        if (json.has(EDITOR_ID)) {
            this.mEditorId = json.getString(EDITOR_ID);
        }

        if (json.has(H5_SHARED_SWITCH)) {
            this.isShareAsH5 = json.getInt(H5_SHARED_SWITCH) == 1;
        }

        if (json.has(EDITOR_LOGO)) {
            this.mEditorLogoUrl = json.getString(EDITOR_LOGO);
            if (null != this.mEditorLogoUrl && !"".equals(this.mEditorLogoUrl)) {
                this.mEditorLogoUrl = ISARNetUtil.getUrlFromMD5(this.mEditorLogoUrl, 200);
            }
        }

        if (json.has(HTML5_SHARE_IMG_URL)) {
            this.mH5ShareImgUrl = json.getString(HTML5_SHARE_IMG_URL);
        }

        if (json.has(HTML5_SHARE_TARGET_URL)) {
            this.mH5ShareTargetUrl = json.getString(HTML5_SHARE_TARGET_URL);
        }

        if (json.has(LOOK_STATS_SHARE_URL)) {
            this.mLookStatsShareUrl = json.getString(LOOK_STATS_SHARE_URL);
        }

        if (json.has(RESOURCE_TYPE)) {
            this.mResourceType = json.getInt(RESOURCE_TYPE);
        }

        if (json.has(HTML5_SHARE_TITLE)) {
            JSONArray stringArray = null;
            try {
                stringArray = json.getJSONArray(HTML5_SHARE_TITLE);
            } catch (JSONException e) {
                Logger.LOGW("json h5 share title error");
            }
            if (stringArray == null) {
                this.mH5ShareTitle = new ArrayList<String>();
                String title = json.getString(HTML5_SHARE_TITLE);
                this.mH5ShareTitle.add(title);
            } else {
                int size = stringArray.length();
                this.mH5ShareTitle = new ArrayList<String>(size);
                for (int i = 0; i < size; i++) {
                    String title = stringArray.getString(i);
                    mH5ShareTitle.add(title);
                }
            }
        }

        if (json.has(PICTURE_SHARE_TITLE)) {
            Logger.LOGD("TestZhang info.share has picture share title");
            JSONArray stringArray = null;
            try {
                stringArray = json.getJSONArray(PICTURE_SHARE_TITLE);
            } catch (JSONException e) {
                Logger.LOGW("json picture share title error");
            }
            if (stringArray == null) {
                this.mPictureShareTitle = new ArrayList<String>();
                String title = json.getString(PICTURE_SHARE_TITLE);
                this.mPictureShareTitle.add(title);
            } else {
                int size = stringArray.length();
                this.mPictureShareTitle = new ArrayList<String>(size);
                for (int i = 0; i < size; i++) {
                    String title = stringArray.getString(i);
                    mPictureShareTitle.add(title);
                }
            }
        }

        if (json.has(VIDEO_SHARE_TITLE)) {
            JSONArray stringArray = null;
            try {
                stringArray = json.getJSONArray(VIDEO_SHARE_TITLE);
            } catch (JSONException e) {
                Logger.LOGW("json video share title error");
            }
            if (stringArray == null) {
                this.mVideoShareTitle = new ArrayList<String>();
                String title = json.getString(VIDEO_SHARE_TITLE);
                this.mVideoShareTitle.add(title);
            } else {
                int size = stringArray.length();
                this.mVideoShareTitle = new ArrayList<String>(size);
                for (int i = 0; i < size; i++) {
                    String title = stringArray.getString(i);
                    mVideoShareTitle.add(title);
                }
            }
        }

        if (json.has(LOOK_STATS_SHARE_TITLE)) {
            JSONArray stringArray = null;
            try {
                stringArray = json.getJSONArray(LOOK_STATS_SHARE_TITLE);
            } catch (JSONException e) {
                Logger.LOGW("json look share title error");
            }
            if (stringArray == null) {
                this.mLookShareTitle = new ArrayList<String>();
                String title = json.getString(LOOK_STATS_SHARE_TITLE);
                this.mLookShareTitle.add(title);
            } else {
                int size = stringArray.length();
                this.mLookShareTitle = new ArrayList<String>(size);
                for (int i = 0; i < size; i++) {
                    String title = stringArray.getString(i);
                    mLookShareTitle.add(title);
                }
            }
        }

        if (json.has(NO_ANIMATION)) {
            this.mIsNoAnimation = (1 == json.getInt(NO_ANIMATION));
            Logger.LOGD("zongRandomInfo  mIsNoAnimation:" + mIsNoAnimation);
        }

        if (json.has(SHOW_RESOURCE_PICTURE)) {
            mShowResoursePic = json.getInt(SHOW_RESOURCE_PICTURE);
            Logger.LOGD("zongRandomInfo  mShowResoursePic:" + mShowResoursePic);
        }
        if (json.has(PICTURE_ANGLE)) {
            JSONObject pictureAngle = json.getJSONObject(PICTURE_ANGLE);
            Logger.LOGD("zongRandomInfo  pictureAngle:" + pictureAngle.toString());
            if (pictureAngle != null) {
                if (pictureAngle.has(ROTATE_X))
                    mXRotate = pictureAngle.getInt(ROTATE_X);
                if (pictureAngle.has(ROTATE_Y))
                    mYRotate = pictureAngle.getInt(ROTATE_Y);
                if (pictureAngle.has(ROTATE_Z))
                    mZRotate = pictureAngle.getInt(ROTATE_Z);
                Logger.LOGD("zongRandomInfo  info:" + this.toString());
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(RESOURCE_ID + "=" + resourceId + "; ");
        builder.append(H5_SHARED_SWITCH + "=" + isShareAsH5 + "; ");
        builder.append(MEDIA_SRC + "=" + mediaSrc + "; ");
        builder.append(ROTATE_X + "=" + mXRotate + "; ");
        builder.append(ROTATE_Y + "=" + mYRotate + "; ");
        builder.append(ROTATE_Z + "=" + mZRotate + "; ");
        builder.append(SHOW_RESOURCE_PICTURE + "=" + mShowResoursePic);
        return builder.toString();
    }

    public int getCollectionCount() {
        return collectionCount;
    }

    public void setCollectionCount(int collectionCount) {
        this.collectionCount = collectionCount;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getCateName() {
        return cateName;
    }

    public void setCateName(String cateName) {
        this.cateName = cateName;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    /**
     * this url is with fixed width and height.
     *
     * @return
     */
    public String getMediaSrc() {
        return mediaSrc;
    }

    public void setMediaSrc(String mediaSrc) {
        this.mediaSrc = mediaSrc;
    }

    public String getResourceTagTitle() {
        return resourceTagTitle;
    }

    public void setResourceTagTitle(String resourceTagTitle) {
        this.resourceTagTitle = resourceTagTitle;
    }

    public String getResourceTagContent() {
        return resourceTagContent;
    }

    public void setResourceTagContent(String resourceTagContent) {
        this.resourceTagContent = resourceTagContent;
    }

    public String getThemePicMd5() {
        return themePicMd5;
    }

    public void setThemePicMd5(String themePicMd5) {
        this.themePicMd5 = themePicMd5;
    }

    public String getSharePicMd5() {
        return sharePicMd5;
    }

    public void setSharePicMd5(String sharePicMd5) {
        this.sharePicMd5 = sharePicMd5;
    }

    public boolean isPraise() {
        return isPraise;
    }

    public void setPraise(boolean isPraise) {
        this.isPraise = isPraise;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getEditorId() {
        return mEditorId;
    }

    public void setmEditorId(String mEditorId) {
        this.mEditorId = mEditorId;
    }

    public String getEditorLogoUrl() {
        return mEditorLogoUrl;
    }

    public void setmEditorLogoUrl(String mEditorLogoUrl) {
        this.mEditorLogoUrl = mEditorLogoUrl;
    }

    public String getmH5ShareImgUrl() {
        return mH5ShareImgUrl;
    }

    public void setmH5ShareImgUrl(String mH5ShareImgUrl) {
        this.mH5ShareImgUrl = mH5ShareImgUrl;
    }

    public String getmH5ShareTargetUrl() {
        return mH5ShareTargetUrl;
    }

    public void setmH5ShareTargetUrl(String mH5ShareTargetUrl) {
        this.mH5ShareTargetUrl = mH5ShareTargetUrl;
    }

    public int getmResourceType() {
        return mResourceType;
    }

    public void setmResourceType(int mResourceType) {
        this.mResourceType = mResourceType;
    }

    public boolean isHasLoaded() {
        return mHasLoaded;
    }

    public void setHasLoaded(boolean mHasLoaded) {
        this.mHasLoaded = mHasLoaded;
    }

    public void setmH5ShareTitle(List<String> mH5ShareTitle) {
        this.mH5ShareTitle = mH5ShareTitle;
    }

    public List<String> getmH5ShareTitle() {
        return mH5ShareTitle;
    }

    public List<String> getmPictureShareTitle() {
        return mPictureShareTitle;
    }

    public void setmPictureShareTitle(List<String> mPictureShareTitle) {
        this.mPictureShareTitle = mPictureShareTitle;
    }

    public List<String> getmVideoShareTitle() {
        return mVideoShareTitle;
    }

    public void setmVideoShareTitle(List<String> mVideoShareTitle) {
        this.mVideoShareTitle = mVideoShareTitle;
    }

    public boolean ismIsNoAnimation() {
        return mIsNoAnimation;
    }

    public void setmIsNoAnimation(boolean mIsNoAnimation) {
        this.mIsNoAnimation = mIsNoAnimation;
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
        if (!(other instanceof ISARRandomInfo)) {
            return false;
        }

        final ISARRandomInfo info = (ISARRandomInfo) other;

        if (!themePicMd5.equals(info.themePicMd5)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = themePicMd5.hashCode();
        result = 29 * result;
        return result;
    }
}
