/**
 * Copyright © 2013成都理想境界科技有限公司. All rights reserved.
 * 项目名称: Idealsee-AR2
 * 类名称: PicturePagerActivity
 * 类描述:
 * 创建人: ly
 * 创建时间: 2013-12-25 下午4:10:29
 * 修改人:
 * 修改时间:
 * 备注:
 *
 * @version
 */

package com.yixun.sdk.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.yixun.sdk.R;
import com.yixun.sdk.server.ISARHttpClient;
import com.yixun.sdk.server.ISARHttpRequest;
import com.yixun.sdk.util.ISARBitmapUtil;
import com.yixun.sdk.util.ISARConstants;
import com.yixun.sdk.util.ISARNetUtil;
import com.yixun.sdk.util.ISARStringUtil;
import com.yixun.sdk.util.ISARThreadPool;
import com.yixun.sdk.util.ISARTipsUtil;
import com.yixun.sdk.util.Logger;
import com.yixun.sdk.widget.ISARImageTouchView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * PicturePagerActivity.
 * ar picture presentation.
 *
 * @author ly
 */
public class ISARPicturePagerActivity extends ISARBaseActivity {
    private static final String TAG = ISARPicturePagerActivity.class.getSimpleName();
    private ViewPager mPicturePager;
    private PagerAdapter mPagerAdapter;
    private List<ImageView> mViews;// 视图数据
    private List<String> mPathList;
    private ProgressBar mLoadPgb;
    private boolean mIsLoadImage = false;
    private TextView mIndexTv;
    private ImageView mSaveBtn;
    private ImageView mCloseBtn;
    private int mCurrIndex = 0;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.LOGD(TAG + " onCreate");
        mContext = getApplicationContext();
        setContentView(R.layout.act_picture);
        String url = getIntent().getStringExtra("pic_json");
        initWidget();
        loadData(url);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Logger.LOGD(TAG + " onNewIntent");
    }

    private void initWidget() {
        mIndexTv = (TextView) findViewById(R.id.tv_picture_index);
        mSaveBtn = (ImageView) findViewById(R.id.iv_picture_save_iv);
        mCloseBtn = (ImageView) findViewById(R.id.iv_picture_close_iv);
        mCloseBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mIsLoadImage) {
                    ISARPicturePagerActivity.this.finish();
                }
            }
        });

        mSaveBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mPathList != null && mPathList.size() > 0) {
                    File srcFile = new File(mPathList.get(mCurrIndex));
                    String fname = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DCIM/"
                            + ISARStringUtil.currentTimeToString() + ".png";
                    File dstFile = new File(fname);
                    try {
                        dstFile.createNewFile();
                        FileInputStream is = new FileInputStream(srcFile);
                        FileOutputStream os = new FileOutputStream(dstFile);
                        byte[] buf = new byte[1024 * 8];
                        int len = -1;
                        while ((len = is.read(buf)) != -1) {
                            os.write(buf, 0, len);
                        }
                        is.close();
                        os.close();
                        ISARTipsUtil.showLongToast(mContext, R.string.msg_save_img_success);
                    } catch (IOException e) {
                        e.printStackTrace();
                        ISARTipsUtil.showLongToast(mContext, R.string.msg_save_img_faild);
                    }
                    //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
                    // 扫描后才能进入到media数据库
                    MediaScannerConnection.scanFile(mContext, new String[]{fname}, null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    Logger.LOGD("PicturePageActivity onScanComplete path=" + path + ",uri="
                                            + uri.toString());
                                }
                            });
                }
            }
        });
        mPicturePager = (ViewPager) findViewById(R.id.vp_picture_pager);
        mPicturePager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                mCurrIndex = arg0;
                mIndexTv.setText(getString(R.string.view_picture_index, String.valueOf(arg0 + 1),
                        String.valueOf(mViews.size())));
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
        mLoadPgb = (ProgressBar) findViewById(R.id.pgb_picture_load);

        mPagerAdapter = new PagerAdapter() {

            @Override
            public boolean isViewFromObject(View arg0, Object arg1) {
                return arg0 == arg1;
            }

            @Override
            public int getCount() {
                return mViews.size();
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(mViews.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView(mViews.get(position), 0);// 添加页卡
                View view = mViews.get(position);
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mIsLoadImage) {
                            ISARPicturePagerActivity.this.finish();
                        }
                    }
                });
                return mViews.get(position);
            }
        };
    }

    private void loadData(final String json) {
        ISARThreadPool.INSTANCE.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    JSONArray jsonArray = new JSONArray(json);
                    int size = jsonArray.length();
                    mPathList = new ArrayList<>(size);
                    for (int i = 0; i < size; i++) {
                        String path = ISARConstants.APP_CACHE_DIRECTORY + File.separator + jsonArray.getString(i);
                        Logger.LOGD(TAG + " loadData jsonArray.getString(i)=" + jsonArray.getString(i));
                        ISARHttpRequest request = new ISARHttpRequest(ISARNetUtil.getUrlFromMD5(jsonArray.getString(i)));
                        request.setTargetPath(path);
                        try {
                            int status = ISARHttpClient.getInstance().downloadFile(request);
                            if (HttpURLConnection.HTTP_OK != status) {
                                Logger.LOGW(TAG + " download failed:" + request.getUrl());
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        File tmp = new File(path);
                        if (tmp.exists()) {
                            mPathList.add(path);
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLoadPgb.setVisibility(View.GONE);
                            initImage();
                            if (mViews != null) {
                                mPicturePager.setAdapter(mPagerAdapter);
                                mIndexTv.setText(getString(R.string.view_picture_index, String.valueOf(1),
                                        String.valueOf(mViews.size())));
                                mIndexTv.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initImage() {
        if (mPathList != null && mPathList.size() > 0) {
            mViews = new ArrayList<>(mPathList.size());
            for (String path : mPathList) {
                ISARImageTouchView imageView = new ISARImageTouchView(mContext);
                imageView.setBackgroundColor(Color.BLACK);
                Bitmap bm = ISARBitmapUtil.decodeFile(mContext, path);
                imageView.setImageBitmap(bm);
                imageView.setScaleType(ScaleType.FIT_CENTER);
                mViews.add(imageView);
            }
            mIsLoadImage = true;
        }
    }
}
