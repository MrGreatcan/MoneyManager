package com.greatcan.moneysaver;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class MonthBalanceDialog extends DialogFragment implements View.OnClickListener {

    private static final String TAG = "MonthBalanceDialog";

    //Interface
    private OnConfirmBalanceListener onConfirmBalanceListener;

    //Objects
    private EditText fieldMonthBalance;
    private TextView btnConfirm;
    private String date;

    public MonthBalanceDialog(String date) {
        this.date = date;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_balance, container, false);

        Log.d(TAG, "onCreateView: started.");

        fieldMonthBalance = view.findViewById(R.id.fieldMonthBalance);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onConfirmBalanceListener = (OnConfirmBalanceListener) context ;
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnConfirm) {
            Log.d(TAG, "onClick: captured password and confirming.");

            double balance = Double.parseDouble(fieldMonthBalance.getText().toString());

            Log.d(TAG, "onClick: balance: " +  balance);

            if (!fieldMonthBalance.equals("")) {
                onConfirmBalanceListener.onConfirmBalance(balance, date);
                getDialog().dismiss();
            } else {
                Toast.makeText(getActivity(), "Your month balance was successfully added", Toast.LENGTH_SHORT).show();
            }
        }

    }

    /**
     * Interface for config balance
     */
    public interface OnConfirmBalanceListener {
        void onConfirmBalance(double monthBalance, String date);
    }
}
