package com.yixun.sdk.demo.permission

import android.databinding.BindingAdapter
import android.databinding.ObservableList
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.widget.TextView
import com.yixun.sdk.demo.permission.data.Permission

/**
 * description: <一句话功能简述>
 * @author tt11
 * @time  2017/9/14.
 */
object PermissionBindings {
    @Suppress("DEPRECATION")
    @BindingAdapter("app:permission", "app:permissionDescription", requireAll = true)
    @JvmStatic
    fun setPermissionDescription(textView: TextView, permission: String, permissionDescription: String) {
        textView.text = Html.fromHtml("●<font color=\"#f70000\">  $permission</font>：$permissionDescription")
    }

    @BindingAdapter("app:permissions")
    @JvmStatic
    fun setPermissions(recyclerView: RecyclerView, permission: ObservableList<Permission>) {
        recyclerView.layoutManager = LinearLayoutManager(recyclerView.context)
        recyclerView.adapter = PermissionAdapter(permission)
    }
}