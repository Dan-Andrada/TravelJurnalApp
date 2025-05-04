package com.example.traveljurnalapp;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddTripDetailsActivity extends AppCompatActivity {

    private EditText spendingInput, placeNameInput;
    private Button uploadPhotoButton, notesButton, doneButton, btnRestaurant, btnHotel, btnActivity, btnMuseum;
    private CalendarView calendarView;
    private Date startDate = null;
    private Date endDate = null;
    private boolean selectingStart = true;
    private String type = "trip";
    Button selectedBtn = null;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_trip_details);

        spendingInput = findViewById(R.id.spendingInput);
        placeNameInput = findViewById(R.id.placeNameInput);

        uploadPhotoButton = findViewById(R.id.uploadPhotoButton);
        notesButton = findViewById(R.id.notesButton);
        doneButton = findViewById(R.id.doneButton);
        btnRestaurant = findViewById(R.id.btnRestaurant);
        btnHotel = findViewById(R.id.btnHotel);
        btnActivity = findViewById(R.id.btnActivity);
        btnMuseum = findViewById(R.id.btnMuseum);

        calendarView = findViewById(R.id.calendarView);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        String suggestedName = getIntent().getStringExtra("suggestedName");
        if(suggestedName != null){
            placeNameInput.setText(suggestedName);
        }

        double lat = getIntent().getDoubleExtra("lat",0);
        double lng = getIntent().getDoubleExtra("lng",0);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            Calendar selectedCal = Calendar.getInstance();
            selectedCal.set(year,month,dayOfMonth);
            Date pickedDate = selectedCal.getTime();

            if(selectingStart){
                startDate = pickedDate;
                Toast.makeText(this,"Start date selected",Toast.LENGTH_SHORT).show();
            } else {
                if(startDate != null && pickedDate.after(startDate)){
                    endDate = pickedDate;
                    Toast.makeText(this, "End date selected", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "End date must be after start date", Toast.LENGTH_SHORT).show();
                }
            }
            selectingStart = !selectingStart;
        });



        View.OnClickListener typeClickListener = view ->{

            Button clickedBtn = (Button) view;
            if(selectedBtn == clickedBtn){
                clickedBtn.setBackgroundResource(R.color.screenBg);
                selectedBtn = null;
                type = "trip";
            } else {
                if(selectedBtn != null)
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

        doneButton.setOnClickListener(v -> {
            String placeName = placeNameInput.getText().toString().trim();

            if(startDate == null){
                Toast.makeText(this, "Please select a start date", Toast.LENGTH_SHORT).show();
            }

            String spending = spendingInput.getText().toString().trim();
            if (spending.isEmpty())
                spending = "0";

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd", Locale.getDefault());

            Map<String, Object> tripDate = new HashMap<>();
            tripDate.put("placeName", placeName);
            tripDate.put("startDate", simpleDateFormat.format(startDate));
            if(endDate != null){
                tripDate.put("endDate", simpleDateFormat.format(endDate));
            }
            tripDate.put("spending", Double.parseDouble(spending));
            tripDate.put("type",type);

            FirebaseUser user = mAuth.getCurrentUser();

            assert user != null;
            db.collection("users")
                    .document(user.getUid())
                    .collection("trips")
                    .add(tripDate)
                    .addOnSuccessListener(documentReference -> Toast.makeText(this, "Trip saved", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("lat", lat);
            intent.putExtra("lng", lng);
            intent.putExtra("title", placeName);
            startActivityForResult(intent,200);
        });
    }

}
