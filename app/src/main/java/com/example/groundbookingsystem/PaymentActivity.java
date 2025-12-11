package com.example.groundbookingsystem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.groundbookingsystem.api.ApiClient;
import com.example.groundbookingsystem.api.ApiService;
import com.example.groundbookingsystem.models.Booking;
import com.example.groundbookingsystem.models.Ground;
import com.example.groundbookingsystem.models.PaymentRequest;
import com.example.groundbookingsystem.models.PaymentResponse;
import com.example.groundbookingsystem.models.ProcessPaymentRequest;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.stripe.android.PaymentConfiguration;
import com.stripe.android.paymentsheet.PaymentSheet;
import com.stripe.android.paymentsheet.PaymentSheetResult;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private MaterialCardView cardMethod;
    private MaterialButton payNowButton;
    private TextView amountTextView, bookingDetailsTextView, selectedMethodTextView;
    private ProgressBar progressBar;
    private ApiService apiService;
    private String token;
    private Booking booking;
    private Ground ground;
    private String selectedPaymentMethod = "";
    private String paymentId = "";
    private String stripeClientSecret = "";
    private String stripePaymentIntentId = "";

    // Stripe PaymentSheet
    private PaymentSheet paymentSheet;
    private static final String STRIPE_PUBLISHABLE_KEY = "pk_test_51QJ..."; // Replace with your Stripe publishable key from backend
    private boolean isStripeConfigured = false; // Set to true when Stripe is configured

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        booking = (Booking) getIntent().getSerializableExtra("booking");
        ground = (Ground) getIntent().getSerializableExtra("ground");

        if (booking == null || ground == null) {
            Toast.makeText(this, "Invalid booking data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Payment");
        }

        initViews();

        android.content.SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        token = prefs.getString("token", "");

        apiService = ApiClient.getClient().create(ApiService.class);

        // For college project: Using simple dummy payment mode (no Stripe integration)
        // Payment will process immediately without asking for card details
        isStripeConfigured = false;
        paymentSheet = null;
        android.util.Log.d("PaymentActivity", "Using simple payment mode (no Stripe) - college project mode");

        setupPaymentMethods();
        displayBookingDetails();
    }

    private void initViews() {
        cardMethod = findViewById(R.id.cardMethod);
        payNowButton = findViewById(R.id.payNowButton);
        amountTextView = findViewById(R.id.amountTextView);
        bookingDetailsTextView = findViewById(R.id.bookingDetailsTextView);
        selectedMethodTextView = findViewById(R.id.selectedMethodTextView);
        progressBar = findViewById(R.id.progressBar);

        payNowButton.setOnClickListener(v -> processPayment());
        payNowButton.setEnabled(false);
    }

    private void setupPaymentMethods() {
        // Only card payment method is available
        cardMethod.setOnClickListener(v -> selectPaymentMethod("card", cardMethod));
        // Auto-select card payment method
        selectPaymentMethod("card", cardMethod);
        android.util.Log.d("PaymentActivity", "Card payment method only - auto-selected");
    }

    private void selectPaymentMethod(String method, MaterialCardView selectedCard) {
        selectedPaymentMethod = "card"; // Always card
        
        // Highlight selected card
        selectedCard.setStrokeWidth(4);
        selectedCard.setStrokeColor(getResources().getColor(R.color.primary, null));
        
        // Update selected method text
        selectedMethodTextView.setText("Selected: Credit/Debit Card");
        selectedMethodTextView.setVisibility(View.VISIBLE);
        
        payNowButton.setEnabled(true);
    }

    private void displayBookingDetails() {
        if (booking != null && ground != null) {
            double amount = booking.price > 0 ? booking.price : ground.price;
            amountTextView.setText("₹" + String.format("%.2f", amount));
            
            String details = "Ground: " + ground.name + "\n" +
                           "Date: " + booking.booking_date + "\n" +
                           "Time: " + booking.time_slot;
            bookingDetailsTextView.setText(details);
        }
    }

    private void processPayment() {
        if (selectedPaymentMethod.isEmpty()) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        payNowButton.setEnabled(false);

        double amount = booking.price > 0 ? booking.price : ground.price;
        PaymentRequest request = new PaymentRequest(booking.id, amount, selectedPaymentMethod);

        // Validate token
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_LONG).show();
            progressBar.setVisibility(View.GONE);
            payNowButton.setEnabled(true);
            return;
        }

        // Step 1: Create payment record
        String authToken = token.startsWith("Bearer ") ? token : "Bearer " + token;
        apiService.createPayment(authToken, request).enqueue(new Callback<PaymentResponse>() {
            @Override
            public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {
                // Log response details for debugging
                android.util.Log.d("PaymentActivity", "Response code: " + response.code());
                android.util.Log.d("PaymentActivity", "Response isSuccessful: " + response.isSuccessful());
                
                if (response.isSuccessful() && response.body() != null) {
                    PaymentResponse paymentResponse = response.body();
                    android.util.Log.d("PaymentActivity", "Response success: " + paymentResponse.success);
                    
                    if (paymentResponse.success && paymentResponse.data != null && paymentResponse.data.id != null) {
                        paymentId = paymentResponse.data.id;
                        stripeClientSecret = paymentResponse.client_secret != null ? paymentResponse.client_secret : "";
                        stripePaymentIntentId = paymentResponse.stripe_payment_intent_id != null ? paymentResponse.stripe_payment_intent_id : "";
                        
                        android.util.Log.d("PaymentActivity", "Payment ID: " + paymentId);
                        android.util.Log.d("PaymentActivity", "Payment created successfully, processing payment...");
                        
                        // Step 2: Process payment immediately (simple mode for college project)
                        // No card input required - payment processes directly
                        processSimplePayment();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        payNowButton.setEnabled(true);
                        String errorMessage = paymentResponse.message != null ? paymentResponse.message : "Payment creation failed";
                        Toast.makeText(PaymentActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                } else {
                    progressBar.setVisibility(View.GONE);
                    payNowButton.setEnabled(true);
                    String errorMessage = "Failed to create payment";
                    
                    if (response.errorBody() != null) {
                        try {
                            String errorBodyString = response.errorBody().string();
                            android.util.Log.e("PaymentActivity", "Error body: " + errorBodyString);
                            
                            PaymentResponse errorResponse = new Gson().fromJson(errorBodyString, PaymentResponse.class);
                            if (errorResponse != null && errorResponse.message != null) {
                                errorMessage = errorResponse.message;
                            }
                        } catch (Exception e) {
                            android.util.Log.e("PaymentActivity", "Error parsing error body", e);
                        }
                    }
                    
                    if (response.code() == 404) {
                        errorMessage = "Payment endpoint not found. Please ensure the server is running.";
                    } else if (response.code() == 401) {
                        errorMessage = "Authentication failed. Please login again.";
                    }
                    
                    Toast.makeText(PaymentActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PaymentResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                payNowButton.setEnabled(true);
                String errorMsg = t.getMessage();
                android.util.Log.e("PaymentActivity", "Payment create network error: " + errorMsg, t);
                
                if (errorMsg != null) {
                    String lowerMsg = errorMsg.toLowerCase();
                    if (lowerMsg.contains("404") || lowerMsg.contains("not found") || lowerMsg.contains("failed to connect")) {
                        Toast.makeText(PaymentActivity.this, "Cannot connect to server. Please check:\n1. Server is running\n2. Internet connection\n3. Server URL is correct", Toast.LENGTH_LONG).show();
                    } else if (lowerMsg.contains("timeout")) {
                        Toast.makeText(PaymentActivity.this, "Request timeout. Please try again.", Toast.LENGTH_LONG).show();
                    } else if (lowerMsg.contains("network")) {
                        Toast.makeText(PaymentActivity.this, "Network error. Please check your internet connection.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(PaymentActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(PaymentActivity.this, "Network error. Please try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void presentStripePaymentSheet() {
        if (paymentSheet == null || stripeClientSecret.isEmpty()) {
            // Fallback to simple payment if Stripe not configured
            android.util.Log.d("PaymentActivity", "Stripe not configured, using simple payment");
            processSimplePayment();
            return;
        }

        try {
            PaymentSheet.Configuration configuration = new PaymentSheet.Configuration.Builder("Ground Booking System")
                    .build();

            paymentSheet.presentWithPaymentIntent(
                    stripeClientSecret,
                    configuration
            );
        } catch (Exception e) {
            android.util.Log.e("PaymentActivity", "Error presenting Stripe PaymentSheet", e);
            // Fallback to simple payment
            processSimplePayment();
        }
    }

    private void onPaymentSheetResult(PaymentSheetResult paymentSheetResult) {
        if (paymentSheetResult instanceof PaymentSheetResult.Completed) {
            // Payment succeeded - confirm with backend
            confirmStripePayment();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Canceled) {
            progressBar.setVisibility(View.GONE);
            payNowButton.setEnabled(true);
            Toast.makeText(this, "Payment canceled", Toast.LENGTH_SHORT).show();
        } else if (paymentSheetResult instanceof PaymentSheetResult.Failed) {
            PaymentSheetResult.Failed failed = (PaymentSheetResult.Failed) paymentSheetResult;
            progressBar.setVisibility(View.GONE);
            payNowButton.setEnabled(true);
            Toast.makeText(this, "Payment failed: " + failed.getError().getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void confirmStripePayment() {
        // Confirm payment with backend
        ProcessPaymentRequest processRequest = new ProcessPaymentRequest(paymentId, stripePaymentIntentId);
        String authToken = token.startsWith("Bearer ") ? token : "Bearer " + token;
        
        apiService.processPayment(authToken, processRequest).enqueue(new Callback<PaymentResponse>() {
            @Override
            public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {
                progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    String transactionId = response.body().transaction_id != null ? 
                                           response.body().transaction_id : 
                                           stripePaymentIntentId;
                    
                    Toast.makeText(PaymentActivity.this, 
                            "Payment successful! Transaction ID: " + transactionId, 
                            Toast.LENGTH_LONG).show();
                    
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("payment_success", true);
                    resultIntent.putExtra("transaction_id", transactionId);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    payNowButton.setEnabled(true);
                    String errorMessage = "Payment confirmation failed";
                    if (response.errorBody() != null) {
                        try {
                            PaymentResponse errorResponse = new Gson().fromJson(response.errorBody().charStream(), PaymentResponse.class);
                            if (errorResponse != null && errorResponse.message != null) {
                                errorMessage = errorResponse.message;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(PaymentActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<PaymentResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                payNowButton.setEnabled(true);
                Toast.makeText(PaymentActivity.this, "Error confirming payment: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void processSimplePayment() {
        // Process payment directly (when Stripe not configured or for fallback)
        android.util.Log.d("PaymentActivity", "Processing payment directly - paymentId: " + paymentId);
        ProcessPaymentRequest processRequest = new ProcessPaymentRequest(paymentId, stripePaymentIntentId);
        String authToken = token.startsWith("Bearer ") ? token : "Bearer " + token;
        
        apiService.processPayment(authToken, processRequest).enqueue(new Callback<PaymentResponse>() {
                @Override
                public void onResponse(Call<PaymentResponse> call, Response<PaymentResponse> response) {
                    progressBar.setVisibility(View.GONE);
                    
                    if (response.isSuccessful() && response.body() != null && response.body().success) {
                        String transactionId = response.body().transaction_id != null ? 
                                               response.body().transaction_id : 
                                               (response.body().data != null && response.body().data.transaction_id != null ? 
                                                response.body().data.transaction_id : 
                                                "TXN" + System.currentTimeMillis());
                        
                        Toast.makeText(PaymentActivity.this, 
                                "Payment successful! Transaction ID: " + transactionId, 
                                Toast.LENGTH_LONG).show();
                        
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("payment_success", true);
                        resultIntent.putExtra("transaction_id", transactionId);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    } else {
                        payNowButton.setEnabled(true);
                        String errorMessage = "Payment processing failed";
                        if (response.code() == 404) {
                            errorMessage = "Payment endpoint not found. Please check server configuration.";
                        } else if (response.errorBody() != null) {
                            try {
                                PaymentResponse errorResponse = new Gson().fromJson(response.errorBody().charStream(), PaymentResponse.class);
                                if (errorResponse != null && errorResponse.message != null) {
                                    errorMessage = errorResponse.message;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        Toast.makeText(PaymentActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(Call<PaymentResponse> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    payNowButton.setEnabled(true);
                    String errorMsg = t.getMessage();
                    android.util.Log.e("PaymentActivity", "Payment process network error: " + errorMsg, t);
                    
                    if (errorMsg != null && (errorMsg.contains("404") || errorMsg.contains("not found"))) {
                        Toast.makeText(PaymentActivity.this, "Cannot connect to server. Please check server is running.", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(PaymentActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                    }
                }
            });
    }
}
