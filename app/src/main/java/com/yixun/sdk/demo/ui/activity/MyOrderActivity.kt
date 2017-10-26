package com.yixun.sdk.demo.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.yixun.chime.entity.BannerAd
import com.yixun.chime.entity.viewbinder.HomeBannerViewBinder
import com.yixun.chime.widge.SpaceListDecoration
import com.yixun.sdk.demo.R
import com.yixun.sdk.demo.databinding.ActivityMyOrderBinding
import com.yixun.sdk.demo.entity.NewsInfo
import com.yixun.sdk.demo.entity.viewbinder.ActNewsInfoViewBinder
import me.drakeet.multitype.MultiTypeAdapter
import java.io.BufferedReader
import java.io.InputStreamReader

/**
* Created by zongkaili on 2017/9/28.
*/
class MyOrderActivity : BaseBindingActivity<ActivityMyOrderBinding>() {
    private lateinit var mToolbar: Toolbar
    private lateinit var mFeedAdapter: MultiTypeAdapter
    private lateinit var items: ArrayList<Any>
    private lateinit var topBannerList: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initToolbar()
        initWidget()
        configRefresh()
        bindFeeds(binding)
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

    private fun initWidget() {
        binding.pageStatusLayout.showLoading()
        binding.pageStatusLayout.postDelayed({
            binding.pageStatusLayout.showContent()
        },2000)
        binding.pageStatusLayout.setRetryListener(View.OnClickListener {
            _ -> Toast.makeText(this, "retry", Toast.LENGTH_LONG).show()
        })
    }

    private fun bindFeeds(mBinding: ActivityMyOrderBinding) {
        val layoutManager: LinearLayoutManager = LinearLayoutManager(this)
        layoutManager.isAutoMeasureEnabled = true
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        mBinding.recyclerView.addItemDecoration(SpaceListDecoration(10))
        mFeedAdapter = MultiTypeAdapter()
        mFeedAdapter.register(BannerAd::class.java, HomeBannerViewBinder())
        mFeedAdapter.register(NewsInfo::class.java, ActNewsInfoViewBinder())
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        mBinding.recyclerView.layoutManager = layoutManager
        mBinding.recyclerView.adapter = mFeedAdapter
        topBannerList = ArrayList<String>()
        items = ArrayList<Any>()
        topBannerList = listOf(
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1509604512&di=bd751618469c31d9bbb753c55a610514&imgtype=jpg&er=1&src=http%3A%2F%2Fyouimg1.c-ctrip.com%2Ftarget%2Ftg%2F945%2F438%2F286%2F3ff7f7bf370b44a18664e59bec312b7a.jpg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1509009684279&di=6cc565baa2779bdae79816f9e18e6d39&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fimage%2Fc0%253Dshijue1%252C0%252C0%252C294%252C40%2Fsign%3D45eabcda953df8dcb23087d2a57818fe%2Fd000baa1cd11728b72353e0fc2fcc3cec3fd2c86.jpg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1509009684276&di=651a85088295848a0cb728440bd45e86&imgtype=0&src=http%3A%2F%2Fwww.sznews.com%2Ftravel%2Fimages%2Fattachement%2Fjpg%2Fsite3%2F20150812%2F7427ea33bc74173557974b.jpg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1509009748208&di=45e12109f8832ca0171d2b6a26b717c3&imgtype=0&src=http%3A%2F%2Fwww.cqzql.com%2Fuploads%2Farcimgs%2F20160112%2F1452581018438513.jpg")
        val bfn = BufferedReader(InputStreamReader(assets?.open("json/newsinfos.json")))
        val newsInfos: ArrayList<NewsInfo> = Gson().fromJson<List<NewsInfo>>(bfn, object : TypeToken<List<NewsInfo>>() {}.type) as ArrayList<NewsInfo>
        items.add(BannerAd(topBannerList))
        newsInfos.forEach {
            it ->
            items.add(it)
        }
        mFeedAdapter.items = items
        mFeedAdapter.notifyDataSetChanged()
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