package com.idealsee.juxingqiancheng.Introduction

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.yixun.chime.ui.activity.Introduction.IntroductionNavigator

/**
 * description: <一句话功能简述>
 * @author tt11
 * @time  2017/9/11.
 */
class IntroductionViewModel(context: Application) : AndroidViewModel(context) {
    var navigator: IntroductionNavigator? = null
    fun skip() {
        navigator?.skipToNextActivity()
    }
}