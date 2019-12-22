package com.greatcan.moneysaver;

import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.greatcan.moneysaver.models.UserMoneyModel;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainMenuActivity extends AppCompatActivity implements
        MonthBalanceDialog.OnConfirmBalanceListener,
        View.OnClickListener {

    private static final String TAG = "MainMenuActivity";

    //Objects
    private ViewPager viewPager;
    private TextView tvMonthlyBalance, tvCurrentBalance;
    private TextView tvExpense;
    private Button btnAdd;
    private RelativeLayout rlKeyboard;
    private RelativeLayout rlSlider;

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
    private boolean isMonthExists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_design);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        viewPager = findViewById(R.id.viewPager);
        tvMonthlyBalance = findViewById(R.id.tvMonthlyBalance);
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        tvExpense = findViewById(R.id.tvExpense);
        btnAdd = findViewById(R.id.btnAdd);
        rlKeyboard = findViewById(R.id.rlKeyboards);
        rlSlider = findViewById(R.id.rlSlider);

        btnAdd.setOnClickListener(this);
        rlSlider.setOnClickListener(this);
        viewPager.setOnClickListener(this);

        //getUserStatistics();

        createNewMonthCollection();

        //  getMonthBalance();

        amountOfExpenses = 0;

        setupViewPager();

        fillMonthlyBalance();
        tvCurrentBalance.setText("$0");

        /*
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

                        DocumentReference reference =
                                db.collection("Money")
                                        .document(currentUser.getUid())
                                        .collection("Dates")
                                        .document(getCurrentDate());
                        reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                if (documentSnapshot.exists()) {
                                    UserMoneyModel userMoneyModel = documentSnapshot.toObject(UserMoneyModel.class);

                                    Log.d(TAG, "onSuccess: found a date with month balance: " + userMoneyModel.getMonthBalance());

                                    double currentBalance = userMoneyModel.getMonthBalance() - amountOfExpenses;
                                    fieldBalance.setText(String.valueOf(currentBalance));
                                }
                            }
                        });

                        recyclerListExpense.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        recyclerListExpense.setAdapter(new ExpenseAdapter(listExpenses));
                    }
                });

         */
    }

    private void fillMonthlyBalance() {
        DocumentReference reference =
                db.collection("Money")
                        .document(currentUser.getUid())
                        .collection("Dates")
                        .document(getCurrentDate());
        reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    UserMoneyModel userMoneyModel = documentSnapshot.toObject(UserMoneyModel.class);

                    Log.d(TAG, "onSuccess: found a date with month balance: " + userMoneyModel.getMonthlyBalance());

                    tvMonthlyBalance.setText("$" + (int) userMoneyModel.getMonthlyBalance());
                    //double currentBalance = userMoneyModel.getMonthBalance() - amountOfExpenses;
                    //fieldBalance.setText(String.valueOf(currentBalance));
                }
            }
        });
    }

    private void setupViewPager() {
        Log.d(TAG, "setupViewPager: setup view pager");
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ExpenseFragment());
        viewPager.setAdapter(adapter);

        //TabLayout tabLayout =
    }

    /**
     * Get current date. Example 05.12.2019
     * If date not exists in database, enter balance and create add date
     */
    private void createNewMonthCollection() {
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
                        isMonthExists = true;
                    } else {
                        isMonthExists = false;
                        Log.d(TAG, "No such document");
                        Log.d(TAG, "Opening a dialogue with entering a month balance");
                        MonthBalanceDialog monthBalanceDialog = new MonthBalanceDialog(date);
                        monthBalanceDialog.setCancelable(false);
                        monthBalanceDialog.show(getSupportFragmentManager(), "Month balance");
                        //new MonthBalanceDialog(date).show(getSupportFragmentManager(), "Month balance");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }


    @Override
    public void onConfirmBalance(double monthlyBalance, String date) {
        Log.d(TAG, "onConfirmBalance: month balance equals: " + monthlyBalance);
        if (monthlyBalance != 0) {
            UserMoneyModel userMoneyModel = new UserMoneyModel(0.0, 0.0, monthlyBalance, 0.0);
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
                            isMonthExists = true;
                        }
                    });
        }
    }

    /**
     * Button click in numpad
     *
     * @param view
     */
    public void numpadClick(View view) {
        //Log.d(TAG, "numpadClick: clicked on: " + ((Button) view).getText());
        //tvAmount.append(((Button) view).getText());
    }


    @Override
    public void onClick(View view) {
        if (view == btnAdd) {
            rlKeyboard.setVisibility(View.VISIBLE);

            hasDateInDatabase(getCurrentDate());
            if (isMonthExists) {
                // startActivity(new Intent(this, AddActivity.class));


            } else createNewMonthCollection();
        }
        if (view == rlSlider) {
            Log.d(TAG, "onClick: click on slider");
            rlKeyboard.setVisibility(View.INVISIBLE);
        }
        if (view == viewPager){
            rlKeyboard.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
