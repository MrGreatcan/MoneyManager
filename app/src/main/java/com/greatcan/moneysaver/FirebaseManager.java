package com.greatcan.moneysaver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.greatcan.moneysaver.configuration.CurrentDate;
import com.greatcan.moneysaver.configuration.IntentExtras;
import com.greatcan.moneysaver.configuration.ReceiverAction;
import com.greatcan.moneysaver.models.UserMoneyModel;

public class FirebaseManager {

    private static final String TAG = "FirebaseManager";

    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private Context context;

    public FirebaseManager(Context context) {
        this.context = context;
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    public void firebaseMenu(FirebaseAction action) {
        switch (action) {
            case MENU_MONTHLY_BALANCE:
                Log.d(TAG, "firebaseMenu: menu");
                fillMonthlyBalance(action.name());
                break;
            case STATS_MONTHLY_BALANCE:
                Log.d(TAG, "firebaseMenu: stats");
                fillMonthlyBalance(action.name());
                break;
        }
    }

    /**
     * Get monthly balance from database and send it to activity
     */
    private void fillMonthlyBalance(final String recipient) {
        db = FirebaseFirestore.getInstance();
        String currentMonth = CurrentDate.getCurrentDate();
        DocumentReference reference =
                db.collection("Money")
                        .document(currentUser.getUid())
                        .collection("Dates")
                        .document(currentMonth);
        reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    UserMoneyModel userMoneyModel = documentSnapshot.toObject(UserMoneyModel.class);
                    if (userMoneyModel != null) {
                        Log.d(TAG, "onSuccess: found a date with month balance: " + userMoneyModel.getMonthlyBalance());

                        if (recipient.equals(FirebaseAction.MENU_MONTHLY_BALANCE.name())) {
                            Intent i = new Intent(ReceiverAction.MENU_MONTHLY_ACTION);
                            i.putExtra(IntentExtras.MONTHLY_KEY, String.valueOf((int) userMoneyModel.getMonthlyBalance()));
                            context.sendBroadcast(i);
                        }
                        if (recipient.equals(FirebaseAction.STATS_MONTHLY_BALANCE.name())) {
                            Intent i = new Intent(ReceiverAction.STATS_MONTHLY_ACTION);
                            i.putExtra(IntentExtras.MONTHLY_KEY, String.valueOf((int) userMoneyModel.getMonthlyBalance()));
                            context.sendBroadcast(i);
                        }

                    } else Log.d(TAG, "onSuccess: user model equals null");
                }
            }
        });
    }


}
