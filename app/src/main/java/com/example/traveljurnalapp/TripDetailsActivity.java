package com.example.traveljurnalapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.traveljurnalapp.NotesActivity;
import com.example.traveljurnalapp.NotesStorage;
import com.example.traveljurnalapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TripDetailsActivity extends AppCompatActivity {

    private TextView tripTitle, tripDates, categoryText, moneySpent, notePreview;
    private Button viewNoteButton, newAlbumButton;
    private ImageButton addPhotoButton;
    private Date startDate, endDate;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_details);

        tripTitle = findViewById(R.id.tripTitle);
        tripDates = findViewById(R.id.tripDates);
        categoryText = findViewById(R.id.categoryText);
        moneySpent = findViewById(R.id.moneySpent);
        notePreview = findViewById(R.id.notePreview);
        viewNoteButton = findViewById(R.id.viewNoteButton);
        newAlbumButton = findViewById(R.id.newAlbumButton);
        addPhotoButton = findViewById(R.id.addPhotoButton);

        db = FirebaseFirestore.getInstance();

        String tripId = getIntent().getStringExtra("tripId");
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (tripId != null && userId != null) {
            db.collection("users")
                    .document(userId)
                    .collection("trips")
                    .document(tripId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            updateUIWithTrip(documentSnapshot, tripId);
                        } else {
                            Toast.makeText(this, "Trip not found", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load trip: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateUIWithTrip(DocumentSnapshot doc, String tripId) {
        String placeName = doc.getString("placeName");
        String type = doc.getString("type");
        String start = doc.getString("startDate");
        String end = doc.getString("endDate");
        String note = doc.getString("note");
        Long spending = doc.getLong("spending");

        tripTitle.setText("Your trip to " + placeName + ":");
        categoryText.setText("• Category: " + type);
        moneySpent.setText("• Money spent: " + (spending != null ? spending : 0) + " $");

        SimpleDateFormat dbFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM", Locale.ENGLISH);
        try {
            startDate = dbFormat.parse(start);
            if(endDate != null)
                endDate = dbFormat.parse(end);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (startDate != null && endDate != null) {
            String dateText = displayFormat.format(startDate) + " - " + displayFormat.format(endDate) + " 2025";
            tripDates.setText(dateText);
        }

        String fullNote = NotesStorage.getNote(tripId);
        String preview = fullNote.split("\n", 2)[0];
        notePreview.setText("• " + preview);

        viewNoteButton.setOnClickListener(v -> {
            Intent intent = new Intent(TripDetailsActivity.this, NotesActivity.class);
            intent.putExtra("tripId", tripId);
            startActivity(intent);
        });

        newAlbumButton.setOnClickListener(v -> {
            // TODO: implement album creation
        });

        addPhotoButton.setOnClickListener(v -> {
            // TODO: implement photo selector
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
