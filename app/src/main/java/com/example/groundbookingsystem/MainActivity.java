package com.example.groundbookingsystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.widget.Toast;
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

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private GroundAdapter adapter;
    private List<Ground> groundList = new ArrayList<>();
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.groundsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroundAdapter(this, groundList);
        recyclerView.setAdapter(adapter);

        apiService = ApiClient.getClient().create(ApiService.class);
        loadGrounds();
    }

    private void loadGrounds() {
        apiService.getGrounds().enqueue(new Callback<GroundsResponse>() {
            @Override
            public void onResponse(Call<GroundsResponse> call, Response<GroundsResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().success) {
                    groundList.clear();
                    groundList.addAll(response.body().data);
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this, "Failed to load grounds", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GroundsResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Network Error: "+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
