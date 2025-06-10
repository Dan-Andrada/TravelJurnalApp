package com.example.traveljurnalapp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.maps.model.LatLng;

public class VisitedDialogFragment extends DialogFragment {

    private LatLng location;
    private OnVisitedListener listener;

    public VisitedDialogFragment(LatLng location, OnVisitedListener listener) {
        this.location = location;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @NonNull ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.custom_dialog,container,false);

        Button yesButton = view.findViewById(R.id.yesButton);
        Button noButton = view.findViewById(R.id.noButton);

        yesButton.setOnClickListener(v -> {
            if(listener != null){
                listener.onVisitedConfirmed(location);
            }
            dismiss();
        });

        noButton.setOnClickListener(v -> dismiss());
        return view;
    }

    public interface OnVisitedListener {
        void onVisitedConfirmed(LatLng location);
    }
}
