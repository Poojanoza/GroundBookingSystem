package com.example.groundbookingsystem.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private GroundAdapter adapter;
    private List<Ground> groundList = new ArrayList<>();
    private ApiService apiService;
    private ProgressBar progressBar;
    private TextView emptyTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.groundsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyTextView = view.findViewById(R.id.emptyTextView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new GroundAdapter(getContext(), groundList);
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getClient().create(ApiService.class);
        loadGrounds();

        return view;
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
                    adapter.notifyDataSetChanged();

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
