package com.yixun.sdk.demo.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.View
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
        initBottomView(savedInstanceState)
//        binding.bottomNavigationView.enableAnimation(false)
//        binding.bottomNavigationView.enableItemShiftingMode(false)
//        binding.bottomNavigationView.enableShiftingMode(false)
//        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item: MenuItem ->
//            val index = binding.bottomNavigationView.getMenuItemPosition(item)
//            changeFragment(index)
//            true
//        }
//        changeFragment(if (savedInstanceState == null) 0
//        else fragmentTag.indexOf(savedInstanceState.getString("tempTag", "page")))
//        binding.fab.setOnClickListener {
//            val intent = Intent(this, SDKDemoActivity::class.java)
//            intent.putExtra("from_main", true)
//            startActivity(intent)
//            overridePendingTransition(R.anim.right_in, R.anim.left_out)
//        }
    }

    private fun initBottomView(savedInstanceState: Bundle?) {
        val listener = View.OnClickListener { view ->
            if (view.id == binding.imgScan.id) {
                val intent = Intent(this, SDKDemoActivity::class.java)
                intent.putExtra("from_main", true)
                startActivity(intent)
                overridePendingTransition(R.anim.right_in, R.anim.left_out)
                return@OnClickListener
            }
            changeFragment(binding.lnlTab.indexOfChild(view))
        }
        binding.imgHome.setOnClickListener(listener)
        binding.imgDiscover.setOnClickListener(listener)
        binding.imgScan.setOnClickListener(listener)
        binding.imgAct.setOnClickListener(listener)
        binding.imgMine.setOnClickListener(listener)
        changeFragment(if (savedInstanceState == null) 0
        else fragmentTag.indexOf(savedInstanceState.getString("tempTag", HomeFragment.TAG)))
    }

    private val fragmentTag = arrayOf(
            HomeFragment::class.java.simpleName,
            DiscoverFragment::class.java.simpleName,
            TestFragment::class.java.simpleName,
            ActFragment::class.java.simpleName,
            MineFragment::class.java.simpleName)

    private fun changeFragment(index: Int) {
        binding.index = index
        if (index == 2) return
        val tempFragment = getFragmentByTag(tempTag)
        var showFragment: Fragment?
        tempFragment?.let { hideFragment(it) }
        showFragment = getFragmentByTag(fragmentTag[index])
        if (showFragment == null) {
            showFragment = when (index) {
                0 -> HomeFragment.newInstance("title")
                1 -> DiscoverFragment.newInstance("title")
                3 -> ActFragment.newInstance("title")
                4 -> MineFragment.newInstance("title")
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