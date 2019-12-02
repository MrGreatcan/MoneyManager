package com.greatcan.moneysaver;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.greatcan.moneysaver.adapters.CategoryAdapter;
import com.greatcan.moneysaver.models.CategoryModels;
import com.greatcan.moneysaver.models.IncomeModels;
import com.greatcan.moneysaver.models.UserMoneyModel;

import java.util.ArrayList;

public class CategoryActivity extends AppCompatActivity {

    private static final String TAG = "CategoryActivity";

    //Objects
    private GridView gvCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Log.d(TAG, "onCreate: starting");
        gvCategory = findViewById(R.id.gvCategory);

        ArrayList<CategoryModels> listCategory = new ArrayList<>();

        for (CategoryEnum item : CategoryEnum.values()) {
            listCategory.add(new CategoryModels(item.toString(), item.getResources()));
        }

        gvCategory.setAdapter(new CategoryAdapter(listCategory, this));
    }

}
