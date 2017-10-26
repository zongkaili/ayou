package com.yixun.chime.widge

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View

/**
* author: zongkaili
* data: 17/08/18
*/

class SpaceGridDecoration @JvmOverloads constructor(private val space: Int, private val column: Int = DEFAULT_COLUMN) : RecyclerView.ItemDecoration() {
    companion object {
        private val DEFAULT_COLUMN = Integer.MAX_VALUE
    }
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State?) {
        /*
         * 除了第一行，每一行的每个item，距离顶部的item距离值space
         * 水平间距略复杂：对于grid方式垂直滚动的recyclerview，假设有4列
         * 排列如下：
         * ———————————————————————————————————————————
         * |    1    | |   2   | |   3    | |   4    |
         * |    5    | |   6   | |   7    | |   8    |
         * -------------------------------------------
         * 如果给不是第一列的设置左边距，第一列的会比其他item宽一个边距
         * 如果给不是最后一列的设置右边距，同理，最后一列比其他item宽一个边距
         * 也就是说，边距也是item的一部分，所以，这种方法，会导致四个item 不一样宽
         *
         * 下面的方法，通过算法，让每一列的所有item平均分担边距
         * 对于 column = n 的grid，假设两个item间距是M：水平方向的内边距为：(n-1)*M
         * 平均到每一个item后，平均是：A=(n-1)*M/n
         * 则有如下规律：
         * 1、第一列item的左边距为零，所以右边距只能是A
         * 2、相邻左itemL，和右itemR，的左右边距和等于一个间距M
         * 类推：
         *      L       R
         *   0  0       A
         *   1  M-A     A-(M-A)
         *   2  2(M-A)  A-2(M-A)
         *   3  3(M-A)  A-3(M-A)
         *   ...
         *   n  n(M-A)  A-n(M-A)
         *   n<=column
         */
        outRect.top = space
        val pos = parent.getChildLayoutPosition(view)
        val total = parent.childCount
        if (isFirstRow(pos)) {
            outRect.top = 0
        }
        if (isLastRow(pos, total)) {
            outRect.bottom = 5
        }
        if (column != DEFAULT_COLUMN) {
            val avg = (column - 1).toFloat() * space.toFloat() * 1.0f / column
            outRect.left = (pos % column * (space - avg)).toInt()
            outRect.right = (avg - pos % column * (space - avg)).toInt()
        }
    }

    private fun isFirstRow(pos: Int): Boolean {
        return pos < column
    }

    private fun isLastRow(pos: Int, total: Int): Boolean {
        return total - pos <= column
    }

    private fun isFirstColumn(pos: Int): Boolean {
        return pos % column == 0
    }

    internal fun isSecondColumn(pos: Int): Boolean {
        return isFirstColumn(pos - 1)
    }

    private fun isEndColumn(pos: Int): Boolean {
        return isFirstColumn(pos + 1)
    }

    internal fun isNearEndColumn(pos: Int): Boolean {
        return isEndColumn(pos + 1)
    }

}