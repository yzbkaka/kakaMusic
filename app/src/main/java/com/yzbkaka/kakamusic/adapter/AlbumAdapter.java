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
import com.yzbkaka.kakamusic.entity.AlbumInfo;

import java.util.List;


public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder>{

    private List<AlbumInfo> albumInfoList;

    private Context context;

    private DBManager dbManager;

    private AlbumAdapter.OnItemClickListener onItemClickListener;


    public AlbumAdapter(Context context, List<AlbumInfo> albumInfoList) {
        this.context = context;
        this.albumInfoList = albumInfoList;
        this.dbManager = DBManager.getInstance(context);
    }

    static class ViewHolder extends RecyclerView.ViewHolder{
        View swipeContent;
        LinearLayout contentLayout;
        ImageView albumImage;
        TextView albumName;
        TextView count;


        public ViewHolder(View itemView) {
            super(itemView);
            this.swipeContent = (View) itemView.findViewById(R.id.model_swipemenu_layout);
            this.contentLayout = (LinearLayout) itemView.findViewById(R.id.model_music_item_ll);
            this.albumImage = (ImageView) itemView.findViewById(R.id.model_head_iv);
            this.albumName = (TextView) itemView.findViewById(R.id.model_item_name);
            this.count = (TextView) itemView.findViewById(R.id.model_music_count);
        }
    }


    @Override
    public AlbumAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.local_model_rv_item,parent,false);
        AlbumAdapter.ViewHolder viewHolder = new AlbumAdapter.ViewHolder(view);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final AlbumAdapter.ViewHolder holder, final int position) {
        AlbumInfo album = albumInfoList.get(position);
        holder.albumImage.setImageResource(R.drawable.album);
        holder.albumName.setText(album.getName());
        holder.count.setText(album.getCount()+"首 "+album.getSinger());

        holder.contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onContentClick(holder.contentLayout,position);
            }
        });

    }


    @Override
    public int getItemCount() {
        return albumInfoList.size();
    }


    public void update(List<AlbumInfo> albumInfoList) {
        this.albumInfoList.clear();
        this.albumInfoList.addAll(albumInfoList);
        notifyDataSetChanged();
    }


    public interface OnItemClickListener{
        void onContentClick(View content,int position);
    }


    public void setOnItemClickListener(AlbumAdapter.OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener ;
    }
}
