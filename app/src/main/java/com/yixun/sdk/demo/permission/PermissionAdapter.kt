package com.yixun.sdk.demo.permission

import android.databinding.ObservableList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.yixun.sdk.demo.databinding.ItemPermissionBinding
import com.yixun.sdk.demo.permission.data.Permission

/**
 * description: <一句话功能简述>
 * @author tt11
 * @time  2017/9/14.
 */
class PermissionAdapter(val persmissions: ObservableList<Permission>) : RecyclerView.Adapter<PermissionAdapter.VH>() {
    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): VH = VH(ItemPermissionBinding.inflate(
            LayoutInflater.from(parent?.context), parent, false))

    override fun getItemCount(): Int = persmissions.size

    override fun onBindViewHolder(holder: VH?, position: Int) {
        holder?.binding?.permission = persmissions[position]
    }

    class VH(val binding: ItemPermissionBinding) : RecyclerView.ViewHolder(binding.root)
}