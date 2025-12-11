package com.example.groundbookingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.groundbookingsystem.adapters.FeedbackAdapter;
import com.example.groundbookingsystem.api.ApiClient;
import com.example.groundbookingsystem.api.ApiService;
import com.example.groundbookingsystem.models.Feedback;
import com.example.groundbookingsystem.models.FeedbacksResponse;
import com.example.groundbookingsystem.models.Ground;
import com.example.groundbookingsystem.utils.StarRatingHelper;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroundDetailActivity extends AppCompatActivity {

    private Ground ground;
    private ApiService apiService;
    private RecyclerView feedbacksRecyclerView;
    private FeedbackAdapter feedbackAdapter;
    private LinearLayout emptyFeedbacksLayout;
    private TextView averageRatingText;
    private TextView totalReviewsText;
    private LinearLayout averageStarsContainer;
    private TextView groundRatingTextView;
    private TextView groundRatingDetailTextView;

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
        apiService = ApiClient.getClient().create(ApiService.class);

        ImageView imageView = findViewById(R.id.groundImageView);
        TextView nameView = findViewById(R.id.groundNameTextView);
        TextView locationView = findViewById(R.id.groundLocationTextView);
        TextView typeView = findViewById(R.id.groundTypeTextView);
        TextView descView = findViewById(R.id.groundDescriptionTextView);
        TextView priceView = findViewById(R.id.groundPriceTextView);
        Button bookBtn = findViewById(R.id.bookButton);
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsingToolbar);
        
        // Feedback views
        feedbacksRecyclerView = findViewById(R.id.feedbacksRecyclerView);
        emptyFeedbacksLayout = findViewById(R.id.emptyFeedbacksLayout);
        averageRatingText = findViewById(R.id.averageRatingText);
        totalReviewsText = findViewById(R.id.totalReviewsText);
        averageStarsContainer = findViewById(R.id.averageStarsContainer);
        groundRatingTextView = findViewById(R.id.groundRatingTextView);
        groundRatingDetailTextView = findViewById(R.id.groundRatingDetailTextView);
        
        // Setup RecyclerView
        feedbacksRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        feedbackAdapter = new FeedbackAdapter(new ArrayList<>());
        feedbacksRecyclerView.setAdapter(feedbackAdapter);

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
        
        // Load feedbacks
        if (ground != null && ground.id != null) {
            loadFeedbacks(ground.id);
        }
    }
    
    private void loadFeedbacks(String groundId) {
        apiService.getGroundFeedbacks(groundId).enqueue(new Callback<FeedbacksResponse>() {
            @Override
            public void onResponse(Call<FeedbacksResponse> call, Response<FeedbacksResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    List<Feedback> feedbacks = response.body().data;
                    if (feedbacks != null) {
                        displayFeedbacks(feedbacks);
                    } else {
                        showEmptyState();
                    }
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<FeedbacksResponse> call, Throwable t) {
                android.util.Log.e("GroundDetailActivity", "Error loading feedbacks: " + t.getMessage());
                showEmptyState();
            }
        });
    }
    
    private void displayFeedbacks(List<Feedback> feedbacks) {
        if (feedbacks.isEmpty()) {
            showEmptyState();
            return;
        }
        
        // Calculate average rating
        float totalRating = 0;
        for (Feedback feedback : feedbacks) {
            totalRating += feedback.rating;
        }
        float averageRating = totalRating / feedbacks.size();
        
        // Update average rating display
        averageRatingText.setText(String.format("%.1f", averageRating));
        StarRatingHelper.displayStarsSimple(averageStarsContainer, averageRating);
        
        // Update total reviews count
        int reviewCount = feedbacks.size();
        totalReviewsText.setText(reviewCount + (reviewCount == 1 ? " review" : " reviews"));
        
        // Update header rating badge
        groundRatingTextView.setText(String.format("%.1f", averageRating));
        if (groundRatingDetailTextView != null) {
            groundRatingDetailTextView.setText(String.format("%.1f", averageRating));
        }
        
        // Update RecyclerView
        feedbackAdapter = new FeedbackAdapter(feedbacks);
        feedbacksRecyclerView.setAdapter(feedbackAdapter);
        
        // Show feedbacks, hide empty state
        feedbacksRecyclerView.setVisibility(View.VISIBLE);
        emptyFeedbacksLayout.setVisibility(View.GONE);
    }
    
    private void showEmptyState() {
        feedbacksRecyclerView.setVisibility(View.GONE);
        emptyFeedbacksLayout.setVisibility(View.VISIBLE);
        
        // Set default values - show "--" when no ratings available
        averageRatingText.setText("--");
        totalReviewsText.setText("0 reviews");
        StarRatingHelper.displayStarsSimple(averageStarsContainer, 0);
        groundRatingTextView.setText("--");
        if (groundRatingDetailTextView != null) {
            groundRatingDetailTextView.setText("--");
        }
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
