package com.yzbkaka.kakamusic.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import com.yzbkaka.kakamusic.R;
import com.yzbkaka.kakamusic.database.DBManager;
import com.yzbkaka.kakamusic.entity.MusicInfo;
import com.yzbkaka.kakamusic.receiver.PlayerManagerReceiver;
import com.yzbkaka.kakamusic.service.MusicPlayerService;
import com.yzbkaka.kakamusic.util.Constant;
import com.yzbkaka.kakamusic.util.MyMusicUtil;
import com.yzbkaka.kakamusic.view.MusicPopMenuWindow;
import com.yzbkaka.kakamusic.view.SideBar;
import com.mcxtzhang.swipemenulib.SwipeMenuLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModelActivity extends PlayBarBaseActivity {

    public static final String KEY_TITLE = "key_title";

    public static final String KEY_TYPE = "key_type";

    public static final String KEY_PATH = "key_path";

    /**
     * 传过来的类型
     */
    public static final String SINGER_TYPE = "singer_type";

    public static final String ALBUM_TYPE = "album_type";

    public static final String FOLDER_TYPE = "folder_type";

    private Toolbar toolbar;

    private String type;

    private String title;

    private RecyclerView recyclerView;

    private ModelAdapter adapter;

    private SideBar sideBar;

    private RelativeLayout playModeLayout;

    private ImageView playModeImage;

    private TextView playModeText;

    private DBManager dbManager;

    private List<MusicInfo> musicInfoList;

    private UpdateReceiver mReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_model);
        dbManager = DBManager.getInstance(this);
        title = getIntent().getStringExtra(KEY_TITLE);
        type = getIntent().getStringExtra(KEY_TYPE);
        toolbar = (Toolbar) findViewById(R.id.model_music_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(title);
        }
        musicInfoList = new ArrayList<>();
        init();
        updateView();
        register();
    }


    @Override
    protected void onResume() {
        super.onResume();
        initDefaultPlayModeView();
    }


    private void init() {
        recyclerView = (RecyclerView) findViewById(R.id.model_recycler_view);
        adapter = new ModelAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onOpenMenuClick(int position) {
                MusicInfo musicInfo = musicInfoList.get(position);
                showPopFormBottom(musicInfo);
            }

            @Override
            public void onDeleteMenuClick(View swipeView, int position) {
                MusicInfo musicInfo = musicInfoList.get(position);
                final int curId = musicInfo.getId();
                final int musicId = MyMusicUtil.getIntSharedPreference(Constant.KEY_ID);
                //从列表移除
                dbManager.removeMusic(musicInfo.getId(), Constant.ACTIVITY_LOCAL);
                if (curId == musicId) {
                    //移除的是当前播放的音乐
                    Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
                    sendBroadcast(intent);
                }
                adapter.notifyItemRemoved(position);//推荐用这个
                updateView();
                //如果删除时，不使用mAdapter.notifyItemRemoved(pos)，则删除没有动画效果，
                //且如果想让侧滑菜单同时关闭，需要同时调用 ((CstSwipeDelMenu) holder.itemView).quickClose();
                ((SwipeMenuLayout) swipeView).quickClose();
            }

        });

        // 当点击外部空白处时，关闭正在展开的侧滑菜单
        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    SwipeMenuLayout viewCache = SwipeMenuLayout.getViewCache();
                    if (null != viewCache) {
                        viewCache.smoothClose();
                    }
                }
                return false;
            }
        });


        sideBar = (SideBar) findViewById(R.id.model_music_siderbar);
        sideBar.setOnListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String letter) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(letter.charAt(0));
                if (position != -1) {
                    recyclerView.smoothScrollToPosition(position);
                }
            }
        });


        playModeLayout = (RelativeLayout) findViewById(R.id.model_music_playmode_rl);
        playModeImage = (ImageView) findViewById(R.id.model_music_playmode_iv);
        playModeText = (TextView) findViewById(R.id.model_music_playmode_tv);

        initDefaultPlayModeView();

        //  顺序 --> 随机-- > 单曲
        playModeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int playMode = MyMusicUtil.getIntSharedPreference(Constant.KEY_MODE);
                switch (playMode) {
                    case Constant.PLAYMODE_SEQUENCE:
                        playModeText.setText(Constant.PLAYMODE_RANDOM_TEXT);
                        MyMusicUtil.setIntSharedPreference(Constant.KEY_MODE, Constant.PLAYMODE_RANDOM);
                        break;
                    case Constant.PLAYMODE_RANDOM:
                        playModeText.setText(Constant.PLAYMODE_SINGLE_REPEAT_TEXT);
                        MyMusicUtil.setIntSharedPreference(Constant.KEY_MODE, Constant.PLAYMODE_SINGLE_REPEAT);
                        break;
                    case Constant.PLAYMODE_SINGLE_REPEAT:
                        playModeText.setText(Constant.PLAYMODE_SEQUENCE_TEXT);
                        MyMusicUtil.setIntSharedPreference(Constant.KEY_MODE, Constant.PLAYMODE_SEQUENCE);
                        break;
                }
                initPlayMode();
            }
        });
    }


    private void initDefaultPlayModeView() {
        int playMode = MyMusicUtil.getIntSharedPreference(Constant.KEY_MODE);
        switch (playMode) {
            case Constant.PLAYMODE_SEQUENCE:
                playModeText.setText(Constant.PLAYMODE_SEQUENCE_TEXT);
                break;
            case Constant.PLAYMODE_RANDOM:
                playModeText.setText(Constant.PLAYMODE_RANDOM_TEXT);
                break;
            case Constant.PLAYMODE_SINGLE_REPEAT:
                playModeText.setText(Constant.PLAYMODE_SINGLE_REPEAT_TEXT);
                break;
        }
        initPlayMode();
    }


    private void initPlayMode() {
        int playMode = MyMusicUtil.getIntSharedPreference(Constant.KEY_MODE);
        if (playMode == -1) {
            playMode = 0;
        }
        playModeImage.setImageLevel(playMode);
    }


    public void updateView() {
        musicInfoList.clear();
        if (type.equals(SINGER_TYPE)) {  //如果是歌手
            musicInfoList.addAll(dbManager.getMusicListBySinger(title));
        } else if (type.equals(ALBUM_TYPE)) {  //如果是专辑
            musicInfoList.addAll(dbManager.getMusicListByAlbum(title));
        } else if (type.equals(FOLDER_TYPE)) {  //如果是文件夹
            musicInfoList.addAll(dbManager.getMusicListByFolder(getIntent().getStringExtra(KEY_PATH)));
        }

        Collections.sort(musicInfoList);
        adapter.notifyDataSetChanged();

        if (musicInfoList.size() == 0) {
            sideBar.setVisibility(View.GONE);
            playModeLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        } else {
            sideBar.setVisibility(View.VISIBLE);
            playModeLayout.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }


    public void showPopFormBottom(MusicInfo musicInfo) {
        MusicPopMenuWindow menuPopupWindow = new MusicPopMenuWindow(ModelActivity.this, musicInfo, findViewById(R.id.activity_model),Constant.ACTIVITY_LOCAL);
//      设置Popupwindow显示位置（从底部弹出）
        menuPopupWindow.showAtLocation(findViewById(R.id.activity_model), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams params = ModelActivity.this.getWindow().getAttributes();
        //当弹出Popupwindow时，背景变半透明
        params.alpha = 0.7f;
        getWindow().setAttributes(params);

        //设置Popupwindow关闭监听，当Popupwindow关闭，背景恢复1f
        menuPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getWindow().getAttributes();
                params.alpha = 1f;
                getWindow().setAttributes(params);
            }
        });
        menuPopupWindow.setOnDeleteUpdateListener(new MusicPopMenuWindow.OnDeleteUpdateListener() {
            @Override
            public void onDeleteUpdate() {
                updateView();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegister();
    }

    private void register() {
        try {
            if (mReceiver != null) {
                this.unRegister();
            }
            mReceiver = new UpdateReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(PlayerManagerReceiver.ACTION_UPDATE_UI_ADAPTER);
            this.registerReceiver(mReceiver, intentFilter);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void unRegister() {
        try {
            if (mReceiver != null) {
                this.unregisterReceiver(mReceiver);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private class UpdateReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            adapter.notifyDataSetChanged();
            Toast.makeText(context, "ModeActivity!!!", Toast.LENGTH_SHORT).show();
        }
    }


    private interface OnItemClickListener {
        void onOpenMenuClick(int position);

        void onDeleteMenuClick(View content, int position);
    }

    /**
     * 内部类，适配器
     * 和RecyclerAdapter相似
     */
    private class ModelAdapter extends RecyclerView.Adapter<ModelAdapter.ViewHolder> implements SectionIndexer {

        private OnItemClickListener onItemClickListener;

        public ModelAdapter() {}

        class ViewHolder extends RecyclerView.ViewHolder {
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


        @Override
        public Object[] getSections() {
            return new Object[0];
        }

        /**
         * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的item的位置
         */
        public int getPositionForSection(int section) {
            for (int i = 0; i < getItemCount(); i++) {
                char firstChar = musicInfoList.get(i).getFirstLetter().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * 根据ListView的当前位置获取分类的首字母的char ascii值
         */
        @Override
        public int getSectionForPosition(int position) {
            return musicInfoList.get(position).getFirstLetter().charAt(0);
        }

        @Override
        public int getItemCount() {
            return musicInfoList.size();
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(ModelActivity.this).inflate(R.layout.local_music_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final MusicInfo musicInfo = musicInfoList.get(position);
            holder.musicName.setText(musicInfo.getName());
            holder.musicIndex.setText("" + (position + 1));
            holder.musicSinger.setText(musicInfo.getSinger());

            //获取主题颜色
            int defaultColor = 0xFFFA7298;
            int[] attrsArray = {R.attr.colorAccent};
            TypedArray typedArray = obtainStyledAttributes(attrsArray);
            int appbg = typedArray.getColor(0, defaultColor);
            typedArray.recycle();

            int[] attrs = {R.attr.text_color};
            TypedArray typed = obtainStyledAttributes(attrs);
            int defaultTvColor = typed.getColor(0, getResources().getColor(R.color.grey700));
            typedArray.recycle();

            if (musicInfo.getId() == MyMusicUtil.getIntSharedPreference(Constant.KEY_ID)){
                holder.musicName.setTextColor(appbg);
                holder.musicIndex.setTextColor(appbg);
                holder.musicSinger.setTextColor(appbg);
            }else {
                holder.musicName.setTextColor(defaultTvColor);
                holder.musicIndex.setTextColor(getResources().getColor(R.color.grey700));
                holder.musicSinger.setTextColor(getResources().getColor(R.color.grey700));
            }

            int section = getSectionForPosition(position);
            int firstPosition = getPositionForSection(section);
            if (firstPosition == position) {
                holder.letterIndex.setVisibility(View.VISIBLE);
                holder.letterIndex.setText("" + musicInfo.getFirstLetter());
            } else {
                holder.letterIndex.setVisibility(View.GONE);
            }

            holder.contentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String path = dbManager.getMusicPath(musicInfo.getId());
                    Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                    intent.putExtra(Constant.KEY_PATH, path);
                    sendBroadcast(intent);
                    MyMusicUtil.setIntSharedPreference(Constant.KEY_ID, musicInfo.getId());
                    if (type.equals(SINGER_TYPE)) {
                        MyMusicUtil.setIntSharedPreference(Constant.KEY_LIST, Constant.LIST_SINGER);
                        MyMusicUtil.setStringSharedPreference(Constant.KEY_LIST_ID, title);
                    } else if (type.equals(ALBUM_TYPE)) {
                        MyMusicUtil.setIntSharedPreference(Constant.KEY_LIST, Constant.LIST_ALBUM);
                        MyMusicUtil.setStringSharedPreference(Constant.KEY_LIST_ID, title);
                    } else if (type.equals(FOLDER_TYPE)) {
                        MyMusicUtil.setIntSharedPreference(Constant.KEY_LIST, Constant.LIST_FOLDER);
                        MyMusicUtil.setStringSharedPreference(Constant.KEY_LIST_ID, getIntent().getStringExtra(KEY_PATH));
                    }
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
                    onItemClickListener.onDeleteMenuClick(holder.swipeContent, holder.getAdapterPosition());
                }
            });
        }


        public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
            this.onItemClickListener = onItemClickListener;
        }
    }
}
