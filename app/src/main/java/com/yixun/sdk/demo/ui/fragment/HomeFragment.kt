package com.yixun.sdk.demo.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.yixun.sdk.demo.R
import com.yixun.sdk.demo.databinding.FragmentHomeBinding
import com.youth.banner.loader.ImageLoaderInterface

/**
 * Created by zongkaili on 2017/10/25.
 */
class HomeFragment : BaseBingingFragment<FragmentHomeBinding>() {
    private lateinit var topBannerList: List<Any>

    override fun onCreateView(mBinding: FragmentHomeBinding, savedInstanceState: Bundle?) {
        bindFeeds(mBinding)
    }

    override fun getLayoutId(): Int = R.layout.fragment_home
    var mTitle: String = ""

    companion object {
        fun newInstance(title: String): HomeFragment {
            val fragment = HomeFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            fragment.mTitle = title
            return fragment
        }
    }

    private fun bindFeeds(mBinding: FragmentHomeBinding) {
        topBannerList = ArrayList<Any>()
        topBannerList = listOf(
                R.drawable.pic_home_banner,
                "http://img1.gtimg.com/sports/pics/hv1/105/196/1592/103569885.jpg",
                "http://img.weiot.net/portal/201604/14/041002yw70140k3wf89uwv.jpg",
                "http://pic.qiantucdn.com/58pic/12/72/06/28n58PICBWj.jpg",
                "http://olpic.tgbusdata.cn/uploads/oltgbuspic/20121010/new/1349830225_39bc7559.jpg")
        mBinding.homeBannerFeed.setDelayTime(3000)
        mBinding.homeBannerFeed.setImageLoader(object : ImageLoaderInterface<View> {
            override fun displayImage(context: Context, path: Any, imageView: View) {
                Glide.with(context.applicationContext)
                        .load(path)
                        .into(imageView as ImageView)
            }

            override fun createImageView(context: Context): View {
                return ImageView(context)
            }
        })
        mBinding.homeBannerFeed.setImages(topBannerList).start()
        mBinding.homeBannerFeed.setOnBannerListener {
            //                Log.d("banner", " click : " + it)
//                binding.root.context.startActivity(Intent(binding.root.context, MyProductsActivity::class.java))
        }
    }
}