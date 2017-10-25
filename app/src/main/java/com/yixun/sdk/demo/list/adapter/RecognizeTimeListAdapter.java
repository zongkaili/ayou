package com.yixun.sdk.demo.list.adapter;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.yixun.sdk.demo.R;

public class RecognizeTimeListAdapter extends RecyclerView.Adapter<ViewHolder> {
    private Context mContext;
    private List<String> mContents;
    public RecognizeTimeListAdapter(Context context, List<String> contents){
        mContext = context;
        mContents = contents;
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_recognize_time_list,parent,false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MyViewHolder viewHolder = (MyViewHolder)holder;
        viewHolder.mTxtContent.setText(mContents.get(position));
    }

    @Override
    public int getItemCount() {
        return mContents.size();
    }

    private class MyViewHolder extends RecyclerView.ViewHolder{
        private TextView mTxtContent;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTxtContent = (TextView) itemView.findViewById(R.id.tv_content);
        }

    }
}
