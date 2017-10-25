package com.yixun.sdk.demo.ui.fragment

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
* Created by zongkaili on 2017/9/21.
*/
abstract class BaseBingingFragment<B : ViewDataBinding> : Fragment() {
    lateinit var mBinding: B
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false)
        onCreateView(mBinding, savedInstanceState)
        return mBinding.root

    }

    abstract fun onCreateView(mBinding: B, savedInstanceState: Bundle?)

    abstract fun getLayoutId(): Int


}