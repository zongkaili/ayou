package com.yixun.sdk.demo.entity.viewbinder

import android.databinding.DataBindingUtil
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yixun.chime.widge.SpaceGridDecoration
import com.yixun.sdk.demo.entity.Scenics
import com.yixun.sdk.demo.R
import com.yixun.sdk.demo.databinding.ItemDiscoverScenicsBinding
import com.yixun.sdk.demo.entity.Scenics.Scenic
import me.drakeet.multitype.ItemViewBinder
import me.drakeet.multitype.MultiTypeAdapter

/**
* Created by zongkaili on 17/9/22.
*/
class DiscoverScenicsViewBinder : ItemViewBinder<Scenics, DiscoverScenicsViewBinder.ViewHolder>() {
    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        val binding: ItemDiscoverScenicsBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_discover_scenics, parent, false)
        binding.recyclerView.addItemDecoration(SpaceGridDecoration(10, 2))
        return ViewHolder(binding, binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, item: Scenics) {
        holder.bindData(item)
    }

    class ViewHolder(val binding: ItemDiscoverScenicsBinding, itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bindData(item: Scenics) {
            binding.recyclerView.isNestedScrollingEnabled = false
            val layoutManager: GridLayoutManager = GridLayoutManager(binding.root.context, 2)
            layoutManager.isAutoMeasureEnabled = true
            binding.recyclerView.layoutManager = layoutManager
            val adapter = MultiTypeAdapter()
            adapter.register(Scenic::class.java, ScenicViewBinder())
            binding.recyclerView.adapter = adapter
            val items: ArrayList<Scenic> = ArrayList()
            if (item.list.isNotEmpty()) {
                item.list.forEach {
                    items.add(it)
                }
            }
            adapter.items = items

            adapter.notifyDataSetChanged()

//            binding.setVariable(BR.viewModel, item)
            binding.executePendingBindings()

        }
    }
}