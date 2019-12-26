package com.yzbkaka.kakamusic.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.yzbkaka.kakamusic.R;
import com.yzbkaka.kakamusic.util.MyMusicUtil;


/**
 * 进行全局主题的改变
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTheme();
    }

    /**
     * 动态设置主题
     * 全局修改app主题颜色
     */
    private void initTheme(){
        int themeId = MyMusicUtil.getTheme(BaseActivity.this);
        switch (themeId){
            default:
            case 0:
                setTheme(R.style.BiLiPinkTheme);
                break;
            case 1:
                setTheme(R.style.ZhiHuBlueTheme);
                break;
            case 2:
                setTheme(R.style.KuAnGreenTheme);
                break;
            case 3:
                setTheme(R.style.CloudRedTheme);
                break;
            case 4:
                setTheme(R.style.TengLuoPurpleTheme);
                break;
            case 5:
                setTheme(R.style.SeaBlueTheme);
                break;
            case 6:
                setTheme(R.style.GrassGreenTheme);
                break;
            case 7:
                setTheme(R.style.CoffeeBrownTheme);
                break;
            case 8:
                setTheme(R.style.LemonOrangeTheme);
                break;
            case 9:
                setTheme(R.style.StartSkyGrayTheme);
                break;
            case 10:
                setTheme(R.style.NightModeTheme);
                break;
        }
    }
}
