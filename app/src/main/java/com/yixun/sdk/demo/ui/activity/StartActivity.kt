package com.yixun.sdk.demo.ui.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.yixun.sdk.demo.R
import com.yixun.sdk.demo.permission.PermissionActivity
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
        if (checkPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            val pre = getSharedPreferences("start", Context.MODE_PRIVATE)
            val b = pre.getBoolean("firstStart", true)
            val intent = Intent(this@StartActivity, if (b) IntroductionActivity::class.java else MainActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this@StartActivity, PermissionActivity::class.java)
            startActivity(intent)
        }
        finish()
    }

    private fun checkPermissions(vararg permissions: String): Boolean {
        permissions.forEach {
            Log.d(TAG, it)
            if (ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

}
