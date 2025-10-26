package com.example.groundbookingsystem.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.groundbookingsystem.GroundDetailActivity;
import com.example.groundbookingsystem.R;
import com.example.groundbookingsystem.models.Ground;

import java.util.List;

public class GroundAdapter extends RecyclerView.Adapter<GroundAdapter.GroundViewHolder> {

    private Context context;
    private List<Ground> groundList;

    public GroundAdapter(Context context, List<Ground> groundList) {
        this.context = context;
        this.groundList = groundList;
    }

    @NonNull
    @Override
    public GroundViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ground, parent, false);
        return new GroundViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroundViewHolder holder, int position) {
        Ground ground = groundList.get(position);

        holder.nameTextView.setText(ground.getName());
        holder.locationTextView.setText(ground.getLocation());
        holder.priceTextView.setText("₹" + ground.getPricePerHour() + "/hour");

        // Load image using Glide
        if (ground.getImageUrl() != null && !ground.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(ground.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(holder.imageView);
        }

        // Click listener for the entire card
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, GroundDetailActivity.class);
            intent.putExtra("groundId", ground.getId());
            intent.putExtra("groundName", ground.getName());
            intent.putExtra("groundLocation", ground.getLocation());
            intent.putExtra("groundDescription", ground.getDescription());
            intent.putExtra("groundImageUrl", ground.getImageUrl());
            intent.putExtra("groundPrice", ground.getPricePerHour());
            intent.putExtra("groundType", ground.getType());
            context.startActivity(intent);
        });

        // Book Now button click
        holder.bookButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, GroundDetailActivity.class);
            intent.putExtra("groundId", ground.getId());
            intent.putExtra("groundName", ground.getName());
            intent.putExtra("groundLocation", ground.getLocation());
            intent.putExtra("groundDescription", ground.getDescription());
            intent.putExtra("groundImageUrl", ground.getImageUrl());
            intent.putExtra("groundPrice", ground.getPricePerHour());
            intent.putExtra("groundType", ground.getType());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return groundList.size();
    }

    public static class GroundViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameTextView, locationTextView, priceTextView;
        Button bookButton;

        public GroundViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.groundImageView);
            nameTextView = itemView.findViewById(R.id.groundNameTextView);
            locationTextView = itemView.findViewById(R.id.groundLocationTextView);
            priceTextView = itemView.findViewById(R.id.groundPriceTextView);
            bookButton = itemView.findViewById(R.id.bookNowButton);
        }
    }
}
