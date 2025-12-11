package com.example.groundbookingsystem.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groundbookingsystem.R;
import com.example.groundbookingsystem.models.Feedback;
import com.example.groundbookingsystem.utils.StarRatingHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.ViewHolder> {
    private List<Feedback> feedbacks;

    public FeedbackAdapter(List<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_feedback, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Feedback feedback = feedbacks.get(position);
        
        // Set user name
        String userName = "Anonymous";
        if (feedback.users != null && feedback.users.name != null) {
            userName = feedback.users.name;
            // Show only first name for privacy
            if (userName.contains(" ")) {
                userName = userName.split(" ")[0];
            }
        }
        holder.userNameText.setText(userName);
        
        // Display stars
        StarRatingHelper.displayStarsSimple(holder.starsContainer, feedback.rating);
        
        // Set comment
        if (feedback.comment != null && !feedback.comment.trim().isEmpty()) {
            holder.commentText.setText(feedback.comment);
            holder.commentText.setVisibility(View.VISIBLE);
        } else {
            holder.commentText.setVisibility(View.GONE);
        }
        
        // Set date
        holder.dateText.setText(formatDate(feedback.created_at));
    }

    @Override
    public int getItemCount() {
        return feedbacks != null ? feedbacks.size() : 0;
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return "Recently";
        }
        
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (ParseException e) {
            // Try alternative format
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                if (date != null) {
                    return outputFormat.format(date);
                }
            } catch (ParseException ex) {
                // Return original if parsing fails
            }
        }
        return dateString;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userNameText;
        LinearLayout starsContainer;
        TextView commentText;
        TextView dateText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.feedbackUserName);
            starsContainer = itemView.findViewById(R.id.feedbackStarsContainer);
            commentText = itemView.findViewById(R.id.feedbackComment);
            dateText = itemView.findViewById(R.id.feedbackDate);
        }
    }
}

