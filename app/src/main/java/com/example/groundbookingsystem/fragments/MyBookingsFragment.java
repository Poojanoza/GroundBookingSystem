package com.example.groundbookingsystem.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groundbookingsystem.R;
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

public class MyBookingsFragment extends Fragment {

    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private List<Booking> bookingList = new ArrayList<>();
    private ApiService apiService;
    private ProgressBar progressBar;
    private View emptyStateView;
    private String token;
    private String userId;
    private int pendingRequests = 0;
    private final Set<String> seenBookingIds = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = null;
        try {
            view = inflater.inflate(R.layout.fragment_my_bookings, container, false);
            
            if (view == null) {
                return new View(requireContext());
            }

            recyclerView = view.findViewById(R.id.myBookingsRecyclerView);
            progressBar = view.findViewById(R.id.progressBar);
            emptyStateView = view.findViewById(R.id.emptyTextView);

            Context context = getContext();
            if (context == null) {
                return view;
            }

            SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
            token = prefs.getString("token", "");
            userId = prefs.getString("userId", "");

            if (recyclerView != null) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                adapter = new BookingAdapter(bookingList, context);
                recyclerView.setAdapter(adapter);
            }

            apiService = ApiClient.getClient().create(ApiService.class);
            
            if (userId != null && !userId.isEmpty() && token != null && !token.isEmpty()) {
                loadAllBookings();
            } else {
                if (emptyStateView != null) {
                    emptyStateView.setVisibility(View.VISIBLE);
                }
                if (recyclerView != null) {
                    recyclerView.setVisibility(View.GONE);
                }
            }

            return view;
        } catch (Exception e) {
            e.printStackTrace();
            // Return the view if it was created, otherwise create a simple one
            if (view != null) {
                return view;
            }
            try {
                return new View(requireContext());
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private void loadAllBookings() {
        try {
            if (!isAdded() || getContext() == null || apiService == null || userId == null || userId.isEmpty() || token == null || token.isEmpty()) {
                if (emptyStateView != null) {
                    emptyStateView.setVisibility(View.VISIBLE);
                }
                if (recyclerView != null) {
                    recyclerView.setVisibility(View.GONE);
                }
                return;
            }

            if (progressBar != null) {
                progressBar.setVisibility(View.VISIBLE);
            }

            pendingRequests = 2;
            bookingList.clear();
            seenBookingIds.clear();

            // Active bookings
            apiService.getActiveBookings(userId, "Bearer " + token).enqueue(new BookingCallback());
            // History bookings
            apiService.getBookingHistory(userId, "Bearer " + token).enqueue(new BookingCallback());
        } catch (Exception e) {
            e.printStackTrace();
            if (isAdded() && getContext() != null) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (emptyStateView != null) {
                    emptyStateView.setVisibility(View.VISIBLE);
                }
                if (recyclerView != null) {
                    recyclerView.setVisibility(View.GONE);
                }
            }
        }
    }

    private class BookingCallback implements Callback<BookingsResponse> {
        @Override
        public void onResponse(Call<BookingsResponse> call, Response<BookingsResponse> response) {
            try {
                if (!isAdded() || getContext() == null) return;

                if (response.isSuccessful() && response.body() != null && response.body().success && response.body().data != null) {
                    for (Booking booking : response.body().data) {
                        if (booking != null && booking.id != null && !seenBookingIds.contains(booking.id)) {
                            seenBookingIds.add(booking.id);
                            bookingList.add(booking);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                onRequestFinished();
            }
        }

        @Override
        public void onFailure(Call<BookingsResponse> call, Throwable t) {
            try {
                if (!isAdded() || getContext() == null) return;
                String errorMsg = t != null && t.getMessage() != null ? t.getMessage() : "Unknown error";
                Toast.makeText(getContext(), "Network Error: " + errorMsg, Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                onRequestFinished();
            }
        }
    }

    private void onRequestFinished() {
        pendingRequests--;
        if (pendingRequests <= 0) {
            if (progressBar != null) {
                progressBar.setVisibility(View.GONE);
            }
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
            if (bookingList.isEmpty()) {
                if (emptyStateView != null) {
                    emptyStateView.setVisibility(View.VISIBLE);
                }
                if (recyclerView != null) {
                    recyclerView.setVisibility(View.GONE);
                }
            } else {
                if (emptyStateView != null) {
                    emptyStateView.setVisibility(View.GONE);
                }
                if (recyclerView != null) {
                    recyclerView.setVisibility(View.VISIBLE);
                }
            }
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Refresh bookings when user returns to this fragment
        if (userId != null && !userId.isEmpty() && token != null && !token.isEmpty()) {
            loadAllBookings();
        }
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up to prevent memory leaks
        recyclerView = null;
        adapter = null;
        progressBar = null;
        emptyStateView = null;
    }
}
