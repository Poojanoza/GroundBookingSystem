package com.example.groundbookingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.groundbookingsystem.models.Ground;

public class GroundDetailActivity extends AppCompatActivity {

    private Ground ground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ground_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Ground Details");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ground = (Ground) getIntent().getSerializableExtra("ground");

        ImageView imageView = findViewById(R.id.groundImageView);
        TextView nameView = findViewById(R.id.groundNameTextView);
        TextView locationView = findViewById(R.id.groundLocationTextView);
        TextView typeView = findViewById(R.id.groundTypeTextView);
        TextView descView = findViewById(R.id.groundDescriptionTextView);
        TextView priceView = findViewById(R.id.groundPriceTextView);
        Button bookBtn = findViewById(R.id.bookButton);

        if (ground != null) {
            // Load image
            if (ground.image_url != null && !ground.image_url.isEmpty()) {
                Glide.with(this).load(ground.image_url).into(imageView);
            }
            
            nameView.setText(ground.name);
            locationView.setText(getString(R.string.ground_location_format, ground.location));
            typeView.setText(getString(R.string.ground_type_format, ground.type));
            descView.setText(getString(R.string.ground_description_format, ground.description));
            priceView.setText(getString(R.string.ground_price_format, String.valueOf(ground.price)));
        }

        bookBtn.setOnClickListener(v -> {
            Intent intent = new Intent(GroundDetailActivity.this, BookingActivity.class);
            intent.putExtra("ground", ground);
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
