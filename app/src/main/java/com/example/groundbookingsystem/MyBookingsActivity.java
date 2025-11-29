package com.example.groundbookingsystem;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.groundbookingsystem.adapters.BookingAdapter;
import com.example.groundbookingsystem.api.ApiClient;
import com.example.groundbookingsystem.api.ApiService;
import com.example.groundbookingsystem.models.Booking;
import com.example.groundbookingsystem.models.BookingsResponse;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyBookingsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private List<Booking> bookingList = new ArrayList<>();
    private ApiService apiService;
    private android.widget.ProgressBar progressBar;
    private String token;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Active Bookings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        token = prefs.getString("token", "");
        userId = prefs.getString("userId", "");

        recyclerView = findViewById(R.id.bookingsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapter(bookingList, this);
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getClient().create(ApiService.class);
        loadActiveBookings();
    }

    private void loadActiveBookings() {
        progressBar.setVisibility(android.view.View.VISIBLE);

        apiService.getActiveBookings(userId, "Bearer " + token).enqueue(new Callback<BookingsResponse>() {
            @Override
            public void onResponse(Call<BookingsResponse> call, Response<BookingsResponse> response) {
                progressBar.setVisibility(android.view.View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    bookingList.clear();
                    bookingList.addAll(response.body().data);
                    adapter.notifyDataSetChanged();

                    if (bookingList.isEmpty()) {
                        Toast.makeText(MyBookingsActivity.this, "No active bookings", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MyBookingsActivity.this, "Failed to load bookings", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BookingsResponse> call, Throwable t) {
                progressBar.setVisibility(android.view.View.GONE);
                Toast.makeText(MyBookingsActivity.this, "Network Error: "+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
