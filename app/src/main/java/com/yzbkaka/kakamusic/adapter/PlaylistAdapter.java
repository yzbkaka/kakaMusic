package com.yzbkaka.kakamusic.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yzbkaka.kakamusic.R;
import com.yzbkaka.kakamusic.database.DBManager;
import com.yzbkaka.kakamusic.entity.MusicInfo;
import com.yzbkaka.kakamusic.entity.PlayListInfo;
import com.yzbkaka.kakamusic.service.MusicPlayerService;
import com.yzbkaka.kakamusic.util.Constant;
import com.yzbkaka.kakamusic.util.MyMusicUtil;

import java.util.List;


public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {

    private List<MusicInfo> musicInfoList;

    private Context context;

    private DBManager dbManager;

    private PlayListInfo playListInfo;

    private PlaylistAdapter.OnItemClickListener onItemClickListener ;


    static class ViewHolder extends RecyclerView.ViewHolder{
        View swipeContent;
        LinearLayout contentLayout;
        TextView musicIndex;
        TextView musicName;
        TextView musicSinger;
        ImageView menuImage;
        Button deleteButton;


        public ViewHolder(View itemView) {
            super(itemView);
            this.swipeContent = (View) itemView.findViewById(R.id.swipemenu_layout);
            this.contentLayout = (LinearLayout) itemView.findViewById(R.id.local_music_item_ll);
            this.musicName = (TextView) itemView.findViewById(R.id.local_music_name);
            this.musicIndex = (TextView) itemView.findViewById(R.id.local_index);
            this.musicSinger = (TextView) itemView.findViewById(R.id.local_music_singer);
            this.menuImage = (ImageView) itemView.findViewById(R.id.local_music_item_never_menu);
            this.deleteButton = (Button) itemView.findViewById(R.id.swip_delete_menu_btn);
        }
    }


    public PlaylistAdapter(Context context, PlayListInfo playListInfo,List<MusicInfo> musicInfoList) {
        this.context = context;
        this.playListInfo = playListInfo;
        this.dbManager = DBManager.getInstance(context);
        this.musicInfoList = musicInfoList;
    }


    @Override
    public int getItemCount() {
        return musicInfoList.size();
    }


    @Override
    public PlaylistAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.local_music_item,parent,false);
        PlaylistAdapter.ViewHolder viewHolder = new PlaylistAdapter.ViewHolder(view);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final PlaylistAdapter.ViewHolder holder, final int position) {
        final MusicInfo musicInfo = musicInfoList.get(position);
        holder.musicName.setText(musicInfo.getName());
        holder.musicIndex.setText("" + (position + 1));
        holder.musicSinger.setText(musicInfo.getSinger());
        if (musicInfo.getId() == MyMusicUtil.getIntSharedPreference(Constant.KEY_ID)){
            holder.musicName.setTextColor(context.getResources().getColor(R.color.colorAccent));
            holder.musicIndex.setTextColor(context.getResources().getColor(R.color.colorAccent));
            holder.musicSinger.setTextColor(context.getResources().getColor(R.color.colorAccent));
        }else {
            holder.musicName.setTextColor(context.getResources().getColor(R.color.grey700));
            holder.musicIndex.setTextColor(context.getResources().getColor(R.color.grey700));
            holder.musicSinger.setTextColor(context.getResources().getColor(R.color.grey700));
        }

        holder.contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = dbManager.getMusicPath(musicInfo.getId());
                Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                intent.putExtra(Constant.KEY_PATH, path);
                context.sendBroadcast(intent);
                MyMusicUtil.setIntSharedPreference(Constant.KEY_ID,musicInfo.getId());  //存储歌曲id
                MyMusicUtil.setIntSharedPreference(Constant.KEY_LIST,Constant.LIST_PLAYLIST);  //存储歌单播放
                MyMusicUtil.setIntSharedPreference(Constant.KEY_LIST_ID,playListInfo.getId());  //存储歌单id
                notifyDataSetChanged();
            }
        });

        holder.menuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onOpenMenuClick(position);
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onDeleteMenuClick(holder.swipeContent,holder.getAdapterPosition());
            }
        });
    }


    public void updateMusicInfoList(List<MusicInfo> musicInfoList) {
        this.musicInfoList.clear();
        this.musicInfoList.addAll(musicInfoList);
        notifyDataSetChanged();
    }


    public interface OnItemClickListener{
        void onOpenMenuClick(int position);
        void onDeleteMenuClick(View content,int position);
    }


    public void setOnItemClickListener(PlaylistAdapter.OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

}
