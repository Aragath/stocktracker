package com.example.csci571.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.csci571.R;
import com.example.csci571.adapters.NewsAdapter;
import com.example.csci571.adapters.PeersAdapter;
import com.example.csci571.adapters.ViewPagerAdapter;
import com.example.csci571.fragments.TradeDialogFragment;
import com.example.csci571.models.News;
import com.example.csci571.models.Profile;
import com.example.csci571.models.Quote;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class DetailActivity extends AppCompatActivity implements TradeDialogFragment.TradeDialogListener{
    private AtomicInteger remainingCalls = new AtomicInteger(0);

    String ticker = "";
    String tickerName = "";
    String color = "";
    double cur_money;
    Boolean isFavorite = false;
    Quote quoteOfStock;
    Profile profileOfStock;
    int quantityOfStock = 0;
    double costOfStock = 0;
    List<String> peers = new ArrayList<>();
    JSONObject insiderOfStock = new JSONObject();
    List<News> newsOfStock = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Base_Theme_CSCI571);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detail), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ticker = getIntent().getStringExtra("ticker");
        cur_money = getIntent().getDoubleExtra("cashBalance", 0);

        // set up toolbar
        Toolbar favToolbar = findViewById(R.id.detail_toolbar);
        setSupportActionBar(favToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // enable setting toolbar title
        favToolbar.setTitle(ticker);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // listen to trade button click
        Button tradeBtn = findViewById(R.id.tradeButton);
        tradeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create and show the dialog
                TradeDialogFragment dialogFragment = TradeDialogFragment.newInstance(ticker, tickerName, cur_money, quantityOfStock, quoteOfStock.getC());
                dialogFragment.show(getSupportFragmentManager(), "tradeDialog");
            }
        });

        fetchInitialData();
    }
    @Override
    public void onTradeCompletedBuy() {
        refreshAfterSell();
    }
    @Override
    public void onTradeCompletedSell() {
        refreshAfterSell();
    }
    private void refreshAfterSell() {
        remainingCalls.set(1);
        RequestQueue queue = Volley.newRequestQueue(this);
        fetchHolding(queue, ()-> { // get quoteOfStock
            fetchQuote(queue, () -> { //  update quantityOfStock and costOfStock
                if (remainingCalls.decrementAndGet() == 0) {
                    updatePortfolio();
                }
            });
        });
    }

    private void updatePortfolio() {
        TextView detailSharesOwned = findViewById(R.id.detailSharesOwned);
        TextView detailAvgCost = findViewById(R.id.detailAvgCost);
        TextView detailTotalCost = findViewById(R.id.detailTotalCost);
        TextView detailChange = findViewById(R.id.detailChange);
        TextView detailMarketValue = findViewById(R.id.detailMarketValue);
        double cur_c = quoteOfStock.getC();
        double diff = cur_c * quantityOfStock - costOfStock;

        if (quantityOfStock == 0){
            detailChange.setTextColor(Color.parseColor("#000000"));
            detailMarketValue.setTextColor(Color.parseColor("#000000"));
        } else if(diff > 0){
            detailChange.setTextColor(Color.parseColor("#62B374"));
            detailMarketValue.setTextColor(Color.parseColor("#62B374"));
        } else if (diff < 0) {
            detailChange.setTextColor(Color.parseColor("#E93711"));
            detailMarketValue.setTextColor(Color.parseColor("#E93711"));
        }

        detailSharesOwned.setText(String.format(Locale.US, "%d", quantityOfStock));
        if (quantityOfStock == 0){
            detailAvgCost.setText(String.format(Locale.US, "$%.2f", 0.00));
            detailTotalCost.setText(String.format(Locale.US, "$%.2f", 0.00));
            detailChange.setText(String.format(Locale.US, "$%.2f", 0.00));
            detailMarketValue.setText(String.format(Locale.US, "$%.2f", 0.00));
        } else {
            detailAvgCost.setText(String.format(Locale.US, "$%.2f", costOfStock / quantityOfStock));
            detailTotalCost.setText(String.format(Locale.US, "$%.2f", costOfStock));
            detailChange.setText(String.format(Locale.US, "$%.2f", diff));
            detailMarketValue.setText(String.format(Locale.US, "$%.2f", cur_c * quantityOfStock));
        }
    }
    private void fetchInitialData(){
        remainingCalls.set(1); // # of async tasks

        RequestQueue queue = Volley.newRequestQueue(this);
        // Fetch favorites
        fetchFavorites(queue, () -> {
            fetchQuote(queue, () -> {
                fetchProfile(queue, () -> {
                    fetchHolding(queue, () -> {
                        fetchPeers(queue, () -> {
                            fetchInsider(queue, () -> {
                                fetchNews(queue, () -> {
                                    if (remainingCalls.decrementAndGet() == 0) {
                                        // set layout visibility and progress bar to gone
                                        RelativeLayout contentLayout = findViewById(R.id.detailLayout);
                                        ProgressBar loadingSpinner = findViewById(R.id.detailSpinner);
                                        contentLayout.setVisibility(View.VISIBLE);
                                        loadingSpinner.setVisibility(View.GONE);
                                        updateField();
                                        updateCharts();
                                    }
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    private void fetchFavorites(RequestQueue queue, Runnable onComplete) {
        String url = "https://myfirstpython-891021.wl.r.appspot.com/favorites";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    handleFavoritesResponse(response);
                    onComplete.run();
                },
                error -> {
                    System.out.println("fetchFavorites error: " + error);
                    onComplete.run();
                });
        queue.add(stringRequest);
    }
    private void handleFavoritesResponse(String response){
        if(response.equals("[]")){
            isFavorite = false;
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String favTicker = jsonObject.getString("ticker");
                if (favTicker.equals(ticker)){
                    isFavorite = true;
                    break;
                }
            }
        } catch (JSONException e){
            Toast.makeText(DetailActivity.this, "Failed to handle favorites response!", Toast.LENGTH_LONG).show();
            System.out.println("handleFavoritesResponse error: " + e);
        }
        invalidateOptionsMenu(); // call onCreateOptionsMenu again
    }
    private void fetchQuote(RequestQueue queue, Runnable onComplete){
        String url = "https://myfirstpython-891021.wl.r.appspot.com/quote/" + Uri.encode(ticker);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    handleQuoteResponse(response);
                    onComplete.run();
                },
                error -> {
                    Toast.makeText(this, "Failed to fetch quote!", Toast.LENGTH_SHORT).show();
                    System.out.println("-- detail: " + error);
                    onComplete.run();
                });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
    private void handleQuoteResponse(String response){
        if(response.equals("[]")){
            return;
        } else if (response.equals("{}")) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(response);
            double c = jsonObject.getDouble("c");
            double d = jsonObject.getDouble("d");
            double dp = jsonObject.getDouble("dp");
            double h = jsonObject.getDouble("h");
            double l = jsonObject.getDouble("l");
            double o = jsonObject.getDouble("o");
            double pc = jsonObject.getDouble("pc");
            double t = jsonObject.getDouble("t");

            quoteOfStock = new Quote(c, d, dp, h, l, o, pc, t);
        } catch (JSONException e){
            Toast.makeText(DetailActivity.this, "Failed to parse quote data!", Toast.LENGTH_LONG).show();
            System.out.println("detail: handleQuoteResponse error: " + e);
        }
    }
    private void fetchProfile(RequestQueue queue, Runnable onComplete) {
        String url = "https://myfirstpython-891021.wl.r.appspot.com/profile/" + Uri.encode(ticker);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    handleProfileResponse(response);
                    onComplete.run();
                },
                error -> {
                    System.out.println("fetchProfile error: " + error);
                    onComplete.run();
                });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
    private void handleProfileResponse(String response){
        if(response.equals("{}")){
            return;
        } else if (response.equals("[]")) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(response);
            String country = jsonObject.getString("country");
            String currency = jsonObject.getString("currency");
            String estimateCurrency = jsonObject.getString("estimateCurrency");
            String exchange = jsonObject.getString("exchange");
            String finnhubIndustry = jsonObject.getString("finnhubIndustry");
            String ipo = jsonObject.getString("ipo");
            String logo = jsonObject.getString("logo");
            double marketCapitalization = jsonObject.getDouble("marketCapitalization");
            String name = jsonObject.getString("name");
            String phone = jsonObject.getString("phone");
            double shareOutstanding = jsonObject.getDouble("shareOutstanding");
            String ticker = jsonObject.getString("ticker");
            String weburl = jsonObject.getString("weburl");

            tickerName = name;
            profileOfStock = new Profile(country, currency, estimateCurrency, exchange, finnhubIndustry, ipo, logo, marketCapitalization, name, phone, shareOutstanding, ticker, weburl);
        } catch (JSONException e){
            Toast.makeText(DetailActivity.this, "Failed to parse profile data!", Toast.LENGTH_LONG).show();
            System.out.println("handleProfileResponse error: " + e);
        }
    }
    private void clickedFavorite(){
        isFavorite = !isFavorite;
        invalidateOptionsMenu(); // call onCreateOptionsMenu again
        String msg = isFavorite ? ticker + " is added to favorites" : ticker + " is removed from favorites";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        // update db
        updateFavorite();
    }
    private void updateFavorite(){
        String url = "https://myfirstpython-891021.wl.r.appspot.com/favorites";
        RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {},
                error -> {Toast.makeText(this, "Failed to update favorite!", Toast.LENGTH_SHORT).show();}){
            // add parameters to request
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("ticker", ticker);
                if(isFavorite){
                    params.put("name", tickerName);
                }
                return params;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
    private void fetchPeers(RequestQueue queue, Runnable onComplete) {
        String url = "https://myfirstpython-891021.wl.r.appspot.com/peers/" + Uri.encode(ticker);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    handlePeersResponse(response);
                    onComplete.run();
                },
                error -> {
                    System.out.println("fetchPeers error: " + error);
                    onComplete.run();
                });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
    private void handlePeersResponse(String response){
        if(response.equals("[]")){
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i< jsonArray.length(); i++){
                String str = jsonArray.getString(i) + ",";
                peers.add(str);
            }
        } catch (JSONException e){
            Toast.makeText(DetailActivity.this, "Failed to parse wallet data!", Toast.LENGTH_LONG).show();
            System.out.println("handlePeersResponse error: " + e);
        }
    }
    private void updateField(){
        TextView detailTickerTextView = findViewById(R.id.detailTicker);
        TextView detailNameTextView = findViewById(R.id.detailName);
        TextView detailValueTextView = findViewById(R.id.detailValue);
        ImageView detailTrendImgView = findViewById(R.id.detailTrendImg);
        TextView detailTrendTextView = findViewById(R.id.detailTrendValue);
        ImageView detailLogo = findViewById(R.id.detailLogo);

        double cur_c = quoteOfStock.getC();
        double cur_d = quoteOfStock.getD();

        detailTickerTextView.setText(ticker);
        detailNameTextView.setText(tickerName);
        detailValueTextView.setText(String.format(Locale.US, "$%.2f", cur_c));
        detailTrendTextView.setText(String.format("$%.2f ( %.2f%% )", cur_d, quoteOfStock.getDp()));

        // set company logo
        Glide.with(this)
                .load(profileOfStock.getLogo())
                .placeholder(R.drawable.ic_launcher_foreground) // placeholder
                .into(detailLogo);
        if(cur_d > 0){
            color = "#62B374";
            detailTrendImgView.setImageResource(R.drawable.trending_up);
            detailTrendTextView.setTextColor(Color.parseColor(color));
        } else if (cur_d < 0) {
            color = "#E93711";
            detailTrendImgView.setImageResource(R.drawable.trending_down);
            detailTrendTextView.setTextColor(Color.parseColor(color));
        } else{
            detailTrendImgView.setVisibility(View.GONE);
        }

        // set up portfolio
        TextView detailSharesOwned = findViewById(R.id.detailSharesOwned);
        TextView detailAvgCost = findViewById(R.id.detailAvgCost);
        TextView detailTotalCost = findViewById(R.id.detailTotalCost);
        TextView detailChange = findViewById(R.id.detailChange);
        TextView detailMarketValue = findViewById(R.id.detailMarketValue);
        double diff = cur_c * quantityOfStock - costOfStock;

        if (quantityOfStock == 0){
            detailChange.setTextColor(Color.parseColor("#000000"));
            detailMarketValue.setTextColor(Color.parseColor("#000000"));
        } else if(diff > 0){
            detailChange.setTextColor(Color.parseColor("#62B374"));
            detailMarketValue.setTextColor(Color.parseColor("#62B374"));
        } else if (diff < 0) {
            detailChange.setTextColor(Color.parseColor("#E93711"));
            detailMarketValue.setTextColor(Color.parseColor("#E93711"));
        }

        detailSharesOwned.setText(String.format(Locale.US, "%d", quantityOfStock));
        if (quantityOfStock == 0){
            detailAvgCost.setText(String.format(Locale.US, "$%.2f", 0.00));
            detailTotalCost.setText(String.format(Locale.US, "$%.2f", 0.00));
            detailChange.setText(String.format(Locale.US, "$%.2f", 0.00));
            detailMarketValue.setText(String.format(Locale.US, "$%.2f", 0.00));
        } else {
            detailAvgCost.setText(String.format(Locale.US, "$%.2f", costOfStock / quantityOfStock));
            detailTotalCost.setText(String.format(Locale.US, "$%.2f", costOfStock));
            detailChange.setText(String.format(Locale.US, "$%.2f", diff));
            detailMarketValue.setText(String.format(Locale.US, "$%.2f", cur_c * quantityOfStock));
        }

        // set up stats
        TextView detailOpenPrice = findViewById(R.id.detailOpenPrice);
        TextView detailHighPrice = findViewById(R.id.detailHighPrice);
        TextView detailLowPrice = findViewById(R.id.detailLowPrice);
        TextView detailPrevClose = findViewById(R.id.detailPrevClose);
        detailOpenPrice.setText(String.format(Locale.US, "$%.2f", quoteOfStock.getO()));
        detailHighPrice.setText(String.format(Locale.US, "$%.2f", quoteOfStock.getH()));
        detailLowPrice.setText(String.format(Locale.US, "$%.2f", quoteOfStock.getL()));
        detailPrevClose.setText(String.format(Locale.US, "$%.2f", quoteOfStock.getPc()));

        // set up about
        TextView detailIPOStartDate = findViewById(R.id.detailIPOStartDate);
        TextView detailIndustry = findViewById(R.id.detailIndustry);
        TextView detailWebpage = findViewById(R.id.detailWebpage);
        PeersAdapter adapter = new PeersAdapter(this, peers, this::handlePeerClick);
        RecyclerView recyclerView = findViewById(R.id.detailCompanyPeers);

        detailIPOStartDate.setText(profileOfStock.getIpo());
        detailIndustry.setText(profileOfStock.getFinnhubIndustry());
        detailWebpage.setText(profileOfStock.getWeburl());
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter);

        // set up news
        RecyclerView detailNewsRecView = findViewById(R.id.detailNewsRecView);
        detailNewsRecView.setLayoutManager(new LinearLayoutManager(this));
        NewsAdapter newsAdapter = new NewsAdapter(newsOfStock);
        detailNewsRecView.setAdapter(newsAdapter);

        // set up insights: social sentiment
        TextView detailSentimentName = findViewById(R.id.detailSentimentName);
        TextView detailTotalMSRP = findViewById(R.id.detailTotalMSRP);
        TextView detailTotalChange = findViewById(R.id.detailTotalChange);
        TextView detailPosMSRP = findViewById(R.id.detailPosMSRP);
        TextView detailPosChange = findViewById(R.id.detailPosChange);
        TextView detailNegMSRP = findViewById(R.id.detailNegMSRP);
        TextView detailNegChange = findViewById(R.id.detailNegChange);
        try {
            detailSentimentName.setText(tickerName);
            JSONArray msprArray = insiderOfStock.getJSONArray("mspr");
            JSONArray changeArray = insiderOfStock.getJSONArray("change");
            detailTotalMSRP.setText(String.format(Locale.US, "%.2f", msprArray.get(0)));
            detailPosMSRP.setText(String.format(Locale.US, "%.2f", msprArray.get(1)));
            detailNegMSRP.setText(String.format(Locale.US, "%.2f", msprArray.get(2)));
            detailTotalChange.setText(String.format(Locale.US, "%.1f", changeArray.getDouble(0)));
            detailPosChange.setText(String.format(Locale.US, "%.1f", changeArray.getDouble(1)));
            detailNegChange.setText(String.format(Locale.US, "%.1f", changeArray.getDouble(2)));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    private void updateCharts(){
        // set up hourly and history chart tabs
        ViewPager2 viewPager = findViewById(R.id.pager);
        viewPager.setAdapter(new ViewPagerAdapter(this, ticker, color));
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setIcon(R.drawable.chart_hour);
                    break;
                case 1:
                    tab.setIcon(R.drawable.chart_historical);
                    break;
            }
        }).attach();

        // set up insights: recommendation trends
        WebView trendChartWebView = findViewById(R.id.trendChartWebView);
        trendChartWebView.getSettings().setJavaScriptEnabled(true); // Enable JavaScript execution
        trendChartWebView.loadUrl("file:///android_asset/trend.html");
        trendChartWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false; // Ensures the WebView handles the URLs it loads
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.evaluateJavascript("fetchData('" + ticker + "');", null);
            }
        });

        // set up insights: EPS Surprise
        WebView EPSChartWebView = findViewById(R.id.EPSChartWebView);
        EPSChartWebView.getSettings().setJavaScriptEnabled(true);
        EPSChartWebView.loadUrl("file:///android_asset/EPS.html");
        EPSChartWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.evaluateJavascript("fetchData('" + ticker + "');", null);
            }
        });
    }
    private void fetchHolding(RequestQueue queue, Runnable onComplete) {
        String url = "https://myfirstpython-891021.wl.r.appspot.com/holdings";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    handleHoldingsResponse(response);
                    onComplete.run();
                },
                error -> {
                    System.out.println("fetchHolding error: " + error);
                    onComplete.run();
                });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
    private void handleHoldingsResponse(String response){
        if(response.equals("[]")){
            quantityOfStock = 0;
            costOfStock = 0;
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i< jsonArray.length(); i++){
                JSONObject item = jsonArray.getJSONObject(i);
                String ticker_ = item.getString("ticker");
                if(ticker_.equals(ticker)){
                    int quantity = item.getInt("quantity");
                    double cost = (item.getDouble("cost"));
                    quantityOfStock = quantity;
                    costOfStock = cost;
                }
            }
        } catch (JSONException e){
            Toast.makeText(DetailActivity.this, "Failed to parse wallet data!", Toast.LENGTH_LONG).show();
            System.out.println("handleHoldingsResponse error: " + e);
        }
    }
    private void fetchInsider(RequestQueue queue, Runnable onComplete) {
        String url = "https://myfirstpython-891021.wl.r.appspot.com/insider/" + Uri.encode(ticker);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    handleInsiderResponse(response);
                    onComplete.run();
                },
                error -> {
                    System.out.println("fetchInsider error: " + error);
                    onComplete.run();
                });
        queue.add(stringRequest);
    }
    private void handleInsiderResponse(String response){
        if (response.equals("[]")){
            return;
        } else if (response.equals("{}")){
            return;
        }
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray jsonArray = jsonResponse.getJSONArray("data");
            double mspr_pos = 0;
            double mspr_neg = 0;
            int change_pos = 0;
            int change_neg = 0;
            for (int i = 0; i< jsonArray.length(); i++){
                JSONObject item = jsonArray.getJSONObject(i);
                int change = item.getInt("change");
                double mspr = item.getDouble("mspr");
                if(change > 0){
                    change_pos += change;
                } else {
                    change_neg += change;
                }
                if(mspr > 0){
                    mspr_pos += mspr;
                } else {
                    mspr_neg += mspr;
                }
            }

            JSONArray msprArray = new JSONArray();
            msprArray.put(mspr_pos + mspr_neg);
            msprArray.put(mspr_pos);
            msprArray.put(mspr_neg);
            JSONArray changeArray = new JSONArray();
            changeArray.put(change_pos + change_neg);
            changeArray.put(change_pos);
            changeArray.put(change_neg);

            insiderOfStock.put("mspr", msprArray);
            insiderOfStock.put("change", changeArray);
        } catch (JSONException e){
            Toast.makeText(DetailActivity.this, "Failed to parse insider data!", Toast.LENGTH_LONG).show();
            System.out.println("handleInsiderResponse error: " + e);
        }
    }
    private void fetchNews(RequestQueue queue, Runnable onComplete) {
        String url = "https://myfirstpython-891021.wl.r.appspot.com/news/" + Uri.encode(ticker);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    handleNewsResponse(response);
                    onComplete.run();
                },
                error -> {
                    System.out.println("fetchNews error: " + error);
                    onComplete.run();
                });
        queue.add(stringRequest);
    }
    private void handleNewsResponse(String response){
        if (response.equals("[]")){
            return;
        } else if (response.equals("{}")) {
            return;
        }
        try {
            JSONArray jsonResponse = new JSONArray(response);
            for (int i = 0; i< jsonResponse.length(); i++){
                JSONObject item = jsonResponse.getJSONObject(i);
                String category = item.getString("category");
                int datetime = item.getInt("datetime");
                String headline = item.getString("headline");
                int id = item.getInt("id");
                String image = item.getString("image");
                if(image.isEmpty()) {continue;}
                String related = item.getString("related");
                String source = item.getString("source");
                String summary = item.getString("summary");
                String url = item.getString("url");
                newsOfStock.add(new News(category, datetime, headline, id, image, related, source, summary, url));
                if (newsOfStock.size() == 20){break;}
            }
        } catch (JSONException e){
            Toast.makeText(DetailActivity.this, "Failed to parse news data!", Toast.LENGTH_LONG).show();
            System.out.println("handleInsiderResponse error: " + e);
        }
    }

    // redirect to peer detail if clicked
    private void handlePeerClick(String peer) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("ticker", peer.substring(0, peer.length()-1));
        intent.putExtra("cashBalance", cur_money);
        startActivity(intent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate search menu bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.stock_favorite, menu);

        // set up configuration and star icon
        MenuItem favItem = menu.findItem(R.id.action_favorite);
        favItem.setIcon(isFavorite ? R.drawable.star_full : R.drawable.star_border);

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home){ // return to previous activity
            setResult(Activity.RESULT_OK);
            finish();
            return true;
        } else if (itemId == R.id.action_favorite) { // star icon clicked
            clickedFavorite();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_OK);
        super.onBackPressed();
    }
}
