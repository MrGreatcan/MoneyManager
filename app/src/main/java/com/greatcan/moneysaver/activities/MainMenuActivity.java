package com.greatcan.moneysaver.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.greatcan.moneysaver.R;
import com.greatcan.moneysaver.adapters.SectionPagerAdapter;
import com.greatcan.moneysaver.configuration.IntentExtras;
import com.greatcan.moneysaver.configuration.ReceiverAction;
import com.greatcan.moneysaver.configuration.date.CurrentDate;
import com.greatcan.moneysaver.configuration.firebase.FirebaseAction;
import com.greatcan.moneysaver.configuration.firebase.FirebaseManager;
import com.greatcan.moneysaver.configuration.firebase.FirebaseReferences;
import com.greatcan.moneysaver.configuration.network.InternetStatus;
import com.greatcan.moneysaver.dialogs.ConfirmAddingDialog;
import com.greatcan.moneysaver.dialogs.MonthBalanceDialog;
import com.greatcan.moneysaver.fragments.AnalysisFragment;
import com.greatcan.moneysaver.fragments.ExpenseFragment;
import com.greatcan.moneysaver.fragments.KeyboardFragment;
import com.greatcan.moneysaver.fragments.SettingsFragment;
import com.greatcan.moneysaver.models.FinanceModel;
import com.greatcan.moneysaver.models.UserModel;
import com.greatcan.moneysaver.models.UserMoneyModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;

