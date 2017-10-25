package com.yixun.sdk.demo.ui.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.yixun.sdk.demo.R
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.yixun.chime.entity.BannerAd
import com.yixun.chime.entity.viewbinder.HomeBannerViewBinder
import com.yixun.sdk.demo.databinding.FragmentHomeBinding
import me.drakeet.multitype.MultiTypeAdapter

/**
* Created by zongkaili on 2017/10/25.
*/
class HomeFragment : BaseBingingFragment<FragmentHomeBinding>() {
    private lateinit var mFeedAdapter: MultiTypeAdapter
    private lateinit var items: ArrayList<Any>
    private lateinit var topBannerList: List<String>

    override fun onCreateView(mBinding: FragmentHomeBinding, savedInstanceState: Bundle?) {
        configRefresh()
        bindFeeds(mBinding)
    }

    override fun getLayoutId(): Int = R.layout.fragment_home
    var mTitle: String = ""

    companion object {
        fun newInstance(title: String): HomeFragment {
            val fragment = HomeFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            fragment.mTitle = title
            return fragment
        }
    }

    private fun bindFeeds(mBinding: FragmentHomeBinding) {
        mFeedAdapter = MultiTypeAdapter()
        mFeedAdapter.register(BannerAd::class.java, HomeBannerViewBinder())
//        mBinding.recyclerView.addItemDecoration(SpaceListDecoration(resources.getDimension(R.dimen.line_height).toInt()))
        val layoutManager: LinearLayoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        mBinding.recyclerView.layoutManager = layoutManager
        mBinding.recyclerView.adapter = mFeedAdapter
        topBannerList = ArrayList<String>()
        items = ArrayList<Any>()
        topBannerList = listOf("http://img1.gtimg.com/sports/pics/hv1/105/196/1592/103569885.jpg",
                "http://img.weiot.net/portal/201604/14/041002yw70140k3wf89uwv.jpg",
                "http://pic.qiantucdn.com/58pic/12/72/06/28n58PICBWj.jpg",
                "http://olpic.tgbusdata.cn/uploads/oltgbuspic/20121010/new/1349830225_39bc7559.jpg")
        items.add(BannerAd(topBannerList))
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