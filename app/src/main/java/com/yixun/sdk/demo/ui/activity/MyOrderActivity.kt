package com.yixun.sdk.demo.ui.activity

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.yixun.sdk.demo.R
import com.yixun.sdk.demo.databinding.ActivityMyOrderBinding
import com.yixun.sdk.demo.ui.fragment.MyFragmentPagerAdapter
import com.yixun.sdk.demo.ui.fragment.MyOrderChildFragment
import com.youth.banner.loader.ImageLoaderInterface

/**
 * Created by zongkaili on 2017/9/28.
 */
class MyOrderActivity : BaseBindingActivity<ActivityMyOrderBinding>() {
    private lateinit var mToolbar: Toolbar
    private val mFragments = ArrayList<Fragment>()
    private lateinit var topBannerList: List<Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initToolbar()
        bindBanner()
        setupViewpager()
        bindTabVp()
    }

    override fun getChildActivity(): Activity? = this

    override fun getLayoutId(): Int = R.layout.activity_my_order

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
        val stringArray = resources.getStringArray(R.array.tab_my_order_titles)
        stringArray.indices.mapTo(mFragments) {
            MyOrderChildFragment.newInstance(stringArray[it]!!)
        }
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

    private fun bindBanner() {
        topBannerList = ArrayList<Any>()
        topBannerList = listOf(
                R.drawable.pic_home_banner,
                "http://b.hiphotos.baidu.com/image/pic/item/dcc451da81cb39dbbf279a97d9160924aa18300f.jpg",
                "http://c.hiphotos.baidu.com/image/pic/item/cb8065380cd791232d6306b4a4345982b3b7806b.jpg",
                "http://imgsrc.baidu.com/image/c0%3Dshijue1%2C0%2C0%2C294%2C40/sign=c55331232c9759ee5e5d6888da922963/3c6d55fbb2fb4316a08b2f542aa4462309f7d30c.jpg")
        binding.bannerFeed.setDelayTime(3000)
        binding.bannerFeed.setImageLoader(object : ImageLoaderInterface<View> {
            override fun displayImage(context: Context, path: Any, imageView: View) {
                Glide.with(context.applicationContext)
                        .load(path)
                        .into(imageView as ImageView)
            }

            override fun createImageView(context: Context): View {
                return ImageView(context)
            }
        })
        binding.bannerFeed.setImages(topBannerList).start()
        binding.bannerFeed.setOnBannerListener {
            //                Log.d("banner", " click : " + it)
//                binding.root.context.startActivity(Intent(binding.root.context, MyProductsActivity::class.java))
        }
    }

}