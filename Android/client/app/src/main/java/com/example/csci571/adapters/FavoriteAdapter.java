package com.example.csci571.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csci571.R;
import com.example.csci571.models.Favorite;
import com.example.csci571.models.Quote;

import java.util.List;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {
    private List<Favorite> favoriteList;
    private List<Quote> quoteOfFavoriteList;
    private OnItemClickListener<Favorite> listener;

    public FavoriteAdapter(List<Favorite> favoriteList, List<Quote> quoteOfFavoriteList, OnItemClickListener<Favorite> listener) {
        this.favoriteList = favoriteList;
        this.quoteOfFavoriteList = quoteOfFavoriteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.favorite, parent, false);
        return new FavoriteViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Favorite fav = favoriteList.get(position);
        Quote quote = quoteOfFavoriteList.get(position);
        double changeInPrice = quote.getD();

        holder.favoriteTicker.setText(fav.getTicker());
        holder.favoriteName.setText(fav.getName());
        holder.favoriteValue.setText(String.format("$%.2f", quote.getC()));
        holder.favoriteTrendValue.setText(String.format("$%.2f ( %.2f%% )", changeInPrice, quote.getDp()));
        if (changeInPrice > 0){
            holder.favoriteTrendImg.setImageResource(R.drawable.trending_up);
            holder.favoriteTrendValue.setTextColor(Color.parseColor("#62B374"));
        } else if (changeInPrice == 0) {
            holder.favoriteTrendImg.setVisibility(View.GONE);
        } else {
            holder.favoriteTrendImg.setImageResource(R.drawable.trending_down);
            holder.favoriteTrendValue.setTextColor(Color.parseColor("#E93711"));
        }
        holder.itemView.setOnClickListener(v->listener.onItemClick(fav));
    }

    @Override
    public int getItemCount() {
        return favoriteList.size();
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        TextView favoriteTicker, favoriteName, favoriteValue, favoriteTrendValue;
        ImageView favoriteTrendImg;

        public FavoriteViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            favoriteTicker = itemView.findViewById(R.id.favoriteTicker);
            favoriteName = itemView.findViewById(R.id.favoriteName);
            favoriteValue = itemView.findViewById(R.id.favoriteValue);
            favoriteTrendValue = itemView.findViewById(R.id.favoriteTrendValue);
            favoriteTrendImg = itemView.findViewById(R.id.favoriteTrendImg);
        }
    }
}
