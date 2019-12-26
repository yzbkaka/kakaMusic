package com.yzbkaka.kakamusic.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yzbkaka.kakamusic.R;
import com.yzbkaka.kakamusic.database.DBManager;
import com.yzbkaka.kakamusic.entity.SingerInfo;

import java.util.List;


public class SingerAdapter extends RecyclerView.Adapter<SingerAdapter.ViewHolder>{

    private List<SingerInfo> singerInfoList;

    private Context context;

    private DBManager dbManager;

    private OnItemClickListener onItemClickListener;


    public SingerAdapter(Context context, List<SingerInfo> singerInfoList) {
        this.context = context;
        this.singerInfoList = singerInfoList;
        this.dbManager = DBManager.getInstance(context);
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        View swipeContent;
        LinearLayout contentLayout;
        ImageView singerImage;
        TextView singerName;
        TextView count;

        public ViewHolder(View itemView) {
            super(itemView);
            this.swipeContent = (View) itemView.findViewById(R.id.model_swipemenu_layout);
            this.contentLayout = (LinearLayout) itemView.findViewById(R.id.model_music_item_ll);
            this.singerImage = (ImageView) itemView.findViewById(R.id.model_head_iv);
            this.singerName = (TextView) itemView.findViewById(R.id.model_item_name);
            this.count = (TextView) itemView.findViewById(R.id.model_music_count);
        }

    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.local_model_rv_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        SingerInfo singer = singerInfoList.get(position);
        holder.singerName.setText(singer.getName());
        holder.singerImage.setImageResource(R.drawable.singer);
        holder.count.setText(singer.getCount()+"首");

        holder.contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onContentClick(holder.contentLayout,position);
            }
        });
    }


    @Override
    public int getItemCount() {
        return singerInfoList.size();
    }


    public void update(List<SingerInfo> singerInfoList) {
        this.singerInfoList.clear();
        this.singerInfoList.addAll(singerInfoList);
        notifyDataSetChanged();
    }


    public interface OnItemClickListener{
        void onContentClick(View content,int position);
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener ;
    }
}
