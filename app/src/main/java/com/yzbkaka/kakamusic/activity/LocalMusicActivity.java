package com.yzbkaka.kakamusic.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.yzbkaka.kakamusic.R;
import com.yzbkaka.kakamusic.fragment.AlbumFragment;
import com.yzbkaka.kakamusic.fragment.FolderFragment;
import com.yzbkaka.kakamusic.fragment.SingerFragment;
import com.yzbkaka.kakamusic.fragment.SingleFragment;
import com.yzbkaka.kakamusic.util.Constant;
import com.yzbkaka.kakamusic.view.MyViewPager;

import java.util.ArrayList;
import java.util.List;

public class LocalMusicActivity extends PlayBarBaseActivity {

    private Toolbar toolbar;

    private TabLayout tabLayout;

    private MyViewPager viewPager;

    /**
     * vuewpager适配器
     */
    private MyAdapter fragmentAdapter;

    private List<String> titleList = new ArrayList<>(4);

    /**
     * Fragment列表
     */
    private List<Fragment> fragmentList = new ArrayList<>(4);

    /**
     * 单曲
     */
    private SingleFragment singleFragment;

    /**
     * 歌手
     */
    private SingerFragment singerFragment;

    /**
     * 专辑
     */
    private AlbumFragment albumFragment;

    /**
     * 文件夹
     */
    private FolderFragment folderFragment;

    private TextView nothingText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_music);
        toolbar = (Toolbar)findViewById(R.id.local_music_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(Constant.LABEL_LOCAL);
        }
        init();
    }


    /**
     * 初始化界面
     */
    private void init(){
        addTapData();
        viewPager = (MyViewPager)findViewById(R.id.local_viewPager);
        tabLayout = (TabLayout)findViewById(R.id.local_tab);
        fragmentAdapter = new MyAdapter(getSupportFragmentManager());
        viewPager.setAdapter(fragmentAdapter);
        viewPager.setOffscreenPageLimit(2); //预加载页面数
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);

        nothingText = (TextView)findViewById(R.id.local_nothing_tv);
        nothingText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LocalMusicActivity.this,ScanActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 增加tap选项卡
     */
    private void addTapData() {
        titleList.add("单曲");
        titleList.add("歌手");
        titleList.add("专辑");
        titleList.add("文件夹");

        if (singleFragment == null) {
            singleFragment = new SingleFragment();
            fragmentList.add(singleFragment);
        }
        if (singerFragment == null) {
            singerFragment = new SingerFragment();
            fragmentList.add(singerFragment);
        }
        if (albumFragment == null) {
            albumFragment = new AlbumFragment();
            fragmentList.add(albumFragment);
        }
        if (folderFragment == null) {
            folderFragment = new FolderFragment();
            fragmentList.add(folderFragment);
        }
    }


    /**
     * 添加左上角扫描按钮
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.local_music_menu,menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if (item.getItemId() == R.id.scan_local_menu){
            Intent intent = new Intent(LocalMusicActivity.this,ScanActivity.class);
            startActivity(intent);
        }else if (item.getItemId() == android.R.id.home){
            this.finish();
        }
        return true;
    }

    /**
     * 内部类，选项卡适配器
     */
    class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        /**
         * 用来显示tab上的名字
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return titleList.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }
    }
}
