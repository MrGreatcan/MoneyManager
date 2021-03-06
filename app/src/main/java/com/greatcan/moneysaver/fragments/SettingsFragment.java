package com.greatcan.moneysaver.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.greatcan.moneysaver.R;
import com.greatcan.moneysaver.dialogs.ConfirmSignOutDialog;
import com.greatcan.moneysaver.dialogs.ExportDialog;
import com.greatcan.moneysaver.dialogs.UpdateCurrencyDialog;

import java.util.ArrayList;

public class SettingsFragment extends Fragment {

    private static final String TAG = "SettingsFragment";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        Log.d(TAG, "onCreateView: starting");
        setupSettings(view);
        return view;
    }

    /**
     * Setting up list with settings
     *
     * @param v
     */
    private void setupSettings(View v) {
        ListView listSettings = v.findViewById(R.id.listSettings);
        listSettings.setDivider(null);

        ArrayList<String> arrayListOptions = new ArrayList<>();
        arrayListOptions.add(getResources().getString(R.string.config_tExport));
        arrayListOptions.add(getResources().getString(R.string.config_tCurrency));
        arrayListOptions.add(getResources().getString(R.string.config_tSignOut));

        ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, arrayListOptions);
        listSettings.setAdapter(adapter);
        listSettings.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: navigation to fragment #: " + position);
                if (position == 0) {
                    Log.d(TAG, "onItemClick: Export date: ");
                    ExportDialog exportDialog = new ExportDialog();
                    exportDialog.show(getActivity().getSupportFragmentManager(), "Export dialog");
                }
                if (position == 1){
                    Log.d(TAG, "onItemClick: Select currency");
                    UpdateCurrencyDialog updateCurrencyDialog = new UpdateCurrencyDialog();
                    updateCurrencyDialog.show(getActivity().getSupportFragmentManager(), "Update currency");
                }
                if (position == 2){
                    Log.d(TAG, "onItemClick: Sign out");
                    ConfirmSignOutDialog confirmSignOutDialog = new ConfirmSignOutDialog();
                    confirmSignOutDialog.show(getActivity().getSupportFragmentManager(), "Sign out dialog");
                }
            }
        });
    }
}
