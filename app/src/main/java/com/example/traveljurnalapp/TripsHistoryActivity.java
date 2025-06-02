package com.example.traveljurnalapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class TripsHistoryActivity extends AppCompatActivity {

    private EditText searchEditText;
    private LinearLayout tripsContainer;
    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trips_history);

        searchEditText = findViewById(R.id.searchEditText);
        tripsContainer = findViewById(R.id.tripListLayout);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadTrips();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                filterTrips(s.toString());
            }
        });
    }

    private void loadTrips() {
        db.collection("users")
                .document(userId)
                .collection("trips")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String placeName = doc.getString("placeName");
                        String tripId = doc.getId();

                        TextView tripView = new TextView(this);
                        tripView.setText("📍 " + placeName);
                        tripView.setTextSize(18);
                        tripView.setTextColor(getResources().getColor(android.R.color.black));
                        tripView.setPadding(20, 30, 20, 30);
                        tripView.setOnClickListener(v -> {
                            Intent intent = new Intent(TripsHistoryActivity.this, TripDetailsActivity.class);
                            intent.putExtra("tripId", tripId);
                            startActivity(intent);
                        });

                        tripsContainer.addView(tripView);
                    }
                });
    }

    private void filterTrips(String query) {
        int count = tripsContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            TextView child = (TextView) tripsContainer.getChildAt(i);
            String text = child.getText().toString().toLowerCase();
            child.setVisibility(text.contains(query.toLowerCase()) ? TextView.VISIBLE : TextView.GONE);
        }
    }
}
