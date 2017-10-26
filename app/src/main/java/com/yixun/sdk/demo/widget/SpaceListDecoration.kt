package com.yixun.chime.widge

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

/**
* author: zongkaili
* data: 17/09/25
*/
class SpaceListDecoration(private val mSpace: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
            outRect.bottom = mSpace
    }

}
