package com.example.traveljurnalapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TripDetailsActivity extends AppCompatActivity {

    private TextView tripTitle, tripDates, categoryText, moneySpent, notePreview;
    private Button viewNoteButton;
    private ImageButton addPhotoButton;
    private Date startDate, endDate;
    private String tripId;
    private static final int REQUEST_CODE_UPLOAD_PHOTOS = 1002;
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
        addPhotoButton = findViewById(R.id.addPhotoButton);

        db = FirebaseFirestore.getInstance();

        tripId = getIntent().getStringExtra("tripId");
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
        if(endDate == null){
            String dateText = displayFormat.format(startDate)  + " 2025";
            tripDates.setText(dateText);
        }


        if (note != null && !note.isEmpty()) {
            String preview = note.split("\n", 2)[0];
            notePreview.setText("• " + preview);
        } else {
            notePreview.setText("• No notes yet");
}

        viewNoteButton.setOnClickListener(v -> {
            Intent intent = new Intent(TripDetailsActivity.this, NotesActivity.class);
            intent.putExtra("tripId", tripId);
            startActivityForResult(intent, REQUEST_CODE_UPLOAD_PHOTOS);
        });

        addPhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(TripDetailsActivity.this, UploadPhotosActivity.class);
            intent.putExtra("tripId", tripId);
            startActivityForResult(intent, REQUEST_CODE_UPLOAD_PHOTOS);
        });

        viewImages(tripId);
    }



    private void viewImages(String tripId){
        List<Photo> photoList = new ArrayList<>();
        TripPhotoAdapter photoAdapter = new TripPhotoAdapter(this, photoList);
        RecyclerView photosGrid = findViewById(R.id.photosGrid);

        photosGrid.setLayoutManager(new GridLayoutManager(this, 3)); // 3 columns
        photosGrid.setAdapter(photoAdapter);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("trips")
                .document(tripId)
                .collection("photos")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot dc : querySnapshot.getDocuments()) {
                        Photo photo = dc.toObject(Photo.class);
                        photo.setTripId(tripId);
                        photo.setPhotoId(dc.getId()); // ← Add this too!
                        photo.setFavorite(dc.getBoolean("isFavorite") != null && dc.getBoolean("isFavorite"));
                        photoList.add(photo);
                    }
                    photoAdapter.notifyDataSetChanged();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_UPLOAD_PHOTOS && resultCode == RESULT_OK) {
            if (tripId != null) {
                viewImages(tripId);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        viewImages(tripId);
    }
}
