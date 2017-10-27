package com.yixun.sdk.demo.ui.fragment

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.yixun.chime.widge.SpaceListDecoration
import com.yixun.sdk.demo.R
import com.yixun.sdk.demo.databinding.FragmentMyOrderChildBinding
import com.yixun.sdk.demo.entity.NewsInfo
import com.yixun.sdk.demo.entity.Order
import com.yixun.sdk.demo.entity.viewbinder.ActNewsInfoViewBinder
import com.yixun.sdk.demo.entity.viewbinder.OrderViewBinder
import me.drakeet.multitype.MultiTypeAdapter
import java.io.BufferedReader
import java.io.InputStreamReader

/**
* Created by zongkaili on 2017/9/21.
*/
class MyOrderChildFragment : BaseBingingFragment<FragmentMyOrderChildBinding>() {
    private lateinit var mFeedAdapter: MultiTypeAdapter
    private lateinit var items: ArrayList<Any>

    override fun onCreateView(mBinding: FragmentMyOrderChildBinding, savedInstanceState: Bundle?) {
        initWidget()
        configRefresh()
        bindFeeds(mBinding)
    }

    override fun getLayoutId(): Int = R.layout.fragment_my_order_child
    var mTitle: String = ""

    companion object {
        fun newInstance(title: String): MyOrderChildFragment {
            val fragment = MyOrderChildFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            fragment.mTitle = title
            return fragment
        }
    }

    private fun initWidget() {
        mBinding.pageStatusLayout.showLoading()
        mBinding.pageStatusLayout.postDelayed({
            mBinding.pageStatusLayout.showContent()
        },2000)
        mBinding.pageStatusLayout.setRetryListener(View.OnClickListener {
            _ -> Toast.makeText(context, "retry", Toast.LENGTH_LONG).show()
        })
    }

    private fun bindFeeds(mBinding: FragmentMyOrderChildBinding) {
        val layoutManager: LinearLayoutManager = LinearLayoutManager(context)
        layoutManager.isAutoMeasureEnabled = true
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        mBinding.recyclerView.addItemDecoration(SpaceListDecoration(10))
        mFeedAdapter = MultiTypeAdapter()
        mFeedAdapter.register(Order::class.java, OrderViewBinder())
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        mBinding.recyclerView.layoutManager = layoutManager
        mBinding.recyclerView.adapter = mFeedAdapter
        items = ArrayList<Any>()
        val bfn = BufferedReader(InputStreamReader(context.assets?.open("json/orders.json")))
        val orders: ArrayList<Order> = Gson().fromJson<List<Order>>(bfn, object : TypeToken<List<Order>>() {}.type) as ArrayList<Order>
        orders.forEach {
            it ->
            items.add(it)
        }
        mFeedAdapter.items = items
        mFeedAdapter.notifyDataSetChanged()
    }

    private fun configRefresh() {
        var refreshLayout: SmartRefreshLayout = mBinding.refreshLayout
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