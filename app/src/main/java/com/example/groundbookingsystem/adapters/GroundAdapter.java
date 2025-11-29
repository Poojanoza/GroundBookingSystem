package com.example.groundbookingsystem.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.groundbookingsystem.BookingActivity;
import com.example.groundbookingsystem.GroundDetailActivity;
import com.example.groundbookingsystem.R;
import com.example.groundbookingsystem.models.Ground;
import java.util.List;

public class GroundAdapter extends RecyclerView.Adapter<GroundAdapter.ViewHolder> {
    private List<Ground> grounds;
    private Context context;

    public GroundAdapter(Context context, List<Ground> grounds) {
        this.context = context;
        this.grounds = grounds;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ground, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Ground ground = grounds.get(position);
        holder.name.setText(ground.name);
        holder.location.setText(ground.location);
        holder.type.setText(ground.type);
        holder.price.setText("₹" + ground.price);
        Glide.with(context).load(ground.image_url).into(holder.image);
        
        holder.bookBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, BookingActivity.class);
            intent.putExtra("ground", ground);
            context.startActivity(intent);
        });
        
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, GroundDetailActivity.class);
            intent.putExtra("ground", ground);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return grounds.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, location, type, price;
        ImageView image;
        Button bookBtn;
        
        ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.groundName);
            location = view.findViewById(R.id.groundLocation);
            type = view.findViewById(R.id.groundType);
            price = view.findViewById(R.id.groundPrice);
            image = view.findViewById(R.id.groundImage);
            bookBtn = view.findViewById(R.id.bookBtn);
        }
    }
}

