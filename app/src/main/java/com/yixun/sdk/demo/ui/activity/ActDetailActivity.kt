package com.yixun.sdk.demo.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.yixun.sdk.demo.R
import com.yixun.sdk.demo.databinding.ActivityActDetailBinding


/**
* Created by zongkaili on 2017/9/28.
*/
class ActDetailActivity : BaseBindingActivity<ActivityActDetailBinding>() {
    private lateinit var mToolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initToolbar()
        initWidget()
        configRefresh()
    }

    override fun getChildActivity(): Activity? = this

    override fun getLayoutId(): Int = R.layout.activity_act_detail

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    private fun initToolbar() {
        mToolbar = binding.toolbar
        mToolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun initWidget() {
        binding.pageStatusLayout.showLoading()
        binding.pageStatusLayout.postDelayed({
            binding.pageStatusLayout.showContent()
        },2000)
        binding.pageStatusLayout.setRetryListener(View.OnClickListener {
            _ -> Toast.makeText(this, "retry", Toast.LENGTH_LONG).show()
        })
        binding.button.setOnClickListener {
            startActivity(Intent(this,ScenicDetailActivity::class.java))
        }
    }

    private fun configRefresh() {
        var refreshLayout: SmartRefreshLayout = binding.refreshLayout
        refreshLayout.isEnableAutoLoadmore = true
        refreshLayout.setOnRefreshListener { refreshlayout ->
            refreshlayout.layout.postDelayed({
                refreshlayout.finishRefresh()
                refreshlayout.isLoadmoreFinished = false
            }, 2000)
        }
        refreshLayout.setOnLoadmoreListener { refreshlayout ->
            refreshLayout.layout.postDelayed({
                refreshLayout.finishLoadmore()
            },2000)
        }
    }
}