package com.example.groundbookingsystem;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.example.groundbookingsystem.api.ApiClient;
import com.example.groundbookingsystem.api.ApiService;
import com.example.groundbookingsystem.models.FeedbackRequest;
import com.example.groundbookingsystem.models.FeedbackResponse;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FeedbackDialog {
    private Context context;
    private ApiService apiService;
    private String token;
    private String bookingId;
    private Dialog dialog;
    private RatingBar ratingBar;
    private EditText commentEditText;
    private MaterialButton submitButton;
    private TextView cancelButton;
    private OnFeedbackSubmittedListener listener;

    public interface OnFeedbackSubmittedListener {
        void onFeedbackSubmitted();
    }

    public FeedbackDialog(Context context, String bookingId, OnFeedbackSubmittedListener listener) {
        this.context = context;
        this.bookingId = bookingId;
        this.listener = listener;
        
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        token = prefs.getString("token", "");
        apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void show() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = View.inflate(context, R.layout.dialog_feedback, null);
        builder.setView(dialogView);

        dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.show();

        initViews(dialogView);
    }

    private void initViews(View view) {
        ratingBar = view.findViewById(R.id.ratingBar);
        commentEditText = view.findViewById(R.id.commentEditText);
        submitButton = view.findViewById(R.id.submitFeedbackButton);
        cancelButton = view.findViewById(R.id.cancelFeedbackButton);

        // Make sure rating bar is interactive
        ratingBar.setIsIndicator(false);
        ratingBar.setClickable(true);
        ratingBar.setFocusable(true);
        
        // Add rating change listener for feedback
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                android.util.Log.d("FeedbackDialog", "Rating changed to: " + rating);
            }
        });

        submitButton.setOnClickListener(v -> submitFeedback());
        cancelButton.setOnClickListener(v -> dialog.dismiss());
    }

    private void submitFeedback() {
        float rating = ratingBar.getRating();
        String comment = commentEditText.getText().toString().trim();

        if (rating < 1) {
            Toast.makeText(context, "Please provide a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        submitButton.setEnabled(false);
        submitButton.setText("Submitting...");

        FeedbackRequest request = new FeedbackRequest(bookingId, (int) rating, comment);
        apiService.createFeedback("Bearer " + token, request).enqueue(new Callback<FeedbackResponse>() {
            @Override
            public void onResponse(Call<FeedbackResponse> call, Response<FeedbackResponse> response) {
                submitButton.setEnabled(true);
                submitButton.setText("Submit Feedback");

                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    Toast.makeText(context, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    if (listener != null) {
                        listener.onFeedbackSubmitted();
                    }
                } else {
                    String errorMessage = "Failed to submit feedback";
                    if (response.errorBody() != null) {
                        try {
                            FeedbackResponse errorResponse = new Gson().fromJson(response.errorBody().charStream(), FeedbackResponse.class);
                            if (errorResponse != null && errorResponse.message != null) {
                                errorMessage = errorResponse.message;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<FeedbackResponse> call, Throwable t) {
                submitButton.setEnabled(true);
                submitButton.setText("Submit Feedback");
                Toast.makeText(context, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

