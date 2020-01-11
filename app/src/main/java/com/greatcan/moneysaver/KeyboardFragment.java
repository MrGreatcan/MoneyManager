package com.greatcan.moneysaver;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.firestore.FirebaseFirestore;
import com.greatcan.moneysaver.adapters.CategoryAdapter;
import com.greatcan.moneysaver.configuration.CategoryEnum;
import com.greatcan.moneysaver.configuration.DecimalDigitsInputFilter;
import com.greatcan.moneysaver.dialogs.ConfirmAddingDialog;
import com.greatcan.moneysaver.interfaces.OnCategoryCallback;
import com.greatcan.moneysaver.interfaces.OnButtonCallback;
import com.greatcan.moneysaver.models.CategoryModel;
import com.greatcan.moneysaver.models.FinanceModel;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class KeyboardFragment extends Fragment implements
        View.OnClickListener,
        OnCategoryCallback,
        OnButtonCallback {

    private static final String TAG = "KeyboardFragment";

    //Objects
    private DatePickerDialog datePicker;
    private Button btnRemove;
    private Button btnDate;
    private Button btnOk;
    private RelativeLayout rlCategory;
    private TextView tvAmount;
    private Dialog dialog;

    //Category objects
    private ImageView ivIcon;
    private TextView tvTextCategory;

    //Firebase
    private FirebaseFirestore db;

    //Variables
    private String selectedDate = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_keyboad, container, false);
        Log.d(TAG, "onCreateView: starting");
        initObject(view);
        return view;
    }

    private void initObject(View v) {
        Log.d(TAG, "onCreate: initialize object");

        db = FirebaseFirestore.getInstance();

        //Init objects
        btnRemove = v.findViewById(R.id.btnRemove);
        btnDate = v.findViewById(R.id.btnDate);
        btnOk = v.findViewById(R.id.btnOk);
        rlCategory = v.findViewById(R.id.rlCategory);
        //fieldNote = findViewById(R.id.fieldNote);
        tvAmount = v.findViewById(R.id.tvAmount);

        //Setup click listener
        btnRemove.setOnClickListener(this);
        btnDate.setOnClickListener(this);
        rlCategory.setOnClickListener(this);
        btnOk.setOnClickListener(this);

        //Init objects for category button
        ivIcon = v.findViewById(R.id.ivIcon);
        tvTextCategory = v.findViewById(R.id.tvTextCategory);

        //Filter for amount
        tvAmount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});
        tvAmount.setText("");

        dialog = new Dialog(getContext());

        tvTextCategory.setText(CategoryEnum.Others.toString());
        ivIcon.setImageResource(CategoryEnum.Others.getResource());

    }

    /**
     * For the opening dialog with category
     */
    private void openDialogCategory() {
        //final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_category);

        GridView gvCategory = dialog.findViewById(R.id.gvCategory);
        gvCategory.setAdapter(new CategoryAdapter(getListCategories(), getContext(), this));

        dialog.show();
    }

    /**
     * Getting the list with all categories
     *
     * @return
     */
    private ArrayList<CategoryModel> getListCategories() {
        ArrayList<CategoryModel> listCategory = new ArrayList<>();

        for (CategoryEnum item : CategoryEnum.values()) {
            listCategory.add(new CategoryModel(item.toString(), item.getResource()));
        }
        return listCategory;
    }

    /**
     * When the category was selected, get data from the adapter
     *
     * @param text
     * @param icon
     */
    @Override
    public void onItemClicked(@NotNull String text, int icon) {
        Log.d(TAG, "onItemClicked: text: " + text + ", with icon: " + icon);
        tvTextCategory.setText(text);
        ivIcon.setImageResource(icon);
        dialog.dismiss();
    }

    /**
     * When the button on numpad was clicked,
     * then the button number is entered in the field
     *
     * @param number
     */
    @Override
    public void onButtonClicked(@NotNull String number) {
        Log.d(TAG, "onPriceChanged: number: " + number);
        tvAmount.append(number);
    }

    @Override
    public void onClick(View view) {
        if (view == btnDate) {
            final Calendar calendar = Calendar.getInstance();
            final int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            // date datePicker dialog
            datePicker = new DatePickerDialog(getContext(),
                    new DatePickerDialog.OnDateSetListener() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @SuppressLint("DefaultLocale")
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            SimpleDateFormat outputDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                            SimpleDateFormat screenDateFormat = new SimpleDateFormat("dd-MM");
                            Calendar cal = Calendar.getInstance();
                            cal.setTimeInMillis(0); ////disable dates
                            cal.set(year, monthOfYear, dayOfMonth, 0, 0, 0);

                            btnDate.setText(screenDateFormat.format(cal.getTime()));
                            selectedDate = outputDateFormat.format(cal.getTime());

                            Log.d(TAG, String.format("onDateSet: selected date: %s ", selectedDate));
                        }
                    }, year, month, day);


            datePicker.show();
        }
        if (view == rlCategory) {
            openDialogCategory();
        }
        if (view == btnRemove) {
            String text = tvAmount.getText().toString();
            if (!text.equals("")) {
                tvAmount.setText(text.substring(0, text.length() - 1));
            }
        }
        if (view == btnOk) {
            String type = "Expense";
            String category = tvTextCategory.getText().toString().trim();
            String date = selectedDate;
            String amount = tvAmount.getText().toString().trim();

            if (!amount.isEmpty() && !date.equals("")) {
                ((MainMenuActivity) getActivity()).getBottomSheetBehavior().setState(BottomSheetBehavior.STATE_HIDDEN);
                ConfirmAddingDialog confirmAddingDialog = new ConfirmAddingDialog(new FinanceModel("", type, category, date, amount, ""));
                confirmAddingDialog.show(getActivity().getSupportFragmentManager(), "Confirm adding");

                tvAmount.setText("");
                btnDate.setText("date");
            } else Toast.makeText(getActivity(), "Date and amount is required", Toast.LENGTH_SHORT).show();
        }
    }
}
