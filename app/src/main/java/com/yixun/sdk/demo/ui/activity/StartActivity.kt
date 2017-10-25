package com.yixun.sdk.demo.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import com.yixun.sdk.demo.R
import com.yixun.sdk.demo.ui.activity.introduction.IntroductionActivity

import com.yixun.sdk.demo.utils.StatusBarUtilCompat

class StartActivity : AppCompatActivity() {
    private val TAG = StartActivity::class.java.simpleName
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
       StatusBarUtilCompat.setTransparent(this)
        Handler(this.mainLooper).postDelayed({
            start()
        }, 2000)
    }

    fun start() {
        val pre = getSharedPreferences("start", Context.MODE_PRIVATE)
        val b = pre.getBoolean("firstStart", true)
        val intent = Intent(this@StartActivity, if (b) IntroductionActivity::class.java else MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}
