package com.example.groundbookingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.groundbookingsystem.models.Ground;
import com.google.android.material.appbar.CollapsingToolbarLayout;

public class GroundDetailActivity extends AppCompatActivity {

    private Ground ground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ground_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        ground = (Ground) getIntent().getSerializableExtra("ground");

        ImageView imageView = findViewById(R.id.groundImageView);
        TextView nameView = findViewById(R.id.groundNameTextView);
        TextView locationView = findViewById(R.id.groundLocationTextView);
        TextView typeView = findViewById(R.id.groundTypeTextView);
        TextView descView = findViewById(R.id.groundDescriptionTextView);
        TextView priceView = findViewById(R.id.groundPriceTextView);
        Button bookBtn = findViewById(R.id.bookButton);
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsingToolbar);

        if (ground != null) {
            collapsingToolbar.setTitle(ground.name); // Set title for collapsing toolbar
            
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
