package com.yixun.sdk.demo.ui.activity

import android.app.Activity
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.yixun.sdk.demo.ui.fragment.ScenicDetailChildOneFragment
import com.yixun.sdk.demo.R
import com.yixun.sdk.demo.databinding.ActivityScenicDetailBinding
import com.yixun.sdk.demo.ui.fragment.MyFragmentPagerAdapter
import com.yixun.sdk.demo.ui.fragment.ScenicDetailChildTwoFragment

/**
* Created by zongkaili on 2017/9/28.
*/
class ScenicDetailActivity : BaseBindingActivity<ActivityScenicDetailBinding>() {
    private lateinit var mToolbar: Toolbar
    private val mFragments = ArrayList<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initToolbar()
        setupViewpager()
        bindTabVp()
    }

    override fun getChildActivity(): Activity? = this

    override fun getLayoutId(): Int = R.layout.activity_scenic_detail

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    private fun initToolbar() {
        mToolbar = binding.toolbar
        mToolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun setupViewpager() {
        val stringArray = resources.getStringArray(R.array.tab_scenic_detail_titles)
//        stringArray.indices.mapTo(mFragments) {
//            ScenicDetailChildOneFragment.newInstance(stringArray[it]!!)
//        }
        mFragments.add(ScenicDetailChildOneFragment.newInstance(stringArray[0]!!))
        mFragments.add(ScenicDetailChildTwoFragment.newInstance(stringArray[1]!!))
        val adapter = MyFragmentPagerAdapter(supportFragmentManager, mFragments, stringArray)
        binding.viewpager.adapter = adapter
        binding.viewpager.offscreenPageLimit = stringArray.size
        adapter.notifyDataSetChanged()
    }

    private fun bindTabVp() {
        val mTabs = binding.tabLayout
        for (i in mFragments.indices) {
            mTabs.addTab(mTabs.newTab().setText(binding.viewpager.adapter.getPageTitle(i)))
        }
        mTabs.setupWithViewPager(binding.viewpager)
    }
}