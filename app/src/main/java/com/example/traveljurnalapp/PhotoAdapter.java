package com.example.traveljurnalapp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private final Context context;
    private final List<Uri> images;
    private final PhotoActionListener listener;
    private int favoritePosition = -1;

    public PhotoAdapter(Context context, List<Uri> images, PhotoActionListener listener) {
        this.context = context;
        this.images = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.icons_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Uri imageUri = images.get(position);
        Glide.with(context).load(imageUri).into(holder.imageView);

        if (position == favoritePosition) {
            holder.favoriteIcon.setImageResource(R.drawable.full_heart_icon);
        } else {
            holder.favoriteIcon.setImageResource(R.drawable.heart_icon);
        }

        holder.favoriteIcon.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;

            if (pos == favoritePosition) return;

            new AlertDialog.Builder(context)
                    .setTitle("You chose this photo as the pin cover.")
                    .setMessage("Would you like to continue?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        favoritePosition = pos;
                        notifyDataSetChanged();
                        if (listener != null) listener.onFavoritePhoto(pos);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });


        holder.removeButton.setOnClickListener(v -> listener.onRemovePhoto(position));

        holder.imageView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullScreenGalleryActivity.class);

            ArrayList<String> imageUrls = new ArrayList<>();
            for (Uri uri : images) {
                imageUrls.add(uri.toString());
            }

            intent.putStringArrayListExtra("imageUrls", imageUrls);
            intent.putExtra("startIndex", position);
            context.startActivity(intent);
        });
    }
    public void promptFavoriteChange(int position) {
        if (position == favoritePosition) return;

        new AlertDialog.Builder(context)
                .setTitle("You chose this photo as the pin cover.")
                .setMessage("Would you like to continue?")
                .setPositiveButton("Yes", (dialog, which) -> setFavoritePosition(position))
                .setNegativeButton("No", null)
                .show();
    }


    @Override
    public int getItemCount() {
        return images.size();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView, removeButton, favoriteIcon;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            removeButton = itemView.findViewById(R.id.removeIcon);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
        }
    }

    public void setFavoritePosition(int position) {
        int oldFavorite = favoritePosition;
        favoritePosition = position;
        notifyItemChanged(oldFavorite);
        notifyItemChanged(favoritePosition);
    }

}

interface PhotoActionListener {

    void onRemovePhoto(int position);
    void onFavoritePhoto(int position);
}

