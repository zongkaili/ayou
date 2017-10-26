package com.yixun.sdk.demo.widget

import android.app.Activity
import android.app.Fragment
import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.annotation.LayoutRes
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.yixun.sdk.demo.R

import java.util.HashMap

class PageStatusLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.stylePageStatusLayout) : FrameLayout(context, attrs, defStyleAttr) {
    interface OnInflateListener {
        fun onInflate(inflated: View)
    }

    internal var mEmptyImage: Int = 0
    internal var mEmptyText: CharSequence

    internal var mErrorImage: Int = 0
    internal var mErrorText: CharSequence
    internal var mRetryText: CharSequence
    internal var mRetryButtonClickListener: View.OnClickListener = View.OnClickListener { v ->
        if (mRetryListener != null) {
            mRetryListener!!.onClick(v)
        }
    }
    internal var mRetryListener: View.OnClickListener? = null

    internal var mOnEmptyInflateListener: OnInflateListener? = null
    internal var mOnErrorInflateListener: OnInflateListener? = null

    internal var mTextColor: Int = 0
    internal var mTextSize: Int = 0
    internal var mButtonTextColor: Int = 0
    internal var mButtonTextSize: Int = 0
    internal var mButtonBackground: Drawable
    internal var mEmptyResId = View.NO_ID
    internal var mLoadingResId = View.NO_ID
    internal var mErrorResId = View.NO_ID
    internal var mContentId = View.NO_ID

