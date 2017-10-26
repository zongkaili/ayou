package com.yixun.sdk.demo.entity.viewbinder

import android.content.Intent
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.yixun.sdk.demo.BR
import com.yixun.sdk.demo.R
import com.yixun.sdk.demo.databinding.ItemActNewsinfoBinding
import com.yixun.sdk.demo.entity.NewsInfo
import com.yixun.sdk.demo.ui.activity.ActDetailActivity
import me.drakeet.multitype.ItemViewBinder

/**
* Created by zongkaili on 17/9/22.
*/
class ActNewsInfoViewBinder : ItemViewBinder<NewsInfo, ActNewsInfoViewBinder.ViewHolder>() {
    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        val binding: ItemActNewsinfoBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_act_newsinfo, parent, false)
        return ViewHolder(binding, binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, item: NewsInfo) {
        holder.bindData(item)
    }

    class ViewHolder(val binding: ItemActNewsinfoBinding, itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bindData(item: NewsInfo) {
            Glide.with(binding.ivImage).load(item.imgUrl).into(binding.ivImage)
            binding.root.setOnClickListener {
                binding.root.context.startActivity(Intent(binding.root.context, ActDetailActivity::class.java))
            }
            binding.setVariable(BR.newsInfo, item)
            binding.executePendingBindings()
        }
    }
}