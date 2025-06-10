package com.example.traveljurnalapp;

public class Photo {
    private String url;
    private boolean isFavorite;
    private String tripId;
    private String photoId;

    public Photo() {
        // Needed for Firestore
    }

    public String getUrl() {
        return url;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getTripId() {
        return tripId;
    }

    public String getPhotoId() {
        return photoId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }
}


