package com.greatcan.moneysaver.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.greatcan.moneysaver.configuration.DecimalDigitsInputFilter;
import com.greatcan.moneysaver.R;

public class MonthBalanceDialog extends DialogFragment implements View.OnClickListener {

    private static final String TAG = "MonthBalanceDialog";

    //Interface
    private OnConfirmBalanceListener onConfirmBalanceListener;

    //Objects
    private TextView tvAmount;
    private Button btnRemove, btnConfirm;
    private String date;

    public MonthBalanceDialog(String date) {
        this.date = date;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_balance, container, false);

        Log.d(TAG, "onCreateView: started.");

        tvAmount = view.findViewById(R.id.tvAmount);
        btnRemove = view.findViewById(R.id.btnRemove);
        btnConfirm = view.findViewById(R.id.btnOk);
        btnRemove.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);

        TextView tvEnterAmount = view.findViewById(R.id.tvEnterAmount);
        RelativeLayout rlCategory = view.findViewById(R.id.rlCategory);

        /* Init numpad buttons */
        Button btn0 = view.findViewById(R.id.btn0);
        Button btn1 = view.findViewById(R.id.btn1);
        Button btn2 = view.findViewById(R.id.btn2);
        Button btn3 = view.findViewById(R.id.btn3);
        Button btn4 = view.findViewById(R.id.btn4);
        Button btn5 = view.findViewById(R.id.btn5);
        Button btn6 = view.findViewById(R.id.btn6);
        Button btn7 = view.findViewById(R.id.btn7);
        Button btn8 = view.findViewById(R.id.btn8);
        Button btn9 = view.findViewById(R.id.btn9);

        /* Init click listener for numpad buttons */
        btn0.setOnClickListener(numpadClick);
        btn1.setOnClickListener(numpadClick);
        btn2.setOnClickListener(numpadClick);
        btn3.setOnClickListener(numpadClick);
        btn4.setOnClickListener(numpadClick);
        btn5.setOnClickListener(numpadClick);
        btn6.setOnClickListener(numpadClick);
        btn7.setOnClickListener(numpadClick);
        btn8.setOnClickListener(numpadClick);
        btn9.setOnClickListener(numpadClick);

        rlCategory.setVisibility(View.GONE);

        tvEnterAmount.setText("Enter your monthly balance: ");

        //Filter for amount
        tvAmount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        tvAmount.setText("");

        return view;
    }

    /**
     * Universal click for entire buttons on the numpad
     */
    private View.OnClickListener numpadClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String btnClick = ((Button) view).getText().toString();
            tvAmount.append(btnClick);

            Log.d(TAG, "numpadClick: clicked on: " + btnClick);
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onConfirmBalanceListener = (OnConfirmBalanceListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        if (v == btnConfirm) {
            Log.d(TAG, "onClick: captured monthly balance.");

            if (!tvAmount.getText().equals("")) {
                double balance = Double.parseDouble(tvAmount.getText().toString());

                onConfirmBalanceListener.onConfirmBalance(balance, date);
                getDialog().dismiss();

                Log.d(TAG, "onClick: balance: " + balance);

            } else {
                Toast.makeText(getActivity(), "Enter your monthly balance", Toast.LENGTH_SHORT).show();
            }
        }
        if (v == btnRemove) {
            String text = tvAmount.getText().toString();
            if (!text.equals("")) {
                tvAmount.setText(text.substring(0, text.length() - 1));
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
