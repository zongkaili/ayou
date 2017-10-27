package com.yixun.sdk.demo.ui.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.yixun.sdk.demo.R
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.yixun.chime.entity.BannerAd
import com.yixun.chime.entity.viewbinder.HomeBannerViewBinder
import com.yixun.sdk.demo.databinding.FragmentTestBinding
import me.drakeet.multitype.MultiTypeAdapter

/**
* Created by zongkaili on 2017/10/25.
*/
class TestFragment : BaseBingingFragment<FragmentTestBinding>() {
    private lateinit var mFeedAdapter: MultiTypeAdapter
    private lateinit var items: ArrayList<Any>
    private lateinit var topBannerList: List<String>

    override fun onCreateView(mBinding: FragmentTestBinding, savedInstanceState: Bundle?) {
        configRefresh()
        bindFeeds(mBinding)
    }

    override fun getLayoutId(): Int = R.layout.fragment_test
    var mTitle: String = ""

    companion object {
        fun newInstance(title: String): TestFragment {
            val fragment = TestFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            fragment.mTitle = title
            return fragment
        }
    }

    private fun bindFeeds(mBinding: FragmentTestBinding) {
        mFeedAdapter = MultiTypeAdapter()
        mFeedAdapter.register(BannerAd::class.java, HomeBannerViewBinder())
//        mBinding.recyclerView.addItemDecoration(SpaceListDecoration(resources.getDimension(R.dimen.line_height).toInt()))
        val layoutManager: LinearLayoutManager = LinearLayoutManager(context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        mBinding.recyclerView.layoutManager = layoutManager
        mBinding.recyclerView.adapter = mFeedAdapter
        topBannerList = ArrayList<String>()
        items = ArrayList<Any>()
        topBannerList = listOf(
                "http://b.hiphotos.baidu.com/image/pic/item/dcc451da81cb39dbbf279a97d9160924aa18300f.jpg",
                "http://c.hiphotos.baidu.com/image/pic/item/cb8065380cd791232d6306b4a4345982b3b7806b.jpg",
                "http://imgsrc.baidu.com/image/c0%3Dshijue1%2C0%2C0%2C294%2C40/sign=c55331232c9759ee5e5d6888da922963/3c6d55fbb2fb4316a08b2f542aa4462309f7d30c.jpg")
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