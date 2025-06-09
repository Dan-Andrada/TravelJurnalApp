package com.example.traveljurnalapp;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMaps;
    private EditText searchEditText;
    private PlacesClient placesClient;
    private ActivityResultLauncher<Intent> tripDetailsLauncher;
    private ImageButton menuButton;
    private LinearLayout menuLayout;
    private TextView accountInfo, tripsHistory;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        searchEditText = findViewById(R.id.searchEditText);
        ImageButton searchButton = findViewById(R.id.searchMapButton);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyAMsrR3jxgkvlaP-V3rzrLc6ZCf3ivtxLg");
        }

        placesClient = Places.createClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        tripDetailsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Intent data = result.getData();
                double lat = data.getDoubleExtra("lat", 0);
                double lng = data.getDoubleExtra("lng", 0);
                LatLng location = new LatLng(lat, lng);
                mMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
            }
        });

        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                        .setQuery(query).build();
                placesClient.findAutocompletePredictions(request)
                        .addOnSuccessListener(response -> {
                            if (!response.getAutocompletePredictions().isEmpty()) {
                                AutocompletePrediction prediction = response.getAutocompletePredictions().get(0);
                                String placeId = prediction.getPlaceId();
                                List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);

                                FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                                placesClient.fetchPlace(placeRequest).addOnSuccessListener(fetchPlaceResponse -> {
                                    Place place = fetchPlaceResponse.getPlace();
                                    if (place.getLatLng() != null) {
                                        mMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
                                    }
                                });
                            } else {
                                Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });

        menuButton = findViewById(R.id.menu);
        menuLayout = findViewById(R.id.menuLayout);
        accountInfo = findViewById(R.id.accountInfo);
        tripsHistory = findViewById(R.id.tripsHistory);

        menuButton.setOnClickListener(view -> {
            if (menuLayout.getVisibility() == VISIBLE) {
                menuLayout.setVisibility(GONE);
            } else {
                menuLayout.setVisibility(VISIBLE);
            }
        });

        accountInfo.setOnClickListener(view -> {
            startActivity(new Intent(MapActivity.this, ProfileActivity.class));
            menuLayout.setVisibility(GONE);
        });

        tripsHistory.setOnClickListener(view -> {
            startActivity(new Intent(MapActivity.this, TripsHistoryActivity.class));
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMaps = googleMap;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        LatLng defaultLoc = new LatLng(48.8584, 2.2945); // Eiffel Tower
        mMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLoc, 4));

        db.collection("users")
                .document(user.getUid())
                .collection("trips")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Double latObj = doc.getDouble("lat");
                        Double lngObj = doc.getDouble("lng");
                        String placeName = doc.getString("placeName");

//                        if (latObj != null && lngObj != null && placeName != null) {
//                            LatLng tripLocation = new LatLng(latObj, lngObj);
//                            mMaps.addMarker(new MarkerOptions().position(tripLocation).title(placeName));
//                        } else {
//                            System.out.println("Ignored: " + doc.getId());
//                        }

                        if (latObj != null && lngObj != null && placeName != null) {
                            LatLng tripLocation = new LatLng(latObj, lngObj);
                            String tripId = doc.getId();

                            // 🔍 Find favorite photo
                            db.collection("users")
                                    .document(user.getUid())
                                    .collection("trips")
                                    .document(tripId)
                                    .collection("photos")
                                    .whereEqualTo("isFavorite", true)
                                    .get()
                                    .addOnSuccessListener(photoSnaps -> {
                                        if (!photoSnaps.isEmpty()) {
                                            String imageUrl = photoSnaps.getDocuments().get(0).getString("url");
                                            createCustomMarkerFromUrl(this, mMaps, imageUrl, tripLocation,tripId);
                                        } else {
                                            // Fallback if no favorite image
                                            Marker marker = mMaps.addMarker(new MarkerOptions().position(tripLocation).title(placeName));
                                            marker.setTag(tripId);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error loading photo for: " + placeName, Toast.LENGTH_SHORT).show();
                                    });
                        }

                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load saved trips", Toast.LENGTH_SHORT).show());

        mMaps.setOnMapClickListener(latLng -> {
            VisitedDialogFragment dialogFragment = new VisitedDialogFragment(latLng, location -> {
                Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());

                try {
                    List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
                    String placeName = "";
                    if (!addresses.isEmpty()) {
                        Address address = addresses.get(0);

                        if (address.getLocality() != null) {
                            placeName = address.getLocality();
                        } else if (address.getSubAdminArea() != null) {
                            placeName = address.getSubAdminArea();
                        } else if (address.getAdminArea() != null) {
                            placeName = address.getAdminArea();
                        }

                        Intent intent = new Intent(MapActivity.this, AddTripDetailsActivity.class);
                        intent.putExtra("lat", location.latitude);
                        intent.putExtra("lng", location.longitude);
                        intent.putExtra("suggestedName", placeName);
                        tripDetailsLauncher.launch(intent);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            dialogFragment.show(getSupportFragmentManager(), "VisitedDialog");
        });

        mMaps.setOnMarkerClickListener(marker -> {
            String tripId = (String) marker.getTag();
            if (tripId != null) {
                Intent intent = new Intent(MapActivity.this, TripDetailsActivity.class);
                intent.putExtra("tripId", tripId);
                startActivity(intent);
            }
            return true; // consume the click
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            double lat = data.getDoubleExtra("lat", 0);
            double lng = data.getDoubleExtra("lng", 0);
            String title = data.getStringExtra("title");

            LatLng location = new LatLng(lat, lng);
            mMaps.addMarker(new MarkerOptions().position(location).title(title));
            mMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        }
    }

    private void createCustomMarkerFromUrl(Context context, GoogleMap map, String imageUrl, LatLng latLng, String tripId) {
        Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .override(180, 120) // Width x Height in pixels — you can tweak these values
                .centerCrop()
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Bitmap withBorder = addWhiteBorder(resource, 8);
                        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(withBorder);

                        Marker marker = map.addMarker(new MarkerOptions().position(latLng).icon(icon));
                        marker.setTag(tripId); // Store trip ID for later

                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // No need to handle this for markers
                    }
                });
    }

    private Bitmap addWhiteBorder(Bitmap original, int borderSize) {
        int width = original.getWidth();
        int height = original.getHeight();
        Bitmap bordered = Bitmap.createBitmap(width + borderSize * 2, height + borderSize * 2, original.getConfig());
        Canvas canvas = new Canvas(bordered);

        // Draw white rectangle
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, bordered.getWidth(), bordered.getHeight(), paint);

        // Draw the original image on top
        canvas.drawBitmap(original, borderSize, borderSize, null);

        return bordered;
    }


}
