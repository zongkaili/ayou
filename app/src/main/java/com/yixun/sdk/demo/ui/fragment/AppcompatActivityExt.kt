package com.yixun.sdk.demo.ui.fragment

import android.app.Activity
import android.content.Intent
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar

/**
 * description: <AppcompatActivity扩展文件>

 * @author tt11
 * *
 * @time 2017/9/11.
</一句话功能简述> */
fun AppCompatActivity.getFragmentByTag(tag: String): Fragment? = supportFragmentManager.findFragmentByTag(tag)

fun AppCompatActivity.addFragment(id: Int, fragment: Fragment, tag: String) {
    supportFragmentManager.beginTransaction().add(id, fragment, tag).commit()
}

fun AppCompatActivity.showFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction().show(fragment).commit()
}

fun AppCompatActivity.hideFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction().hide(fragment).commit()
}

fun AppCompatActivity.nextActivity(cls: Class<out Activity>) {
    startActivity(Intent(this, cls))
}

fun AppCompatActivity.setupToolbar(toolbar: Toolbar) {
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
    supportActionBar?.setDisplayShowHomeEnabled(true)
}

fun AppCompatActivity.nextActivity(cls: Class<out Activity>, requestCode: Int) {
    startActivityForResult(Intent(this, cls), requestCode)
}


