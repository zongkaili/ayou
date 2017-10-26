package com.yixun.sdk.demo.entity

import android.text.TextUtils

/**
 * Created by zongkaili on 2017/9/22.
 */

data class Scenics(var type: String, var list: List<Scenic>) {
    companion object {
        val PRODUCT = "product"
        val COLLATERAL = "collateral"
        val AUCTION_PRE = 0
        val AUCTION_DOING = 1
        val AUCTION_OVER = 2
    }
    data class Scenic(
            val type: Int,
            val name: String,
            val imgUrl: String)
    {
        fun hasImg(): Boolean {
            return TextUtils.isEmpty(imgUrl)
        }

        fun isCollateral(): Boolean {
            return type == 1
        }

    }
}