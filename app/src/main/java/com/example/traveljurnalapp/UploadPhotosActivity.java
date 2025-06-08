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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class UploadPhotosActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private List<Uri> imageUris = new ArrayList<>();
    private PhotoAdapter adapter;
    private Button addPhotosButton;
    private Button doneButton;
    private int favoritePosition = -1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_photos);

        ArrayList<Uri> incomingUris = getIntent().getParcelableArrayListExtra("selectedImages");
        if (incomingUris != null && !incomingUris.isEmpty()) {
            imageUris.addAll(incomingUris);
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
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
                if (favoritePosition == -1) {
                    favoritePosition = position;
                    adapter.setFavoritePosition(position);
                } else if (favoritePosition != position) {
                    // Show confirmation dialog
                    new AlertDialog.Builder(UploadPhotosActivity.this)
                            .setTitle("Change Favorite")
                            .setMessage("You're about to change your favorite photo. Continue?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                favoritePosition = position;
                                adapter.setFavoritePosition(position);
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
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
            Intent resultIntent = new Intent();
            resultIntent.putParcelableArrayListExtra("selectedImages", new ArrayList<>(imageUris));
            setResult(RESULT_OK, resultIntent);
            finish();
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
