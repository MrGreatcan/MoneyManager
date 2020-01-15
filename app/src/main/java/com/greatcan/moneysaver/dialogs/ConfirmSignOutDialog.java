package com.greatcan.moneysaver.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.greatcan.moneysaver.R;
import com.greatcan.moneysaver.activities.MainActivity;
import com.greatcan.moneysaver.configuration.IntentExtras;

public class ConfirmSignOutDialog extends DialogFragment implements View.OnClickListener {

    private static final String TAG = "ConfirmSignOutDialog";

    //Objects
    private Button btnConfirm;
    private TextView tvCancel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_confirm_signout, container, false);

        Log.d(TAG, "onCreateView: started.");

        tvCancel = view.findViewById(R.id.tvCancel);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(this);
        tvCancel.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View v) {
        if (v == btnConfirm) {
            Log.d(TAG, "onClick: sign out...");
            dismiss();
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(getActivity(), MainActivity.class);
            intent.putExtra("SignOutKey", IntentExtras.SIGN_OUT_KEY);
            startActivity(intent);
        }
        if (v == tvCancel) {
            dismiss();
        }
    }

}
