package com.yzbkaka.kakamusic.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yzbkaka.kakamusic.R;
import com.yzbkaka.kakamusic.entity.ThemeInfo;
import com.yzbkaka.kakamusic.util.MyMusicUtil;

import java.util.ArrayList;
import java.util.List;

public class ThemeActivity extends BaseActivity {
    public static int THEME_SIZE = 11;

    private String[] themeType = {"哔哩粉", "知乎蓝", "酷安绿","网易红","藤萝紫","碧海蓝","樱草绿","咖啡棕","柠檬橙","星空灰","夜间模式"};

    private int[] colors = {R.color.biliPink, R.color.zhihuBlue, R.color.kuanGreen, R.color.cloudRed,
            R.color.tengluoPurple, R.color.seaBlue, R.color.grassGreen, R.color.coffeeBrown,
            R.color.lemonOrange,R.color.startSkyGray,R.color.nightActionbar};

    private RecyclerView recyclerView;

    private ThemeAdapter adapter;

    private Toolbar toolbar;

    private int selectTheme = 0;

    private List<ThemeInfo> themeInfoList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theme);
        selectTheme = MyMusicUtil.getTheme(ThemeActivity.this);
        toolbar = (Toolbar) findViewById(R.id.theme_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        init();
    }


    private void init(){
        for (int i = 0 ;i < themeType.length;i++){
            ThemeInfo themeInfo = new ThemeInfo();
            themeInfo.setName(themeType[i]);
            themeInfo.setColor(colors[i]);
            themeInfo.setSelect((selectTheme == i) ? true : false);
            if (i == themeType.length-1){  //如果是最后一个夜间模式
                themeInfo.setBackground(R.color.nightBg);  //设置成暗色
            }else {
                themeInfo.setBackground(R.color.colorWhite);  //设置成亮色
            }
            themeInfoList.add(themeInfo);
        }
        recyclerView = (RecyclerView)findViewById(R.id.theme_rv);
        adapter = new ThemeAdapter();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(ThemeActivity.this,HomeActivity.class);  //更改主题之后需要重新启动Activity
                startActivity(intent);
                break;
        }
        return true;
    }


    /**
     * 当点击实体键返回
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
            Intent intent = new Intent(ThemeActivity.this,HomeActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    private class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ViewHolder>{

        public ThemeAdapter() {}

        class ViewHolder extends RecyclerView.ViewHolder {
            RelativeLayout relativeLayout;
            ImageView circleImage;
            TextView nameText;
            Button selectButton;

            public ViewHolder(View itemView) {
                super(itemView);
                this.relativeLayout = (RelativeLayout) itemView.findViewById(R.id.theme_item_rl);
                this.circleImage = (ImageView) itemView.findViewById(R.id.theme_iv);
                this.nameText = (TextView) itemView.findViewById(R.id.theme_name_tv);
                this.selectButton = (Button) itemView.findViewById(R.id.theme_select_tv);
            }
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(ThemeActivity.this).inflate(R.layout.change_theme_item, parent, false);
            ViewHolder viewHolder = new ViewHolder(view);
            return viewHolder;
        }


        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            final ThemeInfo themeInfo = themeInfoList.get(position);
            if (selectTheme == THEME_SIZE-1){  //如果是夜间模式
                holder.relativeLayout.setBackgroundResource(R.drawable.selector_layout_night);
                holder.selectButton.setBackgroundResource(R.drawable.shape_theme_btn_night);
            }else {  //日间模式
                holder.relativeLayout.setBackgroundResource(R.drawable.selector_layout_day);
                holder.selectButton.setBackgroundResource(R.drawable.shape_theme_btn_day);
            }
            holder.selectButton.setPadding(0,0,0,0);
            if (themeInfo.isSelect()){
                holder.circleImage.setImageResource(R.drawable.tick);
                holder.selectButton.setText("使用中");
                holder.selectButton.setTextColor(getResources().getColor(themeInfo.getColor()));  //设置文字颜色
            }else {
                holder.circleImage.setImageBitmap(null);
                holder.selectButton.setText("使用");
                holder.selectButton.setTextColor(getResources().getColor(R.color.grey500));
            }
            holder.circleImage.setBackgroundResource(themeInfo.getColor());
            holder.nameText.setTextColor(getResources().getColor(themeInfo.getColor()));
            holder.nameText.setText(themeInfo.getName());
            holder.selectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshTheme(themeInfo,position);
                }
            });

            holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshTheme(themeInfo,position);
                }
            });
        }


        @Override
        public int getItemCount() {
            return themeInfoList.size();
        }
    }


    /**
     * 更新主题
     */
    private void refreshTheme(ThemeInfo themeInfo,int position){
        if (position == (THEME_SIZE-1)){  //是夜间模式
            MyMusicUtil.setNightMode(ThemeActivity.this,true);
        } else if(MyMusicUtil.getNightMode(ThemeActivity.this)){
            MyMusicUtil.setNightMode(ThemeActivity.this,false);
        }

        selectTheme = position;
        MyMusicUtil.setTheme(ThemeActivity.this,position);

        //进行颜色的转换
        toolbar.setBackgroundColor(getResources().getColor(themeInfo.getColor()));
        recyclerView.setBackgroundColor(getResources().getColor(themeInfo.getBackground()));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {  //状态栏颜色转换
            getWindow().setStatusBarColor(getResources().getColor(themeInfo.getColor()));
        }
        for (ThemeInfo info : themeInfoList){
            if (info.getName().equals(themeInfo.getName())){
                info.setSelect(true);  //设置被选中
            }else {
                info.setSelect(false);
            }
        }
        adapter.notifyDataSetChanged();
    }
}
