package com.example.traveljurnalapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TripDetailsActivity extends AppCompatActivity {

    private TextView tripTitle, tripDates, categoryText, moneySpent, notePreview;
    private Button viewNoteButton, newAlbumButton;
    private ImageButton addPhotoButton;
    private Date startDate, endDate;

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

        //harcodat
        tripTitle.setText("Your trip to Antalya:");
        categoryText.setText("• Category: City");
        moneySpent.setText("• Money spent: 1000 $");

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", Locale.ENGLISH);
        try {
            startDate = sdf.parse("10 March 2025");
            endDate = sdf.parse("16 March 2025");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (startDate != null && endDate != null) {
            SimpleDateFormat displayFormat = new SimpleDateFormat("dd MMM", Locale.ENGLISH);
            String dateText = displayFormat.format(startDate) + " - " + displayFormat.format(endDate) + " 2025";
            tripDates.setText(dateText);
        }

        //harcodat
        String fullNote = "A fost super super.";
        notePreview.setText(fullNote);

        viewNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TripDetailsActivity.this, MapActivity.class);
                intent.putExtra("noteText", fullNote);
                startActivity(intent);
            }
        });

        newAlbumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // deschide pagina pentru creare album - de implementat
            }
        });

        addPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // deschide selectorul de poze - de implementat
            }
        });

        GridLayout photosGrid = findViewById(R.id.photosGrid);
    }
}
