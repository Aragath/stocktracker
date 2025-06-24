package com.example.csci571.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.csci571.R;

public class HistoryFragment extends Fragment{
    private WebView webView;

    public static HistoryFragment newInstance(String ticker) {
        HistoryFragment fragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString("ticker", ticker);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_fragment, container, false);
        webView = view.findViewById(R.id.historyWebView);
        webView.getSettings().setJavaScriptEnabled(true); // Enable JavaScript

        String ticker = getArguments().getString("ticker", "AA");
        webView.loadUrl("file:///android_asset/history.html");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webView.evaluateJavascript("fetchData('" + ticker + "');", null);
            }
        });
        return view;
    }
}
