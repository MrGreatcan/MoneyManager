package com.greatcan.moneysaver.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.greatcan.moneysaver.R;
import com.greatcan.moneysaver.models.FinanceModel;

public class ViewExpenseDialog extends DialogFragment implements View.OnClickListener {

    private static final String TAG = "ViewExpenseDialog";

    //Objects
    private Button btnClose;
    private TextView tvNote;

    private String note;

    public ViewExpenseDialog(String note) {
        this.note = note;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_view_expense, container, false);

        Log.d(TAG, "onCreateView: started.");

        btnClose = view.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(this);
        tvNote = view.findViewById(R.id.tvNote);
        tvNote.setText(note);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v == btnClose){
            dismiss();
        }
    }

}
