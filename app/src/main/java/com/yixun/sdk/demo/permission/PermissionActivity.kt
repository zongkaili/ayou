package com.yixun.sdk.demo.permission

import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.yixun.sdk.demo.R
import com.yixun.sdk.demo.databinding.ActivityPermissionBinding
import com.yixun.sdk.demo.ui.activity.MainActivity
import com.yixun.sdk.demo.ui.activity.introduction.IntroductionActivity

class PermissionActivity : AppCompatActivity(), Navigatoer {
    private val PERMISSION: Int = 0

    override fun requestManifestPermissions(maniArr: Array<String?>) {
        ActivityCompat.requestPermissions(this, maniArr, PERMISSION)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityPermissionBinding>(this, R.layout.activity_permission)
                .apply {
                    viewModel = ViewModelProviders.of(this@PermissionActivity).get(PermissionViewModel::class.java).apply {
                        navigator = this@PermissionActivity
                    }
                }
        binding.viewModel.start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        grantResults.forEach {
            if (it != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "请授予需要的权限", Toast.LENGTH_LONG).show()
                return
            }
        }
        val pre = getSharedPreferences("start", Context.MODE_PRIVATE)
        val b = pre.getBoolean("firstStart", true)
        val intent = Intent(this, if (b) IntroductionActivity::class.java else MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
