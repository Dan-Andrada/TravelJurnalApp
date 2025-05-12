package com.example.traveljurnalapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class NotesActivity extends AppCompatActivity {

    private EditText notesEditText;
    private Button editButton;
    private boolean isEditing = false;
    private String tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        notesEditText = findViewById(R.id.notesEditText);
        editButton = findViewById(R.id.editButton);

        tripId = getIntent().getStringExtra("tripId");

        String savedNote = NotesStorage.getNote(tripId);
        notesEditText.setText(savedNote);

        //harcodat

        editButton.setOnClickListener(v -> {
            if (!isEditing) {
                isEditing = true;
                notesEditText.setEnabled(true);
                notesEditText.requestFocus();
                editButton.setText("Save");
            } else {
                isEditing = false;
                notesEditText.setEnabled(false);
                editButton.setText("Edit");

                //salvare
                String updatedNotes = notesEditText.getText().toString();
                NotesStorage.saveNote(tripId, updatedNotes);
                //finish();

                // saveNotesToFirebase(tripId, updatedNotes);
            }
        });
    }
}

