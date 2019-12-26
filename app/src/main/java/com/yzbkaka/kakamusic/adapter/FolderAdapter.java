package com.yzbkaka.kakamusic.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yzbkaka.kakamusic.R;
import com.yzbkaka.kakamusic.database.DBManager;
import com.yzbkaka.kakamusic.entity.FolderInfo;

import java.util.List;


public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.ViewHolder> {

    private List<FolderInfo> folderInfoList;

    private Context context;

    private DBManager dbManager;

    private FolderAdapter.OnItemClickListener onItemClickListener;


    public FolderAdapter(Context context, List<FolderInfo> folderInfoList) {
        this.context = context;
        this.folderInfoList = folderInfoList;
        this.dbManager = DBManager.getInstance(context);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View swipeContent;
        LinearLayout contentLayout;
        ImageView folderImage;
        TextView folderName;
        TextView count;


        public ViewHolder(View itemView) {
            super(itemView);
            this.swipeContent = (View) itemView.findViewById(R.id.model_swipemenu_layout);
            this.contentLayout = (LinearLayout) itemView.findViewById(R.id.model_music_item_ll);
            this.folderImage = (ImageView) itemView.findViewById(R.id.model_head_iv);
            this.folderName = (TextView) itemView.findViewById(R.id.model_item_name);
            this.count = (TextView) itemView.findViewById(R.id.model_music_count);
        }

    }


    @Override
    public FolderAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.local_model_rv_item, parent, false);
        FolderAdapter.ViewHolder viewHolder = new FolderAdapter.ViewHolder(view);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(final FolderAdapter.ViewHolder holder, final int position) {
        FolderInfo folder = folderInfoList.get(position);
        holder.folderImage.setImageResource(R.drawable.folder);
        holder.folderName.setText(folder.getName());
        holder.count.setText("" + folder.getCount()+"首"+folder.getPath());
        holder.contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onItemClickListener.onContentClick(holder.contentLayout, position);
            }
        });
    }


    @Override
    public int getItemCount() {
        return folderInfoList.size();
    }


    public void update(List<FolderInfo> folderInfoList) {
        this.folderInfoList.clear();
        this.folderInfoList.addAll(folderInfoList);
        notifyDataSetChanged();
    }


    public interface OnItemClickListener {
        void onContentClick(View content, int position);
    }


    public void setOnItemClickListener(FolderAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
