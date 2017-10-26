package com.yixun.sdk.demo.entity

import android.text.TextUtils

/**
 * Created by zongkaili on 2017/9/22.
 */
data class NewsInfo(
        val title: String,
        val subTitle: String,
        val imgUrl: String)
{

    fun hasImg(): Boolean {
        return TextUtils.isEmpty(imgUrl)
    }
}