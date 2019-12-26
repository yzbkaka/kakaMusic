package com.yzbkaka.kakamusic.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.yzbkaka.kakamusic.R;
import com.yzbkaka.kakamusic.util.HttpUtil;
import com.yzbkaka.kakamusic.util.MyApplication;
import com.yzbkaka.kakamusic.util.MyMusicUtil;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;


public class WelcomeActivity extends BaseActivity {

    /**
     * 获取权限的标识
     */
    private static final int PERMISSON_REQUESTCODE = 1;

    private SharedPreferences sharepreferences;

    private SharedPreferences.Editor editor;

    private ImageView bingImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        bingImage = (ImageView)findViewById(R.id.welcome_bing_iv);
        loadBingPic();
        sharepreferences=this.getSharedPreferences("check", MODE_PRIVATE);
        editor=sharepreferences.edit();
        initPermission();

    }


    private void loadBingPic(){
        HttpUtil.sendOkHttpRequest(HttpUtil.requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    final String bingPic = response.body().string();
                    MyMusicUtil.setBingSharedPreference(bingPic);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(MyApplication.getContext()).load(bingPic).into(bingImage);
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }


    /**
     * 设置定时
     */
    private void checkSkip() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                startMusicActivity();
            }
        };
        timer.schedule(task, 1000);
    }


    private void startMusicActivity() {
        Intent intent = new Intent();
        intent.setClass(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * 获取权限
     */
    private void initPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            checkSkip();
            return;
        }
        if (ContextCompat.checkSelfPermission(WelcomeActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(WelcomeActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSON_REQUESTCODE);
        }else {
            checkSkip();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSON_REQUESTCODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSkip();
                } else {
                    Toast.makeText(this, "必须同意所有权限才能使用APP", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }
}
