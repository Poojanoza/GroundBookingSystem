package com.example.groundbookingsystem.utils;

import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StarRatingHelper {
    
    /**
     * Displays star rating using emoji stars (no drawable resources needed)
     * @param container The LinearLayout container to add stars to
     * @param rating The rating value (0-5)
     */
    public static void displayStarsSimple(LinearLayout container, float rating) {
        container.removeAllViews();
        
        int fullStars = (int) rating;
        boolean hasHalfStar = (rating - fullStars) >= 0.5f;
        
        Context context = container.getContext();
        int starSize = (int) (20 * context.getResources().getDisplayMetrics().density);
        
        // Add full stars (⭐)
        for (int i = 0; i < fullStars; i++) {
            container.addView(createStarText(context, "⭐", starSize));
        }
        
        // Add half star (✨) if needed
        if (hasHalfStar) {
            container.addView(createStarText(context, "✨", starSize));
        }
        
        // Add empty stars (☆)
        int emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
        for (int i = 0; i < emptyStars; i++) {
            container.addView(createStarText(context, "☆", starSize));
        }
    }
    
    private static TextView createStarText(Context context, String star, int size) {
        TextView starView = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(0, 0, (int) (4 * context.getResources().getDisplayMetrics().density), 0);
        starView.setLayoutParams(params);
        starView.setText(star);
        starView.setTextSize(16);
        starView.setGravity(Gravity.CENTER);
        return starView;
    }
}

