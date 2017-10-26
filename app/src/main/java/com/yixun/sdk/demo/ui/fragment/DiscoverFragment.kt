package com.yixun.sdk.demo.ui.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.yixun.chime.entity.BannerAd
import com.yixun.chime.entity.viewbinder.HomeBannerViewBinder
import com.yixun.sdk.demo.R
import com.yixun.sdk.demo.databinding.FragmentDiscoverBinding
import com.yixun.sdk.demo.entity.Category
import com.yixun.sdk.demo.entity.Scenics
import com.yixun.sdk.demo.entity.Scenics.Scenic
import com.yixun.sdk.demo.entity.viewbinder.DiscoverCategoryViewBinder
import com.yixun.sdk.demo.entity.viewbinder.DiscoverScenicsViewBinder
import me.drakeet.multitype.MultiTypeAdapter
import java.io.BufferedReader
import java.io.InputStreamReader

/**
* Created by zongkaili on 2017/10/25.
*/
class DiscoverFragment : BaseBingingFragment<FragmentDiscoverBinding>() {
    private lateinit var mFeedAdapter: MultiTypeAdapter
    private lateinit var items: ArrayList<Any>
    private lateinit var topBannerList: List<Any>

    override fun onCreateView(mBinding: FragmentDiscoverBinding, savedInstanceState: Bundle?) {
        configRefresh()
        bindFeeds(mBinding)
    }

    override fun getLayoutId(): Int = R.layout.fragment_discover
    var mTitle: String = ""

    companion object {
        fun newInstance(title: String): DiscoverFragment {
            val fragment = DiscoverFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            fragment.mTitle = title
            return fragment
        }
    }

    private fun bindFeeds(mBinding: FragmentDiscoverBinding) {
        mFeedAdapter = MultiTypeAdapter()
        mFeedAdapter.register(BannerAd::class.java, HomeBannerViewBinder())
        mFeedAdapter.register(Category::class.java, DiscoverCategoryViewBinder())
        mFeedAdapter.register(Scenics::class.java, DiscoverScenicsViewBinder())
//        mBinding.recyclerView.addItemDecoration(SpaceListDecoration(resources.getDimension(R.dimen.line_height).toInt()))
        val layoutManager: LinearLayoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        mBinding.recyclerView.layoutManager = layoutManager
        mBinding.recyclerView.adapter = mFeedAdapter
        topBannerList = ArrayList<Any>()
        items = ArrayList<Any>()
        topBannerList = listOf(
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1509604512&di=bd751618469c31d9bbb753c55a610514&imgtype=jpg&er=1&src=http%3A%2F%2Fyouimg1.c-ctrip.com%2Ftarget%2Ftg%2F945%2F438%2F286%2F3ff7f7bf370b44a18664e59bec312b7a.jpg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1509009684279&di=6cc565baa2779bdae79816f9e18e6d39&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fimage%2Fc0%253Dshijue1%252C0%252C0%252C294%252C40%2Fsign%3D45eabcda953df8dcb23087d2a57818fe%2Fd000baa1cd11728b72353e0fc2fcc3cec3fd2c86.jpg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1509009684276&di=651a85088295848a0cb728440bd45e86&imgtype=0&src=http%3A%2F%2Fwww.sznews.com%2Ftravel%2Fimages%2Fattachement%2Fjpg%2Fsite3%2F20150812%2F7427ea33bc74173557974b.jpg",
                "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1509009748208&di=45e12109f8832ca0171d2b6a26b717c3&imgtype=0&src=http%3A%2F%2Fwww.cqzql.com%2Fuploads%2Farcimgs%2F20160112%2F1452581018438513.jpg")

        var scenicsList = listOf(
                Scenic(0,"",""),
                Scenic(0,"",""),
                Scenic(0,"",""),
                Scenic(0,"",""),
                Scenic(0,"",""),
                Scenic(0,"","")
        )

        items.add(BannerAd(topBannerList))
        items.add(Category(""))
        items.add(Scenics("",scenicsList))
        mFeedAdapter.items = items
        mFeedAdapter.notifyDataSetChanged()
    }

    private fun configRefresh() {
        val refreshLayout: SmartRefreshLayout = mBinding.refreshLayout
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
                if (mFeedAdapter.itemCount > 60) {
                    Toast.makeText(context, "数据全部加载完毕", Toast.LENGTH_SHORT).show()
                    refreshlayout.isLoadmoreFinished = true //将不会再次触发加载更多事件
                }
            },2000)
        }
    }
}