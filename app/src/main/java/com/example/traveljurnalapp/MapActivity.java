package com.example.traveljurnalapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

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
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMaps;
    private EditText searchEditText;
    private ImageButton searchButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchMapButton);

        if(!Places.isInitialized()){
            Places.initialize(getApplicationContext(),"AIzaSyAMsrR3jxgkvlaP-V3rzrLc6ZCf3ivtxLg");
        }

        PlacesClient placesClient = Places.createClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
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
//                                      mMaps.addMarker(new MarkerOptions()
//                                              .position(place.getLatLng())
//                                              .title(place.getName()));
                                  }
                              });
                          } else {
                              Toast.makeText(this, "No results found", Toast.LENGTH_SHORT).show();
                          }
                       })
                       .addOnFailureListener(e -> {
                           Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                       });

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
                    mMaps.addMarker(new MarkerOptions()
                            .position(location)
                            .title("Visited"));
                }
            });
            dialogFragment.show(getSupportFragmentManager(), "VisitedDialog");
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100 && resultCode == RESULT_OK){
            Place place = Autocomplete.getPlaceFromIntent(data);
            if(place.getLatLng() != null){
                mMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15));
                mMaps.addMarker(new MarkerOptions()
                        .position(place.getLatLng())
                        .title(place.getName()));
            }
        }
    }
}
