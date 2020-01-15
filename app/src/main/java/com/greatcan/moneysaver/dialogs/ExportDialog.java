package com.greatcan.moneysaver.dialogs;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.greatcan.moneysaver.R;
import com.greatcan.moneysaver.configuration.CSVUtils;
import com.greatcan.moneysaver.configuration.date.DateRange;
import com.greatcan.moneysaver.configuration.firebase.FirebaseReferences;
import com.greatcan.moneysaver.configuration.network.InternetStatus;
import com.greatcan.moneysaver.models.FinanceModel;
import com.greatcan.moneysaver.models.UserModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

public class ExportDialog extends DialogFragment implements View.OnClickListener {

    private static final String TAG = "ExportDialog";

    //Objects
    private Button btnOk;
    private TextView tvCancel;
    private EditText fieldStartDate, fieldEndDate;
    private SimpleDateFormat sdf;
    private ProgressBar progressBar;

    //Dialogs
    private Dialog dialogStartDate;
    private Dialog dialogEndDate;

    //Firebase
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;

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
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

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

    /**
     * Get expense from selected range
     *
     * @param sStartDate
     * @param sEndDate
     */
    private void getReportFromRange(final Date sStartDate, final Date sEndDate) {
        final ArrayList<FinanceModel> listExpense = new ArrayList<>();
        progressBar.setVisibility(View.VISIBLE);
        db.collection("MoneyManager")
                .document(currentUser.getUid())
                .collection(FirebaseReferences.STATS.getReferences())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        String fileName = "";
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            FinanceModel model = documentSnapshot.toObject(FinanceModel.class);

                            try {
                                Date dateStart = sdf.parse(sdf.format(sStartDate));
                                Date dateEnd = sdf.parse(sdf.format(sEndDate));
                                Date target = sdf.parse(model.getDate());

                                fileName =
                                        new SimpleDateFormat("dd.MM").format(dateStart) +
                                                "-" +
                                                new SimpleDateFormat("dd.MM").format(dateEnd);

                                if (DateRange.isDateRange(dateStart, dateEnd, target)) {
                                    Log.d(TAG, "onSuccess: date: " + documentSnapshot.getData());
                                    listExpense.add(model);
                                } else {
                                    Log.d(TAG, "onSuccess: not in range");
                                }
                            } catch (ParseException e) {
                                Log.d(TAG, "onSuccess: unknown date");
                            }
                        }
                        getUserCurrency(fileName, listExpense);


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
     * Receiving user currency from database
     */
    private void getUserCurrency(final String fileName, final ArrayList<FinanceModel> listExpense){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FirebaseReferences.USER.getReferences())
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()){
                            UserModel model = documentSnapshot.toObject(UserModel.class);
                            String currency = model.getCurrency();
                            saveToFile(fileName, listExpense, currency);
                        }
                    }
                });
    }

    /**
     * Export date to CSV file
     */
    @SuppressLint("SimpleDateFormat")
    private void saveToFile(String fileName, ArrayList<FinanceModel> listExpense, String currency) {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            dismiss();

            // Permission is not granted
            // Request for permission
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);

        } else {
            File file = new File(Environment.getExternalStorageDirectory(),
                    Environment.DIRECTORY_DOWNLOADS +
                            File.separatorChar +
                            fileName + ".csv");
            FileWriter os;

            try {
                os = new FileWriter(file);

                /* Header */
                CSVUtils.writeLine(os, Arrays.asList("Category", "Date", "Amount"));

                for (FinanceModel model : listExpense) {
                    Log.d(TAG, "saveToFile: date : " + model.getDate());

                    Date date = new SimpleDateFormat("dd-MM-yyy").parse(model.getDate());
                    CSVUtils.writeLine(os, Arrays.asList(model.getCategory(),
                            new SimpleDateFormat("dd.MM.yyy").format(date),
                            model.getAmount() + currency));
                }

                os.close();
                dismiss();

                Log.d(TAG, "onClick: file was saved");
                Toast.makeText(getActivity(), R.string.config_fileSaved, Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            } catch (IOException e) {
                e.printStackTrace();
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getActivity(), R.string.config_fileError, Toast.LENGTH_SHORT).show();
                dismiss();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
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
        if (v == btnOk) {
            String startDate = fieldStartDate.getText().toString().trim();
            String endDate = fieldEndDate.getText().toString().trim();

            try {
                if (InternetStatus.getConnectivityStatus(getActivity())) {
                    getReportFromRange(sdf.parse(startDate), sdf.parse(endDate));
                } else Toast.makeText(getActivity(), R.string.config_noInternet, Toast.LENGTH_SHORT).show();
            } catch (ParseException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), R.string.config_wrongDate, Toast.LENGTH_SHORT).show();
            }
        }
        if (v == tvCancel) {
            dismiss();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                    Toast.makeText(getActivity(), R.string.config_permissionGranted, Toast.LENGTH_SHORT).show();
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                    Toast.makeText(getActivity(), R.string.config_permissionDenied, Toast.LENGTH_SHORT).show();

                }
                break;
        }
    }

}
