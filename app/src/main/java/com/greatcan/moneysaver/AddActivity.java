package com.greatcan.moneysaver;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AddActivity";

    //Objects
    private DatePickerDialog picker;
    private Spinner spinnerType;
    private EditText fieldCategory;
    private EditText fieldDate;
    private EditText fieldAmount;
    private Button btnAdd;

    //Firebase
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        Log.d(TAG, "onCreate: starting");
        db = FirebaseFirestore.getInstance();

        spinnerType = findViewById(R.id.spinnerType);

        fieldCategory = findViewById(R.id.fieldCategory);
        fieldCategory.setInputType(InputType.TYPE_NULL);

        fieldDate = findViewById(R.id.fieldDate);
        fieldDate.setInputType(InputType.TYPE_NULL);
        fieldDate.setOnClickListener(this);

        fieldAmount = findViewById(R.id.fieldAmount);
        btnAdd = findViewById(R.id.btnAdd);

        btnAdd.setOnClickListener(this);

        List<String> types = new ArrayList<String>();
        types.add("Income");
        types.add("Expense");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, types);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(dataAdapter);

        Intent intent = getIntent();
        String category = intent.getStringExtra(IntentExtras.CATEGORY_KEY);

        fieldCategory.setText(category);
    }


    @Override
    public void onClick(View view) {
        if (view == fieldDate) {
            final Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            // date picker dialog
            picker = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            fieldDate.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                        }
                    }, year, month, day);
            picker.show();
        }
        if (view == btnAdd) {
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
}
