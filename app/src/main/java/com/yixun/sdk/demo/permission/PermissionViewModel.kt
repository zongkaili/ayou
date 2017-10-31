package com.yixun.sdk.demo.permission

import android.Manifest.permission.*
import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.content.pm.PackageManager
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import android.support.v4.content.ContextCompat
import com.yixun.sdk.demo.permission.data.Permission

/**
 * description: <一句话功能简述>
 * @author tt11
 * @time  2017/9/14.
 */
class PermissionViewModel(application: Application) : AndroidViewModel(application) {
    //    <uses-permission android:name="android.permission.WAKE_LOCK" />
//    <uses-permission android:name="android.permission.INTERNET" />
//    <uses-permission android:name="android.permission.CAMERA" />
//    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
//    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
//    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
//    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
//    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    var permissions: ObservableList<Permission> = ObservableArrayList()
    lateinit var navigator: Navigatoer
    fun start() {
        checkPermissions()
    }

    private fun checkPermissions() {
        if (!checkPermission(CAMERA))
            permissions.add(Permission("摄像头权限", "扫描AR内容需使用手机摄像头。", CAMERA))
//        if (!checkPermission(READ_PHONE_STATE))
//            permissions.add(Permission("电话权限", "用于电话呼叫和安全识别码判断。", READ_PHONE_STATE))
        if (!checkPermission(READ_PHONE_STATE))
            permissions.add(Permission("存储器权限", "用于在手机存储器上保存AR内容。", WRITE_EXTERNAL_STORAGE))

    }

    private fun checkPermission(permission: String): Boolean = ContextCompat.checkSelfPermission(getApplication(), permission) == PackageManager.PERMISSION_GRANTED
    fun requestPermission() {
        val maniArr = arrayOfNulls<String>(permissions.size)
        permissions.forEach {
            maniArr[permissions.indexOf(it)] = it.manifastPermission
        }
        navigator.requestManifestPermissions(maniArr)
    }
}


