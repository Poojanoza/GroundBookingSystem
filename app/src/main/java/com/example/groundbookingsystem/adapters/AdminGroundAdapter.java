package com.example.groundbookingsystem.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.groundbookingsystem.R;
import com.example.groundbookingsystem.models.Ground;

import java.util.List;

public class AdminGroundAdapter extends RecyclerView.Adapter<AdminGroundAdapter.ViewHolder> {

    private List<Ground> grounds;
    private Context context;
    private OnGroundActionListener listener;

    public interface OnGroundActionListener {
        void onEdit(Ground ground);
        void onDelete(Ground ground);
    }

    public AdminGroundAdapter(Context context, List<Ground> grounds, OnGroundActionListener listener) {
        this.context = context;
        this.grounds = grounds;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_ground, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Ground ground = grounds.get(position);

        holder.name.setText(ground.name);
        holder.location.setText(ground.location);
        holder.type.setText(ground.type);
        holder.price.setText("₹" + (int) ground.price);

        if (ground.image_url != null && !ground.image_url.isEmpty()) {
            Glide.with(context).load(ground.image_url).into(holder.image);
        }

        holder.editBtn.setOnClickListener(v -> listener.onEdit(ground));
        holder.deleteBtn.setOnClickListener(v -> listener.onDelete(ground));
    }

    @Override
    public int getItemCount() {
        return grounds.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView name, location, type, price;
        ImageButton editBtn, deleteBtn;

        ViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.groundImage);
            name = view.findViewById(R.id.groundName);
            location = view.findViewById(R.id.groundLocation);
            type = view.findViewById(R.id.groundType);
            price = view.findViewById(R.id.groundPrice);
            editBtn = view.findViewById(R.id.editBtn);
            deleteBtn = view.findViewById(R.id.deleteBtn);
        }
    }
}

