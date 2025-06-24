package com.example.csci571.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csci571.R;
import com.example.csci571.models.Holding;
import com.example.csci571.models.Quote;

import java.util.List;

public class HoldingAdapter extends RecyclerView.Adapter<HoldingAdapter.HoldingViewHolder>{
    private List<Holding> holdingList;
    private List<Quote> quoteOfHoldingsList;
    private OnItemClickListener<Holding> listener;

    public HoldingAdapter(List<Holding> holdingList, List<Quote> quoteOfHoldingsList, OnItemClickListener<Holding> listener) {
        this.holdingList = holdingList;
        this.quoteOfHoldingsList = quoteOfHoldingsList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HoldingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.holding, parent, false);
        return new HoldingViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull HoldingViewHolder holder, int position) {
        Holding stock = holdingList.get(position);
        Quote quote = quoteOfHoldingsList.get(position);
        int quantityOwned = stock.getQuantity();
        double currentPricePerShare = quote.getC();
        double totalCost = stock.getCost();

        holder.stockTicker.setText(stock.getTicker());
        holder.holdingQuantity.setText(String.format("%s shares", quantityOwned));
        holder.marketValue.setText(String.format("$%.2f", currentPricePerShare * quantityOwned));
        double changeInPriceFromTotalCost = (currentPricePerShare - (totalCost / quantityOwned))* quantityOwned;
        double changeInPriceFromTotalCostPercentage = (changeInPriceFromTotalCost / totalCost) * 100;
        if (changeInPriceFromTotalCost > 0){
            holder.stockTrendImg.setImageResource(R.drawable.trending_up);
            holder.stockTrendValue.setTextColor(Color.parseColor("#62B374"));
        } else if (changeInPriceFromTotalCost == 0) {
            holder.stockTrendImg.setVisibility(View.GONE);
        } else {
            holder.stockTrendImg.setImageResource(R.drawable.trending_down);
            holder.stockTrendValue.setTextColor(Color.parseColor("#E93711"));
        }

        holder.stockTrendValue.setText(String.format("$%.2f ( %.2f%% )", changeInPriceFromTotalCost, changeInPriceFromTotalCostPercentage));
        holder.itemView.setOnClickListener(v->listener.onItemClick(stock));
    }

    @Override
    public int getItemCount() {
        return holdingList.size();
    }

    static class HoldingViewHolder extends RecyclerView.ViewHolder {
        TextView stockTicker, holdingQuantity, marketValue, stockTrendValue;
        ImageView stockTrendImg;

        public HoldingViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            stockTicker = itemView.findViewById(R.id.stockTicker);
            holdingQuantity = itemView.findViewById(R.id.holdingQuantity);
            marketValue = itemView.findViewById(R.id.marketValue);
            stockTrendValue = itemView.findViewById(R.id.stockTrendValue);
            stockTrendImg = itemView.findViewById(R.id.stockTrendImg);
        }
    }
}
