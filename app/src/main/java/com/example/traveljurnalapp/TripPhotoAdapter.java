package com.example.traveljurnalapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class TripPhotoAdapter extends RecyclerView.Adapter<TripPhotoAdapter.PhotoViewHolder> {

    private final List<Photo> photos;
    private final Context context;

    public TripPhotoAdapter(Context context, List<Photo> photos) {
        this.context = context;
        this.photos = photos;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trip_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Photo photo = photos.get(position);
        Glide.with(context).load(photo.getUrl()).into(holder.imageView);

        holder.favoriteIcon.setImageResource(photo.isFavorite() ?
                R.drawable.full_heart_icon : R.drawable.heart_icon);

        holder.favoriteIcon.setOnClickListener(v -> {
            if (photo.isFavorite()) return;

            new AlertDialog.Builder(context)
                    .setTitle("Change favorite?")
                    .setMessage("You chose this photo as PIN COVER. Would you like to continue?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        for (Photo p : photos) p.setFavorite(false);
                        photo.setFavorite(true);
                        notifyDataSetChanged();
                        updateFavoriteInFirebase(photo);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        holder.removeIcon.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Delete photo?")
                    .setMessage("Are you sure you want to delete this photo?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        deletePhotoFromFirebase(photo, position);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        holder.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullScreenGalleryActivity.class);
            ArrayList<String> urls = new ArrayList<>();
            for (Photo p : photos) urls.add(p.getUrl());
            intent.putExtra("imageUrls", urls);
            intent.putExtra("startIndex", position);
            context.startActivity(intent);
        });
    }

    private void updateFavoriteInFirebase(Photo selectedPhoto) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        for (Photo p : photos) {
            db.collection("users").document(user.getUid())
                    .collection("trips").document(p.getTripId())
                    .collection("photos").document(p.getPhotoId())
                    .update("isFavorite", p == selectedPhoto);
        }
    }


    private void deletePhotoFromFirebase(Photo photo, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .collection("trips").document(photo.getTripId())
                .collection("photos").document(photo.getPhotoId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    photos.remove(position);
                    notifyItemRemoved(position);
                    Toast.makeText(context, "Photo deleted", Toast.LENGTH_SHORT).show();
                });
    }




    @Override
    public int getItemCount() {
        return photos.size();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, favoriteIcon, removeIcon;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.tripImage);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
            removeIcon = itemView.findViewById(R.id.removeIcon);
        }
    }

}