public class MainMenuActivity extends AppCompatActivity implements
        MonthBalanceDialog.OnConfirmBalanceListener,
        ConfirmAddingDialog.OnConfirmAddingListener,
        View.OnClickListener {

    private static final String TAG = "MainMenuActivity";

    //Objects
    private ViewPager viewPager;
    private TextView tvMonthlyBalance;
    private TextView tvExpense;
    private Button btnAdd;
    private ProgressBar progressBar;

    //Currency
    private TextView tvExpenseCurrency, tvMonthlyCurrency;

    //Firebase
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FirebaseManager firebaseManager;

    //Variables
    private double amountOfExpenses;
    private double tempExpense;

    private BottomSheetBehavior bottomSheetBehavior;

    public BottomSheetBehavior getBottomSheetBehavior() {
        return bottomSheetBehavior;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        progressBar = findViewById(R.id.progressBar);
        viewPager = findViewById(R.id.viewPager);
        tvMonthlyBalance = findViewById(R.id.tvMonthlyBalance);
        tvExpense = findViewById(R.id.tvExpense);
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);
        tvMonthlyBalance.setOnClickListener(this);

        /* Currency */
        tvExpenseCurrency = findViewById(R.id.tvExpenseCurrency);
        tvMonthlyCurrency = findViewById(R.id.tvMonthlyCurrency);

        View bottomKeyboard = findViewById(R.id.bottomKeyboard);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomKeyboard);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int i) {
                switch (i) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        Log.d(TAG, "onStateChanged: bottom panel was collapsed. Try to hide it");
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        });

        amountOfExpenses = 0.0d;
        progressBar.setVisibility(View.VISIBLE);

        firebaseManager = new FirebaseManager(this);

        /* Own functions */
        setupViewPager();
        firebaseManager.firebaseMenu(FirebaseAction.MENU_MONTHLY_BALANCE);

        hasDateInDatabase(CurrentDate.getCurrentDate());

    }

    /**
     * Receiving user currency from database
     */
    private void getUserCurrency() {
        db.collection(FirebaseReferences.USER.getReferences())
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            UserModel model = documentSnapshot.toObject(UserModel.class);
                            tvExpenseCurrency.setText(model.getCurrency());
                            tvMonthlyCurrency.setText(model.getCurrency());
                        }
                    }
                });
    }


    /**
     * Responsible for receiving date from FirebaseManager.class
     */
    private BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            tvMonthlyBalance.setText("0");

            String action = intent.getAction();

            if (action != null && action.equals(ReceiverAction.MENU_MONTHLY_ACTION)) {
                progressBar.setVisibility(View.VISIBLE);
                String monthlyBalance = intent.getStringExtra(IntentExtras.MONTHLY_KEY);
                tvMonthlyBalance.setText(monthlyBalance);
                getAllExpense();
                getUserCurrency();
            }
        }
    };

    /**
     * Get expense
     */
    private void getAllExpense() {
        amountOfExpenses = 0.0d;
        tempExpense = 0.0d;
        db.collection(FirebaseReferences.MONEY.getReferences())
                .document(currentUser.getUid())
                .collection(FirebaseReferences.STATS.getReferences())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            FinanceModel model = documentSnapshot.toObject(FinanceModel.class);
                            Log.d(TAG, "onSuccess: date: " + documentSnapshot.getData());

                            try {
                                @SuppressLint("SimpleDateFormat")
                                Date modelDate = new SimpleDateFormat("dd-MM-yyyy").parse(model.getDate());
                                LocalDate localModelDate = modelDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                                @SuppressLint("SimpleDateFormat")
                                Date currentDate = new SimpleDateFormat("MM.yyyy").parse(CurrentDate.getCurrentDate());
                                LocalDate localCurrentDate = currentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                                if (localModelDate.getMonthValue() == localCurrentDate.getMonthValue()) {
                                    tempExpense += Double.valueOf(model.getAmount());
                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                        }
                        amountOfExpenses = tempExpense;
                        double monthBalance = Double.parseDouble(tvMonthlyBalance.getText().toString());
                        Log.d(TAG, "onSuccess: monthBalance: " + monthBalance);
                        Log.d(TAG, "onSuccess: amount of expense: " + amountOfExpenses);
                        tvExpense.setText(String.valueOf(amountOfExpenses));
                        tempExpense = 0.0d;
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    /**
     * Setup the main view pager in the menu to slide between expenses and statistics
     */
    private void setupViewPager() {
        Log.d(TAG, "setupViewPager: setup view pager");

        /* Clear fragments */
        if (viewPager.getAdapter() != null) {
            viewPager.getAdapter().notifyDataSetChanged();
        }
        SectionPagerAdapter adapter = new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new ExpenseFragment());
        adapter.addFragment(new AnalysisFragment());
        adapter.addFragment(new SettingsFragment());
        viewPager.setAdapter(adapter);


        TabLayout tabLayout = findViewById(R.id.toolbarTabs);
        tabLayout.setupWithViewPager(viewPager);

        Objects.requireNonNull(tabLayout.getTabAt(0)).setText(R.string.view_menu);
        Objects.requireNonNull(tabLayout.getTabAt(1)).setText(R.string.view_stats);
        Objects.requireNonNull(tabLayout.getTabAt(2)).setText(R.string.view_settings);
    }

    /**
     * Does the date exists in database
     *
     * @param date
     */
    private void hasDateInDatabase(final String date) {
        db.collection(FirebaseReferences.USER.getReferences())
                .document(currentUser.getUid())
                .collection(FirebaseReferences.DATE.getReferences())
                .document(date)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "Date was found");
                    } else {
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        tvMonthlyBalance.setText("0");

                        Log.d(TAG, "No such document");
                        Log.d(TAG, "Opening a dialogue with entering a month balance");
                        MonthBalanceDialog monthBalanceDialog = new MonthBalanceDialog(date);
                        monthBalanceDialog.setCancelable(false);
                        monthBalanceDialog.show(getSupportFragmentManager(), "Month balance");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    /**
     * Button click in numpad
     *
     * @param view
     */
    public void numpadClick(View view) {
        if (view != null) {
            String btnClick = ((Button) view).getText().toString();

            Log.d(TAG, "numpadClick: clicked on: " + btnClick);
            KeyboardFragment fragment = (KeyboardFragment) getSupportFragmentManager().findFragmentById(R.id.keyboardFragment);
            if (fragment != null) {
                fragment.onButtonClicked(btnClick);
            }
        }
    }

    @Override
    public void onConfirmBalance(double monthlyBalance, String date) {
        Log.d(TAG, "onConfirmBalance: month balance equals: " + monthlyBalance);
        if (InternetStatus.getConnectivityStatus(this)) {
            progressBar.setVisibility(View.VISIBLE);

            if (monthlyBalance != 0) {
                UserMoneyModel userMoneyModel = new UserMoneyModel(monthlyBalance);

                String userUID = currentUser.getUid();
                db.collection(FirebaseReferences.USER.getReferences())
                        .document(userUID)
                        .collection(FirebaseReferences.DATE.getReferences())
                        .document(date)
                        .set(userMoneyModel)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "onSuccess: Date was successfully added");
                                setupViewPager();
                                firebaseManager.firebaseMenu(FirebaseAction.MENU_MONTHLY_BALANCE);
                                progressBar.setVisibility(View.GONE);

                            }
                        });
            }
        } else Toast.makeText(this, R.string.config_noInternet, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConfirmAdding(FinanceModel model) {
        if (InternetStatus.getConnectivityStatus(this)) {
            if (model != null) {
                progressBar.setVisibility(View.VISIBLE);

                String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                db.collection(FirebaseReferences.MONEY.getReferences())
                        .document(userUID)
                        .collection(FirebaseReferences.STATS.getReferences())
                        .add(model)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                                setupViewPager();
                                firebaseManager.firebaseMenu(FirebaseAction.MENU_MONTHLY_BALANCE);
                                progressBar.setVisibility(View.GONE);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                            }
                        });
            }
        } else Toast.makeText(this, R.string.config_noInternet, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        if (view == btnAdd) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            String currentMonth = CurrentDate.getCurrentDate();
            hasDateInDatabase(currentMonth);
        }
        if (view == tvMonthlyBalance) {
            int amount = Integer.parseInt(tvMonthlyBalance.getText().toString());
            MonthBalanceDialog monthBalanceDialog = new MonthBalanceDialog(CurrentDate.getCurrentDate(), amount);
            monthBalanceDialog.show(getSupportFragmentManager(), "Month balance");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: start");
        firebaseManager.firebaseMenu(FirebaseAction.MENU_MONTHLY_BALANCE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: resume");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ReceiverAction.MENU_MONTHLY_ACTION);
        registerReceiver(mServiceReceiver, filter);
        firebaseManager.firebaseMenu(FirebaseAction.MENU_MONTHLY_BALANCE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: pause");
        try {
            if (mServiceReceiver != null) {
                unregisterReceiver(mServiceReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}
