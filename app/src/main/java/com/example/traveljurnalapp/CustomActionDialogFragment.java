package com.example.traveljurnalapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class CustomActionDialogFragment extends DialogFragment {

    private final String title;
    private final Runnable onYes;
    private final Runnable onNo;

    public CustomActionDialogFragment(String title, Runnable onYes, Runnable onNo) {
        this.title = title;
        this.onYes = onYes;
        this.onNo = onNo;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.custom_dialog, container, false);

        TextView titleView = view.findViewById(R.id.dialogTitle);
        Button yesButton = view.findViewById(R.id.yesButton);
        Button noButton = view.findViewById(R.id.noButton);

        titleView.setText(title);

        yesButton.setOnClickListener(v -> {
            if (onYes != null) onYes.run();
            dismiss();
        });

        noButton.setOnClickListener(v -> {
            if (onNo != null) onNo.run();
            dismiss();
        });

        return view;
    }
}
