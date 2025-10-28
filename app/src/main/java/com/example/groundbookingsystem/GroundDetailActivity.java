package com.example.groundbookingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.groundbookingsystem.api.ApiClient;
import com.example.groundbookingsystem.api.ApiService;
import com.example.groundbookingsystem.models.Booking;
import com.example.groundbookingsystem.models.Ground;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroundDetailActivity extends AppCompatActivity {
    private TextView name, location, type, desc, price;
    private Button bookButton;
    private ApiService apiService;
    private Ground ground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ground_detail);

        name = findViewById(R.id.groundNameDetail);
        location = findViewById(R.id.groundLocationDetail);
        type = findViewById(R.id.groundTypeDetail);
        desc = findViewById(R.id.groundDescDetail);
        price = findViewById(R.id.groundPriceDetail);
        bookButton = findViewById(R.id.bookButton);

        apiService = ApiClient.getClient().create(ApiService.class);

        // Assume you pass Ground as Serializable extra
        ground = (Ground) getIntent().getSerializableExtra("ground");
        if (ground != null) {
            name.setText(ground.name);
            location.setText(ground.location);
            type.setText(ground.type);
            desc.setText(ground.description);
            price.setText("₹" + ground.price);
        }

        bookButton.setOnClickListener(v -> bookGround());
    }

    private void bookGround() {
        // Get user ID from SharedPreferences
        String userId = getSharedPreferences("app_prefs", MODE_PRIVATE)
                .getString("userId", "");
        // You would get/ask for date/slot through dialog or extras
        String date = "2023-12-01";   // Replace with actual user choice
        String slot = "09:00-10:00";  // Replace with actual user choice

        Booking booking = new Booking();
        booking.user_id = userId;
        booking.ground_id = ground.id;
        booking.booking_date = date;
        booking.time_slot = slot;
        booking.status = "confirmed";
        booking.price = ground.price;

        apiService.createBooking(booking).enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (response.isSuccessful())
                    Toast.makeText(GroundDetailActivity.this, "Booking successful!", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(GroundDetailActivity.this, "Booking failed!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                Toast.makeText(GroundDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
