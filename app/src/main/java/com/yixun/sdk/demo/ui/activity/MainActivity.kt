package com.yixun.sdk.demo.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.MenuItem
import com.yixun.sdk.demo.databinding.ActivityMainNewBinding
import com.yixun.sdk.demo.R
import com.yixun.sdk.demo.SDKDemoActivity
import com.yixun.sdk.demo.ui.fragment.*

class MainActivity : BaseBindingActivity<ActivityMainNewBinding>() {
    override fun getChildActivity(): Activity? = this

    override fun getLayoutId(): Int = R.layout.activity_main_new

    var tempTag: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.bottomNavigationView.enableAnimation(false)
        binding.bottomNavigationView.enableItemShiftingMode(false)
        binding.bottomNavigationView.enableShiftingMode(false)
        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
            val index = binding.bottomNavigationView.getMenuItemPosition(item)
            changeFragment(index)
            true
        }
        changeFragment(if (savedInstanceState == null) 0
        else fragmentTag.indexOf(savedInstanceState.getString("tempTag", "page")))
        binding.fab.setOnClickListener {
            val intent = Intent(this, SDKDemoActivity::class.java)
            intent.putExtra("from_main", true)
            startActivity(intent)
            overridePendingTransition(R.anim.right_in, R.anim.left_out)
        }
    }

    private val fragmentTag = arrayOf(
            TestFragment::class.java.simpleName,
            TestFragment::class.java.simpleName,
            TestFragment::class.java.simpleName,
            TestFragment::class.java.simpleName,
            TestFragment::class.java.simpleName)

    fun setCurrentItem(index: Int){
       binding.bottomNavigationView.getBottomNavigationItemView(index).performClick()
    }

    private fun changeFragment(index: Int) {
        if (index == 2) return
        val tempFragment = getFragmentByTag(tempTag)
        var showFragment: Fragment?
        tempFragment?.let { hideFragment(it) }
        showFragment = getFragmentByTag(fragmentTag[index])
        if (showFragment == null) {
            showFragment = when (index) {
                0 -> HomeFragment.newInstance("title")
                1 -> TestFragment.newInstance("title")
                3 -> TestFragment.newInstance("title")
                4 -> TestFragment.newInstance("title")
                else -> null
            }
            addFragment(R.id.flContainer, showFragment!!, fragmentTag[index])
        }
        showFragment(showFragment)
        tempTag = fragmentTag[index]
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("tempTag", tempTag)
    }
}