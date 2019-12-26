package com.yzbkaka.kakamusic.fragment;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;

import com.yzbkaka.kakamusic.R;
import com.yzbkaka.kakamusic.activity.PlayActivity;
import com.yzbkaka.kakamusic.database.DBManager;
import com.yzbkaka.kakamusic.receiver.PlayerManagerReceiver;
import com.yzbkaka.kakamusic.service.MusicPlayerService;
import com.yzbkaka.kakamusic.util.Constant;
import com.yzbkaka.kakamusic.util.MyMusicUtil;
import com.yzbkaka.kakamusic.view.PlayingPopWindow;

import static com.yzbkaka.kakamusic.receiver.PlayerManagerReceiver.status;


/**
 * 在每个活动页面的下方都需要出现播放器
 */
public class PlayBarFragment extends Fragment {

    public static final String ACTION_UPDATE_UI_PlAYBAR = "com.lijunyan.blackmusic.fragment.PlayBarFragment:action_update_ui_broad_cast";

    public static final String REMOTE_CANCEL_BROADCAST = "com.yzbkaka.kakamusic.remote_cancel";

    public static final String REMOTE_NEXT_BROADCAST = "com,yzbkaka,kakamusic,remote_next";

    public static final String REMOTE = "remote";

    public static final int REMOTE_CANCEL = 1;

    public static final int REMOTE_NEXT = 2;

    private LinearLayout playBarLayout;

    private ImageView playImage;

    private SeekBar seekBar;

    private ImageView nextImage;

    private ImageView menuImage;

    private TextView musicNameText;

    private TextView singerNameText;

    private HomeReceiver homeReceiver;

    private DBManager dbManager;

    private View view;

    private Context context;

    /**
     * 状态播放栏
     */
    private RemoteViews remoteViews;

    /**
     * remoteView管理器
     */
    private NotificationManager notificationManager;

    private Notification notification;

    private RemoteCancelReceiver remoteCancelReceiver;

    private RemoteNextReceiver remoteNextReceiver;

    /**
     * 单例
     */
    public static synchronized PlayBarFragment newInstance(){
        return new PlayBarFragment();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbManager = DBManager.getInstance(getActivity());
        register();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_playbar,container,false);
        playBarLayout = (LinearLayout) view.findViewById(R.id.home_activity_playbar_ll);
        seekBar = (SeekBar) view.findViewById(R.id.home_seekbar);
        playImage = (ImageView)view.findViewById(R.id.play_iv);
        menuImage = (ImageView)view.findViewById(R.id.play_menu_iv);
        nextImage = (ImageView)view.findViewById(R.id.next_iv);
        musicNameText = (TextView) view.findViewById(R.id.home_music_name_tv);
        singerNameText = (TextView) view.findViewById(R.id.home_singer_name_tv);

        initRemoteView();
        setMusicName();
        initPlayImage();
        setFragmentBackground();

        playBarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PlayActivity.class);
                startActivity(intent);
            }
        });

        playImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int musicId = MyMusicUtil.getIntSharedPreference(Constant.KEY_ID);
                if (musicId == -1 || musicId == 0) {
                    Intent intent = new Intent(Constant.MP_FILTER);
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
                    getActivity().sendBroadcast(intent);
                    return;
                }
                if (status == Constant.STATUS_PAUSE) {  //暂停状态
                    Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    intent.putExtra(Constant.COMMAND,Constant.COMMAND_PLAY);
                    getActivity().sendBroadcast(intent);
                }else if (status == Constant.STATUS_PLAY) {  //播放状态
                    Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_PAUSE);
                    getActivity().sendBroadcast(intent);
                }else {   //为停止状态时发送播放命令，并发送将要播放歌曲的路径
                    String path = dbManager.getMusicPath(musicId);
                    Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                    intent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
                    intent.putExtra(Constant.KEY_PATH, path);
                    getActivity().sendBroadcast(intent);
                }
            }
        });

        nextImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyMusicUtil.playNextMusic(getActivity());
            }
        });

        menuImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopFormBottom();
            }
        });

        return view;
    }


    /**
     * 设置远程RemoteViews
     */
    public void initRemoteView(){
        int musicId = MyMusicUtil.getIntSharedPreference(Constant.KEY_ID);
        remoteViews = new RemoteViews(context.getPackageName(),R.layout.remote_layout);
        PendingIntent playPendingIntent = null;
        if (musicId == -1 || musicId == 0) {  //无播放歌曲
            Intent playIntent = new Intent(Constant.MP_FILTER);
            playIntent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
            playPendingIntent = PendingIntent.getBroadcast(context,0,playIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        }
        if (status == Constant.STATUS_PAUSE) {  //暂停状态
            Intent playIntent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
            playIntent.putExtra(Constant.COMMAND,Constant.COMMAND_PLAY);
            playPendingIntent = PendingIntent.getBroadcast(context,0,playIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        }else if (status == Constant.STATUS_PLAY) {  //播放状态
            Intent playIntent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
            playIntent.putExtra(Constant.COMMAND, Constant.COMMAND_PAUSE);
            playPendingIntent = PendingIntent.getBroadcast(context,0,playIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        }else {
            String path = dbManager.getMusicPath(musicId);
            Intent playIntent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
            playIntent.putExtra(Constant.COMMAND, Constant.COMMAND_PLAY);
            playIntent.putExtra(Constant.KEY_PATH, path);
            playPendingIntent = PendingIntent.getBroadcast(context,0,playIntent,PendingIntent.FLAG_CANCEL_CURRENT);
        }

        //下一首
        Intent nextIntent = new Intent(REMOTE_NEXT_BROADCAST);
        nextIntent.putExtra(REMOTE,REMOTE_NEXT);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context,0,nextIntent,PendingIntent.FLAG_CANCEL_CURRENT);

        //取消
        Intent cancelIntent = new Intent(REMOTE_CANCEL_BROADCAST);
        cancelIntent.putExtra(REMOTE,REMOTE_CANCEL);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(context,0,cancelIntent,PendingIntent.FLAG_CANCEL_CURRENT);

        //设置点击事件
        remoteViews.setOnClickPendingIntent(R.id.remote_play_iv,playPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.remote_next_iv,nextPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.remote_cancel,cancelPendingIntent);

        notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.add)
                .setOngoing(true)
                .setContent(remoteViews)
                .build();
        notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1,notification);
    }


    public void showPopFormBottom() {
        PlayingPopWindow playingPopWindow = new PlayingPopWindow(getActivity());
//      设置Popupwindow显示位置（从底部弹出）
        playingPopWindow.showAtLocation(view, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
        WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
        //当弹出Popupwindow时，背景变半透明
        params.alpha=0.7f;
        getActivity().getWindow().setAttributes(params);

        //设置Popupwindow关闭监听，当Popupwindow关闭，背景恢复1f
        playingPopWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                WindowManager.LayoutParams params = getActivity().getWindow().getAttributes();
                params.alpha=1f;
                getActivity().getWindow().setAttributes(params);
            }
        });

    }


    /**
     * 设置bar的颜色
     */
    public void setFragmentBackground(){
        //获取播放控制栏颜色
        int defaultColor = 0xFFFFFF;
        int[] attrsArray = {R.attr.play_bar_color };
        TypedArray typedArray = context.obtainStyledAttributes(attrsArray);
        int color = typedArray.getColor(0, defaultColor);
        typedArray.recycle();
        playBarLayout.setBackgroundColor(color);
    }


    /**
     * 设置bar上音乐的名字
     */
    private void setMusicName(){
        int musicId = MyMusicUtil.getIntSharedPreference(Constant.KEY_ID);
        if (musicId == -1){
            musicNameText.setText("kakaMusic");
            singerNameText.setText("yzbkaka");
        }else{
            musicNameText.setText(dbManager.getMusicInfo(musicId).get(1));
            singerNameText.setText(dbManager.getMusicInfo(musicId).get(2));
            remoteViews.setTextViewText(R.id.remote_music_name_tv,dbManager.getMusicInfo(musicId).get(1));
            remoteViews.setTextViewText(R.id.remote_singer_name_tv,dbManager.getMusicInfo(musicId).get(2));
        }
    }


    /**
     * 设置bar上的图像
     */
    private void initPlayImage(){
        int status = PlayerManagerReceiver.status;
        switch (status) {
            case Constant.STATUS_STOP:
                playImage.setSelected(false);
                remoteViews.setImageViewResource(R.id.remote_play_iv,R.drawable.play);
                break;
            case Constant.STATUS_PLAY:
                playImage.setSelected(true);
                remoteViews.setImageViewResource(R.id.remote_play_iv,R.drawable.pause);
                break;
            case Constant.STATUS_PAUSE:
                playImage.setSelected(false);
                remoteViews.setImageViewResource(R.id.remote_play_iv,R.drawable.play);
                break;
            case Constant.STATUS_RUN:
                playImage.setSelected(true);
                remoteViews.setImageViewResource(R.id.remote_play_iv,R.drawable.pause);
                break;
        }
    }


    /**
     * 注册广播接收器
     */
    private void register() {
        homeReceiver = new HomeReceiver();
        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction(ACTION_UPDATE_UI_PlAYBAR);
        getActivity().registerReceiver(homeReceiver, intentFilter1);

        remoteCancelReceiver = new RemoteCancelReceiver();
        IntentFilter intentFilter2 = new IntentFilter();
        intentFilter2.addAction(REMOTE_CANCEL_BROADCAST);
        getActivity().registerReceiver(remoteCancelReceiver,intentFilter2);

        remoteNextReceiver = new RemoteNextReceiver();
        IntentFilter intentFilter3 = new IntentFilter();
        intentFilter3.addAction(REMOTE_NEXT_BROADCAST);
        getActivity().registerReceiver(remoteNextReceiver,intentFilter3);
    }


    private void unRegister() {
        if (homeReceiver != null) {
            getActivity().unregisterReceiver(homeReceiver);
            getActivity().unregisterReceiver(remoteCancelReceiver);
            getActivity().unregisterReceiver(remoteNextReceiver);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegister();
    }


    /**
     * 接收广播：ACTION_UPDATE_UI_PlAYBAR
     */
    class HomeReceiver extends BroadcastReceiver {
        int status;
        int duration;
        int current;

        @Override
        public void onReceive(Context context, Intent intent) {
            setMusicName();
            status = intent.getIntExtra(Constant.STATUS,0);
            current = intent.getIntExtra(Constant.KEY_CURRENT,0);
            duration = intent.getIntExtra(Constant.KEY_DURATION,100);
            Log.d("status", String.valueOf(status));
            switch (status){  //根据回调过来的状态来设置View
                case Constant.STATUS_STOP:
                    playImage.setSelected(false);
                    remoteViews.setImageViewResource(R.id.remote_play_iv,R.drawable.play);
                    seekBar.setProgress(0);
                    break;
                case Constant.STATUS_PLAY:
                    playImage.setSelected(true);
                    remoteViews.setImageViewResource(R.id.remote_play_iv,R.drawable.pause);
                    break;
                case Constant.STATUS_PAUSE:
                    playImage.setSelected(false);
                    remoteViews.setImageViewResource(R.id.remote_play_iv,R.drawable.play);
                    break;
                case Constant.STATUS_RUN:
                    playImage.setSelected(true);
                    remoteViews.setImageViewResource(R.id.remote_play_iv,R.drawable.pause);
                    seekBar.setMax(duration);
                    seekBar.setProgress(current);
                    break;
                default:
                    break;
            }

            notificationManager.notify(1,notification);  //必须要进行更新，否则view无法改变
            initRemoteView();  //必须要再次调用
        }
    }


    /**
     * 接收广播：REMOTE_CANCEL_BROADCAST
     */
    class RemoteCancelReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            notificationManager.cancel(1);  //消失
        }
    }


    /**
     * 接收广播：REMOTE_NEXT_BROADCAST
     */
    class RemoteNextReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            MyMusicUtil.playNextMusic(getActivity());
        }
    }
}
