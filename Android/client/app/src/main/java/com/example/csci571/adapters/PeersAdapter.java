package com.example.csci571.adapters;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.csci571.R;

import java.util.List;

public class PeersAdapter extends RecyclerView.Adapter<PeersAdapter.PeerViewHolder> {
    private List<String> peers;
    private LayoutInflater inflater;
    private OnPeerClickListener listener;

    public PeersAdapter(Context context, List<String> peers, OnPeerClickListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.peers = peers;
        this.listener = listener;
    }

    @Override
    public PeerViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = inflater.inflate(R.layout.company_peers, parent, false);
        return new PeerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PeerViewHolder holder, int position){
        String peer = peers.get(position);
        holder.peerTextView.setText(Html.fromHtml("<u>" + peer + "</u>   "));
        holder.itemView.setOnClickListener(v -> {
            if(listener!=null){
                listener.onPeerClick(peer);
            }
        });
    }

    @Override
    public int getItemCount(){
        return peers.size();
    }

    public static class PeerViewHolder extends RecyclerView.ViewHolder{
        TextView peerTextView;

        public PeerViewHolder(View itemView){
            super(itemView);
            peerTextView = (TextView) itemView.findViewById(R.id.peerTextView);
        }
    }

    public interface OnPeerClickListener{
        void onPeerClick(String peer);
    }
}
