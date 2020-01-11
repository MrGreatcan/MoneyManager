package com.greatcan.moneysaver.dialogs;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.greatcan.moneysaver.R;
import com.greatcan.moneysaver.configuration.DateRange;
import com.greatcan.moneysaver.configuration.FirebaseReferences;
import com.greatcan.moneysaver.models.FinanceModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ExportDialog extends DialogFragment implements View.OnClickListener {

    private static final String TAG = "ExportDialog";

    //Objects
    private Button btnOk;
    private TextView tvCancel;
    private EditText fieldStartDate, fieldEndDate;
    private SimpleDateFormat sdf;

    //Dialogs
    private Dialog dialogStartDate;
    private Dialog dialogEndDate;

    //Firebase
    private FirebaseFirestore db;
    private FirebaseUser currentUser;


    @SuppressLint("SimpleDateFormat")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_export, container, false);

        Log.d(TAG, "onCreateView: started.");

        btnOk = view.findViewById(R.id.btnOk);
        tvCancel = view.findViewById(R.id.tvCancel);
        fieldStartDate = view.findViewById(R.id.fieldStartDate);
        fieldEndDate = view.findViewById(R.id.fieldEndDate);

        /* Click listener */
        btnOk.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
        fieldStartDate.setOnClickListener(this);
        fieldEndDate.setOnClickListener(this);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        /* Simple Date Format */
        sdf = new SimpleDateFormat("dd-MM-yyyy");
        sdf.setLenient(false);

        return view;
    }

    private void getReportFromRange(final Date sStartDate, final Date sEndDate) {
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

                            try {
                                Date dateStart = sdf.parse(sdf.format(sStartDate));
                                Date dateEnd = sdf.parse(sdf.format(sEndDate));
                                Date target = sdf.parse(model.getData());

                                if (DateRange.isDateRange(dateStart, dateEnd, target)) {
                                    Log.d(TAG, "onSuccess: data: " + documentSnapshot.getData());
                                } else {
                                    Log.d(TAG, "onSuccess: not in range");
                                }
                            } catch (ParseException e) {
                                Log.d(TAG, "onSuccess: unknown date");
                            }

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: unknown date");
                    }
                });
    }



    @Override
    public void onClick(View v) {
        if (v == fieldStartDate) {
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
        if (v == fieldEndDate) {
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
        if (v == btnOk){
            String startDate = fieldStartDate.getText().toString().trim();
            String endDate = fieldEndDate.getText().toString().trim();

            try {
                getReportFromRange(sdf.parse(startDate), sdf.parse(endDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (v == tvCancel){
            dismiss();
        }
    }

}
