package com.yixun.sdk.demo.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.yixun.sdk.demo.R
import com.yixun.sdk.demo.databinding.FragmentScenicDetailChildOneBinding
import com.yixun.sdk.demo.ui.activity.ScenicDetailActivity
import me.drakeet.multitype.MultiTypeAdapter

/**
* Created by zongkaili on 2017/9/21.
*/
class ScenicDetailChildOneFragment : BaseBingingFragment<FragmentScenicDetailChildOneBinding>() {
    private lateinit var mFeedAdapter: MultiTypeAdapter
    private lateinit var items: ArrayList<Any>

    override fun onCreateView(mBinding: FragmentScenicDetailChildOneBinding, savedInstanceState: Bundle?) {
        initWidget()
        configRefresh()
        bindFeeds(mBinding)
    }

    override fun getLayoutId(): Int = R.layout.fragment_scenic_detail_child_one
    var mTitle: String = ""

    companion object {
        fun newInstance(title: String): ScenicDetailChildOneFragment {
            val fragment = ScenicDetailChildOneFragment()
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

    private fun bindFeeds(mBinding: FragmentScenicDetailChildOneBinding) {
        mBinding.item1.setOnClickListener {
            startActivity(Intent(context, ScenicDetailActivity::class.java))
        }
        mBinding.item2.setOnClickListener {
            startActivity(Intent(context, ScenicDetailActivity::class.java))
        }
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