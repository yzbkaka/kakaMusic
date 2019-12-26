package com.yzbkaka.kakamusic.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.yzbkaka.kakamusic.R;
import com.yzbkaka.kakamusic.database.DBManager;
import com.yzbkaka.kakamusic.entity.MusicInfo;
import com.yzbkaka.kakamusic.entity.PlayListInfo;
import com.yzbkaka.kakamusic.service.MusicPlayerService;
import com.yzbkaka.kakamusic.util.Constant;
import com.yzbkaka.kakamusic.util.MyMusicUtil;

import java.io.File;


public class MusicPopMenuWindow extends PopupWindow{

    private View view;

    private Activity activity;

    private TextView nameText;

    private LinearLayout addLayout;

    private LinearLayout loveLayout;

    private LinearLayout deleteLayout;

    private LinearLayout cancelLayout;

    private MusicInfo musicInfo;

    private PlayListInfo playListInfo;

    private int witchActivity = Constant.ACTIVITY_LOCAL;

    private View parentView;


    public MusicPopMenuWindow(Activity activity, MusicInfo musicInfo,View parentView,int witchActivity) {
        super(activity);
        this.activity = activity;
        this.musicInfo = musicInfo;
        this.parentView = parentView;
        this.witchActivity = witchActivity;
        initView();
    }


   /* public MusicPopMenuWindow(Activity activity, MusicInfo musicInfo,View parentView,int witchActivity,PlayListInfo playListInfo) {
        super(activity);
        this.activity = activity;
        this.musicInfo = musicInfo;
        this.parentView = parentView;
        this.witchActivity = witchActivity;
        this.playListInfo = playListInfo;
        initView();
    }*/


    private void initView(){
        this.view = LayoutInflater.from(activity).inflate(R.layout.pop_window_menu, null);
        this.setContentView(this.view);  // 设置视图

        this.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);  //设置弹出窗体的宽和高,不设置显示不出来
        this.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);

        this.setFocusable(true);  //设置弹出窗体可点击
        this.setOutsideTouchable(true);  //设置外部可点击

        this.setBackgroundDrawable(activity.getResources().getDrawable(R.color.colorWhite));  //设置弹出窗体的背景
        this.setAnimationStyle(R.style.pop_window_animation);  //设置弹出窗体显示时的动画，从底部向上弹出

        this.view.setOnTouchListener(new View.OnTouchListener() {  //添加OnTouchListener监听判断获取触屏位置，如果在选择框外面则销毁弹出框
            public boolean onTouch(View v, MotionEvent event) {
                int height = view.getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {  //在框的范围以外
                        dismiss();
                    }
                }
                return true;
            }
        });


        nameText = (TextView)view.findViewById(R.id.popwin_name_tv);
        addLayout = (LinearLayout) view.findViewById(R.id.popwin_add_rl);
        loveLayout = (LinearLayout) view.findViewById(R.id.popwin_love_ll);
        deleteLayout = (LinearLayout) view.findViewById(R.id.popwin_delete_ll);
        cancelLayout = (LinearLayout) view.findViewById(R.id.popwin_cancel_ll);

        nameText.setText("歌曲： " + this.musicInfo.getName());

        addLayout.setOnClickListener(new View.OnClickListener() {  //添加到
            public void onClick(View v) {
                //该弹消失，展示另一个弹框
                dismiss();
                AddPlaylistWindow addPlaylistWindow = new AddPlaylistWindow(activity,musicInfo);
                addPlaylistWindow.showAtLocation(parentView, Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
                WindowManager.LayoutParams params = activity.getWindow().getAttributes();

                params.alpha=0.7f;  //当弹出Popupwindow时，背景变半透明
                activity.getWindow().setAttributes(params);

                //设置Popupwindow关闭监听，当Popupwindow关闭，背景恢复1f
                addPlaylistWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        WindowManager.LayoutParams params = activity.getWindow().getAttributes();
                        params.alpha=1f;
                        activity.getWindow().setAttributes(params);
                    }
                });
            }
        });

        loveLayout.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                MyMusicUtil.setMusicMylove(activity,musicInfo.getId());
                dismiss();
                View view = LayoutInflater.from(activity).inflate(R.layout.my_love_toast,null);
                Toast toast = new Toast(activity);
                toast.setView(view);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();
            }
        });

        deleteLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                deleteOperate(musicInfo,activity);
                dismiss();
            }
        });

        cancelLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    /**
     * 删除弹框
     */
    public void deleteOperate(MusicInfo musicInfo,final Context context){
        final int deleteMusicId = musicInfo.getId();
        final int musicId = MyMusicUtil.getIntSharedPreference(Constant.KEY_ID);  //当前播放音乐Id
        final DBManager dbManager = DBManager.getInstance(context);
        final String path = dbManager.getMusicPath(deleteMusicId);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_delete_file,null);
        final CheckBox deleteFile = (CheckBox)view.findViewById(R.id.dialog_delete_cb);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog dialog = builder.create();

        builder.setView(view);

        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (deleteFile.isChecked()){  //选定同时删除文件
                    File file = new File(path);
                    if (file.exists()) {
                        deleteMediaDB(file,context);
                        dbManager.deleteMusic(deleteMusicId);
                    }
                    if (deleteMusicId == musicId){  //删除的是当前播放的音乐
                        Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                        intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);  //播放音乐暂停
                        context.sendBroadcast(intent);
                        MyMusicUtil.setIntSharedPreference(Constant.KEY_ID,dbManager.getFirstId(Constant.LIST_ALLMUSIC));
                    }
                }else {
                    //从列表移除
                    if (witchActivity == Constant.ACTIVITY_MYLIST){  //歌单
                        dbManager.removeMusicFromPlaylist(deleteMusicId,playListInfo.getId());
                    }else {
                        dbManager.removeMusic(deleteMusicId,witchActivity);
                    }

                    if (deleteMusicId == musicId) {  //移除的是当前播放的音乐
                        Intent intent = new Intent(MusicPlayerService.PLAYER_MANAGER_ACTION);
                        intent.putExtra(Constant.COMMAND, Constant.COMMAND_STOP);
                        context.sendBroadcast(intent);
                    }
                }
                if(onDeleteUpdateListener != null){
                    onDeleteUpdateListener.onDeleteUpdate();
                }
                dialog.dismiss();

            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * 从本地删除
     */
    public static void deleteMediaDB(File file,Context context){
        String filePath = file.getPath();
        int res = context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.Media.DATA + "= \"" + filePath+"\"", null);
    }


    private OnDeleteUpdateListener onDeleteUpdateListener;


    public void setOnDeleteUpdateListener(OnDeleteUpdateListener onDeleteUpdateListener){
        this.onDeleteUpdateListener = onDeleteUpdateListener;
    }


    public interface OnDeleteUpdateListener {
        void onDeleteUpdate();
    }
}
