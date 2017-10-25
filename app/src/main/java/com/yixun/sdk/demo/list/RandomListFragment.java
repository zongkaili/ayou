package com.yixun.sdk.demo.list;

import java.util.List;
import java.util.Locale;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yixun.sdk.demo.R;
import com.yixun.sdk.demo.SDKDemoActivity;
import com.yixun.sdk.demo.list.DemoListAdapter.MyViewHolder;
import com.yixun.sdk.demo.list.DemoListAdapter.OnItemClickLitener;
import com.yixun.sdk.model.ISARRandomInfo;

/**
 * @author hn This is tips layout for showing how to use it.
 */
public class RandomListFragment extends Fragment {
    private static final String TAG = "[RandomListFragment]";
    private static final int MSG_LOAD_MORE_UPDATE = 1001;
    private static final int MSG_PULL_DOWN_UPDATE = 1002;
    private static final int DATA_COUNT_ONCE = 10; // 每次从服务器取数据条数
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeLayout;
    private TextView mReturTv;
    private DemoListAdapter mAdapter;
    private Context mContext;
    private LinearLayoutManager mRVLayoutManager;
    private Locale mLocale;
    // 每次从服务器取数据的位置
    private int mStartIndex = 0;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage what=" + msg.what);
            switch (msg.what) {
            case MSG_LOAD_MORE_UPDATE:
                List<ISARRandomInfo> randomList = (List<ISARRandomInfo>) msg.obj;
                if (null != randomList && randomList.size() > 0) {
                    mStartIndex += randomList.size();
                    if (mAdapter.addItemLast(randomList)) {
                        mAdapter.notifyDataSetChanged();
                    }
                }
                mAdapter.changeMoreStatus(DemoListAdapter.PULLUP_LOAD_MORE);
                break;
            case MSG_PULL_DOWN_UPDATE:
                List<ISARRandomInfo> randomList2 = (List<ISARRandomInfo>) msg.obj;
                if (null != randomList2 && randomList2.size() > 0) {
                    if (mAdapter.addItemLast(randomList2)) {
                        mAdapter.notifyDataSetChanged();
                    }
                }
                mSwipeLayout.setRefreshing(false);
                break;
            default:
                break;
            }
        }
    };

    OnItemClickLitener mOnItemClickLitener = new OnItemClickLitener() {

        @Override
        public void onItemClick(MyViewHolder holder, ISARRandomInfo randomInfo) {
            ((SDKDemoActivity) getActivity()).doStartScanARFromRandom(randomInfo);
            Log.d(TAG, " onItemClick");
        }

        @Override
        public void onLoadMoreclick() {
            Log.d(TAG, " onLoadMoreclick");
            mAdapter.changeMoreStatus(DemoListAdapter.LOADING_MORE);
            loadData(MSG_LOAD_MORE_UPDATE);
        }
    };

    private OnRefreshListener mSwipeOnRefreshListener = new OnRefreshListener() {
        @Override
        public void onRefresh() {
            loadData(MSG_PULL_DOWN_UPDATE);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Configuration config = getResources().getConfiguration();
        mLocale = config.locale;
        mContext = getActivity().getApplicationContext();
        View view = inflater.inflate(R.layout.frag_list, null);
        initWidget(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData(MSG_LOAD_MORE_UPDATE);
    }

    private void initWidget(View rootView) {
        mReturTv = (TextView) rootView.findViewById(R.id.tv_close_list);
        mReturTv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((SDKDemoActivity) getActivity()).doMainPage();
            }
        });
        // init list adapter
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.id_recyclerview);
        mRVLayoutManager = new LinearLayoutManager(mContext);
        mRVLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mRVLayoutManager);
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        mAdapter = new DemoListAdapter(mContext, dm.widthPixels, dm.heightPixels / 3);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnItemClickLitener(mOnItemClickLitener);

        mSwipeLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.demo_swiperefreshlayout);
        // 设置刷新时动画的颜色，可以设置4个
        mSwipeLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light,
                android.R.color.holo_orange_light, android.R.color.holo_green_light);
        mSwipeLayout.setProgressViewOffset(false, 0,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()));
        mSwipeLayout.setOnRefreshListener(mSwipeOnRefreshListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
//        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
//        if (hidden) {
//            if (null != getActivity()) {
//                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//            }
//        } else {
//            if (null != getActivity()) {
//                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
//                        WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//            }
//        }
    }

    /**
     * 加载数据
     * 
     */
    private void loadData(final int type) {
        Thread t = new Thread() {
            public void run() {
                Log.d(TAG, " loadData " + mLocale.getLanguage() + ",type=" + type);

                List<ISARRandomInfo> randomList = null;
                if (MSG_LOAD_MORE_UPDATE == type) {
                    // add search in this page
                    randomList = ((SDKDemoActivity) getActivity()).doGetstartGetRandomList(mStartIndex,
                            DATA_COUNT_ONCE, mLocale.getLanguage(), false);
                    Log.d(TAG, "loadData more randomList.size=" + ((null == randomList) ? null : randomList.size()));
                    Message msg = mHandler.obtainMessage(MSG_LOAD_MORE_UPDATE, randomList);
                    msg.sendToTarget();
                } else if (MSG_PULL_DOWN_UPDATE == type) {
                    randomList = ((SDKDemoActivity) getActivity()).doGetstartGetRandomList(0, DATA_COUNT_ONCE,
                            mLocale.getLanguage(), false);
                    Log.d(TAG, "loadData pull randomList.size=" + ((null == randomList) ? null : randomList.size()));
                    Message msg = mHandler.obtainMessage(MSG_PULL_DOWN_UPDATE, randomList);
                    msg.sendToTarget();
                }
            };
        };
        t.start();
    }
}