    internal var mLayouts: MutableMap<Int, View> = HashMap()
    internal var mInflater: LayoutInflater = LayoutInflater.from(context)
    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.PageStatusLayout, defStyleAttr, R.style.PageStatusLayout_Style)
        mEmptyImage = a.getResourceId(R.styleable.PageStatusLayout_llEmptyImage, View.NO_ID)
        mEmptyText = a.getString(R.styleable.PageStatusLayout_llEmptyText)

        mErrorImage = a.getResourceId(R.styleable.PageStatusLayout_llErrorImage, View.NO_ID)
        mErrorText = a.getString(R.styleable.PageStatusLayout_llErrorText)
        mRetryText = a.getString(R.styleable.PageStatusLayout_llRetryText)

        mTextColor = a.getColor(R.styleable.PageStatusLayout_llTextColor, 0xff999999.toInt())
        mTextSize = a.getDimensionPixelSize(R.styleable.PageStatusLayout_llTextSize, dp2px(16f))

        mButtonTextColor = a.getColor(R.styleable.PageStatusLayout_llButtonTextColor, 0xff999999.toInt())
        mButtonTextSize = a.getDimensionPixelSize(R.styleable.PageStatusLayout_llButtonTextSize, dp2px(16f))
        mButtonBackground = a.getDrawable(R.styleable.PageStatusLayout_llButtonBackground)

        mEmptyResId = a.getResourceId(R.styleable.PageStatusLayout_llEmptyResId, R.layout.layout_page_status_empty)
        mLoadingResId = a.getResourceId(R.styleable.PageStatusLayout_llLoadingResId, R.layout.layout_page_status_loading)
        mErrorResId = a.getResourceId(R.styleable.PageStatusLayout_llErrorResId, R.layout.layout_page_status_error)
        a.recycle()
    }

    internal fun dp2px(dp: Float): Int {
        return (resources.displayMetrics.density * dp).toInt()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount == 0) {
            return
        }
        if (childCount > 1) {
            removeViews(1, childCount - 1)
        }
        val view = getChildAt(0)
        setContentView(view)
        showLoading()
    }

    private fun setContentView(view: View) {
        mContentId = view.id
        mLayouts.put(mContentId, view)
    }

    fun setLoading(@LayoutRes id: Int): PageStatusLayout {
        if (mLoadingResId != id) {
            remove(mLoadingResId)
            mLoadingResId = id
        }
        return this
    }

    fun setEmpty(@LayoutRes id: Int): PageStatusLayout {
        if (mEmptyResId != id) {
            remove(mEmptyResId)
            mEmptyResId = id
        }
        return this
    }

    fun setOnEmptyInflateListener(listener: OnInflateListener): PageStatusLayout {
        mOnEmptyInflateListener = listener
        if (mOnEmptyInflateListener != null && mLayouts.containsKey(mEmptyResId)) {
            listener.onInflate(mLayouts[mEmptyResId]!!)
        }
        return this
    }

    fun setOnErrorInflateListener(listener: OnInflateListener): PageStatusLayout {
        mOnErrorInflateListener = listener
        if (mOnErrorInflateListener != null && mLayouts.containsKey(mErrorResId)) {
            listener.onInflate(mLayouts[mErrorResId]!!)
        }
        return this
    }

    fun setEmptyImage(@DrawableRes resId: Int): PageStatusLayout {
        mEmptyImage = resId
        image(mEmptyResId, R.id.empty_image, mEmptyImage)
        return this
    }

    fun setEmptyText(value: String): PageStatusLayout {
        mEmptyText = value
        text(mEmptyResId, R.id.empty_text, mEmptyText)
        return this
    }

    fun setErrorImage(@DrawableRes resId: Int): PageStatusLayout {
        mErrorImage = resId
        image(mErrorResId, R.id.error_image, mErrorImage)
        return this
    }

    fun setErrorText(value: String): PageStatusLayout {
        mErrorText = value
        text(mErrorResId, R.id.error_text, mErrorText)
        return this
    }

    fun setRetryText(text: String): PageStatusLayout {
        mRetryText = text
        text(mErrorResId, R.id.retry_button, mRetryText)
        return this
    }

    fun setRetryListener(listener: View.OnClickListener): PageStatusLayout {
        mRetryListener = listener
        return this
    }

    fun showLoading() {
        show(mLoadingResId)
    }

    fun showEmpty() {
        show(mEmptyResId)
    }

    fun showError() {
        show(mErrorResId)
    }

    fun showContent() {
        show(mContentId)
    }

    private fun show(layoutId: Int) {
        for (view in mLayouts.values) {
            view.visibility = View.GONE
        }
        layout(layoutId).visibility = View.VISIBLE
    }

    private fun remove(layoutId: Int) {
        if (mLayouts.containsKey(layoutId)) {
            val vg = mLayouts.remove(layoutId)
            removeView(vg)
        }
    }

    private fun layout(layoutId: Int): View {
        if (mLayouts.containsKey(layoutId)) {
            return mLayouts[layoutId]!!
        }
        val layout = mInflater.inflate(layoutId, this, false)
        layout.visibility = View.GONE
        addView(layout)
        mLayouts.put(layoutId, layout)

        if (layoutId == mEmptyResId) {
            val img = layout.findViewById<View>(R.id.empty_image) as ImageView
            img.setImageResource(mEmptyImage)
            val view = layout.findViewById<View>(R.id.empty_text) as TextView
            view.text = mEmptyText
            view.setTextColor(mTextColor)
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize.toFloat())
            if (mOnEmptyInflateListener != null) {
                mOnEmptyInflateListener!!.onInflate(layout)
            }
        } else if (layoutId == mErrorResId) {
            val img = layout.findViewById<View>(R.id.error_image) as ImageView
            img.setImageResource(mErrorImage)
            val txt = layout.findViewById<View>(R.id.error_text) as TextView
            txt.text = mErrorText
            txt.setTextColor(mTextColor)
            txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize.toFloat())
            val btn = layout.findViewById<View>(R.id.retry_button) as TextView
            btn.text = mRetryText
            btn.setTextColor(mButtonTextColor)
            btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, mButtonTextSize.toFloat())
            btn.background = mButtonBackground
            btn.setOnClickListener(mRetryButtonClickListener)
            if (mOnErrorInflateListener != null) {
                mOnErrorInflateListener!!.onInflate(layout)
            }
        }
        return layout
    }

    private fun text(layoutId: Int, ctrlId: Int, value: CharSequence) {
        if (mLayouts.containsKey(layoutId)) {
            val view = mLayouts[layoutId]?.findViewById<View>(ctrlId) as TextView
            view.text = value
        }
    }

    private fun image(layoutId: Int, ctrlId: Int, resId: Int) {
        if (mLayouts.containsKey(layoutId)) {
            val view = mLayouts[layoutId]?.findViewById<View>(ctrlId) as ImageView
            view.setImageResource(resId)
        }
    }

    companion object {

        fun wrap(activity: Activity): PageStatusLayout {
            return wrap((activity.findViewById<View>(android.R.id.content) as ViewGroup).getChildAt(0))
        }

        fun wrap(fragment: Fragment): PageStatusLayout {
            return wrap(fragment.view)
        }

        fun wrap(view: View?): PageStatusLayout {
            if (view == null) {
                throw RuntimeException("content view can not be null")
            }
            val parent = view.parent as ViewGroup
            val lp = view.layoutParams
            val index = parent.indexOfChild(view)
            parent.removeView(view)

            val layout = PageStatusLayout(view.context)
            parent.addView(layout, index, lp)
            layout.addView(view)
            layout.setContentView(view)
            return layout
        }
    }
}
