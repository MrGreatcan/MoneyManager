package com.greatcan.moneysaver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
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
import com.greatcan.moneysaver.configuration.CurrentDate;
import com.greatcan.moneysaver.models.FinanceModel;
import com.greatcan.moneysaver.configuration.FirebaseReferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

public class ExpenseFragment extends Fragment {

    private static final String TAG = "ExpenseFragment";

    //Objects
    private RecyclerView recyclerListExpense;

    //Firebase
    private FirebaseFirestore db;
    private FirebaseUser currentUser;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense, container, false);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        recyclerListExpense = view.findViewById(R.id.recyclerListExpense);

        final ArrayList<FinanceModel> listExpenses = new ArrayList<>();
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
                                Date modelDate = new SimpleDateFormat("dd-MM-yyyy").parse(model.getData());
                                LocalDate localModelDate = modelDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                                Date currentDate = new SimpleDateFormat("MM.yyyy").parse(CurrentDate.getCurrentDate());
                                LocalDate localCurrentDate = currentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                                Log.d(TAG, "onSuccess: date month: " + localModelDate.getMonthValue());
                                Log.d(TAG, "onSuccess: date month: " + localCurrentDate.getMonthValue());

                                if (localModelDate.getMonthValue() == localCurrentDate.getMonthValue()) {
                                    listExpenses.add(model);
                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }

                        recyclerListExpense.setLayoutManager(new LinearLayoutManager(getActivity()));
                        recyclerListExpense.setAdapter(new ExpenseAdapter(listExpenses, getActivity()));
                    }
                });

        return view;
    }

}
