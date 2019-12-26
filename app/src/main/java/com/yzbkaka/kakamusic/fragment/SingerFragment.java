package com.yzbkaka.kakamusic.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yzbkaka.kakamusic.R;
import com.yzbkaka.kakamusic.activity.ModelActivity;
import com.yzbkaka.kakamusic.adapter.SingerAdapter;
import com.yzbkaka.kakamusic.database.DBManager;
import com.yzbkaka.kakamusic.entity.SingerInfo;
import com.yzbkaka.kakamusic.util.MyMusicUtil;

import java.util.ArrayList;
import java.util.List;


public class SingerFragment extends Fragment {

    private RecyclerView recyclerView;

    private SingerAdapter adapter;

    private List<SingerInfo> singerInfoList = new ArrayList<>();

    private DBManager dbManager;

    private Context mContext;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_singer,container,false);
        recyclerView = (RecyclerView)view.findViewById(R.id.singer_recycler_view);
        dbManager = DBManager.getInstance(getContext());
        adapter = new SingerAdapter(getContext(),singerInfoList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(new SingerAdapter.OnItemClickListener() {
            @Override
            public void onContentClick(View content, int position) {
                Intent intent = new Intent(mContext,ModelActivity.class);
                intent.putExtra(ModelActivity.KEY_TITLE,singerInfoList.get(position).getName());
                intent.putExtra(ModelActivity.KEY_TYPE, ModelActivity.SINGER_TYPE);
                mContext.startActivity(intent);
            }
        });
        return view;
    }


    @Override
    public void onResume() {
        super.onResume();
        singerInfoList.clear();
        singerInfoList.addAll(MyMusicUtil.groupBySinger((ArrayList) dbManager.getAllMusicFromMusicTable()));
        adapter.notifyDataSetChanged();
    }
}
