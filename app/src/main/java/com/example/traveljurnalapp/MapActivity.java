package com.example.traveljurnalapp;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
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

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        searchButton.setOnClickListener(v -> {
            String location = searchEditText.getText().toString();
            if(!location.isEmpty()){
                Geocoder geocoder = new Geocoder(MapActivity.this);
                try{
                    List<Address> addresses = geocoder.getFromLocationName(location, 1);
                    if(!addresses.isEmpty()){
                        Address address = addresses.get(0);
                        LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
                        mMaps.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,10));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
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

}
