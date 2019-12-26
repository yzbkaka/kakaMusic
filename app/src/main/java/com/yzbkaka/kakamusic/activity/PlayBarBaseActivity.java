package com.yzbkaka.kakamusic.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;

import com.yzbkaka.kakamusic.R;
import com.yzbkaka.kakamusic.fragment.PlayBarFragment;


/**
 * 每个需要出现播放器的activity都需要进行继承
 */
public abstract class PlayBarBaseActivity extends BaseActivity {

    /**
     * 播放器碎片
     */
    private PlayBarFragment playBarFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        show();  //继承的活动每次都会进行调用
    }


    /**
     * 展示播放器
     * 动态添加碎片
     */
    private void show(){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (playBarFragment == null) {
            playBarFragment = PlayBarFragment.newInstance();
            fragmentTransaction.add(R.id.fragment_playbar, playBarFragment).commit();  //会动态的添加到每id为fragment_playbar的布局中
        }else {
            fragmentTransaction.show(playBarFragment).commit();  //直接展示
        }
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }
}
