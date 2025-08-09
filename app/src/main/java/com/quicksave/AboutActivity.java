package com.quicksave;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ImageView photo = findViewById(R.id.about_photo);
        TextView name = findViewById(R.id.about_name);
        TextView location = findViewById(R.id.about_location);
        TextView desc = findViewById(R.id.about_desc);

        name.setText("Farman Ali");
        location.setText("Sindh, Pakistan");
        desc.setText("This app is built by Farman Ali with dedication and hard work for downloading videos from social media. Future updates will improve features and performance.");

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_placeholder);
        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
        roundedBitmapDrawable.setCircular(true);
        photo.setImageDrawable(roundedBitmapDrawable);
    }
}
