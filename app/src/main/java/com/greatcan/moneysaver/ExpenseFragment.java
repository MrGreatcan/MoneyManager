package com.greatcan.moneysaver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.greatcan.moneysaver.adapters.ExpenseAdapter;
import com.greatcan.moneysaver.models.ExpensesModels;

import java.util.ArrayList;

public class ExpenseFragment extends Fragment {

    private static final String TAG = "ExpenseFragment";

    //Objects
    private RecyclerView recyclerListExpense;

    //Firebase
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private double amountOfExpenses;
    private double monthBalance = 0.0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense, container, false);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerListExpense = view.findViewById(R.id.recyclerListExpense);


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

                        recyclerListExpense.setLayoutManager(new LinearLayoutManager(getActivity()));
                        recyclerListExpense.setAdapter(new ExpenseAdapter(listExpenses));
                    }
                });


        return view;
    }

}
