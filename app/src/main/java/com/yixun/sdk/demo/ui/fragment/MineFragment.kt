package com.yixun.sdk.demo.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.widget.TextView
import android.widget.Toast
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.yixun.chime.entity.BannerAd
import com.yixun.chime.entity.viewbinder.HomeBannerViewBinder
import com.yixun.sdk.demo.R
import com.yixun.sdk.demo.databinding.FragmentMineBinding
import com.yixun.sdk.demo.ui.activity.MyOrderActivity
import com.yixun.sdk.demo.ui.activity.ScenicDetailActivity
import me.drakeet.multitype.MultiTypeAdapter

/**
* Created by zongkaili on 2017/10/25.
*/
class MineFragment : BaseBingingFragment<FragmentMineBinding>() {

    override fun onCreateView(mBinding: FragmentMineBinding, savedInstanceState: Bundle?) {
        configRefresh()
        bindFeeds(mBinding)
    }

    override fun getLayoutId(): Int = R.layout.fragment_mine
    var mTitle: String = ""

    companion object {
        fun newInstance(title: String): MineFragment {
            val fragment = MineFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            fragment.mTitle = title
            return fragment
        }
    }

    private fun bindFeeds(mBinding: FragmentMineBinding) {
        mBinding.root.findViewById<TextView>(R.id.tvOrder).setOnClickListener {
            startActivity(Intent(context, MyOrderActivity::class.java))
        }
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
            },2000)
        }
    }
}