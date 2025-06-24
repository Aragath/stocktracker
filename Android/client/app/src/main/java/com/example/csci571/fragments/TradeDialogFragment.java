package com.example.csci571.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.csci571.R;
import com.example.csci571.activities.DetailActivity;
import com.example.csci571.models.News;
import com.example.csci571.models.Quote;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TradeDialogFragment extends DialogFragment {
    private String ticker;
    private String name;
    private double cur_money;
    private int cur_holding;
    private double cur_price;
    private boolean isBuyingAction = false;
    private int pendingRequests = 0;

    private synchronized void incrementPendingRequests() {
        pendingRequests++;
    }
    private synchronized void decrementPendingRequests(int quantity) {
        pendingRequests--;
        if (pendingRequests == 0) {
            getActivity().runOnUiThread(() -> {
                showConfirmationDialog(ticker, isBuyingAction ? "bought" : "sold", quantity);
                if (mListener != null) {
                    if (isBuyingAction) {
                        mListener.onTradeCompletedBuy();
                    } else {
                        mListener.onTradeCompletedSell();
                    }
                }
                dismiss();
            });
        }
    }

    public static TradeDialogFragment newInstance(String ticker, String name, double cur_money, int cur_holding, double cur_price){
        TradeDialogFragment fragment = new TradeDialogFragment();
        fragment.ticker = ticker;
        fragment.name = name;
        fragment.cur_money = cur_money;
        fragment.cur_holding = cur_holding;
        fragment.cur_price = cur_price;
        return fragment;
    }

    public interface TradeDialogListener {
        void onTradeCompletedBuy();
        void onTradeCompletedSell();
    }
    private TradeDialogListener mListener;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (TradeDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement TradeDialogListener");
        }
    }
    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mListener != null) {
            if(isBuyingAction){
                mListener.onTradeCompletedBuy();
            } else {
                mListener.onTradeCompletedSell();
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = requireActivity().getLayoutInflater().inflate(R.layout.trade_dialog, null);
        TextView tradeDialogText = view.findViewById(R.id.tradeDialogText);
        EditText tradeDialogInput = view.findViewById(R.id.tradeDialogInput);
        TextView tradeDialogFormula = view.findViewById(R.id.tradeDialogFormula);
        TextView tradeDialogWallet = view.findViewById(R.id.tradeDialogWallet);
        Button tradeDialogBuyBtn = view.findViewById(R.id.tradeDialogBuyBtn);
        Button tradeDialogSellBtn = view.findViewById(R.id.tradeDialogSellBtn);

        tradeDialogText.setText("Trade " + name + " shares");
        tradeDialogFormula.setText(String.format(Locale.US,  "0*$%.2f/share = %.2f", cur_price, 0.00));
        tradeDialogWallet.setText(String.format(Locale.US, "%.2f to buy " + ticker, cur_money));
        tradeDialogInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty()) {
                    try {
                        int quantity = Integer.parseInt(s.toString());
                        double totalCost = quantity * cur_price;
                        tradeDialogFormula.setText(String.format(Locale.US, "%d*$%.2f/share = $%.2f", quantity, cur_price, totalCost));
                    } catch (NumberFormatException e) {
                        tradeDialogFormula.setText("Invalid input");
                    }
                } else {
                    tradeDialogFormula.setText(String.format(Locale.US,  "0*$%.2f/share = %.2f", cur_price, 0.00));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        tradeDialogBuyBtn.setOnClickListener(v -> attemptTrade(tradeDialogInput.getText().toString(), true));
        tradeDialogSellBtn.setOnClickListener(v -> attemptTrade(tradeDialogInput.getText().toString(), false));

        // Build and return the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        Dialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }
    private void attemptTrade(String input, boolean isBuying) {
        isBuyingAction = isBuying;
        if (!input.matches("^\\d*\\.?\\d+$")) {
            showToast("Please enter a valid amount");
            return;
        }
        int quantity = Integer.parseInt(input);
        fetchCurrentPrice(ticker);
        double totalCost = quantity * cur_price;
        if (isBuying) {
            if (totalCost > cur_money) {
                showToast("Not enough money to buy");
            } else if (quantity <= 0) {
                showToast("Cannot buy non-positive shares");
            } else {
                // Proceed with updating wallet and holdings
                updateWallet(-totalCost, quantity);
                updateHoldings(quantity, totalCost);
            }
        } else {
            if (quantity > cur_holding) {
                showToast("Not enough shares to sell");
            } else if (quantity <= 0) {
                showToast("Cannot sell non-positive shares");
            } else {
                // Proceed with updating wallet and holdings
                updateWallet(totalCost, -quantity);
                updateHoldings(-quantity, -totalCost);
            }
        }
    }
    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void showConfirmationDialog(String ticker, String operation, int quantity) {
        SuccessfulTradeFragment confirmationDialog = SuccessfulTradeFragment.newInstance(ticker, operation, Math.abs(quantity));
        confirmationDialog.show(getParentFragmentManager(), "tradeConfirmation");
        dismiss();
    }
    private void fetchCurrentPrice(String ticker){
        String url = "https://myfirstpython-891021.wl.r.appspot.com/quote" + Uri.encode(ticker);
        RequestQueue queue = Volley.newRequestQueue(getContext());
        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        double c = jsonObject.getDouble("c");
                        cur_price = c;
                    } catch (JSONException e){
                        System.out.println("handleQuoteResponse error: " + e);
                    }
                },
                error -> {
                    System.out.println("fetchCurrentPrice error: " + error);
                });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
    private void updateWallet(double change, int quantity) {
        incrementPendingRequests();
        String url = "https://myfirstpython-891021.wl.r.appspot.com/wallet";
        RequestQueue queue = Volley.newRequestQueue(getContext());

        JSONObject params = new JSONObject();
        try {
            params.put("change", change);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                response -> decrementPendingRequests(quantity),
                error -> {
                    showToast("Failed to update wallet");
                    decrementPendingRequests(quantity);
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    private void updateHoldings(int quantity, double cost) {
        incrementPendingRequests();
        String url = "https://myfirstpython-891021.wl.r.appspot.com/holdings";
        RequestQueue queue = Volley.newRequestQueue(getContext());

        JSONObject params = new JSONObject();
        try {
            params.put("ticker", ticker);
            params.put("name", name);
            params.put("quantity", quantity);
            params.put("cost", cost);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, params,
                response -> decrementPendingRequests(quantity),
                error -> {
                    showToast("Failed to update holdings");
                    decrementPendingRequests(quantity);
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

}
