package com.yixun.sdk.demo.entity.viewbinder

import android.content.Intent
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.yixun.sdk.demo.R
import com.yixun.sdk.demo.databinding.ItemScenicBinding
import com.yixun.sdk.demo.entity.Scenics
import com.yixun.sdk.demo.ui.activity.ScenicDetailActivity
import me.drakeet.multitype.ItemViewBinder

/**
* Created by zongkaili on 17/9/22.
*/
class ScenicViewBinder : ItemViewBinder<Scenics.Scenic, ScenicViewBinder.ViewHolder>() {
    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        val binding: ItemScenicBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_scenic, parent, false)
        return ViewHolder(binding, binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, item: Scenics.Scenic) {
        holder.bindData(item)
    }

    class ViewHolder(val binding: ItemScenicBinding, itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bindData(item: Scenics.Scenic) {
//            Glide.with(binding.pic).load(item.imgUrl).into(binding.pic)
//            binding.executePendingBindings()
            binding.pic.setOnClickListener {
                binding.root.context.startActivity(Intent(binding.root.context, ScenicDetailActivity::class.java))
            }
        }
    }
}