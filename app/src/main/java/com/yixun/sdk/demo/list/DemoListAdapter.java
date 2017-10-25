package com.yixun.sdk.demo.list;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yixun.sdk.demo.R;
import com.yixun.sdk.model.ISARRandomInfo;
import com.yixun.sdk.util.ISARBitmapLoader;
import com.yixun.sdk.util.ISARDensityUtil;
import com.yixun.sdk.util.Logger;
import com.yixun.sdk.util.ISARStringUtil;

public class DemoListAdapter extends RecyclerView.Adapter<ViewHolder> {
    private static final String TAG = "DemoListAdapter";
    protected static final int MSG_LOAD_CONTENT_ICON = 0x01;
    private static final int MSG_LOAD_AUTHOR_ICON = 0x02;
    private Context mContext;
    protected LinkedList<ISARRandomInfo> mInfoList = new LinkedList<ISARRandomInfo>();
    protected LayoutInflater mInflater;
    private int mItemLayoutWidth;
    private int mItemLayoutHeight;
    private Drawable mDrawable;
    private Drawable mAuthorDrawable;

    private static final int TYPE_ITEM = 0; // 普通Item View
    private static final int TYPE_FOOTER = 1; // 顶部FootView
    // 上拉加载更多
    public static final int PULLUP_LOAD_MORE = 0;
    // 正在加载中
    public static final int LOADING_MORE = 1;
    // 上拉加载更多状态-默认为0
    private int load_more_status = 0;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_CONTENT_ICON:
                    MyViewHolder holder1 = (MyViewHolder) msg.obj;
                    if (msg.arg1 == holder1.mPos) {
                        holder1.mImg.setImageBitmap(holder1.mContentIconBmp);
                    }
                    break;
                case MSG_LOAD_AUTHOR_ICON:
                    MyViewHolder holder2 = (MyViewHolder) msg.obj;
                    Logger.LOGD(TAG + " mHandler audioicon");
                    if (msg.arg1 == holder2.mPos) {
                        holder2.mAuthorIv.setImageBitmap(holder2.mAuthorIconBmp);
                    }
                    break;
            }
        }
    };

    public interface OnItemClickLitener {
        void onItemClick(MyViewHolder holder, ISARRandomInfo randomInfo);

        void onLoadMoreclick();
    }

    private OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    public DemoListAdapter(Context context, int itemLayoutWidth, int itemLayoutHeight) {
        mContext = context;
        mItemLayoutWidth = itemLayoutWidth;
        mItemLayoutHeight = itemLayoutHeight;
        mInflater = LayoutInflater.from(context);
        mDrawable = context.getResources().getDrawable(R.drawable.list_loading);
        mAuthorDrawable = context.getResources().getDrawable(R.drawable.ic_launcher);
    }

    public static class MyViewHolder extends ViewHolder {
        public MyViewHolder(View arg0) {
            super(arg0);
        }

        RelativeLayout mItemLayout;
        public ImageView mImg;
        public TextView mTextView;
        public ImageView mAuthorIv;
        public Bitmap mContentIconBmp;
        public Bitmap mAuthorIconBmp;
        public int mPos;
    }

    /**
     * 底部FootView布局
     */
    public static class FootViewHolder extends ViewHolder {
        private TextView foot_view_item_tv;

        public FootViewHolder(View view) {
            super(view);
        }
    }

    @Override
    public int getItemCount() {
        return mInfoList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        // 最后一个item设置为footerView
        if (position + 1 == getItemCount()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // 进行判断显示类型，来创建返回不同的View
        if (TYPE_ITEM == viewType) {
            View view = mInflater.inflate(R.layout.frag_list_item, viewGroup, false);
            MyViewHolder viewHolder = new MyViewHolder(view);
            viewHolder.mItemLayout = (RelativeLayout) view.findViewById(R.id.rl_list_item);
            viewHolder.mImg = (ImageView) view.findViewById(R.id.iv_list_item);
            viewHolder.mTextView = (TextView) view.findViewById(R.id.tv_list_item_txt);
            viewHolder.mAuthorIv = (ImageView) view.findViewById(R.id.iv_list_item_author);
            return viewHolder;
        } else if (TYPE_FOOTER == viewType) {
            View foot_view = mInflater.inflate(R.layout.recycler_view_footer_item, viewGroup, false);
            // 这边可以做一些属性设置，甚至事件监听绑定
            // view.setBackgroundColor(Color.RED);
            FootViewHolder footViewHolder = new FootViewHolder(foot_view);
            footViewHolder.foot_view_item_tv = (TextView) foot_view.findViewById(R.id.foot_view_item_tv);
            footViewHolder.foot_view_item_tv.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (null != mOnItemClickLitener) {
                        mOnItemClickLitener.onLoadMoreclick();
                    }
                }
            });
            return footViewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        if (viewHolder instanceof MyViewHolder) {
            final MyViewHolder myHolder = (MyViewHolder) viewHolder;
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) myHolder.mImg.getLayoutParams();
            lp.width = mItemLayoutWidth;
            lp.height = mItemLayoutHeight;
            lp.leftMargin = ISARDensityUtil.dip2px(mContext, 1);
            lp.rightMargin = ISARDensityUtil.dip2px(mContext, 1);
            myHolder.mImg.setLayoutParams(lp);

            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) myHolder.mItemLayout.getLayoutParams();
            Log.d(TAG, "RecyclerView onBindViewHolder " + i + ",getItemCount()=" + getItemCount()
                    + ",params.rightMargin=" + params.rightMargin);
            params.leftMargin = ISARDensityUtil.dip2px(mContext, 10);
            params.rightMargin = ISARDensityUtil.dip2px(mContext, 10);
            myHolder.mItemLayout.setLayoutParams(params);

            // read from cache using md5(fileName), if not exist in cache, then
            // read from disk.
            myHolder.mPos = i;
            final ISARRandomInfo randomInfo = mInfoList.get(i);
            myHolder.mTextView.setText(randomInfo.getResourceTagTitle());
            myHolder.mAuthorIv.setImageDrawable(mAuthorDrawable);
            myHolder.mAuthorIv.setTag(myHolder);
            if (!randomInfo.isHasLoaded()) {
                myHolder.mImg.setImageDrawable(mDrawable);
            } else {
                myHolder.mImg.setImageBitmap(myHolder.mContentIconBmp);
            }
            loadAsyncImage(myHolder, randomInfo, i);
            loadAuthorIcon(myHolder, randomInfo.getEditorLogoUrl(), i);
            if (mOnItemClickLitener != null) {
                myHolder.itemView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickLitener.onItemClick(myHolder, randomInfo);
                    }
                });
            }
        } else if (viewHolder instanceof FootViewHolder) {
            FootViewHolder fHolder = (FootViewHolder) viewHolder;
            switch (load_more_status) {
                case PULLUP_LOAD_MORE:
                    fHolder.foot_view_item_tv.setText("加载更多...");
                    break;
                case LOADING_MORE:
                    fHolder.foot_view_item_tv.setText("正在加载更多...");
                    break;

                default:
                    break;
            }
        }
    }

    public boolean addItemLast(List<ISARRandomInfo> datas) {
        boolean result = false;
        for (ISARRandomInfo info : datas) {
            if (!mInfoList.contains(info)) {
                mInfoList.addLast(info);
                result = true;
            }
        }
        return result;
    }

    private void loadAsyncImage(final MyViewHolder holder, final ISARRandomInfo info, final int position) {
        if (holder.mPos == position) {
            Thread t = new Thread() {
                public void run() {
//                    final Bitmap bitmap = ISARImageLoader.getInstance(mContext).loadImage(info.getMediaSrc());
                    final Bitmap bitmap = ISARBitmapLoader.getInstance().loadBitmapByUrlOnHttp(mContext, info.getMediaSrc());
                    Log.d(TAG,
                            "loadAsyncImage url=" + info.getMediaSrc() + ",md5calc="
                                    + ISARStringUtil.getMD5(info.getMediaSrc()));
                    if (bitmap != null) {
                        holder.mContentIconBmp = bitmap;
                        info.setHasLoaded(true);
                        Message msg1 = mHandler.obtainMessage(MSG_LOAD_CONTENT_ICON, position, 0, holder);
                        mHandler.sendMessage(msg1);
                    }
                }
            };
            t.start();
        } else {
            Log.e(TAG, "loadAsyncImage skip a invalid action: ");
        }
    }

    private void loadAuthorIcon(final MyViewHolder holder, final String url, final int position) {
        if ("".equals(url) || null == url) {
            Logger.LOGD(TAG + " loadAuthorIcon no icon address");
            return;
        }
        if (holder.mPos == position) {
            Thread t = new Thread() {
                public void run() {
//                    final Bitmap bitmap = ISARImageLoader.getInstance(mContext).loadImage(url);
                    final Bitmap bitmap = ISARBitmapLoader.getInstance().loadBitmapByUrlOnHttp(mContext, url);
                    Logger.LOGD(TAG + " loadAuthorIcon url=" + url + ",md5calc=" + ISARStringUtil.getMD5(url));
                    if (bitmap != null) {
                        holder.mAuthorIconBmp = bitmap;
                        Message msg1 = mHandler.obtainMessage(MSG_LOAD_AUTHOR_ICON, position, 0, holder);
                        mHandler.sendMessage(msg1);
                    }
                }
            };
            t.start();
        } else {
            Logger.LOGE(TAG + " loadAuthorIcon skip a invalid action");
        }
    }

    /**
     * //上拉加载更多 PULLUP_LOAD_MORE=0; //正在加载中 LOADING_MORE=1; //加载完成已经没有更多数据了
     * NO_MORE_DATA=2;
     *
     * @param status
     */
    public void changeMoreStatus(int status) {
        load_more_status = status;
        notifyDataSetChanged();
    }
}
