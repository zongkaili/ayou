package com.yixun.sdk.demo.entity.viewbinder

import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yixun.sdk.demo.R
import com.yixun.sdk.demo.databinding.ItemDiscoverCategoryBinding
import com.yixun.sdk.demo.entity.Category
import me.drakeet.multitype.ItemViewBinder

/**
* Created by zongkaili on 17/9/22.
*/
class DiscoverCategoryViewBinder : ItemViewBinder<Category, DiscoverCategoryViewBinder.ViewHolder>() {
    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        val binding: ItemDiscoverCategoryBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_discover_category, parent, false)
        return ViewHolder(binding, binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, item: Category) {
        holder.bindData(item)
    }

    class ViewHolder(val binding: ItemDiscoverCategoryBinding, itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bindData(item: Category) {
        }
    }
}