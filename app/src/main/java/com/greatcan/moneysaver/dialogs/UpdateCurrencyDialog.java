package com.greatcan.moneysaver.dialogs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.greatcan.moneysaver.R;
import com.greatcan.moneysaver.configuration.CurrencyEnum;
import com.greatcan.moneysaver.configuration.firebase.FirebaseReferences;
import com.greatcan.moneysaver.models.UserModel;

import java.util.ArrayList;
import java.util.Objects;

public class UpdateCurrencyDialog extends DialogFragment implements View.OnClickListener {

    private static final String TAG = "UpdateCurrencyDialog";

    //Objects
    private Button btnConfirm;
    private TextView tvCancel, tvCurrent;

    private String currency = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_select_currency, container, false);

        Log.d(TAG, "onCreateView: started.");

        tvCancel = view.findViewById(R.id.tvCancel);
        tvCurrent = view.findViewById(R.id.tvCurrent);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        btnConfirm.setOnClickListener(this);
        tvCancel.setOnClickListener(this);

        getUserCurrency();

        ListView listViewCurrency = view.findViewById(R.id.listViewCurrency);
        //listViewCurrency.setDivider(null);

        final ArrayList<String> listCurrencyTitle = new ArrayList<>();
        final ArrayList<String> listCurrency = new ArrayList<>();
        for (CurrencyEnum item : CurrencyEnum.values()) {
            String title = getActivity().getResources().getString(item.getTitle())
                    + " (" +
                    getActivity().getResources().getString(item.getCurrency())
                    + ")";
            listCurrencyTitle.add(title);
            listCurrency.add(getActivity().getResources().getString(item.getCurrency()));
        }

        ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, listCurrencyTitle);
        listViewCurrency.setAdapter(adapter);
        listViewCurrency.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: clicked on: " + view.toString());

                tvCurrent.setText(listCurrencyTitle.get(position));
                currency = listCurrency.get(position);
            }
        });

        return view;
    }

    /**
     * Receiving user currency from database
     */
    private void getUserCurrency(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FirebaseReferences.USER.getReferences())
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            UserModel model = documentSnapshot.toObject(UserModel.class);
                            tvCurrent.setText(model.getCurrency());
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        if (v == btnConfirm) {
           if (!currency.equals("")){
               FirebaseFirestore db = FirebaseFirestore.getInstance();
               db.collection("Users")
                       .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                       .update("currency", currency);

               dismiss();
               getActivity().recreate();
           }

        }
        if (v == tvCancel) {
            dismiss();
        }
    }
}
