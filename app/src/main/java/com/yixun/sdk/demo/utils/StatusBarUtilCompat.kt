package com.yixun.sdk.demo.utils

import android.os.Build
import android.support.v7.app.AppCompatActivity
import com.yixun.sdk.demo.utils.StatusBarUtil

/**
 * description: <一句话功能简述>
 *
 * @author tt11
 * @time 2017/9/22.
</一句话功能简述> */

object StatusBarUtilCompat {
    fun setTransparent(activity: AppCompatActivity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StatusBarUtil.setTransparent(activity)
        } else {
            StatusBarUtil.setTranslucent(activity, (255 * 0.3).toInt())
        }
    }
}
