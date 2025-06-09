package com.example.traveljurnalapp;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;

public class FullScreenGalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_gallery);

        ViewPager2 viewPager = findViewById(R.id.viewPager);

        ArrayList<String> imageUrls = getIntent().getStringArrayListExtra("imageUrls");
        int startIndex = getIntent().getIntExtra("startIndex", 0);

        FullScreenImageAdapter adapter = new FullScreenImageAdapter(this, imageUrls);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(startIndex, false);
    }
}
