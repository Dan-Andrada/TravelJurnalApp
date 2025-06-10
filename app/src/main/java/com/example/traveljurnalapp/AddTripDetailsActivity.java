package com.example.traveljurnalapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddTripDetailsActivity extends AppCompatActivity {

    private EditText spendingInput, placeNameInput;
    private Button doneButton;
    private Button btnRestaurant;
    private Button btnHotel;
    private CalendarView calendarView;
    private Date startDate = null;
    private Date endDate = null;
    private boolean selectingStart = true;
    private String type = "trip";
    Button selectedBtn = null;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String temporaryNote = "";
    private static final int REQUEST_CODE_UPLOAD_PHOTOS = 1001;
    private ArrayList<Uri> selectedImages = new ArrayList<>();
    private int favoriteIndex = -1;
    private String favoriteImageUrl;




    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip_details);

        spendingInput = findViewById(R.id.spendingInput);
        placeNameInput = findViewById(R.id.placeNameInput);
        Button uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        Button notesButton = findViewById(R.id.notesButton);
        doneButton = findViewById(R.id.doneButton);
        btnRestaurant = findViewById(R.id.btnRestaurant);
        btnHotel = findViewById(R.id.btnHotel);
        Button btnActivity = findViewById(R.id.btnActivity);
        Button btnMuseum = findViewById(R.id.btnMuseum);
        calendarView = findViewById(R.id.calendarView);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        String suggestedName = getIntent().getStringExtra("suggestedName");
        if (suggestedName != null) {
            placeNameInput.setText(suggestedName);
        }

        double lat = getIntent().getDoubleExtra("lat", 0);
        double lng = getIntent().getDoubleExtra("lng", 0);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(year, month, dayOfMonth);
            Date pickedDate = selectedCal.getTime();

            if (selectingStart) {
                startDate = pickedDate;
                Toast.makeText(this, "Start date selected", Toast.LENGTH_SHORT).show();
            } else {
                if (startDate != null && pickedDate.after(startDate)) {
                    endDate = pickedDate;
                    Toast.makeText(this, "End date selected", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
                }
            }
            selectingStart = !selectingStart;
        });

        View.OnClickListener typeClickListener = view -> {
            Button clickedBtn = (Button) view;
            if (selectedBtn == clickedBtn) {
                clickedBtn.setBackgroundResource(R.color.screenBg);
                selectedBtn = null;
                type = "trip";
            } else {
                if (selectedBtn != null)
                    selectedBtn.setBackgroundResource(R.color.screenBg);

                clickedBtn.setBackgroundResource(R.drawable.button_selected);
                selectedBtn = clickedBtn;
                type = clickedBtn.getText().toString();
            }
        };

        btnRestaurant.setOnClickListener(typeClickListener);
        btnHotel.setOnClickListener(typeClickListener);
        btnActivity.setOnClickListener(typeClickListener);
        btnMuseum.setOnClickListener(typeClickListener);

        ActivityResultLauncher<Intent> notesLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        temporaryNote = result.getData().getStringExtra("note");
                    }
                }
        );

        uploadPhotoButton.setOnClickListener(view -> {
            Intent intent = new Intent(AddTripDetailsActivity.this, UploadPhotosActivity.class);
            intent.putParcelableArrayListExtra("selectedImages", selectedImages);
            intent.putExtra("favoriteIndex", favoriteIndex);
            startActivityForResult(intent, REQUEST_CODE_UPLOAD_PHOTOS);

        });


        notesButton.setOnClickListener(view -> {
            Intent intent = new Intent(AddTripDetailsActivity.this, NotesActivity.class);
            intent.putExtra("tempNote", temporaryNote);
            notesLauncher.launch(intent);
        });

        doneButton.setOnClickListener(v -> {
            String placeName = placeNameInput.getText().toString().trim();

            if (placeName.isEmpty()) {
                Toast.makeText(this, "Please enter a place name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (startDate == null) {
                Toast.makeText(this, "Please select a start date", Toast.LENGTH_SHORT).show();
                return;
            }

            String spending = spendingInput.getText().toString().trim();
            if (spending.isEmpty())
                spending = "0";

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

            Map<String, Object> tripData = new HashMap<>();
            tripData.put("placeName", placeName);
            tripData.put("startDate", simpleDateFormat.format(startDate));
            if (endDate != null) {
                tripData.put("endDate", simpleDateFormat.format(endDate));
            }
            tripData.put("spending", Double.parseDouble(spending));
            tripData.put("type", type);
            tripData.put("note", temporaryNote);
            tripData.put("lat",lat);
            tripData.put("lng",lng);

            FirebaseUser user = mAuth.getCurrentUser();
            assert user != null;

            db.collection("users")
                    .document(user.getUid())
                    .collection("trips")
                    .add(tripData)
                    .addOnSuccessListener(documentReference -> {

                        String tripId = documentReference.getId();

                        for (int i = 0; i < selectedImages.size(); i++) {
                            Uri imageUri = selectedImages.get(i);

                            try {
                                File tempFile = copyUriToFile(this, imageUri);
                                Uri tempUri = Uri.fromFile(tempFile);

                                StorageReference imageRef = FirebaseStorage.getInstance()
                                        .getReference("users/" + user.getUid() + "/trips/" + tripId + "/photo_" + i + ".jpg");

                                final int currentIndex = i;
                                imageRef.putFile(tempUri)
                                        .addOnSuccessListener(taskSnapshot ->
                                                imageRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                                                    favoriteImageUrl = downloadUrl.toString();
                                                    Map<String, Object> photoData = new HashMap<>();
                                                    photoData.put("url", favoriteImageUrl);
                                                    photoData.put("isFavorite", currentIndex == favoriteIndex);

                                                    db.collection("users")
                                                            .document(user.getUid())
                                                            .collection("trips")
                                                            .document(tripId)
                                                            .collection("photos")
                                                            .add(photoData);
                                                }))
                                        .addOnFailureListener(e ->
                                                Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(this, "Failed to copy image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }


                        Toast.makeText(this, "Trip saved", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent();
                        intent.putExtra("lat", lat);
                        intent.putExtra("lng", lng);
                        intent.putExtra("title", placeName);
                        intent.putExtra("coverUrl", favoriteImageUrl);
                        intent.putExtra("tripId",tripId);
                        setResult(RESULT_OK, intent);
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        });
    }

    private File copyUriToFile(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        File tempFile = File.createTempFile("image_", ".jpg", context.getCacheDir());
        OutputStream outputStream = new FileOutputStream(tempFile);

        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) > 0) {
            outputStream.write(buffer, 0, length);
        }

        inputStream.close();
        outputStream.close();

        return tempFile;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_UPLOAD_PHOTOS && resultCode == RESULT_OK && data != null) {
            selectedImages = data.getParcelableArrayListExtra("selectedImages");
            favoriteIndex = data.getIntExtra("favoriteIndex", -1);
            Toast.makeText(this, selectedImages.size() + " photos selected", Toast.LENGTH_SHORT).show();
        }
    }

}
