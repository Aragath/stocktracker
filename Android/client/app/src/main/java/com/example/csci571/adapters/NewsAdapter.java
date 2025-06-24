package com.example.csci571.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.csci571.R;
import com.example.csci571.fragments.NewsDialogFragment;
import com.example.csci571.models.News;

import java.util.Date;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<News> newsList;
    private static final int TYPE_FIRST = 0;
    private static final int TYPE_OTHER = 1;

    public NewsAdapter(List<News> newsList){
        this.newsList = newsList;
    }

    @Override
    public int getItemViewType(int position){
        if (position == 0){
            return TYPE_FIRST;
        } else{
            return TYPE_OTHER;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view;
        if(viewType == TYPE_FIRST){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item_first, parent, false);
            return new NewsViewHolder(view);
        } else{
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.news_item, parent, false);
            return new NewsViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position){
        News item = newsList.get(position);
        NewsViewHolder viewHolder = (NewsViewHolder) holder;
        viewHolder.newsTitle.setText(item.getHeadline());
        viewHolder.newsSource.setText(item.getSource());
        viewHolder.newsElapse.setText(getTimeElapse(item.getDatetime()));
        Glide.with(viewHolder.newsImg.getContext())
                .load(item.getImage())
                .centerCrop()
                .into(viewHolder.newsImg);

        // set onClick listener
        viewHolder.itemView.setOnClickListener(view -> {
            NewsDialogFragment dialogFragment = NewsDialogFragment.newInstance(newsList.get(position));
            dialogFragment.show(((FragmentActivity)view.getContext()).getSupportFragmentManager(), "newsDialog");
        });
    }

    @Override
    public int getItemCount(){
        return newsList.size();
    }

    public String getTimeElapse(int newsDateTime){
        // Convert epoch seconds to milliseconds
        long newsDateTimeMillis = (long) newsDateTime * 1000;
        Date newsDate = new Date(newsDateTimeMillis);
        Date now = new Date();

        // Calculate the difference in milliseconds
        long diffMillis = now.getTime() - newsDate.getTime();

        // Convert milliseconds to seconds
        long seconds = diffMillis / 1000;
        if (seconds < 60) {
            return seconds + " seconds ago";
        } else if (seconds < 3600) {
            return (seconds / 60) + " minutes ago";
        } else if (seconds < 86400) {
            return (seconds / 3600) + " hours ago";
        } else {
            return (seconds / 86400) + " days ago";
        }
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder{
        TextView newsTitle, newsSource, newsElapse;
        ImageView newsImg;

        NewsViewHolder(View itemView){
            super(itemView);
            newsTitle = itemView.findViewById(R.id.newsTitle);
            newsSource = itemView.findViewById(R.id.newsSource);
            newsElapse = itemView.findViewById(R.id.newsElapse);
            newsImg = itemView.findViewById(R.id.newsImg);
        }
    }
}
