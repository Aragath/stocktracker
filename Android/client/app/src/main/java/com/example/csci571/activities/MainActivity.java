package com.example.csci571.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SearchView.SearchAutoComplete;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.MenuItemCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.csci571.R;
import com.example.csci571.adapters.FavoriteAdapter;
import com.example.csci571.adapters.HoldingAdapter;
import com.example.csci571.models.Favorite;
import com.example.csci571.models.Holding;
import com.example.csci571.models.Quote;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity {
    private final Handler handler = new Handler(Looper.getMainLooper()); // debounce handler for autocomplete suggestions

    private AtomicInteger remainingCalls = new AtomicInteger(0);
    private Runnable fetchSuggestionsRunnable;
    private double cashBalance;
    List<Holding> holdings = new ArrayList<>();
    List<Favorite> favorites = new ArrayList<>();
    List<Quote> quoteOfHoldings = new ArrayList<>();
    List<Quote> quoteOfFavorites = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Base_Theme_CSCI571);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // set up toolbar
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        setCurrentDate(); // set current date
        setFooter(); // set footer click

        fetchInitialData();
    }
    ActivityResultLauncher<Intent> detailActivityLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    fetchInitialData();
                }
            }
    );
    private void fetchInitialData(){
        remainingCalls.set(1); // # of async tasks
        RelativeLayout contentLayout = findViewById(R.id.contentLayout);
        ProgressBar loadingSpinner = findViewById(R.id.loadingSpinner);

        RequestQueue queue = Volley.newRequestQueue(this); // Volley request queue
        fetchWallet(queue, () -> {
            fetchHolding(queue, () -> {
                fetchAllHoldingQuote(queue, () -> {
                    fetchFavorites(queue, () -> {
                        fetchAllFavoritesQuote(queue, () -> {
                            if (remainingCalls.decrementAndGet() == 0) {
                                updatePortfolio();
                                contentLayout.setVisibility(View.VISIBLE);
                                loadingSpinner.setVisibility(View.GONE);
                            }
                        });
                    });
                });
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate search menu bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.stock_search, menu);

        // get SearchView and set up configuration
        MenuItem searchMenu = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenu);

        // get SearchView autocomplete object
        @SuppressLint("RestrictedApi") SearchAutoComplete searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);

        // set a listener for query text change and fetch suggestions
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // hide keyboard, drop down, clear focus
                searchView.clearFocus();
                searchAutoComplete.clearFocus();
                searchAutoComplete.dismissDropDown();
                if (searchView != null) {
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                }

                // direct to DetailActivity
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("ticker", query.toUpperCase()); // pass ticker
                intent.putExtra("cashBalance", cashBalance);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // get rid of the extra detail activity
                detailActivityLauncher.launch(intent);
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.isEmpty() && searchAutoComplete.isFocused()) {
                    if (fetchSuggestionsRunnable != null) {
                        handler.removeCallbacks(fetchSuggestionsRunnable);
                    }
                    fetchSuggestionsRunnable = () -> fetchAutoComplete(newText, searchAutoComplete);
                    handler.postDelayed(fetchSuggestionsRunnable, 300);
                } else {
                    searchAutoComplete.dismissDropDown();
                }
                return true;
            }
        });

        // listen to searchAutoComplete onItemClick event
        searchAutoComplete.setOnItemClickListener((parent, view, position, id) -> {
            // hide keyboard, drop down, clear focus
            searchView.clearFocus();
            searchAutoComplete.clearFocus();
            searchAutoComplete.dismissDropDown();
            if (searchView != null) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            }

            // set text
            String fullText = (String) parent.getItemAtPosition(position);
            String displaySymbol = fullText.split(" \\| ")[0];
            searchAutoComplete.setText(displaySymbol);
            searchAutoComplete.setSelection(displaySymbol.length());

            // direct to DetailActivity
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("ticker", displaySymbol); // pass ticker to detail activity
            intent.putExtra("cashBalance", cashBalance);
            detailActivityLauncher.launch(intent);
        });

        return super.onCreateOptionsMenu(menu);
    }

    // set menu buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();
        if (itemID == R.id.action_search){
            return true;
        } else {
            // User's action isn't recognized. Invoke superclass to handle it.
            return super.onOptionsItemSelected(item);
        }
    }

    private void fetchFavorites(RequestQueue queue, Runnable onComplete) {
        favorites = new ArrayList<>();
        String url = "https://myfirstpython-891021.wl.r.appspot.com/favorites";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    handleFavoritesResponse(response);
                    onComplete.run();
                },
                error -> {
                    System.out.println("fetchFavorites error: " + error);
                    onComplete.run();
                });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
    private void handleFavoritesResponse(String response){
        if(response.equals("[]")){
            favorites = new ArrayList<>();
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray(response);
            List<Favorite> dataList = new ArrayList<>();
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String id = jsonObject.getString("_id");
                String ticker = jsonObject.getString("ticker");
                String name = jsonObject.getString("name");
                dataList.add(new Favorite(id, ticker, name));
            }
            favorites = dataList;
        } catch (JSONException e){
            Toast.makeText(MainActivity.this, "Failed to parse favorite data!", Toast.LENGTH_LONG).show();
            System.out.println("handleFavoritesResponse error: " + e);
        }
    }
    private void fetchAllFavoritesQuote(RequestQueue queue, Runnable allQuotesFetched) {
        AtomicInteger remainingQuotes = new AtomicInteger(favorites.size());
        quoteOfFavorites = new ArrayList<>(Collections.nCopies(favorites.size(), null));
        if (favorites.size() == 0){
            allQuotesFetched.run();
            return;
        }
        for (int i = 0; i < favorites.size(); i++) {
            final int index = i;
            fetchSingleFavoriteQuote(queue, favorites.get(i).getTicker(), response -> {
                handleFavoriteQuoteResponse(response, index);
                if (remainingQuotes.decrementAndGet() == 0) {
                    allQuotesFetched.run();
                }
            });
        }
    }
    private void fetchSingleFavoriteQuote(RequestQueue queue, String ticker, Consumer<String> responseHandler){
        String url = "https://myfirstpython-891021.wl.r.appspot.com/quote/" + Uri.encode(ticker);
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    responseHandler.accept(response);
                },
                error -> {
                    System.out.println("fetchSingleFavoriteQuoteQuote error: " + error);
                    responseHandler.accept(null);
                });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
    private void handleFavoriteQuoteResponse(String response, int index){
        if(response.equals("[]")){
            quoteOfFavorites = new ArrayList<>();
            return;
        } else if (response.equals("{}")) {
            quoteOfFavorites = new ArrayList<>();
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

            quoteOfFavorites.set(index, new Quote(c, d, dp, h, l, o, pc, t));
        } catch (JSONException e){
            Toast.makeText(MainActivity.this, "Failed to parse quote data!", Toast.LENGTH_LONG).show();
            System.out.println("handleFavoriteQuoteResponse error: " + e);
        }
    }

    private void fetchAutoComplete(String query, @SuppressLint("RestrictedApi") SearchAutoComplete searchAutoComplete){
        if (query.isEmpty()) {
            return; // Exit if query is empty
        }
        String url = "https://myfirstpython-891021.wl.r.appspot.com/auto/" + Uri.encode(query);
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    updateSearchAutoComplete(searchAutoComplete, response);
                },
                error -> System.out.println("fetchAutoComplete: ----- " + error + " -----")){
                    @Override
                    public RetryPolicy getRetryPolicy() {
                        // Here you can change the retry policy
                        return new DefaultRetryPolicy(
                                10000, // Timeout milliseconds. Default = 2500
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    }
                };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
    private void updateSearchAutoComplete(@SuppressLint("RestrictedApi") SearchAutoComplete searchAutoComplete, String response){
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray results = jsonResponse.getJSONArray("result");
            List<String> suggestions = new ArrayList<>();
            for(int i = 0; i < results.length(); i++){
                JSONObject item = results.getJSONObject(i);
                String description = item.getString("description");
                String displaySymbol = item.getString("displaySymbol");
                suggestions.add(displaySymbol + " | " + description);
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, suggestions);
            searchAutoComplete.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            searchAutoComplete.showDropDown();
        } catch (JSONException e){
            Toast.makeText(this, "Error updating searchAutoComplete", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchWallet(RequestQueue queue, Runnable onComplete) {
        String url = "https://myfirstpython-891021.wl.r.appspot.com/wallet";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    handleWalletResponse(response);
                    onComplete.run();
                },
                error -> {
                    System.out.println("fetchWallet error: " + error);
                    onComplete.run();
                });

        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
    private void handleWalletResponse(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            cashBalance = jsonObject.getDouble("money");
            updateCashTextView();
        } catch (JSONException e){
            Toast.makeText(MainActivity.this, "Failed to parse wallet data!", Toast.LENGTH_LONG).show();
            System.out.println("handleWalletResponse error: " + e);
        }
    }
    private void updateCashTextView(){
        // set cash balance TextView
        TextView cashBalanceTextView = findViewById(R.id.cashBalanceValue);
        String formattedBalance = String.format(Locale.US, "$%.2f", cashBalance);
        cashBalanceTextView.setText(formattedBalance);
    }
    private void fetchHolding(RequestQueue queue, Runnable onComplete) {
        holdings = new ArrayList<>();
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
            holdings = new ArrayList<>();
            return;
        }
        try {
            JSONArray jsonArray = new JSONArray(response);
            List<Holding> dataList = new ArrayList<>();
            for (int i = 0; i< jsonArray.length(); i++){
                JSONObject item = jsonArray.getJSONObject(i);
                String id = item.getString("_id");
                String ticker = item.getString("ticker");
                String name = item.getString("name");
                int quantity = item.getInt("quantity");
                double cost = (item.getDouble("cost"));
                dataList.add(new Holding(id, ticker, name, quantity, cost));
            }
            holdings = dataList;
        } catch (JSONException e){
            Toast.makeText(MainActivity.this, "Failed to parse holding data!", Toast.LENGTH_LONG).show();
            System.out.println("handleHoldingsResponse error: " + e);
        }
    }
    private void fetchAllHoldingQuote(RequestQueue queue, Runnable allQuotesFetched) {
        AtomicInteger remainingQuotes = new AtomicInteger(holdings.size());
        quoteOfHoldings = new ArrayList<>(Collections.nCopies(holdings.size(), null));
        if (holdings.isEmpty()){
            allQuotesFetched.run();
        }
        for (int i = 0; i < holdings.size(); i++) {
            final int index = i;
            fetchSingleHoldingQuote(queue, holdings.get(i).getTicker(), response -> {
                handleHoldingQuoteResponse(response, index);
                if (remainingQuotes.decrementAndGet() == 0) {
                    allQuotesFetched.run();
                }
            });
        }
    }
    private void handleHoldingQuoteResponse(String response, int index){
        if(response.equals("[]") || response.equals("{}")){
            quoteOfHoldings.set(index, null);
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

            quoteOfHoldings.set(index, new Quote(c, d, dp, h, l, o, pc, t));
        } catch (JSONException e){
            Toast.makeText(MainActivity.this, "main: Failed to parse quote data!", Toast.LENGTH_LONG).show();
            System.out.println("main: handleQuoteResponse error: " + e);
        }
    }
    private void fetchSingleHoldingQuote(RequestQueue queue, String ticker, Consumer<String> responseHandler){
        String url = "https://myfirstpython-891021.wl.r.appspot.com/quote/" + Uri.encode(ticker);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    responseHandler.accept(response);
                },
                error -> {
                    System.out.println("fetchQuote error: " + error);
                    responseHandler.accept(null);
                });
        queue.add(stringRequest);
    }

    private void updatePortfolio(){
        double stockWorth = 0;
        for (int i = 0 ; i < holdings.size(); i++){
            stockWorth+= holdings.get(i).getQuantity() * quoteOfHoldings.get(i).getC();
        }
        double netWorth = cashBalance + stockWorth;

        // set net worth TextView
        TextView netWorthTextView = findViewById(R.id.netWorthValue);
        String formattedBalance = String.format(Locale.US, "$%.2f", netWorth);
        netWorthTextView.setText(formattedBalance);

        // update favorite: adapter
        FavoriteAdapter favoriteAdapter = new FavoriteAdapter(favorites, quoteOfFavorites, this::onStockClicked);
        RecyclerView favRecView = findViewById(R.id.favoriteRecView);
        favRecView.setAdapter(favoriteAdapter);
        favRecView.setLayoutManager(new LinearLayoutManager(this));

        // implement swipe to delete and drag&drop for favorite
        ItemTouchHelper.SimpleCallback favCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                // Swap items in data set
                Collections.swap(favorites, fromPosition, toPosition);
                Collections.swap(quoteOfFavorites, fromPosition, toPosition);

                // Notify adapter the move
                recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
                return true;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                RecyclerView recyclerView = (RecyclerView) viewHolder.itemView.getParent();

                // delete stock from lists and db
                deleteItemFromDatabase(favorites.get(position).getTicker());
                favorites.remove(position);
                quoteOfFavorites.remove(position);
                recyclerView.getAdapter().notifyItemRemoved(position);
                recyclerView.getAdapter().notifyItemRangeChanged(position, recyclerView.getAdapter().getItemCount());
            }
            @Override
            public void onChildDraw (Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive){
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addBackgroundColor(Color.RED)
                        .addActionIcon(R.drawable.delete)
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        new ItemTouchHelper(favCallback).attachToRecyclerView(findViewById(R.id.favoriteRecView));

        // update holding: adapter
        HoldingAdapter holdingAdapter = new HoldingAdapter(holdings, quoteOfHoldings, this::onStockClicked);
        RecyclerView holdingRecView = findViewById(R.id.holdingRecView);
        holdingRecView.setAdapter(holdingAdapter);
        holdingRecView.setLayoutManager(new LinearLayoutManager(this));

        // implement drag&drop for holding
        ItemTouchHelper.SimpleCallback holdingCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                Collections.swap(holdings, fromPosition, toPosition);
                Collections.swap(quoteOfHoldings, fromPosition, toPosition);
                recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);
                return false;
            }
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {}
        };
        new ItemTouchHelper(holdingCallback).attachToRecyclerView(findViewById(R.id.holdingRecView));
    }

    private void deleteItemFromDatabase(String ticker) {
        String url = "https://myfirstpython-891021.wl.r.appspot.com/favorites";

        // Assuming using POST and JSON body
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                response -> {},
                error -> { System.out.println("---- error:" + error);}
        ) {
            @Override
            public byte[] getBody() throws com.android.volley.AuthFailureError {
                try {
                    JSONObject jsonBody = new JSONObject();
                    jsonBody.put("ticker", ticker);
                    return jsonBody.toString().getBytes("utf-8");
                } catch (JSONException | UnsupportedEncodingException e) {
                    System.out.println("---- error " + e);
                }
                return null;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };
        Volley.newRequestQueue(MainActivity.this).add(postRequest);
    }

    // direct to detail activity
    private <T> void onStockClicked(T item) {
        String ticker = "";
        if (item instanceof Favorite) {
            ticker = ((Favorite) item).getTicker();
        } else if (item instanceof Holding) {
            ticker = ((Holding) item).getTicker();
        } else {
            throw new IllegalArgumentException("Unsupported item type");
        }
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("ticker", ticker);
        intent.putExtra("cashBalance", cashBalance);
        detailActivityLauncher.launch(intent);
    }
    private void setCurrentDate(){
        Calendar calendar = Calendar.getInstance(); // get current date

        // format the date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());

        // Set formatted date to TextView
        TextView dateTextView = findViewById(R.id.currentDate);
        dateTextView.setText(currentDate);
    }
    private void setFooter(){
        TextView textView = findViewById(R.id.footer);
        textView.setOnClickListener(view -> {
            String url = "https://www.finnhub.io";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });
    }
}