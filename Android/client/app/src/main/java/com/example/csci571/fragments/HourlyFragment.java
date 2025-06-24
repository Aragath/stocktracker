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

public class HourlyFragment extends Fragment {
    private WebView webView;

    public static HourlyFragment newInstance(String ticker, String color) {
        HourlyFragment fragment = new HourlyFragment();
        Bundle args = new Bundle();
        args.putString("ticker", ticker);
        args.putString("color", color);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.hourly_fragment, container, false);
        webView = view.findViewById(R.id.hourlyWebView);
        webView.getSettings().setJavaScriptEnabled(true); // Enable JavaScript

        String ticker = getArguments().getString("ticker", "AA");
        String color = getArguments().getString("color", "#000000");
        webView.loadUrl("file:///android_asset/hourly.html");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webView.evaluateJavascript("updateChartColor('" + color + "');", null);
                webView.evaluateJavascript("fetchData('" + ticker + "');", null);
            }
        });
        return view;
    }
}
