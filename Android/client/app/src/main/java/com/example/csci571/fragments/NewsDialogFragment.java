package com.example.csci571.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.example.csci571.R;
import com.example.csci571.models.News;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NewsDialogFragment extends DialogFragment {
    private News news;

    public static NewsDialogFragment newInstance(News news){
        NewsDialogFragment fragment = new NewsDialogFragment();
        fragment.news = news;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        // inflate custom dialog layout
        View view = requireActivity().getLayoutInflater().inflate(R.layout.news_dialog, null);
        TextView dialogSource = view.findViewById(R.id.dialogSource);
        TextView dialogDatetime = view.findViewById(R.id.dialogDatetime);
        TextView dialogTitle = view.findViewById(R.id.dialogTitle);
        TextView dialogSummary = view.findViewById(R.id.dialogSummary);
        ImageView dialogChrome = view.findViewById(R.id.dialogChrome);
        ImageView dialogX = view.findViewById(R.id.dialogX);
        ImageView dialogFb = view.findViewById(R.id.dialogFb);

        // set news data to views
        dialogSource.setText(news.getSource());
        Date date = new Date(news.getDatetime() * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        String formattedDate = sdf.format(date);
        dialogDatetime.setText(formattedDate);
        dialogTitle.setText(news.getHeadline());
        dialogSummary.setText(news.getSummary());
        dialogChrome.setOnClickListener(v -> {
            String url = news.getUrl();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
        dialogX.setOnClickListener(v -> {
            String url = news.getUrl();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://twitter.com/share?url=" + url + "&text=Check out this link: "));
            startActivity(intent);
        });
        dialogFb.setOnClickListener(v -> {
            String url = news.getUrl();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://www.facebook.com/sharer/sharer.php?u=" + url));
            startActivity(intent);
        });

        // Build and return the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        Dialog dialog = builder.create();

        // Set the background of dialog to transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }
}
