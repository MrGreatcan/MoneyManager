package com.greatcan.moneysaver;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.greatcan.moneysaver.models.IncomeModels;
import com.greatcan.moneysaver.models.UserMoneyModel;

public class MainMenuActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainMenuActivity";

    private TextView fieldIncome;
    private TextView fieldExpense;
    private TextView fieldBalance;
    private Button btnOpenAdd;

    //Firebase
    private FirebaseFirestore db;
    private FirebaseUser currentUser;


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
            //startActivity(new Intent(this, AddActivity.class));

            //Count all income
            db.collection("MoneyManager")
                    .document(currentUser.getUid())
                    .collection("Income")
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot item: queryDocumentSnapshots) {
                                IncomeModels models = item.toObject(IncomeModels.class);
                                String amount = models.getAmount();
                                Log.d(TAG, "onSuccess: amount: " + amount);
                            }
                        }
                    });

        }
    }
}
