package com.example.groundbookingsystem;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyBookingsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private List<Booking> bookingList = new ArrayList<>();
    private ApiService apiService;
    private android.widget.ProgressBar progressBar;
    private View emptyView;
    private String token;
    private String userId;
    private int pendingRequests = 0;
    private final Set<String> seenBookingIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Bookings");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        token = prefs.getString("token", "");
        userId = prefs.getString("userId", "");

        recyclerView = findViewById(R.id.myBookingsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyTextView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapter(bookingList, this);
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getClient().create(ApiService.class);
        loadAllBookings();
    }

    public void loadAllBookings() {
        if (progressBar != null) {
        progressBar.setVisibility(android.view.View.VISIBLE);
        }
        pendingRequests = 2;
        bookingList.clear();
        seenBookingIds.clear();

        apiService.getActiveBookings(userId, "Bearer " + token).enqueue(new Callback<BookingsResponse>() {
            @Override
            public void onResponse(Call<BookingsResponse> call, Response<BookingsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success && response.body().data != null) {
                    for (Booking booking : response.body().data) {
                        if (booking != null && booking.id != null && !seenBookingIds.contains(booking.id)) {
                            seenBookingIds.add(booking.id);
                            bookingList.add(booking);
                        }
                    }
                }
                onRequestFinished();
            }

            @Override
            public void onFailure(Call<BookingsResponse> call, Throwable t) {
                Toast.makeText(MyBookingsActivity.this, "Network Error: "+t.getMessage(), Toast.LENGTH_SHORT).show();
                onRequestFinished();
            }
        });

        apiService.getBookingHistory(userId, "Bearer " + token).enqueue(new Callback<BookingsResponse>() {
            @Override
            public void onResponse(Call<BookingsResponse> call, Response<BookingsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success && response.body().data != null) {
                    for (Booking booking : response.body().data) {
                        if (booking != null && booking.id != null && !seenBookingIds.contains(booking.id)) {
                            seenBookingIds.add(booking.id);
                            bookingList.add(booking);
                        }
                    }
                }
                onRequestFinished();
            }

            @Override
            public void onFailure(Call<BookingsResponse> call, Throwable t) {
                Toast.makeText(MyBookingsActivity.this, "Network Error: "+t.getMessage(), Toast.LENGTH_SHORT).show();
                onRequestFinished();
            }
        });
    }

    private void onRequestFinished() {
        pendingRequests--;
        if (pendingRequests <= 0) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            adapter.notifyDataSetChanged();
            if (bookingList.isEmpty()) {
                if (emptyView != null) emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                if (emptyView != null) emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
