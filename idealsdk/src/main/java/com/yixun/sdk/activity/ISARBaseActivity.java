/**
 * 项目名称: IDSeeAR
 * 类名称:  IDSeeARBaseActivity
 * 类描述:
 * 创建人: Ly
 * 创建时间: 2013-1-23 上午10:06:33
 * 修改人:
 * 修改时间:
 * 备注:
 *
 * @version
 */
package com.yixun.sdk.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;

import com.yixun.ar.unity.ISARCamera;
import com.yixun.sdk.server.ISARHttpClient;
import com.yixun.sdk.util.ISARBitmapLoader;
import com.yixun.sdk.util.ISARConstants;
import com.yixun.sdk.util.ISARDeviceUtil;
import com.yixun.sdk.util.Logger;

public class ISARBaseActivity extends Activity {
    private static final String TAG = ISARBaseActivity.class.getSimpleName();

    protected Context mContext;
    protected String mImei;
    protected String mPackageName;
    protected String mAppKey;
    protected String mAppId;
    protected SharedPreferences mSp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ISARCamera.getInstance().arcameraInitCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
        ISARCamera.getInstance().arcameraStart();
        mContext = getApplicationContext();
        mSp = getSharedPreferences(ISARConstants.APP_SHARED_PREFERENCE, MODE_PRIVATE);
        ISARConstants.APP_PARENT_PATH = mContext.getExternalFilesDir(null).getAbsolutePath();
        initVersionInfo();
        ISARHttpClient.getInstance().setCachePath(mContext);
        ISARBitmapLoader.getInstance().initCache(mContext);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initVersionInfo() {
        PackageManager pm = mContext.getPackageManager();
        PackageInfo pi;
        try {
            mImei = ISARDeviceUtil.getDeviceId(mContext);
            if (mImei == null) {
                // 小米平板上无法取得imei
                mImei = ISARDeviceUtil.getAndroidId(mContext);
                if (mImei == null) {
                    mImei = String.valueOf(System.currentTimeMillis());
                }
            }
            pi = pm.getPackageInfo(getPackageName(), 0);
            mPackageName = pi.packageName;
            ApplicationInfo aI = pm.getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = aI.metaData;
            mAppKey = bundle.getString("com.idealsee.isarsdk.app_key");
            mAppId = bundle.getString("com.idealsee.isarsdk.app_id");
            Logger.LOGD(TAG + " IdealseeKey = " + bundle.getString("app-key") + ",packageName=" + getPackageName()
                    + ",pi.packageName=" + pi.packageName + ",pi.versionName=" + pi.versionName + ",versionCode="
                    + pi.versionCode);
            if (mAppKey == null) {
                mAppKey = "";
            }
            ISARConstants.APP_ID = mAppId;
            ISARConstants.APP_KEY = mAppKey;
            ISARConstants.APP_IMEI = mImei;
            ISARConstants.APP_VERSION_NAME = ISARConstants.SDK_VERSION;
            if (null != mPackageName) {
                ISARConstants.APP_NAME = mPackageName.substring(mPackageName.lastIndexOf(".") + 1);
            } else {
                ISARConstants.APP_NAME = "DemoApp";
            }
            Logger.LOGD(TAG + " Constants.APP_NAME=" + ISARConstants.APP_NAME + ",appName=" + mPackageName.substring(mPackageName.lastIndexOf(".") + 1));
            ISARConstants.APP_PACKAGE_NAME = mPackageName;
            ISARConstants.updateConstants();
            Logger.LOGD(TAG + " initVersionInfoAPP_ROOT_DIRETCORY=" + ISARConstants.APP_ROOT_DIRETCORY + "，Constants。root=" + ISARConstants.APP_PARENT_PATH);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }
}