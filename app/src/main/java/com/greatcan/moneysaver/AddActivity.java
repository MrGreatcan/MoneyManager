package com.greatcan.moneysaver;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.greatcan.moneysaver.models.IncomeModels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AddActivity";

    private Spinner spinnerType;
    private EditText fieldCategory;
    private EditText fieldDate;
    private EditText fieldAmount;
    private Button btnAdd;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        db = FirebaseFirestore.getInstance();

        spinnerType = findViewById(R.id.spinnerType);
        fieldCategory = findViewById(R.id.fieldCategory);
        fieldDate = findViewById(R.id.fieldDate);
        fieldAmount = findViewById(R.id.fieldAmount);
        btnAdd = findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(this);

        List<String> types = new ArrayList<String>();
        types.add("Income");
        types.add("Expense");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, types);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(dataAdapter);

    }


    @Override
    public void onClick(View view) {

        String type = spinnerType.getSelectedItem().toString();
        String category = fieldCategory.getText().toString();
        String date = fieldDate.getText().toString();
        String amount = fieldAmount.getText().toString();

        IncomeModels data = new IncomeModels(type, category, date, amount);

        db.collection("MoneyManager")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("Income")
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }
}
