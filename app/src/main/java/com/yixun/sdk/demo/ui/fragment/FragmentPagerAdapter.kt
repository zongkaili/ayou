package com.yixun.sdk.demo.ui.fragment

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import java.util.ArrayList

/**
 * author: zongkaili
 * data: 2017/9/25
 */
class MyFragmentPagerAdapter(fm: FragmentManager, fragments: ArrayList<Fragment>, private val mTitles: Array<String>) : FragmentPagerAdapter(fm) {
    private var mFragments = ArrayList<Fragment>()

    init {
        mFragments = fragments
    }

    override fun getCount(): Int {
        return mFragments.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return mTitles[position]
    }

    override fun getItem(position: Int): Fragment {
        return mFragments[position]
    }
}