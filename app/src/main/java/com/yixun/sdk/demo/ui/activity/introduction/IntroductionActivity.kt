package com.yixun.sdk.demo.ui.activity.introduction

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.SharedPreferencesCompat
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.idealsee.juxingqiancheng.Introduction.IntroductionViewModel
import com.yixun.chime.ui.activity.Introduction.IntroductionNavigator
import com.yixun.sdk.demo.R
import com.yixun.sdk.demo.databinding.ActivityIntroductionBinding
import com.yixun.sdk.demo.ui.activity.BaseBindingActivity
import com.yixun.sdk.demo.ui.activity.MainActivity

class IntroductionActivity : BaseBindingActivity<ActivityIntroductionBinding>(), IntroductionNavigator {
    override fun getChildActivity(): Activity? = this

    override fun getLayoutId(): Int = R.layout.activity_introduction


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.viewModel = ViewModelProviders.of(this).get(IntroductionViewModel::class.java)
        binding.viewModel.navigator = this
        binding.viewPager.adapter = IntroductionPagerAdapter()
        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                binding.index = position
            }
        })
    }

    class IntroductionPagerAdapter : PagerAdapter() {
        val resArr = intArrayOf(
                R.drawable.pic_guide_1,
                R.drawable.pic_guide_2,
                R.drawable.pic_guide_3,
                R.drawable.pic_guide_4
        )

        override fun isViewFromObject(view: View?, `object`: Any?): Boolean = view == `object`

        override fun getCount(): Int = resArr.size

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any?) {
            container.removeView(`object` as View?)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            var iv = ImageView(container.context)
            iv.scaleType = ImageView.ScaleType.CENTER_CROP
            iv.setImageResource(resArr[position])
            container.addView(iv)
            return iv
        }
    }

    override fun skipToNextActivity() {
        val pre = getSharedPreferences("start", Context.MODE_PRIVATE)
        val edit = pre.edit()
        edit.putBoolean("firstStart", false)
        SharedPreferencesCompat.EditorCompat.getInstance().apply(edit)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

}
