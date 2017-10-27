package com.yixun.sdk.demo.entity

import android.text.TextUtils

/**
 * Created by zongkaili on 2017/9/22.
 */
data class Order(
        val orderNum: String,
        val orderStatus: String,
        val title: String,
        val lifeTime: String,
        val usedRange: String,
        val money: String,
        val btnStr: String,
        val imgUrl: String)
{

    fun hasImg(): Boolean {
        return TextUtils.isEmpty(imgUrl)
    }
}