package com.example.groundbookingsystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.groundbookingsystem.adapters.BookingHistoryAdapter;
import com.example.groundbookingsystem.api.ApiClient;
import com.example.groundbookingsystem.api.ApiService;
import com.example.groundbookingsystem.models.Booking;
import com.example.groundbookingsystem.models.BookingsResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class BookingHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookingHistoryAdapter adapter;
    private List<Booking> bookingList = new ArrayList<>();
    private ProgressBar progressBar;
    private ApiService apiService;
    private String token;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);

        initViews();
        setupToolbar();

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        token = prefs.getString("token", "");
        userId = prefs.getString("userId", "");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingHistoryAdapter(this, bookingList);
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getClient().create(ApiService.class);
        loadBookingHistory();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.bookingHistoryRecyclerView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Booking History");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void loadBookingHistory() {
        progressBar.setVisibility(android.view.View.VISIBLE);

        apiService.getBookingHistory(userId, "Bearer " + token).enqueue(new Callback<BookingsResponse>() {
            @Override
            public void onResponse(Call<BookingsResponse> call, Response<BookingsResponse> response) {
                progressBar.setVisibility(android.view.View.GONE);
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    bookingList.clear();
                    bookingList.addAll(response.body().data);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(BookingHistoryActivity.this, "Failed to load history", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BookingsResponse> call, Throwable t) {
                progressBar.setVisibility(android.view.View.GONE);
                Toast.makeText(BookingHistoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
