package com.greatcan.moneysaver.fragments;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.greatcan.moneysaver.R;
import com.greatcan.moneysaver.adapters.ExpenseAdapter;
import com.greatcan.moneysaver.configuration.date.CurrentDate;
import com.greatcan.moneysaver.configuration.firebase.FirebaseReferences;
import com.greatcan.moneysaver.models.FinanceModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

public class ExpenseFragment extends Fragment {

    private static final String TAG = "ExpenseFragment";

    //Objects
    private RecyclerView recyclerListExpense;
    private RelativeLayout rlNoExpense;

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
        rlNoExpense = view.findViewById(R.id.rlNoExpense);
        rlNoExpense.setVisibility(View.GONE);

        final ArrayList<FinanceModel> listExpenses = new ArrayList<>();
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
                                Date modelDate = new SimpleDateFormat("dd-MM-yyyy").parse(model.getDate());
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

                        if (listExpenses.size() == 0)
                            rlNoExpense.setVisibility(View.VISIBLE);
                        else rlNoExpense.setVisibility(View.GONE);

                        recyclerListExpense.setLayoutManager(new LinearLayoutManager(getActivity()));
                        recyclerListExpense.setAdapter(new ExpenseAdapter(Objects.requireNonNull(getActivity()),listExpenses, Objects.requireNonNull(getActivity())));

                        Collections.sort(listExpenses, new Comparator<FinanceModel>() {
                            /**
                             * Compares its two arguments for order.  Returns a negative integer,
                             * zero, or a positive integer as the first argument is less than, equal
                             * to, or greater than the second.<p>
                             * <p>
                             * In the foregoing description, the notation
                             * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
                             * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
                             * <tt>0</tt>, or <tt>1</tt> according to whether the value of
                             * <i>expression</i> is negative, zero or positive.<p>
                             * <p>
                             * The implementor must ensure that <tt>sgn(compare(x, y)) ==
                             * -sgn(compare(y, x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
                             * implies that <tt>compare(x, y)</tt> must throw an exception if and only
                             * if <tt>compare(y, x)</tt> throws an exception.)<p>
                             * <p>
                             * The implementor must also ensure that the relation is transitive:
                             * <tt>((compare(x, y)&gt;0) &amp;&amp; (compare(y, z)&gt;0))</tt> implies
                             * <tt>compare(x, z)&gt;0</tt>.<p>
                             * <p>
                             * Finally, the implementor must ensure that <tt>compare(x, y)==0</tt>
                             * implies that <tt>sgn(compare(x, z))==sgn(compare(y, z))</tt> for all
                             * <tt>z</tt>.<p>
                             * <p>
                             * It is generally the case, but <i>not</i> strictly required that
                             * <tt>(compare(x, y)==0) == (x.equals(y))</tt>.  Generally speaking,
                             * any comparator that violates this condition should clearly indicate
                             * this fact.  The recommended language is "Note: this comparator
                             * imposes orderings that are inconsistent with equals."
                             *
                             * @param o1 the first object to be compared.
                             * @param o2 the second object to be compared.
                             * @return a negative integer, zero, or a positive integer as the
                             * first argument is less than, equal to, or greater than the
                             * second.
                             * @throws NullPointerException if an argument is null and this
                             *                              comparator does not permit null arguments
                             * @throws ClassCastException   if the arguments' types prevent them from
                             *                              being compared by this comparator.
                             */
                            @Override
                            public int compare(FinanceModel o1, FinanceModel o2) {
                                return  o2.getDate().compareTo(o1.getDate());
                            }
                        });
                    }
                });

        return view;
    }

}
