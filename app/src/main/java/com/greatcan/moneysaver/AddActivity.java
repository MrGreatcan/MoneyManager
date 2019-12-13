package com.greatcan.moneysaver;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.greatcan.moneysaver.adapters.CategoryAdapter;
import com.greatcan.moneysaver.models.CategoryModels;
import com.greatcan.moneysaver.models.ExpensesModels;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;

public class AddActivity extends AppCompatActivity implements View.OnClickListener, CategoryAdapter.CategoryCallback {

    private static final String TAG = "AddActivity";

    //Objects
    private DatePickerDialog picker;
    private Button btnRemove;
    private Button btnDate;
    private Button btnOk;
    private RelativeLayout rlCategory;
    private EditText fieldNote;
    private TextView tvAmount;
    private Dialog dialog;

    //Category objects
    private ImageView ivIcon;
    private TextView tvTextCategory;

    //Firebase
    private FirebaseFirestore db;
    private static final String EXPENSE_REFERENCE = "Expense";

    //Variables
    private static final String TYPE = "Expenses";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        Log.d(TAG, "onCreate: starting");

        db = FirebaseFirestore.getInstance();

        //Init objects
        btnRemove = findViewById(R.id.btnRemove);
        btnDate = findViewById(R.id.btnDate);
        btnOk = findViewById(R.id.btnOk);
        rlCategory = findViewById(R.id.rlCategory);
        fieldNote = findViewById(R.id.fieldNote);
        tvAmount = findViewById(R.id.tvAmount);

        //Setup click listener
        btnRemove.setOnClickListener(this);
        btnDate.setOnClickListener(this);
        rlCategory.setOnClickListener(this);
        btnOk.setOnClickListener(this);

        //Init objects for category button
        ivIcon = findViewById(R.id.ivIcon);
        tvTextCategory = findViewById(R.id.tvTextCategory);

        //Filter for amount
        tvAmount.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(8, 2)});

        dialog = new Dialog(this);

        tvTextCategory.setText(CategoryEnum.Others.toString());
        ivIcon.setImageResource(CategoryEnum.Others.getResources());
    }

    /**
     * Button click in numpad
     *
     * @param view
     */
    public void numpadClick(View view) {
        Log.d(TAG, "numpadClick: clicked on: " + ((Button) view).getText());
        tvAmount.append(((Button) view).getText());
    }

    /**
     * For the opening dialog with category
     */
    private void openDialogCategory() {
        //final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_category);

        GridView gvCategory = dialog.findViewById(R.id.gvCategory);
        gvCategory.setAdapter(new CategoryAdapter(getListCategories(), this, AddActivity.this));

        dialog.show();
    }

    @Override
    public void onItemClicked(@NotNull String text, int icon) {
        Log.d(TAG, "onItemClicked: Clicked on: " + text);
        tvTextCategory.setText(text);
        ivIcon.setImageResource(icon);
        dialog.dismiss();
    }

    /**
     * Getting the list with all categories
     *
     * @return
     */
    private ArrayList<CategoryModels> getListCategories() {
        ArrayList<CategoryModels> listCategory = new ArrayList<>();

        for (CategoryEnum item : CategoryEnum.values()) {
            listCategory.add(new CategoryModels(item.toString(), item.getResources()));
        }
        return listCategory;
    }


    @Override
    public void onClick(View view) {
        if (view == btnDate) {
            final Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            // date picker dialog
            picker = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            btnDate.setText(dayOfMonth + "." + (monthOfYear + 1) + "." + year);
                        }
                    }, year, month, day);
            picker.show();
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
            String type = TYPE;
            String category = tvTextCategory.getText().toString().trim();
            String date = btnDate.getText().toString().trim();
            String amount = tvAmount.getText().toString().trim();
            String note = fieldNote.getText().toString().trim();

            if (!date.isEmpty() && !amount.isEmpty()) {

                if (note.isEmpty()) {
                    note = tvTextCategory.getText().toString().trim();
                }

                ExpensesModels data = new ExpensesModels("", type, category, date, amount, note);

                String userUID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                db.collection("MoneyManager")
                        .document(userUID)
                        .collection(EXPENSE_REFERENCE)
                        .add(data)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);
                            }
                        });
            } else Toast.makeText(this, "Date and amount is required", Toast.LENGTH_SHORT).show();
        }
    }
}
