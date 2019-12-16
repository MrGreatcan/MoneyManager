package com.greatcan.moneysaver;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.greatcan.moneysaver.adapters.ExpenseAdapter;
import com.greatcan.moneysaver.models.ExpensesModels;
import com.greatcan.moneysaver.models.UserMoneyModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
    private RecyclerView recyclerListExpense;
    private double amountOfExpenses;
    private double income;
    private double expense;
    private double balance;
    private double monthBalance = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerListExpense = findViewById(R.id.recyclerListExpense);
        fieldIncome = findViewById(R.id.fieldIncome);
        fieldExpense = findViewById(R.id.fieldExpense);
        fieldBalance = findViewById(R.id.fieldBalance);
        btnOpenAdd = findViewById(R.id.btnOpenAdd);
        btnOpenAdd.setOnClickListener(this);

        //getUserStatistics();

        newMonth();

        getMonthBalance();

        amountOfExpenses = 0;

        final ArrayList<ExpensesModels> listExpenses = new ArrayList<>();
        db.collection("MoneyManager")
                .document(currentUser.getUid())
                .collection("Expense")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            ExpensesModels note = documentSnapshot.toObject(ExpensesModels.class);
                            note.setId(documentSnapshot.getId());
                            Log.d(TAG, "onSuccess: data: " + documentSnapshot.getData());

                            amountOfExpenses += Double.valueOf(note.getAmount());

                            listExpenses.add(note);
                        }

                        fieldExpense.setText(String.valueOf(amountOfExpenses));

                        double currentBalance = monthBalance - amountOfExpenses;

                        fieldBalance.setText(String.valueOf(currentBalance));

                        recyclerListExpense.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        recyclerListExpense.setAdapter(new ExpenseAdapter(listExpenses));
                    }
                });


    }

    private void getMonthBalance() {
        DocumentReference reference =
                db.collection("Money")
                        .document(currentUser.getUid())
                        .collection("Dates")
                        .document(getCurrentDate());
        reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserMoneyModel userMoneyModel = documentSnapshot.toObject(UserMoneyModel.class);
                Log.d(TAG, "onSuccess: found a date with month balance: " + userMoneyModel.getMonthBalance());
                monthBalance = userMoneyModel.getMonthBalance();
            }
        });
    }

    private void newMonth() {
        //
        //Get current date. Example 05.12.2019
        //Add to database
        //If date not exists in database, enter balance, else - none
        //
        String currentDate = getCurrentDate();
        hasDateInDatabase(currentDate);
    }

    /**
     * Getting current data by format MM.yyyy
     *
     * @return
     */
    private String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM.yyyy");
        Date date = new Date();
        return dateFormat.format(date);
    }

    /**
     * Does the date exists in database
     *
     * @param date
     */
    private void hasDateInDatabase(final String date) {
        db.collection("Money")
                .document(currentUser.getUid())
                .collection("Dates")
                .document(date)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "Date was found");
                    } else {
                        Log.d(TAG, "No such document");
                        saveDate(date);
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    /**
     * Save date to database
     *
     * @param date
     */
    private void saveDate(String date) {
        Log.d(TAG, "saveDate: Opening a dialogue with entering a month balance");
        UserMoneyModel userMoneyModel = new UserMoneyModel(0.0, 0.0, 0.0, 0.0);
        String userUID = currentUser.getUid();
        db.collection("Money")
                .document(userUID)
                .collection("Dates")
                .document(date)
                .set(userMoneyModel)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "onSuccess: Date was successfully added");
                    }
                });

    }

    private void getUserStatistics() {
        DocumentReference reference = db.collection("Money").document(currentUser.getUid());

        reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                UserMoneyModel userMoneyModel = documentSnapshot.toObject(UserMoneyModel.class);

                income = userMoneyModel.getIncome();
                expense = userMoneyModel.getExpense();
                balance = userMoneyModel.getBalance();

                Log.d(TAG, "onSuccess: income: " + userMoneyModel.getIncome());
                Log.d(TAG, "onSuccess: expense: " + userMoneyModel.getExpense());
                Log.d(TAG, "onSuccess: balance: " + userMoneyModel.getBalance());
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == btnOpenAdd) {
            startActivity(new Intent(this, AddActivity.class));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

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
                            ExpensesModels models = item.toObject(ExpensesModels.class);
                            String amount = models.getAmount();
                            Log.d(TAG, "onSuccess: amount: " + amount);
                        }
                    }
                });
    }

    private void getFullList() {
        final ArrayList<ExpensesModels> listExpenses = new ArrayList<>();
        db.collection("MoneyManager")
                .document(currentUser.getUid())
                .collection("Expense")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            ExpensesModels note = documentSnapshot.toObject(ExpensesModels.class);
                            note.setId(documentSnapshot.getId());
                            Log.d(TAG, "onSuccess: data: " + documentSnapshot.getData());

                            amountOfExpenses += Double.valueOf(note.getAmount());

                            listExpenses.add(note);
                        }

                        fieldExpense.setText(String.valueOf(amountOfExpenses));

                        recyclerListExpense.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        recyclerListExpense.setAdapter(new ExpenseAdapter(listExpenses));
                    }
                });
    }
}
