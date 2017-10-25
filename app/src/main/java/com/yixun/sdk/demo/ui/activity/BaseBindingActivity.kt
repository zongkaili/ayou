package com.yixun.sdk.demo.ui.activity

import android.app.Activity
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.yixun.sdk.demo.utils.StatusBarUtilCompat

/**
* Created by zongkaili on 2017/9/21.
*/
abstract class BaseBindingActivity<B : ViewDataBinding> : AppCompatActivity() {

    lateinit var binding: B

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(getChildActivity(), getLayoutId())
        StatusBarUtilCompat.setTransparent(this)
    }

    abstract fun getChildActivity(): Activity?

    abstract fun getLayoutId(): Int

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

}