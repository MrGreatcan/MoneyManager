package com.greatcan.moneysaver.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.greatcan.moneysaver.configuration.firebase.FirebaseAction;
import com.greatcan.moneysaver.configuration.firebase.FirebaseManager;
import com.greatcan.moneysaver.R;
import com.greatcan.moneysaver.adapters.ColorAdapter;
import com.greatcan.moneysaver.adapters.ExpenseAdapter;
import com.greatcan.moneysaver.configuration.date.DateRange;
import com.greatcan.moneysaver.configuration.firebase.FirebaseReferences;
import com.greatcan.moneysaver.configuration.IntentExtras;
import com.greatcan.moneysaver.configuration.ReceiverAction;
import com.greatcan.moneysaver.configuration.network.InternetStatus;
import com.greatcan.moneysaver.models.ColorModel;
import com.greatcan.moneysaver.models.FinanceModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class AnalysisFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "AnalysisFragment";

    //Objects
    private TextView tvMonthlyBalance, tvExpense, tvBalance;
    private EditText fieldStartDate, fieldEndDate;
    private Button btnSearch;
    private RecyclerView recyclerListExpense, recyclerChartDescription;
    private PieChart pieChart;
    private LinearLayout llChartView;
    private RelativeLayout rlList, rlTextMonthlyBalance, rlNothingFound;

    //Firebase
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FirebaseManager firebaseManager;

    //Dialogs
    private Dialog dialogStartDate;
    private Dialog dialogEndDate;

    //Variables
    private double amountOfExpenses = 0.0;
    private double tempExpense = 0.0;
    private double monthlyBalance = 0.0;
    private SimpleDateFormat sdf;

    String aColors[] = {
            "#9400D3", "#D2B48C", "#CD853F", "#FF6347", "#E9967A",
            "#FF00FF", "#BA55D3", "#48D1CC", "#778899", "#B0C4DE",
            "#8B0000", "#3CB371", "#4169E1", "#4682B4", "#5F9EA0",
            "#D8BFD8", "#4B0082", "#D2691E", "#A52A2A", "#00008B",
            "#0000FF", "#6A5ACD", "#A9A9A9", "#F0E68C", "#008080",
            "#171724", "#228B22", "#808000", "#FF69B4", "#94064D"
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analysis, container, false);
        Log.d(TAG, "onCreateView: starting");
        initObjects(view);
        return view;
    }

    @SuppressLint("SimpleDateFormat")
    private void initObjects(View v) {
        Log.d(TAG, "initObjects: start initialize objects");
        tvMonthlyBalance = v.findViewById(R.id.tvMonthlyBalance);
        tvExpense = v.findViewById(R.id.tvExpense);
        tvBalance = v.findViewById(R.id.tvBalance);
        recyclerListExpense = v.findViewById(R.id.recyclerListExpense);
        recyclerChartDescription = v.findViewById(R.id.recyclerChartDescription);
        pieChart = v.findViewById(R.id.pieChart);

        /* Layouts */
        llChartView = v.findViewById(R.id.llChartView);
        rlList = v.findViewById(R.id.rlList);
        rlTextMonthlyBalance = v.findViewById(R.id.rlTextMonthlyBalance);
        rlNothingFound = v.findViewById(R.id.rlNothingFound);
        rlTextMonthlyBalance.setVisibility(View.GONE);
        rlNothingFound.setVisibility(View.GONE);

        fieldStartDate = v.findViewById(R.id.fieldStartDate);
        fieldEndDate = v.findViewById(R.id.fieldEndDate);
        btnSearch = v.findViewById(R.id.btnSearch);

        /* Click listener */
        btnSearch.setOnClickListener(this);
        fieldStartDate.setOnClickListener(this);
        fieldEndDate.setOnClickListener(this);

        /* Simple Date Format */
        sdf = new SimpleDateFormat("dd-MM-yyyy");
        sdf.setLenient(false);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseManager = new FirebaseManager(getActivity());

        tvMonthlyBalance.setText("$0");
        tvBalance.setText("$0");
        tvExpense.setText("$0");

        firebaseManager.firebaseMenu(FirebaseAction.STATS_MONTHLY_BALANCE);
        Calendar calendarStart = Calendar.getInstance();
        Calendar calendarEnd = Calendar.getInstance();
        calendarStart.set(Calendar.DAY_OF_MONTH, 1);
        calendarEnd.set(Calendar.DAY_OF_MONTH, Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH));

        fieldStartDate.setText(sdf.format(calendarStart.getTime()));
        fieldEndDate.setText(sdf.format(calendarEnd.getTime()));

        pieChart.setDrawHoleEnabled(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
    }

    /**
     * Responsible for receiving date from FirebaseManager.class
     */
    private BroadcastReceiver mServiceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(ReceiverAction.STATS_MONTHLY_ACTION)) {
                String balance = intent.getStringExtra(IntentExtras.MONTHLY_KEY);
                monthlyBalance = Double.parseDouble(balance);

                try {
                    getAllExpense(sdf.parse(fieldStartDate.getText().toString().trim()), sdf.parse(fieldEndDate.getText().toString().trim()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private void getAllExpense(final Date sStartDate, final Date sEndDate) {
        amountOfExpenses = 0.0;
        tempExpense = 0.0d;

        final ArrayList<FinanceModel> listExpenses = new ArrayList<>();
        final ArrayList<String> listCategories = new ArrayList<>();
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
                            //Log.d(TAG, "onSuccess: date: " + documentSnapshot.getDate());

                            try {
                                Date dateStart = sdf.parse(sdf.format(sStartDate));
                                Date dateEnd = sdf.parse(sdf.format(sEndDate));
                                Date target = sdf.parse(model.getDate());

                                if (DateRange.isDateRange(dateStart, dateEnd, target)) {
                                    tempExpense += Double.valueOf(model.getAmount());
                                    listExpenses.add(model);
                                    listCategories.add(model.getCategory());
                                } else {
                                    Log.d(TAG, "onSuccess: not in range");
                                }
                            } catch (ParseException e) {
                                Log.d(TAG, "onSuccess: unknown date");
                            }

                        }
                        amountOfExpenses = tempExpense;
                        /* Fill monthly balance*/
                        tvMonthlyBalance.setText("$" + monthlyBalance);

                        /* Fill expense */
                        tvExpense.setText("$" + amountOfExpenses);

                        /* Fill balance*/
                        double balance = monthlyBalance - amountOfExpenses;
                        tvBalance.setText("$" + balance);

                        tempExpense = 0.0d;

                        if (listExpenses.size() == 0) {
                            llChartView.setVisibility(View.GONE);
                            rlList.setVisibility(View.GONE);
                            rlNothingFound.setVisibility(View.VISIBLE);
                        } else {
                            llChartView.setVisibility(View.VISIBLE);
                            rlNothingFound.setVisibility(View.GONE);
                            rlList.setVisibility(View.VISIBLE);
                        }

                        /* Add to list View*/
                        recyclerListExpense.setLayoutManager(new LinearLayoutManager(getActivity()));
                        recyclerListExpense.setAdapter(new ExpenseAdapter(listExpenses, getActivity()));

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

                        fillPieChart(listCategories);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: unknown date");
                    }
                });
    }


    /**
     * Filling PieChart
     * Add click listener to PieChart.
     * Adding date to RecyclerView with color of a specific slice
     *
     * @param listCategories receive list with certain categories and count the duplicate values
     */
    private void fillPieChart(ArrayList<String> listCategories) {
        /* List for display chart qColors */
        ArrayList<ColorModel> listColors = new ArrayList<>();

        /* HashMap for categories */
        HashMap<String, Integer> hmCategories = new HashMap<>();

        /* Queue with qColors */
        Queue<String> quColors = new LinkedList<>(Arrays.asList(aColors));

        /* Get duplicated categories */
        for (String str : listCategories) {
            if (hmCategories.containsKey(str)) {
                hmCategories.put(str, hmCategories.get(str) + 1);
            } else {
                hmCategories.put(str, 1);
            }
        }

        /* List with date for PieChart */
        final ArrayList<PieEntry> aListPieEntry = new ArrayList<>();
        ArrayList<Integer> pieColors = new ArrayList<>();

        /* Fill PieData for PieChartView */
        for (Map.Entry<String, Integer> entry : hmCategories.entrySet()) {
            Log.d(TAG, String.format("fillPieChart: entry is: %s, duplicate = %d ", entry.getKey(), entry.getValue()));

            //Percent to show on pie chart
            double percent = getExpensePercent((double) listCategories.size(), (double) entry.getValue());

            /* Received color from the queue */
            int color = Color.parseColor(quColors.poll());

            /* Add to list with PieEntry*/
            aListPieEntry.add(new PieEntry((float) percent, entry.getKey()));

            /* Add to list with colors */
            listColors.add(new ColorModel(color, String.format("%s (%s)", entry.getKey(),  (double) Math.round(percent * 100) / 100 + "%")));

            /* Add color to PieChat */
            pieColors.add(color);
        }

        /* Responsible for the control hole of PieChart */
        final PieDataSet pieDataSet = new PieDataSet(aListPieEntry, "");
        pieDataSet.setColors(pieColors);
        pieDataSet.setSliceSpace(3f);
        pieDataSet.setSelectionShift(5f);

        /* PieChart date management */
        final PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextSize(0f);

        /* PieChart click listener. Show the selected slice when pressed */
        pieChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                String selected = aListPieEntry.get((int) h.getX()).getLabel();
                Log.d(TAG, "onValueSelected: Selected:" + selected);
                Toast.makeText(getActivity(), selected, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected() {

            }
        });

        /* Add date to PieChart */
        pieChart.setData(pieData);
        pieChart.invalidate();

        /* Add qColors to list View*/
        recyclerChartDescription.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerChartDescription.setAdapter(new ColorAdapter(listColors));
    }

    /**
     * Get a percentage of target expense
     *
     * @param all
     * @param value
     * @return
     */
    private double getExpensePercent(double all, double value) {
        return ((value / all) * 100);
    }

    @Override
    public void onClick(View view) {
        if (view == btnSearch) {
            String startDate = fieldStartDate.getText().toString().trim();
            String endDate = fieldEndDate.getText().toString().trim();

            try {
                if (InternetStatus.getConnectivityStatus(getActivity())) {
                    getAllExpense(sdf.parse(startDate), sdf.parse(endDate));
                } else Toast.makeText(getActivity(), "No internet connection... Try again later", Toast.LENGTH_SHORT).show();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (view == fieldStartDate) {
            final Calendar calendar = Calendar.getInstance();
            final int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            // date datePicker dialog
            dialogStartDate = new DatePickerDialog(getContext(),
                    new DatePickerDialog.OnDateSetListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @SuppressLint("DefaultLocale")
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            @SuppressLint("SimpleDateFormat")
                            SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(0); ////disable dates
                            cal.set(year, monthOfYear, dayOfMonth, 0, 0, 0);

                            String outputDate = outputDateFormat.format(cal.getTime());

                            fieldStartDate.setText(outputDate);

                            Log.d(TAG, String.format("onDateSet: selected date: %s ", outputDate));
                        }
                    }, year, month, day);


            dialogStartDate.show();
        }
        if (view == fieldEndDate) {
            final Calendar calendar = Calendar.getInstance();
            final int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            // date datePicker dialog
            dialogEndDate = new DatePickerDialog(getContext(),
                    new DatePickerDialog.OnDateSetListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @SuppressLint("DefaultLocale")
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            @SuppressLint("SimpleDateFormat")
                            SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(0); ////disable dates
                            cal.set(year, monthOfYear, dayOfMonth, 0, 0, 0);

                            String outputDate = outputDateFormat.format(cal.getTime());

                            fieldEndDate.setText(outputDate);

                            Log.d(TAG, String.format("onDateSet: selected date: %s ", outputDate));
                        }
                    }, year, month, day);


            dialogEndDate.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: resume");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ReceiverAction.STATS_MONTHLY_ACTION);
        getContext().registerReceiver(mServiceReceiver, filter);
        firebaseManager.firebaseMenu(FirebaseAction.STATS_MONTHLY_BALANCE);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: puase");
        try {
            if (mServiceReceiver != null) {
                getContext().unregisterReceiver(mServiceReceiver);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
