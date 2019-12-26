package com.yzbkaka.kakamusic.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yzbkaka.kakamusic.R;
import com.yzbkaka.kakamusic.database.DBManager;
import com.yzbkaka.kakamusic.entity.MusicInfo;
import com.yzbkaka.kakamusic.service.MusicPlayerService;
import com.yzbkaka.kakamusic.util.Constant;
import com.yzbkaka.kakamusic.util.MyMusicUtil;

import java.util.List;


public class PlayingPopWindow extends PopupWindow{

    private View view;

    private Activity activity;

    private TextView countText;

    private RelativeLayout closeLayout;

    private RecyclerView recyclerView;

    private PlayingPopWindowAdapter playingPopWindowAdapter;

    private List<MusicInfo> musicInfoList;

    private DBManager dbManager;


    public PlayingPopWindow(Activity activity) {
        super(activity);
        this.activity = activity;
        dbManager = DBManager.getInstance(activity);
        musicInfoList = MyMusicUtil.getCurrentPlayList(activity);
        initView();
    }


    private void initView(){
        this.view = LayoutInflater.from(activity).inflate(R.layout.playbar_menu_window, null);
        this.setContentView(this.view);
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        int height = (int)(size.y * 0.5);
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        this.setHeight(height);

        this.setFocusable(true);
        this.setOutsideTouchable(true);

        // 设置弹出窗体的背景
        this.setBackgroundDrawable(activity.getResources().getDrawable(R.color.colorWhite));
        // 设置弹出窗体显示时的动画，从底部向上弹出
        this.setAnimationStyle(R.style.pop_window_animation);

        // 添加OnTouchListener监听判断获取触屏位置，如果在选择框外面则销毁弹出框
        this.view.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                int height = view.getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

        recyclerView = (RecyclerView) view.findViewById(R.id.playing_list_rv);
        playingPopWindowAdapter = new PlayingPopWindowAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(playingPopWindowAdapter);

        closeLayout = (RelativeLayout) view.findViewById(R.id.playing_list_close_rv);
        countText = (TextView)view.findViewById(R.id.playing_list_count_tv);
        countText.setText("("+musicInfoList.size()+")");

        closeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }


    private class PlayingPopWindowAdapter extends RecyclerView.Adapter<PlayingPopWindowAdapter.ViewHolder> {

        class ViewHolder extends RecyclerView.ViewHolder{
            LinearLayout contentLayout;
            TextView nameText;
            TextView singerText;

            public ViewHolder(View itemView) {
                super(itemView);
                this.contentLayout = (LinearLayout) itemView.findViewById(R.id.palybar_list_item_ll);
                this.nameText = (TextView) itemView.findViewById(R.id.palybar_list_item_name_tv);
                this.singerText = (TextView) itemView.findViewById(R.id.palybar_list_item_singer_tv);
            }
        }


        @Override
        public int getItemCount() {
            return musicInfoList.size();
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(activity).inflate(R.layout.item_playbar_rv_list,parent,false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }


        @Override
        public void onBindViewHolder(final ViewHolder holder,final int position) {
            final MusicInfo musicInfo = musicInfoList.get(position);
            holder.nameText.setText(musicInfo.getName());
            holder.singerText.setText("-"+musicInfo.getSinger());

            if (musicInfo.getId() == MyMusicUtil.getIntSharedPreference(Constant.KEY_ID)){
                holder.nameText.setTextColor(activity.getResources().getColor(R.color.colorAccent));
                holder.singerText.setTextColor(activity.getResources().getColor(R.color.colorAccent));
            }else {
                holder.nameText.setTextColor(activity.getResources().getColor(R.color.grey700));
                holder.singerText.setTextColor(activity.getResources().getColor(R.color.grey500));
            }

            holder.contentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path = dbManager.getMusicPath(musicInfo.getId());
                    Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                    intent.putExtra(Constant.KEY_PATH, path);
                    activity.sendBroadcast(intent);
                    MyMusicUtil.setIntSharedPreference(Constant.KEY_ID,musicInfo.getId());
                    notifyDataSetChanged();
                }
            });
        }
    }
}
