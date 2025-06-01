package com.example.traveljurnalapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class NotesActivity extends AppCompatActivity {

    private EditText notesEditText;
    private Button editButton;
    private boolean isEditing = false;
    private String tripId;
    private String userId;
    private FirebaseFirestore db;
    private boolean isTemporaryNote = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        notesEditText = findViewById(R.id.notesEditText);
        editButton = findViewById(R.id.editButton);
        notesEditText.setEnabled(false);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        tripId = getIntent().getStringExtra("tripId");

        if (tripId == null || tripId.isEmpty()) {
            isTemporaryNote = true;
            tripId = null;
        }

        String tempNote = getIntent().getStringExtra("tempNote");
        if (tempNote != null) {
            notesEditText.setText(tempNote);
        } else if (!isTemporaryNote) {
            loadNoteFromFirestore();
        }

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

                String updatedNote = notesEditText.getText().toString().trim();

                if (isTemporaryNote) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("note", updatedNote);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    saveNoteToFirestore(updatedNote);
                }
            }
        });
    }

    private void loadNoteFromFirestore() {
        DocumentReference noteRef = db.collection("users")
                .document(userId)
                .collection("trips")
                .document(tripId);

        noteRef.get().addOnSuccessListener(documentSnapshot -> {
            String note = documentSnapshot.getString("note");
            notesEditText.setText(note != null ? note : "");
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load note", Toast.LENGTH_SHORT).show()
        );
    }

    private void saveNoteToFirestore(String note) {
        DocumentReference noteRef = db.collection("users")
                .document(userId)
                .collection("trips")
                .document(tripId);

        noteRef.update("note", note)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save note", Toast.LENGTH_SHORT).show());
    }
}
