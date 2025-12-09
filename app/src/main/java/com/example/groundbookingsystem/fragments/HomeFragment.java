package com.example.groundbookingsystem.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groundbookingsystem.GroundDetailActivity;
import com.example.groundbookingsystem.R;
import com.example.groundbookingsystem.adapters.GroundAdapter;
import com.example.groundbookingsystem.api.ApiClient;
import com.example.groundbookingsystem.api.ApiService;
import com.example.groundbookingsystem.models.Ground;
import com.example.groundbookingsystem.models.GroundsResponse;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private GroundAdapter adapter;
    private List<Ground> groundList = new ArrayList<>();
    private List<Ground> filteredGroundList = new ArrayList<>();
    private ApiService apiService;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private TextInputEditText searchEditText;
    private ImageButton clearSearchBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.groundsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyTextView = view.findViewById(R.id.emptyTextView);
        searchEditText = view.findViewById(R.id.searchEditText);
        clearSearchBtn = view.findViewById(R.id.clearSearchBtn);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GroundAdapter(getContext(), filteredGroundList);
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getClient().create(ApiService.class);
        
        setupSearch();
        loadGrounds();

        return view;
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterGrounds(s.toString());
                clearSearchBtn.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        clearSearchBtn.setOnClickListener(v -> {
            searchEditText.setText("");
            filterGrounds("");
            clearSearchBtn.setVisibility(View.GONE);
        });
    }

    private void filterGrounds(String query) {
        filteredGroundList.clear();
        
        if (query.isEmpty()) {
            filteredGroundList.addAll(groundList);
        } else {
            String lowerQuery = query.toLowerCase().trim();
            for (Ground ground : groundList) {
                boolean matches = 
                    (ground.name != null && ground.name.toLowerCase().contains(lowerQuery)) ||
                    (ground.location != null && ground.location.toLowerCase().contains(lowerQuery)) ||
                    (ground.type != null && ground.type.toLowerCase().contains(lowerQuery));
                
                if (matches) {
                    filteredGroundList.add(ground);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        
        if (filteredGroundList.isEmpty() && emptyTextView != null) {
            emptyTextView.setText(query.isEmpty() ? "No grounds available" : "No grounds found");
            emptyTextView.setVisibility(View.VISIBLE);
        } else if (emptyTextView != null) {
            emptyTextView.setVisibility(View.GONE);
        }
    }

    private void loadGrounds() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        apiService.getGrounds().enqueue(new Callback<GroundsResponse>() {
            @Override
            public void onResponse(Call<GroundsResponse> call, Response<GroundsResponse> response) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    groundList.clear();
                    groundList.addAll(response.body().data);
                    filterGrounds(searchEditText != null ? searchEditText.getText().toString() : "");

                    if (groundList.isEmpty() && emptyTextView != null) {
                        emptyTextView.setVisibility(View.VISIBLE);
                    } else if (emptyTextView != null) {
                        emptyTextView.setVisibility(View.GONE);
                    }
                } else {
                    if (getContext() != null)
                        Toast.makeText(getContext(), "Failed to load grounds", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GroundsResponse> call, Throwable t) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                if (getContext() != null)
                    Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
