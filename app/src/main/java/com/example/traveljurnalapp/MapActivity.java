package com.example.traveljurnalapp;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMaps;
    private EditText searchEditText;
    private PlacesClient placesClient;
    private ActivityResultLauncher<Intent> tripDetailsLauncher;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        searchEditText = findViewById(R.id.searchEditText);
        ImageButton searchButton = findViewById(R.id.searchMapButton);

        if(!Places.isInitialized()){
            Places.initialize(getApplicationContext(),"AIzaSyAMsrR3jxgkvlaP-V3rzrLc6ZCf3ivtxLg");
        }

        placesClient = Places.createClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        tripDetailsLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if(result.getResultCode() == RESULT_OK && result.getData() != null){
                Intent data = result.getData();
                double lat = data.getDoubleExtra("lat",0);
                double lng = data.getDoubleExtra("lng",0);
                String title = data.getStringExtra("title");

                LatLng location = new LatLng(lat,lng);
                mMaps.addMarker(new MarkerOptions().position(location).title(title));
                mMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(location,20));
            }
        });

        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        searchButton.setOnClickListener(v -> {
           String query = searchEditText.getText().toString().trim();
           if(!query.isEmpty()){
               FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                                                            .setQuery(query).build();
               placesClient.findAutocompletePredictions(request)
                       .addOnSuccessListener(response -> {
                          if(!response.getAutocompletePredictions().isEmpty()){
                              AutocompletePrediction prediction = response.getAutocompletePredictions().get(0);
                              String placeId = prediction.getPlaceId();
                              List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,Place.Field.NAME, Place.Field.LAT_LNG);

                              FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
                              placesClient.fetchPlace(placeRequest).addOnSuccessListener(fetchPlaceResponse -> {
                                  Place place = fetchPlaceResponse.getPlace();
                                  if(place.getLatLng() != null){
                                      mMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),20));
                                  }
                              });
                          } else {
                              Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
                          }
                       })
                       .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

           }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMaps = googleMap;
        LatLng defaultLoc = new LatLng(48.8584, 2.2945); // Eiffel Tower
        mMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLoc, 4));

        mMaps.setOnMapClickListener(latLng -> {
            VisitedDialogFragment dialogFragment = new VisitedDialogFragment(latLng, new VisitedDialogFragment.OnVisitedListener() {
                @Override
                public void onVisitedConfirmed(LatLng location) {
                    Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());

                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.latitude,location.longitude,1);
                        String placeName = "";
                        if(!addresses.isEmpty()){
                            Address address = addresses.get(0);

                            //Try extracting city
                            if (address.getLocality() != null) {
                                placeName = address.getLocality(); // most common
                            } else if (address.getSubAdminArea() != null) {
                                placeName = address.getSubAdminArea(); // fallback
                            } else if (address.getAdminArea() != null) {
                                placeName = address.getAdminArea(); // broader fallback
                            }

                            Intent intent = new Intent(MapActivity.this, AddTripDetailsActivity.class);
                            intent.putExtra("lat", location.latitude);
                            intent.putExtra("lng", location.longitude);
                            intent.putExtra("suggestedName", placeName);
                            System.out.println("MapActivity:\nLat: "+ location.latitude + "\tLng: " + location.longitude);
                            tripDetailsLauncher.launch(intent);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            dialogFragment.show(getSupportFragmentManager(), "VisitedDialog");
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        assert data != null;
        double lat = data.getDoubleExtra("lat",0);
            double lng = data.getDoubleExtra("lng",0);
            String title = data.getStringExtra("title");

            LatLng location = new LatLng(lat, lng);
            mMaps.addMarker(new MarkerOptions().position(location).title(title));
            mMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 20));
    }
}
