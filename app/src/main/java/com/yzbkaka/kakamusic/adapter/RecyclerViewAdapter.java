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
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.yzbkaka.kakamusic.R;
import com.yzbkaka.kakamusic.database.DBManager;
import com.yzbkaka.kakamusic.entity.MusicInfo;
import com.yzbkaka.kakamusic.service.MusicPlayerService;
import com.yzbkaka.kakamusic.util.Constant;
import com.yzbkaka.kakamusic.util.CustomAttrValueUtil;
import com.yzbkaka.kakamusic.util.MyMusicUtil;

import java.util.List;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> implements SectionIndexer{

    private List<MusicInfo> musicInfoList;

    private Context context;

    private DBManager dbManager;

    /**
     * 接口
     */
    private OnItemClickListener onItemClickListener ;


    class ViewHolder extends RecyclerView.ViewHolder{
        View swipeContent;
        LinearLayout contentLayout;
        TextView musicIndex;
        TextView musicName;
        TextView musicSinger;
        TextView letterIndex;
        ImageView menuImage;
        Button deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            this.swipeContent = (View) itemView.findViewById(R.id.swipemenu_layout);
            this.contentLayout = (LinearLayout) itemView.findViewById(R.id.local_music_item_ll);
            this.musicName = (TextView) itemView.findViewById(R.id.local_music_name);
            this.musicIndex = (TextView) itemView.findViewById(R.id.local_index);
            this.musicSinger = (TextView) itemView.findViewById(R.id.local_music_singer);
            this.letterIndex = (TextView) itemView.findViewById(R.id.indext_head_tv);
            this.menuImage = (ImageView) itemView.findViewById(R.id.local_music_item_never_menu);
            this.deleteButton = (Button) itemView.findViewById(R.id.swip_delete_menu_btn);
        }

    }


    public RecyclerViewAdapter(Context context, List<MusicInfo> musicInfoList) {
        this.context = context;
        this.musicInfoList = musicInfoList;
        this.dbManager = DBManager.getInstance(context);
    }


    /**
     * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的item的位置
     */
    public int getPositionForSection(int section) {
        for (int i = 0; i < getItemCount(); i++) {
            char firstChar = musicInfoList.get(i).getFirstLetter().charAt(0);  //得到首字母的acsii
            if (firstChar == section) {  //如果相等，则返回item的position
                return i;
            }
        }
        return -1;
    }


    @Override
    public Object[] getSections() {
        return new Object[0];
    }

    /**
     * 根据List的当前位置获取分类的首字母的ascii值
     */
    @Override
    public int getSectionForPosition(int position) {
        return musicInfoList.get(position).getFirstLetter().charAt(0);  //返回ascii值
    }


    @Override
    public int getItemCount() {
        return musicInfoList.size();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.local_music_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder,final int position) {
        final MusicInfo musicInfo = musicInfoList.get(position);
        holder.musicName.setText(musicInfo.getName());
        holder.musicIndex.setText("" + (position + 1));
        holder.musicSinger.setText(musicInfo.getSinger());

        int appBackground = CustomAttrValueUtil.getAttrColorValue(R.attr.colorAccent,0xFFFA7298,context);  //app的背景颜色
        int defaultTextColor = CustomAttrValueUtil.getAttrColorValue(R.attr.text_color,R.color.grey700,context);  //文字的颜色

        if (musicInfo.getId() == MyMusicUtil.getIntSharedPreference(Constant.KEY_ID)){  //如果是正在播放，则颜色改变，为选中状态
            holder.musicName.setTextColor(appBackground);
            holder.musicIndex.setTextColor(appBackground);
            holder.musicSinger.setTextColor(appBackground);
        }else {
            holder.musicName.setTextColor(defaultTextColor);
            holder.musicIndex.setTextColor(context.getResources().getColor(R.color.grey700));
            holder.musicSinger.setTextColor(context.getResources().getColor(R.color.grey700));
        }

        //显示分类的字母
        int section = getSectionForPosition(position);  //得到当前item的首字母的acsii值
        int firstPosition = getPositionForSection(section);  //根据acsii值获得第一次出现该acsii值的位置
        if (firstPosition == position){  //如果第一次出现则进行字母的添加
            holder.letterIndex.setVisibility(View.VISIBLE);
            holder.letterIndex.setText(""+musicInfo.getFirstLetter());
        }else{  //如果之前出现过则直接放在后面
            holder.letterIndex.setVisibility(View.GONE);
        }

        holder.contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = dbManager.getMusicPath(musicInfo.getId());
                Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                intent.putExtra(Constant.KEY_PATH, path);
                context.sendBroadcast(intent);
                MyMusicUtil.setIntSharedPreference(Constant.KEY_ID,musicInfo.getId());  //存储当前正在播放的音乐
                notifyDataSetChanged();
                if (onItemClickListener != null){
                    onItemClickListener.onContentClick(position);
                }
            }
        });

        holder.menuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null){
                    onItemClickListener.onOpenMenuClick(position);
                }
            }
        });

        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null){
                    onItemClickListener.onDeleteMenuClick(holder.swipeContent,holder.getAdapterPosition());
                }
            }
        });
    }


    /**
     * 更新音乐列表
     */
    public void updateMusicInfoList(List<MusicInfo> musicInfoList) {
        this.musicInfoList.clear();
        this.musicInfoList.addAll(musicInfoList);
        notifyDataSetChanged();
    }


    public interface OnItemClickListener{
        void onOpenMenuClick(int position);
        void onDeleteMenuClick(View content,int position);
        void onContentClick(int position);
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener ;
    }
}
