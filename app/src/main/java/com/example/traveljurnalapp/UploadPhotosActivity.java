package com.example.traveljurnalapp;;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadPhotosActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private List<Uri> imageUris = new ArrayList<>();
    private PhotoAdapter adapter;
    private FloatingActionButton addPhotosButton;
    private Button doneButton;
    private int favoritePosition;
    private String tripId;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_photos);
        tripId = getIntent().getStringExtra("tripId");

        ArrayList<Uri> incomingUris = getIntent().getParcelableArrayListExtra("selectedImages");
        if (incomingUris != null && !incomingUris.isEmpty()) {
            imageUris.addAll(incomingUris);
        }
        favoritePosition = getIntent().getIntExtra("favoriteIndex", -1);

        RecyclerView recyclerView = findViewById(R.id.photoRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new PhotoAdapter(this, imageUris, new PhotoActionListener() {
            @Override
            public void onRemovePhoto(int position) {
                imageUris.remove(position);
                if (position == favoritePosition) favoritePosition = -1;
                adapter.notifyItemRemoved(position);
            }


            @Override
            public void onFavoritePhoto(int position) {
                adapter.promptFavoriteChange(position);
                favoritePosition = position;
            }
        });
        recyclerView.setAdapter(adapter);

        addPhotosButton = findViewById(R.id.addPhotosButton);
        doneButton = findViewById(R.id.doneButton);

        addPhotosButton.setOnClickListener(v -> checkPermissionAndPickImages());

        doneButton.setOnClickListener(v -> {
            if (imageUris.isEmpty()) {
                Toast.makeText(this, "No photos selected", Toast.LENGTH_SHORT).show();
                return;
            }
            if (tripId != null) {
                uploadPhotosToFirebase();
            } else {
                Intent resultIntent = new Intent();
                resultIntent.putParcelableArrayListExtra("selectedImages", new ArrayList<>(imageUris));
                resultIntent.putExtra("favoriteIndex", favoritePosition);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

    }

    private void uploadPhotosToFirebase(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();

        db.collection("users")
                .document(user.getUid())
                .collection("trips")
                .document(tripId)
                .collection("photos")
                .whereEqualTo("isFavorite", true)
                .get()
                .addOnSuccessListener(existingFavorites -> {

                    boolean alreadyHasFavorite = !existingFavorites.isEmpty();

                    Runnable uploadTask = () -> {
                        for (int i = 0; i < imageUris.size(); i++) {
                            Uri imageUri = imageUris.get(i);

                            try {
                                File copiedFile = copyUriToFile(this, imageUri);
                                Uri tempUri = Uri.fromFile(copiedFile);

                                StorageReference ref = storage.getReference("users/" + user.getUid() + "/trips/" + tripId + "/photo_" + System.currentTimeMillis() + ".jpg");

                                int finalI = i;
                                ref.putFile(tempUri)
                                        .addOnSuccessListener(taskSnapshot ->
                                                ref.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                                                    Map<String, Object> photoData = new HashMap<>();
                                                    photoData.put("url", downloadUrl.toString());
                                                    photoData.put("isFavorite", finalI == favoritePosition && !alreadyHasFavorite);

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

                        Toast.makeText(this, "Photos uploaded", Toast.LENGTH_SHORT).show();
                        finish();
                    };

                    if (alreadyHasFavorite && favoritePosition != -1) {
                        new AlertDialog.Builder(this)
                                .setTitle("Favorite already exists")
                                .setMessage("This trip already has a favorite photo. Do you want to replace it with the new one?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                    uploadTask.run();
                                })
                                .setNegativeButton("No", (dialog, which) -> {
                                    favoritePosition = -1; // cancel favorite
                                    uploadTask.run();
                                })
                                .show();
                    } else {
                        uploadTask.run();
                    }

                });
    }

    private void checkPermissionAndPickImages() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) != getPackageManager().PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, REQUEST_CODE);
        } else {
            pickImages.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        }
    }

    private final ActivityResultLauncher<PickVisualMediaRequest> pickImages =
            registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(), uris -> {
                if (!uris.isEmpty()) {
                    for (Uri uri : uris) {
                        try {
                            File copied = copyUriToFile(this, uri);
                            imageUris.add(Uri.fromFile(copied)); // temp file URI
                        } catch (IOException e) {
                            Toast.makeText(this, "Failed to copy photo", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();

                }
            });
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

}
