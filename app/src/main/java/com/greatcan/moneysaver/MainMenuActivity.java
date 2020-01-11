package com.greatcan.moneysaver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.widget.TextView;

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
import com.greatcan.moneysaver.adapters.SectionPagerAdapter;
import com.greatcan.moneysaver.configuration.CurrentDate;
import com.greatcan.moneysaver.configuration.FirebaseReferences;
import com.greatcan.moneysaver.configuration.IntentExtras;
import com.greatcan.moneysaver.configuration.ReceiverAction;
import com.greatcan.moneysaver.dialogs.ConfirmAddingDialog;
import com.greatcan.moneysaver.dialogs.MonthBalanceDialog;
import com.greatcan.moneysaver.models.FinanceModel;
import com.greatcan.moneysaver.models.UserMoneyModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class MainMenuActivity extends AppCompatActivity implements
        MonthBalanceDialog.OnConfirmBalanceListener,
        ConfirmAddingDialog.OnConfirmAddingListener,
        View.OnClickListener {

    private static final String TAG = "MainMenuActivity";

    //Objects
    private ViewPager viewPager;
    private TextView tvMonthlyBalance, tvCurrentBalance;
    private TextView tvExpense;
    private Button btnAdd;

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

        viewPager = findViewById(R.id.viewPager);
        tvMonthlyBalance = findViewById(R.id.tvMonthlyBalance);
        tvCurrentBalance = findViewById(R.id.tvCurrentBalance);
        tvExpense = findViewById(R.id.tvExpense);
        btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(this);

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
        tvCurrentBalance.setText("$0");

        firebaseManager = new FirebaseManager(this);

        /* Own functions */
        setupViewPager();
        firebaseManager.firebaseMenu(FirebaseAction.MENU_MONTHLY_BALANCE);

        hasDateInDatabase(CurrentDate.getCurrentDate());
    }

    /**
     * Responsible for receiving data from FirebaseManager.class
     */
    private BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            tvMonthlyBalance.setText("0");

            String action = intent.getAction();

            if (action != null && action.equals(ReceiverAction.MENU_MONTHLY_ACTION)) {
                String monthlyBalance = intent.getStringExtra(IntentExtras.MONTHLY_KEY);
                tvMonthlyBalance.setText(monthlyBalance);
                getAllExpense();
            }
        }
    };

    /**
     * Get expense
     */
    private void getAllExpense() {
        amountOfExpenses = 0.0d;
        tempExpense = 0.0d;
        db.collection("MoneyManager")
                .document(currentUser.getUid())
                .collection(FirebaseReferences.STATS.getReferences())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            FinanceModel model = documentSnapshot.toObject(FinanceModel.class);
                            model.setId(documentSnapshot.getId());
                            Log.d(TAG, "onSuccess: data: " + documentSnapshot.getData());

                            try {
                                @SuppressLint("SimpleDateFormat")
                                Date modelDate = new SimpleDateFormat("dd-MM-yyyy").parse(model.getData());
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
                        //tvExpense.setText(amountOfExpenses <= monthBalance ? "$ " + amountOfExpenses : "- $ " + amountOfExpenses);
                        tvExpense.setText("$ " + amountOfExpenses);
                        tempExpense = 0.0d;
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

        tabLayout.getTabAt(0).setText("Menu");
        tabLayout.getTabAt(1).setText("Statistics");
        tabLayout.getTabAt(2).setText("Settings");
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
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        tvMonthlyBalance.setText("$0");

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
                            //firebaseManager.fillMonthlyBalance();
                        }
                    });
        }
    }

    @Override
    public void onConfirmAdding(FinanceModel model) {
        if (model != null) {
            String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            db.collection("MoneyManager")
                    .document(userUID)
                    .collection(FirebaseReferences.STATS.getReferences())
                    .add(model)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                            setupViewPager();
                           // firebaseManager.fillMonthlyBalance();
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

    @Override
    public void onClick(View view) {
        if (view == btnAdd) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            String currentMonth = CurrentDate.getCurrentDate();
            hasDateInDatabase(currentMonth);
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
