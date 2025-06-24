package com.example.csci571.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import com.example.csci571.R;

public class SuccessfulTradeFragment extends DialogFragment {
    private String ticker;
    private String operation;
    private int quantity;

    public static SuccessfulTradeFragment newInstance(String ticker, String operation, int quantity) {
        SuccessfulTradeFragment fragment = new SuccessfulTradeFragment();
        fragment.ticker = ticker;
        fragment.operation = operation;
        fragment.quantity = quantity;
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = requireActivity().getLayoutInflater().inflate(R.layout.trade_sucessful_dialog, null);
        TextView successfulDialogSubtext = view.findViewById(R.id.successfulDialogSubtext);
        Button successfulDialogBtn = view.findViewById(R.id.successfulDialogBtn);

        successfulDialogSubtext.setText(String.format("You have successfully %s %d shares of %s", operation, quantity, ticker));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view);
        Dialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        successfulDialogBtn.setOnClickListener(v -> {
            dismiss();
        });

        return dialog;
    }
}

