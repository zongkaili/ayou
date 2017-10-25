package com.yixun.chime.entity.viewbinder

import android.content.Context
import android.content.Intent
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.yixun.chime.entity.BannerAd
import com.yixun.sdk.demo.R
import com.yixun.sdk.demo.databinding.ItemHomeBannerBinding

import com.youth.banner.loader.ImageLoaderInterface

import me.drakeet.multitype.ItemViewBinder

/**
 * Created by zongkaili on 17/10/25.
 */
class HomeBannerViewBinder : ItemViewBinder<BannerAd, HomeBannerViewBinder.ViewHolder>() {
    override fun onCreateViewHolder(inflater: LayoutInflater, parent: ViewGroup): ViewHolder {
        val binding: ItemHomeBannerBinding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_home_banner, parent, false)
        return ViewHolder(binding, binding.root)
    }

    override fun onBindViewHolder(holder: ViewHolder, item: BannerAd) {
        holder.bindData(item)
    }

    class ViewHolder(val binding: ItemHomeBannerBinding, itemView: View?) : RecyclerView.ViewHolder(itemView) {
        fun bindData(item: BannerAd) {
            binding.homeBannerFeed.setDelayTime(3000)
            binding.homeBannerFeed.setImageLoader(object : ImageLoaderInterface<View> {
                override fun displayImage(context: Context, path: Any, imageView: View) {
                    Glide.with(context.applicationContext)
                            .load(path)
                            .into(imageView as ImageView)
                }

                override fun createImageView(context: Context): View {
                    return ImageView(context)
                }
            })
            binding.homeBannerFeed.setImages(item.urlList).start()
            binding.homeBannerFeed.setOnBannerListener {
//                Log.d("banner", " click : " + it)
//                binding.root.context.startActivity(Intent(binding.root.context, MyProductsActivity::class.java))
            }

//            binding.setVariable(BR.viewModel, item)
            binding.executePendingBindings()
        }
    }
}
