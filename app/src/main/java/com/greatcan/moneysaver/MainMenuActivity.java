package com.greatcan.moneysaver;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.greatcan.moneysaver.adapters.CategoryAdapter;
import com.greatcan.moneysaver.models.CategoryModels;
import com.greatcan.moneysaver.models.IncomeModels;
import com.greatcan.moneysaver.models.UserMoneyModel;

import java.util.ArrayList;
import java.util.HashMap;

public class MainMenuActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainMenuActivity";

    //Objects
    private TextView fieldIncome;
    private TextView fieldExpense;
    private TextView fieldBalance;
    private Button btnOpenAdd;

    //Firebase
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    //Variables
    private int amountOfExpenses;

    //Objects from numpad
    private TextView tvAmount;
    private Button btnRemove;
    private Button btnOk;

    private String moneyTemplate = "$ ";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        fieldIncome = findViewById(R.id.fieldIncome);
        fieldExpense = findViewById(R.id.fieldExpense);
        fieldBalance = findViewById(R.id.fieldBalance);
        btnOpenAdd = findViewById(R.id.btnOpenAdd);
        btnOpenAdd.setOnClickListener(this);

        getUserStatistics();

        //From numpad
        tvAmount = findViewById(R.id.tvAmount);
        btnRemove = findViewById(R.id.btnRemove);
        btnRemove.setOnClickListener(this);
        btnOk = findViewById(R.id.btnOk);
        btnOk.setOnClickListener(this);

        //tvAmount.setMaxLines(10);

        tvAmount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});

    }

    private void getUserStatistics() {
        DocumentReference reference = db.collection("Money").document(currentUser.getUid());

        reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserMoneyModel userMoneyModel = documentSnapshot.toObject(UserMoneyModel.class);

                fieldIncome.setText(String.valueOf(userMoneyModel.getIncome()));
                fieldExpense.setText(String.valueOf(userMoneyModel.getExpense()));
                fieldBalance.setText(String.valueOf(userMoneyModel.getBalance()));

                Log.d(TAG, "onSuccess: income: " + userMoneyModel.getIncome());
                Log.d(TAG, "onSuccess: expense: " + userMoneyModel.getExpense());
                Log.d(TAG, "onSuccess: balance: " + userMoneyModel.getBalance());
            }
        });
    }


    @Override
    public void onClick(View view) {
        if (view == btnOpenAdd) {
            startActivity(new Intent(this, CategoryActivity.class));


            //db.collection("Category")
            //        .add(data)
            //        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            //            @Override
            //            public void onSuccess(DocumentReference documentReference) {
            //                Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
            //            }
            //        })
            //        .addOnFailureListener(new OnFailureListener() {
            //            @Override
            //            public void onFailure(@NonNull Exception e) {
            //                Log.w(TAG, "Error adding document", e);
            //            }
            //        });


        }

        if (view == btnRemove) {
            String text = tvAmount.getText().toString();
            if (!text.equals("")){
                tvAmount.setText(text.substring(0, text.length() - 1));
            }
        }
        if (view == btnOk) {

        }
    }

    public void numpadClick(View view) {
        Log.d(TAG, "numpadClick: clicked on: " + ((Button) view).getText());
        tvAmount.append(((Button) view).getText());
        //switch (view.getId()){
        //    case R.id.btnNumpad0
        //}
    }

    private void countAll() {
        db.collection("MoneyManager")
                .document(currentUser.getUid())
                .collection("Income")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot item : queryDocumentSnapshots) {
                            IncomeModels models = item.toObject(IncomeModels.class);
                            String amount = models.getAmount();
                            Log.d(TAG, "onSuccess: amount: " + amount);
                        }
                    }
                });
    }
}
