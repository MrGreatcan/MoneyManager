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

public class ConfirmAddingDialog extends DialogFragment implements View.OnClickListener {

    private static final String TAG = "ConfirmAddingDialog";

    //Interface
    private OnConfirmAddingListener onConfirmListener;

    //Objects
    private EditText fieldNote;
    private Button btnConfirm;
    private TextView tvCancel;

    private FinanceModel financeModel;

    public ConfirmAddingDialog(FinanceModel financeModel) {
        this.financeModel = financeModel;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirm_adding, container, false);

        Log.d(TAG, "onCreateView: started.");

        fieldNote = view.findViewById(R.id.fieldNote);
        tvCancel = view.findViewById(R.id.tvCancel);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(this);
        tvCancel.setOnClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onConfirmListener = (OnConfirmAddingListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }


    @Override
    public void onClick(View v) {
        if (v == btnConfirm) {
            Log.d(TAG, "onClick: captured password and confirming.");

            String note = fieldNote.getText().toString().trim();
            //financeModel.setNote(!note.equals("") ? note : financeModel.getCategory());
            financeModel.setNote(note);

            Log.d(TAG, "onClick: amount: " + financeModel.getAmount());

            onConfirmListener.onConfirmAdding(financeModel);
            getDialog().dismiss();

            Log.d(TAG, "onClick: confirm adding, note: " + note);
        }
        if (v == tvCancel){
            dismiss();
        }
    }

    /**
     * Interface for config balance
     */
    public interface OnConfirmAddingListener {
        void onConfirmAdding(FinanceModel model);
    }
}
