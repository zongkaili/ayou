package com.yixun.sdk.demo.ui.fragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.yixun.sdk.demo.R
import com.yixun.sdk.demo.SDKDemoActivity
import com.yixun.sdk.demo.databinding.FragmentHomeBinding
import com.youth.banner.loader.ImageLoaderInterface

/**
 * Created by zongkaili on 2017/10/25.
 */
class HomeFragment : BaseBingingFragment<FragmentHomeBinding>() {
    private lateinit var topBannerList: List<Any>

    override fun onCreateView(mBinding: FragmentHomeBinding, savedInstanceState: Bundle?) {
        bindFeeds(mBinding)
        bindEvent()
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
        val TAG: String = HomeFragment::class.java.simpleName
    }

    private fun bindFeeds(mBinding: FragmentHomeBinding) {
        topBannerList = ArrayList<Any>()
        topBannerList = listOf(
                R.drawable.pic_home_banner,
                "http://b.hiphotos.baidu.com/image/pic/item/dcc451da81cb39dbbf279a97d9160924aa18300f.jpg",
                "http://c.hiphotos.baidu.com/image/pic/item/cb8065380cd791232d6306b4a4345982b3b7806b.jpg",
                "http://imgsrc.baidu.com/image/c0%3Dshijue1%2C0%2C0%2C294%2C40/sign=c55331232c9759ee5e5d6888da922963/3c6d55fbb2fb4316a08b2f542aa4462309f7d30c.jpg")
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

    private fun bindEvent() {
        mBinding.ivAR.setOnClickListener {
            val intent = Intent(context, SDKDemoActivity::class.java)
            intent.putExtra("from_main", true)
            startActivity(intent)
            activity.overridePendingTransition(R.anim.right_in, R.anim.left_out)
        }
    }
}